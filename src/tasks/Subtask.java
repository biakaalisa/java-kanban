package tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, TaskStatus status, int epicId) {
        super(title, description, status, TaskType.SUBTASK);
        this.epicId = epicId;
    }

    public Subtask(String title, String description, TaskStatus status, int epicId,
                   Duration duration, LocalDateTime startTime) {
        super(title, description, status, TaskType.SUBTASK, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask(int id, String title, String description, TaskStatus status, int epicId) {
        super(id, title, description, status, TaskType.SUBTASK);
        this.epicId = epicId;
    }

    public Subtask(int id, String title, String description, TaskStatus status, int epicId,
                   Duration duration, LocalDateTime startTime) {
        super(id, title, description, status, TaskType.SUBTASK, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", epicId=" + epicId +
                '}';
    }
}
