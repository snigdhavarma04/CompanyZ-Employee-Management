# CompanyZ Employee Management - Setup Instructions

This guide explains how to download the project from Git, install Java 21 and JavaFX 21, configure Visual Studio Code, and run the application.

## 1. Required software

Each team member needs:

- Git
- JDK 21
- JavaFX SDK 21
- Visual Studio Code
- Visual Studio Code's **Extension Pack for Java**

JavaFX is not included with JDK 21. Both the JDK and JavaFX SDK must be installed.

## 2. Install Git

1. Download Git for Windows from:
   <https://git-scm.com/download/win>
2. Run the installer.
3. The default installation choices are normally acceptable.
4. Open a new PowerShell window and verify the installation:

```powershell
git --version
```

## 3. Install JDK 21

1. Go to the Eclipse Adoptium download page:
   <https://adoptium.net/temurin/releases/?version=21>
2. Select:
   - Operating System: **Windows**
   - Architecture: **x64**
   - Package Type: **JDK**
   - Version: **21 — LTS**
3. Download the `.msi` installer.
4. Run the installer.
5. If the installer displays these options, enable:
   - **Add to PATH**
   - **Set JAVA_HOME variable**
6. Finish the installation.
7. Completely close and reopen PowerShell and Visual Studio Code.
8. Verify the installation:

```powershell
java -version
javac -version
```

Both commands should show version 21.

To check `JAVA_HOME`, run:

```powershell
echo $env:JAVA_HOME
```

If `java` is not recognized, restart the computer and test again. If it still fails, add the JDK's `bin` directory to the Windows `Path` environment variable.

## 4. Install JavaFX SDK 21

1. Go to:
   <https://gluonhq.com/products/javafx/>
2. In the JavaFX downloads section, choose:
   - Version: **21**
   - Operating System: **Windows**
   - Architecture: **x64**
   - Type: **SDK**
3. Download the ZIP file.
4. Right-click the downloaded ZIP and select **Extract All**.
5. Move the extracted SDK folder to a stable location.

Example:

```text
C:\javafx-sdk-21.0.12
```

Confirm that the following folder exists:

```text
C:\javafx-sdk-21.0.12\lib
```

The exact JavaFX patch version may be different on another computer. For example, a team member might have `javafx-sdk-21.0.13` instead of `javafx-sdk-21.0.12`. That is fine; the environment variable described below must point to the folder that actually exists on that computer.

## 5. Set the `JAVAFX_HOME` environment variable

Each team member must set `JAVAFX_HOME` to their own extracted JavaFX SDK folder.

For example:

```powershell
setx JAVAFX_HOME "C:\javafx-sdk-21.0.12"
```

Important:

- Do not include `\lib` in the value.
- Change the path if JavaFX was extracted somewhere else.
- `setx` affects newly opened programs, not programs that are already running.
- Completely close and reopen Visual Studio Code after running the command.

Verify the variable in a newly opened PowerShell or VS Code terminal:

```powershell
echo $env:JAVAFX_HOME
Test-Path "$env:JAVAFX_HOME\lib"
```

The first command should display the JavaFX folder. The second command should return:

```text
True
```

## 6. Install Visual Studio Code and its Java extensions

1. Download Visual Studio Code from:
   <https://code.visualstudio.com/>
2. Install and open it.
3. Open the **Extensions** panel.
4. Search for **Extension Pack for Java** by Microsoft.
5. Install the extension pack.

## 7. Clone the Git repository

In PowerShell, move to the directory where the project should be stored:

```powershell
cd "$HOME\Documents"
```

Clone the repository by replacing the example URL with the actual repository URL:

```powershell
git clone https://github.com/ORGANIZATION/CompanyZ-Employee-Management.git
cd CompanyZ-Employee-Management
code .
```

If `code` is not recognized, open Visual Studio Code manually and select:

**File → Open Folder → CompanyZ-Employee-Management**

## 8. Repository launch configuration

The repository should contain this file:

```text
.vscode\launch.json
```

Its contents should be:

