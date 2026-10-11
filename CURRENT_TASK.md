# CURRENT TASK

## Task hiện tại
Backend Leaderboard Cache Personalization Fix

## Trạng thái
DONE

## Mục tiêu
Sửa known issue trong `BUG_LOG.md` #012: `LeaderboardServiceImpl.getLeaderboard()` đang dùng `@Cacheable` trên method có logic phụ thuộc current user (`isMe`, `currentUserRanking`). Điều này có thể làm cache pollution: user sau nhìn thấy `isMe` hoặc ranking cá nhân của user trước.

## Vì sao làm task này?
Project hiện đã có hệ thống leaderboard, XP, privacy và profile settings. Frontend contract đã được harden, nhưng backend vẫn còn rủi ro cache personalization. Đây là bug dữ liệu người dùng, ảnh hưởng trực tiếp UX và privacy, nên nên xử lý trước khi mở rộng leaderboard thêm.

## Không làm trong task này
- Không thiết kế lại toàn bộ leaderboard.
- Không đổi schema leaderboard nếu không bắt buộc.
- Không làm badge/achievement mới.
- Không làm notification realtime.
- Không tối ưu Redis phức tạp nếu chưa cần.
- Không thay đổi scoring rule XP.

## File tài liệu cần dùng
- `docs/learning/BUG_LOG.md`
- `docs/learning/CONCEPTS_EXPLAINED.md`
- `docs/23_MVP_SCOPE.md`
- `docs/26_API_PRIORITY.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`
- `docs/05_features/05_06_GAME_GAMIFICATION_FEATURES.md`
- `docs/07_database/07_07_GAME_GAMIFICATION.md`

## Vấn đề hiện tại

Code hiện tại:

```java
@Cacheable(value = "leaderboard", key = "#timeframe + '-' + #page + '-' + #size")
public LeaderboardRes getLeaderboard(String timeframe, int page, int size) {
    User currentUser = getCurrentUser();
    // map rankings với isMe dựa trên currentUser
    // tính currentUserRanking
}
```

Rủi ro:
- Cache key không chứa user.
- Response có dữ liệu user-specific.
- User A gọi trước sẽ cache `isMe = true` cho A.
- User B gọi cùng timeframe/page/size có thể nhận response cache của A.

## Hướng xử lý đề xuất

Ưu tiên cách an toàn, scope nhỏ:

### Option A - Tắt cache ở method personalized
- Remove `@Cacheable` khỏi `getLeaderboard`.
- Giữ logic hiện tại.
- Thêm test chứng minh user A/user B không bị reuse personalized response.
- Ưu điểm: đơn giản, an toàn ngay.
- Nhược điểm: mất cache read performance.

### Option B - Tách cache public rankings và personalization
- Tạo method chỉ lấy public rankings không có `isMe/currentUserRanking`.
- Cache method public đó theo `timeframe/page/size`.
- `getLeaderboard()` không cache, chỉ:
  - lấy public rankings từ cached method,
  - set `isMe` theo current user,
  - tính `currentUserRanking` riêng.
- Ưu điểm: giữ cache cho phần public.
- Nhược điểm: cần cẩn thận self-invocation của Spring Cache; cached method phải được gọi qua proxy hoặc tách sang service riêng.

Khuyến nghị: làm Option A nếu muốn fix nhanh chắc chắn; làm Option B nếu có thời gian và test rõ.

## File cần kiểm tra/chỉnh sửa
- `backend/src/main/java/com/japaneselearning/module_leaderboard/service/LeaderboardServiceImpl.java`
- `backend/src/test/java/com/japaneselearning/module_leaderboard/service/LeaderboardServiceImplTest.java`
- Có thể thêm test controller/cache nếu cần.

## Checklist
- [x] Không cache response chứa `isMe/currentUserRanking` theo key public chung.
- [x] User A và User B gọi leaderboard cùng timeframe/page không bị lẫn `isMe`.
- [x] User private vẫn không có `currentUserRanking`.
- [x] Ranking public vẫn lọc `isPrivateLeaderboard = false`.
- [x] Test backend cover cache/personalization bug.
- [x] Backend test pass với Java 21 hoặc blocker được ghi rõ.

## Cách test sau khi hoàn thành
1. Tạo 2 user có score khác nhau.
2. Gọi leaderboard với user A, kiểm tra `isMe` chỉ đúng user A.
3. Gọi leaderboard cùng params với user B, kiểm tra `isMe` chuyển sang user B.
4. Set user B private, gọi lại và kiểm tra `currentUserRanking = null`.
5. Chạy `mvn -Dtest=LeaderboardServiceImplTest test` bằng JDK 21.
