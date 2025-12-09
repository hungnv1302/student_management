package org.example.repository;

import org.example.domain.Person;
import org.example.domain.RoleType;
import org.example.domain.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Database {

    private final List<User> users = new ArrayList<>();

    public Database() {

        // Tạo Person giả để không bị lỗi constructor
        Person p1 = new Person();
        Person p2 = new Person();
        Person p3 = new Person();

        // Tạo user mẫu
        users.add(new User("1", "admin", "123456", RoleType.ADMIN, p1));
        users.add(new User("2", "student1", "123456", RoleType.STUDENT, p2));
        users.add(new User("3", "lect1", "123456", RoleType.LECTURER, p3));
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }
}
