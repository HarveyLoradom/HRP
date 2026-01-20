<template>
  <div>
    <el-dialog
      title="库存来源信息"
      :visible.sync="dialogVisible"
      width="800px"
      @close="handleClose"
    >
      <div style="margin-bottom: 20px;">
        <strong>资产编码：</strong>{{ assetCode }}
        <span style="margin-left: 20px;"><strong>资产名称：</strong>{{ assetName }}</span>
      </div>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <!-- 入库tab -->
        <el-tab-pane label="入库" name="storage">
          <div v-if="loading" style="text-align: center; padding: 40px;">
            <i class="el-icon-loading" style="font-size: 24px;"></i>
            <p>加载中...</p>
          </div>
          
          <el-table :data="stockInfoList" border style="width: 100%" v-else-if="stockInfoList && stockInfoList.length > 0">
            <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
            <el-table-column prop="storageNo" label="入库单号" width="180">
              <template slot-scope="scope">
                <el-button type="text" @click="handleViewStorage(scope.row.storageNo)" v-if="scope.row.storageNo">
                  {{ scope.row.storageNo }}
                </el-button>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="orderNo" label="采购单号" width="180">
              <template slot-scope="scope">
                <el-button type="text" @click="handleViewPurchase(scope.row.orderNo)" v-if="scope.row.orderNo">
                  {{ scope.row.orderNo }}
                </el-button>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="applyNo" label="申请单号" width="180">
              <template slot-scope="scope">
                <el-button type="text" @click="handleViewApply(scope.row.applyNo)" v-if="scope.row.applyNo">
                  {{ scope.row.applyNo }}
                </el-button>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="storageQuantity" label="入库数量" width="120" align="center">
              <template slot-scope="scope">
                <span>{{ scope.row.storageQuantity || 0 }}</span>
              </template>
            </el-table-column>
          </el-table>
          
          <div v-else style="text-align: center; color: #999; padding: 40px;">
            暂无入库信息
          </div>
        </el-tab-pane>
        
        <!-- 出库tab -->
        <el-tab-pane label="出库" name="receive">
          <div v-if="receiveLoading" style="text-align: center; padding: 40px;">
            <i class="el-icon-loading" style="font-size: 24px;"></i>
            <p>加载中...</p>
          </div>
          
          <el-table :data="receiveInfoList" border style="width: 100%" v-else-if="receiveInfoList && receiveInfoList.length > 0">
            <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
            <el-table-column prop="receiveNo" label="领用单号" width="300">
              <template slot-scope="scope">
                <el-button type="text" @click="handleViewReceive(scope.row.receiveNo)" v-if="scope.row.receiveNo">
                  {{ scope.row.receiveNo }}
                </el-button>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="receiveQuantity" label="领用数量" width="120" align="center">
              <template slot-scope="scope">
                <span>{{ scope.row.receiveQuantity || 0 }}</span>
              </template>
            </el-table-column>
          </el-table>
          
          <div v-else style="text-align: center; color: #999; padding: 40px;">
            暂无出库信息
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </el-dialog>
    
    <!-- 入库单详情对话框 -->
    <StorageDetailDialog 
      :visible.sync="storageDetailVisible" 
      :storage-id="currentStorageId"
      @refresh="handleStorageRefresh"
    />
    
    <!-- 采购单详情对话框 -->
    <PurchaseDetailDialog 
      :visible.sync="purchaseDetailVisible" 
      :purchase-id="currentPurchaseId"
      @refresh="handlePurchaseRefresh"
    />
    
    <!-- 采购申请详情对话框 -->
    <ProcurementApplyDetail 
      v-model="applyDetailVisible" 
      :apply-id="currentApplyId"
      source-type="query"
    />
    
    <!-- 领用详情对话框 -->
    <ReceiveDetailDialog 
      :visible.sync="receiveDetailVisible" 
      :receive-id="currentReceiveId"
    />
  </div>
</template>

<script>
import { getAssetStorageInfo, getAssetReceiveInfo, getAssetInStorageByStorageNo, getAssetPurchaseByOrderNo, getAssetPurchaseApplyByNo, getAssetReceiveByReceiveNo } from '@/api/asset'
import StorageDetailDialog from './StorageDetailDialog.vue'
import PurchaseDetailDialog from './PurchaseDetailDialog.vue'
import ProcurementApplyDetail from './ProcurementApplyDetail.vue'
import ReceiveDetailDialog from './ReceiveDetailDialog.vue'

