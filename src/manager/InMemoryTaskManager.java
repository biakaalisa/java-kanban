package manager;

import history.HistoryManager;
import tasks.*;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Subtask> subtasks;
    private final HistoryManager historyManager;
    private int idCounter = 0;

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
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
            updateEpicStatus(epic.getId());
        }
    }

    // при изначальной реализации данного проекта вынесла поиск по id одтельно на каждый тип задания
    // (просто задания, эпики и т.п.) когда начала работать с историе поиска поняла, что это не совсем удобно.
    // Лучше же делать универсальрный поиск по id или оставлять разрозненный, на каждый тип?

    public Task getAnyTypeOfTaskById(int id){
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
            return task;
        }

        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
            return epic;
        }

        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
            return subtask;
        }

        return null;
    }

    @Override
    public Task getTaskById(int id) {
        Task currentTask = tasks.get(id);

        historyManager.add(currentTask);
        return currentTask;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic currentEpic = epics.get(id);

        historyManager.add(currentEpic);
        return currentEpic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask currentSubtask = subtasks.get(id);

        historyManager.add(currentSubtask);
        return currentSubtask;
    }

    @Override
    public int createTask(Task task) {
        int newId = generateId();
        Task taskToSave = new Task(newId, task.getTitle(), task.getDescription(), task.getStatus(), task.getType());

        tasks.put(newId, taskToSave);
        return newId;
    }

    @Override
    public int createEpic(Epic epic) {
        int newId = generateId();
        Epic epicToSave = new Epic(newId, epic.getTitle(), epic.getDescription(), TaskStatus.NEW);
        epics.put(newId, epicToSave);
        return newId;
    }

    @Override
    public int createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return -1;
        }

        int newId = generateId();
        Subtask subtaskToSave = new Subtask(newId, subtask.getTitle(), subtask.getDescription(),
                subtask.getStatus(), subtask.getEpicId());
        subtasks.put(newId, subtaskToSave);
        epic.addSubtaskId(newId);
        updateEpicStatus(epic.getId());
        return newId;
    }

    @Override
    public void updateTask(Task task) {
        Task existingTask = tasks.get(task.getId());
        if (existingTask != null) {
            if (existingTask.getType() == task.getType()) {
                existingTask.setTitle(task.getTitle());
                existingTask.setDescription(task.getDescription());
                existingTask.setStatus(task.getStatus());
            }
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic existingEpic = epics.get(epic.getId());
        if (existingEpic != null) {
            existingEpic.setTitle(epic.getTitle());
            existingEpic.setDescription(epic.getDescription());
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask existingSubtask = subtasks.get(subtask.getId());
        if (existingSubtask != null) {
            if (existingSubtask.getEpicId() == subtask.getEpicId()) {
                existingSubtask.setTitle(subtask.getTitle());
                existingSubtask.setDescription(subtask.getDescription());
                existingSubtask.setStatus(subtask.getStatus());
                updateEpicStatus(subtask.getEpicId());
            }
        }
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
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
                result.add(subtask);
            }
        }
        return result;
    }

    private void updateEpicStatus(int epicId) {
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

    private int generateId() {
        return idCounter++;
    }
}