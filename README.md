# KEC (R2024) Student Academic & Continuous Evaluation System

A beginner-friendly Java console application using JDBC and MySQL.

## Requirements
- JDK 17 or later
- MySQL Server
- MySQL Connector/J JAR

## Setup
1. Start MySQL.
2. Run `kec_schema.sql` in MySQL Workbench or the MySQL command line.
3. Open `DBConnection.java` and set `PASSWORD` to your MySQL root password.
4. Add MySQL Connector/J to the project's classpath.
5. Compile and run the four Java files.

### Windows command-line example
Assume `mysql-connector-j-8.x.x.jar` is in the project folder:

```bat
javac -cp ".;mysql-connector-j-8.x.x.jar" DBConnection.java StudentDAO.java AcademicDAO.java MainApp.java
java -cp ".;mysql-connector-j-8.x.x.jar" MainApp
```

Replace the JAR filename with the exact filename you downloaded.

## Suggested demo data
1. Add student: `24ITR001`, `Arun Kumar`, `IT`, semester `3`
2. Add course: `24ITR31`, `Database Management Systems`, `4`, `THEORY`
3. Enroll the student in course `24ITR31`, semester `3`; note the enrollment ID shown.
4. Update attendance using 40 classes held and 36 attended (90%).
5. Enter CIA1 `80`, CIA2 `85`.
6. Enter Phase 2 activity mark `90`.
7. Enter ESE mark `75`.
8. Generate the mark sheet for `24ITR001`, semester `3`.

## Screenshots for submission
Capture actual screenshots from your running program:
- Student and course successfully added.
- Enrollment ID displayed after enrollment.
- Attendance updated and incremental marks entered in each phase.
- Mark sheet showing course totals, grade, SGPA and equivalent percentage.
- Attendance defaulter report (add a separate student with attendance below 65% to demonstrate this).
- Department result summary.

## Evaluation assumptions
The assignment gives assessment components as raw marks out of 100 but does not specify how to combine the three internal components. This implementation uses their arithmetic mean as the internal score, then applies the requested CIA/ESE weightage. The ESE minimum is interpreted as a percentage of the ESE component: 45% for THEORY/EMBEDDED and 50% for PRACTICAL. Confirm these assumptions with your faculty if a specific KEC R2024 handbook formula is required.

For attendance 65%-74.9%, the program treats the student as barred until medical condonation is approved; with approval the student is evaluated. Below 65%, the result is SA.
