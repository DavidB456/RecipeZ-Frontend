package com.recipez.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipez.recipe.Recipe;
import com.recipez.user.User;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class ApiClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public ApiClient(String baseUrl) {
        // strip trailing slash so we can always concat "/foo"
        this.baseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.mapper = new ObjectMapper();
        // backend has fields the client doesn't care about (e.g. bmr); don't crash on them
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // ===================== Auth =====================

    /** POST /login → {id, username} on success, throws ApiException on bad creds. */
    public LoginResponse login(String username, String password) throws ApiException {
        return post("/login", new LoginRequest(username, password), LoginResponse.class);
    }

    /** POST /users — creates a new account. Returns the saved user with id populated. */
    public User register(User user) throws ApiException {
        return post("/users", user, User.class);
    }

    /** GET /users/{id} — fetches the full user (login response only has id+username). */
    public User getUserById(Long id) throws ApiException {
        return get("/users/" + id, User.class);
    }

    // ===================== Recipes =====================

    /** GET /recipes?userId=X — all recipes belonging to a single user. */
    public List<Recipe> getRecipesForUser(Long userId) throws ApiException {
        return getList("/recipes?userId=" + userId, new TypeReference<List<Recipe>>() {});
    }

    /** POST /recipes?userId=X — create a recipe owned by the given user. */
    public Recipe createRecipe(Long userId, Recipe recipe) throws ApiException {
        return post("/recipes?userId=" + userId, recipe, Recipe.class);
    }

    /** DELETE /recipes/{id} — remove by primary key (NOT by name). */
    public void deleteRecipe(Long recipeId) throws ApiException {
        delete("/recipes/" + recipeId);
    }

    // ===================== HTTP plumbing =====================

    private <T> T get(String path, Class<T> responseType) throws ApiException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Accept", "application/json")
                .GET()
                .build();
        return execute(req, responseType);
    }

    private <T> List<T> getList(String path, TypeReference<List<T>> typeRef) throws ApiException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            ensureOk(resp);
            return mapper.readValue(resp.body(), typeRef);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(0, "Network error: " + e.getMessage(), e);
        }
    }

    private <T> T post(String path, Object body, Class<T> responseType) throws ApiException {
        try {
            String json = mapper.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return execute(req, responseType);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(0, "Network error: " + e.getMessage(), e);
        }
    }

    private void delete(String path) throws ApiException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .DELETE()
                .build();
        try {
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            ensureOk(resp);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(0, "Network error: " + e.getMessage(), e);
        }
    }

    private <T> T execute(HttpRequest req, Class<T> responseType) throws ApiException {
        try {
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            ensureOk(resp);
            String body = resp.body();
            if (body == null || body.isBlank() || responseType == Void.class) return null;
            return mapper.readValue(body, responseType);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(0, "Network error: " + e.getMessage(), e);
        }
    }

    private void ensureOk(HttpResponse<String> resp) throws ApiException {
        if (resp.statusCode() / 100 != 2) {
            throw new ApiException(resp.statusCode(), resp.body());
        }
    }

    // ===================== Login DTOs =====================

    public static class LoginRequest {
        private String username;
        private String password;
        public LoginRequest() {}
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
        public String getUsername() { return username; }
        public void setUsername(String u) { this.username = u; }
        public String getPassword() { return password; }
        public void setPassword(String p) { this.password = p; }
    }

    public static class LoginResponse {
        private Long id;
        private String username;
        public LoginResponse() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String u) { this.username = u; }
    }
}
