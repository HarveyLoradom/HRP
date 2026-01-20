<template>
  <div class="budget-item">
    <el-card>
      <div slot="header" class="clearfix">
        <span>项目预算</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增项目预算</el-button>
      </div>
      
      <!-- 查询条件 -->
      <div class="toolbar" style="margin-bottom: 10px;">
        <el-form :inline="true" :model="searchForm" size="small">
          <el-form-item label="年度">
            <el-date-picker
              v-model="searchForm.budgetYear"
              type="year"
              placeholder="选择年度"
              format="yyyy"
              value-format="yyyy"
              style="width: 120px"
              clearable
              @change="handleYearChange"
            />
          </el-form-item>
          <el-form-item label="预算分类">
            <el-select v-model="searchForm.categoryType" placeholder="全部" clearable style="width: 140px" @change="handleCategoryTypeChange">
              <el-option label="收入预算" value="INCOME" />
              <el-option label="支出预算" value="EXPENSE" />
            </el-select>
          </el-form-item>
          <el-form-item label="一级分类">
            <el-select v-model="searchForm.level1CategoryId" placeholder="全部" clearable style="width: 180px" @change="handleLevel1CategoryChange">
              <el-option
                v-for="category in level1CategoryOptions"
                :key="category.categoryId"
                :label="category.categoryName"
                :value="category.categoryId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="二级分类">
            <el-select v-model="searchForm.level2CategoryId" placeholder="全部" clearable style="width: 180px" @change="loadData">
              <el-option
                v-for="category in level2CategoryOptions"
                :key="category.categoryId"
                :label="category.categoryName"
                :value="category.categoryId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="项目名称">
            <el-input v-model="searchForm.itemName" placeholder="请输入项目名称" clearable style="width: 200px" @keyup.enter.native="loadData" />
          </el-form-item>
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
      
      <!-- 数据表格 -->
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="level1CategoryName" label="一级分类" width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="level2CategoryName" label="二级分类" width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="itemName" label="项目名称" width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="itemCode" label="项目编码" width="180"></el-table-column>
        <el-table-column prop="assignedSubjects" label="分配主体" width="200">
          <template slot-scope="scope">
            <el-tag v-for="subject in scope.row.assignedSubjects" :key="subject.subjectId" size="mini" style="margin-right: 5px;">
              {{ subject.subjectName }}
            </el-tag>
            <span v-if="!scope.row.assignedSubjects || scope.row.assignedSubjects.length === 0" style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="isStop" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isStop === 0 || scope.row.isStop === '0' ? 'success' : 'info'">
              {{ scope.row.isStop === 0 || scope.row.isStop === '0' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" @click="handleAssignSubject(scope.row)">分配主体</el-button>
            <el-button
              size="mini"
              type="warning"
              v-if="scope.row.isStop === 0 || scope.row.isStop === '0'"
              @click="handleStop(scope.row)"
            >停用</el-button>
            <el-button
              size="mini"
              type="success"
              v-else
              @click="handleStart(scope.row)"
            >启用</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination" style="margin-top: 10px; text-align: right;">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          :page-size="pagination.size"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-form-item label="上级分类" prop="parentCategoryId">
          <el-select 
            v-model="form.parentCategoryId" 
            placeholder="请选择上级分类（二级分类）" 
            style="width: 100%" 
            filterable
          >
            <el-option
              v-for="category in parentCategoryOptions"
              :key="category.categoryId"
              :label="category.categoryName"
              :value="category.categoryId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="项目名称" prop="itemName">
          <el-input v-model="form.itemName" placeholder="请输入项目名称"></el-input>
        </el-form-item>
        <el-form-item label="项目编码" v-if="isEdit">
          <el-input v-model="form.itemCode" disabled></el-input>
        </el-form-item>
        
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </div>
    </el-dialog>

    <!-- 分配主体对话框 -->
    <el-dialog title="分配主体" :visible.sync="assignDialogVisible" width="600px">
      <el-form label-width="120px">
        <el-form-item label="预算项目">
          <el-input :value="currentItem.itemName" disabled></el-input>
        </el-form-item>
        <el-form-item label="分配主体">
          <el-select 
            v-model="selectedSubjectId" 
            placeholder="请选择主体" 
            style="width: 100%" 
            filterable
            clearable
          >
            <el-option
              v-for="subject in allSubjects"
              :key="subject.subjectId"
              :label="subject.subjectName"
              :value="subject.subjectId"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAssign">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getBudgetItemsPage, saveBudgetItem, updateBudgetItem, deleteBudgetItem, stopBudgetItem, startBudgetItem, assignBudgetItemSubjects } from '@/api/budg'
import { getBudgetSubjects, getLevel1CategoriesList, getLevel2CategoriesList } from '@/api/budg'

export default {
  name: 'BudgetProject',
  data() {
    return {
      loading: false,
      tableData: [],
      allSubjects: [],
      level1CategoryOptions: [], // 一级分类列表
      level2CategoryOptions: [], // 二级分类列表
      categoryOptions: [], // 二级分类列表（用于新增/编辑对话框）
      dialogVisible: false,
      assignDialogVisible: false,
      dialogTitle: '新增项目预算',
      isEdit: false,
      currentItem: {},
      selectedSubjectId: null,
      searchForm: {
        budgetYear: new Date().getFullYear().toString(),
        categoryType: '',
        level1CategoryId: null,
        level2CategoryId: null,
        itemName: '',
        isStop: null
      },
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      form: {
        itemId: null,
        itemCode: '',
        itemName: '',
        parentCategoryId: null
      },
      rules: {
        itemName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
        parentCategoryId: [{ required: true, message: '请选择上级分类（二级分类）', trigger: 'change' }]
      }
    }
  },
  computed: {
    parentCategoryOptions() {
      // 只展示二级分类
      return this.categoryOptions
    }
  },
  mounted() {
    this.loadLevel1Categories()
    this.loadLevel2Categories()
    this.loadData()
    this.loadSubjects()
    this.loadCategories()
  },
  methods: {
    loadData() {
      this.loading = true
      const params = {
        budgetYear: this.searchForm.budgetYear || null,
        categoryType: this.searchForm.categoryType || null,
        level1CategoryId: this.searchForm.level1CategoryId || null,
        level2CategoryId: this.searchForm.level2CategoryId || null,
        itemName: this.searchForm.itemName || null,
        isStop: this.searchForm.isStop !== null && this.searchForm.isStop !== '' ? this.searchForm.isStop : null
      }
      getBudgetItemsPage(this.pagination.page, this.pagination.size, params).then(response => {
        if (response.code === 200 && response.data) {
          const records = response.data.records || response.data.list || response.data.rows || []
          this.tableData = records
          this.pagination.total = response.data.total || 0
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    loadSubjects() {
      getBudgetSubjects().then(response => {
        if (response.code === 200) {
          this.allSubjects = response.data || []
        }
      })
    },
    // 加载一级分类列表（用于查询）
    loadLevel1Categories() {
      const year = this.searchForm.budgetYear || new Date().getFullYear().toString()
      const type = this.searchForm.categoryType || null
      getLevel1CategoriesList(year, type, 0).then(response => {
        if (response.code === 200 && response.data) {
          this.level1CategoryOptions = response.data || []
        }
      })
    },
    // 加载二级分类列表（用于查询）
    loadLevel2Categories() {
      const year = this.searchForm.budgetYear || new Date().getFullYear().toString()
      const type = this.searchForm.categoryType || null
      const parentId = this.searchForm.level1CategoryId || null
      getLevel2CategoriesList(year, type, parentId, 0).then(response => {
        if (response.code === 200 && response.data) {
          this.level2CategoryOptions = response.data || []
        }
      })
    },
    // 加载二级分类列表（作为上级分类，用于新增/编辑对话框）
    loadCategories() {
      const year = this.searchForm.budgetYear || new Date().getFullYear().toString()
      const type = this.searchForm.categoryType || null
      getLevel2CategoriesList(year, type, null, 0).then(response => {
        if (response.code === 200 && response.data) {
          this.categoryOptions = response.data || []
        }
      })
    },
    handleYearChange() {
      // 年度改变时，重新加载分类选项
      this.searchForm.level1CategoryId = null
      this.searchForm.level2CategoryId = null
      this.loadLevel1Categories()
      this.loadLevel2Categories()
      this.loadData()
    },
    handleCategoryTypeChange() {
      // 预算分类改变时，清空并重新加载分类选项
      this.searchForm.level1CategoryId = null
      this.searchForm.level2CategoryId = null
      this.loadLevel1Categories()
      this.loadLevel2Categories()
      this.loadData()
    },
    handleLevel1CategoryChange() {
      // 一级分类改变时，清空二级分类并重新加载
      this.searchForm.level2CategoryId = null
      this.loadLevel2Categories()
      this.loadData()
    },
    handleSizeChange(size) {
      this.pagination.size = size
      this.pagination.page = 1
      this.loadData()
    },
    handleCurrentChange(page) {
      this.pagination.page = page
      this.loadData()
    },
    handleReset() {
      this.searchForm = {
        budgetYear: new Date().getFullYear().toString(),
        categoryType: '',
        level1CategoryId: null,
        level2CategoryId: null,
        itemName: '',
        isStop: null
      }
      this.pagination.page = 1
      this.loadLevel1Categories()
      this.loadLevel2Categories()
      this.loadData()
      this.loadCategories()
    },
    handleAdd() {
      this.dialogTitle = '新增项目预算'
      this.isEdit = false
      this.form = {
        itemId: null,
        itemCode: '',
        itemName: '',
        parentCategoryId: null
      }
      this.dialogVisible = true
      this.loadCategories()
    },
    handleEdit(row) {
      this.dialogTitle = '编辑项目预算'
      this.isEdit = true
      this.form = {
        itemId: row.itemId,
        itemCode: row.itemCode,
        itemName: row.itemName,
        parentCategoryId: row.categoryId // 使用categoryId（二级分类ID）作为parentCategoryId
      }
      this.dialogVisible = true
      this.loadCategories()
    },
    handleAssignSubject(row) {
      this.currentItem = row
      // 单选：只取第一个分配的主体，如果没有则设为null
      const assignedSubjects = row.assignedSubjects || []
      this.selectedSubjectId = assignedSubjects.length > 0 ? assignedSubjects[0].subjectId : null
      this.assignDialogVisible = true
    },
    handleSaveAssign() {
      if (!this.currentItem || !this.currentItem.itemId) {
        this.$message.error('预算项目信息不存在')
        return
      }
      
      // 将单个ID转换为数组（后端API需要数组格式）
      const subjectIds = this.selectedSubjectId ? [this.selectedSubjectId] : []
      
      assignBudgetItemSubjects(this.currentItem.itemId, subjectIds).then(response => {
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
    },
    handleStop(row) {
      this.$confirm('确认停用该项目预算吗？', '提示', {
        type: 'warning'
      }).then(() => {
        stopBudgetItem(row.itemId).then(response => {
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
      this.$confirm('确认启用该项目预算吗？', '提示', {
        type: 'warning'
      }).then(() => {
        startBudgetItem(row.itemId).then(response => {
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
      this.$confirm('确认删除该项目预算吗？', '提示', {
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
          // 新增时不传itemCode，后端自动生成
          const formData = { ...this.form }
          if (!this.isEdit) {
            delete formData.itemCode
          }
          
          const api = this.isEdit ? updateBudgetItem : saveBudgetItem
          api(formData).then(response => {
            if (response.code === 200) {
              this.$message.success(this.isEdit ? '更新成功' : '新增成功')
              this.dialogVisible = false
              this.loadData()
            } else {
              this.$message.error(response.message || '操作失败')
            }
          }).catch(error => {
            this.$message.error(error.message || '操作失败')
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
.toolbar {
  margin-bottom: 10px;
}
.pagination {
  margin-top: 10px;
  text-align: right;
}
</style>


