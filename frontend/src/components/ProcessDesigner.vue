<template>
  <div class="process-designer-container">
    <!-- 左侧工具栏 -->
    <div class="designer-toolbar">
      <div class="toolbar-title">节点工具</div>
      <div 
        class="toolbar-node-item"
        v-for="nodeType in nodeTypes"
        :key="nodeType.type"
        draggable="true"
        @dragstart="handleDragStart($event, nodeType)"
      >
        <i :class="nodeType.icon"></i>
        <span>{{ nodeType.label }}</span>
      </div>
    </div>

    <!-- 中间画板 -->
    <div class="designer-canvas-container" ref="canvasContainer">
        <div 
        class="designer-canvas" 
        ref="canvas"
        @drop="handleDrop"
        @dragover.prevent
        @click="handleCanvasClick"
        @mousemove="handleCanvasMouseMove"
        @mouseup="handleCanvasMouseUp"
      >
        <!-- 网格背景 -->
        <svg class="grid-background" width="100%" height="100%">
          <defs>
            <pattern id="grid" width="20" height="20" patternUnits="userSpaceOnUse">
              <path d="M 20 0 L 0 0 0 20" fill="none" stroke="#e0e0e0" stroke-width="1"/>
            </pattern>
          </defs>
          <rect width="100%" height="100%" fill="url(#grid)"/>
        </svg>

        <!-- 连线SVG -->
        <svg class="connections-svg" width="100%" height="100%" ref="connectionsSvg" @click="handleCanvasClick">
          <g v-for="(edge, index) in edges" :key="index">
            <path
              :d="getEdgePath(edge)"
              stroke="#409EFF"
              stroke-width="2"
              fill="none"
              marker-end="url(#arrowhead)"
              class="connection-line"
              @click.stop="handleEdgeClick(edge)"
            />
            <!-- 连线标签（显示条件） -->
            <text
              v-if="edge.conditionType && edge.conditionType !== 'none' && getEdgeLabel(edge)"
              :x="getEdgeLabelPosition(edge).x"
              :y="getEdgeLabelPosition(edge).y"
              class="edge-label"
              text-anchor="middle"
              @click.stop="handleEdgeClick(edge)"
            >
              {{ getEdgeLabel(edge) }}
            </text>
          </g>
          <!-- 临时连线（连接中或长按拖拽） -->
          <path
            v-if="(tempEdge && connectingFrom) || (isLongPressing && draggingNode)"
            :d="getTempEdgePath()"
            stroke="#67C23A"
            stroke-width="2"
            stroke-dasharray="5,5"
            fill="none"
            class="temp-connection-line"
          />
          <!-- 箭头标记 -->
          <defs>
            <marker id="arrowhead" markerWidth="10" markerHeight="10" refX="9" refY="3" orient="auto">
              <polygon points="0 0, 10 3, 0 6" fill="#409EFF" />
            </marker>
          </defs>
        </svg>

        <!-- 流程节点 -->
        <div
          v-for="node in nodes"
          :key="node.id"
          :id="'node-' + node.id"
          class="flow-node"
          :class="[`node-${node.type}`, { 
            'node-selected': selectedNode && selectedNode.id === node.id,
            'node-target': targetNodeForConnection && targetNodeForConnection.id === node.id
          }]"
          :style="{
            left: node.x + 'px',
            top: node.y + 'px'
          }"
          @click.stop="handleNodeClick(node)"
          @mousedown="handleNodeMouseDown($event, node)"
        >
          <div class="node-header">
            <!-- 排他节点显示为X形状 -->
            <template v-if="node.type === 'exclusive'">
              <div class="exclusive-x">
                <span class="x-line x-line-1"></span>
                <span class="x-line x-line-2"></span>
              </div>
            </template>
            <template v-else>
              <i :class="getNodeIcon(node.type)"></i>
              <span class="node-title">{{ node.name || getNodeDefaultName(node.type) }}</span>
            </template>
          </div>
          <div class="node-content" v-if="node.type !== 'exclusive'">
            <div class="node-info">{{ getNodeInfo(node) }}</div>
          </div>
          <!-- 连接点 -->
          <div 
            class="node-connector node-connector-top" 
            :data-node-id="node.id" 
            data-port="top"
            @mousedown.stop="handleConnectorMouseDown($event, node, 'top')"
            @click.stop="handleConnectorClick(node, 'top')"
          ></div>
          <div 
            class="node-connector node-connector-bottom" 
            :data-node-id="node.id" 
            data-port="bottom"
            @mousedown.stop="handleConnectorMouseDown($event, node, 'bottom')"
            @click.stop="handleConnectorClick(node, 'bottom')"
          ></div>
          <div 
            class="node-connector node-connector-left" 
            :data-node-id="node.id" 
            data-port="left"
            @mousedown.stop="handleConnectorMouseDown($event, node, 'left')"
            @click.stop="handleConnectorClick(node, 'left')"
          ></div>
          <div 
            class="node-connector node-connector-right" 
            :data-node-id="node.id" 
            data-port="right"
            @mousedown.stop="handleConnectorMouseDown($event, node, 'right')"
            @click.stop="handleConnectorClick(node, 'right')"
          ></div>
        </div>

        <!-- 空状态提示 -->
        <div v-if="nodes.length === 0" class="canvas-empty">
          <i class="el-icon-info"></i>
          <p>从左侧拖拽节点到此处开始设计流程</p>
        </div>
      </div>
    </div>

    <!-- 右侧属性面板 -->
    <div class="designer-properties" v-if="selectedNode">
      <div class="properties-header">
        <span>节点属性</span>
        <el-button size="mini" type="text" icon="el-icon-close" @click="selectedNode = null"></el-button>
      </div>
      <div class="properties-content">
        <el-form :model="selectedNode" label-width="100px" size="small">
          <el-form-item label="节点名称">
            <el-input v-model="selectedNode.name" placeholder="请输入节点名称"></el-input>
          </el-form-item>

          <!-- 审批节点配置 -->
          <template v-if="selectedNode.type === 'approve'">
            <el-form-item label="审批类型">
              <el-radio-group v-model="selectedNode.approvalType">
                <el-radio label="SINGLE">单人审批</el-radio>
                <el-radio label="COUNTERSIGN">会签（全部同意）</el-radio>
                <el-radio label="OR_SIGN">或签（任意一人同意）</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="审批人类型">
              <el-radio-group v-model="selectedNode.assigneeType">
                <el-radio label="user">指定用户</el-radio>
                <el-radio label="position">指定岗位</el-radio>
                <el-radio label="dept">部门负责人</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="审批人" v-if="selectedNode.assigneeType === 'user'">
              <el-autocomplete
                v-model="selectedNode.assigneeName"
                :fetch-suggestions="searchEmployee"
                placeholder="请输入工号或姓名搜索"
                value-key="empCode"
                @select="(item) => handleAssigneeSelect(item, selectedNode)"
                style="width: 100%"
              >
                <template slot-scope="{ item }">
                  <div>{{ item.empCode }} - {{ item.empName }}</div>
                </template>
              </el-autocomplete>
            </el-form-item>
            <el-form-item label="岗位" v-if="selectedNode.assigneeType === 'position'">
              <el-select v-model="selectedNode.positionCode" placeholder="请选择岗位" style="width: 100%">
                <el-option
                  v-for="pos in positionOptions"
                  :key="pos.positionCode"
                  :label="pos.positionName"
                  :value="pos.positionCode"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="部门" v-if="selectedNode.assigneeType === 'dept'">
              <el-select v-model="selectedNode.deptCode" placeholder="请选择部门" style="width: 100%">
                <el-option
                  v-for="dept in deptOptions"
                  :key="dept.deptCode"
                  :label="dept.deptName"
                  :value="dept.deptCode"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="可加签">
              <el-switch v-model="selectedNode.allowAddsign" :active-value="1" :inactive-value="0"></el-switch>
            </el-form-item>
            <el-form-item label="需要打印">
              <el-switch v-model="selectedNode.needPrint" :active-value="1" :inactive-value="0"></el-switch>
            </el-form-item>
          </template>

          <!-- 并行节点配置 -->
          <template v-if="selectedNode.type === 'parallel'">
            <el-form-item label="并行说明">
              <el-input type="textarea" v-model="selectedNode.description" placeholder="请输入并行说明"></el-input>
            </el-form-item>
          </template>

          <!-- 排他节点配置 -->
          <template v-if="selectedNode.type === 'exclusive'">
            <el-form-item label="条件说明">
              <el-input type="textarea" v-model="selectedNode.description" placeholder="请输入条件说明"></el-input>
            </el-form-item>
          </template>

          <el-form-item>
            <el-button type="danger" size="small" @click="handleDeleteNode">删除节点</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 连线条件配置对话框 -->
    <el-dialog title="连线条件设置" :visible.sync="edgeConfigVisible" width="800px" :modal="false">
      <el-form :model="currentEdge" label-width="120px">
        <el-form-item label="条件类型">
          <el-radio-group v-model="currentEdge.conditionType">
            <el-radio label="none">无条件（默认路径）</el-radio>
            <el-radio label="expression">表达式条件</el-radio>
            <el-radio label="builder">可视化构建</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 可视化构建器 -->
        <template v-if="currentEdge.conditionType === 'builder'">
          <div class="condition-builder">
            <div 
              v-for="(condition, index) in conditionList" 
              :key="index"
              class="condition-item"
            >
              <el-form-item :label="index === 0 ? '条件' : ''">
                <div class="condition-row">
                  <el-select 
                    v-model="condition.field" 
                    placeholder="选择字段" 
                    style="width: 200px;"
                    filterable
                    @change="handleFieldChange(condition)"
                  >
                    <el-option-group
                      v-for="group in fieldGroups"
                      :key="group.tableName"
                      :label="group.tableLabel"
                    >
                      <el-option
                        v-for="field in group.fields"
                        :key="field.fieldKey"
                        :label="`${field.columnLabel} (${field.fieldKey})`"
                        :value="field.fieldKey"
                      >
                        <span style="float: left">{{ field.columnLabel }}</span>
                        <span style="float: right; color: #8492a6; font-size: 13px">{{ field.fieldKey }}</span>
                      </el-option>
                    </el-option-group>
                  </el-select>
                  
                  <el-select v-model="condition.operator" placeholder="操作符" style="width: 120px;">
                    <el-option label="等于 (==)" value="=="></el-option>
                    <el-option label="不等于 (!=)" value="!="></el-option>
                    <el-option label="大于 (>)" value=">"></el-option>
                    <el-option label="小于 (<)" value="<"></el-option>
                    <el-option label="大于等于 (>=)" value=">="></el-option>
                    <el-option label="小于等于 (<=)" value="<="></el-option>
                    <el-option label="包含 (contains)" value="contains"></el-option>
                    <el-option label="不包含 (not contains)" value="not contains"></el-option>
                    <el-option label="在列表中 (in)" value="in"></el-option>
                  </el-select>
                  
                  <el-input 
                    v-model="condition.value" 
                    :placeholder="getValuePlaceholder(condition)"
                    style="width: 200px;"
                  ></el-input>
                  
                  <el-button 
                    type="danger" 
                    icon="el-icon-delete" 
                    circle 
                    size="small"
                    @click="removeCondition(index)"
                    v-if="conditionList.length > 1"
                  ></el-button>
                </div>
              </el-form-item>
              
              <!-- 逻辑运算符 -->
              <el-form-item v-if="index < conditionList.length - 1">
                <el-select v-model="condition.logic" style="width: 100px;">
                  <el-option label="并且 (&&)" value="&&"></el-option>
                  <el-option label="或者 (||)" value="||"></el-option>
                </el-select>
              </el-form-item>
            </div>
            
            <el-button type="text" icon="el-icon-plus" @click="addCondition">添加条件</el-button>
            
            <!-- 括号支持 -->
            <el-form-item label="表达式包装">
              <el-input 
                v-model="currentEdge.conditionWrap" 
                placeholder="可选：用括号包装，如：(condition1) && (condition2)"
              ></el-input>
            </el-form-item>
          </div>
        </template>
        
        <!-- 表达式编辑器 -->
        <template v-if="currentEdge.conditionType === 'expression'">
          <el-form-item label="可用字段">
            <el-select 
              v-model="selectedField" 
              placeholder="选择字段插入到表达式"
              filterable
              style="width: 100%"
              @change="insertFieldToExpression"
            >
              <el-option-group
                v-for="group in fieldGroups"
                :key="group.tableName"
                :label="group.tableLabel"
              >
                <el-option
                  v-for="field in group.fields"
                  :key="field.fieldKey"
                  :label="`${field.columnLabel} (${field.fieldKey})`"
                  :value="field.fieldKey"
                >
                  <span style="float: left">{{ field.columnLabel }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">{{ field.fieldKey }}</span>
                </el-option>
              </el-option-group>
            </el-select>
          </el-form-item>
          <el-form-item label="表达式">
            <el-input 
              ref="expressionTextarea"
              type="textarea" 
              v-model="currentEdge.conditionExpression" 
              :rows="6"
              placeholder="例如：sys_user.type == '管理员' && ctrl_payout.amount > 1000&#10;支持操作符：== != > < >= <= contains in&#10;支持逻辑运算符：&& || ()"
            ></el-input>
          </el-form-item>
          <el-form-item label="快捷操作">
            <el-button size="small" @click="insertText(' && ')">插入 &&</el-button>
            <el-button size="small" @click="insertText(' || ')">插入 ||</el-button>
            <el-button size="small" @click="insertText('(')">插入 (</el-button>
            <el-button size="small" @click="insertText(')')">插入 )</el-button>
            <el-button size="small" @click="insertText(' == ')">插入 ==</el-button>
            <el-button size="small" @click="insertText(' != ')">插入 !=</el-button>
          </el-form-item>
        </template>
      </el-form>
      <div slot="footer">
        <el-button @click="edgeConfigVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdgeConfig">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getEmployeeList, getPositionList, getDeptList } from '@/api/user'
