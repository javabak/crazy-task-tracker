package alim.code.crazy.task.tracker.api.controllers;

import alim.code.crazy.task.tracker.api.dto.AckDto;
import alim.code.crazy.task.tracker.api.dto.ProjectDto;
import alim.code.crazy.task.tracker.api.exceptions.BadRequestException;
import alim.code.crazy.task.tracker.api.exceptions.NotFoundException;
import alim.code.crazy.task.tracker.api.factories.ProjectDtoFactory;
import alim.code.crazy.task.tracker.store.entities.ProjectEntity;
import alim.code.crazy.task.tracker.store.repositories.ProjectRepository;
import ch.qos.logback.core.net.ssl.SSL;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.LongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@RestController
public class ProjectController {

    ProjectRepository projectRepository;
    ProjectDtoFactory projectDtoFactory;

    public static final String CREATE_PROJECT = "/api/projects";
    public static final String EDIT_PROJECT = "/api/projects/{project_id}";
    public static final String DELETE_PROJECT = "/api/projects/{project_id}";
    private static final String CREATE_OR_UPDATE_PROJECT = "/api/projects";

    @PostMapping(CREATE_PROJECT)
    public ProjectDto createProject(@RequestParam(value = "project_name") String projectName) {

        checkNameOrThrowException(projectName);

        projectRepository
                .findByName(projectName)
                .ifPresent(project -> {
                    throw new BadRequestException(String.format("Project \"%\" already exists.", projectName));
                });


        ProjectEntity project = projectRepository.saveAndFlush(
                ProjectEntity
                        .builder()
                        .name(projectName)
                        .build()
        );
        return projectDtoFactory.makeProjectDto(project);
    }

    @PutMapping(CREATE_OR_UPDATE_PROJECT)
    public ProjectDto createOrUpdateProject(
            @RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId,
            @RequestParam(value = "project_name", required = false) Optional<String> optionalProjectName) {

        optionalProjectName = optionalProjectName
                .filter(projectName -> !projectName.trim().isEmpty());

        boolean isCreate = !optionalProjectName.isPresent();

        ProjectEntity project = optionalProjectId
                .map(this::getProjectOrThrowException)
                .orElseGet(() -> ProjectEntity
                        .builder()
                        .build());

        if (isCreate && !optionalProjectName.isPresent()) {
            throw new BadRequestException("Project name can't be empty");
        }

        optionalProjectName
                .ifPresent(projectName -> {
                    projectRepository
                            .findByName(projectName)
                            .filter(anotherProject -> !Objects.equals(anotherProject.getId(), project.getId()))
                            .ifPresent(anotherProject -> {
                                throw new BadRequestException(
                                        String.format("Project \"%\" already exists.",
                                                projectName)
                                );
                            });
                    project.setName(projectName);
                });


        final ProjectEntity savedProject = projectRepository.saveAndFlush(project);

        return projectDtoFactory.makeProjectDto(savedProject);
    }

    @PatchMapping(EDIT_PROJECT)
    public ProjectDto editProject(@PathVariable("project_id") Long projectId,
                                  @RequestParam(value = "project_name") String projectName) {

        checkNameOrThrowException(projectName);

        ProjectEntity project = getProjectOrThrowException(projectId);

        projectRepository
                .findByName(projectName)
                .filter(anotherProject -> !Objects.equals(anotherProject.getId(), projectId))
                .ifPresent(anotherProject -> {
                    throw new BadRequestException(String.format("Project \"%\" already exists.", projectName));
                });

        project.setName(projectName);

        project = projectRepository.saveAndFlush(project);

        return projectDtoFactory.makeProjectDto(project);
    }

    @DeleteMapping(DELETE_PROJECT)
    public AckDto deleteProject(@PathVariable("project_id") Long projectId) {
        getProjectOrThrowException(projectId);

        projectRepository.deleteById(projectId);

        return AckDto.makeDefault(true);
    }

    private ProjectEntity getProjectOrThrowException(Long projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "project with \"%s\" doesn't exist",
                                        projectId
                                )
                        )
                );
    }

    private static void checkNameOrThrowException(String projectName) {
        if (projectName.trim().isEmpty()) {
            throw new BadRequestException(("Name can't be empty"));
        }
    }

}
