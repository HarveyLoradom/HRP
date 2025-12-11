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
import { getMyApprovalPayouts, approvePayout, rejectPayout, getPayoutDetail, getPayoutApprovals } from '@/api/reimb'
import { getAttachmentsByBusiness } from '@/api/attachment'
import { getCodeTypeOptions } from '@/utils/codeType'

export default {
  name: 'ReimbApproval',
  data() {
    return {
      loading: false,
      activeTab: 'PENDING',
      detailActiveTab: 'basic',
      tableData: [],
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
      approvals: []
    }
  },
  mounted() {
    this.loadCodeTypeOptions()
    this.loadData()
  },
  methods: {
    async loadCodeTypeOptions() {
      this.payoutTypeOptions = await getCodeTypeOptions('PAYOUT_TYPE')
      this.applyStatusOptions = await getCodeTypeOptions('APPLY_STATUS')
    },
    loadData() {
      this.loading = true
      const userId = this.$store.state.user.userInfo.userId || this.$store.state.user.userInfo.id
      getMyApprovalPayouts(userId).then(response => {
        if (response.code === 200) {
          const allData = response.data || []
          // 只显示报账单
          const payoutData = allData.filter(item => item.billType === 'PAYOUT')
          // 根据当前tab过滤数据
          if (this.activeTab === 'PENDING') {
            this.tableData = payoutData.filter(item => item.status === 'PENDING')
          } else if (this.activeTab === 'APPROVED') {
            this.tableData = payoutData.filter(item => item.status === 'APPROVED')
          } else if (this.activeTab === 'REJECTED') {
            this.tableData = payoutData.filter(item => item.status === 'REJECTED')
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
