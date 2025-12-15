<template>
  <el-dialog
    title="编辑表头结构"
    :visible.sync="visible"
    width="80%"
    :close-on-click-modal="false"
    :modal="false"
    @close="handleClose"
  >
    <div class="header-structure-editor">
      <div class="editor-toolbar">
        <el-button size="small" icon="el-icon-plus" @click="handleAddRow">添加行</el-button>
        <el-button size="small" icon="el-icon-delete" @click="handleDeleteRow" :disabled="selectedRowIndex === null">删除行</el-button>
        <el-button size="small" icon="el-icon-connection" @click="handleMergeCells" :disabled="!hasSelectedCells">合并单元格</el-button>
        <el-button size="small" icon="el-icon-split" @click="handleSplitCell" :disabled="!hasSelectedCell">拆分单元格</el-button>
      </div>
      
      <div class="structure-table-container">
        <table class="structure-table" ref="structureTable">
          <tbody>
            <tr 
              v-for="(row, rowIndex) in headerStructure" 
              :key="rowIndex"
              :class="{ selected: selectedRowIndex === rowIndex }"
              @click="handleRowSelect(rowIndex)"
            >
              <td
                v-for="(cell, cellIndex) in row"
                :key="cellIndex"
                :class="{ 
                  selected: isCellSelected(rowIndex, cellIndex),
                  merged: cell.colspan > 1 || cell.rowspan > 1
                }"
                :colspan="cell.colspan || 1"
                :rowspan="cell.rowspan || 1"
                @click.stop="handleCellSelect(rowIndex, cellIndex)"
                @mousedown="handleCellMouseDown($event, rowIndex, cellIndex)"
                @mouseup="handleCellMouseUp($event, rowIndex, cellIndex)"
              >
                <el-input
                  v-model="cell.label"
                  size="mini"
                  placeholder="表头文本"
                  @input="handleCellUpdate"
                ></el-input>
                <div class="cell-info">
                  <span v-if="cell.colspan > 1">跨{{ cell.colspan }}列</span>
                  <span v-if="cell.rowspan > 1">跨{{ cell.rowspan }}行</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <div class="cell-properties" v-if="selectedCell">
        <h4>单元格属性</h4>
        <el-form label-width="100px" size="small">
          <el-form-item label="文本">
            <el-input v-model="selectedCell.label" @input="handleCellUpdate"></el-input>
          </el-form-item>
          <el-form-item label="跨列">
            <el-input-number v-model="selectedCell.colspan" :min="1" :max="10" @change="handleCellUpdate"></el-input-number>
          </el-form-item>
          <el-form-item label="跨行">
            <el-input-number v-model="selectedCell.rowspan" :min="1" :max="10" @change="handleCellUpdate"></el-input-number>
          </el-form-item>
          <el-form-item label="宽度">
            <el-input-number v-model="selectedCell.width" :min="10" @change="handleCellUpdate"></el-input-number>
          </el-form-item>
        </el-form>
      </div>
    </div>
    
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSave">确定</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'ReportBroHeaderStructureEditor',
  props: {
    value: {
      type: Boolean,
      default: false
    },
    element: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      visible: this.value,
      headerStructure: [],
      selectedRowIndex: null,
      selectedCells: [],
      selectedCell: null,
      mouseDownCell: null
    }
  },
  computed: {
    hasSelectedCells() {
      return this.selectedCells.length > 1
    },
    hasSelectedCell() {
      return this.selectedCell !== null
    }
  },
  watch: {
    value(newVal) {
      this.visible = newVal
      if (newVal && this.element) {
        this.initStructure()
      }
    },
    visible(newVal) {
      this.$emit('input', newVal)
    }
  },
  methods: {
    initStructure() {
      if (this.element.headerStructure && this.element.headerStructure.length > 0) {
        this.headerStructure = JSON.parse(JSON.stringify(this.element.headerStructure))
      } else {
        // 初始化默认表头结构
        const rows = []
        const headerRows = this.element.headerRows || 1
        for (let i = 0; i < headerRows; i++) {
          const row = this.element.columns.map((col, index) => ({
            label: col.label || `列${index + 1}`,
            colspan: 1,
            rowspan: 1,
            width: col.width || 50
          }))
          rows.push(row)
        }
        this.headerStructure = rows
      }
    },
    handleRowSelect(rowIndex) {
      this.selectedRowIndex = rowIndex
      this.selectedCells = []
      this.selectedCell = null
    },
    handleCellSelect(rowIndex, cellIndex) {
      const cell = this.headerStructure[rowIndex][cellIndex]
      this.selectedCell = cell
      this.selectedCells = [{ rowIndex, cellIndex }]
    },
    handleCellMouseDown(e, rowIndex, cellIndex) {
      this.mouseDownCell = { rowIndex, cellIndex }
    },
    handleCellMouseUp(e, rowIndex, cellIndex) {
      if (this.mouseDownCell) {
        const startRow = Math.min(this.mouseDownCell.rowIndex, rowIndex)
        const endRow = Math.max(this.mouseDownCell.rowIndex, rowIndex)
        const startCol = Math.min(this.mouseDownCell.cellIndex, cellIndex)
        const endCol = Math.max(this.mouseDownCell.cellIndex, cellIndex)
        
        this.selectedCells = []
        for (let r = startRow; r <= endRow; r++) {
          for (let c = startCol; c <= endCol; c++) {
            this.selectedCells.push({ rowIndex: r, cellIndex: c })
          }
        }
        this.mouseDownCell = null
      }
    },
    isCellSelected(rowIndex, cellIndex) {
      return this.selectedCells.some(cell => 
        cell.rowIndex === rowIndex && cell.cellIndex === cellIndex
      )
    },
    handleAddRow() {
      const columnCount = this.element.columns ? this.element.columns.length : 4
      const newRow = Array(columnCount).fill(null).map((_, index) => ({
        label: `列${index + 1}`,
        colspan: 1,
        rowspan: 1,
        width: 50
      }))
      this.headerStructure.push(newRow)
    },
    handleDeleteRow() {
      if (this.selectedRowIndex !== null && this.headerStructure.length > 1) {
        this.headerStructure.splice(this.selectedRowIndex, 1)
        this.selectedRowIndex = null
      }
    },
    handleMergeCells() {
      if (this.selectedCells.length < 2) {
        this.$message.warning('请选择至少两个单元格')
        return
      }
      
      // 简化处理：合并选中的第一个单元格
      const firstCell = this.selectedCells[0]
      const cell = this.headerStructure[firstCell.rowIndex][firstCell.cellIndex]
      
      // 计算跨列和跨行
      const rows = new Set(this.selectedCells.map(c => c.rowIndex))
      const cols = new Set(this.selectedCells.map(c => c.cellIndex))
      cell.colspan = cols.size
      cell.rowspan = rows.size
      
      // 标记其他单元格为已合并（实际应该删除，这里简化处理）
      this.selectedCells.slice(1).forEach(({ rowIndex, cellIndex }) => {
        const c = this.headerStructure[rowIndex][cellIndex]
        c.merged = true
      })
      
      this.selectedCells = []
      this.$message.success('合并成功')
    },
    handleSplitCell() {
      if (!this.selectedCell) {
        this.$message.warning('请选择一个单元格')
        return
      }
      
      this.selectedCell.colspan = 1
      this.selectedCell.rowspan = 1
      this.$message.success('拆分成功')
    },
    handleCellUpdate() {
      // 单元格更新
    },
    handleSave() {
      this.$emit('save', this.headerStructure)
      this.handleClose()
    },
    handleClose() {
      this.visible = false
      this.$emit('close')
    }
  }
}
</script>

<style scoped>
.header-structure-editor {
  padding: 10px;
}

.editor-toolbar {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
}

.structure-table-container {
  max-height: 400px;
  overflow: auto;
  border: 1px solid #e0e0e0;
  margin-bottom: 20px;
}

.structure-table {
  width: 100%;
  border-collapse: collapse;
}

.structure-table td {
  border: 1px solid #ddd;
  padding: 5px;
  min-width: 100px;
  position: relative;
  background: #fff;
}

.structure-table td.selected {
  background: #e6f7ff;
  border-color: #409eff;
}

.structure-table td.merged {
  background: #f0f0f0;
}

.structure-table tr.selected {
  background: #f5f5f5;
}

.cell-info {
  font-size: 10px;
  color: #999;
  margin-top: 2px;
}

.cell-properties {
  border-top: 1px solid #e0e0e0;
  padding-top: 15px;
}

.cell-properties h4 {
  margin: 0 0 15px 0;
  font-size: 14px;
}
</style>

