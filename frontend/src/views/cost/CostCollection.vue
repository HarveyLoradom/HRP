<template>
  <div class="cost-collection" v-loading="loading || importLoading || exportLoading">
    <el-card>
      <div slot="header" class="clearfix">
        <span>成本归集</span>
        <div style="float: right;">
          <el-button type="text" size="big" @click="handleViewCycle">查看周期</el-button>
          <el-button type="text" size="big" @click="handleAdd">新增成本</el-button>
          <el-button type="text" size="big" @click="handleDownloadTemplate">下载导入模板</el-button>
          <el-upload
            ref="upload"
            :action="importUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :show-file-list="false"
            :before-upload="beforeUpload"
            accept=".xlsx,.xls"
            style="display: inline-block; margin-left: 10px;"
          >
            <el-button type="text" size="big">批量导入</el-button>
          </el-upload>
        </div>
      </div>

      <!-- 查询表单 -->
      <el-form :model="searchForm" :inline="true" style="margin-bottom: 20px;">
        <el-form-item label="核算周期">
          <el-select v-model="searchForm.cycleId" placeholder="请选择周期" clearable filterable style="width: 150px;">
            <el-option
              v-for="cycle in cycleList"
              :key="cycle.cycleId"
              :label="cycle.cycleName"
              :value="cycle.cycleId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.deptId" placeholder="请选择部门" clearable filterable style="width: 150px;">
            <el-option
              v-for="dept in deptList"
              :key="dept.deptId"
              :label="dept.deptName"
              :value="dept.deptId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="成本要素">
          <el-select v-model="searchForm.elementType" placeholder="请选择成本要素" clearable filterable style="width: 150px;">
            <el-option
              v-for="item in elementTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="发生日期">
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
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="costNo" label="成本编号" width="180"></el-table-column>
        <el-table-column prop="cycleName" label="核算周期" width="120"></el-table-column>
        <el-table-column prop="deptName" label="部门" width="150"></el-table-column>
        <el-table-column prop="elementType" label="成本要素" width="120">
          <template slot-scope="scope">
            {{ getElementTypeName(scope.row.elementType) }}
          </template>
        </el-table-column>
        <el-table-column prop="costAmount" label="成本金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.costAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="occurDate" label="发生日期" width="120"></el-table-column>
        <el-table-column prop="payType" label="付款方式" width="120">
          <template slot-scope="scope">
            {{ getPayTypeName(scope.row.payType) }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150"></el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top: 20px; display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
        ></el-pagination>
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

    <!-- 周期管理对话框（新增/编辑） -->
    <el-dialog :title="isEditCycle ? '编辑周期' : '新增周期'" :visible.sync="cycleDialogVisible" width="600px">
      <el-form :model="cycleForm" :rules="cycleRules" ref="cycleForm" label-width="120px">
        <el-form-item label="周期编码" prop="cycleCode">
          <el-input v-model="cycleForm.cycleCode" ></el-input>
        </el-form-item>
        <el-form-item label="周期名称" prop="cycleName">
          <el-input v-model="cycleForm.cycleName" ></el-input>
        </el-form-item>
        <el-form-item label="周期类型" prop="cycleType">
          <el-select v-model="cycleForm.cycleType" placeholder="请选择周期类型" style="width: 100%;">
            <el-option label="月度" value="MONTH"></el-option>
            <el-option label="季度" value="QUARTER"></el-option>
            <el-option label="年度" value="YEAR"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="cycleForm.startDate"
            type="date"
            placeholder="选择开始日期"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd"
            style="width: 100%;"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="cycleForm.endDate"
            type="date"
            placeholder="选择结束日期"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd"
            style="width: 100%;"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="cycleForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cycleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCycle">确定</el-button>
      </div>
    </el-dialog>

    <!-- 成本新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="700px">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-form-item label="核算周期" prop="cycleId">
          <el-select v-model="form.cycleId" placeholder="请选择周期" filterable style="width: 100%;">
            <el-option
              v-for="cycle in cycleList"
              :key="cycle.cycleId"
              :label="cycle.cycleName"
              :value="cycle.cycleId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="所属部门" prop="deptId">
          <el-select v-model="form.deptId" placeholder="请选择部门" filterable style="width: 100%;">
            <el-option
              v-for="dept in deptList"
              :key="dept.deptId"
              :label="dept.deptName"
              :value="dept.deptId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="成本要素" prop="elementType">
          <el-select v-model="form.elementType" placeholder="请选择成本要素" filterable style="width: 100%;">
            <el-option
              v-for="item in elementTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="成本金额" prop="costAmount">
          <el-input-number v-model="form.costAmount" :precision="2" :min="0" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="发生日期" prop="occurDate">
          <el-date-picker
            v-model="form.occurDate"
            type="date"
            placeholder="选择发生日期"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd"
            style="width: 100%;"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="付款方式">
          <el-select v-model="form.payType" placeholder="请选择付款方式" clearable filterable style="width: 100%;">
            <el-option
              v-for="item in payTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </div>
    </el-dialog>

    <!-- 周期列表对话框 -->
    <el-dialog title="周期列表" :visible.sync="cycleListDialogVisible" width="900px">
      <div style="margin-bottom: 10px;">
        <el-button type="primary" size="small" @click="handleAddCycle">新增周期</el-button>
      </div>
      <el-table :data="allCycleList" border style="width: 100%" v-loading="cycleLoading">
        <el-table-column prop="cycleCode" label="周期编码" width="120"></el-table-column>
        <el-table-column prop="cycleName" label="周期名称" width="150"></el-table-column>
        <el-table-column prop="cycleType" label="周期类型" width="100">
          <template slot-scope="scope">
            <span v-if="scope.row.cycleType === 'MONTH'">月度</span>
            <span v-else-if="scope.row.cycleType === 'QUARTER'">季度</span>
            <span v-else-if="scope.row.cycleType === 'YEAR'">年度</span>
            <span v-else>{{ scope.row.cycleType }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120"></el-table-column>
        <el-table-column prop="endDate" label="结束日期" width="120"></el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="handleEditCycle(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="handleDeleteCycle(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

  </div>
</template>

<script>
import { getCostCycleList, saveCostCycle, updateCostCycle, deleteCostCycle, getCostCycleById, getCostMainList, saveCostMain, updateCostMain, deleteCostMain, importCostMain, downloadCostMainTemplate } from '@/api/cost'
import { getDeptList, getCodeByType } from '@/api/user'
import { exportExcel } from '@/api/common'
import { mapState } from 'vuex'

export default {
  name: 'CostCollection',
  data() {
    return {
      loading: false,
      exportLoading: false,
      searchForm: {
        cycleId: null,
        deptId: null,
        elementType: '',
        dateRange: []
      },
      tableData: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      cycleList: [],
      allCycleList: [], // 所有周期列表（用于查看周期对话框）
      cycleListDialogVisible: false, // 周期列表对话框显示状态
      cycleLoading: false, // 周期列表加载状态
      deptList: [],
      elementTypeOptions: [], // 成本要素选项
      payTypeOptions: [], // 付款方式选项
      cycleDialogVisible: false,
      isEditCycle: false, // 是否编辑周期
      cycleForm: {
        cycleCode: '',
        cycleName: '',
        cycleType: '',
        startDate: '',
        endDate: '',
        status: 1
      },
      cycleRules: {
        cycleCode: [{ required: true, message: '请输入周期编码', trigger: 'blur' }],
        cycleName: [{ required: true, message: '请输入周期名称', trigger: 'blur' }],
        cycleType: [{ required: true, message: '请选择周期类型', trigger: 'change' }],
        startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
        endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
      },
      dialogVisible: false,
      dialogTitle: '新增成本',
      isEdit: false,
      form: {
        costId: null,
        cycleId: null,
        deptId: null,
        elementType: '',
        costAmount: 0,
        occurDate: '',
        payType: '',
        remark: ''
      },
      rules: {
        cycleId: [{ required: true, message: '请选择核算周期', trigger: 'change' }],
        deptId: [{ required: true, message: '请选择所属部门', trigger: 'change' }],
        elementType: [{ required: true, message: '请输入成本要素', trigger: 'blur' }],
        costAmount: [{ required: true, message: '请输入成本金额', trigger: 'blur' }],
        occurDate: [{ required: true, message: '请选择发生日期', trigger: 'change' }]
      },
      importUrl: '/api/cost/main/import',
      uploadHeaders: {},
      importLoading: false
    }
  },
  computed: {
    ...mapState('user', ['userInfo'])
  },
  mounted() {
    this.loadData()
    this.loadCycleList()
    this.loadDeptList()
    this.loadElementTypeOptions()
    this.loadPayTypeOptions()
    // 设置上传请求头
    const token = this.$store.state.user.token
    if (token) {
      this.uploadHeaders = {
        'Authorization': 'Bearer ' + token
      }
    }
  },
  methods: {
    loadData() {
      this.loading = true
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        cycleId: this.searchForm.cycleId,
        deptId: this.searchForm.deptId,
        elementType: this.searchForm.elementType || null,
        startDate: this.searchForm.dateRange && this.searchForm.dateRange[0] || null,
        endDate: this.searchForm.dateRange && this.searchForm.dateRange[1] || null
      }
      getCostMainList(params).then(response => {
        if (response.code === 200) {
          this.tableData = response.data.records || []
          this.pagination.total = response.data.total || 0
        } else {
          this.$message.error(response.message || '加载失败')
        }
      }).finally(() => {
        this.loading = false
      })
    },
    loadCycleList() {
      getCostCycleList({ status: 1 }).then(response => {
        if (response.code === 200) {
          this.cycleList = response.data || []
        }
      })
    },
    loadDeptList() {
      getDeptList().then(response => {
        if (response.code === 200) {
          this.deptList = response.data || []
        }
      })
    },
    loadElementTypeOptions() {
      getCodeByType('ELEMENT_TYPE').then(response => {
        if (response.code === 200 && response.data) {
          this.elementTypeOptions = response.data
            .filter(item => item.isStop === 0 || item.isStop === '0')
            .map(item => ({
              label: item.codeName,
              value: item.codeValue
            }))
        }
      }).catch(() => {
        this.elementTypeOptions = []
      })
    },
    loadPayTypeOptions() {
      getCodeByType('PAYMENT_METHOD').then(response => {
        if (response.code === 200 && response.data) {
          this.payTypeOptions = response.data
            .filter(item => item.isStop === 0 || item.isStop === '0')
            .map(item => ({
              label: item.codeName,
              value: item.codeValue
            }))
        }
      }).catch(() => {
        this.payTypeOptions = []
      })
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = {
        cycleId: null,
        deptId: null,
        elementType: '',
        dateRange: []
      }
      this.handleSearch()
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.loadData()
    },
    handleCurrentChange(val) {
      this.pagination.page = val
      this.loadData()
    },
    // 查看周期列表
    handleViewCycle() {
      this.cycleListDialogVisible = true
      this.loadAllCycleList()
    },
    // 加载所有周期列表（不限制状态）
    loadAllCycleList() {
      this.cycleLoading = true
      getCostCycleList({}).then(response => {
        if (response.code === 200) {
          this.allCycleList = response.data || []
        } else {
          this.$message.error(response.message || '加载失败')
        }
      }).finally(() => {
        this.cycleLoading = false
      })
    },
    // 新增周期
    handleAddCycle() {
      this.isEditCycle = false
      this.cycleForm = {
        cycleCode: '',
        cycleName: '',
        cycleType: '',
        startDate: '',
        endDate: '',
        status: 1
      }
      this.cycleDialogVisible = true
    },
    // 编辑周期
    handleEditCycle(row) {
      this.isEditCycle = true
      this.cycleForm = {
        cycleId: row.cycleId,
        cycleCode: row.cycleCode,
        cycleName: row.cycleName,
        cycleType: row.cycleType,
        startDate: row.startDate,
        endDate: row.endDate,
        status: row.status
      }
      this.cycleDialogVisible = true
      // 如果是在周期列表对话框中打开的，先关闭列表对话框
      if (this.cycleListDialogVisible) {
        this.cycleListDialogVisible = false
      }
    },
    // 删除周期
    handleDeleteCycle(row) {
      this.$confirm('确认删除该周期吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteCostCycle(row.cycleId).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.loadAllCycleList()
            this.loadCycleList() // 同时刷新查询下拉框中的周期列表
          } else {
            this.$message.error(response.message || '删除失败')
          }
        })
      })
    },
    // 保存周期（新增或编辑）
    handleSaveCycle() {
      this.$refs.cycleForm.validate((valid) => {
        if (valid) {
          const api = this.isEditCycle ? updateCostCycle : saveCostCycle
          api(this.cycleForm).then(response => {
            if (response.code === 200) {
              this.$message.success('保存成功')
              this.cycleDialogVisible = false
              this.loadCycleList() // 刷新查询下拉框中的周期列表
              // 如果周期列表对话框是打开的，刷新周期列表
              if (this.cycleListDialogVisible) {
                this.loadAllCycleList()
              }
            } else {
              this.$message.error(response.message || '保存失败')
            }
          })
        }
      })
    },
    handleAdd() {
      this.dialogTitle = '新增成本'
      this.isEdit = false
      this.form = {
        costId: null,
        cycleId: null,
        deptId: null,
        elementType: '',
        costAmount: 0,
        occurDate: '',
        payType: '',
        remark: ''
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑成本'
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleSave() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          const api = this.isEdit ? updateCostMain : saveCostMain
          api(this.form).then(response => {
            if (response.code === 200) {
              this.$message.success('保存成功')
              this.dialogVisible = false
              this.loadData()
            } else {
              this.$message.error(response.message || '保存失败')
            }
          })
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该成本记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteCostMain(row.costId).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        })
      })
    },
    handleDownloadTemplate() {
      // 下载Excel模板
      window.open('/api/cost/main/template', '_blank')
    },
    beforeUpload(file) {
      const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || 
                      file.type === 'application/vnd.ms-excel'
      if (!isExcel) {
        this.$message.error('只能上传Excel文件！')
        return false
      }
      const isLt10M = file.size / 1024 / 1024 < 10
      if (!isLt10M) {
        this.$message.error('文件大小不能超过10MB！')
        return false
      }
      // 校验通过后开启导入加载动画
      this.importLoading = true
      return true
    },
    handleUploadSuccess(response) {
      // 兼容多种返回格式（字符串 / 对象 / axios 包了一层 data）
      let res = response

      // 如果是字符串，尝试解析为 JSON
      if (typeof res === 'string') {
        try {
          res = JSON.parse(res)
        } catch (e) {
          this.$message.error('导入失败：响应格式错误')
          this.importLoading = false
          return
        }
      }

      // 如果是 axios 响应，可能在 data 里再包一层
      if (res && res.data && typeof res.data === 'object' && typeof res.code === 'undefined') {
        res = res.data
      }

      if (res && res.code === 200) {
        this.$message.success('导入成功')
        // 重新加载数据，保证导入结果立即体现在列表上
        this.loadData()
      } else {
        this.$message.error((res && res.message) || '导入失败')
      }
      // 无论成功失败，关闭加载动画
      this.importLoading = false
    },
    handleUploadError(error) {
      this.$message.error('导入失败：' + (error.message || '未知错误'))
      this.importLoading = false
    },
    getElementTypeName(codeValue) {
      if (!codeValue) return ''
      const option = this.elementTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    getPayTypeName(codeValue) {
      if (!codeValue) return ''
      const option = this.payTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportCost(false)
      } else if (command === 'all') {
        this.handleExportCost(true)
      }
    },
    // 导出成本数据
    async handleExportCost(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const params = {
            page: 1,
            size: 10000, // 设置一个很大的值以获取所有数据
            cycleId: this.searchForm.cycleId,
            deptId: this.searchForm.deptId,
            elementType: this.searchForm.elementType || null,
            startDate: this.searchForm.dateRange && this.searchForm.dateRange[0] || null,
            endDate: this.searchForm.dateRange && this.searchForm.dateRange[1] || null
          }
          
          const res = await getCostMainList(params)
          if (res.code === 200 && res.data) {
            dataToExport = res.data.records || []
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
        const headers = ['成本编号', '核算周期', '部门', '成本要素', '成本金额', '发生日期', '付款方式', '备注']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            String(item.costNo || ''),
            String(item.cycleName || ''),
            String(item.deptName || ''),
            String(this.getElementTypeName(item.elementType) || ''),
            String(item.costAmount ? '¥' + item.costAmount : ''),
            String(item.occurDate || ''),
            String(this.getPayTypeName(item.payType) || ''),
            String(item.remark || '')
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '成本归集' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
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
        link.download = '成本归集' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
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
    formatDate(date) {
      if (!date) return ''
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const hour = String(d.getHours()).padStart(2, '0')
      const minute = String(d.getMinutes()).padStart(2, '0')
      const second = String(d.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}:${second}`
    }
  }
}
</script>

<style scoped>
.cost-collection {
  padding: 20px;
}
</style>

