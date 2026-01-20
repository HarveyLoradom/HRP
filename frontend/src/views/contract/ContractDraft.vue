<template>
  <div class="contract-draft">
    <el-card>
      <div slot="header" class="clearfix">
        <span>合同起草</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增合同</el-button>
      </div>
      
      <!-- 查询表单 -->
      <el-form :model="searchForm" style="margin-bottom: 20px;">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="合同编号:" label-width="100px">
              <el-input v-model="searchForm.contractNo" placeholder="请输入合同编号" clearable style="width: 100%;"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="合同名称:" label-width="100px">
              <el-input v-model="searchForm.contractName" placeholder="请输入合同名称" clearable style="width: 100%;"></el-input>
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
            <el-form-item label="合同类型:" label-width="100px">
              <el-select v-model="searchForm.contractType" placeholder="请选择合同类型" clearable style="width: 100%;">
                <el-option
                  v-for="option in contractTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                ></el-option>
              </el-select>
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
        <el-table-column label="合同编号" width="160">
          <template slot-scope="scope">
            <el-button type="text" @click="handleViewDetail(scope.row)">{{ scope.row.contractNo }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="contractName" label="合同名称" width="200"></el-table-column>
        <el-table-column prop="contractType" label="合同类型" width="120">
          <template slot-scope="scope">
            {{ getContractTypeName(scope.row.contractType) }}
          </template>
        </el-table-column>
        <el-table-column prop="partyA" label="甲方" width="150"></el-table-column>
        <el-table-column prop="partyB" label="乙方" width="150"></el-table-column>
        <el-table-column prop="contractAmount" label="合同金额" width="120">
          <template slot-scope="scope">¥{{ scope.row.contractAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="empName" label="申请人" width="130">
          <template slot-scope="scope">
            {{ scope.row.empName || scope.row.empId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="科室" width="130">
          <template slot-scope="scope">
            {{ scope.row.deptName || scope.row.deptId || '-' }}
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
        <el-table-column prop="createTime" label="申请时间" width="160">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
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
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="1200px" @open="initApplicantInfo">
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
          <el-col :span="8">
        <el-form-item label="合同类型" prop="contractType">
          <el-select v-model="form.contractType" placeholder="请选择合同类型" style="width: 100%">
            <el-option
              v-for="option in contractTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            ></el-option>
          </el-select>
        </el-form-item>
          </el-col>
          <el-col :span="8">
        <el-form-item label="合同名称" prop="contractName">
              <el-input v-model="form.contractName" placeholder="请输入合同名称" style="width: 100%;"></el-input>
        </el-form-item>
          </el-col>
          <el-col :span="8">
        <el-form-item label="甲方" prop="partyA">
              <el-input v-model="form.partyA" placeholder="请输入甲方" style="width: 100%;"></el-input>
        </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
        <el-form-item label="乙方" prop="partyB">
              <el-input v-model="form.partyB" placeholder="请输入乙方" style="width: 100%;">
                <el-button slot="append" icon="el-icon-search" @click="handleOpenSupplierDialog"></el-button>
              </el-input>
        </el-form-item>
          </el-col>
          <el-col :span="8">
        <el-form-item label="合同金额" prop="contractAmount">
          <el-input-number v-model="form.contractAmount" :min="0" :precision="2" style="width: 100%"></el-input-number>
        </el-form-item>
          </el-col>
          <el-col :span="8">
        <el-form-item label="签订日期" prop="signDate">
          <el-date-picker v-model="form.signDate" type="datetime" placeholder="选择签订日期" style="width: 100%"></el-date-picker>
        </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="datetime" placeholder="选择开始日期" style="width: 100%"></el-date-picker>
        </el-form-item>
          </el-col>
          <el-col :span="8">
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="datetime" placeholder="选择结束日期" style="width: 100%"></el-date-picker>
        </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="模板配置" prop="templateConfigId">
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
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="form.remark" :rows="3" placeholder="请输入备注"></el-input>
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

    <!-- 流程信息对话框 -->
    <!-- 流程查看对话框 -->
    <ProcessViewDialog
      :visible.sync="processVisible"
      :row="currentProcessRow"
      :template-config-map="templateConfigMap"
      business-key-field="contractNo"
      business-type-name="合同"
      :show-comment="true"
      :show-complete-time="true"
    />

    <!-- 统一详情组件 -->
    <ContractDetail
      v-if="selectedContractId"
      v-model="detailVisible"
      source-type="draft"
      :contract-id="selectedContractId"
      @edit="handleDetailEdit"
      @submitted="handleDetailSubmitted"
      @withdrawn="handleDetailWithdrawn"
      @deleted="handleDetailDeleted"
      @print="handleDetailPrint"
    />

    <!-- 供应商选择对话框 -->
    <!-- 供应商管理对话框 -->
    <el-dialog title="供应商管理" :visible.sync="supplierDialogVisible" width="1000px">
      <!-- 查询表单 -->
      <el-form :model="supplierSearchForm" :inline="true" style="margin-bottom: 15px;">
        <el-form-item label="供应商编码:">
          <el-input v-model="supplierSearchForm.supplierCode" placeholder="请输入供应商编码" clearable style="width: 180px;"></el-input>
        </el-form-item>
        <el-form-item label="供应商名称:">
          <el-input v-model="supplierSearchForm.supplierName" placeholder="请输入供应商名称" clearable style="width: 180px;"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSupplierSearch">查询</el-button>
          <el-button @click="handleSupplierSearchReset">重置</el-button>
        </el-form-item>
      </el-form>
      <div style="margin-bottom: 10px;">
        <el-button type="primary" size="small" @click="handleAddSupplier">新增供应商</el-button>
      </div>
      <el-table 
        :data="supplierList" 
        border 
        style="width: 100%;" 
        v-loading="supplierLoading"
        @row-click="handleSelectSupplierRow"
        highlight-current-row
      >
        <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
        <el-table-column prop="supplierCode" label="供应商编码" width="150"></el-table-column>
        <el-table-column prop="supplierName" label="供应商名称" width="200"></el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="120"></el-table-column>
        <el-table-column prop="contactPhone" label="联系电话" width="150"></el-table-column>
        <el-table-column prop="bankName" label="银行名称" width="150"></el-table-column>
        <el-table-column prop="bankAccount" label="银行账号" width="180"></el-table-column>
        <el-table-column prop="accountName" label="账户名称" width="150"></el-table-column>
        <el-table-column prop="isStop" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isStop === 0 ? 'success' : 'danger'">
              {{ scope.row.isStop === 0 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click.stop="handleSelectSupplier(scope.row)">选择</el-button>
            <el-button size="mini" @click.stop="handleEditSupplier(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click.stop="handleDeleteSupplier(scope.row)">删除</el-button>
            <el-button 
              v-if="scope.row.isStop === 0" 
              size="mini" 
              type="warning" 
              @click.stop="handleStopSupplier(scope.row)"
            >停用</el-button>
            <el-button 
              v-if="scope.row.isStop === 1" 
              size="mini" 
              type="success" 
              @click.stop="handleStartSupplier(scope.row)"
            >启用</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="supplierDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleConfirmSelectedSupplier" 
          :disabled="!selectedSupplier"
        >
          确认选择{{ selectedSupplier ? `（已选择：${selectedSupplier.supplierName}）` : '' }}
        </el-button>
      </div>
    </el-dialog>

    <!-- 供应商表单对话框 -->
    <el-dialog :title="supplierFormTitle" :visible.sync="supplierFormVisible" width="800px">
      <el-form :model="supplierForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="供应商编码" required>
              <el-input v-model="supplierForm.supplierCode" placeholder="请输入供应商编码"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="供应商名称" required>
              <el-input v-model="supplierForm.supplierName" placeholder="请输入供应商名称"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系人">
              <el-input v-model="supplierForm.contactPerson" placeholder="请输入联系人"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="联系电话">
              <el-input v-model="supplierForm.contactPhone" placeholder="请输入联系电话"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系邮箱">
              <el-input v-model="supplierForm.contactEmail" placeholder="请输入联系邮箱"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="地址">
              <el-input v-model="supplierForm.address" placeholder="请输入地址"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="银行名称">
              <el-input v-model="supplierForm.bankName" placeholder="请输入银行名称"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="银行账号">
              <el-input v-model="supplierForm.bankAccount" placeholder="请输入银行账号"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="账户名称">
              <el-input v-model="supplierForm.accountName" placeholder="请输入账户名称"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="税号">
              <el-input v-model="supplierForm.taxNumber" placeholder="请输入税号"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="supplierFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveSupplier">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAllContractsPage, getContractsPage, saveContract, updateContract, submitContractByNo, approveContract, rejectContract } from '@/api/contract'
import { getCodeTypeOptions } from '@/utils/codeType'
import { paginationMixin } from '@/mixins/pagination'
import ApprovalConfirmDialog from '@/components/ApprovalConfirmDialog.vue'
import ContractDetail from '@/views/contract/ContractDetail.vue'
import ProcessViewDialog from '@/components/ProcessViewDialog.vue'
import { getNextNodeInfoByBusinessKey, getProcessDefinitionById, getProcessInstanceVariables, getProcessTaskByBusinessKey, getProcessTaskByTaskKey, getProcessNodes, getProcessNodesWithBusiness } from '@/api/process'
import { getTemplateConfigByBusinessTypeOnly, getTemplateConfigList, getTemplateConfigById } from '@/api/templateConfig'
import { getAttachmentsByBusinessId, deleteAttachment, getAttachment, uploadFile } from '@/api/attachment'
import { getSuppliers, saveSupplier, updateSupplier, deleteSupplier, stopSupplier, startSupplier } from '@/api/reimb'
import { exportExcel } from '@/api/common'
import Cookies from 'js-cookie'

export default {
  name: 'ContractDraft',
  mixins: [paginationMixin],
  components: {
    ApprovalConfirmDialog,
    ContractDetail,
    ProcessViewDialog
  },
  data() {
    return {
      loading: false,
      exportLoading: false,
      tableData: [],
      allData: [],
      contractTypeOptions: [],
      applyStatusOptions: [],
      executionStatusOptions: [],
      searchForm: {
        contractNo: '',
        contractName: '',
        status: '',
        contractType: ''
      },
      processVisible: false,
      processTab: 'nodes',
      currentProcessInstance: null,
      currentProcessRow: null,
      processVariables: [],
      templateConfigOptions: [],
      templateConfigMap: {},
      detailVisible: false, // 详情查看对话框
      selectedContractId: null,
      fileList: [],
      uploadedAttachmentIds: [],
      // 以下属性用于表单编辑中的附件预览
      imagePreviewVisible: false,
      previewImageUrl: '',
      filePreviewVisible: false,
      previewFileUrl: '',
      approvalConfirmVisible: false,
      nextNodeInfo: null,
      currentContract: {},
      currentApproverMap: {},
      dialogVisible: false,
      supplierDialogVisible: false,
      supplierList: [],
      supplierLoading: false,
      selectedSupplier: null, // 选中的供应商（单选）
      supplierSearchForm: { // 供应商查询表单
        supplierCode: '',
        supplierName: ''
      },
      supplierForm: { // 供应商表单
        supplierId: null,
        supplierCode: '',
        supplierName: '',
        contactPerson: '',
        contactPhone: '',
        contactEmail: '',
        address: '',
        bankName: '',
        bankAccount: '',
        accountName: '',
        taxNumber: '',
        isStop: 0
      },
      supplierFormVisible: false, // 供应商表单对话框
      supplierFormTitle: '新增供应商',
      dialogTitle: '新增合同',
      isEdit: false,
      applicantInfo: {
        empName: '',
        deptName: '',
        empPhone: ''
      },
      form: {
        pactId: null,
        contractNo: '',
        contractName: '',
        contractType: '',
        partyA: '',
        partyB: '',
        contractAmount: 0,
        signDate: null,
        startDate: null,
        endDate: null,
        remark: '',
        status: 'DRAFT',
        executionStatus: '',
        isManualModify: 0,
        deptId: null,
        empId: null,
        templateConfigId: null,
        mainAttachId: null
      },
      rules: {
        contractType: [{ required: true, message: '请选择合同类型', trigger: 'change' }],
        contractName: [{ required: true, message: '请输入合同名称', trigger: 'blur' }],
        partyA: [{ required: true, message: '请输入甲方', trigger: 'blur' }],
        partyB: [{ required: true, message: '请输入乙方', trigger: 'blur' }],
        contractAmount: [{ required: true, message: '请输入合同金额', trigger: 'blur' }],
        signDate: [
          { required: true, message: '请选择签订日期', trigger: 'change' }
        ],
        startDate: [
          { required: true, message: '请选择开始日期', trigger: 'change' },
          {
            validator: (rule, value, callback) => {
              if (!value) {
                callback()
                return
              }
              const signDate = this.form.signDate
              if (signDate && new Date(value) < new Date(signDate)) {
                callback(new Error('开始时间应晚于或等于签订时间'))
              } else {
                callback()
              }
            },
            trigger: 'change'
          }
        ],
        endDate: [
          { required: true, message: '请选择结束日期', trigger: 'change' },
          {
            validator: (rule, value, callback) => {
              if (!value) {
                callback()
                return
              }
              const startDate = this.form.startDate
              if (startDate && new Date(value) <= new Date(startDate)) {
                callback(new Error('结束时间应晚于开始时间'))
              } else {
                callback()
              }
            },
            trigger: 'change'
          }
        ],
        templateConfigId: [{ required: true, message: '请选择模板配置', trigger: 'change' }]
      }
    }
  },
  mounted() {
    this.loadCodeTypeOptions()
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
      this.contractTypeOptions = await getCodeTypeOptions('CONTRACT_TYPE')
      this.applyStatusOptions = await getCodeTypeOptions('APPLY_STATUS')
      this.executionStatusOptions = await getCodeTypeOptions('EXECUTION_STATUS')
    },
    loadTemplateConfigs() {
      getTemplateConfigByBusinessTypeOnly('CONTRACT_TYPE').then(response => {
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
      const config = this.templateConfigMap[configId]
      if (config) {
        // 可以在这里设置流程定义ID等
      }
    },
    loadData() {
      this.loading = true
      // 使用带条件的查询接口，这样后端会返回员工和部门名称
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        contractNo: this.searchForm.contractNo || null,
        contractName: this.searchForm.contractName || null,
        contractType: this.searchForm.contractType || null,
        
        status: this.searchForm.status || null
      }
      // 使用 getContractsPage 接口（带条件查询，会返回 empName 和 deptName）
      getContractsPage(params).then(response => {
        if (response.code === 200 && response.data) {
          const data = response.data.records || []
          this.tableData = data
          this.pagination.total = response.data.total || 0
          // 加载每个合同的当前审批人
          this.tableData.forEach(row => {
            if (row.processInstanceId || row.contractNo) {
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
        contractNo: '',
        contractName: '',
        status: '',
        contractType: ''
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
    getContractTypeName(codeValue) {
      const option = this.contractTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    getExecutionStatusName(codeValue) {
      if (!codeValue) return '-'
      const option = this.executionStatusOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    getExecutionStatusType(codeValue) {
      // 根据执行状态返回对应的标签类型
      if (!codeValue) return 'info'
      const status = codeValue.toUpperCase()
      if (status === 'PENDING_EXECUTION' || status === '待履约') return 'warning'
      if (status === 'EXECUTING' || status === '履约中') return 'success'
      if (status === 'COMPLETED' || status === '已履约') return 'success'
      if (status === 'ARCHIVED' || status === '已归档') return 'info'
      if (status === 'INVALID' || status === '已失效') return 'danger'
      return 'info'
    },
    handleAdd() {
      this.dialogTitle = '新增合同'
      this.isEdit = false
      const mainAttachId = Date.now().toString()
      const userInfo = this.$store.state.user.userInfo || {}
      // 默认执行状态为待履约（PENDING_EXECUTION），如果选项中没有则取第一个
      let defaultExecutionStatus = 'PENDING_EXECUTION'
      const pendingExecutionOption = this.executionStatusOptions.find(opt => opt.value === 'PENDING_EXECUTION')
      if (!pendingExecutionOption && this.executionStatusOptions.length > 0) {
        defaultExecutionStatus = this.executionStatusOptions[0].value
      }
      this.form = {
        pactId: null,
        contractNo: '',
        contractName: '',
        contractType: '',
        partyA: '',
        partyB: '',
        contractAmount: 0,
        signDate: null,
        startDate: null,
        endDate: null,
        remark: '',
        status: 'DRAFT',
        executionStatus: defaultExecutionStatus,
        isManualModify: 0,
        deptId: userInfo.deptId || userInfo.dept_id || null,
        empId: userInfo.empId || userInfo.emp_id || null,
        templateConfigId: null,
        mainAttachId: mainAttachId
      }
      this.fileList = []
      this.uploadedAttachmentIds = []
      this.dialogVisible = true
    },
    async handleEdit(row) {
      if (row.status !== 'DRAFT' && row.status !== 'WITHDRAWN' && row.status !== 'REJECTED') {
        this.$message.warning('只有草稿、已撤回或已拒绝状态的合同才能编辑')
        return
      }
      this.dialogTitle = '编辑合同'
      this.isEdit = true
      this.form = { ...row }
      if (!this.form.mainAttachId) {
        this.form.mainAttachId = Date.now().toString()
      }
      this.fileList = []
      this.uploadedAttachmentIds = []
      await this.loadAttachments(this.form.mainAttachId)
      this.dialogVisible = true
    },
    async loadAttachments(mainAttachId) {
      if (!mainAttachId) {
        return
      }
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
        // 静默处理错误
      }
    },
    async handleSaveDraft() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          try {
            const uploadedIds = await this.uploadAllFiles()
            this.uploadedAttachmentIds = this.uploadedAttachmentIds.concat(uploadedIds)
            
            const userInfo = this.$store.state.user.userInfo || {}
            if (!this.isEdit) {
              this.form.empId = userInfo.empId || userInfo.emp_id || this.form.empId
              this.form.deptId = userInfo.deptId || userInfo.dept_id || this.form.deptId
              this.form.createUser = userInfo.account || userInfo.username || ''
            }
            
          const api = this.isEdit ? updateContract : saveContract
            const response = await api({ ...this.form, status: 'DRAFT' })
            if (response.code === 200) {
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
            
            const userInfo = this.$store.state.user.userInfo || {}
            if (!this.isEdit) {
              this.form.empId = userInfo.empId || userInfo.emp_id || this.form.empId
              this.form.deptId = userInfo.deptId || userInfo.dept_id || this.form.deptId
              this.form.createUser = userInfo.account || userInfo.username || ''
            }
            
            const api = this.isEdit ? updateContract : saveContract
            const response = await api({ ...this.form, status: 'DRAFT' })
            if (response.code !== 200) {
              this.$message.error(response.message || '保存失败')
              return
            }
            
            // 保存成功后，获取合同编号（优先从响应中获取，如果没有则使用表单中的contractNo）
            let contractNo = null
            if (response.data && response.data.contractNo) {
              contractNo = response.data.contractNo
            } else if (this.form.contractNo) {
              contractNo = this.form.contractNo
            }
            
            if (!contractNo) {
              this.$message.error('保存成功，但无法获取合同编号，无法提交')
              return
            }
            
            try {
              // 使用合同编号提交（task_key也是合同编号）
              const submitResponse = await submitContractByNo(contractNo)
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
    async handleDialogCancel() {
      const uploadedFiles = this.fileList.filter(f => f.isUploaded && f.attachmentId)
      if (uploadedFiles.length > 0) {
        for (const file of uploadedFiles) {
          try {
            await deleteAttachment(file.attachmentId)
          } catch (error) {
            // 静默处理删除失败
          }
        }
      }
      
      this.fileList = []
      this.uploadedAttachmentIds = []
      this.dialogVisible = false
    },
    async getBusinessTypeByTemplateConfigId(templateConfigId) {
      if (!templateConfigId) {
        return 'CONTRACT'
      }
      try {
        const templateConfigResponse = await getTemplateConfigById(templateConfigId)
        if (templateConfigResponse.code !== 200 || !templateConfigResponse.data) {
          return 'CONTRACT'
        }
        const templateConfig = templateConfigResponse.data
        return templateConfig.businessType || 'CONTRACT'
      } catch (error) {
        return 'CONTRACT'
      }
    },
    async uploadSingleFile(file) {
      try {
        const businessId = this.form.mainAttachId || (this.isEdit ? (this.form.contractNo || null) : null)
        let businessType = 'CONTRACT'
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
            // 静默处理删除失败
          }
        }
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
        this.previewImageUrl = fileUrl
        this.imagePreviewVisible = true
      } else if (isPreviewable) {
        this.previewFileUrl = fileUrl
        this.filePreviewVisible = true
      } else {
        this.$message.info('该文件类型不支持预览，请点击下载按钮下载后查看')
      }
    },
    handleImageError(event) {
      this.$message.error('图片加载失败')
      this.imagePreviewVisible = false
    },
    handleFilePreviewClose() {
      this.previewFileUrl = ''
      this.filePreviewVisible = false
    },
    handleDownloadAttachment(attachment) {
      let fileUrl = attachment.filePath || attachment.url
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
      if (!date) return '-'
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    async handleApprovalConfirm(opinion) {
      if (!this.currentContract || !this.currentContract.pactId) {
        this.$message.error('合同信息不存在')
        this.approvalConfirmVisible = false
        return
      }
      
      try {
        const userInfo = this.$store.state.user.userInfo || {}
        const userId = userInfo.userId || userInfo.user_id || userInfo.account || userInfo.username || ''
        const response = await approveContract(this.currentContract.pactId, userId, opinion)
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
    async handleApprove(row) {
      if (!row || !row.pactId) {
        this.$message.warning('合同信息不存在')
        return
      }
      
      this.currentContract = row
      
      try {
        const businessKey = row.contractNo
        if (!businessKey) {
          this.$message.warning('合同编号不存在')
          return
        }
        
        const nextNodeResponse = await getNextNodeInfoByBusinessKey(businessKey)
        if (nextNodeResponse.code === 200 && nextNodeResponse.data) {
          this.nextNodeInfo = nextNodeResponse.data
          this.approvalConfirmVisible = true
        } else {
          this.nextNodeInfo = null
          this.approvalConfirmVisible = true
        }
      } catch (error) {
        this.nextNodeInfo = null
        this.approvalConfirmVisible = true
      }
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
    handleViewProcess(row) {
      this.currentProcessRow = row
      this.processVisible = true
    },
    getAssigneeTypeName(assigneeType) {
      const typeMap = {
        'user': '指定用户',
        'position': '指定岗位',
        'dept': '部门负责人',
        'manage_dept': '归口审批人',
        'initiator': '发起人',
        'previous': '上一节点审批人',
        'responsible': '负责人'
      }
      return typeMap[assigneeType] || assigneeType || '-'
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
      if (!status) return 'info'
      const typeMap = {
        'COMPLETED': 'success',
        'RETURNED': 'warning',
        'PENDING': 'warning',
        'TERMINATED': 'danger',
        'TRANSFERRED': 'info'
      }
      return typeMap[status] || 'info'
    },
    getCurrentApprover(row) {
      // 已拒绝的合同不需要显示审批人
      if (row.status === 'REJECTED') {
        return '-'
      }
      // 从currentApproverMap中获取当前审批人
      if (this.currentApproverMap[row.pactId]) {
        return this.currentApproverMap[row.pactId]
      }
      // 如果没有，尝试从流程任务获取
      if (row.processInstanceId || row.contractNo) {
        this.loadCurrentApprover(row)
        return '-'
      }
      return '-'
    },
    async loadCurrentApprover(row) {
      try {
        // 优先使用contractNo（taskKey）来查询任务记录
        const taskKey = row.contractNo
        if (taskKey) {
          const response = await getProcessTaskByTaskKey(taskKey)
          if (response.code === 200 && response.data && response.data.length > 0) {
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
                this.$set(this.currentApproverMap, row.pactId, approverName)
                return
              }
            }
          }
        }
        
        // 如果没有contractNo或者没有找到待处理任务，对于已提交的合同，显示'-'
        if (row.status === 'PENDING' || row.status === 'APPROVED' || row.status === 'REJECTED') {
          // 如果是已提交的状态但没有找到任务，可能是任务还未生成或者已经完成
          this.$set(this.currentApproverMap, row.pactId, '-')
          return
        }
        
        // 对于还未提交的合同（草稿状态），如果没有流程实例，尝试从流程定义的第一个节点获取审批人信息
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
          this.$set(this.currentApproverMap, row.pactId, '-')
          return
        }
        
        // 其他情况
        this.$set(this.currentApproverMap, row.pactId, '-')
      } catch (error) {
        console.error('加载审批人失败:', error)
        this.$set(this.currentApproverMap, row.pactId, '-')
      }
    },
    async handleViewDetail(row) {
      this.selectedContractId = row.pactId
      this.detailVisible = true
    },
    handleDetailEdit(row) {
      this.detailVisible = false
      this.handleEdit(row)
    },
    handleDetailSubmitted() {
      this.loadData()
    },
    handleDetailWithdrawn() {
      this.loadData()
    },
    handleDetailDeleted() {
      this.loadData()
    },
    handleDetailPrint() {
      // 打印功能由统一组件处理，这里只需要刷新数据（如果需要）
      // this.loadData()
    },
    getContractTypeName(codeValue) {
      const option = this.contractTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    // 打开供应商选择对话框
    async handleOpenSupplierDialog() {
      this.supplierDialogVisible = true
      this.supplierSearchForm = {
        supplierCode: '',
        supplierName: ''
      }
      this.selectedSupplier = null
      await this.loadSupplierList()
    },
    // 加载供应商列表
    async loadSupplierList() {
      this.supplierLoading = true
      try {
        const params = {}
        if (this.supplierSearchForm.supplierCode) {
          params.supplierCode = this.supplierSearchForm.supplierCode
        }
        if (this.supplierSearchForm.supplierName) {
          params.supplierName = this.supplierSearchForm.supplierName
        }
        const response = await getSuppliers(params) // 查询供应商
        if (response.code === 200 && response.data) {
          this.supplierList = response.data || []
        } else {
          this.supplierList = []
        }
      } catch (error) {
        console.error('加载供应商列表失败', error)
        this.$message.error('加载供应商列表失败：' + (error.message || '未知错误'))
        this.supplierList = []
      } finally {
        this.supplierLoading = false
      }
    },
    // 供应商查询
    handleSupplierSearch() {
      this.loadSupplierList()
    },
    // 供应商查询重置
    handleSupplierSearchReset() {
      this.supplierSearchForm = {
        supplierCode: '',
        supplierName: ''
      }
      this.loadSupplierList()
    },
    // 点击行选择供应商（单选）
    handleSelectSupplierRow(row) {
      this.selectedSupplier = row
    },
    // 点击选择按钮选择供应商
    handleSelectSupplier(row) {
      this.selectedSupplier = row
    },
    // 确认选择供应商
    handleConfirmSelectedSupplier() {
      if (!this.selectedSupplier) {
        this.$message.warning('请先选择供应商')
        return
      }
      
      if (this.selectedSupplier.isStop === 1) {
        this.$message.warning('不能选择已停用的供应商')
        return
      }
      
      this.form.partyB = this.selectedSupplier.supplierName
      this.supplierDialogVisible = false
      this.$message.success('已选择供应商：' + this.selectedSupplier.supplierName)
      this.selectedSupplier = null
    },
    // 新增供应商
    handleAddSupplier() {
      this.supplierFormTitle = '新增供应商'
      this.supplierForm = {
        supplierId: null,
        supplierCode: '',
        supplierName: '',
        contactPerson: '',
        contactPhone: '',
        contactEmail: '',
        address: '',
        bankName: '',
        bankAccount: '',
        accountName: '',
        taxNumber: '',
        isStop: 0
      }
      this.supplierFormVisible = true
    },
    // 编辑供应商
    handleEditSupplier(supplier) {
      this.supplierFormTitle = '编辑供应商'
      this.supplierForm = { ...supplier }
      this.supplierFormVisible = true
    },
    // 保存供应商
    async handleSaveSupplier() {
      if (!this.supplierForm.supplierCode || !this.supplierForm.supplierName) {
        this.$message.warning('请填写供应商编码和供应商名称')
        return
      }
      
      try {
        const api = this.supplierForm.supplierId ? updateSupplier : saveSupplier
        const response = await api(this.supplierForm)
        
        if (response.code === 200) {
          this.$message.success('保存成功')
          this.supplierFormVisible = false
          // 重新加载供应商列表
          await this.handleOpenSupplierDialog()
          // 如果保存的是当前选中的供应商，更新选中状态
          if (this.selectedSupplier && this.selectedSupplier.supplierId === this.supplierForm.supplierId) {
            const updated = this.supplierList.find(s => s.supplierId === this.supplierForm.supplierId)
            if (updated) {
              this.selectedSupplier = updated
            }
          }
        } else {
          this.$message.error(response.message || '保存失败')
        }
      } catch (error) {
        console.error('保存供应商失败', error)
        this.$message.error('保存供应商失败：' + (error.message || '未知错误'))
      }
    },
    // 删除供应商（物理删除）
    async handleDeleteSupplier(supplier) {
      this.$confirm('确认删除该供应商吗？删除后无法恢复！', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await deleteSupplier(supplier.supplierId)
          if (response.code === 200) {
            this.$message.success('删除成功')
            // 如果删除的是当前选中的供应商，清空选中
            if (this.selectedSupplier && this.selectedSupplier.supplierId === supplier.supplierId) {
              this.selectedSupplier = null
            }
            // 重新加载供应商列表
            await this.handleOpenSupplierDialog()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        } catch (error) {
          console.error('删除供应商失败', error)
          this.$message.error('删除供应商失败：' + (error.message || '未知错误'))
        }
      }).catch(() => {})
    },
    // 停用供应商
    async handleStopSupplier(supplier) {
      try {
        const response = await stopSupplier(supplier.supplierId)
        if (response.code === 200) {
          this.$message.success('停用成功')
          // 如果停用的是当前选中的供应商，清空选中
          if (this.selectedSupplier && this.selectedSupplier.supplierId === supplier.supplierId) {
            this.selectedSupplier = null
          }
          // 重新加载供应商列表
          await this.handleOpenSupplierDialog()
        } else {
          this.$message.error(response.message || '停用失败')
        }
      } catch (error) {
        console.error('停用供应商失败', error)
        this.$message.error('停用供应商失败：' + (error.message || '未知错误'))
      }
    },
    // 启用供应商
    async handleStartSupplier(supplier) {
      try {
        const response = await startSupplier(supplier.supplierId)
        if (response.code === 200) {
          this.$message.success('启用成功')
          // 重新加载供应商列表
          await this.handleOpenSupplierDialog()
        } else {
          this.$message.error(response.message || '启用失败')
        }
      } catch (error) {
        console.error('启用供应商失败', error)
        this.$message.error('启用供应商失败：' + (error.message || '未知错误'))
      }
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportDraft(false)
      } else if (command === 'all') {
        this.handleExportDraft(true)
      }
    },
    // 导出合同起草数据
    async handleExportDraft(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const params = {
            page: 1,
            size: 10000, // 设置一个很大的值以获取所有数据
            contractNo: this.searchForm.contractNo || null,
            contractName: this.searchForm.contractName || null,
            contractType: this.searchForm.contractType || null,
            status: this.searchForm.status || null
          }
          
          const res = await getContractsPage(params)
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
        const headers = ['合同编号', '合同名称', '合同类型', '甲方', '乙方', '合同金额', '状态', '申请人', '科室', '审批人', '申请时间']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            String(item.contractNo || ''),
            String(item.contractName || ''),
            String(this.getContractTypeName(item.contractType) || ''),
            String(item.partyA || ''),
            String(item.partyB || ''),
            String(item.contractAmount ? '¥' + item.contractAmount : ''),
            String(this.getStatusText(item.status) || ''),
            String(item.empName || item.empId || '-'),
            String(item.deptName || item.deptId || '-'),
            String(this.getCurrentApprover(item) || '-'),
            String(this.formatDate(item.createTime) || '')
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '合同起草' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
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
        link.download = '合同起草' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
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
.contract-draft {
  padding: 20px;
}
</style>

