<template>
  <el-dialog
    title="手写签名"
    :visible.sync="visible"
    width="700px"
    :close-on-click-modal="false"
    :modal="true"
    :modal-append-to-body="true"
    :append-to-body="true"
    @close="handleClose"
  >
    <div class="signature-container">
      <div class="signature-toolbar">
        <el-button size="small" icon="el-icon-refresh-left" @click="handleUndo" :disabled="!canUndo">撤销</el-button>
        <el-button size="small" icon="el-icon-delete" @click="handleClear">清空</el-button>
        <div class="color-picker">
          <span style="margin-right: 10px;">颜色：</span>
          <el-color-picker v-model="penColor" size="small" @change="handleColorChange"></el-color-picker>
        </div>
      </div>
      <canvas
        ref="signatureCanvas"
        class="signature-canvas"
      ></canvas>
    </div>
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleConfirm" :loading="saving">确定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import SignaturePad from 'signature_pad'

export default {
  name: 'SignaturePad',
  props: {
    value: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      visible: this.value,
      signaturePad: null,
      canvas: null,
      penColor: '#000000',
      saving: false,
      canUndo: false
    }
  },
  watch: {
    value(newVal) {
      this.visible = newVal
      if (newVal) {
        this.$nextTick(() => {
          this.initCanvas()
        })
      }
    },
    visible(newVal) {
      this.$emit('input', newVal)
    }
  },
  methods: {
    initCanvas() {
      if (!this.$refs.signatureCanvas) {
        return
      }
      
      this.canvas = this.$refs.signatureCanvas
      const rect = this.canvas.getBoundingClientRect()
      const dpr = window.devicePixelRatio || 1
      
      // 设置实际尺寸
      this.canvas.width = rect.width * dpr
      this.canvas.height = 400 * dpr
      
      // 缩放上下文以匹配设备像素比
      const ctx = this.canvas.getContext('2d')
      ctx.scale(dpr, dpr)
      
      // 设置显示尺寸
      this.canvas.style.width = rect.width + 'px'
      this.canvas.style.height = '400px'
      
      // 初始化 SignaturePad（会自动处理鼠标和触摸事件）
      this.signaturePad = new SignaturePad(this.canvas, {
        backgroundColor: 'rgba(255, 255, 255, 1)',
        penColor: this.penColor,
        minWidth: 1,
        maxWidth: 3,
        throttle: 16,
        minDistance: 5
      })
      
      // 监听签名变化
      this.signaturePad.addEventListener('beginStroke', () => {
        this.canUndo = true
      })
    },
    handleColorChange(color) {
      if (this.signaturePad) {
        this.signaturePad.penColor = color
      }
    },
    handleUndo() {
      if (this.signaturePad && !this.signaturePad.isEmpty()) {
        const data = this.signaturePad.toData()
        if (data.length > 0) {
          data.pop() // 移除最后一笔
          this.signaturePad.fromData(data)
          this.canUndo = data.length > 0
        }
      }
    },
    handleClear() {
      if (this.signaturePad) {
        this.signaturePad.clear()
        this.canUndo = false
      }
    },
    handleConfirm() {
      if (!this.signaturePad || this.signaturePad.isEmpty()) {
        this.$message.warning('请先绘制签名')
        return
      }
      
      // 将画布内容转为 Base64 PNG 格式
      const base64Image = this.signaturePad.toDataURL('image/png')
      
      // 触发确认事件，传递 Base64 字符串
      this.$emit('confirm', base64Image)
    },
    handleClose() {
      this.handleClear()
      this.visible = false
      this.$emit('close')
    }
  },
  beforeDestroy() {
    if (this.signaturePad) {
      this.signaturePad.clear()
      this.signaturePad = null
    }
  }
}
</script>

<style scoped>
::v-deep .el-dialog__wrapper {
  z-index: 3000 !important;
}

::v-deep .v-modal {
  z-index: 2999 !important;
}

.signature-container {
  padding: 20px;
}

.signature-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  gap: 10px;
}

.color-picker {
  display: flex;
  align-items: center;
  margin-left: auto;
}

.signature-canvas {
  width: 100%;
  height: 400px;
  border: 2px solid #dcdfe6;
  border-radius: 4px;
  cursor: crosshair;
  background-color: #fff;
  touch-action: none;
}

.dialog-footer {
  text-align: right;
}
</style>

