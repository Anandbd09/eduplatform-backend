// FILE 19: TrendingResponse.java
package com.eduplatform.recommendation.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendingResponse {
    private String courseId;
    private String category;
    private Double rating;
    private Integer duration;
}