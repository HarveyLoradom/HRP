<template>
  <div class="my-reimb-payout">
    <el-card>
      <div slot="header" class="clearfix">
        <span>我的报账</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增报账</el-button>
      </div>
      
      <!-- 查询表单 -->
      <el-form :model="searchForm" style="margin-bottom: 20px;">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="报账单号:" label-width="100px">
              <el-input v-model="searchForm.payoutBillcode" placeholder="请输入报账单号" clearable style="width: 100%;"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="报账类型:" label-width="100px">
              <el-select v-model="searchForm.payoutTypeId" placeholder="请选择报账类型" clearable style="width: 100%;">
                <el-option
                  v-for="option in payoutTypeSearchOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态:" label-width="100px">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100%;">
                <el-option
                  v-for="option in applyStatusOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="报账时间:" label-width="100px">
              <el-date-picker
                v-model="searchForm.applyDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="yyyy-MM-dd"
                value-format="yyyy-MM-dd"
                style="width: 100%;"
              ></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24" style="text-align: right;">
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column label="报账单号" width="160">
          <template slot-scope="scope">
            <el-button type="text" @click="handleViewDetail(scope.row)">{{ scope.row && scope.row.payoutBillcode || '-' }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="empName" label="申请人" width="120">
          <template slot-scope="scope">
            <span>{{ scope.row.empName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="科室" width="150">
          <template slot-scope="scope">
            <span>{{ scope.row.deptName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="payoutTypeId" label="报账类型" width="120">
          <template slot-scope="scope">
            {{ getPayoutTypeName(scope.row.payoutTypeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="applyAmount" label="报账金额" width="130">
          <template slot-scope="scope">
            ¥{{ scope.row.applyAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批人" width="130">
          <template slot-scope="scope">
            <span>{{ getCurrentApprover(scope.row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="流程" width="80">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleViewProcess(scope.row)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="applyDate" label="报账时间" width="160">
          <template slot-scope="scope">
            {{ formatDateOnly(scope.row.applyDate || scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" style="margin-top: 20px; display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total">
        </el-pagination>
        <el-dropdown @command="handleExportCommand" :disabled="exportLoading">
          <el-button size="big" type="text" :loading="exportLoading">
            导出<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="current">导出本页</el-dropdown-item>
            <el-dropdown-item command="all">导出全部</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="1200px" :close-on-click-modal="false" @close="handleDialogCancel">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-tabs v-model="activeTab" type="border-card">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-form :model="form" :rules="rules" ref="form" label-width="120px">
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="申请人">
                    <el-input v-model="applicantInfo.empName" disabled></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="科室">
                    <el-input v-model="applicantInfo.deptName" disabled></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="手机号">
                    <el-input v-model="applicantInfo.empPhone" disabled></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="报账类型" prop="payout.payoutTypeId">
                    <el-select v-model="form.payout.payoutTypeId" placeholder="请选择报账类型" style="width: 100%">
                      <el-option
                        v-for="option in payoutTypeOptions"
                        :key="option.value"
                        :label="option.label"
                        :value="option.value"
                      ></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="模板配置" prop="payout.templateConfigId">
                    <el-select v-model="form.payout.templateConfigId" placeholder="请选择模板配置" filterable style="width: 100%">
                      <el-option
                        v-for="config in templateConfigOptions"
                        :key="config.configId"
                        :label="`${config.businessType}-${config.businessTypeValue}-${config.businessTypeName}`"
                        :value="config.configId"
                      ></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="报账金额" prop="payout.applyAmount">
                    <el-input-number v-model="form.payout.applyAmount" :min="0" :precision="2" style="width: 100%" :disabled="true"></el-input-number>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="申请时间" prop="payout.applyDate">
                    <el-date-picker
                      v-model="form.payout.applyDate"
                      type="date"
                      placeholder="选择申请时间"
                      style="width: 100%"
                      format="yyyy-MM-dd"
                      value-format="yyyy-MM-dd"
                      :default-value="new Date()"
                    ></el-date-picker>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="来源申请单号">
                    <el-input 
                      v-model="form.payout.sourceApplyNo" 
                      placeholder="点击选择申请单" 
                      readonly 
                      style="width: 100%"
                      @click.native="handleOpenSourceApplyDialog"
                    >
                      <el-button slot="append" icon="el-icon-search" @click="handleOpenSourceApplyDialog"></el-button>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="关联合同编号">
                    <el-input 
                      v-model="form.payout.contractNo" 
                      placeholder="点击选择合同" 
                      readonly 
                      style="width: 100%"
                      @click.native="handleOpenContractDialog"
                    >
                      <el-button slot="append" icon="el-icon-search" @click="handleOpenContractDialog"></el-button>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="是否护士">
                    <el-radio-group v-model="form.payout.isNurse">
                      <el-radio :label="0">否</el-radio>
                      <el-radio :label="1">是</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="报账事由" prop="payout.applyReason">
                <el-input type="textarea" v-model="form.payout.applyReason" :rows="4"></el-input>
              </el-form-item>
              
              <el-form-item label="附件">
                <el-upload
                  ref="upload"
                  action="#"
                  :file-list="fileList"
                  :on-remove="handleRemove"
                  :on-change="handleFileChange"
                  :before-upload="beforeUpload"
                  :on-preview="handlePreviewFile"
                  :auto-upload="false"
                  multiple
                >
                  <el-button size="small" type="primary">选择文件</el-button>
                  <div slot="tip" class="el-upload__tip">支持上传多个文件，单个文件大小不超过50MB。文件将在保存时上传到服务器</div>
                </el-upload>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          
          <!-- 预算项目tab -->
          <el-tab-pane label="预算项目" name="budget">
            <div style="margin-bottom: 20px;">
              <el-button type="primary" @click="handleOpenBudgetItemDialog" v-if="!form.payout.sourceApplyNo">新增预算</el-button>
              <el-alert
                v-if="form.payout.sourceApplyNo"
                title="已选择来源申请单，预算项目信息已自动加载，金额可编辑但不可超出原申请金额"
                type="info"
                :closable="false"
                show-icon
                style="margin-bottom: 20px;"
              ></el-alert>
            </div>
            
            <el-table :data="budgetDetailList" border style="width: 100%;">
              <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
              <el-table-column prop="itemName" label="预算项目" width="200"></el-table-column>
              <el-table-column prop="itemCode" label="项目编码" width="150"></el-table-column>
              <el-table-column prop="remainingAmount" label="剩余可执行金额" width="150">
                <template slot-scope="scope">
                  <span :style="{ color: scope.row.remainingAmount < 0 ? 'red' : '' }">
                    ¥{{ scope.row.remainingAmount || 0 }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="amount" label="报账金额" width="150">
                <template slot-scope="scope">
                  <el-input-number 
                    v-model="scope.row.amount" 
                    :min="0" 
                    :max="scope.row.fromSourceApply ? scope.row.maxAmount : (scope.row.remainingAmount || 0)"
                    :precision="2" 
                    style="width: 100%"
                    @change="handleBudgetDetailAmountChange"
                  ></el-input-number>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" v-if="!form.payout.sourceApplyNo">
                <template slot-scope="scope">
                  <el-button size="mini" type="danger" @click="handleRemoveBudgetDetail(scope.$index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          
          <!-- 发票信息 -->
          <el-tab-pane label="发票信息" name="invoice">
            <el-button type="primary" size="small" @click="handleAddInvoice" style="margin-bottom: 10px;">添加发票</el-button>
            <el-table :data="form.invoices" border>
              <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
              <el-table-column prop="invoiceCode" label="发票代码" width="150">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.invoiceCode" placeholder="请输入发票代码"></el-input>
                </template>
              </el-table-column>
              <el-table-column prop="invoiceNumber" label="发票号码" width="150">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.invoiceNumber" placeholder="请输入发票号码"></el-input>
                </template>
              </el-table-column>
              <el-table-column prop="invoiceAmount" label="发票金额" width="150">
                <template slot-scope="scope">
                  <el-input-number v-model="scope.row.invoiceAmount" :min="0" :precision="2" style="width: 100%"></el-input-number>
                </template>
              </el-table-column>
              <el-table-column prop="invoiceDate" label="发票日期" width="180">
                <template slot-scope="scope">
                  <el-date-picker 
                    v-model="scope.row.invoiceDate" 
                    type="date" 
                    placeholder="选择日期" 
                    style="width: 100%"
                    format="yyyy-MM-dd"
                    value-format="yyyy-MM-dd"
                  ></el-date-picker>
                </template>
              </el-table-column>
              <el-table-column prop="invoiceType" label="发票类型" width="150">
                <template slot-scope="scope">
                  <el-select v-model="scope.row.invoiceType" placeholder="请选择" style="width: 100%">
                    <el-option
                      v-for="option in invoiceTypeOptions"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                    ></el-option>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="taxAmount" label="税额" width="150">
                <template slot-scope="scope">
                  <el-input-number v-model="scope.row.taxAmount" :min="0" :precision="2" style="width: 100%"></el-input-number>
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注" width="200">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.remark" placeholder="请输入备注"></el-input>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template slot-scope="scope">
                  <el-button size="mini" type="danger" @click="handleRemoveInvoice(scope.$index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          
          <!-- 支付清单 -->
          <el-tab-pane label="支付清单" name="payment">
            <div style="margin-bottom: 10px;">
              <el-button type="primary" size="small" @click="handleAddPayment">添加支付</el-button>
              <el-button type="success" size="small" @click="handleOpenSupplierDialog">添加供应商</el-button>
            </div>
            <el-table :data="form.payments" border>
              <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
              <el-table-column prop="paymentObject" label="支付对象" width="150">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.paymentObject" placeholder="请输入支付对象"></el-input>
                </template>
              </el-table-column>
              <el-table-column prop="paymentMethod" label="支付方式" width="150">
                <template slot-scope="scope">
                  <el-select v-model="scope.row.paymentMethod" placeholder="请选择" style="width: 100%">
                    <el-option
                      v-for="option in paymentMethodOptions"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                    ></el-option>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="bankName" label="银行名称" width="150">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.bankName" placeholder="请输入银行名称"></el-input>
                </template>
              </el-table-column>
              <el-table-column prop="bankAccount" label="银行账号" width="180">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.bankAccount" placeholder="请输入银行账号"></el-input>
                </template>
              </el-table-column>
              <el-table-column prop="accountName" label="账户名称" width="150">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.accountName" placeholder="请输入账户名称"></el-input>
                </template>
              </el-table-column>
              <el-table-column prop="paymentAmount" label="支付金额" width="150">
                <template slot-scope="scope">
                  <el-input-number v-model="scope.row.paymentAmount" :min="0" :precision="2" style="width: 100%" @change="handlePaymentAmountChange"></el-input-number>
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注" width="200">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.remark" placeholder="请输入备注"></el-input>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template slot-scope="scope">
                  <el-button size="mini" type="danger" @click="handleRemovePayment(scope.$index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleDialogCancel">取消</el-button>
        <el-button @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="primary" @click="handleSaveAndSubmit">保存并提交</el-button>
      </div>
    </el-dialog>

    <!-- 审批确认弹窗 -->
    <ApprovalConfirmDialog
      v-model="approvalConfirmVisible"
      :next-node-info="nextNodeInfo"
      confirm-button-text="确认审批"
      @confirm="handleApprovalConfirm"
    />

    <!-- 统一详情组件 -->
    <ReimbPayoutDetail
      v-model="detailVisible"
      source-type="apply"
      :payout-id="selectedPayoutId"
      @edit="handleDetailEdit"
      @submitted="handleDetailSubmitted"
      @withdrawn="handleDetailWithdrawn"
      @deleted="handleDetailDeleted"
      @print="handleDetailPrint"
    />


    <!-- 流程查看对话框 -->
    <ProcessViewDialog
      :visible.sync="processVisible"
      :row="currentProcessRow"
      business-key-field="payoutBillcode"
      business-type-name="报账"
      :show-comment="true"
      :show-complete-time="true"
    />


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
        @selection-change="handleSupplierSelectionChange"
      >
        <el-table-column type="selection" width="55"></el-table-column>
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
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEditSupplier(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="handleDeleteSupplier(scope.row)">删除</el-button>
            <el-button 
              v-if="scope.row.isStop === 0" 
              size="mini" 
              type="warning" 
              @click="handleStopSupplier(scope.row)"
            >停用</el-button>
            <el-button 
              v-if="scope.row.isStop === 1" 
              size="mini" 
              type="success" 
              @click="handleStartSupplier(scope.row)"
            >启用</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="supplierDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmSelectedSuppliers" :disabled="selectedSuppliers.length === 0">
          确认选择（已选择 {{ selectedSuppliers.length }} 个）
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

    <!-- 预算项目选择对话框 -->
    <el-dialog title="选择预算项目" :visible.sync="budgetItemDialogVisible" width="900px">
      <el-form :model="budgetItemSearchForm" :inline="true" style="margin-bottom: 15px;">
        <el-form-item label="预算项目:">
          <el-input v-model="budgetItemSearchForm.itemName" placeholder="请输入预算项目" clearable style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item label="预算编码:">
          <el-input v-model="budgetItemSearchForm.itemCode" placeholder="请输入预算编码" clearable style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleBudgetItemSearch">查询</el-button>
          <el-button @click="handleBudgetItemSearchReset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-tabs v-model="budgetItemTab" @tab-click="handleBudgetItemTabChange">
        <el-tab-pane label="全部" name="all">
          <el-table 
            ref="allBudgetItemTable"
            :data="allBudgetItemList" 
            border 
            style="width: 100%;" 
            v-loading="budgetItemListLoading"
            @selection-change="handleBudgetItemSelectionChange">
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column prop="itemName" label="预算项目" width="200"></el-table-column>
            <el-table-column prop="itemCode" label="项目编码" width="150"></el-table-column>
            <el-table-column prop="budgetAmount" label="预算总额" width="150">
              <template slot-scope="scope">
                ¥{{ scope.row.budgetAmount || 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="remainingAmount" label="剩余可执行金额" width="150">
              <template slot-scope="scope">
                <span :style="{ color: scope.row.remainingAmount < 0 ? 'red' : '' }">
                  ¥{{ scope.row.remainingAmount || 0 }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="有余额" name="withBalance">
          <el-table 
            ref="withBalanceBudgetItemTable"
            :data="withBalanceBudgetItemList" 
            border 
            style="width: 100%;" 
            v-loading="budgetItemListLoading"
            @selection-change="handleBudgetItemSelectionChange">
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column prop="itemName" label="预算项目" width="200"></el-table-column>
            <el-table-column prop="itemCode" label="项目编码" width="150"></el-table-column>
            <el-table-column prop="budgetAmount" label="预算总额" width="150">
              <template slot-scope="scope">
                ¥{{ scope.row.budgetAmount || 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="remainingAmount" label="剩余可执行金额" width="150">
              <template slot-scope="scope">
                <span :style="{ color: scope.row.remainingAmount < 0 ? 'red' : '' }">
                  ¥{{ scope.row.remainingAmount || 0 }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <div slot="footer" class="dialog-footer">
        <span style="margin-right: 10px;">已选择 {{ selectedBudgetItemsInDialog.length }} 项</span>
        <el-button @click="budgetItemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmSelectedBudgetItems" :disabled="selectedBudgetItemsInDialog.length === 0">确认选择</el-button>
      </div>
    </el-dialog>

    <!-- 来源申请单选择对话框 -->
    <el-dialog title="选择来源申请单" :visible.sync="sourceApplyDialogVisible" width="900px">
      <el-form :model="sourceApplySearchForm" :inline="true" style="margin-bottom: 15px;">
        <el-form-item label="申请单号:">
          <el-input v-model="sourceApplySearchForm.payoutBillcode" placeholder="请输入申请单号" clearable style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item label="申请类型:">
          <el-select v-model="sourceApplySearchForm.payoutTypeId" placeholder="请选择申请类型" clearable style="width: 200px;">
            <el-option
              v-for="option in payoutTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSourceApplySearch">查询</el-button>
          <el-button @click="handleSourceApplySearchReset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table 
        :data="sourceApplyList" 
        border 
        style="width: 100%;" 
        v-loading="sourceApplyLoading"
        @row-click="handleSelectSourceApply"
        highlight-current-row
      >
        <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
        <el-table-column label="申请单号" width="160">
          <template slot-scope="scope">
            <span>{{ scope.row && scope.row.payoutBillcode || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="payoutTypeId" label="申请类型" width="120">
          <template slot-scope="scope">
            {{ getPayoutTypeName(scope.row.payoutTypeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="applyAmount" label="申请金额" width="130">
          <template slot-scope="scope">
            ¥{{ scope.row.applyAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="applyDate" label="申请时间" width="160">
          <template slot-scope="scope">
            {{ formatDateOnly(scope.row.applyDate || scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="sourceApplyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmSourceApply" :disabled="!selectedSourceApply">确认选择</el-button>
      </div>
    </el-dialog>

    <!-- 合同选择对话框 -->
    <el-dialog title="选择关联合同" :visible.sync="contractDialogVisible" width="1000px">
      <el-form :model="contractSearchForm" :inline="true" style="margin-bottom: 15px;">
        <el-form-item label="合同编码:">
          <el-input v-model="contractSearchForm.contractNo" placeholder="请输入合同编码" clearable style="width: 200px;"></el-input>
        </el-form-item>
        <el-form-item label="合同类型:">
          <el-select v-model="contractSearchForm.contractType" placeholder="请选择合同类型" clearable style="width: 200px;">
            <el-option
              v-for="option in contractTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            ></el-option>
          </el-select>
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
            ¥{{ scope.row.contractAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="contractDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmContract" :disabled="!selectedContract">确认选择</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMyPayoutsPage, submitPayout, getPayoutDetail, savePayoutFull, updatePayoutFull, approvePayout, getMyApplyList, getBudgetDetailsByBusinessNo, checkSourceApplyNo, getSuppliers, saveSupplier, updateSupplier, deleteSupplier, stopSupplier, startSupplier } from '@/api/reimb'
import { getTemplateConfigByBusinessTypeOnly, getTemplateConfigList, getTemplateConfigById } from '@/api/templateConfig'
import { getBudgetSubjects, getBudgetItems, getBudgetsBySubjectAndItem, getBudgetItemSubjects, getBudgetSubjectRelatedDepts, getBudgetRemainingAmount } from '@/api/budg'
import { getAttachmentsByBusiness, getAttachmentsByBusinessId, deleteAttachment, updateAttachmentBusinessId, uploadFile } from '@/api/attachment'
import { getUserById, getUserByAccount } from '@/api/user'
import { getCodeTypeOptions } from '@/utils/codeType'
import { getAllContractsPage, getContractByNo } from '@/api/contract'
import { checkContractNo } from '@/api/reimb'
import { paginationMixin } from '@/mixins/pagination'
import { exportExcel } from '@/api/common'
import ApprovalConfirmDialog from '@/components/ApprovalConfirmDialog.vue'
import ReimbPayoutDetail from '@/views/reimb/ReimbPayoutDetail.vue'
import { getNextNodeInfoByBusinessKey, getProcessTaskByTaskKey } from '@/api/process'
import ProcessViewDialog from '@/components/ProcessViewDialog.vue'
import { getDefaultPrintTemplate, generatePrintContent } from '@/api/print'
import Cookies from 'js-cookie'

export default {
  name: 'MyReimbPayout',
  mixins: [paginationMixin],
  components: {
    ApprovalConfirmDialog,
    ReimbPayoutDetail,
    ProcessViewDialog
  },
  data() {
    return {
      loading: false,
      exportLoading: false,
      tableData: [],
      payoutTypeOptions: [], // 表单中的报账类型选项（从PAYOUT_TYPE获取）
      payoutTypeSearchOptions: [], // 查询条件中的报账类型选项（从PAYOUT_STATUS获取）
      applyStatusOptions: [],
      contractTypeOptions: [], // 合同类型选项
      invoiceTypeOptions: [], // 发票类型选项（从sys_code表获取）
      paymentMethodOptions: [], // 支付方式选项（从sys_code表获取）
      budgetSubjects: [],
      budgetItems: [],
      budgets: [],
      budgetSubjectId: null,
      budgetItemId: null,
      approvalConfirmVisible: false,
      nextNodeInfo: null,
      currentApply: {},
      dialogVisible: false,
      detailVisible: false,
      selectedPayoutId: null,
      processVisible: false,
      currentProcessRow: null,
      currentApproverMap: {},
      dialogTitle: '新增报账',
      activeTab: 'basic',
      isEdit: false,
      applicantInfo: {
        empName: '',
        deptName: '',
        empPhone: ''
      },
      searchForm: {
        payoutBillcode: '',
        payoutTypeId: '', // 报账类型
        status: '',
        applyDateRange: null
      },
      fileList: [],
      uploadedAttachmentIds: [], // 记录新上传的附件ID，用于取消时删除
      uploadedInvoiceAttachmentIds: [], // 记录新上传的发票附件ID，用于取消时删除
      uploadUrl: '/api/auth/attachment/upload',
      uploadHeaders: {
        Authorization: 'Bearer ' + Cookies.get('token')
      },
      uploadData: {
        businessType: 'PAYOUT'
      },
      form: {
        payout: {
          payoutId: null,
          billType: 'PAYOUT',
          payoutTypeId: '',
          applyAmount: 0,
          applyReason: '',
          budgetId: null,
          budgetItemId: null,
          empId: null,
          sourceApplyNo: null, // 来源申请单号
          contractNo: null, // 关联合同编号
          applyDate: null, // 申请时间
          templateConfigId: null, // 模板配置ID
          isNurse: 0, // 是否护士：0-否，1-是
          status: 'DRAFT'
        },
        details: [],
        invoices: [],
        payments: []
      },
      sourceApplyDialogVisible: false, // 来源申请单选择对话框
      sourceApplyList: [], // 来源申请单列表
      sourceApplyLoading: false, // 来源申请单列表加载状态
      selectedSourceApply: null, // 选中的来源申请单
      sourceApplySearchForm: { // 来源申请单查询表单
        payoutBillcode: '',
        payoutTypeId: ''
      },
      contractDialogVisible: false, // 合同选择对话框
      contractList: [], // 合同列表
      contractLoading: false, // 合同列表加载状态
      selectedContract: null, // 选中的合同
      contractAmount: null, // 合同金额（用于校验）
      contractSearchForm: { // 合同查询表单
        contractNo: '',
        contractType: '',
        contractName: ''
      },
      budgetDetailList: [], // 预算项目列表（用于预算项目tab）
      budgetItemDialogVisible: false, // 预算项目选择对话框
      budgetItemTab: 'all', // 预算项目对话框tab（all: 全部, withBalance: 有余额）
      allBudgetItemList: [], // 全部预算项目列表（包含预算总额和剩余可执行金额）
      withBalanceBudgetItemList: [], // 有余额的预算项目列表
      budgetItemListLoading: false, // 预算项目列表加载状态
      selectedBudgetItemsInDialog: [], // 对话框中选中的预算项目
      budgetItemSearchForm: {
        itemName: '',
        itemCode: ''
      },
      allBudgetItems: [], // 所有预算项目
      supplierDialogVisible: false, // 供应商管理对话框
      supplierList: [], // 供应商列表
      supplierLoading: false, // 供应商列表加载状态
      selectedSuppliers: [], // 选中的供应商（多选）
      supplierSearchForm: { // 供应商查询表单
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
      supplierFormTitle: '新增供应商',
      templateConfigOptions: [], // 模板配置选项列表
      rules: {
        'payout.payoutTypeId': [{ required: true, message: '请选择报账类型', trigger: 'change' }],
        'payout.templateConfigId': [{ required: true, message: '请选择模板配置', trigger: 'change' }],
        'payout.applyAmount': [{ required: true, message: '请输入报账金额', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.initApplicantInfo()
    this.loadCodeTypeOptions()
    this.loadBudgetSubjects()
    this.loadData()
  },
  methods: {
    initApplicantInfo() {
      const userInfo = this.$store.state.user.userInfo || {}
      this.applicantInfo = {
        empName: userInfo.empName || userInfo.emp_name || userInfo.realName || userInfo.name || '',
        deptName: userInfo.deptName || userInfo.dept_name || '',
        empPhone: userInfo.empPhone || userInfo.emp_phone || userInfo.phone || userInfo.mobile || ''
      }
    },
    async loadCodeTypeOptions() {
      // 表单中的报账类型从PAYOUT_TYPE获取
      this.payoutTypeOptions = await getCodeTypeOptions('PAYOUT_TYPE')
      // 查询条件中的报账类型从PAYOUT_TYPE获取
      this.payoutTypeSearchOptions = await getCodeTypeOptions('PAYOUT_TYPE')
      this.applyStatusOptions = await getCodeTypeOptions('APPLY_STATUS')
      this.contractTypeOptions = await getCodeTypeOptions('CONTRACT_TYPE')
      // 发票类型从sys_code表获取（code_type为INVOICE_TYPE）
      this.invoiceTypeOptions = await getCodeTypeOptions('INVOICE_TYPE')
      // 支付方式从sys_code表获取（code_type为PAYMENT_METHOD）
      this.paymentMethodOptions = await getCodeTypeOptions('PAYMENT_METHOD')
    },
    // 加载模板配置列表
     loadTemplateConfigs() {
      getTemplateConfigByBusinessTypeOnly('PAYOUT_TYPE').then(response => {
        if (response.code === 200 && response.data) {
          this.templateConfigOptions = response.data.filter(config => config.isActive === 1)
          this.templateConfigMap = {}
          this.templateConfigOptions.forEach(config => {
            this.templateConfigMap[config.configId] = config
          })
        }
      }).catch(error => {
        console.error('加载模板配置失败', error)
      })
    },
    async loadBudgetSubjects() {
      try {
        const response = await getBudgetSubjects()
        if (response.code === 200) {
          this.budgetSubjects = response.data || []
        }
      } catch (error) {
        console.error('加载预算主体失败', error)
      }
    },
    async handleSubjectChange(subjectId) {
      this.budgetItemId = null
      this.form.payout.budgetId = null
      this.budgetItems = []
      this.budgets = []
      
      if (subjectId) {
        try {
          const response = await getBudgetItems()
          if (response.code === 200) {
            this.budgetItems = response.data || []
          }
        } catch (error) {
          console.error('加载预算项目失败', error)
        }
      }
    },
    async handleItemChange(itemId) {
      this.form.payout.budgetId = null
      this.budgets = []
      
      if (this.budgetSubjectId && itemId) {
        try {
          const response = await getBudgetsBySubjectAndItem(this.budgetSubjectId, itemId)
          if (response.code === 200) {
            this.budgets = response.data || []
          }
        } catch (error) {
          console.error('加载预算失败', error)
        }
      }
    },
    getPayoutTypeName(codeValue) {
      const option = this.payoutTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : codeValue
    },
    async handlePayoutTypeChange(payoutTypeId) {
      // 根据报账类型加载模板设置信息
      this.templateConfigInfo = ''
      if (payoutTypeId) {
        try {
          const configResponse = await getTemplateConfigByBusinessType('PAYOUT_TYPE', payoutTypeId)
          if (configResponse.code === 200 && configResponse.data) {
            const config = configResponse.data
            const info = []
            if (config.printTemplateName) {
              info.push(`打印模板: ${config.printTemplateName}`)
            }
            if (config.processDefinitionName) {
              info.push(`流程模板: ${config.processDefinitionName}`)
            }
            if (info.length > 0) {
              this.templateConfigInfo = info.join(' | ')
            } else {
              this.templateConfigInfo = '未配置打印模板和流程模板，请在模板设置中配置'
            }
          } else {
            this.templateConfigInfo = '未找到模板设置，请在模板设置中配置'
          }
        } catch (error) {
          console.error('获取模板设置失败:', error)
        }
      }
    },
    getInvoiceTypeName(codeValue) {
      const option = this.invoiceTypeOptions.find(item => item.value === codeValue)
      return option ? option.label : (codeValue || '-')
    },
    getPaymentMethodName(codeValue) {
      const option = this.paymentMethodOptions.find(item => item.value === codeValue)
      return option ? option.label : (codeValue || '-')
    },
    loadData() {
      this.loading = true
      const empId = this.$store.state.user.userInfo.empId || 1
      getMyPayoutsPage(empId, this.pagination.page, this.pagination.size).then(response => {
        if (response.code === 200 && response.data) {
          // 只显示报账单，同时过滤掉null/undefined值
          let records = (response.data.records || []).filter(item => item && item.billType === 'PAYOUT')
          
          // 根据查询条件过滤
          if (this.searchForm.payoutBillcode) {
            records = records.filter(item => 
              item && item.payoutBillcode && item.payoutBillcode.includes(this.searchForm.payoutBillcode)
            )
          }
          if (this.searchForm.payoutTypeId) {
            records = records.filter(item => item && item.payoutTypeId === this.searchForm.payoutTypeId)
          }
          if (this.searchForm.status) {
            records = records.filter(item => item && item.status === this.searchForm.status)
          }
          if (this.searchForm.applyDateRange && this.searchForm.applyDateRange.length === 2) {
            const startDate = this.searchForm.applyDateRange[0]
            const endDate = this.searchForm.applyDateRange[1]
            records = records.filter(item => {
              if (!item) return false
              const applyDate = item.applyDate || item.createTime
              if (!applyDate) return false
              const dateStr = this.formatDateOnly(applyDate)
              return dateStr >= startDate && dateStr <= endDate
            })
          }
          
          this.tableData = records
          this.pagination.total = records.length > 0 ? records.length : (response.data.total || 0)
          // 加载每个报账的当前审批人
          this.tableData.forEach(row => {
            if (row.processInstanceId || row.payoutBillcode) {
              this.loadCurrentApprover(row)
            }
          })
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
        payoutBillcode: '',
        payoutTypeId: '',
        status: '',
        applyDateRange: null
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
    async handleAdd() {
      this.dialogTitle = '新增报账'
      this.isEdit = false
      this.activeTab = 'basic'
      // 生成主附件ID（时间戳），用于附件关联和文件夹命名
      const mainAttachId = Date.now().toString()
      this.fileList = []
      this.budgetSubjectId = null
      this.budgetItemId = null
      // 初始化申请人信息
      this.initApplicantInfo()
      // 初始化申请时间
      const now = new Date()
      const applyDate = this.formatDateForPicker(now)
      
      // 获取用户信息
      const userInfo = this.$store.state.user.userInfo || {}
      
      // 尝试从多个来源获取empId
      let empId = userInfo.empId || userInfo.emp_id
      
      // 如果用户信息中没有empId，尝试通过API获取
      if (!empId && (userInfo.userId || userInfo.id)) {
        try {
          const userId = userInfo.userId || userInfo.id
          const response = await getUserById(userId)
          if (response.code === 200 && response.data) {
            empId = response.data.empId || response.data.emp_id
            // 更新store中的用户信息
            if (empId) {
              this.$store.commit('user/SET_USER_INFO', {
                ...userInfo,
                empId: empId
              })
            }
          }
        } catch (error) {
          console.error('获取用户信息失败', error)
        }
      }
      
      // 如果仍然没有empId，尝试从account获取（通过getUserByAccount）
      if (!empId && userInfo.account) {
        try {
          const response = await getUserByAccount(userInfo.account)
          if (response.code === 200 && response.data) {
            empId = response.data.empId || response.data.emp_id
            if (empId) {
              this.$store.commit('user/SET_USER_INFO', {
                ...userInfo,
                empId: empId
              })
            }
          }
        } catch (error) {
          console.error('通过account获取用户信息失败', error)
        }
      }
      
      if (!empId) {
        this.$message.warning('无法获取员工ID，部分功能可能受限')
        empId = 1 // 使用默认值，避免保存失败
      }
      
      this.form = {
        payout: {
          payoutId: null,
          billType: 'PAYOUT',
          payoutTypeId: '',
          applyAmount: 0,
          applyReason: '',
          budgetId: null,
          budgetItemId: null,
          empId: empId,
          empCode: userInfo.empCode || userInfo.emp_code || userInfo.account || '',
          empName: userInfo.empName || userInfo.emp_name || userInfo.realName || userInfo.name || this.applicantInfo.empName || '',
          deptId: userInfo.deptId || userInfo.dept_id || null,
          sourceApplyNo: null, // 来源申请单号
          contractNo: null, // 关联合同编号
          applyDate: applyDate, // 申请时间
          templateConfigId: null, // 模板配置ID
          isNurse: 0, // 是否护士：0-否，1-是
          mainAttachId: mainAttachId, // 主附件ID（时间戳）
          status: 'DRAFT'
        },
        details: [],
        invoices: [],
        payments: []
      }
      this.selectedSourceApply = null
      this.selectedContract = null // 重置选中的合同
      this.budgetDetailList = []
      this.uploadData.businessId = null
      this.uploadedAttachmentIds = [] // 重置上传附件ID列表
      this.contractAmount = null // 重置合同金额
      // 确保模板配置已加载
      if (this.templateConfigOptions.length === 0) {
        this.loadTemplateConfigs()
      }
      this.dialogVisible = true
    },
    formatDateForPicker(date) {
      if (!date) return ''
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    handleEdit(row) {
      if (row.status !== 'DRAFT' && row.status !== 'WITHDRAWN' && row.status !== 'REJECTED') {
        this.$message.warning('只有草稿、撤回或拒绝状态的报账单才能编辑')
        return
      }
      this.dialogTitle = '编辑报账'
      this.isEdit = true
      this.activeTab = 'basic'
      this.loadPayoutDetail(row.payoutId)
      this.dialogVisible = true
    },
    async loadPayoutDetail(payoutId) {
      try {
        const response = await getPayoutDetail(payoutId)
        if (response.code === 200) {
          this.form.payout = response.data.payout || {}
          this.form.details = response.data.details || []
          this.form.invoices = response.data.invoices || []
          this.form.payments = response.data.payments || []
          // 确保isNurse存在
          if (this.form.payout.isNurse === undefined || this.form.payout.isNurse === null) {
            this.form.payout.isNurse = 0
          }
          // 确保mainAttachId存在（编辑时如果没有则生成，确保附件可以正常关联）
          if (!this.form.payout.mainAttachId) {
            // 如果是编辑模式但没有mainAttachId，生成一个新的
            this.form.payout.mainAttachId = Date.now().toString()
            console.warn('编辑模式下缺少mainAttachId，已生成新的:', this.form.payout.mainAttachId)
          }
          // 始终使用mainAttachId作为businessId
          this.uploadData.businessId = this.form.payout.mainAttachId
          this.uploadedAttachmentIds = [] // 重置上传附件ID列表
          // 初始化申请人信息
          this.initApplicantInfo()
          
          // 加载预算项目列表（如果有来源申请单号，从来源申请单加载；否则从报账明细加载）
          if (this.form.payout.sourceApplyNo) {
            // 从来源申请单加载预算明细
            try {
              const budgetResponse = await getBudgetDetailsByBusinessNo(this.form.payout.sourceApplyNo)
              if (budgetResponse.code === 200 && budgetResponse.data) {
                const budgetDetails = budgetResponse.data || []
                this.budgetDetailList = []
                
                // 为每个明细处理剩余可执行金额
                for (const detail of budgetDetails) {
                  let remainingAmount = detail.remainingAmount != null ? Number(detail.remainingAmount) : null
                  
                  // 如果后端没有返回 remainingAmount，则通过 API 获取
                  if (remainingAmount === null && detail.subjectId && detail.itemId) {
                    try {
                      const budgetInfoResponse = await getBudgetsBySubjectAndItem(detail.subjectId, detail.itemId)
                      if (budgetInfoResponse.code === 200 && budgetInfoResponse.data && budgetInfoResponse.data.length > 0) {
                        const budget = budgetInfoResponse.data[0]
                        if (budget.budgetId) {
                          const remainingResponse = await getBudgetRemainingAmount(budget.budgetId)
                          if (remainingResponse.code === 200) {
                            remainingAmount = remainingResponse.data || 0
                          }
                        }
                      }
                    } catch (error) {
                      // 静默失败，使用默认值
                    }
                  }
                  
                  this.budgetDetailList.push({
                  itemId: detail.itemId,
                  itemCode: detail.itemCode || '',
                  itemName: detail.itemName || '',
                  budgetYear: detail.budgetYear,
                  subjectId: detail.subjectId,
                  subjectName: detail.subjectName || '',
                  subjectCode: detail.subjectCode || '',
                  budgetId: detail.budgetId,
                  amount: detail.amount || 0,
                  maxAmount: detail.amount || 0,
                    remainingAmount: remainingAmount != null ? remainingAmount : 0,
                  fromSourceApply: true,
                  budgetDetailId: detail.id
                  })
                }
              }
            } catch (error) {
              console.error('加载预算明细失败', error)
            }
          } else {
            // 如果没有来源申请单号，从报账单的业务单号加载预算明细
            if (this.form.payout.payoutBillcode) {
              try {
                const budgetResponse = await getBudgetDetailsByBusinessNo(this.form.payout.payoutBillcode)
                if (budgetResponse.code === 200 && budgetResponse.data) {
                  const budgetDetails = budgetResponse.data || []
                  this.budgetDetailList = []
                  
                  // 为每个明细处理剩余可执行金额
                  for (const detail of budgetDetails) {
                    let remainingAmount = detail.remainingAmount != null ? Number(detail.remainingAmount) : null
                    
                    // 如果后端没有返回 remainingAmount，则通过 API 获取
                    if (remainingAmount === null && detail.subjectId && detail.itemId) {
                      try {
                        const budgetInfoResponse = await getBudgetsBySubjectAndItem(detail.subjectId, detail.itemId)
                        if (budgetInfoResponse.code === 200 && budgetInfoResponse.data && budgetInfoResponse.data.length > 0) {
                          const budget = budgetInfoResponse.data[0]
                          if (budget.budgetId) {
                            const remainingResponse = await getBudgetRemainingAmount(budget.budgetId)
                            if (remainingResponse.code === 200) {
                              remainingAmount = remainingResponse.data || 0
                            }
                          }
                        }
                      } catch (error) {
                        // 静默失败，使用默认值
                      }
                    }
                    
                    this.budgetDetailList.push({
                      itemId: detail.itemId,
                      itemCode: detail.itemCode || '',
                      itemName: detail.itemName || '',
                      budgetYear: detail.budgetYear,
                      subjectId: detail.subjectId,
                      subjectName: detail.subjectName || '',
                      subjectCode: detail.subjectCode || '',
                      budgetId: detail.budgetId,
                      amount: detail.amount || 0,
                      maxAmount: detail.amount || 0,
                      remainingAmount: remainingAmount != null ? remainingAmount : 0,
                      fromSourceApply: false,
                      budgetDetailId: detail.id
                    })
                  }
                } else {
                  // 如果加载失败，尝试从返回的 details 字段加载（兼容旧数据）
                  this.budgetDetailList = (response.data.details || []).map(detail => ({
                    itemId: detail.itemId,
                    itemCode: detail.itemCode || '',
                    itemName: detail.itemName || '',
                    subjectId: detail.subjectId,
                    subjectCode: detail.subjectCode || '',
                    subjectName: detail.subjectName || '',
                    budgetId: detail.budgetId,
                    budgetYear: detail.budgetYear,
                    amount: detail.amount || 0,
                    maxAmount: detail.amount || 0,
                    remainingAmount: detail.remainingAmount || 0,
                    fromSourceApply: false,
                    budgetDetailId: detail.id
                  }))
                }
              } catch (error) {
                console.error('加载预算明细失败', error)
                // 如果加载失败，尝试从返回的 details 字段加载（兼容旧数据）
                this.budgetDetailList = (response.data.details || []).map(detail => ({
                  itemId: detail.itemId,
                  itemCode: detail.itemCode || '',
                  itemName: detail.itemName || '',
                  subjectId: detail.subjectId,
                  subjectCode: detail.subjectCode || '',
                  subjectName: detail.subjectName || '',
                  budgetId: detail.budgetId,
                  budgetYear: detail.budgetYear,
                  amount: detail.amount || 0,
                  maxAmount: detail.amount || 0,
                  remainingAmount: detail.remainingAmount || 0,
                  fromSourceApply: false,
                  budgetDetailId: detail.id
                }))
              }
            } else {
              // 如果连业务单号都没有，清空预算明细列表
              this.budgetDetailList = []
            }
          }
          
          this.loadAttachments(this.form.payout.mainAttachId)
          
          // 如果有关联的合同编号，加载合同信息以获取合同金额
          if (this.form.payout.contractNo) {
            try {
              const contractResponse = await getContractByNo(this.form.payout.contractNo)
              if (contractResponse.code === 200 && contractResponse.data) {
                this.contractAmount = contractResponse.data.contractAmount
              }
            } catch (error) {
              console.error('加载合同信息失败', error)
              // 如果加载失败，不影响编辑
            }
          }
        }
      } catch (error) {
        this.$message.error('加载详情失败')
      }
    },
    async loadAttachments(mainAttachId) {
      if (!mainAttachId) return
      try {
        const response = await getAttachmentsByBusinessId(mainAttachId)
        if (response.code === 200) {
          this.fileList = (response.data || []).map(item => ({
            name: item.fileName,
            url: this.getFileUrl(item.filePath),
            uid: item.attachmentId,
            response: { data: item.attachmentId },
            filePath: item.filePath,
            fileName: item.fileName,
            isUploaded: true
          }))
        }
      } catch (error) {
        console.error('加载附件失败', error)
      }
    },
    handleAddDetail() {
      this.form.details.push({
        itemName: '',
        itemType: '',
        amount: 0,
        remark: ''
      })
    },
    handleRemoveDetail(index) {
      this.form.details.splice(index, 1)
    },
    handleAddInvoice() {
      this.form.invoices.push({
        invoiceCode: '',
        invoiceNumber: '',
        invoiceDate: null,
        invoiceAmount: 0,
        invoiceType: null, // 从下拉选择
        taxAmount: 0,
        remark: '',
        attachmentId: null
      })
    },
    handleRemoveInvoice(index) {
      this.form.invoices.splice(index, 1)
    },
    handleInvoiceUploadSuccess(response, index) {
      console.log('发票附件上传成功响应:', response)
      // el-upload 的响应可能是包装后的格式
      let res = response
      if (typeof response === 'string') {
        try {
          res = JSON.parse(response)
        } catch (e) {
          console.error('解析响应JSON失败:', e)
          this.$message.error('上传失败：响应格式错误')
          return
        }
      }
      if (response && response.data && typeof response.data === 'object') {
        res = response.data
      }
      
      if (res && res.code === 200 && res.data) {
        const attachmentId = res.data
        this.form.invoices[index].attachmentId = attachmentId
        // 如果是新增，记录发票附件ID，取消时删除
        if (!this.form.payout.payoutId) {
          this.uploadedInvoiceAttachmentIds.push(attachmentId)
        }
        this.$message.success('上传成功')
      } else {
        this.$message.error((res && res.message) || '上传失败')
      }
    },
    handleAddPayment() {
      this.form.payments.push({
        paymentAmount: 0,
        paymentObject: '',
        paymentMethod: null, // 从下拉选择
        bankName: '',
        bankAccount: '',
        accountName: '',
        paymentDate: null,
        remark: ''
      })
    },
    // 支付金额变化处理
    handlePaymentAmountChange() {
      // 重新计算支付总金额，用于后续校验
    },
    handleRemovePayment(index) {
      this.form.payments.splice(index, 1)
    },
    async handleViewDetail(row) {
      this.selectedPayoutId = row.payoutId
          this.detailVisible = true
    },
    handleDetailEdit(row) {
          this.detailVisible = false
      this.handleEdit(row)
    },
    handleDetailSubmitted() {
            this.loadData()
    },
    handleDetailWithdrawn() {
            this.loadData()
    },
    handleDetailDeleted() {
      this.loadData()
    },
    handleDetailPrint() {
      // 打印功能由统一组件处理，这里只需要刷新数据
      // this.loadData()
    },
    getCurrentApprover(row) {
      if (row.status === 'REJECTED') {
        return '-'
      }
      if (this.currentApproverMap[row.payoutId]) {
        return this.currentApproverMap[row.payoutId]
      }
      if (row.processInstanceId || row.payoutBillcode) {
        this.loadCurrentApprover(row)
        return '-'
      }
      return '-'
    },
    async loadCurrentApprover(row) {
      try {
        const taskKey = row.payoutBillcode
        if (taskKey) {
          const response = await getProcessTaskByTaskKey(taskKey)
          if (response.code === 200 && response.data && response.data.length > 0) {
            const allTasks = response.data
            const pendingTasks = allTasks.filter(task => task.taskStatus === 'PENDING')
            let currentTask = null
            
            if (pendingTasks.length > 0) {
              pendingTasks.sort((a, b) => {
                const aOrder = (a.printOrder != null ? a.printOrder : 999999)
                const bOrder = (b.printOrder != null ? b.printOrder : 999999)
                if (aOrder === bOrder) {
                  const aIsAddsign = a.isAddsignTask === 1
                  const bIsAddsign = b.isAddsignTask === 1
                  if (aIsAddsign && !bIsAddsign) {
                    return -1
                  }
                  if (!aIsAddsign && bIsAddsign) {
                    return 1
                  }
                }
                return aOrder - bOrder
              })
              currentTask = pendingTasks[0]
            }
            
            if (currentTask) {
              const approverName = currentTask.approverList || currentTask.assigneeUserName
              if (approverName && approverName !== '-') {
                this.$set(this.currentApproverMap, row.payoutId, approverName)
                return
              }
            }
          }
        }
        
        this.$set(this.currentApproverMap, row.payoutId, '-')
        } catch (error) {
        console.error('加载审批人失败:', error)
        this.$set(this.currentApproverMap, row.payoutId, '-')
      }
    },
    handleViewProcess(row) {
      this.currentProcessRow = row
              this.processVisible = true
    },
    async handleApprovalConfirm(opinion) {
      if (!this.currentApply || !this.currentApply.payoutId) {
        this.$message.error('报账信息不存在')
        this.approvalConfirmVisible = false
        return
      }
      
      try {
        const userInfo = this.$store.state.user.userInfo || {}
        const userId = userInfo.id || userInfo.userId || ''
        const response = await approvePayout(this.currentApply.payoutId, userId, opinion, null)
        if (response.code === 200) {
          this.$message.success('审批成功')
          this.approvalConfirmVisible = false
          this.loadData()
        } else {
          this.$message.error(response.message || '审批失败')
        }
      } catch (error) {
        this.$message.error('审批失败：' + (error.message || '未知错误'))
      }
    },
    // 保存草稿
    async handleSaveDraft() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          // 如果关联了合同，先检查合同是否已被关联
          if (this.form.payout.contractNo) {
            try {
              const checkResponse = await checkContractNo(this.form.payout.contractNo, this.isEdit ? this.form.payout.payoutId : null)
              if (checkResponse.code === 200 && checkResponse.data === true) {
                this.$message.error('该合同编号已被其他报账单关联，不能重复关联')
                return
              }
            } catch (error) {
              console.error('检查合同编号失败', error)
              this.$message.error('检查合同编号失败：' + (error.message || '未知错误'))
              return
            }
          }
          
          this.savePayoutData('DRAFT')
        }
      })
    },
    // 保存并提交
    async handleSaveAndSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          // 如果关联了合同，先检查合同是否已被关联
          if (this.form.payout.contractNo) {
            try {
              const checkResponse = await checkContractNo(this.form.payout.contractNo, this.isEdit ? this.form.payout.payoutId : null)
              if (checkResponse.code === 200 && checkResponse.data === true) {
                this.$message.error('该合同编号已被其他报账单关联，不能重复关联')
                return
              }
            } catch (error) {
              console.error('检查合同编号失败', error)
              this.$message.error('检查合同编号失败：' + (error.message || '未知错误'))
              return
            }
          }
          
          // 金额校验：预算总金额=申请金额=支付总金额，如果关联了合同，还要校验合同金额
          if (!this.validateAmounts()) {
            return
          }
          
          // 先保存，再提交
          this.savePayoutData('DRAFT', true)
        }
      })
    },
    // 保存报账数据（通用方法）
    async savePayoutData(status, submitAfterSave = false) {
      try {
        // 确保mainAttachId有值
        if (!this.form.payout.mainAttachId) {
          this.form.payout.mainAttachId = Date.now().toString()
        }
        console.log('保存报账数据 - mainAttachId:', this.form.payout.mainAttachId, 'isEdit:', this.isEdit)
        
        // 先上传所有未上传的附件
        const uploadedIds = await this.uploadAllFiles()
        this.uploadedAttachmentIds = this.uploadedAttachmentIds.concat(uploadedIds)
        
        // 确保empCode、empName、deptId等字段已设置
        const userInfo = this.$store.state.user.userInfo || {}
        const payoutData = {
          ...this.form.payout,
          status: status,
          // 确保来源申请单号和关联合同编号被正确传递（即使是 null 或空字符串）
          sourceApplyNo: this.form.payout.sourceApplyNo || null,
          contractNo: this.form.payout.contractNo || null
        }
        
        // 如果这些字段为空，从userInfo中获取
        if (!payoutData.empCode) {
          payoutData.empCode = userInfo.empCode || userInfo.emp_code || userInfo.account || ''
        }
        if (!payoutData.empName) {
          payoutData.empName = userInfo.empName || userInfo.emp_name || userInfo.realName || userInfo.name || this.applicantInfo.empName || ''
        }
        if (!payoutData.deptId) {
          payoutData.deptId = userInfo.deptId || userInfo.dept_id || null
        }
        // 如果empId为空，尝试获取
        if (!payoutData.empId) {
          let empId = userInfo.empId || userInfo.emp_id
          // 如果还是没有，尝试通过API获取
          if (!empId && (userInfo.userId || userInfo.id)) {
            try {
              const response = await getUserById(userInfo.userId || userInfo.id)
              if (response.code === 200 && response.data) {
                empId = response.data.empId || response.data.emp_id
              }
            } catch (error) {
              console.error('获取用户信息失败', error)
            }
          }
          // 尝试通过account获取
          if (!empId && userInfo.account) {
            try {
              const response = await getUserByAccount(userInfo.account)
              if (response.code === 200 && response.data) {
                empId = response.data.empId || response.data.emp_id
              }
            } catch (error) {
              console.error('通过account获取用户信息失败', error)
            }
          }
          if (empId) {
            payoutData.empId = empId
          }
        }
        
        // 准备保存数据，符合后端 CtrlPayoutDTO 的结构
        // 处理支付清单，确保所有字段都被正确传递（包括 null 值）
        const payments = (this.form.payments || []).map(payment => ({
          paymentId: payment.paymentId || null,
          paymentAmount: payment.paymentAmount || 0,
          paymentObject: payment.paymentObject || '',
          paymentMethod: payment.paymentMethod || null, // 确保 paymentMethod 字段被传递，即使是 null
          bankName: payment.bankName || '',
          bankAccount: payment.bankAccount || '',
          accountName: payment.accountName || '',
          paymentDate: payment.paymentDate || null,
          remark: payment.remark || '',
          supplierId: payment.supplierId || null
        }))
        
        const saveData = {
          payout: payoutData,
          invoices: this.form.invoices || [],
          payments: payments,
          budgetDetails: this.budgetDetailList.map(detail => ({
            itemId: detail.itemId,
            itemCode: detail.itemCode,
            itemName: detail.itemName,
            subjectId: detail.subjectId,
            subjectCode: detail.subjectCode,
            subjectName: detail.subjectName,
            budgetId: detail.budgetId,
            budgetYear: detail.budgetYear,
            amount: detail.amount,
            maxAmount: detail.maxAmount,
            remainingAmount: detail.remainingAmount,
            budgetDetailId: detail.budgetDetailId
          }))
        }
        
        const api = this.isEdit ? updatePayoutFull : savePayoutFull
        const response = await api(saveData)
        if (response.code === 200) {
          if (response.data && response.data.payoutBillcode) {
            this.form.payout.payoutBillcode = response.data.payoutBillcode
          }
          // 清空上传附件ID列表（已保存，不再需要删除）
          this.uploadedAttachmentIds = []
          
          // 如果保存后需要提交
            if (submitAfterSave) {
              const payoutId = response.data?.payoutId || response.data?.id || response.data?.payout?.payoutId || this.form.payout.payoutId
              if (payoutId) {
                // 提交报账
                try {
                  const submitResponse = await submitPayout(payoutId)
                  if (submitResponse.code === 200) {
                    this.$message.success('保存并提交成功')
                    this.dialogVisible = false
                    this.pagination.page = 1
                    this.loadData()
                  } else {
                    this.$message.error(submitResponse.message || '提交失败')
                  }
                } catch (error) {
                  this.$message.error('提交失败：' + (error.message || '未知错误'))
                }
              } else {
                this.$message.error('保存成功，但无法获取报账ID，无法提交')
              }
          } else {
            this.$message.success(this.isEdit ? '更新成功' : '保存成功')
            this.dialogVisible = false
            this.pagination.page = 1
            this.loadData()
          }
        } else {
          this.$message.error(response.message || '操作失败')
        }
      } catch (error) {
        this.$message.error('保存失败：' + (error.message || '未知错误'))
      }
    },
    // 上传单个文件
    // 通过templateConfigId获取业务类型（从sys_template_config表的business_type字段）
    async getBusinessTypeByTemplateConfigId(templateConfigId) {
      if (!templateConfigId) {
        return null
      }
      try {
        // 通过templateConfigId获取TemplateConfig
        const templateConfigResponse = await getTemplateConfigById(templateConfigId)
        if (templateConfigResponse.code !== 200 || !templateConfigResponse.data) {
          console.warn('获取模板配置失败，templateConfigId:', templateConfigId)
          return null
        }
        const templateConfig = templateConfigResponse.data
        
        // 直接从TemplateConfig获取businessType字段
        const businessType = templateConfig.businessType
        if (!businessType) {
          console.warn('模板配置中没有businessType字段，templateConfigId:', templateConfigId)
          return null
        }
        console.log('通过templateConfigId获取业务类型成功，templateConfigId:', templateConfigId, 'businessType:', businessType)
        return businessType
      } catch (error) {
        console.error('获取业务类型失败:', error)
        return null
      }
    },
    async uploadSingleFile(file) {
      try {
        // 使用mainAttachId作为businessId（时间戳），这样在保存之前就能确定business_id
        // 完全按照预算申请的方式处理
        const businessId = this.form.payout.mainAttachId || (this.isEdit ? (this.form.payout.payoutBillcode || null) : null)
        
        // 通过templateConfigId获取业务类型
        let businessType = 'PAYOUT' // 默认值
        if (this.form.payout.templateConfigId) {
          const dynamicBusinessType = await this.getBusinessTypeByTemplateConfigId(this.form.payout.templateConfigId)
          if (dynamicBusinessType) {
            businessType = dynamicBusinessType
          }
        }
        console.log('上传文件 - businessType:', businessType, 'templateConfigId:', this.form.payout.templateConfigId)
        
        const fileToUpload = file.raw || file
        if (!fileToUpload) {
          throw new Error('文件对象不存在')
        }
        
        const response = await uploadFile(fileToUpload, businessType, businessId)
        
        if (response.code === 200 && response.data) {
          return response.data
        } else {
          throw new Error(response.message || '上传失败')
        }
      } catch (error) {
        this.$message.error('上传文件失败：' + (error.message || '未知错误'))
        throw error
      }
    },
    // 上传所有文件
    async uploadAllFiles() {
      const filesToUpload = this.fileList.filter(f => {
        const notUploaded = f.isUploaded === false || f.isUploaded === undefined
        const hasRaw = !!(f.raw || (f.status === 'ready' && f))
        return notUploaded && hasRaw
      })
      if (filesToUpload.length === 0) {
        return []
      }
      
      const uploadedIds = []
      for (let i = 0; i < filesToUpload.length; i++) {
        const fileItem = filesToUpload[i]
        try {
          const fileToUpload = fileItem.raw || fileItem
          if (!fileToUpload) {
            throw new Error('文件对象不存在')
          }
          const attachmentId = await this.uploadSingleFile({ ...fileItem, raw: fileToUpload })
          uploadedIds.push(attachmentId)
          
          const index = this.fileList.findIndex(f => f.uid === fileItem.uid)
          if (index > -1) {
            this.$set(this.fileList[index], 'isUploaded', true)
            this.$set(this.fileList[index], 'attachmentId', attachmentId)
            this.$set(this.fileList[index], 'response', { code: 200, data: attachmentId })
          }
        } catch (error) {
          throw new Error(`文件 ${fileItem.name} 上传失败: ${error.message}`)
        }
      }
      
      return uploadedIds
    },
    // 文件变化处理
    handleFileChange(file, fileList) {
      if (file.status === 'ready') {
        if (!file.raw && file.rawFile) {
          file.raw = file.rawFile
        }
        if (!file.raw) {
          file.raw = file
        }
        if (!file.hasOwnProperty('isUploaded')) {
          this.$set(file, 'isUploaded', false)
        }
        const fileToRead = file.raw || file
        if (fileToRead.type && fileToRead.type.startsWith('image/')) {
          const reader = new FileReader()
          reader.onload = (e) => {
            const index = fileList.findIndex(f => f.uid === file.uid)
            if (index > -1) {
              fileList[index].url = e.target.result
            }
          }
          reader.readAsDataURL(fileToRead)
        }
        this.fileList = fileList
        this.$message.success(`文件 "${file.name}" 已添加到列表，保存时将上传到服务器`)
      } else if (file.status === 'removed') {
        this.fileList = fileList
      }
    },
    // 预览文件
    async handlePreviewFile(file) {
      await this.handlePreviewAttachment(file)
    },
    // 获取文件URL
    getFileUrl(filePath) {
      if (!filePath) return ''
      if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
        return filePath
      }
      if (filePath.includes('/uploads/')) {
        const parts = filePath.split('/uploads/')
        return '/api/uploads/' + parts[parts.length - 1]
      } else if (filePath.includes('\\uploads\\')) {
        const parts = filePath.split('\\uploads\\')
        return '/api/uploads/' + parts[parts.length - 1].replace(/\\/g, '/')
      } else if (filePath.includes('attachments/')) {
        const parts = filePath.split('attachments/')
        return '/api/uploads/attachments/' + parts[parts.length - 1].replace(/\\/g, '/')
      } else if (filePath.includes('attachments\\')) {
        const parts = filePath.split('attachments\\')
        return '/api/uploads/attachments/' + parts[parts.length - 1].replace(/\\/g, '/')
      } else {
        return '/api/uploads/' + filePath.replace(/\\/g, '/')
      }
    },
    beforeUpload(file) {
      const maxSize = 50 * 1024 * 1024
      if (file.size > maxSize) {
        this.$message.error('文件大小不能超过50MB')
        return false
      }
      const exists = this.fileList.some(f => {
        const fRaw = f.raw || f
        const fileRaw = file.raw || file
        return (f.name === file.name && f.size === file.size) || 
               (fRaw && fileRaw && fRaw.uid === fileRaw.uid)
      })
      if (exists) {
        this.$message.warning('文件已存在')
        return false
      }
      return false // 阻止自动上传，由保存时统一上传
    },
    handleUploadSuccess(response, file) {
      console.log('附件上传成功响应:', response)
      // el-upload 的响应可能是包装后的格式
      let res = response
      if (typeof response === 'string') {
        try {
          res = JSON.parse(response)
        } catch (e) {
          console.error('解析响应JSON失败:', e)
          this.$message.error('上传失败：响应格式错误')
          return
        }
      }
      if (response && response.data) {
        res = response.data
      }
      
      if (res && res.code === 200 && res.data) {
        this.$message.success('上传成功')
        // 如果是新增，记录附件ID，取消时删除
        if (!this.form.payout.payoutId) {
          this.uploadedAttachmentIds.push(res.data)
        }
      } else {
        this.$message.error((res && res.message) || '上传失败')
      }
    },
    async handleRemove(file, fileList) {
      this.fileList = fileList
      let attachmentId = null
      if (file.response && file.response.data) {
        attachmentId = file.response.data
      } else if (file.attachmentId) {
        attachmentId = file.attachmentId
      } else if (file.uid && typeof file.uid === 'number') {
        attachmentId = file.uid
      }
      
      if (attachmentId) {
        const index = this.uploadedAttachmentIds.indexOf(attachmentId)
        if (index > -1) {
          this.uploadedAttachmentIds.splice(index, 1)
        }
        if (file.isUploaded) {
          try {
            await deleteAttachment(attachmentId)
          } catch (error) {
          }
        }
      }
    },
    async handleDialogCancel() {
      // 只有在新增模式下，且表单未保存（没有payoutBillcode）时，才删除已上传的附件
      // 编辑模式下或已保存的表单，附件应该保留（因为文件是在保存表单时才上传的）
      // 完全按照预算申请的方式处理
      if (!this.isEdit && !this.form.payout.payoutBillcode) {
        // 如果上传了附件但没有保存表单，需要删除这些附件
        // 只删除已上传的附件，未上传的附件只需要清空fileList即可
        const uploadedFiles = this.fileList.filter(f => f.isUploaded && f.attachmentId)
        if (uploadedFiles.length > 0) {
          for (const file of uploadedFiles) {
            try {
              await deleteAttachment(file.attachmentId)
            } catch (error) {
              // 静默处理删除失败
              // 继续删除其他附件，不中断
            }
          }
        }
      }
      
      // 重置文件列表和附件ID列表
      this.fileList = []
      this.uploadedAttachmentIds = []
      
      // 关闭对话框
      this.dialogVisible = false
    },
    handlePreviewAttachment(attachment) {
      console.log('预览附件:', attachment)
      // 构建文件URL
      let fileUrl = attachment.filePath || attachment.url
      if (!fileUrl) {
        this.$message.error('附件路径不存在')
        return
      }
      
      console.log('原始文件路径:', fileUrl)
      
      // 如果filePath是相对路径，需要转换为完整URL
      if (!fileUrl.startsWith('http://') && !fileUrl.startsWith('https://')) {
        // filePath可能是完整路径，需要转换为访问URL
        // 例如：F:/data/uploads/attachments/PAYOUT/xxx.pdf
        // 需要转换为：/api/uploads/attachments/PAYOUT/xxx.pdf
        if (fileUrl.includes('/uploads/')) {
          const parts = fileUrl.split('/uploads/')
          fileUrl = '/api/uploads/' + parts[parts.length - 1]
        } else if (fileUrl.includes('\\uploads\\')) {
          const parts = fileUrl.split('\\uploads\\')
          fileUrl = '/api/uploads/' + parts[parts.length - 1].replace(/\\/g, '/')
        } else if (fileUrl.includes('attachments/')) {
          // 如果路径包含attachments/，说明可能是相对路径
          fileUrl = '/api/uploads/' + fileUrl.replace(/\\/g, '/')
        } else {
          // 其他情况，尝试作为相对路径处理
          fileUrl = '/api/uploads/' + fileUrl.replace(/\\/g, '/')
        }
      }
      
      console.log('转换后的文件URL:', fileUrl)
      
      // 判断文件类型，如果是图片，使用图片预览
      const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp']
      const fileName = attachment.fileName || attachment.name || ''
      const fileExt = fileName ? fileName.substring(fileName.lastIndexOf('.')).toLowerCase() : ''
      
      console.log('文件扩展名:', fileExt)
      
      if (imageExtensions.includes(fileExt)) {
        // 图片预览
        this.$alert(`<img src="${fileUrl}" style="max-width: 100%; max-height: 500px; display: block; margin: 0 auto;" onerror="this.style.display='none'; this.nextElementSibling.style.display='block';" /><p style="display:none; text-align:center; color:red;">图片加载失败，URL: ${fileUrl}</p>`, '图片预览', {
          dangerouslyUseHTMLString: true,
          showConfirmButton: true,
          confirmButtonText: '关闭',
          customClass: 'image-preview-dialog',
          width: '600px'
        })
      } else {
        // 其他文件类型，在新窗口打开
        console.log('打开文件:', fileUrl)
        window.open(fileUrl, '_blank')
      }
    },
    handleDownloadAttachment(attachment) {
      // 构建文件URL
      let fileUrl = attachment.filePath || attachment.url
      if (!fileUrl) {
        this.$message.error('附件路径不存在')
        return
      }
      
      // 如果filePath是相对路径，需要转换为完整URL
      if (!fileUrl.startsWith('http://') && !fileUrl.startsWith('https://')) {
        // filePath可能是完整路径，需要转换为访问URL
        if (fileUrl.includes('/uploads/')) {
          const parts = fileUrl.split('/uploads/')
          fileUrl = '/api/uploads/' + parts[parts.length - 1]
        } else if (fileUrl.includes('\\uploads\\')) {
          const parts = fileUrl.split('\\uploads\\')
          fileUrl = '/api/uploads/' + parts[parts.length - 1].replace(/\\/g, '/')
        } else {
          fileUrl = '/api/uploads/' + fileUrl.replace(/\\/g, '/')
        }
      }
      window.open(fileUrl, '_blank')
    },
    formatFileSize(size) {
      if (!size) return '-'
      if (size < 1024) return size + ' B'
      if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
      return (size / (1024 * 1024)).toFixed(2) + ' MB'
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
    },
    formatDateOnly(date) {
      if (!date) return '-'
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    formatDateTime(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },
    // 打开合同选择对话框
    async handleOpenContractDialog() {
      this.contractDialogVisible = true
      this.contractLoading = true
      this.selectedContract = null
      // 如果搜索表单为空，重置它
      if (!this.contractSearchForm.contractNo && !this.contractSearchForm.contractType && !this.contractSearchForm.contractName) {
        this.contractSearchForm = {
          contractNo: '',
          contractType: '',
          contractName: ''
        }
      }
      
      try {
        // 获取所有已审批的合同
        const response = await getAllContractsPage(1, 1000)
        if (response.code === 200 && response.data) {
          // 过滤出状态为APPROVED的合同，并过滤掉null/undefined
          let contracts = (response.data.records || []).filter(item => item && item.status === 'APPROVED')
          
          // 应用搜索条件
          if (this.contractSearchForm.contractNo) {
            contracts = contracts.filter(item => 
              item.contractNo && item.contractNo.includes(this.contractSearchForm.contractNo)
            )
          }
          if (this.contractSearchForm.contractType) {
            contracts = contracts.filter(item => item.contractType === this.contractSearchForm.contractType)
          }
          if (this.contractSearchForm.contractName) {
            contracts = contracts.filter(item => 
              item.contractName && item.contractName.includes(this.contractSearchForm.contractName)
            )
          }
          
          // 如果正在编辑，需要排除当前报账单已关联的合同
          const currentContractNo = this.isEdit ? this.form.payout.contractNo : null
          
          // 检查每个合同是否已被其他报账单关联（需要后端接口支持）
          // 这里先简单处理，显示所有已审批的合同，在后端校验时检查唯一性
          this.contractList = contracts
          
          // 如果有当前关联的合同，需要包含它（编辑时）
          if (currentContractNo) {
            const currentContract = contracts.find(c => c && c.contractNo === currentContractNo)
            if (currentContract && !this.contractList.find(c => c && c.contractNo === currentContractNo)) {
              this.contractList.push(currentContract)
            }
          }
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
      // 重新加载数据并应用搜索条件
      this.handleOpenContractDialog()
    },
    // 合同查询重置
    handleContractSearchReset() {
      this.contractSearchForm = {
        contractNo: '',
        contractType: '',
        contractName: ''
      }
      this.handleOpenContractDialog()
    },
    // 选择合同
    handleSelectContract(row) {
      this.selectedContract = row
    },
    // 确认选择合同
    async handleConfirmContract() {
      if (!this.selectedContract) {
        this.$message.warning('请先选择合同')
        return
      }
      
      // 检查该合同编号是否已被关联
      try {
        const checkResponse = await checkContractNo(this.selectedContract.contractNo, this.isEdit ? this.form.payout.payoutId : null)
        if (checkResponse.code === 200 && checkResponse.data === true) {
          this.$message.error('该合同编号已被其他报账单关联，不能重复关联')
          return
        }
      } catch (error) {
        console.error('检查合同编号失败', error)
        this.$message.error('检查合同编号失败：' + (error.message || '未知错误'))
          return
        }
        
        // 检查报账金额是否与合同金额一致
        if (this.form.payout.applyAmount && this.selectedContract.contractAmount) {
          const payoutAmount = parseFloat(this.form.payout.applyAmount)
          const contractAmount = parseFloat(this.selectedContract.contractAmount)
          
          if (Math.abs(payoutAmount - contractAmount) > 0.01) {
            this.$message.error(`报账金额（¥${payoutAmount.toFixed(2)}）与合同金额（¥${contractAmount.toFixed(2)}）不一致，请核对后重试`)
            return
          }
        }
        
        // 设置合同编号和金额
        this.form.payout.contractNo = this.selectedContract.contractNo
        this.contractAmount = this.selectedContract.contractAmount
        
        // 如果报账金额已设置，自动校验金额是否一致
        if (this.form.payout.applyAmount && this.contractAmount) {
          const payoutAmount = parseFloat(this.form.payout.applyAmount)
          const contractAmount = parseFloat(this.contractAmount)
          if (Math.abs(payoutAmount - contractAmount) > 0.01) {
            this.$message.warning(`当前报账金额（¥${payoutAmount.toFixed(2)}）与合同金额（¥${contractAmount.toFixed(2)}）不一致，保存时会进行校验`)
          }
        }
        
        this.contractDialogVisible = false
        this.$message.success('已选择合同：' + this.selectedContract.contractNo)
    },
    // 打开来源申请单选择对话框
    async handleOpenSourceApplyDialog() {
      this.sourceApplyDialogVisible = true
      this.sourceApplyLoading = true
      this.selectedSourceApply = null
      // 如果搜索表单为空，重置它
      if (!this.sourceApplySearchForm.payoutBillcode && !this.sourceApplySearchForm.payoutTypeId) {
        this.sourceApplySearchForm = {
          payoutBillcode: '',
          payoutTypeId: ''
        }
      }
      
      try {
        // 获取当前用户信息
        const userInfo = this.$store.state.user.userInfo || {}
        const currentUserAccount = userInfo.account || userInfo.empCode || userInfo.emp_code || ''
        
        // 查询本人申请的、状态为已审批的申请单
        const response = await getMyApplyList({
          page: 1,
          size: 1000, // 获取所有符合条件的申请单
          billTypePrefix: 'SQD', // 申请单前缀
          status: 'APPROVED', // 只查询已审批的
          applicantCode: currentUserAccount // 只查询当前用户申请的
        })
        
        if (response.code === 200 && response.data) {
          // 过滤出billType为'APPLY'的记录
          let records = (response.data.records || []).filter(item => 
            item && (item.billType === 'APPLY' || !item.billType) && item.status === 'APPROVED'
          )
          
          // 应用搜索条件
          if (this.sourceApplySearchForm.payoutBillcode) {
            records = records.filter(item => 
              item.payoutBillcode && item.payoutBillcode.includes(this.sourceApplySearchForm.payoutBillcode)
            )
          }
          if (this.sourceApplySearchForm.payoutTypeId) {
            records = records.filter(item => item.payoutTypeId === this.sourceApplySearchForm.payoutTypeId)
          }
          
          this.sourceApplyList = records
        } else {
          this.sourceApplyList = []
        }
      } catch (error) {
        console.error('加载来源申请单列表失败', error)
        this.$message.error('加载申请单列表失败：' + (error.message || '未知错误'))
        this.sourceApplyList = []
      } finally {
        this.sourceApplyLoading = false
      }
    },
    // 来源申请单查询
    handleSourceApplySearch() {
      this.handleOpenSourceApplyDialog()
    },
    // 来源申请单查询重置
    handleSourceApplySearchReset() {
      this.sourceApplySearchForm = {
        payoutBillcode: '',
        payoutTypeId: ''
      }
      this.handleOpenSourceApplyDialog()
    },
    // 选择来源申请单
    handleSelectSourceApply(row) {
      this.selectedSourceApply = row
    },
    // 确认选择来源申请单
    async handleConfirmSourceApply() {
      if (!this.selectedSourceApply) {
        this.$message.warning('请先选择申请单')
        return
      }
      
      // 检查该申请单号是否已被关联
      try {
        const checkResponse = await checkSourceApplyNo(this.selectedSourceApply.payoutBillcode, this.isEdit ? this.form.payout.payoutId : null)
        if (checkResponse.code === 200 && checkResponse.data === true) {
          this.$message.error('该申请单号已被其他报账单关联，不能重复关联')
          return
        }
      } catch (error) {
        console.error('检查申请单号失败', error)
        this.$message.error('检查申请单号失败，请重试')
        return
      }
      
      // 设置来源申请单号
      this.form.payout.sourceApplyNo = this.selectedSourceApply.payoutBillcode
      
      // 关闭对话框
      this.sourceApplyDialogVisible = false
      
      // 加载该申请单的预算明细
      try {
        this.$message.info('正在加载申请单的预算项目信息...')
        const response = await getBudgetDetailsByBusinessNo(this.selectedSourceApply.payoutBillcode)
        
        if (response.code === 200 && response.data) {
          // 将预算明细转换为预算项目列表（用于预算项目tab）
          const budgetDetails = response.data || []
          
          // 清空现有的预算项目列表
          this.budgetDetailList = []
          
          // 将预算明细添加到budgetDetailList中，金额可编辑，但不可超出原申请金额
          // 需要异步加载当前的剩余可执行金额
          const budgetItemPromises = budgetDetails.map(async (detail) => {
            let remainingAmount = 0
            
            // 通过subjectId和itemId查找预算信息，获取当前的剩余可执行金额
            if (detail.subjectId && detail.itemId) {
              try {
                const budgetResponse = await getBudgetsBySubjectAndItem(detail.subjectId, detail.itemId)
                if (budgetResponse.code === 200 && budgetResponse.data && budgetResponse.data.length > 0) {
                  const budget = budgetResponse.data[0]
                  
                  // 获取当前的剩余可执行金额
                  if (budget.budgetId) {
                    const remainingResponse = await getBudgetRemainingAmount(budget.budgetId)
                    if (remainingResponse.code === 200) {
                      remainingAmount = remainingResponse.data || 0
                    }
                  }
                }
              } catch (error) {
                console.error('获取剩余金额失败', error)
                // 如果查询失败，使用原始值
                remainingAmount = detail.remainingAmount || 0
              }
            } else {
              remainingAmount = detail.remainingAmount || 0
            }
            
            return {
              itemId: detail.itemId,
              itemCode: detail.itemCode || '',
              itemName: detail.itemName || '',
              subjectId: detail.subjectId,
              subjectCode: detail.subjectCode || '',
              subjectName: detail.subjectName || '',
              budgetId: detail.budgetId,
              budgetYear: detail.budgetYear || '',
              amount: detail.amount || 0, // 可编辑，但最大值为原申请金额
              maxAmount: detail.amount || 0, // 保存原申请金额作为最大值
              remainingAmount: remainingAmount, // 当前剩余可执行金额
              fromSourceApply: true, // 标记来自来源申请单
              budgetDetailId: detail.id // 保存预算明细ID，用于后续关联
            }
          })
          
          // 等待所有剩余金额查询完成
          const budgetItems = await Promise.all(budgetItemPromises)
          this.budgetDetailList = budgetItems
          
          // 计算总金额并更新报账金额
          const totalAmount = this.budgetDetailList.reduce((sum, detail) => sum + (detail.amount || 0), 0)
          this.form.payout.applyAmount = totalAmount
          
          // 切换到预算项目tab
          this.activeTab = 'budget'
          this.$message.success(`已加载 ${budgetDetails.length} 个预算项目，总金额：¥${totalAmount.toFixed(2)}`)
        } else {
          this.$message.warning('该申请单暂无预算明细信息')
        }
      } catch (error) {
        console.error('加载预算明细失败', error)
        this.$message.error('加载预算明细失败：' + (error.message || '未知错误'))
      }
    },
    // 预算项目金额变化处理
    handleBudgetDetailAmountChange() {
      // 重新计算总金额
      const totalAmount = this.budgetDetailList.reduce((sum, detail) => sum + (detail.amount || 0), 0)
      this.form.payout.applyAmount = totalAmount
    },
    // 删除预算项目（仅在没有来源申请单时可用）
    handleRemoveBudgetDetail(index) {
      if (this.form.payout.sourceApplyNo) {
        this.$message.warning('已选择来源申请单，不能删除预算项目')
        return
      }
      this.budgetDetailList.splice(index, 1)
      // 重新计算总金额
      this.handleBudgetDetailAmountChange()
    },
    // 打开预算项目选择对话框（仅在没有来源申请单时可用）
    async handleOpenBudgetItemDialog() {
      if (this.form.payout.sourceApplyNo) {
        this.$message.warning('已选择来源申请单，不能手动添加预算项目')
        return
      }
      
      // 确保 deptId 已设置
      const userInfo = this.$store.state.user.userInfo || {}
      let deptId = userInfo.deptId || userInfo.dept_id || null
      
      // 如果还是没有，尝试通过API获取用户信息
      if (!deptId && userInfo.userId) {
        try {
          const response = await getUserById(userInfo.userId)
          if (response.code === 200 && response.data) {
            deptId = response.data.deptId || response.data.dept_id || null
            // 更新store中的用户信息
            if (deptId) {
              this.$store.commit('user/SET_USER_INFO', {
                ...userInfo,
                deptId: deptId
              })
            }
          }
        } catch (error) {
          console.error('获取用户信息失败', error)
        }
      }
      
      if (!deptId) {
        this.$message.warning('无法获取申请人科室信息，请先完善个人信息')
        return
      }
      
      this.budgetItemDialogVisible = true
      this.budgetItemTab = 'all'
      this.selectedBudgetItemsInDialog = []
      await this.loadBudgetItemList()
      // 清空表格选择状态
      this.$nextTick(() => {
        if (this.$refs.allBudgetItemTable) {
          this.$refs.allBudgetItemTable.clearSelection()
        }
        if (this.$refs.withBalanceBudgetItemTable) {
          this.$refs.withBalanceBudgetItemTable.clearSelection()
        }
      })
    },
    // 加载预算项目列表（包含预算总额和剩余可执行金额）
    async loadBudgetItemList() {
      this.budgetItemListLoading = true
      try {
        this.allBudgetItemList = []
        this.withBalanceBudgetItemList = []
        
        // 从多个地方获取 deptId
        const userInfo = this.$store.state.user.userInfo || {}
        let deptId = userInfo.deptId || userInfo.dept_id || null
        
        if (!deptId) {
          this.$message.warning('无法获取申请人科室信息')
          this.budgetItemListLoading = false
          return
        }
        
        // 确保预算主体和预算项目数据已加载
        if (!this.budgetSubjects || this.budgetSubjects.length === 0) {
          await this.loadBudgetSubjects()
        }
        if (!this.allBudgetItems || this.allBudgetItems.length === 0) {
          await this.loadBudgetItems()
        }
        
        // 检查数据是否加载成功
        if (!this.budgetSubjects || this.budgetSubjects.length === 0) {
          this.$message.warning('预算主体数据未加载，请刷新页面重试')
          this.budgetItemListLoading = false
          return
        }
        if (!this.allBudgetItems || this.allBudgetItems.length === 0) {
          this.$message.warning('预算项目数据未加载，请刷新页面重试')
          this.budgetItemListLoading = false
          return
        }
      
        // 先并行找出包含申请人科室的预算主体（使用缓存避免重复查询）
        const subjectDeptCache = new Map() // 缓存：subjectId -> relatedDepts
        const validSubjectIds = []
        
        // 并行查询所有预算主体的关联科室
        const subjectPromises = this.budgetSubjects.map(async (subject) => {
          try {
            if (!subjectDeptCache.has(subject.subjectId)) {
              const response = await getBudgetSubjectRelatedDepts(subject.subjectId)
              if (response.code === 200) {
                subjectDeptCache.set(subject.subjectId, response.data || [])
              }
            }
            const relatedDepts = subjectDeptCache.get(subject.subjectId) || []
            const hasDept = relatedDepts.some(dept => dept.deptId === deptId || dept.dept_id === deptId)
            if (hasDept) {
              validSubjectIds.push(subject.subjectId)
            }
          } catch (error) {
            console.error(`检查预算主体${subject.subjectId}关联科室失败`, error)
          }
        })
        await Promise.all(subjectPromises)
        
        // 并行过滤出分配给这些预算主体的项目（使用缓存避免重复查询）
        const itemSubjectCache = new Map() // 缓存：itemId -> itemSubjects
        const validItems = []
        
        const itemPromises = this.allBudgetItems.map(async (item) => {
          try {
            if (!itemSubjectCache.has(item.itemId)) {
              const itemSubjectsResponse = await getBudgetItemSubjects(item.itemId)
              if (itemSubjectsResponse.code === 200) {
                itemSubjectCache.set(item.itemId, itemSubjectsResponse.data || [])
              }
            }
            const itemSubjects = itemSubjectCache.get(item.itemId) || []
            const hasValidSubject = itemSubjects.some(s => 
              validSubjectIds.includes(s.subjectId || s.subject_id)
            )
            if (hasValidSubject) {
              validItems.push({ item, itemSubjects })
            }
          } catch (error) {
            console.error(`检查预算项目${item.itemId}关联主体失败`, error)
          }
        })
        await Promise.all(itemPromises)
        
        // 并行为每个项目加载预算信息
        const budgetItemPromises = validItems.map(async ({ item, itemSubjects }) => {
          try {
            // 找出包含申请人科室的预算主体（使用缓存）
            let validSubject = null
            for (const subject of itemSubjects) {
              const subjectId = subject.subjectId || subject.subject_id
              let relatedDepts = subjectDeptCache.get(subjectId)
              if (!relatedDepts) {
                try {
                  const deptResponse = await getBudgetSubjectRelatedDepts(subjectId)
                  if (deptResponse.code === 200) {
                    relatedDepts = deptResponse.data || []
                    subjectDeptCache.set(subjectId, relatedDepts)
                  }
                } catch (error) {
                  console.error(`检查预算主体关联科室失败`, error)
                  continue
                }
              }
              if (relatedDepts) {
                const hasDept = relatedDepts.some(dept => dept.deptId === deptId || dept.dept_id === deptId)
                if (hasDept) {
                  validSubject = subject
                  break
                }
              }
            }
            
            if (!validSubject) return null
            
            // 获取该主体和项目对应的预算
            try {
              const budgetResponse = await getBudgetsBySubjectAndItem(
                validSubject.subjectId || validSubject.subject_id, 
                item.itemId
              )
              if (budgetResponse.code === 200 && budgetResponse.data && budgetResponse.data.length > 0) {
                const budget = budgetResponse.data[0] // 取第一个预算
                
                // 获取预算总额和剩余可执行金额
                let budgetAmount = budget.budgetAmount || 0
                let remainingAmount = 0
                try {
                  const remainingResponse = await getBudgetRemainingAmount(budget.budgetId)
                  if (remainingResponse.code === 200) {
                    remainingAmount = remainingResponse.data || 0
                  }
                } catch (error) {
                  console.error('获取剩余金额失败', error)
                }
                
                return {
                  subjectId: validSubject.subjectId || validSubject.subject_id,
                  subjectCode: validSubject.subjectCode || validSubject.subject_code,
                  subjectName: validSubject.subjectName || validSubject.subject_name,
                  itemId: item.itemId,
                  itemCode: item.itemCode,
                  itemName: item.itemName,
                  budgetId: budget.budgetId,
                  budgetNo: budget.budgetNo,
                  budgetName: budget.budgetName,
                  budgetYear: budget.budgetYear,
                  budgetAmount: budgetAmount,
                  remainingAmount: remainingAmount
                }
              }
            } catch (error) {
              console.error('加载预算失败', error)
            }
          } catch (error) {
            console.error('加载预算项目关联主体失败', error)
          }
          return null
        })
        
        const budgetItems = await Promise.all(budgetItemPromises)
        let validBudgetItems = budgetItems.filter(item => item !== null)
        
        // 应用查询条件
        if (this.budgetItemSearchForm.itemName) {
          validBudgetItems = validBudgetItems.filter(item => 
            item.itemName && item.itemName.includes(this.budgetItemSearchForm.itemName)
          )
        }
        if (this.budgetItemSearchForm.itemCode) {
          validBudgetItems = validBudgetItems.filter(item => 
            item.itemCode && item.itemCode.includes(this.budgetItemSearchForm.itemCode)
          )
        }
        
        this.allBudgetItemList = validBudgetItems
        this.withBalanceBudgetItemList = validBudgetItems.filter(item => item.remainingAmount > 0)
        
        // 如果没有加载到任何数据，提示用户
        if (this.allBudgetItemList.length === 0) {
          this.$message.info('暂无符合条件的预算项目')
        }
      } catch (error) {
        console.error('加载预算项目列表失败', error)
        this.$message.error('加载预算项目列表失败：' + (error.message || '未知错误'))
      } finally {
        this.budgetItemListLoading = false
      }
    },
    // 预算项目查询
    handleBudgetItemSearch() {
      this.loadBudgetItemList()
    },
    // 预算项目查询重置
    handleBudgetItemSearchReset() {
      this.budgetItemSearchForm = {
        itemName: '',
        itemCode: ''
      }
      this.loadBudgetItemList()
    },
    // 预算项目对话框tab切换
    handleBudgetItemTabChange(tab) {
      // tab切换时不需要重新加载，因为数据已经在loadBudgetItemList中准备好了
      // 切换tab时保持选中状态（因为两个tab共享selectedBudgetItemsInDialog）
      this.$nextTick(() => {
        // 根据当前tab设置表格选中状态
        if (tab === 'all' && this.$refs.allBudgetItemTable) {
          this.$refs.allBudgetItemTable.clearSelection()
          this.selectedBudgetItemsInDialog.forEach(selectedItem => {
            const row = this.allBudgetItemList.find(item => 
              item.itemId === selectedItem.itemId && item.subjectId === selectedItem.subjectId
            )
            if (row) {
              this.$refs.allBudgetItemTable.toggleRowSelection(row, true)
            }
          })
        } else if (tab === 'withBalance' && this.$refs.withBalanceBudgetItemTable) {
          this.$refs.withBalanceBudgetItemTable.clearSelection()
          this.selectedBudgetItemsInDialog.forEach(selectedItem => {
            const row = this.withBalanceBudgetItemList.find(item => 
              item.itemId === selectedItem.itemId && item.subjectId === selectedItem.subjectId
            )
            if (row) {
              this.$refs.withBalanceBudgetItemTable.toggleRowSelection(row, true)
            }
          })
        }
      })
    },
    // 预算项目选择变化处理
    handleBudgetItemSelectionChange(selection) {
      // 合并当前选中的项目到selectedBudgetItemsInDialog
      // 先移除当前tab中已取消选中的项目
      const currentList = this.budgetItemTab === 'all' ? this.allBudgetItemList : this.withBalanceBudgetItemList
      const currentItemKeys = currentList.map(item => `${item.itemId}_${item.subjectId}`)
      
      // 移除当前tab中不在selection中的项目
      this.selectedBudgetItemsInDialog = this.selectedBudgetItemsInDialog.filter(item => {
        const key = `${item.itemId}_${item.subjectId}`
        return !currentItemKeys.includes(key)
      })
      
      // 添加新选中的项目
      selection.forEach(row => {
        const key = `${row.itemId}_${row.subjectId}`
        const exists = this.selectedBudgetItemsInDialog.some(item => 
          `${item.itemId}_${item.subjectId}` === key
        )
        if (!exists) {
          this.selectedBudgetItemsInDialog.push(row)
        }
      })
    },
    // 确认选择预算项目（批量添加）
    handleConfirmSelectedBudgetItems() {
      if (this.selectedBudgetItemsInDialog.length === 0) {
        this.$message.warning('请至少选择一个预算项目')
        return
      }
      
      let addedCount = 0
      let skippedCount = 0
      
      this.selectedBudgetItemsInDialog.forEach(row => {
        // 检查是否已经添加过
        const exists = this.budgetDetailList.some(detail => 
          detail.itemId === row.itemId && detail.subjectId === row.subjectId
        )
        
        if (exists) {
          skippedCount++
          return
        }
        
        // 添加到预算明细列表
        this.budgetDetailList.push({
          subjectId: row.subjectId,
          subjectCode: row.subjectCode,
          subjectName: row.subjectName,
          itemId: row.itemId,
          itemCode: row.itemCode,
          itemName: row.itemName,
          budgetId: row.budgetId,
          budgetNo: row.budgetNo,
          budgetName: row.budgetName,
          budgetYear: row.budgetYear,
          budgetAmount: row.budgetAmount,
          remainingAmount: row.remainingAmount,
          amount: 0 // 初始金额为0，用户自己填写
        })
        addedCount++
      })
      
      // 更新申请金额
      this.handleBudgetDetailAmountChange()
      
      // 关闭对话框并清空选择
      this.budgetItemDialogVisible = false
      this.selectedBudgetItemsInDialog = []
      
      if (addedCount > 0) {
        if (skippedCount > 0) {
          this.$message.success(`已添加 ${addedCount} 个预算项目，${skippedCount} 个项目已存在`)
        } else {
          this.$message.success(`已添加 ${addedCount} 个预算项目`)
        }
      } else {
        this.$message.warning('所选项目均已添加，未添加新项目')
      }
    },
    // 加载预算项目
    async loadBudgetItems() {
      try {
        const response = await getBudgetItems()
        if (response.code === 200) {
          this.allBudgetItems = response.data || []
        }
      } catch (error) {
        console.error('加载预算项目失败', error)
      }
    },
    // 打开供应商管理对话框
    async handleOpenSupplierDialog() {
      this.supplierDialogVisible = true
      this.supplierLoading = true
      this.selectedSuppliers = []
      this.supplierSearchForm = {
        supplierCode: '',
        supplierName: ''
      }
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
    // 供应商选择变化
    handleSupplierSelectionChange(selection) {
      this.selectedSuppliers = selection
    },
    // 确认选择供应商
    handleConfirmSelectedSuppliers() {
      if (this.selectedSuppliers.length === 0) {
        this.$message.warning('请至少选择一个供应商')
        return
      }
      
      // 为每个选中的供应商创建支付记录
      this.selectedSuppliers.forEach(supplier => {
        this.form.payments.push({
          paymentAmount: 0,
          paymentObject: supplier.supplierName,
          paymentMethod: null,
          bankName: supplier.bankName || '',
          bankAccount: supplier.bankAccount || '',
          accountName: supplier.accountName || '',
          paymentDate: null,
          remark: '',
          supplierId: supplier.supplierId // 保存供应商ID
        })
      })
      
      this.$message.success(`已添加 ${this.selectedSuppliers.length} 个供应商到支付清单`)
      this.supplierDialogVisible = false
      this.selectedSuppliers = []
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
    // 保存供应商
    async handleSaveSupplier() {
      try {
        const api = this.supplierForm.supplierId ? updateSupplier : saveSupplier
        const response = await api(this.supplierForm)
        
        if (response.code === 200) {
          this.$message.success('保存成功')
          this.supplierFormVisible = false
          // 重新加载供应商列表
          await this.loadSupplierList()
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
            // 重新加载供应商列表
            await this.loadSupplierList()
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
          // 重新加载供应商列表
          await this.loadSupplierList()
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
          await this.loadSupplierList()
        } else {
          this.$message.error(response.message || '启用失败')
        }
      } catch (error) {
        console.error('启用供应商失败', error)
        this.$message.error('启用供应商失败：' + (error.message || '未知错误'))
      }
    },
    // 编辑供应商
    handleEditSupplier(supplier) {
      this.supplierFormTitle = '编辑供应商'
      this.supplierForm = { ...supplier }
      this.supplierFormVisible = true
    },
    // 金额校验：预算总金额=申请金额=支付总金额，如果关联了合同，还要校验合同金额
    validateAmounts() {
      // 计算预算项目总金额
      const budgetTotal = this.budgetDetailList.reduce((sum, detail) => sum + (detail.amount || 0), 0)
      
      // 计算支付清单总金额
      const paymentTotal = this.form.payments.reduce((sum, payment) => sum + (payment.paymentAmount || 0), 0)
      
      // 获取申请金额
      const applyAmount = this.form.payout.applyAmount || 0
      
      // 校验：预算总金额 = 申请金额
      if (Math.abs(budgetTotal - applyAmount) > 0.01) {
        this.$message.error(`预算项目总金额（¥${budgetTotal.toFixed(2)}）与申请金额（¥${applyAmount.toFixed(2)}）不一致`)
        return false
      }
      
      // 校验：支付总金额 = 申请金额
      if (Math.abs(paymentTotal - applyAmount) > 0.01) {
        this.$message.error(`支付清单总金额（¥${paymentTotal.toFixed(2)}）与申请金额（¥${applyAmount.toFixed(2)}）不一致，不允许提交`)
        return false
      }
      
      // 如果关联了合同，校验合同金额
      if (this.form.payout.contractNo && this.contractAmount) {
        const contractAmount = parseFloat(this.contractAmount)
        if (Math.abs(applyAmount - contractAmount) > 0.01) {
          this.$message.error(`报账金额（¥${applyAmount.toFixed(2)}）与合同金额（¥${contractAmount.toFixed(2)}）不一致，请核对后重试`)
          return false
        }
      }
      
      return true
    },
    getStatusText(status) {
      const option = this.applyStatusOptions.find(item => item.value === status)
      return option ? option.label : status
    },
    getStatusType(status) {
      const typeMap = {
        'DRAFT': 'info',
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger',
        'WITHDRAWN': 'info'
      }
      return typeMap[status] || ''
    },
    // 获取合同类型名称
    getContractTypeName(codeValue) {
      // 这里需要从合同类型选项中获取，暂时使用空数组，后续从 codeType 加载
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
    // 清除关联的合同
    handleClearContract() {
      this.form.payout.contractNo = ''
      this.contractAmount = null
      this.selectedContract = null
      this.$message.info('已清除关联的合同')
    },
    // 处理导出命令
    handleExportCommand(command) {
      if (command === 'current') {
        this.handleExportPayout(false)
      } else if (command === 'all') {
        this.handleExportPayout(true)
      }
    },
    // 导出报账数据
    async handleExportPayout(exportAll) {
      this.exportLoading = true
      try {
        let dataToExport = []
        
        if (exportAll) {
          // 导出全部数据，需要重新查询所有数据
          const empId = this.$store.state.user.userInfo.empId || 1
          const response = await getMyPayoutsPage(empId, 1, 10000)
          if (response.code === 200 && response.data) {
            let records = (response.data.records || []).filter(item => item.billType === 'PAYOUT')
            
            // 应用筛选条件
            if (this.searchForm.payoutBillcode) {
              records = records.filter(item => 
                item.payoutBillcode && item.payoutBillcode.includes(this.searchForm.payoutBillcode)
              )
            }
            if (this.searchForm.payoutTypeId) {
              records = records.filter(item => item.payoutTypeId === this.searchForm.payoutTypeId)
            }
            if (this.searchForm.status) {
              records = records.filter(item => item.status === this.searchForm.status)
            }
            if (this.searchForm.applyDateRange && this.searchForm.applyDateRange.length === 2) {
              const startDate = this.searchForm.applyDateRange[0]
              const endDate = this.searchForm.applyDateRange[1]
              records = records.filter(item => {
                const applyDate = item.applyDate || item.createTime
                if (!applyDate) return false
                const dateStr = this.formatDateOnly(applyDate)
                return dateStr >= startDate && dateStr <= endDate
              })
            }
            
            dataToExport = records
          } else {
            this.$message.error(response.message || '获取数据失败')
            return
          }
        } else {
          // 导出当前页数据
          dataToExport = this.tableData
        }
        
        if (dataToExport.length === 0) {
          this.$message.warning('没有数据可导出')
          return
        }
        
        // 构建表头
        const headers = ['报账单号', '申请人', '科室', '报账类型', '报账金额', '状态', '审批人', '报账时间']
        
        // 构建数据列表
        const dataList = dataToExport.map(item => {
          return [
            item.payoutBillcode || '',
            item.empName || '',
            item.deptName || '',
            this.getPayoutTypeName(item.payoutTypeId),
            item.applyAmount ? '¥' + item.applyAmount : '',
            this.getStatusText(item.status),
            this.getCurrentApprover(item),
            this.formatDateOnly(item.applyDate || item.createTime)
          ]
        })
        
        // 调用通用导出接口
        const response = await exportExcel({
          fileName: '我的报账' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页'),
          headers: headers,
          dataList: dataList
        })
        
        // 处理blob响应并下载
        const blob = response.data
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '我的报账' + (exportAll ? '_全部' : '_第' + this.pagination.page + '页') + '.xlsx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败：' + (error.message || '未知错误'))
      } finally {
        this.exportLoading = false
      }
    }
  }
}
</script>

<style scoped>
.my-reimb-payout {
  padding: 20px;
}
</style>
