// FILE 20: SimilarCourseResponse.java
package com.eduplatform.recommendation.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimilarCourseResponse {
    private String courseId;
    private Double similarityScore;
}