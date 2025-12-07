<template>
  <div class="cost-data">
    <el-card>
      <div slot="header"><span>成本数据</span></div>
      <el-form :inline="true" class="search-form">
        <el-form-item label="开始日期">
          <el-date-picker v-model="startDate" type="date" format="yyyy-MM-dd" value-format="yyyy-MM-dd"></el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="endDate" type="date" format="yyyy-MM-dd" value-format="yyyy-MM-dd"></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border>
        <el-table-column prop="costNo" label="成本单号" width="150"></el-table-column>
        <el-table-column prop="costDate" label="成本日期" width="120"></el-table-column>
        <el-table-column prop="costAmount" label="成本金额" width="120">
          <template slot-scope="scope">¥{{ scope.row.costAmount }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getAllCostData, getCostDataByDateRange } from '@/api/efficiency'

export default {
  name: 'CostData',
  data() {
    return {
      tableData: [],
      startDate: '',
      endDate: ''
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      getAllCostData().then(response => {
        if (response.code === 200) {
          this.tableData = response.data
        }
      })
    },
    handleSearch() {
      if (this.startDate && this.endDate) {
        getCostDataByDateRange({ startDate: this.startDate, endDate: this.endDate }).then(response => {
          if (response.code === 200) {
            this.tableData = response.data
          }
        })
      } else {
        this.loadData()
      }
    }
  }
}
</script>

<style scoped>
.cost-data { padding: 20px; }
.search-form { margin-bottom: 20px; }
</style>

