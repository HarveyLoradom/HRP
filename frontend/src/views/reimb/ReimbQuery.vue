<template>
  <div class="reimb-query">
    <el-card>
      <div slot="header">
        <span>报账查询</span>
      </div>
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="payoutBillcode" label="申请单号" width="150"></el-table-column>
        <el-table-column prop="empName" label="申请人" width="100"></el-table-column>
        <el-table-column prop="applyAmount" label="申请金额" width="120">
          <template slot-scope="scope">¥{{ scope.row.applyAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getPayoutsByStatus } from '@/api/reimb'

export default {
  name: 'ReimbQuery',
  data() {
    return {
      tableData: []
    }
  },
  mounted() {
    getPayoutsByStatus('APPROVED').then(response => {
      if (response.code === 200) {
        this.tableData = response.data
      }
    })
  },
  methods: {
    getStatusText(status) {
      const map = { 'APPROVED': '已审批', 'REJECTED': '已拒绝' }
      return map[status] || status
    },
    getStatusType(status) {
      return status === 'APPROVED' ? 'success' : 'danger'
    }
  }
}
</script>

<style scoped>
.reimb-query { padding: 20px; }
</style>

