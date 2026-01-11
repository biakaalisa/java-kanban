package history;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import tasks.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static tasks.TaskStatus.NEW;

class InMemoryHistoryManagerTest {
    private TaskManager taskManager;
    private HistoryManager historyManager;
    private Task testTask;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
        historyManager = new InMemoryHistoryManager();
        testTask = new Task("Test Task", "Test description", NEW, TaskType.TASK);
    }

    @Test
    void testHistoryPreservesOrder() {
        Task task = new Task("Задача 1", "Описание", TaskStatus.NEW, TaskType.TASK);
        Epic epic = new Epic("Эпик 1", "Описание");
        Subtask subtask = new Subtask("Подзадача 1", "Описание", TaskStatus.NEW, 0);

        int taskId = taskManager.createTask(task);
        int epicId = taskManager.createEpic(epic);

        subtask = new Subtask("Подзадача 1", "Описание", TaskStatus.NEW, epicId);
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
    void testHistoryLimit10Elements() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Задача " + i, "Описание " + i,
                    TaskStatus.NEW, TaskType.TASK);
            int taskId = taskManager.createTask(task);
            taskManager.getTaskById(taskId);
        }

        List<Task> history = taskManager.getHistory();
        assertEquals(10, history.size(), "История должна быть ограничена 10 элементами");

        assertEquals("Задача 6", history.get(0).getTitle());
        assertEquals("Задача 15", history.get(9).getTitle());
    }

    @Test
    void testAddNewTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description",
                NEW, TaskType.TASK);
        int taskId = taskManager.createTask(task);

        Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task.getTitle(), savedTask.getTitle(), "Заголовки задач не совпадают.");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Описания задач не совпадают.");

        List<Task> allTasks = taskManager.getAllTasks();
        assertNotNull(allTasks, "Список задач не должен быть null.");
        assertEquals(1, allTasks.size(), "Неверное количество задач.");
        assertEquals(taskId, allTasks.get(0).getId(), "ID задач не совпадают.");
    }

    @Test
    void testAddToHistory() {
        historyManager.add(testTask);

        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не должна быть null.");
        assertEquals(1, history.size(), "После добавления задачи, история должна содержать 1 элемент.");
        assertEquals("Test Task", history.get(0).getTitle(), "Названия задач не совпадают.");
    }

    @Test
    void testHistoryManagerAddAndGet() {
        Task task1 = new Task(1, "Task 1", "Description 1", NEW, TaskType.TASK);
        Task task2 = new Task(2, "Task 2", "Description 2", TaskStatus.IN_PROGRESS, TaskType.TASK);

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "История должна содержать 2 задачи.");
        assertEquals(1, history.get(0).getId(), "Первая задача должна иметь ID 1.");
        assertEquals(2, history.get(1).getId(), "Вторая задача должна иметь ID 2.");
    }

    @Test
    void testHistoryManagerPreservesTaskData() {
        Task task = new Task(1, "Original Title", "Original Desc",
                TaskStatus.NEW, TaskType.TASK);

        historyManager.add(task);

        task.setTitle("Changed Title");
        task.setDescription("Changed Desc");
        task.setStatus(TaskStatus.DONE);

        List<Task> history = historyManager.getHistory();
        Task historicalTask = history.get(0);

        assertEquals("Original Title", historicalTask.getTitle(),
                "История должна сохранять оригинальный заголовок");
        assertEquals("Original Desc", historicalTask.getDescription(),
                "История должна сохранять оригинальное описание");
        assertEquals(TaskStatus.NEW, historicalTask.getStatus(),
                "История должна сохранять оригинальный статус");
    }
}