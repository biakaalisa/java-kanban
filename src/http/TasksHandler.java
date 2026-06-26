package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import tasks.Task;

import java.io.IOException;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        handleWithErrors(exchange, () -> {
            Endpoint endpoint = parseEndpoint(exchange, "/tasks");

            switch (exchange.getRequestMethod()) {
                case "GET":
                    handleGet(endpoint, exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(endpoint, exchange);
                    break;
                default:
                    sendNotFound(exchange);
            }
        });
    }

    private void handleGet(Endpoint endpoint, HttpExchange exchange) throws IOException {
        if (!endpoint.hasId()) {
            sendText(exchange, gson.toJson(taskManager.getAllTasks()));
            return;
        }

        Task task = taskManager.getTaskById(endpoint.id());
        if (task == null) {
            sendNotFound(exchange);
            return;
        }

        sendText(exchange, gson.toJson(task));
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Task task = gson.fromJson(readBody(exchange), Task.class);

        if (task.getId() > 0) {
            if (taskManager.getTaskById(task.getId()) == null) {
                sendNotFound(exchange);
                return;
            }
            taskManager.updateTask(task);
        } else {
            taskManager.createTask(task);
        }

        sendCreated(exchange);
    }

    private void handleDelete(Endpoint endpoint, HttpExchange exchange) throws IOException {
        if (!endpoint.hasId() || taskManager.getTaskById(endpoint.id()) == null) {
            sendNotFound(exchange);
            return;
        }

        taskManager.deleteTask(endpoint.id());
        sendText(exchange, "");
    }
}
