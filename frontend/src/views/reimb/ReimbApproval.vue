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

      <el-table :data="tableData" border style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="payoutBillcode" label="报账单号" width="150"></el-table-column>
        <el-table-column prop="empName" label="申请人" width="100"></el-table-column>
        <el-table-column prop="payoutTypeId" label="报账类型" width="120">
          <template slot-scope="scope">
            {{ getPayoutTypeName(scope.row.payoutTypeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="applyAmount" label="报账金额" width="120">
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
        <el-table-column prop="applyDate" label="报账日期" width="150">
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
    <el-dialog title="报账单详情" :visible.sync="detailVisible" width="1200px">
      <div v-if="currentDetail">
        <el-tabs v-model="detailActiveTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="报账单号">{{ currentDetail.payoutBillcode }}</el-descriptions-item>
              <el-descriptions-item label="申请人">{{ currentDetail.empName }}</el-descriptions-item>
              <el-descriptions-item label="报账类型">{{ getPayoutTypeName(currentDetail.payoutTypeId) }}</el-descriptions-item>
              <el-descriptions-item label="报账金额">¥{{ currentDetail.applyAmount }}</el-descriptions-item>
              <el-descriptions-item label="报账日期">{{ formatDate(currentDetail.applyDate) }}</el-descriptions-item>
              <el-descriptions-item label="预算">{{ currentDetail.budgetName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusType(currentDetail.status)">{{ getStatusText(currentDetail.status) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="报账事由" :span="2">{{ currentDetail.applyReason }}</el-descriptions-item>
              <el-descriptions-item label="备注" :span="2">{{ currentDetail.remark || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          
          <el-tab-pane label="支付明细" name="detail">
            <el-table :data="currentDetails" border>
              <el-table-column type="index" label="序号" width="60"></el-table-column>
              <el-table-column prop="itemName" label="项目名称"></el-table-column>
              <el-table-column prop="itemType" label="项目类型" width="120"></el-table-column>
              <el-table-column prop="amount" label="金额" width="120">
                <template slot-scope="scope">
                  ¥{{ scope.row.amount }}
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注"></el-table-column>
            </el-table>
            <div v-if="currentDetails.length === 0" style="color: #999; text-align: center; padding: 20px;">暂无明细</div>
          </el-tab-pane>
          
          <el-tab-pane label="发票信息" name="invoice">
            <el-table :data="currentInvoices" border>
              <el-table-column type="index" label="序号" width="60"></el-table-column>
              <el-table-column prop="invoiceCode" label="发票代码" width="150"></el-table-column>
              <el-table-column prop="invoiceNumber" label="发票号码" width="150"></el-table-column>
              <el-table-column prop="invoiceDate" label="发票日期" width="180">
                <template slot-scope="scope">
                  {{ formatDate(scope.row.invoiceDate) }}
                </template>
              </el-table-column>
              <el-table-column prop="invoiceAmount" label="发票金额" width="120">
                <template slot-scope="scope">
                  ¥{{ scope.row.invoiceAmount }}
                </template>
              </el-table-column>
              <el-table-column prop="invoiceType" label="发票类型" width="120">
                <template slot-scope="scope">
                  {{ scope.row.invoiceType === 'VAT' ? '增值税发票' : '普通发票' }}
                </template>
              </el-table-column>
              <el-table-column prop="taxAmount" label="税额" width="120">
                <template slot-scope="scope">
                  ¥{{ scope.row.taxAmount }}
                </template>
              </el-table-column>
              <el-table-column label="发票附件" width="100">
                <template slot-scope="scope">
                  <el-button v-if="scope.row.attachmentId" size="mini" type="text" @click="handleDownloadInvoice(scope.row)">查看</el-button>
                  <span v-else style="color: #999;">-</span>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="currentInvoices.length === 0" style="color: #999; text-align: center; padding: 20px;">暂无发票</div>
          </el-tab-pane>
          
          <el-tab-pane label="支付清单" name="payment">
            <el-table :data="currentPayments" border>
              <el-table-column type="index" label="序号" width="60"></el-table-column>
              <el-table-column prop="paymentAmount" label="支付金额" width="120">
                <template slot-scope="scope">
                  ¥{{ scope.row.paymentAmount }}
                </template>
              </el-table-column>
              <el-table-column prop="paymentObject" label="支付对象" width="120"></el-table-column>
              <el-table-column prop="paymentMethod" label="支付方式" width="120">
                <template slot-scope="scope">
                  {{ getPaymentMethodName(scope.row.paymentMethod) }}
                </template>
              </el-table-column>
              <el-table-column prop="bankName" label="银行名称" width="150"></el-table-column>
              <el-table-column prop="bankAccount" label="银行账号" width="180"></el-table-column>
              <el-table-column prop="accountName" label="账户名称" width="150"></el-table-column>
              <el-table-column prop="paymentDate" label="支付日期" width="180">
                <template slot-scope="scope">
                  {{ formatDate(scope.row.paymentDate) }}
                </template>
              </el-table-column>
            </el-table>
            <div v-if="currentPayments.length === 0" style="color: #999; text-align: center; padding: 20px;">暂无支付清单</div>
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
import { getDefaultPrintTemplate, generatePrintContent, getPrintTemplateList } from '@/api/print'
import { getTemplateConfigByBusinessType } from '@/api/templateConfig'

export default {
  name: 'ReimbApproval',
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
      currentDetails: [],
      currentInvoices: [],
      currentPayments: [],
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
          // 只显示报账单
          let records = (response.data.records || []).filter(item => item.billType === 'PAYOUT')
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
      this.approvalTitle = '驳回报账'
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
        const taskResponse = await getProcessTaskByBusinessKey(row.payoutBillcode)
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
        // 根据报账类型从模板设置中获取打印模板
        let template = null
        if (row.payoutTypeId) {
          const configResponse = await getTemplateConfigByBusinessType('PAYOUT_TYPE', row.payoutTypeId)
          if (configResponse.code === 200 && configResponse.data && configResponse.data.printTemplateId) {
            // 从模板设置中获取打印模板ID
            const templateResponse = await getDefaultPrintTemplate('PAYOUT')
            // 查找匹配的模板
            if (templateResponse.code === 200 && templateResponse.data) {
              const allTemplates = await getPrintTemplateList()
              if (allTemplates.code === 200) {
                template = allTemplates.data.find(t => t.templateId === configResponse.data.printTemplateId)
              }
            }
          }
        }
        
        // 如果模板设置中没有，使用默认模板
        if (!template) {
          const templateResponse = await getDefaultPrintTemplate('PAYOUT')
          if (templateResponse.code !== 200 || !templateResponse.data) {
            this.$message.warning('未找到打印模板，请先在模板设置中配置打印模板')
            return
          }
          template = templateResponse.data
        }
        
        const printResponse = await generatePrintContent({
          templateId: template.templateId,
          businessKey: row.payoutBillcode,
          templateType: 'PAYOUT'
        })
        if (printResponse.code === 200) {
          const printWindow = window.open('', '_blank')
          printWindow.document.write(`
            <!DOCTYPE html>
            <html>
            <head>
              <title>打印 - ${row.payoutBillcode}</title>
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
          getPayoutDetail(row.payoutId),
          getPayoutApprovals(row.payoutId)
        ])
        
        if (detailRes.code === 200) {
          this.currentDetail = detailRes.data.payout
          this.currentDetails = detailRes.data.details || []
          this.currentInvoices = detailRes.data.invoices || []
          this.currentPayments = detailRes.data.payments || []
          
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
    async handleDownloadInvoice(invoice) {
      if (invoice.attachmentId) {
        try {
          const response = await getAttachmentsByBusiness('PAYOUT_INVOICE', invoice.attachmentId)
          if (response.code === 200 && response.data && response.data.length > 0) {
            window.open(response.data[0].filePath, '_blank')
          }
        } catch (error) {
          this.$message.error('加载发票附件失败')
        }
      }
    },
    getPaymentMethodName(method) {
      const map = {
        'BANK': '银行转账',
        'CASH': '现金',
        'CHECK': '支票',
        'OTHER': '其他'
      }
      return map[method] || method
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
.reimb-approval {
  padding: 20px;
}
</style>
