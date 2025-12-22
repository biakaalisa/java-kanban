package tasks;

public class Task {
    private final int id;
    private String title;
    private String description;
    private TaskStatus status;
    private final TaskType type;

    public Task(String title, String description, TaskStatus status, TaskType type) {
        this.id = 0;
        this.title = title;
        this.description = description;
        this.status = status;
        this.type = type;
    }


    public Task(int id, String title, String description, TaskStatus status, TaskType type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.type = type;
    }

    // насклько вообще плохо писать большой геттер для вывода, допустим, всех полей объекта?

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskType getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", type=" + type +
                '}';
    }
}