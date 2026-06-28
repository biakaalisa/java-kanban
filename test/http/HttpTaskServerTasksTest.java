package http;

import com.google.gson.Gson;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HttpTaskServerTasksTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
        taskServer.start();
    }

    @AfterEach
    void shutDown() {
        taskServer.stop();
    }

    @Test
    void testGetTasksReturnsSuccessCode() throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(getRequest("/tasks"), HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    void testGetTaskByIdReturnsTask() throws IOException, InterruptedException {
        int taskId = manager.createTask(createTask("Task", 10, 30));

        HttpResponse<String> response = client.send(getRequest("/tasks/" + taskId),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(taskId, manager.getTaskById(taskId).getId());
    }

    @Test
    void testGetTaskByIdReturnsNotFound() throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(getRequest("/tasks/999"),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void testPostTaskCreatesTask() throws IOException, InterruptedException {
        String json = gson.toJson(createTask("Task", 10, 30));
        HttpRequest request = postRequest("/tasks", json);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getAllTasks().size());
        assertEquals("Task", manager.getAllTasks().get(0).getTitle());
    }

    @Test
    void testPostTaskWithIntersectionReturnsNotAcceptable() throws IOException, InterruptedException {
        manager.createTask(createTask("First", 10, 60));
        String json = gson.toJson(createTask("Second", 10, 30));

        HttpResponse<String> response = client.send(postRequest("/tasks", json),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }

    @Test
    void testDeleteTaskDeletesTask() throws IOException, InterruptedException {
        int taskId = manager.createTask(createTask("Task", 10, 30));

        HttpResponse<String> response = client.send(deleteRequest("/tasks/" + taskId),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNotNull(manager.getAllTasks());
        assertEquals(0, manager.getAllTasks().size());
    }

    private Task createTask(String title, int hour, int duration) {
        return new Task(title, "Description", TaskStatus.NEW, TaskType.TASK,
                Duration.ofMinutes(duration), LocalDateTime.of(2026, 1, 1, hour, 0));
    }

    private HttpRequest getRequest(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .GET()
                .build();
    }

    private HttpRequest postRequest(String path, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    private HttpRequest deleteRequest(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .DELETE()
                .build();
    }
}
