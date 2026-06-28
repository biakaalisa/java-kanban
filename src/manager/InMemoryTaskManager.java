package manager;

import history.HistoryManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;
import tasks.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks;
    protected final HashMap<Integer, Epic> epics;
    protected final HashMap<Integer, Subtask> subtasks;
    protected final HistoryManager historyManager;
    protected final TreeSet<Task> prioritizedTasks;
    protected int idCounter = 0;

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
        this.prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime)
                .thenComparing(Task::getId));
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getAllTasks() {
        return tasks.values()
                .stream()
                .map(this::copyTask)
                .toList();
    }

    @Override
    public List<Epic> getAllEpics() {
        return epics.values()
                .stream()
                .map(this::copyEpic)
                .toList();
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return subtasks.values()
                .stream()
                .map(this::copySubtask)
                .toList();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream()
                .map(this::copyTaskByType)
                .toList();
    }

    @Override
    public void deleteAllTasks() {
        for (Task task : tasks.values()) {
            removeFromPrioritizedTasks(task);
            historyManager.remove(task.getId());
        }

        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        for (Integer epicId : new ArrayList<>(epics.keySet())) {
            historyManager.remove(epicId);
        }
        for (Subtask subtask : subtasks.values()) {
            removeFromPrioritizedTasks(subtask);
            historyManager.remove(subtask.getId());
        }

        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            removeFromPrioritizedTasks(subtask);
            historyManager.remove(subtask.getId());
        }

        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
            updateEpicFields(epic.getId());
        }
    }

    @Override
    public Task getAnyTypeOfTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
            return copyTask(task);
        }

        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
            return copyEpic(epic);
        }

        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
            return copySubtask(subtask);
        }

        return null;
    }

    @Override
    public Task getTaskById(int id) {
        Task currentTask = tasks.get(id);
        if (currentTask == null) {
            return null;
        }

        historyManager.add(currentTask);
        return copyTask(currentTask);
    }

    @Override
    public Epic getEpicById(int id) {
        Epic currentEpic = epics.get(id);
        if (currentEpic == null) {
            return null;
        }

        historyManager.add(currentEpic);
        return copyEpic(currentEpic);
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask currentSubtask = subtasks.get(id);
        if (currentSubtask == null) {
            return null;
        }

        historyManager.add(currentSubtask);
        return copySubtask(currentSubtask);
    }

    @Override
    public int createTask(Task task) {
        if (task == null) {
            return -1;
        }

        int newId = generateId();
        Task taskToSave = new Task(newId, task.getTitle(), task.getDescription(), task.getStatus(), TaskType.TASK,
                task.getDuration(), task.getStartTime());
        checkIntersection(taskToSave);
        tasks.put(newId, taskToSave);
        addToPrioritizedTasks(taskToSave);
        return newId;
    }

    @Override
    public int createEpic(Epic epic) {
        if (epic == null) {
            return -1;
        }

        int newId = generateId();
        Epic epicToSave = new Epic(newId, epic.getTitle(), epic.getDescription(), TaskStatus.NEW);
        epics.put(newId, epicToSave);
        updateEpicFields(newId);
        return newId;
    }

    @Override
    public int createSubtask(Subtask subtask) {
        if (subtask == null) {
            return -1;
        }

        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return -1;
        }

        int newId = generateId();
        if (newId == subtask.getEpicId()) {
            newId = generateId();
        }

        Subtask subtaskToSave = new Subtask(newId, subtask.getTitle(), subtask.getDescription(),
                subtask.getStatus(), subtask.getEpicId(), subtask.getDuration(), subtask.getStartTime());
        checkIntersection(subtaskToSave);
        subtasks.put(newId, subtaskToSave);
        epic.addSubtaskId(newId);
        addToPrioritizedTasks(subtaskToSave);
        updateEpicFields(epic.getId());
        return newId;
    }

    @Override
    public void updateTask(Task task) {
        if (task == null) {
            return;
        }

        Task existingTask = tasks.get(task.getId());
        if (existingTask != null && existingTask.getType() == task.getType()) {
            Task updatedTask = new Task(task.getId(), task.getTitle(), task.getDescription(), task.getStatus(),
                    TaskType.TASK, task.getDuration(), task.getStartTime());
            checkIntersection(updatedTask);
            removeFromPrioritizedTasks(existingTask);
            tasks.put(task.getId(), updatedTask);
            addToPrioritizedTasks(updatedTask);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) {
            return;
        }

        Epic existingEpic = epics.get(epic.getId());
        if (existingEpic != null) {
            existingEpic.setTitle(epic.getTitle());
            existingEpic.setDescription(epic.getDescription());
            updateEpicFields(existingEpic.getId());
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }

        Subtask existingSubtask = subtasks.get(subtask.getId());
        if (existingSubtask != null && existingSubtask.getEpicId() == subtask.getEpicId()) {
            Subtask updatedSubtask = new Subtask(subtask.getId(), subtask.getTitle(), subtask.getDescription(),
                    subtask.getStatus(), subtask.getEpicId(), subtask.getDuration(), subtask.getStartTime());
            checkIntersection(updatedSubtask);
            removeFromPrioritizedTasks(existingSubtask);
            subtasks.put(subtask.getId(), updatedSubtask);
            addToPrioritizedTasks(updatedSubtask);
            updateEpicFields(subtask.getEpicId());
        }
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            removeFromPrioritizedTasks(task);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            historyManager.remove(id);
            for (Integer subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.remove(subtaskId);
                removeFromPrioritizedTasks(subtask);
                historyManager.remove(subtaskId);
            }
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            removeFromPrioritizedTasks(subtask);
            historyManager.remove(id);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicFields(epic.getId());
            }
        }
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return List.of();
        }

        return epic.getSubtaskIds()
                .stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .map(this::copySubtask)
                .toList();
    }

    protected void addLoadedTask(Task task) {
        Task taskToSave = copyTask(task);
        tasks.put(task.getId(), taskToSave);
        addToPrioritizedTasks(taskToSave);
        updateIdCounterAfterLoading(task.getId());
    }

    protected void addLoadedEpic(Epic epic) {
        epics.put(epic.getId(), copyEpic(epic));
        updateIdCounterAfterLoading(epic.getId());
    }

    protected void addLoadedSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return;
        }

        Subtask subtaskToSave = copySubtask(subtask);
        subtasks.put(subtask.getId(), subtaskToSave);
        epic.addSubtaskId(subtask.getId());
        addToPrioritizedTasks(subtaskToSave);
        updateEpicFields(epic.getId());
        updateIdCounterAfterLoading(subtask.getId());
    }

    protected void updateEpicStatus(int epicId) {
        updateEpicFields(epicId);
    }

    protected void updateEpicFields(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Subtask> epicSubtasks = epic.getSubtaskIds()
                .stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .toList();

        if (epicSubtasks.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }

        boolean allNew = epicSubtasks.stream()
                .allMatch(subtask -> subtask.getStatus() == TaskStatus.NEW);
        boolean allDone = epicSubtasks.stream()
                .allMatch(subtask -> subtask.getStatus() == TaskStatus.DONE);

        if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }

        Duration duration = epicSubtasks.stream()
                .map(Task::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);
        LocalDateTime startTime = epicSubtasks.stream()
                .map(Task::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime endTime = epicSubtasks.stream()
                .map(Task::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        epic.setDuration(duration);
        epic.setStartTime(startTime);
        epic.setEndTime(endTime);
    }

    protected Task copyTask(Task task) {
        return new Task(task.getId(), task.getTitle(), task.getDescription(), task.getStatus(), task.getType(),
                task.getDuration(), task.getStartTime());
    }

    protected Epic copyEpic(Epic epic) {
        Epic copy = new Epic(epic.getId(), epic.getTitle(), epic.getDescription(), epic.getStatus(),
                epic.getDuration(), epic.getStartTime(), epic.getEndTime());
        for (Integer subtaskId : epic.getSubtaskIds()) {
            copy.addSubtaskId(subtaskId);
        }
        return copy;
    }

    protected Subtask copySubtask(Subtask subtask) {
        return new Subtask(subtask.getId(), subtask.getTitle(), subtask.getDescription(), subtask.getStatus(),
                subtask.getEpicId(), subtask.getDuration(), subtask.getStartTime());
    }

    protected Task copyTaskByType(Task task) {
        if (task.getType() == TaskType.SUBTASK) {
            return copySubtask((Subtask) task);
        }

        return copyTask(task);
    }

    protected boolean isTimeIntersecting(Task first, Task second) {
        if (first == null || second == null || first.getStartTime() == null || second.getStartTime() == null) {
            return false;
        }
        if (first.getEndTime() == null || second.getEndTime() == null) {
            return false;
        }

        return first.getStartTime().isBefore(second.getEndTime())
                && second.getStartTime().isBefore(first.getEndTime());
    }

    private void checkIntersection(Task task) {
        if (task.getStartTime() == null) {
            return;
        }

        boolean hasIntersection = prioritizedTasks.stream()
                .filter(currentTask -> currentTask.getId() != task.getId())
                .anyMatch(currentTask -> isTimeIntersecting(task, currentTask));

        if (hasIntersection) {
            throw new IllegalArgumentException(
                    "Задача пересекается по времени с другой задачей"
            );
        }
    }

    private void addToPrioritizedTasks(Task task) {
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritizedTasks(Task task) {
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.remove(task);
        }
    }

    private int generateId() {
        return idCounter++;
    }

    private void updateIdCounterAfterLoading(int id) {
        if (id >= idCounter) {
            idCounter = id + 1;
        }
    }
}
