package com.example.kingculinary;

public class modalComment {

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

    public modalComment() {
        // Default constructor diperlukan untuk Firebase Realtime Database
    }

    public modalComment(String commentId, String recipeId, String userId, String commentText, String timestamp) {
        this.commentId = commentId;
        this.recipeId = recipeId;
        this.userId = userId;
        this.commentText = commentText;
        this.timestamp = timestamp;
    }

    // Getter dan Setter sesuai kebutuhan
    // ...
}

