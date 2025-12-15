<template>
  <div class="income-data">
    <el-card>
      <div slot="header">
        <span>收入数据</span>
      </div>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="HIS收入" name="his"></el-tab-pane>
        <el-tab-pane label="设备收入" name="equipment"></el-tab-pane>
      </el-tabs>

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
        <el-form-item v-if="activeTab === 'equipment'" label="设备编码">
          <el-input v-model="equipmentCode" placeholder="请输入设备编码" clearable style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="paginatedData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="incomeNo" label="收入单号" width="150"></el-table-column>
        <el-table-column prop="incomeDate" label="收入日期" width="120">
          <template slot-scope="scope">
            {{ formatDate(scope.row.incomeDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="incomeType" label="收入类型" width="120">
          <template slot-scope="scope">
            {{ getIncomeTypeName(scope.row.incomeType) }}
          </template>
        </el-table-column>
        <el-table-column prop="incomeAmount" label="收入金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.incomeAmount }}
          </template>
        </el-table-column>
        <el-table-column v-if="activeTab === 'his'" prop="hisSystemNo" label="HIS系统单号" width="150"></el-table-column>
        <el-table-column v-if="activeTab === 'his'" prop="deptCode" label="科室编码" width="120"></el-table-column>
        <el-table-column v-if="activeTab === 'his'" prop="deptName" label="科室名称" width="150"></el-table-column>
        <el-table-column v-if="activeTab === 'equipment'" prop="equipmentCode" label="设备编码" width="120"></el-table-column>
        <el-table-column v-if="activeTab === 'equipment'" prop="equipmentName" label="设备名称" width="150"></el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip></el-table-column>
      </el-table>

      <div style="margin-top: 20px; text-align: right;">
        <span style="font-size: 16px; font-weight: bold;">
          合计收入：¥{{ totalAmount.toFixed(2) }}
        </span>
      </div>

      <!-- 分页组件 -->
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        style="margin-top: 20px; text-align: right;"
      ></el-pagination>
    </el-card>
  </div>
</template>

<script>
import { getAllIncomeHis, getIncomeHisByDateRange, getAllIncomeEquipment, getIncomeEquipmentByDateRange } from '@/api/efficiency'

export default {
  name: 'IncomeData',
  data() {
    return {
      loading: false,
      activeTab: 'his',
      tableData: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
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
        return sum + (parseFloat(item.incomeAmount) || 0)
      }, 0)
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      if (this.activeTab === 'his') {
        if (this.startDate && this.endDate) {
          getIncomeHisByDateRange({ startDate: this.startDate, endDate: this.endDate }).then(response => {
            if (response.code === 200) {
              this.tableData = response.data || []
              this.pagination.total = this.tableData.length
              this.pagination.page = 1
            }
            this.loading = false
          }).catch(() => {
            this.loading = false
          })
        } else {
          getAllIncomeHis().then(response => {
            if (response.code === 200) {
              this.tableData = response.data || []
              this.pagination.total = this.tableData.length
              this.pagination.page = 1
            }
            this.loading = false
          }).catch(() => {
            this.loading = false
          })
        }
      } else {
        const params = {}
        if (this.startDate && this.endDate) {
          params.startDate = this.startDate
          params.endDate = this.endDate
        }
        if (this.equipmentCode) {
          params.equipmentCode = this.equipmentCode
        }
        if (params.startDate || params.equipmentCode) {
          getIncomeEquipmentByDateRange(params).then(response => {
            if (response.code === 200) {
              this.tableData = response.data || []
              this.pagination.total = this.tableData.length
              this.pagination.page = 1
            }
            this.loading = false
          }).catch(() => {
            this.loading = false
          })
        } else {
          getAllIncomeEquipment().then(response => {
            if (response.code === 200) {
              this.tableData = response.data || []
              this.pagination.total = this.tableData.length
              this.pagination.page = 1
            }
            this.loading = false
          }).catch(() => {
            this.loading = false
          })
        }
      }
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
    },
    handleCurrentChange(val) {
      this.pagination.page = val
    },
    handleTabClick() {
      this.equipmentCode = ''
      this.pagination.page = 1
      this.loadData()
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.startDate = ''
      this.endDate = ''
      this.equipmentCode = ''
      this.pagination.page = 1
      this.loadData()
    },
    getIncomeTypeName(type) {
      const typeMap = {
        'HIS': 'HIS收入',
        'EQUIPMENT': '设备收入',
        'SERVICE': '服务收入',
        'OTHER': '其他收入'
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
.income-data {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
</style>
