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

      <el-table :data="paginatedData" border style="width: 100%; margin-top: 20px" v-loading="loading">
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
        <el-table-column label="操作" width="380">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status === 'PENDING'" size="mini" type="success" @click="handleApprove(scope.row)">通过</el-button>
            <el-button v-if="scope.row.status === 'PENDING'" size="mini" type="danger" @click="handleReject(scope.row)">驳回</el-button>
            <el-button v-if="scope.row.status === 'PENDING'" size="mini" type="warning" @click="handleAddSign(scope.row)">加签</el-button>
            <el-button size="mini" type="info" icon="el-icon-printer" @click="handlePrint(scope.row)">打印</el-button>
            <el-button size="mini" @click="handleView(scope.row)">查看</el-button>
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

    <!-- 加签对话框 -->
    <el-dialog title="加签" :visible.sync="addSignVisible" width="500px">
      <el-form :model="addSignForm" :rules="addSignRules" ref="addSignForm" label-width="100px">
        <el-form-item label="被加签人" prop="targetEmpCode">
          <el-autocomplete
            v-model="addSignForm.targetEmpCode"
            :fetch-suggestions="searchEmployee"
            placeholder="请输入工号或姓名搜索"
            value-key="empCode"
            @select="handleAddSignEmployeeSelect"
            style="width: 100%"
          >
            <template slot-scope="{ item }">
              <div>{{ item.empCode }} - {{ item.empName }}</div>
            </template>
          </el-autocomplete>
        </el-form-item>
        <el-form-item label="加签原因" prop="addsignReason">
          <el-input type="textarea" v-model="addSignForm.addsignReason" :rows="4" placeholder="请输入加签原因"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addSignVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmAddSign">确定</el-button>
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
import { paginationMixin } from '@/mixins/pagination'
import { createAddSign, getProcessTaskByBusinessKey } from '@/api/process'
import { getEmployeeList } from '@/api/user'
import { getDefaultPrintTemplate, generatePrintContent } from '@/api/print'

