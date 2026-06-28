package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.ManagerSaveException;
import manager.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {

    protected final TaskManager taskManager;
    protected final Gson gson;

    protected BaseHttpHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    protected void handleWithErrors(HttpExchange exchange, ExchangeProcessor processor) throws IOException {
        try {
            processor.process();
        } catch (IllegalArgumentException exception) {
            sendHasInteractions(exchange);
        } catch (ManagerSaveException exception) {
            sendServerError(exchange);
        } catch (Exception exception) {
            sendServerError(exchange);
        }
    }

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        sendResponse(exchange, text, HttpStatusCode.OK);
    }

    protected void sendCreated(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "", HttpStatusCode.CREATED);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "{\"error\":\"Объект не найден\"}", HttpStatusCode.NOT_FOUND);
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "{\"error\":\"Задача пересекается с существующими\"}", HttpStatusCode.NOT_ACCEPTABLE);
    }

    protected void sendServerError(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "{\"error\":\"Ошибка сервера\"}", HttpStatusCode.INTERNAL_SERVER_ERROR);
    }

    protected String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    protected Endpoint parseEndpoint(HttpExchange exchange, String basePath) {
        String path = exchange.getRequestURI().getPath();
        if (path.equals(basePath)) {
            return new Endpoint(null, false);
        }

        String prefix = basePath + "/";
        if (!path.startsWith(prefix)) {
            return new Endpoint(null, false);
        }

        String tail = path.substring(prefix.length());
        boolean hasSubtasks = false;
        if (tail.endsWith("/subtasks")) {
            hasSubtasks = true;
            tail = tail.substring(0, tail.length() - "/subtasks".length());
        }

        try {
            return new Endpoint(Integer.parseInt(tail), hasSubtasks);
        } catch (NumberFormatException exception) {
            return new Endpoint(null, hasSubtasks);
        }
    }

    private void sendResponse(HttpExchange exchange, String text, HttpStatusCode statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode.getCode(), response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    @FunctionalInterface
    protected interface ExchangeProcessor {

        void process() throws IOException;
    }

    protected static class Endpoint {
        private final Integer id;
        private final boolean subtasks;

        Endpoint(Integer id, boolean subtasks) {
            this.id = id;
            this.subtasks = subtasks;
        }

        boolean hasId() {
            return id != null;
        }

        int id() {
            return id;
        }

        boolean hasSubtasks() {
            return subtasks;
        }
    }
}
