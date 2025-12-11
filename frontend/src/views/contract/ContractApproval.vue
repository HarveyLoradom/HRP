<template>
  <div class="contract-approval">
    <el-card>
      <div slot="header">
        <span>合同审批</span>
      </div>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="待审批" name="PENDING"></el-tab-pane>
        <el-tab-pane label="已审批" name="APPROVED"></el-tab-pane>
        <el-tab-pane label="已拒绝" name="REJECTED"></el-tab-pane>
      </el-tabs>

      <el-table :data="tableData" border style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="contractNo" label="合同编号" width="150"></el-table-column>
        <el-table-column prop="contractName" label="合同名称" width="200"></el-table-column>
        <el-table-column prop="contractType" label="合同类型" width="120">
          <template slot-scope="scope">
            {{ getContractTypeName(scope.row.contractType) }}
          </template>
        </el-table-column>
        <el-table-column prop="partyA" label="甲方" width="150"></el-table-column>
        <el-table-column prop="partyB" label="乙方" width="150"></el-table-column>
        <el-table-column prop="contractAmount" label="合同金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.contractAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status === 'PENDING'" size="mini" type="success" @click="handleApprove(scope.row)">通过</el-button>
            <el-button v-if="scope.row.status === 'PENDING'" size="mini" type="danger" @click="handleReject(scope.row)">驳回</el-button>
            <el-button size="mini" @click="handleView(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 审批对话框 -->
    <el-dialog :title="approvalTitle" :visible.sync="approvalVisible" width="500px">
      <el-form :model="approvalForm" label-width="100px">
        <el-form-item label="审批意见">
          <el-input type="textarea" v-model="approvalForm.approvalOpinion" :rows="4" placeholder="请输入审批意见（可选）"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="approvalVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmApproval">确定</el-button>
      </div>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog title="合同详情" :visible.sync="detailVisible" width="700px">
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="合同编号">{{ currentDetail.contractNo }}</el-descriptions-item>
        <el-descriptions-item label="合同名称">{{ currentDetail.contractName }}</el-descriptions-item>
        <el-descriptions-item label="合同类型">{{ getContractTypeName(currentDetail.contractType) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentDetail.status)">{{ getStatusText(currentDetail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="甲方">{{ currentDetail.partyA }}</el-descriptions-item>
        <el-descriptions-item label="乙方">{{ currentDetail.partyB }}</el-descriptions-item>
        <el-descriptions-item label="合同金额">¥{{ currentDetail.contractAmount }}</el-descriptions-item>
        <el-descriptions-item label="签订日期">{{ formatDate(currentDetail.signDate) }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ formatDate(currentDetail.startDate) }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ formatDate(currentDetail.endDate) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentDetail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { getMyApprovalContracts, approveContract, rejectContract, getContractById } from '@/api/contract'
import { getCodeTypeOptions } from '@/utils/codeType'

export default {
  name: 'ContractApproval',
  data() {
    return {
      loading: false,
      activeTab: 'PENDING',
      tableData: [],
      contractTypeOptions: [],
      approvalVisible: false,
      approvalTitle: '',
      approvalForm: {
        contractId: null,
        approvalType: '', // 'approve' or 'reject'
        approvalOpinion: ''
      },
      detailVisible: false,
      currentDetail: null
    }
  },
  mounted() {
    this.loadCodeTypeOptions()
    this.loadData()
  },
  methods: {
    async loadCodeTypeOptions() {
      this.contractTypeOptions = await getCodeTypeOptions('CONTRACT_TYPE')
    },
    loadData() {
      this.loading = true
      const userId = this.$store.state.user.userInfo.userId || this.$store.state.user.userInfo.id
      getMyApprovalContracts(userId).then(response => {
        if (response.code === 200) {
          // 根据当前tab过滤数据
          if (this.activeTab === 'PENDING') {
            this.tableData = (response.data || []).filter(item => item.status === 'PENDING')
          } else if (this.activeTab === 'APPROVED') {
            this.tableData = (response.data || []).filter(item => item.status === 'APPROVED')
          } else if (this.activeTab === 'REJECTED') {
            this.tableData = (response.data || []).filter(item => item.status === 'REJECTED')
          }
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleTabClick() {
      this.loadData()
    },
    handleApprove(row) {
      this.approvalTitle = '审批通过'
      this.approvalForm = {
        contractId: row.pactId,
        approvalType: 'approve',
        approvalOpinion: ''
      }
      this.approvalVisible = true
    },
    handleReject(row) {
      this.approvalTitle = '驳回合同'
      this.approvalForm = {
        contractId: row.pactId,
        approvalType: 'reject',
        approvalOpinion: ''
      }
      this.approvalVisible = true
    },
    handleConfirmApproval() {
      const userId = this.$store.state.user.userInfo.userId || this.$store.state.user.userInfo.id
      const api = this.approvalForm.approvalType === 'approve' ? approveContract : rejectContract
      api(this.approvalForm.contractId, userId, this.approvalForm.approvalOpinion).then(response => {
        if (response.code === 200) {
          this.$message.success(this.approvalForm.approvalType === 'approve' ? '审批通过' : '已驳回')
          this.approvalVisible = false
          this.loadData()
        } else {
          this.$message.error(response.message || '操作失败')
        }
      })
    },
    handleView(row) {
      getContractById(row.pactId).then(response => {
        if (response.code === 200) {
          this.currentDetail = response.data
          this.detailVisible = true
        }
      })
    },
    getContractTypeName(codeValue) {
      const option = this.contractTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
    },
    getStatusText(status) {
      const statusMap = {
        'DRAFT': '草稿',
        'PENDING': '待审批',
        'APPROVED': '已审批',
        'EXECUTING': '执行中',
        'ARCHIVED': '已归档',
        'REJECTED': '已拒绝'
      }
      return statusMap[status] || status
    },
    getStatusType(status) {
      const typeMap = {
        'DRAFT': 'info',
        'PENDING': 'warning',
        'APPROVED': 'success',
        'EXECUTING': '',
        'ARCHIVED': 'info',
        'REJECTED': 'danger'
      }
      return typeMap[status] || ''
    }
  }
}
</script>

<style scoped>
.contract-approval {
  padding: 20px;
}
</style>

