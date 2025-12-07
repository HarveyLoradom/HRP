<template>
  <div class="investment-return-analysis">
    <el-card>
      <div slot="header">
        <span>投资收益分析</span>
        <el-button style="float: right" type="primary" @click="handleAdd">新增分析</el-button>
      </div>
      <el-table :data="tableData" border>
        <el-table-column prop="analysisNo" label="分析编号" width="150"></el-table-column>
        <el-table-column prop="analysisName" label="分析名称" width="200"></el-table-column>
        <el-table-column prop="investmentAmount" label="投资金额" width="120">
          <template slot-scope="scope">¥{{ scope.row.investmentAmount }}</template>
        </el-table-column>
        <el-table-column prop="returnAmount" label="回报金额" width="120">
          <template slot-scope="scope">¥{{ scope.row.returnAmount }}</template>
        </el-table-column>
        <el-table-column prop="returnRate" label="回报率" width="100">
          <template slot-scope="scope">{{ scope.row.returnRate }}%</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'InvestmentReturnAnalysis',
  data() {
    return { tableData: [] }
  },
  methods: {
    handleAdd() {
      this.$message.info('新增分析功能待实现')
    }
  }
}
</script>

<style scoped>
.investment-return-analysis { padding: 20px; }
</style>

