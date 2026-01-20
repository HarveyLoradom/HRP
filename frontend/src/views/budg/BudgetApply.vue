<template>
  <div class="budget-apply">
    <el-card>
      <div slot="header" class="clearfix">
        <span>预算申请</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增预算申请</el-button>
      </div>
      
      <!-- 查询表单 -->
      <el-form :model="searchForm" style="margin-bottom: 20px;">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="申请单号:" label-width="100px">
              <el-input v-model="searchForm.applyNo" placeholder="请输入申请单号" clearable style="width: 100%;"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="预算项目:" label-width="100px">
              <el-select v-model="searchForm.itemId" placeholder="请选择预算项目" clearable filterable style="width: 100%;">
                <el-option
                  v-for="item in allBudgetItems"
                  :key="item.itemId"
                  :label="item.itemName"
                  :value="item.itemId"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态:" label-width="100px">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100%;">
                <el-option
                  v-for="option in applyStatusOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="申请时间:" label-width="100px">
              <el-date-picker
                v-model="searchForm.applyDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="yyyy-MM-dd"
                value-format="yyyy-MM-dd"
                style="width: 100%;"
              ></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24" style="text-align: right;">
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column label="申请单号" width="160">
          <template slot-scope="scope">
            <el-button type="text" @click="handleViewDetail(scope.row)">{{ scope.row.applyNo }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="itemName" label="预算项目" width="220"></el-table-column>
        <el-table-column prop="subjectName" label="预算主体" width="160"></el-table-column>
        <el-table-column prop="budgetYear" label="年度" width="100"></el-table-column>
        <el-table-column prop="applyAmount" label="申请金额" width="130">
          <template slot-scope="scope">
            ¥{{ scope.row.applyAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="130"></el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批人" width="130">
          <template slot-scope="scope">
            <span>{{ getCurrentApprover(scope.row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="流程" width="80">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleViewProcess(scope.row)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="applyDate" label="申请时间" width="160">
          <template slot-scope="scope">
            {{ formatDate(scope.row.applyDate || scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total">
        </el-pagination>
        <el-dropdown @command="handleExportCommand" :disabled="exportLoading">
          <el-button size="big" type="text" :loading="exportLoading">
            导出<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="current">导出本页</el-dropdown-item>
            <el-dropdown-item command="all">导出全部</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="900px" @open="initApplicantInfo">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="申请人">
              <el-input v-model="applicantInfo.empName" disabled style="width: 100%;"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="科室">
              <el-input v-model="applicantInfo.deptName" disabled style="width: 100%;"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="手机号">
              <el-input v-model="applicantInfo.empPhone" disabled style="width: 100%;"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
        <el-form-item label="预算年度" prop="budgetYear">
          <el-date-picker
            v-model="form.budgetYear"
            type="year"
            placeholder="选择年度"
            format="yyyy"
            value-format="yyyy"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
          </el-col>
          <el-col :span="12">
        <el-form-item label="预算主体" prop="subjectId">
          <el-select 
            v-model="form.subjectId" 
            placeholder="请先选择预算主体" 
            style="width: 100%" 
            filterable
            @change="handleSubjectChange"
          >
            <el-option
              v-for="subject in budgetSubjects"
              :key="subject.subjectId"
              :label="subject.subjectName"
              :value="subject.subjectId"
            ></el-option>
          </el-select>
        </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
        <el-form-item label="预算项目" prop="itemId">
          <el-select 
            v-model="form.itemId" 
            :placeholder="form.subjectId ? '请选择预算项目' : '请先选择预算主体'" 
            style="width: 100%" 
            filterable
            :disabled="!form.subjectId"
            @change="handleItemChange"
          >
            <el-option
              v-for="item in budgetItems"
              :key="item.itemId"
              :label="item.itemName"
              :value="item.itemId"
            ></el-option>
          </el-select>
        </el-form-item>
          </el-col>
          <el-col :span="12">
        <el-form-item label="申请金额" prop="applyAmount">
          <el-input-number 
            v-model="form.applyAmount" 
            :precision="2" 
            :min="0" 
            style="width: 100%"
            placeholder="请输入申请金额"
          ></el-input-number>
        </el-form-item>
          </el-col>
        </el-row>
         <el-row :gutter="20">
           <el-col :span="12">
             <el-form-item label="申请时间" prop="applyDate" required>
               <el-date-picker
                 v-model="form.applyDate"
                 type="date"
                 placeholder="选择申请时间"
                 style="width: 100%"
                 format="yyyy-MM-dd"
                 value-format="yyyy-MM-dd"
                 :default-value="new Date()"
               ></el-date-picker>
             </el-form-item>
           </el-col>
           <el-col :span="12">
             <el-form-item label="模板配置" prop="templateConfigId" required>
               <el-select 
                 v-model="form.templateConfigId" 
                 placeholder="请选择模板配置" 
                 style="width: 100%" 
                 filterable
                 @change="handleTemplateConfigChange"
               >
                 <el-option
                   v-for="config in templateConfigOptions"
                   :key="config.configId"
                   :label="`${config.businessType}-${config.businessTypeValue}-${config.businessTypeName}`"
                   :value="config.configId"
                 ></el-option>
               </el-select>
             </el-form-item>
           </el-col>
         </el-row>
        <el-form-item label="申请事由" prop="applyReason">
          <el-input type="textarea" v-model="form.applyReason" :rows="3" placeholder="请输入申请事由"></el-input>
        </el-form-item>
         <el-form-item label="附件">
           <el-upload
             ref="upload"
             action="#"
             :file-list="fileList"
             :on-remove="handleRemove"
             :on-change="handleFileChange"
             :before-upload="beforeUpload"
             :on-preview="handlePreviewFile"
             :auto-upload="false"
             multiple
           >
             <el-button size="small" type="primary">选择文件</el-button>
             <div slot="tip" class="el-upload__tip">支持上传多个文件，单个文件大小不超过50MB。文件将在保存时上传到服务器</div>
           </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleDialogCancel">取消</el-button>
        <el-button @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="primary" @click="handleSaveAndSubmit">保存并提交</el-button>
      </div>
    </el-dialog>

    <!-- 审批确认弹窗 -->
    <ApprovalConfirmDialog
      v-model="approvalConfirmVisible"
      :next-node-info="nextNodeInfo"
      confirm-button-text="确认审批"
      @confirm="handleApprovalConfirm"
    />

    <!-- 图片预览对话框（仅用于新增/编辑对话框中的文件上传预览） -->
    <el-dialog title="图片预览" :visible.sync="imagePreviewVisible" width="800px" center :modal="false" :append-to-body="true">
      <div style="text-align: center;">
        <img :src="previewImageUrl" style="max-width: 100%; max-height: 600px;" @error="handleImageError" />
      </div>
    </el-dialog>

    <!-- 文件预览对话框（仅用于新增/编辑对话框中的文件上传预览） -->
    <el-dialog title="文件预览" :visible.sync="filePreviewVisible" width="90%" :before-close="handleFilePreviewClose" :modal="false" :append-to-body="true">
      <div style="height: 70vh;">
        <iframe :src="previewFileUrl" style="width: 100%; height: 100%; border: none;" v-if="previewFileUrl"></iframe>
      </div>
    </el-dialog>

     <!-- 详情查看对话框 -->
    <!-- 统一详情组件 -->
    <BudgetApplyDetail
      v-if="selectedApplyId !== null && selectedApplyId !== undefined"
      v-model="detailVisible"
      source-type="apply"
      :apply-id="selectedApplyId"
      @edit="handleDetailEdit"
      @submitted="handleDetailSubmitted"
      @withdrawn="handleDetailWithdrawn"
      @deleted="handleDetailDeleted"
      @print="handleDetailPrint"
    />

     <!-- 流程查看对话框 -->
     <ProcessViewDialog
       :visible.sync="processVisible"
       :row="currentProcessRow"
       :template-config-map="templateConfigMap"
       business-key-field="applyNo"
       business-type-name="预算申请"
       :show-comment="true"
       :show-complete-time="true"
     />

  </div>
</template>

<script>
import { getBudgetApplies, saveBudgetApply, updateBudgetApply, submitBudgetApply, deleteBudgetApply } from '@/api/budg'
import { getBudgetItems, getBudgetItemsBySubject } from '@/api/budg'
import { getBudgetSubjects } from '@/api/budg'
import { paginationMixin } from '@/mixins/pagination'
import ApprovalConfirmDialog from '@/components/ApprovalConfirmDialog.vue'
import { getNextNodeInfoByBusinessKey, getProcessTaskByTaskKey } from '@/api/process'
import { getTemplateConfigByBusinessTypeOnly, getTemplateConfigList, getTemplateConfigById } from '@/api/templateConfig'
import { updateAttachmentBusinessId, deleteAttachment, uploadFile, getAttachmentsByBusinessId } from '@/api/attachment'
import { getCodeTypeOptions } from '@/utils/codeType'
import { exportExcel } from '@/api/common'
import BudgetApplyDetail from '@/views/budg/BudgetApplyDetail.vue'
import ProcessViewDialog from '@/components/ProcessViewDialog.vue'
import Cookies from 'js-cookie'

export default {
  name: 'BudgetApply',
  mixins: [paginationMixin],
  components: {
    ApprovalConfirmDialog,
    BudgetApplyDetail,
    ProcessViewDialog
  },
  data() {
    return {
      loading: false,
      exportLoading: false,
      tableData: [],
      budgetItems: [], // 用于查询表单和新增/编辑表单（根据主体动态加载）
      allBudgetItems: [], // 用于查询表单（所有预算项目）
      budgetSubjects: [],
      searchForm: {
        applyNo: '',
        itemId: null,
        status: '',
        applyDateRange: null
      },
      templateConfigOptions: [], // 模板配置选项
      templateConfigMap: {}, // 模板配置映射（configId -> config对象）
      fileList: [], // 附件列表
      uploadedAttachmentIds: [], // 记录新上传的附件ID，用于取消时删除
      uploadUrl: '/api/auth/attachment/upload',
      uploadHeaders: {
        Authorization: 'Bearer ' + Cookies.get('token')
      },
      uploadData: {
        businessType: 'BUDGET_APPLY'
      },
      approvalConfirmVisible: false, // 审批确认对话框
      nextNodeInfo: null,
      currentApply: {},
      dialogVisible: false,
      detailVisible: false, // 详情查看对话框
      selectedApplyId: null,
      applyStatusOptions: [], // 状态选项
      taskStatusOptions: [], // 任务状态选项
      processVisible: false, // 流程节点对话框
      currentProcessRow: null,
      currentApproverMap: {}, // 存储每个申请的当前审批人
      dialogTitle: '新增预算申请',
      isEdit: false,
      applicantInfo: {
        empName: '',
        deptName: '',
        empPhone: ''
      },
      imagePreviewVisible: false, // 图片预览对话框
      previewImageUrl: '', // 预览图片URL
      filePreviewVisible: false, // 文件预览对话框
      previewFileUrl: '', // 预览文件URL
      form: {
        applyId: null,
        budgetYear: new Date().getFullYear().toString(),
        itemId: null,
        itemCode: '',
        itemName: '',
        subjectId: null,
        subjectCode: '',
        subjectName: '',
        applyAmount: 0,
        applyReason: '',
        templateConfigId: null, // 模板配置ID
        processDefinitionId: null, // 流程定义ID（从模板配置中获取）
        printTemplateId: null, // 打印模板ID（从模板配置中获取）
        mainAttachId: null, // 主附件ID（时间戳，用于附件关联和文件夹命名）
        status: 'DRAFT'
      },
      rules: {
        budgetYear: [{ required: true, message: '请选择预算年度', trigger: 'change' }],
        itemId: [{ required: true, message: '请选择预算项目', trigger: 'change' }],
        subjectId: [{ required: true, message: '请选择预算主体', trigger: 'change' }],
        applyAmount: [{ required: true, message: '请输入申请金额', trigger: 'blur' }],
        templateConfigId: [{ required: true, message: '请选择模板配置', trigger: 'change' }]
      }
    }
  },
  mounted() {
    this.loadCodeTypeOptions()
    this.loadTaskStatusOptions()
    this.loadData()
    // 加载所有预算项目用于查询表单
    this.loadBudgetItems()
    this.loadBudgetSubjects()
    this.loadTemplateConfigs()
    this.initApplicantInfo()
  },
  methods: {
    async loadCodeTypeOptions() {
      this.applyStatusOptions = await getCodeTypeOptions('APPLY_STATUS')
    },
    async loadTaskStatusOptions() {
      this.taskStatusOptions = await getCodeTypeOptions('TASK_STATUS')
    },
    getTaskStatusName(statusValue) {
      if (!statusValue) return '-'
      const option = this.taskStatusOptions.find(item => item.value === statusValue)
      return option ? option.label : statusValue
    },
    getTaskStatusType(statusValue) {
      // 根据系统参数中的状态值返回对应的标签类型
      // 可以根据需要调整映射关系
      if (statusValue === 'COMPLETED') return 'success'
      if (statusValue === 'PENDING') return 'warning'
      if (statusValue === 'TERMINATED') return 'danger'
      if (statusValue === 'TRANSFERRED') return 'info'
      return 'info'
    },
    initApplicantInfo() {
      const userInfo = this.$store.state.user.userInfo || {}
      this.applicantInfo = {
        empName: userInfo.empName || userInfo.emp_name || userInfo.realName || userInfo.name || userInfo.account || '',
        deptName: userInfo.deptName || userInfo.dept_name || '',
        empPhone: userInfo.empPhone || userInfo.emp_phone || userInfo.phone || userInfo.mobile || ''
      }
    },
    loadData() {
      this.loading = true
      const userInfo = this.$store.state.user.userInfo || {}
      
      
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        applyNo: this.searchForm.applyNo || null,
        itemId: this.searchForm.itemId || null,
        status: this.searchForm.status || null,
        startDate: this.searchForm.applyDateRange && this.searchForm.applyDateRange.length > 0 ? this.searchForm.applyDateRange[0] : null,
        endDate: this.searchForm.applyDateRange && this.searchForm.applyDateRange.length > 1 ? this.searchForm.applyDateRange[1] : null
      }
      getBudgetApplies(params).then(response => {
        if (response.code === 200 && response.data) {
          this.tableData = response.data.records || []
          this.pagination.total = response.data.total || 0
          // 加载每个申请的当前审批人
          this.tableData.forEach(row => {
            if (row.processInstanceId || row.applyNo) {
              this.loadCurrentApprover(row)
            }
          })
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = {
        applyNo: '',
        itemId: null,
        status: '',
        applyDateRange: null
      }
      this.pagination.page = 1
      this.loadData()
    },
    loadBudgetItems() {
      getBudgetItems().then(response => {
        if (response.code === 200) {
          this.allBudgetItems = response.data || []
          // 如果还没有选择预算主体，查询表单使用所有预算项目
          if (!this.form.subjectId) {
            this.budgetItems = this.allBudgetItems
          }
        }
      })
    },
    // 根据预算主体加载预算项目
    loadBudgetItemsBySubject(subjectId) {
      if (!subjectId) {
        this.budgetItems = []
        return
      }
      getBudgetItemsBySubject(subjectId).then(response => {
        if (response.code === 200) {
          this.budgetItems = response.data || []
        } else {
          this.budgetItems = []
          this.$message.error(response.message || '加载预算项目失败')
        }
      }).catch(error => {
        this.budgetItems = []
        this.$message.error('加载预算项目失败：' + (error.message || '未知错误'))
      })
    },
    loadBudgetSubjects() {
      getBudgetSubjects().then(response => {
        if (response.code === 200) {
          this.budgetSubjects = response.data || []
        }
      })
    },
    loadTemplateConfigs() {
      getTemplateConfigByBusinessTypeOnly('BUDGET_TYPE').then(response => {
        if (response.code === 200 && response.data) {
          this.templateConfigOptions = response.data.filter(config => config.isActive === 1)
          this.templateConfigMap = {}
          this.templateConfigOptions.forEach(config => {
            this.templateConfigMap[config.configId] = config
          })
        }
      }).catch(error => {
        console.error('加载模板配置失败', error)
      })
    },
    handleTemplateConfigChange(configId) {
      // 根据选择的模板配置ID，设置对应的流程定义ID和打印模板ID
      const config = this.templateConfigMap[configId]
      if (config) {
        this.form.processDefinitionId = config.processDefinitionId || null
        this.form.printTemplateId = config.printTemplateId || null
      } else {
        this.form.processDefinitionId = null
        this.form.printTemplateId = null
      }
    },
    handleItemChange(itemId) {
      const item = this.budgetItems.find(i => i.itemId === itemId)
      if (item) {
        this.form.itemCode = item.itemCode
        this.form.itemName = item.itemName
      }
    },
    handleSubjectChange(subjectId) {
      const subject = this.budgetSubjects.find(s => s.subjectId === subjectId)
      if (subject) {
        this.form.subjectCode = subject.subjectCode
        this.form.subjectName = subject.subjectName
      }
      
      // 清空预算项目选择
      this.form.itemId = null
      this.form.itemCode = ''
      this.form.itemName = ''
      this.budgetItems = []
      
      // 如果选择了预算主体，加载该主体下的预算项目
      if (subjectId) {
        this.loadBudgetItemsBySubject(subjectId)
      }
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
      this.dialogTitle = '新增预算申请'
      this.isEdit = false
      // 生成主附件ID（时间戳），用于附件关联和文件夹命名
      const mainAttachId = Date.now().toString()
      const userInfo = this.$store.state.user.userInfo || {}
      const now = new Date()
      this.form = {
        applyId: null,
        budgetYear: now.getFullYear().toString(),
        itemId: null,
        itemCode: '',
        itemName: '',
        subjectId: null,
        subjectCode: '',
        subjectName: '',
        applyAmount: 0,
        applyReason: '',
        templateConfigId: null,
        processDefinitionId: null,
        printTemplateId: null,
        mainAttachId: mainAttachId, // 主附件ID（时间戳）
         applyDate: this.formatDateForPicker(now),
        status: 'DRAFT',
        applicantId: userInfo.empId || userInfo.emp_id || null,
        applicantCode: userInfo.empCode || userInfo.emp_code || userInfo.account || '',
        applicantName: userInfo.empName || userInfo.emp_name || userInfo.realName || userInfo.name || '',
        deptId: userInfo.deptId || userInfo.dept_id || null,
        deptCode: userInfo.deptCode || userInfo.dept_code || '',
        deptName: userInfo.deptName || userInfo.dept_name || '',
        applicantPhone: userInfo.empPhone || userInfo.emp_phone || userInfo.phone || userInfo.mobile || ''
      }
      // 清空预算项目列表（等选择预算主体后再加载）
      this.budgetItems = []
      this.fileList = []
      this.uploadedAttachmentIds = []
      this.uploadData.businessId = null
      this.dialogVisible = true
    },
    async loadAttachments(mainAttachId) {
      if (!mainAttachId) {
        return
      }
      try {
        // 使用mainAttachId作为businessId查询附件
        const response = await getAttachmentsByBusinessId(mainAttachId)
        if (response.code === 200) {
          const attachments = response.data || []
          this.fileList = attachments.map(item => ({
            name: item.fileName,
            url: this.getFileUrl(item.filePath),
            uid: item.attachmentId,
            response: { data: item.attachmentId },
            filePath: item.filePath,
            fileName: item.fileName,
            isUploaded: true
          }))
        }
      } catch (error) {
        // 静默处理错误
      }
    },
    async handleViewDetail(row) {
      this.selectedApplyId = row.applyId
      this.detailVisible = true
    },
    
    getTaskStatusNameForRecord(status) {
      if (!status) return '-'
      const statusMap = {
        'COMPLETED': '已完成',
        'RETURNED': '已退回',
        'PENDING': '待审批',
        'TERMINATED': '已终止',
        'TRANSFERRED': '已转办'
      }
      return statusMap[status] || status
    },
    getTaskStatusTypeForRecord(status) {
      if (status === 'COMPLETED') return 'success'
      if (status === 'RETURNED') return 'danger'
      if (status === 'PENDING') return 'warning'
      if (status === 'TERMINATED') return 'info'
      if (status === 'TRANSFERRED') return 'info'
      return 'info'
    },
    async handleEdit(row) {
      if (row.status !== 'DRAFT' && row.status !== 'WITHDRAWN' && row.status !== 'REJECTED') {
        this.$message.warning('只有草稿、撤回或拒绝状态的申请单才能编辑')
        return
      }
      this.dialogTitle = '编辑预算申请'
      this.isEdit = true
      this.form = { ...row }
       // 如果申请时间存在，需要格式化为日期选择器需要的格式
       if (this.form.applyDate) {
         this.form.applyDate = this.formatDateForPicker(this.form.applyDate)
       }
      // 如果没有mainAttachId，先尝试使用applyNo查找附件，如果还是没有则生成一个（向后兼容）
      if (!this.form.mainAttachId) {
        // 先尝试使用applyNo查找附件
        if (this.form.applyNo) {
          try {
            const response = await getAttachmentsByBusinessId(this.form.applyNo)
            if (response.code === 200 && response.data && response.data.length > 0) {
              // 如果找到附件，说明这个申请使用的是applyNo作为businessId，保留使用applyNo
              this.form.mainAttachId = this.form.applyNo
            } else {
              // 如果没找到附件，生成一个新的mainAttachId
              this.form.mainAttachId = Date.now().toString()
            }
          } catch (error) {
            // 如果查询失败，生成一个新的mainAttachId
            this.form.mainAttachId = Date.now().toString()
          }
        } else {
          // 如果连applyNo都没有，生成一个新的mainAttachId
          this.form.mainAttachId = Date.now().toString()
        }
      }
      this.fileList = []
      this.uploadedAttachmentIds = []
      this.uploadData.businessId = this.form.mainAttachId
      
      // 如果已有预算主体，加载该主体下的预算项目
      if (this.form.subjectId) {
        await this.loadBudgetItemsBySubject(this.form.subjectId)
      } else {
        this.budgetItems = []
      }
      
      // 加载已有附件（使用mainAttachId，如果mainAttachId等于applyNo，也会正确加载）
      await this.loadAttachments(this.form.mainAttachId)
      
      this.dialogVisible = true
    },
    // 处理审批确认
    async handleApprovalConfirm(opinion) {
      if (!this.currentApply || !this.currentApply.applyId) {
        this.$message.error('申请信息不存在')
        this.approvalConfirmVisible = false
        return
      }
      
      try {
        const response = await approveBudgetApply(this.currentApply.applyId, opinion)
        if (response.code === 200) {
          this.$message.success('审批成功')
          this.approvalConfirmVisible = false
          this.loadData()
        } else {
          this.$message.error(response.message || '审批失败')
        }
      } catch (error) {
        this.$message.error('审批失败：' + (error.message || '未知错误'))
      }
    },
    // 审批按钮处理
    async handleApprove(row) {
      if (!row || !row.applyId) {
        this.$message.warning('申请信息不存在')
        return
      }
      
      // 保存当前申请信息
      this.currentApply = row
      
      // 获取下一节点信息
      try {
        const businessKey = row.applyNo
        if (!businessKey) {
          this.$message.warning('申请单号不存在')
          return
        }
        
        const nextNodeResponse = await getNextNodeInfoByBusinessKey(businessKey)
        if (nextNodeResponse.code === 200 && nextNodeResponse.data) {
          this.nextNodeInfo = nextNodeResponse.data
          this.approvalConfirmVisible = true
        } else {
          // 如果没有下一节点信息，直接显示审批确认对话框
          this.nextNodeInfo = null
          this.approvalConfirmVisible = true
        }
      } catch (error) {
        // 获取下一节点信息失败，仍然显示审批确认对话框
        this.nextNodeInfo = null
        this.approvalConfirmVisible = true
      }
    },
    async handleSaveDraft() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          try {
            const uploadedIds = await this.uploadAllFiles()
            this.uploadedAttachmentIds = this.uploadedAttachmentIds.concat(uploadedIds)
            
            // 确保申请人信息是最新的
            if (!this.isEdit) {
              const userInfo = this.$store.state.user.userInfo || {}
              this.form.applicantId = userInfo.empId || userInfo.emp_id || this.form.applicantId
              this.form.applicantCode = userInfo.empCode || userInfo.emp_code || userInfo.account || this.form.applicantCode
              this.form.applicantName = userInfo.empName || userInfo.emp_name || userInfo.realName || userInfo.name || this.form.applicantName
              this.form.deptId = userInfo.deptId || userInfo.dept_id || this.form.deptId
              this.form.deptCode = userInfo.deptCode || userInfo.dept_code || this.form.deptCode
              this.form.deptName = userInfo.deptName || userInfo.dept_name || this.form.deptName
              this.form.applicantPhone = userInfo.empPhone || userInfo.emp_phone || userInfo.phone || userInfo.mobile || this.form.applicantPhone
            }
            
          const api = this.isEdit ? updateBudgetApply : saveBudgetApply
            const response = await api({ ...this.form, status: 'DRAFT' })
            if (response.code === 200) {
              if (response.data && response.data.applyNo) {
                this.form.applyNo = response.data.applyNo
              }
              
              // 附件已经使用mainAttachId作为businessId上传，无需再关联
              // 保存时mainAttachId已经写入数据库，附件表已经通过mainAttachId关联
              this.$message.success('保存成功')
              this.dialogVisible = false
              this.loadData()
            } else {
              this.$message.error(response.message || '保存失败')
            }
          } catch (error) {
            this.$message.error('保存失败：' + (error.message || '未知错误'))
          }
        }
      })
    },
    async handleSaveAndSubmit() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          try {
            const uploadedIds = await this.uploadAllFiles()
            this.uploadedAttachmentIds = this.uploadedAttachmentIds.concat(uploadedIds)
            
            // 确保申请人信息是最新的
            if (!this.isEdit) {
              const userInfo = this.$store.state.user.userInfo || {}
              this.form.applicantId = userInfo.empId || userInfo.emp_id || this.form.applicantId
              this.form.applicantCode = userInfo.empCode || userInfo.emp_code || userInfo.account || this.form.applicantCode
              this.form.applicantName = userInfo.empName || userInfo.emp_name || userInfo.realName || userInfo.name || this.form.applicantName
              this.form.deptId = userInfo.deptId || userInfo.dept_id || this.form.deptId
              this.form.deptCode = userInfo.deptCode || userInfo.dept_code || this.form.deptCode
              this.form.deptName = userInfo.deptName || userInfo.dept_name || this.form.deptName
              this.form.applicantPhone = userInfo.empPhone || userInfo.emp_phone || userInfo.phone || userInfo.mobile || this.form.applicantPhone
            }
            
          // 先保存申请（新增或更新）
          const api = this.isEdit ? updateBudgetApply : saveBudgetApply
            const response = await api({ ...this.form, status: 'DRAFT' })
            if (response.code !== 200) {
              this.$message.error(response.message || '保存失败')
              return
            }
            
            // 获取保存后的申请ID
            let applyId = null
            if (response.data && response.data.applyId) {
              applyId = response.data.applyId
            } else if (this.form.applyId) {
              applyId = this.form.applyId
            }
            
            if (!applyId) {
              this.$message.error('保存成功，但无法获取申请ID，无法提交')
              return
            }
            
            // 附件已经使用mainAttachId作为businessId上传，无需再关联
            // 保存时mainAttachId已经写入数据库，附件表已经通过mainAttachId关联
            
            // 调用提交接口，生成流程任务记录
            try {
              const submitResponse = await submitBudgetApply(applyId)
              if (submitResponse.code === 200) {
                this.$message.success('提交成功')
                this.dialogVisible = false
                this.loadData()
              } else {
                this.$message.error(submitResponse.message || '提交失败')
              }
            } catch (error) {
              this.$message.error('提交失败：' + (error.message || '未知错误'))
            }
          } catch (error) {
            this.$message.error('提交失败：' + (error.message || '未知错误'))
          }
        }
      })
    },
    // 上传单个文件
    // 通过templateConfigId获取业务类型（definition_key）
    async getBusinessTypeByTemplateConfigId(templateConfigId) {
      if (!templateConfigId) {
        return null
      }
      try {
        // 通过templateConfigId获取TemplateConfig
        const templateConfigResponse = await getTemplateConfigById(templateConfigId)
        if (templateConfigResponse.code !== 200 || !templateConfigResponse.data) {
          console.warn('获取模板配置失败，templateConfigId:', templateConfigId)
          return null
        }
        const templateConfig = templateConfigResponse.data
        
        // 直接从TemplateConfig获取businessType字段
        const businessType = templateConfig.businessType
        if (!businessType) {
          console.warn('模板配置中没有businessType字段，templateConfigId:', templateConfigId)
          return null
        }
        console.log('通过templateConfigId获取业务类型成功，templateConfigId:', templateConfigId, 'businessType:', businessType)
        return businessType
      } catch (error) {
        console.error('获取业务类型失败:', error)
        return null
      }
    },
    async uploadSingleFile(file) {
      try {
        // 使用mainAttachId作为businessId（时间戳），这样在保存之前就能确定business_id
        const businessId = this.form.mainAttachId || (this.isEdit ? (this.form.applyNo || null) : null)
        
        // 通过templateConfigId获取业务类型
        let businessType = 'BUDGET_APPLY' // 默认值
        if (this.form.templateConfigId) {
          const dynamicBusinessType = await this.getBusinessTypeByTemplateConfigId(this.form.templateConfigId)
          if (dynamicBusinessType) {
            businessType = dynamicBusinessType
          }
        }
        console.log('上传文件 - businessType:', businessType, 'templateConfigId:', this.form.templateConfigId)
        
        const fileToUpload = file.raw || file
        if (!fileToUpload) {
          throw new Error('文件对象不存在')
        }
        
        const response = await uploadFile(fileToUpload, businessType, businessId)
        
        if (response.code === 200 && response.data) {
          return response.data
        } else {
          throw new Error(response.message || '上传失败')
        }
      } catch (error) {
        this.$message.error('上传文件失败：' + (error.message || '未知错误'))
        throw error
      }
    },
    
    // 上传所有未上传的文件
    async uploadAllFiles() {
      // 过滤出未上传且有raw文件对象的文件
      const filesToUpload = this.fileList.filter(f => {
        // isUploaded为false或undefined，且有raw文件对象
        const notUploaded = f.isUploaded === false || f.isUploaded === undefined
        const hasRaw = !!(f.raw || (f.status === 'ready' && f))
        return notUploaded && hasRaw
      })
      if (filesToUpload.length === 0) {
        return []
      }
      
      const uploadedIds = []
      for (let i = 0; i < filesToUpload.length; i++) {
        const fileItem = filesToUpload[i]
        try {
          const fileToUpload = fileItem.raw || fileItem
          if (!fileToUpload) {
            throw new Error('文件对象不存在')
          }
          
          const attachmentId = await this.uploadSingleFile({ ...fileItem, raw: fileToUpload })
          uploadedIds.push(attachmentId)
          
          // 更新fileList中对应的文件状态
          const index = this.fileList.findIndex(f => f.uid === fileItem.uid)
          if (index > -1) {
            this.$set(this.fileList[index], 'isUploaded', true)
            this.$set(this.fileList[index], 'attachmentId', attachmentId)
            this.$set(this.fileList[index], 'response', { code: 200, data: attachmentId })
          }
        } catch (error) {
          throw new Error(`文件 ${fileItem.name} 上传失败: ${error.message}`)
        }
      }
      
      return uploadedIds
    },
    async handleRemove(file, fileList) {
      this.fileList = fileList
      // 如果文件已经上传到服务器，需要删除服务器上的文件
      let attachmentId = null
      if (file.response && file.response.data) {
        attachmentId = file.response.data
      } else if (file.attachmentId) {
        attachmentId = file.attachmentId
      } else if (file.uid && typeof file.uid === 'number') {
        // 可能是已保存的附件，uid就是attachmentId
        attachmentId = file.uid
      }
      
      if (attachmentId) {
        // 从列表中移除
        const index = this.uploadedAttachmentIds.indexOf(attachmentId)
        if (index > -1) {
          this.uploadedAttachmentIds.splice(index, 1)
        }
        // 如果是已上传的文件，删除服务器文件
        if (file.isUploaded) {
          try {
            await deleteAttachment(attachmentId)
          } catch (error) {
            // 静默处理删除失败
          }
        }
      }
    },
    beforeUpload(file) {
      const maxSize = 50 * 1024 * 1024 // 50MB
      if (file.size > maxSize) {
        this.$message.error('文件大小不能超过50MB')
        return false
      }
      
      // 检查文件是否已存在（通过name和size判断）
      const exists = this.fileList.some(f => {
        const fRaw = f.raw || f
        const fileRaw = file.raw || file
        return (f.name === file.name && f.size === file.size) || 
               (fRaw && fileRaw && fRaw.uid === fileRaw.uid)
      })
      if (exists) {
        this.$message.warning('文件已存在')
        return false
      }
      
      // 返回true允许添加到列表（但不会自动上传，因为auto-upload="false"）
      return true
    },
    handleFileChange(file, fileList) {
      // 当文件状态为ready时，说明是新添加的文件
      if (file.status === 'ready') {
        
        // 确保文件有raw属性（el-upload在auto-upload="false"时，file.raw就是原始文件对象）
        // 如果没有raw，尝试从file本身获取
        if (!file.raw && file.rawFile) {
          file.raw = file.rawFile
        }
        // 如果还是没有，说明file本身可能就是原始文件对象
        if (!file.raw) {
          // 在auto-upload="false"时，file对象本身就包含了原始文件信息
          // 但我们需要保存raw属性以便后续上传
          file.raw = file
        }
        
        // 标记为未上传
        if (!file.hasOwnProperty('isUploaded')) {
          this.$set(file, 'isUploaded', false)
        }
        
        // 如果是图片，生成预览URL
        const fileToRead = file.raw || file
        if (fileToRead.type && fileToRead.type.startsWith('image/')) {
          const reader = new FileReader()
          reader.onload = (e) => {
            // 更新fileList中对应文件的url
            const index = fileList.findIndex(f => f.uid === file.uid)
            if (index > -1) {
              fileList[index].url = e.target.result
            }
          }
          reader.readAsDataURL(fileToRead)
        }
        
        // 更新fileList（确保响应式）
        this.fileList = fileList
        
        this.$message.success(`文件 "${file.name}" 已添加到列表，保存时将上传到服务器`)
      } else if (file.status === 'removed') {
        // 文件被移除
        this.fileList = fileList
      }
    },
    getFileUrl(filePath) {
      if (!filePath) return ''
      if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
        return filePath
      }
      // 如果是绝对路径，提取uploads之后的部分
      if (filePath.includes('/uploads/')) {
        const parts = filePath.split('/uploads/')
        return '/api/uploads/' + parts[parts.length - 1]
      } else if (filePath.includes('\\uploads\\')) {
        const parts = filePath.split('\\uploads\\')
        return '/api/uploads/' + parts[parts.length - 1].replace(/\\/g, '/')
      } else if (filePath.includes('attachments/')) {
        // 提取attachments之后的部分
        const parts = filePath.split('attachments/')
        return '/api/uploads/attachments/' + parts[parts.length - 1].replace(/\\/g, '/')
      } else if (filePath.includes('attachments\\')) {
        // Windows路径
        const parts = filePath.split('attachments\\')
        return '/api/uploads/attachments/' + parts[parts.length - 1].replace(/\\/g, '/')
      } else {
        // 其他情况，直接使用相对路径
        return '/api/uploads/' + filePath.replace(/\\/g, '/')
      }
    },
    // 以下方法仅用于新增/编辑对话框中的文件上传组件（el-upload）的预览功能
    // 详情查看的附件预览和下载功能已在 BudgetApplyDetail 组件中处理
    async handlePreviewFile(file) {
      // el-upload的预览事件，file是fileList中的文件对象
      await this.handlePreviewAttachment(file)
    },
    async handlePreviewAttachment(attachment) {
      // 如果是未上传的文件，使用本地预览
      if (attachment.raw && (!attachment.isUploaded || attachment.isUploaded === false)) {
        // 如果有data URL（图片预览URL），直接使用
        if (attachment.url && attachment.url.startsWith('data:image/')) {
          this.previewLocalFile(attachment.url, attachment.name)
          return
        }
        
        // 如果是图片但没有url，生成预览URL
        if (attachment.raw && attachment.raw.type && attachment.raw.type.startsWith('image/')) {
          const reader = new FileReader()
          reader.onload = (e) => {
            // 更新fileList中的url，以便下次直接使用
            const index = this.fileList.findIndex(f => f.uid === attachment.uid)
            if (index > -1) {
              this.$set(this.fileList[index], 'url', e.target.result)
            }
            this.previewLocalFile(e.target.result, attachment.name)
          }
          reader.readAsDataURL(attachment.raw)
          return
        }
        
        // 其他文件类型（非图片），提示需要先上传
        this.$message.info('该文件尚未上传，保存后可预览')
        return
      }
      
      // 已上传的文件，构建文件URL
      let fileUrl = attachment.filePath || attachment.url
      
      // 如果没有filePath，需要通过attachmentId获取
      if (!fileUrl) {
        let attachmentId = attachment.attachmentId
        if (!attachmentId && attachment.response && attachment.response.data) {
          attachmentId = attachment.response.data
        }
        if (!attachmentId && attachment.uid && typeof attachment.uid === 'number') {
          attachmentId = attachment.uid
        }
        
        if (attachmentId) {
          try {
            const response = await getAttachment(attachmentId)
            if (response.code === 200 && response.data) {
              fileUrl = this.getFileUrl(response.data.filePath)
            }
          } catch (error) {
          }
        }
      } else {
        // 如果已有filePath，需要转换为访问URL
        fileUrl = this.getFileUrl(fileUrl)
      }
      
      if (!fileUrl) {
        this.$message.error('附件路径不存在')
        return
      }
      
      // 如果filePath是相对路径，需要转换为完整URL
      if (!fileUrl.startsWith('http://') && !fileUrl.startsWith('https://')) {
        if (fileUrl.includes('/uploads/')) {
          const parts = fileUrl.split('/uploads/')
          fileUrl = '/api/uploads/' + parts[parts.length - 1]
        } else if (fileUrl.includes('\\uploads\\')) {
          const parts = fileUrl.split('\\uploads\\')
          fileUrl = '/api/uploads/' + parts[parts.length - 1].replace(/\\/g, '/')
        } else if (fileUrl.includes('attachments/')) {
          fileUrl = '/api/uploads/' + fileUrl.replace(/\\/g, '/')
        } else {
          fileUrl = '/api/uploads/' + fileUrl.replace(/\\/g, '/')
        }
      }
      
      // 判断文件类型并预览
      this.previewLocalFile(fileUrl, attachment.fileName || attachment.name || '')
    },
    previewLocalFile(fileUrl, fileName) {
      if (!fileUrl) {
        this.$message.error('文件路径不存在')
        return
      }
      
      // 判断文件类型，如果是图片，使用图片预览对话框
      const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp']
      const previewableExtensions = ['pdf', 'txt', 'html', 'htm']
      
      // 从fileName或fileUrl中提取扩展名（不包含点号）
      let fileExt = ''
      if (fileName && fileName.includes('.')) {
        fileExt = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()
      } else if (fileUrl && fileUrl.includes('.')) {
        // 如果fileName没有扩展名，尝试从URL中提取
        const urlWithoutQuery = fileUrl.split('?')[0].split('#')[0] // 去掉查询参数和锚点
        const lastDot = urlWithoutQuery.lastIndexOf('.')
        if (lastDot > 0) {
          fileExt = urlWithoutQuery.substring(lastDot + 1).toLowerCase().split('/')[0] // 取第一个部分，去掉可能的路径
        }
      }
      
      // 如果还是无法提取扩展名，尝试从文件名或URL中查找关键字
      const fileNameLower = (fileName || '').toLowerCase()
      const fileUrlLower = (fileUrl || '').toLowerCase()
      
      // 判断是否为图片
      const isImage = fileUrl.startsWith('data:image/') || 
                     imageExtensions.includes(fileExt) ||
                     fileNameLower.match(/\.(jpg|jpeg|png|gif|bmp|webp)$/i) ||
                     fileUrlLower.match(/\.(jpg|jpeg|png|gif|bmp|webp)(\?|#|$)/i)
      
      // 判断是否为可预览的文档
      const isPreviewable = previewableExtensions.includes(fileExt) ||
                           fileNameLower.match(/\.(pdf|txt|html|htm)$/i) ||
                           fileUrlLower.match(/\.(pdf|txt|html|htm)(\?|#|$)/i) ||
                           fileNameLower.includes('.pdf') ||
                           fileUrlLower.includes('.pdf') ||
                           fileUrlLower.includes('application/pdf')
      
      if (isImage) {
        // 图片文件，使用图片预览对话框
        this.previewImageUrl = fileUrl
        this.imagePreviewVisible = true
      } else if (isPreviewable) {
        // PDF等可以在浏览器中预览的文件，使用iframe预览
        this.previewFileUrl = fileUrl
        this.filePreviewVisible = true
      } else {
        // 其他文件类型，提示下载（不自动下载）
        this.$message.info('该文件类型不支持预览，请点击下载按钮下载后查看')
      }
    },
    handleImageError(event) {
      // 图片加载失败处理
      this.$message.error('图片加载失败')
      this.imagePreviewVisible = false
    },
    handleFilePreviewClose() {
      this.previewFileUrl = ''
      this.filePreviewVisible = false
    },
    // 以下方法仅用于新增/编辑对话框中的文件上传组件（el-upload）的下载功能
    // 详情查看的附件预览和下载功能已在 BudgetApplyDetail 组件中处理
    handleDownloadAttachment(attachment) {
      // 构建文件URL
      let fileUrl = attachment.filePath || attachment.url
      if (!fileUrl) {
        this.$message.error('附件路径不存在')
        return
      }
      
      // 如果filePath是相对路径，需要转换为完整URL
      if (!fileUrl.startsWith('http://') && !fileUrl.startsWith('https://')) {
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
    async handleDialogCancel() {
      // 如果上传了附件但没有保存，需要删除这些附件
      // 只删除已上传的附件，未上传的附件只需要清空fileList即可
      const uploadedFiles = this.fileList.filter(f => f.isUploaded && f.attachmentId)
      if (uploadedFiles.length > 0) {
        for (const file of uploadedFiles) {
          try {
            await deleteAttachment(file.attachmentId)
          } catch (error) {
            // 静默处理删除失败
            // 继续删除其他附件，不中断
          }
        }
      }
      
      // 重置文件列表和附件ID列表
      this.fileList = []
      this.uploadedAttachmentIds = []
      
      // 关闭对话框
      this.dialogVisible = false
    },
    getStatusType(status) {
      const map = {
        'DRAFT': 'info',
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger',
        'WITHDRAWN': 'info'
      }
      return map[status] || 'info'
    },
    getStatusText(status) {
      const option = this.applyStatusOptions.find(item => item.value === status)
      return option ? option.label : status
    },
    getCurrentApprover(row) {
      // 已拒绝的申请不需要显示审批人
      if (row.status === 'REJECTED') {
        return '-'
      }
      // 从currentApproverMap中获取当前审批人
      if (this.currentApproverMap[row.applyId]) {
        return this.currentApproverMap[row.applyId]
      }
      // 如果没有，尝试从流程任务获取
      if (row.processInstanceId || row.applyNo) {
        this.loadCurrentApprover(row)
        return '-'
      }
      return '-'
    },
    async loadCurrentApprover(row) {
      try {
        // 优先使用applyNo（taskKey）来查询任务记录
        const taskKey = row.applyNo
        if (taskKey) {
          const response = await getProcessTaskByTaskKey(taskKey)
          if (response.code === 200 && response.data && response.data.length > 0) {
            // 直接按printOrder排序，找到printOrder最小的PENDING任务（当前节点）
            // 这样即使有加签任务，也能正确识别当前应该审批的节点
            const allTasks = response.data
            const pendingTasks = allTasks.filter(task => task.taskStatus === 'PENDING')
            let currentTask = null
            
            if (pendingTasks.length > 0) {
              // 按printOrder排序，printOrder最小的就是当前节点（包括加签任务）
              pendingTasks.sort((a, b) => {
                const aOrder = (a.printOrder != null ? a.printOrder : 999999)
                const bOrder = (b.printOrder != null ? b.printOrder : 999999)
                // 如果printOrder相同，加签任务排在前面
                if (aOrder === bOrder) {
                  const aIsAddsign = a.isAddsignTask === 1
                  const bIsAddsign = b.isAddsignTask === 1
                  if (aIsAddsign && !bIsAddsign) {
                    return -1
                  }
                  if (!aIsAddsign && bIsAddsign) {
                    return 1
                  }
                }
                return aOrder - bOrder
              })
              currentTask = pendingTasks[0]
            }
            
            if (currentTask) {
              // 使用approverList（同一节点的所有审批人，用逗号分隔），如果没有则使用assigneeUserName
              const approverName = currentTask.approverList || currentTask.assigneeUserName
              if (approverName && approverName !== '-') {
                this.$set(this.currentApproverMap, row.applyId, approverName)
                return
              }
            }
          }
        }
        
        // 如果没有applyNo或者没有找到待处理任务，对于已提交的申请，显示'-'
        if (row.status === 'PENDING' || row.status === 'APPROVED' || row.status === 'REJECTED') {
          // 如果是已提交的状态但没有找到任务，可能是任务还未生成或者已经完成
          this.$set(this.currentApproverMap, row.applyId, '-')
          return
        }
        
        // 对于还未提交的申请（草稿状态），如果没有流程实例，尝试从流程定义的第一个节点获取审批人信息
        if (!row.processInstanceId && row.templateConfigId && row.status !== 'WITHDRAWN') {
          // 从模板配置获取流程定义ID
          let config = this.templateConfigMap[row.templateConfigId]
          if (!config && row.templateConfigId) {
            try {
              const configResponse = await getTemplateConfigById(row.templateConfigId)
              if (configResponse.code === 200 && configResponse.data) {
                config = configResponse.data
                this.$set(this.templateConfigMap, row.templateConfigId, config)
              }
            } catch (e) {
            }
          }
          // 如果有流程定义，可以尝试从流程JSON中获取第一个节点的审批人信息
          // 但这里暂时返回'-'，因为流程还未启动
          this.$set(this.currentApproverMap, row.applyId, '-')
          return
        }
        
        // 其他情况
        this.$set(this.currentApproverMap, row.applyId, '-')
      } catch (error) {
        console.error('加载审批人失败:', error)
        this.$set(this.currentApproverMap, row.applyId, '-')
      }
    },
    handleViewProcess(row) {
      this.currentProcessRow = row
      this.processVisible = true
    },
    async handleDetailEdit(row) {
      // 关闭详情对话框
      this.detailVisible = false
      // 打开编辑对话框
      if (row) {
        await this.handleEdit(row)
      }
    },
    async handleDetailSubmitted() {
      // 详情页提交已在组件内部处理，这里只需要刷新数据
      this.loadData()
    },
    handleDetailWithdrawn() {
      // 详情页撤回已在组件内部处理，这里只需要刷新数据
      this.loadData()
    },
    handleDetailDeleted() {
      // 详情页删除已在组件内部处理，这里只需要刷新数据
      this.loadData()
    },
    handleDetailPrint() {
      // 打印逻辑已在统一组件中处理
      // 不需要额外操作
    },
     formatDateForPicker(date) {
       if (!date) return ''
       const d = new Date(date)
       const year = d.getFullYear()
       const month = String(d.getMonth() + 1).padStart(2, '0')
       const day = String(d.getDate()).padStart(2, '0')
       return `${year}-${month}-${day}`
    },
    formatDate(date) {
      if (!date) return '-'
      const d = new Date(date)
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
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportApply(false)
      } else if (command === 'all') {
        this.handleExportApply(true)
      }
    },
    // 导出预算申请数据
    async handleExportApply(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const params = {
            page: 1,
            size: 10000, // 设置一个很大的值以获取所有数据
            applyNo: this.searchForm.applyNo || null,
            itemId: this.searchForm.itemId || null,
            status: this.searchForm.status || null,
            startDate: this.searchForm.applyDateRange && this.searchForm.applyDateRange.length > 0 ? this.searchForm.applyDateRange[0] : null,
            endDate: this.searchForm.applyDateRange && this.searchForm.applyDateRange.length > 1 ? this.searchForm.applyDateRange[1] : null
          }
          
          const res = await getBudgetApplies(params)
          if (res.code === 200 && res.data) {
            dataToExport = res.data.records || res.data.list || []
          } else {
            this.$message.error(res.message || '获取数据失败')
            return
          }
        } else {
          // 导出当前页数据
          dataToExport = this.tableData
        }
        
        if (dataToExport.length === 0) {
          this.$message.warning('没有数据可导出')
          return
        }
        
        // 构建表头
        const headers = ['申请单号', '预算项目', '预算主体', '年度', '申请金额', '申请人', '状态', '审批人', '申请时间']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            String(item.applyNo || ''),
            String(item.itemName || ''),
            String(item.subjectName || ''),
            String(item.budgetYear || ''),
            String(item.applyAmount ? '¥' + item.applyAmount : ''),
            String(item.applicantName || ''),
            String(this.getStatusText(item.status) || ''),
            String(this.getCurrentApprover(item) || '-'),
            String(this.formatDate(item.applyDate || item.createTime) || '')
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '预算申请' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
          headers: headers,
          dataList: dataList
        })
        
        // 处理blob响应并下载
        if (!response || !response.data) {
          this.$message.error('导出失败：服务器返回数据为空')
          return
        }
        
        const blob = response.data
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '预算申请' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败：' + (error.message || '未知错误'))
      } finally {
        this.exportLoading = false
      }
    }
  }
}
</script>

<style scoped>
.budget-apply {
  padding: 20px;
}
</style>

