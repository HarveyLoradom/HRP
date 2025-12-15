<template>
  <div class="bpmn-designer-container">
    <!-- 工具栏 -->
    <div class="bpmn-toolbar">
      <el-button-group>
        <el-button size="small" icon="el-icon-back" @click="handleUndo" :disabled="!canUndo">撤销</el-button>
        <el-button size="small" icon="el-icon-right" @click="handleRedo" :disabled="!canRedo">重做</el-button>
      </el-button-group>
      <el-divider direction="vertical"></el-divider>
      <el-button size="small" icon="el-icon-zoom-in" @click="handleZoomIn">放大</el-button>
      <el-button size="small" icon="el-icon-zoom-out" @click="handleZoomOut">缩小</el-button>
      <el-button size="small" icon="el-icon-full-screen" @click="handleZoomReset">重置</el-button>
      <el-divider direction="vertical"></el-divider>
      <el-button size="small" icon="el-icon-view" @click="handlePreview">预览XML</el-button>
      <el-button size="small" type="primary" icon="el-icon-check" @click="handleSave">保存</el-button>
    </div>

    <!-- 设计器容器 -->
    <div class="bpmn-canvas-container">
      <!-- 左侧属性面板 -->
      <div class="bpmn-properties-panel" v-if="selectedElement">
        <div class="properties-header">
          <h4>属性配置</h4>
          <el-button size="mini" icon="el-icon-close" circle @click="selectedElement = null"></el-button>
        </div>
        <div class="properties-content">
          <element-properties
            :element="selectedElement"
            :bpmn-modeling="bpmnModeling"
            :element-registry="elementRegistry"
            @update="handlePropertyUpdate"
          />
        </div>
      </div>

      <!-- 中间画布 -->
      <div class="bpmn-canvas" ref="bpmnCanvas"></div>
    </div>

    <!-- XML预览对话框 -->
    <el-dialog
      :modal="false"
      title="BPMN XML预览"
      :visible.sync="xmlPreviewVisible"
      width="80%"
      :close-on-click-modal="false"
    >
      <el-input
        type="textarea"
        :rows="20"
        v-model="bpmnXml"
        readonly
      ></el-input>
      <div slot="footer" class="dialog-footer">
        <el-button @click="xmlPreviewVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleCopyXml">复制</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import ElementProperties from './BpmnElementProperties.vue'

// bpmn-js 导入
// 注意：如果遇到导入错误，请确保已安装 bpmn-js: npm install bpmn-js
// 对于 bpmn-js v14，正确的导入路径是 'bpmn-js/lib/Modeler'
// 如果该路径不存在，请检查 node_modules/bpmn-js 目录结构
import BpmnModeler from 'bpmn-js/lib/Modeler'

