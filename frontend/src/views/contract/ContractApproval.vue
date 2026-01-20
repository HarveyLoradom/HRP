<template>
  <div class="contract-approval">
    <el-card>
      <div slot="header" class="clearfix">
        <span>合同审批</span>
      </div>
      
      <!-- 查询表单 -->
      <el-form :model="searchForm" style="margin-bottom: 20px;">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="合同编号:" label-width="100px">
              <el-input v-model="searchForm.contractNo" placeholder="请输入合同编号" clearable style="width: 100%;"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="合同名称:" label-width="100px">
              <el-input v-model="searchForm.contractName" placeholder="请输入合同名称" clearable style="width: 100%;"></el-input>
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
          <el-col :span="6">
            <el-form-item label="合同类型:" label-width="100px">
              <el-select v-model="searchForm.contractType" placeholder="请选择合同类型" clearable style="width: 100%;">
                <el-option
                  v-for="option in contractTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                ></el-option>
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
        <el-table-column label="合同编号" width="160">
          <template slot-scope="scope">
            <el-button type="text" @click="handleViewDetail(scope.row)">{{ scope.row.contractNo }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="contractName" label="合同名称" width="200"></el-table-column>
        <el-table-column prop="contractType" label="合同类型" width="120">
          <template slot-scope="scope">
            {{ getContractTypeName(scope.row.contractType) }}
          </template>
        </el-table-column>
        <el-table-column prop="empName" label="申请人" width="130">
          <template slot-scope="scope">
            {{ scope.row.empName || scope.row.empId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="部门" width="150">
          <template slot-scope="scope">
            {{ scope.row.deptName || scope.row.deptId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="contractAmount" label="合同金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.contractAmount }}
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


    <!-- 统一详情组件 -->
    <ContractDetail
      v-model="detailVisible"
      source-type="approval"
      :contract-id="selectedContractId"
      @approved="handleDetailApproved"
      @returned="handleDetailReturned"
      @add-signed="handleDetailAddSign"
      @transferred="handleDetailTransfer"
      @print="handleDetailPrint"
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
import { getContractsPage, approveContract, rejectContract, returnContract } from '@/api/contract'
import { getCodeTypeOptions } from '@/utils/codeType'
import { paginationMixin } from '@/mixins/pagination'
import { getNextNodeInfoByBusinessKey } from '@/api/process'
import { getDefaultPrintTemplate, generatePrintContent } from '@/api/print'
import { exportExcel } from '@/api/common'
import ApprovalConfirmDialog from '@/components/ApprovalConfirmDialog.vue'
import RejectReturnDialog from '@/components/RejectReturnDialog.vue'
import ContractDetail from '@/views/contract/ContractDetail.vue'

export default {
  name: 'ContractApproval',
  mixins: [paginationMixin],
  components: {
    ApprovalConfirmDialog,
    RejectReturnDialog,
    ContractDetail
  },
  data() {
    return {
      loading: false,
      exportLoading: false,
      tableData: [],
      selectedRows: [],
      searchForm: {
        contractNo: '',
        contractName: '',
        status: 'PENDING', // 默认显示待审批
        contractType: ''
      },
      contractTypeOptions: [],
      executionStatusOptions: [],
      detailVisible: false,
      selectedContractId: null,
      approvalDialogVisible: false,
      nextNodeInfo: null,
      batchApproveRows: [],
      batchRejectRows: [],
      returnDialogVisible: false,
      approvalTitle: '',
      approvalVisible: false,
      approvalForm: {
        contractId: null,
        approvalType: '',
        approvalOpinion: ''
      }
    }
  },
  mounted() {
    this.loadCodeTypeOptions()
    this.loadData()
  },
  methods: {
    async loadCodeTypeOptions() {
      this.contractTypeOptions = await getCodeTypeOptions('CONTRACT_TYPE')
      this.executionStatusOptions = await getCodeTypeOptions('EXECUTION_STATUS')
    },
    loadData() {
      this.loading = true
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        isApprovalList: true,
        ...this.buildSearchParams()
      }
      
      getContractsPage(params).then(response => {
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
      if (this.searchForm.contractNo) {
        params.contractNo = this.searchForm.contractNo
      }
      if (this.searchForm.contractName) {
        params.contractName = this.searchForm.contractName
      }
      if (this.searchForm.contractType) {
        params.contractType = this.searchForm.contractType
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
        contractNo: '',
        contractName: '',
        status: 'PENDING',
        contractType: ''
      }
      this.handleSearch()
    },
    handleSelectionChange(selection) {
      this.selectedRows = selection
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
          this.pagination.page = 1
          this.loadData()
        } else {
          this.$message.error(response.message || '操作失败')
        }
      })
    },
    handleView(row) {
      this.handleViewDetail(row)
    },
    async handleViewDetail(row) {
      this.selectedContractId = row.pactId
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
    handleDetailPrint() {
      // 打印功能由统一组件处理，这里只需要刷新数据
      // this.loadData()
    },
    getContractTypeName(codeValue) {
      const option = this.contractTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
    },
    formatDateOnly(date) {
      if (!date) return ''
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
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
    },
    getExecutionStatusName(codeValue) {
      if (!codeValue) return '-'
      const option = this.executionStatusOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    getExecutionStatusType(codeValue) {
      // 根据执行状态返回对应的标签类型
      if (!codeValue) return 'info'
      const status = codeValue.toUpperCase()
      if (status === 'PENDING_EXECUTION' || status === '待履约') return 'warning'
      if (status === 'EXECUTING' || status === '履约中') return 'success'
      if (status === 'COMPLETED' || status === '已履约') return 'success'
      if (status === 'ARCHIVED' || status === '已归档') return 'info'
      if (status === 'INVALID' || status === '已失效') return 'danger'
      return 'info'
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
        this.$message.warning('请选择要审批的合同')
        return
      }
      
      // 批量审批时，使用第一个选中的合同获取下一节点信息（通常批量审批的合同流程相同）
      const firstRow = this.selectedRows[0]
      if (!firstRow || !firstRow.contractNo) {
        this.$message.warning('无法获取合同信息')
        return
      }
      
      // 保存批量审批的行数据
      this.batchApproveRows = [...this.selectedRows]
      
      // 获取下一节点信息（如果失败也不阻止显示对话框）
      this.nextNodeInfo = null
      try {
        const response = await getNextNodeInfoByBusinessKey(firstRow.contractNo)
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
        this.$message.warning('请选择要拒绝的合同')
        return
      }
      
      // 批量拒绝时，显示退回对话框
      this.batchRejectRows = [...this.selectedRows]
      this.returnDialogVisible = true
    },
    async doApprove(row, opinion, signature) {
      try {
        const userId = this.$store.state.user.userInfo.userId || this.$store.state.user.userInfo.id
        const response = await approveContract(row.pactId, userId, opinion, signature)
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
        const response = await rejectContract(row.pactId, userId, opinion)
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
            const response = await returnContract(row.pactId, returnType, opinion)
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
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportApproval(false)
      } else if (command === 'all') {
        this.handleExportApproval(true)
      }
    },
    // 导出合同审批数据
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
          
          const res = await getContractsPage(params)
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
        const headers = ['合同编号', '合同名称', '合同类型', '申请人', '部门', '合同金额', '状态', '申请时间']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            String(item.contractNo || ''),
            String(item.contractName || ''),
            String(this.getContractTypeName(item.contractType) || ''),
            String(item.empName || item.empId || '-'),
            String(item.deptName || item.deptId || '-'),
            String(item.contractAmount ? '¥' + item.contractAmount : ''),
            String(this.getStatusText(item.status) || ''),
            String(this.formatDateOnly(item.createTime) || '')
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '合同审批' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
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
        link.download = '合同审批' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
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
.contract-approval {
  padding: 20px;
}
</style>

