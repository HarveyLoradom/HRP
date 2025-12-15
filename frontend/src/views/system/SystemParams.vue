<template>
  <div class="system-params">
    <el-card>
      <div slot="header" class="clearfix">
        <span>系统参数</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增参数</el-button>
      </div>
      
      <!-- 筛选条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="参数类型">
          <el-select v-model="searchForm.codeType" placeholder="请选择类型" clearable @change="handleSearch">
            <el-option
              v-for="type in typeList"
              :key="type"
              :label="type"
              :value="type"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="codeType" label="参数类型" width="150"></el-table-column>
        <el-table-column prop="codeTypeName" label="类型名称" width="150"></el-table-column>
        <el-table-column prop="codeValue" label="参数值" width="120"></el-table-column>
        <el-table-column prop="codeName" label="参数名称" width="200"></el-table-column>
        <el-table-column label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isStop === 0 ? 'success' : 'danger'">
              {{ scope.row.isStop === 0 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        style="margin-top: 20px; text-align: right;"
      ></el-pagination>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="700px">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <!-- 参数类型和类型名称（上方固定） -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="参数类型" prop="codeType">
              <el-input v-model="form.codeType" :disabled="isEdit" placeholder="请输入参数类型"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型名称" prop="codeTypeName">
              <el-input v-model="form.codeTypeName" :disabled="isEdit" placeholder="请输入类型名称"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 参数值和参数名称（批量添加） -->
        <el-form-item label="参数值列表" v-if="!isEdit">
          <el-button type="primary" size="small" @click="handleAddValueRow" style="margin-bottom: 10px;">添加一行</el-button>
          <el-table :data="form.valueList" border style="width: 100%">
            <el-table-column label="参数值" width="200">
              <template slot-scope="scope">
                <el-input v-model="scope.row.codeValue" placeholder="请输入参数值"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="参数名称" width="250">
              <template slot-scope="scope">
                <el-input v-model="scope.row.codeName" placeholder="请输入参数名称"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template slot-scope="scope">
                <el-button size="mini" type="danger" @click="handleRemoveValueRow(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        
        <!-- 编辑时显示单个参数值和名称 -->
        <el-form-item label="参数值" prop="codeValue" v-if="isEdit">
          <el-input v-model="form.codeValue" placeholder="请输入参数值"></el-input>
        </el-form-item>
        <el-form-item label="参数名称" prop="codeName" v-if="isEdit">
          <el-input v-model="form.codeName" placeholder="请输入参数名称"></el-input>
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
import { getCodeList, getCodeByType, getCodePage, getCodeByTypePage, saveCode, saveCodeBatch, updateCode, deleteCode } from '@/api/user'

export default {
  name: 'SystemParams',
  data() {
    return {
      loading: false,
      tableData: [],
      typeList: [],
      searchForm: {
        codeType: ''
      },
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      dialogVisible: false,
      dialogTitle: '新增参数',
      isEdit: false,
      form: {
        id: null,
        codeType: '',
        codeTypeName: '',
        codeValue: '',
        codeName: '',
        isStop: 0,
        valueList: [] // 批量添加的参数值列表
      },
      rules: {
        codeType: [{ required: true, message: '请输入参数类型', trigger: 'blur' }],
        codeTypeName: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
        codeValue: [{ required: true, message: '请输入参数值', trigger: 'blur' }],
        codeName: [{ required: true, message: '请输入参数名称', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadData()
    this.loadTypeList()
  },
  methods: {
    loadData() {
      this.loading = true
      const api = this.searchForm.codeType 
        ? getCodeByTypePage(this.searchForm.codeType, this.pagination.page, this.pagination.size)
        : getCodePage(this.pagination.page, this.pagination.size)
      api.then(response => {
        if (response.code === 200 && response.data) {
          this.tableData = response.data.records || []
          this.pagination.total = response.data.total || 0
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
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
    loadTypeList() {
      getCodeList().then(response => {
        if (response.code === 200) {
          const types = new Set()
          ;(response.data || []).forEach(item => {
            if (item.codeType) {
              types.add(item.codeType)
            }
          })
          this.typeList = Array.from(types)
        }
      })
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm.codeType = ''
      this.pagination.page = 1
      this.loadData()
    },
    handleAdd() {
      this.dialogTitle = '新增参数'
      this.isEdit = false
      this.form = {
        id: null,
        codeType: '',
        codeTypeName: '',
        codeValue: '',
        codeName: '',
        isStop: 0,
        valueList: [{ codeValue: '', codeName: '' }] // 默认添加一行
      }
      this.dialogVisible = true
    },
    handleAddValueRow() {
      this.form.valueList.push({ codeValue: '', codeName: '' })
    },
    handleRemoveValueRow(index) {
      this.form.valueList.splice(index, 1)
    },
    handleEdit(row) {
      this.dialogTitle = '编辑参数'
      this.isEdit = true
      this.form = {
        id: row.id,
        codeType: row.codeType,
        codeTypeName: row.codeTypeName,
        codeValue: row.codeValue,
        codeName: row.codeName,
        isStop: row.isStop
      }
      this.dialogVisible = true
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.isEdit) {
            // 编辑模式：更新单个参数
            const formData = { ...this.form }
            const api = updateCode
            api(formData).then(response => {
              if (response.code === 200) {
                this.$message.success(response.message || '操作成功')
                this.dialogVisible = false
                this.pagination.page = 1
                this.loadData()
                this.loadTypeList()
              } else {
                this.$message.error(response.message || '操作失败')
              }
            })
          } else {
            // 新增模式：批量创建参数
            if (!this.form.valueList || this.form.valueList.length === 0) {
              this.$message.warning('请至少添加一个参数值')
              return
            }
            
            // 验证所有参数值是否填写完整
            let hasError = false
            for (let i = 0; i < this.form.valueList.length; i++) {
              const item = this.form.valueList[i]
              if (!item.codeValue || !item.codeName) {
                this.$message.warning(`第${i + 1}行的参数值和参数名称不能为空`)
                hasError = true
                break
              }
            }
            if (hasError) {
              return
            }
            
            // 批量创建
            const codeList = this.form.valueList.map(item => ({
              codeType: this.form.codeType,
              codeTypeName: this.form.codeTypeName,
              codeValue: item.codeValue,
              codeName: item.codeName,
              isStop: 0
            }))
            
            saveCodeBatch(codeList).then(response => {
              if (response.code === 200) {
                this.$message.success(response.message || '批量创建成功')
                this.dialogVisible = false
                this.pagination.page = 1
                this.loadData()
                this.loadTypeList()
              } else {
                this.$message.error(response.message || '批量创建失败')
              }
            }).catch(error => {
              this.$message.error('批量创建失败：' + (error.message || '未知错误'))
            })
          }
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该参数吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteCode(row.id).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.pagination.page = 1
            this.loadData()
            this.loadTypeList()
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
.system-params {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>



