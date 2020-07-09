package sample.model;

public class Contact
{
    private String firstName;
    private String lastName;
    private int contactNumber;
    boolean online = false;

    public Contact () {}
    public Contact (String firstName, String lastName, int contactNumber)
    {
        setFirstName(firstName);
        setLastName(lastName);
        setContactNumber(contactNumber);
    }

    public void setFirstName (String firstName) { this.firstName = firstName; }
    public void setLastName (String lastName) { this.lastName = lastName; }
    public void setContactNumber (int contactNumber) { this.contactNumber = contactNumber; }
    public void setOnline (boolean online) { this.online = online; }

    public String getFirstName () { return this.firstName; }
    public String getLastName () { return this.lastName; }
    public int getContactNumber () { return this.contactNumber; }
    public boolean isOnline () { return this.online; }

    @Override
    public String toString ()
    {
        return String.format ("%s@%d", firstName, contactNumber);
    }

    public static Contact parser (String personalInfo)
    {
        Contact newPerson = new Contact ();

        // can split with multiple delimiters using 'Regex' or individual delimiters and '|' notation
        String [] elements = personalInfo.split("@| ");

        newPerson.setFirstName(elements[0]);
        newPerson.setLastName(elements[1]);
        newPerson.setContactNumber(Integer.parseInt(elements[2]));

        newPerson.setOnline(true);

        return newPerson;
    }
}
