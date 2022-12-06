package dev.cloudfirst.tasks.eventsource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dev.cloudfirst.cloudevent.CloudEventUtil;
import dev.cloudfirst.tasks.funqy.cloudevent.EventMapper;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.smallrye.mutiny.CompositeException;
import io.smallrye.mutiny.Uni;

@Path("/tasks")
public class TasksResource {
    private ValueCommands<String, PersonState> personStateCommands;
    private ValueCommands<String, TaskState> taskStateCommands;

    public TasksResource(RedisDataSource ds) {
        personStateCommands = ds.value(PersonState.class);
    }

    @Inject
    EventMapper eventMapper;
    @Inject
    CloudEventUtil cloudEventUtil;
    private static final Logger LOGGER = Logger.getLogger(TasksResource.class.getName());

    @GET
    @Path("/person/{personId}")
    public Uni<List<PersonTask>> listTasksForPerson(@PathParam("personId") String personId) {
        return PersonTask.findByPersonId(personId);
    }

    // @DELETE
    // @Path("/{key}")
    // public Uni<Void> deleteState(String key) {
    //     return Uni.createFrom().item(personState.remove(key)).onItem().transform(e -> null);
    // }

    // @GET
    // public Uni<Object> listPersonState() {
    //         return Uni.createFrom().item(personState.keySet()).onItem().transform(
    //             e -> {
    //                 System.out.println(e);
    //                 return personState.getAll(e);
    //             }
    //         ).onItem().transform(e -> {
    //             System.out.println(e);

    //             return e;
    //         });
    // }

    @POST
    public Uni<Void> createPerson(NewPerson newPerson) {
        String personId = UUID.randomUUID().toString();
        return cloudEventUtil.sendEvent(eventMapper.toPersonCreated(newPerson, personId), personId)
            .onItem().transformToUni(e -> cloudEventUtil.sendEvent(eventMapper.toEmailUpdated(newPerson, personId), personId))
            .onFailure().invoke(e -> {
                System.out.println("found an error");
                System.err.println(e);
            });
            
    }

    @Provider
    public static class ErrorMapper implements ExceptionMapper<Exception> {

        @Inject
        ObjectMapper objectMapper;

        @Override
        public Response toResponse(Exception exception) {
            LOGGER.error("Failed to handle request", exception);

            Throwable throwable = exception;

            int code = 500;
            if (throwable instanceof WebApplicationException) {
                code = ((WebApplicationException) exception).getResponse().getStatus();
            }

            // This is a Mutiny exception and it happens, for example, when we try to insert a new
            // fruit but the name is already in the database
            if (throwable instanceof CompositeException) {
                throwable = ((CompositeException) throwable).getCause();
            }

            ObjectNode exceptionJson = objectMapper.createObjectNode();
            exceptionJson.put("exceptionType", throwable.getClass().getName());
            exceptionJson.put("code", code);

            if (exception.getMessage() != null) {
                exceptionJson.put("error", throwable.getMessage());
            }

            return Response.status(code)
                    .entity(exceptionJson)
                    .build();
        }

    }
}
