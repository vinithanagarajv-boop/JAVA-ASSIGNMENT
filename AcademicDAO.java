import java.sql.*;
import java.util.Locale;

public class AcademicDAO {
    public void enroll(String roll, String code, int semester) throws SQLException {
        String sql = "INSERT INTO enrollments(roll_no, course_code, semester) VALUES(?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, roll);
            ps.setString(2, code);
            ps.setInt(3, semester);
            ps.executeUpdate();
            int id;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                id = rs.getInt(1);
            }
            try (PreparedStatement ms = con.prepareStatement(
                    "INSERT INTO assessment_marks(enrollment_id) VALUES(?)")) {
                ms.setInt(1, id);
                ms.executeUpdate();
            }
            System.out.println("Enrollment created. Enrollment ID = " + id);
        }
    }

    public void updateAttendance(int enrollmentId, int held, int attended, boolean condonation)
            throws SQLException {
        if (held < 0 || attended < 0 || attended > held) {
            throw new IllegalArgumentException("Attendance must satisfy 0 <= attended <= held.");
        }
        String sql = "UPDATE enrollments SET classes_held=?, classes_attended=?, medical_condonation=? WHERE enrollment_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, held);
            ps.setInt(2, attended);
            ps.setBoolean(3, condonation);
            ps.setInt(4, enrollmentId);
            if (ps.executeUpdate() == 0) System.out.println("Enrollment ID not found.");
            else System.out.println("Attendance updated.");
        }
    }

    // Phase 1: CIA Test 1 and CIA Test 2. Values are out of 100.
    public void enterCIA(int id, double cia1, double cia2) throws SQLException {
        validateMark(cia1); validateMark(cia2);
        updateMarks(id, "UPDATE assessment_marks SET cia1=?, cia2=? WHERE enrollment_id=?",
                cia1, cia2, null);
    }

    // Phase 2: assignments / quizzes / lab evaluations, out of 100.
    public void enterInternalActivity(int id, double activity) throws SQLException {
        validateMark(activity);
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "UPDATE assessment_marks SET assignment_quiz_lab=? WHERE enrollment_id=?")) {
            ps.setDouble(1, activity); ps.setInt(2, id);
            if (ps.executeUpdate() == 0) System.out.println("Enrollment ID not found.");
            else System.out.println("Internal activity mark saved.");
        }
    }

    // Phase 3: ESE mark out of 100, or mark absent (AB).
    public void enterESE(int id, Double mark, boolean absent) throws SQLException {
        if (!absent) {
            if (mark == null) throw new IllegalArgumentException("Enter ESE mark or select absent.");
            validateMark(mark);
        }
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "UPDATE assessment_marks SET ese_marks=?, ese_absent=? WHERE enrollment_id=?")) {
            if (absent) ps.setNull(1, Types.DECIMAL); else ps.setDouble(1, mark);
            ps.setBoolean(2, absent); ps.setInt(3, id);
            if (ps.executeUpdate() == 0) System.out.println("Enrollment ID not found.");
            else System.out.println("ESE result saved.");
        }
    }

    private void updateMarks(int id, String sql, double a, double b, Object unused) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, a); ps.setDouble(2, b); ps.setInt(3, id);
            if (ps.executeUpdate() == 0) System.out.println("Enrollment ID not found.");
            else System.out.println("CIA marks saved.");
        }
    }

    private void validateMark(double mark) {
        if (mark < 0 || mark > 100) throw new IllegalArgumentException("Marks must be between 0 and 100.");
    }

    private static class Result {
        double total;
        String grade, status;
        double gradePoint;
        Result(double total, String grade, String status, double gp) {
            this.total = total; this.grade = grade; this.status = status; this.gradePoint = gp;
        }
    }

    private Result evaluate(String type, double cia1, double cia2, double activity,
                            double ese, boolean absent, double attendance, boolean condonation) {
        if (attendance < 65.0) return new Result(0, "SA", "Shortage of Attendance", 0);
        if (attendance < 75.0 && !condonation)
            return new Result(0, "SA", "Medical condonation approval required; barred until approved", 0);
        if (absent) return new Result(0, "AB", "Absent in ESE", 0);

        // Internal average is based on CIA1, CIA2 and the Phase-2 activity mark.
        double internal100 = (cia1 + cia2 + activity) / 3.0;
        double internalWeight, eseWeight, eseThreshold;
        switch (type.toUpperCase(Locale.ROOT)) {
            case "PRACTICAL": internalWeight = 0.60; eseWeight = 0.40; eseThreshold = 50.0; break;
            case "EMBEDDED":  internalWeight = 0.50; eseWeight = 0.50; eseThreshold = 45.0; break;
            default:          internalWeight = 0.40; eseWeight = 0.60; eseThreshold = 45.0; break;
        }
        double total = internal100 * internalWeight + ese * eseWeight;
        // ESE mark is entered out of 100; compare it with the specified
        // minimum percentage (45 for THEORY/EMBEDDED, 50 for PRACTICAL).
        if (total < 50.0 || ese < eseThreshold)
            return new Result(total, "RA", "Re-Appearance", 0);
        if (total >= 91) return new Result(total, "O", "PASS", 10);
        if (total >= 81) return new Result(total, "A+", "PASS", 9);
        if (total >= 71) return new Result(total, "A", "PASS", 8);
        if (total >= 61) return new Result(total, "B+", "PASS", 7);
        return new Result(total, "B", "PASS", 6);
    }

    public void printMarkSheet(String roll, int semester) throws SQLException {
        String sql = "SELECT s.roll_no,s.student_name,s.department,e.semester,c.course_code,c.title,c.credits,c.course_type," +
            "e.classes_held,e.classes_attended,e.medical_condonation,m.cia1,m.cia2,m.assignment_quiz_lab,m.ese_marks,m.ese_absent " +
            "FROM students s JOIN enrollments e ON s.roll_no=e.roll_no " +
            "JOIN courses c ON c.course_code=e.course_code JOIN assessment_marks m ON m.enrollment_id=e.enrollment_id " +
            "WHERE s.roll_no=? AND e.semester=? ORDER BY c.course_code";
        double weightedPoints = 0, totalCredits = 0;
        boolean found = false;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roll); ps.setInt(2, semester);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n========== KEC R2024 SEMESTER MARK SHEET ==========");
                while (rs.next()) {
                    found = true;
                    double held = rs.getDouble("classes_held");
                    double attended = rs.getDouble("classes_attended");
                    double attendance = held == 0 ? 0 : attended * 100.0 / held;
                    double cia1 = rs.getDouble("cia1");
                    double cia2 = rs.getDouble("cia2");
                    double activity = rs.getDouble("assignment_quiz_lab");
                    double ese = rs.getDouble("ese_marks");
                    boolean absent = rs.getBoolean("ese_absent");
                    boolean condonation = rs.getBoolean("medical_condonation");
                    String type = rs.getString("course_type");
                    Result result = evaluate(type, cia1, cia2, activity, ese, absent, attendance, condonation);
                    System.out.printf(Locale.ROOT,
                        "%s | %s | Credits:%d | Attendance:%.1f%% | CIA1:%.1f CIA2:%.1f Activity:%.1f | ESE:%s | Total:%.2f | Grade:%s | %s%n",
                        rs.getString("course_code"), rs.getString("title"), rs.getInt("credits"), attendance,
                        cia1, cia2, activity, absent ? "AB" : String.format(Locale.ROOT, "%.1f", ese),
                        result.total, result.grade, result.status);
                    weightedPoints += rs.getInt("credits") * result.gradePoint;
                    totalCredits += rs.getInt("credits");
                }
            }
        }
        if (!found) {
            System.out.println("No enrolled courses found for this roll number and semester.");
            return;
        }
        double sgpa = totalCredits == 0 ? 0 : weightedPoints / totalCredits;
        System.out.printf(Locale.ROOT, "SGPA: %.2f | Equivalent Percentage: %.2f%%%n", sgpa, sgpa * 10);
    }

    public void attendanceDefaulters() throws SQLException {
        String sql = "SELECT s.roll_no,s.student_name,c.course_code,e.classes_held,e.classes_attended " +
            "FROM enrollments e JOIN students s ON s.roll_no=e.roll_no JOIN courses c ON c.course_code=e.course_code " +
            "WHERE e.classes_held > 0 AND (e.classes_attended*100.0/e.classes_held) < 65 ORDER BY s.roll_no";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\n--- ATTENDANCE DEFAULTERS (SA) ---");
            while (rs.next()) {
                double pct = rs.getDouble("classes_attended") * 100.0 / rs.getDouble("classes_held");
                System.out.printf(Locale.ROOT, "%s | %s | %s | Attendance: %.1f%% | Grade: SA%n",
                    rs.getString("roll_no"), rs.getString("student_name"), rs.getString("course_code"), pct);
            }
        }
    }

    public void classSummary() throws SQLException {
        String sql = "SELECT s.department,e.semester,c.course_type,c.credits,e.classes_held,e.classes_attended," +
            "m.cia1,m.cia2,m.assignment_quiz_lab,m.ese_marks,m.ese_absent,e.medical_condonation " +
            "FROM students s JOIN enrollments e ON s.roll_no=e.roll_no JOIN courses c ON c.course_code=e.course_code " +
            "JOIN assessment_marks m ON m.enrollment_id=e.enrollment_id";
        class Stat { int pass=0, fail=0; double sum=0; int n=0; }
        java.util.Map<String, Stat> stats = new java.util.TreeMap<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                double held=rs.getDouble("classes_held"), attended=rs.getDouble("classes_attended");
                double attendance=held==0?0:attended*100.0/held;
                Result r=evaluate(rs.getString("course_type"),rs.getDouble("cia1"),rs.getDouble("cia2"),
                    rs.getDouble("assignment_quiz_lab"),rs.getDouble("ese_marks"),rs.getBoolean("ese_absent"),
                    attendance,rs.getBoolean("medical_condonation"));
                String dept=rs.getString("department");
                Stat st=stats.computeIfAbsent(dept,k->new Stat());
                if ("PASS".equals(r.status)) st.pass++; else st.fail++;
                st.sum+=r.gradePoint; st.n++;
            }
        }
        System.out.println("\n--- DEPARTMENT-WISE RESULT SUMMARY ---");
        for (java.util.Map.Entry<String,Stat> entry:stats.entrySet()) {
            Stat s=entry.getValue();
            System.out.printf(Locale.ROOT,"%s | Pass:%d | Fail/SA/AB:%d | Average Grade Point:%.2f%n",
                entry.getKey(),s.pass,s.fail,s.n==0?0:s.sum/s.n);
        }
    }
}
