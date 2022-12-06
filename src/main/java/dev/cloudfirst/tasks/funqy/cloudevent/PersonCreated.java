package dev.cloudfirst.tasks.funqy.cloudevent;

public class PersonCreated {
    public String id;
    public String firstName;
    public String lastName;
    @Override
    public String toString() {
        return "PersonCreated [firstName=" + firstName + ", lastName=" + lastName + "]";
    }
}
