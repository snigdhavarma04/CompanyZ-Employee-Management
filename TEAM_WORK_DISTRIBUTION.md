# Team Work Distribution — CompanyZ Employee Management

Team name: **[FILL IN — decide as a group]**

Reference design: see [ARCHITECTURE.md](ARCHITECTURE.md) for the diagrams and file layout
everything below is built from.

| # | Name | Primary Component |
|---|------|--------------------|
| 1 | Parth | Database + Main Integration |
| 2 | Snigdha | Model Classes |
| 3 | Jaay | Project Coordination + Final Packaging |
| 4 | Maia | Database — Salary + Reports |
| 5 | Iyana | GUI (JavaFX) |
| 6 | Jalen | Testing + QA + Documentation |

---

## Task Breakdown by Person

Everything each person owns from Section B (coding, testing, report, video), in one place so
nobody has to cross-reference tables to find their own work. Section A diagram submissions are
individual and identical for everyone — see [Section A](#1-section-a--individual-submissions-50--100--100--250-points)
below.

---

## Parth Task — Database + Main Integration

- If no server was provided by the professor: run [`sql/schema.sql`](sql/schema.sql) once to
  create the `employeeData` database and share the host/username/password with the team.
- Implement `EmployeeRepository.java`.
- Implement `MySQLEmployeeRepository.java` (CRUD).
- Configure JDBC connection.
- Add SSN column support.
- Insert employee.
- Delete employee.
- Update employee.
- Search employee by Employee ID, Name and SSN.
- Connect the database to all model classes.
- Connect JavaFX GUI with the repository.
- Configure `Main.java`.
- Perform final backend integration.
- Ensure all project modules communicate correctly.

Backup reviewer: **Jaay**

Test cases owned:

- Database connection
- Update employee data
- Search employee
- CRUD validation

Writes the **Database + Integration** section of the SWDD report.

---

## Snigdha Task — Model Classes

- `model/Person.java`
- `Employee.java`
- `JobTitle.java`
- `Division.java`
- `PayrollRecord.java`
- `Payable.java`

Responsibilities

- Abstract class
- Interface
- Lookup classes
- Employee payroll history (`List<PayrollRecord>`)

Backup reviewer: **Jalen**

Writes the **Model Classes** section of the SWDD report.

---

## Jaay Task — Project Coordination

- Review completed modules.
- Assist Parth during final integration if required.
- Collect final files from every member.
- Assemble the SWDD report.
- Convert Word → PDF.
- Create the final project ZIP.
- Verify every team member's files before submission.

Backup reviewer: **Parth**

Writes the **Project Overview** section of the report.

---

## Maia Task — Database (Salary + Reports)

Implement remaining methods inside
`MySQLEmployeeRepository.java`

Including

- Salary raise by percentage.
- Salary range update.
- Payroll history report.
- Total salary by job title.
- Total salary by division.

Backup reviewer: **Parth**

Test cases owned:

- Salary update for employees in a salary range.

Writes the **Reporting Module** section of the SWDD report.

---

## Iyana Task — GUI (JavaFX)

Implement

`gui/EmployeeGUI.java`

Responsibilities

- Insert employee screen.
- Update employee screen.
- Delete employee screen.
- Search employee screen.
- Report screens.
- Connect all buttons to repository methods.

Backup reviewer: **Snigdha**

Also

- Record the project demo.
- Edit the demo video.

Writes the **GUI** section of the SWDD report.

---

## Jalen Task — Testing, QA & Documentation

Responsibilities

- Perform integration testing.
- Perform system testing.
- Execute all test cases.
- Verify GUI functionality.
- Verify database functionality.
- Record pass/fail results.
- Assist in debugging.
- Verify project requirements are met.
- Help Jaay review the final submission.

Backup reviewer: **Everyone**

Writes the **Testing & Quality Assurance** section of the SWDD report.

---

## 1. Section A — Individual Submissions (50 + 100 + 100 = 250 points)

Every one of the 5 diagrams is graded **per person** — each member submits their own Word or
PDF containing **all 5 diagrams**.

### Use Case Diagrams — 50 points

| Diagram | Points | Owner |
|---|---|---|
| Overall System | 25 | Snigdha |
| Reporting | 25 | Maia |

### Class & Sequence Diagrams — 200 points

| Diagram | Points | Owner |
|---|---|---|
| Class Diagram | 100 | Jaay |
| Overall Sequence Diagram | 50 | Parth |
| Reporting Sequence Diagram | 50 | Iyana |

---

## 2. Section B — Group Programming

| File | Contents | Owner | Backup Reviewer |
|---|---|---|---|
| Model Classes | Person, Employee, JobTitle, Division, PayrollRecord, Payable | **Snigdha** | Jalen |
| EmployeeRepository.java + MySQLEmployeeRepository.java | CRUD, JDBC, Search, SSN | **Parth** | Jaay |
| Main.java | Database connection, GUI integration, project wiring | **Parth** | Jaay |
| Salary & Reports | Salary update, Payroll reports | **Maia** | Parth |
| EmployeeGUI.java | JavaFX interface | **Iyana** | Snigdha |
| Testing & QA | Integration testing, validation, documentation | **Jalen** | Everyone |
| Final Packaging | Report assembly, ZIP submission | **Jaay** | Parth |

---

### Test Cases

| Test Case | Owner |
|---|---|
| Database Connection | Parth |
| Update Employee | Parth |
| Search Employee | Parth |
| Salary Update | Maia |
| GUI Testing | Iyana |
| Integration Testing | Jalen |
| System Testing | Jalen |

---

### Report & Video

| Item | Owner | Everyone's Part |
|---|---|---|
| SWDD Final Report | Jaay | Every member writes their assigned section |
| Final Code ZIP | Jaay | Owners verify final code |
| Video Demo | Iyana | At least 2–3 members present |
| Testing Documentation | Jalen | Verify all functionality |

---

## 3. Suggested Internal Timeline

| Date | Milestone |
|---|---|
| Jul 9–10 | Finalize team name and review ARCHITECTURE.md |
| Jul 11 | Use Case diagrams completed |
| Jul 12 | Everyone submits Use Case diagrams |
| Jul 13–16 | Model, Database, Reports and GUI development |
| Jul 17 | Class & Sequence diagrams completed |
| Jul 19 | Everyone submits Class & Sequence diagrams |
| Jul 20–22 | Parth integrates Database, GUI and Main.java |
| Jul 22–24 | Jalen performs integration and system testing |
| Jul 24 | SWDD report drafted |
| Jul 25 | Video recorded and final review |
| Jul 26 | Final submission |
