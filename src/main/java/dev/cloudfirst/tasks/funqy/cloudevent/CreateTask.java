package dev.cloudfirst.tasks.funqy.cloudevent;

public class CreateTask {
    public String personId;
    public String taskId;
    public String task;
    public TaskType taskType;
    
    @Override
    public String toString() {
        return "CreateTask [personId=" + personId + ", taskId=" + taskId + ", task=" + task + ", taskType=" + taskType + "]";
    }
}
