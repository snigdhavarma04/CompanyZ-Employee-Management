-- CompanyZ employeeData schema
-- Matches ER_diagram_employeeData_03062024.pdf, plus the ssn column required by the assignment.
-- Run this ONCE against a fresh MySQL server/database to create all tables.
-- Everyone else just connects to that same server afterward — no need to run this again.

CREATE DATABASE IF NOT EXISTS employeeData;
USE employeeData;

CREATE TABLE employees (
    empid    INT PRIMARY KEY AUTO_INCREMENT,
    Fname    VARCHAR(50) NOT NULL,
    Lname    VARCHAR(50) NOT NULL,
    email    VARCHAR(100),
    ssn      CHAR(9),              -- new column, no dashes, e.g. '123456789'
    HireDate DATE,
    Salary   DECIMAL(10,2)
);

CREATE TABLE job_titles (
    job_title_id INT PRIMARY KEY AUTO_INCREMENT,
    job_title    VARCHAR(50) NOT NULL
);

CREATE TABLE employee_job_titles (
    empid        INT NOT NULL,
    job_title_id INT NOT NULL,
    PRIMARY KEY (empid, job_title_id),
    FOREIGN KEY (empid) REFERENCES employees(empid) ON DELETE CASCADE,
    FOREIGN KEY (job_title_id) REFERENCES job_titles(job_title_id) ON DELETE CASCADE
);

CREATE TABLE division (
    ID           INT PRIMARY KEY AUTO_INCREMENT,
    Name         VARCHAR(100) NOT NULL,
    city         VARCHAR(50),
    addressLine1 VARCHAR(100),
    addressLine2 VARCHAR(100),
    state        VARCHAR(50),
    country      VARCHAR(50),
    postalCode   VARCHAR(20)
);

CREATE TABLE employee_division (
    empid  INT NOT NULL,
    div_ID INT NOT NULL,
    PRIMARY KEY (empid, div_ID),
    FOREIGN KEY (empid) REFERENCES employees(empid) ON DELETE CASCADE,
    FOREIGN KEY (div_ID) REFERENCES division(ID) ON DELETE CASCADE
);

CREATE TABLE payroll (
    payID       INT PRIMARY KEY AUTO_INCREMENT,
    pay_date    DATE,
    earnings    DECIMAL(10,2),
    fed_tax     DECIMAL(10,2),
    fed_med     DECIMAL(10,2),
    fed_SS      DECIMAL(10,2),
    state_tax   DECIMAL(10,2),
    retire_401k DECIMAL(10,2),
    health_care DECIMAL(10,2),
    empid       INT NOT NULL,
    FOREIGN KEY (empid) REFERENCES employees(empid) ON DELETE CASCADE
);
