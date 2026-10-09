<template>
  <div v-if="mode === 'studying'" class="min-h-screen bg-stitch-bg flex flex-col">
    <!-- Progress bar -->
    <div class="fixed top-0 left-0 right-0 z-10 bg-white border-b border-stitch-border">
      <div class="max-w-2xl mx-auto px-4 py-3 flex items-center gap-4">
        <button @click="stopStudying" class="text-stitch-muted-fg hover:text-stitch-fg text-sm">✕</button>
        <div class="flex-1 h-2 bg-stitch-muted rounded-full overflow-hidden">
          <div class="h-full bg-gradient-to-r from-primary to-accent rounded-full transition-all duration-300" :style="{ width: `${progress}%` }"></div>
        </div>
        <span class="text-sm text-stitch-muted-fg whitespace-nowrap">{{ cardIndex + 1 }} / {{ dueCards.length }}</span>
      </div>
    </div>

    <div class="flex-1 flex flex-col items-center justify-center px-4 pt-16 pb-8">
      <div v-if="loadingCards" class="text-center py-12 text-stitch-muted-fg">
        <span class="material-symbols-outlined animate-spin text-4xl mb-4">refresh</span>
        <p>Đang tải thẻ...</p>
      </div>
      
      <div v-else-if="dueCards.length === 0" class="text-center py-12">
        <span class="material-symbols-outlined text-6xl text-green-500 mb-4">check_circle</span>
        <h2 class="text-2xl font-display font-bold mb-2">Hoàn thành xuất sắc!</h2>
        <p class="text-stitch-muted-fg mb-6">Bạn đã học xong tất cả các thẻ hôm nay.</p>
        <button @click="stopStudying" class="px-6 py-2 bg-primary text-white rounded-xl">Quay lại</button>
      </div>

      <div v-else class="w-full max-w-md">
        <!-- Card -->
        <div 
          class="relative w-full cursor-pointer select-none"
          @click="flipCard"
          style="perspective: 1000px;"
        >
          <div 
            class="relative w-full transition-transform duration-500"
            :style="{ transformStyle: 'preserve-3d', transform: flipped ? 'rotateY(180deg)' : 'rotateY(0deg)' }"
          >
            <!-- Front -->
            <div 
              class="w-full bg-white rounded-3xl border-2 border-stitch-border p-10 text-center shadow-xl"
              style="backface-visibility: hidden;"
            >
              <div class="text-xs text-stitch-muted-fg uppercase tracking-widest mb-6">Từ vựng • {{ currentCard.level }}</div>
              <div class="text-8xl font-display mb-4">{{ currentCard.frontText }}</div>
              <div class="text-2xl text-stitch-muted-fg mb-8">{{ currentCard.frontReading }}</div>
              <div class="text-sm text-stitch-muted-fg flex items-center justify-center gap-2">
                <span>👆</span> Nhấn để xem nghĩa
              </div>
            </div>

            <!-- Back -->
            <div 
              class="absolute inset-0 w-full bg-gradient-to-br from-primary to-[#8b1038] rounded-3xl p-10 text-center shadow-xl text-white"
              style="backface-visibility: hidden; transform: rotateY(180deg);"
            >
              <div class="text-4xl mb-2">{{ currentCard.frontText }}</div>
              <div class="text-lg text-white/70 mb-4">{{ currentCard.frontReading }}</div>
              <div class="text-3xl font-display font-bold mb-6">{{ currentCard.backMeaning }}</div>
              <div v-if="currentCard.backExample" class="bg-white/10 rounded-xl p-4">
                <div class="text-lg mb-1">{{ currentCard.backExample }}</div>
                <div class="text-sm text-white/70">{{ currentCard.backExampleMeaning }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Answer buttons -->
        <div v-if="flipped && !answering" class="mt-8 grid grid-cols-3 gap-3">
          <button 
            @click="handleAnswer('HARD')"
            :disabled="submitting"
            class="py-4 rounded-2xl border-2 font-semibold text-sm transition-all bg-red-100 text-red-600 border-red-200 hover:bg-red-200 disabled:opacity-50"
          >
            Khó
          </button>
          <button 
            @click="handleAnswer('MEDIUM')"
            :disabled="submitting"
            class="py-4 rounded-2xl border-2 font-semibold text-sm transition-all bg-amber-100 text-amber-600 border-amber-200 hover:bg-amber-200 disabled:opacity-50"
          >
            Bình thường
          </button>
          <button 
            @click="handleAnswer('EASY')"
            :disabled="submitting"
            class="py-4 rounded-2xl border-2 font-semibold text-sm transition-all bg-green-100 text-green-600 border-green-200 hover:bg-green-200 disabled:opacity-50"
          >
            Dễ
          </button>
        </div>
        
        <div v-if="submitting" class="mt-8 text-center text-sm text-primary flex items-center justify-center gap-2">
           <span class="material-symbols-outlined animate-spin">refresh</span> Đang lưu kết quả...
        </div>

        <div v-if="!flipped && !answering" class="mt-8 text-center text-sm text-stitch-muted-fg">
          Nhấn vào thẻ hoặc phím Space để lật
        </div>
      </div>
    </div>
  </div>

  <div v-else class="min-h-screen bg-stitch-bg">
    <div class="bg-stitch-fg text-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <p class="text-sm text-accent uppercase tracking-widest mb-2">Luyện tập</p>
        <h1 class="text-4xl font-display font-bold mb-2">Flashcard thông minh</h1>
        <p class="text-white/60">Hệ thống SRS — ôn đúng lúc, nhớ lâu hơn.</p>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      
      <!-- Stats row -->
      <div v-if="completed > 0" class="mb-6 p-4 bg-green-50 border border-green-200 rounded-2xl flex items-center gap-4">
        <span class="text-2xl">✅</span>
        <div>
          <div class="font-semibold text-green-800">Phiên vừa xong: {{ completed }} thẻ, {{ known }} thẻ đã thuộc</div>
          <div class="text-sm text-green-600">+{{ completed * 20 }} XP đã được cộng</div>
        </div>
      </div>

      <h2 class="font-display font-bold text-2xl mb-6">Bộ flashcard của bạn</h2>
      
      <div v-if="loadingDecks" class="flex justify-center py-12">
        <span class="material-symbols-outlined animate-spin text-4xl text-primary">refresh</span>
      </div>
      
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-10">
        <div v-for="d in decks" :key="d.id" class="bg-white rounded-2xl border border-stitch-border p-5 hover:shadow-lg hover:shadow-black/5 transition-all">
          <div class="flex items-start justify-between mb-4">
            <div class="text-4xl">{{ d.icon || '📗' }}</div>
            <span class="px-2 py-0.5 bg-stitch-muted text-stitch-muted-fg text-xs rounded-full">{{ d.level }}</span>
          </div>
          <h3 class="font-semibold text-sm mb-1 leading-tight">{{ d.name }}</h3>
          <div class="text-xs text-stitch-muted-fg mb-4">{{ d.totalCards }} thẻ tổng</div>
          <div v-if="d.dueCards > 0" class="text-xs font-medium text-primary bg-primary/10 rounded-lg px-2 py-1 mb-3 text-center">
            {{ d.dueCards }} thẻ cần ôn hôm nay
          </div>
          <div v-else class="h-8 mb-3"></div>
          <button 
            @click="startStudying(d)"
            class="w-full py-2 rounded-lg text-sm font-medium transition-all"
            :class="d.dueCards > 0 ? 'bg-primary text-white hover:bg-primary/90' : 'bg-stitch-muted text-stitch-muted-fg hover:bg-stitch-muted-fg/20'"
          >
            {{ d.dueCards > 0 ? 'Ôn tập ngay' : 'Ôn thêm' }}
          </button>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import FlashcardService from '@/services/flashcard.service';

const mode = ref('home');
const decks = ref([]);
const loadingDecks = ref(false);
const errorMsg = ref('');

const dueCards = ref([]);
const cardIndex = ref(0);
const loadingCards = ref(false);
const flipped = ref(false);
const answering = ref(false);
const submitting = ref(false);

const completed = ref(0);
const known = ref(0);
const currentDeckId = ref(null);

const currentCard = computed(() => {
  if (dueCards.value.length === 0 || cardIndex.value >= dueCards.value.length) return null;
  return dueCards.value[cardIndex.value];
});

const progress = computed(() => {
  if (dueCards.value.length === 0) return 100;
  return (cardIndex.value / dueCards.value.length) * 100;
});

const fetchDecks = async () => {
  loadingDecks.value = true;
  errorMsg.value = '';
  try {
    const res = await FlashcardService.getUserDecks();
    decks.value = res.data;
  } catch (error) {
    errorMsg.value = 'Không thể tải danh sách bộ flashcard';
  } finally {
    loadingDecks.value = false;
  }
};

onMounted(() => {
  fetchDecks();
  window.addEventListener('keydown', handleSpacebar);
});

onUnmounted(() => {
  window.removeEventListener('keydown', handleSpacebar);
});

const handleSpacebar = (e) => {
  if (mode.value === 'studying' && currentCard.value && !flipped.value && !submitting.value && e.code === 'Space') {
    e.preventDefault();
    flipCard();
  }
};

const startStudying = async (deck) => {
  mode.value = 'studying';
  currentDeckId.value = deck.id;
  cardIndex.value = 0;
  flipped.value = false;
  answering.value = false;
  completed.value = 0;
  known.value = 0;
  
  loadingCards.value = true;
  try {
    const res = await FlashcardService.getDueCards(deck.id);
    dueCards.value = res.data;
  } catch (error) {
    errorMsg.value = 'Lỗi tải thẻ';
    mode.value = 'home';
  } finally {
    loadingCards.value = false;
  }
};

const stopStudying = () => {
  mode.value = 'home';
  fetchDecks(); // Refresh due counts
};

const flipCard = () => {
  if (!answering.value && !submitting.value) {
    flipped.value = !flipped.value;
  }
};

const generateIdempotencyKey = (cardId) => {
  return `review_${cardId}_${new Date().getTime()}_${Math.random().toString(36).substring(7)}`;
};

const handleAnswer = async (difficulty) => {
  if (!currentCard.value || submitting.value) return;
  
  answering.value = true;
  submitting.value = true;
  
  try {
    await FlashcardService.reviewCard({
      flashcardId: currentCard.value.id,
      difficulty: difficulty,
      idempotencyKey: generateIdempotencyKey(currentCard.value.id),
      timezone: Intl.DateTimeFormat().resolvedOptions().timeZone
    });
    
    if (difficulty !== 'HARD') known.value++;
    completed.value++;
    
    // Move to next card
    if (cardIndex.value < dueCards.value.length - 1) {
      cardIndex.value++;
      flipped.value = false;
      answering.value = false;
    } else {
      stopStudying();
    }
  } catch (error) {
    alert('Lỗi khi lưu kết quả ôn tập');
    answering.value = false;
  } finally {
    submitting.value = false;
  }
};
</script>