export default {
  name: 'ContractApproval',
  mixins: [paginationMixin],
  data() {
    return {
      loading: false,
      activeTab: 'PENDING',
      tableData: [],
      allData: [],
      contractTypeOptions: [],
      approvalVisible: false,
      approvalTitle: '',
      approvalForm: {
        contractId: null,
        approvalType: '', // 'approve' or 'reject'
        approvalOpinion: ''
      },
      detailVisible: false,
      currentDetail: null,
      addSignVisible: false,
      addSignForm: {
        parentTaskId: null,
        targetUserId: '',
        targetUserName: '',
        targetEmpCode: '',
        addsignReason: ''
      },
      addSignRules: {
        targetEmpCode: [{ required: true, message: '请选择被加签人', trigger: 'change' }],
        addsignReason: [{ required: true, message: '请输入加签原因', trigger: 'blur' }]
      },
      employeeList: []
    }
  },
  computed: {
    paginatedData() {
      const start = (this.pagination.page - 1) * this.pagination.size
      const end = start + this.pagination.size
      return this.tableData.slice(start, end)
    }
  },
  mounted() {
    this.loadCodeTypeOptions()
    this.loadData()
    this.loadEmployeeList()
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
          this.allData = response.data || []
          this.filterAndPaginateData()
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    filterAndPaginateData() {
      let filtered = [...this.allData]
      // 根据当前tab过滤数据
      if (this.activeTab === 'PENDING') {
        filtered = filtered.filter(item => item.status === 'PENDING')
      } else if (this.activeTab === 'APPROVED') {
        filtered = filtered.filter(item => item.status === 'APPROVED')
      } else if (this.activeTab === 'REJECTED') {
        filtered = filtered.filter(item => item.status === 'REJECTED')
      }
      this.tableData = filtered
      this.pagination.total = filtered.length
      this.pagination.page = 1
    },
    handleTabClick() {
      this.pagination.page = 1
      this.filterAndPaginateData()
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
    },
    handleCurrentChange(val) {
      this.pagination.page = val
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
    loadEmployeeList() {
      getEmployeeList().then(response => {
        if (response.code === 200) {
          this.employeeList = response.data || []
        }
      })
    },
    searchEmployee(queryString, cb) {
      const results = this.employeeList.filter(emp => {
        return (emp.empCode && emp.empCode.includes(queryString)) ||
               (emp.empName && emp.empName.includes(queryString))
      }).map(emp => ({
        empCode: emp.empCode,
        empName: emp.empName,
        empId: emp.empId
      }))
      cb(results)
    },
    handleAddSignEmployeeSelect(item) {
      this.addSignForm.targetUserId = item.empId
      this.addSignForm.targetUserName = item.empName
      this.addSignForm.targetEmpCode = item.empCode
    },
    async handleAddSign(row) {
      // 根据业务主键获取当前任务
      try {
        const taskResponse = await getProcessTaskByBusinessKey(row.contractNo)
        if (taskResponse.code === 200 && taskResponse.data && taskResponse.data.length > 0) {
          // 找到当前用户的任务
          const userId = this.$store.state.user.userInfo.userId || this.$store.state.user.userInfo.id
          const currentTask = taskResponse.data.find(t => t.assigneeUserId === userId && t.taskStatus === 'PENDING')
          if (currentTask) {
            this.addSignForm.parentTaskId = currentTask.taskId
            this.addSignForm.targetUserId = ''
            this.addSignForm.targetUserName = ''
            this.addSignForm.targetEmpCode = ''
            this.addSignForm.addsignReason = ''
            this.addSignVisible = true
          } else {
            this.$message.warning('未找到当前用户的待办任务')
          }
        } else {
          this.$message.warning('未找到相关流程任务')
        }
      } catch (error) {
        this.$message.error('获取任务信息失败')
      }
    },
    handleConfirmAddSign() {
      this.$refs.addSignForm.validate(valid => {
        if (valid) {
          const userInfo = this.$store.state.user.userInfo
          const addsignData = {
            parentTaskId: this.addSignForm.parentTaskId,
            targetUserId: this.addSignForm.targetUserId,
            targetUserName: this.addSignForm.targetUserName,
            targetEmpCode: this.addSignForm.targetEmpCode,
            addsignUserId: userInfo.userId || userInfo.id,
            addsignUserName: userInfo.name || userInfo.userName,
            addsignEmpCode: userInfo.empCode || userInfo.account,
            addsignReason: this.addSignForm.addsignReason
          }
          createAddSign(addsignData).then(response => {
            if (response.code === 200) {
              this.$message.success('加签成功')
              this.addSignVisible = false
              this.loadData()
            } else {
              this.$message.error(response.message || '加签失败')
            }
          }).catch(() => {
            this.$message.error('加签失败')
          })
        }
      })
    },
    async handlePrint(row) {
      try {
        const templateResponse = await getDefaultPrintTemplate('CONTRACT')
        if (templateResponse.code !== 200 || !templateResponse.data) {
          this.$message.warning('未找到打印模板，请先在系统设置中配置打印模板')
          return
        }
        const template = templateResponse.data
        const printResponse = await generatePrintContent({
          templateId: template.templateId,
          businessKey: row.contractNo,
          templateType: 'CONTRACT'
        })
        if (printResponse.code === 200) {
          const printWindow = window.open('', '_blank')
          printWindow.document.write(`
            <!DOCTYPE html>
            <html>
            <head>
              <title>打印 - ${row.contractNo}</title>
              <style>
                body { margin: 0; padding: ${template.marginTop || 20}mm ${template.marginRight || 20}mm ${template.marginBottom || 20}mm ${template.marginLeft || 20}mm; }
                @media print {
                  @page { size: ${template.pageSize || 'A4'} ${template.orientation || 'portrait'}; margin: 0; }
                }
              </style>
            </head>
            <body>${printResponse.data}</body>
            </html>
          `)
          printWindow.document.close()
          printWindow.focus()
          setTimeout(() => { printWindow.print() }, 250)
        } else {
          this.$message.error(printResponse.message || '生成打印内容失败')
        }
      } catch (error) {
        this.$message.error('打印失败：' + (error.message || '未知错误'))
      }
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

