package com.eduplatform.core.course.service;

import com.eduplatform.core.course.dto.CreateCourseLessonRequest;
import com.eduplatform.core.course.dto.CreateCourseModuleRequest;
import com.eduplatform.core.course.dto.CreateCourseRequest;
import com.eduplatform.core.course.dto.CourseResponse;
import com.eduplatform.core.course.dto.LessonResponse;
import com.eduplatform.core.course.dto.ModuleRequest;
import com.eduplatform.core.course.dto.ModuleResponse;
import com.eduplatform.core.course.dto.PatchCourseRequest;
import com.eduplatform.core.course.dto.UpdateCourseRequest;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourseService {

    private static final Set<String> HIDDEN_INSTRUCTOR_STATUSES = Set.of("ARCHIVED", "REMOVED");

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

        course.setStatus("PENDING_APPROVAL");
        course.setPublishedAt(null);
        course.setApprovedAt(null);
        course.setApprovedBy(null);
        course.setApprovalNotes(null);
        course.setRejectedAt(null);
        course.setRejectedBy(null);
        course.setRejectionReason(null);
        course.setUpdatedAt(LocalDateTime.now());

        course = courseRepository.save(course);

        log.info("Course submitted for approval: {}", courseId);

        return mapToCourseResponse(course);
    }

    public CourseResponse getCourse(String courseId, String tenantId) {

        Course course = courseRepository.findByIdAndTenantId(courseId, tenantId)
                .orElseThrow(() -> AppException.notFound("Course not found"));

        return mapToCourseResponse(course);
    }

    public CourseResponse updateCourse(String courseId, UpdateCourseRequest request, String instructorId, String tenantId) {
        Course course = getOwnedCourse(courseId, instructorId, tenantId);

        prepareCourseForMetadataUpdate(course);

        course.setTitle(request.getTitle().trim());
        course.setDescription(request.getDescription().trim());
        course.setCategory(request.getCategory().trim());
        course.setLevel(normalizeOptionalText(request.getLevel(), course.getLevel(), "Level"));

        applyThumbnailUpdate(
                course,
                request.getThumbnailAsset(),
                request.getThumbnailUrl(),
                request.getThumbnailAsset() != null || request.getThumbnailUrl() != null
        );

        String pricingType = normalizePricingType(
                firstNonBlank(request.getPricingType(), course.getPricingType()),
                request.getPrice() != null ? request.getPrice() : course.getPrice()
        );
        course.setPricingType(pricingType);
        course.setPrice(resolvePrice(pricingType, request.getPrice() != null ? request.getPrice() : course.getPrice()));
        course.setDiscountPrice(resolveDiscountPrice(
                request.getDiscountPrice() != null ? request.getDiscountPrice() : course.getDiscountPrice(),
                course.getPrice(),
                pricingType
        ));

        if (request.getCertificateEnabled() != null) {
            course.setCertificateEnabled(request.getCertificateEnabled());
        }

        course.setLanguage(normalizeOptionalText(request.getLanguage(), course.getLanguage(), "Language"));
        course.setUpdatedAt(LocalDateTime.now());

        course = courseRepository.save(course);

        log.info("Course updated: {}", courseId);

        return mapToCourseResponse(course);
    }

    public CourseResponse patchCourse(String courseId, PatchCourseRequest request, String instructorId, String tenantId) {
        Course course = getOwnedCourse(courseId, instructorId, tenantId);

        prepareCourseForMetadataUpdate(course);

        if (request.getTitle() != null) {
            course.setTitle(requireNonBlank(request.getTitle(), "Title"));
        }

        if (request.getDescription() != null) {
            course.setDescription(requireNonBlank(request.getDescription(), "Description"));
        }

        if (request.getCategory() != null) {
            course.setCategory(requireNonBlank(request.getCategory(), "Category"));
        }

        if (request.getLevel() != null) {
            course.setLevel(requireNonBlank(request.getLevel(), "Level"));
        }

        if (request.getThumbnailAsset() != null || request.getThumbnailUrl() != null) {
            applyThumbnailUpdate(course, request.getThumbnailAsset(), request.getThumbnailUrl(), true);
        }

        if (request.getPricingType() != null || request.getPrice() != null || request.getDiscountPrice() != null) {
            String pricingType = normalizePricingType(
                    firstNonBlank(request.getPricingType(), course.getPricingType()),
                    request.getPrice() != null ? request.getPrice() : course.getPrice()
            );
            course.setPricingType(pricingType);
            course.setPrice(resolvePrice(pricingType, request.getPrice() != null ? request.getPrice() : course.getPrice()));
            course.setDiscountPrice(resolveDiscountPrice(
                    request.getDiscountPrice() != null ? request.getDiscountPrice() : course.getDiscountPrice(),
                    course.getPrice(),
                    pricingType
            ));
        }

        if (request.getCertificateEnabled() != null) {
            course.setCertificateEnabled(request.getCertificateEnabled());
        }

        if (request.getLanguage() != null) {
            course.setLanguage(requireNonBlank(request.getLanguage(), "Language"));
        }

        course.setUpdatedAt(LocalDateTime.now());

        course = courseRepository.save(course);

        log.info("Course patched: {}", courseId);

        return mapToCourseResponse(course);
    }

    public List<CourseResponse> getInstructorCourses(String instructorId, String tenantId) {

        return courseRepository.findByInstructorIdAndTenantId(instructorId, tenantId)
                .stream()
                .filter(course -> !HIDDEN_INSTRUCTOR_STATUSES.contains(course.getStatus()))
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    public List<CourseResponse> getArchivedInstructorCourses(String instructorId, String tenantId) {

        return courseRepository.findByInstructorIdAndTenantId(instructorId, tenantId)
                .stream()
                .filter(course -> "ARCHIVED".equals(course.getStatus()))
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    public List<CourseResponse> searchPublishedCourses(String query, String tenantId) {

        return courseRepository.searchPublishedCourses(tenantId, query)
                .stream()
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    public CourseResponse archiveCourse(String courseId, String instructorId, String tenantId) {
        Course course = getOwnedCourse(courseId, instructorId, tenantId);

        if ("REMOVED".equals(course.getStatus())) {
            throw AppException.badRequest("Removed courses cannot be archived");
        }

        if ("ARCHIVED".equals(course.getStatus())) {
            throw AppException.conflict("Course is already archived");
        }

        LocalDateTime now = LocalDateTime.now();
        course.setStatus("ARCHIVED");
        course.setUpdatedAt(now);

        course = courseRepository.save(course);

        log.info("Course archived: {}", courseId);

        return mapToCourseResponse(course);
    }

    public CourseResponse restoreCourse(String courseId, String instructorId, String tenantId) {
        Course course = getOwnedCourse(courseId, instructorId, tenantId);

        if (!"ARCHIVED".equals(course.getStatus())) {
            throw AppException.badRequest("Only archived courses can be restored");
        }

        LocalDateTime now = LocalDateTime.now();
        course.setStatus("DRAFT");
        course.setUpdatedAt(now);

        course = courseRepository.save(course);

        log.info("Course restored: {}", courseId);

        return mapToCourseResponse(course);
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
        return normalizePricingType(request.getPricingType(), request.getPrice());
    }

    private BigDecimal resolvePrice(CreateCourseRequest request, String pricingType) {
        return resolvePrice(pricingType, request.getPrice());
    }

    private BigDecimal resolvePrice(String pricingType, BigDecimal price) {
        if ("FREE".equals(pricingType)) {
            return BigDecimal.ZERO;
        }

        if (!hasPositiveValue(price)) {
            throw AppException.badRequest("Price is required for paid courses");
        }

        return price;
    }

    private BigDecimal resolveDiscountPrice(CreateCourseRequest request, BigDecimal price, String pricingType) {
        return resolveDiscountPrice(request.getDiscountPrice(), price, pricingType);
    }

    private BigDecimal resolveDiscountPrice(BigDecimal discountPrice, BigDecimal price, String pricingType) {
        if ("FREE".equals(pricingType) || discountPrice == null) {
            return null;
        }

        if (discountPrice.compareTo(price) > 0) {
            throw AppException.badRequest("Discount price cannot be greater than price");
        }

        return discountPrice;
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

    private Course getOwnedCourse(String courseId, String instructorId, String tenantId) {
        Course course = courseRepository.findByIdAndTenantId(courseId, tenantId)
                .orElseThrow(() -> AppException.notFound("Course not found"));

        if (!course.getInstructorId().equals(instructorId)) {
            throw AppException.unauthorized("You don't have permission to modify this course");
        }

        return course;
    }

    private void prepareCourseForMetadataUpdate(Course course) {
        if ("REMOVED".equals(course.getStatus()) || "ARCHIVED".equals(course.getStatus())) {
            throw AppException.badRequest("This course cannot be edited");
        }

        course.setStatus("DRAFT");
        course.setPublishedAt(null);
        course.setApprovedAt(null);
        course.setApprovedBy(null);
        course.setApprovalNotes(null);
        course.setRejectedAt(null);
        course.setRejectedBy(null);
        course.setRejectionReason(null);
    }

    private void applyThumbnailUpdate(Course course, MediaAsset thumbnailAsset, String thumbnailUrl, boolean replace) {
        if (!replace) {
            return;
        }

        if (thumbnailUrl != null && !thumbnailUrl.isBlank() && thumbnailAsset == null) {
            String normalizedUrl = thumbnailUrl.trim();
            course.setThumbnailAsset(normalizeMediaAsset(null, normalizedUrl));
            course.setThumbnailUrl(resolvePublicUrl(null, normalizedUrl));
            return;
        }

        if (thumbnailUrl != null && thumbnailUrl.isBlank()) {
            throw AppException.badRequest("Thumbnail URL cannot be blank");
        }

        course.setThumbnailAsset(normalizeMediaAsset(thumbnailAsset, thumbnailUrl));
        course.setThumbnailUrl(resolvePublicUrl(thumbnailAsset, thumbnailUrl));
    }

    private String normalizePricingType(String pricingType, BigDecimal price) {
        if (pricingType != null && !pricingType.isBlank()) {
            return pricingType.trim().toUpperCase(Locale.ROOT);
        }

        return hasPositiveValue(price) ? "PAID" : "FREE";
    }

    private String requireNonBlank(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw AppException.badRequest(fieldName + " is required");
        }

        return value.trim();
    }

    private String normalizeOptionalText(String incomingValue, String currentValue, String fieldName) {
        if (incomingValue == null) {
            return currentValue;
        }

        return requireNonBlank(incomingValue, fieldName);
    }

    private String firstNonBlank(String preferred, String fallback) {
        if (StringUtils.hasText(preferred)) {
            return preferred.trim();
        }

        return fallback;
    }
}
