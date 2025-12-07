<template>
  <div class="reimb-approval">
    <el-card>
      <div slot="header">
        <span>报账审批</span>
      </div>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="待审批" name="PENDING"></el-tab-pane>
        <el-tab-pane label="已审批" name="APPROVED"></el-tab-pane>
        <el-tab-pane label="已拒绝" name="REJECTED"></el-tab-pane>
      </el-tabs>

      <el-table :data="tableData" border style="width: 100%; margin-top: 20px">
        <el-table-column prop="payoutBillcode" label="申请单号" width="150"></el-table-column>
        <el-table-column prop="empName" label="申请人" width="100"></el-table-column>
        <el-table-column prop="payoutTypeId" label="申请类型" width="120"></el-table-column>
        <el-table-column prop="applyAmount" label="申请金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.applyAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="applyDate" label="申请日期" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.applyDate) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status === 'PENDING'" size="mini" type="success" @click="handleApprove(scope.row)">通过</el-button>
            <el-button v-if="scope.row.status === 'PENDING'" size="mini" type="danger" @click="handleReject(scope.row)">拒绝</el-button>
            <el-button size="mini" @click="handleView(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 审批对话框 -->
    <el-dialog :title="approvalTitle" :visible.sync="approvalVisible" width="500px">
      <el-form :model="approvalForm" label-width="100px">
        <el-form-item label="审批意见">
          <el-input type="textarea" v-model="approvalForm.approvalOpinion" :rows="4"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="approvalVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmApproval">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getPayoutsByStatus } from '@/api/reimb'

export default {
  name: 'ReimbApproval',
  data() {
    return {
      activeTab: 'PENDING',
      tableData: [],
      approvalVisible: false,
      approvalTitle: '',
      approvalForm: {
        applyId: null,
        approverId: null,
        approvalOpinion: ''
      }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      getPayoutsByStatus(this.activeTab).then(response => {
        if (response.code === 200) {
          this.tableData = response.data
        }
      })
    },
    handleTabClick() {
      this.loadData()
    },
    handleApprove(row) {
      this.$message.info('审批功能待实现')
    },
    handleReject(row) {
      this.$message.info('审批功能待实现')
    },
    handleConfirmApproval() {
      this.$message.info('审批功能待实现')
    },
    handleView(row) {
      // 查看详情
      this.$message.info('查看详情功能待实现')
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.reimb-approval {
  padding: 20px;
}
</style>