export default {
  name: 'StockInfoDialog',
  components: {
    StorageDetailDialog,
    PurchaseDetailDialog,
    ProcurementApplyDetail,
    ReceiveDetailDialog
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    assetCode: {
      type: String,
      default: ''
    },
    assetName: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      dialogVisible: false,
      activeTab: 'storage',
      loading: false,
      receiveLoading: false,
      stockInfoList: [],
      receiveInfoList: [],
      storageDetailVisible: false,
      purchaseDetailVisible: false,
      applyDetailVisible: false,
      receiveDetailVisible: false,
      currentStorageId: null,
      currentPurchaseId: null,
      currentApplyId: null,
      currentReceiveId: null
    }
  },
  watch: {
    visible(val) {
      this.dialogVisible = val
      if (val && this.assetCode) {
        this.loadStockInfo()
      }
    },
    dialogVisible(val) {
      if (!val) {
        this.$emit('update:visible', false)
      }
    },
    assetCode(val) {
      if (val && this.dialogVisible) {
        this.loadStockInfo()
      }
    }
  },
  methods: {
    // 加载库存来源信息
    loadStockInfo() {
      if (!this.assetCode) return
      
      // 根据当前tab加载对应数据
      if (this.activeTab === 'storage') {
        this.loadStorageInfo()
      } else if (this.activeTab === 'receive') {
        this.loadReceiveInfo()
      }
    },
    // 加载入库信息
    loadStorageInfo() {
      if (!this.assetCode) return
      
      this.loading = true
      getAssetStorageInfo(this.assetCode).then(res => {
        if (res.code === 200 && res.data) {
          this.stockInfoList = res.data || []
        } else {
          this.$message.error(res.msg || '加载失败')
        }
      }).catch(err => {
        console.error('加载入库信息失败:', err)
        this.$message.error('加载失败')
      }).finally(() => {
        this.loading = false
      })
    },
    // 加载出库信息
    loadReceiveInfo() {
      if (!this.assetCode) return
      
      this.receiveLoading = true
      getAssetReceiveInfo(this.assetCode).then(res => {
        if (res.code === 200 && res.data) {
          this.receiveInfoList = res.data || []
        } else {
          this.$message.error(res.msg || '加载失败')
        }
      }).catch(err => {
        console.error('加载出库信息失败:', err)
        this.$message.error('加载失败')
      }).finally(() => {
        this.receiveLoading = false
      })
    },
    // tab切换事件
    handleTabClick(tab) {
      if (tab.name === 'storage' && this.stockInfoList.length === 0) {
        this.loadStorageInfo()
      } else if (tab.name === 'receive' && this.receiveInfoList.length === 0) {
        this.loadReceiveInfo()
      }
    },
    // 查看领用单详情
    async handleViewReceive(receiveNo) {
      if (!receiveNo) return
      try {
        const res = await getAssetReceiveByReceiveNo(receiveNo)
        if (res.code === 200 && res.data && res.data.id) {
          this.currentReceiveId = res.data.id
          this.receiveDetailVisible = true
        } else {
          this.$message.error('获取领用单信息失败')
        }
      } catch (err) {
        console.error('获取领用单信息失败:', err)
        this.$message.error('获取领用单信息失败')
      }
    },
    // 查看入库单详情
    async handleViewStorage(storageNo) {
      if (!storageNo) return
      try {
        const res = await getAssetInStorageByStorageNo(storageNo)
        if (res.code === 200 && res.data && res.data.storage && res.data.storage.id) {
          this.currentStorageId = res.data.storage.id
          this.storageDetailVisible = true
        } else {
          this.$message.error('获取入库单信息失败')
        }
      } catch (err) {
        console.error('获取入库单信息失败:', err)
        this.$message.error('获取入库单信息失败')
      }
    },
    // 查看采购单详情
    async handleViewPurchase(orderNo) {
      if (!orderNo) return
      try {
        const res = await getAssetPurchaseByOrderNo(orderNo)
        if (res.code === 200 && res.data && res.data.purchase && res.data.purchase.id) {
          this.currentPurchaseId = res.data.purchase.id
          this.purchaseDetailVisible = true
        } else {
          this.$message.error('获取采购单信息失败')
        }
      } catch (err) {
        console.error('获取采购单信息失败:', err)
        this.$message.error('获取采购单信息失败')
      }
    },
    // 查看申请单详情
    async handleViewApply(applyNo) {
      if (!applyNo) return
      try {
        const res = await getAssetPurchaseApplyByNo(applyNo)
        if (res.code === 200 && res.data && res.data.id) {
          this.currentApplyId = res.data.id
          this.applyDetailVisible = true
        } else {
          this.$message.error('获取申请单信息失败')
        }
      } catch (err) {
        console.error('获取申请单信息失败:', err)
        this.$message.error('获取申请单信息失败')
      }
    },
    // 入库单详情刷新
    handleStorageRefresh() {
      // 可以在这里刷新库存来源信息
      this.loadStockInfo()
    },
    // 采购单详情刷新
    handlePurchaseRefresh() {
      // 可以在这里刷新库存来源信息
      this.loadStockInfo()
    },
    // 关闭对话框
    handleClose() {
      this.dialogVisible = false
      this.activeTab = 'storage'
      this.stockInfoList = []
      this.receiveInfoList = []
    }
  }
}
</script>

<style scoped>
.dialog-footer {
  margin-top: 20px;
  text-align: right;
}
</style>
