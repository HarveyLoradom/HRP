<template>
  <el-dialog
    title="退回申请"
    :visible.sync="visible"
    width="600px"
    :close-on-click-modal="false"
    :modal="false"
    :append-to-body="true"
    @close="handleClose"
  >
    <el-form :model="returnForm" :rules="returnFormRules" ref="returnFormRef" label-width="120px">
      <el-form-item label="退回方式:">
        <el-radio-group v-model="returnForm.returnType">
          <el-radio label="RETURN_TO_CURRENT">退回后重新提交到本节点</el-radio>
          <el-radio label="RETURN_TO_START">退回后重新提交走流程</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="退回意见:" prop="opinion">
        <el-input 
          type="textarea" 
          v-model="returnForm.opinion" 
          :rows="4" 
          placeholder="请输入退回意见"
        ></el-input>
        <div style="margin-top: 10px; text-align: right;">
          <el-button size="small" @click="handleQuickOpinion('同意')">同意</el-button>
          <el-button size="small" type="danger" @click="handleQuickOpinion('不同意')">不同意</el-button>
        </div>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="danger" @click="handleConfirm" :loading="confirming">确认退回</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'RejectReturnDialog',
  props: {
    value: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      visible: this.value,
      returnForm: {
        returnType: 'RETURN_TO_CURRENT',
        opinion: ''
      },
      returnFormRules: {
        opinion: [
          { required: true, message: '退回意见不能为空', trigger: 'blur' },
          { min: 1, message: '退回意见不能为空', trigger: 'blur' }
        ]
      },
      confirming: false
    }
  },
  watch: {
    value(newVal) {
      this.visible = newVal
      if (newVal) {
        // 打开时重置状态
        this.returnForm.returnType = 'RETURN_TO_CURRENT'
        this.returnForm.opinion = ''
        this.confirming = false
        // 清除表单校验状态
        this.$nextTick(() => {
          if (this.$refs.returnFormRef) {
            this.$refs.returnFormRef.clearValidate()
          }
        })
      } else {
        // 关闭时重置状态
        this.confirming = false
        this.returnForm.opinion = ''
      }
    },
    visible(newVal) {
      this.$emit('input', newVal)
    }
  },
  methods: {
    handleQuickOpinion(text) {
      this.returnForm.opinion = text
    },
    handleConfirm() {
      // 表单校验
      this.$refs.returnFormRef.validate((valid) => {
        if (valid) {
          this.confirming = true
          // 传递退回方式和意见
          const data = {
            returnType: this.returnForm.returnType,
            opinion: this.returnForm.opinion
          }
          this.$emit('confirm', data)
          // 延迟重置confirming状态，给父组件时间处理
          setTimeout(() => {
            this.confirming = false
          }, 1000)
        }
      })
    },
    handleClose() {
      this.visible = false
      this.returnForm.opinion = ''
      this.confirming = false
      this.$emit('close')
    }
  }
}
</script>

<style scoped>
</style>

