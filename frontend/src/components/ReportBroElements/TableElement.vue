<template>
  <div
    class="table-element"
    :class="{ selected: selected }"
    :style="elementStyle"
    @click.stop="$emit('select')"
    @mousedown="handleMouseDown"
  >
    <table class="element-table" :style="tableStyle">
      <!-- 表头 -->
      <thead v-if="element.showHeader">
        <tr v-for="(row, rowIndex) in headerRows" :key="`header-${rowIndex}`">
          <th
            v-for="(cell, cellIndex) in row"
            :key="`header-${rowIndex}-${cellIndex}`"
            :colspan="cell.colspan || 1"
            :rowspan="cell.rowspan || 1"
            :style="{ ...getHeaderCellStyle(cell), width: cell.width || 'auto' }"
          >
            {{ cell.label || cell.content || '' }}
          </th>
        </tr>
      </thead>
      
      <!-- 表体 -->
      <tbody>
        <tr v-for="(dataRow, rowIndex) in sampleDataRows" :key="`body-${rowIndex}`">
          <td
            v-for="(col, colIndex) in element.columns"
            :key="`body-${rowIndex}-${colIndex}`"
            :style="{ ...getBodyCellStyle(col), width: col.width ? `${col.width}mm` : 'auto' }"
          >
            <template v-if="col.fieldKey">{{ getFieldPlaceholder(col.fieldKey) }}</template>
            <template v-else>数据</template>
          </td>
        </tr>
      </tbody>
      
      <!-- 表尾 -->
      <tfoot v-if="element.showFooter">
        <tr v-for="(row, rowIndex) in footerRows" :key="`footer-${rowIndex}`">
          <td
            v-for="(cell, cellIndex) in row"
            :key="`footer-${rowIndex}-${cellIndex}`"
            :colspan="cell.colspan || 1"
            :rowspan="cell.rowspan || 1"
            :style="footerCellStyle"
          >
            {{ cell.label || cell.content || '' }}
          </td>
        </tr>
      </tfoot>
    </table>
    
    <div v-if="selected" class="element-handles">
      <div class="handle handle-top-left" @mousedown.stop="handleResizeStart($event, 'nw')"></div>
      <div class="handle handle-top-right" @mousedown.stop="handleResizeStart($event, 'ne')"></div>
      <div class="handle handle-bottom-left" @mousedown.stop="handleResizeStart($event, 'sw')"></div>
      <div class="handle handle-bottom-right" @mousedown.stop="handleResizeStart($event, 'se')"></div>
      <div class="element-delete" @click.stop="$emit('delete')" title="删除">
        <i class="el-icon-delete"></i>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TableElement',
  props: {
    element: {
      type: Object,
      required: true
    },
    index: {
      type: Number,
      default: 0
    },
    selected: {
      type: Boolean,
      default: false
    },
    columnCount: {
      type: Number,
      default: 4
    }
  },
  computed: {
    elementStyle() {
      return {
        position: 'absolute',
        left: `${this.element.x || 0}mm`,
        top: `${this.element.y || 0}mm`,
        width: `${this.element.width || 100}mm`,
        zIndex: this.index + 10,
        cursor: 'move'
      }
    },
    tableStyle() {
      return {
        width: '100%',
        borderCollapse: 'collapse',
        fontSize: '10pt'
      }
    },
    headerCellStyle() {
      return {
        border: '1px solid #ddd',
        padding: '4px',
        backgroundColor: '#f5f5f5'
      }
    },
    bodyCellStyle() {
      return {
        border: '1px solid #ddd',
        padding: '4px',
        textAlign: 'left'
      }
    },
    footerCellStyle() {
      const style = {
        border: '1px solid #ddd',
        padding: '4px',
        backgroundColor: '#f9f9f9',
        fontWeight: 'bold',
        textAlign: 'right'
      }
      if (this.element.footerRowHeight) {
        style.height = `${this.element.footerRowHeight}mm`
      }
      return style
    },
    headerRows() {
      // 如果定义了表头结构，使用表头结构，否则使用默认结构
      if (this.element.headerStructure && this.element.headerStructure.length > 0) {
        return this.element.headerStructure
      }
      const rows = []
      const headerRows = this.element.headerRows || 1
      for (let i = 0; i < headerRows; i++) {
        const row = this.element.columns.map((col, index) => ({
          label: col.label || `列${index + 1}`,
          colspan: col.colspan || 1,
          rowspan: col.rowspan || 1,
          width: col.width ? `${col.width}mm` : 'auto',
          colIndex: index
        }))
        rows.push(row)
      }
      return rows
    },
    footerRows() {
      const rows = []
      const footerRows = this.element.footerRows || 1
      for (let i = 0; i < footerRows; i++) {
        const row = this.element.columns.map((col, index) => ({
          label: '',
          colspan: 1,
          rowspan: 1
        }))
        rows.push(row)
      }
      return rows
    },
    sampleDataRows() {
      // 只显示1行示例数据，实际打印时会根据后端数据自动填充
      return [0]
    }
  },
  data() {
    return {
      isDragging: false,
      isResizing: false,
      dragStartX: 0,
      dragStartY: 0,
      elementStartX: 0,
      elementStartY: 0,
      resizeStartX: 0,
      resizeStartY: 0,
      elementStartWidth: 0,
      elementStartHeight: 0,
      resizeDirection: ''
    }
  },
  mounted() {
    document.addEventListener('mousemove', this.handleMouseMove)
    document.addEventListener('mouseup', this.handleMouseUp)
  },
  beforeDestroy() {
    document.removeEventListener('mousemove', this.handleMouseMove)
    document.removeEventListener('mouseup', this.handleMouseUp)
  },
  methods: {
    getHeaderCellStyle(cell) {
      // 通过label找到对应的列
      const colIndex = this.element.columns.findIndex(c => c.label === cell.label || c.label === cell.content)
      const col = colIndex >= 0 ? this.element.columns[colIndex] : null
      const style = {
        border: '1px solid #ddd',
        padding: '4px',
        backgroundColor: '#f5f5f5',
        fontWeight: col?.headerFontWeight || 'bold',
        textAlign: col?.headerTextAlign || 'center',
        fontSize: col?.headerFontSize ? `${col.headerFontSize}pt` : undefined,
        color: col?.headerColor || '#000000'
      }
      if (this.element.headerRowHeight) {
        style.height = `${this.element.headerRowHeight}mm`
      }
      // 移除 undefined 值
      Object.keys(style).forEach(key => {
        if (style[key] === undefined) {
          delete style[key]
        }
      })
      return style
    },
    getBodyCellStyle(col) {
      const style = {
        border: '1px solid #ddd',
        padding: '4px',
        textAlign: col.textAlign || 'left',
        fontSize: col.fontSize ? `${col.fontSize}pt` : undefined,
        fontWeight: col.fontWeight || 'normal',
        color: col.color || '#000000'
      }
      if (this.element.bodyRowHeight && !this.element.autoRowHeight) {
        style.height = `${this.element.bodyRowHeight}mm`
      }
      // 移除 undefined 值
      Object.keys(style).forEach(key => {
        if (style[key] === undefined) {
          delete style[key]
        }
      })
      return style
    },
    getFieldPlaceholder(fieldKey) {
      return `{{${fieldKey}}}`
    },
    handleMouseDown(e) {
      // 如果点击的是删除按钮或调整大小手柄，不触发拖动
      if (e.target.closest('.element-delete') || e.target.closest('.handle')) {
        return
      }
      // 选中元素并开始拖动
      if (!this.selected) {
        this.$emit('select')
      }
      if (!this.isResizing) {
        this.isDragging = true
        this.dragStartX = e.clientX
        this.dragStartY = e.clientY
        this.elementStartX = this.element.x || 0
        this.elementStartY = this.element.y || 0
        e.preventDefault()
        e.stopPropagation()
      }
    },
    handleResizeStart(e, direction) {
      this.isResizing = true
      this.resizeDirection = direction
      this.resizeStartX = e.clientX
      this.resizeStartY = e.clientY
      this.elementStartWidth = this.element.width || 100
      this.elementStartHeight = this.element.height || 50
      e.preventDefault()
      e.stopPropagation()
    },
    handleMouseMove(e) {
      if (this.isDragging) {
        const deltaX = (e.clientX - this.dragStartX) / 3.779527559
        const deltaY = (e.clientY - this.dragStartY) / 3.779527559
        const newX = Math.max(0, this.elementStartX + deltaX)
        const newY = Math.max(0, this.elementStartY + deltaY)
        this.$emit('update', { x: newX, y: newY })
      } else if (this.isResizing) {
        const deltaX = (e.clientX - this.resizeStartX) / 3.779527559
        const deltaY = (e.clientY - this.resizeStartY) / 3.779527559
        let newWidth = this.elementStartWidth
        let newHeight = this.elementStartHeight
        let newX = this.element.x || 0
        let newY = this.element.y || 0
        
        if (this.resizeDirection.includes('e')) {
          newWidth = Math.max(50, this.elementStartWidth + deltaX)
        }
        if (this.resizeDirection.includes('w')) {
          newWidth = Math.max(50, this.elementStartWidth - deltaX)
          newX = Math.max(0, (this.element.x || 0) + deltaX)
        }
        if (this.resizeDirection.includes('s')) {
          newHeight = Math.max(30, this.elementStartHeight + deltaY)
        }
        if (this.resizeDirection.includes('n')) {
          newHeight = Math.max(30, this.elementStartHeight - deltaY)
          newY = Math.max(0, (this.element.y || 0) + deltaY)
        }
        
        this.$emit('update', { width: newWidth, height: newHeight, x: newX, y: newY })
      }
    },
    handleMouseUp() {
      this.isDragging = false
      this.isResizing = false
      this.resizeDirection = ''
    }
  }
}
</script>

