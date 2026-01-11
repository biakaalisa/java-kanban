package history;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager{
    private final ArrayList<Task> history;

    public InMemoryHistoryManager(){
        this.history = new ArrayList<>();
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }

    @Override
    public void add(Task task){
        if (task == null) {
            return;
        }
        if (history.size() == 10) {
            history.remove(0);
        }
        Task taskCopy = createTaskCopy(task);
        history.add(taskCopy);
    }

    private Task createTaskCopy(Task task) {
        if (task instanceof Epic) {
            Epic epic = (Epic) task;
            Epic copy = new Epic(epic.getId(), epic.getTitle(),
                    epic.getDescription(), epic.getStatus());
            for (Integer subtaskId : epic.getSubtaskIds()) {
                copy.addSubtaskId(subtaskId);
            }
            return copy;
        } else if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            return new Subtask(subtask.getId(), subtask.getTitle(),
                    subtask.getDescription(), subtask.getStatus(),
                    subtask.getEpicId());
        } else {
            return new Task(task.getId(), task.getTitle(),
                    task.getDescription(), task.getStatus(), task.getType());
        }
    }
}
