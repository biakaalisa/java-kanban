package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import history.DurationAdapter;
import history.LocalDateTimeAdapter;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private static final Gson GSON =
            new GsonBuilder()
                    .registerTypeAdapter(
                            LocalDateTime.class,
                            new LocalDateTimeAdapter())
                    .registerTypeAdapter(
                            Duration.class,
                            new DurationAdapter())
                    .create();

    private final HttpServer server;

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TasksHandler(taskManager, GSON));
        server.createContext("/subtasks", new SubtasksHandler(taskManager, GSON));
        server.createContext("/epics", new EpicsHandler(taskManager, GSON));
        server.createContext("/history", new HistoryHandler(taskManager, GSON));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager, GSON));
    }

    public static Gson getGson() {
        return GSON;
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer taskServer = new HttpTaskServer();
        taskServer.start();
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
}
