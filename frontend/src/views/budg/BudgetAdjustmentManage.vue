<template>
  <div class="budget-adjustment-manage">
    <el-card>
      <div slot="header" class="clearfix">
        <span>预算调整管理</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增调整单</el-button>
      </div>
      
      <!-- 查询条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="预算主体">
          <el-select 
            v-model="searchForm.subjectId" 
            placeholder="请选择预算主体" 
            style="width: 200px" 
            filterable
            clearable
          >
            <el-option
              v-for="subject in subjectOptions"
              :key="subject.subjectId"
              :label="subject.subjectName"
              :value="subject.subjectId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="年度">
          <el-date-picker
            v-model="searchForm.budgetYear"
            type="year"
            placeholder="选择年度"
            format="yyyy"
            value-format="yyyy"
            style="width: 150px"
            clearable
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="预算分类">
          <el-select v-model="searchForm.categoryType" placeholder="请选择" style="width: 150px" clearable>
            <el-option label="收入预算" value="INCOME"></el-option>
            <el-option label="支出预算" value="EXPENSE"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="项目名称">
          <el-input 
            v-model="searchForm.itemName" 
            placeholder="请输入项目名称" 
            style="width: 200px"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="itemName" label="项目名称" width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="subjectName" label="预算主体" width="150"></el-table-column>
        <el-table-column prop="budgetYear" label="年度" width="100"></el-table-column>
        <el-table-column prop="categoryType" label="预算分类" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.categoryType === 'INCOME' ? 'success' : 'warning'">
              {{ scope.row.categoryType === 'INCOME' ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="budgetAmount" label="预算总额" width="120" align="right">
          <template slot-scope="scope">
            ¥{{ formatAmount(scope.row.budgetAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="executedAmount" label="执行金额" width="120" align="right">
          <template slot-scope="scope">
            <el-link type="primary" @click="handleViewExecution(scope.row)">
              ¥{{ formatAmount(scope.row.executedAmount) }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="appliedAmount" label="申请金额" width="120" align="right">
          <template slot-scope="scope">
            <el-link type="primary" @click="handleViewApply(scope.row)">
              ¥{{ formatAmount(scope.row.appliedAmount) }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="remainingAmount" label="剩余可执行金额" width="150" align="right">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.remainingAmount < 0 ? 'red' : '' }">
              ¥{{ formatAmount(scope.row.remainingAmount) }}
            </span>
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

    <!-- 执行金额明细对话框 -->
    <el-dialog title="执行金额明细" :visible.sync="executionDialogVisible" width="900px">
      <el-table :data="executionDetails" border style="width: 100%">
        <el-table-column prop="payoutBillcode" label="报账单号" width="150"></el-table-column>
        <el-table-column prop="deptName" label="科室" width="150"></el-table-column>
        <el-table-column prop="empName" label="申请人" width="120"></el-table-column>
        <el-table-column prop="executionAmount" label="执行金额" width="120" align="right">
          <template slot-scope="scope">
            ¥{{ formatAmount(scope.row.executionAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="executionDate" label="执行日期" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.executionDate) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 申请金额明细对话框 -->
    <el-dialog title="申请金额明细" :visible.sync="applyDialogVisible" width="900px">
      <el-table :data="applyDetails" border style="width: 100%">
        <el-table-column prop="applyNo" label="申请单号" width="150"></el-table-column>
        <el-table-column prop="deptName" label="科室" width="150"></el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="120"></el-table-column>
        <el-table-column prop="applyAmount" label="申请金额" width="120" align="right">
          <template slot-scope="scope">
            ¥{{ formatAmount(scope.row.applyAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="executedAmount" label="已执行金额" width="120" align="right">
          <template slot-scope="scope">
            <el-link 
              type="primary" 
              @click="handleViewApplyExecution(scope.row)"
              v-if="scope.row.executedAmount && scope.row.executedAmount > 0"
            >
              ¥{{ formatAmount(scope.row.executedAmount) }}
            </el-link>
            <span v-else>¥{{ formatAmount(scope.row.executedAmount || 0) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 申请单已执行金额明细对话框 -->
    <el-dialog title="已执行金额明细" :visible.sync="applyExecutionDialogVisible" width="900px">
      <el-table :data="applyExecutionDetails" border style="width: 100%">
        <el-table-column prop="payoutBillcode" label="报账单号" width="150"></el-table-column>
        <el-table-column prop="deptName" label="科室" width="150"></el-table-column>
        <el-table-column prop="empName" label="申请人" width="120"></el-table-column>
        <el-table-column prop="executionAmount" label="执行金额" width="120" align="right">
          <template slot-scope="scope">
            ¥{{ formatAmount(scope.row.executionAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="executionDate" label="执行日期" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.executionDate) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 新增调整单对话框 -->
    <el-dialog title="新增调整单" :visible.sync="adjustmentDialogVisible" width="1000px" @close="handleDialogCancel">
      <el-form :model="adjustmentForm" :rules="adjustmentRules" ref="adjustmentForm" label-width="120px">
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
            <el-form-item label="申请人手机号">
              <el-input v-model="applicantInfo.empPhone" disabled></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="调整类型" prop="adjustmentType">
              <el-select v-model="adjustmentForm.adjustmentType" placeholder="请选择调整类型" style="width: 100%" @change="handleAdjustmentTypeChange">
                <el-option
                  v-for="option in adjustmentTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="预算主体" prop="subjectId">
              <el-select v-model="adjustmentForm.subjectId" placeholder="请选择预算主体" filterable style="width: 100%" @change="handleSubjectChange">
                <el-option
                  v-for="subject in budgetSubjects"
                  :key="subject.subjectId"
                  :label="subject.subjectName"
                  :value="subject.subjectId"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="预算项目" prop="itemId">
              <el-select v-model="adjustmentForm.itemId" placeholder="请先选择预算主体" filterable style="width: 100%" :disabled="!adjustmentForm.subjectId" @change="handleItemChange">
                <el-option
                  v-for="item in budgetItems"
                  :key="item.itemId"
                  :label="item.itemName"
                  :value="item.itemId"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="预算" prop="budgetId">
              <el-select v-model="adjustmentForm.budgetId" placeholder="请先选择预算主体和预算项目" filterable style="width: 100%" :disabled="!adjustmentForm.subjectId || !adjustmentForm.itemId || budgets.length === 0" @change="handleBudgetChange">
                <el-option
                  v-for="budget in budgets"
                  :key="budget.budgetId"
                  :label="`${budget.budgetName || budget.budgetNo} (预算: ¥${formatAmount(budget.budgetAmount)})`"
                  :value="budget.budgetId"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 关联单据（仅冲销调整显示） -->
            <el-form-item 
              v-if="isOffsetType" 
              label="关联单据" 
              prop="relatedBillId"
            >
              <el-select 
                v-model="adjustmentForm.relatedBillId" 
                placeholder="请选择关联单据" 
                filterable 
                style="width: 100%" 
                @change="handleRelatedBillChange"
              >
                <el-option
                  v-for="bill in relatedBills"
                  :key="bill.id"
                  :label="`${bill.billNo} - ¥${formatAmount(bill.amount)}`"
                  :value="bill.id"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="调整金额" prop="adjustmentAmount">
              <el-input-number 
                v-model="adjustmentForm.adjustmentAmount" 
                :precision="0" 
                :min="0"
                :max="999999999"
                style="width: 100%"
                placeholder="请输入调整金额"
              ></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="adjustmentForm.adjustmentAmount !== null && adjustmentForm.adjustmentAmount !== undefined">
          <el-col :span="24">
            <div style="color: #909399; font-size: 12px; margin-bottom: 10px; padding-left: 120px;">
              <span v-if="adjustmentForm.adjustmentType === 'QUOTA_INCREASE'">额度调增：请输入调整金额（如：2000），系统将原始金额+2000</span>
              <span v-else-if="adjustmentForm.adjustmentType === 'QUOTA_DECREASE'">额度调减：请输入调整金额（如：2000），系统将原始金额-2000</span>
              <span v-else-if="isOffsetType">申请冲销：请输入调整金额（如：2000），系统将自动插入-2000</span>
            </div>
          </el-col>
        </el-row>
        <el-form-item label="调整原因" prop="adjustmentReason">
          <el-input type="textarea" v-model="adjustmentForm.adjustmentReason" :rows="4" placeholder="请输入调整原因"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleDialogCancel">取消</el-button>
        <el-button type="primary" @click="handleSaveAndSubmit" :loading="submitting">保存并提交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getBudgetDetails, getBudgetExecutionDetails, getBudgetApplyDetails, getBudgetApplyExecutionDetails, getBudgetSubjects, getBudgetItems, getBudgetsBySubjectAndItem, saveBudgetAdjustment, submitBudgetAdjustment, saveAndSubmitBudgetAdjustment, getAppliesByItemId, getPayoutsByItemId, getAppliesBySubjectAndItem } from '@/api/budg'
import { getCodeTypeOptions } from '@/utils/codeType'
import { paginationMixin } from '@/mixins/pagination'
import { exportExcel } from '@/api/common'

export default {
  name: 'BudgetAdjustmentManage',
  mixins: [paginationMixin],
  data() {
    return {
      loading: false,
      exportLoading: false,
      submitting: false,
      tableData: [],
      subjectOptions: [],
      budgetSubjects: [],
      budgetItems: [],
      budgets: [],
      adjustmentTypeOptions: [],
      relatedBills: [],
      applicantInfo: {
        empName: '',
        deptName: '',
        empPhone: ''
      },
      searchForm: {
        subjectId: null,
        budgetYear: new Date().getFullYear().toString(),
        categoryType: '',
        itemName: ''
      },
      executionDialogVisible: false,
      applyDialogVisible: false,
      applyExecutionDialogVisible: false,
      adjustmentDialogVisible: false,
      executionDetails: [],
      applyDetails: [],
      applyExecutionDetails: [],
      currentRow: {},
      adjustmentForm: {
        adjustmentType: '',
        subjectId: null,
        itemId: null,
        budgetId: null,
        relatedBillId: null,
        relatedBillNo: '',
        relatedBillType: '',
        adjustmentAmount: null,
        adjustmentReason: ''
      },
      adjustmentRules: {
        adjustmentType: [{ required: true, message: '请选择调整类型', trigger: 'change' }],
        subjectId: [{ required: true, message: '请选择预算主体', trigger: 'change' }],
        itemId: [{ required: true, message: '请选择预算项目', trigger: 'change' }],
        budgetId: [{ required: true, message: '请选择预算', trigger: 'change' }],
        adjustmentAmount: [{ required: true, message: '请输入调整金额', trigger: 'blur' }],
        adjustmentReason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }]
      }
    }
  },
  computed: {
    isOffsetType() {
      return this.adjustmentForm.adjustmentType === 'APPLY_OFFSET' || 
             this.adjustmentForm.adjustmentType === 'PAYOUT_OFFSET'
    }
  },
  mounted() {
    this.loadData()
    this.loadBudgetSubjectsForSearch()
    this.loadBudgetSubjects()
    this.loadAdjustmentTypeOptions()
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
    loadData() {
      this.loading = true
      getBudgetDetails(this.pagination.page, this.pagination.size, this.searchForm).then(response => {
        if (response.code === 200 && response.data) {
          this.tableData = response.data.records || []
          this.pagination.total = response.data.total || 0
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    loadBudgetSubjectsForSearch() {
      getBudgetSubjects(0).then(response => {
        if (response.code === 200) {
          this.subjectOptions = response.data || []
        }
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
    async loadAdjustmentTypeOptions() {
      this.adjustmentTypeOptions = await getCodeTypeOptions('ADJUSTMENT_TYPE')
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = {
        subjectId: null,
        budgetYear: new Date().getFullYear().toString(),
        categoryType: '',
        itemName: ''
      }
      this.handleSearch()
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
    handleViewExecution(row) {
      this.currentRow = row
      getBudgetExecutionDetails(row.itemId, row.subjectId).then(response => {
        if (response.code === 200) {
          this.executionDetails = response.data || []
          this.executionDialogVisible = true
        }
      })
    },
    handleViewApply(row) {
      this.currentRow = row
      getBudgetApplyDetails(row.itemId, row.subjectId).then(response => {
        if (response.code === 200) {
          this.applyDetails = response.data || []
          this.applyDialogVisible = true
        }
      })
    },
    handleViewApplyExecution(row) {
      getBudgetApplyExecutionDetails(this.currentRow.itemId, this.currentRow.subjectId, row.applyNo).then(response => {
        if (response.code === 200) {
          this.applyExecutionDetails = response.data || []
          this.applyExecutionDialogVisible = true
        }
      })
    },
    handleAdd() {
      this.adjustmentDialogVisible = true
      this.resetAdjustmentForm()
    },
    resetAdjustmentForm() {
      this.adjustmentForm = {
        adjustmentType: '',
        subjectId: null,
        itemId: null,
        budgetId: null,
        relatedBillId: null,
        relatedBillNo: '',
        relatedBillType: '',
        adjustmentAmount: null,
        adjustmentReason: ''
      }
      this.budgetItems = []
      this.budgets = []
      this.relatedBills = []
      if (this.$refs.adjustmentForm) {
        this.$refs.adjustmentForm.clearValidate()
      }
    },
    handleAdjustmentTypeChange(type) {
      this.adjustmentForm.relatedBillId = null
      this.adjustmentForm.relatedBillNo = ''
      this.adjustmentForm.relatedBillType = ''
      this.relatedBills = []
      
      // 如果是冲销类型，需要加载关联单据
      if (this.isOffsetType && this.adjustmentForm.itemId) {
        this.loadRelatedBills()
      }
    },
    async handleSubjectChange(subjectId) {
      this.adjustmentForm.itemId = null
      this.adjustmentForm.budgetId = null
      this.budgetItems = []
      this.budgets = []
      
      if (subjectId) {
        try {
          // 只加载当前年度的预算项目
          const currentYear = new Date().getFullYear().toString()
          const response = await getBudgetItems()
          if (response.code === 200) {
            // 过滤出当前年度的预算项目，并且该预算主体关联的项目
            const allItems = response.data || []
            // 这里需要根据预算主体过滤，暂时先按年度过滤
            this.budgetItems = allItems.filter(item => item.budgetYear === currentYear)
          }
        } catch (error) {
          console.error('加载预算项目失败', error)
        }
      }
    },
    async handleItemChange(itemId) {
      this.adjustmentForm.budgetId = null
      this.budgets = []
      this.relatedBills = []
      
      if (this.adjustmentForm.subjectId && itemId) {
        try {
          const response = await getBudgetsBySubjectAndItem(this.adjustmentForm.subjectId, itemId)
          if (response.code === 200) {
            this.budgets = response.data || []
          }
        } catch (error) {
          console.error('加载预算失败', error)
        }
      }
    },
    async handleBudgetChange(budgetId) {
      // 预算选择后，如果是冲销类型，加载关联单据
      if (this.isOffsetType && budgetId) {
        this.loadRelatedBills()
      }
    },
    async loadRelatedBills() {
      if (!this.adjustmentForm.itemId || !this.adjustmentForm.budgetId) return
      
      try {
        if (this.adjustmentForm.adjustmentType === 'APPLY_OFFSET') {
          // 申请冲销：从budget_detail表查询，根据subject_code和item_code
          // 先获取预算信息以获取subject_code和item_code
          const budget = this.budgets.find(b => b.budgetId === this.adjustmentForm.budgetId)
          if (budget && budget.subjectCode && budget.itemCode) {
            const response = await getAppliesBySubjectAndItem(budget.subjectCode, budget.itemCode)
            if (response.code === 200) {
              // 去重，按business_no分组
              const billMap = new Map()
              for (const record of (response.data || [])) {
                if (record.businessNo && !billMap.has(record.businessNo)) {
                  billMap.set(record.businessNo, {
                    id: record.businessId,
                    billNo: record.businessNo,
                    amount: record.amount || 0,
                    type: 'APPLY'
                  })
                }
              }
              this.relatedBills = Array.from(billMap.values())
            }
          }
        } else if (this.adjustmentForm.adjustmentType === 'PAYOUT_OFFSET') {
          // 加载报账单
          const response = await getPayoutsByItemId(this.adjustmentForm.itemId)
          if (response.code === 200) {
            this.relatedBills = (response.data || []).map(item => ({
              id: item.payoutId,
              billNo: item.payoutBillcode,
              amount: item.applyAmount,
              type: 'PAYOUT'
            }))
          }
        }
      } catch (error) {
        console.error('加载关联单据失败', error)
        this.$message.error('加载关联单据失败')
      }
    },
    handleRelatedBillChange(billId) {
      const bill = this.relatedBills.find(b => b.id === billId)
      if (bill) {
        this.adjustmentForm.relatedBillNo = bill.billNo
        this.adjustmentForm.relatedBillType = bill.type
      }
    },
    handleDialogCancel() {
      this.adjustmentDialogVisible = false
      this.resetAdjustmentForm()
    },
    handleSaveAndSubmit() {
      this.$refs.adjustmentForm.validate(async (valid) => {
        if (!valid) {
          return false
        }
        
        // 冲销调整必须选择关联单据
        if (this.isOffsetType && !this.adjustmentForm.relatedBillId) {
          this.$message.warning('请选择关联单据')
          return
        }
        
        // 调整金额必须大于0
        if (!this.adjustmentForm.adjustmentAmount || this.adjustmentForm.adjustmentAmount <= 0) {
          this.$message.warning('调整金额必须大于0')
          return
        }
        
        this.submitting = true
        
        try {
          // 获取当前用户信息
          const userInfo = this.$store.state.user.userInfo
          
          const adjustmentData = {
            adjustmentType: this.adjustmentForm.adjustmentType,
            budgetId: this.adjustmentForm.budgetId,
            relatedBillId: this.adjustmentForm.relatedBillId,
            relatedBillNo: this.adjustmentForm.relatedBillNo,
            relatedBillType: this.adjustmentForm.relatedBillType,
            adjustmentAmount: this.adjustmentForm.adjustmentAmount,
            adjustmentReason: this.adjustmentForm.adjustmentReason,
            applicantId: userInfo.empId,
            applicantName: userInfo.empName || userInfo.userName,
            createUser: userInfo.userName || userInfo.empCode
          }
          
          // 使用saveAndSubmit方法，在同一个事务中完成保存和提交
          const response = await saveAndSubmitBudgetAdjustment(adjustmentData)
          if (response.code === 200) {
            this.$message.success('保存并提交成功')
            this.handleDialogCancel()
            // 刷新列表
            this.loadData()
          } else {
            this.$message.error(response.message || '保存并提交失败')
          }
        } catch (error) {
          console.error('保存并提交失败', error)
          this.$message.error('保存并提交失败: ' + (error.message || '未知错误'))
        } finally {
          this.submitting = false
        }
      })
    },
    formatAmount(amount) {
      if (!amount) return '0.00'
      return parseFloat(amount).toFixed(2)
    },
    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN')
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportManage(false)
      } else if (command === 'all') {
        this.handleExportManage(true)
      }
    },
    // 导出预算调整管理数据
    async handleExportManage(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const res = await getBudgetDetails(1, 10000, this.searchForm)
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
        const headers = ['项目名称', '预算主体', '年度', '预算分类', '预算总额', '执行金额', '申请金额', '剩余可执行金额']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            String(item.itemName || ''),
            String(item.subjectName || ''),
            String(item.budgetYear || ''),
            String(item.categoryType === 'INCOME' ? '收入' : '支出'),
            String('¥' + this.formatAmount(item.budgetAmount)),
            String('¥' + this.formatAmount(item.executedAmount)),
            String('¥' + this.formatAmount(item.appliedAmount)),
            String('¥' + this.formatAmount(item.remainingAmount))
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '预算调整管理' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
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
        link.download = '预算调整管理' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
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
.budget-adjustment-manage {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
</style>
