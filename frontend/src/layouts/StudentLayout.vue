<template>
  <div class="student-layout">
    <aside class="sidebar">
      <div class="logo">BrianJP Student</div>
      <nav class="menu">
        <router-link to="/student/dashboard">Bảng điều khiển</router-link>
        <router-link to="/student/courses">Khóa học của tôi</router-link>
      </nav>
    </aside>
    <main class="main-content">
      <header class="topbar">
        <span>Xin chào, Học viên</span>
      </header>
      <div class="page-content">
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
</script>

<style scoped>
.student-layout {
  display: flex;
  height: 100vh;
}
.sidebar {
  width: 250px;
  background-color: #1e293b;
  color: white;
  display: flex;
  flex-direction: column;
}
.logo {
  padding: 1.5rem;
  font-size: 1.25rem;
  font-weight: bold;
  border-bottom: 1px solid #334155;
}
.menu {
  display: flex;
  flex-direction: column;
  padding: 1rem 0;
}
.menu a {
  padding: 0.75rem 1.5rem;
  color: #cbd5e1;
}
.menu a:hover, .menu a.router-link-active {
  background-color: #334155;
  color: white;
}
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-color);
}
.topbar {
  height: 60px;
  background-color: var(--card-bg);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 2rem;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.page-content {
  padding: 2rem;
  flex: 1;
  overflow-y: auto;
}
</style>
