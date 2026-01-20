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
        <el-table-column prop="processDefinitionName" label="流程名称" width="150"></el-table-column>
        <el-table-column prop="taskName" label="任务名称" width="150"></el-table-column>
        <el-table-column prop="assigneeUserName" label="当前办理人" width="120"></el-table-column>
        <el-table-column prop="assigneeEmpCode" label="办理人工号" width="120"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="handleTransfer(scope.row)">转办</el-button>
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

    <!-- 转办对话框 -->
    <el-dialog
      title="转办"
      :visible.sync="transferDialogVisible"
      width="500px"
    >
      <el-form :model="transferForm" ref="transferForm" label-width="120px">
        <el-form-item label="转办给:">
          <el-select 
            v-model="transferForm.userId" 
            placeholder="请选择转办人员" 
            filterable 
            style="width: 100%;"
            @change="handleTransferUserChange"
          >
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="`${user.name}(${user.account})`"
              :value="user.id"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="transferDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveTransfer">确认转办</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAllCurrentTasks, getAllCurrentTasksPage, transferProcessTask } from '@/api/process'
import { getUserList } from '@/api/user'
import { getCodeTypeOptions } from '@/utils/codeType'

export default {
  name: 'ProcessTask',
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
      transferDialogVisible: false,
      transferForm: {
        taskId: null,
        userId: '',
        userName: '',
        userCode: ''
      },
      userList: []
    }
  },
  mounted() {
    this.loadCodeTypeOptions()
    this.loadUserList()
    this.loadData()
  },
  methods: {
    async loadCodeTypeOptions() {
      this.businessTypeOptions = await getCodeTypeOptions('BUSINESS_TYPE')
    },
    async loadUserList() {
      // 加载用户列表（用于转办时选择用户，只加载启用的用户）
      try {
        const response = await getUserList(0) // 0表示只加载启用的用户
        if (response.code === 200 && response.data) {
          this.userList = response.data
        }
      } catch (error) {
        console.error('加载用户列表失败:', error)
        this.userList = []
      }
    },
    getBusinessTypeName(codeValue) {
      const option = this.businessTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    loadData() {
      this.loading = true
      
      // 使用分页接口查询所有当前节点的任务（显示所有流程的当前审批节点）
      getAllCurrentTasksPage(this.pagination.page, this.pagination.size).then(response => {
        if (response.code === 200 && response.data) {
          let tasks = response.data.records || []
          
          // 如果有业务主键搜索条件，进行前端过滤（后端分页不支持搜索条件时）
          if (this.searchForm.businessKey && this.searchForm.businessKey.trim()) {
            tasks = tasks.filter(task => 
              task.businessKey && task.businessKey.includes(this.searchForm.businessKey.trim())
            )
          }
          
          this.tableData = tasks
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
    handleTransfer(row) {
      this.transferForm = {
        taskId: row.taskId,
        userId: '',
        userName: '',
        userCode: ''
      }
      this.transferDialogVisible = true
    },
    handleTransferUserChange(userId) {
      const user = this.userList.find(u => u.id === userId)
      if (user) {
        this.transferForm.userName = user.name
        this.transferForm.userCode = user.account
      }
    },
    handleSaveTransfer() {
      if (!this.transferForm.userId) {
        this.$message.warning('请选择转办人员')
        return
      }
      
      transferProcessTask({
        taskId: this.transferForm.taskId,
        newAssigneeUserId: this.transferForm.userId,
        newAssigneeUserName: this.transferForm.userName,
        newAssigneeEmpCode: this.transferForm.userCode
      }).then(response => {
        if (response.code === 200) {
          this.$message.success('转办成功')
          this.transferDialogVisible = false
          this.loadData()
        } else {
          this.$message.error(response.message || '转办失败')
        }
      }).catch(error => {
        this.$message.error('转办失败：' + (error.message || '未知错误'))
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



