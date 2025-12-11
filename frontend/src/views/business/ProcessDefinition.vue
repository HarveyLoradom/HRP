<template>
  <div class="process-definition">
    <el-card>
      <div slot="header" class="clearfix">
        <span>流程定义</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增流程</el-button>
      </div>
      
      <!-- 筛选条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="流程类型">
          <el-select v-model="searchForm.definitionType" placeholder="请选择类型" clearable @change="handleSearch">
            <el-option
              v-for="option in processTypeOptions"
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
        <el-table-column prop="definitionKey" label="流程KEY" width="200"></el-table-column>
        <el-table-column prop="definitionName" label="流程名称" width="200"></el-table-column>
        <el-table-column prop="definitionType" label="流程类型" width="120">
          <template slot-scope="scope">
            {{ getProcessTypeName(scope.row.definitionType) }}
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80"></el-table-column>
        <el-table-column label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isActive === 1 ? 'success' : 'danger'">
              {{ scope.row.isActive === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="250">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="primary" @click="handleDesign(scope.row)">设计流程</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="流程KEY" prop="definitionKey">
          <el-input v-model="form.definitionKey" :disabled="isEdit" placeholder="请输入流程KEY（如：PAYOUT_APPROVAL）"></el-input>
        </el-form-item>
        <el-form-item label="流程名称" prop="definitionName">
          <el-input v-model="form.definitionName" placeholder="请输入流程名称"></el-input>
        </el-form-item>
        <el-form-item label="流程类型" prop="definitionType">
          <el-select v-model="form.definitionType" placeholder="请选择流程类型">
            <el-option
              v-for="option in processTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>

    <!-- 流程设计对话框 -->
    <el-dialog
      title="流程设计"
      :visible.sync="designDialogVisible"
      width="90%"
      :close-on-click-modal="false"
    >
      <div class="process-designer">
        <el-tabs v-model="designActiveTab">
          <el-tab-pane label="可视化设计" name="visual">
            <div class="process-flow-designer">
              <div class="flow-nodes">
                <div class="flow-node start-node" @click="addNode('start')">
                  <i class="el-icon-video-play"></i>
                  <span>开始</span>
                </div>
                <div class="flow-nodes-list">
                  <div 
                    v-for="(node, index) in flowNodes" 
                    :key="index" 
                    class="flow-node-item"
                  >
                    <div class="node-header">
                      <span class="node-type">{{ getNodeTypeName(node.type) }}</span>
                      <el-button size="mini" type="danger" icon="el-icon-delete" @click="removeNode(index)"></el-button>
                    </div>
                    <div class="node-content">
                      <el-form :model="node" label-width="80px" size="small">
                        <el-form-item label="节点名称">
                          <el-input v-model="node.name" placeholder="请输入节点名称"></el-input>
                        </el-form-item>
                        <el-form-item label="节点类型">
                          <el-select v-model="node.type" placeholder="请选择">
                            <el-option label="审批节点" value="approve"></el-option>
                            <el-option label="会签节点" value="countersign"></el-option>
                            <el-option label="条件节点" value="condition"></el-option>
                          </el-select>
                        </el-form-item>
                        <el-form-item label="审批人类型" v-if="node.type === 'approve' || node.type === 'countersign'">
                          <el-radio-group v-model="node.assigneeType">
                            <el-radio label="user">指定用户</el-radio>
                            <el-radio label="position">指定岗位</el-radio>
                            <el-radio label="dept">部门负责人</el-radio>
                          </el-radio-group>
                        </el-form-item>
                        <el-form-item label="审批人" v-if="node.assigneeType === 'user'">
                          <el-autocomplete
                            v-model="node.assigneeName"
                            :fetch-suggestions="searchEmployee"
                            placeholder="请输入工号或姓名搜索"
                            value-key="empCode"
                            @select="(item) => handleAssigneeSelect(item, node)"
                            style="width: 100%"
                          >
                            <template slot-scope="{ item }">
                              <div>{{ item.empCode }} - {{ item.empName }}</div>
                            </template>
                          </el-autocomplete>
                        </el-form-item>
                        <el-form-item label="岗位" v-if="node.assigneeType === 'position'">
                          <el-select v-model="node.positionCode" placeholder="请选择岗位" style="width: 100%">
                            <el-option
                              v-for="pos in positionOptions"
                              :key="pos.codeValue"
                              :label="pos.codeName"
                              :value="pos.codeValue"
                            ></el-option>
                          </el-select>
                        </el-form-item>
                        <el-form-item label="部门" v-if="node.assigneeType === 'dept'">
                          <el-select v-model="node.deptCode" placeholder="请选择部门" style="width: 100%">
                            <el-option
                              v-for="dept in deptOptions"
                              :key="dept.deptCode"
                              :label="dept.deptName"
                              :value="dept.deptCode"
                            ></el-option>
                          </el-select>
                        </el-form-item>
                      </el-form>
                    </div>
                  </div>
                </div>
                <div class="flow-node end-node" @click="addNode('end')">
                  <i class="el-icon-video-pause"></i>
                  <span>结束</span>
                </div>
              </div>
              <div class="flow-actions">
                <el-button type="primary" @click="addNode('approve')">添加审批节点</el-button>
                <el-button type="success" @click="generateProcessJson">生成流程JSON</el-button>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="JSON编辑" name="json">
            <el-form :model="designForm" label-width="100px">
              <el-form-item label="流程JSON">
                <el-input
                  v-model="designForm.processJson"
                  type="textarea"
                  :rows="15"
                  placeholder="请输入流程JSON配置"
                ></el-input>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="designDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveDesign">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getProcessDefinitionList, getProcessDefinitionByType, saveProcessDefinition, updateProcessDefinition, deleteProcessDefinition } from '@/api/process'
import { getCodeTypeOptions } from '@/utils/codeType'
import { getEmployeeList, getCodeByType, getDeptList } from '@/api/user'

export default {
  name: 'ProcessDefinition',
  data() {
    return {
      loading: false,
      tableData: [],
      processTypeOptions: [],
      searchForm: {
        definitionType: ''
      },
      dialogVisible: false,
      dialogTitle: '新增流程',
      isEdit: false,
      form: {
        definitionId: null,
        definitionKey: '',
        definitionName: '',
        definitionType: '',
        description: '',
        processJson: '',
        version: 1,
        isActive: 1
      },
      rules: {
        definitionKey: [{ required: true, message: '请输入流程KEY', trigger: 'blur' }],
        definitionName: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
        definitionType: [{ required: true, message: '请选择流程类型', trigger: 'change' }]
      },
      designDialogVisible: false,
      designActiveTab: 'visual',
      designForm: {
        definitionId: null,
        processJson: ''
      },
      flowNodes: [],
      employeeList: [],
      positionOptions: [],
      deptOptions: []
    }
  },
  mounted() {
    this.loadProcessTypeOptions()
    this.loadData()
  },
  methods: {
    async loadProcessTypeOptions() {
      this.processTypeOptions = await getCodeTypeOptions('PROCESS_TYPE')
    },
    getProcessTypeName(codeValue) {
      const option = this.processTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    loadData() {
      this.loading = true
      const api = this.searchForm.definitionType ? getProcessDefinitionByType(this.searchForm.definitionType) : getProcessDefinitionList()
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
      this.searchForm.definitionType = ''
      this.loadData()
    },
    handleAdd() {
      this.dialogTitle = '新增流程'
      this.isEdit = false
      this.form = {
        definitionId: null,
        definitionKey: '',
        definitionName: '',
        definitionType: '',
        description: '',
        processJson: '',
        version: 1,
        isActive: 1
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑流程'
      this.isEdit = true
      this.form = {
        definitionId: row.definitionId,
        definitionKey: row.definitionKey,
        definitionName: row.definitionName,
        definitionType: row.definitionType,
        description: row.description || '',
        processJson: row.processJson || '',
        version: row.version,
        isActive: row.isActive
      }
      this.dialogVisible = true
    },
    handleDesign(row) {
      this.designForm = {
        definitionId: row.definitionId,
        processJson: row.processJson || ''
      }
      // 如果已有流程JSON，解析为节点
      if (row.processJson) {
        try {
          const json = JSON.parse(row.processJson)
          this.flowNodes = json.nodes || []
        } catch (e) {
          this.flowNodes = []
        }
      } else {
        this.flowNodes = []
      }
      this.loadDesignData()
      this.designDialogVisible = true
    },
    async loadDesignData() {
      // 加载职工列表
      getEmployeeList().then(response => {
        if (response.code === 200) {
          this.employeeList = response.data || []
        }
      })
      // 加载岗位列表
      getCodeByType('POSITION').then(response => {
        if (response.code === 200) {
          this.positionOptions = response.data || []
        }
      })
      // 加载部门列表
      getDeptList().then(response => {
        if (response.code === 200) {
          this.deptOptions = response.data || []
        }
      })
    },
    addNode(type) {
      if (type === 'start' || type === 'end') {
        return
      }
      const newNode = {
        id: 'node_' + Date.now(),
        name: '',
        type: type,
        assigneeType: 'user',
        assigneeId: null,
        assigneeName: '',
        positionCode: '',
        deptCode: ''
      }
      this.flowNodes.push(newNode)
    },
    removeNode(index) {
      this.flowNodes.splice(index, 1)
    },
    getNodeTypeName(type) {
      const typeMap = {
        'approve': '审批节点',
        'countersign': '会签节点',
        'condition': '条件节点'
      }
      return typeMap[type] || type
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
    handleAssigneeSelect(item, node) {
      node.assigneeId = item.empId
      node.assigneeName = `${item.empCode} - ${item.empName}`
    },
    generateProcessJson() {
      const processJson = {
        nodes: this.flowNodes,
        version: '1.0'
      }
      this.designForm.processJson = JSON.stringify(processJson, null, 2)
      this.designActiveTab = 'json'
      this.$message.success('流程JSON已生成，请检查并保存')
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const api = this.isEdit ? updateProcessDefinition : saveProcessDefinition
          api(this.form).then(response => {
            if (response.code === 200) {
              this.$message.success(response.message || '操作成功')
              this.dialogVisible = false
              this.loadData()
            } else {
              this.$message.error(response.message || '操作失败')
            }
          })
        }
      })
    },
    handleSaveDesign() {
      if (!this.designForm.definitionId) {
        this.$message.error('流程定义ID不能为空')
        return
      }
      updateProcessDefinition({
        definitionId: this.designForm.definitionId,
        processJson: this.designForm.processJson
      }).then(response => {
        if (response.code === 200) {
          this.$message.success('保存成功')
          this.designDialogVisible = false
          this.loadData()
        } else {
          this.$message.error(response.message || '保存失败')
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该流程定义吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteProcessDefinition(row.definitionId).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.process-definition {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.process-designer {
  min-height: 400px;
}

.process-flow-designer {
  padding: 20px;
}

.flow-nodes {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 20px;
}

.flow-node {
  padding: 15px;
  border: 2px dashed #409EFF;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #f0f9ff;
}

.flow-node:hover {
  background: #e0f2fe;
  border-color: #1890ff;
}

.flow-node i {
  font-size: 24px;
  color: #409EFF;
  display: block;
  margin-bottom: 5px;
}

.flow-node.start-node {
  border-color: #67C23A;
  background: #f0f9ff;
}

.flow-node.end-node {
  border-color: #F56C6C;
  background: #fef0f0;
}

.flow-nodes-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.flow-node-item {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
}

.node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
}

.node-type {
  font-weight: 600;
  color: #409EFF;
}

.node-content {
  padding: 15px;
}

.flow-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-top: 20px;
}
</style>



