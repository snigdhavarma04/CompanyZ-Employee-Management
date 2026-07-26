package companyz.model;

public abstract class Person {
    private String name;
    private String ssn; // Formatted without dashes per requirements

    public Person(String name, String ssn) {
        this.name = name;
        this.ssn = ssn;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSsn() { return ssn; }
    public void setSsn(String ssn) { this.ssn = ssn; }
}