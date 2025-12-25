<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏 -->
    <GlobalHeader />

    <!-- 内容区域 -->
    <a-layout-content class="layout-content">
      <div class="content-wrapper">
        <RouterView />
      </div>
    </a-layout-content>

    <!-- 底部页脚 -->
    <GlobalFooter />
  </a-layout>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped lang="css">
.basic-layout {
  min-height: 100vh;
}

.layout-content {
  margin-top: 64px; /* 为固定头部留出空间 */
  margin-bottom: 64px; /* 为固定底部留出空间 */
  padding: 24px;
  background: #fff;
  min-height: calc(100vh - 128px);
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .layout-content {
    padding: 16px;
    margin-bottom: 56px;
  }
}
</style>
