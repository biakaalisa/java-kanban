package manager;

import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;
import tasks.TaskType;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File file;

    @Override
    protected FileBackedTaskManager createTaskManager() throws IOException {
        file = File.createTempFile("tasks", ".csv");
        return new FileBackedTaskManager(file);
    }

    @Test
    void testSaveAndLoadEmptyFile() {
        taskManager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    void testSaveAndLoadSeveralTasks() {
        LocalDateTime taskStart = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime subtaskStart = LocalDateTime.of(2026, 1, 1, 12, 0);
        int taskId = taskManager.createTask(new Task("Task", "Description", TaskStatus.NEW, TaskType.TASK,
                Duration.ofMinutes(30), taskStart));
        int epicId = taskManager.createEpic(new Epic("Epic", "Description"));
        int subtaskId = taskManager.createSubtask(new Subtask("Subtask", "Description", TaskStatus.DONE, epicId,
                Duration.ofMinutes(45), subtaskStart));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        Task loadedTask = loadedManager.getTaskById(taskId);
        Epic loadedEpic = loadedManager.getEpicById(epicId);
        Subtask loadedSubtask = loadedManager.getSubtaskById(subtaskId);

        assertEquals("Task", loadedTask.getTitle());
        assertEquals(Duration.ofMinutes(30), loadedTask.getDuration());
        assertEquals(taskStart, loadedTask.getStartTime());
        assertEquals("Epic", loadedEpic.getTitle());
        assertEquals(List.of(subtaskId), loadedEpic.getSubtaskIds());
        assertEquals(Duration.ofMinutes(45), loadedEpic.getDuration());
        assertEquals(subtaskStart, loadedEpic.getStartTime());
        assertEquals(subtaskStart.plusMinutes(45), loadedEpic.getEndTime());
        assertEquals(epicId, loadedSubtask.getEpicId());
    }

    @Test
    void testIdCounterAfterLoading() {
        int firstTaskId = taskManager.createTask(new Task("Task", "Description", TaskStatus.NEW, TaskType.TASK));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        int secondTaskId = loadedManager.createTask(new Task("Second", "Description", TaskStatus.NEW, TaskType.TASK));

        assertTrue(secondTaskId > firstTaskId);
    }

    @Test
    void testSaveDoesNotThrowExceptionForCorrectFile() {
        assertDoesNotThrow(() -> taskManager.save());
    }

    @Test
    void testSaveThrowsManagerSaveExceptionForIncorrectFile() {
        File incorrectFile = new File("missing-directory/tasks.csv");
        FileBackedTaskManager incorrectManager = new FileBackedTaskManager(incorrectFile);

        assertThrows(ManagerSaveException.class, () -> incorrectManager.save());
    }
}
