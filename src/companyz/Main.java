package companyz;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    // ADJUST THESE TO MATCH YOUR LOCAL DATABASE SETTINGS
    private static final String DB_URL = "jdbc:mysql://localhost:3306/employeeData";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Kalki@455";

    public static void main(String[] args) {
        System.out.println("Initializing MySQL database interface connection...");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            MySQLEmployeeRepository repo = new MySQLEmployeeRepository(conn);

            // Runs automatic table scan to verify ssn column exists
            repo.verifyDatabaseStructure();

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n==============================================");
                System.out.println("     COMPANY - EMPLOYEE MANAGEMENT SYSTEM    ");
                System.out.println("==============================================");
                System.out.println("1. Search Employee (Name, SSN, or ID)");
                System.out.println("2. Update Employee Information");
                System.out.println("3. Execute Batch Range Salary Increase (3.2%)");
                System.out.println("4. Generate Division Payroll Report");
                System.out.println("5. Exit System");
                System.out.print("Please enter choice: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume remaining buffer

                switch (choice) {
                    case 1:
                        System.out.print("Enter search term: ");
                        String lookup = scanner.nextLine();
                        List<Employee> results = repo.searchEmployee(lookup);
                        if (results.isEmpty()) {
                            System.out.println("No matching records found.");
                        } else {
                            for (Employee emp : results) {
                                System.out.printf("ID: %d | Name: %s | SSN: %s | Title: %s | Div: %s | Salary: $%,.2f%n",
                                        emp.getEmpId(), emp.getName(), emp.getSsn(), emp.getJobTitle(), emp.getDivision(), emp.getSalary());
                            }
                        }
                        break;

                    case 2:
                        System.out.print("Enter Employee ID to update: ");
                        int targetId = scanner.nextInt();
                        scanner.nextLine();

                        List<Employee> select = repo.searchEmployee(String.valueOf(targetId));
                        if (select.isEmpty()) {
                            System.out.println("Employee profile not found.");
                            break;
                        }
                        Employee activeEmp = select.get(0);

                        System.out.print("Enter New Name [" + activeEmp.getName() + "]: ");
                        String newName = scanner.nextLine();
                        if (!newName.isBlank()) activeEmp.setName(newName);

                        System.out.print("Enter New 9-Digit SSN [" + activeEmp.getSsn() + "]: ");
                        String newSsn = scanner.nextLine();
                        if (!newSsn.isBlank()) activeEmp.setSsn(newSsn);

                        System.out.print("Enter New Salary [" + activeEmp.getSalary() + "]: ");
                        String newSalaryStr = scanner.nextLine();
                        if (!newSalaryStr.isBlank()) {
                            activeEmp.setSalary(Double.parseDouble(newSalaryStr));
                        }

                        if (repo.updateEmployee(activeEmp)) {
                            System.out.println("[SUCCESS] Employee profile updated successfully.");
                        } else {
                            System.out.println("[FAILURE] Update rejected by database.");
                        }
                        break;

                    case 3:
                        System.out.println("Configuring 3.2% increase parameter for salaries between $58,000 and $105,000...");
                        int rows = repo.applyRangeRaise(0.032, 58000.00, 105000.00);
                        System.out.println("[SUCCESS] Calculation applied. " + rows + " employees updated.");
                        break;

                    case 4:
                        System.out.print("Enter reporting start date (YYYY-MM-DD): ");
                        String start = scanner.nextLine();
                        System.out.print("Enter reporting end date (YYYY-MM-DD): ");
                        String end = scanner.nextLine();
                        repo.printDivisionPayReport(start, end);
                        break;

                    case 5:
                        exit = true;
                        System.out.println("Database session closed. Goodbye!");
                        break;

                    default:
                        System.out.println("Invalid selection. Try again.");
                }
            }
            scanner.close();
        } catch (SQLException e) {
            System.err.println("[FATAL ERROR] Cannot establish active connection: " + e.getMessage());
        }
    }
}
