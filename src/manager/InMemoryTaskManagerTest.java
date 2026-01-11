// Свято верю, что тестов достаточно, ибо время от времени меня мотает из крайности в крайность
// то кажется, что их слишком много, то слшком мало. В итоге вообще запуталась как их грамотно раносить по файлам.
// По какому принципу в основном определяется в какой блок тестов каие тесты будут отнесены?
// А то просто принципу тут мы работаем с эпиками (файл чисто для эпиков), тут мы работатаем с просто задачами звучит
// как-то сомнительно. Все равно каша какая-то получается..


package manager;

import tasks.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void testAddNewTask() {
        Task task = new Task("Помыть посуду", "Вечером после ужина",
                TaskStatus.NEW, TaskType.TASK);
        int taskId = taskManager.createTask(task);

        assertNotEquals(-1, taskId, "ID задачи не должен быть -1 (ошибка)");
        assertTrue(taskId >= 0, "ID задачи должен быть >= 0");

        Task savedTask = taskManager.getTaskById(taskId);
        assertNotNull(savedTask);
        assertEquals("Помыть посуду", savedTask.getTitle());
    }

    @Test
    void testAddNewEpic() {
        Epic epic = new Epic("Ремонт в квартире", "Капитальный ремонт кухни");
        int epicId = taskManager.createEpic(epic);

        assertNotEquals(-1, epicId, "ID эпика не должен быть -1 (ошибка)");
        assertTrue(epicId >= 0, "ID эпика должен быть >= 0");

        Epic savedEpic = taskManager.getEpicById(epicId);
        assertNotNull(savedEpic);
        assertEquals("Ремонт в квартире", savedEpic.getTitle());
        assertEquals(TaskStatus.NEW, savedEpic.getStatus());
    }

    @Test
    void testAddNewSubtask() {
        Epic epic = new Epic("Планирование отпуска", "Поездка на море");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Забронировать отель", "На 7 ночей",
                TaskStatus.NEW, epicId);
        int subtaskId = taskManager.createSubtask(subtask);

        assertTrue(subtaskId > 0, "ID подзадачи должен быть положительным");

        Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);
        assertNotNull(savedSubtask);
        assertEquals("Забронировать отель", savedSubtask.getTitle());
        assertEquals(epicId, savedSubtask.getEpicId());

        List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epicId);
        assertEquals(1, epicSubtasks.size());
        assertEquals(subtaskId, epicSubtasks.get(0).getId());
    }

    @Test
    void testSubtaskShouldNotAddToNonExistentEpic() {
        Subtask subtask = new Subtask("Тестовая подзадача", "Описание",
                TaskStatus.NEW, 99999);
        int subtaskId = taskManager.createSubtask(subtask);

        assertTrue(subtaskId <= 0, "Нельзя создать подзадачу для несуществующего эпика");

        Subtask foundSubtask = taskManager.getSubtaskById(99999);
        assertNull(foundSubtask);
    }

    @Test
    void testGetTaskById() {
        Task task = new Task("Прочитать книгу", "Прочитать 50 страниц",
                TaskStatus.NEW, TaskType.TASK);
        int taskId = taskManager.createTask(task);

        Task foundTask = taskManager.getTaskById(taskId);
        assertNotNull(foundTask);
        assertEquals(taskId, foundTask.getId());
        assertEquals("Прочитать книгу", foundTask.getTitle());

        Task notFound = taskManager.getTaskById(99999);
        assertNull(notFound);
    }

    @Test
    void testUpdateTask() {
        Task task = new Task("Старая задача", "Старое описание",
                TaskStatus.NEW, TaskType.TASK);
        int taskId = taskManager.createTask(task);

        Task updatedTask = new Task(taskId, "Обновленная задача",
                "Новое описание", TaskStatus.DONE, TaskType.TASK);
        taskManager.updateTask(updatedTask);

        Task savedTask = taskManager.getTaskById(taskId);
        assertEquals("Обновленная задача", savedTask.getTitle());
        assertEquals("Новое описание", savedTask.getDescription());
        assertEquals(TaskStatus.DONE, savedTask.getStatus());
    }

    @Test
    void testDeleteTask() {
        Task task = new Task("Временная задача", "Удалить потом", TaskStatus.NEW, TaskType.TASK);
        int taskId = taskManager.createTask(task);
        assertNotNull(taskManager.getTaskById(taskId));
        taskManager.deleteTask(taskId);
        assertNull(taskManager.getTaskById(taskId));
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void testDeleteEpicWithSubtasks() {
        Epic epic = new Epic("Большой проект", "Разработка приложения");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Дизайн", "Создать макеты",
                TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("Разработка", "Написать код",
                TaskStatus.NEW, epicId);

        int subtaskId1 = taskManager.createSubtask(subtask1);
        int subtaskId2 = taskManager.createSubtask(subtask2);

        assertNotNull(taskManager.getEpicById(epicId));
        assertNotNull(taskManager.getSubtaskById(subtaskId1));
        assertNotNull(taskManager.getSubtaskById(subtaskId2));
        taskManager.deleteEpic(epicId);
        assertNull(taskManager.getEpicById(epicId));
        assertNull(taskManager.getSubtaskById(subtaskId1));
        assertNull(taskManager.getSubtaskById(subtaskId2));
    }

    @Test
    void testEpicCannotBeItsOwnSubtask() {
        Epic epic = new Epic("Epic", "Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Test", "Desc", TaskStatus.NEW, epicId);
        int subtaskId = taskManager.createSubtask(subtask);
        assertNotEquals(epicId, subtaskId, "Подзадача не должна иметь тот же ID что и эпик");
    }

    @Test
    void testSubtaskCannotBeItsOwnEpic() {
        Epic epic = new Epic("Epic", "Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Desc", TaskStatus.NEW, epicId);
        int subtaskId = taskManager.createSubtask(subtask);
        Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);
        assertNotEquals(savedSubtask.getId(), savedSubtask.getEpicId(),
                "Subtask не может быть эпиком для самой себя");
    }

    @Test
    void testTaskIdsDoNotConflict() {
        Task taskWithCustomId = new Task(999, "Custom ID Task", "Desc",
                TaskStatus.NEW, TaskType.TASK);
        int returnedId1 = taskManager.createTask(taskWithCustomId);
        Task task2 = new Task("Auto ID Task", "Desc", TaskStatus.NEW, TaskType.TASK);
        int returnedId2 = taskManager.createTask(task2);
        assertNotEquals(returnedId1, returnedId2, "ID задач не должны совпадать");
        assertNotNull(taskManager.getTaskById(returnedId1));
        assertNotNull(taskManager.getTaskById(returnedId2));
    }

    @Test
    void testTaskImmutabilityWhenAddedToManager() {
        Task originalTask = new Task("Original Title", "Original Desc",
                TaskStatus.NEW, TaskType.TASK);
        String originalTitle = originalTask.getTitle();
        String originalDesc = originalTask.getDescription();
        TaskStatus originalStatus = originalTask.getStatus();

        int taskId = taskManager.createTask(originalTask);
        originalTask.setTitle("CHANGED!");
        originalTask.setDescription("CHANGED!");
        originalTask.setStatus(TaskStatus.DONE);
        Task savedTask = taskManager.getTaskById(taskId);
        assertEquals("Original Title", savedTask.getTitle(),
                "Заголовок задачи в менеджере не должен меняться при изменении оригинала");
        assertEquals("Original Desc", savedTask.getDescription(),
                "Описание задачи в менеджере не должно меняться при изменении оригинала");
        assertEquals(TaskStatus.NEW, savedTask.getStatus(),
                "Статус задачи в менеджере не должен меняться при изменении оригинала");
    }
}