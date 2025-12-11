<template>
  <div class="apply-query">
    <el-card>
      <div slot="header">
        <span>申请查询</span>
      </div>
      
      <!-- 搜索条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="申请单号">
          <el-input v-model="searchForm.billcode" placeholder="请输入申请单号" clearable @keyup.enter.native="handleSearch"></el-input>
        </el-form-item>
        <el-form-item label="申请人">
          <el-input v-model="searchForm.empName" placeholder="请输入申请人姓名" clearable @keyup.enter.native="handleSearch"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option
              v-for="option in applyStatusOptions"
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
        <el-table-column prop="payoutBillcode" label="申请单号" width="150"></el-table-column>
        <el-table-column prop="empName" label="申请人" width="100"></el-table-column>
        <el-table-column prop="payoutTypeId" label="申请类型" width="120">
          <template slot-scope="scope">
            {{ getPayoutTypeName(scope.row.payoutTypeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="applyAmount" label="申请金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.applyAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="applyDate" label="申请日期" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.applyDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleView(scope.row)">查看</el-button>
            <el-button v-if="scope.row.processInstanceId" size="mini" type="primary" @click="handleViewProcess(scope.row)">流程信息</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog title="申请单详情" :visible.sync="detailVisible" width="600px">
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="申请单号">{{ currentDetail.payoutBillcode }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentDetail.empName }}</el-descriptions-item>
        <el-descriptions-item label="申请类型">{{ getPayoutTypeName(currentDetail.payoutTypeId) }}</el-descriptions-item>
        <el-descriptions-item label="申请金额">¥{{ currentDetail.applyAmount }}</el-descriptions-item>
        <el-descriptions-item label="申请日期">{{ formatDate(currentDetail.applyDate) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentDetail.status)">{{ getStatusText(currentDetail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请事由" :span="2">{{ currentDetail.applyReason }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentDetail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 查看流程图对话框 -->
    <el-dialog title="流程信息" :visible.sync="processVisible" width="90%">
      <div v-if="currentProcessInstance">
        <el-tabs v-model="processTab">
          <el-tab-pane label="流程节点" name="nodes">
            <el-table :data="processNodes" border style="width: 100%">
              <el-table-column prop="name" label="节点名称" width="150"></el-table-column>
              <el-table-column prop="type" label="节点类型" width="120">
                <template slot-scope="scope">
                  {{ getNodeTypeName(scope.row.type) }}
                </template>
              </el-table-column>
              <el-table-column prop="assigneeType" label="审批人类型" width="120">
                <template slot-scope="scope">
                  {{ scope.row.assigneeType === 'user' ? '指定用户' : scope.row.assigneeType === 'position' ? '指定岗位' : scope.row.assigneeType === 'dept' ? '部门负责人' : '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="assigneeName" label="审批人" width="200"></el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="流程变量" name="variables">
            <el-table :data="processVariables" border style="width: 100%">
              <el-table-column prop="variableKey" label="变量KEY" width="200"></el-table-column>
              <el-table-column prop="variableValue" label="变量值"></el-table-column>
              <el-table-column prop="variableType" label="变量类型" width="120"></el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="流程图" name="diagram">
            <div style="text-align: center; padding: 50px; border: 1px dashed #ddd; background: #f9f9f9;">
              <i class="el-icon-picture" style="font-size: 48px; color: #ccc;"></i>
              <p style="margin-top: 20px; color: #999;">流程图展示区域（后续可集成流程图渲染组件）</p>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAllPayouts, getPayoutById } from '@/api/reimb'
import { getProcessInstanceById, getProcessInstanceVariables } from '@/api/process'
import { getProcessDefinitionById } from '@/api/process'
import { getCodeTypeOptions } from '@/utils/codeType'

export default {
  name: 'ApplyQuery',
  data() {
    return {
      loading: false,
      tableData: [],
      allData: [],
      payoutTypeOptions: [],
      applyStatusOptions: [],
      searchForm: {
        billcode: '',
        empName: '',
        status: ''
      },
      detailVisible: false,
      currentDetail: null,
      processVisible: false,
      processTab: 'nodes',
      currentProcessInstance: null,
      processNodes: [],
      processVariables: []
    }
  },
  mounted() {
    this.loadCodeTypeOptions()
    this.loadData()
  },
  methods: {
    async loadCodeTypeOptions() {
      this.payoutTypeOptions = await getCodeTypeOptions('PAYOUT_TYPE')
      this.applyStatusOptions = await getCodeTypeOptions('APPLY_STATUS')
    },
    loadData() {
      this.loading = true
      getAllPayouts().then(response => {
        if (response.code === 200) {
          this.allData = response.data || []
          this.handleSearch()
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleSearch() {
      let filtered = [...this.allData]
      if (this.searchForm.billcode) {
        filtered = filtered.filter(item => item.payoutBillcode && item.payoutBillcode.includes(this.searchForm.billcode))
      }
      if (this.searchForm.empName) {
        filtered = filtered.filter(item => item.empName && item.empName.includes(this.searchForm.empName))
      }
      if (this.searchForm.status) {
        filtered = filtered.filter(item => item.status === this.searchForm.status)
      }
      this.tableData = filtered
    },
    handleReset() {
      this.searchForm = {
        billcode: '',
        empName: '',
        status: ''
      }
      this.tableData = this.allData
    },
    handleView(row) {
      getPayoutById(row.payoutId).then(response => {
        if (response.code === 200) {
          this.currentDetail = response.data
          this.detailVisible = true
        }
      })
    },
    async handleViewProcess(row) {
      if (!row.processInstanceId) {
        this.$message.warning('该申请单尚未启动流程')
        return
      }
      this.processVisible = true
      this.processNodes = []
      this.processVariables = []
      
      // 获取流程实例
      getProcessInstanceById(row.processInstanceId).then(response => {
        if (response.code === 200 && response.data) {
          this.currentProcessInstance = response.data
          // 获取流程定义
          if (response.data.processDefinitionId) {
            getProcessDefinitionById(response.data.processDefinitionId).then(defResponse => {
              if (defResponse.code === 200 && defResponse.data && defResponse.data.processJson) {
                try {
                  const json = JSON.parse(defResponse.data.processJson)
                  this.processNodes = json.nodes || []
                } catch (e) {
                  console.error('解析流程JSON失败:', e)
                }
              }
            })
          }
        }
      })
      
      // 获取流程变量
      getProcessInstanceVariables(row.processInstanceId).then(response => {
        if (response.code === 200) {
          this.processVariables = response.data || []
        }
      })
    },
    getPayoutTypeName(codeValue) {
      const option = this.payoutTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
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
    getNodeTypeName(type) {
      const typeMap = {
        'approve': '审批节点',
        'countersign': '会签节点',
        'condition': '条件节点'
      }
      return typeMap[type] || type
    }
  }
}
</script>

<style scoped>
.apply-query {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>

