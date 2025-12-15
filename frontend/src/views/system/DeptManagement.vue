<template>
  <div class="dept-management">
    <el-card>
      <div slot="header" class="clearfix">
        <span>部门管理</span>
        <div style="float: right;">
          <el-button type="text" @click="handleDownloadTemplate">下载导入模板</el-button>
          <el-upload
            ref="upload"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :show-file-list="false"
            :before-upload="beforeUpload"
            accept=".xlsx,.xls"
            style="display: inline-block; margin-left: 10px;"
          >
            <el-button type="text">批量导入</el-button>
          </el-upload>
          <el-button style="margin-left: 10px; padding: 3px 0" type="text" @click="handleAdd">新增部门</el-button>
        </div>
      </div>
      
      <!-- 搜索条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="部门名称">
          <el-input v-model="searchForm.deptName" placeholder="请输入部门名称" clearable @keyup.enter.native="handleSearch"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table 
        :data="tableData" 
        border 
        style="width: 100%"
        :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
        row-key="deptId"
        :default-expand-all="false"
      >
        <el-table-column prop="deptCode" label="部门编码" width="120"></el-table-column>
        <el-table-column prop="deptName" label="部门名称" width="200" tree-node></el-table-column>
        <el-table-column prop="deptLevel" label="部门级别" width="100"></el-table-column>
        <el-table-column prop="deptPhone" label="部门电话" width="120"></el-table-column>
        <el-table-column label="部门负责人" width="150">
          <template slot-scope="scope">
            <span v-if="scope.row.deptManagerName">{{ scope.row.deptManagerName }}({{ scope.row.deptManagerCode }})</span>
            <span v-else style="color: #999;">未设置</span>
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
        <el-form-item label="部门负责人" prop="deptManagerId">
          <el-autocomplete
            v-model="form.deptManagerCode"
            :fetch-suggestions="searchEmployee"
            placeholder="请输入工号或姓名搜索"
            value-key="empCode"
            @select="handleEmployeeSelect"
            style="width: 100%"
          >
            <template slot-scope="{ item }">
              <div>{{ item.empCode }} - {{ item.empName }}</div>
            </template>
          </el-autocomplete>
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
import { getDeptList, saveDept, updateDept, deleteDept, getEmployeeList, getEmployeeByCode } from '@/api/user'
import Cookies from 'js-cookie'

export default {
  name: 'DeptManagement',
  data() {
    return {
      tableData: [],
      allTableData: [],
      searchForm: {
        deptName: ''
      },
      employeeList: [],
      dialogVisible: false,
      dialogTitle: '新增部门',
      isEdit: false,
      form: {
        deptId: null,
        deptCode: '',
        deptName: '',
        superDeptCode: '',
        deptLevel: 1,
        deptPhone: '',
        deptManagerId: null,
        deptManagerCode: ''
      },
      rules: {
        deptCode: [{ required: true, message: '请输入部门编码', trigger: 'blur' }],
        deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
      },
      uploadUrl: '/api/auth/dept/import',
      uploadHeaders: {}
    }
  },
  mounted() {
    this.loadData()
    this.loadEmployeeList()
    // 设置上传请求头
    const token = this.$store.state.user.token || Cookies.get('token')
    if (token) {
      this.uploadHeaders['Authorization'] = 'Bearer ' + token
    }
  },
  methods: {
    loadEmployeeList() {
      getEmployeeList().then(response => {
        if (response.code === 200) {
          this.employeeList = response.data || []
        }
      })
    },
    searchEmployee(queryString, cb) {
      const results = this.employeeList.filter(emp => {
        return (emp.empCode && emp.empCode.includes(queryString)) ||
               (emp.empName && emp.empName.includes(queryString))
      }).map(emp => ({
        empCode: emp.empCode,
        empName: emp.empName,
        empId: emp.empId
      }))
      cb(results)
    },
    handleEmployeeSelect(item) {
      this.form.deptManagerId = item.empId
      this.form.deptManagerCode = item.empCode
    },
    loadData() {
      getDeptList().then(response => {
        if (response.code === 200) {
          const allDepts = response.data || []
          this.allTableData = allDepts
          // 构建树形结构
          this.handleSearch()
        }
      })
    },
    handleSearch() {
      let filtered = [...this.allTableData]
      
      // 按部门名称筛选
      if (this.searchForm.deptName && this.searchForm.deptName.trim()) {
        const keyword = this.searchForm.deptName.trim().toLowerCase()
        filtered = filtered.filter(dept => 
          dept.deptName && dept.deptName.toLowerCase().includes(keyword)
        )
      }
      
      // 构建树形结构
      this.tableData = this.buildDeptTree(filtered)
    },
    handleReset() {
      this.searchForm.deptName = ''
      this.tableData = this.buildDeptTree(this.allTableData)
    },
    buildDeptTree(depts) {
      const deptMap = {}
      const tree = []
      
      // 创建映射
      depts.forEach(dept => {
        deptMap[dept.deptCode] = { ...dept, children: [] }
      })
      
      // 构建树
      depts.forEach(dept => {
        if (dept.superDeptCode && deptMap[dept.superDeptCode]) {
          // 有父部门，添加到父部门的children中
          deptMap[dept.superDeptCode].children.push(deptMap[dept.deptCode])
        } else {
          // 没有父部门或父部门不存在，作为根节点
          tree.push(deptMap[dept.deptCode])
        }
      })
      
      return tree
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
        deptPhone: '',
        deptManagerId: null,
        deptManagerCode: ''
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑部门'
      this.isEdit = true
      this.form = {
        deptId: row.deptId,
        deptCode: row.deptCode,
        deptName: row.deptName,
        superDeptCode: row.superDeptCode,
        deptLevel: row.deptLevel,
        deptPhone: row.deptPhone,
        deptManagerId: row.deptManagerId,
        deptManagerCode: row.deptManagerCode || ''
      }
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
    },
    handleDownloadTemplate() {
      // 下载Excel模板
      window.open('/api/auth/dept/template', '_blank')
    },
    beforeUpload(file) {
      const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || 
                      file.type === 'application/vnd.ms-excel'
      if (!isExcel) {
        this.$message.error('只能上传Excel文件！')
        return false
      }
      const isLt10M = file.size / 1024 / 1024 < 10
      if (!isLt10M) {
        this.$message.error('文件大小不能超过10MB！')
        return false
      }
      return true
    },
    handleUploadSuccess(response) {
      if (response.code === 200) {
        this.$message.success('导入成功')
        this.loadData()
      } else {
        this.$message.error(response.message || '导入失败')
      }
    },
    handleUploadError(error) {
      this.$message.error('导入失败：' + (error.message || '未知错误'))
    }
  }
}
</script>

<style scoped>
.dept-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>
