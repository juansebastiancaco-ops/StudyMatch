package co.ufps.studymatch.application.auth;

import co.ufps.studymatch.ValidationException;
import co.ufps.studymatch.application.common.AvailabilitySlotInput;
import co.ufps.studymatch.student.AvailabilitySlot;
import co.ufps.studymatch.student.AvailabilityStatus;
import co.ufps.studymatch.subject.SubjectId;

import java.util.List;

public record RegisterStudentCommand(
        String email,
        String password,
        String fullName,
        String career,
        int semester,
        String bio,
        List<String> subjectIds,
        String availabilityStatus,
        List<AvailabilitySlotInput> availabilitySlots
) {
    public RegisterStudentCommand {
        if (email == null || email.isBlank()) {
            throw new ValidationException("El email es obligatorio");
        }
        if (password == null || password.length() < 8) {
            throw new ValidationException("La contrasena debe tener al menos 8 caracteres");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("El nombre completo es obligatorio");
        }
        if (career == null || career.isBlank()) {
            throw new ValidationException("La carrera es obligatoria");
        }
        if (semester < 1 || semester > 12) {
            throw new ValidationException("El semestre debe estar entre 1 y 12");
        }
        subjectIds = subjectIds == null ? List.of() : List.copyOf(subjectIds);
        availabilitySlots = availabilitySlots == null ? List.of() : List.copyOf(availabilitySlots);
    }

    public List<SubjectId> toSubjectIds() {
        return subjectIds.stream().map(SubjectId::of).toList();
    }

    public AvailabilityStatus toAvailabilityStatus() {
        return AvailabilityStatus.fromString(availabilityStatus);
    }

    public List<AvailabilitySlot> toAvailabilitySlots() {
        return availabilitySlots.stream().map(AvailabilitySlotInput::toDomain).toList();
    }
}
