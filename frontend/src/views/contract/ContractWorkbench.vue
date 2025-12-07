<template>
  <div class="contract-workbench">
    <el-card>
      <div slot="header">
        <span>全合同查询</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增合同</el-button>
      </div>
      
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="contractNo" label="合同编号" width="150"></el-table-column>
        <el-table-column prop="contractName" label="合同名称" width="200"></el-table-column>
        <el-table-column prop="contractType" label="合同类型" width="120"></el-table-column>
        <el-table-column prop="partyA" label="甲方" width="150"></el-table-column>
        <el-table-column prop="partyB" label="乙方" width="150"></el-table-column>
        <el-table-column prop="contractAmount" label="合同金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.contractAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getAllContracts, saveContract, updateContract, deleteContract } from '@/api/contract'

export default {
  name: 'ContractWorkbench',
  data() {
    return {
      tableData: []
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      getAllContracts().then(response => {
        if (response.code === 200) {
          this.tableData = response.data
        }
      })
    },
    handleAdd() {
      this.$message.info('新增合同功能待实现')
    },
    handleEdit(row) {
      this.$message.info('编辑合同功能待实现')
    },
    handleDelete(row) {
      this.$confirm('确认删除该合同吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteContract(row.pactId).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          }
        })
      })
    },
    getStatusText(status) {
      const statusMap = {
        'DRAFT': '草稿',
        'PENDING': '待审批',
        'APPROVED': '已审批',
        'EXECUTING': '执行中',
        'ARCHIVED': '已归档'
      }
      return statusMap[status] || status
    },
    getStatusType(status) {
      const typeMap = {
        'DRAFT': 'info',
        'PENDING': 'warning',
        'APPROVED': 'success',
        'EXECUTING': '',
        'ARCHIVED': 'info'
      }
      return typeMap[status] || ''
    }
  }
}
</script>

<style scoped>
.contract-workbench {
  padding: 20px;
}
</style>

