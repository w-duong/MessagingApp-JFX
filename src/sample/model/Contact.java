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

        String [] fullArray = personalInfo.split("@");
        String [] nameArray = fullArray[0].split(" ");

        newPerson.setContactNumber(Integer.parseInt(fullArray[1]));
        newPerson.setFirstName(nameArray[0]);
        newPerson.setLastName(nameArray[1]);
        newPerson.setOnline(true);

        return newPerson;
    }
}
