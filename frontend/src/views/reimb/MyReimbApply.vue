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

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="800px" @close="handleDialogCancel">
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
        <el-button @click="handleDialogCancel">取消</el-button>
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
            <el-table-column label="操作" width="150">
              <template slot-scope="scope">
                <el-button size="mini" type="text" @click="handlePreviewAttachment(scope.row)">预览</el-button>
                <el-button size="mini" type="text" @click="handleDownloadAttachment(scope.row)">下载</el-button>
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
import { getMyPayoutsPage, savePayout, updatePayout, deletePayout, submitPayout, withdrawPayout, getPayoutDetail, getPayoutApprovals } from '@/api/reimb'
import { getBudgetSubjects, getBudgetItems, getBudgetsBySubjectAndItem, checkBudgetAmount } from '@/api/budg'
import { getAttachmentsByBusiness, deleteAttachment, updateAttachmentBusinessId } from '@/api/attachment'
import { getCodeTypeOptions } from '@/utils/codeType'
import { paginationMixin } from '@/mixins/pagination'
import Cookies from 'js-cookie'

export default {
  name: 'MyReimbApply',
  mixins: [paginationMixin],
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
      uploadedAttachmentIds: [], // 记录新上传的附件ID，用于取消时删除
      uploadUrl: '/api/auth/attachment/upload',
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
      getMyPayoutsPage(empId, this.pagination.page, this.pagination.size).then(response => {
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
      this.uploadedAttachmentIds = [] // 重置上传附件ID列表
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
      this.uploadedAttachmentIds = [] // 重置上传附件ID列表
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
            uid: item.attachmentId,
            response: { data: item.attachmentId }, // 添加response以便handleRemove能识别
            filePath: item.filePath, // 保存filePath用于预览
            fileName: item.fileName // 保存fileName用于预览
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
            this.pagination.page = 1
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
            this.pagination.page = 1
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
            this.pagination.page = 1
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
              if (!this.isEdit && this.uploadedAttachmentIds.length > 0) {
                // 从响应中获取保存后的payoutId
                const payoutId = response.data?.payoutId || response.data?.id
                if (payoutId) {
                  // 更新附件的businessId
                  updateAttachmentBusinessId('PAYOUT', payoutId, this.uploadedAttachmentIds).then(() => {
                    console.log('附件businessId更新成功')
                  }).catch(error => {
                    console.error('更新附件businessId失败:', error)
                  })
                }
              }
              // 清空上传附件ID列表（已保存，不再需要删除）
              this.uploadedAttachmentIds = []
              this.$message.success(this.isEdit ? '更新成功' : '新增成功')
              this.dialogVisible = false
              this.pagination.page = 1
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
      console.log('附件上传成功响应:', response)
      // el-upload 的响应可能是包装后的格式
      let res = response
      if (typeof response === 'string') {
        try {
          res = JSON.parse(response)
        } catch (e) {
          console.error('解析响应JSON失败:', e)
          this.$message.error('上传失败：响应格式错误')
          return
        }
      }
      if (response && response.data && typeof response.data === 'object') {
        res = response.data
      }
      
      if (res && res.code === 200 && res.data) {
        this.$message.success('上传成功')
        // 如果是新增，记录附件ID，取消时删除
        if (!this.form.payoutId) {
          this.uploadedAttachmentIds.push(res.data)
        }
      } else {
        this.$message.error((res && res.message) || '上传失败')
      }
    },
    async handleRemove(file, fileList) {
      this.fileList = fileList
      // 如果删除的是新上传的文件，需要删除服务器上的文件
      let attachmentId = null
      if (file.response && file.response.data) {
        attachmentId = file.response.data
      } else if (file.uid && typeof file.uid === 'number') {
        // 可能是已保存的附件，uid就是attachmentId
        attachmentId = file.uid
      }
      
      if (attachmentId) {
        // 从列表中移除
        const index = this.uploadedAttachmentIds.indexOf(attachmentId)
        if (index > -1) {
          this.uploadedAttachmentIds.splice(index, 1)
          // 如果是未保存的新上传文件，删除服务器文件
          if (!this.form.payoutId) {
            try {
              await deleteAttachment(attachmentId)
              console.log('已删除附件:', attachmentId)
            } catch (error) {
              console.error('删除附件失败:', error)
            }
          }
        }
      }
    },
    async handleDialogCancel() {
      // 如果上传了附件但没有保存，需要删除这些附件
      if (this.uploadedAttachmentIds.length > 0) {
        // 批量删除未保存的附件
        for (const attachmentId of this.uploadedAttachmentIds) {
          try {
            await deleteAttachment(attachmentId)
            console.log('已删除未保存的附件:', attachmentId)
          } catch (error) {
            console.error('删除附件失败:', error)
            // 继续删除其他附件，不中断
          }
        }
        this.uploadedAttachmentIds = []
      }
      
      // 重置文件列表
      this.fileList = []
      
      // 关闭对话框
      this.dialogVisible = false
    },
    handlePreviewAttachment(attachment) {
      console.log('预览附件:', attachment)
      // 构建文件URL
      let fileUrl = attachment.filePath || attachment.url
      if (!fileUrl) {
        this.$message.error('附件路径不存在')
        return
      }
      
      console.log('原始文件路径:', fileUrl)
      
      // 如果filePath是相对路径，需要转换为完整URL
      if (!fileUrl.startsWith('http://') && !fileUrl.startsWith('https://')) {
        // filePath可能是完整路径，需要转换为访问URL
        // 例如：F:/data/uploads/attachments/PAYOUT/xxx.pdf
        // 需要转换为：/api/uploads/attachments/PAYOUT/xxx.pdf
        if (fileUrl.includes('/uploads/')) {
          const parts = fileUrl.split('/uploads/')
          fileUrl = '/api/uploads/' + parts[parts.length - 1]
        } else if (fileUrl.includes('\\uploads\\')) {
          const parts = fileUrl.split('\\uploads\\')
          fileUrl = '/api/uploads/' + parts[parts.length - 1].replace(/\\/g, '/')
        } else if (fileUrl.includes('attachments/')) {
          // 如果路径包含attachments/，说明可能是相对路径
          fileUrl = '/api/uploads/' + fileUrl.replace(/\\/g, '/')
        } else {
          // 其他情况，尝试作为相对路径处理
          fileUrl = '/api/uploads/' + fileUrl.replace(/\\/g, '/')
        }
      }
      
      console.log('转换后的文件URL:', fileUrl)
      
      // 判断文件类型，如果是图片，使用图片预览
      const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp']
      const fileName = attachment.fileName || attachment.name || ''
      const fileExt = fileName ? fileName.substring(fileName.lastIndexOf('.')).toLowerCase() : ''
      
      console.log('文件扩展名:', fileExt)
      
      if (imageExtensions.includes(fileExt)) {
        // 图片预览
        this.$alert(`<img src="${fileUrl}" style="max-width: 100%; max-height: 500px; display: block; margin: 0 auto;" onerror="this.style.display='none'; this.nextElementSibling.style.display='block';" /><p style="display:none; text-align:center; color:red;">图片加载失败，URL: ${fileUrl}</p>`, '图片预览', {
          dangerouslyUseHTMLString: true,
          showConfirmButton: true,
          confirmButtonText: '关闭',
          customClass: 'image-preview-dialog',
          width: '600px'
        })
      } else {
        // 其他文件类型，在新窗口打开
        console.log('打开文件:', fileUrl)
        window.open(fileUrl, '_blank')
      }
    },
    handleDownloadAttachment(attachment) {
      // 构建文件URL
      let fileUrl = attachment.filePath || attachment.url
      if (!fileUrl) {
        this.$message.error('附件路径不存在')
        return
      }
      
      // 如果filePath是相对路径，需要转换为完整URL
      if (!fileUrl.startsWith('http://') && !fileUrl.startsWith('https://')) {
        // filePath可能是完整路径，需要转换为访问URL
        if (fileUrl.includes('/uploads/')) {
          const parts = fileUrl.split('/uploads/')
          fileUrl = '/api/uploads/' + parts[parts.length - 1]
        } else if (fileUrl.includes('\\uploads\\')) {
          const parts = fileUrl.split('\\uploads\\')
          fileUrl = '/api/uploads/' + parts[parts.length - 1].replace(/\\/g, '/')
        } else {
          fileUrl = '/api/uploads/' + fileUrl.replace(/\\/g, '/')
        }
      }
      window.open(fileUrl, '_blank')
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
