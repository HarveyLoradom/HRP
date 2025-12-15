<template>
  <div class="print-preview-page">
    <el-card>
      <div slot="header" class="clearfix">
        <span>打印预览</span>
      </div>
      
      <!-- 参数填写表单 -->
      <el-form :model="printForm" :rules="printRules" ref="printForm" label-width="120px" class="print-form">
        <el-form-item label="模板类型" prop="templateType">
          <el-select v-model="printForm.templateType" placeholder="请选择模板类型" style="width: 100%" @change="handleTemplateTypeChange">
            <el-option label="申请单" value="APPLY"></el-option>
            <el-option label="报账单" value="PAYOUT"></el-option>
            <el-option label="合同" value="CONTRACT"></el-option>
            <el-option label="资产审批" value="ASSET"></el-option>
            <el-option label="采购审批" value="PROCUREMENT"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="选择模板" prop="templateId">
          <el-select v-model="printForm.templateId" placeholder="请先选择模板类型" style="width: 100%" :disabled="!printForm.templateType">
            <el-option 
              v-for="template in templateList" 
              :key="template.templateId" 
              :label="template.templateName" 
              :value="template.templateId">
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="业务主键" prop="businessKey">
          <el-input v-model="printForm.businessKey" placeholder="请输入业务主键（如报销单号）"></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handlePreview" :loading="previewLoading">预览</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- PDF预览区域 -->
    <el-card v-if="pdfUrl" style="margin-top: 20px;">
      <div slot="header" class="clearfix">
        <span>PDF预览</span>
        <div style="float: right;">
          <el-button type="primary" icon="el-icon-printer" @click="handlePrint">打印</el-button>
          <el-button type="success" icon="el-icon-download" @click="handleDownload">下载PDF</el-button>
        </div>
      </div>
      
      <div class="pdf-preview-container" v-loading="previewLoading">
        <iframe 
          v-if="pdfUrl" 
          :src="pdfUrl" 
          class="pdf-preview-iframe"
          frameborder="0"
        ></iframe>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getPrintTemplateByType, generatePrintContent } from '@/api/print'

export default {
  name: 'PrintPreview',
  data() {
    return {
      printForm: {
        templateType: '',
        templateId: null,
        businessKey: ''
      },
      printRules: {
        templateType: [{ required: true, message: '请选择模板类型', trigger: 'change' }],
        templateId: [{ required: true, message: '请选择模板', trigger: 'change' }],
        businessKey: [{ required: true, message: '请输入业务主键', trigger: 'blur' }]
      },
      templateList: [],
      pdfUrl: null,
      previewLoading: false
    }
  },
  methods: {
    handleTemplateTypeChange() {
      // 清空模板选择
      this.printForm.templateId = null
      this.templateList = []
      
      if (this.printForm.templateType) {
        // 加载该类型下的模板列表
        getPrintTemplateByType(this.printForm.templateType).then(response => {
          if (response.code === 200 && response.data) {
            // 优先显示默认模板
            const defaultTemplate = response.data.find(t => t.isDefault === 1)
            if (defaultTemplate) {
              this.templateList = response.data
              this.printForm.templateId = defaultTemplate.templateId
            } else {
              this.templateList = response.data
              if (response.data.length > 0) {
                this.printForm.templateId = response.data[0].templateId
              }
            }
          }
        }).catch(() => {
          this.$message.error('加载模板列表失败')
        })
      }
    },
    
    async handlePreview() {
      this.$refs.printForm.validate(async (valid) => {
        if (!valid) {
          return false
        }
        
        this.previewLoading = true
        this.pdfUrl = null
        
        try {
          // 调用后端生成打印内容
          const response = await generatePrintContent({
            templateId: this.printForm.templateId,
            businessKey: this.printForm.businessKey,
            templateType: this.printForm.templateType
          })
          
          if (response.code === 200 && response.data) {
            // 创建 Blob URL 用于预览
            const htmlContent = typeof response.data === 'string' ? response.data : String(response.data)
            const blob = new Blob([htmlContent], { type: 'text/html;charset=utf-8' })
            const url = window.URL.createObjectURL(blob)
            this.pdfUrl = url
            this.$message.success('预览生成成功')
          } else {
            this.$message.error(response.message || '预览生成失败')
          }
        } catch (error) {
          console.error('预览失败:', error)
          this.$message.error('预览失败: ' + (error.message || '未知错误'))
        } finally {
          this.previewLoading = false
        }
      })
    },
    
    handlePrint() {
      if (!this.pdfUrl) {
        this.$message.warning('请先预览PDF')
        return
      }
      
      // 打开新窗口打印
      const printWindow = window.open(this.pdfUrl, '_blank')
      if (printWindow) {
        printWindow.onload = () => {
          setTimeout(() => {
            printWindow.print()
          }, 500)
        }
      } else {
        this.$message.error('无法打开打印窗口，请检查浏览器设置')
      }
    },
    
    async handleDownload() {
      if (!this.pdfUrl) {
        this.$message.warning('请先预览PDF')
        return
      }
      
      try {
        // 重新生成打印内容并下载
        const response = await generatePrintContent({
          templateId: this.printForm.templateId,
          businessKey: this.printForm.businessKey,
          templateType: this.printForm.templateType
        })
        
        if (response.code === 200 && response.data) {
          const htmlContent = typeof response.data === 'string' ? response.data : String(response.data)
          const blob = new Blob([htmlContent], { type: 'text/html;charset=utf-8' })
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.download = `report_${this.printForm.businessKey}_${Date.now()}.html`
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(url)
          
          this.$message.success('下载成功')
        } else {
          this.$message.error(response.message || '下载失败')
        }
      } catch (error) {
        console.error('下载失败:', error)
        this.$message.error('下载失败: ' + (error.message || '未知错误'))
      }
    },
    
    handleReset() {
      this.$refs.printForm.resetFields()
      this.pdfUrl = null
      this.templateList = []
    }
  },
  
  beforeDestroy() {
    // 清理 Blob URL
    if (this.pdfUrl) {
      window.URL.revokeObjectURL(this.pdfUrl)
    }
  }
}
</script>

<style scoped>
.print-preview-page {
  padding: 20px;
}

.print-form {
  max-width: 600px;
}

.pdf-preview-container {
  width: 100%;
  height: 800px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.pdf-preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}
</style>

