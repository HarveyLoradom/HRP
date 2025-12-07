<template>
  <div class="business-platform">
    <el-card>
      <div slot="header">
        <span>业务平台</span>
      </div>
      <div class="placeholder-content">
        <i class="el-icon-s-operation placeholder-icon"></i>
        <p>业务平台功能开发中，敬请期待...</p>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'BusinessPlatform'
}
</script>

<style scoped>
.business-platform {
  padding: 20px;
}

.placeholder-content {
  text-align: center;
  padding: 100px 20px;
  color: #909399;
}

.placeholder-icon {
  font-size: 64px;
  margin-bottom: 20px;
  color: #C0C4CC;
}

.placeholder-content p {
  font-size: 16px;
  margin: 0;
}
</style>
