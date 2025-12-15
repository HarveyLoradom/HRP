<template>
  <div class="reportbro-designer">
    <!-- 顶部工具栏 -->
    <div class="designer-toolbar">
      <el-button-group>
        <el-button size="small" icon="el-icon-back" @click="handleUndo" :disabled="!canUndo">撤销</el-button>
        <el-button size="small" icon="el-icon-right" @click="handleRedo" :disabled="!canRedo">重做</el-button>
      </el-button-group>
      <el-divider direction="vertical"></el-divider>
      <el-button size="small" icon="el-icon-document" @click="handleAddText">添加文本</el-button>
      <el-button size="small" icon="el-icon-edit-outline" @click="handleAddTitle">添加标题</el-button>
      <el-button size="small" icon="el-icon-sort" @click="handleAddLabelField">标签字段</el-button>
      <el-button size="small" icon="el-icon-grid" @click="handleAddTable">添加表格</el-button>
      <el-divider direction="vertical"></el-divider>
      <el-button size="small" icon="el-icon-view" @click="handlePreview">预览</el-button>
      <el-button size="small" type="primary" icon="el-icon-check" @click="handleSave">保存</el-button>
    </div>

    <div class="designer-container">
      <!-- 左侧组件库 -->
      <div class="designer-sidebar-left">
        <div class="sidebar-section">
          <h4>组件</h4>
          <div class="component-list">
            <div class="component-item" @click="handleAddText">
              <i class="el-icon-document"></i>
              <span>文本</span>
            </div>
            <div class="component-item" @click="handleAddTitle">
              <i class="el-icon-edit-outline"></i>
              <span>标题</span>
            </div>
            <div class="component-item" @click="handleAddLabelField">
              <i class="el-icon-sort"></i>
              <span>标签字段</span>
            </div>
            <div class="component-item" @click="handleAddTable">
              <i class="el-icon-grid"></i>
              <span>表格</span>
            </div>
          </div>
          <el-divider></el-divider>
          <h4>数据源字段</h4>
          <div v-if="fieldGroups.length === 0" class="empty-tip">
            <p>请在右侧"数据源"面板中选择表并添加字段</p>
          </div>
          <div v-else class="field-list">
            <div 
              v-for="group in filteredFieldGroups" 
              :key="group.tableName"
              class="field-group"
            >
              <div class="field-group-title">{{ group.tableLabel }}</div>
              <div 
                v-for="field in group.fields"
                :key="field.fieldKey"
                class="field-item"
                draggable="true"
                @dragstart="handleFieldDragStart($event, field)"
              >
                <i class="el-icon-tickets"></i>
                <span>{{ field.columnLabel }}</span>
                <span class="field-key">({{ field.fieldKey }})</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间画布区域 -->
      <div class="designer-canvas-container">
        <div class="canvas-wrapper" ref="canvasWrapper">
          <div 
            class="designer-canvas"
            :style="canvasStyle"
            @drop="handleCanvasDrop"
            @dragover.prevent
            @click="handleCanvasClick"
          >
            <!-- 网格线 -->
            <div v-if="showGrid" class="grid-overlay" :style="gridStyle" @click.stop="handleCanvasClick"></div>
            
            <!-- 画布内容 -->
            <div class="canvas-content" @click.stop="handleCanvasClick">
              <component
                v-for="(element, index) in elements"
                :key="element.id"
                :is="getElementComponent(element.type)"
                :element="element"
                :index="index"
                :selected="selectedElementId === element.id"
                :column-count="pageDesign.columnCount"
                @select="handleElementSelect(element.id)"
                @update="handleElementUpdate(element.id, $event)"
                @delete="handleElementDelete(element.id)"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧面板 -->
      <div class="designer-sidebar-right">
        <el-tabs v-model="activeRightTab" type="border-card">
          <!-- 页面设计 -->
          <el-tab-pane label="页面设计" name="page">
            <div class="panel-content">
              <el-form label-width="85px" size="small">
                <el-form-item label="纸张大小">
                  <el-select v-model="pageDesign.pageSize" @change="handlePageSizeChange">
                    <el-option label="A4" value="A4"></el-option>
                    <el-option label="A3" value="A3"></el-option>
                    <el-option label="Letter" value="Letter"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="方向">
                  <el-radio-group v-model="pageDesign.orientation" @change="handleOrientationChange">
                    <el-radio label="portrait">纵向</el-radio>
                    <el-radio label="landscape">横向</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="列数">
                  <el-input-number 
                    v-model="pageDesign.columnCount" 
                    :min="1" 
                    :max="6"
                    @change="handleColumnCountChange"
                  ></el-input-number>
                </el-form-item>
                <el-form-item label="上边距">
                  <el-input-number v-model="pageDesign.marginTop" :min="0" :max="100"></el-input-number>
                </el-form-item>
                <el-form-item label="下边距">
                  <el-input-number v-model="pageDesign.marginBottom" :min="0" :max="100"></el-input-number>
                </el-form-item>
                <el-form-item label="左边距">
                  <el-input-number v-model="pageDesign.marginLeft" :min="0" :max="100"></el-input-number>
                </el-form-item>
                <el-form-item label="右边距">
                  <el-input-number v-model="pageDesign.marginRight" :min="0" :max="100"></el-input-number>
                </el-form-item>
                <el-form-item label="显示网格">
                  <el-switch v-model="showGrid"></el-switch>
                </el-form-item>
                <el-form-item label="对齐吸附">
                  <el-switch v-model="snapToGrid"></el-switch>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- 属性 -->
          <el-tab-pane label="属性" name="properties">
            <div class="panel-content" v-if="selectedElement">
              <element-properties
                :element="selectedElement"
                :column-count="pageDesign.columnCount"
                :field-groups="fieldGroups"
                @update="handleElementUpdate(selectedElement.id, $event)"
              />
            </div>
            <div v-else class="panel-content empty-state">
              <p>请选择一个元素</p>
            </div>
          </el-tab-pane>

          <!-- 数据源 -->
          <el-tab-pane label="数据源" name="datasource">
            <div class="panel-content">
              <el-input
                v-model="tableSearchText"
                size="small"
                placeholder="搜索表名"
                prefix-icon="el-icon-search"
                style="margin-bottom: 10px;"
                @input="handleTableSearch"
              ></el-input>
              <div class="table-list">
                <div 
                  v-for="table in filteredTables"
                  :key="table.tableName"
                  class="table-item"
                  @click="handleTableSelect(table)"
                >
                  <i class="el-icon-document"></i>
                  <span>{{ table.tableLabel || table.tableName }}</span>
                </div>
              </div>
              <el-divider></el-divider>
              <div v-if="selectedTable" class="selected-table-fields">
                <h5>{{ selectedTable.tableLabel || selectedTable.tableName }} 的字段</h5>
                <div class="field-list">
                  <div 
                    v-for="field in selectedTableFields"
                    :key="field.fieldKey"
                    class="field-item"
                  >
                    <el-checkbox 
                      v-model="field.selected"
                      @change="handleFieldToggle(field)"
                    >
                      {{ field.columnLabel }} ({{ field.fieldKey }})
                    </el-checkbox>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 预览对话框 -->
    <el-dialog
      title="打印预览"
      :visible.sync="previewVisible"
      width="90%"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      :show-close="true"
      :modal="false"
      class="preview-dialog"
      @close="handlePreviewClose"
    >
      <div class="preview-container" ref="previewContainer" id="print-content">
        <div v-html="previewContent" class="print-content"></div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handlePreviewClose">关闭</el-button>
        <el-button type="primary" @click="handlePrint">打印</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { getAllTableNames, getTableFieldsByTableName, getTableFields, previewPrintTemplate } from '@/api/print'
