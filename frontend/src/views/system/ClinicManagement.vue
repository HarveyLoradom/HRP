<template>
  <div class="clinic-management">
    <el-card>
      <div slot="header" class="clearfix">
        <span>科室管理</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增科室</el-button>
      </div>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="clinicCode" label="科室编码" width="120"></el-table-column>
        <el-table-column prop="clinicName" label="科室名称" width="150"></el-table-column>
        <el-table-column prop="clinicType" label="科室类型" width="120"></el-table-column>
        <el-table-column prop="clinicPhone" label="科室电话" width="120"></el-table-column>
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
        <el-form-item label="科室编码" prop="clinicCode">
          <el-input v-model="form.clinicCode" :disabled="isEdit"></el-input>
        </el-form-item>
        <el-form-item label="科室名称" prop="clinicName">
          <el-input v-model="form.clinicName"></el-input>
        </el-form-item>
        <el-form-item label="科室类型" prop="clinicType">
          <el-input v-model="form.clinicType"></el-input>
        </el-form-item>
        <el-form-item label="科室电话" prop="clinicPhone">
          <el-input v-model="form.clinicPhone"></el-input>
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
import { getClinicList, saveClinic, updateClinic, deleteClinic } from '@/api/user'

export default {
  name: 'ClinicManagement',
  data() {
    return {
      tableData: [],
      dialogVisible: false,
      dialogTitle: '新增科室',
      isEdit: false,
      form: {
        clinicId: null,
        clinicCode: '',
        clinicName: '',
        clinicType: '',
        clinicPhone: ''
      },
      rules: {
        clinicCode: [{ required: true, message: '请输入科室编码', trigger: 'blur' }],
        clinicName: [{ required: true, message: '请输入科室名称', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      getClinicList().then(response => {
        if (response.code === 200) {
          this.tableData = response.data
        }
      })
    },
    handleAdd() {
      this.dialogTitle = '新增科室'
      this.isEdit = false
      this.form = {
        clinicId: null,
        clinicCode: '',
        clinicName: '',
        clinicType: '',
        clinicPhone: ''
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑科室'
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleDelete(row) {
      this.$confirm('确定要删除该科室吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteClinic(row.clinicId).then(response => {
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
          const api = this.isEdit ? updateClinic : saveClinic
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
.clinic-management {
  padding: 20px;
}
</style>
