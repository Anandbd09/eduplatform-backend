package com.eduplatform.core.course.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private int sequenceNumber;
}
