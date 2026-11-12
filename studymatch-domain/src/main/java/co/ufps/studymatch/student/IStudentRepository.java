package co.ufps.studymatch.student;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IStudentRepository {
    Student guardar(Student student);

    Optional<Student> buscarPorEmail(String email);

    boolean existeEmail(String email);

    Optional<Student> buscarId(StudentId studentId);

    List<Student> buscarTodos();

    List<Student> buscarPorIds(Collection<StudentId> studentIds);
}
