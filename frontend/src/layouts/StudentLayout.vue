<template>
  <div class="min-h-screen flex flex-col bg-stitch-background text-stitch-foreground font-stitch-sans">
    <Navbar />
    <main class="flex-1 pt-16 flex flex-col">
      <div class="flex-1 overflow-y-auto p-4 md:p-8">
        <div class="max-w-7xl mx-auto h-full">
          <router-view></router-view>
        </div>
      </div>
    </main>
    <Footer />
  </div>
</template>

<script setup>
import Navbar from '@/components/common/Navbar.vue'
import Footer from '@/components/common/Footer.vue'
</script>
