<template>
  <div class="my-reimb-payout">
    <el-card>
      <div slot="header" class="clearfix">
        <span>我的报账</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增报账</el-button>
      </div>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="payoutBillcode" label="报账单号" width="150"></el-table-column>
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
        <el-table-column label="操作" width="300">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button v-if="scope.row.status === 'DRAFT'" size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
            <el-button v-if="scope.row.status === 'DRAFT'" size="mini" type="primary" @click="handleSubmit(scope.row)">提交</el-button>
            <el-button v-if="scope.row.status === 'PENDING'" size="mini" type="warning" @click="handleWithdraw(scope.row)">撤回</el-button>
            <el-button size="mini" type="info" @click="handleView(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="1200px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-tabs v-model="activeTab">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="报账类型" prop="payout.payoutTypeId">
                  <el-select v-model="form.payout.payoutTypeId" placeholder="请选择报账类型" style="width: 100%">
                    <el-option
                      v-for="option in payoutTypeOptions"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="报账金额" prop="payout.applyAmount">
                  <el-input-number v-model="form.payout.applyAmount" :min="0" :precision="2" style="width: 100%"></el-input-number>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="预算主体" prop="budgetSubjectId">
              <el-select v-model="budgetSubjectId" placeholder="请选择预算主体" filterable style="width: 100%" @change="handleSubjectChange">
                <el-option
                  v-for="subject in budgetSubjects"
                  :key="subject.subjectId"
                  :label="subject.subjectName"
                  :value="subject.subjectId"
                ></el-option>
              </el-select>
            </el-form-item>
            
            <el-form-item label="预算项目" prop="budgetItemId">
              <el-select v-model="budgetItemId" placeholder="请先选择预算主体" filterable style="width: 100%" :disabled="!budgetSubjectId" @change="handleItemChange">
                <el-option
                  v-for="item in budgetItems"
                  :key="item.itemId"
                  :label="item.itemName"
                  :value="item.itemId"
                ></el-option>
              </el-select>
            </el-form-item>
            
            <el-form-item label="预算" prop="payout.budgetId">
              <el-select v-model="form.payout.budgetId" placeholder="请先选择预算主体和预算项目" filterable style="width: 100%" :disabled="!budgetSubjectId || !budgetItemId || budgets.length === 0">
                <el-option
                  v-for="budget in budgets"
                  :key="budget.budgetId"
                  :label="`${budget.budgetName || budget.budgetNo} (剩余: ¥${budget.remainingAmount || 0})`"
                  :value="budget.budgetId"
                ></el-option>
              </el-select>
            </el-form-item>
            
            <el-form-item label="报账事由" prop="payout.applyReason">
              <el-input type="textarea" v-model="form.payout.applyReason" :rows="4"></el-input>
            </el-form-item>
          </el-tab-pane>
          
          <!-- 支付明细 -->
          <el-tab-pane label="支付明细" name="detail">
            <el-button type="primary" size="small" @click="handleAddDetail" style="margin-bottom: 10px;">添加明细</el-button>
            <el-table :data="form.details" border>
              <el-table-column type="index" label="序号" width="60"></el-table-column>
              <el-table-column prop="itemName" label="项目名称">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.itemName" placeholder="请输入项目名称"></el-input>
                </template>
              </el-table-column>
              <el-table-column prop="itemType" label="项目类型" width="150">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.itemType" placeholder="请输入项目类型"></el-input>
                </template>
              </el-table-column>
              <el-table-column prop="amount" label="金额" width="150">
                <template slot-scope="scope">
                  <el-input-number v-model="scope.row.amount" :min="0" :precision="2" style="width: 100%"></el-input-number>
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.remark" placeholder="请输入备注"></el-input>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template slot-scope="scope">
                  <el-button size="mini" type="danger" @click="handleRemoveDetail(scope.$index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          
          <!-- 发票信息 -->
          <el-tab-pane label="发票信息" name="invoice">
            <el-button type="primary" size="small" @click="handleAddInvoice" style="margin-bottom: 10px;">添加发票</el-button>
            <el-table :data="form.invoices" border>
              <el-table-column type="index" label="序号" width="60"></el-table-column>
              <el-table-column prop="invoiceCode" label="发票代码" width="150">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.invoiceCode" placeholder="请输入发票代码"></el-input>
                </template>
              </el-table-column>
              <el-table-column prop="invoiceNumber" label="发票号码" width="150">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.invoiceNumber" placeholder="请输入发票号码"></el-input>
                </template>
              </el-table-column>
              <el-table-column prop="invoiceDate" label="发票日期" width="180">
                <template slot-scope="scope">
                  <el-date-picker v-model="scope.row.invoiceDate" type="date" placeholder="选择日期" style="width: 100%"></el-date-picker>
                </template>
              </el-table-column>
              <el-table-column prop="invoiceAmount" label="发票金额" width="150">
                <template slot-scope="scope">
                  <el-input-number v-model="scope.row.invoiceAmount" :min="0" :precision="2" style="width: 100%"></el-input-number>
                </template>
              </el-table-column>
              <el-table-column prop="invoiceType" label="发票类型" width="120">
                <template slot-scope="scope">
                  <el-select v-model="scope.row.invoiceType" placeholder="请选择" style="width: 100%">
                    <el-option label="增值税发票" value="VAT"></el-option>
                    <el-option label="普通发票" value="COMMON"></el-option>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="taxAmount" label="税额" width="120">
                <template slot-scope="scope">
                  <el-input-number v-model="scope.row.taxAmount" :min="0" :precision="2" style="width: 100%"></el-input-number>
                </template>
              </el-table-column>
              <el-table-column label="发票附件" width="150">
                <template slot-scope="scope">
                  <el-upload
                    :action="uploadUrl"
                    :headers="uploadHeaders"
                    :data="{ businessType: 'PAYOUT_INVOICE', businessId: scope.row.invoiceId || 'temp' }"
                    :on-success="(res) => handleInvoiceUploadSuccess(res, scope.$index)"
                    :show-file-list="false"
                  >
                    <el-button size="mini" type="text">上传</el-button>
                  </el-upload>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template slot-scope="scope">
                  <el-button size="mini" type="danger" @click="handleRemoveInvoice(scope.$index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          
          <!-- 支付清单 -->
          <el-tab-pane label="支付清单" name="payment">
            <el-button type="primary" size="small" @click="handleAddPayment" style="margin-bottom: 10px;">添加支付</el-button>
            <el-table :data="form.payments" border>
              <el-table-column type="index" label="序号" width="60"></el-table-column>
              <el-table-column prop="paymentAmount" label="支付金额" width="150">
                <template slot-scope="scope">
                  <el-input-number v-model="scope.row.paymentAmount" :min="0" :precision="2" style="width: 100%"></el-input-number>
                </template>
              </el-table-column>
              <el-table-column prop="paymentObject" label="支付对象" width="150">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.paymentObject" placeholder="请输入收款人"></el-input>
                </template>
              </el-table-column>
              <el-table-column prop="paymentMethod" label="支付方式" width="120">
                <template slot-scope="scope">
                  <el-select v-model="scope.row.paymentMethod" placeholder="请选择" style="width: 100%">
                    <el-option label="银行转账" value="BANK"></el-option>
                    <el-option label="现金" value="CASH"></el-option>
                    <el-option label="支票" value="CHECK"></el-option>
                    <el-option label="其他" value="OTHER"></el-option>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="bankName" label="银行名称" width="150">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.bankName" placeholder="请输入银行名称"></el-input>
                </template>
              </el-table-column>
              <el-table-column prop="bankAccount" label="银行账号" width="180">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.bankAccount" placeholder="请输入银行账号"></el-input>
                </template>
              </el-table-column>
              <el-table-column prop="accountName" label="账户名称" width="150">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.accountName" placeholder="请输入账户名称"></el-input>
                </template>
              </el-table-column>
              <el-table-column prop="paymentDate" label="支付日期" width="180">
                <template slot-scope="scope">
                  <el-date-picker v-model="scope.row.paymentDate" type="date" placeholder="选择日期" style="width: 100%"></el-date-picker>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template slot-scope="scope">
                  <el-button size="mini" type="danger" @click="handleRemovePayment(scope.$index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          
          <!-- 附件 -->
          <el-tab-pane label="附件" name="attachment">
            <el-upload
              ref="upload"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :data="uploadData"
              :file-list="fileList"
              :on-success="handleUploadSuccess"
              :on-remove="handleRemove"
              :before-upload="beforeUpload"
              multiple
            >
              <el-button size="small" type="primary">点击上传</el-button>
              <div slot="tip" class="el-upload__tip">支持多文件上传</div>
            </el-upload>
          </el-tab-pane>
        </el-tabs>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </div>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog title="报账单详情" :visible.sync="detailVisible" width="1200px">
      <div v-if="currentDetail">
        <el-tabs v-model="detailActiveTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="报账单号">{{ currentDetail.payoutBillcode }}</el-descriptions-item>
              <el-descriptions-item label="报账类型">{{ getPayoutTypeName(currentDetail.payoutTypeId) }}</el-descriptions-item>
              <el-descriptions-item label="报账金额">¥{{ currentDetail.applyAmount }}</el-descriptions-item>
              <el-descriptions-item label="报账日期">{{ formatDate(currentDetail.applyDate) }}</el-descriptions-item>
              <el-descriptions-item label="预算">{{ currentDetail.budgetName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusType(currentDetail.status)">{{ getStatusText(currentDetail.status) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="报账事由" :span="2">{{ currentDetail.applyReason }}</el-descriptions-item>
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
            </el-table>
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
import { getMyPayouts, deletePayout, submitPayout, withdrawPayout, getPayoutDetail, getPayoutApprovals, savePayoutFull, updatePayoutFull } from '@/api/reimb'
import { getBudgetSubjects, getBudgetItems, getBudgetsBySubjectAndItem } from '@/api/budg'
import { getAttachmentsByBusiness } from '@/api/attachment'
import { getCodeTypeOptions } from '@/utils/codeType'
import Cookies from 'js-cookie'

export default {
  name: 'MyReimbPayout',
  data() {
    return {
      loading: false,
      tableData: [],
      payoutTypeOptions: [],
      applyStatusOptions: [],
      budgetSubjects: [],
      budgetItems: [],
      budgets: [],
      budgetSubjectId: null,
      budgetItemId: null,
      attachments: [],
      approvals: [],
      currentDetails: [],
      currentInvoices: [],
      currentPayments: [],
      dialogVisible: false,
      detailVisible: false,
      dialogTitle: '新增报账',
      activeTab: 'basic',
      detailActiveTab: 'basic',
      isEdit: false,
      currentDetail: null,
      fileList: [],
      uploadUrl: '/api/attachment/upload',
      uploadHeaders: {
        Authorization: 'Bearer ' + Cookies.get('token')
      },
      uploadData: {
        businessType: 'PAYOUT'
      },
      form: {
        payout: {
          payoutId: null,
          billType: 'PAYOUT',
          payoutTypeId: '',
          applyAmount: 0,
          applyReason: '',
          budgetId: null,
          budgetItemId: null,
          empId: null,
          status: 'DRAFT'
        },
        details: [],
        invoices: [],
        payments: []
      },
      rules: {
        'payout.payoutTypeId': [{ required: true, message: '请选择报账类型', trigger: 'change' }],
        'payout.applyAmount': [{ required: true, message: '请输入报账金额', trigger: 'blur' }],
        'payout.applyReason': [{ required: true, message: '请输入报账事由', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadCodeTypeOptions()
    this.loadBudgetSubjects()
    this.loadData()
  },
  methods: {
    async loadCodeTypeOptions() {
      this.payoutTypeOptions = await getCodeTypeOptions('PAYOUT_TYPE')
      this.applyStatusOptions = await getCodeTypeOptions('APPLY_STATUS')
    },
    async loadBudgetSubjects() {
      try {
        const response = await getBudgetSubjects()
        if (response.code === 200) {
          this.budgetSubjects = response.data || []
        }
      } catch (error) {
        console.error('加载预算主体失败', error)
      }
    },
    async handleSubjectChange(subjectId) {
      this.budgetItemId = null
      this.form.payout.budgetId = null
      this.budgetItems = []
      this.budgets = []
      
      if (subjectId) {
        try {
          const response = await getBudgetItems()
          if (response.code === 200) {
            this.budgetItems = response.data || []
          }
        } catch (error) {
          console.error('加载预算项目失败', error)
        }
      }
    },
    async handleItemChange(itemId) {
      this.form.payout.budgetId = null
      this.budgets = []
      
      if (this.budgetSubjectId && itemId) {
        try {
          const response = await getBudgetsBySubjectAndItem(this.budgetSubjectId, itemId)
          if (response.code === 200) {
            this.budgets = response.data || []
          }
        } catch (error) {
          console.error('加载预算失败', error)
        }
      }
    },
    getPayoutTypeName(codeValue) {
      const option = this.payoutTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
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
    loadData() {
      this.loading = true
      const empId = this.$store.state.user.userInfo.empId || 1
      getMyPayouts(empId).then(response => {
        if (response.code === 200) {
          // 只显示报账单
          this.tableData = (response.data || []).filter(item => item.billType === 'PAYOUT')
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleAdd() {
      this.dialogTitle = '新增报账'
      this.isEdit = false
      this.activeTab = 'basic'
      this.fileList = []
      this.budgetSubjectId = null
      this.budgetItemId = null
      this.form = {
        payout: {
          payoutId: null,
          billType: 'PAYOUT',
          payoutTypeId: '',
          applyAmount: 0,
          applyReason: '',
          budgetId: null,
          budgetItemId: null,
          empId: this.$store.state.user.userInfo.empId || 1,
          status: 'DRAFT'
        },
        details: [],
        invoices: [],
        payments: []
      }
      this.uploadData.businessId = null
      this.dialogVisible = true
    },
    handleEdit(row) {
      if (row.status !== 'DRAFT' && row.status !== 'WITHDRAWN') {
        this.$message.warning('只有草稿或已撤回状态的报账单才能编辑')
        return
      }
      this.dialogTitle = '编辑报账'
      this.isEdit = true
      this.activeTab = 'basic'
      this.loadPayoutDetail(row.payoutId)
      this.dialogVisible = true
    },
    async loadPayoutDetail(payoutId) {
      try {
        const response = await getPayoutDetail(payoutId)
        if (response.code === 200) {
          this.form.payout = response.data.payout || {}
          this.form.details = response.data.details || []
          this.form.invoices = response.data.invoices || []
          this.form.payments = response.data.payments || []
          this.uploadData.businessId = payoutId
          this.loadAttachments(payoutId)
        }
      } catch (error) {
        this.$message.error('加载详情失败')
      }
    },
    async loadAttachments(payoutId) {
      if (!payoutId) return
      try {
        const response = await getAttachmentsByBusiness('PAYOUT', payoutId)
        if (response.code === 200) {
          this.fileList = (response.data || []).map(item => ({
            name: item.fileName,
            url: item.filePath,
            uid: item.attachmentId
          }))
        }
      } catch (error) {
        console.error('加载附件失败', error)
      }
    },
    handleAddDetail() {
      this.form.details.push({
        itemName: '',
        itemType: '',
        amount: 0,
        remark: ''
      })
    },
    handleRemoveDetail(index) {
      this.form.details.splice(index, 1)
    },
    handleAddInvoice() {
      this.form.invoices.push({
        invoiceCode: '',
        invoiceNumber: '',
        invoiceDate: null,
        invoiceAmount: 0,
        invoiceType: 'VAT',
        taxAmount: 0,
        attachmentId: null
      })
    },
    handleRemoveInvoice(index) {
      this.form.invoices.splice(index, 1)
    },
    handleInvoiceUploadSuccess(response, index) {
      if (response.code === 200) {
        this.form.invoices[index].attachmentId = response.data
        this.$message.success('上传成功')
      }
    },
    handleAddPayment() {
      this.form.payments.push({
        paymentAmount: 0,
        paymentObject: '',
        paymentMethod: 'BANK',
        bankName: '',
        bankAccount: '',
        accountName: '',
        paymentDate: null,
        remark: ''
      })
    },
    handleRemovePayment(index) {
      this.form.payments.splice(index, 1)
    },
    handleDelete(row) {
      if (row.status !== 'DRAFT') {
        this.$message.warning('只有草稿状态的报账单才能删除')
        return
      }
      this.$confirm('确认删除该报账单吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deletePayout(row.payoutId).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        })
      })
    },
    handleSubmit(row) {
      if (row.status !== 'DRAFT') {
        this.$message.warning('只有草稿状态的报账单才能提交')
        return
      }
      this.$confirm('确认提交该报账单吗？提交后将进入审批流程。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        submitPayout(row.payoutId).then(response => {
          if (response.code === 200) {
            this.$message.success('提交成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '提交失败')
          }
        })
      })
    },
    handleWithdraw(row) {
      if (row.status !== 'PENDING') {
        this.$message.warning('只有待审批状态的报账单才能撤回')
        return
      }
      this.$confirm('确认撤回该报账单吗？撤回后可以重新编辑。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        withdrawPayout(row.payoutId).then(response => {
          if (response.code === 200) {
            this.$message.success('撤回成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '撤回失败')
          }
        })
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
    handleSave() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const api = this.isEdit ? updatePayoutFull : savePayoutFull
          api(this.form).then(response => {
            if (response.code === 200) {
              // 如果是新增，需要更新附件的businessId
              if (!this.isEdit && this.fileList.length > 0) {
                // TODO: 更新附件的businessId为新的payoutId
              }
              this.$message.success(this.isEdit ? '更新成功' : '新增成功')
              this.dialogVisible = false
              this.loadData()
            } else {
              this.$message.error(response.message || '操作失败')
            }
          })
        }
      })
    },
    beforeUpload(file) {
      // 如果是新增，使用临时ID，保存后需要更新
      if (!this.form.payout.payoutId) {
        this.uploadData.businessId = 'temp'
      } else {
        this.uploadData.businessId = this.form.payout.payoutId
      }
      return true
    },
    handleUploadSuccess(response, file) {
      if (response.code === 200) {
        this.$message.success('上传成功')
      } else {
        this.$message.error(response.message || '上传失败')
      }
    },
    handleRemove(file, fileList) {
      this.fileList = fileList
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
.my-reimb-payout {
  padding: 20px;
}
</style>
