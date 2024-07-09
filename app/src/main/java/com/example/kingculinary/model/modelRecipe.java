package com.example.kingculinary.model;

import java.util.Date;

public class modelRecipe {
    public Integer  getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Integer recipeId) {
        this.recipeId = recipeId;
    }

    private Integer recipeId;
    public String imageFile;
    public String recipeName;
    public String category;
    public String ingredients;
    public String instructions;
    public String descriptions;
    public String createdAt;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String email;
    private boolean isBookmarked;



    // No-argument constructor required for Firebase
    public modelRecipe() {
    }

    // Constructor with parameters
    public modelRecipe(String imageFile, String recipeName) {
        this.imageFile = imageFile;
        this.recipeName = recipeName;
        this.createdAt = new Date().toString();
    }

    public modelRecipe(String imageFile, String recipeName, String category, String descriptions, String ingredients, String instructions, String email) {
        this.imageFile = imageFile;
        this.recipeName = recipeName;
        this.category = category;
        this.descriptions = descriptions;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.email = email;
        this.createdAt = new Date().toString();
        this.isBookmarked = false;
    }

    public modelRecipe(String imageFile, String recipeName, String category, String descriptions, String ingredients, String instructions) {
        this.imageFile = imageFile;
        this.recipeName = recipeName;
        this.category = category;
        this.descriptions = descriptions;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.createdAt = new Date().toString();
    }
    public modelRecipe(Integer  recipeId, String imageFile, String recipeName, String category, String descriptions, String ingredients, String instructions, String email) {
        this.recipeId = recipeId;
        this.imageFile = imageFile;
        this.recipeName = recipeName;
        this.category = category;
        this.descriptions = descriptions;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.email = email;
        this.createdAt = new Date().toString();
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }

    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(String descriptions) {
        this.descriptions = descriptions;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
