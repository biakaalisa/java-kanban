package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import tasks.Epic;

import java.io.IOException;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        handleWithErrors(exchange, () -> {
            Endpoint endpoint = parseEndpoint(exchange, "/epics");

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
            sendText(exchange, gson.toJson(taskManager.getAllEpics()));
            return;
        }

        Epic epic = taskManager.getEpicById(endpoint.id());
        if (epic == null) {
            sendNotFound(exchange);
            return;
        }

        if (endpoint.hasSubtasks()) {
            sendText(exchange, gson.toJson(taskManager.getEpicSubtasks(endpoint.id())));
            return;
        }

        sendText(exchange, gson.toJson(epic));
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Epic epic = gson.fromJson(readBody(exchange), Epic.class);

        if (epic.getId() > 0) {
            if (taskManager.getEpicById(epic.getId()) == null) {
                sendNotFound(exchange);
                return;
            }
            taskManager.updateEpic(epic);
        } else {
            taskManager.createEpic(epic);
        }

        sendCreated(exchange);
    }

    private void handleDelete(Endpoint endpoint, HttpExchange exchange) throws IOException {
        if (!endpoint.hasId() || taskManager.getEpicById(endpoint.id()) == null) {
            sendNotFound(exchange);
            return;
        }

        taskManager.deleteEpic(endpoint.id());
        sendText(exchange, "");
    }
}
