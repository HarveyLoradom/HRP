<template>
  <el-dialog
    title="审批确认"
    :visible.sync="visible"
    width="600px"
    :close-on-click-modal="false"
    :modal="false"
    :append-to-body="true"
    @close="handleClose"
  >
    <div class="approval-confirm-content">
      <!-- 下一节点信息 -->
      <div class="next-node-info" v-if="nextNodeInfo">
        <el-divider content-position="left">下一审批节点信息</el-divider>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="节点名称">
            {{ nextNodeInfo.taskName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="审批人">
            {{ nextNodeInfo.approverList || nextNodeInfo.approverName || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 审批意见 -->
      <div class="approval-opinion" style="margin-top: 20px;">
        <el-divider content-position="left">审批意见</el-divider>
        <el-input
          v-model="approvalForm.opinion"
          type="textarea"
          :rows="3"
          placeholder="请输入审批意见"
        ></el-input>
        <div style="margin-top: 10px; text-align: right;">
          <el-button size="small" @click="handleQuickOpinion('同意')">同意</el-button>
          <el-button size="small" type="danger" @click="handleQuickOpinion('不同意')">不同意</el-button>
        </div>
      </div>

      <!-- 手写签名（仅在开启签名认证时显示） -->
      <div class="signature-display" style="margin-top: 20px;" v-if="enableSignatureAuth && signatureUrl">
        <el-divider content-position="left">手写签名</el-divider>
        <div style="text-align: center; padding: 20px; background-color: #f5f7fa; border-radius: 4px;">
          <img 
            :src="signatureUrl" 
            alt="手写签名" 
            style="max-width: 100%; max-height: 150px; border: 1px solid #dcdfe6; background-color: #fff;"
          />
        </div>
      </div>
      <div v-else-if="enableSignatureAuth && !signatureUrl" style="margin-top: 20px; text-align: center; color: #909399; padding: 20px;">
        <i class="el-icon-warning"></i>
        <span>未找到手写签名，请前往个人设置上传签名</span>
      </div>
    </div>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleConfirm" :loading="confirming">{{ confirmButtonText }}</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { getCurrentUserSignatureImage, getCodeByCodeName } from '@/api/user'

export default {
  name: 'ApprovalConfirmDialog',
  props: {
    value: {
      type: Boolean,
      default: false
    },
    nextNodeInfo: {
      type: Object,
      default: null
    },
    confirmButtonText: {
      type: String,
      default: '确认提交'
    }
  },
  data() {
    return {
      visible: this.value,
      approvalForm: {
        opinion: ''
      },
      signatureUrl: '',
      signatureBase64: '', // 存储签名的Base64数据
      enableSignatureAuth: false, // 是否开启签名认证
      confirming: false
    }
  },
  watch: {
    value(newVal) {
      this.visible = newVal
      if (newVal) {
        this.loadSignatureAuthConfig()
        this.loadSignature()
        // 重置状态
        this.confirming = false
        this.approvalForm.opinion = ''
      } else {
        // 关闭时重置状态
        this.confirming = false
        this.approvalForm.opinion = ''
        this.signatureBase64 = ''
      }
    },
    visible(newVal) {
      this.$emit('input', newVal)
    }
  },
  methods: {
    async loadSignatureAuthConfig() {
      try {
        // 查询系统参数：是否开启签名认证（使用codeName查询，参考重置密码的方式）
        const response = await getCodeByCodeName('是否开启签名认证')
        if (response.code === 200 && response.data) {
          this.enableSignatureAuth = response.data.codeValue === '是'
        } else {
          this.enableSignatureAuth = false
        }
      } catch (error) {
        console.error('加载签名认证配置失败', error)
        this.enableSignatureAuth = false
      }
    },
    async loadSignature() {
      try {
        const response = await getCurrentUserSignatureImage()
        if (response.code === 200 && response.data && response.data.imageUrl) {
          // imageUrl是data URL格式（data:image/png;base64,xxx），可以直接用于显示和保存
          this.signatureUrl = response.data.imageUrl
          this.signatureBase64 = response.data.imageUrl
        } else {
          this.signatureUrl = ''
          this.signatureBase64 = ''
        }
      } catch (error) {
        console.error('加载签名失败', error)
        this.signatureUrl = ''
        this.signatureBase64 = ''
      }
    },
    handleQuickOpinion(text) {
      this.approvalForm.opinion = text
    },
    handleConfirm() {
      if (!this.approvalForm.opinion || this.approvalForm.opinion.trim() === '') {
        this.$message.warning('请输入审批意见')
        return
      }
      // 如果开启签名认证，但没有签名，提示用户
      if (this.enableSignatureAuth && (!this.signatureBase64 || this.signatureBase64.trim() === '')) {
        this.$message.warning('请先上传手写签名')
        return
      }
      this.confirming = true
      // 传递意见和签名（如果开启签名认证）
      const data = {
        opinion: this.approvalForm.opinion,
        signature: this.enableSignatureAuth ? this.signatureBase64 : null
      }
      this.$emit('confirm', data)
      // 延迟重置confirming状态，给父组件时间处理
      // 如果父组件处理成功会关闭对话框，如果失败则重置状态
      setTimeout(() => {
        this.confirming = false
      }, 1000)
    },
    handleClose() {
      this.visible = false
      this.approvalForm.opinion = ''
      this.signatureBase64 = ''
      this.confirming = false // 重置加载状态
      this.$emit('close')
    }
  }
}
</script>

<style scoped>
.approval-confirm-content {
  padding: 10px 0;
}
</style>

