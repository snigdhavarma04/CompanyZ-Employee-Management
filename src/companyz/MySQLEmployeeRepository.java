package companyz;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLEmployeeRepository {
    private final Connection dbConnection;

    public MySQLEmployeeRepository(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    // Task 1: Initialize table & dynamically apply SSN column if missing
    // Task 1: Dynamically initialize table & mock records if missing
    public void verifyDatabaseStructure() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS employee (" +
                "empid INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(100) NOT NULL, " +
                "ssn VARCHAR(9) NOT NULL, " +
                "salary DECIMAL(10,2) NOT NULL, " +
                "job_title VARCHAR(100) NOT NULL, " +
                "division VARCHAR(100) NOT NULL" +
                ")";

        String countQuery = "SELECT COUNT(*) FROM employee";

        String seedDataQuery = "INSERT INTO employee (name, ssn, salary, job_title, division) VALUES " +
                "('Alice Smith', '123456789', 60000.00, 'Software Engineer', 'Engineering'), " +
                "('Bob Jones', '987654321', 110000.00, 'Product Manager', 'Product'), " +
                "('Charlie Brown', '456789123', 75000.00, 'Data Analyst', 'Engineering')";

        try (Statement stmt = dbConnection.createStatement()) {
            // 1. Create the table if it doesn't exist yet
            stmt.executeUpdate(createTableQuery);
            System.out.println("[DB SETUP] 'employee' table verified/created successfully.");

            // 2. Check if the table is empty. If yes, seed it with mock test profiles
            try (ResultSet rs = stmt.executeQuery(countQuery)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate(seedDataQuery);
                    System.out.println("[DB SETUP] Mock employee dataset injected successfully.");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Auto-initialization structural check failed: " + e.getMessage());
        }
    }

    // Task 2: Search by Name, SSN, or unique Employee ID
    public List<Employee> searchEmployee(String lookup) {
        List<Employee> results = new ArrayList<>();
        String sql = "SELECT * FROM employee WHERE name LIKE ? OR ssn = ? OR empid = ?";

        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setString(1, "%" + lookup + "%");
            stmt.setString(2, lookup);

            int numericId = -1;
            try {
                numericId = Integer.parseInt(lookup);
            } catch (NumberFormatException ignored) {}
            stmt.setInt(3, numericId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new Employee(
                            rs.getInt("empid"),
                            rs.getString("name"),
                            rs.getString("ssn"),
                            rs.getDouble("salary"),
                            rs.getString("job_title"),
                            rs.getString("division")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Search failed: " + e.getMessage());
        }
        return results;
    }

    // Task 3: Save general employee updates
    public boolean updateEmployee(Employee emp) {
        String sql = "UPDATE employee SET name = ?, ssn = ?, salary = ?, job_title = ?, division = ? WHERE empid = ?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setString(1, emp.getName());
            stmt.setString(2, emp.getSsn());
            stmt.setDouble(3, emp.getSalary());
            stmt.setString(4, emp.getJobTitle());
            stmt.setString(5, emp.getDivision());
            stmt.setInt(6, emp.getEmpId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Update execution failed: " + e.getMessage());
            return false;
        }
    }

    // Task 4: Math equation-based range salary adjustment 
    public int applyRangeRaise(double percentage, double minSalary, double maxSalary) {
        String sql = "UPDATE employee SET salary = salary * (1 + ?) WHERE salary >= ? AND salary < ?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setDouble(1, percentage);
            stmt.setDouble(2, minSalary);
            stmt.setDouble(3, maxSalary);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Raise process interrupted: " + e.getMessage());
            return 0;
        }
    }

    // Task 5: Print aggregated Monthly Division Payroll Report
    public void printDivisionPayReport(String startDate, String endDate) {
        String sql = "SELECT e.division, SUM(p.gross_pay) AS total_payroll " +
                "FROM employee e INNER JOIN pay_statements p ON e.empid = p.empid " +
                "WHERE p.pay_date >= ? AND p.pay_date <= ? " +
                "GROUP BY e.division";

        System.out.println("\n===== DIVISION MONTHLY PAYROLL REPORT =====");
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("Division: %-15s | Total Outlay: $%,.2f%n",
                            rs.getString("division"), rs.getDouble("total_payroll"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Could not compile Division report: " + e.getMessage());
        }
    }
}