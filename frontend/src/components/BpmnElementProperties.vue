<template>
  <div class="bpmn-element-properties">
    <el-form label-width="120px" size="small" v-if="element">
      <!-- 通用属性 -->
      <el-form-item label="节点名称">
        <el-input v-model="localElement.name" @input="handleUpdate"></el-input>
      </el-form-item>

      <!-- 开始节点 -->
      <template v-if="isStartEvent">
        <el-form-item label="节点类型">
          <el-tag type="success">开始节点</el-tag>
        </el-form-item>
      </template>

      <!-- 用户任务节点 -->
      <template v-if="isUserTask">
        <el-form-item label="节点类型">
          <el-tag type="primary">用户任务</el-tag>
        </el-form-item>
        
        <el-divider>审批人配置</el-divider>
        <el-form-item label="审批类型">
          <el-radio-group v-model="localElement.approvalType" @change="handleUpdate">
            <el-radio label="SINGLE">单人审批</el-radio>
            <el-radio label="MULTI">会签（全部通过）</el-radio>
            <el-radio label="OR">或签（任一通过）</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="审批人类型">
          <el-select v-model="localElement.assigneeType" @change="handleUpdate" style="width: 100%;">
            <el-option label="指定用户" value="user"></el-option>
            <el-option label="指定岗位" value="position"></el-option>
            <el-option label="指定部门" value="dept"></el-option>
            <el-option label="发起人" value="initiator"></el-option>
            <el-option label="上一节点审批人" value="previous"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="审批人" v-if="localElement.assigneeType === 'user'">
          <el-select
            v-model="localElement.assigneeId"
            filterable
            placeholder="请选择审批人"
            @change="handleAssigneeChange"
            style="width: 100%;"
            :loading="loadingEmployee"
          >
            <el-option
              v-for="user in employeeList"
              :key="user.empId || user.employeeId"
              :label="(user.empName || user.employeeName) + (user.empCode ? ` (${user.empCode})` : '')"
              :value="user.empId || user.employeeId"
            ></el-option>
            <el-option v-if="employeeList.length === 0 && !loadingEmployee" disabled value="">
              暂无数据
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="岗位" v-if="localElement.assigneeType === 'position'">
          <el-select
            v-model="localElement.positionCode"
            placeholder="请选择岗位"
            @change="handleUpdate"
            style="width: 100%;"
          >
            <el-option
              v-for="pos in positionOptions"
              :key="pos.positionCode"
              :label="pos.positionName"
              :value="pos.positionCode"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="部门" v-if="localElement.assigneeType === 'dept'">
          <el-select
            v-model="localElement.deptCode"
            placeholder="请选择部门"
            @change="handleUpdate"
            style="width: 100%;"
          >
            <el-option
              v-for="dept in deptOptions"
              :key="dept.deptCode"
              :label="dept.deptName"
              :value="dept.deptCode"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-divider>会签配置</el-divider>
        <el-form-item label="会签策略" v-if="localElement.approvalType === 'MULTI'">
          <el-radio-group v-model="localElement.multiInstanceType" @change="handleUpdate">
            <el-radio label="ALL">全部通过</el-radio>
            <el-radio label="ANY">任一通过</el-radio>
            <el-radio label="PERCENT">按比例通过</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="通过比例" v-if="localElement.approvalType === 'MULTI' && localElement.multiInstanceType === 'PERCENT'">
          <el-input-number 
            v-model="localElement.completionCondition" 
            :min="1" 
            :max="100" 
            :precision="0"
            @change="handleUpdate"
            style="width: 100%;"
          >
            <template slot="append">%</template>
          </el-input-number>
        </el-form-item>
        
        <el-form-item label="会签人数" v-if="localElement.approvalType === 'MULTI'">
          <el-input-number 
            v-model="localElement.multiInstanceCount" 
            :min="2" 
            :max="100" 
            @change="handleUpdate"
            style="width: 100%;"
            placeholder="会签人数"
          ></el-input-number>
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            提示：会签人数为0时，将根据审批人类型自动计算人数
          </div>
        </el-form-item>

        <el-divider>高级配置</el-divider>
        <el-form-item label="允许加签">
          <el-switch v-model="localElement.allowAddsign" :active-value="1" :inactive-value="0" @change="handleUpdate"></el-switch>
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            开启后，审批人可以在审批过程中添加其他审批人
          </div>
        </el-form-item>
        
        <el-form-item label="允许减签" v-if="localElement.allowAddsign === 1">
          <el-switch v-model="localElement.allowReduceSign" :active-value="1" :inactive-value="0" @change="handleUpdate"></el-switch>
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            开启后，审批人可以在审批过程中移除其他审批人
          </div>
        </el-form-item>
        
        <el-form-item label="允许转办">
          <el-switch v-model="localElement.allowTransfer" :active-value="1" :inactive-value="0" @change="handleUpdate"></el-switch>
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            开启后，审批人可以将任务转办给其他人
          </div>
        </el-form-item>
        
        <el-form-item label="允许退回">
          <el-switch v-model="localElement.allowReject" :active-value="1" :inactive-value="0" @change="handleUpdate"></el-switch>
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            开启后，审批人可以退回任务到上一节点
          </div>
        </el-form-item>
        
        <el-form-item label="退回策略" v-if="localElement.allowReject === 1">
          <el-radio-group v-model="localElement.rejectStrategy" @change="handleUpdate">
            <el-radio label="PREVIOUS">退回上一节点</el-radio>
            <el-radio label="START">退回发起人</el-radio>
            <el-radio label="SPECIFY">退回指定节点</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="需要打印">
          <el-switch v-model="localElement.needPrint" :active-value="1" :inactive-value="0" @change="handleUpdate"></el-switch>
        </el-form-item>
        
        <el-form-item label="打印顺序" v-if="localElement.needPrint === 1">
          <el-input-number v-model="localElement.printOrder" :min="1" :max="100" @change="handleUpdate"></el-input-number>
        </el-form-item>
        
        <el-form-item label="超时设置">
          <el-switch v-model="localElement.enableTimeout" :active-value="1" :inactive-value="0" @change="handleUpdate"></el-switch>
        </el-form-item>
        
        <el-form-item label="超时时间（小时）" v-if="localElement.enableTimeout === 1">
          <el-input-number 
            v-model="localElement.timeoutHours" 
            :min="1" 
            :max="720" 
            @change="handleUpdate"
            style="width: 100%;"
          ></el-input-number>
        </el-form-item>
        
        <el-form-item label="超时处理" v-if="localElement.enableTimeout === 1">
          <el-radio-group v-model="localElement.timeoutAction" @change="handleUpdate">
            <el-radio label="AUTO_PASS">自动通过</el-radio>
            <el-radio label="AUTO_REJECT">自动拒绝</el-radio>
            <el-radio label="NOTIFY">通知提醒</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="节点描述">
          <el-input type="textarea" v-model="localElement.description" @input="handleUpdate" :rows="3"></el-input>
        </el-form-item>
      </template>

      <!-- 网关节点 -->
      <template v-if="isGateway">
        <el-form-item label="节点类型">
          <el-tag type="warning">排他网关</el-tag>
        </el-form-item>
        <el-form-item label="网关类型">
          <el-radio-group v-model="localElement.gatewayType" @change="handleUpdate">
            <el-radio label="EXCLUSIVE">排他网关（XOR）</el-radio>
            <el-radio label="PARALLEL">并行网关（AND）</el-radio>
            <el-radio label="INCLUSIVE">包容网关（OR）</el-radio>
          </el-radio-group>
        </el-form-item>
      </template>

      <!-- 结束节点 -->
      <template v-if="isEndEvent">
        <el-form-item label="节点类型">
          <el-tag type="danger">结束节点</el-tag>
        </el-form-item>
      </template>

      <!-- 连线条件 -->
      <template v-if="isSequenceFlow">
        <el-form-item label="连线类型">
          <el-tag type="info">连线</el-tag>
        </el-form-item>
        <el-form-item label="条件类型">
          <el-radio-group v-model="localElement.conditionType" @change="handleUpdate">
            <el-radio label="none">无条件</el-radio>
            <el-radio label="expression">表达式</el-radio>
            <el-radio label="script">脚本</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="条件表达式" v-if="localElement.conditionType !== 'none'">
          <el-input
            type="textarea"
            v-model="localElement.conditionExpression"
            @input="handleUpdate"
            :rows="4"
            placeholder="例如: ${amount > 1000}"
            ref="conditionExpressionInput"
          ></el-input>
          <div style="margin-top: 10px;">
            <el-button 
              size="small" 
              icon="el-icon-search" 
              @click="showTableSelector = true"
              title="选择数据库字段"
            >选择字段</el-button>
          </div>
        </el-form-item>
      </template>
    </el-form>

    <!-- 数据库表字段选择对话框 -->
    <el-dialog
      title="选择数据库字段"
      :visible.sync="showTableSelector"
      width="800px"
      :modal="false"
      top="10vh"
      custom-class="table-selector-dialog"
    >
      <div style="display: flex; height: 500px; border: 1px solid #e0e0e0; border-radius: 4px;">
        <!-- 左侧表列表 -->
        <div style="width: 250px; border-right: 1px solid #e0e0e0; display: flex; flex-direction: column;">
          <div style="padding: 10px; border-bottom: 1px solid #e0e0e0;">
            <el-input
              placeholder="搜索表名"
              v-model="tableSearchKeyword"
              clearable
              size="small"
              @input="filterTableTreeData"
            ></el-input>
          </div>
          <div style="flex: 1; overflow-y: auto;">
            <el-tree
              :data="filteredTableTreeData"
              :props="{ label: 'name' }"
              @node-click="handleTableSelect"
              :expand-on-click-node="false"
              node-key="id"
              ref="tableTree"
              v-loading="loadingTables"
            >
              <span class="custom-tree-node" slot-scope="{ node, data }">
                <span>{{ data.name }}</span>
              </span>
            </el-tree>
            <div v-if="!loadingTables && filteredTableTreeData.length === 0" style="text-align: center; color: #909399; padding: 20px;">
              暂无数据
            </div>
          </div>
        </div>

        <!-- 右侧字段列表 -->
        <div style="flex: 1; display: flex; flex-direction: column;">
          <div style="padding: 10px; border-bottom: 1px solid #e0e0e0;">
            <el-input
              placeholder="搜索字段名/说明"
              v-model="fieldSearchKeyword"
              clearable
              size="small"
              @input="handleFieldSearch"
            ></el-input>
          </div>
          <div style="flex: 1; overflow-y: auto;">
            <el-table
              :data="filteredTableFields"
              style="width: 100%;"
              size="small"
              highlight-current-row
              @row-click="handleFieldRowClick"
              v-loading="loadingFields"
            >
              <el-table-column prop="columnName" label="字段名" width="150"></el-table-column>
              <el-table-column prop="columnLabel" label="字段说明"></el-table-column>
              <el-table-column prop="dataType" label="数据类型" width="100"></el-table-column>
              <el-table-column label="操作" width="80">
                <template slot-scope="scope">
                  <el-button type="text" size="small" @click.stop="insertField(scope.row)">插入</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="!loadingFields && filteredTableFields.length === 0" style="text-align: center; color: #909399; padding: 20px;">
              暂无数据
            </div>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showTableSelector = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getEmployeeList, getPositionList, getDeptList } from '@/api/user'
