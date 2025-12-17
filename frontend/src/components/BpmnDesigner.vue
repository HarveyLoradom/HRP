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

// 导入 bpmn-js 样式文件（必需，否则组件无法显示）
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-js.css'
// 尝试导入 bpmn-js 字体文件（如果路径存在）
// 注意：bpmn-js v14 的字体文件路径可能不同，如果导入失败，请检查实际路径
try {
  require('bpmn-js/dist/assets/bpmn-font/css/bpmn.css')
  require('bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css')
  require('bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css')
} catch (e) {
  console.warn('bpmn-font CSS 文件导入失败，图标可能无法显示:', e)
  // 尝试其他可能的路径
  try {
    require('bpmn-js/dist/assets/bpmn-font/bpmn.css')
  } catch (e2) {
    console.warn('备用路径也失败:', e2)
  }
}

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
    // 等待 DOM 渲染完成后再初始化
    this.$nextTick(() => {
      setTimeout(() => {
        this.initBpmnModeler()
        if (this.value) {
          this.loadBpmnXml(this.value)
        } else {
          this.createNewDiagram()
        }
      }, 100) // 延迟一点确保容器完全渲染
    })
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
        // 确保容器元素存在
        if (!this.$refs.bpmnCanvas) {
          console.error('画布容器元素不存在')
          this.$message.error('画布容器未找到，请刷新页面重试')
          return
        }
        
        this.bpmnModeler = new BpmnModeler({
          container: this.$refs.bpmnCanvas,
          additionalModules: [],
          moddleExtensions: {}
        })
        
        // 监听错误事件
        this.bpmnModeler.on('error', (e) => {
          console.error('BPMN 设计器错误:', e)
          if (e.error) {
            this.$message.error('设计器错误: ' + (e.error.message || e.error))
          }
        })
        
        // 监听导入完成事件，确保 palette 正确显示
        this.bpmnModeler.on('import.done', () => {
          console.log('BPMN 导入完成')
          this.$nextTick(() => {
            // 确保 palette 可见
            const palette = this.bpmnModeler.get('palette')
            if (palette) {
              console.log('Palette 已初始化')
              // 强制显示 palette
              const paletteElement = document.querySelector('.djs-palette')
              if (paletteElement) {
                paletteElement.style.display = 'block'
                paletteElement.style.visibility = 'visible'
                paletteElement.style.opacity = '1'
              }
            } else {
              console.warn('Palette 未找到')
            }
            
            // 在导入完成后初始化 commandStack 监听
            this.initCommandStackListener()
          })
        })
      } catch (e) {
        console.error('初始化 BPMN 设计器失败:', e)
        this.$message.error('初始化流程设计器失败: ' + (e.message || '未知错误'))
        return
      }

      // 获取服务和注册表
      this.bpmnModeling = this.bpmnModeler.get('modeling')
      this.elementRegistry = this.bpmnModeler.get('elementRegistry')

      // 监听选择变化
      this.bpmnModeler.on('selection.changed', (e) => {
        const newElement = e.newSelection[0] || null
        this.selectedElement = newElement
        // 确保属性面板能正确更新
        this.$nextTick(() => {
          if (newElement) {
            console.log('选中元素:', newElement.id, newElement.type)
          }
        })
      })

      // 监听元素变化
      this.bpmnModeler.on('element.changed', (e) => {
        this.emitChange()
      })
    },
    
    // 初始化命令栈监听（需要在导入完成后调用）
    initCommandStackListener() {
      try {
        const commandStack = this.bpmnModeler.get('commandStack')
        if (commandStack && typeof commandStack.on === 'function') {
          commandStack.on('changed', () => {
            this.canUndo = commandStack.canUndo()
            this.canRedo = commandStack.canRedo()
          })
          // 初始化当前状态
          this.canUndo = commandStack.canUndo()
          this.canRedo = commandStack.canRedo()
        } else {
          console.warn('commandStack 不可用或 API 不同')
        }
      } catch (e) {
        console.error('初始化 commandStack 监听失败:', e)
      }
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

    // 加载BPMN XML（公开方法，供外部调用）
    async loadBpmnXml(xml) {
      try {
        await this.bpmnModeler.importXML(xml)
        // 确保画布正确显示
        this.$nextTick(() => {
          setTimeout(() => {
            try {
              const canvas = this.bpmnModeler.get('canvas')
              if (canvas) {
                // 检查画布尺寸是否有效
                const container = canvas._container
                if (container && container.clientWidth > 0 && container.clientHeight > 0) {
                  canvas.zoom('fit-viewport')
                } else {
                  // 如果容器尺寸无效，延迟重试
                  setTimeout(() => {
                    if (canvas && canvas._container && canvas._container.clientWidth > 0) {
                      canvas.zoom('fit-viewport')
                    }
                  }, 200)
                }
              }
            } catch (zoomError) {
              console.warn('缩放失败，但继续执行:', zoomError)
            }
            // 加载完成后初始化 commandStack 监听
            this.initCommandStackListener()
          }, 100)
        })
      } catch (err) {
        console.error('加载BPMN XML失败:', err)
        this.$message.error('加载失败: ' + (err.message || '未知错误'))
        // 如果加载失败，尝试创建新图表
        if (!this.value) {
          this.createNewDiagram()
        }
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
      try {
        const canvas = this.bpmnModeler.get('canvas')
        if (!canvas) return
        const zoom = canvas.zoom()
        if (isFinite(zoom) && zoom > 0) {
          const newZoom = Math.min(3, zoom + 0.1)
          canvas.zoom(newZoom)
        }
      } catch (e) {
        console.error('放大失败:', e)
      }
    },

    // 缩小
    handleZoomOut() {
      try {
        const canvas = this.bpmnModeler.get('canvas')
        if (!canvas) return
        const zoom = canvas.zoom()
        if (isFinite(zoom) && zoom > 0) {
          const newZoom = Math.max(0.1, zoom - 0.1)
          canvas.zoom(newZoom)
        }
      } catch (e) {
        console.error('缩小失败:', e)
      }
    },

    // 重置缩放
    handleZoomReset() {
      try {
        const canvas = this.bpmnModeler.get('canvas')
        if (!canvas) return
        const container = canvas._container
        if (container && container.clientWidth > 0 && container.clientHeight > 0) {
          canvas.zoom('fit-viewport')
        }
      } catch (e) {
        console.error('重置缩放失败:', e)
      }
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
      if (!this.selectedElement) {
        console.warn('没有选中的元素，无法更新属性')
        return
      }

      try {
        const modeling = this.bpmnModeler.get('modeling')
        const elementRegistry = this.bpmnModeler.get('elementRegistry')
        const moddle = this.bpmnModeler.get('moddle')
        const element = elementRegistry.get(this.selectedElement.id)

        if (!element) {
          console.warn('无法找到元素:', this.selectedElement.id)
          return
        }

        // 更新名称（标签）
        if (updates.name !== undefined && updates.name !== null) {
          modeling.updateLabel(element, updates.name)
        }

        // 准备要更新的属性对象
        const propertiesToUpdate = {}

      // 用户任务节点属性
      if (element.type && element.type.includes('UserTask')) {
        if (updates.approvalType !== undefined) {
          propertiesToUpdate.approvalType = updates.approvalType
        }
        if (updates.assigneeType !== undefined) {
          propertiesToUpdate.assigneeType = updates.assigneeType
        }
        if (updates.assigneeId !== undefined) {
          propertiesToUpdate.assigneeId = updates.assigneeId
        }
        if (updates.assigneeName !== undefined) {
          propertiesToUpdate.assigneeName = updates.assigneeName
        }
        if (updates.positionCode !== undefined) {
          propertiesToUpdate.positionCode = updates.positionCode
        }
        if (updates.deptCode !== undefined) {
          propertiesToUpdate.deptCode = updates.deptCode
        }
        // 会签配置
        if (updates.multiInstanceType !== undefined) {
          propertiesToUpdate.multiInstanceType = updates.multiInstanceType
        }
        if (updates.multiInstanceCount !== undefined) {
          propertiesToUpdate.multiInstanceCount = updates.multiInstanceCount
        }
        if (updates.completionCondition !== undefined) {
          propertiesToUpdate.completionCondition = updates.completionCondition
        }
        // 高级配置
        if (updates.allowAddsign !== undefined) {
          propertiesToUpdate.allowAddsign = updates.allowAddsign
        }
        if (updates.allowReduceSign !== undefined) {
          propertiesToUpdate.allowReduceSign = updates.allowReduceSign
        }
        if (updates.allowTransfer !== undefined) {
          propertiesToUpdate.allowTransfer = updates.allowTransfer
        }
        if (updates.allowReject !== undefined) {
          propertiesToUpdate.allowReject = updates.allowReject
        }
        if (updates.rejectStrategy !== undefined) {
          propertiesToUpdate.rejectStrategy = updates.rejectStrategy
        }
        if (updates.needPrint !== undefined) {
          propertiesToUpdate.needPrint = updates.needPrint
        }
        if (updates.printOrder !== undefined) {
          propertiesToUpdate.printOrder = updates.printOrder
        }
        // 超时设置
        if (updates.enableTimeout !== undefined) {
          propertiesToUpdate.enableTimeout = updates.enableTimeout
        }
        if (updates.timeoutHours !== undefined) {
          propertiesToUpdate.timeoutHours = updates.timeoutHours
        }
        if (updates.timeoutAction !== undefined) {
          propertiesToUpdate.timeoutAction = updates.timeoutAction
        }
        if (updates.description !== undefined) {
          propertiesToUpdate.description = updates.description
        }
      }

      // 网关节点属性
      if (element.type && (element.type.includes('Gateway') || element.type.includes('ExclusiveGateway') || element.type.includes('ParallelGateway'))) {
        if (updates.gatewayType !== undefined) {
          propertiesToUpdate.gatewayType = updates.gatewayType
        }
        if (updates.description !== undefined) {
          propertiesToUpdate.description = updates.description
        }
      }

      // 连线条件属性
      if (element.type && element.type.includes('SequenceFlow')) {
        if (updates.conditionType !== undefined) {
          propertiesToUpdate.conditionType = updates.conditionType
        }
        if (updates.conditionExpression !== undefined) {
          // 如果有条件表达式，需要创建 FormalExpression 对象
          if (updates.conditionExpression && updates.conditionType !== 'none') {
            const conditionExpression = moddle.create('bpmn:FormalExpression', {
              body: updates.conditionExpression
            })
            propertiesToUpdate.conditionExpression = conditionExpression
          } else if (updates.conditionType === 'none') {
            // 移除条件表达式
            propertiesToUpdate.conditionExpression = null
          }
        }
      }

      // 通用属性
      if (updates.description !== undefined && !propertiesToUpdate.description) {
        propertiesToUpdate.description = updates.description
      }

      // 使用 modeling.updateProperties 更新属性
      if (Object.keys(propertiesToUpdate).length > 0) {
        try {
          modeling.updateProperties(element, propertiesToUpdate)
          console.log('属性更新成功:', propertiesToUpdate)
        } catch (error) {
          console.error('属性更新失败:', error)
          this.$message.error('属性更新失败: ' + (error.message || '未知错误'))
        }
      }

      // 触发变化事件
      this.$nextTick(() => {
        this.emitChange()
      })
      } catch (error) {
        console.error('属性更新处理失败:', error)
        this.$message.error('属性更新失败: ' + (error.message || '未知错误'))
      }
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
  position: relative;
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
  min-height: 500px;
  width: 100%;
  height: 100%;
}

/* BPMN.js 样式覆盖 */
.bpmn-canvas ::v-deep .djs-container {
  background: #fafafa;
  width: 100% !important;
  height: 100% !important;
}

.bpmn-canvas ::v-deep .djs-palette {
  left: 20px !important;
  top: 20px !important;
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
  z-index: 1000;
  position: absolute !important;
}

/* 确保画布区域可见 */
.bpmn-canvas ::v-deep .djs-canvas {
  width: 100% !important;
  height: 100% !important;
}

.bpmn-canvas ::v-deep .djs-palette .entry {
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin-bottom: 5px;
  width: 46px;
  height: 46px;
  display: flex !important;
  align-items: center;
  justify-content: center;
  cursor: grab !important;
  pointer-events: auto !important;
  user-select: none;
  font-size: 20px !important;
  color: #333 !important;
}

.bpmn-canvas ::v-deep .djs-palette .entry:hover {
  background: #f0f0f0;
}

.bpmn-canvas ::v-deep .djs-palette .entry:active {
  cursor: grabbing !important;
}

/* 确保图标字体正确加载 */
.bpmn-canvas ::v-deep .djs-palette .entry .djs-icon {
  font-family: 'bpmn', 'bpmn-codes', 'bpmn-embedded' !important;
  font-size: 20px !important;
  line-height: 1 !important;
}

.bpmn-canvas ::v-deep .djs-palette .entry .djs-palette-icon {
  width: 24px;
  height: 24px;
}

/* 确保画布可以接收拖放 */
.bpmn-canvas ::v-deep .djs-element {
  pointer-events: auto !important;
}

.bpmn-canvas ::v-deep .djs-canvas {
  position: relative !important;
  pointer-events: auto !important;
}
</style>