<style scoped>
.table-element {
  border: 1px dashed transparent;
  position: relative;
}

.table-element.selected {
  border-color: #409eff;
}

.element-table {
  width: 100%;
  border-collapse: collapse;
}

.element-handles {
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  pointer-events: none;
}

.handle {
  position: absolute;
  width: 8px;
  height: 8px;
  background: #409eff;
  border: 1px solid #fff;
  border-radius: 50%;
  pointer-events: all;
  cursor: nwse-resize;
}

.handle-top-left {
  top: -4px;
  left: -4px;
  cursor: nwse-resize;
}

.handle-top-right {
  top: -4px;
  right: -4px;
  cursor: nesw-resize;
}

.handle-bottom-left {
  bottom: -4px;
  left: -4px;
  cursor: nesw-resize;
}

.handle-bottom-right {
  bottom: -4px;
  right: -4px;
  cursor: nwse-resize;
}

.element-delete {
  position: absolute;
  top: -20px;
  right: -20px;
  width: 24px;
  height: 24px;
  background: #f56c6c;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  pointer-events: all;
  z-index: 100;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
  transition: all 0.3s;
}

.element-delete:hover {
  background: #f78989;
  transform: scale(1.1);
}

.element-delete i {
  color: #fff;
  font-size: 14px;
}
</style>

