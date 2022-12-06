package dev.cloudfirst.tasks.funqy.cloudevent;

public class TaskCreated {
    public String personId;
    public String taskId;
    public String task;
    public TaskType taskType;
    
    @Override
    public String toString() {
        return "TaskCreated [personId=" + personId + ", taskId=" + taskId + ", task=" + task + ", taskType=" + taskType + "]";
    }
}
