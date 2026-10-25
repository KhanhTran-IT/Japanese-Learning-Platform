# MariaDB Production Backup & Recovery Toolkit

Bộ công cụ quản lý sao lưu và phục hồi dữ liệu MariaDB chuẩn Production cho hệ thống Japanese Learning Web.

## Mục lục
- [1. Cấu trúc thư mục](#1-cấu-trúc-thư-mục)
- [2. Cấu hình biến môi trường (`backup.env`)](#2-cấu-hình-biến-môi-trường-backupenv)
- [3. Hướng dẫn sử dụng](#3-hướng-dẫn-sử-dụng)
  - [Sao lưu thủ công](#sao-lưu-thủ-công)
  - [Lập lịch tự động qua Cron](#lập-lịch-tự-động-qua-cron)
  - [Phục hồi an toàn (`restore.sh`)](#phục-hồi-an-toàn-restoresh)
  - [Kiểm tra tính toàn vẹn độc lập (`verify-restore.sh`)](#kiểm-tra-tính-toàn-vẹn-độc-lập-verify-restoresh)
  - [Chạy bộ test tự động (`test_backup_workflow.sh`)](#chạy-bộ-test-tự-động-test_backup_workflowsh)
- [4. Nguyên tắc An toàn & Mã hóa](#4-nguyên-tắc-an-toàn--mã-hóa)

---

## 1. Cấu trúc thư mục

```text
scripts/backup/
├── backup.env.example       # Mẫu cấu hình biến môi trường sao lưu
├── backup.sh                # Script sao lưu: dump online, nén gzip, mã hóa AES-256, sha256, retention
├── restore.sh               # Script phục hồi an toàn kèm chốt chặn bảo vệ database production
├── verify-restore.sh        # Script kiểm tra phục hồi độc lập trên container tạm hoặc phân tích stream
├── notify.sh                # Helper gửi cảnh báo Webhook (Slack / Discord / Alertmanager)
├── test_backup_workflow.sh  # Bộ unit/integration test tự động cho toàn bộ workflow
└── README.md                # Tài liệu hướng dẫn sử dụng nhanh
```

---

## 2. Cấu hình biến môi trường (`backup.env`)

Sao chép từ file mẫu:
```bash
cp scripts/backup/backup.env.example scripts/backup/backup.env
chmod 600 scripts/backup/backup.env
```

Thiết lập các biến quan trọng:
- `DB_PASSWORD`: Mật khẩu MariaDB (trùng với `DB_PASSWORD` trong `.env.prod`).
- `BACKUP_ENCRYPTION_KEY`: Khóa mật mã tối thiểu 32 ký tự để mã hóa AES-256-CBC PBKDF2 (hoặc chỉ định qua `BACKUP_ENCRYPTION_KEY_FILE`).
- `BACKUP_DIR`: Thư mục lưu trữ bản backup cục bộ (mặc định `/var/backups/japanese-learning-db`).
- `RETENTION_DAYS_DAILY`: Số ngày lưu bản backup hàng ngày (mặc định 7).
- `RETENTION_WEEKS`: Số tuần lưu bản backup hàng tuần (mặc định 4).
- `RETENTION_MONTHS`: Số tháng lưu bản backup hàng tháng (mặc định 12).
- `EXTERNAL_STORAGE_TYPE`: `none` | `s3` | `rclone` | `rsync`.
- `EXTERNAL_STORAGE_DEST`: Địa chỉ bucket hoặc remote host (ví dụ: `s3://my-backups/japanese-db/`).
- `BACKUP_NOTIFICATION_WEBHOOK`: Webhook URL để nhận thông báo cảnh báo khi backup lỗi.

> ⚠️ **LƯU Ý:** File `backup.env` chứa mật khẩu và khóa mã hóa, **TUYỆT ĐỐI KHÔNG COMMIT LÊN GIT**. File này đã được thêm vào `.gitignore`.

---

## 3. Hướng dẫn sử dụng

### Sao lưu thủ công

```bash
# Tự động nhận diện tier (ngày 1 = monthly, chủ nhật = weekly, ngày khác = daily)
./scripts/backup/backup.sh

# Hoặc chỉ định rõ tier
./scripts/backup/backup.sh --tier monthly
```

Kết quả tạo ra 3 file:
1. `japanese_learning_db_<tier>_<timestamp>.sql.gz.enc`: File sao lưu đã nén và mã hóa.
2. `japanese_learning_db_<tier>_<timestamp>.sql.gz.enc.sha256`: Mã băm SHA-256 để chống giả mạo/hỏng hóc.
3. `japanese_learning_db_<tier>_<timestamp>.sql.gz.enc.meta.json`: Metadata về kích thước, thời gian, thuật toán và host.

### Lập lịch tự động qua Cron

Thêm vào crontab của server (`crontab -e`):

```bash
# Chạy backup mỗi ngày lúc 02:00 sáng và ghi log
0 2 * * * /opt/japanese-learning/scripts/backup/backup.sh >> /var/log/db_backup.log 2>&1
```

### Phục hồi an toàn (`restore.sh`)

Script có cơ chế **Chốt chặn an toàn (Destructive Overwrite Guard)**: Bắt buộc truyền cờ `--confirm-destructive-overwrite` mới cho phép thực thi, tránh vô tình chạy đè database đang hoạt động.

```bash
# Phục hồi vào target database
./scripts/backup/restore.sh /path/to/backup.sql.gz.enc \
    --confirm-destructive-overwrite \
    --target-host 127.0.0.1 \
    --target-port 3306 \
    --target-db japanese_learning_db \
    --target-user root \
    --target-password "SECRET_PASS"
```

### Kiểm tra tính toàn vẹn độc lập (`verify-restore.sh`)

Script này kiểm tra bản backup mà **KHÔNG HỀ ĐỘNG ĐẾN DATABASE PRODUCTION**:

```bash
./scripts/backup/verify-restore.sh /path/to/backup.sql.gz.enc
```

Quy trình xác minh:
1. Xác thực mã băm SHA-256.
2. Kiểm tra header dump MariaDB và footer `Dump completed on`.
3. Kiểm tra sự hiện diện của 13 bảng cốt lõi (`users`, `roles`, `courses`, `lessons`, `quizzes`, `orders`, `payments`, `refresh_tokens`, `flyway_schema_history`...).
4. Nếu có Docker: Tự động khởi tạo một container MariaDB độc lập (ephemeral container), nạp dữ liệu vào, chạy query đếm bản ghi và kiểm tra UTF-8 tiếng Nhật, sau đó dọn dẹp sạch sẽ container và volume test.

### Chạy bộ test tự động (`test_backup_workflow.sh`)

```bash
./scripts/backup/test_backup_workflow.sh
```
Kiểm tra tự động 6 kịch bản:
- Sinh dump mẫu chuẩn UTF-8 tiếng Nhật.
- Nén và mã hóa AES-256 PBKDF2.
- Kiểm tra xác minh thành công.
- Kiểm tra tính năng phát hiện file bị sửa đổi / giả mạo (Tamper detection).
- Kiểm tra từ chối khi nhập sai khóa giải mã.
- Kiểm tra chốt chặn an toàn chống ghi đè nhầm của `restore.sh`.

---

## 4. Nguyên tắc An toàn & Mã hóa

1. **Non-blocking Online Backup**: Sử dụng cờ `--single-transaction --quick --hex-blob` của MariaDB Dump, bảo đảm đọc nhất quán (MVCC) cho bảng InnoDB mà không khóa bảng hay làm gián đoạn học viên đang học bài / thanh toán.
2. **Bảo mật dữ liệu khi lưu trữ (Encryption at Rest)**: Sử dụng thuật toán chuẩn quân đội AES-256-CBC kèm hàm dẫn xuất khóa PBKDF2 với 100,000 vòng lặp và Salt ngẫu nhiên.
3. **Toàn vẹn mật mã (Cryptographic Integrity)**: Luôn đi kèm file băm SHA-256 độc lập. Mọi thao tác restore/verify đều kiểm tra mã băm trước khi bắt đầu giải mã.
