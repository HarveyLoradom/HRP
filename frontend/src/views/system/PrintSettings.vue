<template>
  <div class="print-settings">
    <el-card>
      <div slot="header" class="clearfix">
        <span>打印模板管理</span>
        <div style="float: right;">
          <el-button style="padding: 3px 0; margin-right: 10px" type="text" @click="handleImport">导入</el-button>
          <el-button style="padding: 3px 0" type="text" @click="handleAdd">新增模板</el-button>
        </div>
      </div>
      
      <!-- 筛选条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="模板类型">
          <el-select v-model="searchForm.templateType" placeholder="请选择类型" clearable @change="handleSearch">
            <el-option
              v-for="option in templateTypeOptions"
              :key="option.codeValue"
              :label="option.codeName"
              :value="option.codeValue"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="paginatedData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="templateCode" label="模板编码" width="150"></el-table-column>
        <el-table-column prop="templateName" label="模板名称" width="200"></el-table-column>
        <el-table-column prop="templateType" label="模板类型" width="120">
          <template slot-scope="scope">
            {{ getTemplateTypeName(scope.row.templateType) }}
          </template>
        </el-table-column>
        <el-table-column label="是否默认" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isDefault === 1 ? 'success' : 'info'">
              {{ scope.row.isDefault === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isActive === 1 ? 'success' : 'danger'">
              {{ scope.row.isActive === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="380" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="primary" @click="handleDesign(scope.row)">设计模板</el-button>
            <el-button 
              size="mini" 
              :type="scope.row.isActive === 1 ? 'warning' : 'success'"
              @click="handleToggleActive(scope.row)"
            >
              {{ scope.row.isActive === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button size="mini" type="info" @click="handleExport(scope.row)">导出</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" style="margin-top: 20px; text-align: right;">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total">
        </el-pagination>
      </div>
    </el-card>

    <!-- 新增/编辑模板对话框 -->
    <el-dialog 
      :title="dialogTitle" 
      :visible.sync="dialogVisible" 
      width="600px" 
      :close-on-click-modal="false"
      :modal="false"
    >
      <el-form :model="form" :rules="rules" ref="form" label-width="120px" style="margin-top: 20px;">
        <el-form-item label="模板编码" prop="templateCode">
          <el-input v-model="form.templateCode" :disabled="isEdit" placeholder="请输入模板编码（唯一）"></el-input>
        </el-form-item>
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="form.templateName" placeholder="请输入模板名称"></el-input>
        </el-form-item>
        <el-form-item label="模板类型" prop="templateType">
          <el-select v-model="form.templateType" placeholder="请选择模板类型" style="width: 100%">
            <el-option
              v-for="option in templateTypeOptions"
              :key="option.codeValue"
              :label="option.codeName"
              :value="option.codeValue"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="是否默认">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="form.isActive" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="form.remark" placeholder="请输入备注"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveBasicInfo">保存</el-button>
      </div>
    </el-dialog>

    <!-- 模板设计器对话框 -->
    <el-dialog 
      title="设计模板" 
      :visible.sync="designerVisible" 
      width="95%" 
      :close-on-click-modal="false"
      :modal="false"
      top="2vh"
      custom-class="template-designer-dialog"
      @close="handleDesignerClose"
    >
      <ReportBroDesigner
        v-if="designerVisible"
        ref="reportBroDesigner"
        :template-type="currentDesignTemplate.templateType"
        :template-id="currentDesignTemplate.templateId"
        :template-json="currentDesignTemplate.templateJson"
        :page-size="currentDesignTemplate.pageSize"
        :orientation="currentDesignTemplate.orientation"
        @save="handleSaveDesign"
        style="height: 80vh;"
      />
      <div slot="footer" class="dialog-footer">
        <el-button @click="designerVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleDesignerSave">保存</el-button>
        <el-button type="success" @click="handleDesignerPreview">预览</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { 
  getPrintTemplateList, 
  getPrintTemplateByType, 
  savePrintTemplate, 
  updatePrintTemplate, 
  deletePrintTemplate,
  getPrintTemplateById,
  togglePrintTemplateActive,
  exportPrintTemplate,
  importPrintTemplate
} from '@/api/print'
import { getCodeByType } from '@/api/user'
import { paginationMixin } from '@/mixins/pagination'
import ReportBroDesigner from '@/components/ReportBroDesigner.vue'

export default {
  name: 'PrintSettings',
  mixins: [paginationMixin],
  components: {
    ReportBroDesigner
  },
  data() {
    return {
      loading: false,
      tableData: [],
      allData: [],
      templateTypeOptions: [], // 初始化模板类型选项
      searchForm: {
        templateType: ''
      },
      dialogVisible: false,
      dialogTitle: '新增模板',
      isEdit: false,
      form: {
        templateId: null,
        templateCode: '',
        templateName: '',
        templateType: '',
        templateJson: '',
        pageSize: 'A4',
        orientation: 'portrait',
        marginTop: 20,
        marginBottom: 20,
        marginLeft: 20,
        marginRight: 20,
        isDefault: 0,
        isActive: 1,
        remark: ''
      },
      rules: {
        templateCode: [{ required: true, message: '请输入模板编码', trigger: 'blur' }],
        templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
        templateType: [{ required: true, message: '请选择模板类型', trigger: 'change' }]
      },
      designerVisible: false,
      currentDesignTemplate: {
        templateId: null,
        templateType: '',
        templateXml: '',
        pageSize: 'A4',
        orientation: 'portrait',
        marginTop: 20,
        marginBottom: 20,
        marginLeft: 20,
        marginRight: 20
      }
    }
  },
  computed: {
    paginatedData() {
      const start = (this.pagination.page - 1) * this.pagination.size
      const end = start + this.pagination.size
      return this.tableData.slice(start, end)
    }
  },
  mounted() {
    this.loadTemplateTypeOptions() // 加载模板类型选项
    this.loadData()
  },
  methods: {
    loadTemplateTypeOptions() {
      // 从sys_code获取业务类型（BUSINESS_TYPE）作为模板类型
      getCodeByType('BUSINESS_TYPE').then(response => {
        if (response.code === 200) {
          this.templateTypeOptions = response.data || []
        }
      })
    },
    getTemplateTypeName(type) {
      const option = this.templateTypeOptions.find(opt => opt.codeValue === type)
      return option ? option.codeName : type
    },
    loadData() {
      this.loading = true
      const api = this.searchForm.templateType ? getPrintTemplateByType(this.searchForm.templateType) : getPrintTemplateList()
      api.then(response => {
        if (response.code === 200) {
          this.allData = response.data || []
          this.handleSearch()
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleSearch() {
      let filtered = [...this.allData]
      if (this.searchForm.templateType) {
        filtered = filtered.filter(item => item.templateType === this.searchForm.templateType)
      }
      this.tableData = filtered
      this.pagination.total = filtered.length
      this.pagination.page = 1
    },
    handleReset() {
      this.searchForm.templateType = ''
      this.pagination.page = 1
      this.loadData()
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
    },
    handleCurrentChange(val) {
      this.pagination.page = val
    },
    handleAdd() {
      this.dialogTitle = '新增模板'
      this.isEdit = false
      this.form = {
        templateId: null,
        templateCode: '',
        templateName: '',
        templateType: 'CUSTOM', // 默认类型
        templateJson: '',
        pageSize: 'A4',
        orientation: 'portrait',
        marginTop: 20,
        marginBottom: 20,
        marginLeft: 20,
        marginRight: 20,
        isDefault: 0,
        isActive: 1,
        remark: ''
      }
      this.dialogVisible = true
      this.activeTab = 'basic'
    },
    async handleEdit(row) {
      this.dialogTitle = '编辑模板'
      this.isEdit = true
      this.form = {
        templateId: row.templateId,
        templateCode: row.templateCode,
        templateName: row.templateName,
        templateType: row.templateType || 'CUSTOM',
        templateJson: row.templateJson || '',
        pageSize: row.pageSize || 'A4',
        orientation: row.orientation || 'portrait',
        marginTop: row.marginTop || 20,
        marginBottom: row.marginBottom || 20,
        marginLeft: row.marginLeft || 20,
        marginRight: row.marginRight || 20,
        isDefault: row.isDefault || 0,
        isActive: row.isActive !== undefined ? row.isActive : 1,
        remark: row.remark || ''
      }
      this.dialogVisible = true
      this.activeTab = 'basic'
    },
    handleSaveBasicInfo() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const api = this.isEdit ? updatePrintTemplate : savePrintTemplate
          api(this.form).then(response => {
            if (response.code === 200) {
              this.$message.success(response.message || '保存成功')
              this.dialogVisible = false
              this.loadData()
            } else {
              this.$message.error(response.message || '保存失败')
            }
          }).catch(error => {
            this.$message.error('保存失败: ' + (error.message || '未知错误'))
          })
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该模板吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deletePrintTemplate(row.templateId).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        })
      }).catch(() => {})
    },
    
    handleDesign(row) {
      // 打开设计器
      this.currentDesignTemplate = {
        templateId: row.templateId,
        templateType: row.templateType || 'CUSTOM',
        templateJson: row.templateJson || '',
        templateFields: row.templateFields || '',
        pageSize: row.pageSize || 'A4',
        orientation: row.orientation || 'portrait',
        marginTop: row.marginTop || 20,
        marginBottom: row.marginBottom || 20,
        marginLeft: row.marginLeft || 20,
        marginRight: row.marginRight || 20
      }
      this.designerVisible = true
    },
    
    handleDesignerClose() {
      this.designerVisible = false
      this.currentDesignTemplate = {
        templateId: null,
        templateType: '',
        templateJson: ''
      }
    },
    
    handleDesignerSave() {
      // 调用设计器的保存方法
      if (this.$refs.reportBroDesigner) {
        this.$refs.reportBroDesigner.handleSave()
      }
    },
    
    handleDesignerPreview() {
      // 调用设计器的预览方法
      if (this.$refs.reportBroDesigner) {
        this.$refs.reportBroDesigner.handlePreview()
      }
    },
    
    handleToggleActive(row) {
      const action = row.isActive === 1 ? '停用' : '启用'
      const newStatus = row.isActive === 1 ? 0 : 1
      this.$confirm(`确定要${action}该模板吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 使用专门的启用/停用API
        togglePrintTemplateActive(row.templateId, newStatus).then(response => {
          if (response.code === 200) {
            this.$message.success(`${action}成功`)
            this.loadData()
          } else {
            this.$message.error(response.message || `${action}失败`)
          }
        }).catch(error => {
          this.$message.error(`${action}失败: ` + (error.message || '未知错误'))
        })
      }).catch(() => {})
    },
    
    handleExport(row) {
      exportPrintTemplate(row.templateId).then(response => {
        // response是Blob对象
        const blob = new Blob([response], { type: 'application/json' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `${row.templateCode || 'template'}_${Date.now()}.json`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        this.$message.success('导出成功')
      }).catch(error => {
        this.$message.error('导出失败: ' + (error.message || '未知错误'))
      })
    },
    
    handleImport() {
      const input = document.createElement('input')
      input.type = 'file'
      input.accept = '.json'
      input.onchange = (e) => {
        const file = e.target.files[0]
        if (!file) return
        
        const reader = new FileReader()
        reader.onload = (event) => {
          try {
            const jsonContent = event.target.result
            // 验证JSON格式
            JSON.parse(jsonContent)
            
            // 发送导入请求
            importPrintTemplate({
              jsonContent: jsonContent,
              templateCode: null, // 让后端自动生成或从JSON中读取
              templateName: null,
              templateType: null
            }).then(response => {
              if (response.code === 200) {
                this.$message.success('导入成功')
                this.loadData()
              } else {
                this.$message.error(response.message || '导入失败')
              }
            }).catch(error => {
              this.$message.error('导入失败: ' + (error.message || '未知错误'))
            })
          } catch (error) {
            this.$message.error('文件格式错误: ' + error.message)
          }
        }
        reader.readAsText(file, 'UTF-8')
      }
      input.click()
    },
    
    handleSaveDesign(data) {
      // 保存模板JSON和页面设置到数据库
      if (!this.currentDesignTemplate.templateId) {
        this.$message.warning('模板ID不存在')
        return
      }
      
      // 获取模板的其他信息
      getPrintTemplateById(this.currentDesignTemplate.templateId).then(response => {
        if (response.code === 200 && response.data) {
          const template = response.data
          const templateData = {
            ...template,
            templateJson: typeof data === 'string' ? data : JSON.stringify(data),
            templateFields: data.fieldGroups ? JSON.stringify(data.fieldGroups) : (template.templateFields || ''),
            pageSize: data.pageSize || this.currentDesignTemplate.pageSize,
            orientation: data.orientation || this.currentDesignTemplate.orientation,
            marginTop: data.marginTop !== undefined ? data.marginTop : this.currentDesignTemplate.marginTop,
            marginBottom: data.marginBottom !== undefined ? data.marginBottom : this.currentDesignTemplate.marginBottom,
            marginLeft: data.marginLeft !== undefined ? data.marginLeft : this.currentDesignTemplate.marginLeft,
            marginRight: data.marginRight !== undefined ? data.marginRight : this.currentDesignTemplate.marginRight
          }
          
          updatePrintTemplate(templateData).then(updateResponse => {
            if (updateResponse.code === 200) {
              this.$message.success('模板保存成功')
              this.loadData()
              // 更新当前设计模板的数据
              if (typeof data === 'object') {
                Object.assign(this.currentDesignTemplate, {
                  templateJson: JSON.stringify(data),
                  templateFields: data.fieldGroups ? JSON.stringify(data.fieldGroups) : this.currentDesignTemplate.templateFields,
                  pageSize: data.pageSize,
                  orientation: data.orientation,
                  marginTop: data.marginTop,
                  marginBottom: data.marginBottom,
                  marginLeft: data.marginLeft,
                  marginRight: data.marginRight
                })
              } else {
                this.currentDesignTemplate.templateJson = data
              }
            } else {
              this.$message.error(updateResponse.message || '保存失败')
            }
          }).catch(error => {
            this.$message.error('保存失败: ' + (error.message || '未知错误'))
          })
        }
      }).catch(() => {
        this.$message.error('加载模板信息失败')
      })
    },
    
    handlePageSettingsChange(pageSettings) {
      // 更新页面设置
      Object.assign(this.currentDesignTemplate, pageSettings)
    }
  }
}
</script>

<style scoped>
.print-settings {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>
