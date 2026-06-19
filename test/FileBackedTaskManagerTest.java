import manager.FileBackedTaskManager;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;
import tasks.TaskType;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @Test
    void testSaveAndLoadEmptyFile() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        manager.save();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    void testSaveSeveralTasks() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        int taskId = manager.createTask(new Task("Task", "Description", TaskStatus.NEW, TaskType.TASK));
        int epicId = manager.createEpic(new Epic("Epic", "Epic description"));
        int subtaskId = manager.createSubtask(new Subtask("Subtask", "Subtask description", TaskStatus.DONE,
                epicId));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals("Task", loadedManager.getTaskById(taskId).getTitle());
        assertEquals("Epic", loadedManager.getEpicById(epicId).getTitle());
        assertEquals("Subtask", loadedManager.getSubtaskById(subtaskId).getTitle());
        assertEquals(epicId, loadedManager.getSubtaskById(subtaskId).getEpicId());
    }

    @Test
    void testLoadSeveralTasks() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        int taskId1 = manager.createTask(new Task("Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK));
        int taskId2 = manager.createTask(new Task("Task 2", "Description 2", TaskStatus.DONE, TaskType.TASK));
        int epicId = manager.createEpic(new Epic("Epic", "Epic description"));
        int subtaskId = manager.createSubtask(new Subtask("Subtask", "Subtask description", TaskStatus.IN_PROGRESS,
                epicId));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertNotNull(loadedManager.getTaskById(taskId1));
        assertNotNull(loadedManager.getTaskById(taskId2));
        assertNotNull(loadedManager.getEpicById(epicId));
        assertNotNull(loadedManager.getSubtaskById(subtaskId));
        assertEquals(2, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubtasks().size());
    }

    @Test
    void testLoadedManagerContinuesGeneratingCorrectIds() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        int firstTaskId = manager.createTask(new Task("Task 1", "Description 1", TaskStatus.NEW, TaskType.TASK));
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        int secondTaskId = loadedManager.createTask(new Task("Task 2", "Description 2", TaskStatus.NEW,
                TaskType.TASK));

        assertNotEquals(firstTaskId, secondTaskId);
        assertNotNull(loadedManager.getTaskById(firstTaskId));
        assertNotNull(loadedManager.getTaskById(secondTaskId));
    }

    @Test
    void testSaveAfterDeletingTask() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        int taskId = manager.createTask(new Task("Task", "Description", TaskStatus.NEW, TaskType.TASK));
        manager.deleteTask(taskId);
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertNull(loadedManager.getTaskById(taskId));
        assertTrue(loadedManager.getAllTasks().isEmpty());
    }
}
