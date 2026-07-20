package companyz;

import companyz.model.Division;
import companyz.model.Employee;
import companyz.model.JobTitle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLEmployeeRepository implements EmployeeRepository {

    private final Connection connection;

    public MySQLEmployeeRepository(Connection connection) throws SQLException {
        this.connection = connection;
        ensureSchema();
    }

    // Creates the employee table if it doesn't exist yet. No-op if it's already there.
    private void ensureSchema() throws SQLException {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS employee (" +
                "empid INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(100) NOT NULL, " +
                "ssn VARCHAR(9) NOT NULL, " +
                "salary DECIMAL(10,2) NOT NULL, " +
                "job_title VARCHAR(50) NOT NULL, " +
                "division VARCHAR(50) NOT NULL" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createTableQuery);
        }
    }

    @Override
    public int insertEmployee(Employee employee) throws SQLException {
        String sql = "INSERT INTO employee (name, ssn, salary, job_title, division) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, employee.getName());
            stmt.setString(2, employee.getSsn());
            stmt.setDouble(3, employee.getSalary());
            stmt.setString(4, employee.getJobTitle().name());
            stmt.setString(5, employee.getDivision().name());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Insert succeeded but no generated empid was returned.");
                }
                return keys.getInt(1);
            }
        }
    }

    @Override
    public boolean updateEmployee(Employee employee) throws SQLException {
        String sql = "UPDATE employee SET name = ?, ssn = ?, salary = ?, job_title = ?, division = ? WHERE empid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employee.getName());
            stmt.setString(2, employee.getSsn());
            stmt.setDouble(3, employee.getSalary());
            stmt.setString(4, employee.getJobTitle().name());
            stmt.setString(5, employee.getDivision().name());
            stmt.setInt(6, employee.getEmpId());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteEmployee(int empId) throws SQLException {
        String sql = "DELETE FROM employee WHERE empid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, empId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public List<Employee> searchByName(String name) throws SQLException {
        String sql = "SELECT * FROM employee WHERE name LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + name + "%");
            List<Employee> results = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToEmployee(rs));
                }
            }
            return results;
        }
    }

    @Override
    public Optional<Employee> searchBySsn(String ssn) throws SQLException {
        String sql = "SELECT * FROM employee WHERE ssn = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ssn);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRowToEmployee(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Optional<Employee> searchByEmpId(int empId) throws SQLException {
        String sql = "SELECT * FROM employee WHERE empid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, empId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRowToEmployee(rs)) : Optional.empty();
            }
        }
    }

    private Employee mapRowToEmployee(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getInt("empid"),
                rs.getString("name"),
                rs.getString("ssn"),
                rs.getDouble("salary"),
                JobTitle.valueOf(rs.getString("job_title")),
                Division.valueOf(rs.getString("division"))
        );
    }

    // --- Maia: Part 2 (salary raise + reports) ---
    // Carried over as-is from the previous version of this file; not touched here.
    // Note: printDivisionPayReport() references a pay_statements table that doesn't exist
    // in this schema (only `employee`), so it will currently always report a DB error.

    public int applyRangeRaise(double percentage, double minSalary, double maxSalary) throws SQLException {
        String sql = "UPDATE employee SET salary = salary * (1 + ?) WHERE salary >= ? AND salary < ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, percentage);
            stmt.setDouble(2, minSalary);
            stmt.setDouble(3, maxSalary);
            return stmt.executeUpdate();
        }
    }

    public void printDivisionPayReport(String startDate, String endDate) {
        String sql = "SELECT e.division, SUM(p.gross_pay) AS total_payroll " +
                "FROM employee e INNER JOIN pay_statements p ON e.empid = p.empid " +
                "WHERE p.pay_date >= ? AND p.pay_date <= ? " +
                "GROUP BY e.division";

        System.out.println("\n===== DIVISION MONTHLY PAYROLL REPORT =====");
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
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
