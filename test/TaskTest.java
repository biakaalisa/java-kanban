package tasks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void testTaskCreation() {
        Task task = new Task("Test Task", "Description", TaskStatus.NEW, TaskType.TASK);

        assertNotNull(task, "Задача не должна быть null");
        assertEquals("Test Task", task.getTitle(), "Некорректный title");
        assertEquals("Description", task.getDescription(), "Некорректное описание");
        assertEquals(TaskStatus.NEW, task.getStatus(), "Некорректный статус");
        assertEquals(TaskType.TASK, task.getType(), "Некорректный тип");
    }

    @Test
    void testTaskCreationWithId() {
        Task task = new Task(1, "Test Task", "Description", TaskStatus.NEW, TaskType.TASK);

        assertEquals(1, task.getId(), "Некорректный ID");
        assertEquals("Test Task", task.getTitle(), "Некорректный title");
    }

    @Test
    void testGetters() {
        Task task = new Task(1, "Test", "Desc", TaskStatus.IN_PROGRESS, TaskType.TASK);

        assertEquals(1, task.getId());
        assertEquals("Test", task.getTitle());
        assertEquals("Desc", task.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(TaskType.TASK, task.getType());
    }

    @Test
    void testSetters() {
        Task task = new Task(1, "Original", "Original Desc", TaskStatus.NEW, TaskType.TASK);

        task.setTitle("Updated");
        task.setDescription("Updated Desc");
        task.setStatus(TaskStatus.DONE);

        assertEquals("Updated", task.getTitle());
        assertEquals("Updated Desc", task.getDescription());
        assertEquals(TaskStatus.DONE, task.getStatus());
    }

    @Test
    void testEqualsAndHashCode() {
        Task task1 = new Task(1, "Task 1", "Desc", TaskStatus.NEW, TaskType.TASK);
        Task task2 = new Task(1, "Task 2", "Different Desc", TaskStatus.DONE, TaskType.TASK);
        Task task3 = new Task(2, "Task 1", "Desc", TaskStatus.NEW, TaskType.TASK);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
        assertNotEquals(task1, task3, "Задачи с разным ID не должны быть равны");
        assertEquals(task1.hashCode(), task2.hashCode());
    }
}