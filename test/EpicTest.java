import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.TaskStatus;
import tasks.TaskType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private Epic epic;

    @BeforeEach
    void setUp() {
        epic = new Epic("Test Epic", "Description");
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
        Epic epic1 = new Epic(1, "Epic 1", "Description", TaskStatus.NEW);
        Epic epic2 = new Epic(1, "Epic 2", "Different", TaskStatus.DONE);

        assertEquals(epic1, epic2, "Epic должны быть равны если одинаковый ID");
    }
}
