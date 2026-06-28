package tasks;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private Epic epic;
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        epic = new Epic("Test Epic", "Description");
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void testEpicCreation() {
        assertNotNull(epic, "Эпик не должен быть null");
        assertEquals("Test Epic", epic.getTitle());
        assertEquals(TaskType.EPIC, epic.getType());
        assertEquals(TaskStatus.NEW, epic.getStatus());
        assertTrue(epic.getSubtaskIds().isEmpty(), "Новый эпик должен иметь пустой список подзадач");
    }

    @Test
    void testAddSubtaskId() {
        epic.addSubtaskId(1);
        epic.addSubtaskId(2);

        List<Integer> subtaskIds = epic.getSubtaskIds();
        assertEquals(2, subtaskIds.size());
        assertTrue(subtaskIds.contains(1));
        assertTrue(subtaskIds.contains(2));
    }

    @Test
    void testRemoveSubtaskId() {
        epic.addSubtaskId(1);
        epic.addSubtaskId(2);
        epic.addSubtaskId(3);

        epic.removeSubtaskId(2);

        List<Integer> subtaskIds = epic.getSubtaskIds();
        assertEquals(2, subtaskIds.size());
        assertTrue(subtaskIds.contains(1));
        assertTrue(subtaskIds.contains(3));
        assertFalse(subtaskIds.contains(2));
    }

    @Test
    void testGetSubtaskIdsReturnsCopy() {
        epic.addSubtaskId(1);
        List<Integer> original = epic.getSubtaskIds();
        original.add(999);

        List<Integer> afterModification = epic.getSubtaskIds();
        assertEquals(1, afterModification.size(), "Оригинальный список не должен измениться");
        assertFalse(afterModification.contains(999));
    }

    @Test
    void testClearSubtaskIds() {
        epic.addSubtaskId(1);
        epic.addSubtaskId(2);
        epic.addSubtaskId(3);

        epic.clearSubtaskIds();

        assertTrue(epic.getSubtaskIds().isEmpty(), "Список подзадач должен быть пустым после очистки");
    }

    @Test
    void testEpicEqualsByInheritance() {
        Epic firstEpic = new Epic(1, "Epic 1", "Description", TaskStatus.NEW);
        Epic secondEpic = new Epic(1, "Epic 2", "Different", TaskStatus.DONE);

        assertEquals(firstEpic, secondEpic, "Epic должны быть равны если одинаковый ID");
    }

    @Test
    void testEpicStatusNewWhenAllSubtasksNew() {
        int epicId = taskManager.createEpic(new Epic("Epic", "Description"));

        taskManager.createSubtask(new Subtask("First", "Description", TaskStatus.NEW, epicId));
        taskManager.createSubtask(new Subtask("Second", "Description", TaskStatus.NEW, epicId));

        assertEquals(TaskStatus.NEW, taskManager.getEpicById(epicId).getStatus());
    }

    @Test
    void testEpicStatusDoneWhenAllSubtasksDone() {
        int epicId = taskManager.createEpic(new Epic("Epic", "Description"));

        taskManager.createSubtask(new Subtask("First", "Description", TaskStatus.DONE, epicId));
        taskManager.createSubtask(new Subtask("Second", "Description", TaskStatus.DONE, epicId));

        assertEquals(TaskStatus.DONE, taskManager.getEpicById(epicId).getStatus());
    }

    @Test
    void testEpicStatusInProgressWhenSubtasksNewAndDone() {
        int epicId = taskManager.createEpic(new Epic("Epic", "Description"));

        taskManager.createSubtask(new Subtask("First", "Description", TaskStatus.NEW, epicId));
        taskManager.createSubtask(new Subtask("Second", "Description", TaskStatus.DONE, epicId));

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epicId).getStatus());
    }

    @Test
    void testEpicStatusInProgressWhenSubtaskInProgress() {
        int epicId = taskManager.createEpic(new Epic("Epic", "Description"));

        taskManager.createSubtask(new Subtask("First", "Description", TaskStatus.IN_PROGRESS, epicId));

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epicId).getStatus());
    }
}
