package alim.code.crazy.task.tracker.api.factories;

import alim.code.crazy.task.tracker.api.dto.TaskDto;
import alim.code.crazy.task.tracker.api.dto.TaskStateDto;
import alim.code.crazy.task.tracker.store.entities.TaskEntity;
import alim.code.crazy.task.tracker.store.entities.TaskStateEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskDtoFactory {

    public TaskDto makeTaskStateDto(TaskEntity entity) {
        return TaskDto
                .builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
