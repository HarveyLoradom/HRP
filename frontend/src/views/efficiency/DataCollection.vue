<template>
  <div class="data-collection">
    <el-card>
      <div slot="header" class="clearfix">
        <span>数据采集</span>
        <el-button style="float: right; padding: 3px 0" type="primary" @click="handleCollect">采集数据</el-button>
      </div>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="科室数据" name="dept"></el-tab-pane>
        <el-tab-pane label="设备数据" name="equipment"></el-tab-pane>
      </el-tabs>

      <el-form :inline="true" :model="queryForm" class="search-form" style="margin-bottom: 20px;">
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="queryForm.startDate"
            type="date"
            placeholder="选择开始日期"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="queryForm.endDate"
            type="date"
            placeholder="选择结束日期"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="paginatedData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="collectionNo" label="采集编号" width="150"></el-table-column>
        <el-table-column prop="collectionDate" label="采集日期" width="120">
          <template slot-scope="scope">
            {{ formatDate(scope.row.collectionDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="collectionType" label="采集类型" width="120">
          <template slot-scope="scope">
            {{ getCollectionTypeName(scope.row.collectionType) }}
          </template>
        </el-table-column>
        <el-table-column v-if="activeTab === 'dept'" prop="deptCode" label="科室编码" width="120"></el-table-column>
        <el-table-column v-if="activeTab === 'dept'" prop="deptName" label="科室名称" width="150"></el-table-column>
        <el-table-column v-if="activeTab === 'equipment'" prop="equipmentCode" label="设备编码" width="120"></el-table-column>
        <el-table-column v-if="activeTab === 'equipment'" prop="equipmentName" label="设备名称" width="150"></el-table-column>
        <el-table-column prop="dataCount" label="采集数据量" width="120"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 采集数据对话框 -->
    <el-dialog :title="`采集${activeTab === 'dept' ? '科室' : '设备'}数据`" :visible.sync="collectVisible" width="600px">
      <el-form :model="collectForm" label-width="120px">
        <el-form-item label="采集日期">
          <el-date-picker
            v-model="collectForm.collectionDate"
            type="date"
            placeholder="选择采集日期"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item v-if="activeTab === 'dept'" label="选择科室">
          <el-select v-model="collectForm.deptCode" placeholder="请选择科室" filterable style="width: 100%">
            <el-option
              v-for="dept in deptList"
              :key="dept.deptCode"
              :label="dept.deptName"
              :value="dept.deptCode"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="activeTab === 'equipment'" label="选择设备">
          <el-select v-model="collectForm.equipmentCode" placeholder="请选择设备" filterable style="width: 100%">
            <el-option
              v-for="equipment in equipmentList"
              :key="equipment.equipmentCode"
              :label="equipment.equipmentName"
              :value="equipment.equipmentCode"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="collectVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmCollect">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { collectDeptData, collectEquipmentData, getCollectionRecords, getAllEquipments } from '@/api/efficiency'

export default {
  name: 'DataCollection',
  data() {
    return {
      loading: false,
      activeTab: 'dept',
      tableData: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      collectVisible: false,
      queryForm: {
        startDate: '',
        endDate: ''
      },
      collectForm: {
        collectionDate: '',
        deptCode: '',
        equipmentCode: ''
      },
      deptList: [],
      equipmentList: []
    }
  },
  computed: {
    paginatedData() {
      const start = (this.pagination.page - 1) * this.pagination.size
      const end = start + this.pagination.size
      return this.tableData.slice(start, end)
    }
  },
  mounted() {
    this.loadData()
    this.loadEquipmentList()
  },
  methods: {
    loadData() {
      this.loading = true
      const collectionType = this.activeTab === 'dept' ? 'DEPT' : 'EQUIPMENT'
      getCollectionRecords(collectionType).then(response => {
        if (response.code === 200) {
          let allData = response.data || []
          // 根据日期过滤
          if (this.queryForm.startDate && this.queryForm.endDate) {
            allData = allData.filter(item => {
              const itemDate = new Date(item.collectionDate)
              const startDate = new Date(this.queryForm.startDate)
              const endDate = new Date(this.queryForm.endDate)
              return itemDate >= startDate && itemDate <= endDate
            })
          }
          this.tableData = allData
          this.pagination.total = allData.length
          this.pagination.page = 1
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
    },
    handleCurrentChange(val) {
      this.pagination.page = val
    },
    loadEquipmentList() {
      getAllEquipments().then(response => {
        if (response.code === 200) {
          this.equipmentList = response.data || []
        }
      })
    },
    handleTabClick() {
      this.pagination.page = 1
      this.loadData()
    },
    handleQuery() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.queryForm = {
        startDate: '',
        endDate: ''
      }
      this.pagination.page = 1
      this.loadData()
    },
    handleCollect() {
      this.collectForm = {
        collectionDate: new Date().toISOString().slice(0, 10),
        deptCode: '',
        equipmentCode: ''
      }
      this.collectVisible = true
    },
    handleConfirmCollect() {
      if (!this.collectForm.collectionDate) {
        this.$message.warning('请选择采集日期')
        return
      }
      if (this.activeTab === 'dept' && !this.collectForm.deptCode) {
        this.$message.warning('请选择科室')
        return
      }
      if (this.activeTab === 'equipment' && !this.collectForm.equipmentCode) {
        this.$message.warning('请选择设备')
        return
      }
      
      const api = this.activeTab === 'dept' ? collectDeptData : collectEquipmentData
      api(this.collectForm).then(response => {
        if (response.code === 200) {
          this.$message.success('数据采集成功')
          this.collectVisible = false
          this.loadData()
        } else {
          this.$message.error(response.message || '数据采集失败')
        }
      })
    },
    getCollectionTypeName(type) {
      const typeMap = {
        'DEPT': '科室',
        'EQUIPMENT': '设备'
      }
      return typeMap[type] || type
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN').split(' ')[0]
    },
    getStatusText(status) {
      const statusMap = {
        'SUCCESS': '成功',
        'FAILED': '失败',
        'PROCESSING': '处理中'
      }
      return statusMap[status] || status
    },
    getStatusType(status) {
      const typeMap = {
        'SUCCESS': 'success',
        'FAILED': 'danger',
        'PROCESSING': 'warning'
      }
      return typeMap[status] || ''
    }
  }
}
</script>

<style scoped>
.data-collection {
  padding: 20px;
}
.clearfix::after {
  content: '';
  display: table;
  clear: both;
}
.search-form {
  margin-bottom: 20px;
}
</style>