import { getAllTableNames, getTableFieldsByTableName } from '@/api/print'

export default {
  name: 'BpmnElementProperties',
  props: {
    element: {
      type: Object,
      default: null
    },
    bpmnModeling: {
      type: Object,
      default: null
    },
    elementRegistry: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      localElement: {},
      employeeList: [],
      positionOptions: [],
      deptOptions: [],
      loadingEmployee: false,
      // 数据库表字段选择相关
      showTableSelector: false,
      tableList: [],
      tableTreeData: [],
      filteredTableTreeData: [],
      tableSearchKeyword: '',
      selectedTableName: '',
      tableFields: [],
      fieldSearchKeyword: '',
      filteredTableFields: [],
      loadingTables: false,
      loadingFields: false
    }
  },
  computed: {
    isStartEvent() {
      return this.element && this.element.type && this.element.type.includes('StartEvent')
    },
    isUserTask() {
      return this.element && this.element.type && this.element.type.includes('UserTask')
    },
    isGateway() {
      return this.element && this.element.type && (
        this.element.type.includes('Gateway') || 
        this.element.type.includes('ExclusiveGateway') ||
        this.element.type.includes('ParallelGateway')
      )
    },
    isEndEvent() {
      return this.element && this.element.type && this.element.type.includes('EndEvent')
    },
    isSequenceFlow() {
      return this.element && this.element.type && this.element.type.includes('SequenceFlow')
    }
  },
  watch: {
    element: {
      immediate: true,
      deep: true,
      handler(newVal) {
        if (newVal) {
          this.loadElementProperties(newVal)
        }
      }
    },
    tableSearchKeyword(newVal) {
      this.filterTableTreeData(newVal)
    },
    fieldSearchKeyword(newVal) {
      this.handleFieldSearch(newVal)
    },
    showTableSelector(newVal) {
      if (newVal && this.tableList.length === 0) {
        this.loadTableList()
      }
    }
  },
  mounted() {
    this.loadData()
    this.loadTableList()
  },
  methods: {
    loadData() {
      // 加载员工列表
      this.loadingEmployee = true
      getEmployeeList().then(response => {
        this.loadingEmployee = false
        if (response.code === 200) {
          this.employeeList = response.data || []
          console.log('员工列表加载成功，数量:', this.employeeList.length)
          console.log('员工列表数据示例:', this.employeeList[0])
          if (this.employeeList.length === 0) {
            console.warn('员工列表为空，请检查后端接口或数据')
            this.$message.warning('员工列表为空，请先添加员工数据')
          }
        } else {
          console.error('加载员工列表失败:', response.message)
          this.$message.warning('加载员工列表失败: ' + (response.message || '未知错误'))
        }
      }).catch(error => {
        this.loadingEmployee = false
        console.error('加载员工列表异常:', error)
        this.$message.error('加载员工列表异常: ' + (error.message || '网络错误'))
      })
      
      // 加载岗位列表
      getPositionList().then(response => {
        if (response.code === 200) {
          this.positionOptions = (response.data || []).filter(pos => pos.isStop === 0)
          console.log('岗位列表加载成功，数量:', this.positionOptions.length)
        } else {
          console.error('加载岗位列表失败:', response.message)
        }
      }).catch(error => {
        console.error('加载岗位列表异常:', error)
      })
      
      // 加载部门列表
      getDeptList().then(response => {
        if (response.code === 200) {
          this.deptOptions = response.data || []
          console.log('部门列表加载成功，数量:', this.deptOptions.length)
        } else {
          console.error('加载部门列表失败:', response.message)
        }
      }).catch(error => {
        console.error('加载部门列表异常:', error)
      })
    },
    loadElementProperties(element) {
      const businessObject = element.businessObject || {}
      const name = businessObject.name || ''
      
      // 处理条件表达式（可能是 FormalExpression 对象）
      let conditionExpression = ''
      let conditionType = 'none'
      if (businessObject.conditionExpression) {
        if (typeof businessObject.conditionExpression === 'string') {
          conditionExpression = businessObject.conditionExpression
          conditionType = businessObject.conditionType || 'expression'
        } else if (businessObject.conditionExpression.body) {
          conditionExpression = businessObject.conditionExpression.body
          conditionType = businessObject.conditionType || 'expression'
        }
      } else if (businessObject.conditionType) {
        conditionType = businessObject.conditionType
      }
      
      this.localElement = {
        id: element.id,
        name: name,
        type: element.type,
        approvalType: businessObject.approvalType || 'SINGLE',
        assigneeType: businessObject.assigneeType || 'user',
        assigneeId: businessObject.assigneeId || null,
        assigneeName: businessObject.assigneeName || '',
        positionCode: businessObject.positionCode || '',
        deptCode: businessObject.deptCode || '',
        // 会签配置
        multiInstanceType: businessObject.multiInstanceType || 'ALL',
        multiInstanceCount: businessObject.multiInstanceCount || 0,
        completionCondition: businessObject.completionCondition || 100,
        // 高级配置
        allowAddsign: businessObject.allowAddsign !== undefined ? businessObject.allowAddsign : 0,
        allowReduceSign: businessObject.allowReduceSign !== undefined ? businessObject.allowReduceSign : 0,
        allowTransfer: businessObject.allowTransfer !== undefined ? businessObject.allowTransfer : 0,
        allowReject: businessObject.allowReject !== undefined ? businessObject.allowReject : 0,
        rejectStrategy: businessObject.rejectStrategy || 'PREVIOUS',
        needPrint: businessObject.needPrint !== undefined ? businessObject.needPrint : 0,
        printOrder: businessObject.printOrder || 1,
        // 超时设置
        enableTimeout: businessObject.enableTimeout !== undefined ? businessObject.enableTimeout : 0,
        timeoutHours: businessObject.timeoutHours || 24,
        timeoutAction: businessObject.timeoutAction || 'NOTIFY',
        description: businessObject.description || '',
        gatewayType: businessObject.gatewayType || 'EXCLUSIVE',
        conditionType: conditionType || 'none', // 默认为无条件
        conditionExpression: conditionExpression
      }
    },
    handleUpdate() {
      this.$emit('update', this.localElement)
    },
    handleAssigneeChange(value) {
      const user = this.employeeList.find(u => (u.empId || u.employeeId) === value)
      if (user) {
        this.localElement.assigneeName = user.empName || user.employeeName || ''
        this.localElement.assigneeCode = user.empCode || user.employeeCode || ''
        console.log('选择审批人:', this.localElement.assigneeName, this.localElement.assigneeCode)
      }
      this.handleUpdate()
    },
    // 数据库表字段选择相关方法
    async loadTableList() {
      this.loadingTables = true
      try {
        const response = await getAllTableNames()
        if (response.code === 200) {
          this.tableList = response.data || []
          this.buildTableTree()
        } else {
          this.$message.error('加载表列表失败')
        }
      } catch (error) {
        console.error('加载表列表失败:', error)
        this.$message.error('加载表列表失败')
      } finally {
        this.loadingTables = false
      }
    },
    buildTableTree() {
      this.tableTreeData = this.tableList.map((tableName, index) => ({
        id: `table-${index}`,
        name: tableName,
        type: 'table',
        children: []
      }))
      this.filterTableTreeData(this.tableSearchKeyword)
    },
    filterTableTreeData(keyword) {
      if (!keyword) {
        this.filteredTableTreeData = this.tableTreeData
      } else {
        this.filteredTableTreeData = this.tableTreeData.filter(node => 
          node.name.toLowerCase().includes(keyword.toLowerCase())
        )
      }
    },
    handleTableSearch() {
      if (this.$refs.tableTree) {
        this.$refs.tableTree.filter(this.tableSearchKeyword)
      }
    },
    filterTableNode(value, data) {
      if (!value) return true
      return data.name.toLowerCase().includes(value.toLowerCase())
    },
    async handleTableSelect(data) {
      if (data.type === 'table') {
        this.selectedTableName = data.name
        this.fieldSearchKeyword = ''
        await this.loadTableFields(data.name)
      }
    },
    async loadTableFields(tableName) {
      this.loadingFields = true
      try {
        const response = await getTableFieldsByTableName(tableName)
        this.loadingFields = false
        if (response.code === 200) {
          this.tableFields = response.data || []
          this.filteredTableFields = this.tableFields
          console.log('字段列表加载成功，数量:', this.tableFields.length)
        } else {
          this.$message.error('加载字段列表失败: ' + (response.message || '未知错误'))
          this.tableFields = []
          this.filteredTableFields = []
        }
      } catch (error) {
        this.loadingFields = false
        console.error('加载字段列表失败:', error)
        this.$message.error('加载字段列表失败: ' + (error.message || '网络错误'))
        this.tableFields = []
        this.filteredTableFields = []
      }
    },
    handleFieldSearch() {
      if (!this.fieldSearchKeyword) {
        this.filteredTableFields = this.tableFields
        return
      }
      const keyword = this.fieldSearchKeyword.toLowerCase()
      this.filteredTableFields = this.tableFields.filter(field => 
        (field.columnName && field.columnName.toLowerCase().includes(keyword)) ||
        (field.columnLabel && field.columnLabel.toLowerCase().includes(keyword))
      )
    },
    handleFieldRowClick(row) {
      this.insertField(row)
    },
    insertField(field) {
      // 获取当前光标位置
      const input = this.$refs.conditionExpressionInput
      if (!input) {
        // 如果没有ref，直接追加到末尾
        const currentValue = this.localElement.conditionExpression || ''
        const fieldExpression = `\${${field.tableName}.${field.columnName}}`
        this.localElement.conditionExpression = currentValue + fieldExpression
        this.handleUpdate()
        return
      }

      // 获取textarea元素
      const textarea = input.$el.querySelector('textarea')
      if (!textarea) {
        const currentValue = this.localElement.conditionExpression || ''
        const fieldExpression = `\${${field.tableName}.${field.columnName}}`
        this.localElement.conditionExpression = currentValue + fieldExpression
        this.handleUpdate()
        return
      }

      // 获取当前光标位置
      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const currentValue = this.localElement.conditionExpression || ''
      
      // 构建字段表达式
      const fieldExpression = `\${${field.tableName}.${field.columnName}}`
      
      // 在光标位置插入字段表达式
      const newValue = currentValue.substring(0, start) + fieldExpression + currentValue.substring(end)
      this.localElement.conditionExpression = newValue
      
      // 设置光标位置到插入内容之后
      this.$nextTick(() => {
        textarea.focus()
        const newCursorPos = start + fieldExpression.length
        textarea.setSelectionRange(newCursorPos, newCursorPos)
      })
      
      this.handleUpdate()
    }
  }
}
</script>

<style scoped>
.bpmn-element-properties {
  padding: 10px;
}
</style>

