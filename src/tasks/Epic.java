package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds;
    private LocalDateTime endTime;

    public Epic(String title, String description) {
        super(title, description, TaskStatus.NEW, TaskType.EPIC, Duration.ZERO, null);
        this.subtaskIds = new ArrayList<>();
    }

    public Epic(int id, String title, String description, TaskStatus status) {
        super(id, title, description, status, TaskType.EPIC, Duration.ZERO, null);
        this.subtaskIds = new ArrayList<>();
    }

    public Epic(int id, String title, String description, TaskStatus status,
                Duration duration, LocalDateTime startTime, LocalDateTime endTime) {
        super(id, title, description, status, TaskType.EPIC, duration, startTime);
        this.endTime = endTime;
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

    public void clearSubtaskIds() {
        subtaskIds.clear();
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}
