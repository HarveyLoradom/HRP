<template>
  <div
    class="label-field-element"
    :class="{ selected: selected }"
    :style="elementStyle"
    @click.stop="$emit('select')"
    @dblclick="handleEdit"
    @mousedown="handleMouseDown"
  >
    <div class="element-content" :style="contentStyle">
      <span class="label-text" :style="labelStyle">{{ element.labelText || '标签' }}</span>
      <span class="field-placeholder" :style="fieldStyle">{{ getFieldPlaceholder() }}</span>
    </div>
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
  name: 'LabelFieldElement',
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
      const columnWidth = (100 - 40) / this.columnCount
      const widthPercent = (this.element.columnSpan || 1) * (columnWidth / 100) * 100
      
      return {
        position: 'absolute',
        left: `${this.element.x || 0}mm`,
        top: `${this.element.y || 0}mm`,
        width: `${this.element.width || 100}mm`,
        minHeight: '10mm',
        zIndex: this.index + 10,
        cursor: 'move'
      }
    },
    contentStyle() {
      return {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '2mm',
        wordBreak: 'break-word'
      }
    },
    labelStyle() {
      return {
        fontSize: `${this.element.fontSize || 12}pt`,
        fontWeight: this.element.fontWeight || 'normal',
        textAlign: 'left',
        color: this.element.labelColor || '#000000',
        flex: this.element.labelWidth ? '0 0 ' + this.element.labelWidth + 'mm' : '0 0 auto',
        marginRight: '5mm'
      }
    },
    fieldStyle() {
      return {
        fontSize: `${this.element.fontSize || 12}pt`,
        fontWeight: this.element.fieldFontWeight || this.element.fontWeight || 'normal',
        textAlign: this.element.fieldAlign || 'left',
        color: this.element.fieldColor || '#333',
        flex: '1 1 auto'
      }
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
    handleEdit() {
      // 可以在这里实现内联编辑
    },
    getFieldPlaceholder() {
      if (this.element.fieldKey) {
        return `{{${this.element.fieldKey}}}`
      }
      return '字段'
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
      this.elementStartHeight = this.element.height || 20
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
          newWidth = Math.max(20, this.elementStartWidth + deltaX)
        }
        if (this.resizeDirection.includes('w')) {
          newWidth = Math.max(20, this.elementStartWidth - deltaX)
          newX = Math.max(0, (this.element.x || 0) + deltaX)
        }
        if (this.resizeDirection.includes('s')) {
          newHeight = Math.max(10, this.elementStartHeight + deltaY)
        }
        if (this.resizeDirection.includes('n')) {
          newHeight = Math.max(10, this.elementStartHeight - deltaY)
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
.label-field-element {
  border: 1px dashed transparent;
  position: relative;
}

.label-field-element.selected {
  border-color: #409eff;
}

.element-content {
  min-height: 10mm;
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

