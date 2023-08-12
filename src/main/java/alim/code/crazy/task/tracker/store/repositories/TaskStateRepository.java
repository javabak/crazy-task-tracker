package alim.code.crazy.task.tracker.store.repositories;

import alim.code.crazy.task.tracker.store.entities.TaskStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskStateRepository extends JpaRepository<TaskStateEntity, Long> {
}
