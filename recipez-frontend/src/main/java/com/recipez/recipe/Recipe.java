package com.recipez.recipe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.recipez.util.DietType;

import java.util.ArrayList;
import java.util.List;

/**
 * Plain DTO that mirrors the backend Recipe entity.
 * The `id` field is null for client-built recipes and is populated by the
 * server after POST /recipes.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Recipe {

    private Long id;
    private String name;
    private String description;
    private String instructions;
    private Integer calories;
    private DietType dietType;
    private List<Ingredient> ingredients = new ArrayList<>();

    public Recipe() {}

    /** Same constructor signature your old DashboardUI uses, so call sites need no changes. */
    public Recipe(String name, String description, String instructions,
                  List<Ingredient> ingredients, Integer calories, DietType dietType) {
        this.name = name;
        this.description = description;
        this.instructions = instructions;
        this.ingredients = (ingredients != null) ? ingredients : new ArrayList<>();
        this.calories = calories;
        this.dietType = dietType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public Integer getCalories() { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }

    public DietType getDietType() { return dietType; }
    public void setDietType(DietType dietType) { this.dietType = dietType; }

    public List<Ingredient> getIngredients() { return ingredients; }
    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = (ingredients != null) ? ingredients : new ArrayList<>();
    }
}
