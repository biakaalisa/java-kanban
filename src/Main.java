import manager.Managers;
import manager.TaskManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;
import tasks.TaskType;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        int task1 = manager.createTask(new Task("Задача 1", "Описание задачи 1",
                TaskStatus.NEW, TaskType.TASK));
        int task2 = manager.createTask(new Task("Задача 2", "Описание задачи 2",
                TaskStatus.NEW, TaskType.TASK));

        int epicWithSubtasks = manager.createEpic(new Epic("Эпик с подзадачами",
                "Описание эпика"));
        int subtask1 = manager.createSubtask(new Subtask("Подзадача 1",
                "Описание подзадачи 1", TaskStatus.NEW,
                epicWithSubtasks));
        int subtask2 = manager.createSubtask(new Subtask("Подзадача 2",
                "Описание подзадачи 2", TaskStatus.DONE,
                epicWithSubtasks));
        int subtask3 = manager.createSubtask(new Subtask("Подзадача 3",
                "Описание подзадачи 3",
                TaskStatus.IN_PROGRESS, epicWithSubtasks));

        int emptyEpic = manager.createEpic(new Epic("Эпик без подзадач",
                "Описание пустого эпика"));

        manager.getTaskById(task1);
        printHistory(manager);

        manager.getTaskById(task2);
        printHistory(manager);

        manager.getEpicById(epicWithSubtasks);
        printHistory(manager);

        manager.getSubtaskById(subtask1);
        printHistory(manager);

        manager.getSubtaskById(subtask2);
        printHistory(manager);

        manager.getTaskById(task1);
        printHistory(manager);

        manager.getSubtaskById(subtask3);
        printHistory(manager);

        manager.getEpicById(emptyEpic);
        printHistory(manager);

        manager.deleteTask(task1);
        printHistory(manager);

        manager.deleteEpic(epicWithSubtasks);
        printHistory(manager);
    }

    private static void printHistory(TaskManager manager) {
        System.out.println("История просмотров:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
        System.out.println();
    }
}
