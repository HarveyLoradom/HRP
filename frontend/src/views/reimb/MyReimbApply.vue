<template>
  <div class="my-reimb-apply">
    <el-card>
      <div slot="header" class="clearfix">
        <span>我的申请</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增申请</el-button>
      </div>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="payoutBillcode" label="申请单号" width="150"></el-table-column>
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
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="800px">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="申请类型" prop="payoutTypeId">
              <el-select v-model="form.payoutTypeId" placeholder="请选择申请类型" style="width: 100%">
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
            <el-form-item label="申请金额" prop="applyAmount">
              <el-input-number v-model="form.applyAmount" :min="0" :precision="2" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="预算主体" prop="budgetSubjectId">
          <el-select v-model="form.budgetSubjectId" placeholder="请选择预算主体" filterable style="width: 100%" @change="handleSubjectChange">
            <el-option
              v-for="subject in budgetSubjects"
              :key="subject.subjectId"
              :label="subject.subjectName"
              :value="subject.subjectId"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="预算项目" prop="budgetItemId">
          <el-select v-model="form.budgetItemId" placeholder="请先选择预算主体" filterable style="width: 100%" :disabled="!form.budgetSubjectId" @change="handleItemChange">
            <el-option
              v-for="item in budgetItems"
              :key="item.itemId"
              :label="item.itemName"
              :value="item.itemId"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="预算" prop="budgetId">
          <el-select v-model="form.budgetId" placeholder="请先选择预算主体和预算项目" filterable style="width: 100%" :disabled="!form.budgetSubjectId || !form.budgetItemId || budgets.length === 0" @change="handleBudgetChange">
            <el-option
              v-for="budget in budgets"
              :key="budget.budgetId"
              :label="`${budget.budgetName || budget.budgetNo} (剩余: ¥${budget.remainingAmount || 0})`"
              :value="budget.budgetId"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="申请事由" prop="applyReason">
          <el-input type="textarea" v-model="form.applyReason" :rows="4"></el-input>
        </el-form-item>
        
        <el-form-item label="附件">
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
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </div>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog title="申请单详情" :visible.sync="detailVisible" width="900px">
      <div v-if="currentDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请单号">{{ currentDetail.payoutBillcode }}</el-descriptions-item>
          <el-descriptions-item label="申请类型">{{ getPayoutTypeName(currentDetail.payoutTypeId) }}</el-descriptions-item>
          <el-descriptions-item label="申请金额">¥{{ currentDetail.applyAmount }}</el-descriptions-item>
          <el-descriptions-item label="申请日期">{{ formatDate(currentDetail.applyDate) }}</el-descriptions-item>
          <el-descriptions-item label="预算">{{ currentDetail.budgetName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentDetail.status)">{{ getStatusText(currentDetail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请事由" :span="2">{{ currentDetail.applyReason }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 附件列表 -->
        <el-divider content-position="left">附件</el-divider>
        <div v-if="attachments.length > 0">
          <el-table :data="attachments" border size="small">
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
        </div>
        <div v-else style="color: #999; text-align: center; padding: 20px;">暂无附件</div>
        
        <!-- 审批记录 -->
        <el-divider content-position="left">审批记录</el-divider>
        <el-timeline v-if="approvals.length > 0">
          <el-timeline-item
            v-for="approval in approvals"
            :key="approval.approvalId"
            :timestamp="formatDate(approval.approvalTime)"
            placement="top"
          >
            <el-card>
              <h4>{{ approval.approverName }} ({{ approval.approvalType === 'APPROVE' ? '通过' : '驳回' }})</h4>
              <p v-if="approval.approvalOpinion">{{ approval.approvalOpinion }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <div v-else style="color: #999; text-align: center; padding: 20px;">暂无审批记录</div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMyPayouts, savePayout, updatePayout, deletePayout, submitPayout, withdrawPayout, getPayoutDetail, getPayoutApprovals } from '@/api/reimb'
import { getBudgetSubjects, getBudgetItems, getBudgetsBySubjectAndItem, checkBudgetAmount } from '@/api/budg'
import { getAttachmentsByBusiness } from '@/api/attachment'
import { getCodeTypeOptions } from '@/utils/codeType'
import Cookies from 'js-cookie'

export default {
  name: 'MyReimbApply',
  data() {
    return {
      loading: false,
      tableData: [],
      payoutTypeOptions: [],
      applyStatusOptions: [],
      budgetSubjects: [],
      budgetItems: [],
      budgets: [],
      attachments: [],
      approvals: [],
      dialogVisible: false,
      detailVisible: false,
      dialogTitle: '新增申请',
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
        payoutId: null,
        billType: 'APPLY',
        payoutTypeId: '',
        applyAmount: 0,
        applyReason: '',
        budgetSubjectId: null,
        budgetItemId: null,
        budgetId: null,
        empId: null,
        status: 'DRAFT'
      },
      rules: {
        payoutTypeId: [{ required: true, message: '请选择申请类型', trigger: 'change' }],
        applyAmount: [{ required: true, message: '请输入申请金额', trigger: 'blur' }],
        applyReason: [{ required: true, message: '请输入申请事由', trigger: 'blur' }]
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
      this.form.budgetItemId = null
      this.form.budgetId = null
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
      this.form.budgetId = null
      this.budgets = []
      
      if (this.form.budgetSubjectId && itemId) {
        try {
          const response = await getBudgetsBySubjectAndItem(this.form.budgetSubjectId, itemId)
          if (response.code === 200) {
            this.budgets = response.data || []
          }
        } catch (error) {
          console.error('加载预算失败', error)
        }
      }
    },
    async handleBudgetChange(budgetId) {
      if (budgetId && this.form.applyAmount) {
        try {
          const response = await checkBudgetAmount(budgetId, this.form.applyAmount)
          if (response.code === 200 && !response.data) {
            this.$message.warning('预算额度不足，请检查')
          }
        } catch (error) {
          console.error('检查预算额度失败', error)
        }
      }
    },
    getPayoutTypeName(codeValue) {
      const option = this.payoutTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    loadData() {
      this.loading = true
      const empId = this.$store.state.user.userInfo.empId || 1
      getMyPayouts(empId).then(response => {
        if (response.code === 200) {
          // 只显示申请单
          this.tableData = (response.data || []).filter(item => item.billType === 'APPLY' || !item.billType)
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleAdd() {
      this.dialogTitle = '新增申请'
      this.isEdit = false
      this.fileList = []
      this.form = {
        payoutId: null,
        billType: 'APPLY',
        payoutTypeId: '',
        applyAmount: 0,
        applyReason: '',
        budgetSubjectId: null,
        budgetItemId: null,
        budgetId: null,
        empId: this.$store.state.user.userInfo.empId || 1,
        status: 'DRAFT'
      }
      this.uploadData.businessId = null
      this.dialogVisible = true
    },
    handleEdit(row) {
      if (row.status !== 'DRAFT' && row.status !== 'WITHDRAWN') {
        this.$message.warning('只有草稿或已撤回状态的申请单才能编辑')
        return
      }
      this.dialogTitle = '编辑申请'
      this.isEdit = true
      this.form = { ...row }
      this.uploadData.businessId = row.payoutId
      this.loadAttachments(row.payoutId)
      this.dialogVisible = true
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
    handleDelete(row) {
      if (row.status !== 'DRAFT') {
        this.$message.warning('只有草稿状态的申请单才能删除')
        return
      }
      this.$confirm('确认删除该申请吗？', '提示', {
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
        this.$message.warning('只有草稿状态的申请单才能提交')
        return
      }
      this.$confirm('确认提交该申请吗？提交后将进入审批流程。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        submitPayout(row.payoutId).then(response => {
          if (response.code === 200) {
            const message = response.message || '提交成功'
            this.$message.success(message)
            this.loadData()
          } else {
            this.$message.error(response.message || '提交失败')
          }
        })
      })
    },
    handleWithdraw(row) {
      if (row.status !== 'PENDING') {
        this.$message.warning('只有待审批状态的申请单才能撤回')
        return
      }
      this.$confirm('确认撤回该申请吗？撤回后可以重新编辑。', '提示', {
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
          this.attachments = []
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
        
        this.detailVisible = true
      } catch (error) {
        this.$message.error('加载详情失败')
      }
    },
    handleSave() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const api = this.isEdit ? updatePayout : savePayout
          api(this.form).then(response => {
            if (response.code === 200) {
              // 如果是新增，需要更新附件的businessId
              if (!this.isEdit && this.fileList.length > 0) {
                // TODO: 更新附件的businessId为新的payoutId
                // 这里需要获取保存后的payoutId，可以通过返回数据或重新查询获取
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
      if (!this.form.payoutId) {
        this.uploadData.businessId = 'temp'
      } else {
        this.uploadData.businessId = this.form.payoutId
      }
      return true
    },
    handleUploadSuccess(response, file) {
      if (response.code === 200) {
        this.$message.success('上传成功')
        // 如果是新增，保存后需要更新附件关联的businessId
        if (!this.form.payoutId && response.data) {
          // 保存附件ID，在保存主表后更新
        }
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
.my-reimb-apply {
  padding: 20px;
}
</style>
