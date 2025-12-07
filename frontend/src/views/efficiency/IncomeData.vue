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

      <el-form :inline="true" class="search-form">
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
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="incomeNo" label="收入单号" width="150"></el-table-column>
        <el-table-column prop="incomeDate" label="收入日期" width="120"></el-table-column>
        <el-table-column prop="incomeType" label="收入类型" width="120"></el-table-column>
        <el-table-column prop="incomeAmount" label="收入金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.incomeAmount }}
          </template>
        </el-table-column>
        <el-table-column v-if="activeTab === 'his'" prop="hisSystemNo" label="HIS系统单号" width="150"></el-table-column>
        <el-table-column v-if="activeTab === 'equipment'" prop="equipmentCode" label="设备编码" width="150"></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getAllIncomeHis, getIncomeHisByDateRange, getAllIncomeEquipment } from '@/api/efficiency'

export default {
  name: 'IncomeData',
  data() {
    return {
      activeTab: 'his',
      tableData: [],
      startDate: '',
      endDate: ''
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      if (this.activeTab === 'his') {
        getAllIncomeHis().then(response => {
          if (response.code === 200) {
            this.tableData = response.data
          }
        })
      } else {
        getAllIncomeEquipment().then(response => {
          if (response.code === 200) {
            this.tableData = response.data
          }
        })
      }
    },
    handleTabClick() {
      this.loadData()
    },
    handleSearch() {
      if (this.startDate && this.endDate) {
        const params = {
          startDate: this.startDate,
          endDate: this.endDate
        }
        if (this.activeTab === 'his') {
          getIncomeHisByDateRange(params).then(response => {
            if (response.code === 200) {
              this.tableData = response.data
            }
          })
        }
      } else {
        this.loadData()
      }
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

