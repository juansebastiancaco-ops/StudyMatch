package co.ufps.studymatch.application.subject;

import co.ufps.studymatch.subject.Subject;

public record SubjectDTO(
        String subjectId,
        String code,
        String name,
        String area,
        int credits,
        int semester
) {
    public static SubjectDTO from(Subject subject) {
        return new SubjectDTO(
                subject.getSubjectId().value(),
                subject.getCode(),
                subject.getName(),
                subject.getArea(),
                subject.getCredits(),
                subject.getSemester()
        );
    }
}