import ElementProperties from './ReportBroElementProperties.vue'
import TextElement from './ReportBroElements/TextElement.vue'
import TitleElement from './ReportBroElements/TitleElement.vue'
import LabelFieldElement from './ReportBroElements/LabelFieldElement.vue'
import TableElement from './ReportBroElements/TableElement.vue'

export default {
  name: 'ReportBroDesigner',
  components: {
    ElementProperties,
    TextElement,
    TitleElement,
    LabelFieldElement,
    TableElement
  },
  props: {
    templateType: {
      type: String,
      default: ''
    },
    templateId: {
      type: [Number, String],
      default: null
    },
    templateJson: {
      type: String,
      default: ''
    },
    pageSize: {
      type: String,
      default: 'A4'
    },
    orientation: {
      type: String,
      default: 'portrait'
    }
  },
  data() {
    return {
      // 历史记录
      history: [],
      historyIndex: -1,
      
      // 元素列表
      elements: [],
      selectedElementId: null,
      
      // 页面设计
      pageDesign: {
        pageSize: this.pageSize || 'A4',
        orientation: this.orientation || 'portrait',
        columnCount: 4,
        marginTop: 20,
        marginBottom: 20,
        marginLeft: 20,
        marginRight: 20
      },
      
      // 画布设置
      showGrid: true,
      snapToGrid: true,
      gridSize: 10,
      
      // 右侧面板
      activeRightTab: 'page',
      
      // 字段组
      fieldGroups: [],
      fieldSearchText: '',
      
      // 数据源
      allTables: [],
      tableSearchText: '',
      selectedTable: null,
      selectedTableFields: [],
      
      // 预览
      previewVisible: false,
      previewContent: '',
      
      
      // 元素ID计数器
      elementIdCounter: 1
    }
  },
  computed: {
    canUndo() {
      return this.historyIndex > 0
    },
    canRedo() {
      return this.historyIndex < this.history.length - 1
    },
    selectedElement() {
      return this.elements.find(el => el.id === this.selectedElementId)
    },
    canvasStyle() {
      const pageSizes = {
        A4: { width: 210, height: 297 },
        A3: { width: 297, height: 420 },
        Letter: { width: 216, height: 279 }
      }
      const size = pageSizes[this.pageDesign.pageSize] || pageSizes.A4
      const isLandscape = this.pageDesign.orientation === 'landscape'
      const width = isLandscape ? size.height : size.width
      const height = isLandscape ? size.width : size.height
      
      return {
        width: `${width}mm`,
        minHeight: `${height}mm`,
        marginTop: `${this.pageDesign.marginTop}mm`,
        marginBottom: `${this.pageDesign.marginBottom}mm`,
        marginLeft: `${this.pageDesign.marginLeft}mm`,
        marginRight: `${this.pageDesign.marginRight}mm`,
        backgroundColor: '#fff',
        boxShadow: '0 0 10px rgba(0,0,0,0.1)',
        position: 'relative'
      }
    },
    gridStyle() {
      return {
        backgroundImage: `linear-gradient(to right, #e0e0e0 1px, transparent 1px),
                         linear-gradient(to bottom, #e0e0e0 1px, transparent 1px)`,
        backgroundSize: `${this.gridSize}px ${this.gridSize}px`,
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        pointerEvents: 'none',
        zIndex: 1
      }
    },
    filteredFieldGroups() {
      if (!this.fieldSearchText) {
        return this.fieldGroups
      }
      const search = this.fieldSearchText.toLowerCase()
      return this.fieldGroups.map(group => ({
        ...group,
        fields: group.fields.filter(field => 
          field.columnLabel.toLowerCase().includes(search) ||
          field.fieldKey.toLowerCase().includes(search)
        )
      })).filter(group => group.fields.length > 0)
    },
    filteredTables() {
      if (!this.tableSearchText) {
        return this.allTables.map(table => ({
          tableName: typeof table === 'string' ? table : table.tableName,
          tableLabel: typeof table === 'string' ? table : (table.tableLabel || table.tableName)
        }))
      }
      const search = this.tableSearchText.toLowerCase()
      return this.allTables
        .map(table => ({
          tableName: typeof table === 'string' ? table : table.tableName,
          tableLabel: typeof table === 'string' ? table : (table.tableLabel || table.tableName)
        }))
        .filter(table => 
          table.tableLabel.toLowerCase().includes(search) ||
          table.tableName.toLowerCase().includes(search)
        )
    }
  },
  mounted() {
    this.loadTemplate()
    // 不再预先加载字段组，只在数据源中添加后才显示
    // this.loadFieldGroups()
    this.loadTables()
    this.saveHistory()
  },
  methods: {
    // 历史记录
    saveHistory() {
      const state = JSON.parse(JSON.stringify({
        elements: this.elements,
        pageDesign: this.pageDesign
      }))
      this.history = this.history.slice(0, this.historyIndex + 1)
      this.history.push(state)
      this.historyIndex = this.history.length - 1
      if (this.history.length > 50) {
        this.history.shift()
        this.historyIndex--
      }
    },
    handleUndo() {
      if (this.canUndo) {
        this.historyIndex--
        this.restoreState(this.history[this.historyIndex])
      }
    },
    handleRedo() {
      if (this.canRedo) {
        this.historyIndex++
        this.restoreState(this.history[this.historyIndex])
      }
    },
    restoreState(state) {
      this.elements = JSON.parse(JSON.stringify(state.elements))
      this.pageDesign = JSON.parse(JSON.stringify(state.pageDesign))
    },
    
    // 加载模板
    loadTemplate() {
      if (this.templateJson) {
        try {
          const data = typeof this.templateJson === 'string' ? JSON.parse(this.templateJson) : this.templateJson
          if (data.elements) {
            this.elements = data.elements
            this.elementIdCounter = Math.max(...this.elements.map(el => el.id || 0), 0) + 1
          }
          if (data.pageDesign) {
            this.pageDesign = { ...this.pageDesign, ...data.pageDesign }
          }
        } catch (e) {
          console.error('加载模板失败:', e)
        }
      }
    },
    
    // 加载字段组
    async loadFieldGroups() {
      if (this.templateType) {
        try {
          const response = await getTableFields(this.templateType)
          if (response.code === 200 && response.data) {
            // 将字段列表按表名分组
            const grouped = {}
            response.data.forEach(field => {
              const tableName = field.tableName || 'default'
              if (!grouped[tableName]) {
                grouped[tableName] = {
                  tableName: tableName,
                  tableLabel: field.tableLabel || tableName,
                  fields: []
                }
              }
              grouped[tableName].fields.push(field)
            })
            this.fieldGroups = Object.values(grouped)
          }
        } catch (e) {
          console.error('加载字段失败:', e)
          this.$message.error('加载字段失败: ' + (e.message || '未知错误'))
        }
      }
    },
    
    // 加载表列表
    async loadTables() {
      try {
        const response = await getAllTableNames()
        if (response.code === 200 && response.data) {
          this.allTables = response.data
        }
      } catch (e) {
        console.error('加载表列表失败:', e)
      }
    },
    
    // 表搜索
    handleTableSearch() {
      // 搜索逻辑已在 computed 中实现
    },
    
    // 选择表
    async handleTableSelect(table) {
      const tableName = typeof table === 'string' ? table : table.tableName
      this.selectedTable = { tableName, tableLabel: typeof table === 'string' ? table : (table.tableLabel || table.tableName) }
      try {
        const response = await getTableFieldsByTableName(tableName)
        if (response.code === 200 && response.data) {
          this.selectedTableFields = response.data.map(field => ({
            ...field,
            selected: this.isFieldInDataSource(field)
          }))
        } else {
          this.$message.error(response.message || '加载表字段失败')
        }
      } catch (e) {
        console.error('加载表字段失败:', e)
        this.$message.error('加载表字段失败: ' + (e.message || '未知错误'))
      }
    },
    
    // 检查字段是否在数据源中
    isFieldInDataSource(field) {
      return this.fieldGroups.some(group => 
        group.fields.some(f => f.fieldKey === field.fieldKey)
      )
    },
    
    // 切换字段
    handleFieldToggle(field) {
      if (field.selected) {
        // 添加到数据源
        this.addFieldToDataSource(field)
      } else {
        // 从数据源移除
        this.removeFieldFromDataSource(field)
      }
    },
    
    // 添加字段到数据源
    addFieldToDataSource(field) {
      if (!this.selectedTable || !this.selectedTable.tableName) {
        this.$message.warning('请先选择表')
        return
      }
      const group = this.fieldGroups.find(g => g.tableName === this.selectedTable.tableName)
      if (group) {
        if (!group.fields.find(f => f.fieldKey === field.fieldKey)) {
          group.fields.push(field)
          this.$message.success('字段已添加到数据源')
        } else {
          this.$message.warning('字段已在数据源中')
        }
      } else {
        this.fieldGroups.push({
          tableName: this.selectedTable.tableName,
          tableLabel: this.selectedTable.tableLabel || this.selectedTable.tableName,
          fields: [field]
        })
        this.$message.success('字段已添加到数据源')
      }
    },
    
    // 从数据源移除字段
    removeFieldFromDataSource(field) {
      if (!this.selectedTable || !this.selectedTable.tableName) {
        this.$message.warning('请先选择表')
        return
      }
      const group = this.fieldGroups.find(g => g.tableName === this.selectedTable.tableName)
      if (group) {
        const index = group.fields.findIndex(f => f.fieldKey === field.fieldKey)
        if (index > -1) {
          group.fields.splice(index, 1)
          this.$message.success('字段已从数据源移除')
        }
        if (group.fields.length === 0) {
          const groupIndex = this.fieldGroups.findIndex(g => g.tableName === this.selectedTable.tableName)
          if (groupIndex > -1) {
            this.fieldGroups.splice(groupIndex, 1)
          }
        }
      }
    },
    
    // 元素操作
    getElementComponent(type) {
      const components = {
        text: 'TextElement',
        title: 'TitleElement',
        'label-field': 'LabelFieldElement',
        table: 'TableElement'
      }
      return components[type] || 'TextElement'
    },
    
    handleElementSelect(id) {
      this.selectedElementId = id
      this.activeRightTab = 'properties'
    },
    
    handleElementUpdate(id, updates) {
      const index = this.elements.findIndex(el => el.id === id)
      if (index > -1) {
        // 确保位置信息被正确保存，不会丢失x, y坐标
        const currentElement = this.elements[index]
        this.elements[index] = { 
          ...currentElement, 
          ...updates,
          // 如果updates中没有x或y，保留原有的值
          x: updates.x !== undefined ? updates.x : currentElement.x,
          y: updates.y !== undefined ? updates.y : currentElement.y
        }
        this.saveHistory()
      }
    },
    
    
    handleElementDelete(id) {
      const index = this.elements.findIndex(el => el.id === id)
      if (index > -1) {
        this.elements.splice(index, 1)
        if (this.selectedElementId === id) {
          this.selectedElementId = null
        }
        this.saveHistory()
        this.$message.success('已删除')
      }
    },
    
    // 键盘事件处理
    handleKeyDown(e) {
      // Delete 或 Backspace 键删除选中的元素
      if ((e.key === 'Delete' || e.key === 'Backspace') && this.selectedElementId) {
        e.preventDefault()
        this.handleElementDelete(this.selectedElementId)
      }
      // Ctrl+Z 撤销
      if (e.ctrlKey && e.key === 'z' && !e.shiftKey) {
        e.preventDefault()
        this.handleUndo()
      }
      // Ctrl+Shift+Z 或 Ctrl+Y 重做
      if ((e.ctrlKey && e.shiftKey && e.key === 'z') || (e.ctrlKey && e.key === 'y')) {
        e.preventDefault()
        this.handleRedo()
      }
    },
    
    handleCanvasClick(e) {
      // 如果点击的是画布本身或canvas-content，取消选择
      if (e.target === e.currentTarget || 
          e.target.classList.contains('designer-canvas') ||
          e.target.classList.contains('canvas-content') ||
          e.target.classList.contains('grid-overlay')) {
        this.selectedElementId = null
      }
    },
    
    // 添加元素
    handleAddText() {
      const element = {
        id: this.elementIdCounter++,
        type: 'text',
        content: '文本内容',
        x: 10,
        y: 10,
        width: 100,
        fontSize: 12,
        fontWeight: 'normal',
        textAlign: 'left',
        color: '#000000',
        lineHeight: 1.5,
        columnSpan: 1
      }
      this.elements.push(element)
      this.selectedElementId = element.id
      this.saveHistory()
    },
    
    handleAddTitle() {
      const element = {
        id: this.elementIdCounter++,
        type: 'title',
        content: '标题',
        x: 10,
        y: 10,
        width: 100,
        fontSize: 18,
        fontWeight: 'bold',
        textAlign: 'center',
        columnSpan: this.pageDesign.columnCount
      }
      this.elements.push(element)
      this.selectedElementId = element.id
      this.saveHistory()
    },
    
    handleAddLabelField() {
      const element = {
        id: this.elementIdCounter++,
        type: 'label-field',
        labelText: '标签',
        fieldKey: '',
        x: 10,
        y: 10,
        width: 100,
        fontSize: 12,
        fontWeight: 'normal',
        labelColor: '#000000',
        fieldAlign: 'left',
        fieldColor: '#333',
        fieldFontWeight: 'normal',
        labelWidth: 30,
        columnSpan: 1
      }
      this.elements.push(element)
      this.selectedElementId = element.id
      this.saveHistory()
    },
    
    handleAddTable() {
      const element = {
        id: this.elementIdCounter++,
        type: 'table',
        x: 10,
        y: 10,
        width: 100,
        columns: [
          { fieldKey: '', label: '列1', width: 50, fontSize: 12, fontWeight: 'normal', color: '#000000', textAlign: 'left', headerFontSize: 12, headerFontWeight: 'bold', headerColor: '#000000', headerTextAlign: 'center' },
          { fieldKey: '', label: '列2', width: 50, fontSize: 12, fontWeight: 'normal', color: '#000000', textAlign: 'left', headerFontSize: 12, headerFontWeight: 'bold', headerColor: '#000000', headerTextAlign: 'center' }
        ],
        showHeader: true,
        showFooter: false,
        headerRows: 1,
        footerRows: 1,
        columnSpan: this.pageDesign.columnCount
      }
      this.elements.push(element)
      this.selectedElementId = element.id
      this.saveHistory()
    },
    
    // 字段拖拽
    handleFieldDragStart(e, field) {
      e.dataTransfer.setData('application/json', JSON.stringify({
        type: 'field',
        field: field
      }))
    },
    
    handleCanvasDrop(e) {
      e.preventDefault()
      try {
        const data = JSON.parse(e.dataTransfer.getData('application/json'))
        if (data.type === 'field') {
          // 在拖放位置添加字段
          const rect = this.$refs.canvasWrapper.getBoundingClientRect()
          const x = e.clientX - rect.left
          const y = e.clientY - rect.top
          
          const element = {
            id: this.elementIdCounter++,
            type: 'text',
            content: `{{${data.field.fieldKey}}}`,
            fieldKey: data.field.fieldKey,
            x: x - 10,
            y: y - 10,
            width: 100,
            fontSize: 12,
            fontWeight: 'normal',
            textAlign: 'left',
            columnSpan: 1
          }
          this.elements.push(element)
          this.selectedElementId = element.id
          this.saveHistory()
        }
      } catch (err) {
        console.error('拖放处理失败:', err)
      }
    },
    
    // 页面设计变更
    handlePageSizeChange() {
      this.saveHistory()
    },
    
    handleOrientationChange() {
      this.saveHistory()
    },
    
    handleColumnCountChange() {
      // 更新所有元素的列跨度
      this.elements.forEach(el => {
        if (el.columnSpan && el.columnSpan > this.pageDesign.columnCount) {
          el.columnSpan = this.pageDesign.columnCount
        }
      })
      this.saveHistory()
    },
    
    // 预览
    async handlePreview() {
      try {
        // 生成HTML预览内容
        const html = this.generatePreviewHtml()
        this.previewContent = html
        this.previewVisible = true
      } catch (e) {
        console.error('预览失败:', e)
        this.$message.error('预览失败')
        this.previewVisible = false
      }
    },
    
    // 关闭预览
    handlePreviewClose() {
      this.previewVisible = false
      this.previewContent = ''
    },
    
    // 生成预览HTML
    generatePreviewHtml() {
      const pageSizes = {
        A4: { width: 210, height: 297 },
        A3: { width: 297, height: 420 },
        Letter: { width: 216, height: 279 }
      }
      const size = pageSizes[this.pageDesign.pageSize] || pageSizes.A4
      const isLandscape = this.pageDesign.orientation === 'landscape'
      const width = isLandscape ? size.height : size.width
      const height = isLandscape ? size.width : size.height
      
      let html = `
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <title>打印预览</title>
          <style>
            @page {
              size: ${this.pageDesign.pageSize} ${this.pageDesign.orientation};
              margin: ${this.pageDesign.marginTop}mm ${this.pageDesign.marginRight}mm ${this.pageDesign.marginBottom}mm ${this.pageDesign.marginLeft}mm;
            }
            body {
              margin: 0;
              padding: 0;
              font-family: Arial, sans-serif;
              font-size: 12pt;
            }
            .print-page {
              width: ${width}mm;
              min-height: ${height}mm;
              margin: 0 auto;
              padding: ${this.pageDesign.marginTop}mm ${this.pageDesign.marginRight}mm ${this.pageDesign.marginBottom}mm ${this.pageDesign.marginLeft}mm;
              box-sizing: border-box;
            }
            .print-element {
              position: relative;
              margin-bottom: 5mm;
            }
            .print-text, .print-title {
              word-break: break-word;
            }
            .print-title {
              font-weight: bold;
              text-align: center;
            }
            .print-table {
              width: 100%;
              border-collapse: collapse;
              page-break-inside: auto;
            }
            .print-table thead {
              display: table-header-group;
            }
            .print-table tfoot {
              display: table-footer-group;
            }
            .print-table tbody tr {
              page-break-inside: avoid;
              page-break-after: auto;
            }
            .print-table th,
            .print-table td {
              border: 1px solid #ddd;
              padding: 4px;
              text-align: left;
            }
            .print-table th {
              background-color: #f5f5f5;
              font-weight: bold;
              text-align: center;
            }
            .print-table tfoot td {
              background-color: #f9f9f9;
              font-weight: bold;
              text-align: right;
            }
            .print-label-field {
              word-break: break-word;
            }
            @media print {
              .print-page {
                width: 100%;
                min-height: auto;
              }
            }
          </style>
        </head>
        <body>
          <div class="print-page">
      `
      
      // 生成元素HTML
      this.elements.forEach(element => {
        if (element.type === 'text' || element.type === 'title') {
          const style = `
            position: absolute;
            left: ${element.x || 0}mm;
            top: ${element.y || 0}mm;
            width: ${element.width || 100}mm;
            font-size: ${element.fontSize || 12}pt;
            font-weight: ${element.fontWeight || 'normal'};
            text-align: ${element.textAlign || 'left'};
            color: ${element.color || '#000000'};
            line-height: ${element.lineHeight || 1.5};
          `
          const className = element.type === 'title' ? 'print-title' : 'print-text'
          html += `<div class="print-element ${className}" style="${style}">${element.content || ''}</div>`
        } else if (element.type === 'label-field') {
          const style = `
            position: absolute;
            left: ${element.x || 0}mm;
            top: ${element.y || 0}mm;
            width: ${element.width || 100}mm;
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 2mm;
          `
          const labelStyle = `
            font-size: ${element.fontSize || 12}pt;
            font-weight: ${element.fontWeight || 'normal'};
            color: ${element.labelColor || '#000000'};
            margin-right: 5mm;
            flex: 0 0 ${element.labelWidth || 30}mm;
          `
          const fieldStyle = `
            font-size: ${element.fontSize || 12}pt;
            font-weight: ${element.fieldFontWeight || element.fontWeight || 'normal'};
            text-align: ${element.fieldAlign || 'left'};
            color: ${element.fieldColor || '#333'};
            flex: 1 1 auto;
          `
          const labelText = element.labelText || '标签'
          const fieldPlaceholder = element.fieldKey ? `{{${element.fieldKey}}}` : '字段'
          html += `<div class="print-element print-label-field" style="${style}">
            <span style="${labelStyle}">${labelText}</span>
            <span style="${fieldStyle}">${fieldPlaceholder}</span>
          </div>`
        } else if (element.type === 'table') {
          html += this.generateTableHtml(element)
        }
      })
      
      html += `
          </div>
        </body>
        </html>
      `
      
      return html
    },
    
    // 生成表格HTML
    generateTableHtml(element) {
      let html = `<table class="print-table" style="position: absolute; left: ${element.x || 0}mm; top: ${element.y || 0}mm; width: ${element.width || 100}mm;">`
      
      // 表头
      if (element.showHeader) {
        html += '<thead><tr>'
        element.columns.forEach(col => {
          let style = col.width ? `width: ${col.width}mm;` : ''
          if (col.headerFontSize) style += ` font-size: ${col.headerFontSize}pt;`
          if (col.headerFontWeight) style += ` font-weight: ${col.headerFontWeight};`
          if (col.headerColor) style += ` color: ${col.headerColor};`
          if (col.headerTextAlign) style += ` text-align: ${col.headerTextAlign};`
          html += `<th style="${style}">${col.label || ''}</th>`
        })
        html += '</tr></thead>'
      }
      
      // 表体 - 只生成1行，实际打印时会根据后端数据自动循环填充
      html += '<tbody>'
      html += '<tr>'
      element.columns.forEach(col => {
        let style = col.width ? `width: ${col.width}mm;` : ''
        // 应用列的数据样式
        if (col.fontSize) style += ` font-size: ${col.fontSize}pt;`
        if (col.fontWeight) style += ` font-weight: ${col.fontWeight};`
        if (col.color) style += ` color: ${col.color};`
        if (col.textAlign) style += ` text-align: ${col.textAlign};`
        
        const className = ''
        const content = col.fieldKey ? `{{${col.fieldKey}}}` : '数据'
        html += `<td${className} style="${style}">${content}</td>`
      })
      html += '</tr>'
      html += '</tbody>'
      
      // 表尾
      if (element.showFooter) {
        html += '<tfoot>'
        const footerRows = element.footerRows || 1
        for (let i = 0; i < footerRows; i++) {
          html += '<tr>'
          element.columns.forEach(col => {
            html += '<td></td>'
          })
          html += '</tr>'
        }
        html += '</tfoot>'
      }
      
      html += '</table>'
      return html
    },
    
    
    // 打印
    handlePrint() {
      this.$nextTick(() => {
        if (this.$refs.previewContainer) {
          // 使用 vue-print-nb
          this.$print(this.$refs.previewContainer, {
            title: '打印预览',
            popTitle: '打印预览'
          })
        }
      })
    },
    
    // 保存
    handleSave() {
      const templateData = {
        elements: this.elements,
        pageDesign: this.pageDesign,
        fieldGroups: this.fieldGroups
      }
      this.$emit('save', templateData)
      this.$message.success('保存成功')
    },
    
    // 获取模板JSON
    getTemplateJson() {
      return JSON.stringify({
        elements: this.elements,
        pageDesign: this.pageDesign,
        fieldGroups: this.fieldGroups
      })
    },
    
    // 生成测试数据
    generateTestData() {
      const data = {}
      this.fieldGroups.forEach(group => {
        group.fields.forEach(field => {
          data[field.fieldKey] = `测试${field.columnLabel}`
        })
      })
      return data
    }
  }
}
</script>

