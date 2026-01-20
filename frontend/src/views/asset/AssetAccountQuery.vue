<template>
  <div class="asset-account-query">
    <el-card>
      <div slot="header" class="clearfix">
        <span>资产台账查询</span>
        <el-button style="float: right; padding: 3px 0" type="primary" @click="handleAddReceive">新增领用</el-button>
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
          <el-form-item label="是否有库存">
            <el-select v-model="searchForm.hasStock" placeholder="全部" clearable style="width: 120px" @change="loadData">
              <el-option label="有库存" :value="true"></el-option>
              <el-option label="无库存" :value="false"></el-option>
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
        <el-table-column prop="assetCode" label="资产编码" width="180"></el-table-column>
        <el-table-column prop="assetName" label="资产名称" width="200"></el-table-column>
        <el-table-column prop="spec" label="规格型号" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="manufacturer" label="生产厂家" width="150"></el-table-column>
        <el-table-column prop="stockNum" label="库存数量" width="120" align="center">
          <template slot-scope="scope">
            <el-button type="text" @click="handleViewStockInfo(scope.row)">
              {{ scope.row.stockNum || 0 }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页和导出 -->
      <div class="pagination" style="display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
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
        <el-dropdown @command="handleExportCommand" :disabled="exportLoading">
          <el-button size="big" type="text" :loading="exportLoading">
            导出<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="current">导出本页</el-dropdown-item>
            <el-dropdown-item command="all">导出全部</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </el-card>
    
    <!-- 新增领用对话框 -->
    <ReceiveDialog
      :visible.sync="receiveDialogVisible"
      @refresh="loadData"
    />
    
    <!-- 库存信息对话框（显示入库单号、采购单号、申请单号） -->
    <StockInfoDialog
      :visible.sync="stockInfoDialogVisible"
      :asset-code="currentAssetCode"
      :asset-name="currentAssetName"
    />
  </div>
</template>

<script>
import { getAssetAccountPage, getAssetStorageInfo } from '@/api/asset'
import { getAssetCategoryLevel1List, getAssetCategoryLevel2List, getAssetCategoryLevel3List } from '@/api/asset'
import { exportExcel } from '@/api/common'
import ReceiveDialog from './ReceiveDialog.vue'
import StockInfoDialog from './StockInfoDialog.vue'

export default {
  name: 'AssetAccountQuery',
  components: {
    ReceiveDialog,
    StockInfoDialog
  },
  data() {
    return {
      loading: false,
      exportLoading: false,
      searchForm: {
        assetCode: '',
        assetName: '',
        level1Id: null,
        level2Id: null,
        categoryId: null,
        hasStock: null
      },
      tableData: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      level1CategoryOptions: [],
      searchLevel2Options: [],
      searchLevel3Options: [],
      receiveDialogVisible: false,
      stockInfoDialogVisible: false,
      currentAssetCode: '',
      currentAssetName: ''
    }
  },
  mounted() {
    this.loadLevel1Categories()
    this.loadData()
  },
  methods: {
    // 加载一级分类
    async loadLevel1Categories() {
      try {
        const res = await getAssetCategoryLevel1List(1) // 只加载启用的
        if (res.code === 200 && res.data) {
          this.level1CategoryOptions = res.data || []
        }
      } catch (error) {
        console.error('加载一级分类失败:', error)
      }
    },
    // 一级分类变化时加载二级分类
    handleSearchLevel1Change() {
      this.searchForm.level2Id = null
      this.searchForm.categoryId = null
      this.searchLevel2Options = []
      this.searchLevel3Options = []
      if (this.searchForm.level1Id) {
        getAssetCategoryLevel2List(this.searchForm.level1Id, 1).then(res => {
          if (res.code === 200 && res.data) {
            this.searchLevel2Options = res.data || []
          }
        })
      }
      this.loadData()
    },
    // 二级分类变化时加载三级分类
    handleSearchLevel2Change() {
      this.searchForm.categoryId = null
      this.searchLevel3Options = []
      if (this.searchForm.level2Id) {
        getAssetCategoryLevel3List(this.searchForm.level2Id, 1).then(res => {
          if (res.code === 200 && res.data) {
            this.searchLevel3Options = res.data || []
          }
        })
      }
      this.loadData()
    },
    // 加载数据
    loadData() {
      this.loading = true
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        assetCode: this.searchForm.assetCode || null,
        assetName: this.searchForm.assetName || null,
        level1Id: this.searchForm.level1Id || null,
        level2Id: this.searchForm.level2Id || null,
        categoryId: this.searchForm.categoryId || null,
        hasStock: this.searchForm.hasStock
      }
      
      getAssetAccountPage(params).then(res => {
        if (res.code === 200 && res.data) {
          // PageResult使用records字段，不是list
          this.tableData = res.data.records || res.data.list || []
          this.pagination.total = res.data.total || 0
        } else {
          this.tableData = []
          this.pagination.total = 0
        }
      }).catch(err => {
        console.error('查询失败:', err)
        this.$message.error('查询失败')
        this.tableData = []
        this.pagination.total = 0
      }).finally(() => {
        this.loading = false
      })
    },
    // 重置查询条件
    handleReset() {
      this.searchForm = {
        assetCode: '',
        assetName: '',
        level1Id: null,
        level2Id: null,
        categoryId: null,
        hasStock: null
      }
      this.searchLevel2Options = []
      this.searchLevel3Options = []
      this.loadData()
    },
    // 分页大小变化
    handleSizeChange(size) {
      this.pagination.size = size
      this.loadData()
    },
    // 当前页变化
    handleCurrentChange(page) {
      this.pagination.page = page
      this.loadData()
    },
    // 新增领用
    handleAddReceive() {
      this.receiveDialogVisible = true
    },
    // 查看库存信息（入库单号、采购单号、申请单号）
    handleViewStockInfo(row) {
      this.currentAssetCode = row.assetCode
      this.currentAssetName = row.assetName
      this.stockInfoDialogVisible = true
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportAccount(false)
      } else if (command === 'all') {
        this.handleExportAccount(true)
      }
    },
    // 导出资产台账数据
    async handleExportAccount(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const params = {
            page: 1,
            size: 10000,
            assetCode: this.searchForm.assetCode || null,
            assetName: this.searchForm.assetName || null,
            level1Id: this.searchForm.level1Id || null,
            level2Id: this.searchForm.level2Id || null,
            categoryId: this.searchForm.categoryId || null,
            hasStock: this.searchForm.hasStock
          }
          
          const res = await getAssetAccountPage(params)
          if (res.code === 200 && res.data) {
            dataToExport = res.data.records || res.data.list || []
          } else {
            this.$message.error(res.message || '获取数据失败')
            return
          }
        } else {
          // 导出当前页数据
          dataToExport = this.tableData
        }
        
        if (dataToExport.length === 0) {
          this.$message.warning('没有数据可导出')
          return
        }
        
        // 构建表头
        const headers = ['资产编码', '资产名称', '规格型号', '生产厂家', '库存数量']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            String(item.assetCode || ''),
            String(item.assetName || ''),
            String(item.spec || ''),
            String(item.manufacturer || ''),
            String(item.stockNum || 0)
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '资产台账查询' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
          headers: headers,
          dataList: dataList
        })
        
        // 处理blob响应并下载
        const blob = response.data
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '资产台账查询' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败：' + (error.message || '未知错误'))
      } finally {
        this.exportLoading = false
      }
    }
  }
}
</script>

<style scoped>
.asset-account-query {
  padding: 20px;
}
.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
