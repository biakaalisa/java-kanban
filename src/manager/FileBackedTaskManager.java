package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;
import tasks.TaskType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final String HEADER = "id,type,name,status,description,duration,startTime,epic";
    private final File file;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            if (!file.exists() || Files.size(file.toPath()) == 0) {
                return manager;
            }

            List<String> lines = Files.readAllLines(file.toPath());
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) {
                    continue;
                }

                Task task = fromString(line);
                manager.addLoadedTaskByType(task);
            }
        } catch (IOException exception) {
            throw new ManagerSaveException("Не удалось загрузить задачи из файла",
                    exception);
        }

        return manager;
    }

    @Override
    public int createTask(Task task) {
        int id = super.createTask(task);
        save();
        return id;
    }

    @Override
    public int createEpic(Epic epic) {
        int id = super.createEpic(epic);
        save();
        return id;
    }

    @Override
    public int createSubtask(Subtask subtask) {
        int id = super.createSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    public void save() {
        StringBuilder builder = new StringBuilder();
        builder.append(HEADER).append(System.lineSeparator());

        for (Task task : tasks.values()) {
            builder.append(toString(task)).append(System.lineSeparator());
        }
        for (Epic epic : epics.values()) {
            builder.append(toString(epic)).append(System.lineSeparator());
        }
        for (Subtask subtask : subtasks.values()) {
            builder.append(toString(subtask)).append(System.lineSeparator());
        }

        try {
            Files.writeString(file.toPath(), builder.toString());
        } catch (IOException exception) {
            throw new ManagerSaveException("Не удалось сохранить задачи в файл",
                    exception);
        }
    }

    public static void main(String[] args) {
        File file = new File("tasks.csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        int taskId = manager.createTask(new Task("Задача", "Описание", TaskStatus.NEW, TaskType.TASK,
                Duration.ofMinutes(30), LocalDateTime.now()));
        int epicId = manager.createEpic(new Epic("Эпик", "Описание эпика"));
        int subtaskId = manager.createSubtask(new Subtask("Подзадача", "Описание",
                TaskStatus.DONE, epicId, Duration.ofMinutes(20), LocalDateTime.now().plusHours(1)));

        manager.getTaskById(taskId);
        manager.getEpicById(epicId);
        manager.getSubtaskById(subtaskId);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        System.out.println(loadedManager.getAllTasks());
        System.out.println(loadedManager.getAllEpics());
        System.out.println(loadedManager.getAllSubtasks());
    }

    private static String toString(Task task) {
        String epicId = "";
        if (task.getType() == TaskType.SUBTASK) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        return task.getId() + "," + task.getType() + "," + task.getTitle() + "," + task.getStatus() + ","
                + task.getDescription() + "," + task.getDuration().toMinutes() + ","
                + formatStartTime(task.getStartTime()) + "," + epicId;
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",", -1);
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String title = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];
        Duration duration = parseDuration(parts[5]);
        LocalDateTime startTime = parseStartTime(parts[6]);

        if (type == TaskType.EPIC) {
            return new Epic(id, title, description, status, duration, startTime, null);
        }
        if (type == TaskType.SUBTASK) {
            int epicId = Integer.parseInt(parts[7]);
            return new Subtask(id, title, description, status, epicId, duration, startTime);
        }
        return new Task(id, title, description, status, TaskType.TASK, duration, startTime);
    }

    private static Duration parseDuration(String value) {
        if (value == null || value.isBlank()) {
            return Duration.ZERO;
        }

        return Duration.ofMinutes(Long.parseLong(value));
    }

    private static LocalDateTime parseStartTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalDateTime.parse(value);
    }

    private static String formatStartTime(LocalDateTime startTime) {
        if (startTime == null) {
            return "";
        }

        return startTime.toString();
    }

    private void addLoadedTaskByType(Task task) {
        if (task.getType() == TaskType.EPIC) {
            addLoadedEpic((Epic) task);
        } else if (task.getType() == TaskType.SUBTASK) {
            addLoadedSubtask((Subtask) task);
        } else {
            addLoadedTask(task);
        }
    }
}
