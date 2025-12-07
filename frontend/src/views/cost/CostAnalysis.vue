<template>
  <div class="cost-analysis">
    <el-card>
      <div slot="header">
        <span>成本分析</span>
        <el-button style="float: right" type="primary" @click="handleAdd">新增分析</el-button>
      </div>
      <el-table :data="tableData" border>
        <el-table-column prop="analysisNo" label="分析编号" width="150"></el-table-column>
        <el-table-column prop="analysisName" label="分析名称" width="200"></el-table-column>
        <el-table-column prop="analysisPeriod" label="分析期间" width="120"></el-table-column>
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
import { getAllCostAnalyses } from '@/api/cost'

export default {
  name: 'CostAnalysis',
  data() {
    return { tableData: [] }
  },
  mounted() {
    getAllCostAnalyses().then(response => {
      if (response.code === 200) {
        this.tableData = response.data
      }
    })
  },
  methods: {
    handleAdd() {
      this.$message.info('新增分析功能待实现')
    },
    handleView(row) {
      this.$message.info('查看分析功能待实现')
    }
  }
}
</script>

<style scoped>
.cost-analysis { padding: 20px; }
</style>

