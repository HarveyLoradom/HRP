<template>
  <div class="business-apply">
    <el-card>
      <div slot="header" class="clearfix">
        <span>业务申请</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增申请</el-button>
      </div>
      
      <!-- 查询表单 -->
      <el-form :model="searchForm" :inline="true" style="margin-bottom: 20px;">
        <el-form-item label="申请单号:">
          <el-input v-model="searchForm.applyNo" placeholder="请输入申请单号" clearable style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item label="申请类型:">
          <el-select v-model="searchForm.hrApplyType" placeholder="全部" clearable style="width: 180px;">
            <el-option
              v-for="option in applyTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态:">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 150px;">
            <el-option
              v-for="option in statusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="申请时间:">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd"
            style="width: 240px;"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 数据表格 -->
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="applyNo" label="申请单号" width="180">
          <template slot-scope="scope">
            <el-button type="text" @click="handleViewDetail(scope.row)">{{ scope.row.applyNo }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="empName" label="申请人" width="120"></el-table-column>
        <el-table-column prop="hrApplyType" label="申请类型" width="120">
          <template slot-scope="scope">
            {{ getApplyTypeText(scope.row.hrApplyType) }}
          </template>
        </el-table-column>
        <el-table-column prop="hrApplySubType" label="申请子类型" width="120">
          <template slot-scope="scope">
            {{ getApplySubTypeName(scope.row.hrApplySubType, scope.row.hrApplyType) }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="applyDay" label="申请天数" width="100" align="center"></el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="审批人" width="120">
          <template slot-scope="scope">
            <span>{{ getCurrentApprover(scope.row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="流程" width="80">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleViewProcess(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination" style="margin-top: 20px; display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :total="pagination.total"
          :page-size="pagination.size"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
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
    
    <!-- 详情对话框 -->
    <BusinessApplyDetail
      v-model="detailDialogVisible"
      :apply-id="currentApplyId"
      source-type="apply"
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
      business-type-name="业务申请"
      :show-comment="true"
      :show-complete-time="true"
    />
    
    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="900px" @close="handleDialogCancel">
      <el-form :model="form" :rules="rules" ref="form" label-width="140px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="申请人:">
              <el-input v-model="applicantInfo.empName" disabled></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="科室:">
              <el-input v-model="applicantInfo.deptName" disabled></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="手机号:">
              <el-input v-model="applicantInfo.empPhone" disabled></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="申请类型:" prop="hrApplyType">
              <el-select v-model="form.hrApplyType" placeholder="请选择申请类型" style="width: 100%" @change="handleApplyTypeChange">
                <el-option
                  v-for="option in applyTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="申请子类型:" prop="hrApplySubType">
              <el-select v-model="form.hrApplySubType" placeholder="请选择申请子类型" style="width: 100%" :disabled="!form.hrApplyType">
                <el-option
                  v-for="option in currentSubTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="模板配置:" prop="templateConfigId">
              <el-select v-model="form.templateConfigId" placeholder="请选择模板配置" filterable style="width: 100%">
                <el-option
                  v-for="config in templateConfigOptions"
                  :key="config.configId"
                  :label="`${config.businessTypeName || config.businessTypeValue || config.businessType}`"
                  :value="config.configId"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="开始时间:" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
                format="yyyy-MM-dd HH:mm:ss"
                value-format="yyyy-MM-dd HH:mm:ss"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="结束时间:" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
                format="yyyy-MM-dd HH:mm:ss"
                value-format="yyyy-MM-dd HH:mm:ss"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="申请天数:">
              <el-input-number v-model="form.applyDay" :min="0" :precision="1" style="width: 100%" :disabled="true"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="是否护士:">
              <el-radio-group v-model="form.isNurse">
                <el-radio :label="1">是</el-radio>
                <el-radio :label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="申请事由:" prop="applyReason">
          <el-input type="textarea" v-model="form.applyReason" :rows="4" placeholder="请输入申请事由"></el-input>
        </el-form-item>
        <el-form-item label="附件:">
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
  </div>
</template>

<script>
import { getHrApplyPage, deleteHrApply, saveHrApply, updateHrApply, submitHrApply, getHrApplyById } from '@/api/hr'
import { getCodeTypeOptions } from '@/utils/codeType'
import { getProcessTaskByTaskKey } from '@/api/process'
import { getTemplateConfigPage } from '@/api/templateConfig'
import { uploadFile, deleteAttachment } from '@/api/attachment'
import { exportExcel } from '@/api/common'
import BusinessApplyDetail from './BusinessApplyDetail.vue'
import ProcessViewDialog from '@/components/ProcessViewDialog.vue'
import Cookies from 'js-cookie'

export default {
  name: 'BusinessApply',
  components: {
    BusinessApplyDetail,
    ProcessViewDialog
  },
  data() {
    return {
      loading: false,
      exportLoading: false,
      searchForm: {
        applyNo: '',
        hrApplyType: '',
        status: '',
        dateRange: null
      },
      tableData: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      applyTypeOptions: [],
      applySubTypeOptions: [],
      applySubTypeOptionsMap: {}, // 存储按申请类型分组的子类型选项
      statusOptions: [],
      detailDialogVisible: false,
      currentApplyId: null,
      processVisible: false,
      currentProcessRow: null,
      templateConfigMap: {},
      currentApproverMap: {},
      // 新增/编辑对话框相关
      dialogVisible: false,
      dialogTitle: '新增申请',
      isEdit: false,
      form: {
        applyId: null,
        hrApplyType: '',
        hrApplySubType: '',
        startTime: '',
        endTime: '',
        applyDay: 0,
        applyReason: '',
        mainAttachId: null,
        isNurse: 0,
        templateConfigId: null,
        status: 'DRAFT',
        empId: null
      },
      rules: {
        hrApplyType: [{ required: true, message: '请选择申请类型', trigger: 'change' }],
        startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
        endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
        templateConfigId: [{ required: true, message: '请选择模板配置', trigger: 'change' }]
      },
      applicantInfo: {
        empName: '',
        deptName: '',
        empPhone: ''
      },
      fileList: [],
      uploadedAttachmentIds: [],
      uploadUrl: '/api/auth/attachment/upload',
      uploadHeaders: {
        Authorization: 'Bearer ' + Cookies.get('token')
      },
      uploadData: {
        businessType: 'HR_APPLY'
      },
      mainAttachId: null,
      templateConfigOptions: [],
      currentSubTypeOptions: []
    }
  },
  watch: {
    'form.startTime'(newVal) {
      this.calculateApplyDay()
    },
    'form.endTime'(newVal) {
      this.calculateApplyDay()
    }
  },
  mounted() {
    this.loadCodeOptions()
    this.loadTemplateConfigs()
    this.loadData()
  },
  methods: {
    // 加载代码选项
    async loadCodeOptions() {
      try {
        this.applyTypeOptions = await getCodeTypeOptions('HR_APPLY_TYPE')
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
    // 加载模板配置
    async loadTemplateConfigs() {
      try {
        const res = await getTemplateConfigPage('HR_TYPE', 1, 1, 100)
        if (res.code === 200 && res.data && res.data.records) {
          this.templateConfigMap = res.data.records.reduce((map, config) => {
            map[config.configId] = config
            return map
          }, {})
        } else if (res.code === 200 && res.data && Array.isArray(res.data)) {
          // 兼容直接返回数组的情况
          this.templateConfigMap = res.data.reduce((map, config) => {
            map[config.configId] = config
            return map
          }, {})
        }
      } catch (error) {
        console.error('加载模板配置失败:', error)
      }
    },
    // 加载数据
    loadData() {
      this.loading = true
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        applyNo: this.searchForm.applyNo || null,
        empId: null, // 可以添加当前用户ID过滤
        hrApplyType: this.searchForm.hrApplyType || null,
        status: this.searchForm.status || null,
        startDate: this.searchForm.dateRange && this.searchForm.dateRange[0] || null,
        endDate: this.searchForm.dateRange && this.searchForm.dateRange[1] || null
      }
      
      // 只查询当前登录人的申请
      const userInfo = this.$store.state.user.userInfo || {}
      params.empId = userInfo.empId || null
      
      getHrApplyPage(params).then(res => {
        if (res.code === 200 && res.data) {
          this.tableData = res.data.records || res.data.list || []
          this.pagination.total = res.data.total || 0
          // 加载每个申请的当前审批人
          this.tableData.forEach(row => {
            if (row.processInstanceId || row.applyNo) {
              this.loadCurrentApprover(row)
            }
          })
        } else {
          this.$message.error(res.msg || '查询失败')
          this.tableData = []
          this.pagination.total = 0
        }
      }).catch(err => {
        console.error('查询失败:', err)
        this.$message.error('查询失败')
        this.tableData = []
        this.pagination.total = 0
      }).finally(() => {
        this.loading = false
      })
    },
    // 查询
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    // 重置
    handleReset() {
      this.searchForm = {
        applyNo: '',
        hrApplyType: '',
        status: '',
        dateRange: null
      }
      this.handleSearch()
    },
    // 分页大小变化
    handleSizeChange(size) {
      this.pagination.size = size
      this.loadData()
    },
    // 当前页变化
    handleCurrentChange(page) {
      this.pagination.page = page
      this.loadData()
    },
    // 新增申请
    handleAdd() {
      this.dialogTitle = '新增申请'
      this.isEdit = false
      this.resetForm()
      this.loadApplicantInfo()
      this.loadTemplateConfigOptions()
      this.dialogVisible = true
    },
    // 重置表单
    resetForm() {
      this.form = {
        applyId: null,
        hrApplyType: '',
        hrApplySubType: '',
        startTime: '',
        endTime: '',
        applyDay: 0,
        applyReason: '',
        mainAttachId: null,
        isNurse: 0,
        templateConfigId: null,
        status: 'DRAFT',
        empId: null
      }
      this.fileList = []
      this.uploadedAttachmentIds = []
      this.mainAttachId = null
      this.currentSubTypeOptions = []
      if (this.$refs.form) {
        this.$refs.form.clearValidate()
      }
      if (this.$refs.upload) {
        this.$refs.upload.clearFiles()
      }
    },
    // 加载申请人信息
    async loadApplicantInfo() {
      const userInfo = this.$store.state.user.userInfo || {}
      this.applicantInfo.empName = userInfo.empName || userInfo.emp_name || userInfo.name || ''
      this.applicantInfo.deptName = userInfo.deptName || userInfo.dept_name || ''
      this.applicantInfo.empPhone = userInfo.empPhone || userInfo.emp_phone || ''
      
      // 获取员工ID
      let empId = userInfo.empId || userInfo.emp_id
      if (!empId && userInfo.userId) {
        try {
          const { getUserById } = await import('@/api/user')
          const response = await getUserById(userInfo.userId)
          if (response.code === 200 && response.data) {
            empId = response.data.empId || response.data.emp_id
            this.applicantInfo.empName = response.data.name || this.applicantInfo.empName
            this.applicantInfo.deptName = response.data.deptName || this.applicantInfo.deptName
            this.applicantInfo.empPhone = response.data.empPhone || this.applicantInfo.empPhone
          }
        } catch (error) {
          console.error('获取用户信息失败:', error)
        }
      }
      this.form.empId = empId
    },
    // 加载模板配置选项
    async loadTemplateConfigOptions() {
      try {
        const res = await getTemplateConfigPage('HR_TYPE', 1, 1, 100)
        if (res.code === 200 && res.data) {
          this.templateConfigOptions = res.data.records || res.data.list || []
        }
      } catch (error) {
        console.error('加载模板配置失败:', error)
      }
    },
    // 申请类型变化
    async handleApplyTypeChange() {
      this.form.hrApplySubType = ''
      this.currentSubTypeOptions = []
      if (this.form.hrApplyType) {
        try {
          this.currentSubTypeOptions = await getCodeTypeOptions(this.form.hrApplyType + '_SUB_TYPE')
        } catch (error) {
          console.error('加载申请子类型失败:', error)
        }
      }
    },
    // 计算申请天数
    // 规则：9:00-18:00算一天（9小时），不足半天按半天算（4.5小时）
    calculateApplyDay() {
      if (!this.form.startTime || !this.form.endTime) {
        this.form.applyDay = 0
        return
      }
      
      const start = new Date(this.form.startTime)
      const end = new Date(this.form.endTime)
      
      if (end < start) {
        this.form.applyDay = 0
        return
      }
      
      // 标准工作时间：9:00-18:00（9小时）
      const STANDARD_WORK_HOURS = 9
      const HALF_DAY_HOURS = 4.5
      const WORK_START_HOUR = 9
      const WORK_END_HOUR = 18
      
      // 获取开始和结束的日期部分（年月日）
      const startDate = new Date(start.getFullYear(), start.getMonth(), start.getDate())
      const endDate = new Date(end.getFullYear(), end.getMonth(), end.getDate())
      
      // 计算日期差（天数）
      const dateDiff = Math.floor((endDate - startDate) / (1000 * 60 * 60 * 24))
      
      let totalDays = 0
      
      if (dateDiff === 0) {
        // 同一天：直接计算时间差
        const hours = (end - start) / (1000 * 60 * 60)
        if (hours >= STANDARD_WORK_HOURS) {
          totalDays = 1 // >= 9小时，算1天
        } else if (hours > HALF_DAY_HOURS) {
          totalDays = 1 // > 4.5小时且 < 9小时，算1天
        } else if (hours > 0) {
          totalDays = 0.5 // < 4.5小时，不足半天按半天算
        }
      } else {
        // 跨天：分别计算开始日期、结束日期和中间完整天数
        
        // 1. 计算开始日期的工作小时数
        const startHour = start.getHours() + start.getMinutes() / 60
        let startWorkHours = 0
        if (startHour < WORK_START_HOUR) {
          // 早于9点，从9点开始算
          startWorkHours = WORK_END_HOUR - WORK_START_HOUR // 9小时
        } else if (startHour >= WORK_START_HOUR && startHour < WORK_END_HOUR) {
          // 在9-18点之间
          startWorkHours = WORK_END_HOUR - startHour
        } else {
          // 晚于18点，不算
          startWorkHours = 0
        }
        
        let startDay = 0
        if (startWorkHours >= STANDARD_WORK_HOURS) {
          startDay = 1
        } else if (startWorkHours > HALF_DAY_HOURS) {
          startDay = 1 // > 4.5小时且 < 9小时，算1天
        } else if (startWorkHours > 0) {
          startDay = 0.5 // < 4.5小时，不足半天按半天算
        }
        
        // 2. 计算结束日期的工作小时数
        const endHour = end.getHours() + end.getMinutes() / 60
        let endWorkHours = 0
        if (endHour > WORK_END_HOUR) {
          // 晚于18点，算到18点
          endWorkHours = WORK_END_HOUR - WORK_START_HOUR // 9小时
        } else if (endHour > WORK_START_HOUR && endHour <= WORK_END_HOUR) {
          // 在9-18点之间
          endWorkHours = endHour - WORK_START_HOUR
        } else {
          // 早于9点，不算
          endWorkHours = 0
        }
        
        let endDay = 0
        if (endWorkHours >= STANDARD_WORK_HOURS) {
          endDay = 1
        } else if (endWorkHours > HALF_DAY_HOURS) {
          endDay = 1 // > 4.5小时且 < 9小时，算1天
        } else if (endWorkHours > 0) {
          endDay = 0.5 // < 4.5小时，不足半天按半天算
        }
        
        // 3. 中间完整的天数（每完整一天算1天）
        const middleDays = Math.max(0, dateDiff - 1)
        
        totalDays = startDay + middleDays + endDay
      }
      
      this.form.applyDay = parseFloat(totalDays.toFixed(1))
    },
    // 文件变化
    handleFileChange(file, fileList) {
      this.fileList = fileList
    },
    // 移除文件
    handleRemove(file, fileList) {
      this.fileList = fileList
      // 如果文件已上传，记录到待删除列表
      if (file.response && file.response.data) {
        this.uploadedAttachmentIds = this.uploadedAttachmentIds.filter(id => id !== file.response.data)
      }
    },
    // 上传前检查
    beforeUpload(file) {
      const isLt50M = file.size / 1024 / 1024 < 50
      if (!isLt50M) {
        this.$message.error('上传文件大小不能超过 50MB!')
      }
      return isLt50M
    },
    // 预览文件
    handlePreviewFile(file) {
      if (file.url) {
        window.open(file.url, '_blank')
      }
    },
    // 上传所有文件
    async uploadAllFiles() {
      if (!this.mainAttachId) {
        this.mainAttachId = Date.now().toString()
        this.form.mainAttachId = this.mainAttachId
      }
      this.uploadData.businessId = this.mainAttachId
      
      const uploadedIds = []
      for (const file of this.fileList) {
        if (!file.response) {
          // 文件还未上传
          try {
            const response = await uploadFile(file.raw, 'HR_APPLY', this.mainAttachId)
            if (response.code === 200 && response.data) {
              uploadedIds.push(response.data)
              file.response = response
            }
          } catch (error) {
            console.error('上传文件失败:', error)
            this.$message.error(`文件 ${file.name} 上传失败`)
          }
        } else if (file.response && file.response.data) {
          // 文件已上传
          uploadedIds.push(file.response.data)
        }
      }
      return uploadedIds
    },
    // 取消对话框
    handleDialogCancel() {
      // 删除已上传但未保存的附件
      if (this.uploadedAttachmentIds.length > 0) {
        this.uploadedAttachmentIds.forEach(id => {
          deleteAttachment(id).catch(err => console.error('删除附件失败:', err))
        })
      }
      this.dialogVisible = false
      this.resetForm()
    },
    // 保存草稿
    async handleSaveDraft() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          try {
            // 上传附件
            if (this.fileList.length > 0) {
              await this.uploadAllFiles()
            }
            
            // 确保mainAttachId有值
            if (!this.form.mainAttachId && !this.mainAttachId) {
              this.form.mainAttachId = Date.now().toString()
              this.mainAttachId = this.form.mainAttachId
            } else if (!this.form.mainAttachId) {
              this.form.mainAttachId = this.mainAttachId
            }
            
            const formData = {
              ...this.form,
              status: 'DRAFT'
            }
            
            const api = this.isEdit ? updateHrApply : saveHrApply
            const res = await api(formData)
            
            if (res.code === 200) {
              this.$message.success('保存成功')
              this.dialogVisible = false
              this.uploadedAttachmentIds = []
              this.loadData()
            } else {
              this.$message.error(res.msg || '保存失败')
            }
          } catch (error) {
            console.error('保存失败:', error)
            this.$message.error('保存失败：' + (error.message || '未知错误'))
          }
        }
      })
    },
    // 保存并提交
    async handleSaveAndSubmit() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          try {
            // 上传附件
            if (this.fileList.length > 0) {
              await this.uploadAllFiles()
            }
            
            // 确保mainAttachId有值
            if (!this.form.mainAttachId && !this.mainAttachId) {
              this.form.mainAttachId = Date.now().toString()
              this.mainAttachId = this.form.mainAttachId
            } else if (!this.form.mainAttachId) {
              this.form.mainAttachId = this.mainAttachId
            }
            
            let applyId = this.form.applyId
            
            // 先保存
            if (!this.isEdit || !applyId) {
              const formData = {
                ...this.form,
                status: 'DRAFT'
              }
              const saveRes = await saveHrApply(formData)
              if (saveRes.code === 200 && saveRes.data) {
                applyId = saveRes.data.applyId
              } else {
                this.$message.error(saveRes.msg || '保存失败')
                return
              }
            } else {
              const formData = {
                ...this.form,
                status: 'DRAFT'
              }
              const updateRes = await updateHrApply(formData)
              if (updateRes.code !== 200) {
                this.$message.error(updateRes.msg || '更新失败')
                return
              }
            }
            
            // 提交
            const submitRes = await submitHrApply(applyId)
            if (submitRes.code === 200) {
              this.$message.success('提交成功')
              this.dialogVisible = false
              this.uploadedAttachmentIds = []
              this.loadData()
            } else {
              this.$message.error(submitRes.msg || '提交失败')
            }
          } catch (error) {
            console.error('保存并提交失败:', error)
            this.$message.error('保存并提交失败：' + (error.message || '未知错误'))
          }
        }
      })
    },
    // 查看详情
    handleViewDetail(row) {
      this.currentApplyId = row.applyId
      this.detailDialogVisible = true
    },
    // 详情页编辑
    async handleDetailEdit(applyId) {
      this.detailDialogVisible = false
      // 打开编辑对话框
      this.dialogTitle = '编辑申请'
      this.isEdit = true
      this.resetForm()
      this.loadApplicantInfo()
      this.loadTemplateConfigOptions()
      
      // 加载申请详情
      try {
        const res = await getHrApplyById(applyId)
        if (res.code === 200 && res.data) {
          const data = res.data
          this.form = {
            applyId: data.applyId,
            hrApplyType: data.hrApplyType || '',
            hrApplySubType: data.hrApplySubType || '',
            startTime: data.startTime ? (typeof data.startTime === 'string' ? data.startTime : data.startTime.substring(0, 19).replace('T', ' ')) : '',
            endTime: data.endTime ? (typeof data.endTime === 'string' ? data.endTime : data.endTime.substring(0, 19).replace('T', ' ')) : '',
            applyDay: data.applyDay || 0,
            applyReason: data.applyReason || '',
            mainAttachId: data.mainAttachId || null,
            isNurse: data.isNurse !== undefined && data.isNurse !== null ? data.isNurse : 0,
            templateConfigId: data.templateConfigId || null,
            status: data.status || 'DRAFT',
            empId: data.empId || null
          }
          this.mainAttachId = data.mainAttachId
          
          // 加载申请子类型选项（先保存当前的hrApplySubType，加载选项后再恢复）
          const savedSubType = this.form.hrApplySubType
          if (this.form.hrApplyType) {
            await this.handleApplyTypeChange()
            // 恢复申请子类型
            if (savedSubType) {
              this.form.hrApplySubType = savedSubType
            }
          }
          
          // 加载附件列表
          if (data.mainAttachId) {
            try {
              const { getAttachmentsByBusiness } = await import('@/api/attachment')
              const attachRes = await getAttachmentsByBusiness('HR_APPLY', data.mainAttachId)
              if (attachRes.code === 200 && attachRes.data) {
                this.fileList = attachRes.data.map(att => ({
                  name: att.fileName,
                  url: att.filePath,
                  response: { data: { attachmentId: att.attachmentId } }
                }))
              }
            } catch (error) {
              console.error('加载附件失败:', error)
            }
          }
        }
      } catch (error) {
        console.error('加载申请详情失败:', error)
        this.$message.error('加载申请详情失败')
        return
      }
      
      this.dialogVisible = true
    },
    // 详情页提交后刷新
    handleDetailSubmitted() {
      this.detailDialogVisible = false
      this.loadData()
    },
    // 详情页撤回后刷新
    handleDetailWithdrawn() {
      this.detailDialogVisible = false
      this.loadData()
    },
    // 详情页删除后刷新
    handleDetailDeleted() {
      this.detailDialogVisible = false
      this.loadData()
    },
    // 详情页打印
    handleDetailPrint(row) {
      // TODO: 实现打印逻辑
      this.$message.info('打印功能待实现')
    },
    // 查看流程
    handleViewProcess(row) {
      this.currentProcessRow = row
      this.processVisible = true
    },
    // 获取当前审批人
    getCurrentApprover(row) {
      if (row.status === 'REJECTED') {
        return '-'
      }
      if (this.currentApproverMap[row.applyId]) {
        return this.currentApproverMap[row.applyId]
      }
      if (row.processInstanceId || row.applyNo) {
        this.loadCurrentApprover(row)
        return '-'
      }
      return '-'
    },
    async loadCurrentApprover(row) {
      try {
        const taskKey = row.applyNo
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
                this.$set(this.currentApproverMap, row.applyId, approverName)
                return
              }
            }
          }
        }
        
        this.$set(this.currentApproverMap, row.applyId, '-')
      } catch (error) {
        console.error('加载审批人失败:', error)
        this.$set(this.currentApproverMap, row.applyId, '-')
      }
    },
    // 删除
    handleDelete(row) {
      this.$confirm('确定要删除该申请吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteHrApply(row.applyId).then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(res.msg || '删除失败')
          }
        }).catch(err => {
          console.error('删除失败:', err)
          this.$message.error('删除失败')
        })
      })
    },
    // 获取申请类型文本
    getApplyTypeText(type) {
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
    // 获取状态文本
    getStatusText(status) {
      const option = this.statusOptions.find(item => item.value === status)
      return option ? option.label : status
    },
    // 获取状态类型
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
    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      if (typeof dateTime === 'string') {
        return dateTime.substring(0, 19).replace('T', ' ')
      }
      return dateTime
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportBusiness(false)
      } else if (command === 'all') {
        this.handleExportBusiness(true)
      }
    },
    // 导出业务申请数据
    async handleExportBusiness(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const userInfo = this.$store.state.user.userInfo || {}
          const userId = userInfo.userId || userInfo.id
          
          if (!userId) {
            this.$message.error('未获取到当前用户信息')
            return
          }
          
          const params = {
            page: 1,
            size: 10000, // 设置一个很大的值以获取所有数据
            approver: false, // 申请人视角
            applyNo: this.searchForm.applyNo || null,
            empName: userInfo.empName || null,
            hrApplyType: this.searchForm.hrApplyType || null,
            status: this.searchForm.status || null,
            startDate: this.searchForm.dateRange && this.searchForm.dateRange[0] || null,
            endDate: this.searchForm.dateRange && this.searchForm.dateRange[1] || null
          }
          
          const res = await getHrApplyPage(params)
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
        const headers = ['申请单号', '申请人', '部门', '申请类型', '申请子类型','开始时间', '结束时间', '申请天数', '状态', '申请时间']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            String(item.applyNo || ''),
            String(item.empName || ''),
            String(item.deptName || ''),
            String(this.getApplyTypeText(item.hrApplyType) || ''),
            String(this.getApplySubTypeName(item.hrApplySubType, item.hrApplyType) || ''),
            String(item.startTime ? this.formatDateTime(item.startTime) : ''),
            String(item.endTime ? this.formatDateTime(item.endTime) : ''),
            String(item.applyDay ? item.applyDay + ' 天' : '0 天'),
            String(this.getStatusText(item.status) || ''),
            String(this.formatDateTime(item.createTime) || '')
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '业务申请' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
          headers: headers,
          dataList: dataList
        })
        
        // 处理blob响应并下载
        const blob = response.data
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '业务申请' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
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
.business-apply {
  padding: 20px;
}
</style>