export default {
  name: 'BpmnDesigner',
  components: {
    ElementProperties
  },
  props: {
    value: {
      type: String,
      default: ''
    },
    definitionId: {
      type: [Number, String],
      default: null
    }
  },
  data() {
    return {
      bpmnModeler: null,
      bpmnModeling: null,
      elementRegistry: null,
      selectedElement: null,
      canUndo: false,
      canRedo: false,
      bpmnXml: '',
      xmlPreviewVisible: false
    }
  },
  mounted() {
    this.initBpmnModeler()
    if (this.value) {
      this.loadBpmnXml(this.value)
    } else {
      this.createNewDiagram()
    }
  },
  beforeDestroy() {
    if (this.bpmnModeler) {
      this.bpmnModeler.destroy()
    }
  },
  methods: {
    // 初始化BPMN设计器
    initBpmnModeler() {
      if (!BpmnModeler) {
        this.$message.error('bpmn-js 未安装或导入失败，请运行: npm install bpmn-js')
        console.error('bpmn-js 未安装或导入失败')
        return
      }
      
      try {
        this.bpmnModeler = new BpmnModeler({
          container: this.$refs.bpmnCanvas,
          additionalModules: [],
          moddleExtensions: {}
        })
      } catch (e) {
        console.error('初始化 BPMN 设计器失败:', e)
        this.$message.error('初始化流程设计器失败，请检查 bpmn-js 是否正确安装')
        return
      }

      // 获取服务和注册表
      this.bpmnModeling = this.bpmnModeler.get('modeling')
      this.elementRegistry = this.bpmnModeler.get('elementRegistry')

      // 监听选择变化
      this.bpmnModeler.on('selection.changed', (e) => {
        const newElement = e.newSelection[0] || null
        this.selectedElement = newElement
      })

      // 监听元素变化
      this.bpmnModeler.on('element.changed', (e) => {
        this.emitChange()
      })

      // 监听命令栈变化（撤销/重做）
      const commandStack = this.bpmnModeler.get('commandStack')
      commandStack.on('changed', () => {
        this.canUndo = commandStack.canUndo()
        this.canRedo = commandStack.canRedo()
      })
    },

    // 创建新流程图
    createNewDiagram() {
      const xml = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn2:definitions xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" 
                   xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" 
                   xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" 
                   xmlns:di="http://www.omg.org/spec/DD/20100524/DI" 
                   id="sample-diagram" 
                   targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn2:process id="Process_1" isExecutable="true">
    <bpmn2:startEvent id="StartEvent_1"/>
  </bpmn2:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1">
        <dc:Bounds x="179" y="99" width="36" height="36"/>
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn2:definitions>`
      this.loadBpmnXml(xml)
    },

    // 加载BPMN XML
    async loadBpmnXml(xml) {
      try {
        await this.bpmnModeler.importXML(xml)
        this.$message.success('加载成功')
      } catch (err) {
        console.error('加载BPMN XML失败:', err)
        this.$message.error('加载失败: ' + err.message)
      }
    },

    // 获取BPMN XML
    async getBpmnXml() {
      try {
        const { xml } = await this.bpmnModeler.saveXML({ format: true })
        return xml
      } catch (err) {
        console.error('获取BPMN XML失败:', err)
        this.$message.error('获取XML失败: ' + err.message)
        return null
      }
    },

    // 获取BPMN JSON
    async getBpmnJson() {
      try {
        const { xml } = await this.bpmnModeler.saveXML({ format: true })
        // 将XML转换为JSON（简化处理，实际可以使用xml2js等库）
        return { xml }
      } catch (err) {
        console.error('获取BPMN JSON失败:', err)
        return null
      }
    },

    // 撤销
    handleUndo() {
      const commandStack = this.bpmnModeler.get('commandStack')
      commandStack.undo()
    },

    // 重做
    handleRedo() {
      const commandStack = this.bpmnModeler.get('commandStack')
      commandStack.redo()
    },

    // 放大
    handleZoomIn() {
      const canvas = this.bpmnModeler.get('canvas')
      const zoom = canvas.zoom()
      canvas.zoom(zoom + 0.1)
    },

    // 缩小
    handleZoomOut() {
      const canvas = this.bpmnModeler.get('canvas')
      const zoom = canvas.zoom()
      canvas.zoom(Math.max(0.1, zoom - 0.1))
    },

    // 重置缩放
    handleZoomReset() {
      const canvas = this.bpmnModeler.get('canvas')
      canvas.zoom('fit-viewport')
    },

    // 预览XML
    async handlePreview() {
      const xml = await this.getBpmnXml()
      if (xml) {
        this.bpmnXml = xml
        this.xmlPreviewVisible = true
      }
    },

    // 复制XML
    handleCopyXml() {
      const textarea = document.createElement('textarea')
      textarea.value = this.bpmnXml
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
      this.$message.success('已复制到剪贴板')
    },

    // 保存
    async handleSave() {
      const xml = await this.getBpmnXml()
      if (xml) {
        const json = await this.getBpmnJson()
        this.$emit('save', { xml, json })
        this.$message.success('保存成功')
      }
    },

    // 属性更新
    handlePropertyUpdate(updates) {
      if (!this.selectedElement) return

      const modeling = this.bpmnModeler.get('modeling')
      const elementRegistry = this.bpmnModeler.get('elementRegistry')
      const element = elementRegistry.get(this.selectedElement.id)

      // 更新元素属性
      if (updates.name !== undefined) {
        modeling.updateLabel(element, updates.name)
      }

      // 更新业务对象属性
      const businessObject = element.businessObject
      if (businessObject) {
        if (updates.assigneeType !== undefined) {
          businessObject.assigneeType = updates.assigneeType
        }
        if (updates.assigneeId !== undefined) {
          businessObject.assigneeId = updates.assigneeId
        }
        if (updates.assigneeName !== undefined) {
          businessObject.assigneeName = updates.assigneeName
        }
        if (updates.approvalType !== undefined) {
          businessObject.approvalType = updates.approvalType
        }
        if (updates.allowAddsign !== undefined) {
          businessObject.allowAddsign = updates.allowAddsign
        }
        if (updates.needPrint !== undefined) {
          businessObject.needPrint = updates.needPrint
        }
        if (updates.printOrder !== undefined) {
          businessObject.printOrder = updates.printOrder
        }
        if (updates.conditionExpression !== undefined) {
          businessObject.conditionExpression = updates.conditionExpression
        }
      }

      this.emitChange()
    },

    // 触发变化事件
    async emitChange() {
      const xml = await this.getBpmnXml()
      if (xml) {
        this.$emit('input', xml)
        this.$emit('change', xml)
      }
    }
  }
}
</script>

<style scoped>
.bpmn-designer-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f5f5;
}

.bpmn-toolbar {
  padding: 10px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.bpmn-canvas-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.bpmn-properties-panel {
  width: 300px;
  background: #fff;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.properties-header {
  padding: 15px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.properties-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.properties-content {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
}

.bpmn-canvas {
  flex: 1;
  position: relative;
}

/* BPMN.js 样式覆盖 */
.bpmn-canvas ::v-deep .djs-container {
  background: #fafafa;
}

.bpmn-canvas ::v-deep .djs-palette {
  left: 20px;
  top: 20px;
}

.bpmn-canvas ::v-deep .djs-palette .entry {
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin-bottom: 5px;
}

.bpmn-canvas ::v-deep .djs-palette .entry:hover {
  background: #f0f0f0;
}
</style>

