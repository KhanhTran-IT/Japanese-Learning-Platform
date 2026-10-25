# MariaDB Production Backup & Disaster Recovery Architecture

> **Mã tài liệu:** DR-OPS-371  
> **Áp dụng cho:** Hệ thống Japanese Learning Platform (Môi trường Production)  
> **Cập nhật lần cuối:** 2026-10-25  

---

## Mục lục
1. [Tổng quan Kiến trúc & Phạm vi Dữ liệu](#1-tổng-quan-kiến-trúc--phạm-vi-dữ-liệu)
2. [Chiến lược Dữ liệu Redis (Persistence vs Rebuild)](#2-chiến-lược-dữ-liệu-redis-persistence-vs-rebuild)
3. [Mục tiêu Phục hồi: RPO và RTO](#3-mục-tiêu-phục-hồi-rpo-và-rto)
4. [Quy trình Sao lưu (Backup Workflow)](#4-quy-trình-sao-lưu-backup-workflow)
   - [Đặc tả kỹ thuật Pipeline](#đặc-tả-kỹ-thuật-pipeline)
   - [Chính sách Lưu trữ (Tiered GFS Retention)](#chính-sách-lưu-trữ-tiered-gfs-retention)
   - [Đồng bộ Lưu trữ Ngoài (External Storage Sync)](#đồng-bộ-lưu-trữ-ngoài-external-storage-sync)
5. [Quy trình Phục hồi & Chốt chặn An toàn (Safe Restore)](#5-quy-trình-phục-hồi--chốt-chặn-an-toàn-safe-restore)
6. [Quy trình Kiểm tra Phục hồi Độc lập (Isolated Restore Verification)](#6-quy-trình-kiểm-tra-phục-hồi-độc-lập-isolated-restore-verification)
7. [Hệ thống Cảnh báo Sự cố (Failure Alerting)](#7-hệ-thống-cảnh-báo-sự-cố-failure-alerting)
8. [Quy trình Diễn tập Phục hồi Định kỳ (Disaster Recovery Drill Runbook)](#8-quy-trình-diễn-tập-phục-hồi-định-kỳ-disaster-recovery-drill-runbook)
9. [Kịch bản Ứng phó Sự cố (Incident Response Runbooks)](#9-kịch-bản-ứng-phó-sự-cố-incident-response-runbooks)

---

## 1. Tổng quan Kiến trúc & Phạm vi Dữ liệu

Trong hệ thống Japanese Learning Platform, toàn bộ dữ liệu nghiệp vụ có tính lâu dài (persistent state) được lưu trữ tập trung tại **MariaDB 10.11 InnoDB**:
- **Tài khoản & Phân quyền:** `users`, `roles`, `user_roles`.
- **Khóa học & Tiến độ học tập:** `courses`, `course_sections`, `lessons`, `course_enrollments`, `lesson_progress`.
- **Đánh giá & Bài kiểm tra:** `quizzes`, `quiz_questions`, `quiz_answers`, `quiz_attempts`, `quiz_attempt_answers`.
- **Học từ vựng:** `flashcard_decks`, `flashcards`, `flashcard_progress`, `flashcard_review_logs`.
- **Bảng xếp hạng:** `leaderboard_ranks`.
- **Đơn hàng & Thanh toán:** `orders`, `order_items`, `payments`.
- **Bảo mật phiên đăng nhập:** `refresh_tokens` (được băm SHA-256 an toàn theo RFC 7519 `jti`).
- **Lịch sử migration CSDL:** `flyway_schema_history`.

Toàn bộ các bảng trên thuộc diện **Bắt buộc Sao lưu An toàn (Mission-Critical Data)**.

---

## 2. Chiến lược Dữ liệu Redis (Persistence vs Rebuild)

### 2.1. Bản chất dữ liệu trong Redis
Trong kiến trúc backend của hệ thống, Redis được sử dụng cho đúng 2 mục đích:
1. **Phân tán Giới hạn Tần suất (Distributed Rate Limiting):** Sử dụng `RedisRateLimiterService` với cấu trúc Sorted Set (`ZSET`) trượt (sliding window) để đếm số lần đăng nhập / làm mới token theo IP và Email. Cửa sổ thời gian tối đa là **15 phút**.
2. **Kiểm tra Sức khỏe Hệ thống (Readiness Probe):** `RedisHealthIndicator` gửi lệnh `PING` tới Redis để báo cáo trạng thái sẵn sàng nhận traffic cho Kubernetes / Docker health check.

Redis **HOÀN TOÀN KHÔNG** lưu trữ:
- Session hay Refresh Token (toàn bộ Refresh Token được lưu trong bảng MariaDB `refresh_tokens`).
- Thông tin giỏ hàng hay đơn hàng (lưu trong bảng MariaDB `orders`).
- Dữ liệu người dùng hay cache bài học không thể tái tạo.

### 2.2. Kết luận Kiến trúc
> **QUYẾT ĐỊNH:** Dữ liệu trong Redis **KHÔNG CẦN BACKUP NGOẠI BIÊN (NO COLD BACKUP NEEDED)** và **CÓ THỂ TÁI TẠO TỨC THÌ (INSTANTLY REBUILT)** khi khởi động lại container.

- **Khi Redis bị crash hoặc restart:**
  - Bộ đếm rate limit 15 phút sẽ được reset về 0. Sau thời điểm restart, hệ thống bắt đầu tính cửa sổ 15 phút mới.
  - Không có bất kỳ dữ liệu người dùng, tiến độ học tập hay giao dịch tài chính nào bị mất.
  - Các phiên đăng nhập hiện tại không bị đăng xuất vì JWT stateless và refresh token nằm trong MariaDB.
- **Cấu hình Container:** Trong `docker-compose.prod.yml`, volume `redis_data_prod:/data` đã được gắn sẵn để hỗ trợ lưu trữ tạm thời qua cơ chế RDB/AOF mặc định khi container khởi động lại nhanh. Tuy nhiên, volume này **được miễn trừ (exempt)** khỏi các tác vụ backup định kỳ và lưu trữ ngoại vi để tiết kiệm chi phí băng thông và lưu trữ.

---

## 3. Mục tiêu Phục hồi: RPO và RTO

| Chỉ số | Mục tiêu (Target) | Giải thích & Cơ chế Đảm bảo |
|---|---|---|
| **RPO** (Recovery Point Objective) | **≤ 24 giờ** (Mặc định)<br>**≤ 15 phút** (Khi bật Binlog) | - **Mặc định:** Bản backup toàn phần (Full Dump) được chụp tự động mỗi đêm lúc 02:00 UTC.<br>- **Nâng cao (Point-in-Time Recovery - PITR):** Khi bật MariaDB Binary Logging (`log_bin=ON`), toàn bộ thay đổi giữa 2 lần full backup được lưu trong binlog, cho phép phục hồi về bất kỳ giây nào trước sự cố. |
| **RTO** (Recovery Time Objective) | **< 30 phút** | Thời gian tối đa để: phát hiện sự cố (5m), tải bản backup mã hóa từ S3 (5m), xác thực checksum & giải mã stream nạp vào MariaDB (10m), backend readiness probe pass (5m), kiểm tra xác nhận (5m). |
| **Tần suất Kiểm tra Độc lập** | **Hàng ngày** | Script `verify-restore.sh` tự động xác minh cấu trúc và mã băm ngay sau mỗi lần backup. |
| **Tần suất Diễn tập Toàn diện** | **Hàng quý** | Đội ngũ kỹ thuật thực hiện diễn tập theo kịch bản khôi phục thảm họa (Disaster Recovery Drill). |

---

## 4. Quy trình Sao lưu (Backup Workflow)

Tất cả các tác vụ sao lưu được thực thi thông qua script sản xuất [scripts/backup/backup.sh](JapaneseLearningWeb/scripts/backup/backup.sh).

```
┌──────────────┐     --single-transaction     ┌──────────────┐
│   MariaDB    │ ───────────────────────────► │  mariadb-    │
│  (Container) │       (Online/Lock-free)     │     dump     │
└──────────────┘                              └──────┬───────┘
                                                     │ SQL Stream
                                                     ▼
                                              ┌──────────────┐
                                              │   gzip -9    │
                                              └──────┬───────┘
                                                     │ Compressed
                                                     ▼
                                              ┌──────────────┐
                                              │   openssl    │ AES-256-CBC
                                              │ (PBKDF2/Salt)│ 100k iters
                                              └──────┬───────┘
                                                     │
                             ┌───────────────────────┴───────────────────────┐
                             ▼                                               ▼
                     ┌───────────────┐                               ┌───────────────┐
                     │ .sql.gz.enc   │                               │ .sha256 Hash  │
                     │  (Encrypted)  │                               │ & .meta.json  │
                     └───────┬───────┘                               └───────┬───────┘
                             │                                               │
                             └───────────────────────┬───────────────────────┘
                                                     ▼
                                            ┌─────────────────┐
                                            │ Local Retention │
                                            │  (Daily/Wk/Mo)  │
                                            └────────┬────────┘
                                                     ▼
                                            ┌─────────────────┐
                                            │ External S3/R2  │
                                            │  Remote Storage │
                                            └─────────────────┘
```

### 4.1. Đặc tả kỹ thuật Pipeline

1. **Dump Không Khóa (Non-blocking Consistent Snapshot):**
   - Lệnh `mariadb-dump` kích hoạt cờ:
     - `--single-transaction`: Sử dụng MVCC (Multi-Version Concurrency Control) của InnoDB để chụp snapshot nhất quán tại một thời điểm mà **KHÔNG KHÓA BẢNG**. Học viên vẫn làm bài quiz và đặt hàng bình thường trong lúc backup.
     - `--quick`: Đọc từng hàng thay vì tải cả bảng vào RAM, bảo vệ server khỏi hiện tượng Out-Of-Memory.
     - `--routines --triggers --events`: Sao lưu toàn bộ stored procedures, triggers và database events.
     - `--default-character-set=utf8mb4`: Đảm bảo toàn vẹn tuyệt đối cho chữ Kanji, Hiragana, Katakana và Emojis.
     - `--hex-blob`: Mã hóa trường nhị phân sang định dạng hex để chống lỗi ký tự ngắt dòng.
2. **Nén Tỷ Lệ Cao:** Luồng SQL được nén qua `gzip -9` ngay trên đường truyền (stream pipe), giảm 75-85% dung lượng trước khi ghi đĩa.
3. **Mã Hóa Chuẩn Quân Đội (Encryption at Rest):**
   - Thuật toán: **AES-256-CBC**.
   - Cơ chế dẫn xuất khóa: **PBKDF2** với **100,000 vòng lặp (iterations)** và Salt ngẫu nhiên được thêm tự động vào file header.
   - Không lưu trữ khóa mã hóa trong file sao lưu. Khóa được cấp qua biến môi trường bảo mật `BACKUP_ENCRYPTION_KEY` hoặc file bảo vệ `BACKUP_ENCRYPTION_KEY_FILE` (permission `0400`).
4. **Kiểm tra Toàn vẹn Tức thì:**
   - Kiểm tra ngưỡng dung lượng tối thiểu `MIN_BACKUP_SIZE_BYTES` (mặc định 10KB) để phát hiện dump rỗng do lỗi cấp quyền.
   - Thử nghiệm giải mã block đầu tiên của file để xác thực khóa mã hóa hoạt động tốt.
   - Sinh mã băm **SHA-256** lưu vào file companion `.sha256`.
5. **Ghi Đĩa Bất khả biến (Atomic Write):**
   - Toàn bộ quá trình dump được ghi vào thư mục tạm `tmp/*.tmp`.
   - Chỉ khi kiểm tra dung lượng, giải mã và checksum thành công 100%, file mới được `mv` (atomic rename) vào thư mục lưu trữ chính thức.

### 4.2. Chính sách Lưu trữ (Tiered GFS Retention)

Hệ thống áp dụng mô hình phân tầng **Grandfather-Father-Son (GFS)**:

| Tầng (Tier) | Quy tắc Phân loại | Tần suất | Thời gian Lưu (Retention) | Thư mục Lưu trữ |
|---|---|---|---|---|
| **Daily (Son)** | Các ngày trong tuần (T2 - T7) | Hàng ngày | **7 ngày** | `${BACKUP_DIR}/daily/` |
| **Weekly (Father)** | Ngày Chủ nhật hàng tuần | Hàng tuần | **4 tuần** | `${BACKUP_DIR}/weekly/` |
| **Monthly (Grandfather)** | Ngày mùng 1 hàng tháng | Hàng tháng | **12 tháng** | `${BACKUP_DIR}/monthly/` |

Tiến trình dọn dẹp cục bộ tự động quét và xóa các bản ghi vượt quá thời hạn retention trong từng thư mục sau mỗi lần backup thành công.

### 4.3. Đồng bộ Lưu trữ Ngoài (External Storage Sync)

Sau khi bản backup cục bộ được đóng gói và xác thực, script kích hoạt đồng bộ hóa sang kho lưu trữ ngoài độc lập thông qua biến `EXTERNAL_STORAGE_TYPE`:
- **Amazon S3 / Cloudflare R2 / MinIO (`s3`):** Đồng bộ qua `aws s3 cp` với cờ bảo mật. Khuyến nghị bật **Object Lock (WORM - Write Once Read Many)** trên bucket S3 để chống tấn công tống tiền (Ransomware).
- **Rclone (`rclone`):** Tương thích Google Cloud Storage, Backblaze B2, Dropbox, Wasabi.
- **Rsync / SFTP (`rsync`):** Đồng bộ qua SSH tới một máy chủ backup vật lý tách biệt.

---

## 5. Quy trình Phục hồi & Chốt chặn An toàn (Safe Restore)

Script phục hồi [scripts/backup/restore.sh](JapaneseLearningWeb/scripts/backup/restore.sh) được thiết kế với cơ chế an toàn chủ động:

### 5.1. Chốt chặn An toàn (Destructive Overwrite Guard)
> ⚠️ **QUY TẮC BẢO VỆ:** Khác với các script thông thường, `restore.sh` **TỰ ĐỘNG CHẶN VÀ DỪNG NGAY LẬP TỨC** nếu người dùng không truyền tham số tường minh `--confirm-destructive-overwrite`.

Cơ chế này loại trừ 100% rủi ro gõ nhầm lệnh trên terminal máy chủ production làm ghi đè mất mát dữ liệu đang online.

### 5.2. Các bước Thực thi Phục hồi

```bash
# Cú pháp phục hồi chuẩn
./scripts/backup/restore.sh <path-to-backup.sql.gz.enc> \
    --confirm-destructive-overwrite \
    --target-host 127.0.0.1 \
    --target-port 3306 \
    --target-db japanese_learning_db \
    --target-user root \
    --target-password "${DB_PASSWORD}"
```

**Thứ tự xử lý tự động của script:**
1. **Kiểm tra Checksum Trước Khi Giải mã:** Đọc file `.sha256`. Nếu mã băm hiện tại không khớp hoàn toàn với mã băm gốc, script lập tức từ chối thực thi với mã lỗi `2` (Integrity Violation), bảo vệ database khỏi việc nạp dữ liệu bị lỗi mạng hoặc bị can thiệp trái phép.
2. **Giải mã & Giải nén Stream:** Sử dụng OpenSSL AES-256-CBC PBKDF2 stream trực tiếp qua pipe tới MariaDB client, không ghi file SQL giải mã ra ổ đĩa (ngăn ngừa rò rỉ dữ liệu plaintext ra disk).
3. **Kiểm tra Mã Lỗi Pipe (`PIPESTATUS`):** Giám sát trạng thái thoát của từng mắt xích trong chuỗi `openssl -> gzip -> mariadb`. Nếu có bất kỳ lỗi cú pháp SQL hoặc kết nối đứt đoạn nào, script báo lỗi chi tiết.

---

## 6. Quy trình Kiểm tra Phục hồi Độc lập (Isolated Restore Verification)

Một trong những sai lầm phổ biến nhất của các hệ thống là "chỉ kiểm tra backup, không kiểm tra restore" cho đến khi thảm họa thực tế xảy ra và phát hiện file backup không thể phục hồi được.

Hệ thống tích hợp script chuyên dụng [scripts/backup/verify-restore.sh](JapaneseLearningWeb/scripts/backup/verify-restore.sh).

### 6.1. Nguyên tắc Cách ly Tuyệt đối
- **Tuyệt đối KHÔNG chạy lệnh kiểm tra trên database production.**
- Khi có Docker: Tự động khởi tạo một **Container MariaDB Tạm thời (Ephemeral Container)** trên một Docker Network riêng biệt và Volume tạm.
- Nạp bản backup vào container tạm thời này.

### 6.2. Các Bước Kiểm Tra Tính Toàn Vẹn Tự Động
Script thực hiện 4 giai đoạn kiểm chứng:
1. **Xác thực Checksum SHA-256:** Bảo đảm file không suy hao.
2. **Kiểm tra Cấu trúc Dump:** Đọc stream header xác thực nhãn MariaDB/MySQL và footer `-- Dump completed on ...` (chứng minh dump không bị ngắt giữa chừng).
3. **Kiểm tra Hiện diện Toàn bộ Bảng Cốt lõi:** Quét stream xác nhận đủ 13 bảng: `users`, `roles`, `user_roles`, `courses`, `course_sections`, `lessons`, `quizzes`, `quiz_questions`, `quiz_answers`, `orders`, `payments`, `refresh_tokens`, `flyway_schema_history`.
4. **Kiểm tra Dữ liệu Thực tế (Sanity Checks):**
   - Đếm số bản ghi migration thành công trong `flyway_schema_history`.
   - Đếm số lượng `users`, `courses`, `lessons`.
   - Kiểm tra mã hóa ký tự UTF-8 (Kanji/Kana) trong bảng `courses`.
5. **Dọn dẹp Tức thì (Auto Teardown):** Lệnh `trap cleanup EXIT` đảm bảo container và network tạm thời bị tiêu hủy sạch sẽ sau khi hoàn tất kiểm tra.

```bash
# Lệnh chạy kiểm tra độc lập
./scripts/backup/verify-restore.sh /var/backups/japanese-learning-db/daily/japanese_learning_db_daily_20261025_020000.sql.gz.enc
```

---

## 7. Hệ thống Cảnh báo Sự cố (Failure Alerting)

Script [scripts/backup/notify.sh](JapaneseLearningWeb/scripts/backup/notify.sh) được tích hợp trực tiếp vào bẫy lỗi `trap ERR` của các tiến trình sao lưu.

### 7.1. Các kênh Thông báo
Khi cấu hình biến `BACKUP_NOTIFICATION_WEBHOOK` trong `backup.env`, script tự động gửi payload JSON định dạng chuẩn tới:
- Slack Webhook (kênh `#ops-alerts`)
- Discord Webhook
- Alertmanager Generic Webhook Receiver

### 7.2. Bảng Mã Lỗi Tiêu Chuẩn (Exit Codes)
| Mã Lỗi | Ý nghĩa | Mức độ Cảnh báo | Hành động Yêu cầu |
|---|---|---|---|
| `0` | Thành công | INFO | Không có |
| `1` | Lỗi tham số, thiếu công cụ, dump fail | CRITICAL | Kiểm tra quyền truy cập CSDL hoặc dung lượng ổ đĩa |
| `2` | Sai lệch Checksum SHA-256 (Tamper / Corrupt) | CRITICAL | Kiểm tra đường truyền mạng hoặc kiểm tra mã độc |
| `3` | Lỗi giải mã OpenSSL (Sai key / Hỏng salt) | CRITICAL | Xác minh lại `BACKUP_ENCRYPTION_KEY` |
| `4` | Thiếu các bảng cốt lõi trong database | CRITICAL | Kiểm tra lại quyền của user `DB_USER` khi chạy dump |

### 7.3. Cảnh báo Định kỳ từ Prometheus
Tích hợp luật cảnh báo trong file cấu hình Prometheus Alertmanager (`prometheus_rules.yml`):
```yaml
- alert: MariaDBBackupMissingOrOutdated
  expr: time() - backup_last_successful_timestamp_seconds > 90000 # Quá 25 giờ chưa có backup mới
  for: 30m
  labels:
    severity: critical
  annotations:
    summary: "Bản sao lưu MariaDB hàng ngày chưa được tạo hoặc bị gián đoạn"
    description: "Không tìm thấy bản sao lưu mới trong hơn 25 giờ qua. Kiểm tra cronjob và log tại /var/log/db_backup.log."
```

---

## 8. Quy trình Diễn tập Phục hồi Định kỳ (Disaster Recovery Drill Runbook)

Diễn tập phục hồi định kỳ (DR Drill) là yêu cầu bắt buộc đối với hệ thống Production nhằm đảm bảo đội ngũ vận hành luôn nắm vững quy trình và các bản sao lưu luôn sẵn sàng phục vụ khi có sự cố.

### Lịch trình Thực hiện
- **Tần suất:** Mỗi quý 1 lần (Quý 1, Quý 2, Quý 3, Quý 4).
- **Môi trường:** Máy chủ Staging hoặc máy chủ diễn tập cô lập, **TUYỆT ĐỐI KHÔNG DIỄN TẬP TRỰC TIẾP TRÊN CỤM PRODUCTION**.

### 6 Bước Thực Hiện Diễn Tập

#### Bước 1: Giả lập Sự cố & Tuyên bố Diễn tập
1. Đội trưởng kỹ thuật (Lead Ops) ghi nhận thời điểm bắt đầu diễn tập: `DRILL_START_TIME=$(date +%s)`.
2. Giả lập kịch bản: Máy chủ cơ sở dữ liệu chính bị hỏng phần cứng hoặc volume bị xóa.

#### Bước 2: Tải Bản Sao lưu Từ Lưu Trữ Ngoại Biên (S3/Cloud)
Không dùng file backup sẵn có trên máy chủ, tải trực tiếp từ S3 để kiểm tra tính sẵn sàng của kho lưu trữ ngoài:
```bash
aws s3 cp s3://my-backups/japanese-db/daily/$(aws s3 ls s3://my-backups/japanese-db/daily/ | tail -1 | awk '{print $4}') /tmp/drill_backup.sql.gz.enc
aws s3 cp s3://my-backups/japanese-db/daily/$(aws s3 ls s3://my-backups/japanese-db/daily/ | tail -1 | awk '{print $4}').sha256 /tmp/drill_backup.sql.gz.enc.sha256
```

#### Bước 3: Xác minh Toàn vẹn Độc lập
Chạy script kiểm tra độc lập trên môi trường diễn tập:
```bash
./scripts/backup/verify-restore.sh /tmp/drill_backup.sql.gz.enc
```
Xác nhận màn hình in kết quả: `RESTORE VERIFICATION REPORT: [PASS]`.

#### Bước 4: Phục hồi vào Môi trường Diễn tập
Khởi chạy container MariaDB diễn tập và thực hiện phục hồi:
```bash
./scripts/backup/restore.sh /tmp/drill_backup.sql.gz.enc \
    --confirm-destructive-overwrite \
    --target-host 127.0.0.1 \
    --target-port 3307 \
    --target-db japanese_learning_db \
    --target-password "DrillTestPassword123!"
```

#### Bước 5: Kiểm tra Tính Năng Ứng Dụng (Smoke Test)
Khởi động container backend trỏ tới cơ sở dữ liệu diễn tập và kiểm tra:
1. `GET http://localhost:8080/actuator/health/readiness` trả về `{"status":"UP"}`.
2. Kiểm tra đăng nhập với tài khoản test học viên.
3. Kiểm tra hiển thị danh sách khóa học và tiến độ bài học.

#### Bước 6: Đánh giá RTO & Lập Biên bản Diễn tập
1. Tính toán thời gian phục hồi thực tế: `DRILL_END_TIME=$(date +%s)`.
   $$\text{Actual RTO} = \frac{\text{DRILL\_END\_TIME} - \text{DRILL\_START\_TIME}}{60} \text{ (phút)}$$
2. Đối soát: Nếu $\text{Actual RTO} \le 30\text{ phút}$, bài diễn tập đạt yêu cầu.
3. Lưu biên bản diễn tập gồm: Thời gian bắt đầu/kết thúc, tên file backup, mã SHA-256, số lượng bản ghi các bảng chính, chữ ký xác nhận của Lead Ops.
4. Xóa toàn bộ file tạm và container diễn tập.

---

## 9. Kịch bản Ứng phó Sự cố (Incident Response Runbooks)

### Kịch bản 1: Container MariaDB Bị Crash Hoặc Volume Bị Lỗi
1. Kiểm tra trạng thái: `docker-compose -f docker-compose.prod.yml ps db`.
2. Kiểm tra log: `docker logs japanese_learning_db_prod`.
3. Nếu volume bị hỏng không thể phục hồi:
   ```bash
   # Dừng container db
   docker-compose -f docker-compose.prod.yml stop db
   # Xóa container cũ
   docker-compose -f docker-compose.prod.yml rm -f db
   # Khởi tạo container db mới với volume sạch
   docker-compose -f docker-compose.prod.yml up -d db
   # Chờ container db healthy
   docker-compose -f docker-compose.prod.yml ps db
   # Thực hiện nạp lại bản backup gần nhất
   ./scripts/backup/restore.sh /var/backups/japanese-learning-db/daily/LATEST_BACKUP.sql.gz.enc \
       --confirm-destructive-overwrite \
       --target-password "${DB_PASSWORD}"
   # Khởi động lại backend để kết nối lại connection pool
   docker-compose -f docker-compose.prod.yml restart backend
   ```

### Kịch bản 2: Container Redis Bị Crash Hoặc Không Thể Kết Nối
1. Triệu chứng: Endpoint `/actuator/health/readiness` báo DOWN ở mục `redisCustomHealthIndicator`. Người dùng bị chặn đăng nhập nếu cấu hình `APP_RATE_LIMIT_REDIS_FAILURE_POLICY=FAIL_CLOSED`.
2. Khắc phục:
   ```bash
   # Khởi động lại Redis (dữ liệu rate limit tự động tạo mới, không cần restore)
   docker-compose -f docker-compose.prod.yml restart redis
   # Kiểm tra trạng thái Redis ping
   docker exec -it japanese_learning_redis_prod redis-cli -a "${REDIS_PASSWORD}" ping
   ```
3. Sau khi Redis trả về `PONG`, backend readiness probe tự động chuyển sang `UP` trong vòng 15 giây mà không cần thao tác nạp dữ liệu.
