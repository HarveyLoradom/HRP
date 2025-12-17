<template>
  <div class="process-definition">
    <el-card>
      <div slot="header" class="clearfix">
        <span>流程定义</span>
        <div style="float: right;">
          <el-button size="small" type="primary" @click="handleImport" style="margin-right: 10px;">导入</el-button>
          <el-button size="small" type="text" @click="handleAdd">新增流程</el-button>
        </div>
      </div>
      
      <!-- 筛选条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="流程类型">
          <el-select v-model="searchForm.definitionType" placeholder="请选择类型" clearable @change="handleSearch">
            <el-option
              v-for="option in processTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="definitionKey" label="流程编码" width="200">
          <template slot-scope="scope">
            {{ scope.row.definitionKey || scope.row.definitionCode }}
          </template>
        </el-table-column>
        <el-table-column prop="definitionName" label="流程名称" width="200"></el-table-column>
        <el-table-column prop="definitionType" label="流程类型" width="120">
          <template slot-scope="scope">
            {{ getProcessTypeName(scope.row.definitionType) }}
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80"></el-table-column>
        <el-table-column label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isActive === 1 ? 'success' : 'danger'">
              {{ scope.row.isActive === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="400" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="primary" @click="handleDesign(scope.row)">设计流程</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px" :modal="false">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="流程编码" prop="definitionCode">
          <el-input v-model="form.definitionCode" :disabled="isEdit" placeholder="请输入流程编码（如：PAYOUT_APPROVAL）"></el-input>
        </el-form-item>
        <el-form-item label="流程名称" prop="definitionName">
          <el-input v-model="form.definitionName" placeholder="请输入流程名称"></el-input>
        </el-form-item>
        <el-form-item label="模板类型" prop="definitionType">
          <el-select v-model="form.definitionType" placeholder="请选择模板类型" @change="handleDefinitionTypeChange">
            <el-option
              v-for="option in processTypeOptions"
              :key="option.codeValue"
              :label="option.codeName"
              :value="option.codeValue"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="业务类型" prop="businessType" v-if="businessTypeOptions.length > 0">
          <el-select v-model="form.businessType" placeholder="请选择业务类型" style="width: 100%">
            <el-option
              v-for="option in businessTypeOptions"
              :key="option.codeValue"
              :label="option.codeName"
              :value="option.codeValue"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>

    <!-- 流程设计对话框 -->
    <el-dialog
      title="流程设计"
      :visible.sync="designDialogVisible"
      width="95%"
      :close-on-click-modal="false"
      :modal="false"
      top="5vh"
      custom-class="process-design-dialog"
      :before-close="handleCancelDesign"
    >
      <div class="process-designer-wrapper" style="height: 75vh;">
        <BpmnDesigner 
          ref="bpmnDesigner"
          :value="designForm.processXml"
          :definition-id="designForm.definitionId"
          @save="handleBpmnSave"
        />
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleCancelDesign">取消</el-button>
        <el-button type="primary" @click="handleSaveDesign">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { 
  getProcessDefinitionPage, 
  getProcessDefinitionByTypePage,
  getProcessDefinitionById,
  saveProcessDefinition, 
  updateProcessDefinition, 
  deleteProcessDefinition,
  toggleProcessDefinitionActive,
  exportProcessDefinition,
  importProcessDefinition
} from '@/api/process'
import { getCodeByType } from '@/api/user'
import BpmnDesigner from '@/components/BpmnDesigner.vue'
import { paginationMixin } from '@/mixins/pagination'

export default {
  name: 'ProcessDefinition',
  components: {
    BpmnDesigner
  },
  mixins: [paginationMixin],
  data() {
    return {
      loading: false,
      tableData: [],
      allData: [],
      processTypeOptions: [],
      businessTypeOptions: [], // 业务类型选项（根据流程类型动态加载）
      searchForm: {
        definitionType: ''
      },
      dialogVisible: false,
      dialogTitle: '新增流程',
      isEdit: false,
      form: {
        definitionId: null,
        definitionCode: '',
        definitionName: '',
        definitionType: '',
        businessType: '', // 业务类型
        description: '',
        processJson: '',
        version: 1,
        isActive: 1
      },
      rules: {
        definitionCode: [{ required: true, message: '请输入流程编码', trigger: 'blur' }],
        definitionName: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
        definitionType: [{ required: true, message: '请选择模板类型', trigger: 'change' }]
      },
      designDialogVisible: false,
      designForm: {
        definitionId: null,
        processXml: ''
      },
      originalProcessXml: '' // 保存原始XML，用于取消时恢复
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
    this.loadProcessTypeOptions()
    this.loadData()
  },
  methods: {
    async loadProcessTypeOptions() {
      // 从sys_code获取业务类型（BUSINESS_TYPE）作为模板类型
      getCodeByType('BUSINESS_TYPE').then(response => {
        if (response.code === 200) {
          this.processTypeOptions = response.data || []
        }
      })
    },
    getProcessTypeName(codeValue) {
      const option = this.processTypeOptions.find(item => item.codeValue === codeValue)
      return option ? option.codeName : codeValue
    },
    // 根据流程类型加载业务类型选项
    handleDefinitionTypeChange(definitionType) {
      this.form.businessType = '' // 清空业务类型
      this.businessTypeOptions = [] // 清空选项
      
      if (!definitionType) {
        return
      }
      
      // 根据流程类型拼接业务类型 code_type（如：PAYOUT -> PAYOUT_TYPE）
      const businessTypeCode = `${definitionType}_TYPE`
      
      // 查询对应的业务类型选项
      getCodeByType(businessTypeCode).then(response => {
        if (response.code === 200 && response.data) {
          this.businessTypeOptions = response.data || []
        }
      }).catch(error => {
        console.error('加载业务类型失败:', error)
        this.businessTypeOptions = []
      })
    },
    loadData() {
      this.loading = true
      const api = this.searchForm.definitionType 
        ? getProcessDefinitionByTypePage(this.searchForm.definitionType, this.pagination.page, this.pagination.size)
        : getProcessDefinitionPage(this.pagination.page, this.pagination.size)
      api.then(response => {
        if (response.code === 200 && response.data) {
          this.tableData = response.data.records || []
          this.pagination.total = response.data.total || 0
        } else {
          this.tableData = []
          this.pagination.total = 0
        }
        this.loading = false
      }).catch(() => {
        this.tableData = []
        this.pagination.total = 0
        this.loading = false
      })
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm.definitionType = ''
      this.pagination.page = 1
      this.loadData()
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
      this.loadData()
    },
    handleCurrentChange(val) {
      this.pagination.page = val
      this.loadData()
    },
    handleAdd() {
      this.dialogTitle = '新增流程'
      this.isEdit = false
      this.form = {
        definitionId: null,
        definitionCode: '',
        definitionName: '',
        definitionType: '',
        businessType: '',
        description: '',
        processJson: '',
        version: 1,
        isActive: 1
      }
      this.businessTypeOptions = [] // 清空业务类型选项
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑流程'
      this.isEdit = true
      this.form = {
        definitionId: row.definitionId,
        definitionCode: row.definitionCode || row.definitionKey || '',
        definitionName: row.definitionName,
        definitionType: row.definitionType,
        businessType: row.businessType || '',
        description: row.description || '',
        processJson: row.processJson || '',
        version: row.version,
        isActive: row.isActive
      }
      // 如果已有流程类型，加载对应的业务类型选项
      if (row.definitionType) {
        this.handleDefinitionTypeChange(row.definitionType)
      } else {
        this.businessTypeOptions = []
      }
      this.dialogVisible = true
    },
    async handleDesign(row) {
      // 从后端重新获取最新数据，确保显示的是最新版本
      try {
        const response = await getProcessDefinitionById(row.definitionId)
        if (response.code === 200 && response.data) {
          const latestData = response.data
          // 保存原始XML，用于取消时恢复
          this.originalProcessXml = latestData.processXml || ''
          this.designForm = {
            definitionId: latestData.definitionId,
            processXml: latestData.processXml || ''
          }
        } else {
          // 如果获取失败，使用表格中的数据
          this.originalProcessXml = row.processXml || ''
          this.designForm = {
            definitionId: row.definitionId,
            processXml: row.processXml || ''
          }
        }
        this.designDialogVisible = true
      } catch (error) {
        console.error('获取流程定义详情失败:', error)
        // 如果获取失败，使用表格中的数据
        this.originalProcessXml = row.processXml || ''
        this.designForm = {
          definitionId: row.definitionId,
          processXml: row.processXml || ''
        }
        this.designDialogVisible = true
      }
    },
    
    handleBpmnSave(data) {
      // 保存BPMN设计器的数据
      if (!this.designForm.definitionId) {
        this.$message.error('流程定义ID不能为空')
        return
      }
      updateProcessDefinition({
        definitionId: this.designForm.definitionId,
        processXml: data.xml,
        processJson: data.json ? JSON.stringify(data.json) : ''
      }).then(response => {
        if (response.code === 200) {
          this.$message.success('保存成功')
          // 更新本地数据，确保下次打开时显示最新内容
          this.designForm.processXml = data.xml
          this.originalProcessXml = data.xml
          // 更新表格中对应行的数据
          const row = this.tableData.find(item => item.definitionId === this.designForm.definitionId)
          if (row) {
            row.processXml = data.xml
          }
          // 重新加载列表数据
          this.loadData()
        } else {
          this.$message.error(response.message || '保存失败')
        }
      }).catch(error => {
        console.error('保存失败:', error)
        this.$message.error('保存失败，请重试')
      })
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const api = this.isEdit ? updateProcessDefinition : saveProcessDefinition
          // 将 definitionCode 映射为 definitionKey 以兼容后端
          const submitData = {
            ...this.form,
            definitionKey: this.form.definitionCode
          }
          api(submitData).then(response => {
            if (response.code === 200) {
              this.$message.success(response.message || '操作成功')
              this.dialogVisible = false
              this.loadData()
            } else {
              this.$message.error(response.message || '操作失败')
            }
          })
        }
      })
    },
    handleSaveDesign() {
      // 调用设计器的保存方法
      if (this.$refs.bpmnDesigner && typeof this.$refs.bpmnDesigner.handleSave === 'function') {
        this.$refs.bpmnDesigner.handleSave()
      } else {
        this.$message.warning('设计器未初始化，请稍候再试')
      }
    },
    handleCancelDesign() {
      // 取消时恢复原始XML并关闭对话框
      if (this.$refs.bpmnDesigner && this.originalProcessXml !== undefined) {
        // 重新加载原始XML，撤销所有未保存的更改
        this.$refs.bpmnDesigner.loadBpmnXml(this.originalProcessXml)
      }
      this.designDialogVisible = false
      // 清空原始XML
      this.originalProcessXml = ''
    },
    handleDelete(row) {
      this.$confirm('确定要删除该流程定义吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteProcessDefinition(row.definitionId).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        })
      }).catch(() => {})
    },
    handleImport() {
      // 创建文件输入元素
      const input = document.createElement('input')
      input.type = 'file'
      input.accept = '.xml,.bpmn'
      input.onchange = (e) => {
        const file = e.target.files[0]
        if (!file) {
          return
        }
        
        const reader = new FileReader()
        reader.onload = (event) => {
          const xml = event.target.result
          
          // 调用导入API
          importProcessDefinition({ processXml: xml }).then(response => {
            if (response.code === 200) {
              this.$message.success('导入成功')
              this.loadData()
            } else {
              this.$message.error(response.message || '导入失败')
            }
          }).catch(error => {
            console.error('导入失败:', error)
            this.$message.error('导入失败，请检查文件格式')
          })
        }
        reader.readAsText(file)
      }
      input.click()
    },
    handleExport(row) {
      if (!row.processXml) {
        this.$message.warning('该流程定义没有流程XML，无法导出')
        return
      }
      
      // 创建Blob对象并下载
      const blob = new Blob([row.processXml], { type: 'application/xml' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${row.definitionName || row.definitionCode || 'process'}.bpmn`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      this.$message.success('导出成功')
    },
    handleToggleActive(row) {
      const action = row.isActive === 1 ? '停用' : '启用'
      const newIsActive = row.isActive === 1 ? 0 : 1
      this.$confirm(`确定要${action}该流程定义吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        toggleProcessDefinitionActive(row.definitionId, newIsActive).then(response => {
          if (response.code === 200) {
            this.$message.success(`${action}成功`)
            this.loadData()
          } else {
            this.$message.error(response.message || `${action}失败`)
          }
        }).catch(error => {
          console.error('启用/停用失败:', error)
          this.$message.error(`${action}失败: ` + (error.message || '未知错误'))
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.process-definition {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.process-designer-wrapper {
  width: 100%;
  height: 100%;
}
</style>

<style>
.process-design-dialog .el-dialog__body {
  padding: 0;
}
</style>



