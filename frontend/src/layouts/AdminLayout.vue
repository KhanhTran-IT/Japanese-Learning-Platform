<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="logo" style="display: flex; align-items: center;">
        <img src="@/assets/logo.png" alt="BrianJP Logo" style="height: 32px; width: 32px; object-fit: contain; margin-right: 8px; border-radius: 4px;" />
        BrianJP Admin
      </div>
      <nav class="menu">
        <router-link to="/admin/dashboard">
          <span class="menu-icon">📊</span> Tổng quan
        </router-link>
        <router-link to="/admin/users">
          <span class="menu-icon">👥</span> Người dùng
        </router-link>
        <router-link to="/admin/courses">
          <span class="menu-icon">📚</span> Khóa học
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <button @click="handleLogout" class="btn-logout">🚪 Đăng xuất</button>
      </div>
    </aside>
    <main class="main-content">
      <header class="topbar">
        <span class="greeting">Xin chào, Quản trị viên <strong>{{ displayName }}</strong></span>
      </header>
      <div class="page-content">
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { AuthService } from '@/services/auth.service'

const authStore = useAuthStore()
const router = useRouter()

const displayName = computed(() => {
  return authStore.user?.fullName || 'Admin'
})

const handleLogout = async () => {
  try {
    await AuthService.logout()
  } catch (error) {
    console.error('Logout error:', error)
  } finally {
    authStore.clearAuth()
    router.push('/login')
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
}
.sidebar {
  width: 250px;
  background-color: #0f172a; /* Đậm hơn Student Layout (1e293b) để dễ phân biệt */
  color: white;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.logo {
  padding: 1.5rem;
  font-size: 1.25rem;
  font-weight: bold;
  border-bottom: 1px solid #1e293b;
  color: #38bdf8; /* Màu xanh nhạt tạo điểm nhấn cho Admin */
}
.menu {
  display: flex;
  flex-direction: column;
  padding: 1rem 0;
  flex: 1;
}
.menu a {
  padding: 0.75rem 1.5rem;
  color: #cbd5e1;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: background-color 0.2s, color 0.2s;
  font-size: 0.9rem;
}
.menu a:hover, .menu a.router-link-active {
  background-color: #1e293b;
  color: white;
  border-left: 3px solid #38bdf8;
}
.menu-icon {
  font-size: 1rem;
}
.sidebar-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid #1e293b;
}
.btn-logout {
  width: 100%;
  padding: 0.625rem 1rem;
  background: transparent;
  border: 1px solid #475569;
  color: #cbd5e1;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: background-color 0.2s, color 0.2s;
}
.btn-logout:hover {
  background-color: #ef4444;
  border-color: #ef4444;
  color: white;
}
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f8fafc; /* Nền sáng nhẹ */
  min-width: 0;
}
.topbar {
  height: 60px;
  background-color: var(--card-bg, #ffffff);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 2rem;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  flex-shrink: 0;
}
.greeting {
  font-size: 0.9rem;
  color: #64748b;
}
.greeting strong {
  color: #0f172a;
}
.page-content {
  padding: 2rem;
  flex: 1;
  overflow-y: auto;
}
</style>
