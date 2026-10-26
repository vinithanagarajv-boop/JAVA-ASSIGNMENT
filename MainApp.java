import java.sql.SQLException;
import java.util.Scanner;

public class MainApp {
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        StudentDAO studentDAO = new StudentDAO();
        AcademicDAO academicDAO = new AcademicDAO();

        while (true) {
            System.out.println("\n===== KEC R2024 ACADEMIC SYSTEM =====");
            System.out.println("1. Add student");
            System.out.println("2. Add course");
            System.out.println("3. Enroll student in course");
            System.out.println("4. Update attendance");
            System.out.println("5. Phase 1: Enter CIA 1 and CIA 2");
            System.out.println("6. Phase 2: Enter assignment/quiz/lab mark");
            System.out.println("7. Phase 3: Enter ESE mark / mark AB");
            System.out.println("8. Generate semester mark sheet");
            System.out.println("9. Attendance defaulter report");
            System.out.println("10. Department result summary");
            System.out.println("0. Exit");
            System.out.print("Choose option: ");

            try {
                int choice = Integer.parseInt(sc.nextLine().trim());
                switch (choice) {
                    case 1:
                        System.out.print("Roll number: "); String roll = sc.nextLine().trim();
                        System.out.print("Student name: "); String name = sc.nextLine().trim();
                        System.out.print("Department (CSE/ECE/IT/MECH...): "); String dept = sc.nextLine().trim();
                        System.out.print("Current semester (1-8): "); int sem = Integer.parseInt(sc.nextLine().trim());
                        studentDAO.addStudent(roll, name, dept, sem); break;
                    case 2:
                        System.out.print("Course code: "); String code = sc.nextLine().trim();
                        System.out.print("Course title: "); String title = sc.nextLine().trim();
                        System.out.print("Credits: "); int credits = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("Type (THEORY/PRACTICAL/EMBEDDED): "); String type = sc.nextLine().trim();
                        studentDAO.addCourse(code, title, credits, type); break;
                    case 3:
                        System.out.print("Roll number: "); roll = sc.nextLine().trim();
                        System.out.print("Course code: "); code = sc.nextLine().trim();
                        System.out.print("Semester: "); sem = Integer.parseInt(sc.nextLine().trim());
                        academicDAO.enroll(roll, code, sem); break;
                    case 4:
                        System.out.print("Enrollment ID: "); int id = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("Classes held: "); int held = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("Classes attended: "); int attended = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("Medical condonation approved? (yes/no): ");
                        boolean approved = sc.nextLine().trim().equalsIgnoreCase("yes");
                        academicDAO.updateAttendance(id, held, attended, approved); break;
                    case 5:
                        System.out.print("Enrollment ID: "); id = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("CIA 1 (0-100): "); double c1 = Double.parseDouble(sc.nextLine().trim());
                        System.out.print("CIA 2 (0-100): "); double c2 = Double.parseDouble(sc.nextLine().trim());
                        academicDAO.enterCIA(id, c1, c2); break;
                    case 6:
                        System.out.print("Enrollment ID: "); id = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("Assignment/quiz/lab mark (0-100): ");
                        double activity = Double.parseDouble(sc.nextLine().trim());
                        academicDAO.enterInternalActivity(id, activity); break;
                    case 7:
                        System.out.print("Enrollment ID: "); id = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("Absent in ESE? (yes/no): "); boolean absent = sc.nextLine().trim().equalsIgnoreCase("yes");
                        Double ese = null;
                        if (!absent) {
                            System.out.print("ESE mark (0-100): "); ese = Double.parseDouble(sc.nextLine().trim());
                        }
                        academicDAO.enterESE(id, ese, absent); break;
                    case 8:
                        System.out.print("Roll number: "); roll = sc.nextLine().trim();
                        System.out.print("Semester: "); sem = Integer.parseInt(sc.nextLine().trim());
                        academicDAO.printMarkSheet(roll, sem); break;
                    case 9: academicDAO.attendanceDefaulters(); break;
                    case 10: academicDAO.classSummary(); break;
                    case 0: System.out.println("Goodbye."); sc.close(); return;
                    default: System.out.println("Invalid choice.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid number.");
            } catch (IllegalArgumentException e) {
                System.out.println("Input error: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
                System.out.println("Check MySQL is running, schema is imported, and DBConnection password is correct.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
