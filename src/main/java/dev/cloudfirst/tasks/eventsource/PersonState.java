package dev.cloudfirst.tasks.eventsource;

public class PersonState {
	public String personId;
    public String firstName;
    public String lastName;
    public String emailAddress;
	public Boolean welcomeEmailSent = false;
	public Float ledgerBalance = 0.0f;

	@Override
	public String toString() {
		return "PersonState [personId=" + personId + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", emailAddress=" + emailAddress + ", welcomeEmailSent=" + welcomeEmailSent + ", ledgerBalance="
				+ ledgerBalance + "]";
	}
}
