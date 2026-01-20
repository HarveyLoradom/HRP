<template>
  <div class="procurement-storage">
    <el-card>
      <div slot="header" class="clearfix">
        <span>采购入库</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAddPurchase" v-if="activeTab === 'purchase'">新增采购单</el-button>
      </div>
      
      <!-- Tab 切换 -->
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <!-- Tab 1: 采购界面 -->
        <el-tab-pane label="采购界面" name="purchase">
          <!-- 查询表单 -->
          <el-form :model="purchaseSearchForm" :inline="true" style="margin-bottom: 20px;">
            <el-form-item label="订单号:">
              <el-input v-model="purchaseSearchForm.orderNo" placeholder="请输入订单号" clearable style="width: 200px;"></el-input>
            </el-form-item>
            <el-form-item label="申请单号:">
              <el-input v-model="purchaseSearchForm.applyNo" placeholder="请输入申请单号" clearable style="width: 200px;"></el-input>
            </el-form-item>
            <el-form-item label="采购状态:">
              <el-select v-model="purchaseSearchForm.purchaseStatus" placeholder="请选择采购状态" clearable style="width: 200px;">
                <el-option
                  v-for="option in purchaseStatusOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearchPurchase">查询</el-button>
              <el-button @click="handleResetPurchase">重置</el-button>
            </el-form-item>
          </el-form>
          
          <!-- 采购单列表 -->
          <el-table :data="purchaseList" border style="width: 100%">
            <el-table-column prop="orderNo" label="采购单号" width="180">
              <template slot-scope="scope">
                <el-button type="text" @click="handleViewPurchaseDetail(scope.row)">{{ scope.row.orderNo }}</el-button>
              </template>
            </el-table-column>
            <el-table-column prop="applyNo" label="申请单号" width="180"></el-table-column>
            <el-table-column prop="purchaseDate" label="采购日期" width="120">
              <template slot-scope="scope">
                {{ formatDateOnly(scope.row.purchaseDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="purchaseStatus" label="采购状态" width="120">
              <template slot-scope="scope">
                <el-tag :type="getPurchaseStatusType(scope.row.purchaseStatus)">
                  {{ getPurchaseStatusText(scope.row.purchaseStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="contractNo" label="合同编号" width="180"></el-table-column>
            <el-table-column prop="supplierName" label="供应商" width="200"></el-table-column>
            <el-table-column prop="totalAmount" label="采购总金额" width="150">
              <template slot-scope="scope">
                {{ formatMoney(scope.row.totalAmount) }}
              </template>
            </el-table-column>
            <el-table-column prop="operatorName" label="操作人" width="120"></el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页和导出 -->
          <div class="pagination-container" style="margin-top: 20px; display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
            <el-pagination
              @size-change="handlePurchaseSizeChange"
              @current-change="handlePurchaseCurrentChange"
              :current-page="purchasePagination.page"
              :page-sizes="[10, 20, 50, 100]"
              :page-size="purchasePagination.size"
              layout="total, sizes, prev, pager, next, jumper"
              :total="purchasePagination.total">
            </el-pagination>
            <el-dropdown @command="handleExportPurchaseCommand" :disabled="purchaseExportLoading">
              <el-button size="big" type="text" :loading="purchaseExportLoading">
                导出<i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="current">导出本页</el-dropdown-item>
                <el-dropdown-item command="all">导出全部</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-tab-pane>
        
        <!-- Tab 2: 入库界面 -->
        <el-tab-pane label="入库界面" name="storage">
          <!-- 查询表单 -->
          <el-form :model="storageSearchForm" :inline="true" style="margin-bottom: 20px;">
            <el-form-item label="入库单号:">
              <el-input v-model="storageSearchForm.storageNo" placeholder="请输入入库单号" clearable style="width: 200px;"></el-input>
            </el-form-item>
            <el-form-item label="采购单号:">
              <el-input v-model="storageSearchForm.orderNo" placeholder="请输入采购单号" clearable style="width: 200px;"></el-input>
            </el-form-item>
            <el-form-item label="申请单号:">
              <el-input v-model="storageSearchForm.applyNo" placeholder="请输入申请单号" clearable style="width: 200px;"></el-input>
            </el-form-item>
            <el-form-item label="入库状态:">
              <el-select v-model="storageSearchForm.storageStatus" placeholder="请选择入库状态" clearable style="width: 200px;">
                <el-option
                  v-for="option in storageStatusOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearchStorage">查询</el-button>
              <el-button @click="handleResetStorage">重置</el-button>
            </el-form-item>
          </el-form>
          
          <!-- 入库单列表 -->
          <el-table :data="storageList" border style="width: 100%">
            <el-table-column prop="storageNo" label="入库单号" width="180">
              <template slot-scope="scope">
                <el-button type="text" @click="handleViewStorageDetail(scope.row)">{{ scope.row.storageNo }}</el-button>
              </template>
            </el-table-column>
            <el-table-column prop="orderNo" label="采购单号" width="180"></el-table-column>
            <el-table-column prop="applyNo" label="申请单号" width="180"></el-table-column>
            <el-table-column prop="storageDate" label="入库日期" width="120">
              <template slot-scope="scope">
                {{ formatDateOnly(scope.row.storageDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="storageStatus" label="入库状态" width="120">
              <template slot-scope="scope">
                <el-tag :type="getStorageStatusType(scope.row.storageStatus)">
                  {{ getStorageStatusText(scope.row.storageStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalAmount" label="入库总金额" width="150">
              <template slot-scope="scope">
                {{ formatMoney(scope.row.totalAmount) }}
              </template>
            </el-table-column>
            <el-table-column prop="operatorName" label="操作人" width="120"></el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页和导出 -->
          <div class="pagination-container" style="margin-top: 20px; display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
            <el-pagination
              @size-change="handleStorageSizeChange"
              @current-change="handleStorageCurrentChange"
              :current-page="storagePagination.page"
              :page-sizes="[10, 20, 50, 100]"
              :page-size="storagePagination.size"
              layout="total, sizes, prev, pager, next, jumper"
              :total="storagePagination.total">
            </el-pagination>
            <el-dropdown @command="handleExportStorageCommand" :disabled="storageExportLoading">
              <el-button size="big" type="text" :loading="storageExportLoading">
                导出<i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="current">导出本页</el-dropdown-item>
                <el-dropdown-item command="all">导出全部</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 新增/编辑采购单对话框 -->
    <el-dialog :title="purchaseDialogTitle" :visible.sync="purchaseDialogVisible" width="1200px" @close="handlePurchaseDialogCancel">
      <el-tabs v-model="purchaseActiveTab" type="border-card">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="purchaseForm" :rules="purchaseRules" ref="purchaseForm" label-width="140px" style="margin-top: 20px;">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="操作人:">
                  <el-input v-model="purchaseForm.operatorName" disabled></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="操作人科室:">
                  <el-input v-model="purchaseForm.operatorDeptName" disabled></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="操作人手机号:">
                  <el-input v-model="purchaseForm.operatorPhone" disabled></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="申请单号:" prop="applyNo" required>
                  <el-input 
                    v-model="purchaseForm.applyNo" 
                    placeholder="点击选择申请单" 
                    readonly 
                    style="width: 100%"
                    @click.native="handleOpenApplyDialog"
                    :disabled="purchaseIsEdit"
                  >
                    <el-button slot="append" icon="el-icon-search" @click="handleOpenApplyDialog" :disabled="purchaseIsEdit"></el-button>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="采购日期:" prop="purchaseDate">
                  <el-date-picker
                    v-model="purchaseForm.purchaseDate"
                    type="date"
                    placeholder="选择采购日期"
                    format="yyyy-MM-dd"
                    value-format="yyyy-MM-dd"
                    style="width: 100%;"
                    :disabled="purchaseIsEdit && purchaseForm.purchaseStatus === 'COMPLETED'"
                  ></el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="供应商名称:">
                  <el-input 
                    v-model="purchaseForm.supplierName" 
                    placeholder="点击选择供应商" 
                    readonly 
                    style="width: 100%"
                    @click.native="handleOpenSupplierDialog"
                    :disabled="purchaseIsEdit && purchaseForm.purchaseStatus === 'COMPLETED'"
                  >
                    <el-button slot="append" icon="el-icon-search" @click="handleOpenSupplierDialog" :disabled="purchaseIsEdit && purchaseForm.purchaseStatus === 'COMPLETED'"></el-button>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="采购状态:">
                  <el-select v-model="purchaseForm.purchaseStatus" placeholder="请选择采购状态" style="width: 100%;" :disabled="purchaseIsEdit && purchaseForm.purchaseStatus === 'COMPLETED'">
                    <el-option
                      v-for="option in purchaseStatusOptions"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="合同编号:">
                  <el-input 
                    v-model="purchaseForm.contractNo" 
                    placeholder="点击选择合同" 
                    readonly 
                    style="width: 100%"
                    @click.native="handleOpenContractDialog"
                    :disabled="purchaseIsEdit && purchaseForm.purchaseStatus === 'COMPLETED'"
                  >
                    <el-button slot="append" icon="el-icon-search" @click="handleOpenContractDialog" :disabled="purchaseIsEdit && purchaseForm.purchaseStatus === 'COMPLETED'"></el-button>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="采购总金额:" prop="totalAmount">
                  <el-input-number 
                    v-model="purchaseForm.totalAmount" 
                    :min="0" 
                    :precision="2" 
                    style="width: 100%" 
                    :disabled="true"
                    placeholder="自动计算"
                  ></el-input-number>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="备注:">
              <el-input type="textarea" v-model="purchaseForm.remark" :rows="3" placeholder="请输入备注" :disabled="purchaseIsEdit && purchaseForm.purchaseStatus === 'COMPLETED'"></el-input>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 资产信息 -->
        <el-tab-pane label="资产信息" name="asset">
          <div style="margin-top: 20px;">
            <el-table :data="purchaseDetailList" border style="width: 100%">
              <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
              <el-table-column prop="assetCode" label="资产编码" width="150"></el-table-column>
              <el-table-column prop="assetName" label="资产名称" width="200"></el-table-column>
              <el-table-column prop="spec" label="规格型号" width="200"></el-table-column>
              <el-table-column prop="manufacturer" label="生产厂家" width="150"></el-table-column>
              <el-table-column prop="unit" label="单位" width="80"></el-table-column>
              <el-table-column label="申请数量" width="120">
                <template slot-scope="scope">
                  <span>{{ scope.row.applyQuantity || 0 }}</span>
                </template>
              </el-table-column>
              <el-table-column label="采购数量" width="150">
                <template slot-scope="scope">
                  <el-input-number
                    v-model="scope.row.purchaseQuantity"
                    :min="1"
                    :precision="0"
                    style="width: 100%"
                    @change="handlePurchaseDetailChange(scope.$index)"
                    :disabled="purchaseIsEdit && purchaseForm.purchaseStatus === 'COMPLETED'"
                  ></el-input-number>
                </template>
              </el-table-column>
              <el-table-column label="实际单价" width="150">
                <template slot-scope="scope">
                  <el-input-number
                    v-model="scope.row.price"
                    :min="0"
                    :precision="2"
                    style="width: 100%"
                    @change="handlePurchaseDetailChange(scope.$index)"
                    :disabled="purchaseIsEdit && purchaseForm.purchaseStatus === 'COMPLETED'"
                  ></el-input-number>
                </template>
              </el-table-column>
              <el-table-column label="总价" width="150">
                <template slot-scope="scope">
                  <span style="font-weight: bold;">¥{{ formatMoney(scope.row.totalPrice) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="备注" width="200">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.remark" placeholder="请输入备注" size="small" :disabled="purchaseIsEdit && purchaseForm.purchaseStatus === 'COMPLETED'"></el-input>
                </template>
              </el-table-column>
            </el-table>
            <div style="margin-top: 10px; text-align: right;">
              <strong>合计金额：¥{{ formatMoney(purchaseTotalAmount) }}</strong>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="handlePurchaseDialogCancel">取消</el-button>
        <el-button type="primary" @click="handlePurchaseSave" :disabled="purchaseIsEdit && purchaseForm.purchaseStatus === 'COMPLETED'">保存</el-button>
      </div>
    </el-dialog>
    
    <!-- 采购单详情对话框 -->
    <PurchaseDetailDialog 
      :visible.sync="purchaseDetailVisible" 
      :purchase-id="currentPurchaseId"
      @refresh="handleSearchPurchase"
      @edit="handleEditFromDetail"
    />
    
    <!-- 入库单详情对话框 -->
    <StorageDetailDialog 
      :visible.sync="storageDetailVisible" 
      :storage-id="currentStorageId"
      @refresh="handleSearchStorage"
    />
    
    <!-- 申请单选择对话框 -->
    <el-dialog title="选择申请单" :visible.sync="applyDialogVisible" width="900px">
      <el-form :model="applySearchForm" :inline="true" style="margin-bottom: 15px;">
        <el-form-item label="申请单号:">
          <el-input v-model="applySearchForm.applyNo" placeholder="请输入申请单号" clearable style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleApplySearch">查询</el-button>
          <el-button @click="handleApplySearchReset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table 
        :data="applyList" 
        border 
        style="width: 100%;" 
        v-loading="applyLoading"
        @row-click="handleSelectApply"
        highlight-current-row
      >
        <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
        <el-table-column prop="applyNo" label="申请单号" width="160"></el-table-column>
        <el-table-column prop="applyEmpName" label="申请人" width="120"></el-table-column>
        <el-table-column prop="applyDeptName" label="申请部门" width="150"></el-table-column>
        <el-table-column prop="applyMoney" label="申请金额" width="150">
          <template slot-scope="scope">
            ¥{{ formatMoney(scope.row.applyMoney) }}
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="160">
          <template slot-scope="scope">
            {{ formatDateOnly(scope.row.applyTime || scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmApply" :disabled="!selectedApply">确认选择</el-button>
      </div>
    </el-dialog>
    
    <!-- 合同选择对话框 -->
    <el-dialog title="选择关联合同" :visible.sync="contractDialogVisible" width="1000px">
      <el-form :model="contractSearchForm" :inline="true" style="margin-bottom: 15px;">
        <el-form-item label="合同编码:">
          <el-input v-model="contractSearchForm.contractNo" placeholder="请输入合同编码" clearable style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item label="合同名称:">
          <el-input v-model="contractSearchForm.contractName" placeholder="请输入合同名称" clearable style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleContractSearch">查询</el-button>
          <el-button @click="handleContractSearchReset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table 
        :data="contractList" 
        border 
        style="width: 100%;" 
        v-loading="contractLoading"
        @row-click="handleSelectContract"
        highlight-current-row
      >
        <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
        <el-table-column prop="contractNo" label="合同编号" width="160"></el-table-column>
        <el-table-column prop="contractName" label="合同名称" width="200"></el-table-column>
        <el-table-column prop="contractType" label="合同类型" width="120">
          <template slot-scope="scope">
            {{ getContractTypeName(scope.row.contractType) }}
          </template>
        </el-table-column>
        <el-table-column prop="partyA" label="甲方" width="150"></el-table-column>
        <el-table-column prop="partyB" label="乙方" width="150"></el-table-column>
        <el-table-column prop="contractAmount" label="合同金额" width="120">
          <template slot-scope="scope">
            ¥{{ formatMoney(scope.row.contractAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getContractStatusType(scope.row.status)" size="small">
              {{ getContractStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="contractDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmContract" :disabled="!selectedContract">确认选择</el-button>
      </div>
    </el-dialog>
    
    <!-- 供应商选择对话框 -->
    <!-- 供应商管理对话框 -->
    <el-dialog title="供应商管理" :visible.sync="supplierDialogVisible" width="1000px">
      <!-- 查询表单 -->
      <el-form :model="supplierSearchForm" :inline="true" style="margin-bottom: 15px;">
        <el-form-item label="供应商编码:">
          <el-input v-model="supplierSearchForm.supplierCode" placeholder="请输入供应商编码" clearable style="width: 180px;"></el-input>
        </el-form-item>
        <el-form-item label="供应商名称:">
          <el-input v-model="supplierSearchForm.supplierName" placeholder="请输入供应商名称" clearable style="width: 180px;"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSupplierSearch">查询</el-button>
          <el-button @click="handleSupplierSearchReset">重置</el-button>
        </el-form-item>
      </el-form>
      <div style="margin-bottom: 10px;">
        <el-button type="primary" size="small" @click="handleAddSupplier">新增供应商</el-button>
      </div>
      <el-table 
        :data="supplierList" 
        border 
        style="width: 100%;" 
        v-loading="supplierLoading"
        @row-click="handleSelectSupplierRow"
        highlight-current-row
      >
        <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
        <el-table-column prop="supplierCode" label="供应商编码" width="150"></el-table-column>
        <el-table-column prop="supplierName" label="供应商名称" width="200"></el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="120"></el-table-column>
        <el-table-column prop="contactPhone" label="联系电话" width="150"></el-table-column>
        <el-table-column prop="bankName" label="银行名称" width="150"></el-table-column>
        <el-table-column prop="bankAccount" label="银行账号" width="180"></el-table-column>
        <el-table-column prop="accountName" label="账户名称" width="150"></el-table-column>
        <el-table-column prop="isStop" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isStop === 0 ? 'success' : 'danger'">
              {{ scope.row.isStop === 0 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click.stop="handleSelectSupplier(scope.row)">选择</el-button>
            <el-button size="mini" @click.stop="handleEditSupplier(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click.stop="handleDeleteSupplier(scope.row)">删除</el-button>
            <el-button 
              v-if="scope.row.isStop === 0" 
              size="mini" 
              type="warning" 
              @click.stop="handleStopSupplier(scope.row)"
            >停用</el-button>
            <el-button 
              v-if="scope.row.isStop === 1" 
              size="mini" 
              type="success" 
              @click.stop="handleStartSupplier(scope.row)"
            >启用</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="supplierDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleConfirmSelectedSupplier" 
          :disabled="!selectedSupplier"
        >
          确认选择{{ selectedSupplier ? `（已选择：${selectedSupplier.supplierName}）` : '' }}
        </el-button>
      </div>
    </el-dialog>

    <!-- 供应商表单对话框 -->
    <el-dialog :title="supplierFormTitle" :visible.sync="supplierFormVisible" width="800px">
      <el-form :model="supplierForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="供应商编码" required>
              <el-input v-model="supplierForm.supplierCode" placeholder="请输入供应商编码"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="供应商名称" required>
              <el-input v-model="supplierForm.supplierName" placeholder="请输入供应商名称"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系人">
              <el-input v-model="supplierForm.contactPerson" placeholder="请输入联系人"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="联系电话">
              <el-input v-model="supplierForm.contactPhone" placeholder="请输入联系电话"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系邮箱">
              <el-input v-model="supplierForm.contactEmail" placeholder="请输入联系邮箱"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="地址">
              <el-input v-model="supplierForm.address" placeholder="请输入地址"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="银行名称">
              <el-input v-model="supplierForm.bankName" placeholder="请输入银行名称"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="银行账号">
              <el-input v-model="supplierForm.bankAccount" placeholder="请输入银行账号"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="账户名称">
              <el-input v-model="supplierForm.accountName" placeholder="请输入账户名称"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="税号">
              <el-input v-model="supplierForm.taxNumber" placeholder="请输入税号"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="supplierFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveSupplier">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getAssetPurchasePage,
  deleteAssetPurchase,
  getAssetPurchaseById,
  getAvailableApplies,
      getApplyByNo,
      saveAssetPurchase,
      updateAssetPurchase,
      getAssetPurchaseApplyPage
} from '@/api/asset'
import {
  getAssetInStoragePage,
  completeAssetInStorage,
  getAssetInStorageById
} from '@/api/asset'
import { getCodeTypeOptions } from '@/utils/codeType'
import { getAllContractsPage, getContractByNo } from '@/api/contract'
import { getSuppliers, saveSupplier, updateSupplier, deleteSupplier, stopSupplier, startSupplier } from '@/api/reimb'
import { exportExcel } from '@/api/common'
import PurchaseDetailDialog from './PurchaseDetailDialog.vue'
import StorageDetailDialog from './StorageDetailDialog.vue'

export default {
  name: 'ProcurementStorage',
  components: {
    PurchaseDetailDialog,
    StorageDetailDialog
  },
  data() {
    return {
      activeTab: 'purchase',
      
      // 采购界面数据
      purchaseSearchForm: {
        orderNo: '',
        applyNo: '',
        purchaseStatus: '',
        startDate: '',
        endDate: ''
      },
      purchaseList: [],
      purchasePagination: {
        page: 1,
        size: 10,
        total: 0
      },
      purchaseStatusOptions: [],
      purchaseExportLoading: false,
      
      // 入库界面数据
      storageSearchForm: {
        storageNo: '',
        orderNo: '',
        applyNo: '',
        storageStatus: '',
        startDate: '',
        endDate: ''
      },
      storageList: [],
      storagePagination: {
        page: 1,
        size: 10,
        total: 0
      },
      storageStatusOptions: [],
      storageExportLoading: false,
      
      // 对话框
      purchaseDetailVisible: false,
      currentPurchaseId: null,
      storageDetailVisible: false,
      currentStorageId: null,
      
      // 新增/编辑采购单对话框
      purchaseDialogVisible: false,
      purchaseDialogTitle: '新增采购单',
      purchaseIsEdit: false,
      purchaseActiveTab: 'basic', // 对话框内tab激活项
      purchaseForm: {
        id: null,
        orderNo: '',
        applyNo: '',
        applyId: null,
        purchaseDate: new Date().toISOString().substring(0, 10),
        purchaseStatus: 'PURCHASING',
        contractNo: '',
        supplierId: null,
        supplierName: '',
        totalAmount: 0,
        operatorName: '',
        operatorDeptName: '',
        operatorPhone: '',
        remark: ''
      },
      contractAmount: null, // 合同金额（用于校验）
      purchaseRules: {
        applyNo: [
          { required: true, message: '请选择申请单号', trigger: 'change' }
        ],
        purchaseDate: [
          { required: true, message: '请选择采购日期', trigger: 'change' }
        ],
        totalAmount: [
          { 
            validator: (rule, value, callback) => {
              // 计算资产信息的总价之和
              const calculatedTotal = this.purchaseDetailList.reduce((sum, item) => {
                return sum + (parseFloat(item.totalPrice) || 0)
              }, 0)
              
              // 校验：总金额必须等于资产信息的总价之和
              if (Math.abs(value - calculatedTotal) > 0.01) {
                callback(new Error(`总金额必须等于资产信息的总价之和（¥${calculatedTotal.toFixed(2)}）`))
                return
              }
              
              // 如果有合同，校验总金额必须小于等于合同金额
              if (this.contractAmount && value > parseFloat(this.contractAmount)) {
                callback(new Error(`总金额不能超过合同金额（¥${parseFloat(this.contractAmount).toFixed(2)}）`))
                return
              }
              
              callback()
            },
            trigger: 'blur'
          }
        ]
      },
      purchaseDetailList: [],
      applyOptions: [],
      applyLoading: false,
      // 申请单选择对话框
      applyDialogVisible: false,
      applyList: [],
      applySearchForm: {
        applyNo: ''
      },
      selectedApply: null,
      // 合同选择对话框
      contractDialogVisible: false,
      contractList: [],
      contractLoading: false,
      selectedContract: null,
      contractSearchForm: {
        contractNo: '',
        contractName: ''
      },
      contractTypeOptions: [],
      // 供应商选择对话框
      supplierDialogVisible: false,
      supplierList: [],
      supplierLoading: false,
      selectedSupplier: null,
      supplierSearchForm: {
        supplierCode: '',
        supplierName: ''
      },
      supplierForm: { // 供应商表单
        supplierId: null,
        supplierCode: '',
        supplierName: '',
        contactPerson: '',
        contactPhone: '',
        contactEmail: '',
        address: '',
        bankName: '',
        bankAccount: '',
        accountName: '',
        taxNumber: '',
        isStop: 0
      },
      supplierFormVisible: false, // 供应商表单对话框
      supplierFormTitle: '新增供应商'
    }
  },
  async mounted() {
    // 加载状态选项
    await this.loadStatusOptions()
    
    // 检查路由参数，切换tab
    if (this.$route.query.tab) {
      this.activeTab = this.$route.query.tab
    }
    
    // 根据当前tab加载数据
    if (this.activeTab === 'purchase') {
      this.handleSearchPurchase()
    } else {
      this.handleSearchStorage()
    }
  },
  methods: {
    // 加载状态选项
    async loadStatusOptions() {
      try {
        this.purchaseStatusOptions = await getCodeTypeOptions('PURCHASE_STATUS')
        this.storageStatusOptions = await getCodeTypeOptions('IN_STORAGE_STATUS')
        this.contractTypeOptions = await getCodeTypeOptions('CONTRACT_TYPE')
      } catch (error) {
        console.error('加载状态选项失败:', error)
        // 使用默认值
        this.purchaseStatusOptions = [
          { label: '采购中', value: 'PURCHASING' },
          { label: '已完成', value: 'COMPLETED' },
          { label: '已取消', value: 'CANCELLED' }
        ]
        this.storageStatusOptions = [
          { label: '未入库', value: 'NOT_STORED' },
          { label: '已入库', value: 'STORED' }
        ]
      }
    },
    // Tab 切换
    handleTabClick(tab) {
      if (tab.name === 'purchase') {
        this.handleSearchPurchase()
      } else if (tab.name === 'storage') {
        this.handleSearchStorage()
      }
    },
    
    // 采购界面方法
    handleSearchPurchase() {
      this.purchasePagination.page = 1
      this.loadPurchaseList()
    },
    handleResetPurchase() {
      this.purchaseSearchForm = {
        orderNo: '',
        applyNo: '',
        purchaseStatus: '',
        startDate: '',
        endDate: ''
      }
      this.handleSearchPurchase()
    },
    loadPurchaseList() {
      const params = {
        page: this.purchasePagination.page,
        size: this.purchasePagination.size,
        ...this.purchaseSearchForm
      }
      getAssetPurchasePage(params).then(res => {
        if (res.code === 200) {
          // PageResult使用records字段，不是list
          this.purchaseList = res.data.records || res.data.list || []
          this.purchasePagination.total = res.data.total || 0
        } else {
          this.$message.error(res.msg || '查询失败')
        }
      }).catch(err => {
        console.error('查询采购单列表失败:', err)
        this.$message.error('查询失败')
      })
    },
    handlePurchaseSizeChange(size) {
      this.purchasePagination.size = size
      this.loadPurchaseList()
    },
    handlePurchaseCurrentChange(page) {
      this.purchasePagination.page = page
      this.loadPurchaseList()
    },
    handleAddPurchase() {
      this.purchaseDialogTitle = '新增采购单'
      this.purchaseIsEdit = false
      
      // 获取当前登录用户信息
      const userInfo = this.$store.state.user.userInfo || {}
      const operatorName = userInfo.empName || userInfo.emp_name || userInfo.realName || userInfo.name || ''
      const operatorDeptName = userInfo.deptName || userInfo.dept_name || ''
      const operatorPhone = userInfo.empPhone || userInfo.emp_phone || userInfo.phone || userInfo.mobile || ''
      
      this.purchaseForm = {
        id: null,
        orderNo: '',
        applyNo: '',
        applyId: null,
        purchaseDate: new Date().toISOString().substring(0, 10),
        purchaseStatus: 'PURCHASING',
        contractNo: '',
        supplierId: null,
        supplierName: '',
        totalAmount: 0,
        operatorName: operatorName,
        operatorDeptName: operatorDeptName,
        operatorPhone: operatorPhone,
        remark: ''
      }
      this.contractAmount = null
      this.purchaseDetailList = []
      this.purchaseActiveTab = 'basic'
      this.purchaseDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.purchaseForm) {
          this.$refs.purchaseForm.clearValidate()
        }
      })
    },
    handleViewPurchaseDetail(row) {
      this.currentPurchaseId = row.id
      this.purchaseDetailVisible = true
    },
    // 从详情页触发编辑
    handleEditFromDetail(purchaseId) {
      // 先加载数据检查状态
      getAssetPurchaseById(purchaseId).then(res => {
        if (res.code === 200 && res.data) {
          const purchase = res.data.purchase || res.data
          if (purchase.purchaseStatus === 'COMPLETED') {
            this.$message.warning('已完成状态的采购单不允许编辑')
            return
          }
          this.purchaseDialogTitle = '编辑采购单'
          this.purchaseIsEdit = true
          this.loadPurchaseDetailForEdit(purchaseId)
        }
      }).catch(err => {
        console.error('检查采购单状态失败:', err)
        this.$message.error('检查采购单状态失败')
      })
    },
    handleEditPurchase(row) {
      if (row.purchaseStatus === 'COMPLETED') {
        this.$message.warning('已完成状态的采购单不允许编辑')
        return
      }
      this.purchaseDialogTitle = '编辑采购单'
      this.purchaseIsEdit = true
      this.loadPurchaseDetailForEdit(row.id)
    },
    handleDeletePurchase(row) {
      this.$confirm('确定要删除该采购单吗？删除后无法恢复。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteAssetPurchase(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.handleSearchPurchase()
          } else {
            this.$message.error(res.msg || '删除失败')
          }
        }).catch(err => {
          console.error('删除采购单失败:', err)
          this.$message.error('删除失败')
        })
      }).catch(() => {})
    },
    
    // 入库界面方法
    handleSearchStorage() {
      this.storagePagination.page = 1
      this.loadStorageList()
    },
    handleResetStorage() {
      this.storageSearchForm = {
        storageNo: '',
        orderNo: '',
        applyNo: '',
        storageStatus: '',
        startDate: '',
        endDate: ''
      }
      this.handleSearchStorage()
    },
    loadStorageList() {
      const params = {
        page: this.storagePagination.page,
        size: this.storagePagination.size,
        ...this.storageSearchForm
      }
      getAssetInStoragePage(params).then(res => {
        if (res.code === 200) {
          // PageResult使用records字段，不是list
          this.storageList = res.data.records || res.data.list || []
          this.storagePagination.total = res.data.total || 0
        } else {
          this.$message.error(res.msg || '查询失败')
        }
      }).catch(err => {
        console.error('查询入库单列表失败:', err)
        this.$message.error('查询失败')
      })
    },
    handleStorageSizeChange(size) {
      this.storagePagination.size = size
      this.loadStorageList()
    },
    handleStorageCurrentChange(page) {
      this.storagePagination.page = page
      this.loadStorageList()
    },
    handleViewStorageDetail(row) {
      this.currentStorageId = row.id
      this.storageDetailVisible = true
    },
    handleCompleteStorage(row) {
      this.$confirm('确定要完成入库吗？完成后将更新资产账户的库存数量。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        completeAssetInStorage(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('入库成功')
            this.handleSearchStorage()
          } else {
            this.$message.error(res.msg || '入库失败')
          }
        }).catch(err => {
          console.error('完成入库失败:', err)
          this.$message.error('入库失败')
        })
      }).catch(() => {})
    },
    
    // 工具方法
    getPurchaseStatusText(status) {
      const option = this.purchaseStatusOptions.find(opt => opt.value === status)
      return option ? option.label : status
    },
    getPurchaseStatusType(status) {
      const typeMap = {
        'PURCHASING': 'warning',
        'COMPLETED': 'success',
        'CANCELLED': 'info'
      }
      return typeMap[status] || ''
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
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      if (typeof dateTime === 'string') {
        return dateTime.substring(0, 19).replace('T', ' ')
      }
      return dateTime
    },
    formatMoney(amount) {
      if (amount == null) return '0.00'
      return parseFloat(amount).toFixed(2)
    },
    
    // 新增/编辑采购单相关方法
    // 打开申请单选择对话框
    async handleOpenApplyDialog() {
      if (this.purchaseIsEdit) {
        return
      }
      this.applyDialogVisible = true
      this.applySearchForm = {
        applyNo: ''
      }
      this.selectedApply = null
      await this.loadApplyList()
    },
    // 加载申请单列表
    async loadApplyList() {
      this.applyLoading = true
      try {
        const params = {
          page: 1,
          size: 1000,
          status: 'APPROVED' // 只查询已审批的申请单
        }
        if (this.applySearchForm.applyNo) {
          params.applyNo = this.applySearchForm.applyNo
        }
        const response = await getAssetPurchaseApplyPage(params)
        if (response.code === 200 && response.data) {
          this.applyList = response.data.list || response.data.records || []
        } else {
          this.applyList = []
        }
      } catch (error) {
        console.error('加载申请单列表失败', error)
        this.$message.error('加载申请单列表失败：' + (error.message || '未知错误'))
        this.applyList = []
      } finally {
        this.applyLoading = false
      }
    },
    // 申请单查询
    handleApplySearch() {
      this.loadApplyList()
    },
    // 申请单查询重置
    handleApplySearchReset() {
      this.applySearchForm = {
        applyNo: ''
      }
      this.loadApplyList()
    },
    // 选择申请单
    handleSelectApply(row) {
      this.selectedApply = row
    },
    // 确认选择申请单
    async handleConfirmApply() {
      if (!this.selectedApply) {
        this.$message.warning('请先选择申请单')
        return
      }
      
      // 设置申请单号
      this.purchaseForm.applyNo = this.selectedApply.applyNo
      this.applyDialogVisible = false
      
      // 导入申请单信息
      await this.loadApplyDetail(this.selectedApply.applyNo)
    },
    // 加载申请单明细
    async loadApplyDetail(applyNo) {
      if (!applyNo) {
        this.purchaseDetailList = []
        return
      }
      
      try {
        const res = await getApplyByNo(applyNo)
        if (res.code === 200 && res.data) {
          const apply = res.data.apply || res.data
          const details = res.data.details || []
          
          // 设置申请ID
          this.purchaseForm.applyId = apply.id
          
          // 导入明细表信息
          this.purchaseDetailList = details.map(detail => ({
            id: null,
            applyDetailId: detail.id,
            assetCode: detail.assetCode,
            assetName: detail.assetName,
            spec: detail.spec,
            manufacturer: detail.manufacturer,
            unit: detail.unit,
            applyQuantity: detail.applyQuantity,
            purchaseQuantity: detail.applyQuantity, // 默认采购数量等于申请数量
            price: detail.price || 0,
            totalPrice: (detail.applyQuantity || 0) * (detail.price || 0),
            remark: detail.remark || ''
          }))
          
          // 重新计算总金额（遍历所有明细）
          this.purchaseDetailList.forEach((detail) => {
            if (detail.purchaseQuantity && detail.price) {
              detail.totalPrice = parseFloat(detail.purchaseQuantity) * parseFloat(detail.price)
            } else {
              detail.totalPrice = 0
            }
          })
          
          // 更新总金额
          this.updatePurchaseTotalAmount()
        } else {
          this.$message.error(res.msg || '加载申请单信息失败')
        }
      } catch (err) {
        console.error('加载申请单信息失败:', err)
        this.$message.error('加载申请单信息失败')
      }
    },
    
    // 加载采购单详情用于编辑
    loadPurchaseDetailForEdit(id) {
      getAssetPurchaseById(id).then(res => {
        if (res.code === 200 && res.data) {
          const purchase = res.data.purchase || res.data
          const details = res.data.details || []
          
          this.purchaseForm = {
            id: purchase.id,
            orderNo: purchase.orderNo,
            applyNo: purchase.applyNo,
            applyId: purchase.applyId,
            purchaseDate: purchase.purchaseDate || new Date().toISOString().substring(0, 10),
            purchaseStatus: purchase.purchaseStatus || 'PURCHASING',
            contractNo: purchase.contractNo || '',
            supplierId: purchase.supplierId || null,
            supplierName: purchase.supplierName || '',
            totalAmount: purchase.totalAmount || 0,
            operatorName: purchase.operatorName || '',
            operatorDeptName: purchase.operatorDeptName || '',
            operatorPhone: purchase.operatorPhone || '',
            remark: purchase.remark || ''
          }
          
          this.purchaseDetailList = details.map(detail => ({
            id: detail.id,
            applyDetailId: detail.applyDetailId,
            assetCode: detail.assetCode,
            assetName: detail.assetName,
            spec: detail.spec,
            manufacturer: detail.manufacturer,
            unit: detail.unit,
            applyQuantity: detail.applyQuantity,
            purchaseQuantity: detail.purchaseQuantity,
            price: detail.price || 0,
            totalPrice: detail.totalPrice || 0,
            remark: detail.remark || ''
          }))
          
          // 如果有合同编号，加载合同金额
          if (this.purchaseForm.contractNo) {
            this.loadContractAmount(this.purchaseForm.contractNo)
          } else {
            this.contractAmount = null
          }
          
          // 更新总金额
          this.updatePurchaseTotalAmount()
          
          this.purchaseActiveTab = 'basic'
          this.purchaseDialogVisible = true
          this.$nextTick(() => {
            if (this.$refs.purchaseForm) {
              this.$refs.purchaseForm.clearValidate()
            }
          })
        } else {
          this.$message.error(res.msg || '加载失败')
        }
      }).catch(err => {
        console.error('加载采购单详情失败:', err)
        this.$message.error('加载失败')
      })
    },
    // 根据合同编号加载合同金额
    async loadContractAmount(contractNo) {
      if (!contractNo) {
        this.contractAmount = null
        return
      }
      try {
        const response = await getContractByNo(contractNo)
        if (response.code === 200 && response.data) {
          this.contractAmount = response.data.contractAmount
        } else {
          this.contractAmount = null
        }
      } catch (error) {
        // 合同API可能不存在，静默失败，不影响其他功能
        console.warn('加载合同金额失败（合同API可能不存在）:', error)
        this.contractAmount = null
      }
    },
    
    // 明细变化时重新计算总价
    handlePurchaseDetailChange(index) {
      const detail = this.purchaseDetailList[index]
      if (detail.purchaseQuantity && detail.price) {
        detail.totalPrice = parseFloat(detail.purchaseQuantity) * parseFloat(detail.price)
      } else {
        detail.totalPrice = 0
      }
      // 自动更新总金额
      this.updatePurchaseTotalAmount()
    },
    // 更新采购总金额
    updatePurchaseTotalAmount() {
      const total = this.purchaseDetailList.reduce((sum, item) => {
        return sum + (parseFloat(item.totalPrice) || 0)
      }, 0)
      this.purchaseForm.totalAmount = total
      // 触发校验
      this.$nextTick(() => {
        if (this.$refs.purchaseForm) {
          this.$refs.purchaseForm.validateField('totalAmount')
        }
      })
    },
    
    // 保存采购单
    handlePurchaseSave() {
      // 如果是编辑已完成状态的采购单，不允许保存
      if (this.purchaseIsEdit && this.purchaseForm.purchaseStatus === 'COMPLETED') {
        this.$message.warning('已完成状态的采购单不允许编辑')
        return false
      }
      
      // 先更新总金额
      this.updatePurchaseTotalAmount()
      
      this.$refs.purchaseForm.validate((valid) => {
        if (!valid) {
          return false
        }
        
        if (!this.purchaseDetailList || this.purchaseDetailList.length === 0) {
          this.$message.warning('请先选择申请单并导入明细信息')
          return false
        }
        
        // 再次校验总金额
        const calculatedTotal = this.purchaseDetailList.reduce((sum, item) => {
          return sum + (parseFloat(item.totalPrice) || 0)
        }, 0)
        
        if (Math.abs(this.purchaseForm.totalAmount - calculatedTotal) > 0.01) {
          this.$message.error(`总金额必须等于资产信息的总价之和（¥${calculatedTotal.toFixed(2)}）`)
          return false
        }
        
        // 如果有合同，校验总金额必须小于等于合同金额
        if (this.contractAmount && this.purchaseForm.totalAmount > parseFloat(this.contractAmount)) {
          this.$message.error(`总金额不能超过合同金额（¥${parseFloat(this.contractAmount).toFixed(2)}）`)
          return false
        }
        
        const data = {
          purchase: {
            id: this.purchaseForm.id,
            applyNo: this.purchaseForm.applyNo,
            applyId: this.purchaseForm.applyId,
            purchaseDate: this.purchaseForm.purchaseDate,
            purchaseStatus: this.purchaseForm.purchaseStatus,
            contractNo: this.purchaseForm.contractNo,
            supplierId: this.purchaseForm.supplierId,
            supplierName: this.purchaseForm.supplierName,
            totalAmount: this.purchaseForm.totalAmount,
            operatorName: this.purchaseForm.operatorName,
            operatorDeptName: this.purchaseForm.operatorDeptName,
            operatorPhone: this.purchaseForm.operatorPhone,
            remark: this.purchaseForm.remark
          },
          details: this.purchaseDetailList
        }
        
        const saveMethod = this.purchaseIsEdit ? updateAssetPurchase : saveAssetPurchase
        saveMethod(data).then(res => {
          if (res.code === 200) {
            this.$message.success(this.purchaseIsEdit ? '更新成功' : '保存成功')
            this.purchaseDialogVisible = false
            this.handleSearchPurchase()
          } else {
            this.$message.error(res.msg || '保存失败')
          }
        }).catch(err => {
          console.error('保存失败:', err)
          this.$message.error('保存失败')
        })
      })
    },
    
    // 取消对话框
    handlePurchaseDialogCancel() {
      this.purchaseDialogVisible = false
      this.purchaseForm = {
        id: null,
        orderNo: '',
        applyNo: '',
        applyId: null,
        purchaseDate: new Date().toISOString().substring(0, 10),
        purchaseStatus: 'PURCHASING',
        contractNo: '',
        supplierId: null,
        supplierName: '',
        totalAmount: 0,
        operatorName: '',
        operatorDeptName: '',
        operatorPhone: '',
        remark: ''
      }
      this.contractAmount = null
      this.purchaseDetailList = []
      this.purchaseActiveTab = 'basic'
      if (this.$refs.purchaseForm) {
        this.$refs.purchaseForm.clearValidate()
      }
    },
    
    // 合同选择对话框相关方法
    // 打开合同选择对话框
    async handleOpenContractDialog() {
      this.contractDialogVisible = true
      this.contractLoading = true
      this.selectedContract = null
      this.contractSearchForm = {
        contractNo: '',
        contractName: ''
      }
      await this.loadContractList()
    },
    // 加载合同列表
    async loadContractList() {
      this.contractLoading = true
      try {
        const response = await getAllContractsPage(1, 1000)
        if (response.code === 200 && response.data) {
          let contracts = (response.data.records || []).filter(item => item.status === 'APPROVED')
          
          // 应用查询条件
          if (this.contractSearchForm.contractNo) {
            contracts = contracts.filter(c => 
              c.contractNo && c.contractNo.includes(this.contractSearchForm.contractNo)
            )
          }
          if (this.contractSearchForm.contractName) {
            contracts = contracts.filter(c => 
              c.contractName && c.contractName.includes(this.contractSearchForm.contractName)
            )
          }
          
          this.contractList = contracts
        } else {
          this.contractList = []
        }
      } catch (error) {
        console.error('加载合同列表失败', error)
        this.$message.error('加载合同列表失败：' + (error.message || '未知错误'))
        this.contractList = []
      } finally {
        this.contractLoading = false
      }
    },
    // 合同查询
    handleContractSearch() {
      this.loadContractList()
    },
    // 合同查询重置
    handleContractSearchReset() {
      this.contractSearchForm = {
        contractNo: '',
        contractName: ''
      }
      this.loadContractList()
    },
    // 选择合同
    handleSelectContract(row) {
      this.selectedContract = row
    },
    // 确认选择合同
    handleConfirmContract() {
      if (!this.selectedContract) {
        this.$message.warning('请先选择合同')
        return
      }
      
      // 设置合同编号和合同金额
      this.purchaseForm.contractNo = this.selectedContract.contractNo
      this.contractAmount = this.selectedContract.contractAmount
      this.contractDialogVisible = false
      this.$message.success('已选择合同：' + this.selectedContract.contractNo)
      
      // 如果已有总金额，触发校验
      this.$nextTick(() => {
        if (this.$refs.purchaseForm && this.purchaseForm.totalAmount > 0) {
          this.$refs.purchaseForm.validateField('totalAmount')
        }
      })
    },
    // 获取合同类型名称
    getContractTypeName(codeValue) {
      const option = this.contractTypeOptions && this.contractTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    // 获取合同状态文本
    getContractStatusText(status) {
      const statusMap = {
        'DRAFT': '草稿',
        'PENDING': '待审批',
        'APPROVED': '已审批',
        'REJECTED': '已拒绝',
        'EXECUTING': '履约中',
        'COMPLETED': '已履约',
        'ARCHIVED': '已归档'
      }
      return statusMap[status] || status
    },
    // 获取合同状态类型
    getContractStatusType(status) {
      const typeMap = {
        'DRAFT': 'info',
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger',
        'EXECUTING': '',
        'COMPLETED': 'success',
        'ARCHIVED': 'info'
      }
      return typeMap[status] || ''
    },
    
    // 供应商选择对话框相关方法
    // 打开供应商选择对话框
    async handleOpenSupplierDialog() {
      this.supplierDialogVisible = true
      this.supplierSearchForm = {
        supplierCode: '',
        supplierName: ''
      }
      this.selectedSupplier = null
      await this.loadSupplierList()
    },
    // 加载供应商列表
    async loadSupplierList() {
      this.supplierLoading = true
      try {
        const params = {}
        if (this.supplierSearchForm.supplierCode) {
          params.supplierCode = this.supplierSearchForm.supplierCode
        }
        if (this.supplierSearchForm.supplierName) {
          params.supplierName = this.supplierSearchForm.supplierName
        }
        const response = await getSuppliers(params)
        if (response.code === 200 && response.data) {
          this.supplierList = response.data || []
        } else {
          this.supplierList = []
        }
      } catch (error) {
        console.error('加载供应商列表失败', error)
        this.$message.error('加载供应商列表失败：' + (error.message || '未知错误'))
        this.supplierList = []
      } finally {
        this.supplierLoading = false
      }
    },
    // 供应商查询
    handleSupplierSearch() {
      this.loadSupplierList()
    },
    // 供应商查询重置
    handleSupplierSearchReset() {
      this.supplierSearchForm = {
        supplierCode: '',
        supplierName: ''
      }
      this.loadSupplierList()
    },
    // 点击行选择供应商（单选）
    handleSelectSupplierRow(row) {
      this.selectedSupplier = row
    },
    // 点击选择按钮选择供应商
    handleSelectSupplier(row) {
      this.selectedSupplier = row
    },
    // 确认选择供应商
    handleConfirmSelectedSupplier() {
      if (!this.selectedSupplier) {
        this.$message.warning('请先选择供应商')
        return
      }
      
      if (this.selectedSupplier.isStop === 1) {
        this.$message.warning('不能选择已停用的供应商')
        return
      }
      
      // 设置供应商ID和名称
      this.purchaseForm.supplierId = this.selectedSupplier.supplierId
      this.purchaseForm.supplierName = this.selectedSupplier.supplierName
      this.supplierDialogVisible = false
      this.$message.success('已选择供应商：' + this.selectedSupplier.supplierName)
      this.selectedSupplier = null
    },
    // 新增供应商
    handleAddSupplier() {
      this.supplierFormTitle = '新增供应商'
      this.supplierForm = {
        supplierId: null,
        supplierCode: '',
        supplierName: '',
        contactPerson: '',
        contactPhone: '',
        contactEmail: '',
        address: '',
        bankName: '',
        bankAccount: '',
        accountName: '',
        taxNumber: '',
        isStop: 0
      }
      this.supplierFormVisible = true
    },
    // 编辑供应商
    handleEditSupplier(supplier) {
      this.supplierFormTitle = '编辑供应商'
      this.supplierForm = { ...supplier }
      this.supplierFormVisible = true
    },
    // 保存供应商
    async handleSaveSupplier() {
      if (!this.supplierForm.supplierCode || !this.supplierForm.supplierName) {
        this.$message.warning('请填写供应商编码和供应商名称')
        return
      }
      
      try {
        const api = this.supplierForm.supplierId ? updateSupplier : saveSupplier
        const response = await api(this.supplierForm)
        
        if (response.code === 200) {
          this.$message.success('保存成功')
          this.supplierFormVisible = false
          // 重新加载供应商列表
          await this.handleOpenSupplierDialog()
          // 如果保存的是当前选中的供应商，更新选中状态
          if (this.selectedSupplier && this.selectedSupplier.supplierId === this.supplierForm.supplierId) {
            const updated = this.supplierList.find(s => s.supplierId === this.supplierForm.supplierId)
            if (updated) {
              this.selectedSupplier = updated
            }
          }
        } else {
          this.$message.error(response.message || '保存失败')
        }
      } catch (error) {
        console.error('保存供应商失败', error)
        this.$message.error('保存供应商失败：' + (error.message || '未知错误'))
      }
    },
    // 删除供应商（物理删除）
    async handleDeleteSupplier(supplier) {
      this.$confirm('确认删除该供应商吗？删除后无法恢复！', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await deleteSupplier(supplier.supplierId)
          if (response.code === 200) {
            this.$message.success('删除成功')
            // 如果删除的是当前选中的供应商，清空选中
            if (this.selectedSupplier && this.selectedSupplier.supplierId === supplier.supplierId) {
              this.selectedSupplier = null
            }
            // 重新加载供应商列表
            await this.handleOpenSupplierDialog()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        } catch (error) {
          console.error('删除供应商失败', error)
          this.$message.error('删除供应商失败：' + (error.message || '未知错误'))
        }
      }).catch(() => {})
    },
    // 停用供应商
    async handleStopSupplier(supplier) {
      try {
        const response = await stopSupplier(supplier.supplierId)
        if (response.code === 200) {
          this.$message.success('停用成功')
          // 如果停用的是当前选中的供应商，清空选中
          if (this.selectedSupplier && this.selectedSupplier.supplierId === supplier.supplierId) {
            this.selectedSupplier = null
          }
          // 重新加载供应商列表
          await this.handleOpenSupplierDialog()
        } else {
          this.$message.error(response.message || '停用失败')
        }
      } catch (error) {
        console.error('停用供应商失败', error)
        this.$message.error('停用供应商失败：' + (error.message || '未知错误'))
      }
    },
    // 启用供应商
    async handleStartSupplier(supplier) {
      try {
        const response = await startSupplier(supplier.supplierId)
        if (response.code === 200) {
          this.$message.success('启用成功')
          // 重新加载供应商列表
          await this.handleOpenSupplierDialog()
        } else {
          this.$message.error(response.message || '启用失败')
        }
      } catch (error) {
        console.error('启用供应商失败', error)
        this.$message.error('启用供应商失败：' + (error.message || '未知错误'))
      }
    },
    // 处理采购导出命令
    handleExportPurchaseCommand(command) {
      if (command === 'current') {
        this.handleExportPurchase(false)
      } else if (command === 'all') {
        this.handleExportPurchase(true)
      }
    },
    // 导出采购数据
    async handleExportPurchase(exportAll) {
      this.purchaseExportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const params = {
            page: 1,
            size: 10000, // 设置一个很大的值以获取所有数据
            ...this.purchaseSearchForm
          }
          
          const res = await getAssetPurchasePage(params)
          if (res.code === 200 && res.data) {
            dataToExport = res.data.records || res.data.list || []
          } else {
            this.$message.error(res.message || '获取数据失败')
            return
          }
        } else {
          // 导出当前页数据
          dataToExport = this.purchaseList
        }
        
        if (dataToExport.length === 0) {
          this.$message.warning('没有数据可导出')
          return
        }
        
        // 构建表头
        const headers = ['采购单号', '申请单号', '采购日期', '采购状态', '合同编号', '供应商', '采购总金额', '操作人', '创建时间']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            String(item.orderNo || ''),
            String(item.applyNo || ''),
            String(this.formatDateOnly(item.purchaseDate) || ''),
            String(this.getPurchaseStatusText(item.purchaseStatus) || ''),
            String(item.contractNo || ''),
            String(item.supplierName || ''),
            String(item.totalAmount ? '¥' + item.totalAmount : ''),
            String(item.operatorName || ''),
            String(this.formatDateTime(item.createTime) || '')
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '采购单' + (exportAll ? '_全部' : '_第' + this.purchasePagination.page + '页'),
          headers: headers,
          dataList: dataList
        })
        
        // 处理blob响应并下载
        if (!response || !response.data) {
          this.$message.error('导出失败：服务器返回数据为空')
          return
        }
        
        const blob = response.data
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '采购单' + (exportAll ? '_全部' : '_第' + this.purchasePagination.page + '页') + '.xlsx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败：' + (error.message || '未知错误'))
      } finally {
        this.purchaseExportLoading = false
      }
    },
    // 处理入库导出命令
    handleExportStorageCommand(command) {
      if (command === 'current') {
        this.handleExportStorage(false)
      } else if (command === 'all') {
        this.handleExportStorage(true)
      }
    },
    // 导出入库数据
    async handleExportStorage(exportAll) {
      this.storageExportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const params = {
            page: 1,
            size: 10000, // 设置一个很大的值以获取所有数据
            ...this.storageSearchForm
          }
          
          const res = await getAssetInStoragePage(params)
          if (res.code === 200 && res.data) {
            dataToExport = res.data.records || res.data.list || []
          } else {
            this.$message.error(res.message || '获取数据失败')
            return
          }
        } else {
          // 导出当前页数据
          dataToExport = this.storageList
        }
        
        if (dataToExport.length === 0) {
          this.$message.warning('没有数据可导出')
          return
        }
        
        // 构建表头
        const headers = ['入库单号', '采购单号', '申请单号', '入库日期', '入库状态', '入库总金额', '操作人', '创建时间']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            String(item.storageNo || ''),
            String(item.orderNo || ''),
            String(item.applyNo || ''),
            String(this.formatDateOnly(item.storageDate) || ''),
            String(this.getStorageStatusText(item.storageStatus) || ''),
            String(item.totalAmount ? '¥' + item.totalAmount : ''),
            String(item.operatorName || ''),
            String(this.formatDateTime(item.createTime) || '')
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '入库单' + (exportAll ? '_全部' : '_第' + this.storagePagination.page + '页'),
          headers: headers,
          dataList: dataList
        })
        
        // 处理blob响应并下载
        if (!response || !response.data) {
          this.$message.error('导出失败：服务器返回数据为空')
          return
        }
        
        const blob = response.data
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '入库单' + (exportAll ? '_全部' : '_第' + this.storagePagination.page + '页') + '.xlsx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败：' + (error.message || '未知错误'))
      } finally {
        this.storageExportLoading = false
      }
    }
  },
  computed: {
    purchaseTotalAmount() {
      return this.purchaseDetailList.reduce((sum, item) => {
        return sum + (parseFloat(item.totalPrice) || 0)
      }, 0)
    }
  }
}
</script>