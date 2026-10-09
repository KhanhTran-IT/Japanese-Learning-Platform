<template>
  <div class="min-h-screen flex flex-col bg-stitch-background text-stitch-foreground font-stitch-sans">
    <Navbar />

    <main class="flex-1 pt-16 flex flex-col">
      <router-view></router-view>
    </main>

    <Footer />
  </div>
</template>

<script setup>
import Navbar from '@/components/common/Navbar.vue'
import Footer from '@/components/common/Footer.vue'
</script>
