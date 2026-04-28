package com.recipez.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.recipez.util.DietType;

/**
 * Plain DTO that mirrors the backend User entity.
 * No persistence logic — that all lives on the server now.
 *
 * Note on isMan: the backend has `private Boolean isMan;` with `isMan()` getter
 * and `setMan(Boolean)` setter. Jackson's default naming turns that into the
 * JSON property "man". We mirror the same getter/setter names here so the JSON
 * serialization round-trips correctly without any annotations.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)  // don't send nulls (backend doesn't need them)
public class User {

    private Long id;
    private String username;
    private String password;
    private BodyGoal bodyGoal;
    private DietType dietType;
    private Double weight;
    private Double height;
    private Integer age;
    private Integer bmr;
    private Boolean isMan;

    public User() {}

    /** Convenience for login attempts (only username + password). */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /** Full constructor used during registration. */
    public User(String username, String password, BodyGoal bodyGoal, DietType dietType,
                double weight, double height, int age, boolean isMan) {
        this.username = username;
        this.password = password;
        this.bodyGoal = bodyGoal;
        this.dietType = dietType;
        this.weight = weight;
        this.height = height;
        this.age = age;
        this.isMan = isMan;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public BodyGoal getBodyGoal() { return bodyGoal; }
    public void setBodyGoal(BodyGoal bodyGoal) { this.bodyGoal = bodyGoal; }

    public DietType getDietType() { return dietType; }
    public void setDietType(DietType dietType) { this.dietType = dietType; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Integer getBmr() { return bmr; }
    public void setBmr(Integer bmr) { this.bmr = bmr; }

    public Boolean isMan() { return isMan; }
    public void setMan(Boolean isMan) { this.isMan = isMan; }
}
