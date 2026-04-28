package com.eduplatform.wishlist.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "wishlist_collections")
@CompoundIndex(name = "userId_name_idx", def = "{'userId': 1, 'name': 1, 'tenantId': 1}", unique = true)
public class WishlistCollection {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String name;
    private String description;
    private String visibility; // PRIVATE, SHARED, PUBLIC

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime updatedAt;

    private Integer courseCount = 0;

    private List<String> courseIds;

    @Indexed
    private String tenantId;

    private Long version = 0L;

    /**
     * Add course to collection
     */
    public void addCourse(String courseId) {
        if (this.courseIds == null) {
            this.courseIds = new java.util.ArrayList<>();
        }
        if (!this.courseIds.contains(courseId)) {
            this.courseIds.add(courseId);
            this.courseCount = (this.courseCount == null ? 0 : this.courseCount) + 1;
        }
    }

    /**
     * Remove course from collection
     */
    public void removeCourse(String courseId) {
        if (this.courseIds != null && this.courseIds.remove(courseId)) {
            this.courseCount = Math.max(0, (this.courseCount == null ? 0 : this.courseCount) - 1);
        }
    }

    /**
     * Check if collection is private
     */
    public boolean isPrivate() {
        return "PRIVATE".equals(this.visibility);
    }

    /**
     * Check if collection is shared
     */
    public boolean isShared() {
        return "SHARED".equals(this.visibility);
    }

    /**
     * Check if collection is public
     */
    public boolean isPublic() {
        return "PUBLIC".equals(this.visibility);
    }
}
