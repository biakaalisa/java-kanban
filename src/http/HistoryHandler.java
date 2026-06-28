package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        handleWithErrors(exchange, () -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendNotFound(exchange);
                return;
            }

            sendText(exchange, gson.toJson(taskManager.getHistory()));
        });
    }
}
