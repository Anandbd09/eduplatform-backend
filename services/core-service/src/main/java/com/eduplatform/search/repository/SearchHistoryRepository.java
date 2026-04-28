package com.eduplatform.search.repository;

import com.eduplatform.search.model.SearchQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SearchHistoryRepository extends MongoRepository<SearchQuery, String> {
    List<SearchQuery> findByUserId(String userId);
}
