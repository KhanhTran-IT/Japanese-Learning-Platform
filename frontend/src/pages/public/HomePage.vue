<template>
  <div class="home">
    <!-- Hero Section -->
    <section class="relative min-h-[85vh] flex items-center overflow-hidden bg-surface">
      <div class="container mx-auto px-margin-mobile md:px-margin-desktop relative z-10 grid lg:grid-cols-2 gap-12 items-center">
        <div class="max-w-2xl">
          <div class="inline-flex items-center gap-2 px-4 py-1 rounded-full bg-primary-fixed text-on-primary-fixed-variant font-label-sm text-label-sm mb-6">
            <span class="material-symbols-outlined text-[16px]">celebration</span>
            Nền tảng học tiếng Nhật hàng đầu
          </div>
          <h1 class="font-headline-lg text-headline-lg-mobile md:text-headline-lg text-ink-black mb-6 leading-tight">
            Chinh phục tiếng Nhật<br>
            <span class="text-primary italic">theo cách của riêng bạn</span>
          </h1>
          <p class="font-body-lg text-body-lg text-secondary mb-10 max-w-lg">
            Trải nghiệm phương pháp học Zen độc đáo: Tập trung, tĩnh lặng và hiệu quả. Biến quá trình học ngôn ngữ thành một hành trình khám phá bản thân.
          </p>
          <div class="flex flex-wrap gap-4">
            <router-link to="/courses" class="bg-primary hover:bg-primary-container text-on-primary px-8 py-4 rounded-xl font-button text-button shadow-lg hover:shadow-xl transition-all hover:-translate-y-1">
              Khám phá khóa học
            </router-link>
            <router-link to="/login" class="bg-surface-container-lowest border border-paper-shadow hover:bg-surface-container-low px-8 py-4 rounded-xl font-button text-button transition-all">
              Đăng nhập
            </router-link>
          </div>
        </div>
        <div class="relative hidden lg:block">
          <div class="absolute -top-10 -right-10 w-64 h-64 bg-primary-fixed/20 rounded-full blur-3xl"></div>
          <div class="relative z-10 zen-card p-4 rounded-2xl overflow-hidden shadow-2xl rotate-2">
            <!-- Using a standard unsplash image since the placeholder might fail -->
            <img class="w-full aspect-[4/5] object-cover rounded-xl" alt="Japanese Study" src="https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?q=80&w=600&auto=format&fit=crop">
          </div>
          <div class="absolute -bottom-6 -left-6 z-20 zen-card p-6 rounded-2xl -rotate-3 max-w-[200px]">
            <div class="flex items-center gap-3 mb-2">
              <span class="material-symbols-outlined text-success-green" style="font-variation-settings: 'FILL' 1;">stars</span>
              <span class="font-label-sm text-label-sm text-secondary">Tập trung</span>
            </div>
            <div class="font-headline-md text-headline-md text-ink-black">Hiệu quả</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Lộ trình học Section -->
    <section class="py-24 bg-surface-container-lowest">
      <div class="container mx-auto px-margin-mobile md:px-margin-desktop">
        <div class="text-center mb-16">
          <h2 class="font-headline-lg text-headline-lg-mobile md:text-headline-lg text-ink-black mb-4">Lộ trình học tập cá nhân hóa</h2>
          <p class="font-body-lg text-body-lg text-secondary max-w-2xl mx-auto">Chúng tôi chia nhỏ hành trình từ người mới bắt đầu đến bậc thầy thành các giai đoạn rõ ràng.</p>
        </div>
        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
          <div class="zen-card p-8 rounded-2xl text-center group cursor-pointer hover:border-jlpt-n5/50 border-2 border-transparent">
            <div class="w-16 h-16 mx-auto mb-6 rounded-full bg-surface-container flex items-center justify-center text-jlpt-n5 group-hover:scale-110 transition-transform">
              <span class="material-symbols-outlined text-4xl">filter_1</span>
            </div>
            <h3 class="font-headline-md text-headline-md text-ink-black mb-2">JLPT N5</h3>
            <p class="font-label-sm text-label-sm text-secondary">Căn bản</p>
          </div>
          <div class="zen-card p-8 rounded-2xl text-center group cursor-pointer hover:border-secondary/50 border-2 border-transparent">
            <div class="w-16 h-16 mx-auto mb-6 rounded-full bg-surface-container flex items-center justify-center text-secondary group-hover:scale-110 transition-transform">
              <span class="material-symbols-outlined text-4xl">filter_2</span>
            </div>
            <h3 class="font-headline-md text-headline-md text-ink-black mb-2">JLPT N4</h3>
            <p class="font-label-sm text-label-sm text-secondary">Sơ cấp</p>
          </div>
          <div class="zen-card p-8 rounded-2xl text-center group cursor-pointer hover:border-tertiary/50 border-2 border-transparent">
            <div class="w-16 h-16 mx-auto mb-6 rounded-full bg-surface-container flex items-center justify-center text-tertiary group-hover:scale-110 transition-transform">
              <span class="material-symbols-outlined text-4xl">filter_3</span>
            </div>
            <h3 class="font-headline-md text-headline-md text-ink-black mb-2">JLPT N3</h3>
            <p class="font-label-sm text-label-sm text-secondary">Trung cấp</p>
          </div>
          <div class="zen-card p-8 rounded-2xl text-center group cursor-pointer hover:border-primary/50 border-2 border-transparent">
            <div class="w-16 h-16 mx-auto mb-6 rounded-full bg-surface-container flex items-center justify-center text-primary group-hover:scale-110 transition-transform">
              <span class="material-symbols-outlined text-4xl">filter_4</span>
            </div>
            <h3 class="font-headline-md text-headline-md text-ink-black mb-2">JLPT N2</h3>
            <p class="font-label-sm text-label-sm text-secondary">Cao cấp</p>
          </div>
          <div class="zen-card p-8 rounded-2xl text-center group cursor-pointer hover:border-jlpt-n1/50 border-2 border-transparent">
            <div class="w-16 h-16 mx-auto mb-6 rounded-full bg-surface-container flex items-center justify-center text-jlpt-n1 group-hover:scale-110 transition-transform">
              <span class="material-symbols-outlined text-4xl">filter_5</span>
            </div>
            <h3 class="font-headline-md text-headline-md text-ink-black mb-2">JLPT N1</h3>
            <p class="font-label-sm text-label-sm text-secondary">Bản ngữ</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
</script>
