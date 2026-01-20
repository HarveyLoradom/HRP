<template>
  <el-dialog
    title="入库单详情"
    :visible.sync="dialogVisible"
    width="1200px"
    @close="handleClose"
  >
    <div v-if="loading" style="text-align: center; padding: 40px;">
      <i class="el-icon-loading" style="font-size: 24px;"></i>
      <p>加载中...</p>
    </div>
    
    <div v-else-if="storageData">
      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="storageData" label-width="140px" style="margin-top: 20px;">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="入库单号:">
                  <span>{{ storageData.storageNo || '-' }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="采购单号:">
                  <span>{{ storageData.orderNo || '-' }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="申请单号:">
                  <span>{{ storageData.applyNo || '-' }}</span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="入库日期:">
                  <span>{{ formatDateOnly(storageData.storageDate) }}</span>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="入库状态:">
                  <el-tag :type="getStorageStatusType(storageData.storageStatus)">
                    {{ getStorageStatusText(storageData.storageStatus) }}
                  </el-tag>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="操作人:">
                  <span>{{ storageData.operatorName || '-' }}</span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="入库总金额:">
                  <span style="font-weight: bold; color: #409EFF;">¥{{ formatMoney(storageData.totalAmount) }}</span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="备注:">
              <span>{{ storageData.remark || '-' }}</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 入库明细 -->
        <el-tab-pane label="入库明细" name="detail">
          <el-table :data="detailList" border style="width: 100%; margin-top: 20px;" v-if="detailList && detailList.length > 0">
            <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
            <el-table-column prop="assetCode" label="资产编码" width="150"></el-table-column>
            <el-table-column prop="assetName" label="资产名称" width="200"></el-table-column>
            <el-table-column prop="spec" label="规格型号" width="200"></el-table-column>
            <el-table-column prop="manufacturer" label="生产厂家" width="150"></el-table-column>
            <el-table-column prop="unit" label="单位" width="80"></el-table-column>
            <el-table-column prop="storageQuantity" label="入库数量" width="100"></el-table-column>
            <el-table-column prop="price" label="单价" width="120">
              <template slot-scope="scope">
                <span>¥{{ formatMoney(scope.row.price) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="totalPrice" label="总价" width="120">
              <template slot-scope="scope">
                <span>¥{{ formatMoney(scope.row.totalPrice) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" width="150"></el-table-column>
          </el-table>
          <div v-if="detailList && detailList.length > 0" style="margin-top: 10px; text-align: right; padding-right: 10px;">
            <strong>合计金额：¥{{ formatMoney(storageData.totalAmount) }}</strong>
          </div>
          <div v-else style="text-align: center; color: #999; padding: 20px;">暂无明细数据</div>
        </el-tab-pane>
      </el-tabs>
      
      <!-- 操作按钮 -->
      <div slot="footer" class="dialog-footer" style="margin-top: 20px; text-align: right;">
        <el-button v-if="storageData.storageStatus === 'NOT_STORED'" type="primary" @click="handleCompleteStorage">完成入库</el-button>
        <el-button v-if="storageData.storageStatus !== 'STORED'" type="danger" @click="handleDelete">删除</el-button>
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import { getAssetInStorageById, completeAssetInStorage, deleteAssetInStorage } from '@/api/asset'
import { getCodeTypeOptions } from '@/utils/codeType'

export default {
  name: 'StorageDetailDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    storageId: {
      type: Number,
      default: null
    }
  },
  data() {
    return {
      dialogVisible: false,
      loading: false,
      activeTab: 'basic',
      storageData: null,
      detailList: [],
      storageStatusOptions: []
    }
  },
  async mounted() {
    await this.loadStatusOptions()
  },
  watch: {
    visible(val) {
      this.dialogVisible = val
      if (val && this.storageId) {
        this.loadStorageDetail()
      }
    },
    dialogVisible(val) {
      if (!val) {
        this.$emit('update:visible', false)
      }
    },
    storageId(val) {
      if (val && this.dialogVisible) {
        this.loadStorageDetail()
      }
    }
  },
  methods: {
    // 加载状态选项
    async loadStatusOptions() {
      try {
        this.storageStatusOptions = await getCodeTypeOptions('IN_STORAGE_STATUS')
      } catch (error) {
        console.error('加载状态选项失败:', error)
        // 使用默认值
        this.storageStatusOptions = [
          { label: '未入库', value: 'NOT_STORED' },
          { label: '已入库', value: 'STORED' }
        ]
      }
    },
    loadStorageDetail() {
      if (!this.storageId) return
      
      this.loading = true
      getAssetInStorageById(this.storageId).then(res => {
        if (res.code === 200 && res.data) {
          this.storageData = res.data.storage || res.data
          this.detailList = res.data.details || []
        } else {
          this.$message.error(res.msg || '加载失败')
        }
      }).catch(err => {
        console.error('加载入库单详情失败:', err)
        this.$message.error('加载失败')
      }).finally(() => {
        this.loading = false
      })
    },
    handleCompleteStorage() {
      this.$confirm('确定要完成入库吗？完成后将更新资产账户的库存数量。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        completeAssetInStorage(this.storageId).then(res => {
          if (res.code === 200) {
            this.$message.success('入库成功，已更新资产账户库存数量')
            this.handleClose()
            this.$emit('refresh')
          } else {
            this.$message.error(res.msg || '入库失败')
          }
        }).catch(err => {
          console.error('完成入库失败:', err)
          this.$message.error('入库失败')
        })
      }).catch(() => {})
    },
    handleDelete() {
      this.$confirm('确定要删除该入库单吗？删除后无法恢复，包括明细表也会被删除。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteAssetInStorage(this.storageId).then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.handleClose()
            this.$emit('refresh')
          } else {
            this.$message.error(res.msg || '删除失败')
          }
        }).catch(err => {
          console.error('删除入库单失败:', err)
          this.$message.error('删除失败')
        })
      }).catch(() => {})
    },
    handleClose() {
      this.dialogVisible = false
      this.storageData = null
      this.detailList = []
      this.activeTab = 'basic'
    },
    getStorageStatusText(status) {
      const option = this.storageStatusOptions.find(opt => opt.value === status)
      return option ? option.label : status
    },
    getStorageStatusType(status) {
      const typeMap = {
        'NOT_STORED': 'warning',
        'STORED': 'success'
      }
      return typeMap[status] || ''
    },
    formatDateOnly(date) {
      if (!date) return ''
      if (typeof date === 'string') {
        return date.substring(0, 10)
      }
      return date
    },
    formatMoney(amount) {
      if (amount == null) return '0.00'
      return parseFloat(amount).toFixed(2)
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

