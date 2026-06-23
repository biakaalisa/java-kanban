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
        epicId = taskManager.createEpic(new Epic("Epic", "Description"));
    }

    @Test
    void testEpicStatusNewWhenAllSubtasksNew() {
        taskManager.createSubtask(new Subtask("First", "Description", TaskStatus.NEW, epicId));
        taskManager.createSubtask(new Subtask("Second", "Description", TaskStatus.NEW, epicId));

        assertEquals(TaskStatus.NEW, taskManager.getEpicById(epicId).getStatus());
    }

    @Test
    void testEpicStatusDoneWhenAllSubtasksDone() {
        taskManager.createSubtask(new Subtask("First", "Description", TaskStatus.DONE, epicId));
        taskManager.createSubtask(new Subtask("Second", "Description", TaskStatus.DONE, epicId));

        assertEquals(TaskStatus.DONE, taskManager.getEpicById(epicId).getStatus());
    }

    @Test
    void testEpicStatusInProgressWhenSubtasksNewAndDone() {
        taskManager.createSubtask(new Subtask("First", "Description", TaskStatus.NEW, epicId));
        taskManager.createSubtask(new Subtask("Second", "Description", TaskStatus.DONE, epicId));

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epicId).getStatus());
    }

    @Test
    void testEpicStatusInProgressWhenSubtaskInProgress() {
        taskManager.createSubtask(new Subtask("First", "Description", TaskStatus.IN_PROGRESS, epicId));

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epicId).getStatus());
    }
}
