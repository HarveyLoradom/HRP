<template>
  <div class="procurement-approval">
    <el-card>
      <div slot="header" class="clearfix">
        <span>采购审批</span>
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
            <el-form-item label="申请人:" label-width="100px">
              <el-input v-model="searchForm.applyEmpName" placeholder="请输入申请人" clearable style="width: 100%;"></el-input>
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
        <el-table-column prop="applyEmpName" label="申请人" width="130"></el-table-column>
        <el-table-column prop="applyDeptName" label="部门" width="150"></el-table-column>
        <el-table-column prop="applyMoney" label="申请金额" width="130">
          <template slot-scope="scope">
            ¥{{ scope.row.applyMoney }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="160">
          <template slot-scope="scope">
            {{ formatDateOnly(scope.row.applyTime || scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" style="margin-top: 20px; display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
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
    <ProcurementApplyDetail
      v-model="detailVisible"
      source-type="approval"
      :apply-id="selectedApplyId"
      @approved="handleDetailApproved"
      @returned="handleDetailReturned"
      @add-signed="handleDetailAddSign"
      @transferred="handleDetailTransfer"
    />

    <!-- 审批确认对话框 -->
    <ApprovalConfirmDialog
      v-model="approvalDialogVisible"
      :next-node-info="nextNodeInfo"
      confirm-button-text="确认同意"
      @confirm="handleConfirmApprove"
      @close="handleCloseApprovalDialog"
    />

    <!-- 退回对话框 -->
    <RejectReturnDialog
      v-model="returnDialogVisible"
      @confirm="handleConfirmReturn"
      @close="handleCloseReturnDialog"
    />

  </div>
</template>

<script>
import { getAssetPurchaseApprovalPage, approveAssetPurchaseApply, rejectAssetPurchaseApply, returnAssetPurchaseApply } from '@/api/asset'
import { getCodeTypeOptions } from '@/utils/codeType'
import { getNextNodeInfoByBusinessKey } from '@/api/process'
import { paginationMixin } from '@/mixins/pagination'
import { exportExcel } from '@/api/common'
import ApprovalConfirmDialog from '@/components/ApprovalConfirmDialog.vue'
import RejectReturnDialog from '@/components/RejectReturnDialog.vue'
import ProcurementApplyDetail from '@/views/asset/ProcurementApplyDetail.vue'

export default {
  name: 'ProcurementApproval',
  mixins: [paginationMixin],
  components: {
    ApprovalConfirmDialog,
    RejectReturnDialog,
    ProcurementApplyDetail
  },
  data() {
    return {
      loading: false,
      exportLoading: false,
      tableData: [],
      selectedRows: [],
      searchForm: {
        applyNo: '',
        applyEmpName: '',
        applyDateRange: null,
        status: 'PENDING' // 默认显示待审批
      },
      applyStatusOptions: [],
      detailVisible: false,
      selectedApplyId: null,
      approvalDialogVisible: false,
      nextNodeInfo: null,
      batchApproveRows: [],
      batchRejectRows: [],
      returnDialogVisible: false
    }
  },
  mounted() {
    this.loadCodeTypeOptions()
    this.loadData()
  },
  methods: {
    async loadCodeTypeOptions() {
      this.applyStatusOptions = await getCodeTypeOptions('APPLY_STATUS')
    },
    loadData() {
      this.loading = true
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        ...this.buildSearchParams()
      }
      
      getAssetPurchaseApprovalPage(params).then(response => {
        if (response.code === 200 && response.data) {
          this.tableData = response.data.records || []
          this.pagination.total = response.data.total || 0
        } else {
          this.tableData = []
          this.pagination.total = 0
        }
        this.loading = false
      }).catch(() => {
        this.tableData = []
        this.pagination.total = 0
        this.loading = false
      })
    },
    buildSearchParams() {
      const params = {}
      if (this.searchForm.applyNo) {
        params.applyNo = this.searchForm.applyNo
      }
      if (this.searchForm.applyEmpName) {
        params.applyEmpName = this.searchForm.applyEmpName
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
        applyEmpName: '',
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
      this.selectedApplyId = row.id
      this.detailVisible = true
    },
    async handleConfirmApprove(data) {
      try {
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
      
      const firstRow = this.selectedRows[0]
      if (!firstRow || !firstRow.applyNo) {
        this.$message.warning('无法获取申请信息')
        return
      }
      
      this.batchApproveRows = [...this.selectedRows]
      
      this.nextNodeInfo = null
      try {
        const response = await getNextNodeInfoByBusinessKey(firstRow.applyNo)
        if (response.code === 200 && response.data) {
          this.nextNodeInfo = response.data
        }
      } catch (error) {
        console.error('获取下一节点信息失败:', error)
      }
      
      this.approvalDialogVisible = true
    },
    async handleBatchReject() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要拒绝的申请')
        return
      }
      
      this.batchRejectRows = [...this.selectedRows]
      this.returnDialogVisible = true
    },
    async doApprove(row, opinion, signature) {
      try {
        const userId = this.$store.state.user.userInfo.userId || this.$store.state.user.userInfo.id
        const response = await approveAssetPurchaseApply(row.id, userId, opinion, signature)
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
        const userId = this.$store.state.user.userInfo.userId || this.$store.state.user.userInfo.id
        const response = await rejectAssetPurchaseApply(row.id, userId, opinion)
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
      
      if (this.batchRejectRows && this.batchRejectRows.length > 0) {
        let successCount = 0
        let failCount = 0
        
        for (const row of this.batchRejectRows) {
          try {
            const response = await returnAssetPurchaseApply(row.id, returnType, opinion)
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
    formatDateOnly(date) {
      if (!date) return '-'
      const d = new Date(date)
      if (isNaN(d.getTime())) return date
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    getStatusText(status) {
      const option = this.applyStatusOptions.find(item => item.value === status)
      return option ? option.label : status
    },
    getStatusType(status) {
      const typeMap = {
        'DRAFT': 'info',
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger',
        'WITHDRAWN': 'info'
      }
      return typeMap[status] || ''
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportProcurement(false)
      } else if (command === 'all') {
        this.handleExportProcurement(true)
      }
    },
    // 导出采购审批数据
    async handleExportProcurement(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const params = {
            page: 1,
            size: 10000, // 设置一个很大的值以获取所有数据
            ...this.buildSearchParams()
          }
          
          const res = await getAssetPurchaseApprovalPage(params)
          if (res.code === 200 && res.data) {
            dataToExport = res.data.records || []
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
        const headers = ['申请单号', '申请人', '部门', '申请金额', '状态', '申请时间']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            item.applyNo || '',
            item.applyEmpName || '',
            item.applyDeptName || '',
            item.applyMoney ? '¥' + item.applyMoney : '',
            this.getStatusText(item.status),
            this.formatDateOnly(item.applyTime || item.createTime)
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '采购审批' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
          headers: headers,
          dataList: dataList
        })
        
        // 处理blob响应并下载
        const blob = response.data
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '采购审批' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
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
.procurement-approval {
  padding: 20px;
}
</style>
