package companyz;

public class Employee {
    private int empId;
    private String name;
    private String ssn; // Digit-only string (no dashes)
    private double salary;
    private String jobTitle;
    private String division;

    public Employee(int empId, String name, String ssn, double salary, String jobTitle, String division) {
        this.empId = empId;
        this.name = name;
        this.ssn = ssn;
        this.salary = salary;
        this.jobTitle = jobTitle;
        this.division = division;
    }

    // Getters
    public int getEmpId() { return empId; }
    public String getName() { return name; }
    public String getSsn() { return ssn; }
    public double getSalary() { return salary; }
    public String getJobTitle() { return jobTitle; }
    public String getDivision() { return division; }

    // Setters
    public void setEmpId(int empId) { this.empId = empId; }
    public void setName(String name) { this.name = name; }
    public void setSsn(String ssn) { this.ssn = ssn; }
    public void setSalary(double salary) { this.salary = salary; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public void setDivision(String division) { this.division = division; }
}