import { getTableFields } from '@/api/print'

export default {
  name: 'ProcessDesigner',
  props: {
    value: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      nodes: [],
      edges: [],
      selectedNode: null,
      draggingNode: null,
      dragOffset: { x: 0, y: 0 },
      mouseDownPosition: { x: 0, y: 0 },
      connectingFrom: null,
      longPressTimer: null, // 长按计时器
      isLongPressing: false, // 是否正在长按
      mousePosition: { x: 0, y: 0 }, // 当前鼠标位置（用于临时连线）
      animationFrameId: null, // requestAnimationFrame ID
      connectingFromPort: null,
      tempEdge: null,
      nodeTypes: [
        { type: 'start', label: '开始节点', icon: 'el-icon-video-play' },
        { type: 'approve', label: '审批节点', icon: 'el-icon-user' },
        { type: 'exclusive', label: '排他节点', icon: 'el-icon-share' },
        { type: 'end', label: '结束节点', icon: 'el-icon-video-pause' }
      ],
      employeeList: [],
      positionOptions: [],
      deptOptions: [],
      edgeConfigVisible: false,
      currentEdge: {},
      targetNodeForConnection: null, // 长按拖拽时的目标节点
      conditionList: [], // 条件构建器列表
      fieldGroups: [], // 字段分组列表
      selectedField: '', // 选中的字段（用于插入表达式）
      expressionInputRef: null // 表达式输入框引用
    }
  },
  mounted() {
    this.loadData()
    this.loadFieldGroups()
    if (this.value) {
      this.loadFromJson(this.value)
    }
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
    handleDragStart(event, nodeType) {
      event.dataTransfer.effectAllowed = 'copy'
      event.dataTransfer.setData('nodeType', nodeType.type)
    },
    handleDrop(event) {
      const nodeType = event.dataTransfer.getData('nodeType')
      if (!nodeType) return

      const canvas = this.$refs.canvas
      const rect = canvas.getBoundingClientRect()
      const x = event.clientX - rect.left - 75 // 节点宽度的一半
      const y = event.clientY - rect.top - 40  // 节点高度的一半

      // 检查是否已有开始/结束节点
      if (nodeType === 'start' && this.nodes.find(n => n.type === 'start')) {
        this.$message.warning('流程只能有一个开始节点')
        return
      }
      if (nodeType === 'end' && this.nodes.find(n => n.type === 'end')) {
        this.$message.warning('流程只能有一个结束节点')
        return
      }

      const newNode = {
        id: 'node_' + Date.now(),
        type: nodeType,
        name: '',
        x: Math.max(0, x),
        y: Math.max(0, y),
        approvalType: 'SINGLE',
        assigneeType: 'user',
        assigneeId: null,
        assigneeName: '',
        positionCode: '',
        deptCode: '',
        allowAddsign: 0,
        needPrint: 0,
        description: ''
      }

      this.nodes.push(newNode)
      this.updateEdges()
      this.emitChange()
    },
    handleNodeClick(node) {
      // 点击节点时，选中节点，不移动
      // 只有在按住鼠标并移动时才拖拽
      if (!this.draggingNode) {
        this.selectedNode = node
        this.connectingFrom = null
      }
    },
    handleCanvasClick() {
      this.selectedNode = null
      if (this.connectingFrom) {
        this.connectingFrom = null
        this.tempEdge = null
      }
    },
    getTempEdgePath() {
      // 长按拖拽模式
      if (this.isLongPressing && this.draggingNode) {
        const fromNode = this.draggingNode
        const getNodeWidth = (node) => node.type === 'exclusive' ? 60 : (node.type === 'start' || node.type === 'end' ? 60 : 150)
        const getNodeHeight = (node) => node.type === 'exclusive' ? 60 : (node.type === 'start' || node.type === 'end' ? 60 : 80)
        
        const fromWidth = getNodeWidth(fromNode)
        const fromHeight = getNodeHeight(fromNode)
        const startX = fromNode.x + fromWidth / 2
        const startY = fromNode.y + fromHeight
        
        // 使用当前鼠标位置作为终点
        const canvas = this.$refs.canvas
        const rect = canvas.getBoundingClientRect()
        const endX = this.mousePosition.x - rect.left
        const endY = this.mousePosition.y - rect.top
        
        const dx = endX - startX
        const dy = endY - startY
        const cp1x = startX + dx * 0.5
        const cp1y = startY
        const cp2x = startX + dx * 0.5
        const cp2y = endY
        
        return `M ${startX} ${startY} C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${endX} ${endY}`
      }
      
      // 连接点模式
      if (!this.tempEdge || !this.connectingFrom) return ''
      const fromNode = this.tempEdge.from
      const getNodeWidth = (node) => node.type === 'exclusive' ? 60 : (node.type === 'start' || node.type === 'end' ? 60 : 150)
      const getNodeHeight = (node) => node.type === 'exclusive' ? 60 : (node.type === 'start' || node.type === 'end' ? 60 : 80)
      
      const fromWidth = getNodeWidth(fromNode)
      const fromHeight = getNodeHeight(fromNode)
      const fromX = fromNode.x + fromWidth / 2
      const fromY = fromNode.y + fromHeight / 2
      
      let startX = fromX, startY = fromY
      
      if (this.tempEdge.fromPort === 'top') {
        startX = fromNode.x + fromWidth / 2
        startY = fromNode.y
      } else if (this.tempEdge.fromPort === 'bottom') {
        startX = fromNode.x + fromWidth / 2
        startY = fromNode.y + fromHeight
      } else if (this.tempEdge.fromPort === 'left') {
        startX = fromNode.x
        startY = fromNode.y + fromHeight / 2
      } else if (this.tempEdge.fromPort === 'right') {
        startX = fromNode.x + fromWidth
        startY = fromNode.y + fromHeight / 2
      }
      
      // 使用鼠标位置作为终点
      const canvas = this.$refs.canvas
      const rect = canvas.getBoundingClientRect()
      const endX = this.mousePosition.x - rect.left
      const endY = this.mousePosition.y - rect.top
      
      const dx = endX - startX
      const dy = endY - startY
      const cp1x = startX + dx * 0.5
      const cp1y = startY
      const cp2x = startX + dx * 0.5
      const cp2y = endY
      
      return `M ${startX} ${startY} C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${endX} ${endY}`
    },
    getEdgeLabel(edge) {
      if (!edge.conditionType || edge.conditionType === 'none') {
        return ''
      }
      if (edge.conditionType === 'expression' && edge.conditionExpression) {
        // 显示简化后的表达式
        let label = edge.conditionExpression
        if (label.length > 30) {
          label = label.substring(0, 27) + '...'
        }
        return label
      }
      if (edge.conditionType === 'builder' && edge.conditionExpression) {
        let label = edge.conditionExpression
        if (label.length > 30) {
          label = label.substring(0, 27) + '...'
        }
        return label
      }
      return ''
    },
    getEdgeLabelPosition(edge) {
      const fromNode = this.nodes.find(n => n.id === edge.from)
      const toNode = this.nodes.find(n => n.id === edge.to)
      if (!fromNode || !toNode) return { x: 0, y: 0 }
      
      const getNodeWidth = (node) => {
        if (node.type === 'exclusive' || node.type === 'start' || node.type === 'end') return 60
        return 150
      }
      const getNodeHeight = (node) => {
        if (node.type === 'exclusive' || node.type === 'start' || node.type === 'end') return 60
        return 80
      }
      
      const fromX = fromNode.x + getNodeWidth(fromNode) / 2
      const fromY = fromNode.y + getNodeHeight(fromNode) / 2
      const toX = toNode.x + getNodeWidth(toNode) / 2
      const toY = toNode.y + getNodeHeight(toNode) / 2
      
      // 标签位置在连线中点
      return {
        x: (fromX + toX) / 2,
        y: (fromY + toY) / 2 - 5
      }
    },
    handleNodeMouseDown(event, node) {
      // 如果点击的是连接点，不处理
      if (event.target.classList.contains('node-connector')) {
        return
      }
      
      if (event.button === 0 && !this.connectingFrom) { // 左键且不在连接状态
        const nodeElement = event.currentTarget
        const rect = nodeElement.getBoundingClientRect()
        const clickX = event.clientX - rect.left
        const clickY = event.clientY - rect.top
        
        // 如果点击的是连接点区域，不拖拽
        if (clickX < 20 || clickX > rect.width - 20 || clickY < 20 || clickY > rect.height - 20) {
          return
        }
        
        this.mouseDownPosition = { x: event.clientX, y: event.clientY }
        this.mousePosition = { x: event.clientX, y: event.clientY }
        
        // 设置长按计时器（500ms后显示连线）
        this.longPressTimer = setTimeout(() => {
          if (this.draggingNode === node) {
            this.isLongPressing = true
            // 开始跟踪鼠标位置
            document.addEventListener('mousemove', this.handleLongPressMouseMove)
          }
        }, 500)
        
        // 设置拖拽节点和偏移量
        this.draggingNode = node
        this.dragOffset = {
          x: clickX,
          y: clickY
        }

        document.addEventListener('mousemove', this.handleMouseMove)
        document.addEventListener('mouseup', this.handleMouseUp)
        event.preventDefault()
        event.stopPropagation()
      }
    },
    handleLongPressMouseMove(event) {
      // 更新鼠标位置用于临时连线
      this.mousePosition = { x: event.clientX, y: event.clientY }
    },
    handleCanvasMouseMove(event) {
      // 更新鼠标位置（用于临时连线）
      this.mousePosition = { x: event.clientX, y: event.clientY }
      
      // 如果正在长按拖拽，检查是否悬停在节点上
      if (this.isLongPressing && this.draggingNode) {
        const canvas = this.$refs.canvas
        const rect = canvas.getBoundingClientRect()
        const mouseX = event.clientX - rect.left
        const mouseY = event.clientY - rect.top
        
        // 查找鼠标下的节点
        const targetNode = this.nodes.find(node => {
          if (node.id === this.draggingNode.id) return false
          const getNodeWidth = (n) => n.type === 'exclusive' ? 60 : (n.type === 'start' || n.type === 'end' ? 60 : 150)
          const getNodeHeight = (n) => n.type === 'exclusive' ? 60 : (n.type === 'start' || n.type === 'end' ? 60 : 80)
          const width = getNodeWidth(node)
          const height = getNodeHeight(node)
          return mouseX >= node.x && mouseX <= node.x + width &&
                 mouseY >= node.y && mouseY <= node.y + height
        })
        
        // 高亮目标节点（可选）
        if (targetNode) {
          this.targetNodeForConnection = targetNode
        } else {
          this.targetNodeForConnection = null
        }
      }
    },
    handleCanvasMouseUp(event) {
      // 如果正在长按拖拽，检查是否在节点上松手
      if (this.isLongPressing && this.draggingNode) {
        event.preventDefault()
        event.stopPropagation()
        
        const canvas = this.$refs.canvas
        const rect = canvas.getBoundingClientRect()
        const mouseX = event.clientX - rect.left
        const mouseY = event.clientY - rect.top
        
        // 查找鼠标下的节点
        const targetNode = this.nodes.find(node => {
          if (node.id === this.draggingNode.id) return false
          const getNodeWidth = (n) => {
            if (n.type === 'exclusive' || n.type === 'start' || n.type === 'end') return 60
            return 150
          }
          const getNodeHeight = (n) => {
            if (n.type === 'exclusive' || n.type === 'start' || n.type === 'end') return 60
            return 80
          }
          const width = getNodeWidth(node)
          const height = getNodeHeight(node)
          return mouseX >= node.x && mouseX <= node.x + width &&
                 mouseY >= node.y && mouseY <= node.y + height
        })
        
        if (targetNode) {
          // 连接到目标节点
          this.connectNodes(this.draggingNode, 'bottom', targetNode, 'top')
          this.$message.success('连线已创建，请点击连线设置条件')
        }
        
        // 清理长按状态
        this.isLongPressing = false
        this.targetNodeForConnection = null
        document.removeEventListener('mousemove', this.handleLongPressMouseMove)
        // 也要清理拖拽状态
        this.draggingNode = null
        this.mouseDownPosition = null
      }
    },
    handleConnectorMouseDown(event, node, port) {
      event.stopPropagation()
      if (this.connectingFrom && this.connectingFrom.node.id !== node.id) {
        // 完成连接
        this.connectNodes(this.connectingFrom.node, this.connectingFrom.port, node, port)
        this.connectingFrom = null
        this.tempEdge = null
      } else {
        // 开始连接
        this.connectingFrom = { node, port }
        this.tempEdge = { from: node, fromPort: port }
      }
    },
    handleConnectorClick(node, port) {
      // 点击连接点，开始或完成连接
      if (this.connectingFrom && this.connectingFrom.node.id !== node.id) {
        this.connectNodes(this.connectingFrom.node, this.connectingFrom.port, node, port)
        this.connectingFrom = null
        this.tempEdge = null
      } else if (!this.connectingFrom) {
        this.connectingFrom = { node, port }
        this.tempEdge = { from: node, fromPort: port }
      }
    },
    handleMouseMove(event) {
      if (!this.draggingNode) return
      
      // 更新鼠标位置（用于临时连线）
      this.mousePosition = { x: event.clientX, y: event.clientY }
      
      // 如果正在长按，不移动节点，只显示连线
      if (this.isLongPressing) {
        return
      }

      // 使用requestAnimationFrame优化性能
      if (this.animationFrameId) {
        cancelAnimationFrame(this.animationFrameId)
      }
      
      this.animationFrameId = requestAnimationFrame(() => {
        const canvas = this.$refs.canvas
        const rect = canvas.getBoundingClientRect()
        const newX = Math.max(0, event.clientX - rect.left - this.dragOffset.x)
        const newY = Math.max(0, event.clientY - rect.top - this.dragOffset.y)
        
        // 只有当鼠标真正移动了（距离超过阈值）时才更新节点位置
        const dx = Math.abs(newX - this.draggingNode.x)
        const dy = Math.abs(newY - this.draggingNode.y)
        if (dx > 2 || dy > 2) {
          // 如果长按计时器还在，说明用户开始移动了，取消长按
          if (this.longPressTimer) {
            clearTimeout(this.longPressTimer)
            this.longPressTimer = null
          }
          // 如果已经在长按状态，取消长按，恢复正常拖动
          if (this.isLongPressing) {
            this.isLongPressing = false
            this.targetNodeForConnection = null
            document.removeEventListener('mousemove', this.handleLongPressMouseMove)
          }
          
          this.draggingNode.x = newX
          this.draggingNode.y = newY
          this.updateEdges()
        }
      })
    },
    handleMouseUp(event) {
      // 清除长按计时器
      if (this.longPressTimer) {
        clearTimeout(this.longPressTimer)
        this.longPressTimer = null
      }
      
      // 如果正在长按，不处理拖拽
      if (this.isLongPressing) {
        // handleCanvasMouseUp会处理连接
        return
      }
      
      // 如果鼠标没有移动，说明是点击操作，不应该更新位置
      if (this.draggingNode && this.mouseDownPosition) {
        const dx = Math.abs(event.clientX - this.mouseDownPosition.x)
        const dy = Math.abs(event.clientY - this.mouseDownPosition.y)
        if (dx > 3 || dy > 3) {
          // 鼠标移动了，这是拖拽操作，更新位置
          this.emitChange()
        }
      }
      
      // 清理
      if (this.animationFrameId) {
        cancelAnimationFrame(this.animationFrameId)
        this.animationFrameId = null
      }
      
      this.draggingNode = null
      this.mouseDownPosition = null
      this.isLongPressing = false
      this.targetNodeForConnection = null
      document.removeEventListener('mousemove', this.handleMouseMove)
      document.removeEventListener('mouseup', this.handleMouseUp)
      document.removeEventListener('mousemove', this.handleLongPressMouseMove)
    },
    connectNodes(fromNode, fromPort, toNode, toPort) {
      // 检查是否已存在连接
      const exists = this.edges.find(e => 
        e.from === fromNode.id && e.to === toNode.id
      )
      if (exists) {
        this.$message.warning('节点之间已存在连接')
        return
      }

      this.edges.push({
        from: fromNode.id,
        fromPort: fromPort,
        to: toNode.id,
        toPort: toPort,
        conditionType: 'none',
        conditionVariable: '',
        conditionOperator: 'eq',
        conditionValue: '',
        conditionExpression: ''
      })
      this.updateEdges()
      this.emitChange()
    },
    handleEdgeClick(edge) {
      this.currentEdge = { ...edge }
      // 初始化条件列表
      if (this.currentEdge.conditionList && this.currentEdge.conditionList.length > 0) {
        this.conditionList = JSON.parse(JSON.stringify(this.currentEdge.conditionList))
      } else {
        this.conditionList = [{
          field: '',
          operator: '==',
          value: '',
          logic: '&&'
        }]
      }
      // 初始化条件类型
      if (!this.currentEdge.conditionType) {
        this.currentEdge.conditionType = 'none'
      }
      this.edgeConfigVisible = true
    },
    handleSaveEdgeConfig() {
      // 如果使用构建器，生成表达式
      if (this.currentEdge.conditionType === 'builder') {
        this.currentEdge.conditionExpression = this.generateExpressionFromBuilder()
        this.currentEdge.conditionList = JSON.parse(JSON.stringify(this.conditionList))
      }
      
      const index = this.edges.findIndex(e => e.from === this.currentEdge.from && e.to === this.currentEdge.to)
      if (index >= 0) {
        this.edges[index] = { ...this.currentEdge }
        this.updateEdges()
        this.emitChange()
      }
      this.edgeConfigVisible = false
    },
    loadFieldGroups() {
      // 加载可用字段（可以根据流程类型加载不同字段）
      // 这里先加载报账单相关字段，后续可以根据流程类型动态加载
      getTableFields('PAYOUT').then(response => {
        if (response.code === 200 && response.data) {
          // 按表分组
          const groups = {}
          response.data.forEach(field => {
            if (!groups[field.tableName]) {
              groups[field.tableName] = {
                tableName: field.tableName,
                tableLabel: field.tableLabel,
                fields: []
              }
            }
            groups[field.tableName].fields.push(field)
          })
          this.fieldGroups = Object.values(groups)
        }
      }).catch(() => {
        // 如果API不可用，使用默认字段
        this.fieldGroups = [
          {
            tableName: 'sys_user',
            tableLabel: '用户表',
            fields: [
              { fieldKey: 'sys_user.type', columnLabel: '用户类型', dataType: 'VARCHAR' },
              { fieldKey: 'sys_user.name', columnLabel: '用户姓名', dataType: 'VARCHAR' },
              { fieldKey: 'sys_user.account', columnLabel: '账号', dataType: 'VARCHAR' }
            ]
          },
          {
            tableName: 'ctrl_payout',
            tableLabel: '报账单',
            fields: [
              { fieldKey: 'ctrl_payout.amount', columnLabel: '金额', dataType: 'DECIMAL' },
              { fieldKey: 'ctrl_payout.status', columnLabel: '状态', dataType: 'VARCHAR' },
              { fieldKey: 'ctrl_payout.apply_user', columnLabel: '申请人', dataType: 'VARCHAR' }
            ]
          }
        ]
      })
    },
    addCondition() {
      this.conditionList.push({
        field: '',
        operator: '==',
        value: '',
        logic: '&&'
      })
    },
    removeCondition(index) {
      this.conditionList.splice(index, 1)
    },
    handleFieldChange(condition) {
      // 字段改变时可以做一些默认值设置
    },
    getValuePlaceholder(condition) {
      if (!condition.operator) return '请输入值'
      if (condition.operator === 'in') {
        return '多个值用逗号分隔，如：1,2,3'
      }
      if (condition.operator === 'contains' || condition.operator === 'not contains') {
        return '请输入要包含的文本'
      }
      return '请输入值'
    },
    generateExpressionFromBuilder() {
      if (!this.conditionList || this.conditionList.length === 0) {
        return ''
      }
      
      let expression = ''
      this.conditionList.forEach((condition, index) => {
        if (!condition.field || !condition.operator || condition.value === '') {
          return
        }
        
        let conditionStr = ''
        const fieldKey = condition.field
        const operator = condition.operator
        let value = condition.value
        
        // 处理值（字符串需要加引号，数字不需要）
        const isNumber = /^\d+(\.\d+)?$/.test(value.trim())
        if (operator === 'in') {
          // in操作符特殊处理
          const values = value.split(',').map(v => {
            const trimmed = v.trim()
            return /^\d+(\.\d+)?$/.test(trimmed) ? trimmed : `'${trimmed}'`
          }).join(',')
          conditionStr = `${fieldKey} in [${values}]`
        } else if (operator === 'contains' || operator === 'not contains') {
          conditionStr = `${fieldKey}.${operator === 'contains' ? 'contains' : 'not contains'}('${value}')`
        } else {
          // 字符串值加引号
          if (!isNumber) {
            value = `'${value}'`
          }
          conditionStr = `${fieldKey} ${operator} ${value}`
        }
        
        // 添加括号（如果有包装）
        if (this.currentEdge.conditionWrap && this.currentEdge.conditionWrap.trim()) {
          const wrap = this.currentEdge.conditionWrap.trim()
          if (wrap.includes('condition')) {
            conditionStr = wrap.replace('condition', conditionStr)
          } else {
            conditionStr = `(${conditionStr})`
          }
        }
        
        expression += conditionStr
        
        // 添加逻辑运算符
        if (index < this.conditionList.length - 1 && condition.logic) {
          expression += ` ${condition.logic} `
        }
      })
      
      return expression
    },
    insertFieldToExpression() {
      if (!this.selectedField) return
      const expr = this.currentEdge.conditionExpression || ''
      const textarea = this.$refs.expressionTextarea
      if (textarea && textarea.$refs && textarea.$refs.textarea) {
        const input = textarea.$refs.textarea
        const start = input.selectionStart || 0
        const end = input.selectionEnd || 0
        const before = expr.substring(0, start)
        const after = expr.substring(end)
        this.currentEdge.conditionExpression = before + this.selectedField + after
        this.$nextTick(() => {
          input.focus()
          input.setSelectionRange(start + this.selectedField.length, start + this.selectedField.length)
        })
      } else {
        this.currentEdge.conditionExpression = (expr || '') + this.selectedField
      }
      this.selectedField = ''
    },
    insertText(text) {
      const expr = this.currentEdge.conditionExpression || ''
      const textarea = this.$refs.expressionTextarea
      if (textarea && textarea.$refs && textarea.$refs.textarea) {
        const input = textarea.$refs.textarea
        const start = input.selectionStart || 0
        const end = input.selectionEnd || 0
        const before = expr.substring(0, start)
        const after = expr.substring(end)
        this.currentEdge.conditionExpression = before + text + after
        this.$nextTick(() => {
          input.focus()
          input.setSelectionRange(start + text.length, start + text.length)
        })
      } else {
        this.currentEdge.conditionExpression = (expr || '') + text
      }
    },
    updateEdges() {
      // 使用requestAnimationFrame优化性能
      if (this.animationFrameId) {
        return
      }
      this.animationFrameId = requestAnimationFrame(() => {
        // 强制更新视图
        this.$forceUpdate()
        this.animationFrameId = null
      })
    },
    getEdgePath(edge) {
      const fromNode = this.nodes.find(n => n.id === edge.from)
      const toNode = this.nodes.find(n => n.id === edge.to)
      if (!fromNode || !toNode) return ''

      // 节点尺寸（开始、结束、排他节点是60x60，其他是150x80）
      const getNodeWidth = (node) => {
        if (node.type === 'exclusive' || node.type === 'start' || node.type === 'end') {
          return 60
        }
        return 150
      }
      const getNodeHeight = (node) => {
        if (node.type === 'exclusive' || node.type === 'start' || node.type === 'end') {
          return 60
        }
        return 80
      }

      const fromWidth = getNodeWidth(fromNode)
      const fromHeight = getNodeHeight(fromNode)
      const toWidth = getNodeWidth(toNode)
      const toHeight = getNodeHeight(toNode)

      const fromX = fromNode.x + fromWidth / 2 // 节点中心
      const fromY = fromNode.y + fromHeight / 2
      const toX = toNode.x + toWidth / 2
      const toY = toNode.y + toHeight / 2

      // 根据端口计算起点和终点（精确对应连接点位置）
      let startX = fromX, startY = fromY, endX = toX, endY = toY

      if (edge.fromPort === 'top') {
        startX = fromNode.x + fromWidth / 2
        startY = fromNode.y
      } else if (edge.fromPort === 'bottom') {
        startX = fromNode.x + fromWidth / 2
        startY = fromNode.y + fromHeight
      } else if (edge.fromPort === 'left') {
        startX = fromNode.x
        startY = fromNode.y + fromHeight / 2
      } else if (edge.fromPort === 'right') {
        startX = fromNode.x + fromWidth
        startY = fromNode.y + fromHeight / 2
      }

      if (edge.toPort === 'top') {
        endX = toNode.x + toWidth / 2
        endY = toNode.y
      } else if (edge.toPort === 'bottom') {
        endX = toNode.x + toWidth / 2
        endY = toNode.y + toHeight
      } else if (edge.toPort === 'left') {
        endX = toNode.x
        endY = toNode.y + toHeight / 2
      } else if (edge.toPort === 'right') {
        endX = toNode.x + toWidth
        endY = toNode.y + toHeight / 2
      }

      // 计算控制点（改进的贝塞尔曲线，使连线更平滑）
      const dx = endX - startX
      const dy = endY - startY
      
      // 根据连线的方向决定控制点的位置
      let cp1x, cp1y, cp2x, cp2y
      
      // 水平连线为主
      if (Math.abs(dx) > Math.abs(dy)) {
        const offset = Math.abs(dx) * 0.5
        cp1x = startX + (dx > 0 ? offset : -offset)
        cp1y = startY
        cp2x = endX + (dx > 0 ? -offset : offset)
        cp2y = endY
      } else {
        // 垂直连线为主
        const offset = Math.abs(dy) * 0.5
        cp1x = startX
        cp1y = startY + (dy > 0 ? offset : -offset)
        cp2x = endX
        cp2y = endY + (dy > 0 ? -offset : offset)
      }

      return `M ${startX} ${startY} C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${endX} ${endY}`
    },
    getNodeIcon(type) {
      const iconMap = {
        'start': 'el-icon-video-play',
        'approve': 'el-icon-user',
        'exclusive': 'el-icon-close', // 改为X图标
        'end': 'el-icon-video-pause'
      }
      return iconMap[type] || 'el-icon-question'
    },
    getNodeDefaultName(type) {
      const nameMap = {
        'start': '开始',
        'approve': '审批节点',
        'exclusive': '排他',
        'end': '结束'
      }
      return nameMap[type] || type
    },
    getNodeInfo(node) {
      if (node.type === 'approve') {
        if (node.assigneeName) {
          return node.assigneeName
        } else if (node.positionCode) {
          const pos = this.positionOptions.find(p => p.positionCode === node.positionCode)
          return pos ? pos.positionName : ''
        } else if (node.deptCode) {
          const dept = this.deptOptions.find(d => d.deptCode === node.deptCode)
          return dept ? dept.deptName + '负责人' : ''
        }
      }
      return node.description || ''
    },
    handleDeleteNode() {
      if (!this.selectedNode) return

      this.$confirm('确定要删除该节点吗？删除节点会同时删除相关的连线。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 删除相关的边
        this.edges = this.edges.filter(e => 
          e.from !== this.selectedNode.id && e.to !== this.selectedNode.id
        )
        // 删除节点
        this.nodes = this.nodes.filter(n => n.id !== this.selectedNode.id)
        this.selectedNode = null
        this.updateEdges()
        this.emitChange()
      }).catch(() => {})
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
      this.emitChange()
    },
    loadFromJson(jsonStr) {
      try {
        const data = JSON.parse(jsonStr)
        this.nodes = data.nodes || []
        this.edges = data.edges || []
        this.updateEdges()
      } catch (e) {
        console.error('解析流程JSON失败:', e)
      }
    },
    generateJson() {
      return JSON.stringify({
        nodes: this.nodes,
        edges: this.edges,
        version: '1.0'
      }, null, 2)
    },
    emitChange() {
      this.$emit('input', this.generateJson())
      this.$emit('change', this.generateJson())
    }
  }
}
</script>

