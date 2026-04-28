// FILE 18: RecommendationListResponse.java
package com.eduplatform.recommendation.dto;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationListResponse {
    private List<RecommendationResponse> recommendations;
    private Integer totalCount;
    private Integer pageNumber;
    private Integer pageSize;
}