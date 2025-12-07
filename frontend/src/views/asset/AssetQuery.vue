<template>
  <div class="asset-query">
    <el-card>
      <div slot="header"><span>综合查询</span></div>
      <el-table :data="tableData" border>
        <el-table-column prop="assetNo" label="资产编号" width="150"></el-table-column>
        <el-table-column prop="assetName" label="资产名称" width="200"></el-table-column>
        <el-table-column prop="purchaseAmount" label="采购金额" width="120">
          <template slot-scope="scope">¥{{ scope.row.purchaseAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100"></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getAllAssets } from '@/api/asset'

export default {
  name: 'AssetQuery',
  data() {
    return { tableData: [] }
  },
  mounted() {
    getAllAssets().then(response => {
      if (response.code === 200) {
        this.tableData = response.data
      }
    })
  }
}
</script>

<style scoped>
.asset-query { padding: 20px; }
</style>

