<template>
  <el-dialog 
    title="合同详情" 
    :visible.sync="visible" 
    width="1200px"
    @close="handleClose"
  >
    <el-tabs v-model="detailActiveTab" v-if="detailData">
      <el-tab-pane label="基本信息" name="basic">
        <el-form :model="detailData" label-width="140px">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="申请人:">
                <span>{{ detailData.empName || detailData.empId || '-' }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="科室:">
                <span>{{ detailData.deptName || detailData.deptId || '-' }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="手机号:">
                <span>{{ detailData.empPhone || '-' }}</span>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="合同编号:">
                <span>{{ detailData.contractNo || '-' }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="合同名称:">
                <span>{{ detailData.contractName || '-' }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="合同类型:">
                <span>{{ getContractTypeName(detailData.contractType) }}</span>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="甲方:">
                <span>{{ detailData.partyA || '-' }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="乙方:">
                <span>{{ detailData.partyB || '-' }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="合同金额:">
                <span>¥{{ detailData.contractAmount || 0 }}</span>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="签订日期:">
                <span>{{ formatDateTime(detailData.signDate) }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="开始日期:">
                <span>{{ formatDateTime(detailData.startDate) }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="结束日期:">
                <span>{{ formatDateTime(detailData.endDate) }}</span>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="状态:">
                <el-tag :type="getStatusType(detailData.status)">
                  {{ getStatusText(detailData.status) }}
                </el-tag>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="执行状态:">
                <el-tag :type="getExecutionStatusType(detailData.executionStatus)">
                  {{ getExecutionStatusName(detailData.executionStatus) }}
                </el-tag>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="备注:">
            <span>{{ detailData.remark || '-' }}</span>
          </el-form-item>
          <el-form-item label="附件:">
            <el-button size="small" @click="handlePreviewAttachments">查看附件</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      
      <el-tab-pane label="流程记录" name="process">
        <el-table :data="filteredCompletedNodes" border style="width: 100%; margin-bottom: 20px;" v-if="filteredCompletedNodes && filteredCompletedNodes.length > 0">
          <el-table-column prop="taskName" label="节点名称" width="180"></el-table-column>
          <el-table-column prop="assigneeUserName" label="审批人" width="120">
            <template slot-scope="scope">
              {{ scope.row ? (scope.row.assigneeUserName || '-') : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="taskStatus" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag v-if="scope.row && scope.row.taskStatus" :type="getTaskStatusTypeForRecord(scope.row.taskStatus)" size="small">
                {{ getTaskStatusNameForRecord(scope.row.taskStatus) }}
              </el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="comment" label="审批意见/退回意见" min-width="200">
            <template slot-scope="scope">
              <div v-if="scope.row.taskStatus === 'RETURNED'">
                <div v-if="scope.row.comment">
                  <strong>退回意见：</strong>{{ scope.row.comment }}
                </div>
                <div v-else style="color: #999;">无退回意见</div>
              </div>
              <div v-else>
                {{ scope.row.comment || '-' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="completeTime" label="完成时间" width="160">
            <template slot-scope="scope">
              {{ formatDateTime(scope.row.completeTime || scope.row.updateTime) }}
            </template>
          </el-table-column>
        </el-table>
        <div v-else style="text-align: center; color: #999; padding: 20px;">
          暂无流程记录（仅显示有审批意见的记录）
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <div slot="footer" class="dialog-footer">
      <!-- 工作台/起草页面的按钮 -->
      <template v-if=" sourceType === 'draft'">
        <el-button v-if="detailData && detailData.status === 'APPROVED'" type="primary" icon="el-icon-printer" @click="handlePrint">打印</el-button>
        <el-button v-if="detailData && (detailData.status === 'DRAFT' || detailData.status === 'WITHDRAWN' || detailData.status === 'REJECTED')" type="primary" @click="handleEdit">编辑</el-button>
        <el-button v-if="detailData && (detailData.status === 'DRAFT' || detailData.status === 'WITHDRAWN' || detailData.status === 'REJECTED')" type="success" @click="handleSubmit">提交</el-button>
        <el-button v-if="detailData && detailData.status === 'PENDING'" type="danger" @click="handleWithdraw">撤回</el-button>
        <el-button v-if="detailData && (detailData.status === 'DRAFT' || detailData.status === 'WITHDRAWN' || detailData.status === 'REJECTED')" type="danger" @click="handleDelete">删除</el-button>
      </template>
      
      <!-- 审批页面的按钮 -->
      <template v-if="sourceType === 'approval'">
        <el-button type="success" @click="handleApprove">同意</el-button>
        <el-button type="danger" @click="handleReject">拒绝/退回</el-button>
        <el-button 
          v-if="currentTask && currentTask.allowAddsign === 1" 
          type="warning" 
          @click="handleAddSign"
        >加签</el-button>
        <el-button 
          v-if="currentTask && currentTask.allowTransfer === 1" 
          type="info" 
          @click="handleTransfer"
        >转签</el-button>
        <el-button v-if="detailData && detailData.status === 'APPROVED'" type="primary" icon="el-icon-printer" @click="handlePrint">打印</el-button>
      </template>
      
      <!-- 执行页面的按钮 -->
      <el-button @click="handleClose">关闭</el-button>
    </div>

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

    <!-- 加签对话框 -->
    <el-dialog title="加签" :visible.sync="addSignDialogVisible" width="500px" :modal="false" :append-to-body="true">
      <el-form :model="addSignForm" label-width="120px">
        <el-form-item label="加签人员:">
          <el-select 
            v-model="addSignForm.userId" 
            placeholder="请选择加签人员" 
            filterable 
            style="width: 100%;"
            @change="handleAddSignUserChange"
          >
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="`${user.name}(${user.account})`"
              :value="user.id"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addSignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmAddSign">确认加签</el-button>
      </div>
    </el-dialog>

    <!-- 转签对话框 -->
    <el-dialog title="转签" :visible.sync="transferDialogVisible" width="500px" :modal="false" :append-to-body="true">
      <el-form :model="transferForm" label-width="120px">
        <el-form-item label="转签给:">
          <el-select 
            v-model="transferForm.userId" 
            placeholder="请选择转签人员" 
            filterable 
            style="width: 100%;"
            @change="handleTransferUserChange"
          >
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="`${user.name}(${user.account})`"
              :value="user.id"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="transferDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmTransfer">确认转签</el-button>
      </div>
    </el-dialog>

    <!-- 附件预览对话框 -->
    <el-dialog title="附件列表" :visible.sync="attachmentPreviewVisible" width="800px" :modal="false" :append-to-body="true">
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
    </el-dialog>

    <!-- 图片预览对话框 -->
    <el-dialog title="图片预览" :visible.sync="imagePreviewVisible" width="800px" center :modal="false" :append-to-body="true">
      <div style="text-align: center;">
        <img :src="previewImageUrl" style="max-width: 100%; max-height: 600px;" @error="handleImageError" />
      </div>
    </el-dialog>
  </el-dialog>
</template>

<script>
import { getContractById, withdrawContract, deleteContract, submitContract, approveContract, rejectContract, returnContract } from '@/api/contract'
import { getProcessTaskByTaskKey, getProcessTaskByBusinessKey, getNextNodeInfoByBusinessKey, transferProcessTask } from '@/api/process'
import { getAttachmentsByBusinessId, getAttachment } from '@/api/attachment'
import { getUserList } from '@/api/user'
import axios from 'axios'
import Cookies from 'js-cookie'
import { getCodeTypeOptions } from '@/utils/codeType'
import { generatePrintContent, getDefaultPrintTemplate } from '@/api/print'
import { getTemplateConfigById } from '@/api/templateConfig'
import ApprovalConfirmDialog from '@/components/ApprovalConfirmDialog.vue'
import RejectReturnDialog from '@/components/RejectReturnDialog.vue'
import request from '@/api/request'

export default {
  name: 'ContractDetail',
  components: {
    ApprovalConfirmDialog,
    RejectReturnDialog
  },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    sourceType: {
      type: String,
      required: true,
      validator: value => ['workbench', 'draft', 'approval', 'execution', 'query'].includes(value)
    },
    contractId: {
      type: [Number, String],
      required: false,
      default: null
    }
  },
  data() {
    return {
      detailActiveTab: 'basic',
      detailData: null,
      completedNodes: [],
      attachments: [],
      currentTask: null,
      userList: [],
      contractTypeOptions: [],
      executionStatusOptions: [],
      applyStatusOptions: [],
      
      approvalDialogVisible: false,
      returnDialogVisible: false,
      addSignDialogVisible: false,
      transferDialogVisible: false,
      attachmentPreviewVisible: false,
      imagePreviewVisible: false,
      previewImageUrl: '',
      
      nextNodeInfo: null,
      addSignForm: {
        userId: '',
        userName: '',
        userCode: ''
      },
      transferForm: {
        userId: '',
        userName: '',
        userCode: ''
      }
    }
  },
  computed: {
    visible: {
      get() {
        return this.value
      },
      set(val) {
        this.$emit('input', val)
      }
    },
    filteredCompletedNodes() {
      if (!this.completedNodes || this.completedNodes.length === 0) {
        return []
      }
      return this.completedNodes.filter(node => {
        const comment = node.comment
        return comment && comment.trim() !== '' && comment.trim() !== '-'
      })
    }
  },
  watch: {
    visible(val) {
      if (val && this.contractId) {
        this.loadDetail()
      }
    }
  },
  mounted() {
    this.loadCodeTypeOptions()
    this.loadUserList()
    if (this.visible && this.contractId) {
      this.loadDetail()
    }
  },
  methods: {
    async loadCodeTypeOptions() {
      this.contractTypeOptions = await getCodeTypeOptions('CONTRACT_TYPE')
      this.executionStatusOptions = await getCodeTypeOptions('EXECUTION_STATUS')
      this.applyStatusOptions = await getCodeTypeOptions('APPLY_STATUS')
    },
    async loadUserList() {
      try {
        const response = await getUserList(0)
        if (response.code === 200 && response.data) {
          this.userList = response.data
        }
      } catch (error) {
        console.error('加载用户列表失败:', error)
      }
    },
    async loadDetail() {
      try {
        const response = await getContractById(this.contractId)
        if (response.code === 200 && response.data) {
          this.detailData = response.data
          if (this.detailData.contractNo) {
            await this.loadCompletedNodes(this.detailData.contractNo)
            if (this.sourceType === 'approval') {
              await this.loadCurrentTask(this.detailData.contractNo)
            }
          }
        }
      } catch (error) {
        this.$message.error('加载详情失败：' + (error.message || '未知错误'))
      }
    },
    async loadCompletedNodes(contractNo) {
      try {
        const response = await getProcessTaskByTaskKey(contractNo)
        if (response.code === 200 && response.data && response.data.length > 0) {
          const completedTasks = response.data.filter(task => 
            (task.taskStatus === 'COMPLETED' || task.taskStatus === 'RETURNED') &&
            task.comment != null && task.comment.trim() !== ''
          )
          completedTasks.sort((a, b) => {
            const aOrder = a.printOrder != null ? a.printOrder : 999999
            const bOrder = b.printOrder != null ? b.printOrder : 999999
            if (aOrder !== bOrder) {
              return aOrder - bOrder
            }
            const aTime = a.completeTime || a.updateTime
            const bTime = b.completeTime || b.updateTime
            if (aTime && bTime) {
              return new Date(aTime) - new Date(bTime)
            }
            return 0
          })
          this.completedNodes = completedTasks
        }
      } catch (error) {
        console.error('加载流程记录失败:', error)
      }
    },
    async loadCurrentTask(contractNo) {
      try {
        const response = await getProcessTaskByBusinessKey(contractNo)
        if (response.code === 200 && response.data && response.data.length > 0) {
          const userInfo = this.$store.state.user.userInfo || {}
          const currentUserId = userInfo.userId || userInfo.id
          const pendingTasks = response.data.filter(task => task.taskStatus === 'PENDING')
          
          if (pendingTasks.length > 0) {
            pendingTasks.sort((a, b) => {
              const aOrder = a.printOrder != null ? a.printOrder : 999999
              const bOrder = b.printOrder != null ? b.printOrder : 999999
              return aOrder - bOrder
            })
            this.currentTask = pendingTasks.find(task => task.assigneeUserId === currentUserId)
          }
        }
      } catch (error) {
        console.error('加载当前任务失败:', error)
      }
    },
    async handlePreviewAttachments() {
      try {
        const businessId = this.detailData.mainAttachId || this.detailData.contractNo
        if (!businessId) {
          this.$message.warning('无法获取附件标识')
          return
        }
        const response = await getAttachmentsByBusinessId(businessId)
        if (response.code === 200 && response.data) {
          this.attachments = response.data || []
          if (this.attachments.length === 0) {
            this.$message.info('暂无附件')
            return
          }
          this.attachmentPreviewVisible = true
        }
      } catch (error) {
        this.$message.error('加载附件失败：' + (error.message || '未知错误'))
      }
    },
    async handlePreviewAttachment(attachment) {
      // 预览功能：图片弹窗预览；其他文件新窗口打开（图2效果）
      let filePath = attachment.filePath || attachment.url
      if (!filePath && attachment.attachmentId) {
        try {
          const response = await getAttachment(attachment.attachmentId)
          if (response.code === 200 && response.data) {
            filePath = response.data.filePath
          }
        } catch (error) {
          this.$message.error('获取附件信息失败')
          return
        }
      }
      
      if (!filePath) {
        this.$message.error('附件路径不存在')
        return
      }
      
      const fileName = attachment.fileName || attachment.name || ''
      const fileUrl = this.getFileUrl(filePath, false)

      if (this.isImageFile(fileName, fileUrl)) {
        this.previewImageUrl = fileUrl
        this.imagePreviewVisible = true
        return
      }

      const newWin = window.open(fileUrl, '_blank')
      if (!newWin) {
        this.$message.warning('浏览器拦截了新窗口，请允许弹窗后重试')
      }
    },
    getFileUrl(filePath, forDownload = false) {
      if (!filePath) return ''
      if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
        return filePath
      }
      if (filePath.startsWith('data:')) {
        return filePath
      }
      
      // 构建文件URL
      let url = ''
      if (filePath.includes('/uploads/')) {
        const parts = filePath.split('/uploads/')
        url = '/api/uploads/' + parts[parts.length - 1]
      } else if (filePath.includes('\\uploads\\')) {
        const parts = filePath.split('\\uploads\\')
        url = '/api/uploads/' + parts[parts.length - 1].replace(/\\/g, '/')
      } else if (filePath.includes('attachments/')) {
        const parts = filePath.split('attachments/')
        url = '/api/uploads/attachments/' + parts[parts.length - 1].replace(/\\/g, '/')
      } else if (filePath.includes('attachments\\')) {
        const parts = filePath.split('attachments\\')
        url = '/api/uploads/attachments/' + parts[parts.length - 1].replace(/\\/g, '/')
      } else {
        url = '/api/uploads/' + filePath.replace(/\\/g, '/')
      }
      
      // 添加时间戳防止缓存（仅用于预览）
      if (!forDownload && url) {
        url += (url.includes('?') ? '&' : '?') + '_t=' + new Date().getTime()
      }
      
      return url
    },
    isImageFile(fileName, fileUrl) {
      const name = (fileName || '').toLowerCase()
      const url = (fileUrl || '').toLowerCase()
      return (
        url.startsWith('data:image/') ||
        /\.(jpg|jpeg|png|gif|bmp|webp)(\?|#|$)/i.test(name) ||
        /\.(jpg|jpeg|png|gif|bmp|webp)(\?|#|$)/i.test(url)
      )
    },
    async handleDownloadAttachment(attachment) {
      // 下载功能：下载文件到本地（真实下载）
      let filePath = attachment.filePath || attachment.url
      if (!filePath && attachment.attachmentId) {
        try {
          const response = await getAttachment(attachment.attachmentId)
          if (response.code === 200 && response.data) {
            filePath = response.data.filePath
          }
        } catch (error) {
          this.$message.error('获取附件信息失败')
          return
        }
      }
      
      if (!filePath) {
        this.$message.error('附件路径不存在')
        return
      }
      
      // 构建下载URL（不加时间戳）
      const fileUrl = this.getFileUrl(filePath, true)
      const fileName = attachment.fileName || attachment.name || 'download'
      
      // 使用axios下载文件（带认证）
      try {
        const token = Cookies.get('token')
        const response = await axios({
          url: fileUrl,
          method: 'GET',
          responseType: 'blob',
          headers: {
            'Authorization': token ? 'Bearer ' + token : ''
          }
        })
        
        // 创建blob URL并下载
        const blob = new Blob([response.data])
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', fileName)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        this.$message.success('下载成功')
      } catch (error) {
        console.error('下载失败', error)
        this.$message.error('下载失败：' + (error.message || '未知错误'))
      }
    },
    handleImageError() {
      this.$message.error('图片加载失败')
      this.imagePreviewVisible = false
    },
    handleEdit() {
      this.$emit('edit', this.detailData)
      this.handleClose()
    },
    async handleSubmit() {
      if (!this.detailData || !this.detailData.pactId) {
        this.$message.warning('无法获取合同信息')
        return
      }
      
      if (this.detailData.status !== 'DRAFT' && this.detailData.status !== 'WITHDRAWN' && this.detailData.status !== 'REJECTED') {
        this.$message.warning('只有草稿、撤回或拒绝状态的合同才能提交')
        return
      }
      
      try {
        const response = await submitContract(this.detailData.pactId)
        if (response.code === 200) {
          this.$message.success('提交成功')
          this.$emit('submitted')
          this.handleClose()
        } else {
          this.$message.error(response.message || '提交失败')
        }
      } catch (error) {
        this.$message.error('提交失败：' + (error.message || '未知错误'))
      }
    },
    async handleWithdraw() {
      this.$confirm('确认撤回该合同吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await withdrawContract(this.detailData.pactId)
          if (response.code === 200) {
            this.$message.success('撤回成功')
            this.$emit('withdrawn')
            this.handleClose()
          } else {
            this.$message.error(response.message || '撤回失败')
          }
        } catch (error) {
          this.$message.error('撤回失败：' + (error.message || '未知错误'))
        }
      }).catch(() => {})
    },
    async handleDelete() {
      if (!this.detailData || !this.detailData.pactId) {
        this.$message.warning('无法获取合同信息')
        return
      }
      
      if (this.detailData.status !== 'DRAFT' && this.detailData.status !== 'WITHDRAWN' && this.detailData.status !== 'REJECTED') {
        this.$message.warning('只有草稿、撤回或拒绝状态的合同才能删除')
        return
      }
      
      this.$confirm('确认删除该合同吗？删除后无法恢复！', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await deleteContract(this.detailData.pactId)
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.$emit('deleted')
            this.handleClose()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        } catch (error) {
          this.$message.error('删除失败：' + (error.message || '未知错误'))
        }
      }).catch(() => {})
    },
    async handleApprove() {
      try {
        const response = await getNextNodeInfoByBusinessKey(this.detailData.contractNo)
        if (response.code === 200 && response.data) {
          this.nextNodeInfo = response.data
        } else {
          this.nextNodeInfo = null
        }
      } catch (error) {
        this.nextNodeInfo = null
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
        
        const userId = this.$store.state.user.userInfo.userId || this.$store.state.user.userInfo.id
        const response = await approveContract(this.detailData.pactId, userId, opinion, signature)
        if (response.code === 200) {
          this.$message.success('审批通过')
          // 先触发事件，再关闭对话框，确保事件能正确传递
          this.$emit('approved')
          this.$nextTick(() => {
            this.handleClose()
          })
        } else {
          this.$message.error(response.message || '审批失败')
        }
      } catch (error) {
        this.$message.error('审批失败：' + (error.message || '未知错误'))
      } finally {
        this.approvalDialogVisible = false
      }
    },
    handleCloseApprovalDialog() {
      this.approvalDialogVisible = false
      this.nextNodeInfo = null
    },
    handleReject() {
      this.returnDialogVisible = true
    },
    async handleConfirmReturn(data) {
      try {
        const returnType = data.returnType || 'RETURN_TO_CURRENT'
        const opinion = data.opinion || ''
        const response = await returnContract(this.detailData.pactId, returnType, opinion)
        if (response.code === 200) {
          this.$message.success('退回成功')
          this.returnDialogVisible = false
          // 先触发事件，再关闭对话框，确保事件能正确传递
          this.$emit('returned')
          this.$nextTick(() => {
            this.handleClose()
          })
        } else {
          this.$message.error(response.message || '退回失败')
        }
      } catch (error) {
        this.$message.error('退回失败：' + (error.message || '未知错误'))
      } finally {
        this.returnDialogVisible = false
      }
    },
    handleCloseReturnDialog() {
      this.returnDialogVisible = false
    },
    handleAddSign() {
      if (!this.currentTask) {
        this.$message.warning('无法获取任务信息')
        return
      }
      this.addSignForm = {
        userId: '',
        userName: '',
        userCode: ''
      }
      this.addSignDialogVisible = true
    },
    handleAddSignUserChange(userId) {
      const user = this.userList.find(u => u.id === userId)
      if (user) {
        this.addSignForm.userName = user.name
        this.addSignForm.userCode = user.account
      }
    },
    async handleConfirmAddSign() {
      if (!this.addSignForm.userId) {
        this.$message.warning('请选择被加签人')
        return
      }
      try {
        const taskName = this.currentTask.taskName || '审批'
        const response = await request.post('/auth/process-task/add-sign', {
          taskId: this.currentTask.taskId,
          newAssigneeUserId: this.addSignForm.userId,
          newAssigneeUserName: this.addSignForm.userName,
          newAssigneeEmpCode: this.addSignForm.userCode,
          taskName: taskName + '（加签）'
        })
        if (response.code === 200) {
          this.$message.success('加签成功')
          this.addSignDialogVisible = false
          // 先触发事件，再关闭对话框，确保事件能正确传递
          this.$emit('add-signed')
          this.$nextTick(() => {
            this.handleClose()
          })
        } else {
          this.$message.error(response.message || '加签失败')
        }
      } catch (error) {
        this.$message.error('加签失败：' + (error.message || '未知错误'))
      }
    },
    handleTransfer() {
      if (!this.currentTask) {
        this.$message.warning('无法获取任务信息')
        return
      }
      this.transferForm = {
        userId: '',
        userName: '',
        userCode: ''
      }
      this.transferDialogVisible = true
    },
    handleTransferUserChange(userId) {
      const user = this.userList.find(u => u.id === userId)
      if (user) {
        this.transferForm.userName = user.name
        this.transferForm.userCode = user.account
      }
    },
    async handleConfirmTransfer() {
      if (!this.transferForm.userId) {
        this.$message.warning('请选择转签人员')
        return
      }
      try {
        const response = await transferProcessTask({
          taskId: this.currentTask.taskId,
          newAssigneeUserId: this.transferForm.userId,
          newAssigneeUserName: this.transferForm.userName,
          newAssigneeEmpCode: this.transferForm.userCode
        })
        if (response.code === 200) {
          this.$message.success('转签成功')
          this.transferDialogVisible = false
          // 先触发事件，再关闭对话框，确保事件能正确传递
          this.$emit('transferred')
          this.$nextTick(() => {
            this.handleClose()
          })
        } else {
          this.$message.error(response.message || '转签失败')
        }
      } catch (error) {
        this.$message.error('转签失败：' + (error.message || '未知错误'))
      }
    },
    async handlePrint() {
      if (!this.detailData || !this.detailData.contractNo) {
        this.$message.warning('无法获取合同编号')
        return
      }
      
      let templateId = null
      if (this.detailData.printTemplateId) {
        templateId = this.detailData.printTemplateId
      } else {
        try {
          if (this.detailData.templateConfigId) {
            const configResponse = await getTemplateConfigById(this.detailData.templateConfigId)
            if (configResponse.code === 200 && configResponse.data && configResponse.data.printTemplateId) {
              templateId = configResponse.data.printTemplateId
            }
          }
          if (!templateId) {
            const defaultTemplateResponse = await getDefaultPrintTemplate('CONTRACT')
            if (defaultTemplateResponse.code === 200 && defaultTemplateResponse.data) {
              templateId = defaultTemplateResponse.data.templateId
            }
          }
        } catch (error) {
          console.error('获取打印模板失败:', error)
        }
      }
      
      if (!templateId) {
        this.$message.warning('未找到打印模板，请先配置打印模板')
        return
      }
      
      try {
        this.$message.info('正在生成打印内容...')
        const response = await generatePrintContent({
          templateId: templateId,
          businessKey: this.detailData.contractNo,
          templateType: 'CONTRACT'
        })
        
        if (response.code === 200 && response.data) {
          let htmlContent = typeof response.data === 'string' ? response.data : String(response.data)
          if (!htmlContent.includes('<!DOCTYPE') && !htmlContent.includes('<html')) {
            htmlContent = '<!DOCTYPE html>\n<html>\n<head>\n<meta charset="UTF-8">\n<title>打印</title>\n</head>\n<body>\n' + htmlContent + '\n</body>\n</html>'
          }
          
          const iframe = document.createElement('iframe')
          iframe.style.position = 'fixed'
          iframe.style.right = '0'
          iframe.style.bottom = '0'
          iframe.style.width = '0'
          iframe.style.height = '0'
          iframe.style.border = '0'
          document.body.appendChild(iframe)
          
          let hasPrinted = false
          const removeIframe = () => {
            try {
              if (iframe && iframe.parentNode) {
                iframe.parentNode.removeChild(iframe)
              }
            } catch (e) {
              console.warn('移除iframe失败:', e)
            }
          }
          
          const doPrint = () => {
            if (hasPrinted) return
            hasPrinted = true
            try {
              iframe.contentWindow.focus()
              iframe.contentWindow.print()
              const printHandler = () => {
                setTimeout(removeIframe, 500)
                iframe.contentWindow.removeEventListener('afterprint', printHandler)
              }
              iframe.contentWindow.addEventListener('afterprint', printHandler)
              setTimeout(() => {
                if (iframe && iframe.parentNode) {
                  removeIframe()
                }
              }, 3000)
            } catch (e) {
              console.error('打印失败:', e)
              removeIframe()
            }
          }
          
          const iframeDoc = iframe.contentDocument || iframe.contentWindow.document
          iframeDoc.open()
          iframeDoc.write(htmlContent)
          iframeDoc.close()
          
          iframe.onload = () => {
            setTimeout(doPrint, 100)
          }
          
          if (iframeDoc.readyState === 'complete') {
            setTimeout(doPrint, 100)
          } else {
            setTimeout(removeIframe, 5000)
          }
        } else {
          this.$message.error(response.message || '生成打印内容失败')
        }
      } catch (error) {
        this.$message.error('打印失败：' + (error.message || '未知错误'))
      }
    },
    handleClose() {
      this.visible = false
      this.detailActiveTab = 'basic'
      this.detailData = null
      this.completedNodes = []
      this.currentTask = null
    },
    getContractTypeName(codeValue) {
      if (!codeValue) return '-'
      const option = this.contractTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    getExecutionStatusName(codeValue) {
      if (!codeValue) return '-'
      const option = this.executionStatusOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    getExecutionStatusType(codeValue) {
      if (!codeValue) return 'info'
      const status = codeValue.toUpperCase()
      if (status === 'PENDING_EXECUTION' || status === '待履约') return 'warning'
      if (status === 'EXECUTING' || status === '履约中') return 'success'
      if (status === 'COMPLETED' || status === '已履约') return 'success'
      if (status === 'ARCHIVED' || status === '已归档') return 'info'
      if (status === 'INVALID' || status === '已失效') return 'danger'
      return 'info'
    },
    getStatusText(status) {
      if (!status) return '-'
      const option = this.applyStatusOptions.find(item => item.value === status)
      return option ? option.label : status
    },
    getStatusType(status) {
      const typeMap = {
        'DRAFT': 'info',
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger',
        'WITHDRAWN': 'info',
        'EXECUTING': '',
        'COMPLETED': 'success',
        'ARCHIVED': 'info'
      }
      return typeMap[status] || ''
    },
    getTaskStatusTypeForRecord(status) {
      const typeMap = {
        'COMPLETED': 'success',
        'RETURNED': 'danger'
      }
      return typeMap[status] || 'info'
    },
    getTaskStatusNameForRecord(status) {
      const nameMap = {
        'COMPLETED': '已完成',
        'RETURNED': '已退回'
      }
      return nameMap[status] || status
    },
    formatDateOnly(date) {
      if (!date) return '-'
      const d = new Date(date)
      if (isNaN(d.getTime())) return date
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    formatDateTime(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },
    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN')
    },
    formatFileSize(size) {
      if (!size) return '-'
      if (size < 1024) return size + ' B'
      if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
      return (size / (1024 * 1024)).toFixed(2) + ' MB'
    }
  }
}
</script>

<style scoped>
</style>

