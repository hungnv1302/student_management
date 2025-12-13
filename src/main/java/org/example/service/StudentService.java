package org.example.service;

import org.example.dto.StudentDTO;
import org.example.mapper.StudentMapper;
import org.example.repository.StudentRepository;
import org.example.service.exception.BusinessException;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class StudentService {
    private final StudentRepository repo;

    public StudentService(StudentRepository repo) {
        this.repo = repo;
    }

    public boolean create(StudentDTO dto) throws SQLException {
        validate(dto);

        if (repo.findById(dto.getStudentID()).isPresent()) {
            throw new BusinessException("StudentID đã tồn tại: " + dto.getStudentID());
        }
        return repo.insert(StudentMapper.toEntity(dto));
    }

    public StudentDTO getById(String studentId) throws SQLException {
        return repo.findById(studentId)
                .map(StudentMapper::toDTO)
                .orElseThrow(() -> new BusinessException("Không tìm thấy sinh viên: " + studentId));
    }

    public List<StudentDTO> getAll() throws SQLException {
        return repo.findAll()
                .stream()
                .map(StudentMapper::toDTO)
                .collect(Collectors.toList());
    }

    public boolean update(StudentDTO dto) throws SQLException {
        validate(dto);

        if (repo.findById(dto.getStudentID()).isEmpty()) {
            throw new BusinessException("Không tìm thấy sinh viên để update: " + dto.getStudentID());
        }
        return repo.update(StudentMapper.toEntity(dto));
    }

    public boolean delete(String studentId) throws SQLException {
        if (studentId == null || studentId.isBlank()) {
            throw new BusinessException("StudentID không hợp lệ");
        }
        return repo.deleteById(studentId.trim());
    }

    private void validate(StudentDTO dto) {
        if (dto == null) throw new BusinessException("Dữ liệu sinh viên trống");
        if (dto.getStudentID() == null || dto.getStudentID().isBlank())
            throw new BusinessException("StudentID bắt buộc");
        if (dto.getFullName() == null || dto.getFullName().isBlank())
            throw new BusinessException("Họ tên bắt buộc");

        // Enum string phải khớp name() (MALE/FEMALE..., STUDYING/...)
        // Nếu UI của cháu dùng ComboBox enum.name() thì sẽ luôn đúng.
    }
}
