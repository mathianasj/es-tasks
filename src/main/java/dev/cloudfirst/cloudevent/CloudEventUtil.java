package dev.cloudfirst.cloudevent;

import java.net.URI;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.funqy.knative.events.CloudEvent;
import io.quarkus.funqy.knative.events.CloudEventBuilder;
import io.quarkus.reactivemessaging.http.runtime.OutgoingHttpMetadata;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.ce.CloudEventMetadata;
import io.smallrye.reactive.messaging.ce.DefaultCloudEventMetadataBuilder;
import io.smallrye.reactive.messaging.ce.OutgoingCloudEventMetadata;
import io.smallrye.reactive.messaging.ce.OutgoingCloudEventMetadataBuilder;
import io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage;

@ApplicationScoped
public class CloudEventUtil {
    @Inject
    ObjectMapper mapper;

    @ConfigProperty(name = "service.source")
    String serviceSource;

    @Channel("default-broker")
    Emitter<Object> cloudEventEmitter;

    public <T> CloudEvent<T> CreateCloudEvent(T record, String id) {
        return CloudEventBuilder.create()
            .source(serviceSource)
            .type(record.getClass().getName())
            .id(UUID.randomUUID().toString())
            .extensions(Collections.singletonMap("partitionkey", id))
            .build(record);
    }

    public Uni<Void> sendEvent(Object record, String id) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // Make it a cloud event
        OutgoingCloudEventMetadata<Object> cloudEventMetadata = new OutgoingCloudEventMetadataBuilder<Object>()
            .withSource(URI.create(serviceSource))
            .withType(record.getClass().getName())
            .withExtension("partitionkey", id)
            .build();
 
        // Make sure we have the right content type header
        OutgoingHttpMetadata httpMetaData = new OutgoingHttpMetadata.Builder().addHeader("content-type", MediaType.APPLICATION_JSON).build();

        // Setup the message with ack / nack to be passed on as reactive
        Message<Object> msg = ContextAwareMessage.of(record)
        .addMetadata(httpMetaData)
        .addMetadata(cloudEventMetadata)
        .withAck(() -> {
            System.out.println("got an complete");
            future.complete(null);
            return CompletableFuture.completedFuture(null);
        }).withNack(reason -> {
            System.out.println("got an error");
            future.completeExceptionally(reason);
            return CompletableFuture.completedFuture(null);
        });

        // emit the message
        cloudEventEmitter.send(msg);

        // return the future so the chain can be monitored
        return Uni.createFrom().completionStage(future);
    }

}