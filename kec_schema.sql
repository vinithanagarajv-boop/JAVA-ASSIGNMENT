CREATE DATABASE IF NOT EXISTS kec_academic;
USE kec_academic;

CREATE TABLE IF NOT EXISTS students (
    roll_no VARCHAR(12) PRIMARY KEY,
    student_name VARCHAR(100) NOT NULL,
    department VARCHAR(20) NOT NULL,
    current_semester INT NOT NULL CHECK (current_semester BETWEEN 1 AND 8)
);

CREATE TABLE IF NOT EXISTS courses (
    course_code VARCHAR(12) PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    credits INT NOT NULL CHECK (credits > 0),
    course_type ENUM('THEORY','PRACTICAL','EMBEDDED') NOT NULL
);

CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_no VARCHAR(12) NOT NULL,
    course_code VARCHAR(12) NOT NULL,
    semester INT NOT NULL CHECK (semester BETWEEN 1 AND 8),
    classes_held INT NOT NULL DEFAULT 0 CHECK (classes_held >= 0),
    classes_attended INT NOT NULL DEFAULT 0 CHECK (classes_attended >= 0),
    medical_condonation BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (roll_no, course_code, semester),
    CONSTRAINT fk_enroll_student FOREIGN KEY (roll_no) REFERENCES students(roll_no),
    CONSTRAINT fk_enroll_course FOREIGN KEY (course_code) REFERENCES courses(course_code),
    CHECK (classes_attended <= classes_held)
);

CREATE TABLE IF NOT EXISTS assessment_marks (
    enrollment_id INT PRIMARY KEY,
    cia1 DECIMAL(5,2) NULL,
    cia2 DECIMAL(5,2) NULL,
    assignment_quiz_lab DECIMAL(5,2) NULL,
    ese_marks DECIMAL(5,2) NULL,
    ese_absent BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_marks_enrollment FOREIGN KEY (enrollment_id)
        REFERENCES enrollments(enrollment_id) ON DELETE CASCADE,
    CHECK (cia1 IS NULL OR cia1 BETWEEN 0 AND 100),
    CHECK (cia2 IS NULL OR cia2 BETWEEN 0 AND 100),
    CHECK (assignment_quiz_lab IS NULL OR assignment_quiz_lab BETWEEN 0 AND 100),
    CHECK (ese_marks IS NULL OR ese_marks BETWEEN 0 AND 100)
);
