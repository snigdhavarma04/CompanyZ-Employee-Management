# Team Work Distribution — CompanyZ Employee Management

Team name: **[FILL IN — decide as a group]**

Reference design: see [ARCHITECTURE.md](ARCHITECTURE.md) for the diagrams and file layout
everything below is built from.

| # | Name | Primary Component |
|---|------|--------------------|
| 1 | Parth (you) | Database — CRUD + search |
| 2 | Snigdha | Model classes |
| 3 | Jaay | Main + Integration/Testing |
| 4 | Maia | Database — salary + reports |
| 5 | Iyana | GUI (JavaFX) |

---

## Task Breakdown by Person

Everything each person owns from Section B (coding, testing, report, video), in one place so
nobody has to cross-reference tables to find their own work. Section A diagram submissions are
individual and identical for everyone — see [Section A](#1-section-a--individual-submissions-50--100--100--250-points)
below.

## Parth Task — Database (CRUD + Search)

- If no server was provided by the professor: run [`sql/schema.sql`](sql/schema.sql) once to
  create the `employeeData` tables, then share the host/username/password with the team
- `db/EmployeeRepository.java` + `MySQLEmployeeRepository.java` (Part 1) — JDBC connection
  setup, `ALTER TABLE employees ADD ssn`, insert, delete, update, search by name/SSN/empid
- Backup reviewer for: Jaay's `Main.java` + integration
- Test cases owned: **Update employee data**, **Search for employee** (valid update, invalid
  empid, partial-field update, match by name/SSN/empid, no-match case)
- Writes the database-layer section of the SWDD report

## Snigdha Task — Model Classes

- `model/Person.java`, `Employee.java`, `JobTitle.java`, `Division.java`, `PayrollRecord.java`,
  `Payable.java` — abstract class, interface, lookup classes, `List<PayrollRecord>` history
- Backup reviewer for: Jaay's `Main.java` + integration
- Writes the model-classes section of the SWDD report

## Jaay Task — Main + Integration/Testing

- `Main.java` + integration — wire GUI to the real `MySQLEmployeeRepository`, connection
  config, end-to-end smoke test
- Backup reviewer for: Snigdha's model classes, Iyana's GUI
- Assembles the SWDD final report (Word → PDF, TOC, all sections) and the code zip for dropbox
- Confirms all owners' files are final before zipping

## Maia Task — Database (Salary + Reports)

- `db/MySQLEmployeeRepository.java` (Part 2) — salary raise by % for a salary range, 3 report
  queries (pay history, total by job title, total by division)
- Backup reviewer for: Parth's database work
- Test cases owned: **Salary update for employees in a range** (e.g. 3.2% for $58K–<$105K —
  below range, at lower bound, inside range, at upper bound exclusive, above range)
- Writes the reporting/salary-tool section of the SWDD report

## Iyana Task — GUI (JavaFX)

- `gui/EmployeeGUI.java` — JavaFX screens for insert/search/update forms, buttons wired to
  `EmployeeRepository` methods, report views
- Backup reviewer for: Snigdha's model classes
- Records and edits the 7–15 min video demo (50 pts); at least 2–3 members speak on camera
- Writes the GUI section of the SWDD report

**Everyone**, regardless of the above: submits Section A diagrams individually and reads
[ARCHITECTURE.md](ARCHITECTURE.md) before coding starts.

---

## 1. Section A — Individual Submissions (50 + 100 + 100 = 250 points)

Every one of the 5 diagrams is graded **per person** — each member submits their own Word or
PDF containing **all 5 diagrams**, not just the one they're listed as "owner" of below.
"Owner" only means who finalizes that diagram in the team's UML tool (draw.io/Lucidchart/etc.)
first and shares it with the group, so everyone else has a correct reference to redraw/export
from before the real deadline. Distributing ownership this way means no single person has to
design all 5 from scratch, but the submission itself is still individual and complete.

### Use Case Diagrams — 50 points — Due **Jul 12, 11:59pm** (one Word/PDF)

Two diagrams, everyone submits both:

| Diagram | Points | Owner of "source of truth" |
|---|---|---|
| Use case diagram — Overall System | 25 | Snigdha |
| Use case diagram — Reporting | 25 | Maia |

### Class & Sequence Diagrams — 100 + 100 = 200 points — Due **Jul 19, 11:59pm** (one Word/PDF)

Three diagrams, everyone submits all three:

| Diagram | Points | Owner of "source of truth" |
|---|---|---|
| Class diagram (1, whole system) | 100 | Jaay |
| Sequence diagram — Overall System | 50 | Parth |
| Sequence diagram — Reporting | 50 | Iyana |

**Internal team deadline:** diagram owners finish and share their diagram by **Jul 11, 6pm**
(use case) and **Jul 17, 6pm** (class + sequence) — leaves a buffer day before each real
deadline for everyone to redraw/export their own copy of all diagrams due that round.

---

## 2. Section B — Group Programming (due Jul 26, 11:59pm)

Built around the 4-file split from [ARCHITECTURE.md §8](ARCHITECTURE.md#code--file-structure).
Model classes now include `JobTitle`/`Division`/`PayrollRecord` (renamed from `PayStatement`)
to match the real `employeeData` schema — see [ARCHITECTURE.md](ARCHITECTURE.md#data-model).

| File | Contents | Owner | Backup reviewer |
|---|---|---|---|
| `model/Person.java`, `Employee.java`, `JobTitle.java`, `Division.java`, `PayrollRecord.java`, `Payable.java` | Abstract class, interface, lookup classes, `List<PayrollRecord>` history | **Snigdha** | Jaay |
| `db/EmployeeRepository.java` + `MySQLEmployeeRepository.java` — Part 1 | JDBC connection setup, `ALTER TABLE employees ADD ssn`, insert, delete, update, search by name/SSN/empid (joins to `job_titles`/`division` for display) | **Parth** | Jaay |
| `db/MySQLEmployeeRepository.java` — Part 2 | Salary raise by % for salary range, 3 report queries joining `payroll` → `employees` → `employee_job_titles`/`employee_division` → `job_titles`/`division` | **Maia** | Parth |
| `gui/EmployeeGUI.java` | JavaFX screens: insert/search/update forms, buttons wired to `EmployeeRepository` methods, report views | **Iyana** | Snigdha |
| `Main.java` + integration | Wires GUI to the real `MySQLEmployeeRepository`, connection config, end-to-end smoke test | **Jaay** | Iyana |

**Task 5 for the 5-programming-tasks requirement** maps 1:1 to [ARCHITECTURE.md §9](ARCHITECTURE.md#programming-tasks--user-story-mapping):
schema update, search, update, salary raise, reporting — one task per person, same owners as above.

### Test cases (pass/fail), per the assignment's required 3 categories

| Test case set | Owner | Notes |
|---|---|---|
| Update employee data | Parth | Cover valid update, invalid empid, partial-field update |
| Search for employee | Parth | Cover match by name, by SSN, by empid, and no-match case |
| Salary update for employees in a range (e.g. 3.2% for $58K–<$105K) | Maia | Cover below range, at lower bound, inside range, at upper bound (exclusive), above range |

Write each as: task description → pass condition → fail condition, per the format discussed
in class, in the SWDD report's test case section.

### Report & video (shared)

| Item | Points | Owner | Everyone's part |
|---|---|---|---|
| SWDD final report (Word → PDF, TOC, all sections) | 100 | Jaay assembles | Each member writes their own component's section |
| Code zip for dropbox | (part of 100 above) | Jaay | All owners confirm their file is final before zip |
| 7–15 min video demo (search, update, insert) | 50 | Iyana records/edits | At least 2–3 members speak on camera walking through their part |

**Reminder:** all 5 members must submit individually for *both* Section A and Section B, even
though Section B is graded as a group — check the drop box requirements for what "individual
submission" means for the group section (likely a form or short individual write-up).

---

## 3. Suggested Internal Timeline

| Date | Milestone |
|---|---|
| Jul 9–10 | Finalize team name, everyone reads ARCHITECTURE.md |
| Jul 11 | Use case diagrams finalized and shared (Snigdha & Maia) |
| Jul 12 | **Section A #1 due** — everyone submits use case diagrams individually |
| Jul 13–16 | Model + DB skeleton coded (Snigdha, Parth, Maia), class/sequence diagrams drafted |
| Jul 17 | Class + sequence diagrams finalized and shared (Jaay, Parth, Iyana) |
| Jul 19 | **Section A #2 due** — everyone submits class + sequence diagrams individually |
| Jul 20–23 | GUI wired to DB (Iyana + Jaay), test cases written (Parth, Maia) |
| Jul 24 | Integration test pass, SWDD report drafted |
| Jul 25 | Video recorded, report finalized, code zipped |
| Jul 26 | **Section B due** — group programming, test cases, SWDD report, code zip, video |
