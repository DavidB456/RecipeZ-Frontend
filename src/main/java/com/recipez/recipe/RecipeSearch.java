package com.recipez.recipe;

import com.recipez.util.DietType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Stub implementations using the standard library so the project compiles
 * and runs out of the box.
 *
 * If you want to keep your CS 370 hand-written algorithms (quicksort, KMP,
 * etc.), replace this file with the version from your old project.
 * The method signatures match what the old DashboardUI expects.
 */
public class RecipeSearch {

    public static List<Recipe> searchByString(List<Recipe> recipes, String query) {
        if (query == null || query.isBlank()) return new ArrayList<>(recipes);
        String q = query.toLowerCase();
        List<Recipe> out = new ArrayList<>();
        for (Recipe r : recipes) {
            if (r.getName() != null && r.getName().toLowerCase().contains(q)) {
                out.add(r);
            }
        }
        return out;
    }

    public static List<Recipe> quickSortCaloriesAscending(List<Recipe> recipes, int lo, int hi) {
        return sorted(recipes, Comparator.comparingInt(r -> safe(r.getCalories())));
    }

    public static List<Recipe> quickSortCaloriesDescending(List<Recipe> recipes, int lo, int hi) {
        return sorted(recipes, Comparator.<Recipe>comparingInt(r -> safe(r.getCalories())).reversed());
    }

    public static List<Recipe> quickSortNameAscending(List<Recipe> recipes, int lo, int hi) {
        return sorted(recipes, Comparator.comparing(
                r -> r.getName() == null ? "" : r.getName().toLowerCase()));
    }

    public static List<Recipe> quickSortNameDescending(List<Recipe> recipes, int lo, int hi) {
        return sorted(recipes, Comparator.<Recipe, String>comparing(
                r -> r.getName() == null ? "" : r.getName().toLowerCase()).reversed());
    }

    public static List<Recipe> filterByDietType(List<Recipe> recipes, DietType dietType) {
        List<Recipe> out = new ArrayList<>();
        for (Recipe r : recipes) {
            if (r.getDietType() == dietType) out.add(r);
        }
        return out;
    }

    private static int safe(Integer i) { return i == null ? 0 : i; }

    private static List<Recipe> sorted(List<Recipe> in, Comparator<Recipe> cmp) {
        List<Recipe> copy = new ArrayList<>(in);
        copy.sort(cmp);
        return copy;
    }
}
