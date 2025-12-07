<template>
  <div class="data-collection">
    <el-card>
      <div slot="header"><span>数据采集</span></div>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="部门数据" name="dept"></el-tab-pane>
        <el-tab-pane label="设备数据" name="equipment"></el-tab-pane>
      </el-tabs>
      <el-button type="primary" style="margin: 20px 0" @click="handleCollect">采集数据</el-button>
      <el-table :data="tableData" border>
        <el-table-column prop="collectionDate" label="采集日期" width="120"></el-table-column>
        <el-table-column prop="collectionType" label="采集类型" width="120"></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'DataCollection',
  data() {
    return {
      activeTab: 'dept',
      tableData: []
    }
  },
  methods: {
    handleCollect() {
      this.$message.info('数据采集功能待实现')
    }
  }
}
</script>

<style scoped>
.data-collection { padding: 20px; }
</style>

