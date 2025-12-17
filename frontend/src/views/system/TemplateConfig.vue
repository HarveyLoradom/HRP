<template>
  <div class="template-config">
    <el-card>
      <div slot="header" class="clearfix">
        <span>模板设置</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增配置</el-button>
      </div>
      
      <!-- 筛选条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="业务类型">
          <el-select v-model="searchForm.businessType" placeholder="请选择业务类型" clearable @change="handleBusinessTypeChange">
            <el-option
              v-for="option in businessTypeOptions"
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
        <el-table-column prop="businessType" label="业务类型" width="150">
          <template slot-scope="scope">
            {{ getBusinessTypeName(scope.row.businessType) }}
          </template>
        </el-table-column>
        <el-table-column prop="businessTypeValue" label="业务类型值" width="150"></el-table-column>
        <el-table-column prop="businessTypeName" label="业务类型名称" width="150"></el-table-column>
        <el-table-column prop="printTemplateName" label="打印模板" width="200"></el-table-column>
        <el-table-column prop="processDefinitionName" label="流程模板" width="200"></el-table-column>
        <el-table-column label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isActive === 1 ? 'success' : 'danger'">
              {{ scope.row.isActive === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button 
              size="mini" 
              :type="scope.row.isActive === 1 ? 'warning' : 'success'"
              @click="handleToggleActive(scope.row)"
            >
              {{ scope.row.isActive === 1 ? '停用' : '启用' }}
            </el-button>
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
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="800px" :modal="false">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-form-item label="业务类型" prop="businessType">
          <el-select v-model="form.businessType" placeholder="请选择业务类型" @change="handleBusinessTypeSelect" style="width: 100%;">
            <el-option
              v-for="option in businessTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="业务类型值" prop="businessTypeValue">
          <el-select 
            v-model="form.businessTypeValue" 
            placeholder="请先选择业务类型" 
            @change="handleBusinessTypeValueSelect"
            :disabled="!form.businessType"
            filterable
            style="width: 100%;"
          >
            <el-option
              v-for="option in businessTypeValueOptions"
              :key="option.codeValue"
              :label="option.codeValue"
              :value="option.codeValue"
            >
              <span style="float: left">{{ option.codeValue }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ option.codeName }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="业务类型名称" prop="businessTypeName">
          <el-input v-model="form.businessTypeName" placeholder="自动填充，可手动修改"></el-input>
        </el-form-item>
        <el-form-item label="打印模板" prop="printTemplateCode">
          <el-select 
            v-model="form.printTemplateCode" 
            placeholder="请选择打印模板" 
            filterable
            @change="handlePrintTemplateChange"
            style="width: 100%;"
          >
            <el-option
              v-for="template in printTemplateOptions"
              :key="template.templateCode"
              :label="`${template.templateCode} - ${template.templateName}`"
              :value="template.templateCode"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="流程模板" prop="processDefinitionCode">
          <el-select 
            v-model="form.processDefinitionCode" 
            placeholder="请选择流程模板" 
            filterable
            @change="handleProcessDefinitionChange"
            style="width: 100%;"
          >
            <el-option
              v-for="definition in processDefinitionOptions"
              :key="definition.definitionCode || definition.definitionKey"
              :label="`${definition.definitionCode || definition.definitionKey} - ${definition.definitionName}`"
              :value="definition.definitionCode || definition.definitionKey"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="form.isActive" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { 
  getTemplateConfigPage, 
  saveTemplateConfig, 
  updateTemplateConfig, 
  deleteTemplateConfig 
} from '@/api/templateConfig'
import { getCodeByType } from '@/api/user'
import { getPrintTemplateList } from '@/api/print'
import { getProcessDefinitionList } from '@/api/process'
import { paginationMixin } from '@/mixins/pagination'

export default {
  name: 'TemplateConfig',
  mixins: [paginationMixin],
  data() {
    return {
      loading: false,
      tableData: [],
      allData: [],
      searchForm: {
        businessType: ''
      },
      businessTypeOptions: [],
      businessTypeValueOptions: [],
      printTemplateOptions: [],
      processDefinitionOptions: [],
      dialogVisible: false,
      dialogTitle: '新增配置',
      isEdit: false,
      form: {
        configId: null,
        businessType: '',
        businessTypeValue: '',
        businessTypeName: '',
        printTemplateCode: '',
        printTemplateName: '',
        processDefinitionCode: '',
        processDefinitionName: '',
        isActive: 1,
        remark: ''
      },
      rules: {
        businessType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
        businessTypeValue: [{ required: true, message: '请选择业务类型值', trigger: 'change' }],
        businessTypeName: [{ required: true, message: '请输入业务类型名称', trigger: 'blur' }]
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
    this.loadData()
    this.loadBusinessTypeOptions()
    this.loadPrintTemplates()
    this.loadProcessDefinitions()
  },
  methods: {
    loadData() {
      this.loading = true
      getTemplateConfigPage(this.pagination.page, this.pagination.size).then(response => {
        if (response.code === 200 && response.data) {
          this.allData = response.data.records || []
          this.handleSearch()
          this.pagination.total = response.data.total || 0
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    loadBusinessTypeOptions() {
      // 从sys_code获取业务类型（如：PAYOUT_TYPE、APPLY_TYPE等）
      getCodeByType('BUSINESS_TYPE').then(response => {
        if (response.code === 200) {
          this.businessTypeOptions = (response.data || []).map(item => ({
            value: item.codeValue,
            label: item.codeName
          }))
        }
      })
    },
    loadBusinessTypeValues(businessType) {
      if (!businessType) {
        this.businessTypeValueOptions = []
        return
      }
      // 根据业务类型拼接业务类型 code_type（如：PAYOUT -> PAYOUT_TYPE）
      const businessTypeCode = `${businessType}_TYPE`
      // 根据业务类型获取对应的业务类型值（如：PAYOUT_TYPE下的餐费、差旅费等）
      getCodeByType(businessTypeCode).then(response => {
        if (response.code === 200) {
          this.businessTypeValueOptions = response.data || []
        }
      }).catch(error => {
        console.error('加载业务类型值失败:', error)
        this.businessTypeValueOptions = []
      })
    },
    loadPrintTemplates() {
      getPrintTemplateList().then(response => {
        if (response.code === 200) {
          // 显示所有打印模板，包括停用的
          this.printTemplateOptions = response.data || []
        }
      })
    },
    loadProcessDefinitions() {
      getProcessDefinitionList().then(response => {
        if (response.code === 200) {
          // 显示所有流程定义，包括停用的
          this.processDefinitionOptions = response.data || []
        }
      })
    },
    handleSearch() {
      let filtered = [...this.allData]
      if (this.searchForm.businessType) {
        filtered = filtered.filter(item => item.businessType === this.searchForm.businessType)
      }
      this.tableData = filtered
      this.pagination.total = filtered.length
      this.pagination.page = 1
    },
    handleReset() {
      this.searchForm.businessType = ''
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
    handleBusinessTypeChange() {
      this.handleSearch()
    },
    handleAdd() {
      this.dialogTitle = '新增配置'
      this.isEdit = false
      this.form = {
        configId: null,
        businessType: '',
        businessTypeValue: '',
        businessTypeName: '',
        printTemplateCode: '',
        printTemplateName: '',
        processDefinitionCode: '',
        processDefinitionName: '',
        isActive: 1,
        remark: ''
      }
      this.businessTypeValueOptions = []
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑配置'
      this.isEdit = true
      // 处理业务类型：如果后端返回的是 PAYOUT_TYPE，需要转换为 PAYOUT
      let businessType = row.businessType
      if (businessType && businessType.endsWith('_TYPE')) {
        businessType = businessType.replace('_TYPE', '')
      }
      this.form = {
        configId: row.configId,
        businessType: businessType,
        businessTypeValue: row.businessTypeValue, // 这里保存的是 codeValue
        businessTypeName: row.businessTypeName,
        printTemplateCode: row.printTemplateCode || '',
        printTemplateName: row.printTemplateName || '',
        processDefinitionCode: row.processDefinitionCode || row.processDefinitionKey || '',
        processDefinitionName: row.processDefinitionName || '',
        isActive: row.isActive,
        remark: row.remark || ''
      }
      // 加载业务类型值选项
      this.loadBusinessTypeValues(businessType)
      this.dialogVisible = true
    },
    handleBusinessTypeSelect(value) {
      this.form.businessTypeValue = ''
      this.form.businessTypeName = ''
      this.loadBusinessTypeValues(value)
    },
    handleBusinessTypeValueSelect(value) {
      // 自动填充业务类型名称
      const option = this.businessTypeValueOptions.find(opt => opt.codeValue === value)
      if (option) {
        this.form.businessTypeName = option.codeName
      }
    },
    handlePrintTemplateChange(value) {
      const template = this.printTemplateOptions.find(t => t.templateCode === value)
      if (template) {
        this.form.printTemplateName = template.templateName
      }
    },
    handleProcessDefinitionChange(value) {
      const definition = this.processDefinitionOptions.find(d => (d.definitionCode || d.definitionKey) === value)
      if (definition) {
        this.form.processDefinitionName = definition.definitionName
      }
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const api = this.isEdit ? updateTemplateConfig : saveTemplateConfig
          // 确保 businessType 保存为完整的类型（如 PAYOUT_TYPE），businessTypeValue 保存的是 codeValue
          const submitData = {
            ...this.form,
            businessType: this.form.businessType && !this.form.businessType.endsWith('_TYPE') 
              ? `${this.form.businessType}_TYPE` 
              : this.form.businessType
            // businessTypeValue 已经是 codeValue，不需要修改
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
    handleToggleActive(row) {
      const action = row.isActive === 1 ? '停用' : '启用'
      const newStatus = row.isActive === 1 ? 0 : 1
      this.$confirm(`确定要${action}该配置吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        updateTemplateConfig({
          ...row,
          isActive: newStatus
        }).then(response => {
          if (response.code === 200) {
            this.$message.success(`${action}成功`)
            this.loadData()
          } else {
            this.$message.error(response.message || `${action}失败`)
          }
        })
      }).catch(() => {})
    },
    handleDelete(row) {
      this.$confirm('确定要删除该配置吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteTemplateConfig(row.configId).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        })
      }).catch(() => {})
    },
    getBusinessTypeName(businessType) {
      // 处理 businessType 可能是 PAYOUT_TYPE 或 PAYOUT 的情况
      let type = businessType
      if (type && type.endsWith('_TYPE')) {
        type = type.replace('_TYPE', '')
      }
      const option = this.businessTypeOptions.find(opt => opt.value === type)
      return option ? option.label : businessType
    },
  }
}
</script>

<style scoped>
.template-config {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>

