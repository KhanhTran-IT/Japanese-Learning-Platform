<template>
  <div class="auth-layout">
    <div class="auth-container">
      <h1 class="logo">BrianJP</h1>
      <router-view></router-view>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.auth-layout {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: var(--bg-color);
}
.auth-container {
  background: var(--card-bg);
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  width: 100%;
  max-width: 400px;
  text-align: center;
}
.logo {
  color: var(--primary-color);
  margin-bottom: 1.5rem;
}
</style>
