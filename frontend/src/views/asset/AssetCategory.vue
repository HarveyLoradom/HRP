<template>
  <div class="asset-category">
    <el-card>
      <div slot="header" class="clearfix">
        <span>资产分类管理</span>
      </div>
      <el-tabs v-model="activeTab">
        <!-- 一级分类 -->
        <el-tab-pane label="一级分类" name="LEVEL1">
          <div class="toolbar">
            <el-form :inline="true" :model="search1" size="small">
              <el-form-item label="分类名称">
                <el-input v-model="search1.categoryName" placeholder="请输入分类名称" clearable style="width: 200px" @keyup.enter.native="loadLevel1" />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="search1.status" placeholder="全部" clearable style="width: 120px" @change="loadLevel1">
                  <el-option label="启用" :value="1"></el-option>
                  <el-option label="禁用" :value="0"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="small" @click="loadLevel1">查询</el-button>
                <el-button size="small" @click="handleResetSearch1">重置</el-button>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="small" @click="handleAddLevel1">新增一级分类</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table :data="level1List" border style="width: 100%" v-loading="loading1">
            <el-table-column prop="categoryCode" label="分类编码" width="180" />
            <el-table-column prop="categoryName" label="分类名称" />
            <el-table-column prop="status" label="状态" width="80">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
                  {{ scope.row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template slot-scope="scope">
                <el-button size="mini" @click="handleEditLevel1(scope.row)">编辑</el-button>
                <el-button
                  size="mini"
                  type="warning"
                  v-if="scope.row.status === 1"
                  @click="handleStop(scope.row)"
                >禁用</el-button>
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
              <el-form-item label="分类名称">
                <el-input v-model="search2.categoryName" placeholder="请输入分类名称" clearable style="width: 200px" @keyup.enter.native="loadLevel2" />
              </el-form-item>
              <el-form-item label="上级分类">
                <el-select v-model="search2.parentId" placeholder="全部" clearable style="width: 200px" @change="loadLevel2">
                  <el-option
                    v-for="item in level1Options"
                    :key="item.id"
                    :label="item.categoryName"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="search2.status" placeholder="全部" clearable style="width: 120px" @change="loadLevel2">
                  <el-option label="启用" :value="1"></el-option>
                  <el-option label="禁用" :value="0"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="small" @click="loadLevel2">查询</el-button>
                <el-button size="small" @click="handleResetSearch2">重置</el-button>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="small" @click="handleAddLevel2">新增二级分类</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table :data="level2List" border style="width: 100%" v-loading="loading2">
            <el-table-column prop="categoryCode" label="分类编码" width="200" />
            <el-table-column prop="categoryName" label="分类名称" />
            <el-table-column prop="status" label="状态" width="80">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
                  {{ scope.row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template slot-scope="scope">
                <el-button size="mini" @click="handleEditLevel2(scope.row)">编辑</el-button>
                <el-button
                  size="mini"
                  type="warning"
                  v-if="scope.row.status === 1"
                  @click="handleStop(scope.row)"
                >禁用</el-button>
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

        <!-- 三级分类 -->
        <el-tab-pane label="三级分类" name="LEVEL3">
          <div class="toolbar">
            <el-form :inline="true" :model="search3" size="small">
              <el-form-item label="分类名称">
                <el-input v-model="search3.categoryName" placeholder="请输入分类名称" clearable style="width: 200px" @keyup.enter.native="loadLevel3" />
              </el-form-item>
              <el-form-item label="一级分类">
                <el-select v-model="search3.level1Id" placeholder="全部" clearable style="width: 200px" @change="handleLevel1ChangeForSearch">
                  <el-option
                    v-for="item in level1Options"
                    :key="item.id"
                    :label="item.categoryName"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="二级分类">
                <el-select v-model="search3.parentId" placeholder="请先选择一级分类" clearable style="width: 200px" :disabled="!search3.level1Id" @change="loadLevel3">
                  <el-option
                    v-for="item in level2Options"
                    :key="item.id"
                    :label="item.categoryName"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="search3.status" placeholder="全部" clearable style="width: 120px" @change="loadLevel3">
                  <el-option label="启用" :value="1"></el-option>
                  <el-option label="禁用" :value="0"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="small" @click="loadLevel3">查询</el-button>
                <el-button size="small" @click="handleResetSearch3">重置</el-button>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="small" @click="handleAddLevel3">新增三级分类</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table :data="level3List" border style="width: 100%" v-loading="loading3">
            <el-table-column prop="categoryCode" label="分类编码" width="220" />
            <el-table-column prop="categoryName" label="分类名称" />
            <el-table-column prop="status" label="状态" width="80">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
                  {{ scope.row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template slot-scope="scope">
                <el-button size="mini" @click="handleEditLevel3(scope.row)">编辑</el-button>
                <el-button
                  size="mini"
                  type="warning"
                  v-if="scope.row.status === 1"
                  @click="handleStop(scope.row)"
                >禁用</el-button>
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
              :total="pagination3.total"
              :page-size="pagination3.size"
              :current-page="pagination3.page"
              @size-change="handleSizeChange3"
              @current-change="handleCurrentChange3"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="formRules" ref="form" label-width="100px">
        <el-form-item label="分类编码" prop="categoryCode" v-if="!isLevel2 && !isLevel3">
          <el-input v-model="form.categoryCode" placeholder="请输入分类编码" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="一级分类" prop="level1Id" v-if="isLevel3">
          <el-select v-model="form.level1Id" placeholder="请先选择一级分类" style="width: 100%" filterable @change="handleLevel1ChangeForLevel3">
            <el-option
              v-for="item in level1OptionsForLevel3"
              :key="item.id"
              :label="item.categoryName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="二级分类" prop="parentId" v-if="isLevel2 || isLevel3">
          <el-select 
            v-model="form.parentId" 
            :placeholder="isLevel3 ? '请先选择一级分类' : '请选择上级分类'" 
            style="width: 100%" 
            filterable 
            :disabled="isLevel3 && !form.level1Id"
            @change="handleParentChange"
          >
            <el-option
              v-for="item in parentOptions"
              :key="item.id"
              :label="item.categoryName"
              :value="item.id"
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
  getAssetCategoryLevel1Page,
  getAssetCategoryLevel2Page,
  getAssetCategoryLevel3Page,
  getAssetCategoryLevel1List,
  getAssetCategoryLevel2List,
  getAssetCategoryLevel3List,
  getAssetCategoryById,
  saveAssetCategoryLevel1,
  saveAssetCategoryLevel2,
  saveAssetCategoryLevel3,
  updateAssetCategory,
  deleteAssetCategory,
  stopAssetCategory,
  startAssetCategory
} from '@/api/asset'

export default {
  name: 'AssetCategory',
  data() {
    return {
      activeTab: 'LEVEL1',
      loading1: false,
      loading2: false,
      loading3: false,
      level1List: [],
      level2List: [],
      level3List: [],
      level1Options: [],
      level2Options: [],
      search1: {
        categoryName: '',
        status: null
      },
      search2: {
        categoryName: '',
        parentId: null,
        status: null
      },
      search3: {
        categoryName: '',
        level1Id: null,
        parentId: null,
        status: null
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
      pagination3: {
        page: 1,
        size: 10,
        total: 0
      },
      dialogVisible: false,
      dialogTitle: '新增分类',
      form: {
        id: null,
        categoryCode: '',
        categoryName: '',
        parentId: null,
        level: 1,
        status: 1
      },
      isLevel2: false,
      isLevel3: false,
      level1OptionsForLevel3: [], // 用于三级分类的一级分类选项
      level2OptionsForLevel3: [] // 用于三级分类的二级分类选项
    }
  },
  computed: {
    parentOptions() {
      if (this.isLevel2) {
        return this.level1Options
      } else if (this.isLevel3) {
        return this.level2OptionsForLevel3
      }
      return []
    },
    formRules() {
      const rules = {}
      // 一级分类需要验证编码
      if (!this.isLevel2 && !this.isLevel3) {
        rules.categoryCode = [{ required: true, message: '请输入分类编码', trigger: 'blur' }]
      }
      // 所有分类都需要名称
      rules.categoryName = [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
      // 二级分类需要选择上级分类
      if (this.isLevel2) {
        rules.parentId = [{ required: true, message: '请选择上级分类', trigger: 'change' }]
      }
      // 三级分类需要选择一级和二级分类
      if (this.isLevel3) {
        rules.level1Id = [{ required: true, message: '请选择一级分类', trigger: 'change' }]
        rules.parentId = [{ required: true, message: '请选择二级分类', trigger: 'change' }]
      }
      return rules
    }
  },
  watch: {
    activeTab(newVal) {
      // 当切换到三级分类标签页时
      if (newVal === 'LEVEL3') {
        // 确保一级分类选项已加载
        if (this.level1Options.length === 0) {
          this.loadLevel1Options()
        }
        // 如果已选择一级分类，重新加载二级分类选项和数据
        if (this.search3.level1Id) {
          this.loadLevel2OptionsForSearch(this.search3.level1Id)
          // 重新加载三级分类数据
          this.pagination3.page = 1
          this.loadLevel3()
        }
      }
    }
  },
  mounted() {
    this.loadLevel1()
    this.loadLevel2()
    this.loadLevel3()
    this.loadLevel1Options()
    // 三级分类页面的二级分类选项会在选择一级分类后动态加载
  },
  methods: {
    loadLevel1() {
      this.loading1 = true
      const status = this.search1.status !== null && this.search1.status !== '' ? this.search1.status : null
      const categoryName = this.search1.categoryName && this.search1.categoryName.trim() ? this.search1.categoryName.trim() : null
      getAssetCategoryLevel1Page(this.pagination1.page, this.pagination1.size, status, categoryName)
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
      const parentId = this.search2.parentId !== null && this.search2.parentId !== '' ? this.search2.parentId : null
      const status = this.search2.status !== null && this.search2.status !== '' ? this.search2.status : null
      const categoryName = this.search2.categoryName && this.search2.categoryName.trim() ? this.search2.categoryName.trim() : null
      getAssetCategoryLevel2Page(this.pagination2.page, this.pagination2.size, parentId, status, categoryName)
        .then(res => {
          if (res.code === 200 && res.data) {
            const records = res.data.records || res.data.list || res.data.rows || []
            this.level2List = records
            this.pagination2.total = res.data.total || records.length || 0
          }
          this.loading2 = false
        })
        .catch(() => {
          this.loading2 = false
        })
    },
    loadLevel3() {
      this.loading3 = true
      const parentId = this.search3.parentId !== null && this.search3.parentId !== '' ? this.search3.parentId : null
      // 如果选择了二级分类，不使用一级分类ID；如果只选择了一级分类，使用一级分类ID
      const level1Id = parentId ? null : (this.search3.level1Id !== null && this.search3.level1Id !== '' ? this.search3.level1Id : null)
      const status = this.search3.status !== null && this.search3.status !== '' ? this.search3.status : null
      const categoryName = this.search3.categoryName && this.search3.categoryName.trim() ? this.search3.categoryName.trim() : null
      getAssetCategoryLevel3Page(this.pagination3.page, this.pagination3.size, parentId, level1Id, status, categoryName)
        .then(res => {
          if (res.code === 200 && res.data) {
            const records = res.data.records || res.data.list || res.data.rows || []
            this.level3List = [...records]
            this.pagination3.total = res.data.total || records.length || 0
          } else {
            this.level3List = []
          }
          this.loading3 = false
        })
        .catch(() => {
          this.level3List = []
          this.loading3 = false
        })
    },
    loadLevel1Options() {
      getAssetCategoryLevel1List(1).then(res => {
        if (res.code === 200 && res.data) {
          this.level1Options = res.data || []
        }
      })
    },
    loadLevel2Options(parentId) {
      if (!parentId) {
        this.level2Options = []
        return
      }
      getAssetCategoryLevel2List(parentId, 1).then(res => {
        if (res.code === 200 && res.data) {
          this.level2Options = res.data || []
        }
      })
    },
    loadLevel2OptionsForSearch(level1Id) {
      // 根据一级分类ID加载二级分类，用于三级分类页面的搜索下拉框
      if (!level1Id) {
        this.level2Options = []
        return
      }
      getAssetCategoryLevel2List(level1Id, 1).then(res => {
        if (res.code === 200 && res.data) {
          this.level2Options = res.data || []
        } else {
          this.level2Options = []
        }
      }).catch(() => {
        this.level2Options = []
      })
    },
    handleLevel1ChangeForSearch(level1Id) {
      // 当选择一级分类时，加载该一级分类下的二级分类，并清空二级分类选择
      this.search3.parentId = null
      if (level1Id) {
        // 如果选择了一级分类，加载该一级分类下的二级分类
        this.loadLevel2OptionsForSearch(level1Id)
      } else {
        // 如果清空了一级分类，清空二级分类选项
        this.level2Options = []
      }
      // 重置分页并重新加载三级分类列表（会根据一级分类ID过滤）
      this.pagination3.page = 1
      this.loadLevel3()
    },
    handleResetSearch1() {
      this.search1 = {
        categoryName: '',
        status: null
      }
      this.pagination1.page = 1
      this.loadLevel1()
    },
    handleResetSearch2() {
      this.search2 = {
        categoryName: '',
        parentId: null,
        status: null
      }
      this.pagination2.page = 1
      this.loadLevel2()
    },
    handleResetSearch3() {
      this.search3 = {
        categoryName: '',
        level1Id: null,
        parentId: null,
        status: null
      }
      this.level2Options = []
      this.pagination3.page = 1
      this.loadLevel3()
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
    handleSizeChange3(size) {
      this.pagination3.size = size
      this.loadLevel3()
    },
    handleCurrentChange3(page) {
      this.pagination3.page = page
      this.loadLevel3()
    },
    handleAddLevel1() {
      this.dialogTitle = '新增一级分类'
      this.isLevel2 = false
      this.isLevel3 = false
      this.form = {
        id: null,
        categoryCode: '',
        categoryName: '',
        parentId: null,
        level: 1,
        status: 1
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.form) {
          this.$refs.form.clearValidate()
        }
      })
    },
    handleAddLevel2() {
      this.dialogTitle = '新增二级分类'
      this.isLevel2 = true
      this.isLevel3 = false
      this.form = {
        id: null,
        categoryCode: '',
        categoryName: '',
        parentId: this.search2.parentId || null,
        level: 2,
        status: 1
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.form) {
          this.$refs.form.clearValidate()
        }
      })
    },
    handleAddLevel3() {
      this.dialogTitle = '新增三级分类'
      this.isLevel2 = false
      this.isLevel3 = true
      this.form = {
        id: null,
        categoryCode: '',
        categoryName: '',
        level1Id: null,
        parentId: null,
        level: 3,
        status: 1
      }
      // 加载所有启用的一级分类供选择
      getAssetCategoryLevel1List(1).then(res => {
        if (res.code === 200 && res.data) {
          this.level1OptionsForLevel3 = res.data || []
        }
      })
      // 清空二级分类选项
      this.level2OptionsForLevel3 = []
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.form) {
          this.$refs.form.clearValidate()
        }
      })
    },
    handleLevel1ChangeForLevel3(level1Id) {
      // 当选择一级分类时，加载该一级分类下的二级分类
      this.form.parentId = null // 清空二级分类选择
      this.level2OptionsForLevel3 = []
      if (level1Id) {
        getAssetCategoryLevel2List(level1Id, 1).then(res => {
          if (res.code === 200 && res.data) {
            this.level2OptionsForLevel3 = res.data || []
          }
        })
      }
    },
    handleEditLevel1(row) {
      this.dialogTitle = '编辑一级分类'
      this.isLevel2 = false
      this.isLevel3 = false
      this.form = { ...row, level: 1 }
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.form) {
          this.$refs.form.clearValidate()
        }
      })
    },
    handleEditLevel2(row) {
      this.dialogTitle = '编辑二级分类'
      this.isLevel2 = true
      this.isLevel3 = false
      this.form = { ...row, level: 2 }
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.form) {
          this.$refs.form.clearValidate()
        }
      })
    },
    handleEditLevel3(row) {
      this.dialogTitle = '编辑三级分类'
      this.isLevel2 = false
      this.isLevel3 = true
      // 获取三级分类的详细信息，包括其父级分类信息
      getAssetCategoryById(row.id).then(res => {
        if (res.code === 200 && res.data) {
          const category = res.data
          // 获取二级分类信息，然后获取一级分类ID
          getAssetCategoryById(category.parentId).then(parentRes => {
            if (parentRes.code === 200 && parentRes.data) {
              const parentCategory = parentRes.data
              this.form = {
                ...category,
                level: 3,
                level1Id: parentCategory.parentId, // 一级分类ID
                parentId: category.parentId // 二级分类ID（作为parentId）
              }
              // 加载所有启用的一级分类
              getAssetCategoryLevel1List(1).then(level1Res => {
                if (level1Res.code === 200 && level1Res.data) {
                  this.level1OptionsForLevel3 = level1Res.data || []
                }
              })
              // 加载该一级分类下的二级分类
              if (parentCategory.parentId) {
                getAssetCategoryLevel2List(parentCategory.parentId, 1).then(level2Res => {
                  if (level2Res.code === 200 && level2Res.data) {
                    this.level2OptionsForLevel3 = level2Res.data || []
                  }
                })
              }
            } else {
              // 如果获取父级失败，直接使用row数据
              this.form = { ...category, level: 3, level1Id: null, parentId: category.parentId }
              getAssetCategoryLevel1List(1).then(level1Res => {
                if (level1Res.code === 200 && level1Res.data) {
                  this.level1OptionsForLevel3 = level1Res.data || []
                }
              })
            }
            this.dialogVisible = true
            this.$nextTick(() => {
              if (this.$refs.form) {
                this.$refs.form.clearValidate()
              }
            })
          }).catch(() => {
            // 如果获取父级失败，直接使用row数据
            this.form = { ...category, level: 3, level1Id: null, parentId: category.parentId }
            getAssetCategoryLevel1List(1).then(level1Res => {
              if (level1Res.code === 200 && level1Res.data) {
                this.level1OptionsForLevel3 = level1Res.data || []
              }
            })
            this.dialogVisible = true
            this.$nextTick(() => {
              if (this.$refs.form) {
                this.$refs.form.clearValidate()
              }
            })
          })
        } else {
          // 如果获取失败，直接使用row数据
          this.form = { ...row, level: 3, level1Id: null }
          getAssetCategoryLevel1List(1).then(level1Res => {
            if (level1Res.code === 200 && level1Res.data) {
              this.level1OptionsForLevel3 = level1Res.data || []
            }
          })
          this.dialogVisible = true
          this.$nextTick(() => {
            if (this.$refs.form) {
              this.$refs.form.clearValidate()
            }
          })
        }
      }).catch(() => {
        // 如果获取失败，直接使用row数据
        this.form = { ...row, level: 3, level1Id: null }
        getAssetCategoryLevel1List(1).then(level1Res => {
          if (level1Res.code === 200 && level1Res.data) {
            this.level1OptionsForLevel3 = level1Res.data || []
          }
        })
        this.dialogVisible = true
        this.$nextTick(() => {
          if (this.$refs.form) {
            this.$refs.form.clearValidate()
          }
        })
      })
    },
    handleParentChange(value) {
      // 编码会在保存时自动生成，这里清空编码显示
      this.form.categoryCode = ''
    },
    handleStop(row) {
      this.$confirm('确认禁用该分类吗？', '提示', {
        type: 'warning'
      }).then(() => {
        stopAssetCategory(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('禁用成功')
            this.reloadCurrentTab()
          } else {
            this.$message.error(res.message || '禁用失败')
          }
        }).catch(err => {
          this.$message.error(err.message || '禁用失败')
        })
      }).catch(() => {
        // 用户取消，不做任何处理
      })
    },
    handleStart(row) {
      this.$confirm('确认启用该分类吗？', '提示', {
        type: 'warning'
      }).then(() => {
        startAssetCategory(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('启用成功')
            this.reloadCurrentTab()
          } else {
            this.$message.error(res.message || '启用失败')
          }
        }).catch(err => {
          this.$message.error(err.message || '启用失败')
        })
      }).catch(() => {
        // 用户取消，不做任何处理
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该分类吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteAssetCategory(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.reloadCurrentTab()
          } else {
            this.$message.error(res.message || '删除失败')
          }
        }).catch(err => {
          this.$message.error(err.message || '删除失败')
        })
      }).catch(() => {
        // 用户取消，不做任何处理
      })
    },
    reloadCurrentTab() {
      if (this.activeTab === 'LEVEL1') {
        this.loadLevel1()
        this.loadLevel1Options()
      } else if (this.activeTab === 'LEVEL2') {
        this.loadLevel2()
      } else if (this.activeTab === 'LEVEL3') {
        this.loadLevel3()
        // 如果已选择一级分类，重新加载二级分类选项
        if (this.search3.level1Id) {
          this.loadLevel2OptionsForSearch(this.search3.level1Id)
        }
      }
    },
    handleSave() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const isNew = !this.form.id
        
        // 如果是新增二级或三级分类，编码由后端自动生成，不需要前端传递
        if (isNew && (this.isLevel2 || this.isLevel3)) {
          // 不传递categoryCode，让后端自动生成
          const formData = { ...this.form }
          delete formData.categoryCode
          // 如果是三级分类，删除 level1Id，只保留 parentId（二级分类ID）
          if (this.isLevel3) {
            delete formData.level1Id
          }
          
          const api = this.isLevel2 ? saveAssetCategoryLevel2 : saveAssetCategoryLevel3
          api(formData).then(res => {
            if (res.code === 200) {
              this.$message.success('新增成功')
              this.dialogVisible = false
              this.reloadCurrentTab()
            } else {
              this.$message.error(res.message || '操作失败')
            }
          }).catch(err => {
            this.$message.error(err.message || '操作失败')
          })
        } else {
          // 一级分类新增或编辑，使用update接口
          // 如果是三级分类编辑，删除 level1Id
          const formData = { ...this.form }
          if (this.isLevel3) {
            delete formData.level1Id
          }
          const api = isNew ? saveAssetCategoryLevel1 : updateAssetCategory
          api(formData).then(res => {
            if (res.code === 200) {
              this.$message.success(isNew ? '新增成功' : '更新成功')
              this.dialogVisible = false
              this.reloadCurrentTab()
            } else {
              this.$message.error(res.message || '操作失败')
            }
          }).catch(err => {
            this.$message.error(err.message || '操作失败')
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.asset-category {
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