<style scoped>
.reportbro-designer {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f5f5;
}

.designer-toolbar {
  padding: 10px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.designer-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.designer-sidebar-left {
  width: 250px;
  background: #fff;
  border-right: 1px solid #e0e0e0;
  overflow-y: auto;
  padding: 10px;
}

.sidebar-section h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  font-weight: 600;
}

.component-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
}

.component-item {
  padding: 10px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
}

.component-item:hover {
  background-color: #f5f5f5;
  border-color: #409eff;
}

.component-item i {
  font-size: 18px;
  color: #409eff;
}

.empty-tip {
  padding: 20px;
  text-align: center;
  color: #999;
  font-size: 12px;
}

.field-list {
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}

.field-group {
  margin-bottom: 15px;
}

.field-group-title {
  font-weight: 600;
  font-size: 12px;
  color: #666;
  margin-bottom: 5px;
  padding: 5px;
  background: #f0f0f0;
  border-radius: 3px;
}

.field-item {
  padding: 8px;
  margin: 2px 0;
  cursor: move;
  border: 1px solid transparent;
  border-radius: 3px;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
}

.field-item:hover {
  background: #f0f0f0;
  border-color: #409eff;
}

.field-key {
  color: #999;
  font-size: 11px;
}

.designer-canvas-container {
  flex: 1;
  overflow: auto;
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.canvas-wrapper {
  position: relative;
}

.designer-canvas {
  position: relative;
  margin: 0 auto;
}

.grid-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 1;
}

