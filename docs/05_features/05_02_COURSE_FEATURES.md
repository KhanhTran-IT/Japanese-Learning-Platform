> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## 5.2. Nhóm chức năng khóa học

### 5.2.1. Khóa học

Một khóa học gồm:

```text
Tên khóa học
Slug URL
Ảnh đại diện
Mô tả ngắn
Mô tả chi tiết
Level: N5/N4/N3/N2/N1
Loại: miễn phí/trả phí
Giá gốc
Giá khuyến mãi
Trạng thái: draft/published/hidden/archived
Số lượng học viên
Rating trung bình
Tổng thời lượng
Teacher phụ trách
Ngày tạo
Ngày cập nhật
```

### 5.2.2. Chương học

Một khóa học có nhiều chương:

```text
Course
  Section 1
    Lesson 1
    Lesson 2
  Section 2
    Lesson 3
    Lesson 4
```

Thông tin chương:

```text
Tên chương
Mô tả
Thứ tự hiển thị
Trạng thái hiển thị
```

### 5.2.3. Bài học

Một bài học gồm:

```text
Tiêu đề bài học
Slug
Nội dung text
Video URL
Audio URL
File tài liệu
Thời lượng
Thứ tự hiển thị
Có miễn phí xem thử hay không
Quiz sau bài học
Trạng thái bài học
```

### 5.2.4. Ghi danh khóa học

Chức năng:

- User đăng ký khóa học miễn phí.
- User mua khóa học trả phí.
- Sau thanh toán thành công, hệ thống tự mở khóa.
- Theo dõi ngày đăng ký.
- Theo dõi ngày hết hạn nếu có.
- Theo dõi phần trăm hoàn thành.

---
