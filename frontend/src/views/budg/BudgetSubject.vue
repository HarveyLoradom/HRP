<template>
  <div class="budget-subject">
    <el-card>
      <div slot="header" class="clearfix">
        <span>预算主体管理</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增预算主体</el-button>
      </div>
      
      <!-- 查询条件 -->
      <div class="toolbar" style="margin-bottom: 10px;">
        <el-form :inline="true" :model="searchForm" size="small">
          <el-form-item label="状态">
            <el-select v-model="searchForm.isStop" placeholder="全部" clearable style="width: 120px" @change="loadData">
              <el-option label="启用" :value="0"></el-option>
              <el-option label="停用" :value="1"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
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
        <el-table-column prop="isStop" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isStop === 0 || scope.row.isStop === '0' ? 'success' : 'danger'">
              {{ scope.row.isStop === 0 || scope.row.isStop === '0' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="info" @click="handleView(scope.row)">查看</el-button>
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
              v-for="dept in allDepts"
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
          <el-input
            v-model="relatedDeptNames"
            placeholder="请选择关联科室（可多选）"
            readonly
            style="width: 100%"
            @click.native="deptTreeVisible = true"
          >
            <el-button slot="append" icon="el-icon-setting" @click.stop="deptTreeVisible = true"></el-button>
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </div>
    </el-dialog>

    <!-- 科室树形选择对话框 -->
    <el-dialog
      title="选择关联科室"
      :visible.sync="deptTreeVisible"
      width="500px"
      @open="handleDeptTreeOpen"
      @close="handleDeptTreeClose"
    >
      <div style="margin-bottom: 10px;">
        <el-button size="small" @click="handleSelectAllDepts">全选</el-button>
        <el-button size="small" @click="handleClearAllDepts">清空</el-button>
      </div>
      <el-tree
        ref="deptTree"
        :data="deptOptions"
        show-checkbox
        node-key="deptId"
        :props="{ children: 'children', label: 'deptName' }"
        :default-checked-keys="form.relatedDeptIds || []"
        check-strictly
      ></el-tree>
      <div slot="footer" class="dialog-footer">
        <el-button @click="deptTreeVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmDeptSelection">确定</el-button>
      </div>
    </el-dialog>

    <!-- 查看关联科室对话框 -->
    <el-dialog
      title="关联科室"
      :visible.sync="viewDeptTreeVisible"
      width="500px"
    >
      <div v-if="currentViewSubject" style="margin-bottom: 15px;">
        <p><strong>预算主体：</strong>{{ currentViewSubject.subjectName }}</p>
        <p><strong>主体编码：</strong>{{ currentViewSubject.subjectCode }}</p>
      </div>
      <el-tree
        :data="viewDeptTreeData"
        :props="{ children: 'children', label: 'deptName' }"
        default-expand-all
      ></el-tree>
      <div slot="footer" class="dialog-footer">
        <el-button @click="viewDeptTreeVisible = false">关闭</el-button>
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
      allDepts: [], // 所有部门的扁平列表
      deptTreeVisible: false, // 树形选择器显示状态
      relatedDeptNames: '', // 关联科室名称显示
      viewDeptTreeVisible: false, // 查看关联科室对话框显示状态
      viewDeptTreeData: [], // 查看对话框中的科室树数据
      currentViewSubject: null, // 当前查看的预算主体
      empOptions: [],
      searchForm: {
        isStop: null
      },
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
      const isStop = this.searchForm.isStop !== null && this.searchForm.isStop !== '' ? this.searchForm.isStop : null
      getBudgetSubjects(isStop).then(response => {
        if (response.code === 200) {
          this.tableData = response.data || []
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleReset() {
      this.searchForm.isStop = null
      this.loadData()
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
      // 查询所有启用的部门（isStop=0）
      getDeptList(0).then(response => {
        if (response.code === 200) {
          const allDepts = response.data || []
          // 构建树形结构
          this.deptOptions = this.buildDeptTree(allDepts)
          this.allDepts = allDepts // 保存扁平列表用于查找
        }
      })
    },
    buildDeptTree(depts) {
      // 构建部门树（使用superDeptCode作为父级关系）
      const deptMap = {}
      const tree = []
      
      // 先创建所有节点的映射（使用deptCode作为key）
      depts.forEach(dept => {
        deptMap[dept.deptCode] = {
          ...dept,
          children: []
        }
      })
      
      // 构建树结构
      depts.forEach(dept => {
        const node = deptMap[dept.deptCode]
        if (dept.superDeptCode && deptMap[dept.superDeptCode]) {
          // 有父部门，添加到父部门的children
          deptMap[dept.superDeptCode].children.push(node)
        } else {
          // 没有父部门或父部门不存在，作为根节点
          tree.push(node)
        }
      })
      
      return tree
    },
    loadEmpList() {
      getEmployeeList().then(response => {
        if (response.code === 200) {
          this.empOptions = response.data || []
        }
      })
    },
    handleDeptChange(deptId) {
      // 从扁平列表中查找（因为deptOptions是树形结构）
      const dept = this.allDepts.find(d => d.deptId === deptId)
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
      this.relatedDeptNames = ''
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
        this.updateRelatedDeptNames()
        this.dialogVisible = true
      }).catch(() => {
        // 如果加载失败，使用空数组
        this.form = {
          ...row,
          relatedDeptIds: []
        }
        this.updateRelatedDeptNames()
        this.dialogVisible = true
      })
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
        }).catch(err => {
          this.$message.error(err.message || '停用失败')
        })
      }).catch(() => {
        // 用户取消，不做任何处理
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
        }).catch(err => {
          this.$message.error(err.message || '启用失败')
        })
      }).catch(() => {
        // 用户取消，不做任何处理
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
    // 处理科室树选择确认
    handleConfirmDeptSelection() {
      const checkedKeys = this.$refs.deptTree.getCheckedKeys()
      const halfCheckedKeys = this.$refs.deptTree.getHalfCheckedKeys()
      // 合并选中和半选中的节点（使用check-strictly时，只取checkedKeys即可）
      this.form.relatedDeptIds = checkedKeys
      this.updateRelatedDeptNames()
      this.deptTreeVisible = false
    },
    // 全选科室
    handleSelectAllDepts() {
      const allDeptIds = this.allDepts.map(dept => dept.deptId)
      this.$refs.deptTree.setCheckedKeys(allDeptIds)
    },
    // 清空科室选择
    handleClearAllDepts() {
      this.$refs.deptTree.setCheckedKeys([])
    },
    // 更新关联科室名称显示
    updateRelatedDeptNames() {
      if (!this.form.relatedDeptIds || this.form.relatedDeptIds.length === 0) {
        this.relatedDeptNames = ''
        return
      }
      const names = this.form.relatedDeptIds
        .map(id => {
          const dept = this.allDepts.find(d => d.deptId === id)
          return dept ? dept.deptName : ''
        })
        .filter(name => name)
      this.relatedDeptNames = names.join('、')
    },
    // 处理科室树打开
    handleDeptTreeOpen() {
      // 对话框打开时，设置默认选中项
      this.$nextTick(() => {
        if (this.$refs.deptTree) {
          this.$refs.deptTree.setCheckedKeys(this.form.relatedDeptIds || [])
        }
      })
    },
    // 处理科室树关闭
    handleDeptTreeClose() {
      // 关闭时不保存选择，保持原状态
    },
    // 查看关联科室
    handleView(row) {
      this.currentViewSubject = row
      // 加载关联的科室
      getBudgetSubjectRelatedDepts(row.subjectId).then(response => {
        if (response.code === 200 && response.data) {
          const relatedDepts = response.data || []
          // 构建树形结构，只显示关联的科室及其父级路径
          this.viewDeptTreeData = this.buildViewDeptTree(relatedDepts)
        } else {
          this.viewDeptTreeData = []
        }
        this.viewDeptTreeVisible = true
      }).catch(() => {
        this.viewDeptTreeData = []
        this.viewDeptTreeVisible = true
      })
    },
    // 构建查看用的科室树（只显示关联的科室及其父级路径）
    buildViewDeptTree(relatedDepts) {
      if (!relatedDepts || relatedDepts.length === 0) {
        return []
      }
      
      // 获取所有关联科室的ID
      const relatedDeptIds = relatedDepts.map(dept => dept.deptId)
      
      // 构建完整的树结构，但只标记关联的科室
      const deptMap = {}
      const tree = []
      
      // 先创建所有节点的映射（使用deptCode作为key）
      this.allDepts.forEach(dept => {
        deptMap[dept.deptCode] = {
          ...dept,
          children: [],
          isRelated: relatedDeptIds.includes(dept.deptId)
        }
      })
      
      // 构建完整树结构
      this.allDepts.forEach(dept => {
        const node = deptMap[dept.deptCode]
        if (dept.superDeptCode && deptMap[dept.superDeptCode]) {
          deptMap[dept.superDeptCode].children.push(node)
        } else {
          tree.push(node)
        }
      })
      
      // 过滤树，只保留有关联科室的分支
      const filterTree = (nodes) => {
        return nodes
          .map(node => {
            const filteredChildren = filterTree(node.children)
            // 如果当前节点是关联的，或者有子节点是关联的，则保留
            if (node.isRelated || filteredChildren.length > 0) {
              return {
                ...node,
                children: filteredChildren
              }
            }
            return null
          })
          .filter(node => node !== null)
      }
      
      return filterTree(tree)
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

