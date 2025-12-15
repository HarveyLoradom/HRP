<template>
  <div class="element-properties">
    <el-form label-width="85px" size="small">
      <!-- 文本/标题属性 -->
      <template v-if="element.type === 'text' || element.type === 'title'">
        <el-form-item label="内容">
          <el-input v-model="localElement.content" @input="handleUpdate"></el-input>
        </el-form-item>
        <el-form-item label="字体大小">
          <el-input-number v-model="localElement.fontSize" :min="8" :max="72" @change="handleUpdate"></el-input-number>
        </el-form-item>
        <el-form-item label="字体粗细">
          <el-radio-group v-model="localElement.fontWeight" @change="handleUpdate">
            <el-radio label="normal">正常</el-radio>
            <el-radio label="bold">粗体</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="对齐方式">
          <el-radio-group v-model="localElement.textAlign" @change="handleUpdate">
            <el-radio label="left">左对齐</el-radio>
            <el-radio label="center">居中</el-radio>
            <el-radio label="right">右对齐</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="文字颜色">
          <el-color-picker v-model="localElement.color" @change="handleUpdate"></el-color-picker>
        </el-form-item>
        <el-form-item label="行高">
          <el-input-number v-model="localElement.lineHeight" :min="1" :max="3" :step="0.1" @change="handleUpdate"></el-input-number>
        </el-form-item>
        <el-form-item label="列跨度">
          <el-input-number 
            v-model="localElement.columnSpan" 
            :min="1" 
            :max="columnCount"
            @change="handleUpdate"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="位置X">
          <el-input-number v-model="localElement.x" :min="0" @change="handleUpdate"></el-input-number>
        </el-form-item>
        <el-form-item label="位置Y">
          <el-input-number v-model="localElement.y" :min="0" @change="handleUpdate"></el-input-number>
        </el-form-item>
        <el-form-item label="宽度">
          <el-input-number v-model="localElement.width" :min="10" @change="handleUpdate"></el-input-number>
        </el-form-item>
      </template>

      <!-- 标签字段属性 -->
      <template v-if="element.type === 'label-field'">
        <el-form-item label="标签文本">
          <el-input v-model="localElement.labelText" @input="handleUpdate"></el-input>
        </el-form-item>
        <el-form-item label="字段">
          <el-select v-model="localElement.fieldKey" filterable placeholder="选择字段" @change="handleUpdate" style="width: 100%">
            <el-option
              v-for="field in allFields"
              :key="field.key"
              :label="field.label"
              :value="field.key"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="标签宽度">
          <el-input-number v-model="localElement.labelWidth" :min="10" :max="200" @change="handleUpdate"></el-input-number>
        </el-form-item>
        <el-form-item label="字体大小">
          <el-input-number v-model="localElement.fontSize" :min="8" :max="72" @change="handleUpdate"></el-input-number>
        </el-form-item>
        <el-form-item label="字体粗细">
          <el-radio-group v-model="localElement.fontWeight" @change="handleUpdate">
            <el-radio label="normal">正常</el-radio>
            <el-radio label="bold">粗体</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标签颜色">
          <el-color-picker v-model="localElement.labelColor" @change="handleUpdate"></el-color-picker>
        </el-form-item>
        <el-form-item label="字段对齐">
          <el-radio-group v-model="localElement.fieldAlign" @change="handleUpdate">
            <el-radio label="left">左对齐</el-radio>
            <el-radio label="center">居中</el-radio>
            <el-radio label="right">右对齐</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="字段颜色">
          <el-color-picker v-model="localElement.fieldColor" @change="handleUpdate"></el-color-picker>
        </el-form-item>
        <el-form-item label="字段字体粗细">
          <el-radio-group v-model="localElement.fieldFontWeight" @change="handleUpdate">
            <el-radio label="normal">正常</el-radio>
            <el-radio label="bold">粗体</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="列跨度">
          <el-input-number 
            v-model="localElement.columnSpan" 
            :min="1" 
            :max="columnCount"
            @change="handleUpdate"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="位置X">
          <el-input-number v-model="localElement.x" :min="0" @change="handleUpdate"></el-input-number>
        </el-form-item>
        <el-form-item label="位置Y">
          <el-input-number v-model="localElement.y" :min="0" @change="handleUpdate"></el-input-number>
        </el-form-item>
        <el-form-item label="宽度">
          <el-input-number v-model="localElement.width" :min="10" @change="handleUpdate"></el-input-number>
        </el-form-item>
      </template>

      <!-- 表格属性 -->
      <template v-if="element.type === 'table'">
        <el-form-item label="显示表头">
          <el-switch v-model="localElement.showHeader" @change="handleUpdate"></el-switch>
        </el-form-item>
        <el-form-item label="显示表尾">
          <el-switch v-model="localElement.showFooter" @change="handleUpdate"></el-switch>
        </el-form-item>
        <el-form-item label="表头行数">
          <el-input-number v-model="localElement.headerRows" :min="1" :max="5" @change="handleUpdate"></el-input-number>
        </el-form-item>
        <el-form-item label="表尾行数">
          <el-input-number v-model="localElement.footerRows" :min="1" :max="5" @change="handleUpdate"></el-input-number>
        </el-form-item>
        <el-form-item label="列跨度">
          <el-input-number 
            v-model="localElement.columnSpan" 
            :min="1" 
            :max="columnCount"
            @change="handleUpdate"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="位置X">
          <el-input-number v-model="localElement.x" :min="0" @change="handleUpdate"></el-input-number>
        </el-form-item>
        <el-form-item label="位置Y">
          <el-input-number v-model="localElement.y" :min="0" @change="handleUpdate"></el-input-number>
        </el-form-item>
        <el-form-item label="宽度">
          <el-input-number v-model="localElement.width" :min="10" @change="handleUpdate"></el-input-number>
        </el-form-item>
        
        <!-- 列设置 -->
        <el-divider>列设置</el-divider>
        <div class="columns-container">
          <div v-for="(col, index) in localElement.columns" :key="index" class="column-item">
            <div class="column-header">
              <span class="column-title">列{{ index + 1 }}</span>
              <el-button 
                size="mini" 
                type="danger" 
                icon="el-icon-delete"
                @click="handleRemoveColumn(index)"
                circle
              ></el-button>
            </div>
            <el-form-item label="列标题">
              <el-input v-model="col.label" placeholder="列标题" @input="handleUpdate" size="small"></el-input>
            </el-form-item>
            <el-form-item label="字段">
              <el-select 
                v-model="col.fieldKey" 
                placeholder="选择字段" 
                clearable
                @change="handleUpdate"
                size="small"
                style="width: 100%;"
              >
                <el-option
                  v-for="field in allFields"
                  :key="field.fieldKey"
                  :label="`${field.columnLabel} (${field.fieldKey})`"
                  :value="field.fieldKey"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="宽度(mm)">
              <el-input-number 
                v-model="col.width" 
                :min="20" 
                @change="handleUpdate"
                size="small"
                style="width: 100%;"
              ></el-input-number>
            </el-form-item>
            <el-form-item label="字体大小">
              <el-input-number 
                v-model="col.fontSize" 
                :min="8" 
                :max="72"
                @change="handleUpdate"
                size="small"
                style="width: 100%;"
              ></el-input-number>
            </el-form-item>
            <el-form-item label="字体粗细">
              <el-radio-group v-model="col.fontWeight" @change="handleUpdate" size="small">
                <el-radio label="normal">正常</el-radio>
                <el-radio label="bold">粗体</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="文字颜色">
              <el-color-picker v-model="col.color" @change="handleUpdate" size="small"></el-color-picker>
            </el-form-item>
            <el-form-item label="对齐方式">
              <el-radio-group v-model="col.textAlign" @change="handleUpdate" size="small">
                <el-radio label="left">左</el-radio>
                <el-radio label="center">中</el-radio>
                <el-radio label="right">右</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <!-- 表头样式 -->
            <el-divider>表头样式</el-divider>
            <el-form-item label="表头字体大小">
              <el-input-number 
                v-model="col.headerFontSize" 
                :min="8" 
                :max="72"
                @change="handleUpdate"
                size="small"
                style="width: 100%;"
              ></el-input-number>
            </el-form-item>
            <el-form-item label="表头字体粗细">
              <el-radio-group v-model="col.headerFontWeight" @change="handleUpdate" size="small">
                <el-radio label="normal">正常</el-radio>
                <el-radio label="bold">粗体</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="表头文字颜色">
              <el-color-picker v-model="col.headerColor" @change="handleUpdate" size="small"></el-color-picker>
            </el-form-item>
            <el-form-item label="表头对齐方式">
              <el-radio-group v-model="col.headerTextAlign" @change="handleUpdate" size="small">
                <el-radio label="left">左</el-radio>
                <el-radio label="center">中</el-radio>
                <el-radio label="right">右</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
        </div>
        <el-button 
          size="small" 
          type="primary" 
          icon="el-icon-plus"
          @click="handleAddColumn"
          style="width: 100%; margin-bottom: 10px;"
        >添加列</el-button>
        
        <!-- 行高设置 -->
        <el-divider>行高设置</el-divider>
        <el-form-item label="表头行高">
          <el-input-number 
            v-model="localElement.headerRowHeight" 
            :min="10" 
            :max="100"
            placeholder="行高(mm)"
            @change="handleUpdate"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="表体行高">
          <el-input-number 
            v-model="localElement.bodyRowHeight" 
            :min="10" 
            :max="100"
            placeholder="行高(mm)"
            @change="handleUpdate"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="表尾行高">
          <el-input-number 
            v-model="localElement.footerRowHeight" 
            :min="10" 
            :max="100"
            placeholder="行高(mm)"
            @change="handleUpdate"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="自适应高度">
          <el-switch v-model="localElement.autoRowHeight" @change="handleUpdate"></el-switch>
        </el-form-item>
      </template>
    </el-form>
  </div>
