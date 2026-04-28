package com.eduplatform.core.course.controller;

import com.eduplatform.core.course.dto.CreateCourseRequest;
import com.eduplatform.core.course.dto.CourseResponse;
import com.eduplatform.core.course.dto.ModuleRequest;
import com.eduplatform.core.course.dto.PatchCourseRequest;
import com.eduplatform.core.course.dto.UpdateCourseRequest;
import com.eduplatform.core.course.service.CourseService;
import com.eduplatform.core.common.response.ApiResponse;
import com.eduplatform.core.common.security.RequestContext;
import com.eduplatform.core.media.model.MediaAsset;
import com.eduplatform.core.media.model.MediaUploadCategory;
import com.eduplatform.core.media.service.MediaStorageService;
import com.eduplatform.core.media.service.MediaUrlResolver;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private RequestContext requestContext;

    @Autowired
    private MediaStorageService mediaStorageService;

    @Autowired
    private MediaUrlResolver mediaUrlResolver;

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @Valid @RequestBody CreateCourseRequest request) {

        log.info("Create course request: {}", request.getTitle());

        CourseResponse response = courseService.createCourse(
                request,
                requestContext.getUserId(),
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Course created successfully"));
    }

    @PostMapping(value = "/media/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<MediaAsset>> uploadCourseMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "VIDEO") MediaUploadCategory category,
            @RequestParam(required = false) String provider) {

        MediaAsset response = mediaStorageService.upload(
                file,
                category,
                requestContext.getTenantId(),
                requestContext.getUserId(),
                provider
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(mediaUrlResolver.resolve(response), "Media uploaded successfully"));
    }

    @PostMapping("/{courseId}/modules")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CourseResponse>> addModule(
            @PathVariable String courseId,
            @Valid @RequestBody ModuleRequest request) {

        log.info("Add module to course: {}", courseId);

        CourseResponse response = courseService.addModule(
                courseId,
                request,
                requestContext.getUserId(),
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Module added successfully"));
    }

    @PostMapping("/{courseId}/publish")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CourseResponse>> publishCourse(
            @PathVariable String courseId) {

        log.info("Publish course: {}", courseId);

        CourseResponse response = courseService.publishCourse(
                courseId,
                requestContext.getUserId(),
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Course submitted for approval"));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourse(
            @PathVariable String courseId) {

        CourseResponse response = courseService.getCourse(
                courseId,
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Course retrieved successfully"));
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable String courseId,
            @Valid @RequestBody UpdateCourseRequest request) {

        log.info("Replace course: {}", courseId);

        CourseResponse response = courseService.updateCourse(
                courseId,
                request,
                requestContext.getUserId(),
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Course updated successfully"));
    }

    @PatchMapping("/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CourseResponse>> patchCourse(
            @PathVariable String courseId,
            @Valid @RequestBody PatchCourseRequest request) {

        log.info("Patch course: {}", courseId);

        CourseResponse response = courseService.patchCourse(
                courseId,
                request,
                requestContext.getUserId(),
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Course updated successfully"));
    }

    @PostMapping("/{courseId}/archive")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CourseResponse>> archiveCourse(
            @PathVariable String courseId) {

        log.info("Archive course: {}", courseId);

        CourseResponse response = courseService.archiveCourse(
                courseId,
                requestContext.getUserId(),
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Course archived successfully"));
    }

    @PostMapping("/{courseId}/restore")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CourseResponse>> restoreCourse(
            @PathVariable String courseId) {

        log.info("Restore course: {}", courseId);

        CourseResponse response = courseService.restoreCourse(
                courseId,
                requestContext.getUserId(),
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Course restored successfully"));
    }

    @GetMapping("/instructor/my-courses")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getInstructorCourses() {

        List<CourseResponse> response = courseService.getInstructorCourses(
                requestContext.getUserId(),
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Courses retrieved successfully"));
    }

    @GetMapping("/instructor/my-courses/archived")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getArchivedInstructorCourses() {

        List<CourseResponse> response = courseService.getArchivedInstructorCourses(
                requestContext.getUserId(),
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Archived courses retrieved successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> searchCourses(
            @RequestParam String query) {

        List<CourseResponse> response = courseService.searchPublishedCourses(
                query,
                requestContext.getTenantId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Search completed successfully"));
    }
}
