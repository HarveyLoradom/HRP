<template>
  <div class="budget-item">
    <el-card>
      <div slot="header" class="clearfix">
        <span>项目预算</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增项目预算</el-button>
      </div>
      
      <!-- Tab页签：收入预算和支出预算 -->
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="收入预算" name="INCOME">
          <el-table :data="filteredTableData" border style="width: 100%" v-loading="loading" row-key="itemId" :tree-props="{children: 'children'}">
            <el-table-column prop="itemName" label="项目名称" width="300" show-overflow-tooltip>
              <template slot-scope="scope">
                <span v-if="scope.row.categoryId">{{ scope.row.itemName }}</span>
                <span v-else style="font-weight: bold;">{{ scope.row.itemName }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="itemCode" label="项目编码" width="150"></el-table-column>
            <el-table-column prop="budgetYear" label="年度" width="100"></el-table-column>
            <el-table-column prop="assignedSubjects" label="分配主体" width="200">
              <template slot-scope="scope">
                <el-tag v-for="subject in scope.row.assignedSubjects" :key="subject.subjectId" size="mini" style="margin-right: 5px;">
                  {{ subject.subjectName }}
                </el-tag>
                <span v-if="!scope.row.assignedSubjects || scope.row.assignedSubjects.length === 0" style="color: #999;">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template slot-scope="scope">
                <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
                <el-button size="mini" @click="handleAssignSubject(scope.row)">分配主体</el-button>
                <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="支出预算" name="EXPENSE">
          <el-table :data="filteredTableData" border style="width: 100%" v-loading="loading" row-key="itemId" :tree-props="{children: 'children'}">
            <el-table-column prop="itemName" label="项目名称" width="300" show-overflow-tooltip>
              <template slot-scope="scope">
                <span v-if="scope.row.categoryId">{{ scope.row.itemName }}</span>
                <span v-else style="font-weight: bold;">{{ scope.row.itemName }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="itemCode" label="项目编码" width="150"></el-table-column>
            <el-table-column prop="budgetYear" label="年度" width="100"></el-table-column>
            <el-table-column prop="assignedSubjects" label="分配主体" width="200">
              <template slot-scope="scope">
                <el-tag v-for="subject in scope.row.assignedSubjects" :key="subject.subjectId" size="mini" style="margin-right: 5px;">
                  {{ subject.subjectName }}
                </el-tag>
                <span v-if="!scope.row.assignedSubjects || scope.row.assignedSubjects.length === 0" style="color: #999;">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template slot-scope="scope">
                <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
                <el-button size="mini" @click="handleAssignSubject(scope.row)">分配主体</el-button>
                <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="700px">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-form-item label="项目名称" prop="itemName">
          <el-input v-model="form.itemName" placeholder="请输入项目名称"></el-input>
        </el-form-item>
        <el-form-item label="项目编码" prop="itemCode">
          <el-input v-model="form.itemCode" placeholder="自动生成，可编辑"></el-input>
        </el-form-item>
        <el-form-item label="预算年度" prop="budgetYear">
          <el-date-picker
            v-model="form.budgetYear"
            type="year"
            placeholder="选择年度"
            format="yyyy"
            value-format="yyyy"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="预算分类" prop="categoryType">
          <el-radio-group v-model="form.categoryType">
            <el-radio label="INCOME">收入预算</el-radio>
            <el-radio label="EXPENSE">支出预算</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="上级分类" prop="parentCategoryId">
          <el-select 
            v-model="form.parentCategoryId" 
            placeholder="请选择上级分类（二级分类）" 
            style="width: 100%" 
            filterable
            clearable
          >
            <el-option
              v-for="category in parentCategoryOptions"
              :key="category.categoryId"
              :label="category.categoryName"
              :value="category.categoryId"
            ></el-option>
          </el-select>
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
            v-model="selectedSubjectIds" 
            placeholder="请选择主体（可多选）" 
            style="width: 100%" 
            multiple
            filterable
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
import { getBudgetItems, saveBudgetItem, updateBudgetItem, deleteBudgetItem } from '@/api/budg'
import { getBudgetSubjects, getLevel2CategoriesList } from '@/api/budg'
import { paginationMixin } from '@/mixins/pagination'

export default {
  name: 'BudgetProject',
  mixins: [paginationMixin],
  data() {
    return {
      loading: false,
      tableData: [],
      allData: [],
      allSubjects: [],
      categoryOptions: [], // 二级分类列表
      activeTab: 'INCOME',
      dialogVisible: false,
      assignDialogVisible: false,
      dialogTitle: '新增项目预算',
      isEdit: false,
      currentItem: {},
      selectedSubjectIds: [],
      form: {
        itemId: null,
        itemCode: '',
        itemName: '',
        budgetYear: new Date().getFullYear().toString(),
        categoryType: 'INCOME',
        categoryId: null,
        parentCategoryId: null
      },
      rules: {
        itemName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
        budgetYear: [{ required: true, message: '请选择预算年度', trigger: 'change' }],
        categoryType: [{ required: true, message: '请选择预算分类', trigger: 'change' }],
        parentCategoryId: [{ required: true, message: '请选择上级分类（二级分类）', trigger: 'change' }]
      }
    }
  },
  computed: {
    filteredTableData() {
      return this.tableData.filter(item => item.categoryType === this.activeTab)
    },
    parentCategoryOptions() {
      // 只展示二级分类
      return this.categoryOptions
    }
  },
  mounted() {
    this.loadData()
    this.loadSubjects()
    this.loadCategories()
  },
  methods: {
    loadData() {
      this.loading = true
      getBudgetItems().then(response => {
        if (response.code === 200) {
          this.allData = response.data || []
          this.buildTreeData()
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
    // 加载二级分类列表（作为上级分类）
    loadCategories() {
      const year = this.form.budgetYear
      const type = this.form.categoryType
      getLevel2CategoriesList(year, type, null).then(response => {
        if (response.code === 200 && response.data) {
          this.categoryOptions = response.data || []
        }
      })
    },
    buildTreeData() {
      const map = {}
      const tree = []
      
      this.allData.forEach(item => {
        map[item.itemId] = { ...item, children: [], assignedSubjects: item.assignedSubjects || [] }
      })
      
      this.allData.forEach(item => {
        if (item.parentCategoryId && map[item.parentCategoryId]) {
          map[item.parentCategoryId].children.push(map[item.itemId])
        } else {
          tree.push(map[item.itemId])
        }
      })
      
      this.tableData = tree
    },
    handleTabClick(tab) {
      this.activeTab = tab.name
      this.form.categoryType = tab.name
      this.loadCategories()
    },
    handleAdd() {
      this.dialogTitle = '新增项目预算'
      this.isEdit = false
      this.form = {
        itemId: null,
        itemCode: '',
        itemName: '',
        budgetYear: new Date().getFullYear().toString(),
        categoryType: this.activeTab,
        categoryId: null,
        parentCategoryId: null
      }
      this.dialogVisible = true
      this.loadCategories()
    },
    handleEdit(row) {
      this.dialogTitle = '编辑项目预算'
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
      this.loadCategories()
    },
    handleAssignSubject(row) {
      this.currentItem = row
      this.selectedSubjectIds = (row.assignedSubjects || []).map(s => s.subjectId)
      this.assignDialogVisible = true
    },
    handleSaveAssign() {
      // TODO: 调用API保存分配的主体
      this.$message.success('分配成功')
      this.assignDialogVisible = false
      this.loadData()
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


