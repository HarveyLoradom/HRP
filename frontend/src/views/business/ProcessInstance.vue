<template>
  <div class="process-instance">
    <el-card>
      <div slot="header" class="clearfix">
        <span>流程实例</span>
      </div>
      
      <!-- 筛选条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="业务主键">
          <el-input v-model="searchForm.businessKey" placeholder="请输入单号或合同号" clearable @keyup.enter.native="handleSearch"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="businessKey" label="业务主键" width="200"></el-table-column>
        <el-table-column prop="businessType" label="业务类型" width="150">
          <template slot-scope="scope">
            {{ getBusinessTypeName(scope.row.businessType) }}
          </template>
        </el-table-column>
        <el-table-column prop="startUserName" label="审批人" width="120"></el-table-column>
        <el-table-column prop="startTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="250">
          <template slot-scope="scope">
            <el-button size="mini" type="danger" @click="handleTerminate(scope.row)" v-if="scope.row.processStatus === 'RUNNING'">停止流程</el-button>
            <el-button size="mini" type="primary" @click="handleViewVariables(scope.row)">查看变量</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container" style="margin-top: 20px; text-align: right;">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total">
        </el-pagination>
      </div>
    </el-card>

    <!-- 查看变量对话框 -->
    <el-dialog
      title="流程变量"
      :visible.sync="variablesDialogVisible"
      width="900px"
    >
      <el-table :data="paginatedVariablesData" border style="width: 100%">
        <el-table-column prop="variableKey" label="变量KEY" width="200"></el-table-column>
        <el-table-column prop="variableValue" label="变量值">
          <template slot-scope="scope">
            <el-input 
              v-model="scope.row.variableValue"
              :disabled="!variablesEditMode"
              placeholder="请输入变量值"
            ></el-input>
          </template>
        </el-table-column>
        <el-table-column prop="variableType" label="变量类型" width="120"></el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        @size-change="handleVariablesSizeChange"
        @current-change="handleVariablesCurrentChange"
        :current-page="variablesPagination.page"
        :page-sizes="[5,10, 20, 50, 100]"
        :page-size="variablesPagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="variablesPagination.total"
        style="margin-top: 20px; text-align: right;"
      ></el-pagination>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="variablesDialogVisible = false">关闭</el-button>
        <el-button v-if="!variablesEditMode" type="primary" @click="handleEditVariables">编辑</el-button>
        <template v-else>
          <el-button @click="handleCancelEditVariables">取消</el-button>
          <el-button type="primary" @click="handleSaveVariables">保存</el-button>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getProcessInstanceList, getProcessInstanceListPage, getProcessInstanceVariables, updateProcessInstanceVariables, terminateProcessInstance } from '@/api/process'
import { getCodeTypeOptions } from '@/utils/codeType'

export default {
  name: 'ProcessInstance',
  data() {
    return {
      loading: false,
      tableData: [],
      businessTypeOptions: [],
      searchForm: {
        businessKey: ''
      },
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      variablesDialogVisible: false,
      variablesData: [],
      originalVariablesData: [], // 保存原始数据，用于取消编辑
      variablesEditMode: false, // 是否处于编辑模式
      variablesPagination: {
        page: 1,
        size: 5,
        total: 0
      },
      currentBusinessKey: null,
      currentBusinessType: null
    }
  },
  mounted() {
    this.loadCodeTypeOptions()
    this.loadData()
  },
  computed: {
    paginatedVariablesData() {
      const start = (this.variablesPagination.page - 1) * this.variablesPagination.size
      const end = start + this.variablesPagination.size
      return this.variablesData.slice(start, end)
    }
  },
  methods: {
    async loadCodeTypeOptions() {
      this.businessTypeOptions = await getCodeTypeOptions('BUSINESS_TYPE')
    },
    getBusinessTypeName(codeValue) {
      const option = this.businessTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    loadData() {
      this.loading = true
      
      // 使用分页接口查询所有流程实例（显示全院人员发起的流程）
      getProcessInstanceListPage(this.pagination.page, this.pagination.size).then(response => {
        if (response.code === 200 && response.data) {
          let instances = response.data.records || []
          
          // 如果有业务主键搜索条件，进行前端过滤（后端分页不支持搜索条件时）
          if (this.searchForm.businessKey && this.searchForm.businessKey.trim()) {
            instances = instances.filter(instance => 
              instance.businessKey && instance.businessKey.includes(this.searchForm.businessKey.trim())
            )
          }
          
          this.tableData = instances
          this.pagination.total = response.data.total || 0
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
      this.searchForm.businessKey = ''
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
    handleViewVariables(row) {
      this.currentBusinessKey = row.businessKey
      this.currentBusinessType = row.businessType
      this.variablesEditMode = false
      this.variablesPagination.page = 1
      this.variablesPagination.size = 5
      
      // 传入businessKey和businessType，从业务表获取变量
      getProcessInstanceVariables(row.businessKey, row.businessType).then(response => {
        if (response.code === 200) {
          // 深拷贝数据
          this.variablesData = JSON.parse(JSON.stringify(response.data || []))
          this.originalVariablesData = JSON.parse(JSON.stringify(response.data || []))
          this.variablesPagination.total = this.variablesData.length
          this.variablesDialogVisible = true
        } else {
          this.$message.error('获取流程变量失败')
        }
      }).catch(error => {
        this.$message.error('获取流程变量失败：' + (error.message || '未知错误'))
      })
    },
    handleEditVariables() {
      this.variablesEditMode = true
    },
    handleCancelEditVariables() {
      // 恢复原始数据
      this.variablesData = JSON.parse(JSON.stringify(this.originalVariablesData))
      this.variablesEditMode = false
    },
    handleSaveVariables() {
      if (!this.currentBusinessKey || !this.currentBusinessType) {
        this.$message.error('业务主键或业务类型为空')
        return
      }
      
      // 传入businessKey和businessType，直接更新业务表
      updateProcessInstanceVariables(this.currentBusinessKey, this.currentBusinessType, this.variablesData).then(response => {
        if (response.code === 200) {
          this.$message.success('保存成功')
          // 更新原始数据
          this.originalVariablesData = JSON.parse(JSON.stringify(this.variablesData))
          this.variablesEditMode = false
          this.loadData() // 刷新列表数据
        } else {
          this.$message.error(response.message || '保存失败')
        }
      }).catch(error => {
        this.$message.error('保存失败：' + (error.message || '未知错误'))
      })
    },
    handleVariablesSizeChange(val) {
      this.variablesPagination.size = val
      this.variablesPagination.page = 1
    },
    handleVariablesCurrentChange(val) {
      this.variablesPagination.page = val
    },
    handleTerminate(row) {
      this.$confirm('确认要停止该流程吗？停止后所有待处理任务将被终止。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        terminateProcessInstance(row.businessKey).then(response => {
          if (response.code === 200) {
            this.$message.success('流程已停止')
            this.loadData()
          } else {
            this.$message.error(response.message || '停止流程失败')
          }
        }).catch(error => {
          this.$message.error('停止流程失败：' + (error.message || '未知错误'))
        })
      }).catch(() => {})
    },
  }
}
</script>

<style scoped>
.process-instance {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>



