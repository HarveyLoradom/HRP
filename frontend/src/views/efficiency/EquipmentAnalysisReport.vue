<template>
  <div class="equipment-analysis-report">
    <el-card>
      <div slot="header">
        <span>设备分析报告</span>
        <el-button style="float: right" type="primary" @click="handleAdd">生成报告</el-button>
      </div>
      <el-table :data="tableData" border>
        <el-table-column prop="reportNo" label="报告编号" width="150"></el-table-column>
        <el-table-column prop="reportName" label="报告名称" width="200"></el-table-column>
        <el-table-column prop="equipmentCode" label="设备编码" width="150"></el-table-column>
        <el-table-column prop="reportPeriod" label="报告期间" width="120"></el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleView(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'EquipmentAnalysisReport',
  data() {
    return { tableData: [] }
  },
  methods: {
    handleAdd() {
      this.$message.info('生成报告功能待实现')
    },
    handleView(row) {
      this.$message.info('查看报告功能待实现')
    }
  }
}
</script>

<style scoped>
.equipment-analysis-report { padding: 20px; }
</style>

