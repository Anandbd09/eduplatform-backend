package com.eduplatform.core.course.service;

import com.eduplatform.core.course.dto.CreateCourseLessonRequest;
import com.eduplatform.core.course.dto.CreateCourseModuleRequest;
import com.eduplatform.core.course.dto.CreateCourseRequest;
import com.eduplatform.core.course.dto.CourseResponse;
import com.eduplatform.core.course.dto.LessonResponse;
import com.eduplatform.core.course.dto.ModuleRequest;
import com.eduplatform.core.course.dto.ModuleResponse;
import com.eduplatform.core.course.model.Course;
import com.eduplatform.core.course.model.Lesson;
import com.eduplatform.core.course.model.Module;
import com.eduplatform.core.course.repository.CourseRepository;
import com.eduplatform.core.common.exception.AppException;
import com.eduplatform.core.media.model.MediaAsset;
import com.eduplatform.core.media.model.StoredMedia;
import com.eduplatform.core.media.service.MediaUrlResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private MediaUrlResolver mediaUrlResolver;

    public CourseResponse createCourse(CreateCourseRequest request, String instructorId, String tenantId) {
        String pricingType = normalizePricingType(request);
        BigDecimal price = resolvePrice(request, pricingType);
        BigDecimal discountPrice = resolveDiscountPrice(request, price, pricingType);
        LocalDateTime now = LocalDateTime.now();

        Course course = Course.builder()
                .tenantId(tenantId)
                .instructorId(instructorId)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .level(request.getLevel() != null ? request.getLevel() : "BEGINNER")
                .status("DRAFT")
                .thumbnailAsset(normalizeMediaAsset(request.getThumbnailAsset(), request.getThumbnailUrl()))
                .thumbnailUrl(resolvePublicUrl(request.getThumbnailAsset(), request.getThumbnailUrl()))
                .pricingType(pricingType)
                .price(price)
                .discountPrice(discountPrice)
                .certificateEnabled(Boolean.TRUE.equals(request.getCertificateEnabled()))
                .language(request.getLanguage() != null ? request.getLanguage() : "en")
                .modules(mapModules(request.getModules(), now))
                .createdAt(now)
                .updatedAt(now)
                .build();

        course = courseRepository.save(course);

        log.info("Course created: {} by instructor: {}", course.getId(), instructorId);

        return mapToCourseResponse(course);
    }

    public CourseResponse addModule(String courseId, ModuleRequest request, String instructorId, String tenantId) {

        Course course = courseRepository.findByIdAndTenantId(courseId, tenantId)
                .orElseThrow(() -> AppException.notFound("Course not found"));

        if (!course.getInstructorId().equals(instructorId)) {
            throw AppException.unauthorized("You don't have permission to modify this course");
        }

        Module module = Module.builder()
                .id(java.util.UUID.randomUUID().toString())
                .title(request.getTitle())
                .description(request.getDescription())
                .sequenceNumber(request.getSequenceNumber())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        course.getModules().add(module);
        course.setUpdatedAt(LocalDateTime.now());

        course = courseRepository.save(course);

        log.info("Module added to course: {}", courseId);

        return mapToCourseResponse(course);
    }

    public CourseResponse publishCourse(String courseId, String instructorId, String tenantId) {

        Course course = courseRepository.findByIdAndTenantId(courseId, tenantId)
                .orElseThrow(() -> AppException.notFound("Course not found"));

        if (!course.getInstructorId().equals(instructorId)) {
            throw AppException.unauthorized("You don't have permission to publish this course");
        }

        if (course.getModules().isEmpty()) {
            throw AppException.badRequest("Course must have at least one module");
        }

        course.setStatus("PUBLISHED");
        course.setPublishedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());

        course = courseRepository.save(course);

        log.info("Course published: {}", courseId);

        return mapToCourseResponse(course);
    }

    public CourseResponse getCourse(String courseId, String tenantId) {

        Course course = courseRepository.findByIdAndTenantId(courseId, tenantId)
                .orElseThrow(() -> AppException.notFound("Course not found"));

        return mapToCourseResponse(course);
    }

    public List<CourseResponse> getInstructorCourses(String instructorId, String tenantId) {

        return courseRepository.findByInstructorIdAndTenantId(instructorId, tenantId)
                .stream()
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    public List<CourseResponse> searchPublishedCourses(String query, String tenantId) {

        return courseRepository.searchPublishedCourses(tenantId, query)
                .stream()
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    private CourseResponse mapToCourseResponse(Course course) {
        List<ModuleResponse> moduleResponses = safeModules(course).stream()
                .map(this::mapToModuleResponse)
                .collect(Collectors.toList());

        int lessonCount = safeModules(course).stream()
                .mapToInt(module -> safeLessons(module).size())
                .sum();

        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .category(course.getCategory())
                .level(course.getLevel())
                .status(course.getStatus())
                .thumbnailUrl(mediaUrlResolver.resolveUrl(course.getThumbnailUrl()))
                .thumbnailAsset(mediaUrlResolver.resolve(course.getThumbnailAsset()))
                .pricingType(course.getPricingType())
                .price(course.getPrice())
                .discountPrice(course.getDiscountPrice())
                .enrolledCount(course.getEnrolledCount())
                .moduleCount(moduleResponses.size())
                .lessonCount(lessonCount)
                .certificateEnabled(course.isCertificateEnabled())
                .language(course.getLanguage())
                .modules(moduleResponses)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .publishedAt(course.getPublishedAt())
                .build();
    }

    private List<Module> mapModules(List<CreateCourseModuleRequest> moduleRequests, LocalDateTime now) {
        if (moduleRequests == null || moduleRequests.isEmpty()) {
            return new ArrayList<>();
        }

        List<Module> modules = new ArrayList<>();
        for (int i = 0; i < moduleRequests.size(); i++) {
            CreateCourseModuleRequest moduleRequest = moduleRequests.get(i);
            modules.add(
                    Module.builder()
                            .id(UUID.randomUUID().toString())
                            .title(moduleRequest.getTitle())
                            .description(moduleRequest.getDescription())
                            .sequenceNumber(moduleRequest.getSequenceNumber() != null ? moduleRequest.getSequenceNumber() : i + 1)
                            .lessons(mapLessons(moduleRequest.getLessons(), now))
                            .createdAt(now)
                            .updatedAt(now)
                            .build()
            );
        }

        return modules;
    }

    private List<Lesson> mapLessons(List<CreateCourseLessonRequest> lessonRequests, LocalDateTime now) {
        if (lessonRequests == null || lessonRequests.isEmpty()) {
            return new ArrayList<>();
        }

        List<Lesson> lessons = new ArrayList<>();
        for (int i = 0; i < lessonRequests.size(); i++) {
            CreateCourseLessonRequest lessonRequest = lessonRequests.get(i);
            lessons.add(
                    Lesson.builder()
                            .id(UUID.randomUUID().toString())
                            .title(lessonRequest.getTitle())
                            .description(lessonRequest.getDescription())
                            .lessonType(defaultLessonType(lessonRequest.getLessonType()))
                            .videoAsset(normalizeMediaAsset(lessonRequest.getVideoAsset(), lessonRequest.getVideoUrl()))
                            .videoUrl(resolvePublicUrl(lessonRequest.getVideoAsset(), lessonRequest.getVideoUrl()))
                            .thumbnailAsset(normalizeMediaAsset(lessonRequest.getThumbnailAsset(), lessonRequest.getVideoThumbnail()))
                            .videoThumbnail(resolvePublicUrl(lessonRequest.getThumbnailAsset(), lessonRequest.getVideoThumbnail()))
                            .durationSeconds(lessonRequest.getDurationSeconds() != null ? lessonRequest.getDurationSeconds() : 0)
                            .sequenceNumber(lessonRequest.getSequenceNumber() != null ? lessonRequest.getSequenceNumber() : i + 1)
                            .content(lessonRequest.getContent())
                            .resources(lessonRequest.getResources() != null ? lessonRequest.getResources() : new ArrayList<>())
                            .freePreview(Boolean.TRUE.equals(lessonRequest.getFreePreview()))
                            .createdAt(now)
                            .updatedAt(now)
                            .build()
            );
        }

        return lessons;
    }

    private ModuleResponse mapToModuleResponse(Module module) {
        List<LessonResponse> lessonResponses = safeLessons(module).stream()
                .map(this::mapToLessonResponse)
                .collect(Collectors.toList());

        return ModuleResponse.builder()
                .id(module.getId())
                .title(module.getTitle())
                .description(module.getDescription())
                .sequenceNumber(module.getSequenceNumber())
                .lessonCount(lessonResponses.size())
                .lessons(lessonResponses)
                .createdAt(module.getCreatedAt())
                .updatedAt(module.getUpdatedAt())
                .build();
    }

    private LessonResponse mapToLessonResponse(Lesson lesson) {
        return LessonResponse.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .lessonType(lesson.getLessonType())
                .videoUrl(mediaUrlResolver.resolveUrl(lesson.getVideoUrl()))
                .videoAsset(mediaUrlResolver.resolve(lesson.getVideoAsset()))
                .videoThumbnail(mediaUrlResolver.resolveUrl(lesson.getVideoThumbnail()))
                .thumbnailAsset(mediaUrlResolver.resolve(lesson.getThumbnailAsset()))
                .durationSeconds(lesson.getDurationSeconds())
                .sequenceNumber(lesson.getSequenceNumber())
                .content(lesson.getContent())
                .resources(safeResources(lesson))
                .freePreview(lesson.isFreePreview())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }

    private String normalizePricingType(CreateCourseRequest request) {
        if (request.getPricingType() != null && !request.getPricingType().isBlank()) {
            return request.getPricingType().trim().toUpperCase(Locale.ROOT);
        }

        return hasPositiveValue(request.getPrice()) ? "PAID" : "FREE";
    }

    private BigDecimal resolvePrice(CreateCourseRequest request, String pricingType) {
        if ("FREE".equals(pricingType)) {
            return BigDecimal.ZERO;
        }

        if (!hasPositiveValue(request.getPrice())) {
            throw AppException.badRequest("Price is required for paid courses");
        }

        return request.getPrice();
    }

    private BigDecimal resolveDiscountPrice(CreateCourseRequest request, BigDecimal price, String pricingType) {
        if ("FREE".equals(pricingType) || request.getDiscountPrice() == null) {
            return null;
        }

        if (request.getDiscountPrice().compareTo(price) > 0) {
            throw AppException.badRequest("Discount price cannot be greater than price");
        }

        return request.getDiscountPrice();
    }

    private boolean hasPositiveValue(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private String defaultLessonType(String lessonType) {
        if (lessonType == null || lessonType.isBlank()) {
            return "VIDEO";
        }

        return lessonType.trim().toUpperCase(Locale.ROOT);
    }

    private List<Module> safeModules(Course course) {
        return course.getModules() != null ? course.getModules() : new ArrayList<>();
    }

    private List<Lesson> safeLessons(Module module) {
        return module.getLessons() != null ? module.getLessons() : new ArrayList<>();
    }

    private List<String> safeResources(Lesson lesson) {
        return lesson.getResources() != null ? lesson.getResources() : new ArrayList<>();
    }

    private MediaAsset normalizeMediaAsset(MediaAsset mediaAsset, String fallbackUrl) {
        if (mediaAsset != null && mediaAsset.getPrimary() != null) {
            return mediaAsset;
        }

        if (!StringUtils.hasText(fallbackUrl)) {
            return null;
        }

        return MediaAsset.builder()
                .primary(
                        StoredMedia.builder()
                                .provider("external")
                                .storageKey(fallbackUrl)
                                .publicUrl(fallbackUrl)
                                .build()
                )
                .build();
    }

    private String resolvePublicUrl(MediaAsset mediaAsset, String fallbackUrl) {
        if (mediaAsset != null
                && mediaAsset.getPrimary() != null
                && StringUtils.hasText(mediaAsset.getPrimary().getPublicUrl())) {
            return mediaAsset.getPrimary().getPublicUrl();
        }

        return fallbackUrl;
    }
}
