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
</script>

<style scoped>
.global-footer {
  text-align: center;
  background: #f0f2f5;
  padding: 16px 24px;
  border-top: 1px solid #e8e8e8;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  color: rgba(0, 0, 0, 0.65);
  font-size: 14px;
}

.footer-content a {
  color: #1890ff;
  text-decoration: none;
  margin-left: 4px;
}

.footer-content a:hover {
  color: #40a9ff;
  text-decoration: underline;
}

@media (max-width: 768px) {
  .global-footer {
    padding: 12px 16px;
    font-size: 12px;
  }
}
</style>

