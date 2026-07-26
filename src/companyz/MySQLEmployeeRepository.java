package companyz;

import companyz.model.Division;
import companyz.model.Employee;
import companyz.model.JobTitle;
import companyz.model.PayrollRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.YearMonth;

public class MySQLEmployeeRepository implements EmployeeRepository {

    private final Connection connection;

    public MySQLEmployeeRepository(Connection connection) throws SQLException {
        this.connection = connection;
        ensureSchema();
    }

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

    @Override
    public int applyRangeRaise(double percentage, double minimum, double maximum) throws SQLException {
        return updateSalaryByPercent(percentage, minimum, maximum);
    }

    @Override
    public int updateSalaryByPercent(double percentage, double minSalary, double maxSalary) throws SQLException {
        String sql = "UPDATE employee SET salary = salary * (1 + (? / 100.0)) WHERE salary >= ? AND salary < ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, percentage);
            stmt.setDouble(2, minSalary);
            stmt.setDouble(3, maxSalary);
            return stmt.executeUpdate();
        }
    }

    @Override
    public List<PayrollRecord> getPayHistory(int empId) throws SQLException {
        String sql = "SELECT payID, earnings, pay_date FROM payroll WHERE empid = ? ORDER BY pay_date";
        List<PayrollRecord> records = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, empId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PayrollRecord record = new PayrollRecord(
                        rs.getInt("payID"),
                        rs.getDouble("earnings"),
                        rs.getDate("pay_date").toLocalDate()
                    );
                    records.add(record);
                }
            }
        }
        return records;
    }

    @Override
    public Map<String, Double> getTotalPayByJobTitle(YearMonth month) throws SQLException {
        String sql = "SELECT jt.job_title, SUM(p.earnings) AS total_pay " +
                "FROM payroll p " +
                "JOIN employee_job_titles ejt ON p.empid = ejt.empid " +
                "JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id " +
                "WHERE p.pay_date >= ? AND p.pay_date < ? " +
                "GROUP BY jt.job_title ORDER BY jt.job_title";
        Map<String, Double> totals = new HashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(month.atDay(1)));
            stmt.setDate(2, java.sql.Date.valueOf(month.plusMonths(1).atDay(1)));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    totals.put(rs.getString("job_title"), rs.getDouble("total_pay"));
                }
            }
        }
        return totals;
    }

    @Override
    public Map<String, Double> getTotalPayByDivision(YearMonth month) throws SQLException {
        String sql = "SELECT d.Name AS division_name, SUM(p.earnings) AS total_pay " +
                "FROM payroll p " +
                "JOIN employee_division ed ON p.empid = ed.empid " +
                "JOIN division d ON ed.div_ID = d.ID " +
                "WHERE p.pay_date >= ? AND p.pay_date < ? " +
                "GROUP BY d.ID, d.Name ORDER BY d.Name";
        Map<String, Double> totals = new HashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(month.atDay(1)));
            stmt.setDate(2, java.sql.Date.valueOf(month.plusMonths(1).atDay(1)));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    totals.put(rs.getString("division_name"), rs.getDouble("total_pay"));
                }
            }
        }
        return totals;
    }
}
