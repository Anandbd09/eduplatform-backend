package com.eduplatform.core.course.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCourseModuleRequest {

    @NotBlank(message = "Module title is required")
    private String title;

    private String description;

    private Integer sequenceNumber;

    @Builder.Default
    @Valid
    private List<CreateCourseLessonRequest> lessons = new ArrayList<>();
}
