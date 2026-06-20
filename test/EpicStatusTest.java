import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicStatusTest {
    private TaskManager taskManager;
    private int epicId;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
        Epic epic = new Epic("Тестовый эпик", "Проверка статусов");
        epicId = taskManager.createEpic(epic);
    }

    @Test
    void testEpicStatusNewWhenEmpty() {
        Epic epic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.NEW, epic.getStatus(),
                "Пустой эпик должен иметь статус NEW");
    }

    @Test
    void testEpicStatusNewWhenAllSubtasksNew() {
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", TaskStatus.NEW, epicId);

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        Epic epic = taskManager.getEpicById(epicId);
        assertEquals(TaskStatus.NEW, epic.getStatus(),
                "Эпик со всеми подзадачами NEW должен иметь статус NEW");
    }

    @Test
    void testEpicStatusDoneWhenAllSubtasksDone() {
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", TaskStatus.DONE, epicId);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", TaskStatus.DONE, epicId);

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        Epic epic = taskManager.getEpicById(epicId);
        assertEquals(TaskStatus.DONE, epic.getStatus(),
                "Эпик со всеми подзадачами DONE должен иметь статус DONE");
    }

    @Test
    void testEpicStatusInProgressWhenMixed() {
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", TaskStatus.DONE, epicId);
        Subtask subtask3 = new Subtask("Подзадача 3", "Описание", TaskStatus.IN_PROGRESS, epicId);

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        taskManager.createSubtask(subtask3);

        Epic epic = taskManager.getEpicById(epicId);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(),
                "Эпик с подзадачами разных статусов должен иметь статус IN_PROGRESS");
    }
}
