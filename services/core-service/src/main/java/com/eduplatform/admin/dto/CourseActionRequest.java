package com.eduplatform.admin.dto;

import lombok.Data;

@Data
public class CourseActionRequest {
    private String courseId;
    private String action; // APPROVE, REJECT, REMOVE
    private String notes;
}