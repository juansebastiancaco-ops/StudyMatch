package co.ufps.studymatch.application.group;

import co.ufps.studymatch.ValidationException;
import co.ufps.studymatch.application.common.PageResult;
import co.ufps.studymatch.group.GroupModality;
import co.ufps.studymatch.group.GroupStatus;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.subject.SubjectId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class SearchGroupsQuery {
    private final IStudyGroupRepository groupRepository;
    private final GroupDtoAssembler groupAssembler;

    @Inject
    public SearchGroupsQuery(IStudyGroupRepository groupRepository, GroupDtoAssembler groupAssembler) {
        this.groupRepository = groupRepository;
        this.groupAssembler = groupAssembler;
    }

    public PageResult<StudyGroupSummaryDTO> execute(String subjectId,
                                                    String status,
                                                    String texto,
                                                    String modality,
                                                    int page,
                                                    int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, Math.min(50, size == 0 ? 10 : size));
        SubjectId subject = subjectId == null || subjectId.isBlank() ? null : SubjectId.of(subjectId);
        GroupStatus groupStatus = status == null || status.isBlank()
                ? GroupStatus.OPEN
                : parseStatus(status);
        if (groupStatus == GroupStatus.DRAFT) {
            throw new ValidationException("Los grupos DRAFT no se publican en la busqueda; consulta los grupos del estudiante");
        }
        GroupModality groupModality = modality == null || modality.isBlank()
                ? null
                : GroupModality.fromString(modality);

        long total = groupRepository.contar(subject, groupStatus, texto, groupModality);
        List<StudyGroup> groups = groupRepository.buscar(subject, groupStatus, texto, groupModality,
                (safePage - 1) * safeSize, safeSize);
        List<StudyGroupSummaryDTO> items = groups.stream()
                .map(group -> groupAssembler.toSummary(group, null, List.of()))
                .toList();
        return PageResult.of(items, safePage, safeSize, total);
    }

    private GroupStatus parseStatus(String status) {
        try {
            return GroupStatus.valueOf(status.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Estado de grupo invalido: " + status);
        }
    }
}
