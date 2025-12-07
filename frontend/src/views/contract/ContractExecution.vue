<template>
  <div class="contract-execution">
    <el-card>
      <div slot="header"><span>合同执行</span></div>
      <el-table :data="tableData" border>
        <el-table-column prop="contractNo" label="合同编号" width="150"></el-table-column>
        <el-table-column prop="contractName" label="合同名称" width="200"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag>{{ scope.row.status === 'EXECUTING' ? '执行中' : '已归档' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleChange(scope.row)">变更</el-button>
            <el-button size="mini" @click="handleArchive(scope.row)">归档</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getContractsByStatus } from '@/api/contract'

export default {
  name: 'ContractExecution',
  data() {
    return { tableData: [] }
  },
  mounted() {
    getContractsByStatus('EXECUTING').then(response => {
      if (response.code === 200) {
        this.tableData = response.data
      }
    })
  },
  methods: {
    handleChange(row) {
      this.$message.info('合同变更功能待实现')
    },
    handleArchive(row) {
      this.$message.info('归档功能待实现')
    },
    loadData() {
      getContractsByStatus('EXECUTING').then(response => {
        if (response.code === 200) {
          this.tableData = response.data
        }
      })
    }
  }
}
</script>

<style scoped>
.contract-execution { padding: 20px; }
</style>

