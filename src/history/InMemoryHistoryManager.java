package history;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> historyNodes = new HashMap<>();
    private Node head;
    private Node tail;

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        remove(task.getId());
        Task taskCopy = createTaskCopy(task);
        Node newNode = linkLast(taskCopy);
        historyNodes.put(taskCopy.getId(), newNode);
    }

    @Override
    public void remove(int id) {
        Node node = historyNodes.remove(id);
        removeNode(node);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private Node linkLast(Task task) {
        Node oldTail = tail;
        Node newNode = new Node(oldTail, task, null);
        tail = newNode;

        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }

        return newNode;
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node current = head;

        while (current != null) {
            tasks.add(createTaskCopy(current.task));
            current = current.next;
        }

        return tasks;
    }

    private void removeNode(Node node) {
        if (node == null) {
            return;
        }

        Node previous = node.previous;
        Node next = node.next;

        if (previous == null) {
            head = next;
        } else {
            previous.next = next;
        }

        if (next == null) {
            tail = previous;
        } else {
            next.previous = previous;
        }
    }

    private Task createTaskCopy(Task task) {
        if (task instanceof Epic) {
            Epic epic = (Epic) task;
            Epic copy = new Epic(epic.getId(), epic.getTitle(), epic.getDescription(), epic.getStatus(),
                    epic.getDuration(), epic.getStartTime(), epic.getEndTime());
            for (Integer subtaskId : epic.getSubtaskIds()) {
                copy.addSubtaskId(subtaskId);
            }
            return copy;
        }

        if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            return new Subtask(subtask.getId(), subtask.getTitle(), subtask.getDescription(), subtask.getStatus(),
                    subtask.getEpicId(), subtask.getDuration(), subtask.getStartTime());
        }

        return new Task(task.getId(), task.getTitle(), task.getDescription(), task.getStatus(), task.getType(),
                task.getDuration(), task.getStartTime());
    }

    private static class Node {
        private Node previous;
        private final Task task;
        private Node next;

        private Node(Node previous, Task task, Node next) {
            this.previous = previous;
            this.task = task;
            this.next = next;
        }
    }
}
