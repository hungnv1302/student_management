package org.example.controller.student;

public interface StudentViewContextAware {
    void setContext(Long studentId, String username);
}
