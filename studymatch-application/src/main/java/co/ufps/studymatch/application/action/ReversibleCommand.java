package co.ufps.studymatch.application.action;

import co.ufps.studymatch.student.StudentId;

/**
 * Patron Command de GoF. Se diferencia de los Command de CQRS porque sabe deshacerse.
 */
public interface ReversibleCommand {
    String actionId();

    StudentId studentId();

    String type();

    String description();

    void execute();

    void undo();
}
