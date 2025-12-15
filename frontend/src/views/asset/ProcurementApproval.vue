<template>
  <div class="procurement-approval">
    <el-card>
      <div slot="header">
        <span>需求审核</span>
      </div>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="待审批" name="PENDING"></el-tab-pane>
        <el-tab-pane label="已审批" name="APPROVED"></el-tab-pane>
        <el-tab-pane label="已拒绝" name="REJECTED"></el-tab-pane>
      </el-tabs>

      <el-table :data="tableData" border style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="requirementNo" label="需求单号" width="150"></el-table-column>
        <el-table-column prop="requirementName" label="需求名称" width="200"></el-table-column>
        <el-table-column prop="requirementType" label="需求类型" width="120">
          <template slot-scope="scope">
            {{ getRequirementTypeName(scope.row.requirementType) }}
          </template>
        </el-table-column>
        <el-table-column prop="estimatedAmount" label="预估金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.estimatedAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100"></el-table-column>
        <el-table-column prop="deptName" label="部门" width="120"></el-table-column>
        <el-table-column prop="createTime" label="申请日期" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
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

      <!-- 分页组件 -->
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        style="margin-top: 20px; text-align: right;"
      ></el-pagination>
    </el-card>

    <!-- 审批对话框 -->
    <el-dialog :title="approvalTitle" :visible.sync="approvalVisible" width="500px">
      <el-form :model="approvalForm" label-width="100px">
        <el-form-item label="审批意见">
          <el-input type="textarea" v-model="approvalForm.opinion" :rows="4" placeholder="请输入审批意见（可选）"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="approvalVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmApproval">确定</el-button>
      </div>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog title="需求详情" :visible.sync="detailVisible" width="900px">
      <div v-if="currentDetail">
        <el-tabs v-model="detailActiveTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="需求单号">{{ currentDetail.requirementNo }}</el-descriptions-item>
              <el-descriptions-item label="需求名称">{{ currentDetail.requirementName }}</el-descriptions-item>
              <el-descriptions-item label="需求类型">{{ getRequirementTypeName(currentDetail.requirementType) }}</el-descriptions-item>
              <el-descriptions-item label="预估金额">¥{{ currentDetail.estimatedAmount }}</el-descriptions-item>
              <el-descriptions-item label="申请人">{{ currentDetail.applicantName }}</el-descriptions-item>
              <el-descriptions-item label="部门">{{ currentDetail.deptName }}</el-descriptions-item>
              <el-descriptions-item label="申请日期">{{ formatDate(currentDetail.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusType(currentDetail.status)">{{ getStatusText(currentDetail.status) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="需求描述" :span="2">{{ currentDetail.requirementDesc }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          
          <el-tab-pane label="审批记录" name="approval">
            <el-timeline>
              <el-timeline-item
                v-for="approval in approvalRecords"
                :key="approval.approvalId"
                :timestamp="formatDate(approval.approvalTime)"
                placement="top"
              >
                <el-card>
                  <h4>{{ approval.approverName }} 
                    <el-tag :type="approval.approvalResult === 'APPROVED' ? 'success' : 'danger'" size="small" style="margin-left: 10px;">
                      {{ approval.approvalResult === 'APPROVED' ? '通过' : '驳回' }}
                    </el-tag>
                  </h4>
                  <p v-if="approval.approvalOpinion" style="margin-top: 10px;">{{ approval.approvalOpinion }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
            <div v-if="approvalRecords.length === 0" style="color: #999; text-align: center; padding: 20px;">暂无审批记录</div>
          </el-tab-pane>
        </el-tabs>
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
  </div>
</template>

<script>
import { 
  getProcurementMyListPage,
  approveProcurement,
  rejectProcurement,
  getProcurementDetail,
  getProcurementApprovals
} from '@/api/asset'
import { createAddSign, getProcessTaskByBusinessKey } from '@/api/process'
import { getEmployeeList } from '@/api/user'
import { getDefaultPrintTemplate, generatePrintContent } from '@/api/print'

export default {
  name: 'ProcurementApproval',
  data() {
    return {
      loading: false,
      activeTab: 'PENDING',
      detailActiveTab: 'basic',
      tableData: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      approvalVisible: false,
      approvalTitle: '',
      detailVisible: false,
      currentDetail: null,
      approvalRecords: [],
      approvalForm: {
        requirementId: null,
        opinion: ''
      },
      statusMap: {
        'DRAFT': '草稿',
        'PENDING': '待审批',
        'APPROVED': '已审批',
        'REJECTED': '已拒绝',
        'WITHDRAWN': '已撤回'
      }
    }
  },
  mounted() {
    this.loadData()
    this.loadEmployeeList()
  },
  methods: {
    loadData() {
      this.loading = true
      const empId = this.$store.state.user.userInfo.empId || null
      getProcurementMyListPage(empId, this.pagination.page, this.pagination.size).then(response => {
        if (response.code === 200 && response.data) {
          let records = response.data.records || []
          // 根据当前tab过滤数据（前端过滤，如果数据量大可以移到后端）
          if (this.activeTab === 'PENDING') {
            records = records.filter(item => item.status === 'PENDING')
          } else if (this.activeTab === 'APPROVED') {
            records = records.filter(item => item.status === 'APPROVED')
          } else if (this.activeTab === 'REJECTED') {
            records = records.filter(item => item.status === 'REJECTED')
          }
          this.tableData = records
          this.pagination.total = records.length > 0 ? records.length : (response.data.total || 0)
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
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
      this.loadData()
    },
    handleCurrentChange(val) {
      this.pagination.page = val
      this.loadData()
    },
    handleTabClick() {
      this.pagination.page = 1
      this.loadData()
    },
    handleApprove(row) {
      this.approvalTitle = '审批通过'
      this.approvalForm = {
        requirementId: row.requirementId,
        opinion: ''
      }
      this.approvalVisible = true
    },
    handleReject(row) {
      this.approvalTitle = '驳回需求'
      this.approvalForm = {
        requirementId: row.requirementId,
        opinion: ''
      }
      this.approvalVisible = true
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
        const taskResponse = await getProcessTaskByBusinessKey(row.requirementNo)
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
    handleConfirmApproval() {
      const userId = this.$store.state.user.userInfo.userId || this.$store.state.user.userInfo.id
      const isApprove = this.approvalTitle === '审批通过'
      const api = isApprove ? approveProcurement : rejectProcurement
      const data = {
        requirementId: this.approvalForm.requirementId,
        userId: userId,
        opinion: this.approvalForm.opinion
      }
      api(data).then(response => {
        if (response.code === 200) {
          this.$message.success(isApprove ? '审批通过' : '已驳回')
          this.approvalVisible = false
          this.loadData()
        } else {
          this.$message.error(response.message || '操作失败')
        }
      })
    },
    async handlePrint(row) {
      try {
        const templateResponse = await getDefaultPrintTemplate('PROCUREMENT')
        if (templateResponse.code !== 200 || !templateResponse.data) {
          this.$message.warning('未找到打印模板，请先在系统设置中配置打印模板')
          return
        }
        const template = templateResponse.data
        const printResponse = await generatePrintContent({
          templateId: template.templateId,
          businessKey: row.requirementNo,
          templateType: 'PROCUREMENT'
        })
        if (printResponse.code === 200) {
          const printWindow = window.open('', '_blank')
          printWindow.document.write(`
            <!DOCTYPE html>
            <html>
            <head>
              <title>打印 - ${row.requirementNo}</title>
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
    async handleView(row) {
      try {
        const [detailRes, approvalRes] = await Promise.all([
          getProcurementDetail(row.requirementId),
          getProcurementApprovals(row.requirementId)
        ])
        
        if (detailRes.code === 200 && detailRes.data && detailRes.data.requirement) {
          this.currentDetail = detailRes.data.requirement
        } else {
          this.currentDetail = row
        }
        
        if (approvalRes.code === 200) {
          this.approvalRecords = approvalRes.data || []
        } else if (detailRes.code === 200 && detailRes.data && detailRes.data.approvals) {
          this.approvalRecords = detailRes.data.approvals || []
        } else {
          this.approvalRecords = []
        }
        
        this.detailActiveTab = 'basic'
        this.detailVisible = true
      } catch (error) {
        this.$message.error('加载详情失败')
      }
    },
    getRequirementTypeName(type) {
      const typeMap = {
        'EQUIPMENT': '设备',
        'CONSUMABLE': '耗材',
        'OTHER': '其他'
      }
      return typeMap[type] || type
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
    },
    getStatusText(status) {
      return this.statusMap[status] || status
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
    }
  }
}
</script>

<style scoped>
.procurement-approval {
  padding: 20px;
}
</style>



