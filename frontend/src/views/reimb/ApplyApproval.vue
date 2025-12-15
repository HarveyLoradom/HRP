<template>
  <div class="apply-approval">
    <el-card>
      <div slot="header">
        <span>申请审批</span>
      </div>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="待审批" name="PENDING"></el-tab-pane>
        <el-tab-pane label="已审批" name="APPROVED"></el-tab-pane>
        <el-tab-pane label="已拒绝" name="REJECTED"></el-tab-pane>
      </el-tabs>

      <el-table :data="tableData" border style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="payoutBillcode" label="申请单号" width="150"></el-table-column>
        <el-table-column prop="empName" label="申请人" width="100"></el-table-column>
        <el-table-column prop="payoutTypeId" label="申请类型" width="120">
          <template slot-scope="scope">
            {{ getPayoutTypeName(scope.row.payoutTypeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="applyAmount" label="申请金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.applyAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="budgetName" label="预算" width="150">
          <template slot-scope="scope">
            <span v-if="scope.row.budgetName">{{ scope.row.budgetName }}</span>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="applyDate" label="申请日期" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.applyDate) }}
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
    <el-dialog title="申请单详情" :visible.sync="detailVisible" width="900px">
      <div v-if="currentDetail">
        <el-tabs v-model="detailActiveTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="申请单号">{{ currentDetail.payoutBillcode }}</el-descriptions-item>
              <el-descriptions-item label="申请人">{{ currentDetail.empName }}</el-descriptions-item>
              <el-descriptions-item label="申请类型">{{ getPayoutTypeName(currentDetail.payoutTypeId) }}</el-descriptions-item>
              <el-descriptions-item label="申请金额">¥{{ currentDetail.applyAmount }}</el-descriptions-item>
              <el-descriptions-item label="申请日期">{{ formatDate(currentDetail.applyDate) }}</el-descriptions-item>
              <el-descriptions-item label="预算">{{ currentDetail.budgetName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusType(currentDetail.status)">{{ getStatusText(currentDetail.status) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="申请事由" :span="2">{{ currentDetail.applyReason }}</el-descriptions-item>
              <el-descriptions-item label="备注" :span="2">{{ currentDetail.remark || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          
          <el-tab-pane label="附件" name="attachment">
            <el-table :data="attachments" border>
              <el-table-column prop="fileName" label="文件名称"></el-table-column>
              <el-table-column prop="fileSize" label="文件大小" width="120">
                <template slot-scope="scope">
                  {{ formatFileSize(scope.row.fileSize) }}
                </template>
              </el-table-column>
              <el-table-column prop="uploadTime" label="上传时间" width="180">
                <template slot-scope="scope">
                  {{ formatDate(scope.row.uploadTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template slot-scope="scope">
                  <el-button size="mini" type="text" @click="handleDownload(scope.row)">下载</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="attachments.length === 0" style="color: #999; text-align: center; padding: 20px;">暂无附件</div>
          </el-tab-pane>
          
          <el-tab-pane label="审批记录" name="approval">
            <el-timeline>
              <el-timeline-item
                v-for="approval in approvals"
                :key="approval.approvalId"
                :timestamp="formatDate(approval.approvalTime)"
                placement="top"
              >
                <el-card>
                  <h4>{{ approval.approverName }} 
                    <el-tag :type="approval.approvalType === 'APPROVE' ? 'success' : 'danger'" size="small" style="margin-left: 10px;">
                      {{ approval.approvalType === 'APPROVE' ? '通过' : '驳回' }}
                    </el-tag>
                  </h4>
                  <p v-if="approval.approvalOpinion" style="margin-top: 10px;">{{ approval.approvalOpinion }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
            <div v-if="approvals.length === 0" style="color: #999; text-align: center; padding: 20px;">暂无审批记录</div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getPayoutsByStatusPage, approvePayout, rejectPayout, getPayoutDetail, getPayoutApprovals } from '@/api/reimb'
import { getAttachmentsByBusiness } from '@/api/attachment'
import { getCodeTypeOptions } from '@/utils/codeType'
import { createAddSign, getProcessTaskByBusinessKey } from '@/api/process'
import { getEmployeeList } from '@/api/user'
import { getDefaultPrintTemplate, generatePrintContent } from '@/api/print'

export default {
  name: 'ApplyApproval',
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
      payoutTypeOptions: [],
      applyStatusOptions: [],
      approvalVisible: false,
      approvalTitle: '',
      approvalForm: {
        payoutId: null,
        approvalType: '',
        approvalOpinion: ''
      },
      detailVisible: false,
      currentDetail: null,
      attachments: [],
      approvals: [],
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
  mounted() {
    this.loadCodeTypeOptions()
    this.loadData()
    this.loadEmployeeList()
  },
  methods: {
    async loadCodeTypeOptions() {
      this.payoutTypeOptions = await getCodeTypeOptions('PAYOUT_TYPE')
      this.applyStatusOptions = await getCodeTypeOptions('APPLY_STATUS')
    },
    loadData() {
      this.loading = true
      const status = this.activeTab === 'PENDING' ? 'PENDING' : (this.activeTab === 'APPROVED' ? 'APPROVED' : 'REJECTED')
      getPayoutsByStatusPage(status, this.pagination.page, this.pagination.size).then(response => {
        if (response.code === 200 && response.data) {
          // 只显示申请单
          let records = (response.data.records || []).filter(item => item.billType === 'APPLY' || !item.billType)
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
        payoutId: row.payoutId,
        approvalType: 'approve',
        approvalOpinion: ''
      }
      this.approvalVisible = true
    },
    handleReject(row) {
      this.approvalTitle = '驳回申请'
      this.approvalForm = {
        payoutId: row.payoutId,
        approvalType: 'reject',
        approvalOpinion: ''
      }
      this.approvalVisible = true
    },
    handleConfirmApproval() {
      const userId = this.$store.state.user.userInfo.userId || this.$store.state.user.userInfo.id
      const api = this.approvalForm.approvalType === 'approve' ? approvePayout : rejectPayout
      api(this.approvalForm.payoutId, userId, this.approvalForm.approvalOpinion).then(response => {
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
    async handlePrint(row) {
      try {
        // 获取默认模板
        const templateResponse = await getDefaultPrintTemplate('APPLY')
        if (templateResponse.code !== 200 || !templateResponse.data) {
          this.$message.warning('未找到打印模板，请先在系统设置中配置打印模板')
          return
        }
        
        const template = templateResponse.data
        // 生成打印内容
        const printResponse = await generatePrintContent({
          templateId: template.templateId,
          businessKey: row.payoutBillcode,
          templateType: 'APPLY'
        })
        
        if (printResponse.code === 200) {
          // 打开打印窗口
          const printWindow = window.open('', '_blank')
          printWindow.document.write(`
            <!DOCTYPE html>
            <html>
            <head>
              <title>打印 - ${row.payoutBillcode}</title>
              <style>
                body { margin: 0; padding: ${template.marginTop || 20}mm ${template.marginRight || 20}mm ${template.marginBottom || 20}mm ${template.marginLeft || 20}mm; }
                @media print {
                  @page { 
                    size: ${template.pageSize || 'A4'} ${template.orientation || 'portrait'};
                    margin: 0;
                  }
                }
              </style>
            </head>
            <body>
              ${printResponse.data}
            </body>
            </html>
          `)
          printWindow.document.close()
          printWindow.focus()
          setTimeout(() => {
            printWindow.print()
          }, 250)
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
          getPayoutDetail(row.payoutId),
          getPayoutApprovals(row.payoutId)
        ])
        
        if (detailRes.code === 200) {
          this.currentDetail = detailRes.data.payout
          if (detailRes.data.payout && detailRes.data.payout.payoutId) {
            const attachRes = await getAttachmentsByBusiness('PAYOUT', detailRes.data.payout.payoutId)
            if (attachRes.code === 200) {
              this.attachments = attachRes.data || []
            }
          }
        }
        
        if (approvalRes.code === 200) {
          this.approvals = approvalRes.data || []
        }
        
        this.detailActiveTab = 'basic'
        this.detailVisible = true
      } catch (error) {
        this.$message.error('加载详情失败')
      }
    },
    handleDownload(attachment) {
      window.open(attachment.filePath, '_blank')
    },
    formatFileSize(size) {
      if (!size) return '-'
      if (size < 1024) return size + ' B'
      if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
      return (size / (1024 * 1024)).toFixed(2) + ' MB'
    },
    getPayoutTypeName(codeValue) {
      const option = this.payoutTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
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
    }
  }
}
</script>

<style scoped>
.apply-approval {
  padding: 20px;
}
</style>
