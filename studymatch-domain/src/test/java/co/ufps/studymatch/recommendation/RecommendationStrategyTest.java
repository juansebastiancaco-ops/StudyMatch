package co.ufps.studymatch.recommendation;

import co.ufps.studymatch.group.GroupModality;
import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.student.AvailabilitySlot;
import co.ufps.studymatch.student.AvailabilityStatus;
import co.ufps.studymatch.student.Student;
import co.ufps.studymatch.subject.SubjectId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationStrategyTest {
    private final SubjectId sharedSubject = SubjectId.generate();
    private final SubjectId extraSubject = SubjectId.generate();

    private Student student(String email, String career, int semester,
                            List<SubjectId> subjects, List<AvailabilitySlot> slots) {
        return Student.register(email, "$2a$10$hash", "Estudiante", career, semester,
                null, subjects, AvailabilityStatus.AVAILABLE, slots);
    }

    @Test
    void calculaAfinidadDeAsignaturasComoPorcentajeDelOrigen() {
        SubjectAffinityStrategy strategy = new SubjectAffinityStrategy();
        Student source = student("origen@ufps.edu.co", "Ingeniería de Sistemas", 5,
                List.of(sharedSubject, extraSubject), List.of());
        Student candidate = student("candidato@ufps.edu.co", "Ingeniería de Sistemas", 5,
                List.of(sharedSubject), List.of());

        double score = strategy.score(RecommendationContext.forStudent(source, candidate,
                List.of(), List.of())).value();

        assertEquals(50.0, score);
    }

    @Test
    void calculaSolapamientoDeHorarioSobreElTiempoDelOrigen() {
        ScheduleOverlapStrategy strategy = new ScheduleOverlapStrategy();
        Student source = student("horario-origen@ufps.edu.co", "Ingeniería de Sistemas", 5,
                List.of(), List.of(AvailabilitySlot.create("MONDAY", "08:00", "10:00")));
        Student candidate = student("horario-candidato@ufps.edu.co", "Ingeniería Civil", 4,
                List.of(), List.of(AvailabilitySlot.create("MONDAY", "09:00", "11:00")));

        double score = strategy.score(RecommendationContext.forStudent(source, candidate,
                List.of(), List.of())).value();

        assertEquals(50.0, score);
    }

    @Test
    void laEstrategiaCompuestaAplicaLosPesosConfigurados() {
        Student source = student("compuesta-origen@ufps.edu.co", "Ingeniería de Sistemas", 5,
                List.of(sharedSubject, extraSubject), List.of(AvailabilitySlot.create("MONDAY", "08:00", "10:00")));
        Student candidate = student("compuesta-candidato@ufps.edu.co", "Ingeniería de Sistemas", 5,
                List.of(sharedSubject), List.of(AvailabilitySlot.create("MONDAY", "08:00", "10:00")));

        List<RecommendationStrategy> strategies = List.of(
                new SubjectAffinityStrategy(),
                new ScheduleOverlapStrategy(),
                new CareerDiversityStrategy(),
                new AcademicLevelStrategy()
        );
        CompositeRecommendationStrategy composite = new CompositeRecommendationStrategy(
                strategies, RecommendationWeights.defaults().asMap());

        double score = composite.score(RecommendationContext.forStudent(source, candidate,
                List.of(), List.of())).value();

        assertEquals(70.5, score, 0.01);
        assertEquals(4, composite.getStrategies().size());
        assertEquals(0.35, composite.getWeights().get("subject"));
    }

    @Test
    void laEstrategiaCompuestaParaGruposCombinaDiversidadYNivel() {
        Student source = student("grupo-origen@ufps.edu.co", "Ingeniería de Sistemas", 5,
                List.of(sharedSubject), List.of(AvailabilitySlot.create("MONDAY", "08:00", "10:00")));
        Student leader = student("grupo-lider@ufps.edu.co", "Ingeniería Electrónica", 6,
                List.of(sharedSubject), List.of());
        StudyGroup group = StudyGroup.create(sharedSubject, "Grupo de prueba", "Descripción",
                leader.getStudentId(), 4,
                List.of(AvailabilitySlot.create("MONDAY", "08:00", "10:00")), GroupModality.VIRTUAL);

        CompositeRecommendationStrategy composite = new CompositeRecommendationStrategy(
                List.of(new SubjectAffinityStrategy(), new ScheduleOverlapStrategy(),
                        new CareerDiversityStrategy(), new AcademicLevelStrategy()),
                RecommendationWeights.defaults().asMap());

        double score = composite.score(RecommendationContext.forGroup(source, group,
                List.of(leader), List.of())).value();

        assertTrue(score >= 90, "El grupo debe puntuar alto: " + score);
    }

    @Test
    void elPuntajeNuncaSaleDelRangoPermitido() {
        assertEquals(0, RecommendationScore.of(-20).value());
        assertEquals(100, RecommendationScore.of(250).value());
        assertEquals(50, RecommendationScore.of(50.004).value());
    }
}
