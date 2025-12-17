<template>
  <div class="budget-subject">
    <el-card>
      <div slot="header" class="clearfix">
        <span>预算主体管理</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增预算主体</el-button>
      </div>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading" row-key="subjectId">
        <el-table-column prop="subjectCode" label="主体编码" width="150"></el-table-column>
        <el-table-column prop="subjectName" label="主体名称" width="200"></el-table-column>
        <el-table-column prop="subjectType" label="主体类型" width="120">
          <template slot-scope="scope">
            {{ getSubjectTypeName(scope.row.subjectType) }}
          </template>
        </el-table-column>
        <el-table-column prop="manageDeptName" label="归口部门" width="150"></el-table-column>
        <el-table-column prop="manageEmpName" label="归口负责人" width="120"></el-table-column>
        <el-table-column prop="relatedDepts" label="关联科室" width="200">
          <template slot-scope="scope">
            <el-tag v-for="dept in scope.row.relatedDepts" :key="dept.deptId" size="mini" style="margin-right: 5px;">
              {{ dept.deptName }}
            </el-tag>
            <span v-if="!scope.row.relatedDepts || scope.row.relatedDepts.length === 0" style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="isStop" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isStop === 0 || scope.row.isStop === '0' ? 'success' : 'danger'">
              {{ scope.row.isStop === 0 || scope.row.isStop === '0' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button 
              v-if="scope.row.isStop === 0 || scope.row.isStop === '0'"
              size="mini" 
              type="warning" 
              @click="handleStop(scope.row)"
            >停用</el-button>
            <el-button 
              v-else
              size="mini" 
              type="success" 
              @click="handleStart(scope.row)"
            >启用</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="700px">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-form-item label="主体编码" prop="subjectCode">
          <el-input v-model="form.subjectCode" placeholder="请输入主体编码"></el-input>
        </el-form-item>
        <el-form-item label="主体名称" prop="subjectName">
          <el-input v-model="form.subjectName" placeholder="请输入主体名称"></el-input>
        </el-form-item>
        <el-form-item label="主体类型" prop="subjectType">
          <el-select v-model="form.subjectType" placeholder="请选择主体类型" style="width: 100%" filterable>
            <el-option
              v-for="type in subjectTypeOptions"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="归口部门" prop="manageDeptId">
          <el-select 
            v-model="form.manageDeptId" 
            placeholder="请选择归口部门" 
            style="width: 100%" 
            filterable
            @change="handleDeptChange"
          >
            <el-option
              v-for="dept in deptOptions"
              :key="dept.deptId"
              :label="dept.deptName"
              :value="dept.deptId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="归口负责人" prop="manageEmpId">
          <el-select 
            v-model="form.manageEmpId" 
            placeholder="请选择归口负责人" 
            style="width: 100%" 
            filterable
            @change="handleEmpChange"
          >
            <el-option
              v-for="emp in empOptions"
              :key="emp.empId"
              :label="`${emp.empName}(${emp.empCode})`"
              :value="emp.empId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="关联科室" prop="relatedDeptIds">
          <el-select 
            v-model="form.relatedDeptIds" 
            placeholder="请选择关联科室（可多选）" 
            style="width: 100%" 
            multiple
            filterable
            clearable
            collapse-tags
          >
            <el-option
              v-for="dept in deptOptions"
              :key="dept.deptId"
              :label="dept.deptName"
              :value="dept.deptId"
            ></el-option>
          </el-select>
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
import { getBudgetSubjectTree, saveBudgetSubject, updateBudgetSubject, deleteBudgetSubject, getBudgetSubjects, stopBudgetSubject, startBudgetSubject, getBudgetSubjectRelatedDepts } from '@/api/budg'
import { getCodeByType } from '@/api/user'
import { getDeptList } from '@/api/user'
import { getEmployeeList } from '@/api/user'
import Cookies from 'js-cookie'

export default {
  name: 'BudgetSubject',
  data() {
    return {
      loading: false,
      tableData: [],
      allSubjects: [],
      subjectTypeOptions: [],
      deptOptions: [],
      empOptions: [],
      dialogVisible: false,
      dialogTitle: '新增预算主体',
      isEdit: false,
      form: {
        subjectId: null,
        subjectCode: '',
        subjectName: '',
        subjectType: '',
        manageDeptId: null,
        manageDeptCode: '',
        manageDeptName: '',
        manageEmpId: null,
        manageEmpCode: '',
        manageEmpName: '',
        relatedDeptIds: []
      },
      rules: {
        subjectCode: [{ required: true, message: '请输入主体编码', trigger: 'blur' }],
        subjectName: [{ required: true, message: '请输入主体名称', trigger: 'blur' }],
        subjectType: [{ required: true, message: '请选择主体类型', trigger: 'change' }],
        manageDeptId: [{ required: true, message: '请选择归口部门', trigger: 'change' }],
        manageEmpId: [{ required: true, message: '请选择归口负责人', trigger: 'change' }]
      }
    }
  },
  computed: {
    currentUser() {
      return this.$store.state.user.userInfo || JSON.parse(localStorage.getItem('userInfo') || '{}')
    }
  },
  mounted() {
    this.loadData()
    this.loadAllSubjects()
    this.loadSubjectTypes()
    this.loadDeptList()
    this.loadEmpList()
  },
  methods: {
    loadData() {
      this.loading = true
      getBudgetSubjects().then(response => {
        if (response.code === 200) {
          this.tableData = response.data || []
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
    loadSubjectTypes() {
      getCodeByType('SUBJECT_TYPE').then(response => {
        if (response.code === 200 && response.data) {
          this.subjectTypeOptions = response.data
            .filter(item => item.isStop === 0 || item.isStop === '0')
            .map(item => ({
              label: item.codeName,
              value: item.codeValue
            }))
        }
      })
    },
    loadDeptList() {
      getDeptList().then(response => {
        if (response.code === 200) {
          this.deptOptions = response.data || []
        }
      })
    },
    loadEmpList() {
      getEmployeeList().then(response => {
        if (response.code === 200) {
          this.empOptions = response.data || []
        }
      })
    },
    handleDeptChange(deptId) {
      const dept = this.deptOptions.find(d => d.deptId === deptId)
      if (dept) {
        this.form.manageDeptCode = dept.deptCode
        this.form.manageDeptName = dept.deptName
      }
    },
    handleEmpChange(empId) {
      const emp = this.empOptions.find(e => e.empId === empId)
      if (emp) {
        this.form.manageEmpCode = emp.empCode
        this.form.manageEmpName = emp.empName
      }
    },
    getSubjectTypeName(type) {
      const option = this.subjectTypeOptions.find(opt => opt.value === type)
      return option ? option.label : type
    },
    handleAdd() {
      this.dialogTitle = '新增预算主体'
      this.isEdit = false
      this.form = {
        subjectId: null,
        subjectCode: '',
        subjectName: '',
        subjectType: '',
        manageDeptId: null,
        manageDeptCode: '',
        manageDeptName: '',
        manageEmpId: null,
        manageEmpCode: '',
        manageEmpName: '',
        relatedDeptIds: [],
        createUser: this.currentUser.account || this.currentUser.empCode || this.currentUser.name || 'SYSTEM'
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑预算主体'
      this.isEdit = true
      // 加载关联的科室ID列表
      this.loadRelatedDepts(row.subjectId).then(relatedDeptIds => {
        this.form = {
          ...row,
          relatedDeptIds: relatedDeptIds || []
        }
      })
      this.dialogVisible = true
    },
    loadRelatedDepts(subjectId) {
      return getBudgetSubjectRelatedDepts(subjectId).then(response => {
        if (response.code === 200 && response.data) {
          return response.data.map(dept => dept.deptId)
        }
        return []
      }).catch(() => {
        return []
      })
    },
    handleStop(row) {
      this.$confirm('确认停用该预算主体吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        stopBudgetSubject(row.subjectId).then(response => {
          if (response.code === 200) {
            this.$message.success('停用成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '停用失败')
          }
        })
      })
    },
    handleStart(row) {
      this.$confirm('确认启用该预算主体吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        startBudgetSubject(row.subjectId).then(response => {
          if (response.code === 200) {
            this.$message.success('启用成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '启用失败')
          }
        })
      })
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
          // 设置创建人
          if (!this.isEdit) {
            this.form.createUser = this.currentUser.account || this.currentUser.empCode || this.currentUser.name || 'SYSTEM'
          }
          
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

