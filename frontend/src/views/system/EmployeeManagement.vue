<template>
  <div class="employee-management">
    <el-card>
      <div slot="header" class="clearfix">
        <span>职工管理</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增职工</el-button>
      </div>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="empCode" label="工号" width="120"></el-table-column>
        <el-table-column prop="empName" label="姓名" width="100"></el-table-column>
        <el-table-column prop="empSex" label="性别" width="80">
          <template slot-scope="scope">
            {{ scope.row.empSex === 1 ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="empPhone" label="手机号" width="120"></el-table-column>
        <el-table-column prop="empEmail" label="邮箱" width="150"></el-table-column>
        <el-table-column prop="deptCode" label="部门编码" width="120"></el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="工号" prop="empCode">
          <el-input v-model="form.empCode" :disabled="isEdit"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="empName">
          <el-input v-model="form.empName"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="empSex">
          <el-radio-group v-model="form.empSex">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="empPhone">
          <el-input v-model="form.empPhone"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="empEmail">
          <el-input v-model="form.empEmail"></el-input>
        </el-form-item>
        <el-form-item label="部门编码" prop="deptCode">
          <el-input v-model="form.deptCode"></el-input>
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
import { getEmployeeList, saveEmployee, updateEmployee, deleteEmployee } from '@/api/user'

export default {
  name: 'EmployeeManagement',
  data() {
    return {
      tableData: [],
      dialogVisible: false,
      dialogTitle: '新增职工',
      isEdit: false,
      form: {
        empId: null,
        empCode: '',
        empName: '',
        empSex: 1,
        idCard: '',
        empPhone: '',
        empEmail: '',
        deptCode: ''
      },
      rules: {
        empCode: [{ required: true, message: '请输入工号', trigger: 'blur' }],
        empName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        empSex: [{ required: true, message: '请选择性别', trigger: 'change' }]
      }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      getEmployeeList().then(response => {
        if (response.code === 200) {
          this.tableData = response.data
        }
      })
    },
    handleAdd() {
      this.dialogTitle = '新增职工'
      this.isEdit = false
      this.form = {
        empId: null,
        empCode: '',
        empName: '',
        empSex: 1,
        idCard: '',
        empPhone: '',
        empEmail: '',
        deptCode: ''
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑职工'
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleDelete(row) {
      this.$confirm('确定要删除该职工吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteEmployee(row.empId).then(response => {
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
          const api = this.isEdit ? updateEmployee : saveEmployee
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
.employee-management {
  padding: 20px;
}
</style>
