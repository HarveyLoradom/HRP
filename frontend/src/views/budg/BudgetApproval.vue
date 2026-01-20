<template>
  <div class="budget-approval">
    <el-card>
      <div slot="header" class="clearfix">
        <span>预算审批</span>
      </div>
      
      <!-- 查询表单 -->
      <el-form :model="searchForm" style="margin-bottom: 20px;">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="申请单号:" label-width="100px">
              <el-input v-model="searchForm.applyNo" placeholder="请输入申请单号" clearable style="width: 100%;"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="预算项目:" label-width="100px">
              <el-select v-model="searchForm.itemId" placeholder="请选择预算项目" clearable filterable style="width: 100%;">
                <el-option
                  v-for="item in budgetItems"
                  :key="item.itemId"
                  :label="item.itemName"
                  :value="item.itemId"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="申请人:" label-width="100px">
              <el-input v-model="searchForm.applicantName" placeholder="请输入申请人" clearable style="width: 100%;"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="申请时间:" label-width="100px">
              <el-date-picker
                v-model="searchForm.applyDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="yyyy-MM-dd"
                value-format="yyyy-MM-dd"
                style="width: 100%;"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态:" label-width="100px">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100%;">
                <el-option label="待审批" value="PENDING"></el-option>
                <el-option label="已审批" value="APPROVED"></el-option>
                <el-option label="已拒绝" value="REJECTED"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24" style="text-align: right;">
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
      
      <!-- 批量操作按钮 -->
      <div style="margin-bottom: 20px; text-align: right;">
        <el-button 
          type="success" 
          :disabled="selectedRows.length === 0"
          @click="handleBatchApprove"
        >同意（{{ selectedRows.length }}）</el-button>
        <el-button 
          type="danger" 
          :disabled="selectedRows.length === 0"
          @click="handleBatchReject"
          style="margin-left: 10px;"
        >拒绝（{{ selectedRows.length }}）</el-button>
      </div>
      
      <el-table 
        :data="tableData" 
        border 
        style="width: 100%" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column label="申请单号" width="160">
          <template slot-scope="scope">
            <el-button type="text" @click="handleViewDetail(scope.row)">{{ scope.row.applyNo }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="itemName" label="预算项目" width="220"></el-table-column>
        <el-table-column prop="subjectName" label="预算主体" width="160"></el-table-column>
        <el-table-column prop="budgetYear" label="年度" width="100"></el-table-column>
        <el-table-column prop="applyAmount" label="申请金额" width="130">
          <template slot-scope="scope">
            ¥{{ scope.row.applyAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="130"></el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyDate" label="申请时间" width="160">
          <template slot-scope="scope">
            {{ formatDateOnly(scope.row.applyDate || scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total">
        </el-pagination>
        <el-dropdown @command="handleExportCommand" :disabled="exportLoading">
          <el-button size="big" type="text" :loading="exportLoading">
            导出<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="current">导出本页</el-dropdown-item>
            <el-dropdown-item command="all">导出全部</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </el-card>

    <!-- 统一详情组件 -->
    <BudgetApplyDetail
      v-model="detailVisible"
      source-type="approval"
      :apply-id="selectedApplyId"
      @approved="handleDetailApproved"
      @returned="handleDetailReturned"
      @add-signed="handleDetailAddSign"
      @transferred="handleDetailTransfer"
    />

    <!-- 退回对话框 -->
    <RejectReturnDialog
      v-model="returnDialogVisible"
      @confirm="handleConfirmReturn"
      @close="handleCloseReturnDialog"
    />

    <!-- 审批确认对话框 -->
    <ApprovalConfirmDialog
      v-model="approvalDialogVisible"
      :next-node-info="nextNodeInfo"
      confirm-button-text="确认同意"
      @confirm="handleConfirmApprove"
      @close="handleCloseApprovalDialog"
    />


  </div>
</template>

<script>
import { getBudgetApplies, approveBudgetApply, rejectBudgetApply, returnBudgetApply } from '@/api/budg'
import { getBudgetItems } from '@/api/budg'
import { getNextNodeInfoByBusinessKey } from '@/api/process'
import { paginationMixin } from '@/mixins/pagination'
import { exportExcel } from '@/api/common'
import ApprovalConfirmDialog from '@/components/ApprovalConfirmDialog.vue'
import RejectReturnDialog from '@/components/RejectReturnDialog.vue'
import BudgetApplyDetail from '@/views/budg/BudgetApplyDetail.vue'

export default {
  name: 'BudgetApproval',
  mixins: [paginationMixin],
  components: {
    ApprovalConfirmDialog,
    RejectReturnDialog,
    BudgetApplyDetail
  },
  data() {
    return {
      loading: false,
      exportLoading: false,
      tableData: [],
      selectedRows: [],
      searchForm: {
        applyNo: '',
        itemId: null,
        applicantName: '',
        applyDateRange: null,
        status: 'PENDING' // 默认显示待审批
      },
      budgetItems: [],
      detailVisible: false,
      selectedApplyId: null,
      approvalDialogVisible: false, // 审批确认对话框
      nextNodeInfo: null, // 下一节点信息
      batchApproveRows: [], // 批量审批的行数据
      batchRejectRows: [], // 批量拒绝的行数据
      returnDialogVisible: false
    }
  },
  mounted() {
    this.loadBudgetItems()
    this.loadData()
  },
  methods: {
    async loadBudgetItems() {
      try {
        const response = await getBudgetItems()
        if (response.code === 200 && response.data) {
          this.budgetItems = response.data
        }
      } catch (error) {
        console.error('加载预算项目失败:', error)
      }
    },
    loadData() {
      this.loading = true
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        isApprovalList: true, // 标记为审批列表，后端会过滤只显示当前用户的待审批任务
        ...this.buildSearchParams()
      }
      
      getBudgetApplies(params).then(response => {
        if (response.code === 200 && response.data) {
          this.tableData = response.data.records || []
          this.pagination.total = response.data.total || 0
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    buildSearchParams() {
      const params = {}
      if (this.searchForm.applyNo) {
        params.applyNo = this.searchForm.applyNo
      }
      if (this.searchForm.itemId) {
        params.itemId = this.searchForm.itemId
      }
      if (this.searchForm.applicantName) {
        params.applicantName = this.searchForm.applicantName
      }
      if (this.searchForm.applyDateRange && this.searchForm.applyDateRange.length === 2) {
        params.startDate = this.searchForm.applyDateRange[0]
        params.endDate = this.searchForm.applyDateRange[1]
      }
      if (this.searchForm.status) {
        params.status = this.searchForm.status
      }
      return params
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = {
        applyNo: '',
        itemId: null,
        applicantName: '',
        applyDateRange: null,
        status: 'PENDING' // 重置时默认显示待审批
      }
      this.handleSearch()
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
    handleSelectionChange(selection) {
      this.selectedRows = selection
    },
    async handleViewDetail(row) {
      this.selectedApplyId = row.applyId
      this.detailVisible = true
    },
    handleDetailApproved() {
      // 确保详情对话框已关闭后再刷新数据
      this.$nextTick(() => {
        this.loadData()
      })
    },
    handleDetailReturned() {
      // 确保详情对话框已关闭后再刷新数据
      this.$nextTick(() => {
        this.loadData()
      })
    },
    handleDetailAddSign() {
      // 确保详情对话框已关闭后再刷新数据
      this.$nextTick(() => {
        this.loadData()
      })
    },
    handleDetailTransfer() {
      // 确保详情对话框已关闭后再刷新数据
      this.$nextTick(() => {
        this.loadData()
      })
    },
    async handleConfirmApprove(data) {
      try {
        // data可能是字符串（意见）或对象（包含意见和签名）
        let opinion = ''
        let signature = null
        if (typeof data === 'string') {
          opinion = data
        } else if (data && typeof data === 'object') {
          opinion = data.opinion || ''
          signature = data.signature || null
        }
        
        // 如果是批量审批
        if (this.batchApproveRows && this.batchApproveRows.length > 0) {
          let successCount = 0
          let failCount = 0
          
          for (const row of this.batchApproveRows) {
            try {
              await this.doApprove(row, opinion, signature)
              successCount++
            } catch (error) {
              failCount++
              console.error('审批失败:', error)
            }
          }
          
          this.$message.success(`批量审批完成：成功 ${successCount} 个，失败 ${failCount} 个`)
          this.batchApproveRows = []
          this.selectedRows = []
          this.loadData()
        }
      } catch (error) {
        console.error('审批确认失败:', error)
        this.$message.error('审批失败：' + (error.message || '未知错误'))
      } finally {
        // 无论成功还是失败，都关闭对话框，避免一直转圈
        this.approvalDialogVisible = false
      }
    },
    handleCloseApprovalDialog() {
      this.approvalDialogVisible = false
      this.nextNodeInfo = null
      this.batchApproveRows = []
    },
    async handleBatchApprove() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要审批的申请')
        return
      }
      
      // 批量审批时，使用第一个选中的申请获取下一节点信息（通常批量审批的申请流程相同）
      const firstRow = this.selectedRows[0]
      if (!firstRow || !firstRow.applyNo) {
        this.$message.warning('无法获取申请信息')
        return
      }
      
      // 保存批量审批的行数据
      this.batchApproveRows = [...this.selectedRows]
      
      // 获取下一节点信息（如果失败也不阻止显示对话框）
      this.nextNodeInfo = null
      try {
        const response = await getNextNodeInfoByBusinessKey(firstRow.applyNo)
        if (response.code === 200 && response.data) {
          this.nextNodeInfo = response.data
        }
      } catch (error) {
        console.error('获取下一节点信息失败:', error)
        // 即使获取失败，也显示对话框，只是不显示下一节点信息
      }
      
      // 显示审批确认对话框
      this.approvalDialogVisible = true
    },
    async handleBatchReject() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要拒绝的申请')
        return
      }
      
      // 批量拒绝时，显示退回对话框
      this.batchRejectRows = [...this.selectedRows]
      this.returnDialogVisible = true
    },
    async doApprove(row, opinion, signature) {
      try {
        const response = await approveBudgetApply(row.applyId, opinion, signature)
        if (response.code === 200) {
          this.$message.success('审批通过')
          this.loadData()
        } else {
          this.$message.error(response.message || '审批失败')
        }
      } catch (error) {
        this.$message.error('审批失败：' + (error.message || '未知错误'))
        throw error
      }
    },
    async doReject(row, opinion) {
      try {
        const response = await rejectBudgetApply(row.applyId, opinion)
        if (response.code === 200) {
          this.$message.success('已拒绝')
          this.loadData()
        } else {
          this.$message.error(response.message || '操作失败')
        }
      } catch (error) {
        this.$message.error('拒绝失败：' + (error.message || '未知错误'))
        throw error
      }
    },
    async handleConfirmReturn(data) {
      // data包含returnType和opinion
      const returnType = data.returnType || 'RETURN_TO_CURRENT'
      const opinion = data.opinion || ''
      
      // 如果是批量退回
      if (this.batchRejectRows && this.batchRejectRows.length > 0) {
        let successCount = 0
        let failCount = 0
        
        for (const row of this.batchRejectRows) {
          try {
            const response = await returnBudgetApply(row.applyId, returnType, opinion)
            if (response.code === 200) {
              successCount++
            } else {
              failCount++
              console.error('退回失败:', response.message)
            }
          } catch (error) {
            failCount++
            console.error('退回失败:', error)
          }
        }
        
        this.$message.success(`批量退回完成：成功 ${successCount} 个，失败 ${failCount} 个`)
        this.batchRejectRows = []
        this.selectedRows = []
        this.returnDialogVisible = false
        this.loadData()
      }
    },
    handleCloseReturnDialog() {
      this.returnDialogVisible = false
      this.batchRejectRows = []
    },
    getStatusType(status) {
      const map = {
        'DRAFT': 'info',
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger',
        'WITHDRAWN': 'info'
      }
      return map[status] || 'info'
    },
    getStatusText(status) {
      const map = {
        'DRAFT': '草稿',
        'PENDING': '待审批',
        'APPROVED': '已审批',
        'REJECTED': '已拒绝',
        'WITHDRAWN': '已撤回'
      }
      return map[status] || status
    },
    formatDateOnly(date) {
      if (!date) return '-'
      const d = new Date(date)
      if (isNaN(d.getTime())) return date
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportApproval(false)
      } else if (command === 'all') {
        this.handleExportApproval(true)
      }
    },
    // 导出预算审批数据
    async handleExportApproval(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const params = {
            page: 1,
            size: 10000, // 设置一个很大的值以获取所有数据
            isApprovalList: true,
            ...this.buildSearchParams()
          }
          
          const res = await getBudgetApplies(params)
          if (res.code === 200 && res.data) {
            dataToExport = res.data.records || res.data.list || []
          } else {
            this.$message.error(res.message || '获取数据失败')
            return
          }
        } else {
          // 导出当前页数据
          dataToExport = this.tableData
        }
        
        if (dataToExport.length === 0) {
          this.$message.warning('没有数据可导出')
          return
        }
        
        // 构建表头
        const headers = ['申请单号', '预算项目', '预算主体', '年度', '申请金额', '申请人', '状态', '申请时间']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            String(item.applyNo || ''),
            String(item.itemName || ''),
            String(item.subjectName || ''),
            String(item.budgetYear || ''),
            String(item.applyAmount ? '¥' + item.applyAmount : ''),
            String(item.applicantName || ''),
            String(this.getStatusText(item.status) || ''),
            String(this.formatDateOnly(item.applyDate || item.createTime) || '')
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '预算审批' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
          headers: headers,
          dataList: dataList
        })
        
        // 处理blob响应并下载
        if (!response || !response.data) {
          this.$message.error('导出失败：服务器返回数据为空')
          return
        }
        
        const blob = response.data
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '预算审批' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败：' + (error.message || '未知错误'))
      } finally {
        this.exportLoading = false
      }
    }
  }
}
</script>

<style scoped>
.budget-approval {
  padding: 20px;
}
</style>