```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Run EmployeeGUI",
            "request": "launch",
            "mainClass": "companyz.EmployeeGUI",
            "vmArgs": "--module-path \"${env:JAVAFX_HOME}\\lib\" --add-modules javafx.controls,javafx.fxml"
        }
    ]
}
```

The shared file uses `${env:JAVAFX_HOME}` instead of a personal path such as `C:\javafx-sdk-21.0.12`. Therefore, the same committed `launch.json` can work for everyone.

## 9. Run the application

1. Open the project folder in Visual Studio Code.
2. Allow the Java extensions to finish loading.
3. Open **Run and Debug**.
4. Select **Run EmployeeGUI** from the configuration list.
5. Click the green Run button.

## 10. What a team member may need to change

Most team members should not need to edit any source code or the committed `launch.json`.

They may need to change only the value of their local `JAVAFX_HOME` environment variable:

```powershell
setx JAVAFX_HOME "THEIR-ACTUAL-JAVAFX-FOLDER"
```

Examples:

```powershell
setx JAVAFX_HOME "C:\javafx-sdk-21.0.12"
```

```powershell
setx JAVAFX_HOME "D:\Development\javafx-sdk-21.0.12"
```

The `mainClass` value should remain:

```text
companyz.EmployeeGUI
```

Change `mainClass` only if the package declaration or GUI startup class is renamed in the project. For example, if `EmployeeGUI.java` begins with:

```java
package companyz;
```

then the correct fully qualified main class is:

```text
companyz.EmployeeGUI
```

The `--add-modules` list may need to be updated only if the code begins using another JavaFX module. Common examples include:

- `javafx.controls` for buttons, labels, tables, layouts, and other controls
- `javafx.fxml` when FXML files are used
- `javafx.media` for audio or video
- `javafx.web` for `WebView`

For example:

```json
"vmArgs": "--module-path \"${env:JAVAFX_HOME}\\lib\" --add-modules javafx.controls,javafx.fxml,javafx.media"
```

Do not add modules unless the application actually uses them.

## 11. Files that should and should not be committed

Commit:

- Java source files
- Project resources and FXML files
- `.vscode/launch.json`
- This `INSTRUCTIONS.md` file
- Build configuration files such as `pom.xml` or `build.gradle`, if added later

Do not commit:

- The downloaded JavaFX SDK
- The installed JDK
- Personal absolute paths
- Compiled `.class` files
- Build-output folders such as `bin`, `out`, or `target`
- Passwords, database credentials, or API keys

If the repository's `.gitignore` currently ignores the entire `.vscode` directory, add these rules so only the shared launch configuration is included:

```gitignore
.vscode/*
!.vscode/launch.json
```

## 12. Troubleshooting

### JavaFX runtime components are missing

Confirm that `JAVAFX_HOME` is set and points to the extracted SDK:

```powershell
echo $env:JAVAFX_HOME
Test-Path "$env:JAVAFX_HOME\lib"
```

Then completely close and reopen Visual Studio Code.

### `${env:JAVAFX_HOME}` appears unresolved

Visual Studio Code was probably opened before the environment variable was created. Close every VS Code window and reopen the project.

### `java` or `javac` is not recognized

Restart PowerShell and Visual Studio Code. If needed, restart the computer. Verify that JDK 21 was installed with the **Add to PATH** and **Set JAVA_HOME** options.

### Main class could not be found

Confirm that:

- `EmployeeGUI.java` exists.
- It contains `package companyz;`.
- The class is named `EmployeeGUI`.
- The launch configuration uses `"mainClass": "companyz.EmployeeGUI"`.

### Package name does not match the folder

Java source files declared with:

```java
package companyz;
```

should be stored under a corresponding `companyz` source folder. Do not remove or change package declarations only to fix a local setup problem.

### JavaFX package does not exist during compilation

The project may need JavaFX added to its Java project references in addition to the runtime configuration. A Maven or Gradle build is the best long-term solution because it downloads and configures JavaFX dependencies automatically.

## Recommended future improvement

Convert the repository to Maven or Gradle. A build tool can declare the JavaFX version and modules in the repository, which removes most manual JavaFX setup and makes the project easier to build consistently on Windows, macOS, and Linux.