<style scoped>
.process-designer-container {
  display: flex;
  height: 100%;
  border: 1px solid #dcdfe6;
  background: #fff;
}

.designer-toolbar {
  width: 200px;
  border-right: 1px solid #dcdfe6;
  background: #f5f7fa;
  padding: 15px;
  overflow-y: auto;
}

.toolbar-title {
  font-weight: bold;
  margin-bottom: 15px;
  color: #303133;
}

.toolbar-node-item {
  padding: 12px;
  margin-bottom: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  cursor: move;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s;
}

.toolbar-node-item:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.2);
}

.toolbar-node-item i {
  font-size: 18px;
  color: #409EFF;
}

.designer-canvas-container {
  flex: 1;
  position: relative;
  overflow: auto;
  background: #fafafa;
}

.designer-canvas {
  position: relative;
  width: 100%;
  height: 100%;
  min-width: 2000px;
  min-height: 1500px;
}

.grid-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.connections-svg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.connection-line {
  pointer-events: stroke;
  cursor: pointer;
}

.connection-line:hover {
  stroke: #67C23A;
  stroke-width: 3;
}

.flow-node {
  position: absolute;
  width: 150px;
  min-height: 80px;
  border: 2px solid #dcdfe6;
  border-radius: 8px;
  background: #fff;
  cursor: move;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.flow-node:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.flow-node.node-selected {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.node-start {
  border-color: #67C23A;
  background: #f0f9ff;
  width: 60px;
  height: 60px;
  border-radius: 50%;
}

.node-start .node-header {
  padding: 0;
  background: transparent;
  border: none;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.node-start .node-content {
  display: none;
}

.node-end {
  border-color: #F56C6C;
  background: #fef0f0;
  width: 60px;
  height: 60px;
  border-radius: 50%;
}

.node-end .node-header {
  padding: 0;
  background: transparent;
  border: none;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.node-end .node-content {
  display: none;
}

.node-approve {
  border-color: #409EFF;
}

.node-parallel {
  border-color: #E6A23C;
}

.node-exclusive {
  border-color: #909399;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #fff;
}

.node-exclusive .node-header {
  padding: 0;
  background: transparent;
  border: none;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.exclusive-x {
  position: relative;
  width: 40px;
  height: 40px;
}

.x-line {
  position: absolute;
  width: 2px;
  height: 30px;
  background: #909399;
  top: 5px;
  left: 19px;
}

.x-line-1 {
  transform: rotate(45deg);
}

.x-line-2 {
  transform: rotate(-45deg);
}

.node-header {
  padding: 8px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 6px 6px 0 0;
}

.node-header i {
  font-size: 16px;
  color: #409EFF;
}

.node-title {
  font-weight: 600;
  font-size: 13px;
  color: #303133;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-content {
  padding: 8px 12px;
  font-size: 12px;
  color: #606266;
}

.node-info {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-connector {
  position: absolute;
  width: 12px;
  height: 12px;
  background: #409EFF;
  border: 2px solid #fff;
  border-radius: 50%;
  cursor: crosshair;
  z-index: 10;
}

.node-connector-top {
  top: -6px;
  left: 50%;
  transform: translateX(-50%);
}

.node-connector-bottom {
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
}

.node-connector-left {
  left: -6px;
  top: 50%;
  transform: translateY(-50%);
}

.node-connector-right {
  right: -6px;
  top: 50%;
  transform: translateY(-50%);
}

.node-connector:hover {
  background: #67C23A;
  transform: scale(1.5);
  z-index: 20;
}

.node-target {
  border-color: #67C23A !important;
  box-shadow: 0 0 10px rgba(103, 194, 58, 0.5) !important;
}

.temp-connection-line {
  pointer-events: none;
}

.condition-builder {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  background: #f5f7fa;
}

.condition-item {
  margin-bottom: 10px;
}

.condition-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.edge-label {
  font-size: 11px;
  fill: #409EFF;
  font-weight: bold;
  cursor: pointer;
  pointer-events: all;
}

.edge-label:hover {
  fill: #67C23A;
  text-decoration: underline;
}

.canvas-empty {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: #909399;
}

.canvas-empty i {
  font-size: 48px;
  margin-bottom: 10px;
  display: block;
}

.designer-properties {
  width: 320px;
  border-left: 1px solid #dcdfe6;
  background: #fff;
  display: flex;
  flex-direction: column;
}

.properties-header {
  padding: 15px;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.properties-content {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
}
</style>

