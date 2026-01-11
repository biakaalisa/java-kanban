import manager.Managers;
import manager.TaskManager;
import tasks.*;


public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();
        int task = manager.createTask(new Task("Тест", "Описание", TaskStatus.NEW, TaskType.TASK));
        int epic1 = manager.createEpic(new Epic("Эпик 1", "Описание"));
        manager.createSubtask(new Subtask("Подзадача 1", "Описание", TaskStatus.NEW, epic1));
        manager.createSubtask(new Subtask("Подзадача 2", "Описание", TaskStatus.DONE, epic1));
        manager.createEpic(new Epic("Эпик 2", "Описание"));

        manager.getAnyTypeOfTaskById(task);
        manager.getAnyTypeOfTaskById(epic1);
        manager.getSubtaskById(3);
        manager.getAnyTypeOfTaskById(task);

        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("Эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);

            for (Subtask subtask : manager.getEpicSubtasks(epic.getId())) {
                System.out.println("--> " + subtask);
            }
        }

        System.out.println("Подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}