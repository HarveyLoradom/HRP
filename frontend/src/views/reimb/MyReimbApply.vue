<template>
  <div class="my-reimb-apply">
    <el-card>
      <div slot="header" class="clearfix">
        <span>我的申请</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增申请</el-button>
      </div>
      
      <!-- 查询表单 -->
      <el-form :model="searchForm" style="margin-bottom: 20px;">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="申请单号:" label-width="100px">
              <el-input v-model="searchForm.payoutBillcode" placeholder="请输入申请单号" clearable style="width: 100%;"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="申请类型:" label-width="100px">
              <el-select v-model="searchForm.payoutTypeId" placeholder="请选择申请类型" clearable style="width: 100%;">
                <el-option
                  v-for="option in applyTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
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
            <el-button type="text" @click="handleViewDetail(scope.row)">{{ scope.row.payoutBillcode }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="empName" label="申请人" width="120">
          <template slot-scope="scope">
            <span>{{ scope.row.empName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="科室" width="150">
          <template slot-scope="scope">
            <span>{{ scope.row.deptName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="payoutTypeId" label="申请类型" width="120">
          <template slot-scope="scope">
            {{ getPayoutTypeName(scope.row.payoutTypeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="applyAmount" label="申请金额" width="130">
          <template slot-scope="scope">
            ¥{{ scope.row.applyAmount }}
          </template>
        </el-table-column>
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
            {{ formatDateOnly(scope.row.applyDate || scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" style="margin-top: 20px; display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
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
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="900px" @close="handleDialogCancel">
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基本信息tab -->
        <el-tab-pane label="基本信息" name="basic">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-row :gutter="20">
              <el-col :span="8">
            <el-form-item label="申请人">
              <el-input v-model="applicantInfo.empName" disabled></el-input>
            </el-form-item>
          </el-col>
              <el-col :span="8">
            <el-form-item label="科室">
              <el-input v-model="applicantInfo.deptName" disabled></el-input>
            </el-form-item>
          </el-col>
              <el-col :span="8">
                <el-form-item label="手机号">
              <el-input v-model="applicantInfo.empPhone" disabled></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
              <el-col :span="8">
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
              <el-col :span="8">
                <el-form-item label="模板配置" prop="templateConfigId">
                  <el-select v-model="form.templateConfigId" placeholder="请选择模板配置" filterable style="width: 100%">
            <el-option
                      v-for="config in templateConfigOptions"
                      :key="config.configId"
                      :label="`${config.businessType}-${config.businessTypeValue}-${config.businessTypeName}`"
                      :value="config.configId"
            ></el-option>
          </el-select>
        </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="申请金额" prop="applyAmount">
                  <el-input-number v-model="form.applyAmount" :min="0" :precision="2" style="width: 100%" :disabled="true"></el-input-number>
                  
        </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="申请时间" prop="applyDate">
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
              <el-col :span="8">
                <el-form-item label="是否护士">
                  <el-radio-group v-model="form.isNurse">
                    <el-radio :label="0">否</el-radio>
                    <el-radio :label="1">是</el-radio>
                </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
        
      
        
        <el-form-item label="申请事由" prop="applyReason">
          <el-input type="textarea" v-model="form.applyReason" :rows="4"></el-input>
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
        </el-tab-pane>
        
        <!-- 预算项目tab -->
        <el-tab-pane label="预算项目" name="budget">
          <div style="margin-bottom: 20px;">
            <el-button type="primary" @click="handleOpenBudgetItemDialog">新增预算</el-button>
          </div>
          
          <el-table :data="budgetDetailList" border style="width: 100%;">
            <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
            <el-table-column prop="itemName" label="预算项目" width="200"></el-table-column>
            <el-table-column prop="itemCode" label="项目编码" width="150"></el-table-column>
            <el-table-column prop="remainingAmount" label="剩余可执行金额" width="150">
              <template slot-scope="scope">
                <span :style="{ color: scope.row.remainingAmount < 0 ? 'red' : '' }">
                  ¥{{ scope.row.remainingAmount || 0 }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="申请金额" width="150">
              <template slot-scope="scope">
                <el-input-number 
                  v-model="scope.row.amount" 
                  :min="0" 
                  :max="scope.row.remainingAmount || 0"
                  :precision="2" 
                  style="width: 100%"
                  @change="handleBudgetDetailAmountChange"
                ></el-input-number>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template slot-scope="scope">
                <el-button size="mini" type="danger" @click="handleRemoveBudgetDetail(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      
      
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

    <!-- 统一详情组件 -->
    <ApplyApplyDetail
      v-model="detailVisible"
      source-type="apply"
      :payout-id="selectedPayoutId"
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
      business-key-field="payoutBillcode"
      business-type-name="申请"
      :show-comment="true"
      :show-complete-time="true"
    />


    <!-- 预算项目选择对话框 -->
    <el-dialog title="选择预算项目" :visible.sync="budgetItemDialogVisible" width="900px">
      <el-form :model="budgetItemSearchForm" :inline="true" style="margin-bottom: 15px;">
        <el-form-item label="预算项目:">
          <el-input v-model="budgetItemSearchForm.itemName" placeholder="请输入预算项目" clearable style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item label="预算编码:">
          <el-input v-model="budgetItemSearchForm.itemCode" placeholder="请输入预算编码" clearable style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleBudgetItemSearch">查询</el-button>
          <el-button @click="handleBudgetItemSearchReset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-tabs v-model="budgetItemTab" @tab-click="handleBudgetItemTabChange">
        <el-tab-pane label="全部" name="all">
          <el-table 
            ref="allBudgetItemTable"
            :data="allBudgetItemList" 
            border 
            style="width: 100%;" 
            v-loading="budgetItemListLoading"
            @selection-change="handleBudgetItemSelectionChange">
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column prop="itemName" label="预算项目" width="200"></el-table-column>
            <el-table-column prop="itemCode" label="项目编码" width="150"></el-table-column>
            <el-table-column prop="budgetAmount" label="预算总额" width="150">
              <template slot-scope="scope">
                ¥{{ scope.row.budgetAmount || 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="remainingAmount" label="剩余可执行金额" width="150">
              <template slot-scope="scope">
                <span :style="{ color: scope.row.remainingAmount < 0 ? 'red' : '' }">
                  ¥{{ scope.row.remainingAmount || 0 }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="有余额" name="withBalance">
          <el-table 
            ref="withBalanceBudgetItemTable"
            :data="withBalanceBudgetItemList" 
            border 
            style="width: 100%;" 
            v-loading="budgetItemListLoading"
            @selection-change="handleBudgetItemSelectionChange">
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column prop="itemName" label="预算项目" width="200"></el-table-column>
            <el-table-column prop="itemCode" label="项目编码" width="150"></el-table-column>
            <el-table-column prop="budgetAmount" label="预算总额" width="150">
              <template slot-scope="scope">
                ¥{{ scope.row.budgetAmount || 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="remainingAmount" label="剩余可执行金额" width="150">
              <template slot-scope="scope">
                <span :style="{ color: scope.row.remainingAmount < 0 ? 'red' : '' }">
                  ¥{{ scope.row.remainingAmount || 0 }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <div slot="footer" class="dialog-footer">
        <span style="margin-right: 10px;">已选择 {{ selectedBudgetItemsInDialog.length }} 项</span>
        <el-button @click="budgetItemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmSelectedBudgetItems" :disabled="selectedBudgetItemsInDialog.length === 0">确认选择</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMyPayoutsPage, savePayout, updatePayout, submitPayout, deletePayout, getPayoutDetail, getBudgetDetailsByBusinessNo } from '@/api/reimb'
import { getBudgetSubjects, getBudgetItems, getBudgetsBySubjectAndItem, checkBudgetAmount, getBudgetSubjectRelatedDepts, getBudgetItemSubjects, getBudgetRemainingAmount } from '@/api/budg'
import { getAttachmentsByBusiness, getAttachmentsByBusinessId, deleteAttachment, updateAttachmentBusinessId, uploadFile, getAttachment } from '@/api/attachment'
import { getCodeTypeOptions } from '@/utils/codeType'
import { paginationMixin } from '@/mixins/pagination'
import { exportExcel } from '@/api/common'
import ApprovalConfirmDialog from '@/components/ApprovalConfirmDialog.vue'
import ApplyApplyDetail from '@/views/reimb/ApplyApplyDetail.vue'
import ProcessViewDialog from '@/components/ProcessViewDialog.vue'
import { getNextNodeInfoByBusinessKey, getProcessTaskByTaskKey } from '@/api/process'
import { getTemplateConfigByBusinessTypeOnly, getTemplateConfigList, getTemplateConfigById } from '@/api/templateConfig'
import { getDefaultPrintTemplate, generatePrintContent } from '@/api/print'
import Cookies from 'js-cookie'

export default {
  name: 'MyReimbApply',
  mixins: [paginationMixin],
  components: {
    ApprovalConfirmDialog,
    ApplyApplyDetail,
    ProcessViewDialog
  },
  data() {
    return {
      loading: false,
      exportLoading: false,
      tableData: [],
      payoutTypeOptions: [],
      applyTypeOptions: [], // 申请类型选项（从APPLY_STATUS获取）
      applyStatusOptions: [],
      budgetSubjects: [],
      budgetItems: [],
      budgets: [],
      approvalConfirmVisible: false,
      nextNodeInfo: null,
      currentApply: {},
      dialogVisible: false,
      detailVisible: false,
      selectedPayoutId: null,
      processVisible: false,
      currentProcessRow: null,
      currentApproverMap: {},
      dialogTitle: '新增申请',
      isEdit: false,
      currentDetail: null,
      searchForm: {
        payoutBillcode: '',
        payoutTypeId: '', // 申请类型
        status: '',
        applyDateRange: null
      },
      fileList: [],
      uploadedAttachmentIds: [], // 记录新上传的附件ID，用于取消时删除
      uploadUrl: '/api/auth/attachment/upload',
      uploadHeaders: {
        Authorization: 'Bearer ' + Cookies.get('token')
      },
      uploadData: {
        businessType: 'PAYOUT_APPLY'
      },
      mainAttachId: null, // 主附件ID（时间戳，用于附件关联和文件夹命名）
      applicantInfo: {
        empName: '',
        deptName: '',
        empPhone: ''
      },
      form: {
        payoutId: null,
        billType: 'APPLY',
        payoutTypeId: '',
        applyAmount: 0,
        applyReason: '',
        isNurse: 0,
        budgetSubjectId: null,
        budgetItemId: null,
        budgetId: null,
        empId: null,
        applyDate: null,
        mainAttachId: null,
        templateConfigId: null, // 模板配置ID
        status: 'DRAFT'
      },
      rules: {
        payoutTypeId: [{ required: true, message: '请选择申请类型', trigger: 'change' }],
        templateConfigId: [{ required: true, message: '请选择模板配置', trigger: 'change' }],
        applyAmount: [
          { required: true, message: '申请金额不能为0', trigger: 'blur' },
          { validator: (rule, value, callback) => {
              if (value <= 0) {
                callback(new Error('申请金额必须大于0，请至少填写一个预算项目的金额'))
              } else {
                callback()
              }
            }, trigger: 'blur' }
        ]
      },
      activeTab: 'basic', // 当前激活的tab
      budgetDetailList: [], // 预算项目列表（包含预算主体、预算等信息）
      filteredBudgetSubjects: [], // 根据科室过滤后的预算主体
      filteredBudgetItems: [], // 根据科室过滤后的预算项目（包含该科室的预算主体下的项目）
      filteredBudgets: [], // 根据预算主体和项目过滤后的预算
      templateConfigOptions: [], // 模板配置选项列表
      allBudgetItems: [], // 所有预算项目
      selectedBudgetItems: [], // 选中的预算项目（多选）
      budgetItemDialogVisible: false, // 预算项目选择对话框
      budgetItemTab: 'all', // 预算项目对话框tab（all: 全部, withBalance: 有余额）
      allBudgetItemList: [], // 全部预算项目列表（包含预算总额和剩余可执行金额）
      withBalanceBudgetItemList: [], // 有余额的预算项目列表
      budgetItemListLoading: false, // 预算项目列表加载状态
      selectedBudgetItemsInDialog: [], // 对话框中选中的预算项目
      budgetItemSearchForm: {
        itemName: '',
        itemCode: ''
      },
      templateConfigMap: {} // 模板配置映射（用于缓存）
    }
  },
  mounted() {
    this.loadCodeTypeOptions()
    this.loadBudgetSubjects()
    this.loadBudgetItems()
    this.loadTemplateConfigs()
    this.loadData()
    this.initApplicantInfo()
  },
  methods: {
    initApplicantInfo() {
      const userInfo = this.$store.state.user.userInfo || {}
      this.applicantInfo = {
        empName: userInfo.empName || userInfo.emp_name || userInfo.realName || userInfo.name || '',
        deptName: userInfo.deptName || userInfo.dept_name || '',
        empPhone: userInfo.empPhone || userInfo.emp_phone || userInfo.phone || userInfo.mobile || ''
      }
    },
    async loadCodeTypeOptions() {
      this.payoutTypeOptions = await getCodeTypeOptions('PAYOUT_TYPE')
      this.applyTypeOptions = await getCodeTypeOptions('APPLY_TYPE') // 申请类型从APPLY_TYPE获取
      this.applyStatusOptions = await getCodeTypeOptions('APPLY_STATUS')
    },
    // 加载模板配置列表（直接加载所有启用的配置，模仿预算申请）
     loadTemplateConfigs() {
      getTemplateConfigByBusinessTypeOnly('APPLY_TYPE').then(response => {
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
    async loadBudgetItems() {
        try {
          const response = await getBudgetItems()
          if (response.code === 200) {
          this.allBudgetItems = response.data || []
          }
        } catch (error) {
          console.error('加载预算项目失败', error)
        }
    },
    // 根据申请人科室过滤预算项目（只显示包含该科室的预算主体下的项目）
    async filterBudgetItemsByDept() {
      // 从多个地方获取 deptId
      let deptId = this.form.deptId
      if (!deptId) {
        const userInfo = this.$store.state.user.userInfo || {}
        deptId = userInfo.deptId || userInfo.dept_id || null
      }
      
      if (!deptId) {
        this.filteredBudgetItems = []
        return
      }
      
      // 先找出包含申请人科室的预算主体
      const validSubjectIds = []
      for (const subject of this.budgetSubjects) {
        try {
          const response = await getBudgetSubjectRelatedDepts(subject.subjectId)
          if (response.code === 200) {
            const relatedDepts = response.data || []
            // 检查是否包含申请人科室
            const hasDept = relatedDepts.some(dept => dept.deptId === deptId || dept.dept_id === deptId)
            if (hasDept) {
              validSubjectIds.push(subject.subjectId)
            }
          }
        } catch (error) {
          console.error(`检查预算主体${subject.subjectId}关联科室失败`, error)
        }
      }
      
      // 过滤出分配给这些预算主体的项目
      const filtered = []
      for (const item of this.allBudgetItems) {
        try {
          const itemSubjectsResponse = await getBudgetItemSubjects(item.itemId)
          if (itemSubjectsResponse.code === 200) {
            const itemSubjects = itemSubjectsResponse.data || []
            // 检查是否分配给包含申请人科室的预算主体
            const hasValidSubject = itemSubjects.some(s => 
              validSubjectIds.includes(s.subjectId || s.subject_id)
            )
            if (hasValidSubject) {
              filtered.push(item)
            }
          }
        } catch (error) {
          console.error(`检查预算项目${item.itemId}关联主体失败`, error)
        }
      }
      this.filteredBudgetItems = filtered
    },
    // 打开预算项目选择对话框
    async handleOpenBudgetItemDialog() {
      // 确保 deptId 已设置
      if (!this.form.deptId) {
        const userInfo = this.$store.state.user.userInfo || {}
        this.form.deptId = userInfo.deptId || userInfo.dept_id || null
        
        // 如果还是没有，尝试通过API获取用户信息
        if (!this.form.deptId && userInfo.userId) {
          try {
            const { getUserById } = await import('@/api/user')
            const response = await getUserById(userInfo.userId)
            if (response.code === 200 && response.data) {
              this.form.deptId = response.data.deptId || response.data.dept_id || null
              // 更新store中的用户信息
              if (this.form.deptId) {
                this.$store.commit('user/SET_USER_INFO', {
                  ...userInfo,
                  deptId: this.form.deptId
                })
              }
            }
          } catch (error) {
            console.error('获取用户信息失败', error)
          }
        }
      }
      
      if (!this.form.deptId) {
        this.$message.warning('无法获取申请人科室信息，请先完善个人信息')
        return
      }
      
      this.budgetItemDialogVisible = true
      this.budgetItemTab = 'all'
      this.selectedBudgetItemsInDialog = []
      await this.loadBudgetItemList()
      // 清空表格选择状态
      this.$nextTick(() => {
        if (this.$refs.allBudgetItemTable) {
          this.$refs.allBudgetItemTable.clearSelection()
        }
        if (this.$refs.withBalanceBudgetItemTable) {
          this.$refs.withBalanceBudgetItemTable.clearSelection()
        }
      })
    },
    // 加载预算项目列表（包含预算总额和剩余可执行金额）
    async loadBudgetItemList() {
      this.budgetItemListLoading = true
      try {
        this.allBudgetItemList = []
        this.withBalanceBudgetItemList = []
        
        // 从多个地方获取 deptId
        let deptId = this.form.deptId
        if (!deptId) {
          const userInfo = this.$store.state.user.userInfo || {}
          deptId = userInfo.deptId || userInfo.dept_id || null
        }
        
        if (!deptId) {
          this.$message.warning('无法获取申请人科室信息')
          this.budgetItemListLoading = false
          return
        }
        
        // 确保预算主体和预算项目数据已加载
        if (!this.budgetSubjects || this.budgetSubjects.length === 0) {
          await this.loadBudgetSubjects()
        }
        if (!this.allBudgetItems || this.allBudgetItems.length === 0) {
          await this.loadBudgetItems()
        }
        
        // 检查数据是否加载成功
        if (!this.budgetSubjects || this.budgetSubjects.length === 0) {
          this.$message.warning('预算主体数据未加载，请刷新页面重试')
          this.budgetItemListLoading = false
          return
        }
        if (!this.allBudgetItems || this.allBudgetItems.length === 0) {
          this.$message.warning('预算项目数据未加载，请刷新页面重试')
          this.budgetItemListLoading = false
          return
        }
      
        // 先并行找出包含申请人科室的预算主体（使用缓存避免重复查询）
        const subjectDeptCache = new Map() // 缓存：subjectId -> relatedDepts
        const validSubjectIds = []
        
        // 并行查询所有预算主体的关联科室
        const subjectPromises = this.budgetSubjects.map(async (subject) => {
          try {
            if (!subjectDeptCache.has(subject.subjectId)) {
              const response = await getBudgetSubjectRelatedDepts(subject.subjectId)
          if (response.code === 200) {
                subjectDeptCache.set(subject.subjectId, response.data || [])
              }
            }
            const relatedDepts = subjectDeptCache.get(subject.subjectId) || []
            const hasDept = relatedDepts.some(dept => dept.deptId === deptId || dept.dept_id === deptId)
            if (hasDept) {
              validSubjectIds.push(subject.subjectId)
            }
          } catch (error) {
            console.error(`检查预算主体${subject.subjectId}关联科室失败`, error)
          }
        })
        await Promise.all(subjectPromises)
        
        // 并行过滤出分配给这些预算主体的项目（使用缓存避免重复查询）
        const itemSubjectCache = new Map() // 缓存：itemId -> itemSubjects
        const validItems = []
        
        const itemPromises = this.allBudgetItems.map(async (item) => {
          try {
            if (!itemSubjectCache.has(item.itemId)) {
              const itemSubjectsResponse = await getBudgetItemSubjects(item.itemId)
              if (itemSubjectsResponse.code === 200) {
                itemSubjectCache.set(item.itemId, itemSubjectsResponse.data || [])
              }
            }
            const itemSubjects = itemSubjectCache.get(item.itemId) || []
            const hasValidSubject = itemSubjects.some(s => 
              validSubjectIds.includes(s.subjectId || s.subject_id)
            )
            if (hasValidSubject) {
              validItems.push({ item, itemSubjects })
            }
          } catch (error) {
            console.error(`检查预算项目${item.itemId}关联主体失败`, error)
          }
        })
        await Promise.all(itemPromises)
        
        // 并行为每个项目加载预算信息
        const budgetItemPromises = validItems.map(async ({ item, itemSubjects }) => {
          try {
            // 找出包含申请人科室的预算主体（使用缓存）
            let validSubject = null
            for (const subject of itemSubjects) {
              const subjectId = subject.subjectId || subject.subject_id
              let relatedDepts = subjectDeptCache.get(subjectId)
              if (!relatedDepts) {
                try {
                  const deptResponse = await getBudgetSubjectRelatedDepts(subjectId)
                  if (deptResponse.code === 200) {
                    relatedDepts = deptResponse.data || []
                    subjectDeptCache.set(subjectId, relatedDepts)
                  }
                } catch (error) {
                  console.error(`检查预算主体关联科室失败`, error)
                  continue
                }
              }
              if (relatedDepts) {
                const hasDept = relatedDepts.some(dept => dept.deptId === deptId || dept.dept_id === deptId)
                if (hasDept) {
                  validSubject = subject
                  break
                }
              }
            }
            
            if (!validSubject) return null
            
            // 获取该主体和项目对应的预算
            try {
              const budgetResponse = await getBudgetsBySubjectAndItem(
                validSubject.subjectId || validSubject.subject_id, 
                item.itemId
              )
              if (budgetResponse.code === 200 && budgetResponse.data && budgetResponse.data.length > 0) {
                const budget = budgetResponse.data[0] // 取第一个预算
                
                // 获取预算总额和剩余可执行金额
                let budgetAmount = budget.budgetAmount || 0
                let remainingAmount = 0
                try {
                  const remainingResponse = await getBudgetRemainingAmount(budget.budgetId)
                  if (remainingResponse.code === 200) {
                    remainingAmount = remainingResponse.data || 0
                  }
                } catch (error) {
                  console.error('获取剩余金额失败', error)
                }
                
                return {
                  subjectId: validSubject.subjectId || validSubject.subject_id,
                  subjectCode: validSubject.subjectCode || validSubject.subject_code,
                  subjectName: validSubject.subjectName || validSubject.subject_name,
                  itemId: item.itemId,
                  itemCode: item.itemCode,
                  itemName: item.itemName,
                  budgetId: budget.budgetId,
                  budgetNo: budget.budgetNo,
                  budgetName: budget.budgetName,
                  budgetYear: budget.budgetYear,
                  budgetAmount: budgetAmount,
                  remainingAmount: remainingAmount
                }
          }
        } catch (error) {
          console.error('加载预算失败', error)
        }
          } catch (error) {
            console.error('加载预算项目关联主体失败', error)
          }
          return null
        })
        
        const budgetItems = await Promise.all(budgetItemPromises)
        let validBudgetItems = budgetItems.filter(item => item !== null)
        
        // 应用查询条件
        if (this.budgetItemSearchForm.itemName) {
          validBudgetItems = validBudgetItems.filter(item => 
            item.itemName && item.itemName.includes(this.budgetItemSearchForm.itemName)
          )
        }
        if (this.budgetItemSearchForm.itemCode) {
          validBudgetItems = validBudgetItems.filter(item => 
            item.itemCode && item.itemCode.includes(this.budgetItemSearchForm.itemCode)
          )
        }
        
        this.allBudgetItemList = validBudgetItems
        this.withBalanceBudgetItemList = validBudgetItems.filter(item => item.remainingAmount > 0)
        
        // 如果没有加载到任何数据，提示用户
        if (this.allBudgetItemList.length === 0) {
          this.$message.info('暂无符合条件的预算项目')
        }
      } catch (error) {
        console.error('加载预算项目列表失败', error)
        this.$message.error('加载预算项目列表失败：' + (error.message || '未知错误'))
      } finally {
        this.budgetItemListLoading = false
      }
    },
    // 预算项目对话框tab切换
    handleBudgetItemTabChange(tab) {
      // tab切换时不需要重新加载，因为数据已经在loadBudgetItemList中准备好了
      // 切换tab时保持选中状态（因为两个tab共享selectedBudgetItemsInDialog）
      this.$nextTick(() => {
        // 根据当前tab设置表格选中状态
        if (tab === 'all' && this.$refs.allBudgetItemTable) {
          this.$refs.allBudgetItemTable.clearSelection()
          this.selectedBudgetItemsInDialog.forEach(selectedItem => {
            const row = this.allBudgetItemList.find(item => 
              item.itemId === selectedItem.itemId && item.subjectId === selectedItem.subjectId
            )
            if (row) {
              this.$refs.allBudgetItemTable.toggleRowSelection(row, true)
            }
          })
        } else if (tab === 'withBalance' && this.$refs.withBalanceBudgetItemTable) {
          this.$refs.withBalanceBudgetItemTable.clearSelection()
          this.selectedBudgetItemsInDialog.forEach(selectedItem => {
            const row = this.withBalanceBudgetItemList.find(item => 
              item.itemId === selectedItem.itemId && item.subjectId === selectedItem.subjectId
            )
            if (row) {
              this.$refs.withBalanceBudgetItemTable.toggleRowSelection(row, true)
            }
          })
        }
      })
    },
    // 预算项目查询
    handleBudgetItemSearch() {
      this.loadBudgetItemList()
    },
    // 预算项目查询重置
    handleBudgetItemSearchReset() {
      this.budgetItemSearchForm = {
        itemName: '',
        itemCode: ''
      }
      this.loadBudgetItemList()
    },
    // 预算项目选择变化处理
    handleBudgetItemSelectionChange(selection) {
      // 合并当前选中的项目到selectedBudgetItemsInDialog
      // 先移除当前tab中已取消选中的项目
      const currentList = this.budgetItemTab === 'all' ? this.allBudgetItemList : this.withBalanceBudgetItemList
      const currentItemKeys = currentList.map(item => `${item.itemId}_${item.subjectId}`)
      
      // 移除当前tab中不在selection中的项目
      this.selectedBudgetItemsInDialog = this.selectedBudgetItemsInDialog.filter(item => {
        const key = `${item.itemId}_${item.subjectId}`
        return !currentItemKeys.includes(key)
      })
      
      // 添加新选中的项目
      selection.forEach(row => {
        const key = `${row.itemId}_${row.subjectId}`
        const exists = this.selectedBudgetItemsInDialog.some(item => 
          `${item.itemId}_${item.subjectId}` === key
        )
        if (!exists) {
          this.selectedBudgetItemsInDialog.push(row)
        }
      })
    },
    // 确认选择预算项目（批量添加）
    handleConfirmSelectedBudgetItems() {
      if (this.selectedBudgetItemsInDialog.length === 0) {
        this.$message.warning('请至少选择一个预算项目')
        return
      }
      
      let addedCount = 0
      let skippedCount = 0
      
      this.selectedBudgetItemsInDialog.forEach(row => {
        // 检查是否已经添加过
        const exists = this.budgetDetailList.some(detail => 
          detail.itemId === row.itemId && detail.subjectId === row.subjectId
        )
        
        if (exists) {
          skippedCount++
          return
        }
        
        // 添加到预算明细列表
        this.budgetDetailList.push({
          subjectId: row.subjectId,
          subjectCode: row.subjectCode,
          subjectName: row.subjectName,
          itemId: row.itemId,
          itemCode: row.itemCode,
          itemName: row.itemName,
          budgetId: row.budgetId,
          budgetNo: row.budgetNo,
          budgetName: row.budgetName,
          budgetYear: row.budgetYear,
          budgetAmount: row.budgetAmount,
          remainingAmount: row.remainingAmount,
          amount: 0 // 初始金额为0，用户自己填写
        })
        addedCount++
      })
      
      // 更新申请金额
      this.updateApplyAmount()
      
      // 关闭对话框并清空选择
      this.budgetItemDialogVisible = false
      this.selectedBudgetItemsInDialog = []
      
      if (addedCount > 0) {
        if (skippedCount > 0) {
          this.$message.success(`已添加 ${addedCount} 个预算项目，${skippedCount} 个项目已存在`)
        } else {
          this.$message.success(`已添加 ${addedCount} 个预算项目`)
        }
      } else {
        this.$message.warning('所选项目均已添加，未添加新项目')
      }
    },
    // 预算明细金额变化处理
    handleBudgetDetailAmountChange() {
      // 校验每个明细的金额不能大于剩余可执行金额
      for (const detail of this.budgetDetailList) {
        if (detail.amount > detail.remainingAmount) {
          this.$message.warning(`预算项目"${detail.itemName}"的申请金额不能大于剩余可执行金额`)
          detail.amount = detail.remainingAmount
        }
      }
      this.updateApplyAmount()
    },
    // 更新申请金额（等于所有明细金额的总和）
    updateApplyAmount() {
      const total = this.budgetDetailList.reduce((sum, detail) => {
        return sum + (detail.amount || 0)
      }, 0)
      this.form.applyAmount = total
    },
    // 删除预算项目
    handleRemoveBudgetDetail(index) {
      this.budgetDetailList.splice(index, 1)
      this.updateApplyAmount()
    },
    // 加载预算项目列表
    async loadBudgetDetails(businessNo) {
      if (!businessNo) {
        this.budgetDetailList = []
        this.selectedBudgetItems = []
        return
      }
      try {
        // 调用后端API获取预算明细（通过reimb服务，内部调用budg服务）
        // 后端已经返回了 remainingAmount 字段，直接使用
        const response = await getBudgetDetailsByBusinessNo(businessNo)
        if (response.code === 200 && response.data) {
          const details = response.data || []
          this.budgetDetailList = []
          
          // 为每个明细加载预算总额和剩余可执行金额
          for (const detail of details) {
            let budgetAmount = 0
            // 直接使用后端返回的 remainingAmount，如果没有则尝试通过 API 获取
            let remainingAmount = detail.remainingAmount != null ? Number(detail.remainingAmount) : null
            
            // 通过subjectId和itemId查找预算信息（用于获取预算总额）
            if (detail.subjectId && detail.itemId) {
              try {
                // 获取预算信息
                const budgetResponse = await getBudgetsBySubjectAndItem(detail.subjectId, detail.itemId)
                if (budgetResponse.code === 200 && budgetResponse.data && budgetResponse.data.length > 0) {
                  const budget = budgetResponse.data[0]
                  budgetAmount = budget.budgetAmount || 0
                  
                  // 如果后端没有返回 remainingAmount，则通过 API 获取
                  if (remainingAmount === null && budget.budgetId) {
                    const remainingResponse = await getBudgetRemainingAmount(budget.budgetId)
                    if (remainingResponse.code === 200) {
                      remainingAmount = remainingResponse.data || 0
                    }
                  }
          }
        } catch (error) {
                // 静默失败，使用默认值
              }
            }
            
            this.budgetDetailList.push({
              subjectId: detail.subjectId,
              subjectCode: detail.subjectCode,
              subjectName: detail.subjectName,
              itemId: detail.itemId,
              itemCode: detail.itemCode,
              itemName: detail.itemName,
              budgetId: detail.budgetId,
              budgetNo: detail.budgetNo,
              budgetName: detail.budgetName || detail.budgetNo,
              budgetYear: detail.budgetYear,
              budgetAmount: budgetAmount,
              remainingAmount: remainingAmount != null ? remainingAmount : 0,
              amount: detail.amount || 0
            })
          }
          
          // 设置选中的项目
          this.selectedBudgetItems = this.budgetDetailList.map(detail => detail.itemId)
          this.updateApplyAmount()
        } else {
          this.budgetDetailList = []
          this.selectedBudgetItems = []
        }
      } catch (error) {
        console.error('加载预算明细失败', error)
        this.budgetDetailList = []
        this.selectedBudgetItems = []
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
          
          // 根据查询条件过滤
          if (this.searchForm.payoutBillcode) {
            records = records.filter(item => 
              item.payoutBillcode && item.payoutBillcode.includes(this.searchForm.payoutBillcode)
            )
          }
          if (this.searchForm.payoutTypeId) {
            records = records.filter(item => item.payoutTypeId === this.searchForm.payoutTypeId)
          }
          if (this.searchForm.status) {
            records = records.filter(item => item.status === this.searchForm.status)
          }
          if (this.searchForm.applyDateRange && this.searchForm.applyDateRange.length === 2) {
            const startDate = this.searchForm.applyDateRange[0]
            const endDate = this.searchForm.applyDateRange[1]
            records = records.filter(item => {
              const applyDate = item.applyDate || item.createTime
              if (!applyDate) return false
              const dateStr = this.formatDateOnly(applyDate)
              return dateStr >= startDate && dateStr <= endDate
            })
          }
          
          this.tableData = records
          this.pagination.total = records.length > 0 ? records.length : (response.data.total || 0)
          // 加载每个申请的当前审批人
          this.tableData.forEach(row => {
            if (row.processInstanceId || row.payoutBillcode) {
              this.loadCurrentApprover(row)
            }
          })
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
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = {
        payoutBillcode: '',
        payoutTypeId: '',
        status: '',
        applyDateRange: null
      }
      this.pagination.page = 1
      this.loadData()
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
    async handleAdd() {
      this.dialogTitle = '新增申请'
      this.isEdit = false
      const mainAttachId = Date.now().toString()
      const userInfo = this.$store.state.user.userInfo || {}
      
      // 尝试从多个来源获取empId
      let empId = userInfo.empId || userInfo.emp_id
      
      // 如果用户信息中没有empId，尝试通过API获取
      if (!empId && (userInfo.userId || userInfo.id)) {
        try {
          const { getUserById } = await import('@/api/user')
          const userId = userInfo.userId || userInfo.id
          const response = await getUserById(userId)
          if (response.code === 200 && response.data) {
            empId = response.data.empId || response.data.emp_id
            // 更新store中的用户信息
            if (empId) {
              this.$store.commit('user/SET_USER_INFO', {
                ...userInfo,
                empId: empId
              })
            }
          }
        } catch (error) {
          console.error('获取用户信息失败', error)
        }
      }
      
      // 如果仍然没有empId，尝试从account获取（通过getUserByAccount）
      if (!empId && userInfo.account) {
        try {
          const { getUserByAccount } = await import('@/api/user')
          const response = await getUserByAccount(userInfo.account)
          if (response.code === 200 && response.data) {
            empId = response.data.empId || response.data.emp_id
            if (empId) {
              this.$store.commit('user/SET_USER_INFO', {
                ...userInfo,
                empId: empId
              })
            }
          }
        } catch (error) {
          console.error('通过account获取用户信息失败', error)
        }
      }
      
      if (!empId) {
        this.$message.error('无法获取员工ID，请重新登录或联系管理员')
        return
      }
      
      const now = new Date()
      this.form = {
        payoutId: null,
        billType: 'APPLY',
        payoutTypeId: '',
        templateConfigId: null,
        applyAmount: 0,
        applyReason: '',
        isNurse: 0,
        empId: empId,
        empCode: userInfo.empCode || userInfo.emp_code || userInfo.account || '',
        empName: userInfo.empName || userInfo.emp_name || userInfo.realName || userInfo.name || '',
        deptId: userInfo.deptId || userInfo.dept_id || null,
        applyDate: this.formatDateForPicker(now),
        mainAttachId: mainAttachId,
        status: 'DRAFT'
      }
      // 确保模板配置已加载（显示所有启用的配置，不根据业务类型过滤）
      if (this.templateConfigOptions.length === 0) {
        this.loadTemplateConfigs()
      }
      this.mainAttachId = mainAttachId
      this.fileList = []
      this.uploadedAttachmentIds = []
      this.uploadData.businessId = null
      this.budgetDetailList = []
      this.selectedBudgetItems = []
      this.activeTab = 'basic'
      // 根据申请人科室过滤预算项目
      this.filterBudgetItemsByDept()
      this.dialogVisible = true
    },
    formatDateForPicker(date) {
      if (!date) return ''
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    async loadAttachments(mainAttachId) {
      if (!mainAttachId) return
      try {
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
        console.error('加载附件失败', error)
      }
    },
    getFileUrl(filePath) {
      if (!filePath) return ''
      if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
        return filePath
      }
      if (filePath.includes('/uploads/')) {
        const parts = filePath.split('/uploads/')
        return '/api/uploads/' + parts[parts.length - 1]
      } else if (filePath.includes('\\uploads\\')) {
        const parts = filePath.split('\\uploads\\')
        return '/api/uploads/' + parts[parts.length - 1].replace(/\\/g, '/')
      } else if (filePath.includes('attachments/')) {
        const parts = filePath.split('attachments/')
        return '/api/uploads/attachments/' + parts[parts.length - 1].replace(/\\/g, '/')
      } else if (filePath.includes('attachments\\')) {
        const parts = filePath.split('attachments\\')
        return '/api/uploads/attachments/' + parts[parts.length - 1].replace(/\\/g, '/')
      } else {
        return '/api/uploads/' + filePath.replace(/\\/g, '/')
      }
    },
    async handleViewDetail(row) {
      this.selectedPayoutId = row.payoutId
      this.detailVisible = true
    },
    async handleDetailEdit(row) {
      // 关闭详情对话框
      this.detailVisible = false
      // 打开编辑对话框
      if (row) {
        await this.handleEdit(row)
      }
    },
    async handleEdit(row) {
      if (row.status !== 'DRAFT' && row.status !== 'WITHDRAWN' && row.status !== 'REJECTED') {
        this.$message.warning('只有草稿、撤回或拒绝状态的申请单才能编辑')
        return
      }
      this.dialogTitle = '编辑申请'
      this.isEdit = true
      this.form = { ...row, isNurse: row.isNurse !== undefined ? row.isNurse : 0 }
      if (this.form.applyDate) {
        this.form.applyDate = this.formatDateForPicker(this.form.applyDate)
      }
      // 始终使用mainAttachId作为businessId
      if (!this.form.mainAttachId) {
        this.form.mainAttachId = Date.now().toString()
      }
      this.mainAttachId = this.form.mainAttachId
      this.uploadData.businessId = this.form.mainAttachId
      this.fileList = []
      this.uploadedAttachmentIds = []
      this.activeTab = 'basic'
      // 根据申请人科室过滤预算项目
      this.filterBudgetItemsByDept()
      // 加载预算项目列表
      await this.loadBudgetDetails(row.payoutBillcode)
      this.loadAttachments(this.form.mainAttachId)
      this.dialogVisible = true
    },
    async handleDetailSubmitted(row) {
      // 详情页提交已在组件内部处理，这里只需要刷新数据
      this.loadData()
    },
    async handleSubmit(row) {
      if (!row) return
      if (row.status !== 'DRAFT' && row.status !== 'WITHDRAWN' && row.status !== 'REJECTED') {
        this.$message.warning('只有草稿、撤回或拒绝状态的申请单才能提交')
        return
      }
      
      try {
        const submitResponse = await submitPayout(row.payoutId)
        if (submitResponse.code !== 200) {
          this.$message.error(submitResponse.message || '提交失败')
          return
        }
        
        if (this.detailVisible) {
          this.detailVisible = false
        }
        this.$message.success('提交成功')
            this.loadData()
      } catch (error) {
        this.$message.error('提交失败：' + (error.message || '未知错误'))
          }
    },
    handleDetailWithdrawn() {
      this.loadData()
    },
    handleDetailDeleted(row) {
      // 详情页删除已在组件内部处理，这里只需要刷新数据
      this.loadData()
    },
    handleDelete(row) {
      if (!row) return
      if (row.status !== 'DRAFT' && row.status !== 'WITHDRAWN' && row.status !== 'REJECTED') {
        this.$message.warning('只有草稿、撤回或拒绝状态的申请单才能删除')
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
            if (this.detailVisible) {
            this.detailVisible = false
            }
            this.pagination.page = 1
            this.loadData()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        })
      })
    },
    handleDetailPrint() {
      // 打印逻辑已在统一组件中处理
      this.loadData()
    },
    getCurrentApprover(row) {
      if (row.status === 'REJECTED') {
        return '-'
      }
      if (this.currentApproverMap[row.payoutId]) {
        return this.currentApproverMap[row.payoutId]
      }
      if (row.processInstanceId || row.payoutBillcode) {
        this.loadCurrentApprover(row)
        return '-'
      }
      return '-'
    },
    async loadCurrentApprover(row) {
      try {
        const taskKey = row.payoutBillcode
        if (taskKey) {
          const response = await getProcessTaskByTaskKey(taskKey)
          if (response.code === 200 && response.data && response.data.length > 0) {
            const allTasks = response.data
            const pendingTasks = allTasks.filter(task => task.taskStatus === 'PENDING')
            let currentTask = null
            
            if (pendingTasks.length > 0) {
              pendingTasks.sort((a, b) => {
                const aOrder = (a.printOrder != null ? a.printOrder : 999999)
                const bOrder = (b.printOrder != null ? b.printOrder : 999999)
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
              const approverName = currentTask.approverList || currentTask.assigneeUserName
              if (approverName && approverName !== '-') {
                this.$set(this.currentApproverMap, row.payoutId, approverName)
        return
      }
            }
          }
        }
        
        this.$set(this.currentApproverMap, row.payoutId, '-')
      } catch (error) {
        console.error('加载审批人失败:', error)
        this.$set(this.currentApproverMap, row.payoutId, '-')
      }
    },
    handleViewProcess(row) {
      this.currentProcessRow = row
                this.processVisible = true
    },
    async handleApprovalConfirm(opinion) {
      if (!this.currentApply || !this.currentApply.payoutId) {
        this.$message.error('申请信息不存在')
        this.approvalConfirmVisible = false
        return
      }
      
      try {
        const userInfo = this.$store.state.user.userInfo || {}
        const userId = userInfo.id || userInfo.userId || ''
        const response = await approvePayout(this.currentApply.payoutId, userId, opinion, null)
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
    async handleSaveDraft() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          try {
            // 确保mainAttachId有值
            if (!this.form.mainAttachId && !this.mainAttachId) {
              this.form.mainAttachId = Date.now().toString()
              this.mainAttachId = this.form.mainAttachId
            }
            const uploadedIds = await this.uploadAllFiles()
            this.uploadedAttachmentIds = this.uploadedAttachmentIds.concat(uploadedIds)
            
            if (!this.isEdit) {
              const userInfo = this.$store.state.user.userInfo || {}
              // 确保empId不为空，如果为空或为默认值1则尝试从API获取
              if (!this.form.empId || this.form.empId === 1) {
                let empId = userInfo.empId || userInfo.emp_id
                
                // 尝试通过userId获取
                if (!empId && (userInfo.userId || userInfo.id)) {
                  try {
                    const { getUserById } = await import('@/api/user')
                    const response = await getUserById(userInfo.userId || userInfo.id)
                    if (response.code === 200 && response.data) {
                      empId = response.data.empId || response.data.emp_id
                    }
                  } catch (error) {
                    console.error('获取用户信息失败', error)
                  }
                }
                
                // 尝试通过account获取
                if (!empId && userInfo.account) {
                  try {
                    const { getUserByAccount } = await import('@/api/user')
                    const response = await getUserByAccount(userInfo.account)
                    if (response.code === 200 && response.data) {
                      empId = response.data.empId || response.data.emp_id
                    }
                  } catch (error) {
                    console.error('通过account获取用户信息失败', error)
                  }
                }
                
                if (!empId) {
                  this.$message.error('无法获取员工ID，请检查用户信息或联系管理员')
                  return
                }
                this.form.empId = empId
              }
              this.form.empCode = userInfo.empCode || userInfo.emp_code || userInfo.account || this.form.empCode
              this.form.empName = userInfo.empName || userInfo.emp_name || userInfo.realName || userInfo.name || this.form.empName
              this.form.deptId = userInfo.deptId || userInfo.dept_id || this.form.deptId
            }
            
          const api = this.isEdit ? updatePayout : savePayout
            const response = await api({ 
              ...this.form, 
              status: 'DRAFT',
              budgetDetails: this.budgetDetailList // 将预算明细数据一起传给后端
            })
            if (response.code === 200) {
              if (response.data && response.data.payoutBillcode) {
                this.form.payoutBillcode = response.data.payoutBillcode
              }
              // 清空上传附件ID列表（已保存，不再需要删除）
              this.uploadedAttachmentIds = []
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
            // 确保mainAttachId有值
            if (!this.form.mainAttachId && !this.mainAttachId) {
              this.form.mainAttachId = Date.now().toString()
              this.mainAttachId = this.form.mainAttachId
            }
            const uploadedIds = await this.uploadAllFiles()
            this.uploadedAttachmentIds = this.uploadedAttachmentIds.concat(uploadedIds)
            
            if (!this.isEdit) {
              const userInfo = this.$store.state.user.userInfo || {}
              // 确保empId不为空，如果为空或为默认值1则尝试从API获取
              if (!this.form.empId || this.form.empId === 1) {
                let empId = userInfo.empId || userInfo.emp_id
                
                // 尝试通过userId获取
                if (!empId && (userInfo.userId || userInfo.id)) {
                  try {
                    const { getUserById } = await import('@/api/user')
                    const response = await getUserById(userInfo.userId || userInfo.id)
                    if (response.code === 200 && response.data) {
                      empId = response.data.empId || response.data.emp_id
                    }
          } catch (error) {
                    console.error('获取用户信息失败', error)
                  }
                }
                
                // 尝试通过account获取
                if (!empId && userInfo.account) {
                  try {
                    const { getUserByAccount } = await import('@/api/user')
                    const response = await getUserByAccount(userInfo.account)
                    if (response.code === 200 && response.data) {
                      empId = response.data.empId || response.data.emp_id
                    }
                  } catch (error) {
                    console.error('通过account获取用户信息失败', error)
                  }
                }
                
                if (!empId) {
                  this.$message.error('无法获取员工ID，请检查用户信息或联系管理员')
                  return
                }
                this.form.empId = empId
              }
              this.form.empCode = userInfo.empCode || userInfo.emp_code || userInfo.account || this.form.empCode
              this.form.empName = userInfo.empName || userInfo.emp_name || userInfo.realName || userInfo.name || this.form.empName
              this.form.deptId = userInfo.deptId || userInfo.dept_id || this.form.deptId
            }
            
            const api = this.isEdit ? updatePayout : savePayout
            const response = await api({ 
              ...this.form, 
              status: 'DRAFT',
              budgetDetails: this.budgetDetailList // 将预算明细数据一起传给后端
            })
            if (response.code !== 200) {
              this.$message.error(response.message || '保存失败')
          return
        }
            
            if (response.data && response.data.payoutBillcode) {
              this.form.payoutBillcode = response.data.payoutBillcode
            }
            
            // 清空上传附件ID列表（已保存，不再需要删除）
            this.uploadedAttachmentIds = []
            
            let payoutId = null
            if (response.data && response.data.payoutId) {
              payoutId = response.data.payoutId
            } else if (this.form.payoutId) {
              payoutId = this.form.payoutId
            }
            
            if (!payoutId) {
              this.$message.error('保存成功，但无法获取申请ID，无法提交')
              return
            }
            
            try {
              const submitResponse = await submitPayout(payoutId)
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
    // 通过templateConfigId获取业务类型（从sys_template_config表的business_type字段）
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
        return businessType
      } catch (error) {
        console.error('获取业务类型失败:', error)
        return null
      }
    },
    async uploadSingleFile(file) {
      try {
        // 使用mainAttachId作为businessId（时间戳），这样在保存之前就能确定business_id
        // 完全按照预算申请的方式处理
        const businessId = this.form.mainAttachId || (this.isEdit ? (this.form.payoutBillcode || null) : null)
        
        // 通过templateConfigId获取业务类型
        let businessType = 'PAYOUT_APPLY' // 默认值
        if (this.form.templateConfigId) {
          const dynamicBusinessType = await this.getBusinessTypeByTemplateConfigId(this.form.templateConfigId)
          if (dynamicBusinessType) {
            businessType = dynamicBusinessType
          }
        }
        
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
    async uploadAllFiles() {
      const filesToUpload = this.fileList.filter(f => {
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
    handleFileChange(file, fileList) {
      if (file.status === 'ready') {
        if (!file.raw && file.rawFile) {
          file.raw = file.rawFile
        }
        if (!file.raw) {
          file.raw = file
        }
        if (!file.hasOwnProperty('isUploaded')) {
          this.$set(file, 'isUploaded', false)
        }
        const fileToRead = file.raw || file
        if (fileToRead.type && fileToRead.type.startsWith('image/')) {
          const reader = new FileReader()
          reader.onload = (e) => {
            const index = fileList.findIndex(f => f.uid === file.uid)
            if (index > -1) {
              fileList[index].url = e.target.result
            }
          }
          reader.readAsDataURL(fileToRead)
        }
        this.fileList = fileList
        this.$message.success(`文件 "${file.name}" 已添加到列表，保存时将上传到服务器`)
      } else if (file.status === 'removed') {
        this.fileList = fileList
      }
    },
    async handlePreviewFile(file) {
      await this.handlePreviewAttachment(file)
    },
    async handlePreviewAttachment(attachment) {
      if (attachment.raw && (!attachment.isUploaded || attachment.isUploaded === false)) {
        if (attachment.url && attachment.url.startsWith('data:image/')) {
          this.previewLocalFile(attachment.url, attachment.name)
          return
        }
        if (attachment.raw && attachment.raw.type && attachment.raw.type.startsWith('image/')) {
          const reader = new FileReader()
          reader.onload = (e) => {
            const index = this.fileList.findIndex(f => f.uid === attachment.uid)
            if (index > -1) {
              this.$set(this.fileList[index], 'url', e.target.result)
            }
            this.previewLocalFile(e.target.result, attachment.name)
          }
          reader.readAsDataURL(attachment.raw)
          return
        }
        this.$message.info('该文件尚未上传，保存后可预览')
        return
      }
      
      let fileUrl = attachment.filePath || attachment.url
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
        fileUrl = this.getFileUrl(fileUrl)
      }
      
      if (!fileUrl) {
        this.$message.error('附件路径不存在')
        return
      }
      
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
      
      this.previewLocalFile(fileUrl, attachment.fileName || attachment.name || '')
    },
    previewLocalFile(fileUrl, fileName) {
      if (!fileUrl) {
        this.$message.error('文件路径不存在')
        return
      }
      
      const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp']
      const previewableExtensions = ['pdf', 'txt', 'html', 'htm']
      
      let fileExt = ''
      if (fileName && fileName.includes('.')) {
        fileExt = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()
      } else if (fileUrl && fileUrl.includes('.')) {
        const urlWithoutQuery = fileUrl.split('?')[0].split('#')[0]
        const lastDot = urlWithoutQuery.lastIndexOf('.')
        if (lastDot > 0) {
          fileExt = urlWithoutQuery.substring(lastDot + 1).toLowerCase().split('/')[0]
        }
      }
      
      const fileNameLower = (fileName || '').toLowerCase()
      const fileUrlLower = (fileUrl || '').toLowerCase()
      
      const isImage = fileUrl.startsWith('data:image/') || 
                     imageExtensions.includes(fileExt) ||
                     fileNameLower.match(/\.(jpg|jpeg|png|gif|bmp|webp)$/i) ||
                     fileUrlLower.match(/\.(jpg|jpeg|png|gif|bmp|webp)(\?|#|$)/i)
      
      const isPreviewable = previewableExtensions.includes(fileExt) ||
                           fileNameLower.match(/\.(pdf|txt|html|htm)$/i) ||
                           fileUrlLower.match(/\.(pdf|txt|html|htm)(\?|#|$)/i) ||
                           fileNameLower.includes('.pdf') ||
                           fileUrlLower.includes('.pdf') ||
                           fileUrlLower.includes('application/pdf')
      
      if (isImage) {
        this.$alert(`<img src="${fileUrl}" style="max-width: 100%; max-height: 500px; display: block; margin: 0 auto;" onerror="this.style.display='none'; this.nextElementSibling.style.display='block';" /><p style="display:none; text-align:center; color:red;">图片加载失败，URL: ${fileUrl}</p>`, '图片预览', {
          dangerouslyUseHTMLString: true,
          showConfirmButton: true,
          confirmButtonText: '关闭',
          customClass: 'image-preview-dialog',
          width: '600px'
        })
      } else if (isPreviewable) {
        window.open(fileUrl, '_blank')
      } else {
        this.$message.info('该文件类型不支持预览，请点击下载按钮下载后查看')
      }
    },
    beforeUpload(file) {
      const maxSize = 50 * 1024 * 1024
      if (file.size > maxSize) {
        this.$message.error('文件大小不能超过50MB')
        return false
      }
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
      return true
    },
    async handleRemove(file, fileList) {
      this.fileList = fileList
      let attachmentId = null
      if (file.response && file.response.data) {
        attachmentId = file.response.data
      } else if (file.attachmentId) {
        attachmentId = file.attachmentId
      } else if (file.uid && typeof file.uid === 'number') {
        attachmentId = file.uid
      }
      
      if (attachmentId) {
        const index = this.uploadedAttachmentIds.indexOf(attachmentId)
        if (index > -1) {
          this.uploadedAttachmentIds.splice(index, 1)
        }
        if (file.isUploaded) {
          try {
            await deleteAttachment(attachmentId)
          } catch (error) {
          }
        }
      }
    },
    async handleDialogCancel() {
      // 只有在新增模式下，且表单未保存（没有payoutBillcode）时，才删除已上传的附件
      // 编辑模式下或已保存的表单，附件应该保留（因为文件是在保存表单时才上传的）
      // 完全按照预算申请的方式处理
      if (!this.isEdit && !this.form.payoutBillcode) {
        // 如果上传了附件但没有保存表单，需要删除这些附件
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
      }
      
      // 重置文件列表和附件ID列表
      this.fileList = []
      this.uploadedAttachmentIds = []
      
      // 关闭对话框
      this.dialogVisible = false
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
    formatDateOnly(date) {
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
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportApply(false)
      } else if (command === 'all') {
        this.handleExportApply(true)
      }
    },
    // 导出申请数据
    async handleExportApply(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const empId = this.$store.state.user.userInfo.empId || 1
          const response = await getMyPayoutsPage(empId, 1, 10000)
          if (response.code === 200 && response.data) {
            let records = (response.data.records || []).filter(item => item.billType === 'APPLY' || !item.billType)
            
            // 应用筛选条件
            if (this.searchForm.payoutBillcode) {
              records = records.filter(item => 
                item.payoutBillcode && item.payoutBillcode.includes(this.searchForm.payoutBillcode)
              )
            }
            if (this.searchForm.payoutTypeId) {
              records = records.filter(item => item.payoutTypeId === this.searchForm.payoutTypeId)
            }
            if (this.searchForm.status) {
              records = records.filter(item => item.status === this.searchForm.status)
            }
            if (this.searchForm.applyDateRange && this.searchForm.applyDateRange.length === 2) {
              const startDate = this.searchForm.applyDateRange[0]
              const endDate = this.searchForm.applyDateRange[1]
              records = records.filter(item => {
                const applyDate = item.applyDate || item.createTime
                if (!applyDate) return false
                const dateStr = this.formatDateOnly(applyDate)
                return dateStr >= startDate && dateStr <= endDate
              })
            }
            
            dataToExport = records
          } else {
            this.$message.error(response.message || '获取数据失败')
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
        const headers = ['申请单号', '申请人', '科室', '申请类型', '申请金额', '状态', '审批人', '申请时间']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            item.payoutBillcode || '',
            item.empName || '',
            item.deptName || '',
            this.getPayoutTypeName(item.payoutTypeId),
            item.applyAmount ? '¥' + item.applyAmount : '',
            this.getStatusText(item.status),
            this.getCurrentApprover(item),
            this.formatDateOnly(item.applyDate || item.createTime)
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '我的申请' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
          headers: headers,
          dataList: dataList
        })
        
        // 处理blob响应并下载
        const blob = response.data
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '我的申请' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
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
.my-reimb-apply {
  padding: 20px;
}
</style>
