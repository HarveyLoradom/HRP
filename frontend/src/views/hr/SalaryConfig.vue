<template>
  <div class="salary-config">
    <el-card>
      <div slot="header">
        <span>薪酬设置</span>
      </div>
      
      <el-tabs v-model="activeTab">
        <!-- 薪酬规则 -->
        <el-tab-pane label="薪酬规则" name="rule">
          <div style="margin-bottom: 16px;">
            <el-button type="primary" @click="handleAddRule">新增规则</el-button>
          </div>
          
          <el-table :data="ruleList" border style="width: 100%" v-loading="ruleLoading">
            <el-table-column prop="ruleId" label="规则ID" width="80" align="center" />
            <el-table-column prop="ruleType" label="规则类型" width="150">
              <template slot-scope="scope">
                {{ getRuleTypeText(scope.row.ruleType) }}
              </template>
            </el-table-column>
            <el-table-column prop="ruleName" label="规则名称" />
            <el-table-column prop="salCoefficient" label="薪酬影响系数" width="150" align="center">
              <template slot-scope="scope">
                {{ scope.row.salCoefficient }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" fixed="right">
              <template slot-scope="scope">
                <el-button size="mini" type="success" @click="handleEditRule(scope.row)">编辑</el-button>
                <el-button size="mini" type="danger" @click="handleDeleteRule(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <!-- 薪酬配置 -->
        <el-tab-pane label="薪酬配置" name="config">
          <div style="margin-bottom: 16px;">
            <el-button type="primary" @click="handleAddEmployee">新增员工</el-button>
          </div>
          <el-form :model="configSearchForm" :inline="true" style="margin-bottom: 16px;">
            <el-form-item label="工号:">
              <el-input v-model="configSearchForm.empCode" placeholder="请输入工号" clearable style="width: 140px;" />
            </el-form-item>
            <el-form-item label="姓名:">
              <el-input v-model="configSearchForm.empName" placeholder="请输入姓名" clearable style="width: 140px;" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearchConfig">查询</el-button>
              <el-button @click="handleResetConfig">重置</el-button>
            </el-form-item>
          </el-form>
          
          <el-table :data="configList" border style="width: 100%" v-loading="configLoading">
            <el-table-column prop="empCode" label="工号" width="120" />
            <el-table-column prop="empName" label="姓名" width="120" />
            <el-table-column prop="basicSalary" label="基本工资" width="120" align="right">
              <template slot-scope="scope">
                {{ formatMoney(scope.row.basicSalary) }}
              </template>
            </el-table-column>
            <el-table-column prop="postAllowance" label="岗位津贴" width="120" align="right">
              <template slot-scope="scope">
                {{ formatMoney(scope.row.postAllowance) }}
              </template>
            </el-table-column>
            <el-table-column prop="socialSecurity" label="社保比例(%)" width="120" align="right">
              <template slot-scope="scope">
                {{ formatPercent(scope.row.socialSecurity) }}
              </template>
            </el-table-column>
            <el-table-column prop="providentFund" label="公积金比例(%)" width="130" align="right">
              <template slot-scope="scope">
                {{ formatPercent(scope.row.providentFund) }}
              </template>
            </el-table-column>
            <el-table-column prop="taxThreshold" label="个税起征点" width="120" align="right">
              <template slot-scope="scope">
                {{ formatMoney(scope.row.taxThreshold) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center" header-align="center">
              <template slot-scope="scope">
                <el-button size="mini" type="primary" @click="handleEditConfig(scope.row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页和导出 -->
          <div style="margin-top: 16px; display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
            <el-pagination
              @size-change="handleConfigSizeChange"
              @current-change="handleConfigCurrentChange"
              :current-page="configPagination.page"
              :page-sizes="[10, 20, 50, 100]"
              :page-size="configPagination.size"
              layout="total, sizes, prev, pager, next, jumper"
              :total="configPagination.total"
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
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 规则编辑对话框 -->
    <el-dialog
      :title="ruleDialogTitle"
      :visible.sync="ruleDialogVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="ruleForm" :rules="ruleRules" ref="ruleFormRef" label-width="120px">
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="ruleForm.ruleType" placeholder="请选择规则类型" style="width: 100%;">
            <el-option
              v-for="option in ruleTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="ruleForm.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="薪酬影响系数" prop="salCoefficient">
          <el-input-number
            v-model="ruleForm.salCoefficient"
            :precision="2"
            :step="0.1"
            :min="0"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="ruleSubmitting" @click="handleSubmitRule">确定</el-button>
      </div>
    </el-dialog>
    
    <!-- 薪酬配置编辑对话框 -->
    <el-dialog
      :title="configDialogTitle"
      :visible.sync="configDialogVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="configForm" :rules="configRules" ref="configFormRef" label-width="120px">
        <el-form-item label="员工:">
          <el-input :value="configForm.empName" disabled />
        </el-form-item>
        <el-form-item label="基本工资" prop="basicSalary">
          <el-input-number
            v-model="configForm.basicSalary"
            :precision="2"
            :step="100"
            :min="0"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="岗位津贴" prop="postAllowance">
          <el-input-number
            v-model="configForm.postAllowance"
            :precision="2"
            :step="100"
            :min="0"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="社保比例(%)" prop="socialSecurity">
          <el-input-number
            v-model="configForm.socialSecurity"
            :precision="2"
            :step="0.1"
            :min="0"
            :max="100"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="公积金比例(%)" prop="providentFund">
          <el-input-number
            v-model="configForm.providentFund"
            :precision="2"
            :step="0.1"
            :min="0"
            :max="100"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="个税起征点" prop="taxThreshold">
          <el-input-number
            v-model="configForm.taxThreshold"
            :precision="2"
            :step="100"
            :min="0"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="configSubmitting" @click="handleSubmitConfig">确定</el-button>
      </div>
    </el-dialog>
    
    <!-- 新增员工对话框 -->
    <el-dialog
      title="新增员工"
      :visible.sync="addEmployeeDialogVisible"
      width="700px"
      :close-on-click-modal="false"
      @open="handleAddEmployeeDialogOpen"
    >
      <div style="margin-bottom: 10px;">
        <el-input
          v-model="employeeSearchKeyword"
          placeholder="搜索员工姓名或工号"
          clearable
          style="width: 300px;"
          @input="filterEmployeeList"
        >
          <el-button slot="append" icon="el-icon-search"></el-button>
        </el-input>
        <el-button size="small" style="margin-left: 10px;" @click="handleSelectAllEmployees">全选</el-button>
        <el-button size="small" @click="handleClearAllEmployees">清空</el-button>
      </div>
      <el-table
        :data="filteredEmployeeList"
        border
        height="400"
        @selection-change="handleEmployeeSelectionChange"
        :row-key="row => row.empId"
        ref="employeeTable"
      >
        <el-table-column type="selection" width="55" :reserve-selection="true"></el-table-column>
        <el-table-column prop="empCode" label="工号" width="120"></el-table-column>
        <el-table-column prop="empName" label="姓名" width="120"></el-table-column>
        <el-table-column prop="deptName" label="部门" width="150"></el-table-column>
        <el-table-column label="是否已有配置" width="120" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.hasConfig ? 'success' : 'info'" size="small">
              {{ scope.row.hasConfig ? '已有配置' : '未配置' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addEmployeeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addEmployeeSubmitting" @click="handleSubmitAddEmployee">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getHrAttRuleList, saveHrAttRule, updateHrAttRule, deleteHrAttRule } from '@/api/hr'
import { getHrSalConfigPage, saveHrSalConfig, updateHrSalConfig, getHrSalConfigEmpIds, batchCreateHrSalConfig } from '@/api/hr'
import { getAllEmployeesWithUser } from '@/api/user'
import { getCodeTypeOptions } from '@/utils/codeType'
import { exportExcel } from '@/api/common'

export default {
  name: 'SalaryConfig',
  data() {
    return {
      activeTab: 'rule',
      // 规则相关
      ruleList: [],
      ruleLoading: false,
      ruleDialogVisible: false,
      ruleDialogTitle: '新增规则',
      ruleSubmitting: false,
      ruleForm: {
        ruleId: null,
        ruleType: '',
        ruleName: '',
        salCoefficient: 1.0
      },
      ruleRules: {
        ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
        ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
        salCoefficient: [{ required: true, message: '请输入薪酬影响系数', trigger: 'blur' }]
      },
      ruleTypeOptions: [],
      // 配置相关
      configList: [],
      configLoading: false,
      configSearchForm: {
        empCode: '',
        empName: ''
      },
      configPagination: {
        page: 1,
        size: 10,
        total: 0
      },
      configDialogVisible: false,
      configDialogTitle: '编辑薪酬配置',
      configSubmitting: false,
      configForm: {
        configId: null,
        empId: null,
        empCode: '',
        empName: '',
        basicSalary: 0,
        postAllowance: 0,
        socialSecurity: 0,
        providentFund: 0,
        taxThreshold: 5000
      },
      configRules: {
        basicSalary: [{ required: true, message: '请输入基本工资', trigger: 'blur' }]
      },
      allEmployees: [],
      // 新增员工相关
      addEmployeeDialogVisible: false,
      addEmployeeSubmitting: false,
      employeeSearchKeyword: '',
      filteredEmployeeList: [],
      selectedEmployees: [],
      configedEmpIds: [],
      exportLoading: false
    }
  },
  mounted() {
    this.loadRuleTypeOptions()
    this.loadRuleList()
    this.loadAllEmployees()
    this.loadConfigList()
  },
  methods: {
    // 加载规则类型选项
    async loadRuleTypeOptions() {
      try {
        this.ruleTypeOptions = await getCodeTypeOptions('RULE_TYPE')
      } catch (error) {
        console.error('加载规则类型选项失败:', error)
      }
    },
    // 加载规则列表
    async loadRuleList() {
      this.ruleLoading = true
      try {
        const res = await getHrAttRuleList()
        if (res.code === 200) {
          this.ruleList = res.data || []
        } else {
          this.$message.error(res.message || '加载规则列表失败')
        }
      } catch (error) {
        console.error('加载规则列表失败:', error)
        this.$message.error('加载规则列表失败')
      } finally {
        this.ruleLoading = false
      }
    },
    // 新增规则
    handleAddRule() {
      this.ruleDialogTitle = '新增规则'
      this.ruleForm = {
        ruleId: null,
        ruleType: '',
        ruleName: '',
        salCoefficient: 1.0
      }
      this.ruleDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.ruleFormRef) {
          this.$refs.ruleFormRef.clearValidate()
        }
      })
    },
    // 编辑规则
    handleEditRule(row) {
      this.ruleDialogTitle = '编辑规则'
      this.ruleForm = {
        ruleId: row.ruleId,
        ruleType: row.ruleType,
        ruleName: row.ruleName,
        salCoefficient: row.salCoefficient
      }
      this.ruleDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.ruleFormRef) {
          this.$refs.ruleFormRef.clearValidate()
        }
      })
    },
    // 提交规则
    async handleSubmitRule() {
      this.$refs.ruleFormRef.validate(async (valid) => {
        if (!valid) return
        
        this.ruleSubmitting = true
        try {
          let res
          if (this.ruleForm.ruleId) {
            res = await updateHrAttRule(this.ruleForm)
          } else {
            res = await saveHrAttRule(this.ruleForm)
          }
          
          if (res.code === 200) {
            this.$message.success(this.ruleForm.ruleId ? '更新成功' : '新增成功')
            this.ruleDialogVisible = false
            this.loadRuleList()
          } else {
            this.$message.error(res.message || '操作失败')
          }
        } catch (error) {
          console.error('操作失败:', error)
          this.$message.error('操作失败：' + (error.message || '未知错误'))
        } finally {
          this.ruleSubmitting = false
        }
      })
    },
    // 删除规则
    handleDeleteRule(row) {
      this.$confirm('确定要删除该规则吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteHrAttRule(row.ruleId)
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadRuleList()
          } else {
            this.$message.error(res.message || '删除失败')
          }
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    // 获取规则类型文本
    getRuleTypeText(ruleType) {
      const option = this.ruleTypeOptions.find(opt => opt.value === ruleType)
      return option ? option.label : ruleType
    },
    // 加载所有员工
    async loadAllEmployees() {
      try {
        const res = await getAllEmployeesWithUser(0)
        if (res.code === 200) {
          this.allEmployees = res.data || []
        }
      } catch (error) {
        console.error('加载员工列表失败:', error)
      }
    },
    // 加载薪酬配置列表
    async loadConfigList() {
      this.configLoading = true
      try {
        const params = {
          page: this.configPagination.page,
          size: this.configPagination.size
        }
        
        // 如果填写了工号，传递给后端进行查询
        if (this.configSearchForm.empCode) {
          params.empCode = this.configSearchForm.empCode
        }
        // 如果填写了员工姓名，传递给后端进行查询
        if (this.configSearchForm.empName) {
          params.empName = this.configSearchForm.empName
        }
        
        const res = await getHrSalConfigPage(params)
        if (res.code === 200 && res.data) {
          this.configList = res.data.records || res.data.list || []
          // 使用后端返回的total
          this.configPagination.total = res.data.total || 0
        } else {
          this.$message.error(res.message || '加载配置列表失败')
          this.configList = []
          this.configPagination.total = 0
        }
      } catch (error) {
        console.error('加载配置列表失败:', error)
        this.$message.error('加载配置列表失败')
        this.configList = []
        this.configPagination.total = 0
      } finally {
        this.configLoading = false
      }
    },
    // 查询配置
    handleSearchConfig() {
      this.configPagination.page = 1
      this.loadConfigList()
    },
    // 重置配置查询
    handleResetConfig() {
      this.configSearchForm.empCode = ''
      this.configSearchForm.empName = ''
      this.handleSearchConfig()
    },
    // 编辑配置
    handleEditConfig(row) {
      this.configDialogTitle = '编辑薪酬配置'
      this.configForm = {
        configId: row.configId,
        empId: row.empId,
        empCode: row.empCode,
        empName: row.empName,
        basicSalary: row.basicSalary || 0,
        postAllowance: row.postAllowance || 0,
        socialSecurity: row.socialSecurity || 0,
        providentFund: row.providentFund || 0,
        taxThreshold: row.taxThreshold || 5000
      }
      this.configDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.configFormRef) {
          this.$refs.configFormRef.clearValidate()
        }
      })
    },
    // 提交配置
    async handleSubmitConfig() {
      this.$refs.configFormRef.validate(async (valid) => {
        if (!valid) return
        
        this.configSubmitting = true
        try {
          const userInfo = this.$store.state.user.userInfo || {}
          this.configForm.createUser = userInfo.account || userInfo.userName || 'admin'
          
          let res
          if (this.configForm.configId) {
            res = await updateHrSalConfig(this.configForm)
          } else {
            res = await saveHrSalConfig(this.configForm)
          }
          
          if (res.code === 200) {
            this.$message.success(this.configForm.configId ? '更新成功' : '新增成功')
            this.configDialogVisible = false
            this.loadConfigList()
          } else {
            this.$message.error(res.message || '操作失败')
          }
        } catch (error) {
          console.error('操作失败:', error)
          this.$message.error('操作失败：' + (error.message || '未知错误'))
        } finally {
          this.configSubmitting = false
        }
      })
    },
    // 配置分页
    handleConfigSizeChange(size) {
      this.configPagination.size = size
      this.configPagination.page = 1
      this.loadConfigList()
    },
    handleConfigCurrentChange(page) {
      this.configPagination.page = page
      this.loadConfigList()
    },
    // 格式化金额
    formatMoney(value) {
      const num = Number(value || 0)
      if (isNaN(num)) return '0.00'
      return num.toFixed(2)
    },
    // 格式化百分比
    formatPercent(value) {
      const num = Number(value || 0)
      if (isNaN(num)) return '0.00'
      return num.toFixed(2)
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportConfig(false)
      } else if (command === 'all') {
        this.handleExportConfig(true)
      }
    },
    // 导出薪酬配置
    async handleExportConfig(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const params = {
            page: 1,
            size: 10000 // 设置一个很大的值以获取所有数据
          }
          
          // 传递查询条件
          if (this.configSearchForm.empCode) {
            params.empCode = this.configSearchForm.empCode
          }
          if (this.configSearchForm.empName) {
            params.empName = this.configSearchForm.empName
          }
          
          const res = await getHrSalConfigPage(params)
          if (res.code === 200 && res.data) {
            dataToExport = res.data.records || res.data.list || []
          } else {
            this.$message.error(res.message || '获取数据失败')
            return
          }
        } else {
          // 导出当前页数据
          dataToExport = this.configList
        }
        
        if (dataToExport.length === 0) {
          this.$message.warning('没有数据可导出')
          return
        }
        
        // 构建表头
        const headers = ['工号', '姓名', '基本工资', '岗位津贴', '社保比例(%)', '公积金比例(%)', '个税起征点']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            item.empCode || '',
            item.empName || '',
            this.formatMoney(item.basicSalary),
            this.formatMoney(item.postAllowance),
            this.formatPercent(item.socialSecurity),
            this.formatPercent(item.providentFund),
            this.formatMoney(item.taxThreshold)
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '薪酬配置' + (exportAll ? '_全部' : '_第' + this.configPagination.page + '页'),
          headers: headers,
          dataList: dataList
        })
        
        // 处理blob响应并下载
        const blob = response.data
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '薪酬配置' + (exportAll ? '_全部' : '_第' + this.configPagination.page + '页') + '.xlsx'
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
    },
    // 新增员工
    async handleAddEmployee() {
      this.addEmployeeDialogVisible = true
    },
    // 新增员工对话框打开
    async handleAddEmployeeDialogOpen() {
      this.employeeSearchKeyword = ''
      this.selectedEmployees = []
      
      // 加载已有配置的员工ID列表
      try {
        const res = await getHrSalConfigEmpIds()
        if (res.code === 200) {
          this.configedEmpIds = res.data || []
        } else {
          this.configedEmpIds = []
        }
      } catch (error) {
        console.error('加载已有配置的员工ID失败:', error)
        this.configedEmpIds = []
      }
      
      // 准备员工列表数据
      this.prepareEmployeeList()
    },
    // 准备员工列表数据
    prepareEmployeeList() {
      this.filteredEmployeeList = this.allEmployees.map(emp => ({
        empId: emp.empId,
        empCode: emp.empCode,
        empName: emp.empName,
        deptName: emp.deptName || '-',
        hasConfig: this.configedEmpIds.includes(emp.empId)
      }))
      
      // 设置默认勾选已有配置的员工
      this.$nextTick(() => {
        if (this.$refs.employeeTable) {
          this.$refs.employeeTable.clearSelection()
          this.filteredEmployeeList.forEach(emp => {
            if (emp.hasConfig) {
              this.$refs.employeeTable.toggleRowSelection(emp, true)
            }
          })
        }
      })
    },
    // 过滤员工列表
    filterEmployeeList() {
      const keyword = (this.employeeSearchKeyword || '').toLowerCase()
      if (!keyword) {
        this.prepareEmployeeList()
        return
      }
      
      this.filteredEmployeeList = this.allEmployees
        .filter(emp => {
          const empCode = (emp.empCode || '').toLowerCase()
          const empName = (emp.empName || '').toLowerCase()
          return empCode.includes(keyword) || empName.includes(keyword)
        })
        .map(emp => ({
          empId: emp.empId,
          empCode: emp.empCode,
          empName: emp.empName,
          deptName: emp.deptName || '-',
          hasConfig: this.configedEmpIds.includes(emp.empId)
        }))
    },
    // 员工选择变化
    handleEmployeeSelectionChange(selection) {
      this.selectedEmployees = selection
    },
    // 全选员工
    handleSelectAllEmployees() {
      this.$refs.employeeTable.clearSelection()
      this.filteredEmployeeList.forEach(emp => {
        this.$refs.employeeTable.toggleRowSelection(emp, true)
      })
    },
    // 清空选择
    handleClearAllEmployees() {
      this.$refs.employeeTable.clearSelection()
    },
    // 提交新增员工
    async handleSubmitAddEmployee() {
      const selectedEmpIds = this.selectedEmployees.map(emp => emp.empId)
      if (selectedEmpIds.length === 0) {
        this.$message.warning('请至少选择一个员工')
        return
      }
      
      this.addEmployeeSubmitting = true
      try {
        const userInfo = this.$store.state.user.userInfo || {}
        const createUser = userInfo.account || userInfo.userName || 'admin'
        
        const res = await batchCreateHrSalConfig(selectedEmpIds, createUser)
        if (res.code === 200) {
          this.$message.success(res.message || '批量创建成功')
          this.addEmployeeDialogVisible = false
          // 刷新配置列表
          this.loadConfigList()
        } else {
          this.$message.error(res.message || '批量创建失败')
        }
      } catch (error) {
        console.error('批量创建失败:', error)
        this.$message.error('批量创建失败：' + (error.message || '未知错误'))
      } finally {
        this.addEmployeeSubmitting = false
      }
    }
  }
}
</script>