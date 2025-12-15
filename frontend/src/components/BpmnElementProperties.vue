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
          >
            <el-option
              v-for="user in employeeList"
              :key="user.employeeId"
              :label="user.employeeName"
              :value="user.employeeId"
            ></el-option>
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

        <el-divider>高级配置</el-divider>
        <el-form-item label="允许加签">
          <el-switch v-model="localElement.allowAddsign" :active-value="1" :inactive-value="0" @change="handleUpdate"></el-switch>
        </el-form-item>
        
        <el-form-item label="需要打印">
          <el-switch v-model="localElement.needPrint" :active-value="1" :inactive-value="0" @change="handleUpdate"></el-switch>
        </el-form-item>
        
        <el-form-item label="打印顺序" v-if="localElement.needPrint === 1">
          <el-input-number v-model="localElement.printOrder" :min="1" :max="100" @change="handleUpdate"></el-input-number>
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
          ></el-input>
        </el-form-item>
      </template>
    </el-form>
  </div>
</template>

<script>
import { getEmployeeList, getPositionList, getDeptList } from '@/api/hr'

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
      deptOptions: []
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
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      getEmployeeList().then(response => {
        if (response.code === 200) {
          this.employeeList = response.data || []
        }
      })
      getPositionList().then(response => {
        if (response.code === 200) {
          this.positionOptions = (response.data || []).filter(pos => pos.isStop === 0)
        }
      })
      getDeptList().then(response => {
        if (response.code === 200) {
          this.deptOptions = response.data || []
        }
      })
    },
    loadElementProperties(element) {
      const businessObject = element.businessObject || {}
      const name = businessObject.name || ''
      
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
        allowAddsign: businessObject.allowAddsign !== undefined ? businessObject.allowAddsign : 0,
        needPrint: businessObject.needPrint !== undefined ? businessObject.needPrint : 0,
        printOrder: businessObject.printOrder || 1,
        description: businessObject.description || '',
        gatewayType: businessObject.gatewayType || 'EXCLUSIVE',
        conditionType: businessObject.conditionType || 'none',
        conditionExpression: businessObject.conditionExpression || ''
      }
    },
    handleUpdate() {
      this.$emit('update', this.localElement)
    },
    handleAssigneeChange(value) {
      const user = this.employeeList.find(u => u.employeeId === value)
      if (user) {
        this.localElement.assigneeName = user.employeeName
      }
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

