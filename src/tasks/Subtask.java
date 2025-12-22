package tasks;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, TaskStatus status, int epicId) {
        super(title, description, status, TaskType.SUBTASK);
        this.epicId = epicId;
    }

    public Subtask(int id, String title, String description, TaskStatus status, int epicId) {
        super(id, title, description, status, TaskType.SUBTASK);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }
}