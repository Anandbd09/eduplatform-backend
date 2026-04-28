// FILE 22: UserImportRequest.java
package com.eduplatform.batch.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserImportRequest {
    private String importType; // STUDENTS, INSTRUCTORS, ADMINS
    private String duplicateHandling; // SKIP, UPDATE, ERROR
    private String invalidHandling; // SKIP, ERROR
}