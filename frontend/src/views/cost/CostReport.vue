<template>
  <div class="cost-report">
    <el-card>
      <div slot="header">
        <span>成本报表</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增报表</el-button>
      </div>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="reportNo" label="报表编号" width="150"></el-table-column>
        <el-table-column prop="reportName" label="报表名称" width="200"></el-table-column>
        <el-table-column prop="reportType" label="报表类型" width="120"></el-table-column>
        <el-table-column prop="reportPeriod" label="报表期间" width="120"></el-table-column>
        <el-table-column prop="totalCost" label="总成本" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.totalCost }}
          </template>
        </el-table-column>
        <el-table-column prop="directCost" label="直接成本" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.directCost }}
          </template>
        </el-table-column>
        <el-table-column prop="indirectCost" label="间接成本" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.indirectCost }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleView(scope.row)">查看</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getAllCostReports, deleteCostReport } from '@/api/cost'

export default {
  name: 'CostReport',
  data() {
    return {
      tableData: []
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      getAllCostReports().then(response => {
        if (response.code === 200) {
          this.tableData = response.data
        }
      })
    },
    handleAdd() {
      this.$message.info('新增报表功能待实现')
    },
    handleView(row) {
      this.$message.info('查看报表功能待实现')
    },
    handleDelete(row) {
      this.$confirm('确认删除该报表吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteCostReport(row.reportId).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          }
        })
      })
    }
  }
}
</script>

<style scoped>
.cost-report {
  padding: 20px;
}
</style>

