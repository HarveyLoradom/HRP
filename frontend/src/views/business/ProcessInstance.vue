<template>
  <div class="process-instance">
    <el-card>
      <div slot="header" class="clearfix">
        <span>流程实例</span>
      </div>
      
      <!-- 筛选条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="流程状态">
          <el-select v-model="searchForm.processStatus" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option
              v-for="option in processStatusOptions"
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
        <el-table-column prop="startUserName" label="启动人" width="120"></el-table-column>
        <el-table-column label="流程状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getProcessStatusType(scope.row.processStatus)">
              {{ getProcessStatusName(scope.row.processStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="启动时间" width="160"></el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="160"></el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="handleViewVariables(scope.row)">查看变量</el-button>
            <el-button size="mini" type="success" @click="handleViewDiagram(scope.row)">流程图</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 查看变量对话框 -->
    <el-dialog
      title="流程变量"
      :visible.sync="variablesDialogVisible"
      width="700px"
    >
      <el-table :data="variablesData" border style="width: 100%">
        <el-table-column prop="variableKey" label="变量KEY" width="200"></el-table-column>
        <el-table-column prop="variableValue" label="变量值"></el-table-column>
        <el-table-column prop="variableType" label="变量类型" width="120"></el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="variablesDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <!-- 查看流程图对话框 -->
    <el-dialog
      title="流程图"
      :visible.sync="diagramDialogVisible"
      width="90%"
    >
      <div class="process-diagram">
        <el-alert
          title="流程图"
          type="info"
          :closable="false"
          show-icon>
          <template slot="default">
            <p>流程图展示功能开发中，后续将集成流程图渲染组件（如：bpmn-js）</p>
            <p>当前流程实例ID：{{ currentInstanceId }}</p>
            <p v-if="currentProcessDefinition">流程定义：{{ currentProcessDefinition.definitionName }}</p>
          </template>
        </el-alert>
        <div v-if="currentProcessDefinition && currentProcessDefinition.processJson" style="margin-top: 20px;">
          <el-tabs v-model="diagramTab">
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
                <el-table-column prop="positionCode" label="岗位" width="120"></el-table-column>
                <el-table-column prop="deptCode" label="部门" width="120"></el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="流程JSON" name="json">
              <el-input
                v-model="processJsonText"
                type="textarea"
                :rows="15"
                readonly
              ></el-input>
            </el-tab-pane>
          </el-tabs>
        </div>
        <div v-else style="margin-top: 20px; text-align: center; padding: 50px; border: 1px dashed #ddd; background: #f9f9f9;">
          <i class="el-icon-picture" style="font-size: 48px; color: #ccc;"></i>
          <p style="margin-top: 20px; color: #999;">流程图展示区域</p>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="diagramDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getProcessInstanceList, getProcessInstanceByStatus, getProcessInstanceVariables } from '@/api/process'
import { getProcessDefinitionById } from '@/api/process'
import { getCodeTypeOptions } from '@/utils/codeType'

export default {
  name: 'ProcessInstance',
  data() {
    return {
      loading: false,
      tableData: [],
      businessTypeOptions: [],
      processStatusOptions: [],
      searchForm: {
        processStatus: ''
      },
      variablesDialogVisible: false,
      variablesData: [],
      diagramDialogVisible: false,
      currentInstanceId: null,
      currentProcessDefinition: null,
      diagramTab: 'nodes',
      processNodes: [],
      processJsonText: ''
    }
  },
  mounted() {
    this.loadCodeTypeOptions()
    this.loadData()
  },
  methods: {
    async loadCodeTypeOptions() {
      this.businessTypeOptions = await getCodeTypeOptions('BUSINESS_TYPE')
      this.processStatusOptions = await getCodeTypeOptions('PROCESS_STATUS')
    },
    getBusinessTypeName(codeValue) {
      const option = this.businessTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    getProcessStatusName(codeValue) {
      const option = this.processStatusOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    getProcessStatusType(codeValue) {
      const typeMap = {
        'RUNNING': 'warning',
        'COMPLETED': 'success',
        'TERMINATED': 'danger',
        'SUSPENDED': 'info'
      }
      return typeMap[codeValue] || 'info'
    },
    loadData() {
      this.loading = true
      const api = this.searchForm.processStatus ? getProcessInstanceByStatus(this.searchForm.processStatus) : getProcessInstanceList()
      api.then(response => {
        if (response.code === 200) {
          this.tableData = response.data || []
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleSearch() {
      this.loadData()
    },
    handleReset() {
      this.searchForm.processStatus = ''
      this.loadData()
    },
    handleViewVariables(row) {
      getProcessInstanceVariables(row.instanceId).then(response => {
        if (response.code === 200) {
          this.variablesData = response.data || []
          this.variablesDialogVisible = true
        } else {
          this.$message.error('获取流程变量失败')
        }
      })
    },
    async handleViewDiagram(row) {
      this.currentInstanceId = row.instanceId
      this.currentProcessDefinition = null
      this.processNodes = []
      this.processJsonText = ''
      // 获取流程定义，用于显示流程图
      if (row.processDefinitionId) {
        getProcessDefinitionById(row.processDefinitionId).then(response => {
          if (response.code === 200 && response.data) {
            this.currentProcessDefinition = response.data
            // 解析流程JSON
            if (response.data.processJson) {
              try {
                const json = JSON.parse(response.data.processJson)
                this.processNodes = json.nodes || []
                this.processJsonText = JSON.stringify(json, null, 2)
              } catch (e) {
                console.error('解析流程JSON失败:', e)
                this.processJsonText = response.data.processJson
              }
            }
          }
        })
      }
      this.diagramDialogVisible = true
    },
    getNodeTypeName(type) {
      const typeMap = {
        'approve': '审批节点',
        'countersign': '会签节点',
        'condition': '条件节点'
      }
      return typeMap[type] || type
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

.process-diagram {
  min-height: 400px;
}
</style>



