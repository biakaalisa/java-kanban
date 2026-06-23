import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;
import tasks.TaskType;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager() throws IOException;

    @BeforeEach
    void setUp() throws IOException {
        taskManager = createTaskManager();
    }

    @Test
    void testCreateTask() {
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 1, 10, 0);
        Task task = new Task("Task", "Description", TaskStatus.NEW, TaskType.TASK,
                Duration.ofMinutes(30), startTime);

        int taskId = taskManager.createTask(task);
        Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask);
        assertEquals("Task", savedTask.getTitle());
        assertEquals(Duration.ofMinutes(30), savedTask.getDuration());
        assertEquals(startTime, savedTask.getStartTime());
        assertEquals(startTime.plusMinutes(30), savedTask.getEndTime());
    }

    @Test
    void testCreateEpic() {
        Epic epic = new Epic("Epic", "Description");

        int epicId = taskManager.createEpic(epic);
        Epic savedEpic = taskManager.getEpicById(epicId);

        assertNotNull(savedEpic);
        assertEquals(TaskStatus.NEW, savedEpic.getStatus());
        assertEquals(Duration.ZERO, savedEpic.getDuration());
        assertNull(savedEpic.getStartTime());
        assertNull(savedEpic.getEndTime());
    }

    @Test
    void testCreateSubtaskWithEpic() {
        int epicId = taskManager.createEpic(new Epic("Epic", "Description"));
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 1, 11, 0);
        Subtask subtask = new Subtask("Subtask", "Description", TaskStatus.NEW, epicId,
                Duration.ofMinutes(40), startTime);

        int subtaskId = taskManager.createSubtask(subtask);
        Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);
        Epic savedEpic = taskManager.getEpicById(epicId);

        assertNotNull(savedSubtask);
        assertEquals(epicId, savedSubtask.getEpicId());
        assertTrue(savedEpic.getSubtaskIds().contains(subtaskId));
    }

    @Test
    void testSubtaskIsNotCreatedWithoutEpic() {
        Subtask subtask = new Subtask("Subtask", "Description", TaskStatus.NEW, 999,
                Duration.ofMinutes(10), LocalDateTime.of(2026, 1, 1, 12, 0));

        int subtaskId = taskManager.createSubtask(subtask);

        assertEquals(-1, subtaskId);
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    void testGetTaskByIdAddsTaskToHistory() {
        int taskId = taskManager.createTask(new Task("Task", "Description", TaskStatus.NEW, TaskType.TASK));

        taskManager.getTaskById(taskId);

        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(taskId, history.get(0).getId());
    }

    @Test
    void testUpdateTask() {
        int taskId = taskManager.createTask(new Task("Old", "Old description", TaskStatus.NEW, TaskType.TASK,
                Duration.ofMinutes(20), LocalDateTime.of(2026, 1, 1, 10, 0)));
        Task updatedTask = new Task(taskId, "New", "New description", TaskStatus.DONE, TaskType.TASK,
                Duration.ofMinutes(50), LocalDateTime.of(2026, 1, 1, 12, 0));

        taskManager.updateTask(updatedTask);
        Task savedTask = taskManager.getTaskById(taskId);

        assertEquals("New", savedTask.getTitle());
        assertEquals("New description", savedTask.getDescription());
        assertEquals(TaskStatus.DONE, savedTask.getStatus());
        assertEquals(Duration.ofMinutes(50), savedTask.getDuration());
        assertEquals(LocalDateTime.of(2026, 1, 1, 12, 0), savedTask.getStartTime());
    }

    @Test
    void testUpdateSubtaskRecalculatesEpicFields() {
        int epicId = taskManager.createEpic(new Epic("Epic", "Description"));
        int subtaskId = taskManager.createSubtask(new Subtask("Subtask", "Description", TaskStatus.NEW, epicId,
                Duration.ofMinutes(20), LocalDateTime.of(2026, 1, 1, 10, 0)));
        Subtask updatedSubtask = new Subtask(subtaskId, "Subtask", "Description", TaskStatus.DONE, epicId,
                Duration.ofMinutes(35), LocalDateTime.of(2026, 1, 1, 11, 0));

        taskManager.updateSubtask(updatedSubtask);
        Epic epic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.DONE, epic.getStatus());
        assertEquals(Duration.ofMinutes(35), epic.getDuration());
        assertEquals(LocalDateTime.of(2026, 1, 1, 11, 0), epic.getStartTime());
        assertEquals(LocalDateTime.of(2026, 1, 1, 11, 35), epic.getEndTime());
    }

    @Test
    void testDeleteTaskRemovesItFromHistoryAndPrioritizedTasks() {
        int taskId = taskManager.createTask(new Task("Task", "Description", TaskStatus.NEW, TaskType.TASK,
                Duration.ofMinutes(20), LocalDateTime.of(2026, 1, 1, 10, 0)));
        taskManager.getTaskById(taskId);

        taskManager.deleteTask(taskId);

        assertNull(taskManager.getTaskById(taskId));
        assertTrue(taskManager.getHistory().isEmpty());
        assertTrue(taskManager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void testDeleteSubtaskRemovesIdFromEpic() {
        int epicId = taskManager.createEpic(new Epic("Epic", "Description"));
        int subtaskId = taskManager.createSubtask(new Subtask("Subtask", "Description", TaskStatus.NEW, epicId,
                Duration.ofMinutes(20), LocalDateTime.of(2026, 1, 1, 10, 0)));

        taskManager.deleteSubtask(subtaskId);
        Epic epic = taskManager.getEpicById(epicId);

        assertNull(taskManager.getSubtaskById(subtaskId));
        assertFalse(epic.getSubtaskIds().contains(subtaskId));
        assertTrue(taskManager.getEpicSubtasks(epicId).isEmpty());
    }

    @Test
    void testDeleteEpicRemovesEpicAndSubtasks() {
        int epicId = taskManager.createEpic(new Epic("Epic", "Description"));
        int firstSubtaskId = taskManager.createSubtask(new Subtask("First", "Description", TaskStatus.NEW, epicId,
                Duration.ofMinutes(20), LocalDateTime.of(2026, 1, 1, 10, 0)));
        int secondSubtaskId = taskManager.createSubtask(new Subtask("Second", "Description", TaskStatus.NEW, epicId,
                Duration.ofMinutes(20), LocalDateTime.of(2026, 1, 1, 11, 0)));

        taskManager.deleteEpic(epicId);

        assertNull(taskManager.getEpicById(epicId));
        assertNull(taskManager.getSubtaskById(firstSubtaskId));
        assertNull(taskManager.getSubtaskById(secondSubtaskId));
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    void testEpicTimeFieldsCalculatedFromSubtasks() {
        int epicId = taskManager.createEpic(new Epic("Epic", "Description"));
        taskManager.createSubtask(new Subtask("Late", "Description", TaskStatus.NEW, epicId,
                Duration.ofMinutes(40), LocalDateTime.of(2026, 1, 1, 12, 0)));
        taskManager.createSubtask(new Subtask("Early", "Description", TaskStatus.NEW, epicId,
                Duration.ofMinutes(30), LocalDateTime.of(2026, 1, 1, 10, 0)));

        Epic epic = taskManager.getEpicById(epicId);

        assertEquals(Duration.ofMinutes(70), epic.getDuration());
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0), epic.getStartTime());
        assertEquals(LocalDateTime.of(2026, 1, 1, 12, 40), epic.getEndTime());
    }

    @Test
    void testGetPrioritizedTasksReturnsTasksSortedByStartTime() {
        int lateTaskId = taskManager.createTask(new Task("Late", "Description", TaskStatus.NEW, TaskType.TASK,
                Duration.ofMinutes(10), LocalDateTime.of(2026, 1, 1, 15, 0)));
        int earlyTaskId = taskManager.createTask(new Task("Early", "Description", TaskStatus.NEW, TaskType.TASK,
                Duration.ofMinutes(10), LocalDateTime.of(2026, 1, 1, 10, 0)));
        int taskWithoutStartId = taskManager.createTask(new Task("Without start", "Description", TaskStatus.NEW,
                TaskType.TASK));

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(2, prioritizedTasks.size());
        assertEquals(earlyTaskId, prioritizedTasks.get(0).getId());
        assertEquals(lateTaskId, prioritizedTasks.get(1).getId());
        assertNotEquals(taskWithoutStartId, prioritizedTasks.get(0).getId());
        assertNotEquals(taskWithoutStartId, prioritizedTasks.get(1).getId());
    }

    @Test
    void testCreateTaskWithTimeIntersectionThrowsException() {
        taskManager.createTask(new Task("First", "Description", TaskStatus.NEW, TaskType.TASK,
                Duration.ofMinutes(60), LocalDateTime.of(2026, 1, 1, 10, 0)));
        Task intersectingTask = new Task("Second", "Description", TaskStatus.NEW, TaskType.TASK,
                Duration.ofMinutes(30), LocalDateTime.of(2026, 1, 1, 10, 30));

        assertThrows(IllegalArgumentException.class, () -> taskManager.createTask(intersectingTask));
    }

    @Test
    void testCreateTaskWithoutTimeIntersectionDoesNotThrowException() {
        taskManager.createTask(new Task("First", "Description", TaskStatus.NEW, TaskType.TASK,
                Duration.ofMinutes(60), LocalDateTime.of(2026, 1, 1, 10, 0)));
        Task notIntersectingTask = new Task("Second", "Description", TaskStatus.NEW, TaskType.TASK,
                Duration.ofMinutes(30), LocalDateTime.of(2026, 1, 1, 11, 0));

        assertDoesNotThrow(() -> taskManager.createTask(notIntersectingTask));
    }

    @Test
    void testUpdateTaskWithTimeIntersectionThrowsException() {
        taskManager.createTask(new Task("First", "Description", TaskStatus.NEW, TaskType.TASK,
                Duration.ofMinutes(60), LocalDateTime.of(2026, 1, 1, 10, 0)));
        int secondTaskId = taskManager.createTask(new Task("Second", "Description", TaskStatus.NEW, TaskType.TASK,
                Duration.ofMinutes(30), LocalDateTime.of(2026, 1, 1, 12, 0)));
        Task updatedTask = new Task(secondTaskId, "Second", "Description", TaskStatus.NEW, TaskType.TASK,
                Duration.ofMinutes(30), LocalDateTime.of(2026, 1, 1, 10, 30));

        assertThrows(IllegalArgumentException.class, () -> taskManager.updateTask(updatedTask));
    }
}
