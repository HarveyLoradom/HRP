<template>
  <div class="process-task">
    <el-card>
      <div slot="header" class="clearfix">
        <span>流程任务</span>
      </div>
      
      <!-- 筛选条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="业务主键">
          <el-input v-model="searchForm.businessKey" placeholder="请输入单号或合同号" clearable @keyup.enter.native="handleSearch"></el-input>
        </el-form-item>
        <el-form-item label="任务状态">
          <el-select v-model="searchForm.taskStatus" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option
              v-for="option in taskStatusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="businessKey" label="业务主键" width="150"></el-table-column>
        <el-table-column prop="businessType" label="业务类型" width="100">
          <template slot-scope="scope">
            {{ getBusinessTypeName(scope.row.businessType) }}
          </template>
        </el-table-column>
        <el-table-column prop="processDefinitionName" label="流程名称" width="150"></el-table-column>
        <el-table-column prop="taskName" label="任务名称" width="150"></el-table-column>
        <el-table-column prop="assigneeUserName" label="当前办理人" width="120"></el-table-column>
        <el-table-column prop="assigneeEmpCode" label="办理人工号" width="120"></el-table-column>
        <el-table-column label="任务状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.taskStatus === 'PENDING' ? 'warning' : scope.row.taskStatus === 'COMPLETED' ? 'success' : 'info'">
              {{ scope.row.taskStatus === 'PENDING' ? '待办理' : scope.row.taskStatus === 'COMPLETED' ? '已完成' : '已转办' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="handleTransfer(scope.row)" v-if="scope.row.taskStatus === 'PENDING'">转办</el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 转办对话框 -->
    <el-dialog
      title="转办任务"
      :visible.sync="transferDialogVisible"
      width="500px"
    >
      <el-form :model="transferForm" :rules="transferRules" ref="transferForm" label-width="100px">
        <el-form-item label="当前办理人">
          <el-input :value="transferForm.currentAssignee" disabled></el-input>
        </el-form-item>
        <el-form-item label="新办理人" prop="newAssigneeEmpCode">
          <el-autocomplete
            v-model="transferForm.newAssigneeEmpCode"
            :fetch-suggestions="searchEmployee"
            placeholder="请输入工号或姓名搜索"
            value-key="empCode"
            @select="handleEmployeeSelect"
            style="width: 100%"
          >
            <template slot-scope="{ item }">
              <div>{{ item.empCode }} - {{ item.empName }}</div>
            </template>
          </el-autocomplete>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="transferDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveTransfer">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getProcessTaskByBusinessKeyPage, getProcessTaskByStatusPage, transferProcessTask } from '@/api/process'
import { getEmployeeList, getEmployeeByCode, getUserByAccount } from '@/api/user'
import { getCodeTypeOptions } from '@/utils/codeType'
import { paginationMixin } from '@/mixins/pagination'

export default {
  name: 'ProcessTask',
  mixins: [paginationMixin],
  data() {
    return {
      loading: false,
      tableData: [],
      businessTypeOptions: [],
      taskStatusOptions: [],
      searchForm: {
        businessKey: '',
        taskStatus: ''
      },
      transferDialogVisible: false,
      transferForm: {
        taskId: null,
        currentAssignee: '',
        newAssigneeUserId: '',
        newAssigneeUserName: '',
        newAssigneeEmpCode: ''
      },
      transferRules: {
        newAssigneeEmpCode: [{ required: true, message: '请选择新办理人', trigger: 'change' }]
      },
      employeeList: []
    }
  },
  mounted() {
    this.loadCodeTypeOptions()
    this.loadData()
    this.loadEmployeeList()
  },
  methods: {
    async loadCodeTypeOptions() {
      this.businessTypeOptions = await getCodeTypeOptions('BUSINESS_TYPE')
      this.taskStatusOptions = await getCodeTypeOptions('TASK_STATUS')
    },
    getBusinessTypeName(codeValue) {
      const option = this.businessTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    getTaskStatusName(codeValue) {
      const option = this.taskStatusOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    getTaskStatusType(codeValue) {
      const typeMap = {
        'PENDING': 'warning',
        'COMPLETED': 'success',
        'TRANSFERRED': 'info'
      }
      return typeMap[codeValue] || 'info'
    },
    loadData() {
      this.loading = true
      let api
      if (this.searchForm.businessKey) {
        api = getProcessTaskByBusinessKeyPage(this.searchForm.businessKey, this.pagination.page, this.pagination.size)
      } else if (this.searchForm.taskStatus) {
        api = getProcessTaskByStatusPage(this.searchForm.taskStatus, this.pagination.page, this.pagination.size)
      } else {
        // 默认查询待办理任务
        api = getProcessTaskByStatusPage('PENDING', this.pagination.page, this.pagination.size)
      }
      api.then(response => {
        if (response.code === 200 && response.data) {
          this.tableData = response.data.records || []
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
    loadEmployeeList() {
      getEmployeeList().then(response => {
        if (response.code === 200) {
          this.employeeList = response.data || []
        }
      })
    },
    searchEmployee(queryString, cb) {
      const results = this.employeeList.filter(emp => {
        return (emp.empCode && emp.empCode.includes(queryString)) ||
               (emp.empName && emp.empName.includes(queryString))
      }).map(emp => ({
        empCode: emp.empCode,
        empName: emp.empName,
        empId: emp.empId
      }))
      cb(results)
    },
    handleEmployeeSelect(item) {
      // 根据工号查询用户信息
      getEmployeeByCode(item.empCode).then(response => {
        if (response.code === 200 && response.data) {
          const employee = response.data
          // 根据工号查询用户
          getUserByAccount(item.empCode).then(userResponse => {
            if (userResponse.code === 200 && userResponse.data) {
              this.transferForm.newAssigneeUserId = userResponse.data.id
              this.transferForm.newAssigneeUserName = userResponse.data.name
            } else {
              this.transferForm.newAssigneeUserId = ''
              this.transferForm.newAssigneeUserName = employee.empName
            }
          }).catch(() => {
            this.transferForm.newAssigneeUserId = ''
            this.transferForm.newAssigneeUserName = employee.empName
          })
        }
      })
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm.businessKey = ''
      this.searchForm.taskStatus = ''
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
    handleTransfer(row) {
      this.transferForm = {
        taskId: row.taskId,
        currentAssignee: `${row.assigneeUserName}(${row.assigneeEmpCode})`,
        newAssigneeUserId: '',
        newAssigneeUserName: '',
        newAssigneeEmpCode: ''
      }
      this.transferDialogVisible = true
    },
    handleSaveTransfer() {
      this.$refs.transferForm.validate(valid => {
        if (valid) {
          if (!this.transferForm.newAssigneeUserId && !this.transferForm.newAssigneeEmpCode) {
            this.$message.error('请选择新办理人')
            return
          }
          transferProcessTask({
            taskId: this.transferForm.taskId,
            newAssigneeUserId: this.transferForm.newAssigneeUserId,
            newAssigneeUserName: this.transferForm.newAssigneeUserName,
            newAssigneeEmpCode: this.transferForm.newAssigneeEmpCode
          }).then(response => {
            if (response.code === 200) {
              this.$message.success('转办成功')
              this.transferDialogVisible = false
              this.loadData()
            } else {
              this.$message.error(response.message || '转办失败')
            }
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.process-task {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>



