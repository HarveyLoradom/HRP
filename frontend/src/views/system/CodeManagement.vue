<template>
  <div class="code-management">
    <el-card>
      <div slot="header" class="clearfix">
        <span>系统字典管理</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增字典</el-button>
      </div>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="codeType" label="字典类型" width="120"></el-table-column>
        <el-table-column prop="codeTypeName" label="类型名称" width="120"></el-table-column>
        <el-table-column prop="codeValue" label="字典值" width="100"></el-table-column>
        <el-table-column prop="codeName" label="字典名称" width="150"></el-table-column>
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
        <el-form-item label="字典ID" prop="id">
          <el-input v-model="form.id" :disabled="isEdit"></el-input>
        </el-form-item>
        <el-form-item label="字典类型" prop="codeType">
          <el-input v-model="form.codeType"></el-input>
        </el-form-item>
        <el-form-item label="类型名称" prop="codeTypeName">
          <el-input v-model="form.codeTypeName"></el-input>
        </el-form-item>
        <el-form-item label="字典值" prop="codeValue">
          <el-input v-model="form.codeValue"></el-input>
        </el-form-item>
        <el-form-item label="字典名称" prop="codeName">
          <el-input v-model="form.codeName"></el-input>
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
import { getCodeList, saveCode, updateCode, deleteCode } from '@/api/user'

export default {
  name: 'CodeManagement',
  data() {
    return {
      tableData: [],
      dialogVisible: false,
      dialogTitle: '新增字典',
      isEdit: false,
      form: {
        id: '',
        codeType: '',
        codeTypeName: '',
        codeValue: '',
        codeName: ''
      },
      rules: {
        id: [{ required: true, message: '请输入字典ID', trigger: 'blur' }],
        codeType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
        codeValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
        codeName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      getCodeList().then(response => {
        if (response.code === 200) {
          this.tableData = response.data
        }
      })
    },
    handleAdd() {
      this.dialogTitle = '新增字典'
      this.isEdit = false
      this.form = {
        id: '',
        codeType: '',
        codeTypeName: '',
        codeValue: '',
        codeName: ''
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑字典'
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleDelete(row) {
      this.$confirm('确定要删除该字典吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteCode(row.id).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        })
      })
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const api = this.isEdit ? updateCode : saveCode
          api(this.form).then(response => {
            if (response.code === 200) {
              this.$message.success(this.isEdit ? '更新成功' : '新增成功')
              this.dialogVisible = false
              this.loadData()
            } else {
              this.$message.error(response.message || '操作失败')
            }
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.code-management {
  padding: 20px;
}
</style>
