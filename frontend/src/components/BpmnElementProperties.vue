<template>
  <div class="bpmn-element-properties">
    <el-form ref="propertiesForm" label-width="120px" size="small" v-if="element">
      <!-- 通用属性 -->
      <el-form-item label="节点名称">
        <el-input v-model="localElement.name"></el-input>
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
          <el-radio-group v-model="localElement.approvalType">
            <el-radio label="SINGLE">单人审批</el-radio>
            <el-radio label="MULTI">会签（全部通过）</el-radio>
            <el-radio label="OR">或签（任一通过）</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="审批人类型">
          <el-select v-model="localElement.assigneeType" @change="handleAssigneeTypeChange" style="width: 100%;">
            <el-option label="指定用户" value="user"></el-option>
            <el-option label="指定岗位" value="position"></el-option>
            <el-option label="指定部门" value="dept"></el-option>
            <el-option label="归口审批人" value="manage_dept"></el-option>
            <el-option label="发起人" value="initiator"></el-option>
            <el-option label="上一节点审批人" value="previous"></el-option>
            <el-option label="请选择负责人" value="responsible"></el-option>
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
            clearable
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
            @change="handleDeptChange"
            style="width: 100%;"
            filterable
            clearable
          >
            <el-option
              v-for="dept in deptOptions"
              :key="dept.deptCode"
              :label="dept.deptName"
              :value="dept.deptCode"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="请选择负责人" v-if="localElement.assigneeType === 'responsible'" prop="responsibleType">
          <el-select
            v-model="localElement.responsibleType"
            placeholder="请选择负责人类型"
            @change="handleResponsibleTypeChange"
            style="width: 100%;"
          >
            <el-option
              label="发起人部门负责人"
              value="DEPT_MANAGER"
            ></el-option>
            <el-option
              label="发起人部门护士长"
              value="NURSE_MANAGER"
            ></el-option>
            <el-option
              label="发起人部门分管院长"
              value="VICE_PRESIDENT"
            ></el-option>
          </el-select>
        </el-form-item>
        

        <el-divider>会签配置</el-divider>
        <el-form-item label="会签策略" v-if="localElement.approvalType === 'MULTI'">
          <el-radio-group v-model="localElement.multiInstanceType">
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
            style="width: 100%;"
            placeholder="会签人数"
          ></el-input-number>
        </el-form-item>

        <el-divider>高级配置</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="允许加签">
              <el-switch v-model="localElement.allowAddsign" :active-value="1" :inactive-value="0"></el-switch>
            </el-form-item>
          </el-col>
          <el-col :span="12">
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="允许转办">
              <el-switch v-model="localElement.allowTransfer" :active-value="1" :inactive-value="0"></el-switch>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="允许退回">
              <el-switch v-model="localElement.allowReject" :active-value="1" :inactive-value="0"></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="退回策略" v-if="localElement.allowReject === 1">
          <el-radio-group v-model="localElement.rejectStrategy">
            <el-radio label="PREVIOUS">退回上一节点</el-radio>
            <el-radio label="START">退回发起人</el-radio>
            <el-radio label="SPECIFY">退回指定节点</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="必须打印" :rules="[{ required: true, message: '必须打印字段为必填项', trigger: 'change' }]">
              <el-switch v-model="localElement.needPrint" :active-value="1" :inactive-value="0"></el-switch>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="打印顺序" prop="printOrder" :rules="[{ required: true, message: '打印顺序为必填项', trigger: 'blur' }, { type: 'number', min: 1, max: 100, message: '打印顺序必须在1-100之间', trigger: 'blur' }]">
              <el-input-number v-model="localElement.printOrder" :min="1" :max="100" style="width: 100%;" :precision="0"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="超时设置">
              <el-switch v-model="localElement.enableTimeout" :active-value="1" :inactive-value="0"></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="超时时间（小时）" v-if="localElement.enableTimeout === 1">
          <el-input-number 
            v-model="localElement.timeoutHours" 
            :min="1" 
            :max="720" 
            style="width: 100%;"
          ></el-input-number>
        </el-form-item>
        
        <el-form-item label="超时处理" v-if="localElement.enableTimeout === 1">
          <el-radio-group v-model="localElement.timeoutAction">
            <el-radio label="AUTO_PASS">自动通过</el-radio>
            <el-radio label="AUTO_REJECT">自动拒绝</el-radio>
            <el-radio label="NOTIFY">通知提醒</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="节点描述">
          <el-input type="textarea" v-model="localElement.description" :rows="3"></el-input>
        </el-form-item>
      </template>

      <!-- 网关节点 -->
      <template v-if="isGateway">
        <el-form-item label="节点类型">
          <el-tag type="warning">排他网关</el-tag>
        </el-form-item>
        <el-form-item label="网关类型">
          <el-radio-group v-model="localElement.gatewayType">
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
          <el-radio-group v-model="localElement.conditionType">
            <el-radio label="none">无条件</el-radio>
            <el-radio label="expression">表达式</el-radio>
            <el-radio label="script">脚本</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="条件表达式" v-if="localElement.conditionType !== 'none'">
          <el-input
            type="textarea"
            v-model="localElement.conditionExpression"
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
    <div slot="footer" class="dialog-footer" style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; padding-top: 20px; border-top: 1px solid #e0e0e0;">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确定</el-button>
    </div>

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
    },
  },
  watch: {
    element: {
      immediate: true,
      deep: true,
      handler(newVal, oldVal) {
        if (newVal) {
          // 每次打开弹窗时都重新加载属性，确保获取最新的值
          // 重新加载所有列表数据，确保数据是最新的
          this.loadEmployeeList()
          this.loadPositionList()
          this.loadDeptList()
          this.$nextTick(() => {
            this.loadElementProperties(newVal)
          })
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
      this.loadEmployeeList()
      
      // 加载岗位列表
      this.loadPositionList()
      
      // 加载部门列表
      this.loadDeptList()
    },
    loadEmployeeList() {
      // 先清空旧数据
      this.employeeList = []
      this.loadingEmployee = true
      getEmployeeList().then(response => {
        this.loadingEmployee = false
        if (response.code === 200 && response.data) {
          // 确保只显示有效的员工（有 empId 和 empCode）
          this.employeeList = (response.data || []).filter(emp => {
            return emp && emp.empId && emp.empCode
          })
          if (this.employeeList.length === 0) {
            this.$message.warning('员工列表为空，请先添加员工数据')
          }
        } else {
          this.employeeList = []
          this.$message.warning('加载员工列表失败: ' + (response.message || '未知错误'))
        }
      }).catch(error => {
        this.loadingEmployee = false
        this.employeeList = []
        this.$message.error('加载员工列表异常: ' + (error.message || '网络错误'))
      })
    },
    loadDeptList() {
      // 先清空旧数据
      this.deptOptions = []
      getDeptList().then(response => {
        if (response.code === 200 && response.data) {
          // 确保只显示有效的部门（有 deptId）
          this.deptOptions = (response.data || []).filter(dept => {
            return dept && dept.deptId
          })
        } else {
          this.deptOptions = []
        }
      }).catch(() => {
        this.deptOptions = []
      })
    },
    loadPositionList() {
      // 先清空旧数据
      this.positionOptions = []
      // 重新加载岗位列表，确保数据是最新的
      // 传递 null 获取所有岗位，然后在前端过滤停用的
      getPositionList(null).then(response => {
        if (response.code === 200 && response.data) {
          // 过滤停用的岗位（isStop === 0），并确保有有效的 positionId 和 positionCode
          this.positionOptions = (response.data || []).filter(pos => {
            return pos && 
                   pos.positionId && 
                   pos.positionCode && 
                   pos.isStop === 0
          })
        } else {
          this.positionOptions = []
        }
      }).catch(() => {
        this.positionOptions = []
      })
    },
    loadElementProperties(element) {
      // 始终从 elementRegistry 获取最新的元素，确保获取到最新的 businessObject
      let businessObject = null
      if (element && element.id && this.$parent && this.$parent.bpmnModeler) {
        try {
          const elementRegistry = this.$parent.bpmnModeler.get('elementRegistry')
          const latestElement = elementRegistry.get(element.id)
          if (latestElement && latestElement.businessObject) {
            businessObject = latestElement.businessObject
          }
        } catch (e) {
          // 静默处理错误
        }
      }
      
      // 如果从 elementRegistry 获取失败，尝试使用传入的 element.businessObject
      if (!businessObject && element && element.businessObject) {
        businessObject = element.businessObject
      }
      
      businessObject = businessObject || {}
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
        assigneeCode: businessObject.assigneeCode || '',
        positionCode: businessObject.positionCode || '',
        deptCode: businessObject.deptCode || '',
        responsibleType: businessObject.responsibleType || '',
        // 会签配置
        multiInstanceType: businessObject.multiInstanceType || 'ALL',
        multiInstanceCount: businessObject.multiInstanceCount || 0,
        completionCondition: businessObject.completionCondition || 100,
        // 高级配置
        allowAddsign: businessObject.allowAddsign !== undefined ? businessObject.allowAddsign : 0,
        allowTransfer: businessObject.allowTransfer !== undefined ? businessObject.allowTransfer : 0,
        allowReject: businessObject.allowReject !== undefined ? businessObject.allowReject : 0,
        rejectStrategy: businessObject.rejectStrategy || 'PREVIOUS',
        needPrint: businessObject.needPrint !== undefined ? businessObject.needPrint : 1, // 默认必须打印
        printOrder: businessObject.printOrder || 1, // 默认打印顺序为1
        // 超时设置
        enableTimeout: businessObject.enableTimeout !== undefined ? businessObject.enableTimeout : 0,
        timeoutHours: businessObject.timeoutHours || 24,
        timeoutAction: businessObject.timeoutAction || 'NOTIFY',
        description: businessObject.description || '',
        gatewayType: businessObject.gatewayType || 'EXCLUSIVE',
        conditionType: conditionType || 'none', // 默认为无条件
        conditionExpression: conditionExpression
      }
      
      // 如果是部门类型，加载部门负责人信息
      if (this.localElement.assigneeType === 'dept' && this.localElement.deptCode) {
        this.$nextTick(() => {
          this.handleDeptChange(this.localElement.deptCode)
        })
      }
      // 如果是负责人类型，不需要预先设置审批人
    },
    handleConfirm() {
      // 验证必须打印和打印顺序
      if (this.isUserTask) {
        // 对于用户任务节点，必须打印必须为1
        if (this.localElement.needPrint !== 1) {
          this.$message.error('流程设置中，必须打印字段必须设置为"是"')
          return
        }
        // 打印顺序必须填写且大于0
        if (!this.localElement.printOrder || this.localElement.printOrder < 1 || this.localElement.printOrder > 100) {
          this.$message.error('打印顺序为必填项，且必须在1-100之间')
          return
        }
      }
      
      // 触发确认事件，传递当前配置
      this.$emit('confirm', this.localElement)
    },
    handleCancel() {
      // 触发取消事件
      this.$emit('cancel')
    },
    handleAssigneeChange(value) {
      const user = this.employeeList.find(u => (u.empId || u.employeeId) === value)
      if (user) {
      this.localElement.assigneeName = user.empName || user.employeeName || ''
      this.localElement.assigneeCode = user.empCode || user.employeeCode || ''
    }
    },
    handleAssigneeTypeChange(value) {
      // 当审批人类型改变时，清空相关的配置
      if (value !== 'user') {
        this.localElement.assigneeId = null
        this.localElement.assigneeName = ''
        this.localElement.assigneeCode = ''
      }
      if (value !== 'position') {
        this.localElement.positionCode = ''
      }
      if (value !== 'dept') {
        this.localElement.deptCode = ''
      }
      if (value !== 'responsible') {
        this.localElement.responsibleType = ''
      }
      // 归口审批人不需要额外配置
      if (value === 'manage_dept') {
        // 清空其他配置
        this.localElement.assigneeId = null
        this.localElement.positionCode = ''
        this.localElement.deptCode = ''
        this.localElement.responsibleType = ''
      }
    },
    handleDeptChange(value) {
      // 当选择部门时，自动获取该部门的负责人信息（用于流程审批时分配任务给负责人）
      if (value) {
        const selectedDept = this.deptOptions.find(dept => dept.deptCode === value)
        if (selectedDept && selectedDept.deptManagerName) {
          // 设置审批人信息，以便在流程中使用（自动分配给该部门的负责人）
          this.localElement.assigneeId = selectedDept.deptManagerId
          this.localElement.assigneeName = selectedDept.deptManagerName
          this.localElement.assigneeCode = selectedDept.deptManagerCode
        } else {
          this.localElement.assigneeId = null
          this.localElement.assigneeName = ''
          this.localElement.assigneeCode = ''
        }
      } else {
        this.localElement.assigneeId = null
        this.localElement.assigneeName = ''
        this.localElement.assigneeCode = ''
      }
    },
    handleResponsibleTypeChange(value) {
      // 当选择负责人类型时，清空具体的审批人信息，在流程启动时动态获取
      // 同时将responsibleType存储到deptCode字段中，以便后端识别
      this.localElement.deptCode = value // 将responsibleType存储到deptCode中
      this.localElement.assigneeId = null
      this.localElement.assigneeName = ''
      this.localElement.assigneeCode = ''
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
        return
      }

      // 获取textarea元素
      const textarea = input.$el.querySelector('textarea')
      if (!textarea) {
        const currentValue = this.localElement.conditionExpression || ''
        const fieldExpression = `\${${field.tableName}.${field.columnName}}`
        this.localElement.conditionExpression = currentValue + fieldExpression
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
    }
  }
}
</script>

<style scoped>
.bpmn-element-properties {
  padding: 10px;
}
</style>

