package dev.cloudfirst.tasks.funqy.cloudevent;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import dev.cloudfirst.tasks.eventsource.NewPerson;
import dev.cloudfirst.tasks.eventsource.PersonState;
import dev.cloudfirst.tasks.eventsource.PersonTask;
import dev.cloudfirst.tasks.eventsource.TaskState;

@Mapper(componentModel = "cdi")
public interface EventMapper {
    PersonCreated toPersonCreated(NewPerson newPerson, String id);

    EmailUpdated toEmailUpdated(NewPerson newPerson, String id);

    void mergePersonState(PersonCreated personCreated, @MappingTarget PersonState personState);

    void mergePersonState(EmailUpdated emailUpdated, @MappingTarget PersonState personState);

    void mergeTaskState(CreateTask createTask, @MappingTarget TaskState taskState);

    TaskCreated toTaskCreated(TaskState taskState);

    PersonTask tPersonTask(TaskCreated taskCreated);
}
