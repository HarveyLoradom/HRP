<template>
  <div class="dept-management">
    <el-card>
      <div slot="header" class="clearfix">
        <span>部门管理</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增部门</el-button>
      </div>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="deptCode" label="部门编码" width="120"></el-table-column>
        <el-table-column prop="deptName" label="部门名称" width="150"></el-table-column>
        <el-table-column prop="superDeptCode" label="上级部门编码" width="120"></el-table-column>
        <el-table-column prop="deptLevel" label="部门级别" width="100"></el-table-column>
        <el-table-column prop="deptPhone" label="部门电话" width="120"></el-table-column>
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
        <el-form-item label="部门编码" prop="deptCode">
          <el-input v-model="form.deptCode" :disabled="isEdit"></el-input>
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName"></el-input>
        </el-form-item>
        <el-form-item label="上级部门编码" prop="superDeptCode">
          <el-input v-model="form.superDeptCode"></el-input>
        </el-form-item>
        <el-form-item label="部门级别" prop="deptLevel">
          <el-input-number v-model="form.deptLevel" :min="1"></el-input-number>
        </el-form-item>
        <el-form-item label="部门电话" prop="deptPhone">
          <el-input v-model="form.deptPhone"></el-input>
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
import { getDeptList, saveDept, updateDept, deleteDept } from '@/api/user'

export default {
  name: 'DeptManagement',
  data() {
    return {
      tableData: [],
      dialogVisible: false,
      dialogTitle: '新增部门',
      isEdit: false,
      form: {
        deptId: null,
        deptCode: '',
        deptName: '',
        superDeptCode: '',
        deptLevel: 1,
        deptPhone: ''
      },
      rules: {
        deptCode: [{ required: true, message: '请输入部门编码', trigger: 'blur' }],
        deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      getDeptList().then(response => {
        if (response.code === 200) {
          this.tableData = response.data
        }
      })
    },
    handleAdd() {
      this.dialogTitle = '新增部门'
      this.isEdit = false
      this.form = {
        deptId: null,
        deptCode: '',
        deptName: '',
        superDeptCode: '',
        deptLevel: 1,
        deptPhone: ''
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑部门'
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleDelete(row) {
      this.$confirm('确定要删除该部门吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteDept(row.deptId).then(response => {
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
          const api = this.isEdit ? updateDept : saveDept
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
.dept-management {
  padding: 20px;
}
</style>
