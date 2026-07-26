package companyz;

import companyz.model.Division;
import companyz.model.Employee;
import companyz.model.JobTitle;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Scanner;

public class Main {

    // Reads connection details from db.properties (gitignored — copy db.properties.example
    // to db.properties and fill in the values shared in the team chat).
    private static Properties loadDbProperties() throws IOException {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("db.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new IOException("Missing db.properties — copy db.properties.example to " +
                    "db.properties and fill in the connection details shared in the team chat.", e);
        }
        return props;
    }

    public static void main(String[] args) {
        System.out.println("Initializing MySQL database interface connection...");

        Properties dbProps;
        try {
            dbProps = loadDbProperties();
        } catch (IOException e) {
            System.err.println("[FATAL ERROR] " + e.getMessage());
            return;
        }

        String dbUrl = dbProps.getProperty("db.url");
        String dbUser = dbProps.getProperty("db.user");
        String dbPass = dbProps.getProperty("db.password");

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
            EmployeeRepository repo = new MySQLEmployeeRepository(conn);

            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                System.out.println("\n==============================================");
                System.out.println("     COMPANY - EMPLOYEE MANAGEMENT SYSTEM    ");
                System.out.println("==============================================");
                System.out.println("1. Insert New Employee");
                System.out.println("2. Search Employee (Name, SSN, or ID)");
                System.out.println("3. Update Employee Information");
                System.out.println("4. Execute Batch Range Salary Increase (3.2%)");
                System.out.println("5. Generate Division Payroll Report");
                System.out.println("6. Exit System");
                System.out.print("Please enter choice: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume remaining buffer

                switch (choice) {
                    case 1: {
                        System.out.print("Enter Name: ");
                        String name = scanner.nextLine();

                        System.out.print("Enter 9-Digit SSN: ");
                        String ssn = scanner.nextLine();

                        System.out.print("Enter Salary: ");
                        double salary = Double.parseDouble(scanner.nextLine());

                        JobTitle jobTitle = promptEnum(scanner, "Job Title", JobTitle.values());
                        Division division = promptEnum(scanner, "Division", Division.values());

                        Employee newEmployee = new Employee(0, name, ssn, salary, jobTitle, division);
                        int newId = repo.insertEmployee(newEmployee);
                        System.out.println("[SUCCESS] Employee inserted with ID " + newId + ".");
                        break;
                    }

                    case 2:
                        System.out.print("Enter search term: ");
                        String lookup = scanner.nextLine();
                        List<Employee> results = search(repo, lookup);
                        if (results.isEmpty()) {
                            System.out.println("No matching records found.");
                        } else {
                            for (Employee emp : results) {
                                System.out.printf("ID: %d | Name: %s | SSN: %s | Title: %s | Div: %s | Salary: $%,.2f%n",
                                        emp.getEmpId(), emp.getName(), emp.getSsn(), emp.getJobTitle(), emp.getDivision(), emp.getSalary());
                            }
                        }
                        break;

                    case 3:
                        System.out.print("Enter Employee ID to update: ");
                        int targetId = scanner.nextInt();
                        scanner.nextLine();

                        List<Employee> select = search(repo, String.valueOf(targetId));
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

                    case 4:
                        System.out.println("Configuring 3.2% increase parameter for salaries between $58,000 and $105,000...");
                        int rows = repo.updateSalaryByPercent(3.2, 58000.00, 105000.00);
                        System.out.println("[SUCCESS] Calculation applied. " + rows + " employees updated.");
                        break;

                    case 5:
                        System.out.print("Enter report month (YYYY-MM): ");
                        YearMonth month = YearMonth.parse(scanner.nextLine());
                        Map<String, Double> report = repo.getTotalPayByDivision(month);

                        if (report.isEmpty()) {
                        System.out.println("No payroll data found.");
                        } else {
                            for (Map.Entry<String, Double> entry : report.entrySet()) {
                                System.out.printf("%s: $%,.2f%n", entry.getKey(), entry.getValue());
                            }
                        }
                        break;
                        
                    case 6:
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

    // Prompts until the user picks one of the given enum values by number.
    private static <T extends Enum<T>> T promptEnum(Scanner scanner, String label, T[] values) {
        while (true) {
            System.out.println("Choose " + label + ":");
            for (int i = 0; i < values.length; i++) {
                System.out.println("  " + (i + 1) + ". " + values[i].name());
            }
            System.out.print("Enter number: ");
            String input = scanner.nextLine();
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < values.length) {
                    return values[index];
                }
            } catch (NumberFormatException ignored) {
                // fall through to retry
            }
            System.out.println("Invalid choice, try again.");
        }
    }

    // Tries empid, then SSN (9 digits), then falls back to a name search.
    private static List<Employee> search(EmployeeRepository repo, String lookup) throws SQLException {
        try {
            int empId = Integer.parseInt(lookup);
            Optional<Employee> byId = repo.searchByEmpId(empId);
            if (byId.isPresent()) {
                return List.of(byId.get());
            }
        } catch (NumberFormatException ignored) {
            // not a numeric empid
        }

        if (lookup.matches("\\d{9}")) {
            Optional<Employee> bySsn = repo.searchBySsn(lookup);
            return bySsn.map(List::of).orElseGet(List::of);
        }

        return repo.searchByName(lookup);
    }
}
