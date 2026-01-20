<template>
  <div class="asset-item" v-loading="importLoading">
    <el-card>
      <div slot="header" class="clearfix">
        <span>资产信息维护</span>
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
          <el-button style="margin-left: 10px; padding: 3px 0" type="text" @click="handleAdd">新增资产信息</el-button>
        </div>
      </div>
      
      <!-- 查询条件 -->
      <div class="toolbar" style="margin-bottom: 10px;">
        <el-form :inline="true" :model="searchForm" size="small">
          <el-form-item label="资产编码">
            <el-input v-model="searchForm.assetCode" placeholder="请输入资产编码" clearable style="width: 180px" @keyup.enter.native="loadData" />
          </el-form-item>
          <el-form-item label="资产名称">
            <el-input v-model="searchForm.assetName" placeholder="请输入资产名称" clearable style="width: 180px" @keyup.enter.native="loadData" />
          </el-form-item>
          <el-form-item label="一级分类">
            <el-select v-model="searchForm.level1Id" placeholder="全部" clearable style="width: 200px" @change="handleSearchLevel1Change">
              <el-option
                v-for="category in level1CategoryOptions"
                :key="category.id"
                :label="category.categoryName"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="二级分类">
            <el-select v-model="searchForm.level2Id" placeholder="请先选择一级分类" clearable style="width: 200px" :disabled="!searchForm.level1Id" @change="handleSearchLevel2Change">
              <el-option
                v-for="category in searchLevel2Options"
                :key="category.id"
                :label="category.categoryName"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="三级分类">
            <el-select v-model="searchForm.categoryId" placeholder="请先选择二级分类" clearable style="width: 200px" :disabled="!searchForm.level2Id" @change="loadData">
              <el-option
                v-for="category in searchLevel3Options"
                :key="category.id"
                :label="category.categoryName"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px" @change="loadData">
              <el-option label="启用" :value="1"></el-option>
              <el-option label="禁用" :value="0"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="small" @click="loadData">查询</el-button>
            <el-button size="small" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="assetCode" label="资产编码" width="180" />
        <el-table-column prop="assetName" label="资产名称" width="200" />
        <el-table-column prop="categoryName" label="三级分类" width="150" />
        <el-table-column prop="spec" label="规格型号" min-width="200" show-overflow-tooltip />
        <el-table-column prop="manufacturer" label="生产厂家" width="150" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="price" label="参考单价" width="120">
          <template slot-scope="scope">
            <span>{{ scope.row.price ? '¥' + scope.row.price.toFixed(2) : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
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

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
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
        <el-form-item label="资产编码" prop="assetCode" v-if="isEdit">
          <el-input v-model="form.assetCode" disabled></el-input>
        </el-form-item>
        <el-form-item label="一级分类" prop="level1CategoryId">
          <el-select 
            v-model="form.level1CategoryId" 
            placeholder="请先选择一级分类" 
            style="width: 100%" 
            filterable
            @change="handleLevel1Change"
          >
            <el-option
              v-for="category in level1CategoryOptions"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="二级分类" prop="level2CategoryId">
          <el-select 
            v-model="form.level2CategoryId" 
            placeholder="请先选择一级分类" 
            style="width: 100%" 
            filterable
            :disabled="!form.level1CategoryId"
            @change="handleLevel2Change"
          >
            <el-option
              v-for="category in level2CategoryOptions"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="三级分类" prop="categoryId">
          <el-select 
            v-model="form.categoryId" 
            placeholder="请先选择二级分类" 
            style="width: 100%" 
            filterable
            :disabled="!form.level2CategoryId"
          >
            <el-option
              v-for="category in level3CategoryOptions"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="资产名称" prop="assetName">
          <el-input v-model="form.assetName" placeholder="请输入资产名称"></el-input>
        </el-form-item>
        <el-form-item label="规格型号" prop="spec">
          <el-input 
            v-model="form.spec" 
            type="textarea" 
            :rows="2"
            placeholder="请输入详细配置规格（如：i5-13400/16G/512G SSD/Win11）"
          ></el-input>
        </el-form-item>
        <el-form-item label="生产厂家" prop="manufacturer">
          <el-input v-model="form.manufacturer" placeholder="请输入生产厂家"></el-input>
        </el-form-item>
        <el-form-item label="计量单位" prop="unit">
          <el-input v-model="form.unit" placeholder="请输入计量单位（如：台、套、个等）"></el-input>
        </el-form-item>
        <el-form-item label="参考单价" prop="price">
          <el-input-number 
            v-model="form.price" 
            :precision="2" 
            :min="0" 
            :step="0.01"
            placeholder="请输入参考单价"
            style="width: 100%"
          ></el-input-number>
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
  getAssetItemPage,
  getAssetItemById,
  saveAssetItem,
  updateAssetItem,
  deleteAssetItem,
  stopAssetItem,
  startAssetItem,
  getAssetCategoryLevel1List,
  getAssetCategoryLevel2List,
  getAssetCategoryLevel3List,
  getAssetCategoryById
} from '@/api/asset'

export default {
  name: 'AssetItem',
  data() {
    return {
      loading: false,
      tableData: [],
      level1CategoryOptions: [], // 一级分类选项（用于表单和查询）
      level2CategoryOptions: [], // 二级分类选项（用于表单）
      level3CategoryOptions: [], // 三级分类选项（用于表单）
      searchLevel2Options: [], // 二级分类选项（用于查询）
      searchLevel3Options: [], // 三级分类选项（用于查询）
      dialogVisible: false,
      dialogTitle: '新增资产信息',
      isEdit: false,
      uploadUrl: '/api/asset/item/import',
      uploadHeaders: {},
      importLoading: false,
      searchForm: {
        assetCode: '',
        assetName: '',
        level1Id: null, // 一级分类ID
        level2Id: null, // 二级分类ID
        categoryId: null, // 三级分类ID
        status: null
      },
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      form: {
        id: null,
        assetCode: '',
        level1CategoryId: null, // 一级分类ID
        level2CategoryId: null, // 二级分类ID
        categoryId: null, // 三级分类ID
        assetName: '',
        spec: '',
        manufacturer: '',
        unit: '',
        price: 0.00,
        status: 1
      },
      rules: {
        level1CategoryId: [{ required: true, message: '请选择一级分类', trigger: 'change' }],
        level2CategoryId: [{ required: true, message: '请选择二级分类', trigger: 'change' }],
        categoryId: [{ required: true, message: '请选择三级分类', trigger: 'change' }],
        assetName: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
        unit: [{ required: true, message: '请输入计量单位', trigger: 'blur' }],
        price: [{ required: true, message: '请输入参考单价', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadData()
    this.loadLevel1Categories()
  },
  methods: {
    loadData() {
      this.loading = true
      const params = {
        page: this.pagination.page,
        size: this.pagination.size
      }
      if (this.searchForm.assetCode) {
        params.assetCode = this.searchForm.assetCode
      }
      if (this.searchForm.assetName) {
        params.assetName = this.searchForm.assetName
      }
      // 优先级：三级分类 > 二级分类 > 一级分类
      if (this.searchForm.categoryId) {
        params.categoryId = this.searchForm.categoryId
      } else if (this.searchForm.level2Id) {
        params.level2Id = this.searchForm.level2Id
      } else if (this.searchForm.level1Id) {
        params.level1Id = this.searchForm.level1Id
      }
      if (this.searchForm.status !== null && this.searchForm.status !== undefined) {
        params.status = this.searchForm.status
      }
      
      getAssetItemPage(params).then(res => {
        if (res.code === 200 && res.data) {
          const records = res.data.records || res.data.list || res.data.rows || []
          this.tableData = records
          this.pagination.total = res.data.total || records.length || 0
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleSearchLevel1Change(level1Id) {
      // 当选择一级分类时，加载该一级分类下的二级分类，并清空二级、三级分类选择
      this.searchForm.level2Id = null
      this.searchForm.categoryId = null
      this.searchLevel2Options = []
      this.searchLevel3Options = []
      if (level1Id) {
        getAssetCategoryLevel2List(level1Id, 1).then(res => {
          if (res.code === 200 && res.data) {
            this.searchLevel2Options = res.data || []
          }
        })
      }
      this.pagination.page = 1
      this.loadData()
    },
    handleSearchLevel2Change(level2Id) {
      // 当选择二级分类时，加载该二级分类下的三级分类，并清空三级分类选择
      this.searchForm.categoryId = null
      this.searchLevel3Options = []
      if (level2Id) {
        getAssetCategoryLevel3List(level2Id, 1).then(res => {
          if (res.code === 200 && res.data) {
            this.searchLevel3Options = res.data || []
          }
        })
      }
      this.pagination.page = 1
      this.loadData()
    },
    loadLevel1Categories() {
      // 加载所有启用的一级分类
      getAssetCategoryLevel1List(1).then(res => {
        if (res.code === 200 && res.data) {
          this.level1CategoryOptions = res.data || []
        }
      })
    },
    handleLevel1Change(level1Id) {
      // 当选择一级分类时，加载该一级分类下的二级分类
      this.form.level2CategoryId = null // 清空二级分类选择
      this.form.categoryId = null // 清空三级分类选择
      this.level2CategoryOptions = []
      this.level3CategoryOptions = []
      if (level1Id) {
        getAssetCategoryLevel2List(level1Id, 1).then(res => {
          if (res.code === 200 && res.data) {
            this.level2CategoryOptions = res.data || []
          }
        })
      }
    },
    handleLevel2Change(level2Id) {
      // 当选择二级分类时，加载该二级分类下的三级分类
      this.form.categoryId = null // 清空三级分类选择
      this.level3CategoryOptions = []
      if (level2Id) {
        getAssetCategoryLevel3List(level2Id, 1).then(res => {
          if (res.code === 200 && res.data) {
            this.level3CategoryOptions = res.data || []
          }
        })
      }
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
        assetCode: '',
        assetName: '',
        level1Id: null,
        level2Id: null,
        categoryId: null,
        status: null
      }
      this.searchLevel2Options = []
      this.searchLevel3Options = []
      this.pagination.page = 1
      this.loadData()
    },
    handleAdd() {
      this.dialogTitle = '新增资产信息'
      this.isEdit = false
      this.form = {
        id: null,
        assetCode: '',
        level1CategoryId: null,
        level2CategoryId: null,
        categoryId: null,
        assetName: '',
        spec: '',
        manufacturer: '',
        unit: '',
        price: 0.00,
        status: 1
      }
      // 清空二级和三级分类选项
      this.level2CategoryOptions = []
      this.level3CategoryOptions = []
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.form) {
          this.$refs.form.clearValidate()
        }
      })
    },
    handleEdit(row) {
      this.dialogTitle = '编辑资产信息'
      this.isEdit = true
      getAssetItemById(row.id).then(res => {
        if (res.code === 200 && res.data) {
          const item = res.data
          // 获取三级分类信息，然后获取二级和一级分类ID
          if (item.categoryId) {
            getAssetCategoryById(item.categoryId).then(categoryRes => {
              if (categoryRes.code === 200 && categoryRes.data) {
                const category = categoryRes.data
                // 获取二级分类信息
                if (category.parentId) {
                  getAssetCategoryById(category.parentId).then(parentRes => {
                    if (parentRes.code === 200 && parentRes.data) {
                      const parentCategory = parentRes.data
                      this.form = {
                        ...item,
                        level1CategoryId: parentCategory.parentId, // 一级分类ID
                        level2CategoryId: category.parentId, // 二级分类ID
                        categoryId: item.categoryId // 三级分类ID
                      }
                      // 加载一级分类下的二级分类
                      if (parentCategory.parentId) {
                        getAssetCategoryLevel2List(parentCategory.parentId, 1).then(level2Res => {
                          if (level2Res.code === 200 && level2Res.data) {
                            this.level2CategoryOptions = level2Res.data || []
                          }
                        })
                      }
                      // 加载二级分类下的三级分类
                      getAssetCategoryLevel3List(category.parentId, 1).then(level3Res => {
                        if (level3Res.code === 200 && level3Res.data) {
                          this.level3CategoryOptions = level3Res.data || []
                        }
                      })
                      this.dialogVisible = true
                      this.$nextTick(() => {
                        if (this.$refs.form) {
                          this.$refs.form.clearValidate()
                        }
                      })
                    } else {
                      // 如果获取父级失败，直接使用item数据
                      this.form = { ...item, level1CategoryId: null, level2CategoryId: null }
                      this.dialogVisible = true
                      this.$nextTick(() => {
                        if (this.$refs.form) {
                          this.$refs.form.clearValidate()
                        }
                      })
                    }
                  }).catch(() => {
                    // 如果获取父级失败，直接使用item数据
                    this.form = { ...item, level1CategoryId: null, level2CategoryId: null }
                    this.dialogVisible = true
                    this.$nextTick(() => {
                      if (this.$refs.form) {
                        this.$refs.form.clearValidate()
                      }
                    })
                  })
                } else {
                  // 如果没有父级，直接使用item数据
                  this.form = { ...item, level1CategoryId: null, level2CategoryId: null }
                  this.dialogVisible = true
                  this.$nextTick(() => {
                    if (this.$refs.form) {
                      this.$refs.form.clearValidate()
                    }
                  })
                }
              } else {
                // 如果获取分类失败，直接使用item数据
                this.form = { ...item, level1CategoryId: null, level2CategoryId: null }
                this.dialogVisible = true
                this.$nextTick(() => {
                  if (this.$refs.form) {
                    this.$refs.form.clearValidate()
                  }
                })
              }
            }).catch(() => {
              // 如果获取分类失败，直接使用item数据
              this.form = { ...item, level1CategoryId: null, level2CategoryId: null }
              this.dialogVisible = true
              this.$nextTick(() => {
                if (this.$refs.form) {
                  this.$refs.form.clearValidate()
                }
              })
            })
          } else {
            // 如果没有分类ID，直接使用item数据
            this.form = { ...item, level1CategoryId: null, level2CategoryId: null }
            this.dialogVisible = true
            this.$nextTick(() => {
              if (this.$refs.form) {
                this.$refs.form.clearValidate()
              }
            })
          }
        } else {
          this.$message.error(res.message || '获取数据失败')
        }
      }).catch(err => {
        this.$message.error(err.message || '获取数据失败')
      })
    },
    handleStop(row) {
      this.$confirm('确认禁用该资产信息吗？', '提示', {
        type: 'warning'
      }).then(() => {
        stopAssetItem(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('禁用成功')
            this.loadData()
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
      this.$confirm('确认启用该资产信息吗？', '提示', {
        type: 'warning'
      }).then(() => {
        startAssetItem(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('启用成功')
            this.loadData()
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
      this.$confirm('确认删除该资产信息吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteAssetItem(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.error(res.message || '删除失败')
          }
        }).catch(err => {
          this.$message.error(err.message || '删除失败')
        })
      })
    },
    handleSave() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        
        // 保存时只发送 categoryId（三级分类ID），不发送 level1CategoryId 和 level2CategoryId
        const formData = { ...this.form }
        delete formData.level1CategoryId
        delete formData.level2CategoryId
        
        const api = this.isEdit ? updateAssetItem : saveAssetItem
        api(formData).then(res => {
          if (res.code === 200) {
            this.$message.success(this.isEdit ? '更新成功' : '新增成功')
            this.dialogVisible = false
            this.loadData()
          } else {
            this.$message.error(res.message || '操作失败')
          }
        }).catch(err => {
          this.$message.error(err.message || '操作失败')
        })
      })
    },
    handleDownloadTemplate() {
      window.open('/api/asset/item/template', '_blank')
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
      this.importLoading = true
      return true
    },
    handleUploadSuccess(response) {
      let res = response
      if (typeof res === 'string') {
        try {
          res = JSON.parse(res)
        } catch (e) {
          this.$message.error('导入失败：响应格式错误')
          this.importLoading = false
          return
        }
      }
      if (res && res.data && typeof res.data === 'object' && typeof res.code === 'undefined') {
        res = res.data
      }
      if (res && res.code === 200) {
        const message = res.message || res.data || '导入成功'
        // 检查是否有失败信息（包含"失败"关键字或失败详情）
        if (message.includes('失败') || message.includes('失败详情')) {
          // 使用alert显示详细信息，支持换行
          this.$alert(message.replace(/\n/g, '<br>'), '导入结果', {
            dangerouslyUseHTMLString: true,
            type: 'warning',
            confirmButtonText: '确定'
          })
        } else {
          this.$message.success(message)
        }
        this.loadData()
      } else {
        this.$message.error((res && res.message) || '导入失败')
      }
      this.importLoading = false
    },
    handleUploadError(error) {
      this.$message.error('导入失败：' + (error.message || '未知错误'))
      this.importLoading = false
    }
  }
}
</script>

<style scoped>
.asset-item {
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
