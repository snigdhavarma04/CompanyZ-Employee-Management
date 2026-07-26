package companyz;

import companyz.model.Employee;
import companyz.model.PayrollRecord;

import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Contract for the data layer. Keeps Main and the GUI decoupled from SQL.
public interface EmployeeRepository {

    int insertEmployee(Employee employee) throws SQLException;
    
    int applyRangeRaise(double percentage, double minimum, double maximum) throws SQLException;

    boolean updateEmployee(Employee employee) throws SQLException;

    boolean deleteEmployee(int empId) throws SQLException;

    List<Employee> searchByName(String name) throws SQLException;

    Optional<Employee> searchBySsn(String ssn) throws SQLException;

    Optional<Employee> searchByEmpId(int empId) throws SQLException;

    // Maia: salary and reporting features

    int updateSalaryByPercent(
            double percentage,
            double minSalary,
            double maxSalary) throws SQLException;

    List<PayrollRecord> getPayHistory(int empId) throws SQLException;

    Map<String, Double> getTotalPayByJobTitle(
            YearMonth month) throws SQLException;

    Map<String, Double> getTotalPayByDivision(
            YearMonth month) throws SQLException;
}