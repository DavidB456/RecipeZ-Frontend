# Recipez Frontend (API-Connected)

This is the Swing UI from CS 370 rewired to call your friend's Spring Boot backend
instead of reading/writing local JSON.

## Prerequisites

- Java 17+
- Maven
- The backend running at `http://localhost:8080` (start it first — login won't work without it)

If your backend is on a different host/port, edit:
```
src/main/java/com/recipez/core/Application.java
```
and change `API_BASE_URL`.

## Running

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass=com.recipez.Main
```

Or import into IntelliJ / Eclipse and run `com.recipez.Main`.

A test account is preloaded by the backend (only when it runs with the `dev` profile):
- username: `admin`
- password: `admin123`

## What changed vs. the CS 370 version

| Area | Before | Now |
|---|---|---|
| Auth | `UserManager.checkPassword()` against a local file | `POST /login` → returns `{id, username}`, then `GET /users/{id}` for the full profile |
| Register | `new User(...)` saved locally | `POST /users` |
| Load recipes | `Application.activeUser.getRecipes()` (in-memory) | `GET /recipes?userId=X` on dashboard load + Home button |
| Save recipe | `getUserManager().storeRecipe(r)` | `POST /recipes?userId=X` |
| Delete recipe | `removeRecipe(name)` | `DELETE /recipes/{id}` — **now keyed by id, not name** |
| Filter by diet | Local filter | Local filter on the cached list (still done client-side; we fetch all once) |

All HTTP calls run off the EDT via `ApiTask` (a tiny SwingWorker wrapper). UI stays
responsive; errors come back as friendly dialogs through `ApiException.userMessage()`.

## File map

```
core/
  Application.java     <- singletons: activeUser, activeInstance, apiClient + screen switching
  ApiClient.java       <- HttpClient + Jackson; all backend calls live here
  ApiException.java    <- wraps HTTP errors, knows about 401 / 429 / network
  ApiTask.java         <- SwingWorker.run(call, onSuccess, onError)

user/
  User.java            <- DTO; mirrors backend fields. NOTE: isMan()/setMan() — see gotcha below
  BodyGoal.java        <- enum; NONE was added to match backend

recipe/
  Recipe.java          <- DTO with id field (the new key for delete)
  Ingredient.java      <- DTO; quantifier is now double, not float
  MeasurementType.java <- matches backend exactly
  RecipeSearch.java    <- STUB using java.util.Comparator. Drop in your CS 370
                          quicksort/KMP version if you want to keep the algorithms.

util/
  DietType.java        <- NONE, VEGAN, VEGETARIAN, PESCATARIAN, KETO, CARNIVOROUS
  Log.java             <- thin System.out/err wrapper

ui/
  AuthenticationUI.java <- login + register, now async via ApiTask
  DashboardUI.java      <- recipe grid; refreshRecipes() pulls from backend
  RecipeCardPanel.java  <- now stores the whole Recipe (so delete has the id)
  Window.java, WrapLayout.java <- unchanged
```

## Gotchas (read before debugging)

1. **`isMan` vs `"man"` JSON field.** Backend has `Boolean isMan` with getter `isMan()`
   and setter `setMan(Boolean)`. Jackson's default introspection sees `setMan` and
   serializes the property as `"man"` — **not** `"isMan"`. Our `User.java` matches
   that pattern exactly (no annotations needed). If you ever rename either side,
   rename both.

2. **Delete is by id, not name.** The old code called `removeRecipe(name)`. The
   backend route is `DELETE /recipes/{id}`. `RecipeCardPanel` now holds the whole
   `Recipe` so the dashboard can pull `.getId()` when the user clicks delete.

3. **`RecipeSearch` is a stub.** I replaced your CS 370 algorithms file with a
   minimal `java.util.Comparator`-based implementation just so the project
   compiles. If your group wants to keep the hand-written quicksort + KMP
   (which was kind of the point of the 370 project), drop the original
   `RecipeSearch.java` back in — the method signatures it needs are:
   ```java
   public static List<Recipe> sortByName(List<Recipe> recipes);
   public static List<Recipe> filterByDiet(List<Recipe> recipes, DietType diet);
   public static List<Recipe> searchByName(List<Recipe> recipes, String query);
   ```

4. **No tokens, no sessions.** The backend doesn't issue a JWT. We just hold
   `Application.activeUser` in memory and pass `userId` as a query param. Logging
   out clears it. If you close the app you have to log in again.

5. **Rate limiting.** `/login` and `/users` (POST) are rate-limited (5 per refill
   window for login). If you spam the login button you'll get a 429; the UI shows
   "Too many requests, try again in a moment."

6. **Assets.** `/assets/logo.png` and `/assets/recipez.png` references are now
   guarded — if they're missing the UI just skips the image instead of crashing.
   Drop them under `src/main/resources/assets/` if you want them back.

## Adding new backend calls

Pattern lives in `ApiClient`. Example for a hypothetical PATCH:

```java
public Recipe patchRecipe(Long id, Recipe r) throws ApiException {
    return send("PATCH", "/recipes/" + id, r, Recipe.class);
}
```

Then call from the UI:

```java
ApiTask.run(
    () -> Application.apiClient.patchRecipe(id, updated),
    saved -> { /* update UI */ },
    err   -> JOptionPane.showMessageDialog(this, err.userMessage())
);
```
