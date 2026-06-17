import history.HistoryManager;
import history.InMemoryHistoryManager;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;
import tasks.TaskType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private TaskManager taskManager;
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void testHistoryPreservesOrder() {
        Task task = new Task("Задача 1", "Описание", TaskStatus.NEW, TaskType.TASK);
        Epic epic = new Epic("Эпик 1", "Описание");

        int taskId = taskManager.createTask(task);
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Подзадача 1", "Описание", TaskStatus.NEW, epicId);
        int subtaskId = taskManager.createSubtask(subtask);

        taskManager.getTaskById(taskId);
        taskManager.getEpicById(epicId);
        taskManager.getSubtaskById(subtaskId);

        List<Task> history = taskManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(taskId, history.get(0).getId());
        assertEquals(epicId, history.get(1).getId());
        assertEquals(subtaskId, history.get(2).getId());
    }

    @Test
    void testHistoryHasNoSizeLimit() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Задача " + i, "Описание " + i, TaskStatus.NEW, TaskType.TASK);
            int taskId = taskManager.createTask(task);
            taskManager.getTaskById(taskId);
        }

        List<Task> history = taskManager.getHistory();
        assertEquals(15, history.size(), "История больше не должна быть ограничена 10 элементами");
        assertEquals("Задача 1", history.get(0).getTitle());
        assertEquals("Задача 15", history.get(14).getTitle());
    }

    @Test
    void testHistoryDoesNotContainDuplicates() {
        Task task1 = new Task(1, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK);
        Task task2 = new Task(2, "Task 2", "Description 2", TaskStatus.NEW, TaskType.TASK);
        Task task3 = new Task(3, "Task 3", "Description 3", TaskStatus.NEW, TaskType.TASK);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(2, history.get(0).getId());
        assertEquals(3, history.get(1).getId());
        assertEquals(1, history.get(2).getId());
    }

    @Test
    void testRemoveFromBeginningOfHistory() {
        historyManager.add(new Task(1, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK));
        historyManager.add(new Task(2, "Task 2", "Description 2", TaskStatus.NEW, TaskType.TASK));
        historyManager.add(new Task(3, "Task 3", "Description 3", TaskStatus.NEW, TaskType.TASK));

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(2, history.get(0).getId());
        assertEquals(3, history.get(1).getId());
    }

    @Test
    void testRemoveFromMiddleOfHistory() {
        historyManager.add(new Task(1, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK));
        historyManager.add(new Task(2, "Task 2", "Description 2", TaskStatus.NEW, TaskType.TASK));
        historyManager.add(new Task(3, "Task 3", "Description 3", TaskStatus.NEW, TaskType.TASK));

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(1, history.get(0).getId());
        assertEquals(3, history.get(1).getId());
    }

    @Test
    void testRemoveFromEndOfHistory() {
        historyManager.add(new Task(1, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK));
        historyManager.add(new Task(2, "Task 2", "Description 2", TaskStatus.NEW, TaskType.TASK));
        historyManager.add(new Task(3, "Task 3", "Description 3", TaskStatus.NEW, TaskType.TASK));

        historyManager.remove(3);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(1, history.get(0).getId());
        assertEquals(2, history.get(1).getId());
    }

    @Test
    void testRemoveOnlyElementFromHistory() {
        historyManager.add(new Task(1, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK));

        historyManager.remove(1);

        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void testRemoveMissingTaskDoesNotChangeHistory() {
        historyManager.add(new Task(1, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK));
        historyManager.add(new Task(2, "Task 2", "Description 2", TaskStatus.NEW, TaskType.TASK));

        historyManager.remove(999);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(1, history.get(0).getId());
        assertEquals(2, history.get(1).getId());
    }

    @Test
    void testAddNullDoesNotChangeHistory() {
        historyManager.add(null);

        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void testHistoryManagerPreservesTaskData() {
        Task task = new Task(1, "Original Title", "Original Desc", TaskStatus.NEW, TaskType.TASK);

        historyManager.add(task);

        task.setTitle("Changed Title");
        task.setDescription("Changed Desc");
        task.setStatus(TaskStatus.DONE);

        List<Task> history = historyManager.getHistory();
        Task historicalTask = history.get(0);

        assertEquals("Original Title", historicalTask.getTitle(), "История должна сохранять оригинальный заголовок");
        assertEquals("Original Desc", historicalTask.getDescription(), "История должна сохранять оригинальное описание");
        assertEquals(TaskStatus.NEW, historicalTask.getStatus(), "История должна сохранять оригинальный статус");
    }

    @Test
    void testReturnedHistoryDoesNotChangeHistoryManager() {
        historyManager.add(new Task(1, "Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK));

        List<Task> history = historyManager.getHistory();
        history.clear();

        assertEquals(1, historyManager.getHistory().size());
    }
}
