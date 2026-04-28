package com.recipez.recipe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ingredient {

    private Long id;
    private String name;
    private double quantifier;
    private MeasurementType measurementType;

    public Ingredient() {}

    public Ingredient(String name, double quantifier, MeasurementType measurementType) {
        this.name = name;
        this.quantifier = quantifier;
        this.measurementType = measurementType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantifier() { return quantifier; }
    public void setQuantifier(double quantifier) { this.quantifier = quantifier; }

    public MeasurementType getMeasurementType() { return measurementType; }
    public void setMeasurementType(MeasurementType measurementType) {
        this.measurementType = measurementType;
    }
}
