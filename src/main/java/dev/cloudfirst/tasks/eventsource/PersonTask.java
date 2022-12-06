package dev.cloudfirst.tasks.eventsource;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import dev.cloudfirst.tasks.funqy.cloudevent.TaskType;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;

@Entity
@Table
@IdClass(PersonTaskPK.class)
public class PersonTask extends PanacheEntityBase {
    @Id
    public String taskId;
    @Id
    public String personId;
    public String task;
    public TaskType taskType;
    public Boolean completed = false;

    public static Uni<List<PersonTask>> findByPersonId(String personId) {
        return list("personId", personId);
    }
}