</template>

<script>
export default {
  name: 'ElementProperties',
  props: {
    element: {
      type: Object,
      required: true
    },
    columnCount: {
      type: Number,
      default: 4
    },
    fieldGroups: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      localElement: {}
    }
  },
  computed: {
    allFields() {
      const fields = []
      this.fieldGroups.forEach(group => {
        group.fields.forEach(field => {
          fields.push(field)
        })
      })
      return fields
    }
  },
  watch: {
    element: {
      immediate: true,
      deep: true,
      handler(newVal) {
        this.localElement = JSON.parse(JSON.stringify(newVal))
      }
    }
  },
  methods: {
    handleUpdate() {
      this.$emit('update', this.localElement)
    },
    handleAddColumn() {
      if (!this.localElement.columns) {
        this.localElement.columns = []
      }
      this.localElement.columns.push({
        fieldKey: '',
        label: `列${this.localElement.columns.length + 1}`,
        width: 50,
        fontSize: 12,
        fontWeight: 'normal',
        color: '#000000',
        textAlign: 'left',
        headerFontSize: 12,
        headerFontWeight: 'bold',
        headerColor: '#000000',
        headerTextAlign: 'center'
      })
      this.handleUpdate()
    },
    handleRemoveColumn(index) {
      if (this.localElement.columns && this.localElement.columns.length > 1) {
        this.localElement.columns.splice(index, 1)
        this.handleUpdate()
      } else {
        this.$message.warning('至少保留一列')
      }
    },
    handleMergeCell(index) {
      // 合并单元格逻辑 - 这里简化处理，实际应该支持选择多个单元格
      if (!this.localElement.headerStructure) {
        this.localElement.headerStructure = []
      }
      this.$message.info('合并单元格功能：请在表头结构中配置')
      this.handleUpdate()
    },
    handleSplitCell(index) {
      // 拆分单元格逻辑
      this.$message.info('拆分单元格功能：请在表头结构中配置')
      this.handleUpdate()
    },
  }
}
</script>

