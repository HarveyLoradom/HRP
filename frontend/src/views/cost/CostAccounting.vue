<template>
  <div class="cost-accounting">
    <el-card>
      <div slot="header">
        <span>成本核算</span>
        <el-button style="float: right" type="primary" @click="handleAdd">新增核算</el-button>
      </div>
      <el-table :data="tableData" border>
        <el-table-column prop="accountingNo" label="核算编号" width="150"></el-table-column>
        <el-table-column prop="accountingName" label="核算名称" width="200"></el-table-column>
        <el-table-column prop="accountingPeriod" label="核算期间" width="120"></el-table-column>
        <el-table-column prop="costAmount" label="成本金额" width="120">
          <template slot-scope="scope">¥{{ scope.row.costAmount }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getAllCostAccountings } from '@/api/cost'

export default {
  name: 'CostAccounting',
  data() {
    return { tableData: [] }
  },
  mounted() {
    getAllCostAccountings().then(response => {
      if (response.code === 200) {
        this.tableData = response.data
      }
    })
  },
  methods: {
    handleAdd() {
      this.$message.info('新增核算功能待实现')
    }
  }
}
</script>

<style scoped>
.cost-accounting { padding: 20px; }
</style>

