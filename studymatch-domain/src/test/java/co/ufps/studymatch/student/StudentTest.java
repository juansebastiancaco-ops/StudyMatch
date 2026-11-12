package co.ufps.studymatch.student;

import co.ufps.studymatch.ValidationException;
import co.ufps.studymatch.group.StudyGroupId;
import co.ufps.studymatch.subject.SubjectId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentTest {

    private Student newStudent(String email, List<SubjectId> subjects) {
        return Student.register(
                email,
                "$2a$10$hash",
                "Estudiante de Prueba",
                "Ingeniería de Sistemas",
                5,
                "Bio de prueba",
                subjects,
                AvailabilityStatus.AVAILABLE,
                List.of(AvailabilitySlot.create("MONDAY", "08:00", "10:00"))
        );
    }

    @Test
    void registraEstudianteActivoConPerfilAcademico() {
        Student student = newStudent("laura@ufps.edu.co", List.of(SubjectId.generate()));

        assertEquals(StudentStatus.ACTIVE, student.getStatus());
        assertTrue(student.isActive());
        assertEquals("laura@ufps.edu.co", student.getEmail());
        assertEquals(1, student.getSubjectIds().size());
        assertEquals(AvailabilityStatus.AVAILABLE, student.getAvailabilityStatus());
    }

    @Test
    void rechazaEmailInvalidoYSemestreFueraDeRango() {
        assertThrows(ValidationException.class, () -> newStudent("correo-invalido", List.of()));
        assertThrows(ValidationException.class, () -> Student.register(
                "valido@ufps.edu.co", "$2a$10$hash", "Nombre", "Ingeniería", 13,
                null, List.of(), AvailabilityStatus.AVAILABLE, List.of()));
    }

    @Test
    void imponeMaximoDeCincoGruposActivos() {
        Student student = newStudent("grupos@ufps.edu.co", List.of());
        for (int i = 0; i < Student.MAX_ACTIVE_GROUPS; i++) {
            student.joinGroup(StudyGroupId.generate());
        }

        assertThrows(MaxActiveGroupsExceededException.class,
                () -> student.joinGroup(StudyGroupId.generate()));
        assertEquals(5, student.getActiveGroupCount());
    }

    @Test
    void identificaAsignaturasCompartidas() {
        SubjectId calculo = SubjectId.generate();
        SubjectId programacion = SubjectId.generate();
        Student first = newStudent("uno@ufps.edu.co", List.of(calculo, programacion));
        Student second = newStudent("dos@ufps.edu.co", List.of(programacion));

        List<SubjectId> shared = first.sharedSubjectsWith(second);

        assertEquals(List.of(programacion), shared);
    }

    @Test
    void detectaSolapamientoDeHorariosDelGrupo() {
        Student student = newStudent("horario@ufps.edu.co", List.of());
        List<AvailabilitySlot> compatible = List.of(AvailabilitySlot.create("MONDAY", "09:00", "11:00"));
        List<AvailabilitySlot> incompatible = List.of(AvailabilitySlot.create("TUESDAY", "09:00", "11:00"));

        assertTrue(student.hasScheduleOverlapWith(compatible));
        assertTrue(!student.hasScheduleOverlapWith(incompatible));
    }

    @Nested
    class DisponibilidadConState {

        @Test
        void permiteTransicionesValidasDesdeAvailable() {
            Student student = newStudent("disponible@ufps.edu.co", List.of());
            List<AvailabilitySlot> slots = List.of(AvailabilitySlot.create("MONDAY", "14:00", "16:00"));

            assertEquals(AvailabilityStatus.BUSY, student.changeAvailability(AvailabilityStatus.BUSY, slots));
            assertEquals(AvailabilityStatus.OFFLINE,
                    student.changeAvailability(AvailabilityStatus.OFFLINE, slots));
            assertEquals(AvailabilityStatus.AVAILABLE,
                    student.changeAvailability(AvailabilityStatus.AVAILABLE, slots));
        }

        @Test
        void rechazaTransicionInvalidaDeBusyAInClass() {
            Student student = newStudent("ocupado@ufps.edu.co", List.of());
            student.changeAvailability(AvailabilityStatus.BUSY, List.of());

            assertThrows(InvalidAvailabilityTransitionException.class,
                    () -> student.changeAvailability(AvailabilityStatus.IN_CLASS, List.of()));
        }

        @Test
        void restauraDisponibilidadSinValidarTransicion() {
            Student student = newStudent("restaura@ufps.edu.co", List.of());
            student.changeAvailability(AvailabilityStatus.OFFLINE, List.of());

            student.restoreAvailability(AvailabilityStatus.AVAILABLE, List.of());

            assertEquals(AvailabilityStatus.AVAILABLE, student.getAvailabilityStatus());
        }
    }
}
