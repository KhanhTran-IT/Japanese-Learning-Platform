package com.japaneselearning.module_enrollment.service;

import com.japaneselearning.common.audit.AuditAction;
import com.japaneselearning.common.audit.AuditOutcome;
import com.japaneselearning.common.audit.AuditService;
import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_enrollment.entity.CourseEnrollment;
import com.japaneselearning.module_enrollment.enums.EnrollmentStatus;
import com.japaneselearning.module_enrollment.policy.EnrollmentAccessPolicy;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseEnrollmentServiceImpl implements CourseEnrollmentService {

    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public void enrollFreeCourse(Long courseId) {
        // 1. Lấy thông tin user hiện tại
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Kiểm tra xem Course có tồn tại không
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // 3. Kiểm tra tính hợp lệ của Khóa học (nghiệp vụ)
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new AppException(ErrorCode.COURSE_NOT_AVAILABLE_FOR_ENROLLMENT);
        }

        if (course.getCourseType() != CourseType.FREE) {
            throw new AppException(ErrorCode.COURSE_CANNOT_ENROLL_PAID);
        }

        // 4. Kiểm tra xem người dùng đã ghi danh chưa (chống trùng lặp, bảo toàn bản ghi lịch sử)
        java.util.Optional<CourseEnrollment> existingOpt = enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId);
        if (existingOpt.isPresent()) {
            CourseEnrollment existing = existingOpt.get();
            if (EnrollmentAccessPolicy.hasAccess(existing.getStatus())) {
                throw new AppException(ErrorCode.USER_ALREADY_ENROLLED);
            }
            // Tái kích hoạt bản ghi lịch sử đã bị hủy hoặc tạm dừng mà không xóa bản ghi cũ
            existing.setStatus(EnrollmentStatus.ACTIVE);
            try {
                enrollmentRepository.saveAndFlush(existing);
                courseRepository.incrementTotalStudents(courseId);

                auditService.record(AuditAction.COURSE_ENROLLMENT_CREATE, "COURSE", String.valueOf(courseId),
                        AuditOutcome.SUCCESS, Map.of("courseTitle", course.getTitle() != null ? course.getTitle() : "", "type", "FREE_REACTIVATE"));
                return;
            } catch (DataIntegrityViolationException ex) {
                throw new AppException(ErrorCode.USER_ALREADY_ENROLLED);
            }
        }

        if (enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(user.getId(), courseId, EnrollmentAccessPolicy.ACCESSIBLE_STATUSES)) {
            throw new AppException(ErrorCode.USER_ALREADY_ENROLLED);
        }

        // 5. Khởi tạo và lưu thông tin ghi danh
        CourseEnrollment enrollment = CourseEnrollment.builder()
                .user(user)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .progressPercent(0)
                .build();

        try {
            enrollmentRepository.saveAndFlush(enrollment);
            courseRepository.incrementTotalStudents(courseId);

            auditService.record(AuditAction.COURSE_ENROLLMENT_CREATE, "COURSE", String.valueOf(courseId),
                    AuditOutcome.SUCCESS, Map.of("courseTitle", course.getTitle() != null ? course.getTitle() : "", "type", "FREE_NEW"));
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.USER_ALREADY_ENROLLED);
        }
    }
}
