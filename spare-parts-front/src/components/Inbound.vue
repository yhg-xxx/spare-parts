<template>
  <div>
    <!-- 操作按钮行 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="12">
        <!-- 新增入库按钮 -->
        <el-button
            type="primary"
            :icon="Plus"
            @click="openAddDailyDialog"
        >
          新增入库
        </el-button>
      </el-col>
      <el-col :span="12" class="flex items-center justify-end">
        <div class="flex gap-2">
          <el-input
              v-model="querySparePartName"
              placeholder="请输入备件名称"
              style="width: 200px;"
              clearable
          ></el-input>
          <el-button @click="handleSearch" type="primary" :icon="Search">查询</el-button>
          <el-button @click="clearSearch">清空</el-button>
        </div>
      </el-col>
    </el-row>
    <!-- 新增入库对话框 -->
    <el-dialog v-model="addDialogVisible" title="新建入库">
      <el-form :model="addForm" label-width="120px">
        <!-- 采购单选择 -->
        <el-form-item label="采购单号" prop="orderId" required>

          <el-select
              v-model="addForm.orderId"
              @change="handlePurchaseSelect"
              placeholder="请选择采购单"
              filterable
          >
            <!-- 添加空数据提示 -->
            <el-option
                v-if="purchaseOrders.length === 0"
                disabled
                label="暂无待入库采购单"
                value=""
            />
            <!-- 正确渲染选项 -->
            <el-option
                v-for="order in purchaseOrders"
                :key="order.order_id"
                :label="`${order.spare_part_name} (No.${order.order_id})`"
                :value="order.order_id"
            />
          </el-select>
        </el-form-item>

        <!-- 库位信息 -->
        <el-form-item label="仓库ID" prop="locationId" required>
          <el-input-number
              v-model="addForm.locationId"
              :min="1"
              controls-position="right"
          />
        </el-form-item>

        <!-- 备件信息 -->
        <el-form-item label="备件分类" prop="sparePartCategory" required>
          <el-select v-model="addForm.sparePartCategory">
            <el-option label="机械类" value="机械类" />
            <el-option label="电子类" value="电子类" />
            <el-option label="液压类" value="液压类" />
            <el-option label="电气类" value="电气类" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位" prop="unit" required>
          <el-select v-model="addForm.unit">
            <el-option label="个" value="个" />
            <el-option label="台" value="台" />
            <el-option label="套" value="套" />
            <el-option label="件" value="件" />
          </el-select>
        </el-form-item>

        <!-- 状态信息 -->
        <el-form-item label="备件状态" prop="sparePartStatus" required>
          <el-radio-group v-model="addForm.sparePartStatus">
            <el-radio label="新好件" />
            <el-radio label="修好件" />
            <el-radio label="坏件" />
          </el-radio-group>
        </el-form-item>

        <!-- 财务信息 -->
        <el-form-item label="含税单价" prop="unitPrice" required>
          <el-input-number
              v-model="addForm.unitPrice"
              :precision="2"
              :step="0.1"
              :min="0"
              controls-position="right"
          >
            <template #prefix>¥</template>
          </el-input-number>
        </el-form-item>

        <el-form-item label="税率" prop="taxRate" required>
          <el-input-number
              v-model="addForm.taxRate"
              :precision="3"
              :step="0.01"
              :min="0"
              :max="1"
              controls-position="right"
          >
            <template #suffix>%</template>
          </el-input-number>
        </el-form-item>
        <!-- 在保修截止日期前添加生产厂家字段 -->
        <el-form-item label="生产厂家" prop="manufacturer" required>
          <el-input
              v-model="addForm.manufacturer"
              placeholder="请输入生产厂家名称"
              clearable
          />
        </el-form-item>

        <!-- 其他信息 -->
        <el-form-item label="保修截止" prop="warrantyUntil">
          <el-date-picker
              v-model="addForm.warrantyUntil"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择日期"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdd">提交</el-button>
      </template>
    </el-dialog>
    <!-- 入库列表 -->
    <el-table
        :data="dailyList"
        stripe
        style="width: 100%"
        @row-click="handleRowClick"
    >
      <el-table-column prop="inboundRecord.inboundId" label="入库ID" />
      <el-table-column prop="purchaseOrder.order_id" label="采购订单ID" />
      <el-table-column prop="inboundRecord.locationId" label="仓库ID" />
      <el-table-column prop="purchaseOrder.applicant_id" label="申请人ID" />
      <el-table-column prop="purchaseOrder.spare_part_name" label="备件名称" />
      <el-table-column prop="purchaseOrder.spare_part_model" label="备件型号" />
      <el-table-column prop="inboundRecord.sn" label="备件SN" />
      <el-table-column prop="inboundRecord.warrantyUntil" label="保修截止">
        <template #default="{ row }">
          {{ formatDate(row.inboundRecord.warrantyUntil) }}
        </template>
      </el-table-column>
      <el-table-column prop="inboundRecord.manufacturer" label="生产厂家" />
      <el-table-column prop="inboundRecord.createdAt" label="入库时间">
        <template #default="{ row }">
          {{ formatDate(row.inboundRecord.createdAt) }}
        </template>
      </el-table-column>
    </el-table>
    <!-- 在el-table下方添加 -->
    <el-dialog
        v-model="detailDialogVisible"
        title="入库详情"
        width="70%"
        v-loading="loadingDetail"
    >
      <!-- 备件信息 -->
      <div class="section-title">备件信息</div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="入库ID">{{ detailData.inboundRecord?.inboundId }}</el-descriptions-item>
        <el-descriptions-item label="仓库ID">{{ detailData.inboundRecord?.locationId }}</el-descriptions-item>
        <el-descriptions-item label="备件分类">{{ detailData.inboundRecord?.sparePartCategory }}</el-descriptions-item>
        <el-descriptions-item label="备件状态">
          <el-tag :type="getStatusTag(detailData.inboundRecord?.sparePartStatus)">
            {{ detailData.inboundRecord?.sparePartStatus }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="SN码" :span="2">{{ detailData.inboundRecord?.sn }}</el-descriptions-item>
        <el-descriptions-item label="单位">{{ detailData.inboundRecord?.unit }}</el-descriptions-item>
        <el-descriptions-item label="生产厂家">{{ detailData.inboundRecord?.manufacturer }}</el-descriptions-item>
        <el-descriptions-item label="保修截止">{{ formatDate(detailData.inboundRecord?.warrantyUntil) }}</el-descriptions-item>
        <el-descriptions-item label="入库时间">{{ formatDate(detailData.inboundRecord?.createdAt) }}</el-descriptions-item>

        <!-- 采购信息 -->
        <el-descriptions-item label="采购单号" :span="2">{{ detailData.purchaseOrder?.order_id }}</el-descriptions-item>
        <el-descriptions-item label="申请人ID">{{ detailData.purchaseOrder?.applicant_id }}</el-descriptions-item>
        <el-descriptions-item label="需求车站">{{ detailData.purchaseOrder?.station }}</el-descriptions-item>
        <el-descriptions-item label="所属工区">{{ detailData.purchaseOrder?.workshop }}</el-descriptions-item>
        <el-descriptions-item label="备件名称">{{ detailData.purchaseOrder?.spare_part_name }}</el-descriptions-item>
        <el-descriptions-item label="备件型号">{{ detailData.purchaseOrder?.spare_part_model }}</el-descriptions-item>
        <el-descriptions-item label="采购数量">{{ detailData.purchaseOrder?.number }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">{{ detailData.purchaseOrder?.status }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(detailData.purchaseOrder?.created_at) }}</el-descriptions-item>
      </el-descriptions>

      <!-- 财务信息 -->
      <div class="section-title mt-6">财务信息</div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="含税单价">{{ formatCurrency(detailData.inboundRecord?.unitPrice) }}</el-descriptions-item>
        <el-descriptions-item label="税率">{{ formatPercentage(detailData.inboundRecord?.taxRate) }}</el-descriptions-item>
        <el-descriptions-item label="不含税单价">{{ formatCurrency(detailData.inboundRecord?.taxFreeUnitPrice) }}</el-descriptions-item>
        <el-descriptions-item label="单件税额">{{ formatCurrency(detailData.inboundRecord?.taxAmount) }}</el-descriptions-item>
        <el-descriptions-item label="不含税总额">{{ formatCurrency(detailData.inboundRecord?.taxFreeTotal) }}</el-descriptions-item>
        <el-descriptions-item label="总税额">{{ formatCurrency(detailData.inboundRecord?.totalTax) }}</el-descriptions-item>
        <el-descriptions-item label="含税总额" :span="2">
          {{ formatCurrency(
            (detailData.inboundRecord?.taxFreeTotal || 0) +
            (detailData.inboundRecord?.totalTax || 0)
        )
          }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

  </div>

</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from "element-plus";
import axios from "axios";
import router from "@/router.js";
import { Plus, Search } from "@element-plus/icons-vue";

/* ---------------------------- 数据声明 ---------------------------- */
// 用户相关
const currentUser = ref({})

// 列表数据
const dailyList = ref([]);
const querySparePartName = ref('');

// 详情对话框相关
const detailDialogVisible = ref(false);
const detailData = ref({});
const loadingDetail = ref(false);

// 新增对话框相关
const addDialogVisible = ref(false);
const purchaseOrders = ref([]);
const selectedPurchase = ref({});
const snPreview = ref([]);
const addForm = reactive({
  orderId: null,
  locationId: null,
  sparePartCategory: '',
  sparePartStatus: '新好件',
  sparePartType: '正常件',
  unitPrice: 0,
  taxRate: 0.13,
  unit: '',
  manufacturer: '',
  warrantyUntil: ''
});

/* ---------------------------- 生命周期 ---------------------------- */
onMounted(async () => {
  const user = JSON.parse(sessionStorage.getItem('user'))
  if (!user) {
    ElMessage.error('请先登录')
    await router.push('/login')
    return
  }
  currentUser.value = user
  await getDailyList()
})

/* ---------------------------- 数据获取 ---------------------------- */
// 获取入库记录列表
const getDailyList = async () => {
  try {
    const params = {};
    if (querySparePartName.value) {
      params.spare_part_name = querySparePartName.value;
    }
    const res = await axios.get("http://localhost:8080/api/inbound-records/with-purchase", {
      params,
      withCredentials: true
    });
    dailyList.value = res.data;
  } catch (error) {
    ElMessage.error('获取订单列表失败: ' + error.message)
  }
};

/* ---------------------------- 界面交互 ---------------------------- */
// 行点击显示详情
const handleRowClick = async (row) => {
  try {
    loadingDetail.value = true;
    const res = await axios.get(
        `http://localhost:8080/api/inbound-records/${row.inboundRecord.inboundId}/with-purchase`
    );
    detailData.value = res.data;
    detailDialogVisible.value = true;
  } catch (error) {
    ElMessage.error('获取详情失败: ' + error.message);
  } finally {
    loadingDetail.value = false;
  }
};

// 打开新增对话框
const openAddDailyDialog = async () => {
  try {
    const res = await axios.get("http://localhost:8080/purchase_order/s");
    purchaseOrders.value = res.data || [];
    addDialogVisible.value = true;
  } catch (error) {
    ElMessage.error("获取采购单失败");
  }
};

/* ---------------------------- 表单处理 ---------------------------- */
// 采购单选择处理
const handlePurchaseSelect = async (orderId) => {
  try {
    const selectedOrder = purchaseOrders.value.find(o => o.order_id === orderId);
    if (!selectedOrder) return;

    const generateSN = (count, orderId) => {
      const dateStr = new Date().toISOString().slice(0,10).replace(/-/g, '');
      return Array.from({length: count}, (_, i) =>
          `SN${dateStr}-PO${orderId}-${(i+1).toString().padStart(3, '0')}`
      );
    };

    const orderQty = parseInt(selectedOrder.number);
    if (isNaN(orderQty) || orderQty < 1) {
      ElMessage.warning('采购单数量无效');
      return;
    }

    snPreview.value = generateSN(orderQty, orderId);
  } catch (error) {
    ElMessage.error('生成SN失败: ' + error.message);
  }
};

// 提交新增表单
const submitAdd = async () => {
  try {
    const requests = snPreview.value.map(sn => ({
      locationId: addForm.locationId,
      sn: sn,
      sparePartCategory: addForm.sparePartCategory,
      sparePartStatus: addForm.sparePartStatus,
      sparePartType: addForm.sparePartType,
      unitPrice: addForm.unitPrice,
      taxRate: addForm.taxRate,
      unit: addForm.unit,
      manufacturer: addForm.manufacturer,
      warrantyUntil: addForm.warrantyUntil
    }));

    const res = await axios.post("/api/inbound-records", {
      orderId: addForm.orderId,
      requests: requests
    });

    ElMessage.success(`创建成功，共${res.data.length}条记录`);
    await getDailyList();
    addDialogVisible.value = false;
  } catch (error) {
    ElMessage.error('提交失败: ' + error.response?.data?.message || error.message);
  }
};

/* ---------------------------- 工具函数 ---------------------------- */
// 查询功能
const handleSearch = () => getDailyList();
const clearSearch = () => {
  querySparePartName.value = '';
  getDailyList();
};

// 格式化函数
const formatCurrency = (value) => value ? `¥${Number(value).toFixed(2)}` : '-';
const formatPercentage = (value) => value ? `${(value * 100).toFixed(2)}%` : '-';
const formatDate = (timestamp) => timestamp ? new Date(timestamp).toLocaleString() : '-';

// 状态样式
const getStatusTag = (status) => {
  const map = {
    '新好件': 'success',
    '修好件': 'warning',
    '坏件': 'danger'
  }
  return map[status] || 'info';
};
</script>
<style scoped>
.dialog-footer {
  text-align: right;
}
/* 添加描述列表样式 */
:deep(.el-descriptions) {
  margin-top: 20px;
}
:deep(.el-descriptions__label) {
  width: 120px;
  text-align: right;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #606266;
  margin: 20px 0 10px;
  padding-left: 8px;
  border-left: 4px solid #409eff;
}

.mt-6 {
  margin-top: 24px;
}

:deep(.el-descriptions__content) {
  font-family: monospace;
}
.sn-preview {
  max-height: 150px;
  overflow-y: auto;
  padding: 8px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}
.mr-2 {
  margin-right: 8px;
}
</style>
