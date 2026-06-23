import history.HistoryManager;
import history.InMemoryHistoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;
import tasks.TaskType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void testEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void testAddTaskToHistory() {
        Task task = new Task(1, "Task", "Description", TaskStatus.NEW, TaskType.TASK);

        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(1, history.get(0).getId());
    }

    @Test
    void testHistoryDoesNotContainDuplicates() {
        Task firstTask = new Task(1, "First", "Description", TaskStatus.NEW, TaskType.TASK);
        Task secondTask = new Task(2, "Second", "Description", TaskStatus.NEW, TaskType.TASK);

        historyManager.add(firstTask);
        historyManager.add(secondTask);
        historyManager.add(firstTask);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(2, history.get(0).getId());
        assertEquals(1, history.get(1).getId());
    }

    @Test
    void testRemoveFromBeginning() {
        addThreeTasksToHistory();

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(2, history.get(0).getId());
        assertEquals(3, history.get(1).getId());
    }

    @Test
    void testRemoveFromMiddle() {
        addThreeTasksToHistory();

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(1, history.get(0).getId());
        assertEquals(3, history.get(1).getId());
    }

    @Test
    void testRemoveFromEnd() {
        addThreeTasksToHistory();

        historyManager.remove(3);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(1, history.get(0).getId());
        assertEquals(2, history.get(1).getId());
    }

    @Test
    void testRemoveUnknownIdDoesNotThrowException() {
        addThreeTasksToHistory();

        assertDoesNotThrow(() -> historyManager.remove(999));
        assertEquals(3, historyManager.getHistory().size());
    }

    private void addThreeTasksToHistory() {
        historyManager.add(new Task(1, "First", "Description", TaskStatus.NEW, TaskType.TASK));
        historyManager.add(new Task(2, "Second", "Description", TaskStatus.NEW, TaskType.TASK));
        historyManager.add(new Task(3, "Third", "Description", TaskStatus.NEW, TaskType.TASK));
    }
}
