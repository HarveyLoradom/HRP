<template>
  <div class="password-change-log" v-loading="loading">
    <el-card shadow="hover">
      <div slot="header" class="card-header">
        <i class="el-icon-document"></i>
        <span class="header-title">业务日志 - 密码修改记录</span>
      </div>
      
      <!-- 搜索条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="工号/姓名">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入工号或姓名"
            clearable
            @keyup.enter.native="handleSearch"
            prefix-icon="el-icon-search"
            style="width: 250px;"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table 
        :data="tableData" 
        border 
        style="width: 100%" 
        v-loading="loading"
        stripe
        :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 'bold' }"
      >
        <el-table-column prop="empCode" label="工号" width="120" align="center">
          <template slot-scope="scope">
            <el-tag type="info" size="small">{{ scope.row.empCode || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="empName" label="姓名" width="120" align="center">
          <template slot-scope="scope">
            <span style="font-weight: 500;">{{ scope.row.empName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="changeTime" label="修改时间" width="180" align="center">
          <template slot-scope="scope">
            <i class="el-icon-time" style="margin-right: 5px; color: #909399;"></i>
            <span>{{ formatDate(scope.row.changeTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="changeIp" label="修改IP" width="150" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.changeIp" type="success" size="small">
              <i class="el-icon-location" style="margin-right: 3px;"></i>
              {{ scope.row.changeIp }}
            </el-tag>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="changeUser" label="操作人" width="120" align="center">
          <template slot-scope="scope">
            <i class="el-icon-user" style="margin-right: 5px; color: #909399;"></i>
            <span v-if="scope.row.changeUser">{{ scope.row.changeUser }}</span>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template slot-scope="scope">
            <el-button type="primary" icon="el-icon-view" size="mini" @click="handleView(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
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

    <!-- 查看详情对话框 -->
    <el-dialog 
      title="密码修改详情" 
      :visible.sync="detailVisible" 
      width="700px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="1" border v-if="currentDetail" class="detail-descriptions">
        <el-descriptions-item label="工号">
          <el-tag type="info">{{ currentDetail.empCode || '-' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="姓名">
          <span style="font-weight: 500; font-size: 15px;">{{ currentDetail.empName || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="原密码">
          <el-input 
            :value="currentDetail.oldPassword || '-'" 
            readonly 
            style="width: 100%;"
            class="password-input"
          >
            <template slot="prepend">
              <i class="el-icon-lock"></i>
            </template>
          </el-input>
        </el-descriptions-item>
        <el-descriptions-item label="新密码">
          <el-input 
            :value="currentDetail.newPassword || '-'" 
            readonly 
            style="width: 100%;"
            class="password-input"
          >
            <template slot="prepend">
              <i class="el-icon-key"></i>
            </template>
          </el-input>
        </el-descriptions-item>
        <el-descriptions-item label="修改时间">
          <i class="el-icon-time" style="margin-right: 5px; color: #909399;"></i>
          <span>{{ formatDate(currentDetail.changeTime) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="修改IP">
          <el-tag v-if="currentDetail.changeIp" type="success">
            <i class="el-icon-location" style="margin-right: 3px;"></i>
            {{ currentDetail.changeIp }}
          </el-tag>
          <span v-else style="color: #c0c4cc;">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="操作人">
          <i class="el-icon-user" style="margin-right: 5px; color: #909399;"></i>
          <span>{{ currentDetail.changeUser || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注">
          <span>{{ currentDetail.remark || '-' }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="detailVisible = false">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getPasswordChangeLogPage, getPasswordChangeLogById } from '@/api/user'
import { paginationMixin } from '@/mixins/pagination'

export default {
  name: 'PasswordChangeLog',
  mixins: [paginationMixin],
  data() {
    return {
      loading: false,
      tableData: [],
      searchForm: {
        keyword: ''
      },
      detailVisible: false,
      currentDetail: null
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      const params = {
        page: this.pagination.page,
        size: this.pagination.size
      }
      
      if (this.searchForm.keyword) {
        params.keyword = this.searchForm.keyword
      }
      
      getPasswordChangeLogPage(params).then(response => {
        if (response.code === 200 && response.data) {
          this.tableData = response.data.records || []
          this.pagination.total = response.data.total || 0
        } else {
          this.tableData = []
          this.pagination.total = 0
        }
        this.loading = false
      }).catch(() => {
        this.tableData = []
        this.pagination.total = 0
        this.loading = false
      })
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = {
        keyword: ''
      }
      this.pagination.page = 1
      this.loadData()
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.pagination.page = 1
      this.loadData()
    },
    handleCurrentChange(val) {
      this.pagination.page = val
      this.loadData()
    },
    handleView(row) {
      getPasswordChangeLogById(row.logId).then(response => {
        if (response.code === 200 && response.data) {
          this.currentDetail = response.data
          this.detailVisible = true
        } else {
          this.$message.error('获取详情失败')
        }
      }).catch(() => {
        this.$message.error('获取详情失败')
      })
    },
    formatDate(date) {
      if (!date) return '-'
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const hour = String(d.getHours()).padStart(2, '0')
      const minute = String(d.getMinutes()).padStart(2, '0')
      const second = String(d.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}:${second}`
    }
  }
}
</script>

<style scoped>
.password-change-log {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.card-header {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-header .el-icon-document {
  margin-right: 8px;
  font-size: 18px;
  color: #409EFF;
}

.header-title {
  flex: 1;
}

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.el-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.el-table {
  border-radius: 4px;
  overflow: hidden;
}

.detail-descriptions {
  margin: 20px 0;
}

.detail-descriptions /deep/ .el-descriptions-item__label {
  font-weight: 600;
  color: #606266;
  width: 120px;
  background-color: #fafafa;
}

.detail-descriptions /deep/ .el-descriptions-item__content {
  color: #303133;
}

.password-input /deep/ .el-input__inner {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  letter-spacing: 1px;
  background-color: #f5f7fa;
}

.dialog-footer {
  text-align: right;
  padding-top: 10px;
}
</style>

