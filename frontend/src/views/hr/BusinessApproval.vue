<template>
  <div class="business-approval">
    <el-card>
      <div slot="header" class="clearfix">
        <span>业务审批</span>
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
              <el-input v-model="searchForm.empName" placeholder="请输入申请人" clearable style="width: 100%;"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="申请类型:" label-width="100px">
              <el-select v-model="searchForm.hrApplyType" placeholder="请选择申请类型" clearable style="width: 100%;">
                <el-option
                  v-for="option in applyTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="申请时间:" label-width="100px">
              <el-date-picker
                v-model="searchForm.dateRange"
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
        </el-row>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="状态:" label-width="100px">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100%;">
                <el-option label="待审批" value="PENDING"></el-option>
                <el-option label="已审批" value="APPROVED"></el-option>
                <el-option label="已拒绝" value="REJECTED"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="18" style="text-align: right;">
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
        <el-table-column prop="empName" label="申请人" width="130"></el-table-column>
        <el-table-column prop="deptName" label="部门" width="150"></el-table-column>
        <el-table-column prop="hrApplyType" label="申请类型" width="120">
          <template slot-scope="scope">
            {{ getApplyTypeName(scope.row.hrApplyType) }}
          </template>
        </el-table-column>
        <el-table-column prop="applyDay" label="申请天数" width="100">
          <template slot-scope="scope">
            {{ scope.row.applyDay || 0 }} 天
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160">
          <template slot-scope="scope">
            {{ formatDateOnly(scope.row.createTime) }}
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
    <BusinessApplyDetail
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
import { getHrApplyPage, approveHrApply, rejectHrApply } from '@/api/hr'
import { getCodeTypeOptions } from '@/utils/codeType'
import { getNextNodeInfoByBusinessKey } from '@/api/process'
import { exportExcel } from '@/api/common'
import BusinessApplyDetail from './BusinessApplyDetail.vue'
import ApprovalConfirmDialog from '@/components/ApprovalConfirmDialog.vue'
import RejectReturnDialog from '@/components/RejectReturnDialog.vue'

export default {
  name: 'BusinessApproval',
  components: {
    BusinessApplyDetail,
    ApprovalConfirmDialog,
    RejectReturnDialog
  },
  data() {
    return {
      loading: false,
      exportLoading: false,
      searchForm: {
        applyNo: '',
        empName: '',
        hrApplyType: '',
        status: '',
        dateRange: null
      },
      tableData: [],
      selectedRows: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      applyTypeOptions: [],
      statusOptions: [],
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
    this.loadCodeOptions()
    this.loadData()
  },
  methods: {
    async loadCodeOptions() {
      try {
        this.applyTypeOptions = await getCodeTypeOptions('HR_APPLY_TYPE')
        this.statusOptions = await getCodeTypeOptions('APPLY_STATUS')
      } catch (error) {
        console.error('加载代码选项失败:', error)
      }
    },
    loadData() {
      this.loading = true
      const userInfo = this.$store.state.user.userInfo || {}
      const userId = userInfo.userId || userInfo.id
      
      if (!userId) {
        this.$message.error('未获取到当前用户信息')
        this.loading = false
        return
      }
      
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        approver: true, // 审批人视角
        applyNo: this.searchForm.applyNo || null,
        empName: this.searchForm.empName || null,
        hrApplyType: this.searchForm.hrApplyType || null,
        status: this.searchForm.status || null,
        startDate: this.searchForm.dateRange && this.searchForm.dateRange[0] || null,
        endDate: this.searchForm.dateRange && this.searchForm.dateRange[1] || null
      }
      
      getHrApplyPage(params).then(res => {
        if (res.code === 200 && res.data) {
          this.tableData = res.data.records || res.data.list || []
          this.pagination.total = res.data.total || 0
        } else {
          this.$message.error(res.msg || '查询失败')
          this.tableData = []
          this.pagination.total = 0
        }
      }).catch(err => {
        console.error('查询失败:', err)
        this.$message.error('查询失败')
        this.tableData = []
        this.pagination.total = 0
      }).finally(() => {
        this.loading = false
      })
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = {
        applyNo: '',
        empName: '',
        hrApplyType: '',
        status: '',
        dateRange: null
      }
      this.handleSearch()
    },
    handleSizeChange(size) {
      this.pagination.size = size
      this.loadData()
    },
    handleCurrentChange(page) {
      this.pagination.page = page
      this.loadData()
    },
    handleSelectionChange(selection) {
      this.selectedRows = selection
    },
    handleViewDetail(row) {
      this.selectedApplyId = row.applyId
      this.detailVisible = true
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
              const userInfo = this.$store.state.user.userInfo || {}
              const userId = userInfo.userId || userInfo.id
              
              const res = await approveHrApply(row.applyId, userId, opinion, signature)
              if (res.code === 200) {
                successCount++
              } else {
                failCount++
              }
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
    async handleBatchReject() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要拒绝的申请')
        return
      }
      
      this.batchRejectRows = [...this.selectedRows]
      this.returnDialogVisible = true
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
            const userInfo = this.$store.state.user.userInfo || {}
            const userId = userInfo.userId || userInfo.id
            
            const res = await rejectHrApply(row.applyId, userId, opinion || '批量拒绝')
            if (res.code === 200) {
              successCount++
            } else {
              failCount++
              console.error('拒绝失败:', res.message)
            }
          } catch (error) {
            failCount++
            console.error('拒绝失败:', error)
          }
        }
        
        this.$message.success(`批量拒绝完成：成功 ${successCount} 个，失败 ${failCount} 个`)
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
      this.detailVisible = false
      this.loadData()
    },
    handleDetailReturned() {
      this.detailVisible = false
      this.loadData()
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
    getApplyTypeName(type) {
      const option = this.applyTypeOptions.find(item => item.value === type)
      return option ? option.label : type
    },
    getStatusText(status) {
      const option = this.statusOptions.find(item => item.value === status)
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
    formatDateOnly(date) {
      if (!date) return '-'
      if (typeof date === 'string') {
        return date.substring(0, 10)
      }
      return date
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportBusiness(false)
      } else if (command === 'all') {
        this.handleExportBusiness(true)
      }
    },
    // 导出业务申请数据
    async handleExportBusiness(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const userInfo = this.$store.state.user.userInfo || {}
          const userId = userInfo.userId || userInfo.id
          
          if (!userId) {
            this.$message.error('未获取到当前用户信息')
            return
          }
          
          const params = {
            page: 1,
            size: 10000, // 设置一个很大的值以获取所有数据
            approver: true,
            applyNo: this.searchForm.applyNo || null,
            empName: this.searchForm.empName || null,
            hrApplyType: this.searchForm.hrApplyType || null,
            status: this.searchForm.status || null,
            startDate: this.searchForm.dateRange && this.searchForm.dateRange[0] || null,
            endDate: this.searchForm.dateRange && this.searchForm.dateRange[1] || null
          }
          
          const res = await getHrApplyPage(params)
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
        const headers = ['申请单号', '申请人', '部门', '申请类型', '申请天数', '状态', '申请时间']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            item.applyNo || '',
            item.empName || '',
            item.deptName || '',
            this.getApplyTypeName(item.hrApplyType),
            item.applyDay ? item.applyDay + ' 天' : '0 天',
            this.getStatusText(item.status),
            this.formatDateOnly(item.createTime)
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '业务审批' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
          headers: headers,
          dataList: dataList
        })
        
        // 处理blob响应并下载
        const blob = response.data
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '业务审批' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
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
.business-approval {
  padding: 20px;
}
</style>


