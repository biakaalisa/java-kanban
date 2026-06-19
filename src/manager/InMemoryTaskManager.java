package manager;

import history.HistoryManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;
import tasks.TaskType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks;
    protected final HashMap<Integer, Epic> epics;
    protected final HashMap<Integer, Subtask> subtasks;
    protected final HistoryManager historyManager;
    protected int idCounter = 0;

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getAllTasks() {
        List<Task> result = new ArrayList<>();
        for (Task task : tasks.values()) {
            result.add(copyTask(task));
        }
        return result;
    }

    @Override
    public List<Epic> getAllEpics() {
        List<Epic> result = new ArrayList<>();
        for (Epic epic : epics.values()) {
            result.add(copyEpic(epic));
        }
        return result;
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        List<Subtask> result = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            result.add(copySubtask(subtask));
        }
        return result;
    }

    @Override
    public void deleteAllTasks() {
        for (Integer taskId : new ArrayList<>(tasks.keySet())) {
            historyManager.remove(taskId);
        }
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        for (Integer epicId : new ArrayList<>(epics.keySet())) {
            historyManager.remove(epicId);
        }
        for (Integer subtaskId : new ArrayList<>(subtasks.keySet())) {
            historyManager.remove(subtaskId);
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer subtaskId : new ArrayList<>(subtasks.keySet())) {
            historyManager.remove(subtaskId);
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
            updateEpicStatus(epic.getId());
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
        Task taskToSave = new Task(newId, task.getTitle(), task.getDescription(), task.getStatus(), TaskType.TASK);
        tasks.put(newId, taskToSave);
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

        Subtask subtaskToSave = new Subtask(newId, subtask.getTitle(), subtask.getDescription(), subtask.getStatus(),
                subtask.getEpicId());
        subtasks.put(newId, subtaskToSave);
        epic.addSubtaskId(newId);
        updateEpicStatus(epic.getId());
        return newId;
    }

    @Override
    public void updateTask(Task task) {
        if (task == null) {
            return;
        }

        Task existingTask = tasks.get(task.getId());
        if (existingTask != null && existingTask.getType() == task.getType()) {
            existingTask.setTitle(task.getTitle());
            existingTask.setDescription(task.getDescription());
            existingTask.setStatus(task.getStatus());
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
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }

        Subtask existingSubtask = subtasks.get(subtask.getId());
        if (existingSubtask != null && existingSubtask.getEpicId() == subtask.getEpicId()) {
            existingSubtask.setTitle(subtask.getTitle());
            existingSubtask.setDescription(subtask.getDescription());
            existingSubtask.setStatus(subtask.getStatus());
            updateEpicStatus(subtask.getEpicId());
        }
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            historyManager.remove(id);
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            historyManager.remove(id);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic.getId());
            }
        }
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return Collections.emptyList();
        }

        List<Subtask> result = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                result.add(copySubtask(subtask));
            }
        }
        return result;
    }

    protected void addLoadedTask(Task task) {
        tasks.put(task.getId(), copyTask(task));
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

        subtasks.put(subtask.getId(), copySubtask(subtask));
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic.getId());
        updateIdCounterAfterLoading(subtask.getId());
    }

    protected void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null || epic.getSubtaskIds().isEmpty()) {
            if (epic != null) {
                epic.setStatus(TaskStatus.NEW);
            }
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) {
                continue;
            }

            TaskStatus status = subtask.getStatus();
            if (status != TaskStatus.NEW) {
                allNew = false;
            }
            if (status != TaskStatus.DONE) {
                allDone = false;
            }
        }

        if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    protected Task copyTask(Task task) {
        return new Task(task.getId(), task.getTitle(), task.getDescription(), task.getStatus(), task.getType());
    }

    protected Epic copyEpic(Epic epic) {
        Epic copy = new Epic(epic.getId(), epic.getTitle(), epic.getDescription(), epic.getStatus());
        for (Integer subtaskId : epic.getSubtaskIds()) {
            copy.addSubtaskId(subtaskId);
        }
        return copy;
    }

    protected Subtask copySubtask(Subtask subtask) {
        return new Subtask(subtask.getId(), subtask.getTitle(), subtask.getDescription(), subtask.getStatus(),
                subtask.getEpicId());
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
