package com.example.kingculinary.model;

import java.util.Date;

public class modelComment {

    private String commentId;
    private String recipeId;

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    private String userId;
    private String commentText;
    private String timestamp;

    public modelComment() {
        // Default constructor diperlukan untuk Firebase Realtime Database
    }

    public modelComment(String commentId, String recipeId, String userId, String commentText) {
        this.commentId = commentId;
        this.recipeId = recipeId;
        this.userId = userId;
        this.commentText = commentText;
        this.timestamp = new Date().toString();
    }

    // Getter dan Setter sesuai kebutuhan
    // ...
}

