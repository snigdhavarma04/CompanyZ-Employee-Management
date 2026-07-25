package companyz;

import companyz.model.Division;
import companyz.model.Employee;
import companyz.model.JobTitle;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.FileInputStream;
import java.net.URI;
import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class EmployeeGUI extends Application {

    private EmployeeRepository repo;
    private Connection connection;
    private ObservableList<Employee> sessionList = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Company Z - Employee Management System");

        try {
            Properties dbProps = new Properties();
            FileInputStream input = new FileInputStream("db.properties");
            dbProps.load(input);
            input.close();

            connection = DriverManager.getConnection(
                    dbProps.getProperty("db.url"),
                    dbProps.getProperty("db.user"),
                    dbProps.getProperty("db.password")
            );
            repo = new MySQLEmployeeRepository(connection);
            ensurePayStatementsTable();
        } catch (Exception ex) {
            showStartupError(primaryStage, ex);
            return;
        }

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                new Tab("Insert Employee", createInsertLayout()),
                new Tab("Search Employee", createSearchLayout()),
                new Tab("Update Employee", createUpdateLayout()),
                new Tab("Delete Employee", createDeleteLayout()),
                new Tab("Reports", createReportLayout()),
                new Tab("Team Info", createTeamLayout())
        );
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Scene scene = new Scene(tabPane, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private TableView<Employee> buildEmployeeTable(ObservableList<Employee> items) {
        TableView<Employee> table = new TableView<>(items);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Employee, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getEmpId()));

        TableColumn<Employee, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        TableColumn<Employee, String> ssnCol = new TableColumn<>("SSN");
        ssnCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSsn()));

        TableColumn<Employee, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getJobTitle() == null ? "" : data.getValue().getJobTitle().toString()
        ));

        TableColumn<Employee, String> divisionCol = new TableColumn<>("Division");
        divisionCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDivision() == null ? "" : data.getValue().getDivision().toString()
        ));

        TableColumn<Employee, Number> salaryCol = new TableColumn<>("Salary ($)");
        salaryCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getSalary()));

        table.getColumns().addAll(idCol, nameCol, ssnCol, titleCol, divisionCol, salaryCol);
        return table;
    }

    private VBox createInsertLayout() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        Label header = new Label("Insert New Employee");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        
        TextField ssnField = new TextField();
        ssnField.setPromptText("9-Digit SSN (no dashes)");
        
        TextField salaryField = new TextField();
        salaryField.setPromptText("Annual Salary (e.g. 65000)");

        ComboBox<JobTitle> titleBox = new ComboBox<>();
        titleBox.getItems().addAll(JobTitle.values());
        titleBox.setPromptText("Select Job Title");

        ComboBox<Division> divisionBox = new ComboBox<>();
        divisionBox.getItems().addAll(Division.values());
        divisionBox.setPromptText("Select Division");

        Button submitBtn = new Button("Insert Employee");
        Label statusLabel = new Label();
        TableView<Employee> employeeTable = buildEmployeeTable(sessionList);

        submitBtn.setOnAction(event -> {
            try {
                validateEmployeeInput(
                        nameField.getText(), ssnField.getText(), salaryField.getText(),
                        titleBox.getValue(), divisionBox.getValue()
                );
                
                Employee newEmployee = new Employee(
                        0,
                        nameField.getText().trim(),
                        ssnField.getText().trim(),
                        Double.parseDouble(salaryField.getText().trim()),
                        titleBox.getValue(),
                        divisionBox.getValue()
                );
                
                int newId = repo.insertEmployee(newEmployee);
                statusLabel.setText("Added employee ID: " + newId);
                
                sessionList.add(new Employee(
                        newId,
                        newEmployee.getName(),
                        newEmployee.getSsn(),
                        newEmployee.getSalary(),
                        newEmployee.getJobTitle(),
                        newEmployee.getDivision()
                ));
                
                nameField.clear();
                ssnField.clear();
                salaryField.clear();
                titleBox.setValue(null);
                divisionBox.setValue(null);
            } catch (Exception ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });

        layout.getChildren().addAll(
                header, nameField, ssnField, salaryField, titleBox, divisionBox,
                submitBtn, statusLabel,
                new Label("Recently Inserted Employees (This Session):"),
                employeeTable
        );
        return layout;
    }

    private VBox createSearchLayout() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        Label header = new Label("Search Employee");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField searchInput = new TextField();
        searchInput.setPromptText("Enter Name, 9-Digit SSN, or Employee ID");
        
        Button searchBtn = new Button("Search");
        Label statusLabel = new Label();
        ObservableList<Employee> searchResults = FXCollections.observableArrayList();
        TableView<Employee> searchTable = buildEmployeeTable(searchResults);

        searchBtn.setOnAction(event -> {
            String query = searchInput.getText().trim();
            if (query.isEmpty()) {
                statusLabel.setText("Please enter a name, SSN, or employee ID.");
                return;
            }

            try {
                searchResults.clear();
                statusLabel.setText("");
                
                if (query.matches("\\d+") && query.length() < 9) {
                    Optional<Employee> emp = repo.searchByEmpId(Integer.parseInt(query));
                    if (emp.isPresent()) {
                        searchResults.add(emp.get());
                    } else {
                        statusLabel.setText("No record found.");
                    }
                } else if (query.matches("\\d{9}")) {
                    Optional<Employee> emp = repo.searchBySsn(query);
                    if (emp.isPresent()) {
                        searchResults.add(emp.get());
                    } else {
                        statusLabel.setText("No record found.");
                    }
                } else {
                    List<Employee> employees = repo.searchByName(query);
                    if (employees.isEmpty()) {
                        statusLabel.setText("No record found.");
                    } else {
                        searchResults.addAll(employees);
                    }
                }
            } catch (Exception ex) {
                statusLabel.setText("Database error: " + ex.getMessage());
            }
        });

        layout.getChildren().addAll(
                header,
                new HBox(10, searchInput, searchBtn),
                statusLabel,
                searchTable
        );
        return layout;
    }

    private VBox createUpdateLayout() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        Label header = new Label("Update Employee Record");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField idInput = new TextField();
        idInput.setPromptText("Target Employee ID");
        TextField nameField = new TextField();
        nameField.setPromptText("New Name");
        TextField ssnField = new TextField();
        ssnField.setPromptText("New 9-Digit SSN");
        TextField salaryField = new TextField();
        salaryField.setPromptText("New Annual Salary");

        ComboBox<JobTitle> titleBox = new ComboBox<>();
        titleBox.getItems().addAll(JobTitle.values());
        titleBox.setPromptText("Select New Job Title");

        ComboBox<Division> divisionBox = new ComboBox<>();
        divisionBox.getItems().addAll(Division.values());
        divisionBox.setPromptText("Select New Division");

        Button updateBtn = new Button("Update Employee");
        Label statusLabel = new Label();

        updateBtn.setOnAction(event -> {
            try {
                int empId = Integer.parseInt(idInput.getText().trim());
                validateEmployeeInput(
                        nameField.getText(), ssnField.getText(), salaryField.getText(),
                        titleBox.getValue(), divisionBox.getValue()
                );

                Employee employee = new Employee(
                        empId,
                        nameField.getText().trim(),
                        ssnField.getText().trim(),
                        Double.parseDouble(salaryField.getText().trim()),
                        titleBox.getValue(),
                        divisionBox.getValue()
                );
                
                boolean success = repo.updateEmployee(employee);
                if (success) {
                    statusLabel.setText("Record updated.");
                } else {
                    statusLabel.setText("Employee ID not found.");
                }
            } catch (NumberFormatException ex) {
                statusLabel.setText("Error: Employee ID and salary must be numbers.");
            } catch (Exception ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });

        layout.getChildren().addAll(
                header,
                idInput, nameField, ssnField, salaryField, titleBox, divisionBox,
                updateBtn, statusLabel
        );
        return layout;
    }

    private VBox createDeleteLayout() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        Label header = new Label("Delete Employee Record");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField idInput = new TextField();
        idInput.setPromptText("Enter Employee ID to Delete");
        Button deleteBtn = new Button("Delete Employee");
        Label statusLabel = new Label();

        deleteBtn.setOnAction(event -> {
            try {
                int empId = Integer.parseInt(idInput.getText().trim());
                boolean success = repo.deleteEmployee(empId);
                if (success) {
                    statusLabel.setText("Employee deleted.");
                } else {
                    statusLabel.setText("Employee ID not found.");
                }
            } catch (NumberFormatException ex) {
                statusLabel.setText("Error: Please enter a numeric ID.");
            } catch (Exception ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });

        layout.getChildren().addAll(header, idInput, deleteBtn, statusLabel);
        return layout;
    }

    private VBox createReportLayout() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        ComboBox<Month> monthBox = new ComboBox<>();
        monthBox.getItems().addAll(Month.values());
        monthBox.setValue(LocalDate.now().getMonth());

        ComboBox<Integer> yearBox = new ComboBox<>();
        int currentYear = Year.now().getValue();
        for (int y = currentYear - 5; y <= currentYear + 1; y++) {
            yearBox.getItems().add(y);
        }
        yearBox.setValue(currentYear);

        HBox monthSelection = new HBox(
                10,
                new Label("Report month:"), monthBox,
                new Label("Year:"), yearBox
        );

        TextArea reportArea = new TextArea();
        reportArea.setEditable(false);
        reportArea.setWrapText(false);
        reportArea.setStyle("-fx-font-family: monospace;");
        VBox.setVgrow(reportArea, Priority.ALWAYS);

        Button employeeHistoryBtn = new Button("Employee Information & Pay History");
        Button jobTitleBtn = new Button("Monthly Pay by Job Title");
        Button divisionBtn = new Button("Monthly Pay by Division");
        HBox reportButtons = new HBox(10, employeeHistoryBtn, jobTitleBtn, divisionBtn);

        employeeHistoryBtn.setOnAction(event -> {
            try {
                YearMonth selected = selectedMonth(monthBox, yearBox);
                reportArea.setText(runEmployeeHistoryReport(selected));
            } catch (Exception ex) {
                reportArea.setText("Report Error: " + ex.getMessage());
            }
        });

        jobTitleBtn.setOnAction(event -> {
            try {
                YearMonth selected = selectedMonth(monthBox, yearBox);
                reportArea.setText(runGroupedReport("job_title", "TOTAL PAY BY JOB TITLE", selected));
            } catch (Exception ex) {
                reportArea.setText("Report Error: " + ex.getMessage());
            }
        });

        divisionBtn.setOnAction(event -> {
            try {
                YearMonth selected = selectedMonth(monthBox, yearBox);
                reportArea.setText(runGroupedReport("division", "TOTAL PAY BY DIVISION", selected));
            } catch (Exception ex) {
                reportArea.setText("Report Error: " + ex.getMessage());
            }
        });

        GridPane payEntry = new GridPane();
        payEntry.setHgap(10);
        payEntry.setVgap(8);
        TextField payrollEmpId = new TextField();
        payrollEmpId.setPromptText("Employee ID");
        TextField grossPay = new TextField();
        grossPay.setPromptText("Gross Pay");
        DatePicker payDate = new DatePicker(LocalDate.now());
        Button recordPayBtn = new Button("Record Pay Statement");
        Label payrollStatus = new Label();

        payEntry.add(new Label("Employee ID:"), 0, 0);
        payEntry.add(payrollEmpId, 1, 0);
        payEntry.add(new Label("Gross pay:"), 2, 0);
        payEntry.add(grossPay, 3, 0);
        payEntry.add(new Label("Pay date:"), 0, 1);
        payEntry.add(payDate, 1, 1);
        payEntry.add(recordPayBtn, 2, 1);
        payEntry.add(payrollStatus, 3, 1);

        recordPayBtn.setOnAction(event -> {
            try {
                int empId = Integer.parseInt(payrollEmpId.getText().trim());
                double pay = Double.parseDouble(grossPay.getText().trim());
                if (pay <= 0) {
                    throw new IllegalArgumentException("Gross pay must be positive.");
                }
                if (payDate.getValue() == null) {
                    throw new IllegalArgumentException("Please select a pay date.");
                }

                int recordId = insertPayrollRecord(empId, pay, payDate.getValue());
                payrollStatus.setText("Saved pay statement #" + recordId);
                payrollEmpId.clear();
                grossPay.clear();
            } catch (NumberFormatException ex) {
                payrollStatus.setText("ID and pay must be numeric.");
            } catch (Exception ex) {
                payrollStatus.setText("Error: " + ex.getMessage());
            }
        });

        TextField percentageField = new TextField("3.2");
        TextField minimumSalaryField = new TextField("58000");
        TextField maximumSalaryField = new TextField("105000");
        Button raiseBtn = new Button("Apply Salary Increase");
        Label raiseStatus = new Label();

        GridPane salaryRaise = new GridPane();
        salaryRaise.setHgap(10);
        salaryRaise.setVgap(8);
        salaryRaise.add(new Label("Raise percentage:"), 0, 0);
        salaryRaise.add(percentageField, 1, 0);
        salaryRaise.add(new Label("Minimum salary:"), 2, 0);
        salaryRaise.add(minimumSalaryField, 3, 0);
        salaryRaise.add(new Label("Maximum salary:"), 0, 1);
        salaryRaise.add(maximumSalaryField, 1, 1);
        salaryRaise.add(raiseBtn, 2, 1);
        salaryRaise.add(raiseStatus, 3, 1);

        raiseBtn.setOnAction(event -> {
            try {
                double percentage = Double.parseDouble(percentageField.getText().trim()) / 100.0;
                double minimum = Double.parseDouble(minimumSalaryField.getText().trim());
                double maximum = Double.parseDouble(maximumSalaryField.getText().trim());

                if (percentage <= 0 || minimum < 0 || maximum <= minimum) {
                    throw new IllegalArgumentException("Please check your raise values.");
                }

                int count = ((MySQLEmployeeRepository) repo).applyRangeRaise(percentage, minimum, maximum);
                raiseStatus.setText("Updated " + count + " employee(s).");
            } catch (NumberFormatException ex) {
                raiseStatus.setText("Error: All raise values must be numbers.");
            } catch (Exception ex) {
                raiseStatus.setText("Error: " + ex.getMessage());
            }
        });

        Label header1 = new Label("Payroll Reports");
        header1.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label header2 = new Label("Add a Pay Statement");
        header2.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label header3 = new Label("Salary Range Increase");
        header3.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        layout.getChildren().addAll(
                header1, monthSelection, reportButtons, reportArea,
                header2, payEntry,
                header3, salaryRaise
        );
        return layout;
    }

    private void ensurePayStatementsTable() throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS pay_statements (" +
                "statement_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "empid INT NOT NULL, " +
                "gross_pay DECIMAL(10,2) NOT NULL, " +
                "pay_date DATE NOT NULL, " +
                "FOREIGN KEY (empid) REFERENCES employee(empid) ON DELETE CASCADE" +
                ")";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private int insertPayrollRecord(int empId, double grossPay, LocalDate payDate) throws Exception {
        if (!repo.searchByEmpId(empId).isPresent()) {
            throw new IllegalArgumentException("Employee ID " + empId + " was not found.");
        }

        String sql = "INSERT INTO pay_statements (empid, gross_pay, pay_date) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, empId);
            statement.setDouble(2, grossPay);
            statement.setDate(3, java.sql.Date.valueOf(payDate));
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                } else {
                    throw new IllegalStateException("Statement saved, but no ID was returned.");
                }
            }
        }
    }

    private String runEmployeeHistoryReport(YearMonth selectedMonth) throws Exception {
        String sql = "SELECT e.empid, e.name, e.ssn, e.salary, " +
                "e.job_title, e.division, p.statement_id, " +
                "p.gross_pay, p.pay_date " +
                "FROM employee e " +
                "LEFT JOIN pay_statements p ON e.empid = p.empid " +
                "AND p.pay_date BETWEEN ? AND ? " +
                "ORDER BY e.name, p.pay_date";

        StringBuilder output = new StringBuilder();
        output.append("FULL-TIME EMPLOYEE INFORMATION AND PAY HISTORY\n");
        output.append("Month: ").append(selectedMonth).append("\n");
        output.append("------------------------------------------------------------\n\n");

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, java.sql.Date.valueOf(selectedMonth.atDay(1)));
            statement.setDate(2, java.sql.Date.valueOf(selectedMonth.atEndOfMonth()));

            try (ResultSet results = statement.executeQuery()) {
                boolean foundEmployee = false;
                int currentEmployeeId = -1;
                double currentEmployeeTotal = 0;
                boolean currentEmployeeHasPay = false;

                while (results.next()) {
                    foundEmployee = true;
                    int employeeId = results.getInt("empid");

                    if (employeeId != currentEmployeeId) {
                        if (currentEmployeeId != -1) {
                            if (!currentEmployeeHasPay) {
                                output.append("  No pay statements this month.\n");
                            } else {
                                output.append(String.format("  Employee Monthly Total: $%,.2f%n", currentEmployeeTotal));
                            }
                            output.append("\n");
                        }

                        currentEmployeeId = employeeId;
                        currentEmployeeTotal = 0;
                        currentEmployeeHasPay = false;

                        output.append(String.format(
                                "ID: %d | Name: %s | SSN: %s | Title: %s | Division: %s | Salary: $%,.2f%n",
                                employeeId,
                                results.getString("name"),
                                results.getString("ssn"),
                                displayEnumValue(results.getString("job_title")),
                                displayEnumValue(results.getString("division")),
                                results.getDouble("salary")
                        ));
                    }

                    int statementId = results.getInt("statement_id");
                    if (!results.wasNull()) {
                        currentEmployeeHasPay = true;
                        double grossPay = results.getDouble("gross_pay");
                        currentEmployeeTotal += grossPay;
                        output.append(String.format(
                                "  Statement ID: %d | Pay Date: %s | Gross Pay: $%,.2f%n",
                                statementId,
                                results.getDate("pay_date"),
                                grossPay
                        ));
                    }
                }

                if (!foundEmployee) {
                    output.append("No employees found.");
                } else if (!currentEmployeeHasPay) {
                    output.append("  No pay statements this month.\n");
                } else {
                    output.append(String.format("  Employee Monthly Total: $%,.2f%n", currentEmployeeTotal));
                }
            }
        }
        return output.toString();
    }

    private String runGroupedReport(String groupColumn, String title, YearMonth selectedMonth) throws Exception {
        if (!groupColumn.equals("job_title") && !groupColumn.equals("division")) {
            throw new IllegalArgumentException("Invalid report grouping.");
        }

        String sql = "SELECT e." + groupColumn + " AS report_group, " +
                "COALESCE(SUM(p.gross_pay), 0) AS total_pay " +
                "FROM employee e " +
                "LEFT JOIN pay_statements p ON e.empid = p.empid " +
                "AND p.pay_date BETWEEN ? AND ? " +
                "GROUP BY e." + groupColumn + " " +
                "ORDER BY e." + groupColumn;

        StringBuilder output = new StringBuilder();
        output.append(title).append("\n");
        output.append("Month: ").append(selectedMonth).append("\n");
        output.append("------------------------------------------------------------\n");

        double grandTotal = 0;
        boolean foundData = false;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, java.sql.Date.valueOf(selectedMonth.atDay(1)));
            statement.setDate(2, java.sql.Date.valueOf(selectedMonth.atEndOfMonth()));

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    foundData = true;
                    double total = results.getDouble("total_pay");
                    grandTotal += total;
                    output.append(String.format(
                            "%-30s $%,12.2f%n",
                            displayEnumValue(results.getString("report_group")),
                            total
                    ));
                }
            }
        }

        if (!foundData) {
            return output.append("No report data found.").toString();
        }

        output.append("------------------------------------------------------------\n");
        output.append(String.format("%-30s $%,12.2f%n", "GRAND TOTAL", grandTotal));
        return output.toString();
    }

    private VBox createTeamLayout() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        Label header = new Label("Team Members & LinkedIn Profiles:");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TableView<TeamMember> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<TeamMember, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(data -> data.getValue().fullNameProperty());

        TableColumn<TeamMember, String> linkCol = new TableColumn<>("LinkedIn Profile");
        linkCol.setCellValueFactory(data -> data.getValue().linkedInProperty());

        linkCol.setCellFactory(column -> new TableCell<TeamMember, String>() {
            private final Hyperlink hyperlink = new Hyperlink();

            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null) {
                    setGraphic(null);
                    return;
                }

                hyperlink.setText(url);
                hyperlink.setOnAction(event -> {
                    try {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().browse(new URI(url));
                        }
                    } catch (Exception ex) {
                        System.err.println("Failed to open link: " + ex.getMessage());
                    }
                });
                setGraphic(hyperlink);
            }
        });
        //We had the idea to show our team info - I honestly do not know everyone's full names
        table.getColumns().addAll(nameCol, linkCol);
        table.setItems(FXCollections.observableArrayList(
                new TeamMember("Janiya Green", "https://www.linkedin.com/in/jaaygreen"),
                new TeamMember("Parth Chavan", "https://www.linkedin.com/in/parth-chavan-a5343b310/"),
                new TeamMember("Snigdha Varma", "https://www.linkedin.com/in/snigdhavarma/"),
                new TeamMember("Jalen Foreman", "https://www.linkedin.com/in/jalen-foreman-975a81207/"),
                new TeamMember("Iyana Halliburton", "https://www.linkedin.com/in/iyana-halliburton-67211a244/"),
                new TeamMember("Maia", "https://www.linkedin.com/in/maia-placeholder")
        ));

        layout.getChildren().addAll(header, table);
        return layout;
    }

    private String displayEnumValue(String value) {
        if (value == null) return "";
        return value.replace('_', ' ');
    }

    private YearMonth selectedMonth(ComboBox<Month> monthBox, ComboBox<Integer> yearBox) {
        if (monthBox.getValue() == null || yearBox.getValue() == null) {
            throw new IllegalArgumentException("Please select both a month and a year.");
        }
        return YearMonth.of(yearBox.getValue(), monthBox.getValue());
    }

    private void validateEmployeeInput(String name, String ssn, String salary, JobTitle jobTitle, Division division) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (ssn == null || !ssn.trim().matches("\\d{9}")) {
            throw new IllegalArgumentException("SSN must be 9 digits.");
        }
        
        double parsedSalary;
        try {
            parsedSalary = Double.parseDouble(salary.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Salary must be a valid number.");
        }
        
        if (parsedSalary <= 0) {
            throw new IllegalArgumentException("Salary must be greater than zero.");
        }
        if (jobTitle == null) {
            throw new IllegalArgumentException("Please select a job title.");
        }
        if (division == null) {
            throw new IllegalArgumentException("Please select a division.");
        }
    }

    private void showStartupError(Stage stage, Exception exception) {
        Label message = new Label("Could not connect to database.\n\n" + exception.getMessage() + "\n\nPlease check db.properties.");
        message.setWrapText(true);
        
        VBox layout = new VBox(message);
        layout.setPadding(new Insets(25));
        
        stage.setScene(new Scene(layout, 600, 250));
        stage.show();
    }

    public static class TeamMember {
        private final SimpleStringProperty fullName;
        private final SimpleStringProperty linkedIn;

        public TeamMember(String fullName, String linkedIn) {
            this.fullName = new SimpleStringProperty(fullName);
            this.linkedIn = new SimpleStringProperty(linkedIn);
        }

        public SimpleStringProperty fullNameProperty() {
            return fullName;
        }

        public SimpleStringProperty linkedInProperty() {
            return linkedIn;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
