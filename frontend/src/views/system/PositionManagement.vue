<template>
  <div class="position-management">
    <el-card>
      <div slot="header" class="clearfix">
        <span>岗位管理</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增岗位</el-button>
      </div>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="codeType" label="岗位类型" width="120"></el-table-column>
        <el-table-column prop="codeTypeName" label="类型名称" width="150"></el-table-column>
        <el-table-column prop="codeValue" label="岗位编码" width="120"></el-table-column>
        <el-table-column prop="codeName" label="岗位名称" width="150"></el-table-column>
        <el-table-column label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isStop === 0 ? 'success' : 'danger'">
              {{ scope.row.isStop === 0 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
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
        <el-form-item label="岗位类型" prop="codeType">
          <el-input v-model="form.codeType" placeholder="请输入岗位类型（如：POSITION）"></el-input>
        </el-form-item>
        <el-form-item label="类型名称" prop="codeTypeName">
          <el-input v-model="form.codeTypeName" placeholder="请输入类型名称"></el-input>
        </el-form-item>
        <el-form-item label="岗位编码" prop="codeValue">
          <el-input v-model="form.codeValue" placeholder="请输入岗位编码"></el-input>
        </el-form-item>
        <el-form-item label="岗位名称" prop="codeName">
          <el-input v-model="form.codeName" placeholder="请输入岗位名称"></el-input>
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
import { getCodeByType, saveCode, updateCode, deleteCode } from '@/api/user'

export default {
  name: 'PositionManagement',
  data() {
    return {
      loading: false,
      tableData: [],
      dialogVisible: false,
      dialogTitle: '新增岗位',
      isEdit: false,
      form: {
        id: null,
        codeType: 'POSITION',
        codeTypeName: '岗位',
        codeValue: '',
        codeName: '',
        isStop: 0
      },
      rules: {
        codeType: [{ required: true, message: '请输入岗位类型', trigger: 'blur' }],
        codeTypeName: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
        codeValue: [{ required: true, message: '请输入岗位编码', trigger: 'blur' }],
        codeName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      getCodeByType('POSITION').then(response => {
        if (response.code === 200) {
          this.tableData = response.data || []
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleAdd() {
      this.dialogTitle = '新增岗位'
      this.isEdit = false
      this.form = {
        id: null,
        codeType: 'POSITION',
        codeTypeName: '岗位',
        codeValue: '',
        codeName: '',
        isStop: 0
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑岗位'
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
          if (!this.isEdit) {
            // 生成ID
            this.form.id = 'POSITION_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
          }
          const api = this.isEdit ? updateCode : saveCode
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
    handleDelete(row) {
      this.$confirm('确定要删除该岗位吗？', '提示', {
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
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.position-management {
  padding: 20px;
}
</style>
