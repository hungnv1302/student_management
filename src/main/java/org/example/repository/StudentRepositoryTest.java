package org.example.repository;

import org.example.domain.Student;
import org.example.domain.enums.Gender;
import org.example.domain.enums.StudentStatus;

import java.time.LocalDate;

public class StudentRepositoryTest {
    public static void main(String[] args) throws Exception {
        StudentRepository repo = new StudentRepository();

        Student s = new Student();
        s.setStudentID("SV001");
        s.setFullName("Nguyen Van A");
        s.setDateOfBirth(LocalDate.of(2004, 1, 1));
        s.setGender(Gender.MALE);
        s.setEmail("a@gmail.com");
        s.setPhoneNumber("0123456789");
        s.setAddress("Ha Noi");

        s.setDepartment("CNTT");
        s.setMajor("KTPM");
        s.setClassName("K67");
        s.setAdmissionYear(2022);
        s.setGpa(3.2);
        s.setTrainingScore(85);
        s.setStatus(StudentStatus.STUDYING);

        System.out.println("Insert: " + repo.insert(s));
        System.out.println("FindById: " + repo.findById("SV001").orElse(null));

        s.setGpa(3.6);
        System.out.println("Update: " + repo.update(s));

        System.out.println("All size: " + repo.findAll().size());

        System.out.println("Delete: " + repo.deleteById("SV001"));
    }
}
