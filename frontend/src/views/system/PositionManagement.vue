<template>
  <div class="position-management">
    <el-card>
      <div slot="header" class="clearfix">
        <span>岗位管理</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增岗位</el-button>
      </div>
      
      <!-- 搜索条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="岗位名称">
          <el-input v-model="searchForm.positionName" placeholder="请输入岗位名称" clearable @keyup.enter.native="handleSearch"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.isStop"
            placeholder="请选择状态"
            clearable
            style="width: 150px;"
            @change="loadData"
          >
            <el-option label="启用" :value="0"></el-option>
            <el-option label="停用" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="positionCode" label="岗位编码" width="150"></el-table-column>
        <el-table-column prop="positionName" label="岗位名称" width="200"></el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isStop === 0 ? 'success' : 'danger'">
              {{ scope.row.isStop === 0 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="primary" @click="handleAssignUsers(scope.row)">分配人员</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="岗位编码" prop="positionCode">
          <el-input v-model="form.positionCode" placeholder="请输入岗位编码" :disabled="isEdit"></el-input>
        </el-form-item>
        <el-form-item label="岗位名称" prop="positionName">
          <el-input v-model="form.positionName" placeholder="请输入岗位名称"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="isStop">
          <el-radio-group v-model="form.isStop">
            <el-radio :label="0">启用</el-radio>
            <el-radio :label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>

    <!-- 分配人员对话框 -->
    <el-dialog title="分配人员" :visible.sync="assignDialogVisible" width="600px" @open="handleAssignDialogOpen">
      <el-transfer
        :key="transferKey"
        v-model="selectedUserIds"
        :data="allUsers"
        :titles="['可选人员', '已选人员']"
        :props="{
          key: 'id',
          label: 'name'
        }"
        filterable
        filter-placeholder="请输入姓名或工号搜索"
      ></el-transfer>
      <div slot="footer" class="dialog-footer">
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAssign">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getPositionList, savePosition, updatePosition, deletePosition, assignPositionUsers, getPositionUsers, getUserList } from '@/api/user'

export default {
  name: 'PositionManagement',
  data() {
    return {
      loading: false,
      tableData: [],
      allTableData: [],
      searchForm: {
        positionName: '',
        isStop: null
      },
      dialogVisible: false,
      dialogTitle: '新增岗位',
      isEdit: false,
      form: {
        positionId: null,
        positionCode: '',
        positionName: '',
        isStop: 0
      },
      rules: {
        positionCode: [{ required: true, message: '请输入岗位编码', trigger: 'blur' }],
        positionName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }]
      },
      assignDialogVisible: false,
      currentPosition: null,
      allUsers: [],
      selectedUserIds: []
    }
  },
  mounted() {
    this.loadData()
    this.loadAllUsers()
  },
  methods: {
    loadData() {
      this.loading = true
      getPositionList(this.searchForm.isStop !== null && this.searchForm.isStop !== '' ? this.searchForm.isStop : null).then(response => {
        if (response.code === 200) {
          let filtered = response.data || []
          
          // 按岗位名称筛选（前端筛选）
          if (this.searchForm.positionName && this.searchForm.positionName.trim()) {
            const keyword = this.searchForm.positionName.trim().toLowerCase()
            filtered = filtered.filter(pos => 
              pos.positionName && pos.positionName.toLowerCase().includes(keyword)
            )
          }
          
          this.allTableData = response.data || []
          this.tableData = filtered
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleSearch() {
      this.loadData()
    },
    handleReset() {
      this.searchForm.positionName = ''
      this.searchForm.isStop = null
      this.loadData()
    },
    loadAllUsers() {
      // 先清空旧数据
      this.allUsers = []
      // 只获取启用状态的用户（isStop === 0 或 null）
      getUserList(0).then(response => {
        if (response.code === 200 && response.data) {
          // 确保只显示有效的、未停用的用户（有 id 和 account，且 isStop === 0）
          this.allUsers = (response.data || [])
            .filter(user => {
              return user && 
                     user.id && 
                     user.account && 
                     (user.isStop === 0 || user.isStop === null || user.isStop === undefined)
            })
            .map(user => ({
              id: user.id,
              name: `${user.account} - ${user.name || ''}`
            }))
        } else {
          this.allUsers = []
        }
      }).catch(() => {
        this.allUsers = []
      })
    },
    handleAdd() {
      this.dialogTitle = '新增岗位'
      this.isEdit = false
      this.form = {
        positionId: null,
        positionCode: '',
        positionName: '',
        isStop: 0
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑岗位'
      this.isEdit = true
      this.form = {
        positionId: row.positionId,
        positionCode: row.positionCode,
        positionName: row.positionName,
        isStop: row.isStop
      }
      this.dialogVisible = true
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const api = this.isEdit ? updatePosition : savePosition
          api(this.form).then(response => {
            if (response.code === 200) {
              this.$message.success(response.message || '操作成功')
              this.dialogVisible = false
              this.loadData()
            } else {
              this.$message.error(response.message || '操作失败')
            }
          }).catch(error => {
            this.$message.error('操作失败：' + (error.message || '未知错误'))
          })
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该岗位吗？删除后该岗位的人员关联也会被删除。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deletePosition(row.positionId).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        }).catch(error => {
          this.$message.error('删除失败：' + (error.message || '未知错误'))
        })
      }).catch(() => {})
    },
    handleAssignUsers(row) {
      this.currentPosition = row
      this.selectedUserIds = []
      // 更新 key 强制组件重新渲染
      this.transferKey = new Date().getTime()
      // 先清空用户列表
      this.allUsers = []
      // 同时加载用户列表和岗位已有的人员
      Promise.all([
        // 重新加载用户列表，确保数据是最新的
        new Promise((resolve) => {
          getUserList(0).then(response => {
            if (response.code === 200 && response.data) {
              // 确保只显示有效的、未停用的用户（有 id 和 account，且 isStop === 0）
              this.allUsers = (response.data || [])
                .filter(user => {
                  return user && 
                         user.id && 
                         user.account && 
                         (user.isStop === 0 || user.isStop === null || user.isStop === undefined)
                })
                .map(user => ({
                  id: user.id,
                  name: `${user.account} - ${user.name || ''}`
                }))
            } else {
              this.allUsers = []
            }
            resolve()
          }).catch(() => {
            this.allUsers = []
            resolve()
          })
        }),
        // 加载该岗位已有的人员
        getPositionUsers(row.positionId).then(response => {
          if (response.code === 200 && response.data) {
            this.selectedUserIds = response.data.map(user => user.id).filter(id => id != null)
          }
          return Promise.resolve()
        }).catch(() => {
          return Promise.resolve()
        })
      ]).then(() => {
        // 数据加载完成后再打开对话框
        this.assignDialogVisible = true
      })
    },
    handleAssignDialogOpen() {
      // 对话框打开时，再次确保加载最新数据
      if (this.currentPosition) {
        this.loadAllUsers()
      }
    },
    handleSaveAssign() {
      if (!this.currentPosition) {
        return
      }
      assignPositionUsers(this.currentPosition.positionId, this.selectedUserIds).then(response => {
        if (response.code === 200) {
          this.$message.success('分配成功')
          this.assignDialogVisible = false
          this.loadData()
        } else {
          this.$message.error(response.message || '分配失败')
        }
      }).catch(error => {
        this.$message.error('分配失败：' + (error.message || '未知错误'))
      })
    }
  }
}
</script>

<style scoped>
.position-management {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>
