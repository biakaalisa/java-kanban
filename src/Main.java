import manager.TaskManager;
import tasks.*;
import java.util.List;

// тестов много, зато сразу видно, что все работатет )
// А как вообще в реале проходит проверка работоспособности при помощи инструментов по типу Swagger??


public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        System.out.println("\n СОЗДАНИЕ ЗАДАЧ");
        testCreateTasks(manager);
        System.out.println("\n\n ПОЛУЧЕНИЕ ПО ID");
        testGetById(manager);
        System.out.println("\n\n ПОЛУЧЕНИЕ ВСЕХ ЗАДАЧ");
        testGetAllTasks(manager);
        System.out.println("\n\n ОБНОВЛЕНИЕ ЗАДАЧ");
        testUpdateTasks(manager);
        System.out.println("\n\n ПОДЗАДАЧИ ЭПИКА");
        testGetEpicSubtasks(manager);
        System.out.println("\n\n УДАЛЕНИЕ ПО ID");
        testDeleteById(manager);
        System.out.println("\n\n УДАЛЕНИЕ ВСЕХ ЗАДАЧ");
        testDeleteAll(manager);
        System.out.println("\n\nУПРАВЛЕНИЕ СТАТУСАМИ ЭПИКОВ");
        testEpicStatusManagement(manager);
    }

    private static void testCreateTasks(TaskManager manager) {
        Task task1 = manager.createTask(new Task("Помыть посуду", "Помыть всю грязную посуду",
                TaskStatus.NEW, TaskType.TASK));
        Task task2 = manager.createTask(new Task("Сделать ДЗ", "Выполнить домашнее задание по Java",
                TaskStatus.IN_PROGRESS, TaskType.TASK));

        System.out.println("Созданы обычные задачи:");
        System.out.println("  Задача 1: ID=" + task1.getId() + ", " + task1.getTitle());
        System.out.println("  Задача 2: ID=" + task2.getId() + ", " + task2.getTitle());

        Epic epic1 = manager.createEpic(new Epic("Переезд", "Организовать переезд в новый офис"));
        Epic epic2 = manager.createEpic(new Epic("Разработка проекта", "Запуск нового продукта"));

        System.out.println("\nСозданы эпики:");
        System.out.println("  Эпик 1: ID=" + epic1.getId() + ", " + epic1.getTitle());
        System.out.println("  Эпик 2: ID=" + epic2.getId() + ", " + epic2.getTitle());

        Subtask subtask1 = manager.createSubtask(new Subtask("Собрать коробки", "Подготовить упаковку",
                TaskStatus.NEW, epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Заказать грузчиков", "Найти транспорт",
                TaskStatus.NEW, epic1.getId()));
        Subtask subtask3 = manager.createSubtask(new Subtask("Анализ требований", "Изучить ТЗ проекта",
                TaskStatus.NEW, epic2.getId()));

        System.out.println("\nСозданы подзадачи:");
        System.out.println("  Подзадача 1: ID=" + subtask1.getId() + " для эпика " + epic1.getId());
        System.out.println("  Подзадача 2: ID=" + subtask2.getId() + " для эпика " + epic1.getId());
        System.out.println("  Подзадача 3: ID=" + subtask3.getId() + " для эпика " + epic2.getId());

        Subtask invalidSubtask = manager.createSubtask(new Subtask("Невалидная", "Нет эпика",
                TaskStatus.NEW, 999));
        System.out.println("\nПопытка создать подзадачу для несуществующего эпика: " +
                (invalidSubtask == null ? "НЕ УДАЛОСЬ" : "ОШИБКА"));
    }

    private static void testGetById(TaskManager manager) {
        System.out.println("Получение существующей задачи ID=0: " +
                (manager.getTaskById(0) != null ? "УСПЕХ" : "ОШИБКА"));
        System.out.println("Получение существующего эпика ID=2: " +
                (manager.getEpicById(2) != null ? "УСПЕХ" : "ОШИБКА"));
        System.out.println("Получение существующей подзадачи ID=4: " +
                (manager.getSubtaskById(4) != null ? "УСПЕХ" : "ОШИБКА"));
        System.out.println("Получение несуществующей задачи ID=999: " +
                (manager.getTaskById(999) == null ? "НЕ НАЙДЕНО" : "ОШИБКА"));
    }

    private static void testGetAllTasks(TaskManager manager) {
        List<Task> allTasks = manager.getAllTasks();
        List<Epic> allEpics = manager.getAllEpics();
        List<Subtask> allSubtasks = manager.getAllSubtasks();

        System.out.println("Все обычные задачи: " + allTasks.size() + " шт.");
        for (Task task : allTasks) {
            System.out.println("  ID=" + task.getId() + ": " + task.getTitle() +
                    " [" + task.getStatus() + "]");
        }

        System.out.println("\nВсе эпики: " + allEpics.size() + " шт.");
        for (Epic epic : allEpics) {
            System.out.println("  ID=" + epic.getId() + ": " + epic.getTitle() +
                    " [" + epic.getStatus() + "], подзадач: " + epic.getSubtaskIds().size());
        }

        System.out.println("\nВсе подзадачи: " + allSubtasks.size() + " шт.");
        for (Subtask subtask : allSubtasks) {
            System.out.println("  ID=" + subtask.getId() + ": " + subtask.getTitle() +
                    " (эпик ID=" + subtask.getEpicId() + ")");
        }
    }

    private static void testUpdateTasks(TaskManager manager) {
        Task taskToUpdate = new Task(0, "Помыть посуду (обновлено)",
                "Помыть посуду и протереть стол",
                TaskStatus.IN_PROGRESS, TaskType.TASK);
        manager.updateTask(taskToUpdate);
        Task updatedTask = manager.getTaskById(0);
        System.out.println("Обновлена задача ID=0:");
        System.out.println("  Новый заголовок: " + updatedTask.getTitle());
        System.out.println("  Новый статус: " + updatedTask.getStatus());

        Epic epicToUpdate = new Epic(2, "Переезд офиса (переименован)",
                "Организовать переезд офиса в новый район",
                TaskStatus.NEW);
        manager.updateEpic(epicToUpdate);
        Epic updatedEpic = manager.getEpicById(2);
        System.out.println("\nОбновлен эпик ID=2:");
        System.out.println("  Новый заголовок: " + updatedEpic.getTitle());
        System.out.println("  Статус: " + updatedEpic.getStatus());

        System.out.println("\nСтатус эпика перед обновлением подзадачи: " +
                manager.getEpicById(2).getStatus());

        Subtask subtaskToUpdate = new Subtask(4, "Собрать коробки (срочно!)",
                "Коробки для хрупких вещей",
                TaskStatus.DONE, 2);
        manager.updateSubtask(subtaskToUpdate);

        System.out.println("Обновлена подзадача ID=4 на статус DONE");
        System.out.println("Статус эпика после обновления подзадачи: " +
                manager.getEpicById(2).getStatus());

        Task nonExistent = new Task(999, "Несуществующая", "Не должно быть",
                TaskStatus.NEW, TaskType.TASK);
        manager.updateTask(nonExistent);
        System.out.println("\nПопытка обновления несуществующей задачи ID=999: " +
                (manager.getTaskById(999) == null ? "НЕ ВЫПОЛНЕНО" : "ОШИБКА"));
    }

    private static void testGetEpicSubtasks(TaskManager manager) {
        System.out.println("Подзадачи эпика ID=2:");
        List<Subtask> subtasks = manager.getEpicSubtasks(2);
        for (Subtask subtask : subtasks) {
            System.out.println("  ID=" + subtask.getId() + ": " + subtask.getTitle() +
                    " [" + subtask.getStatus() + "]");
        }

        System.out.println("\nПодзадачи эпика ID=3:");
        List<Subtask> subtasks2 = manager.getEpicSubtasks(3);
        System.out.println("  Найдено подзадач: " + subtasks2.size());

        System.out.println("\nПодзадачи несуществующего эпика ID=999:");
        List<Subtask> subtasks3 = manager.getEpicSubtasks(999);
        System.out.println("  Найдено подзадач: " + subtasks3.size());
    }

    private static void testDeleteById(TaskManager manager) {
        System.out.println("Удаление обычной задачи ID=1...");
        int initialTasksCount = manager.getAllTasks().size();
        manager.deleteTask(1);
        int finalTasksCount = manager.getAllTasks().size();
        System.out.println("  Было: " + initialTasksCount + ", стало: " + finalTasksCount +
                " - " + (initialTasksCount > finalTasksCount ? "УСПЕХ" : "ОШИБКА"));

        System.out.println("\nУдаление подзадачи ID=5...");
        System.out.println("  Статус эпика перед удалением: " + manager.getEpicById(2).getStatus());
        int initialSubtasksCount = manager.getAllSubtasks().size();
        manager.deleteSubtask(5);
        int finalSubtasksCount = manager.getAllSubtasks().size();
        System.out.println("  Подзадач было: " + initialSubtasksCount + ", стало: " + finalSubtasksCount);
        System.out.println("  Статус эпика после удаления: " + manager.getEpicById(2).getStatus());

        System.out.println("\nУдаление эпика ID=3 (с подзадачами)...");
        int initialEpicsCount = manager.getAllEpics().size();
        int initialAllSubtasksCount = manager.getAllSubtasks().size();
        manager.deleteEpic(3);
        int finalEpicsCount = manager.getAllEpics().size();
        int finalAllSubtasksCount = manager.getAllSubtasks().size();
        System.out.println("  Эпиков было: " + initialEpicsCount + ", стало: " + finalEpicsCount);
        System.out.println("  Подзадач было: " + initialAllSubtasksCount + ", стало: " + finalAllSubtasksCount);

        System.out.println("\nПопытка удаления несуществующей задачи ID=999...");
        try {
            manager.deleteTask(999);
            System.out.println("  УДАЛЕНИЕ ПРОШЛО БЕЗ ОШИБОК");
        } catch (Exception e) {
            System.out.println("  ОШИБКА: " + e.getMessage());
        }
    }

    private static void testDeleteAll(TaskManager manager) {
        manager.createTask(new Task("Временная задача", "Для теста удаления",
                TaskStatus.NEW, TaskType.TASK));

        System.out.println("Перед удалением всех обычных задач:");
        System.out.println("  Обычных задач: " + manager.getAllTasks().size());
        System.out.println("  Эпиков: " + manager.getAllEpics().size());
        System.out.println("  Подзадач: " + manager.getAllSubtasks().size());

        manager.deleteAllTasks();
        System.out.println("\nПосле удаления всех обычных задач:");
        System.out.println("  Обычных задач: " + manager.getAllTasks().size());
        System.out.println("  Эпиков: " + manager.getAllEpics().size());

        System.out.println("\nУдаление всех подзадач...");
        manager.deleteAllSubtasks();
        System.out.println("  Подзадач после удаления: " + manager.getAllSubtasks().size());
        System.out.println("  Статус эпика после удаления подзадач: " +
                manager.getEpicById(2).getStatus());

        System.out.println("\nУдаление всех эпиков (с подзадачами)...");
        manager.deleteAllEpics();
        System.out.println("  Эпиков после удаления: " + manager.getAllEpics().size());
        System.out.println("  Подзадач после удаления: " + manager.getAllSubtasks().size());
    }

    private static void testEpicStatusManagement(TaskManager manager) {
        TaskManager freshManager = new TaskManager();
        Epic testEpic = freshManager.createEpic(new Epic("Тестовый эпик", "Для проверки статусов"));

        System.out.println("\nТест 1: Эпик без подзадач");
        System.out.println("  Статус эпика: " + testEpic.getStatus());

        Subtask sub1 = freshManager.createSubtask(new Subtask("Подзадача 1", "Описание 1",
                TaskStatus.NEW, testEpic.getId()));
        Subtask sub2 = freshManager.createSubtask(new Subtask("Подзадача 2", "Описание 2",
                TaskStatus.NEW, testEpic.getId()));
        System.out.println("\nТест 2: Все подзадачи NEW");
        System.out.println("  Статус эпика: " + testEpic.getStatus());

        Subtask updatedSub1 = new Subtask(sub1.getId(), sub1.getTitle(), sub1.getDescription(),
                TaskStatus.IN_PROGRESS, testEpic.getId());
        freshManager.updateSubtask(updatedSub1);
        System.out.println("\nТест 3: Одна подзадача IN_PROGRESS, другая NEW");
        System.out.println("  Статус эпика: " + testEpic.getStatus());

        Subtask updatedSub1Done = new Subtask(sub1.getId(), sub1.getTitle(), sub1.getDescription(),
                TaskStatus.DONE, testEpic.getId());
        Subtask updatedSub2Done = new Subtask(sub2.getId(), sub2.getTitle(), sub2.getDescription(),
                TaskStatus.DONE, testEpic.getId());
        freshManager.updateSubtask(updatedSub1Done);
        freshManager.updateSubtask(updatedSub2Done);
        System.out.println("\nТест 4: Все подзадачи DONE");
        System.out.println("  Статус эпика: " + testEpic.getStatus());

        Subtask sub3 = freshManager.createSubtask(new Subtask("Подзадача 3", "Новая",
                TaskStatus.NEW, testEpic.getId()));
        System.out.println("\nТест 5: Добавили новую подзадачу в эпик");
        System.out.println("  Статус эпика: " + testEpic.getStatus());

    }
}