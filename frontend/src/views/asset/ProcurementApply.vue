<template>
  <div class="procurement-apply">
    <el-card>
      <div slot="header" class="clearfix">
        <span>采购申请</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增申请</el-button>
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
          <el-col :span="6" style="text-align: right;">
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
        <el-table-column prop="applyEmpName" label="申请人" width="120"></el-table-column>
        <el-table-column prop="applyDeptName" label="申请部门" width="150"></el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
         <el-table-column prop="applyMoney" label="申请金额" width="150"></el-table-column>
        <el-table-column label="审批人" width="130">
          <template slot-scope="scope">
            <span>{{ getCurrentApprover(scope.row) }}</span>
          </template>
        </el-table-column>
         <el-table-column prop="demandDate" label="需求到位日期" width="130">
          <template slot-scope="scope">
            {{ formatDateOnly(scope.row.demandDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="160">
          <template slot-scope="scope">
            {{ formatDateOnly(scope.row.applyTime || scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="流程" width="80">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleViewProcess(scope.row)">查看</el-button>
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
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="1000px">
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="form" :rules="rules" ref="form" label-width="120px" style="margin-top: 20px;">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="申请人">
                  <el-input v-model="applicantInfo.empName" disabled></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="申请部门">
                  <el-input v-model="applicantInfo.deptName" disabled></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="申请人手机号">
                  <el-input v-model="applicantInfo.empPhone" disabled></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
               <el-col :span="8">
                <el-form-item label="申请时间" prop="applyTime">
                  <el-date-picker
                    v-model="form.applyTime"
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
                <el-form-item label="需求到位日期" prop="demandDate">
                  <el-date-picker
                    v-model="form.demandDate"
                    type="date"
                    placeholder="选择需求到位日期"
                    style="width: 100%"
                    format="yyyy-MM-dd"
                    value-format="yyyy-MM-dd"
                  ></el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="申请总金额">
                  <el-input v-model="form.applyMoney" disabled style="width: 100%">
                    <template slot="prepend">¥</template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="模板配置" prop="templateConfigId">
                  <el-select 
                    v-model="form.templateConfigId" 
                    placeholder="请选择模板配置" 
                    filterable
                    style="width: 100%"
                    @change="handleTemplateConfigChange"
                  >
                    <el-option
                      v-for="config in templateConfigOptions"
                      :key="config.configId"
                      :label="config.businessTypeName || `${config.businessTypeValue || ''}`"
                      :value="config.configId"
                    >
                      <span style="float: left">{{ config.businessTypeName || config.businessTypeValue || '' }}</span>
                      <span style="float: right; color: #8492a6; font-size: 13px">{{ config.businessTypeValue || '' }}</span>
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="申请原因" prop="applyReason">
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
        
        <!-- 资产信息 -->
        <el-tab-pane label="资产信息" name="asset">
          <div style="margin-top: 20px;">
            <div style="margin-bottom: 10px;">
              <el-button type="primary" size="small" @click="handleOpenAssetItemDialog">选择资产</el-button>
            </div>
            <el-table :data="detailList" border style="width: 100%">
              <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
              <el-table-column prop="assetCode" label="资产编码" width="120"></el-table-column>
              <el-table-column prop="assetName" label="资产名称" width="200"></el-table-column>
              <el-table-column prop="spec" label="规格型号" width="200"></el-table-column>
              <el-table-column prop="manufacturer" label="生产厂家" width="150"></el-table-column>
              <el-table-column prop="unit" label="单位" width="80"></el-table-column>
              <el-table-column label="申请数量" width="150">
                <template slot-scope="scope">
                  <el-input-number 
                    v-model="scope.row.applyQuantity" 
                    :min="1" 
                    :precision="0"
                    style="width: 100%"
                    @change="handleDetailChange(scope.$index)"
                  ></el-input-number>
                </template>
              </el-table-column>
              <el-table-column label="单价" width="200">
                <template slot-scope="scope">
                  <el-input-number 
                    v-model="scope.row.price" 
                    :min="0" 
                    :precision="2"
                    style="width: 100%"
                    @change="handleDetailChange(scope.$index)"
                  ></el-input-number>
                </template>
              </el-table-column>
              <el-table-column label="总价" width="120">
                <template slot-scope="scope">
                  <span>¥{{ (scope.row.applyQuantity * scope.row.price).toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="备注" width="150">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.remark" placeholder="备注"></el-input>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80">
                <template slot-scope="scope">
                  <el-button size="mini" type="danger" @click="handleRemoveDetail(scope.$index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div style="margin-top: 10px; text-align: right;">
              <strong>合计金额：¥{{ totalAmount.toFixed(2) }}</strong>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleDialogCancel">取消</el-button>
        <el-button @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="primary" @click="handleSaveAndSubmit">保存并提交</el-button>
      </div>
    </el-dialog>

    <!-- 资产选择对话框 -->
    <el-dialog title="选择资产" :visible.sync="assetItemDialogVisible" width="1000px" :close-on-click-modal="false">
      <div>
        <el-form :inline="true" style="margin-bottom: 10px;">
          <el-form-item label="资产名称">
            <el-input v-model="assetItemSearchForm.assetName" placeholder="请输入资产名称" clearable style="width: 200px;"></el-input>
          </el-form-item>
          <el-form-item label="资产编码">
            <el-input v-model="assetItemSearchForm.assetCode" placeholder="请输入资产编码" clearable style="width: 200px;"></el-input>
          </el-form-item>
          <el-form-item label="一级分类">
            <el-select v-model="assetItemSearchForm.level1Id" placeholder="全部" clearable style="width: 200px" @change="handleAssetItemLevel1Change">
              <el-option
                v-for="category in assetItemLevel1Options"
                :key="category.id"
                :label="category.categoryName"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="二级分类">
            <el-select v-model="assetItemSearchForm.level2Id" placeholder="请先选择一级分类" clearable style="width: 200px" :disabled="!assetItemSearchForm.level1Id" @change="handleAssetItemLevel2Change">
              <el-option
                v-for="category in assetItemLevel2Options"
                :key="category.id"
                :label="category.categoryName"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="三级分类">
            <el-select v-model="assetItemSearchForm.categoryId" placeholder="请先选择二级分类" clearable style="width: 200px" :disabled="!assetItemSearchForm.level2Id" @change="handleSearchAssetItems">
              <el-option
                v-for="category in assetItemLevel3Options"
                :key="category.id"
                :label="category.categoryName"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearchAssetItems">查询</el-button>
            <el-button @click="handleResetAssetItemSearch">重置</el-button>
          </el-form-item>
        </el-form>
        <el-table 
          ref="assetItemTable"
          :data="assetItemList" 
          border 
          style="width: 100%" 
          v-loading="assetItemListLoading"
          @selection-change="handleAssetItemSelectionChange"
        >
          <el-table-column type="selection" width="30"></el-table-column>
          <el-table-column type="index" label="序号" width="50" align="center"></el-table-column>
          <el-table-column prop="assetCode" label="资产编码" width="150"></el-table-column>
          <el-table-column prop="assetName" label="资产名称" width="200"></el-table-column>
          <el-table-column prop="spec" label="规格型号" width="200"></el-table-column>
          <el-table-column prop="manufacturer" label="生产厂家" width="150"></el-table-column>
          <el-table-column prop="unit" label="单位" width="80"></el-table-column>
          <el-table-column prop="price" label="参考单价" width="120">
            <template slot-scope="scope">
              <span>¥{{ (scope.row.price || 0).toFixed(2) }}</span>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top: 10px; text-align: right;" v-if="assetItemPagination.total > 0">
          <el-pagination
            @size-change="handleAssetItemSizeChange"
            @current-change="handleAssetItemCurrentChange"
            :current-page="assetItemPagination.page"
            :page-sizes="[5,10, 20, 50, 100]"
            :page-size="assetItemPagination.size"
            layout="total, sizes, prev, pager, next, jumper"
            :total="assetItemPagination.total">
          </el-pagination>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="assetItemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmSelectedAssetItems">确定</el-button>
      </div>
    </el-dialog>

    <!-- 统一的详情页组件 -->
    <ProcurementApplyDetail
      v-model="detailVisible"
      source-type="apply"
      :apply-id="currentDetailId"
      @edit="handleEditFromDetail"
      @submitted="handleDetailSubmitted"
      @withdrawn="handleDetailWithdrawn"
      @deleted="handleDetailDeleted"
      @approved="handleDetailApproved"
      @returned="handleDetailReturned"
      @add-signed="handleDetailAddSign"
      @transferred="handleDetailTransfer"
    />

    <!-- 流程查看对话框 -->
    <ProcessViewDialog
      :visible.sync="processVisible"
      :row="currentProcessRow"
      :template-config-map="templateConfigMap"
      business-key-field="applyNo"
      business-type-name="申请"
      :show-comment="true"
      :show-complete-time="true"
    />
  </div>
</template>

<script>
import {
  getAssetPurchaseApplyPage,
  getAssetPurchaseApplyById,
  saveAssetPurchaseApply,
  updateAssetPurchaseApply,
  deleteAssetPurchaseApply,
  submitAssetPurchaseApply,
  withdrawAssetPurchaseApply
} from '@/api/asset'
import { getAssetItemList, getAssetItemPage } from '@/api/asset'
import { getAssetCategoryLevel1List, getAssetCategoryLevel2List, getAssetCategoryLevel3List } from '@/api/asset'
import { getCodeTypeOptions } from '@/utils/codeType'
import { getAttachmentsByBusinessId, deleteAttachment, uploadFile, getAttachment } from '@/api/attachment'
import { getProcessTaskByTaskKey, getProcessDefinitionById } from '@/api/process'
import { getTemplateConfigById, getTemplateConfigByBusinessTypeOnly } from '@/api/templateConfig'
import { getDefaultPrintTemplate, generatePrintContent } from '@/api/print'
import { exportExcel } from '@/api/common'
import ProcessViewDialog from '@/components/ProcessViewDialog.vue'
import ProcurementApplyDetail from '@/views/asset/ProcurementApplyDetail.vue'
import Cookies from 'js-cookie'

export default {
  name: 'ProcurementApply',
  components: {
    ProcessViewDialog,
    ProcurementApplyDetail
  },
  data() {
    return {
      loading: false,
      exportLoading: false,
      tableData: [],
      applyStatusOptions: [], // 申请状态选项（从APPLY_STATUS获取）
      dialogVisible: false,
      detailVisible: false,
      currentDetailId: null,
      detailData: null,
      detailDetailList: [],
      detailActiveTab: 'basic',
      dialogTitle: '新增申请',
      isEdit: false,
      activeTab: 'basic', // 对话框内tab激活项
      currentApproverMap: {}, // 审批人映射
      processVisible: false, // 流程查看对话框
      currentProcessRow: null, // 当前查看流程的行数据
      templateConfigMap: {}, // 模板配置映射
      searchForm: {
        applyNo: '',
        status: '',
        applyDateRange: null
      },
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      applicantInfo: {
        empName: '',
        deptName: '',
        empPhone: ''
      },
      form: {
        id: null,
        applyNo: '',
        applyDeptId: null,
        applyEmpId: null,
        applyTime: null,
        demandDate: null,
        applyReason: '',
        applyMoney: 0.00,
        status: 'DRAFT',
        templateConfigId: null,
        mainAttachId: null
      },
      detailList: [],
      fileList: [],
      uploadedAttachmentIds: [],
      uploadUrl: '/api/auth/attachment/upload',
      uploadHeaders: {
        Authorization: 'Bearer ' + Cookies.get('token')
      },
      uploadData: {
        businessType: 'ASSET_PURCHASE_APPLY'
      },
      mainAttachId: null,
      attachments: [],
      attachmentPreviewVisible: false,
      completedNodes: [],
      rules: {
        applyTime: [{ required: true, message: '请选择申请时间', trigger: 'change' }],
        demandDate: [{ required: true, message: '请选择需求到位日期', trigger: 'change' }]
      },
      // 资产选择对话框相关
      assetItemDialogVisible: false,
      assetItemSearchForm: {
        assetName: '',
        assetCode: '',
        level1Id: null,
        level2Id: null,
        categoryId: null
      },
      assetItemLevel1Options: [], // 一级分类选项
      assetItemLevel2Options: [], // 二级分类选项
      assetItemLevel3Options: [], // 三级分类选项
      assetItemList: [],
      assetItemListLoading: false,
      assetItemPagination: {
        page: 1,
        size: 5,
        total: 0
      },
      selectedAssetItemsInDialog: [],
      // 模板配置选项
      templateConfigOptions: []
    }
  },
  computed: {
    totalAmount() {
      return this.detailList.reduce((sum, item) => {
        return sum + (item.applyQuantity || 0) * (item.price || 0)
      }, 0)
    },
    filteredCompletedNodes() {
      // 只显示有审批意见的记录（comment不为空且不为空字符串，且不为"-"）
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
    totalAmount: {
      handler(newVal) {
        // 自动更新申请总金额
        this.form.applyMoney = parseFloat((newVal || 0).toFixed(2))
      },
      immediate: true
    }
  },
  mounted() {
    this.loadCodeTypeOptions()
    this.loadData()
    this.initApplicantInfo()
    this.loadTemplateConfigs()
  },
  methods: {
    async loadCodeTypeOptions() {
      this.applyStatusOptions = await getCodeTypeOptions('APPLY_STATUS')
    },
    loadData() {
      this.loading = true
      const params = {
        page: this.pagination.page,
        size: this.pagination.size
      }
      if (this.searchForm.applyNo) {
        params.applyNo = this.searchForm.applyNo
      }
      if (this.searchForm.status) {
        params.status = this.searchForm.status
      }
      if (this.searchForm.applyDateRange && this.searchForm.applyDateRange.length === 2) {
        params.startDate = this.searchForm.applyDateRange[0]
        params.endDate = this.searchForm.applyDateRange[1]
      }
      
      getAssetPurchaseApplyPage(params).then(res => {
        if (res.code === 200 && res.data) {
          const records = res.data.records || res.data.list || res.data.rows || []
          this.tableData = records
          this.pagination.total = res.data.total || records.length || 0
          // 加载审批人信息
          this.loadApproversForTable(records)
          // 加载模板配置信息
          this.loadTemplateConfigsForTable(records)
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    // 加载审批人信息
    async loadApproversForTable(records) {
      for (const row of records) {
        if (row.status === 'PENDING' && row.applyNo) {
          this.loadCurrentApprover(row)
        }
      }
    },
    // 加载单个申请的审批人
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
                this.$set(this.currentApproverMap, row.id, approverName)
                return
              }
            }
          }
        }
        
        this.$set(this.currentApproverMap, row.id, '-')
      } catch (error) {
        console.error('加载审批人失败:', error)
        this.$set(this.currentApproverMap, row.id, '-')
      }
    },
    // 获取当前审批人
    getCurrentApprover(row) {
      if (row.status === 'REJECTED' || row.status === 'DRAFT' || row.status === 'WITHDRAWN') {
        return '-'
      }
      if (this.currentApproverMap[row.id]) {
        return this.currentApproverMap[row.id]
      }
      if (row.applyNo) {
        this.loadCurrentApprover(row)
        return '-'
      }
      return '-'
    },
    // 查看流程
    handleViewProcess(row) {
      this.currentProcessRow = row
      this.processVisible = true
    },
    // 加载模板配置列表（用于流程查看）
    async loadTemplateConfigs() {
      try {
        const response = await getTemplateConfigByBusinessTypeOnly('ASSET_TYPE')
        if (response.code === 200 && response.data) {
          const configs = response.data || []
          configs.forEach(config => {
            if (config.configId) {
              this.$set(this.templateConfigMap, config.configId, config)
            }
          })
        }
      } catch (error) {
        console.error('加载模板配置失败:', error)
      }
    },
    // 加载表格数据的模板配置
    async loadTemplateConfigsForTable(records) {
      const configIds = new Set()
      records.forEach(row => {
        if (row.templateConfigId) {
          configIds.add(row.templateConfigId)
        }
      })
      
      for (const configId of configIds) {
        if (!this.templateConfigMap[configId]) {
          try {
            const response = await getTemplateConfigById(configId)
            if (response.code === 200 && response.data) {
              this.$set(this.templateConfigMap, configId, response.data)
            }
          } catch (error) {
            console.error('加载模板配置失败:', error)
          }
        }
      }
    },
    initApplicantInfo() {
      const userInfo = this.$store.state.user.userInfo || {}
      this.applicantInfo = {
        empName: userInfo.empName || userInfo.emp_name || userInfo.realName || userInfo.name || '',
        deptName: userInfo.deptName || userInfo.dept_name || '',
        empPhone: userInfo.empPhone || userInfo.emp_phone || userInfo.phone || userInfo.mobile || ''
      }
      this.form.applyEmpId = userInfo.empId || userInfo.emp_id || null
      this.form.applyDeptId = userInfo.deptId || userInfo.dept_id || null
    },
    handleSizeChange(size) {
      this.pagination.size = size
      this.pagination.page = 1
      this.loadData()
    },
    handleCurrentChange(page) {
      this.pagination.page = page
      this.loadData()
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = {
        applyNo: '',
        status: '',
        applyDateRange: null
      }
      this.pagination.page = 1
      this.loadData()
    },
    handleAdd() {
      this.dialogTitle = '新增申请'
      this.isEdit = false
      const mainAttachId = Date.now().toString()
      const now = new Date()
      this.form = {
        id: null,
        applyNo: '',
        applyDeptId: this.form.applyDeptId,
        applyEmpId: this.form.applyEmpId,
        applyTime: this.formatDateForPicker(now),
        demandDate: null,
        applyReason: '',
        applyMoney: 0.00,
        status: 'DRAFT',
        templateConfigId: null,
        mainAttachId: mainAttachId
      }
      this.mainAttachId = mainAttachId
      this.fileList = []
      this.uploadedAttachmentIds = []
      this.uploadData.businessId = mainAttachId
      this.detailList = []
      this.activeTab = 'basic'
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.form) {
          this.$refs.form.clearValidate()
        }
      })
    },
    async handleEdit(row) {
      this.dialogTitle = '编辑申请'
      this.isEdit = true
      const res = await getAssetPurchaseApplyById(row.id)
      if (res.code === 200 && res.data) {
        const data = res.data
        if (!data.mainAttachId) {
          data.mainAttachId = Date.now().toString()
        }
        this.detailList = (data.details || []).map(item => ({
          id: item.id,
          assetCode: item.assetCode,
          assetName: item.assetName,
          spec: item.spec || '',
          manufacturer: item.manufacturer || '',
          unit: item.unit || '',
          applyQuantity: item.applyQuantity || 1,
          price: item.price || 0,
          remark: item.remark || ''
        }))
        
        // 计算总金额（从明细列表计算，确保准确）
        const calculatedMoney = this.detailList.reduce((sum, item) => {
          return sum + (item.applyQuantity || 0) * (item.price || 0)
        }, 0)
        
        this.form = {
          id: data.id,
          applyNo: data.applyNo,
          applyDeptId: data.applyDeptId,
          applyEmpId: data.applyEmpId,
          applyTime: data.applyTime ? this.formatDateForPicker(new Date(data.applyTime)) : null,
          demandDate: data.demandDate ? this.formatDateForPicker(new Date(data.demandDate)) : null,
          applyReason: data.applyReason || '',
          applyMoney: parseFloat(calculatedMoney.toFixed(2)),
          status: data.status || 'DRAFT',
          templateConfigId: data.templateConfigId,
          mainAttachId: data.mainAttachId
        }
        this.mainAttachId = data.mainAttachId
        this.uploadData.businessId = data.mainAttachId
        this.fileList = []
        this.uploadedAttachmentIds = []
        await this.loadAttachments(data.mainAttachId)
        this.activeTab = 'basic'
        this.dialogVisible = true
        this.$nextTick(() => {
          if (this.$refs.form) {
            this.$refs.form.clearValidate()
          }
        })
      }
    },
    handleRemoveDetail(index) {
      this.detailList.splice(index, 1)
      // 删除后自动更新总金额
      this.$nextTick(() => {
        this.updateApplyMoney()
      })
    },
    // 打开资产选择对话框
    // 加载资产选择对话框的一级分类选项
    loadAssetItemLevel1Options() {
      getAssetCategoryLevel1List(1).then(res => {
        if (res.code === 200 && res.data) {
          this.assetItemLevel1Options = res.data || []
        }
      })
    },
    async handleOpenAssetItemDialog() {
      // 打开对话框时加载一级分类选项
      this.loadAssetItemLevel1Options()
      this.assetItemDialogVisible = true
      this.selectedAssetItemsInDialog = []
      this.assetItemSearchForm = {
        assetName: '',
        assetCode: '',
        level1Id: null,
        level2Id: null,
        categoryId: null
      }
      this.assetItemLevel2Options = []
      this.assetItemLevel3Options = []
      this.assetItemPagination.page = 1
      await this.loadAssetItemList()
      // 清空表格选择状态
      this.$nextTick(() => {
        if (this.$refs.assetItemTable) {
          this.$refs.assetItemTable.clearSelection()
        }
      })
    },
    // 加载资产列表
    async loadAssetItemList() {
      this.assetItemListLoading = true
      try {
        const params = {
          page: this.assetItemPagination.page,
          size: this.assetItemPagination.size,
          status: 1 // 只查询启用的资产
        }
        if (this.assetItemSearchForm.assetName) {
          params.assetName = this.assetItemSearchForm.assetName
        }
        if (this.assetItemSearchForm.assetCode) {
          params.assetCode = this.assetItemSearchForm.assetCode
        }
        // 优先级：三级分类 > 二级分类 > 一级分类
        if (this.assetItemSearchForm.categoryId) {
          params.categoryId = this.assetItemSearchForm.categoryId
        } else if (this.assetItemSearchForm.level2Id) {
          params.level2Id = this.assetItemSearchForm.level2Id
        } else if (this.assetItemSearchForm.level1Id) {
          params.level1Id = this.assetItemSearchForm.level1Id
        }
        
        // 先尝试使用分页接口
        try {
          const res = await getAssetItemPage(params)
          if (res.code === 200 && res.data) {
            if (Array.isArray(res.data)) {
              this.assetItemList = res.data
              this.assetItemPagination.total = res.data.length
            } else if (res.data.records || res.data.list || res.data.rows) {
              this.assetItemList = res.data.records || res.data.list || res.data.rows || []
              this.assetItemPagination.total = res.data.total || this.assetItemList.length
            } else {
              throw new Error('分页数据格式不正确')
            }
          } else {
            throw new Error(res.message || '分页接口返回错误')
          }
        } catch (pageError) {
          // 如果分页接口失败，使用列表接口并在前端分页
          console.warn('分页接口失败，使用列表接口:', pageError)
          const listParams = { status: 1 }
          // 添加分类过滤参数
          if (this.assetItemSearchForm.categoryId) {
            listParams.categoryId = this.assetItemSearchForm.categoryId
          } else if (this.assetItemSearchForm.level2Id) {
            listParams.level2Id = this.assetItemSearchForm.level2Id
          } else if (this.assetItemSearchForm.level1Id) {
            listParams.level1Id = this.assetItemSearchForm.level1Id
          }
          const listRes = await getAssetItemList(listParams)
          if (listRes.code === 200 && listRes.data) {
            const allItems = Array.isArray(listRes.data) ? listRes.data : []
            // 前端过滤
            let filteredItems = allItems
            if (this.assetItemSearchForm.assetName) {
              filteredItems = filteredItems.filter(item => 
                item.assetName && item.assetName.includes(this.assetItemSearchForm.assetName)
              )
            }
            if (this.assetItemSearchForm.assetCode) {
              filteredItems = filteredItems.filter(item => 
                item.assetCode && item.assetCode.includes(this.assetItemSearchForm.assetCode)
              )
            }
            this.assetItemPagination.total = filteredItems.length
            // 前端分页
            const start = (this.assetItemPagination.page - 1) * this.assetItemPagination.size
            const end = start + this.assetItemPagination.size
            this.assetItemList = filteredItems.slice(start, end)
          } else {
            this.assetItemList = []
            this.assetItemPagination.total = 0
          }
        }
      } catch (error) {
        console.error('加载资产列表失败', error)
        this.$message.error('加载资产列表失败：' + (error.message || '未知错误'))
        this.assetItemList = []
        this.assetItemPagination.total = 0
      } finally {
        this.assetItemListLoading = false
      }
    },
    // 搜索资产
    handleSearchAssetItems() {
      this.assetItemPagination.page = 1
      this.loadAssetItemList()
    },
    // 重置搜索
    handleResetAssetItemSearch() {
      this.assetItemSearchForm = {
        assetName: '',
        assetCode: '',
        level1Id: null,
        level2Id: null,
        categoryId: null
      }
      this.assetItemLevel2Options = []
      this.assetItemLevel3Options = []
      this.assetItemPagination.page = 1
      this.loadAssetItemList()
    },
    // 资产选择对话框 - 一级分类变化
    handleAssetItemLevel1Change(level1Id) {
      // 当选择一级分类时，加载该一级分类下的二级分类，并清空二级、三级分类选择
      this.assetItemSearchForm.level2Id = null
      this.assetItemSearchForm.categoryId = null
      this.assetItemLevel2Options = []
      this.assetItemLevel3Options = []
      if (level1Id) {
        getAssetCategoryLevel2List(level1Id, 1).then(res => {
          if (res.code === 200 && res.data) {
            this.assetItemLevel2Options = res.data || []
          }
        })
      }
      this.assetItemPagination.page = 1
      this.loadAssetItemList()
    },
    // 资产选择对话框 - 二级分类变化
    handleAssetItemLevel2Change(level2Id) {
      // 当选择二级分类时，加载该二级分类下的三级分类，并清空三级分类选择
      this.assetItemSearchForm.categoryId = null
      this.assetItemLevel3Options = []
      if (level2Id) {
        getAssetCategoryLevel3List(level2Id, 1).then(res => {
          if (res.code === 200 && res.data) {
            this.assetItemLevel3Options = res.data || []
          }
        })
      }
      this.assetItemPagination.page = 1
      this.loadAssetItemList()
    },
    // 资产选择变化
    handleAssetItemSelectionChange(selection) {
      this.selectedAssetItemsInDialog = selection
    },
    // 确认选择资产
    handleConfirmSelectedAssetItems() {
      if (this.selectedAssetItemsInDialog.length === 0) {
        this.$message.warning('请至少选择一个资产')
        return
      }
      
      let addedCount = 0
      let skippedCount = 0
      
      this.selectedAssetItemsInDialog.forEach(asset => {
        // 检查assetCode是否有效
        if (!asset.assetCode || asset.assetCode.toString().trim() === '') {
          skippedCount++
          return
        }
        
        // 检查是否已经添加过（通过assetCode判断）
        const exists = this.detailList.some(detail => detail.assetCode === asset.assetCode)
        
        if (exists) {
          skippedCount++
          return
        }
        
        // 添加到明细列表，确保assetCode是字符串（后端会转换为Long）
        this.detailList.push({
          id: null,
          assetCode: asset.assetCode.toString().trim(),
          assetName: asset.assetName || '',
          spec: asset.spec || '',
          manufacturer: asset.manufacturer || '',
          unit: asset.unit || '',
          applyQuantity: 1,
          price: asset.price || 0,
          remark: ''
        })
        addedCount++
      })
      
      if (addedCount > 0) {
        this.$message.success(`成功添加 ${addedCount} 个资产${skippedCount > 0 ? `，已跳过 ${skippedCount} 个重复项` : ''}`)
        // 添加后自动更新总金额
        this.$nextTick(() => {
          this.updateApplyMoney()
        })
      } else {
        this.$message.warning('所选资产已全部存在，未添加新项')
      }
      
      this.assetItemDialogVisible = false
    },
    // 资产列表分页大小变化
    handleAssetItemSizeChange(size) {
      this.assetItemPagination.size = size
      this.assetItemPagination.page = 1
      this.loadAssetItemList()
    },
    // 资产列表当前页变化
    handleAssetItemCurrentChange(page) {
      this.assetItemPagination.page = page
      this.loadAssetItemList()
    },
    handleDetailChange(index) {
      // 明细变化时，自动更新总金额
      this.updateApplyMoney()
    },
    // 更新申请总金额
    updateApplyMoney() {
      const total = this.detailList.reduce((sum, item) => {
        return sum + (item.applyQuantity || 0) * (item.price || 0)
      }, 0)
      this.form.applyMoney = parseFloat(total.toFixed(2))
    },
    // 获取当前审批人
    getCurrentApprover(row) {
      if (row.status === 'REJECTED' || row.status === 'DRAFT' || row.status === 'WITHDRAWN') {
        return '-'
      }
      if (this.currentApproverMap[row.id]) {
        return this.currentApproverMap[row.id]
      }
      if (row.applyNo) {
        this.loadCurrentApprover(row)
        return '-'
      }
      return '-'
    },
    // 加载单个申请的审批人
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
                this.$set(this.currentApproverMap, row.id, approverName)
                return
              }
            }
          }
        }
        
        this.$set(this.currentApproverMap, row.id, '-')
      } catch (error) {
        console.error('加载审批人失败:', error)
        this.$set(this.currentApproverMap, row.id, '-')
      }
    },
    // 加载审批人信息
    async loadApproversForTable(records) {
      for (const row of records) {
        if (row.status === 'PENDING' && row.applyNo) {
          this.loadCurrentApprover(row)
        }
      }
    },
    // 查看流程
    handleViewProcess(row) {
      this.currentProcessRow = row
      this.processVisible = true
    },
    // 加载模板配置列表（用于流程查看和选择）
    async loadTemplateConfigs() {
      try {
        const response = await getTemplateConfigByBusinessTypeOnly('ASSET_TYPE')
        if (response.code === 200 && response.data) {
          this.templateConfigOptions = response.data || []
          // 建立映射关系
          const configs = response.data || []
          configs.forEach(config => {
            if (config.configId) {
              this.$set(this.templateConfigMap, config.configId, config)
            }
          })
        }
      } catch (error) {
        console.error('加载模板配置失败:', error)
        this.templateConfigOptions = []
      }
    },
    // 加载表格数据的模板配置
    async loadTemplateConfigsForTable(records) {
      const configIds = new Set()
      records.forEach(row => {
        if (row.templateConfigId) {
          configIds.add(row.templateConfigId)
        }
      })
      
      for (const configId of configIds) {
        if (!this.templateConfigMap[configId]) {
          try {
            const response = await getTemplateConfigById(configId)
            if (response.code === 200 && response.data) {
              this.$set(this.templateConfigMap, configId, response.data)
            }
          } catch (error) {
            console.error('加载模板配置失败:', error)
          }
        }
      }
    },
    // 模板配置变化处理
    handleTemplateConfigChange() {
      // 可以选择在这里做其他处理，比如根据模板配置加载流程定义等
    },
    async handleSaveDraft() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return
        if (this.detailList.length === 0) {
          this.$message.warning('请至少添加一条资产明细')
          return
        }
        
        try {
          // 确保mainAttachId有值（必须在保存前设置，以便附件上传时使用）
          if (!this.form.mainAttachId && !this.mainAttachId) {
            this.form.mainAttachId = Date.now().toString()
            this.mainAttachId = this.form.mainAttachId
          }
          // 更新uploadData的businessId，确保附件上传时使用正确的mainAttachId
          this.uploadData.businessId = this.form.mainAttachId || this.mainAttachId
          
          // 保存前再次计算申请总金额，确保准确
          this.updateApplyMoney()
          
          // 先上传附件（使用mainAttachId作为businessId）
          const uploadedIds = await this.uploadAllFiles()
          this.uploadedAttachmentIds = this.uploadedAttachmentIds.concat(uploadedIds)
          
          const formData = {
            ...this.form,
            mainAttachId: this.form.mainAttachId || this.mainAttachId, // 确保mainAttachId包含在提交数据中
            applyMoney: this.form.applyMoney, // 确保包含申请总金额
            details: this.detailList.map(item => ({
              id: item.id,
              assetCode: item.assetCode,
              assetName: item.assetName,
              spec: item.spec,
              manufacturer: item.manufacturer,
              unit: item.unit,
              applyQuantity: item.applyQuantity,
              price: item.price,
              totalPrice: item.applyQuantity * item.price,
              remark: item.remark
            }))
          }
          
          const api = this.isEdit ? updateAssetPurchaseApply : saveAssetPurchaseApply
          const res = await api(formData)
          if (res.code === 200) {
            // 保存成功后，更新form的applyNo（如果后端返回了），这样取消时不会误删附件
            if (res.data && res.data.applyNo) {
              this.form.applyNo = res.data.applyNo
            }
            // 如果是新增，也更新id（如果后端返回了）
            if (res.data && res.data.id) {
              this.form.id = res.data.id
            }
            this.$message.success('保存成功')
            this.dialogVisible = false
            this.loadData()
          } else {
            this.$message.error(res.message || '保存失败')
          }
        } catch (error) {
          this.$message.error('保存失败：' + (error.message || '未知错误'))
        }
      })
    },
    async handleSaveAndSubmit() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return
        if (this.detailList.length === 0) {
          this.$message.warning('请至少添加一条资产明细')
          return
        }
        
        try {
          // 确保mainAttachId有值（必须在保存前设置，以便附件上传时使用）
          if (!this.form.mainAttachId && !this.mainAttachId) {
            this.form.mainAttachId = Date.now().toString()
            this.mainAttachId = this.form.mainAttachId
          }
          // 更新uploadData的businessId，确保附件上传时使用正确的mainAttachId
          this.uploadData.businessId = this.form.mainAttachId || this.mainAttachId
          
          // 保存前再次计算申请总金额，确保准确
          this.updateApplyMoney()
          
          // 先上传附件（使用mainAttachId作为businessId）
          const uploadedIds = await this.uploadAllFiles()
          this.uploadedAttachmentIds = this.uploadedAttachmentIds.concat(uploadedIds)
          
          const formData = {
            ...this.form,
            mainAttachId: this.form.mainAttachId || this.mainAttachId, // 确保mainAttachId包含在提交数据中
            applyMoney: this.form.applyMoney, // 确保包含申请总金额
            details: this.detailList.map(item => ({
              id: item.id,
              assetCode: item.assetCode,
              assetName: item.assetName,
              spec: item.spec,
              manufacturer: item.manufacturer,
              unit: item.unit,
              applyQuantity: item.applyQuantity,
              price: item.price,
              totalPrice: item.applyQuantity * item.price,
              remark: item.remark
            }))
          }
          
          const api = this.isEdit ? updateAssetPurchaseApply : saveAssetPurchaseApply
          const res = await api(formData)
          if (res.code === 200) {
            // 保存成功后，更新form的applyNo和id（如果后端返回了），这样取消时不会误删附件
            if (res.data && res.data.applyNo) {
              this.form.applyNo = res.data.applyNo
            }
            if (res.data && res.data.id) {
              this.form.id = res.data.id
            }
            
            const applyId = res.data?.id || this.form.id
            if (applyId) {
              const submitRes = await submitAssetPurchaseApply(applyId)
              if (submitRes.code === 200) {
                this.$message.success('提交成功')
                this.dialogVisible = false
                this.loadData()
              } else {
                this.$message.error(submitRes.message || '提交失败')
              }
            } else {
              this.$message.success('保存成功，请手动提交')
              this.dialogVisible = false
              this.loadData()
            }
          } else {
            this.$message.error(res.message || '保存失败')
          }
        } catch (error) {
          this.$message.error('保存失败：' + (error.message || '未知错误'))
        }
      })
    },
    handleSubmit(row) {
      this.$confirm('确认提交该申请吗？', '提示', {
        type: 'warning'
      }).then(() => {
        submitAssetPurchaseApply(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('提交成功')
            this.loadData()
          } else {
            this.$message.error(res.message || '提交失败')
          }
        })
      })
    },
    handleWithdraw(row) {
      this.$confirm('确认撤回该申请吗？', '提示', {
        type: 'warning'
      }).then(() => {
        withdrawAssetPurchaseApply(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('撤回成功')
            this.loadData()
            if (this.detailVisible) {
              this.detailVisible = false
            }
          } else {
            this.$message.error(res.message || '撤回失败')
          }
        })
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该申请吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteAssetPurchaseApply(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(res.message || '删除失败')
          }
        })
      })
    },
    async handleViewDetail(row) {
      this.currentDetailId = row.id
      this.detailVisible = true
    },
    handleEditFromDetail() {
      this.detailVisible = false
      this.$nextTick(() => {
        // 从详情页编辑时，需要重新加载数据以获取最新的申请信息
        this.loadData()
        // 然后打开编辑对话框
        const row = this.tableData.find(item => item.id === this.currentDetailId)
        if (row) {
          this.handleEdit(row)
        }
      })
    },
    handleSubmitFromDetail() {
      this.handleSubmit(this.detailData)
    },
    handleDeleteDetail(row) {
      this.handleDelete(row)
    },
    handleDetailClose() {
      this.detailVisible = false
      this.currentDetailId = null
      this.loadData()
    },
    handleDetailSubmitted() {
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
    handleDetailApproved() {
      // 详情页审批通过已在组件内部处理，这里只需要刷新数据
      this.loadData()
    },
    handleDetailReturned() {
      // 详情页退回已在组件内部处理，这里只需要刷新数据
      this.loadData()
    },
    
    handleDetailTransferred() {
      // 详情页转签已在组件内部处理，这里只需要刷新数据
      this.loadData()
    },
     handleDetailAddSign() {
      this.loadData()
    },
    handleDetailTransfer() {
      // 确保详情对话框已关闭后再刷新数据
      this.loadData()
    },
    async handleDialogCancel() {
      // 只有在新增模式下，且表单未保存（没有id和applyNo）时，才删除已上传的附件
      // 如果form有id或applyNo，说明已经保存过，不应该删除附件
      const isSaved = this.form.id || this.form.applyNo
      if (!this.isEdit && !isSaved) {
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
      }
      
      // 重置文件列表和附件ID列表
      this.fileList = []
      this.uploadedAttachmentIds = []
      
      // 关闭对话框
      this.dialogVisible = false
      this.detailList = []
    },
    getStatusType(status) {
      // 根据状态值返回对应的标签类型
      // 可以根据需要调整映射关系
      if (!status) return 'info'
      // 常见的状态类型映射
      if (status === 'APPROVED' || status === '已审批') return 'success'
      if (status === 'PENDING' || status === '待审批') return 'warning'
      if (status === 'REJECTED' || status === '已拒绝') return 'danger'
      if (status === 'DRAFT' || status === '草稿' || status === 'WITHDRAWN' || status === '已撤回') return 'info'
      return 'info'
    },
    getStatusText(status) {
      if (!status) return '-'
      // 从applyStatusOptions中查找状态文本
      const option = this.applyStatusOptions.find(item => item.value === status)
      return option ? option.label : status
    },
    formatDateOnly(date) {
      if (!date) return '-'
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    formatDateForPicker(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
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
            isUploaded: true,
            attachmentId: item.attachmentId
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
    async uploadSingleFile(file) {
      try {
        // 使用mainAttachId作为businessId（时间戳），这样在保存之前就能确定business_id
        const businessId = this.form.mainAttachId || (this.isEdit ? (this.form.applyNo || null) : null)
        
        // 通过templateConfigId获取业务类型
        let businessType = 'ASSET_PURCHASE_APPLY' // 默认值
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
        // 返回businessType字段
        return templateConfig.businessType || null
      } catch (error) {
        console.error('获取业务类型失败:', error)
        return null
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
    async handlePreviewAttachments(row) {
      try {
        const businessId = row.mainAttachId || row.applyNo
        if (!businessId) {
          this.$message.warning('无法获取附件标识，无法加载附件')
          return
        }
        
        const attachResponse = await getAttachmentsByBusinessId(businessId)
        if (attachResponse.code === 200) {
          this.attachments = attachResponse.data || []
          if (this.attachments.length === 0) {
            this.$message.info('暂无附件')
            return
          }
          this.attachmentPreviewVisible = true
        } else {
          this.$message.warning('加载附件失败：' + (attachResponse.message || '未知错误'))
        }
      } catch (error) {
        this.$message.error('加载附件失败：' + (error.message || '未知错误'))
      }
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
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
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
    async loadCompletedNodes(applyNo) {
      try {
        const response = await getProcessTaskByTaskKey(applyNo)
        if (response.code === 200 && response.data && response.data.length > 0) {
          const completedTasks = response.data.filter(task => 
            task.taskStatus === 'COMPLETED' || task.taskStatus === 'RETURNED'
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
        } else {
          this.completedNodes = []
        }
      } catch (error) {
        console.error('加载已完成节点失败:', error)
        this.completedNodes = []
      }
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
    async handlePrint() {
      if (!this.detailData || !this.detailData.applyNo) {
        this.$message.warning('无法获取申请单号')
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
            const defaultTemplateResponse = await getDefaultPrintTemplate('ASSET')
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
          businessKey: this.detailData.applyNo,
          templateType: 'ASSET'
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
          iframe.style.border = 'none'
          document.body.appendChild(iframe)
          
          const iframeDoc = iframe.contentDocument || iframe.contentWindow.document
          iframeDoc.open()
          iframeDoc.write(htmlContent)
          iframeDoc.close()
          
          iframe.contentWindow.focus()
          iframe.contentWindow.print()
          
          setTimeout(() => {
            document.body.removeChild(iframe)
          }, 1000)
        } else {
          this.$message.error('生成打印内容失败：' + (response.message || '未知错误'))
        }
      } catch (error) {
        this.$message.error('打印失败：' + (error.message || '未知错误'))
      }
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportApply(false)
      } else if (command === 'all') {
        this.handleExportApply(true)
      }
    },
    // 导出采购申请数据
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
            status: this.searchForm.status || null
          }
          if (this.searchForm.applyDateRange && this.searchForm.applyDateRange.length === 2) {
            params.startDate = this.searchForm.applyDateRange[0]
            params.endDate = this.searchForm.applyDateRange[1]
          }
          
          const res = await getAssetPurchaseApplyPage(params)
          if (res.code === 200 && res.data) {
            dataToExport = res.data.records || res.data.list || res.data.rows || []
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
        const headers = ['申请单号', '申请人', '申请部门', '状态', '申请金额', '审批人', '需求到位日期', '申请时间']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            String(item.applyNo || ''),
            String(item.applyEmpName || ''),
            String(item.applyDeptName || ''),
            String(this.getStatusText(item.status) || ''),
            String(item.applyMoney || ''),
            String(this.getCurrentApprover(item) || ''),
            String(this.formatDateOnly(item.demandDate) || ''),
            String(this.formatDateOnly(item.applyTime || item.createTime) || '')
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '采购申请' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
          headers: headers,
          dataList: dataList
        })
        
        // 检查响应是否是blob类型
        if (!response || !response.data) {
          this.$message.error('导出失败：服务器返回数据为空')
          return
        }
        
        // 检查是否是错误响应（blob类型的错误响应需要读取）
        if (response.data instanceof Blob && response.data.type === 'application/json') {
          const text = await response.data.text()
          const errorData = JSON.parse(text)
          this.$message.error('导出失败：' + (errorData.message || '服务器错误'))
          return
        }
        
        // 处理blob响应并下载
        const blob = response.data
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '采购申请' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        // 如果是blob类型的错误响应，尝试读取错误信息
        if (error.response && error.response.data instanceof Blob) {
          try {
            const text = await error.response.data.text()
            const errorData = JSON.parse(text)
            this.$message.error('导出失败：' + (errorData.message || '服务器错误'))
          } catch (e) {
            this.$message.error('导出失败：服务器响应错误')
          }
        } else {
          this.$message.error('导出失败：' + (error.message || '未知错误'))
        }
      } finally {
        this.exportLoading = false
      }
    }
  }
}
</script>

<style scoped>
.procurement-apply {
  padding: 20px;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>
