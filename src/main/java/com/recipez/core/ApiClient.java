package com.recipez.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipez.recipe.Ingredient;
import com.recipez.recipe.Recipe;
import com.recipez.user.User;
import com.recipez.util.DietType;

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
        this.baseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.mapper = new ObjectMapper();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // ===================== Auth =====================

    /** POST /login - returns {id, username} on success. */
    public LoginResponse login(String username, String password) throws ApiException {
        return post("/login", new LoginRequest(username, password), LoginResponse.class);
    }

    // ===================== Users =====================

    /** GET /users - fetches every user. */
    public List<User> getUsers() throws ApiException {
        return getList("/users", new TypeReference<List<User>>() {});
    }

    /** GET /users/{id} - fetches one user by primary key. */
    public User getUserById(Long id) throws ApiException {
        return get("/users/" + id, User.class);
    }

    /** POST /users - creates a new account. */
    public User register(User user) throws ApiException {
        return post("/users", user, User.class);
    }

    /** PUT /users/{id} - updates a user profile. */
    public User updateUser(Long id, User user) throws ApiException {
        return put("/users/" + id, user, User.class);
    }

    /** DELETE /users/{id} - deletes a user by primary key. */
    public void deleteUser(Long id) throws ApiException {
        delete("/users/" + id);
    }

    // ===================== Recipes =====================

    /** GET /recipes - fetches every recipe. */
    public List<Recipe> getRecipes() throws ApiException {
        return getList("/recipes", new TypeReference<List<Recipe>>() {});
    }

    /** GET /recipes?userId=X - fetches recipes belonging to one user. */
    public List<Recipe> getRecipesForUser(Long userId) throws ApiException {
        return getList("/recipes?userId=" + userId, new TypeReference<List<Recipe>>() {});
    }

    /** GET /recipes?dietType=X - fetches recipes matching one diet type. */
    public List<Recipe> getRecipesByDietType(DietType dietType) throws ApiException {
        return getList("/recipes?dietType=" + dietType, new TypeReference<List<Recipe>>() {});
    }

    /** GET /recipes?userId=X&dietType=Y - fetches recipes for one user matching one diet type. */
    public List<Recipe> getRecipesForUserAndDietType(Long userId, DietType dietType) throws ApiException {
        return getList("/recipes?userId=" + userId + "&dietType=" + dietType,
                new TypeReference<List<Recipe>>() {});
    }

    /** GET /recipes/{id} - fetches one recipe by primary key. */
    public Recipe getRecipeById(Long id) throws ApiException {
        return get("/recipes/" + id, Recipe.class);
    }

    /** GET /explore?dietType=X&search=X - browses all users' recipes with optional filters. */
    public List<Recipe> exploreRecipes(Long userId, String dietType, String search) throws ApiException {
        StringBuilder path = new StringBuilder("/explore?");
        if (userId != null)
            path.append("userId=").append(userId).append("&");
        if (dietType != null && !dietType.equals("ALL"))
            path.append("dietType=").append(dietType).append("&");
        if (search != null && !search.isBlank())
            path.append("search=").append(java.net.URLEncoder.encode(search.trim(),
                    java.nio.charset.StandardCharsets.UTF_8));

        String finalPath = path.toString().replaceAll("[?&]$", "");
        return getList(finalPath, new TypeReference<List<Recipe>>() {});
    }

    /** POST /recipes - creates a standalone recipe without an owning user. */
    public Recipe createRecipe(Recipe recipe) throws ApiException {
        return post("/recipes", recipe, Recipe.class);
    }

    /** POST /recipes?userId=X - creates a recipe owned by the given user. */
    public Recipe createRecipe(Long userId, Recipe recipe) throws ApiException {
        return post("/recipes?userId=" + userId, recipe, Recipe.class);
    }

    /** PUT /recipes/{id} - replaces a recipe by primary key. */
    public Recipe updateRecipe(Long id, Recipe recipe) throws ApiException {
        return put("/recipes/" + id, recipe, Recipe.class);
    }

    /** PATCH /recipes/{id} - partially updates a recipe by primary key. */
    public Recipe patchRecipe(Long id, Recipe recipe) throws ApiException {
        return patch("/recipes/" + id, recipe, Recipe.class);
    }

    /** DELETE /recipes/{id} - removes a recipe by primary key. */
    public void deleteRecipe(Long recipeId) throws ApiException {
        delete("/recipes/" + recipeId);
    }

    // ===================== Ingredients =====================

    /** GET /ingredients - fetches every ingredient. */
    public List<Ingredient> getIngredients() throws ApiException {
        return getList("/ingredients", new TypeReference<List<Ingredient>>() {});
    }

    /** GET /ingredients/{id} - fetches one ingredient by primary key. */
    public Ingredient getIngredientById(Long id) throws ApiException {
        return get("/ingredients/" + id, Ingredient.class);
    }

    /** GET /recipes/{recipeId}/ingredients - fetches all ingredients for one recipe. */
    public List<Ingredient> getIngredientsForRecipe(Long recipeId) throws ApiException {
        return getList("/recipes/" + recipeId + "/ingredients",
                new TypeReference<List<Ingredient>>() {});
    }

    /** POST /ingredients - creates an ingredient, optionally linked by nested recipe id. */
    public Ingredient createIngredient(Ingredient ingredient) throws ApiException {
        return post("/ingredients", ingredient, Ingredient.class);
    }

    /** POST /recipes/{recipeId}/ingredients - creates an ingredient for one recipe. */
    public Ingredient createIngredientForRecipe(Long recipeId, Ingredient ingredient) throws ApiException {
        return post("/recipes/" + recipeId + "/ingredients", ingredient, Ingredient.class);
    }

    /** PUT /ingredients/{id} - updates an ingredient by primary key. */
    public Ingredient updateIngredient(Long id, Ingredient ingredient) throws ApiException {
        return put("/ingredients/" + id, ingredient, Ingredient.class);
    }

    /** DELETE /ingredients/{id} - deletes an ingredient by primary key. */
    public void deleteIngredient(Long id) throws ApiException {
        delete("/ingredients/" + id);
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
        return sendWithBody("POST", path, body, responseType);
    }

    private <T> T put(String path, Object body, Class<T> responseType) throws ApiException {
        return sendWithBody("PUT", path, body, responseType);
    }

    private <T> T patch(String path, Object body, Class<T> responseType) throws ApiException {
        return sendWithBody("PATCH", path, body, responseType);
    }

    private <T> T sendWithBody(String method, String path, Object body, Class<T> responseType)
            throws ApiException {
        try {
            String json = mapper.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(json))
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
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginResponse {
        private Long id;
        private String username;

        public LoginResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }
}
