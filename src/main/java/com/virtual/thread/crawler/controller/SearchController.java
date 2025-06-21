package com.virtual.thread.crawler.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SearchController {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();

    // 예제 외부 API 리스트 (JSONPlaceholder 사용)
    private static final List<String> DUMMY_API_URLS = List.of(
            "https://jsonplaceholder.typicode.com/posts/1",
            "https://jsonplaceholder.typicode.com/posts/2",
            "https://jsonplaceholder.typicode.com/posts/3",
            "https://jsonplaceholder.typicode.com/posts/4",
            "https://jsonplaceholder.typicode.com/posts/5"
    );

    @GetMapping("/search")
    public List<String> search(@RequestParam String query) throws InterruptedException {
        List<Callable<String>> tasks = DUMMY_API_URLS.stream()
                .map(url -> (Callable<String>) () -> fetchApi(url + "?q=" + query))
                .collect(Collectors.toList());

        List<Future<String>> futures = virtualExecutor.invokeAll(tasks);

        return futures.stream().map(future -> {
            try {
                return future.get();
            } catch (Exception e) {
                return "[Error] " + e.getMessage();
            }
        }).collect(Collectors.toList());
    }

    private String fetchApi(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
