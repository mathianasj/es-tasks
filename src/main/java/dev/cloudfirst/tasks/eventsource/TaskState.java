package dev.cloudfirst.tasks.eventsource;

public class TaskState {
    public String taskId;
    public String personId;
    public Boolean completed = false;

    @Override
    public String toString() {
        return "TaskState [taskId=" + taskId + ", personId=" + personId + ", completed=" + completed + "]";
    }
}
