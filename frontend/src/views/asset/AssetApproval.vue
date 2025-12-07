<template>
  <div class="asset-approval">
    <el-card>
      <div slot="header"><span>业务审批</span></div>
      <el-table :data="tableData" border>
        <el-table-column prop="requirementNo" label="需求单号" width="150"></el-table-column>
        <el-table-column prop="requirementName" label="需求名称" width="200"></el-table-column>
        <el-table-column prop="estimatedAmount" label="预估金额" width="120">
          <template slot-scope="scope">¥{{ scope.row.estimatedAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'PENDING' ? 'warning' : 'success'">
              {{ scope.row.status === 'PENDING' ? '待审批' : '已审批' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status === 'PENDING'" size="mini" type="primary">审批</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getAllProcurements } from '@/api/asset'

export default {
  name: 'AssetApproval',
  data() {
    return { tableData: [] }
  },
  mounted() {
    getAllProcurements().then(response => {
      if (response.code === 200) {
        this.tableData = response.data
      }
    })
  }
}
</script>

<style scoped>
.asset-approval { padding: 20px; }
</style>

