<template>
  <div class="my-reimb-apply">
    <el-card>
      <div slot="header" class="clearfix">
        <span>我的申请</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增申请</el-button>
      </div>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="payoutBillcode" label="申请单号" width="150"></el-table-column>
        <el-table-column prop="payoutTypeId" label="申请类型" width="120"></el-table-column>
        <el-table-column prop="applyAmount" label="申请金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.applyAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="applyDate" label="申请日期" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.applyDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
            <el-button v-if="scope.row.status === 'DRAFT'" size="mini" type="primary" @click="handleSubmit(scope.row)">提交</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="申请类型" prop="payoutTypeId">
          <el-select v-model="form.payoutTypeId" placeholder="请选择申请类型">
            <el-option label="差旅费" value="TRAVEL"></el-option>
            <el-option label="办公费" value="OFFICE"></el-option>
            <el-option label="其他" value="OTHER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="申请金额" prop="applyAmount">
          <el-input-number v-model="form.applyAmount" :min="0" :precision="2" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="申请事由" prop="applyReason">
          <el-input type="textarea" v-model="form.applyReason" :rows="4"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMyPayouts, savePayout, updatePayout, deletePayout, submitPayout } from '@/api/reimb'

export default {
  name: 'MyReimbApply',
  data() {
    return {
      tableData: [],
      dialogVisible: false,
      dialogTitle: '新增申请',
      isEdit: false,
      form: {
        payoutId: null,
        payoutTypeId: '',
        applyAmount: 0,
        applyReason: ''
      },
      rules: {
        payoutTypeId: [{ required: true, message: '请选择申请类型', trigger: 'change' }],
        applyAmount: [{ required: true, message: '请输入申请金额', trigger: 'blur' }],
        applyReason: [{ required: true, message: '请输入申请事由', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      const empId = this.$store.state.user.userInfo.empId || 1
      getMyPayouts(empId).then(response => {
        if (response.code === 200) {
          this.tableData = response.data
        }
      })
    },
    handleAdd() {
      this.dialogTitle = '新增申请'
      this.isEdit = false
      this.form = {
        payoutId: null,
        payoutTypeId: '',
        applyAmount: 0,
        applyReason: '',
        empId: this.$store.state.user.userInfo.empId || 1,
        status: 'DRAFT'
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑申请'
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleDelete(row) {
      this.$confirm('确认删除该申请吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deletePayout(row.payoutId).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          }
        })
      })
    },
    handleSubmit(row) {
      this.$confirm('确认提交该申请吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        submitPayout(row.payoutId).then(response => {
          if (response.code === 200) {
            this.$message.success('提交成功')
            this.loadData()
          }
        })
      })
    },
    handleSave() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const api = this.isEdit ? updatePayout : savePayout
          api(this.form).then(response => {
            if (response.code === 200) {
              this.$message.success(this.isEdit ? '更新成功' : '新增成功')
              this.dialogVisible = false
              this.loadData()
            }
          })
        }
      })
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
    },
    getStatusText(status) {
      const statusMap = {
        'DRAFT': '草稿',
        'PENDING': '待审批',
        'APPROVED': '已审批',
        'REJECTED': '已拒绝'
      }
      return statusMap[status] || status
    },
    getStatusType(status) {
      const typeMap = {
        'DRAFT': 'info',
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger'
      }
      return typeMap[status] || ''
    }
  }
}
</script>

<style scoped>
.my-reimb-apply {
  padding: 20px;
}
</style>

