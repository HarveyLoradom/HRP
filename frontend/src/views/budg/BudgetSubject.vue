<template>
  <div class="budget-subject">
    <el-card>
      <div slot="header" class="clearfix">
        <span>预算主体管理</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增预算主体</el-button>
      </div>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading" row-key="subjectId" :tree-props="{children: 'children'}">
        <el-table-column prop="subjectCode" label="主体编码" width="150"></el-table-column>
        <el-table-column prop="subjectName" label="主体名称" width="200"></el-table-column>
        <el-table-column prop="subjectType" label="主体类型" width="120">
          <template slot-scope="scope">
            {{ getSubjectTypeName(scope.row.subjectType) }}
          </template>
        </el-table-column>
        <el-table-column prop="deptCode" label="责任科室编码" width="150"></el-table-column>
        <el-table-column prop="managerName" label="负责人" width="120"></el-table-column>
        <el-table-column prop="subjectLevel" label="层级" width="80"></el-table-column>
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
        <el-form-item label="主体编码" prop="subjectCode">
          <el-input v-model="form.subjectCode" placeholder="请输入主体编码"></el-input>
        </el-form-item>
        <el-form-item label="主体名称" prop="subjectName">
          <el-input v-model="form.subjectName" placeholder="请输入主体名称"></el-input>
        </el-form-item>
        <el-form-item label="主体类型" prop="subjectType">
          <el-select v-model="form.subjectType" placeholder="请选择主体类型" style="width: 100%">
            <el-option label="临床科室" value="CLINIC"></el-option>
            <el-option label="行政部门" value="ADMIN"></el-option>
            <el-option label="医技科室" value="TECH"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="上级主体" prop="parentSubjectId">
          <el-select v-model="form.parentSubjectId" placeholder="请选择上级主体（可选）" filterable style="width: 100%" clearable>
            <el-option
              v-for="subject in allSubjects"
              :key="subject.subjectId"
              :label="subject.subjectName"
              :value="subject.subjectId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="责任科室" prop="deptId">
          <el-input v-model="form.deptCode" placeholder="请输入责任科室编码"></el-input>
        </el-form-item>
        <el-form-item label="负责人" prop="managerId">
          <el-input v-model="form.managerCode" placeholder="请输入负责人工号"></el-input>
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
import { getBudgetSubjectTree, saveBudgetSubject, updateBudgetSubject, deleteBudgetSubject, getBudgetSubjects } from '@/api/budg'

export default {
  name: 'BudgetSubject',
  data() {
    return {
      loading: false,
      tableData: [],
      allSubjects: [],
      dialogVisible: false,
      dialogTitle: '新增预算主体',
      isEdit: false,
      form: {
        subjectId: null,
        subjectCode: '',
        subjectName: '',
        subjectType: '',
        parentSubjectId: null,
        deptId: null,
        deptCode: '',
        managerId: null,
        managerCode: '',
        subjectLevel: 1
      },
      rules: {
        subjectCode: [{ required: true, message: '请输入主体编码', trigger: 'blur' }],
        subjectName: [{ required: true, message: '请输入主体名称', trigger: 'blur' }],
        subjectType: [{ required: true, message: '请选择主体类型', trigger: 'change' }]
      }
    }
  },
  mounted() {
    this.loadData()
    this.loadAllSubjects()
  },
  methods: {
    loadData() {
      this.loading = true
      getBudgetSubjectTree().then(response => {
        if (response.code === 200) {
          this.tableData = this.buildTree(response.data || [])
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    loadAllSubjects() {
      getBudgetSubjects().then(response => {
        if (response.code === 200) {
          this.allSubjects = response.data || []
        }
      })
    },
    buildTree(data) {
      const map = {}
      const tree = []
      data.forEach(item => {
        map[item.subjectId] = { ...item, children: [] }
      })
      data.forEach(item => {
        if (item.parentSubjectId && map[item.parentSubjectId]) {
          map[item.parentSubjectId].children.push(map[item.subjectId])
        } else {
          tree.push(map[item.subjectId])
        }
      })
      return tree
    },
    getSubjectTypeName(type) {
      const map = {
        'CLINIC': '临床科室',
        'ADMIN': '行政部门',
        'TECH': '医技科室'
      }
      return map[type] || type
    },
    handleAdd() {
      this.dialogTitle = '新增预算主体'
      this.isEdit = false
      this.form = {
        subjectId: null,
        subjectCode: '',
        subjectName: '',
        subjectType: '',
        parentSubjectId: null,
        deptId: null,
        deptCode: '',
        managerId: null,
        managerCode: '',
        subjectLevel: 1
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑预算主体'
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleDelete(row) {
      this.$confirm('确认删除该预算主体吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteBudgetSubject(row.subjectId).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
            this.loadAllSubjects()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        })
      })
    },
    handleSave() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const api = this.isEdit ? updateBudgetSubject : saveBudgetSubject
          api(this.form).then(response => {
            if (response.code === 200) {
              this.$message.success(this.isEdit ? '更新成功' : '新增成功')
              this.dialogVisible = false
              this.loadData()
              this.loadAllSubjects()
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
.budget-subject {
  padding: 20px;
}
</style>

