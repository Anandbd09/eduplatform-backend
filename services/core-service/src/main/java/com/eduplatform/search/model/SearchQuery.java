package com.eduplatform.search.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "search_queries")
public class SearchQuery {
    @Id
    private String id;

    @Indexed
    private String userId;

    private String query;
    private Integer resultCount;

    @Indexed
    private LocalDateTime searchedAt;

    @Indexed
    private String tenantId;
}
