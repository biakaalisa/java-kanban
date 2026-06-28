package http;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;
import tasks.TaskType;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpTaskServerHistoryAndPrioritizedTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        client = HttpClient.newHttpClient();
        taskServer.start();
    }

    @AfterEach
    void shutDown() {
        taskServer.stop();
    }

    @Test
    void testGetHistoryReturnsSuccessCode() throws IOException, InterruptedException {
        int taskId = manager.createTask(createTask("Task", 10));
        manager.getTaskById(taskId);

        HttpResponse<String> response = client.send(getRequest("/history"),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    void testGetPrioritizedReturnsSuccessCode() throws IOException, InterruptedException {
        manager.createTask(createTask("Second", 12));
        manager.createTask(createTask("First", 10));

        HttpResponse<String> response = client.send(getRequest("/prioritized"),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(2, manager.getPrioritizedTasks().size());
        assertEquals("First", manager.getPrioritizedTasks().get(0).getTitle());
    }

    private Task createTask(String title, int hour) {
        return new Task(title, "Description", TaskStatus.NEW, TaskType.TASK,
                Duration.ofMinutes(30), LocalDateTime.of(2026, 1, 1, hour, 0));
    }

    private HttpRequest getRequest(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .GET()
                .build();
    }
}
