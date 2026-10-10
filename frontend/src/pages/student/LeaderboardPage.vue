<template>
  <div class="min-h-screen bg-[var(--background)]">
    <!-- Header -->
    <div class="bg-[var(--foreground)] text-white pb-0">
      <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 pt-12">
        <div class="text-center mb-8">
          <div class="text-5xl mb-3">🏆</div>
          <h1 class="text-4xl font-display font-bold mb-2">Bảng xếp hạng</h1>
          <p class="text-white/60">Những học viên xuất sắc nhất của BrianJP</p>
        </div>

        <!-- Tabs -->
        <div class="flex border-b border-white/10">
          <button
            v-for="(tab, i) in tabs"
            :key="tab.value"
            @click="activeTab = i"
            :class="[
              'px-6 py-3 text-sm font-medium transition-colors',
              activeTab === i ? 'text-white border-b-2 border-[var(--accent)]' : 'text-white/50 hover:text-white/80'
            ]"
          >
            {{ tab.label }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="loading" class="flex justify-center items-center py-20">
      <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-[var(--primary)]"></div>
    </div>
    <div v-else-if="error" class="text-center text-red-500 py-10">
      {{ error }}
    </div>
    <div v-else class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Podium top 3 -->
      <div v-if="top3.length > 0" class="flex items-end justify-center gap-4 mb-10">
        <!-- 2nd -->
        <div v-if="top3.length > 1" class="flex flex-col items-center">
          <div class="w-14 h-14 rounded-full bg-gradient-to-br from-gray-300 to-gray-400 flex items-center justify-center text-white font-bold text-lg mb-2 border-4 border-white shadow-lg overflow-hidden">
            <img v-if="top3[1].avatar && top3[1].avatar.startsWith('http')" :src="top3[1].avatar" alt="avatar" class="w-full h-full object-cover" />
            <span v-else>{{ top3[1].avatar }}</span>
          </div>
          <div class="font-semibold text-sm text-center mb-1 max-w-[80px] truncate" :title="top3[1].name">{{ top3[1].name.split(' ')[0] }}</div>
          <div class="text-xs text-[var(--muted-foreground)] mb-2">{{ top3[1].xp.toLocaleString() }} XP</div>
          <div class="w-20 h-20 bg-gradient-to-t from-gray-200 to-gray-100 rounded-t-xl flex items-end justify-center pb-2">
            <span class="text-3xl">🥈</span>
          </div>
        </div>

        <!-- 1st -->
        <div v-if="top3.length > 0" class="flex flex-col items-center">
          <div class="text-2xl mb-1">👑</div>
          <div class="w-[72px] h-[72px] rounded-full bg-gradient-to-br from-amber-400 to-amber-500 flex items-center justify-center text-white font-bold text-xl mb-2 border-4 border-white shadow-xl overflow-hidden">
            <img v-if="top3[0].avatar && top3[0].avatar.startsWith('http')" :src="top3[0].avatar" alt="avatar" class="w-full h-full object-cover" />
            <span v-else>{{ top3[0].avatar }}</span>
          </div>
          <div class="font-bold text-center mb-1 max-w-[100px] truncate" :title="top3[0].name">{{ top3[0].name.split(' ')[0] }}</div>
          <div class="text-xs text-[var(--muted-foreground)] mb-2">{{ top3[0].xp.toLocaleString() }} XP</div>
          <div class="w-20 h-28 bg-gradient-to-t from-amber-200 to-amber-100 rounded-t-xl flex items-end justify-center pb-2">
            <span class="text-3xl">🥇</span>
          </div>
        </div>

        <!-- 3rd -->
        <div v-if="top3.length > 2" class="flex flex-col items-center">
          <div class="w-14 h-14 rounded-full bg-gradient-to-br from-amber-600 to-amber-700 flex items-center justify-center text-white font-bold text-lg mb-2 border-4 border-white shadow-lg overflow-hidden">
             <img v-if="top3[2].avatar && top3[2].avatar.startsWith('http')" :src="top3[2].avatar" alt="avatar" class="w-full h-full object-cover" />
            <span v-else>{{ top3[2].avatar }}</span>
          </div>
          <div class="font-semibold text-sm text-center mb-1 max-w-[80px] truncate" :title="top3[2].name">{{ top3[2].name.split(' ')[0] }}</div>
          <div class="text-xs text-[var(--muted-foreground)] mb-2">{{ top3[2].xp.toLocaleString() }} XP</div>
          <div class="w-20 h-14 bg-gradient-to-t from-amber-100 to-amber-50 rounded-t-xl flex items-end justify-center pb-2">
            <span class="text-3xl">🥉</span>
          </div>
        </div>
      </div>
      
      <div v-if="leaderboardData.length === 0" class="text-center text-[var(--muted-foreground)] py-10">
        Chưa có dữ liệu bảng xếp hạng cho thời gian này.
      </div>

      <!-- Full list -->
      <div v-else class="bg-white rounded-2xl border border-[var(--border)] overflow-hidden">
        <div class="grid grid-cols-12 px-5 py-3 bg-[var(--muted)] text-xs text-[var(--muted-foreground)] font-semibold uppercase tracking-wider">
          <div class="col-span-1">#</div>
          <div class="col-span-5">Học viên</div>
          <div class="col-span-2 text-right">XP</div>
          <div class="col-span-2 text-right">Cấp</div>
          <div class="col-span-2 text-right">Streak</div>
        </div>

        <div
          v-for="user in leaderboardData"
          :key="user.rank"
          :class="[
            'grid grid-cols-12 px-5 py-4 items-center border-b border-[var(--border)] last:border-0 hover:bg-[var(--muted)]/30 transition-colors',
            user.isMe ? 'bg-[var(--primary)]/5 border-l-4 border-l-[var(--primary)]' : ''
          ]"
        >
          <div class="col-span-1">
            <span :class="[
              'font-bold text-sm',
              user.rank === 1 ? 'text-amber-500' :
              user.rank === 2 ? 'text-gray-400' :
              user.rank === 3 ? 'text-amber-700' : 'text-[var(--muted-foreground)]'
            ]">
              {{ user.rank <= 3 ? user.badge : user.rank }}
            </span>
          </div>
          <div class="col-span-5 flex items-center gap-3">
            <div :class="[
              'w-9 h-9 rounded-full flex items-center justify-center text-white text-sm font-bold flex-shrink-0 overflow-hidden',
              user.isMe ? 'bg-[var(--accent)]' : 'bg-gradient-to-br from-[var(--primary)]/70 to-[var(--accent)]/70'
            ]">
              <img v-if="user.avatar && user.avatar.startsWith('http')" :src="user.avatar" alt="avatar" class="w-full h-full object-cover" />
              <span v-else>{{ user.avatar }}</span>
            </div>
            <div>
              <div :class="['font-semibold text-sm', user.isMe ? 'text-[var(--primary)]' : '']">
                {{ user.name }} {{ user.country }}
                <span v-if="user.isMe" class="ml-2 text-xs bg-[var(--primary)] text-white px-1.5 py-0.5 rounded-full">Bạn</span>
              </div>
            </div>
          </div>
          <div class="col-span-2 text-right font-bold text-sm">{{ user.xp.toLocaleString() }}</div>
          <div class="col-span-2 text-right">
            <span class="px-2 py-0.5 bg-[var(--muted)] text-[var(--muted-foreground)] text-xs rounded-full">Lv.{{ user.level }}</span>
          </div>
          <div class="col-span-2 text-right text-sm">
            <span class="text-orange-500">🔥</span> {{ user.streak }}
          </div>
        </div>
      </div>

      <!-- My rank card -->
      <div v-if="myRanking" class="mt-6 p-5 bg-gradient-to-r from-[var(--primary)]/10 to-[var(--accent)]/10 border border-[var(--primary)]/20 rounded-2xl flex items-center gap-4">
        <div class="w-12 h-12 rounded-full bg-[var(--accent)] flex items-center justify-center text-white font-bold overflow-hidden">
          <img v-if="myRanking.avatar && myRanking.avatar.startsWith('http')" :src="myRanking.avatar" alt="avatar" class="w-full h-full object-cover" />
          <span v-else>{{ myRanking.avatar }}</span>
        </div>
        <div class="flex-1">
          <div class="font-bold">{{ myRanking.name }} — Hạng #{{ myRanking.rank }} của bạn</div>
          <div class="text-sm text-[var(--muted-foreground)]">{{ myRanking.xp.toLocaleString() }} XP · Duy trì học tập để nâng hạng!</div>
        </div>
        <button
          @click="$router.push({ name: 'StudentDashboard' })"
          class="px-4 py-2 bg-[var(--primary)] text-white text-sm font-medium rounded-lg hover:bg-[var(--primary)]/90"
        >
          Học ngay
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import leaderboardService from '@/services/leaderboard.service';

const router = useRouter();

const tabs = [
  { label: 'Tuần này', value: 'WEEKLY' },
  { label: 'Tháng này', value: 'MONTHLY' },
  { label: 'Tất cả', value: 'ALL_TIME' }
];

const activeTab = ref(0);
const loading = ref(true);
const error = ref('');
const leaderboardData = ref([]);
const myRanking = ref(null);

const top3 = computed(() => leaderboardData.value.slice(0, 3));

const fetchLeaderboard = async () => {
  loading.value = true;
  error.value = '';
  try {
    const timeframe = tabs[activeTab.value].value;
    const response = await leaderboardService.getLeaderboard(timeframe, 0, 50);
    leaderboardData.value = response.data.rankings || [];
    myRanking.value = response.data.currentUserRanking || null;
  } catch (err) {
    console.error('Error fetching leaderboard:', err);
    error.value = 'Không thể tải bảng xếp hạng lúc này.';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchLeaderboard();
});

watch(activeTab, () => {
  fetchLeaderboard();
});
</script>
