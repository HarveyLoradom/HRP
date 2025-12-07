<template>
  <div class="contract-approval">
    <el-card>
      <div slot="header"><span>合同审批</span></div>
      <el-table :data="tableData" border>
        <el-table-column prop="contractNo" label="合同编号" width="150"></el-table-column>
        <el-table-column prop="contractName" label="合同名称" width="200"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'PENDING' ? 'warning' : 'success'">
              {{ scope.row.status === 'PENDING' ? '待审批' : '已审批' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status === 'PENDING'" size="mini" type="primary" @click="handleApprove(scope.row)">审批</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getContractsByStatus } from '@/api/contract'

export default {
  name: 'ContractApproval',
  data() {
    return { tableData: [] }
  },
  mounted() {
    getContractsByStatus('PENDING').then(response => {
      if (response.code === 200) {
        this.tableData = response.data
      }
    })
  },
  methods: {
    handleApprove(row) {
      this.$message.info('审批功能待实现')
    },
    loadData() {
      getContractsByStatus('PENDING').then(response => {
        if (response.code === 200) {
          this.tableData = response.data
        }
      })
    }
  }
}
</script>

<style scoped>
.contract-approval { padding: 20px; }
</style>

