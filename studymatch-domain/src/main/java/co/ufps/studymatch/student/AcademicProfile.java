package co.ufps.studymatch.student;

import co.ufps.studymatch.ValidationException;
import co.ufps.studymatch.subject.SubjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * Entidad interna del agregado Student. Contiene la vida academica y la disponibilidad.
 */
public class AcademicProfile {
    public static final int MAX_SUBJECTS = 5;

    private final AcademicProfileId profileId;
    private String bio;
    private List<SubjectId> subjects;
    private AvailabilityState availabilityState;
    private List<AvailabilitySlot> availabilitySlots;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private AcademicProfile(AcademicProfileId profileId,
                            String bio,
                            List<SubjectId> subjects,
                            AvailabilityState availabilityState,
                            List<AvailabilitySlot> availabilitySlots,
                            LocalDateTime createdAt,
                            LocalDateTime updatedAt) {
        this.profileId = Objects.requireNonNull(profileId, "El profileId es obligatorio");
        this.bio = normalizeBio(bio);
        this.subjects = normalizeSubjects(subjects);
        this.availabilityState = Objects.requireNonNull(availabilityState, "El estado de disponibilidad es obligatorio");
        this.availabilitySlots = normalizeSlots(availabilitySlots);
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static AcademicProfile create(String bio,
                                         List<SubjectId> subjects,
                                         AvailabilityStatus availabilityStatus,
                                         List<AvailabilitySlot> availabilitySlots) {
        LocalDateTime now = LocalDateTime.now();
        return new AcademicProfile(
                AcademicProfileId.generate(),
                bio,
                subjects,
                AvailabilityStateFactory.from(availabilityStatus),
                availabilitySlots,
                now,
                now
        );
    }

    public static AcademicProfile of(AcademicProfileId profileId,
                                     String bio,
                                     List<SubjectId> subjects,
                                     AvailabilityStatus availabilityStatus,
                                     List<AvailabilitySlot> availabilitySlots,
                                     LocalDateTime createdAt,
                                     LocalDateTime updatedAt) {
        return new AcademicProfile(
                profileId,
                bio,
                subjects,
                AvailabilityStateFactory.from(availabilityStatus),
                availabilitySlots,
                createdAt,
                updatedAt
        );
    }

    public void update(String bio, List<SubjectId> newSubjects) {
        this.bio = normalizeBio(bio);
        this.subjects = normalizeSubjects(newSubjects);
        this.updatedAt = LocalDateTime.now();
    }

    public AvailabilityStatus changeAvailability(AvailabilityStatus target, List<AvailabilitySlot> slots) {
        this.availabilityState = availabilityState.transitionTo(target);
        this.availabilitySlots = normalizeSlots(slots);
        this.updatedAt = LocalDateTime.now();
        return availabilityState.status();
    }

    /**
     * Restauracion usada por el patron Command. No valida la transicion porque deshace un cambio ya ocurrido.
     */
    public void restoreAvailability(AvailabilityStatus target, List<AvailabilitySlot> slots) {
        this.availabilityState = AvailabilityStateFactory.from(target);
        this.availabilitySlots = normalizeSlots(slots);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasSubject(SubjectId subjectId) {
        return subjects.contains(subjectId);
    }

    public boolean isAvailableAt(AvailabilitySlot candidate) {
        if (availabilityState.status() != AvailabilityStatus.AVAILABLE) {
            return false;
        }
        return availabilitySlots.stream().anyMatch(slot -> slot.overlaps(candidate));
    }

    public List<SubjectId> sharedSubjectsWith(AcademicProfile other) {
        return subjects.stream().filter(other::hasSubject).toList();
    }

    private String normalizeBio(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.strip();
        if (normalized.length() > 500) {
            throw new ValidationException("La biografia no puede superar 500 caracteres");
        }
        return normalized;
    }

    private List<SubjectId> normalizeSubjects(List<SubjectId> value) {
        if (value == null || value.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<SubjectId> unique = new LinkedHashSet<>();
        for (SubjectId subject : value) {
            if (subject == null) {
                throw new ValidationException("Las asignaturas no pueden contener valores nulos");
            }
            unique.add(subject);
        }
        if (unique.size() > MAX_SUBJECTS) {
            throw new MaxSubjectsExceededException("Un estudiante puede tener como maximo "
                    + MAX_SUBJECTS + " asignaturas activas");
        }
        return List.copyOf(unique);
    }

    private List<AvailabilitySlot> normalizeSlots(List<AvailabilitySlot> value) {
        if (value == null || value.isEmpty()) {
            return List.of();
        }
        List<AvailabilitySlot> copy = new ArrayList<>(value);
        for (AvailabilitySlot slot : copy) {
            if (slot == null) {
                throw new ValidationException("Los horarios no pueden contener valores nulos");
            }
        }
        for (int i = 0; i < copy.size(); i++) {
            for (int j = i + 1; j < copy.size(); j++) {
                if (copy.get(i).overlaps(copy.get(j))) {
                    throw new ValidationException("Los horarios de disponibilidad no pueden solaparse");
                }
            }
        }
        return List.copyOf(copy);
    }

    public AcademicProfileId getProfileId() {
        return profileId;
    }

    public String getBio() {
        return bio;
    }

    public List<SubjectId> getSubjects() {
        return subjects;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityState.status();
    }

    public List<AvailabilitySlot> getAvailabilitySlots() {
        return availabilitySlots;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
