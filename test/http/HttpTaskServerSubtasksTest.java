package http;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpTaskServerSubtasksTest {
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
    void testPostSubtaskCreatesSubtask() throws IOException, InterruptedException {
        int epicId = manager.createEpic(new Epic("Epic", "Description"));
        String json = gson.toJson(createSubtask(epicId, 10));

        HttpResponse<String> response = client.send(postRequest("/subtasks", json),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getAllSubtasks().size());
        assertEquals(epicId, manager.getAllSubtasks().get(0).getEpicId());
    }

    @Test
    void testGetSubtaskByIdReturnsSubtask() throws IOException, InterruptedException {
        int epicId = manager.createEpic(new Epic("Epic", "Description"));
        int subtaskId = manager.createSubtask(createSubtask(epicId, 10));

        HttpResponse<String> response = client.send(getRequest("/subtasks/" + subtaskId),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    void testGetSubtaskByIdReturnsNotFound() throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(getRequest("/subtasks/999"),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void testDeleteSubtaskDeletesSubtask() throws IOException, InterruptedException {
        int epicId = manager.createEpic(new Epic("Epic", "Description"));
        int subtaskId = manager.createSubtask(createSubtask(epicId, 10));

        HttpResponse<String> response = client.send(deleteRequest("/subtasks/" + subtaskId),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(0, manager.getAllSubtasks().size());
    }

    private Subtask createSubtask(int epicId, int hour) {
        return new Subtask("Subtask", "Description", TaskStatus.NEW, epicId,
                Duration.ofMinutes(30), LocalDateTime.of(2026, 1, 1, hour, 0));
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
