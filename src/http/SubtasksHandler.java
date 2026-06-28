package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import tasks.Subtask;

import java.io.IOException;

public class SubtasksHandler extends BaseHttpHandler {

    public SubtasksHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        handleWithErrors(exchange, () -> {
            Endpoint endpoint = parseEndpoint(exchange, "/subtasks");

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
            sendText(exchange, gson.toJson(taskManager.getAllSubtasks()));
            return;
        }

        Subtask subtask = taskManager.getSubtaskById(endpoint.id());
        if (subtask == null) {
            sendNotFound(exchange);
            return;
        }

        sendText(exchange, gson.toJson(subtask));
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Subtask subtask = gson.fromJson(readBody(exchange), Subtask.class);

        if (subtask.getId() > 0) {
            if (taskManager.getSubtaskById(subtask.getId()) == null) {
                sendNotFound(exchange);
                return;
            }
            taskManager.updateSubtask(subtask);
        } else {
            int id = taskManager.createSubtask(subtask);
            if (id < 0) {
                sendNotFound(exchange);
                return;
            }
        }

        sendCreated(exchange);
    }

    private void handleDelete(Endpoint endpoint, HttpExchange exchange) throws IOException {
        if (!endpoint.hasId() || taskManager.getSubtaskById(endpoint.id()) == null) {
            sendNotFound(exchange);
            return;
        }

        taskManager.deleteSubtask(endpoint.id());
        sendText(exchange, "");
    }
}
