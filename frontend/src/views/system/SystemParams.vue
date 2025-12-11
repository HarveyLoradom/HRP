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
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="参数类型" prop="codeType">
          <el-input v-model="form.codeType" placeholder="请输入参数类型"></el-input>
        </el-form-item>
        <el-form-item label="类型名称" prop="codeTypeName">
          <el-input v-model="form.codeTypeName" placeholder="请输入类型名称"></el-input>
        </el-form-item>
        <el-form-item label="参数值" prop="codeValue">
          <el-input v-model="form.codeValue" placeholder="请输入参数值"></el-input>
        </el-form-item>
        <el-form-item label="参数名称" prop="codeName">
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
import { getCodeList, getCodeByType, saveCode, updateCode, deleteCode } from '@/api/user'

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
      dialogVisible: false,
      dialogTitle: '新增参数',
      isEdit: false,
      form: {
        id: null,
        codeType: '',
        codeTypeName: '',
        codeValue: '',
        codeName: '',
        isStop: 0
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
      const api = this.searchForm.codeType ? getCodeByType(this.searchForm.codeType) : getCodeList()
      api.then(response => {
        if (response.code === 200) {
          this.tableData = response.data || []
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
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
      this.loadData()
    },
    handleReset() {
      this.searchForm.codeType = ''
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
        isStop: 0
      }
      this.dialogVisible = true
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
          // 新增时不需要传ID，后端会自动生成UUID
          const formData = { ...this.form }
          if (!this.isEdit) {
            delete formData.id
          }
          const api = this.isEdit ? updateCode : saveCode
          api(formData).then(response => {
            if (response.code === 200) {
              this.$message.success(response.message || '操作成功')
              this.dialogVisible = false
              this.loadData()
              this.loadTypeList()
            } else {
              this.$message.error(response.message || '操作失败')
            }
          })
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



