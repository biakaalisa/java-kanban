package tasks;

import manager.TaskManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    private TaskManager taskManager;

    @Test
    void testSubtaskCreation() {
        Subtask subtask = new Subtask("Test Subtask", "Description", TaskStatus.NEW, 1);
        assertNotNull(subtask, "Подзадача не должна быть null");
        assertEquals("Test Subtask", subtask.getTitle());
        assertEquals(TaskType.SUBTASK, subtask.getType());
        assertEquals(1, subtask.getEpicId());
    }

    @Test
    void testGetEpicId() {
        Subtask subtask = new Subtask(1, "Test", "Desc", TaskStatus.NEW, 5);
        assertEquals(5, subtask.getEpicId(), "Некорректный epicId");
    }

    @Test
    void testSubtaskEqualsByInheritance() {
        Subtask subtask1 = new Subtask(1, "Subtask 1", "Desc", TaskStatus.NEW, 10);
        Subtask subtask2 = new Subtask(1, "Subtask 2", "Diff", TaskStatus.DONE, 20);

        assertEquals(subtask1, subtask2, "Subtask должны быть равны если одинаковый ID");
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
}