<template>
  <div class="budget-item">
    <el-card>
      <div slot="header" class="clearfix">
        <span>预算项目与分类管理</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增预算项目</el-button>
      </div>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="itemCode" label="项目编码" width="150"></el-table-column>
        <el-table-column prop="itemName" label="项目名称" width="200"></el-table-column>
        <el-table-column prop="categoryCode" label="分类编码" width="150"></el-table-column>
        <el-table-column prop="accountSubject" label="会计科目" width="150"></el-table-column>
        <el-table-column prop="isCentralized" label="归口管理" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isCentralized === 1 ? 'success' : 'info'">
              {{ scope.row.isCentralized === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="allowAdjust" label="允许调整" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.allowAdjust === 1 ? 'success' : 'danger'">
              {{ scope.row.allowAdjust === 1 ? '是' : '否' }}
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
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-form-item label="项目编码" prop="itemCode">
          <el-input v-model="form.itemCode" placeholder="请输入项目编码"></el-input>
        </el-form-item>
        <el-form-item label="项目名称" prop="itemName">
          <el-input v-model="form.itemName" placeholder="请输入项目名称"></el-input>
        </el-form-item>
        <el-form-item label="分类编码" prop="categoryCode">
          <el-input v-model="form.categoryCode" placeholder="请输入分类编码"></el-input>
        </el-form-item>
        <el-form-item label="会计科目" prop="accountSubject">
          <el-input v-model="form.accountSubject" placeholder="请输入会计科目"></el-input>
        </el-form-item>
        <el-form-item label="归口管理" prop="isCentralized">
          <el-switch v-model="form.isCentralized" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
        <el-form-item label="允许调整" prop="allowAdjust">
          <el-switch v-model="form.allowAdjust" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
        <el-form-item label="项目描述" prop="itemDesc">
          <el-input type="textarea" v-model="form.itemDesc" :rows="3"></el-input>
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
import { getBudgetItems, saveBudgetItem, updateBudgetItem, deleteBudgetItem } from '@/api/budg'

export default {
  name: 'BudgetItem',
  data() {
    return {
      loading: false,
      tableData: [],
      dialogVisible: false,
      dialogTitle: '新增预算项目',
      isEdit: false,
      form: {
        itemId: null,
        itemCode: '',
        itemName: '',
        categoryId: null,
        categoryCode: '',
        accountSubject: '',
        isCentralized: 0,
        allowAdjust: 1,
        itemDesc: ''
      },
      rules: {
        itemCode: [{ required: true, message: '请输入项目编码', trigger: 'blur' }],
        itemName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      getBudgetItems().then(response => {
        if (response.code === 200) {
          this.tableData = response.data || []
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleAdd() {
      this.dialogTitle = '新增预算项目'
      this.isEdit = false
      this.form = {
        itemId: null,
        itemCode: '',
        itemName: '',
        categoryId: null,
        categoryCode: '',
        accountSubject: '',
        isCentralized: 0,
        allowAdjust: 1,
        itemDesc: ''
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑预算项目'
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleDelete(row) {
      this.$confirm('确认删除该预算项目吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteBudgetItem(row.itemId).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        })
      })
    },
    handleSave() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const api = this.isEdit ? updateBudgetItem : saveBudgetItem
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
.budget-item {
  padding: 20px;
}
</style>

