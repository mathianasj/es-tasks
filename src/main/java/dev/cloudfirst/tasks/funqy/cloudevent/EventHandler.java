package dev.cloudfirst.tasks.funqy.cloudevent;

import io.quarkus.funqy.Funq;
import io.quarkus.funqy.knative.events.CloudEventMapping;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.smallrye.mutiny.Uni;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.jboss.logging.Logger;

import dev.cloudfirst.cloudevent.CloudEventUtil;
import dev.cloudfirst.tasks.eventsource.PersonState;
import dev.cloudfirst.tasks.eventsource.PersonTask;
import dev.cloudfirst.tasks.eventsource.TaskState;

public class EventHandler {
    @Inject
    CloudEventUtil cloudEventUtil;
    
    private ValueCommands<String, PersonState> personState;
    private ValueCommands<String, TaskState> taskState;

    public EventHandler(RedisDataSource ds) {
        personState = ds.value(PersonState.class);
        taskState = ds.value(TaskState.class);
    }

    @Inject
    EventMapper eventMapper;
    
    private static final Logger log = Logger.getLogger(EventHandler.class);

    @Funq
    @CloudEventMapping(trigger = "dev.cloudfirst.demographics.funqy.cloudevent.EmailUpdated")
    public void handleEmailUpdated(EmailUpdated emailUpdated) {
        PersonState currentState = Optional.ofNullable(personState.get(emailUpdated.id)).orElse(new PersonState());
        eventMapper.mergePersonState(emailUpdated, currentState);

        // run logic
        // TODO should be a rules engine or something but for demo sufficient
        runRules(currentState);

        // update state
        personState.set(emailUpdated.id, currentState);
    }

    @Funq
    @CloudEventMapping(trigger = "dev.cloudfirst.demographics.funqy.cloudevent.PersonCreated")
    public void handlePersonCreated(PersonCreated personCreated) {
        PersonState currentState = Optional.ofNullable(personState.get(personCreated.id)).orElse(new PersonState());
        eventMapper.mergePersonState(personCreated, currentState);

        // run logic
        // TODO should be a rules engine or something but for demo sufficient
        runRules(currentState);

        // update state
        personState.set(personCreated.id, currentState);
    }

    @Funq
    @CloudEventMapping(trigger = "dev.cloudfirst.tasks.funqy.cloudevent.CreateTask")
    public void handleCreateTask(CreateTask createTask) {
        TaskState currentState = Optional.ofNullable(taskState.get(createTask.taskId)).orElse(new TaskState());
        eventMapper.mergeTaskState(createTask, currentState);

        // update state
        taskState.set(currentState.taskId, currentState);

        // Send task created
        cloudEventUtil.sendEvent(eventMapper.toTaskCreated(currentState), currentState.taskId);
    }

    @Funq
    @CloudEventMapping(trigger = "dev.cloudfirst.tasks.funqy.cloudevent.TaskCreated")
    public Uni<Void> handleTaskCreated(TaskCreated taskCreated) {
        // update person state that welcome message sent
        if (taskCreated.taskType == TaskType.WELCOME_MESSAGE) {
            PersonState currentState = Optional.ofNullable(personState.get(taskCreated.personId)).orElse(new PersonState());

            // set the message sent
            currentState.welcomeEmailSent = true;
            System.out.println("updated welcome sent for " + currentState);

            // update state
            personState.set(taskCreated.personId, currentState);
        }

        // save the views
        PersonTask personTask = eventMapper.tPersonTask(taskCreated);
        return Panache.withTransaction(() -> personTask.persist()).replaceWithVoid();
    }

    private void runRules(PersonState personState) {
        sendWelcomeEmail(personState);
    }

    private void sendWelcomeEmail(PersonState personState) {
        if(!personState.welcomeEmailSent && 
            personState.emailAddress != null &&
            personState.firstName != null &&
            personState.lastName != null) {
                // create the task to complete setting up your account
                CreateTask createTask = new CreateTask();
                createTask.personId = personState.personId;
                createTask.taskId = UUID.randomUUID().toString();
                createTask.task = "Complete account setup";

                // request to create task
                cloudEventUtil.sendEvent(createTask, createTask.taskId);

                System.out.println("sending welcome message to " + personState.firstName + " " + personState.lastName + " (" + personState.emailAddress + ")");
        }
    }
}
