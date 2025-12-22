package tasks;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds;

    public Epic(String title, String description) {
        super(title, description, TaskStatus.NEW, TaskType.EPIC);
        this.subtaskIds = new ArrayList<>();
    }

    public Epic(int id, String title, String description, TaskStatus status) {
        super(id, title, description, status, TaskType.EPIC);
        this.subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }
}