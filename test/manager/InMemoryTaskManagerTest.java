package manager;

import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;
import tasks.TaskType;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() throws IOException {
        return new InMemoryTaskManager();
    }

    @Test
    void testReturnedTaskDoesNotChangeManagerData() {
        int taskId = taskManager.createTask(new Task("Original", "Description", TaskStatus.NEW, TaskType.TASK,
                Duration.ofMinutes(10), LocalDateTime.of(2026, 1, 1, 10, 0)));
        Task task = taskManager.getTaskById(taskId);

        task.setTitle("Changed");
        task.setStatus(TaskStatus.DONE);

        Task savedTask = taskManager.getTaskById(taskId);
        assertEquals("Original", savedTask.getTitle());
        assertEquals(TaskStatus.NEW, savedTask.getStatus());
    }
}
