package companyz;

import companyz.model.Employee;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// Contract for the data layer. Keeps Main decoupled from SQL entirely.
public interface EmployeeRepository {

    int insertEmployee(Employee employee) throws SQLException;

    boolean updateEmployee(Employee employee) throws SQLException;

    boolean deleteEmployee(int empId) throws SQLException;

    List<Employee> searchByName(String name) throws SQLException;

    Optional<Employee> searchBySsn(String ssn) throws SQLException;

    Optional<Employee> searchByEmpId(int empId) throws SQLException;
}
