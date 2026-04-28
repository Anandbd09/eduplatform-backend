// FILE 8: ExportFormatRepository.java
package com.eduplatform.export.repository;

import com.eduplatform.export.model.ExportFormat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ExportFormatRepository extends MongoRepository<ExportFormat, String> {

    Optional<ExportFormat> findByFormatName(String formatName);
}