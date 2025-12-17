<template>
  <div class="budget-approval">
    <el-card>
      <div slot="header" class="clearfix">
        <span>预算审批</span>
      </div>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="applyNo" label="申请单号" width="150"></el-table-column>
        <el-table-column prop="itemName" label="预算项目" width="200"></el-table-column>
        <el-table-column prop="subjectName" label="预算主体" width="150"></el-table-column>
        <el-table-column prop="budgetYear" label="年度" width="100"></el-table-column>
        <el-table-column prop="applyAmount" label="申请金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.applyAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="120"></el-table-column>
        <el-table-column prop="deptName" label="部门" width="150"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleView(scope.row)">查看</el-button>
            <el-button 
              v-if="scope.row.status === 'PENDING'" 
              size="mini" 
              type="success" 
              @click="handleApprove(scope.row)"
            >审批</el-button>
            <el-button 
              v-if="scope.row.status === 'PENDING'" 
              size="mini" 
              type="danger" 
              @click="handleReject(scope.row)"
            >拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" style="margin-top: 20px; text-align: right;">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total">
        </el-pagination>
      </div>
    </el-card>

    <!-- 审批对话框 -->
    <el-dialog title="预算审批" :visible.sync="approvalDialogVisible" width="600px">
      <el-form :model="approvalForm" label-width="120px">
        <el-form-item label="申请单号">
          <el-input :value="currentApply.applyNo" disabled></el-input>
        </el-form-item>
        <el-form-item label="预算项目">
          <el-input :value="currentApply.itemName" disabled></el-input>
        </el-form-item>
        <el-form-item label="申请金额">
          <el-input :value="'¥' + currentApply.applyAmount" disabled></el-input>
        </el-form-item>
        <el-form-item label="审批意见" prop="approvalOpinion">
          <el-input type="textarea" v-model="approvalForm.approvalOpinion" :rows="3" placeholder="请输入审批意见"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button type="success" @click="handleConfirmApprove">同意</el-button>
        <el-button type="danger" @click="handleConfirmReject">拒绝</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getBudgetApplies, approveBudgetApply, rejectBudgetApply } from '@/api/budg'
import { paginationMixin } from '@/mixins/pagination'

export default {
  name: 'BudgetApproval',
  mixins: [paginationMixin],
  data() {
    return {
      loading: false,
      tableData: [],
      approvalDialogVisible: false,
      currentApply: {},
      approvalForm: {
        approvalOpinion: ''
      }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      getBudgetApplies(this.pagination.page, this.pagination.size).then(response => {
        if (response.code === 200 && response.data) {
          // 显示所有申请，不只是审批人的
          this.tableData = response.data.records || []
          this.pagination.total = response.data.total || 0
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
      this.loadData()
    },
    handleCurrentChange(val) {
      this.pagination.page = val
      this.loadData()
    },
    handleView(row) {
      // TODO: 查看详情
      this.$message.info('查看功能待实现')
    },
    handleApprove(row) {
      this.currentApply = row
      this.approvalForm.approvalOpinion = ''
      this.approvalDialogVisible = true
    },
    handleReject(row) {
      this.currentApply = row
      this.approvalForm.approvalOpinion = ''
      this.approvalDialogVisible = true
    },
    handleConfirmApprove() {
      approveBudgetApply(this.currentApply.applyId, this.approvalForm.approvalOpinion).then(response => {
        if (response.code === 200) {
          this.$message.success('审批通过')
          this.approvalDialogVisible = false
          this.loadData()
        } else {
          this.$message.error(response.message || '审批失败')
        }
      })
    },
    handleConfirmReject() {
      rejectBudgetApply(this.currentApply.applyId, this.approvalForm.approvalOpinion).then(response => {
        if (response.code === 200) {
          this.$message.success('已拒绝')
          this.approvalDialogVisible = false
          this.loadData()
        } else {
          this.$message.error(response.message || '操作失败')
        }
      })
    },
    getStatusType(status) {
      const map = {
        'DRAFT': 'info',
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger'
      }
      return map[status] || 'info'
    },
    getStatusText(status) {
      const map = {
        'DRAFT': '草稿',
        'PENDING': '待审批',
        'APPROVED': '已审批',
        'REJECTED': '已拒绝'
      }
      return map[status] || status
    },
    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.budget-approval {
  padding: 20px;
}
</style>