<style scoped>
.element-properties {
  padding: 10px;
  width: 100%;
  box-sizing: border-box;
}

.element-properties .el-form {
  width: 100%;
  box-sizing: border-box;
}

.element-properties .el-form-item {
  margin-bottom: 15px;
  width: 100%;
  box-sizing: border-box;
}

.element-properties .el-form-item__label {
  width: 85px !important;
  text-align: right;
  padding-right: 8px;
  word-break: break-word;
  font-size: 12px;
  line-height: 32px;
}

.element-properties .el-form-item__content {
  margin-left: 85px !important;
  width: calc(100% - 85px);
  box-sizing: border-box;
}

.element-properties .el-form-item__content > * {
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.columns-container {
  max-height: 400px;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 5px;
}

.column-item {
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 10px;
  background: #f9f9f9;
  width: 100%;
  box-sizing: border-box;
}

.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e0e0e0;
}

.column-title {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.column-item .el-form-item {
  margin-bottom: 12px;
}

.column-item .el-form-item__label {
  width: 80px !important;
  text-align: right;
  padding-right: 8px;
  font-size: 12px;
  line-height: 32px;
}

.column-item .el-form-item__content {
  margin-left: 80px !important;
  width: calc(100% - 80px);
  box-sizing: border-box;
}

.column-item .el-form-item__content > * {
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.conditional-style-list {
  max-height: 300px;
  overflow-y: auto;
}

.conditional-style-item {
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 10px;
  background: #f9f9f9;
}

.el-divider {
  margin: 15px 0;
}
</style>

