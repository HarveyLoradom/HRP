<template>
  <el-dialog 
    title="业务申请详情" 
    :visible.sync="visible" 
    width="1000px"
    @close="handleClose"
  >
    <el-tabs v-model="detailActiveTab" v-if="detailData">
      <el-tab-pane label="基本信息" name="basic">
        <el-form :model="detailData" label-width="140px">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="申请单号:">
                <span>{{ detailData.applyNo || '-' }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="申请人:">
                <span>{{ detailData.empName || '-' }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="科室:">
                <span>{{ detailData.deptName || '-' }}</span>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="手机号:">
                <span>{{ detailData.empPhone || '-' }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="申请类型:">
                <span>{{ getApplyTypeName(detailData.hrApplyType) }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="申请子类型:">
                <span>{{ getApplySubTypeName(detailData.hrApplySubType, detailData.hrApplyType) }}</span>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="开始时间:">
                <span>{{ formatDateTime(detailData.startTime) }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="结束时间:">
                <span>{{ formatDateTime(detailData.endTime) }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="申请天数:">
                <span>{{ detailData.applyDay || 0 }} 天</span>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="申请时间:">
                <span>{{ formatDateTime(detailData.createTime) }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="是否护士:">
                <span>{{ detailData.isNurse === 1 ? '是' : '否' }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="状态:">
                <el-tag :type="getStatusType(detailData.status)">
                  {{ getStatusText(detailData.status) }}
                </el-tag>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="申请事由:">
            <span>{{ detailData.applyReason || '-' }}</span>
          </el-form-item>
          <el-form-item label="附件:">
            <el-button size="small" @click="handlePreviewAttachments">查看附件</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      
      <el-tab-pane label="流程记录" name="process">
        <el-table :data="filteredCompletedNodes" border style="width: 100%; margin-bottom: 20px;" v-if="filteredCompletedNodes && filteredCompletedNodes.length > 0">
          <el-table-column prop="taskName" label="节点名称" width="180"></el-table-column>
          <el-table-column prop="assigneeUserName" label="审批人" width="120"></el-table-column>
          <el-table-column prop="taskStatus" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getTaskStatusTypeForRecord(scope.row.taskStatus)" size="small">
                {{ getTaskStatusNameForRecord(scope.row.taskStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="comment" label="审批意见/退回意见" min-width="200">
            <template slot-scope="scope">
              <div v-if="scope.row.taskStatus === 'RETURNED'">
                <div v-if="scope.row.returnType">
                  <div style="margin-bottom: 5px;">
                    <strong>退回方式：</strong>
                    <span>{{ scope.row.returnType === 'RETURN_TO_CURRENT' ? '退回后重新提交到本节点' : '退回后重新走流程' }}</span>
                  </div>
                </div>
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
      <!-- 我的申请页面的按钮 -->
      <template v-if="sourceType === 'apply'">
        <el-button v-if="detailData && (detailData.status === 'DRAFT' || detailData.status === 'WITHDRAWN' || detailData.status === 'REJECTED')" type="primary" @click="handleEdit">编辑</el-button>
        <el-button v-if="detailData && (detailData.status === 'DRAFT' || detailData.status === 'WITHDRAWN' || detailData.status === 'REJECTED')" type="success" @click="handleSubmit">提交</el-button>
        <el-button v-if="detailData && detailData.status === 'PENDING'" type="danger" @click="handleWithdraw">撤回</el-button>
        <el-button v-if="detailData && (detailData.status === 'DRAFT' || detailData.status === 'WITHDRAWN' || detailData.status === 'REJECTED')" type="danger" @click="handleDelete">删除</el-button>
      </template>
      
      <!-- 申请审批页面的按钮 -->
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
      </template>
      
      <!-- 申请查询页面的按钮 -->
      <template v-if="sourceType === 'query'">
        <el-button @click="handleClose">关闭</el-button>
      </template>
      
      <el-button v-if="sourceType !== 'query'" @click="handleClose">关闭</el-button>
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
import { getHrApplyById } from '@/api/hr'
import { getAttachmentsByBusiness, getAttachment } from '@/api/attachment'
import { getProcessTaskByTaskKey, getNextNodeInfoByBusinessKey, transferProcessTask } from '@/api/process'
import { getCodeTypeOptions } from '@/utils/codeType'
import { approveHrApply, rejectHrApply, returnHrApply, withdrawHrApply, submitHrApply, deleteHrApply } from '@/api/hr'
import { getUserList } from '@/api/user'
import ApprovalConfirmDialog from '@/components/ApprovalConfirmDialog.vue'
import RejectReturnDialog from '@/components/RejectReturnDialog.vue'
import ProcessViewDialog from '@/components/ProcessViewDialog.vue'
import { generatePrintContent, getDefaultPrintTemplate } from '@/api/print'
import { getTemplateConfigById } from '@/api/templateConfig'
import { getTemplateConfigPage } from '@/api/templateConfig'
import request from '@/api/request'

export default {
  name: 'BusinessApplyDetail',
  components: {
    ApprovalConfirmDialog,
    RejectReturnDialog,
    ProcessViewDialog
  },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    sourceType: {
      type: String,
      default: 'apply', // 'apply', 'approval', 'query'
      validator: (value) => ['apply', 'approval', 'query'].includes(value)
    },
    applyId: {
      type: [Number, String],
      default: null
    }
  },
  data() {
    return {
      visible: this.value,
      detailData: null,
      detailActiveTab: 'basic',
      completedNodes: [],
      applyTypeOptions: [],
      applySubTypeOptions: [],
      applySubTypeOptionsMap: {}, // 存储按申请类型分组的子类型选项
      statusOptions: [],
      attachments: [],
      attachmentPreviewVisible: false,
      imagePreviewVisible: false,
      previewImageUrl: '',
      approvalDialogVisible: false,
      nextNodeInfo: null,
      returnDialogVisible: false,
      currentTask: null,
      processVisible: false,
      currentProcessRow: null,
      templateConfigMap: {},
      addSignDialogVisible: false,
      addSignForm: { userId: '' },
      transferDialogVisible: false,
      transferForm: { userId: '' },
      userList: []
    }
  },
  computed: {
    filteredCompletedNodes() {
      if (!this.completedNodes || this.completedNodes.length === 0) {
        return []
      }
      return this.completedNodes.filter(task => {
        const comment = task.comment
        return comment && comment.trim() !== '' && comment.trim() !== '-'
      })
    }
  },
  watch: {
    value(newVal) {
      this.visible = newVal
      if (newVal && this.applyId) {
        this.loadDetail()
      }
    },
    visible(newVal) {
      this.$emit('input', newVal)
    },
    applyId(newVal) {
      if (newVal && this.visible) {
        this.loadDetail()
      }
    }
  },
  mounted() {
    this.loadCodeOptions()
    if (this.sourceType === 'approval') {
      this.loadUserList()
    }
    if (this.visible && this.applyId) {
      this.loadDetail()
    }
  },
  methods: {
    async loadCodeOptions() {
      try {
        this.applyTypeOptions = await getCodeTypeOptions('HR_APPLY_TYPE')
        this.applySubTypeOptions = await getCodeTypeOptions('HR_APPLY_SUB_TYPE')
        this.statusOptions = await getCodeTypeOptions('APPLY_STATUS')
        
        // 加载所有申请类型的子类型选项
        if (this.applyTypeOptions && this.applyTypeOptions.length > 0) {
          for (const typeOption of this.applyTypeOptions) {
            try {
              const subTypeOptions = await getCodeTypeOptions(typeOption.value + '_SUB_TYPE')
              this.$set(this.applySubTypeOptionsMap, typeOption.value, subTypeOptions || [])
            } catch (error) {
              console.error(`加载${typeOption.value}的子类型选项失败:`, error)
              this.$set(this.applySubTypeOptionsMap, typeOption.value, [])
            }
          }
        }
      } catch (error) {
        console.error('加载代码选项失败:', error)
      }
    },
    async loadDetail() {
      if (!this.applyId) return
      
      try {
        const res = await getHrApplyById(this.applyId)
        if (res.code === 200 && res.data) {
          this.detailData = res.data
          await this.loadProcessTasks()
          await this.loadAttachments()
        } else {
          this.$message.error(res.msg || '加载详情失败')
        }
      } catch (error) {
        console.error('加载详情失败:', error)
        this.$message.error('加载详情失败')
      }
    },
    async loadProcessTasks() {
      if (!this.detailData || !this.detailData.applyNo) return
      
      try {
        const res = await getProcessTaskByTaskKey(this.detailData.applyNo)
        if (res.code === 200 && res.data) {
          const allTasks = res.data || []
          // 筛选已完成的任务
          const completedTasks = allTasks.filter(task => 
            task.taskStatus === 'COMPLETED' || task.taskStatus === 'RETURNED'
          )
          this.completedNodes = completedTasks
          
          // 如果是审批页面，查找当前待处理任务
          if (this.sourceType === 'approval') {
            await this.loadCurrentTask(this.detailData.applyNo)
          }
        }
      } catch (error) {
        console.error('加载流程任务失败:', error)
      }
    },
    async loadCurrentTask(applyNo) {
      try {
        const response = await getProcessTaskByTaskKey(applyNo)
        if (response.code === 200 && response.data && response.data.length > 0) {
          const userInfo = this.$store.state.user.userInfo || {}
          const currentUserId = userInfo.userId || userInfo.id
          const pendingTasks = response.data.filter(task => task.taskStatus === 'PENDING')
          
          if (pendingTasks.length === 0) {
            this.currentTask = null
            return
          }
          
          pendingTasks.sort((a, b) => {
            const aOrder = a.printOrder != null ? a.printOrder : 999999
            const bOrder = b.printOrder != null ? b.printOrder : 999999
            return aOrder - bOrder
          })
          
          this.currentTask = pendingTasks.find(task => task.assigneeUserId === currentUserId)
          
          if (!this.currentTask) {
            this.currentTask = null
          }
        }
      } catch (error) {
        console.error('加载任务信息失败:', error)
        this.currentTask = null
      }
    },
    async loadUserList() {
      try {
        const response = await getUserList(0)
        if (response.code === 200 && response.data) {
          this.userList = response.data
        }
      } catch (error) {
        console.error('加载用户列表失败:', error)
        this.userList = []
      }
    },
    async loadAttachments() {
      if (!this.detailData || !this.detailData.mainAttachId) return
      
      try {
        const res = await getAttachmentsByBusiness('HR_APPLY', this.detailData.mainAttachId)
        if (res.code === 200 && res.data) {
          this.attachments = res.data || []
        }
      } catch (error) {
        console.error('加载附件失败:', error)
      }
    },
    handleClose() {
      this.visible = false
      this.detailData = null
      this.completedNodes = []
      this.attachments = []
      this.detailActiveTab = 'basic'
    },
    handleEdit() {
      this.$emit('edit', this.detailData.applyId)
    },
    async handleSubmit() {
      try {
        const res = await submitHrApply(this.detailData.applyId)
        if (res.code === 200) {
          this.$message.success('提交成功')
          this.$emit('submitted')
          this.handleClose()
        } else {
          this.$message.error(res.msg || '提交失败')
        }
      } catch (error) {
        console.error('提交失败:', error)
        this.$message.error('提交失败')
      }
    },
    async handleWithdraw() {
      this.$confirm('确定撤回该申请吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await withdrawHrApply(this.detailData.applyId)
          if (res.code === 200) {
            this.$message.success('撤回成功')
            this.$emit('withdrawn')
            this.handleClose()
          } else {
            this.$message.error(res.msg || '撤回失败')
          }
        } catch (error) {
          console.error('撤回失败:', error)
          this.$message.error('撤回失败')
        }
      }).catch(() => {})
    },
    async handleDelete() {
      this.$confirm('确定删除该申请吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteHrApply(this.detailData.applyId)
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.$emit('deleted')
            this.handleClose()
          } else {
            this.$message.error(res.msg || '删除失败')
          }
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    async handleApprove() {
      if (!this.detailData || !this.detailData.applyNo) {
        this.$message.warning('申请单号不存在')
        return
      }
      
      this.nextNodeInfo = null
      try {
        const res = await getNextNodeInfoByBusinessKey(this.detailData.applyNo)
        if (res.code === 200 && res.data) {
          this.nextNodeInfo = res.data
        } else {
          // 获取下一节点信息失败，但不影响打开审批对话框
          console.warn('获取下一节点信息失败:', res.msg)
          this.nextNodeInfo = null
        }
      } catch (error) {
        // 获取下一节点信息失败，但不影响打开审批对话框
        console.error('获取下一节点信息失败:', error)
        this.nextNodeInfo = null
      }
      
      // 无论是否获取到下一节点信息，都打开审批对话框
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
        
        const userInfo = this.$store.state.user.userInfo || {}
        const userId = userInfo.userId || userInfo.id
        
        if (!userId) {
          this.$message.error('未获取到当前用户信息')
          return
        }
        
        const res = await approveHrApply(this.detailData.applyId, userId, opinion, signature)
        if (res.code === 200) {
          this.$message.success('审批成功')
          // 先触发事件，再关闭对话框，确保事件能正确传递
          this.$emit('approved')
          this.$nextTick(() => {
            this.handleClose()
          })
        } else {
          this.$message.error(res.msg || '审批失败')
        }
      } catch (error) {
        console.error('审批失败:', error)
        this.$message.error('审批失败：' + (error.message || '未知错误'))
      } finally {
        this.approvalDialogVisible = false
      }
    },
    handleCloseApprovalDialog() {
      this.approvalDialogVisible = false
    },
    handleReject() {
      this.returnDialogVisible = true
    },
    async handleConfirmReturn(data) {
      // data包含returnType和opinion
      const returnType = data.returnType || 'RETURN_TO_CURRENT'
      const opinion = data.opinion || ''
      
      try {
        const res = await returnHrApply(this.detailData.applyId, returnType, opinion)
        if (res.code === 200) {
          this.$message.success('退回成功')
          this.returnDialogVisible = false
          // 先触发事件，再关闭对话框，确保事件能正确传递
          this.$emit('returned')
          this.$nextTick(() => {
            this.handleClose()
          })
        } else {
          this.$message.error(res.message || res.msg || '退回失败')
        }
      } catch (error) {
        console.error('退回失败:', error)
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
      this.addSignForm = { userId: '' }
      this.addSignDialogVisible = true
    },
    async handleConfirmAddSign() {
      if (!this.addSignForm.userId) {
        this.$message.warning('请选择加签人员')
        return
      }
      if (!this.currentTask) {
        this.$message.warning('无法获取任务信息')
        return
      }
      
      const user = this.userList.find(u => u.id === this.addSignForm.userId)
      if (!user) {
        this.$message.warning('未找到选中的用户信息')
        return
      }
      
      const taskName = this.currentTask.taskName || '审批'
      
      try {
        const response = await request.post('/auth/process-task/add-sign', {
          taskId: this.currentTask.taskId,
          newAssigneeUserId: this.addSignForm.userId,
          newAssigneeUserName: user.name,
          newAssigneeEmpCode: user.account,
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
      this.transferForm = { userId: '' }
      this.transferDialogVisible = true
    },
    async handleConfirmTransfer() {
      if (!this.transferForm.userId) {
        this.$message.warning('请选择转签人员')
        return
      }
      if (!this.currentTask) {
        this.$message.warning('无法获取任务信息')
        return
      }
      
      const user = this.userList.find(u => u.id === this.transferForm.userId)
      if (!user) {
        this.$message.warning('未找到选中的用户信息')
        return
      }
      
      try {
        const response = await transferProcessTask({
          taskId: this.currentTask.taskId,
          newAssigneeUserId: this.transferForm.userId,
          newAssigneeUserName: user.name,
          newAssigneeEmpCode: user.account
        })
        if (response.code === 200) {
          this.$message.success('转签成功')
          this.transferDialogVisible = false
          // 重新加载详情数据，包括当前任务和流程记录
          if (this.detailData && this.detailData.applyNo) {
            await this.loadCurrentTask(this.detailData.applyNo)
            await this.loadProcessTasks()
          }
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
    // 查看流程
    handleViewProcess() {
      if (!this.detailData || !this.detailData.applyNo) {
        this.$message.warning('该申请单暂无流程信息')
        return
      }
      // ProcessViewDialog 组件直接使用 detailData 作为 row
      this.currentProcessRow = this.detailData
      this.processVisible = true
    },
    async handlePreviewAttachments() {
      try {
        if (!this.detailData || !this.detailData.mainAttachId) {
          this.$message.warning('无法获取附件标识')
          return
        }
        
        const res = await getAttachmentsByBusiness('HR_APPLY', this.detailData.mainAttachId)
        if (res.code === 200) {
          this.attachments = res.data || []
          if (this.attachments.length === 0) {
            this.$message.info('暂无附件')
            return
          }
          this.attachmentPreviewVisible = true
        } else {
          this.$message.error(res.message || '加载附件失败')
        }
      } catch (error) {
        this.$message.error('加载附件失败：' + (error.message || '未知错误'))
      }
    },
    handlePreviewAttachment(file) {
      // 预览附件
      const fileUrl = this.getFileUrl(file.filePath || file.url)
      const fileExtension = file.fileName ? file.fileName.split('.').pop().toLowerCase() : ''
      const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp']
      
      if (imageExtensions.includes(fileExtension)) {
        this.previewImageUrl = fileUrl
        this.imagePreviewVisible = true
      } else {
        window.open(fileUrl, '_blank')
      }
    },
    handleDownloadAttachment(file) {
      const fileUrl = this.getFileUrl(file.filePath || file.url)
      window.open(fileUrl, '_blank')
    },
    getFileUrl(filePath) {
      if (!filePath) return ''
      let fileUrl = filePath
      if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
        return fileUrl
      }
      if (filePath.includes('/uploads/')) {
        const parts = filePath.split('/uploads/')
        fileUrl = '/api/uploads/' + parts[parts.length - 1]
      } else if (filePath.includes('\\uploads\\')) {
        const parts = filePath.split('\\uploads\\')
        fileUrl = '/api/uploads/' + parts[parts.length - 1].replace(/\\/g, '/')
      } else {
        fileUrl = '/api/uploads/' + filePath.replace(/\\/g, '/')
      }
      return fileUrl
    },
    handleImageError() {
      this.$message.error('图片加载失败')
    },
    getApplyTypeName(type) {
      const option = this.applyTypeOptions.find(item => item.value === type)
      return option ? option.label : type
    },
    // 获取申请子类型文本（同步方法）
    getApplySubTypeName(subType, hrApplyType) {
      if (!subType || !hrApplyType) return subType || '-'
      const subTypeOptions = this.applySubTypeOptionsMap[hrApplyType] || []
      const option = subTypeOptions.find(item => item.value === subType)
      return option ? option.label : subType
    },
    getStatusText(status) {
      const option = this.statusOptions.find(item => item.value === status)
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
    },
    getTaskStatusTypeForRecord(status) {
      const typeMap = {
        'COMPLETED': 'success',
        'RETURNED': 'danger',
        'PENDING': 'warning'
      }
      return typeMap[status] || ''
    },
    getTaskStatusNameForRecord(status) {
      const nameMap = {
        'COMPLETED': '已完成',
        'RETURNED': '已退回',
        'PENDING': '待处理'
      }
      return nameMap[status] || status
    },
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      if (typeof dateTime === 'string') {
        return dateTime.substring(0, 19).replace('T', ' ')
      }
      return dateTime
    },
    formatDate(date) {
      if (!date) return ''
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
.dialog-footer {
  text-align: right;
}
</style>