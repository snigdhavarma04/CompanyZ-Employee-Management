package companyz.model;

import java.util.ArrayList;
import java.util.List;

public class Employee extends Person implements Payable {
    private int employeeId;
    private double salary;
    private JobTitle jobTitle;
    private Division division;
    private List<PayrollRecord> history;

    public Employee(int employeeId, String name, String ssn, double salary, JobTitle jobTitle, Division division) {
        super(name, ssn);
        this.employeeId = employeeId;
        this.salary = salary;
        this.jobTitle = jobTitle;
        this.division = division;
        this.history = new ArrayList<>();
    }

    @Override
    public double calculatePay() {
        return this.salary / 12.0;
    }

    public void addPayrollRecord(PayrollRecord record) {
        this.history.add(record);
    }

    public List<PayrollRecord> getHistory() {
        return new ArrayList<>(this.history);
    }

    // Standard getter
    public int getEmployeeId() {
        return employeeId;
    }

    // Bridge method for backward compatibility with existing Main.java calls
    public int getEmpId() {
        return this.employeeId;
    }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }
    public JobTitle getJobTitle() { return jobTitle; }
    public void setJobTitle(JobTitle jobTitle) { this.jobTitle = jobTitle; }
    public Division getDivision() { return division; }
    public void setDivision(Division division) { this.division = division; }
}