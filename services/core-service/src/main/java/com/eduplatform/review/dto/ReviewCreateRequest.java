package com.eduplatform.review.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCreateRequest {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 20, max = 5000, message = "Content must be between 20 and 5000 characters")
    private String content;

    private List<String> tags;

    /**
     * Validate review request manually
     */
    public void validate() {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }

        if (title.length() < 5) {
            throw new IllegalArgumentException("Title must be at least 5 characters");
        }

        if (title.length() > 100) {
            throw new IllegalArgumentException("Title cannot exceed 100 characters");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content is required");
        }

        if (content.length() < 20) {
            throw new IllegalArgumentException("Content must be at least 20 characters");
        }

        if (content.length() > 5000) {
            throw new IllegalArgumentException("Content cannot exceed 5000 characters");
        }
    }

    /**
     * Sanitize input
     */
    public void sanitize() {
        if (title != null) {
            this.title = title.trim();
        }
        if (content != null) {
            this.content = content.trim();
        }
    }
}