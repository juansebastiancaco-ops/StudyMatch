package co.ufps.studymatch.group;

import co.ufps.studymatch.ConflictException;
import co.ufps.studymatch.ValidationException;
import co.ufps.studymatch.student.AvailabilitySlot;
import co.ufps.studymatch.student.Student;
import co.ufps.studymatch.student.AvailabilityStatus;
import co.ufps.studymatch.student.StudentId;
import co.ufps.studymatch.subject.SubjectId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudyGroupTest {

    private Student student(String email, String... days) {
        List<AvailabilitySlot> slots = java.util.Arrays.stream(days)
                .map(day -> AvailabilitySlot.create(day, "14:00", "16:00"))
                .toList();
        return Student.register(email, "$2a$10$hash", "Estudiante", "Ingeniería de Sistemas", 5,
                null, List.of(), AvailabilityStatus.AVAILABLE, slots);
    }

    private StudyGroup openGroup(Student leader, int maxMembers) {
        StudyGroup group = StudyGroup.create(
                SubjectId.generate(),
                "Grupo de estudio",
                "Descripción del grupo",
                leader.getStudentId(),
                maxMembers,
                List.of(AvailabilitySlot.create("MONDAY", "14:00", "16:00")),
                GroupModality.HYBRID
        );
        group.changeStatus(leader.getStudentId(), GroupStatus.OPEN);
        return group;
    }

    @Test
    void naceEnDraftConElCreadorComoLider() {
        Student leader = student("lider@ufps.edu.co", "MONDAY");
        StudyGroup group = StudyGroup.create(
                SubjectId.generate(), "Álgebra", "Grupo de álgebra", leader.getStudentId(), 4,
                List.of(AvailabilitySlot.create("MONDAY", "08:00", "10:00")), GroupModality.VIRTUAL);

        assertEquals(GroupStatus.DRAFT, group.getStatus());
        assertEquals(1, group.getMemberCount());
        assertTrue(group.isLeader(leader.getStudentId()));
    }

    @Test
    void soloGruposOpenAceptanSolicitudes() {
        Student leader = student("lider-draft@ufps.edu.co", "MONDAY");
        Student applicant = student("solicitante@ufps.edu.co", "MONDAY");
        StudyGroup group = StudyGroup.create(
                SubjectId.generate(), "Cálculo", "Grupo de cálculo", leader.getStudentId(), 4,
                List.of(AvailabilitySlot.create("MONDAY", "14:00", "16:00")), GroupModality.VIRTUAL);

        assertThrows(ConflictException.class,
                () -> group.sendJoinRequest(applicant.getStudentId(), "Hola", true));
    }

    @Test
    void aceptarSolicitudAgregaMiembroYPasaAFull() {
        Student leader = student("lider-full@ufps.edu.co", "MONDAY");
        Student applicant = student("aspirante@ufps.edu.co", "MONDAY");
        StudyGroup group = openGroup(leader, 2);
        JoinRequest request = group.sendJoinRequest(applicant.getStudentId(), "Quiero unirme", true);

        group.acceptRequest(request.getRequestId(), leader.getStudentId());

        assertEquals(RequestStatus.ACCEPTED, request.getStatus());
        assertEquals(2, group.getMemberCount());
        assertEquals(GroupStatus.FULL, group.getStatus());
        assertTrue(group.isMember(applicant.getStudentId()));
        assertThrows(ConflictException.class,
                () -> group.sendJoinRequest(student("otro@ufps.edu.co", "MONDAY").getStudentId(), "Hola", true));
    }

    @Test
    void rechazarSolicitudNoAgregaMiembro() {
        Student leader = student("lider-reject@ufps.edu.co", "MONDAY");
        Student applicant = student("rechazado@ufps.edu.co", "MONDAY");
        StudyGroup group = openGroup(leader, 4);
        JoinRequest request = group.sendJoinRequest(applicant.getStudentId(), "Hola", true);

        group.rejectRequest(request.getRequestId(), leader.getStudentId());

        assertEquals(RequestStatus.REJECTED, request.getStatus());
        assertFalse(group.isMember(applicant.getStudentId()));
    }

    @Test
    void soloElLiderPuedeAceptarSolicitudes() {
        Student leader = student("lider-perm@ufps.edu.co", "MONDAY");
        Student applicant = student("aspirante-perm@ufps.edu.co", "MONDAY");
        Student intruder = student("intruso@ufps.edu.co", "MONDAY");
        StudyGroup group = openGroup(leader, 4);
        JoinRequest request = group.sendJoinRequest(applicant.getStudentId(), "Hola", true);

        assertThrows(NotGroupLeaderException.class,
                () -> group.acceptRequest(request.getRequestId(), intruder.getStudentId()));
    }

    @Test
    void liderQueSaleTransfiereElLiderazgo() {
        Student leader = student("lider-salida@ufps.edu.co", "MONDAY");
        Student applicant = student("miembro@ufps.edu.co", "MONDAY");
        StudyGroup group = openGroup(leader, 4);
        JoinRequest request = group.sendJoinRequest(applicant.getStudentId(), "Hola", true);
        group.acceptRequest(request.getRequestId(), leader.getStudentId());

        group.leaveGroup(leader.getStudentId());

        assertTrue(group.isLeader(applicant.getStudentId()));
        assertFalse(group.isMember(leader.getStudentId()));
    }

    @Test
    void validaTransicionesDeLaMaquinaDeEstados() {
        assertThrows(InvalidGroupTransitionException.class,
                () -> GroupStatus.DRAFT.transitionTo(GroupStatus.COMPLETED));
        assertEquals(GroupStatus.OPEN, GroupStatus.DRAFT.transitionTo(GroupStatus.OPEN));
        assertEquals(GroupStatus.ARCHIVED, GroupStatus.COMPLETED.transitionTo(GroupStatus.ARCHIVED));
    }

    @Test
    void validaCapacidadYHorariosDelGrupo() {
        Student leader = student("lider-validacion@ufps.edu.co", "MONDAY");
        assertThrows(ValidationException.class, () -> StudyGroup.create(
                SubjectId.generate(), "X", null, leader.getStudentId(), 1,
                List.of(AvailabilitySlot.create("MONDAY", "08:00", "10:00")), GroupModality.VIRTUAL));
        assertThrows(ValidationException.class, () -> StudyGroup.create(
                SubjectId.generate(), "Grupo válido", null, leader.getStudentId(), 4,
                List.of(), GroupModality.VIRTUAL));
    }
}
