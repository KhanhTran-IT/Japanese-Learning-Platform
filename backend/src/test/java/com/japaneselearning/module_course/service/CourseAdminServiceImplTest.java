package com.japaneselearning.module_course.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_course.dto.CourseCreateReq;
import com.japaneselearning.module_course.dto.CourseRes;
import com.japaneselearning.module_course.dto.CourseUpdateReq;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_course.repository.CourseSectionRepository;
import com.japaneselearning.module_course.repository.LessonRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseAdminServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseSectionRepository sectionRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CourseAdminServiceImpl courseAdminService;

    private User teacher;
    private User otherTeacher;

    @BeforeEach
    void setUp() {
        teacher = User.builder()
                .id(1L)
                .email("teacher@example.com")
                .fullName("Teacher One")
                .build();

        otherTeacher = User.builder()
                .id(2L)
                .email("other@example.com")
                .fullName("Other Teacher")
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "teacher@example.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_TEACHER"))
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =========================================================================
    // 1. PRICING INVARIANTS (CREATE)
    // =========================================================================
    @Nested
    @DisplayName("Pricing Invariants on Course Creation")
    class CreatePricingTests {

        @Test
        @DisplayName("FREE course with positive originalPrice throws COURSE_FREE_PRICE_INVALID")
        void createCourse_FreeCourse_PositiveOriginalPrice_ThrowsError() {
            CourseCreateReq req = CourseCreateReq.builder()
                    .title("Free Japanese")
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .originalPrice(BigDecimal.valueOf(100000))
                    .build();

            when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(teacher));

            assertThatThrownBy(() -> courseAdminService.createCourse(req))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_FREE_PRICE_INVALID);
        }

        @Test
        @DisplayName("FREE course with positive salePrice throws COURSE_FREE_PRICE_INVALID")
        void createCourse_FreeCourse_PositiveSalePrice_ThrowsError() {
            CourseCreateReq req = CourseCreateReq.builder()
                    .title("Free Japanese")
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .salePrice(BigDecimal.valueOf(50000))
                    .build();

            when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(teacher));

            assertThatThrownBy(() -> courseAdminService.createCourse(req))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_FREE_PRICE_INVALID);
        }

        @Test
        @DisplayName("FREE course with null prices normalizes to ZERO and creates successfully")
        void createCourse_FreeCourse_NullPrices_NormalizesToZero() {
            CourseCreateReq req = CourseCreateReq.builder()
                    .title("Free Japanese N5")
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .build();

            when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(teacher));
            when(courseRepository.existsBySlug("free-japanese-n5")).thenReturn(false);
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
                Course c = invocation.getArgument(0);
                c.setId(10L);
                return c;
            });

            CourseRes res = courseAdminService.createCourse(req);

            assertThat(res).isNotNull();
            assertThat(res.getCourseType()).isEqualTo("FREE");
            assertThat(res.getOriginalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(res.getSalePrice()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(res.getStatus()).isEqualTo("DRAFT");
        }

        @Test
        @DisplayName("PAID course with null originalPrice throws COURSE_PRICE_REQUIRED")
        void createCourse_PaidCourse_NullOriginalPrice_ThrowsError() {
            CourseCreateReq req = CourseCreateReq.builder()
                    .title("Paid Japanese")
                    .level(CourseLevel.N4)
                    .courseType(CourseType.PAID)
                    .originalPrice(null)
                    .build();

            when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(teacher));

            assertThatThrownBy(() -> courseAdminService.createCourse(req))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_PRICE_REQUIRED);
        }

        @Test
        @DisplayName("PAID course with zero originalPrice throws COURSE_PRICE_REQUIRED")
        void createCourse_PaidCourse_ZeroOriginalPrice_ThrowsError() {
            CourseCreateReq req = CourseCreateReq.builder()
                    .title("Paid Japanese")
                    .level(CourseLevel.N4)
                    .courseType(CourseType.PAID)
                    .originalPrice(BigDecimal.ZERO)
                    .build();

            when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(teacher));

            assertThatThrownBy(() -> courseAdminService.createCourse(req))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_PRICE_REQUIRED);
        }

        @Test
        @DisplayName("PAID course with salePrice greater than originalPrice throws COURSE_SALE_PRICE_INVALID")
        void createCourse_PaidCourse_SalePriceGreaterThanOriginal_ThrowsError() {
            CourseCreateReq req = CourseCreateReq.builder()
                    .title("Paid Japanese")
                    .level(CourseLevel.N4)
                    .courseType(CourseType.PAID)
                    .originalPrice(BigDecimal.valueOf(500000))
                    .salePrice(BigDecimal.valueOf(600000))
                    .build();

            when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(teacher));

            assertThatThrownBy(() -> courseAdminService.createCourse(req))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_SALE_PRICE_INVALID);
        }

        @Test
        @DisplayName("PAID course with salePrice <= 0 throws COURSE_SALE_PRICE_INVALID")
        void createCourse_PaidCourse_ZeroSalePrice_ThrowsError() {
            CourseCreateReq req = CourseCreateReq.builder()
                    .title("Paid Japanese")
                    .level(CourseLevel.N4)
                    .courseType(CourseType.PAID)
                    .originalPrice(BigDecimal.valueOf(500000))
                    .salePrice(BigDecimal.ZERO)
                    .build();

            when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(teacher));

            assertThatThrownBy(() -> courseAdminService.createCourse(req))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_SALE_PRICE_INVALID);
        }

        @Test
        @DisplayName("PAID course with valid salePrice <= originalPrice succeeds")
        void createCourse_PaidCourse_ValidPrices_Success() {
            CourseCreateReq req = CourseCreateReq.builder()
                    .title("Paid Japanese N4")
                    .level(CourseLevel.N4)
                    .courseType(CourseType.PAID)
                    .originalPrice(BigDecimal.valueOf(500000))
                    .salePrice(BigDecimal.valueOf(350000))
                    .build();

            when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(teacher));
            when(courseRepository.existsBySlug("paid-japanese-n4")).thenReturn(false);
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
                Course c = invocation.getArgument(0);
                c.setId(20L);
                return c;
            });

            CourseRes res = courseAdminService.createCourse(req);

            assertThat(res).isNotNull();
            assertThat(res.getCourseType()).isEqualTo("PAID");
            assertThat(res.getOriginalPrice()).isEqualByComparingTo(BigDecimal.valueOf(500000));
            assertThat(res.getSalePrice()).isEqualByComparingTo(BigDecimal.valueOf(350000));
        }
    }

    // =========================================================================
    // 2. STATE TRANSITIONS & UPDATE BYPASS PREVENTION
    // =========================================================================
    @Nested
    @DisplayName("State Transitions and Update Validations")
    class StateTransitionTests {

        @Test
        @DisplayName("Updating ARCHIVED course throws COURSE_INVALID_STATUS_TRANSITION")
        void updateCourse_ArchivedCourse_ThrowsInvalidTransition() {
            Course course = Course.builder()
                    .id(1L)
                    .teacher(teacher)
                    .status(CourseStatus.ARCHIVED)
                    .courseType(CourseType.FREE)
                    .title("Archived Course")
                    .slug("archived-course")
                    .build();

            CourseUpdateReq updateReq = CourseUpdateReq.builder()
                    .title("Trying to revive")
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .status(CourseStatus.DRAFT)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

            assertThatThrownBy(() -> courseAdminService.updateCourse(1L, updateReq))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_INVALID_STATUS_TRANSITION);
        }

        @Test
        @DisplayName("Transition DRAFT to HIDDEN throws COURSE_INVALID_STATUS_TRANSITION")
        void updateCourse_DraftToHidden_ThrowsInvalidTransition() {
            Course course = Course.builder()
                    .id(1L)
                    .teacher(teacher)
                    .status(CourseStatus.DRAFT)
                    .courseType(CourseType.FREE)
                    .title("Draft Course")
                    .slug("draft-course")
                    .build();

            CourseUpdateReq updateReq = CourseUpdateReq.builder()
                    .title("Draft Course")
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .status(CourseStatus.HIDDEN)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

            assertThatThrownBy(() -> courseAdminService.updateCourse(1L, updateReq))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_INVALID_STATUS_TRANSITION);
        }

        @Test
        @DisplayName("Transition PUBLISHED to DRAFT throws COURSE_INVALID_STATUS_TRANSITION")
        void updateCourse_PublishedToDraft_ThrowsInvalidTransition() {
            Course course = Course.builder()
                    .id(1L)
                    .teacher(teacher)
                    .status(CourseStatus.PUBLISHED)
                    .courseType(CourseType.FREE)
                    .title("Published Course")
                    .slug("published-course")
                    .build();

            CourseUpdateReq updateReq = CourseUpdateReq.builder()
                    .title("Published Course")
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .status(CourseStatus.DRAFT)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

            assertThatThrownBy(() -> courseAdminService.updateCourse(1L, updateReq))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_INVALID_STATUS_TRANSITION);
        }

        @Test
        @DisplayName("Update request to PUBLISHED on course with no lessons throws COURSE_CANNOT_PUBLISH_EMPTY")
        void updateCourse_ToPublished_NoLessons_ThrowsCannotPublishEmpty() {
            Course course = Course.builder()
                    .id(1L)
                    .teacher(teacher)
                    .status(CourseStatus.DRAFT)
                    .courseType(CourseType.FREE)
                    .title("Draft Course")
                    .slug("draft-course")
                    .build();

            CourseUpdateReq updateReq = CourseUpdateReq.builder()
                    .title("Draft Course")
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .status(CourseStatus.PUBLISHED)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(sectionRepository.countByCourseId(1L)).thenReturn(0L);
            when(lessonRepository.countByCourseId(1L)).thenReturn(0L);

            assertThatThrownBy(() -> courseAdminService.updateCourse(1L, updateReq))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_CANNOT_PUBLISH_EMPTY);
        }

        @Test
        @DisplayName("Update request to PUBLISHED with no published lessons throws COURSE_CANNOT_PUBLISH_NO_PUBLISHED_LESSONS")
        void updateCourse_ToPublished_NoPublishedLessons_ThrowsError() {
            Course course = Course.builder()
                    .id(1L)
                    .teacher(teacher)
                    .status(CourseStatus.DRAFT)
                    .courseType(CourseType.FREE)
                    .title("Draft Course")
                    .slug("draft-course")
                    .build();

            CourseUpdateReq updateReq = CourseUpdateReq.builder()
                    .title("Draft Course")
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .status(CourseStatus.PUBLISHED)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(sectionRepository.countByCourseId(1L)).thenReturn(1L);
            when(lessonRepository.countByCourseId(1L)).thenReturn(1L);
            when(sectionRepository.existsByCourseIdAndStatus(1L, CourseStatus.PUBLISHED)).thenReturn(false);

            assertThatThrownBy(() -> courseAdminService.updateCourse(1L, updateReq))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_CANNOT_PUBLISH_NO_PUBLISHED_LESSONS);
        }

        @Test
        @DisplayName("Update request with valid published curriculum succeeds")
        void updateCourse_ToPublished_ValidCurriculum_Success() {
            Course course = Course.builder()
                    .id(1L)
                    .teacher(teacher)
                    .status(CourseStatus.DRAFT)
                    .courseType(CourseType.FREE)
                    .title("Draft Course")
                    .slug("draft-course")
                    .build();

            CourseUpdateReq updateReq = CourseUpdateReq.builder()
                    .title("Updated Course")
                    .slug("updated-course")
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .status(CourseStatus.PUBLISHED)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(sectionRepository.countByCourseId(1L)).thenReturn(1L);
            when(lessonRepository.countByCourseId(1L)).thenReturn(1L);
            when(sectionRepository.existsByCourseIdAndStatus(1L, CourseStatus.PUBLISHED)).thenReturn(true);
            when(lessonRepository.hasPublishedLessonsInPublishedSections(1L)).thenReturn(true);
            when(courseRepository.existsBySlug("updated-course")).thenReturn(false);
            when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

            CourseRes res = courseAdminService.updateCourse(1L, updateReq);

            assertThat(res.getStatus()).isEqualTo("PUBLISHED");
            assertThat(res.getTitle()).isEqualTo("Updated Course");
        }
    }

    // =========================================================================
    // 3. PUBLISH COURSE LOGIC
    // =========================================================================
    @Nested
    @DisplayName("Course Publication Invariants")
    class PublishCourseTests {

        @Test
        @DisplayName("Publishing ARCHIVED course throws COURSE_INVALID_STATUS_TRANSITION")
        void publishCourse_Archived_ThrowsInvalidTransition() {
            Course course = Course.builder()
                    .id(1L)
                    .teacher(teacher)
                    .status(CourseStatus.ARCHIVED)
                    .courseType(CourseType.FREE)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

            assertThatThrownBy(() -> courseAdminService.publishCourse(1L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_INVALID_STATUS_TRANSITION);
        }

        @Test
        @DisplayName("Publishing empty course throws COURSE_CANNOT_PUBLISH_EMPTY")
        void publishCourse_Empty_ThrowsCannotPublishEmpty() {
            Course course = Course.builder()
                    .id(1L)
                    .teacher(teacher)
                    .status(CourseStatus.DRAFT)
                    .courseType(CourseType.FREE)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(sectionRepository.countByCourseId(1L)).thenReturn(0L);
            when(lessonRepository.countByCourseId(1L)).thenReturn(0L);

            assertThatThrownBy(() -> courseAdminService.publishCourse(1L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_CANNOT_PUBLISH_EMPTY);
        }

        @Test
        @DisplayName("Publishing course with only draft sections throws COURSE_CANNOT_PUBLISH_NO_PUBLISHED_LESSONS")
        void publishCourse_DraftSectionsOnly_ThrowsError() {
            Course course = Course.builder()
                    .id(1L)
                    .teacher(teacher)
                    .status(CourseStatus.DRAFT)
                    .courseType(CourseType.FREE)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(sectionRepository.countByCourseId(1L)).thenReturn(1L);
            when(lessonRepository.countByCourseId(1L)).thenReturn(1L);
            when(sectionRepository.existsByCourseIdAndStatus(1L, CourseStatus.PUBLISHED)).thenReturn(false);

            assertThatThrownBy(() -> courseAdminService.publishCourse(1L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_CANNOT_PUBLISH_NO_PUBLISHED_LESSONS);
        }

        @Test
        @DisplayName("Publishing course with published section but only draft lessons throws COURSE_CANNOT_PUBLISH_NO_PUBLISHED_LESSONS")
        void publishCourse_PublishedSectionDraftLessons_ThrowsError() {
            Course course = Course.builder()
                    .id(1L)
                    .teacher(teacher)
                    .status(CourseStatus.DRAFT)
                    .courseType(CourseType.FREE)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(sectionRepository.countByCourseId(1L)).thenReturn(1L);
            when(lessonRepository.countByCourseId(1L)).thenReturn(1L);
            when(sectionRepository.existsByCourseIdAndStatus(1L, CourseStatus.PUBLISHED)).thenReturn(true);
            when(lessonRepository.hasPublishedLessonsInPublishedSections(1L)).thenReturn(false);

            assertThatThrownBy(() -> courseAdminService.publishCourse(1L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_CANNOT_PUBLISH_NO_PUBLISHED_LESSONS);
        }

        @Test
        @DisplayName("Publishing PAID course with invalid pricing throws error")
        void publishCourse_PaidInvalidPricing_ThrowsError() {
            Course course = Course.builder()
                    .id(1L)
                    .teacher(teacher)
                    .status(CourseStatus.DRAFT)
                    .courseType(CourseType.PAID)
                    .originalPrice(null)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

            assertThatThrownBy(() -> courseAdminService.publishCourse(1L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_PRICE_REQUIRED);
        }

        @Test
        @DisplayName("Publishing course with valid curriculum and pricing succeeds")
        void publishCourse_ValidCurriculumAndPricing_Success() {
            Course course = Course.builder()
                    .id(1L)
                    .teacher(teacher)
                    .status(CourseStatus.DRAFT)
                    .courseType(CourseType.PAID)
                    .originalPrice(BigDecimal.valueOf(500000))
                    .salePrice(BigDecimal.valueOf(350000))
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(sectionRepository.countByCourseId(1L)).thenReturn(1L);
            when(lessonRepository.countByCourseId(1L)).thenReturn(1L);
            when(sectionRepository.existsByCourseIdAndStatus(1L, CourseStatus.PUBLISHED)).thenReturn(true);
            when(lessonRepository.hasPublishedLessonsInPublishedSections(1L)).thenReturn(true);
            when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

            CourseRes res = courseAdminService.publishCourse(1L);

            assertThat(res.getStatus()).isEqualTo("PUBLISHED");
            verify(courseRepository).save(course);
        }
    }

    // =========================================================================
    // 4. HIDE & DELETE OPERATIONS
    // =========================================================================
    @Nested
    @DisplayName("Hide and Delete State Transitions")
    class HideAndDeleteTests {

        @Test
        @DisplayName("Hiding DRAFT course throws COURSE_INVALID_STATUS_TRANSITION")
        void hideCourse_DraftCourse_ThrowsError() {
            Course course = Course.builder()
                    .id(1L)
                    .teacher(teacher)
                    .status(CourseStatus.DRAFT)
                    .courseType(CourseType.FREE)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

            assertThatThrownBy(() -> courseAdminService.hideCourse(1L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_INVALID_STATUS_TRANSITION);
        }

        @Test
        @DisplayName("Hiding PUBLISHED course succeeds and sets status to HIDDEN")
        void hideCourse_PublishedCourse_Success() {
            Course course = Course.builder()
                    .id(1L)
                    .teacher(teacher)
                    .status(CourseStatus.PUBLISHED)
                    .courseType(CourseType.FREE)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
            when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

            CourseRes res = courseAdminService.hideCourse(1L);

            assertThat(res.getStatus()).isEqualTo("HIDDEN");
            assertThat(course.getStatus()).isEqualTo(CourseStatus.HIDDEN);
        }

        @Test
        @DisplayName("Deleting ARCHIVED course throws COURSE_INVALID_STATUS_TRANSITION")
        void deleteCourse_AlreadyArchived_ThrowsError() {
            Course course = Course.builder()
                    .id(1L)
                    .teacher(teacher)
                    .status(CourseStatus.ARCHIVED)
                    .courseType(CourseType.FREE)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

            assertThatThrownBy(() -> courseAdminService.deleteCourse(1L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_INVALID_STATUS_TRANSITION);
        }

        @Test
        @DisplayName("Deleting course sets status to ARCHIVED")
        void deleteCourse_DraftCourse_Success() {
            Course course = Course.builder()
                    .id(1L)
                    .teacher(teacher)
                    .status(CourseStatus.DRAFT)
                    .courseType(CourseType.FREE)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

            courseAdminService.deleteCourse(1L);

            assertThat(course.getStatus()).isEqualTo(CourseStatus.ARCHIVED);
            verify(courseRepository).save(course);
        }
    }

    // =========================================================================
    // 5. TEACHER DATA ISOLATION PRESERVATION
    // =========================================================================
    @Nested
    @DisplayName("Teacher Ownership Isolation Preservation")
    class TeacherIsolationTests {

        @Test
        @DisplayName("Teacher cannot update course owned by another teacher (403 DATA_ISOLATION_FORBIDDEN)")
        void updateCourse_CrossTeacher_ThrowsForbidden() {
            Course course = Course.builder()
                    .id(1L)
                    .teacher(otherTeacher)
                    .status(CourseStatus.DRAFT)
                    .courseType(CourseType.FREE)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

            CourseUpdateReq updateReq = CourseUpdateReq.builder()
                    .title("Hacked")
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .status(CourseStatus.DRAFT)
                    .build();

            assertThatThrownBy(() -> courseAdminService.updateCourse(1L, updateReq))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_ISOLATION_FORBIDDEN);
        }

        @Test
        @DisplayName("Teacher cannot publish course owned by another teacher (403 DATA_ISOLATION_FORBIDDEN)")
        void publishCourse_CrossTeacher_ThrowsForbidden() {
            Course course = Course.builder()
                    .id(1L)
                    .teacher(otherTeacher)
                    .status(CourseStatus.DRAFT)
                    .courseType(CourseType.FREE)
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

            assertThatThrownBy(() -> courseAdminService.publishCourse(1L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_ISOLATION_FORBIDDEN);
        }

        @Test
        @DisplayName("ADMIN role bypasses teacher check")
        void adminRole_BypassesTeacherCheck_Success() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                            "admin@example.com",
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    )
            );

            Course course = Course.builder()
                    .id(1L)
                    .teacher(otherTeacher)
                    .status(CourseStatus.DRAFT)
                    .courseType(CourseType.FREE)
                    .title("Course")
                    .slug("course")
                    .build();

            when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

            CourseRes res = courseAdminService.getCourse(1L);
            assertThat(res).isNotNull();
        }
    }
}
