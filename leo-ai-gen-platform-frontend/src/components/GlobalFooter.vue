<template>
  <a-layout-footer class="global-footer">
    <div class="footer-content">
      编程导航原创项目 by
      <a href="https://www.codefather.cn" target="_blank" rel="noopener noreferrer">
        程序员鱼皮
      </a>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 页脚组件，无需额外逻辑
</script>

<style scoped lang="css">
.global-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 16px 24px;
  text-align: center;
  background: #f0f2f5;
  border-top: 1px solid #e8e8e8;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  color: #666;
  font-size: 14px;
}

.footer-content a {
  color: #1890ff;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-content a:hover {
  color: #40a9ff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .global-footer {
    padding: 12px 16px;
  }

  .footer-content {
    font-size: 12px;
  }
}
</style>
