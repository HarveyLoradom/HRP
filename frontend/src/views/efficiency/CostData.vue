<template>
  <div class="cost-data">
    <el-card>
      <div slot="header"><span>成本数据</span></div>
      
      <el-form :inline="true" class="search-form" style="margin-bottom: 20px;">
        <el-form-item label="开始日期">
          <el-date-picker 
            v-model="startDate" 
            type="date" 
            placeholder="选择开始日期"
            format="yyyy-MM-dd" 
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker 
            v-model="endDate" 
            type="date" 
            placeholder="选择结束日期"
            format="yyyy-MM-dd" 
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="设备编码">
          <el-input v-model="equipmentCode" placeholder="请输入设备编码" clearable style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="paginatedData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="costNo" label="成本单号" width="150"></el-table-column>
        <el-table-column prop="costDate" label="成本日期" width="120">
          <template slot-scope="scope">
            {{ formatDate(scope.row.costDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="costType" label="成本类型" width="120">
          <template slot-scope="scope">
            {{ getCostTypeName(scope.row.costType) }}
          </template>
        </el-table-column>
        <el-table-column prop="costAmount" label="成本金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.costAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="equipmentCode" label="设备编码" width="120"></el-table-column>
        <el-table-column prop="equipmentName" label="设备名称" width="150"></el-table-column>
        <el-table-column prop="deptCode" label="科室编码" width="120"></el-table-column>
        <el-table-column prop="deptName" label="科室名称" width="150"></el-table-column>
        <el-table-column prop="costDesc" label="成本说明" show-overflow-tooltip></el-table-column>
      </el-table>

      <div class="pagination-container" style="margin-top: 20px; text-align: right;">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total">
        </el-pagination>
      </div>

      <div style="margin-top: 20px; text-align: right;">
        <span style="font-size: 16px; font-weight: bold;">
          合计成本：¥{{ totalAmount.toFixed(2) }}
        </span>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getAllCostData, getCostDataByDateRange, getCostDataByEquipment } from '@/api/efficiency'
import { paginationMixin } from '@/mixins/pagination'

export default {
  name: 'CostData',
  mixins: [paginationMixin],
  data() {
    return {
      loading: false,
      tableData: [],
      allData: [],
      startDate: '',
      endDate: '',
      equipmentCode: ''
    }
  },
  computed: {
    paginatedData() {
      const start = (this.pagination.page - 1) * this.pagination.size
      const end = start + this.pagination.size
      return this.tableData.slice(start, end)
    },
    totalAmount() {
      return this.tableData.reduce((sum, item) => {
        return sum + (parseFloat(item.costAmount) || 0)
      }, 0)
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      if (this.equipmentCode && this.startDate && this.endDate) {
        getCostDataByEquipment(this.equipmentCode, this.startDate, this.endDate).then(response => {
          if (response.code === 200) {
            this.allData = response.data || []
            this.handleSearch()
          }
          this.loading = false
        }).catch(() => {
          this.loading = false
        })
      } else if (this.startDate && this.endDate) {
        getCostDataByDateRange({ startDate: this.startDate, endDate: this.endDate }).then(response => {
          if (response.code === 200) {
            this.allData = response.data || []
            this.handleSearch()
          }
          this.loading = false
        }).catch(() => {
          this.loading = false
        })
      } else {
        getAllCostData().then(response => {
          if (response.code === 200) {
            this.allData = response.data || []
            this.handleSearch()
          }
          this.loading = false
        }).catch(() => {
          this.loading = false
        })
      }
    },
    handleSearch() {
      let filtered = [...this.allData]
      if (this.equipmentCode) {
        filtered = filtered.filter(item => item.equipmentCode && item.equipmentCode.includes(this.equipmentCode))
      }
      this.tableData = filtered
      this.pagination.total = filtered.length
      this.pagination.page = 1
    },
    handleReset() {
      this.startDate = ''
      this.endDate = ''
      this.equipmentCode = ''
      this.pagination.page = 1
      this.handleSearch()
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
    },
    handleCurrentChange(val) {
      this.pagination.page = val
    },
    getCostTypeName(type) {
      const typeMap = {
        'MAINTENANCE': '维护成本',
        'ENERGY': '能源成本',
        'PERSONNEL': '人力成本',
        'DEPRECIATION': '折旧成本',
        'OTHER': '其他成本'
      }
      return typeMap[type] || type
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN').split(' ')[0]
    }
  }
}
</script>

<style scoped>
.cost-data { 
  padding: 20px; 
}
.search-form {
  margin-bottom: 20px;
}
</style>
