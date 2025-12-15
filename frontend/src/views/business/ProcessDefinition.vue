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
        <el-table-column prop="definitionKey" label="流程KEY" width="200"></el-table-column>
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
        <el-form-item label="流程KEY" prop="definitionKey">
          <el-input v-model="form.definitionKey" :disabled="isEdit" placeholder="请输入流程KEY（如：PAYOUT_APPROVAL）"></el-input>
        </el-form-item>
        <el-form-item label="流程名称" prop="definitionName">
          <el-input v-model="form.definitionName" placeholder="请输入流程名称"></el-input>
        </el-form-item>
        <el-form-item label="流程类型" prop="definitionType">
          <el-select v-model="form.definitionType" placeholder="请选择流程类型" @change="handleDefinitionTypeChange">
            <el-option
              v-for="option in processTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
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
    >
      <div class="process-designer-wrapper" style="height: 75vh;">
        <el-tabs v-model="designerType" @tab-click="handleDesignerTypeChange">
          <el-tab-pane label="BPMN设计器" name="bpmn">
            <BpmnDesigner 
              v-if="designerType === 'bpmn'"
              :value="designForm.processXml"
              :definition-id="designForm.definitionId"
              @save="handleBpmnSave"
            />
          </el-tab-pane>
          <el-tab-pane label="传统设计器" name="legacy">
            <ProcessDesigner 
              v-if="designerType === 'legacy'"
              v-model="designForm.processJson"
              @change="handleProcessChange"
            />
          </el-tab-pane>
        </el-tabs>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="designDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveDesign">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { 
  getProcessDefinitionPage, 
  getProcessDefinitionByTypePage, 
  saveProcessDefinition, 
  updateProcessDefinition, 
  deleteProcessDefinition,
  toggleProcessDefinitionActive,
  exportProcessDefinition,
  importProcessDefinition
} from '@/api/process'
import { getCodeTypeOptions } from '@/utils/codeType'
import { getCodeByType } from '@/api/user'
import ProcessDesigner from '@/components/ProcessDesigner.vue'
import BpmnDesigner from '@/components/BpmnDesigner.vue'
import { paginationMixin } from '@/mixins/pagination'

export default {
  name: 'ProcessDefinition',
  components: {
    ProcessDesigner,
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
        definitionKey: '',
        definitionName: '',
        definitionType: '',
        businessType: '', // 业务类型
        description: '',
        processJson: '',
        version: 1,
        isActive: 1
      },
      rules: {
        definitionKey: [{ required: true, message: '请输入流程KEY', trigger: 'blur' }],
        definitionName: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
        definitionType: [{ required: true, message: '请选择流程类型', trigger: 'change' }]
      },
      designDialogVisible: false,
      designForm: {
        definitionId: null,
        processJson: ''
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
    this.loadProcessTypeOptions()
    this.loadData()
  },
  methods: {
    async loadProcessTypeOptions() {
      this.processTypeOptions = await getCodeTypeOptions('PROCESS_TYPE')
    },
    getProcessTypeName(codeValue) {
      const option = this.processTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
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
        definitionKey: '',
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
        definitionKey: row.definitionKey,
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
    handleDesign(row) {
      this.designForm = {
        definitionId: row.definitionId,
        processJson: row.processJson || '{"nodes":[],"edges":[],"version":"1.0"}',
        processXml: row.processXml || ''
      }
      // 如果有processXml，默认使用BPMN设计器
      this.designerType = row.processXml ? 'bpmn' : 'legacy'
      this.designDialogVisible = true
    },
    
    handleDesignerTypeChange(tab) {
      // 切换设计器类型时的处理
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
        processJson: JSON.stringify(data.json)
      }).then(response => {
        if (response.code === 200) {
          this.$message.success('保存成功')
          this.designDialogVisible = false
          this.loadData()
        } else {
          this.$message.error(response.message || '保存失败')
        }
      })
    },
    handleProcessChange(jsonStr) {
      this.designForm.processJson = jsonStr
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const api = this.isEdit ? updateProcessDefinition : saveProcessDefinition
          api(this.form).then(response => {
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
      if (!this.designForm.definitionId) {
        this.$message.error('流程定义ID不能为空')
        return
      }
      // 根据设计器类型保存不同的数据
      if (this.designerType === 'bpmn') {
        // BPMN设计器通过@save事件处理
        return
      }
      updateProcessDefinition({
        definitionId: this.designForm.definitionId,
        processJson: this.designForm.processJson
      }).then(response => {
        if (response.code === 200) {
          this.$message.success('保存成功')
          this.designDialogVisible = false
          this.loadData()
        } else {
          this.$message.error(response.message || '保存失败')
        }
      })
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



