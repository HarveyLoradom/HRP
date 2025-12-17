<template>
  <div class="budget-category">
    <el-card>
      <div slot="header" class="clearfix">
        <span>分类管理</span>
      </div>
      <el-tabs v-model="activeTab">
        <!-- 一级分类 -->
        <el-tab-pane label="一级分类" name="LEVEL1">
          <div class="toolbar">
            <el-form :inline="true" :model="search1" size="small">
              <el-form-item label="年度">
                <el-date-picker
                  v-model="search1.budgetYear"
                  type="year"
                  placeholder="选择年度"
                  format="yyyy"
                  value-format="yyyy"
                  style="width: 120px"
                  @change="loadLevel1"
                />
              </el-form-item>
              <el-form-item label="预算分类">
                <el-select v-model="search1.categoryType" placeholder="全部" clearable style="width: 140px" @change="loadLevel1">
                  <el-option label="收入预算" value="INCOME" />
                  <el-option label="支出预算" value="EXPENSE" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="small" @click="handleAddLevel1">新增一级分类</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table :data="level1List" border style="width: 100%" v-loading="loading1">
            <el-table-column prop="budgetYear" label="年度" width="100" />
            <el-table-column prop="categoryType" label="预算分类" width="120">
              <template slot-scope="scope">
                <span v-if="scope.row.categoryType === 'INCOME'">收入预算</span>
                <span v-else-if="scope.row.categoryType === 'EXPENSE'">支出预算</span>
                <span v-else>{{ scope.row.categoryType }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="categoryCode" label="分类编码" width="160" />
            <el-table-column prop="categoryName" label="分类名称" />
            <el-table-column prop="isStop" label="状态" width="80">
              <template slot-scope="scope">
                <el-tag :type="scope.row.isStop === 0 || scope.row.isStop === '0' ? 'success' : 'info'">
                  {{ scope.row.isStop === 0 || scope.row.isStop === '0' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template slot-scope="scope">
                <el-button size="mini" @click="handleEditLevel1(scope.row)">编辑</el-button>
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
          <div class="pagination">
            <el-pagination
              background
              layout="total, prev, pager, next, sizes"
              :total="pagination1.total"
              :page-size="pagination1.size"
              :current-page="pagination1.page"
              @size-change="handleSizeChange1"
              @current-change="handleCurrentChange1"
            />
          </div>
        </el-tab-pane>

        <!-- 二级分类 -->
        <el-tab-pane label="二级分类" name="LEVEL2">
          <div class="toolbar">
            <el-form :inline="true" :model="search2" size="small">
              <el-form-item label="年度">
                <el-date-picker
                  v-model="search2.budgetYear"
                  type="year"
                  placeholder="选择年度"
                  format="yyyy"
                  value-format="yyyy"
                  style="width: 120px"
                  @change="handleSearch2Change"
                />
              </el-form-item>
              <el-form-item label="预算分类">
                <el-select v-model="search2.categoryType" placeholder="全部" clearable style="width: 140px" @change="handleSearch2Change">
                  <el-option label="收入预算" value="INCOME" />
                  <el-option label="支出预算" value="EXPENSE" />
                </el-select>
              </el-form-item>
              <el-form-item label="上级分类">
                <el-select v-model="search2.parentCategoryId" placeholder="全部" clearable style="width: 200px" @change="loadLevel2">
                  <el-option
                    v-for="item in level1Options"
                    :key="item.categoryId"
                    :label="item.categoryName"
                    :value="item.categoryId"
                  />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="small" @click="handleAddLevel2">新增二级分类</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table :data="level2List" border style="width: 100%" v-loading="loading2">
            <el-table-column prop="budgetYear" label="年度" width="100" />
            <el-table-column prop="categoryType" label="预算分类" width="120">
              <template slot-scope="scope">
                <span v-if="scope.row.categoryType === 'INCOME'">收入预算</span>
                <span v-else-if="scope.row.categoryType === 'EXPENSE'">支出预算</span>
                <span v-else>{{ scope.row.categoryType }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="categoryCode" label="分类编码" width="180" />
            <el-table-column prop="categoryName" label="分类名称" />
            <el-table-column prop="isStop" label="状态" width="80">
              <template slot-scope="scope">
                <el-tag :type="scope.row.isStop === 0 || scope.row.isStop === '0' ? 'success' : 'info'">
                  {{ scope.row.isStop === 0 || scope.row.isStop === '0' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template slot-scope="scope">
                <el-button size="mini" @click="handleEditLevel2(scope.row)">编辑</el-button>
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
          <div class="pagination">
            <el-pagination
              background
              layout="total, prev, pager, next, sizes"
              :total="pagination2.total"
              :page-size="pagination2.size"
              :current-page="pagination2.page"
              @size-change="handleSizeChange2"
              @current-change="handleCurrentChange2"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="年度" prop="budgetYear">
          <el-date-picker
            v-model="form.budgetYear"
            type="year"
            placeholder="选择年度"
            format="yyyy"
            value-format="yyyy"
            style="width: 100%"
            :disabled="isLevel2"
          />
        </el-form-item>
        <el-form-item label="预算分类" prop="categoryType" v-if="!isLevel2">
          <el-radio-group v-model="form.categoryType">
            <el-radio label="INCOME">收入预算</el-radio>
            <el-radio label="EXPENSE">支出预算</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="上级分类" prop="parentCategoryId" v-if="isLevel2">
          <el-select v-model="form.parentCategoryId" placeholder="请选择上级分类" style="width: 100%" filterable>
            <el-option
              v-for="item in level1Options"
              :key="item.categoryId"
              :label="item.categoryName"
              :value="item.categoryId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
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
import {
  getLevel1CategoriesPage,
  getLevel2CategoriesPage,
  getLevel1CategoriesList,
  saveLevel1Category,
  saveLevel2Category,
  updateBudgetCategory,
  deleteBudgetCategory,
  stopBudgetCategory,
  startBudgetCategory
} from '@/api/budg'

export default {
  name: 'BudgetCategory',
  data() {
    return {
      activeTab: 'LEVEL1',
      loading1: false,
      loading2: false,
      level1List: [],
      level2List: [],
      level1Options: [],
      search1: {
        budgetYear: new Date().getFullYear().toString(),
        categoryType: ''
      },
      search2: {
        budgetYear: new Date().getFullYear().toString(),
        categoryType: '',
        parentCategoryId: null
      },
      pagination1: {
        page: 1,
        size: 10,
        total: 0
      },
      pagination2: {
        page: 1,
        size: 10,
        total: 0
      },
      dialogVisible: false,
      dialogTitle: '新增分类',
      form: {
        categoryId: null,
        categoryName: '',
        budgetYear: new Date().getFullYear().toString(),
        categoryType: 'INCOME',
        parentCategoryId: null,
        categoryLevel: 1
      },
      isLevel2: false,
      rules: {
        budgetYear: [{ required: true, message: '请选择年度', trigger: 'change' }],
        categoryType: [{ required: true, message: '请选择预算分类', trigger: 'change' }],
        categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
        parentCategoryId: [{ required: true, message: '请选择上级分类', trigger: 'change' }]
      }
    }
  },
  mounted() {
    this.loadLevel1()
    this.loadLevel2()
    this.loadLevel1Options()
  },
  methods: {
    loadLevel1() {
      this.loading1 = true
      getLevel1CategoriesPage(this.pagination1.page, this.pagination1.size, this.search1.budgetYear, this.search1.categoryType)
        .then(res => {
          if (res.code === 200 && res.data) {
            const records = res.data.records || res.data.list || res.data.rows || []
            this.level1List = records
            this.pagination1.total = res.data.total || records.length || 0
          }
          this.loading1 = false
        })
        .catch(() => {
          this.loading1 = false
        })
    },
    loadLevel2() {
      this.loading2 = true
      getLevel2CategoriesPage(
        this.pagination2.page,
        this.pagination2.size,
        this.search2.budgetYear,
        this.search2.categoryType,
        this.search2.parentCategoryId
      ).then(res => {
        if (res.code === 200 && res.data) {
          const records = res.data.records || res.data.list || res.data.rows || []
          this.level2List = records
          this.pagination2.total = res.data.total || records.length || 0
        }
        this.loading2 = false
      }).catch(() => {
        this.loading2 = false
      })
    },
    loadLevel1Options() {
      getLevel1CategoriesList(this.search2.budgetYear, this.search2.categoryType).then(res => {
        if (res.code === 200 && res.data) {
          this.level1Options = res.data || []
        }
      })
    },
    handleSizeChange1(size) {
      this.pagination1.size = size
      this.loadLevel1()
    },
    handleCurrentChange1(page) {
      this.pagination1.page = page
      this.loadLevel1()
    },
    handleSizeChange2(size) {
      this.pagination2.size = size
      this.loadLevel2()
    },
    handleCurrentChange2(page) {
      this.pagination2.page = page
      this.loadLevel2()
    },
    handleSearch2Change() {
      this.loadLevel1Options()
      this.loadLevel2()
    },
    handleAddLevel1() {
      this.dialogTitle = '新增一级分类'
      this.isLevel2 = false
      this.form = {
        categoryId: null,
        categoryName: '',
        budgetYear: this.search1.budgetYear || new Date().getFullYear().toString(),
        categoryType: this.search1.categoryType || 'INCOME',
        parentCategoryId: null,
        categoryLevel: 1
      }
      this.dialogVisible = true
    },
    handleAddLevel2() {
      this.dialogTitle = '新增二级分类'
      this.isLevel2 = true
      this.form = {
        categoryId: null,
        categoryName: '',
        budgetYear: this.search2.budgetYear || new Date().getFullYear().toString(),
        categoryType: this.search2.categoryType || 'INCOME',
        parentCategoryId: this.search2.parentCategoryId,
        categoryLevel: 2
      }
      this.dialogVisible = true
    },
    handleEditLevel1(row) {
      this.dialogTitle = '编辑一级分类'
      this.isLevel2 = false
      this.form = { ...row, categoryLevel: 1 }
      this.dialogVisible = true
    },
    handleEditLevel2(row) {
      this.dialogTitle = '编辑二级分类'
      this.isLevel2 = true
      this.form = { ...row, categoryLevel: 2 }
      this.dialogVisible = true
    },
    handleStop(row) {
      this.$confirm('确认停用该分类吗？', '提示', {
        type: 'warning'
      }).then(() => {
        stopBudgetCategory(row.categoryId).then(res => {
          if (res.code === 200) {
            this.$message.success('停用成功')
            this.reloadCurrentTab()
          } else {
            this.$message.error(res.message || '停用失败')
          }
        })
      })
    },
    handleStart(row) {
      this.$confirm('确认启用该分类吗？', '提示', {
        type: 'warning'
      }).then(() => {
        startBudgetCategory(row.categoryId).then(res => {
          if (res.code === 200) {
            this.$message.success('启用成功')
            this.reloadCurrentTab()
          } else {
            this.$message.error(res.message || '启用失败')
          }
        })
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该分类吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteBudgetCategory(row.categoryId).then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.reloadCurrentTab()
          } else {
            this.$message.error(res.message || '删除失败')
          }
        })
      })
    },
    reloadCurrentTab() {
      if (this.activeTab === 'LEVEL1') {
        this.loadLevel1()
        this.loadLevel1Options()
      } else {
        this.loadLevel2()
      }
    },
    handleSave() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const isNew = !this.form.categoryId
        const api = isNew ? (this.isLevel2 ? saveLevel2Category : saveLevel1Category) : updateBudgetCategory
        api(this.form).then(res => {
          if (res.code === 200) {
            this.$message.success(isNew ? '新增成功' : '更新成功')
            this.dialogVisible = false
            this.reloadCurrentTab()
          } else {
            this.$message.error(res.message || '操作失败')
          }
        })
      })
    }
  }
}
</script>

<style scoped>
.budget-category {
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

