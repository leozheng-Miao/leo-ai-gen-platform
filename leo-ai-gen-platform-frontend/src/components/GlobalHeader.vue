<template>
  <a-layout-header class="global-header">
    <div class="header-content">
      <!-- 左侧：Logo 和标题 -->
      <div class="header-left">
        <img alt="Logo" class="logo" src="@/assets/logo.png" />
        <span class="site-title">Leo AI Gen Platform</span>
      </div>

      <!-- 中间：菜单 -->
      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="horizontal"
        :items="menuItems"
        class="header-menu"
        @click="handleMenuClick"
      />

      <!-- 右侧：用户信息 -->
      <div class="header-right">
        <a-button type="primary" @click="handleLogin">登录</a-button>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()

// 菜单配置
const menuItems: MenuProps['items'] = [
  {
    key: '/',
    label: '首页',
  },
  {
    key: '/about',
    label: '关于',
  },
]

// 当前选中的菜单项
const selectedKeys = ref<string[]>([route.path])

// 监听路由变化，更新选中的菜单项
watch(
  () => route.path,
  (newPath) => {
    selectedKeys.value = [newPath]
  },
)

// 菜单点击处理
const handleMenuClick: MenuProps['onClick'] = (e) => {
  router.push(e.key as string)
}

// 登录按钮处理
const handleLogin = () => {
  // TODO: 实现登录逻辑
  console.log('登录')
}
</script>

<style scoped lang="css">
.global-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 0;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.logo {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.site-title {
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
  white-space: nowrap;
}

.header-menu {
  flex: 1;
  min-width: 0;
  border-bottom: none;
  line-height: 64px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
  }

  .site-title {
    font-size: 16px;
  }

  .header-menu {
    display: none;
  }
}

@media (max-width: 480px) {
  .site-title {
    display: none;
  }
}
</style>
