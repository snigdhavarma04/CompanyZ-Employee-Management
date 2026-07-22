package companyz;

import companyz.model.Division;
import companyz.model.Employee;
import companyz.model.JobTitle;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class EmployeeGUI extends Application {

    private EmployeeRepository repo;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Company Z - Employee Management System");

        try {
            Properties dbProps = new Properties();
            dbProps.load(new FileInputStream("db.properties"));
            Connection conn = DriverManager.getConnection(
                dbProps.getProperty("db.url"),
                dbProps.getProperty("db.user"),
                dbProps.getProperty("db.password")
            );
            this.repo = new MySQLEmployeeRepository(conn);
            System.out.println("[GUI] Database connected successfully!");
        } catch (Exception e) {
            System.err.println("[GUI ERROR] Failed to connect to DB: " + e.getMessage());
        }

        TabPane tabPane = new TabPane();

        Tab insertTab = new Tab("Insert Employee", createInsertLayout());
        Tab searchTab = new Tab("Search Employee", createSearchLayout());
        Tab updateTab = new Tab("Update Employee", createUpdateLayout());

        tabPane.getTabs().addAll(insertTab, searchTab, updateTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Scene scene = new Scene(tabPane, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createInsertLayout() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField ssnField = new TextField();
        ssnField.setPromptText("9-Digit SSN (no dashes)");

        TextField salaryField = new TextField();
        salaryField.setPromptText("Salary (e.g. 65000)");

        ComboBox<JobTitle> titleBox = new ComboBox<>();
        titleBox.getItems().addAll(JobTitle.values());
        titleBox.setPromptText("Select Job Title");

        ComboBox<Division> divisionBox = new ComboBox<>();
        divisionBox.getItems().addAll(Division.values());
        divisionBox.setPromptText("Select Division");

        Button submitBtn = new Button("Insert Employee");
        Label statusLabel = new Label();

        submitBtn.setOnAction(e -> {
            try {
                String name = nameField.getText();
                String ssn = ssnField.getText();
                double salary = Double.parseDouble(salaryField.getText());
                JobTitle title = titleBox.getValue();
                Division division = divisionBox.getValue();

                Employee newEmp = new Employee(0, name, ssn, salary, title, division);
                int newId = repo.insertEmployee(newEmp);

                statusLabel.setText("SUCCESS! Created Employee ID: " + newId);
            } catch (Exception ex) {
                statusLabel.setText("ERROR: " + ex.getMessage());
            }
        });

        layout.getChildren().addAll(
            new Label("Insert New Employee:"), nameField, ssnField, 
            salaryField, titleBox, divisionBox, submitBtn, statusLabel
        );
        return layout;
    }

    private VBox createSearchLayout() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        TextField searchInput = new TextField();
        searchInput.setPromptText("Enter Name, 9-Digit SSN, or Emp ID");

        Button searchBtn = new Button("Search");
        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);

        searchBtn.setOnAction(e -> {
            String query = searchInput.getText().trim();
            try {
                resultArea.clear();
                if (query.matches("\\d+") && query.length() < 9) {
                    Optional<Employee> emp = repo.searchByEmpId(Integer.parseInt(query));
                    emp.ifPresentOrElse(
                        r -> resultArea.setText(formatEmp(r)),
                        () -> resultArea.setText("No employee found with ID: " + query)
                    );
                } 
                else if (query.matches("\\d{9}")) {
                    Optional<Employee> emp = repo.searchBySsn(query);
                    emp.ifPresentOrElse(
                        r -> resultArea.setText(formatEmp(r)),
                        () -> resultArea.setText("No employee found with SSN: " + query)
                    );
                } 
                else {
                    List<Employee> list = repo.searchByName(query);
                    if (list.isEmpty()) {
                        resultArea.setText("No employee found with name: " + query);
                    } else {
                        StringBuilder sb = new StringBuilder();
                        for (Employee emp : list) {
                            sb.append(formatEmp(emp)).append("\n");
                        }
                        resultArea.setText(sb.toString());
                    }
                }
            } catch (SQLException ex) {
                resultArea.setText("Database error: " + ex.getMessage());
            }
        });

        layout.getChildren().addAll(new Label("Search Employee:"), searchInput, searchBtn, resultArea);
        return layout;
    }

    private VBox createUpdateLayout() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.getChildren().add(new Label("Update / Delete Employee Screen (Coming next)"));
        return layout;
    }

    private String formatEmp(Employee emp) {
        return String.format("ID: %d | Name: %s | SSN: %s | Title: %s | Div: %s | Salary: $%,.2f",
            emp.getEmpId(), emp.getName(), emp.getSsn(), emp.getJobTitle(), emp.getDivision(), emp.getSalary());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