.canvas-content {
  position: relative;
  z-index: 2;
}

.designer-sidebar-right {
  width: 320px;
  min-width: 320px;
  background: #fff;
  border-left: 1px solid #e0e0e0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.designer-sidebar-right .el-tabs {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.designer-sidebar-right .el-tabs__content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.designer-sidebar-right .el-tab-pane {
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.panel-content {
  padding: 15px;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  box-sizing: border-box;
  width: 100%;
  min-height: 0;
}

.panel-content .el-form {
  width: 100%;
  box-sizing: border-box;
}

.panel-content .el-form-item {
  width: 100%;
  margin-bottom: 15px;
  box-sizing: border-box;
}

.panel-content .el-form-item__label {
  width: 85px !important;
  text-align: right;
  padding-right: 8px;
  font-size: 12px;
  line-height: 32px;
}

.panel-content .el-form-item__content {
  margin-left: 85px !important;
  width: calc(100% - 85px);
  box-sizing: border-box;
}

.panel-content .el-form-item__content > * {
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.empty-state {
  text-align: center;
  color: #999;
  padding: 40px 20px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.table-list {
  max-height: 200px;
  overflow-y: auto;
}

.table-item {
  padding: 8px;
  cursor: pointer;
  border-radius: 3px;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
}

.table-item:hover {
  background: #f0f0f0;
}

.selected-table-fields {
  margin-top: 15px;
}

.selected-table-fields h5 {
  margin: 0 0 10px 0;
  font-size: 13px;
  font-weight: 600;
}

.preview-container {
  max-height: 70vh;
  overflow: auto;
  border: 1px solid #e0e0e0;
  padding: 20px;
  background: #fff;
}

.print-content {
  width: 100%;
}

.print-content table {
  width: 100%;
  border-collapse: collapse;
  page-break-inside: auto;
}

.print-content table thead {
  display: table-header-group;
}

.print-content table tfoot {
  display: table-footer-group;
}

.print-content table tr {
  page-break-inside: avoid;
  page-break-after: auto;
}

/* 打印样式 */
@media print {
  .preview-container {
    max-height: none;
    overflow: visible;
  }
  
  .print-content {
    width: 100%;
  }
  
  /* 表格跨页续打 */
  .print-content table thead {
    display: table-header-group;
  }
  
  .print-content table tfoot {
    display: table-footer-group;
  }
  
  .print-content table tbody tr {
    page-break-inside: avoid;
  }
}
</style>

