<template>
  <div>


    <!-- 领用记录表格 -->
    <el-table :data="usageRequests" stripe style="width: 100%" class="mt-4">
      <el-table-column prop="id" label="领用单号" width="120" />
      <el-table-column prop="partName" label="备件名称" />
      <el-table-column prop="partModel" label="备件型号" />
      <el-table-column prop="number" label="数量" width="100">
        <template #default="{row}">
          <span v-if="row.number">{{ row.number }}</span>
          <span v-else style="color: #999">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="类型">
        <template #default="{row}">
          <el-tag :type="row.type === '维修借用' ? 'warning' : 'success'">
            {{ row.type }}
            <span v-if="row.autoConverted" style="margin-left: 5px;font-size:12px">(系统自动转换)</span>
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="申请说明" width="200" show-overflow-tooltip>
        <template #default="{row}">
          <span v-if="row.description">{{ row.description }}</span>
          <span v-else style="color: #999">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="applicantId" label="申请人ID" />
      <el-table-column prop="status" label="状态">
        <template #default="{row}">
          <el-tag :type="getStatusTagType(row.status)">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间">
        <template #default="{row}">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="出库时间">
        <template #default="{row}">
          {{ getStockoutTime(row.id) ? formatDate(getStockoutTime(row.id)) : '-' }}
        </template>
      </el-table-column>
      <!-- 新增审核操作列 -->
      <el-table-column label="操作" width="180">
        <template #default="{row}">
          <el-button
              v-if="row.status === '待审核'"
              type="success"
              size="small"
              @click="handleApprove(row.id)"
          >
            通过
          </el-button>
          <el-button
              v-if="row.status === '待审核'"
              type="danger"
              size="small"
              @click="handleReject(row.id)"
          >
            驳回
          </el-button>
          <!-- 出库操作 -->
          <el-button
              v-if="row.status === '待出库'"
              type="primary"
              size="small"
              @click="showOutDialog(row)"
          >
            出库
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 出库信息对话框 -->
    <el-dialog
        v-model="outDialogVisible"
        title="出库确认"
        width="70%"
    >
      <div class="out-info-container">
        <!-- 领用单信息 -->
        <div class="apply-info">
          <h4>领用单信息</h4>
          <el-descriptions border>
            <el-descriptions-item label="领用单号">{{ currentApply.id }}</el-descriptions-item>
            <el-descriptions-item label="备件名称">{{ currentApply.partName }}</el-descriptions-item>
            <el-descriptions-item label="备件型号">{{ currentApply.partModel }}</el-descriptions-item>
            <el-descriptions-item label="申请数量">{{ currentApply.number }}</el-descriptions-item>
            <el-descriptions-item label="申请说明">{{ currentApply.description }}</el-descriptions-item>
            <el-descriptions-item label="申请时间">{{ formatDate(currentApply.createTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 备件库存信息 -->
        <div class="spareparts-list mt-4">
          <h4>可用备件列表</h4>
          <el-table
              :data="matchedSpareParts"
              border
              @selection-change="handleSelectionChange"
          >
            <el-table-column
                type="selection"
                width="55"
                :selectable="(row) => isSelectable(row)"/>
            <el-table-column prop="sn" label="SN码" width="150" />
            <el-table-column prop="partName" label="名称" />
            <el-table-column prop="partModel" label="型号" />
            <el-table-column prop="category" label="分类">
              <template #default="{row}">
                {{ formatCategory(row.category) }}
              </template>
            </el-table-column>
            <el-table-column prop="sparePartStatus" label="状态">
              <template #default="{row}">
                {{ formatStatus(row.sparePartStatus) }}
              </template>
            </el-table-column>
            <el-table-column prop="sparePartType" label="类型">
              <template #default="{row}">
                {{ formatType(row.sparePartType) }}
              </template>
            </el-table-column>
            <el-table-column prop="locationName" label="仓库" >
            <template #default="{row}">
              {{ getLocationName(row.locationId) }}
            </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <template #footer>
        <div style="display: flex; align-items: center; gap: 16px">
          <div class="selection-counter">
            已选: {{ selectedSpares.length }}/{{ currentApply.number }}
            <el-text v-if="selectedSpares.length < currentApply.number" type="warning" size="small">
              （还需选择{{ currentApply.number - selectedSpares.length }}个）
            </el-text>
          </div>

          <el-button @click="outDialogVisible = false">取消</el-button>
          <el-button
              type="primary"
              :disabled="selectedSpares.length !== currentApply.number"
              @click="confirmOut"
          >
            确认出库（{{ selectedSpares.length }}）
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import axios from 'axios';
import router from '@/router.js';

const applyDialogVisible = ref(false);
const usageRequests = ref([]);
const currentUser = ref(null);
// 新增响应式数据
const outDialogVisible = ref(false);
const currentApply = ref({});
const allSpareParts = ref([]);
const matchedSpareParts = ref([]);
// 新增warehouseList响应式数据
const warehouseList = ref([]);
const selectedSpares = ref([]); // 新增：选中的备件数组
// 新增响应式变量
const stockoutsList = ref([]);
const usageTypes = [
  { value: '维修申领', label: '维修申领（永久领用）' },
  { value: '维修借用', label: '维修借用（48小时内归还）' }
];

const applyForm = ref({
  type: '',
  partName: '',
  partModel: '',
  description: ''
});

// 初始化加载
onMounted(async () => {
  const user = JSON.parse(sessionStorage.getItem('user'));
  if (!user) {
    ElMessage.error('请先登录');
    await router.push('/');
    return;
  }
  currentUser.value = user;
  await fetchStockoutsList(); // 先加载出库记录
  await fetchUsageRequests(); // 再处理领用记录
  await fetchWarehouseList();
});

// 获取所有领用记录
// 修改后的获取领用记录方法
const fetchUsageRequests = async () => {
  try {
    const res = await axios.get('/api/usage-requests', {
      withCredentials: true
    });


    usageRequests.value = res.data.map(item => {
      // 只在初始状态为维修借用且未转换过的情况下处理
      if (item.type === '维修借用' && item.status === '已出库-维修借用') {
        const stockoutTime = getStockoutTime(item.id);

        if (stockoutTime) {
          const outTime = new Date(stockoutTime);
          const now = new Date();

          // 添加时区安全的时间计算
          const diffHours = Math.floor(
              (now.getTime() - outTime.getTime()) / (1000 * 60 * 60)
          );

          // 调试日志（生产环境可移除）
          console.log(`领用单 ${item.id} 时间差：${diffHours} 小时`);

          if (diffHours > 48) {
            return {
              ...item,
              type: '维修申领',
              status: '已出库-维修申领',
              autoConverted: true,
              // 保留原始出库时间
              _originalOutTime: stockoutTime
            };
          }
        }
      }
      return item;
    });

  } catch (error) {
    ElMessage.error('获取记录失败: ' + error.message);
  }
};




/// 审批通过
const handleApprove = async (id) => {
  try {
    await ElMessageBox.confirm('确定通过该申请？', '确认操作');
    await axios.put(`/api/usage-requests/${id}/approve`, {}, {
      withCredentials: true
    });
    await fetchUsageRequests();
    ElMessage.success('已批准，状态更新为待出库');
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error.message);
  }
};

// 审批驳回
const handleReject = async (id) => {
  try {
    await ElMessageBox.confirm('确定驳回该申请？', '确认操作');
    await axios.put(`/api/usage-requests/${id}/reject`, {}, {
      withCredentials: true
    });
    await fetchUsageRequests();
    ElMessage.success('已驳回申请');
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error.message);
  }
};

// 辅助方法
// 增强日期格式化
const formatDate = (timeString) => {
  try {
    return new Date(timeString).toLocaleString('zh-CN');
  } catch (e) {
    return timeString; // 返回原始字符串
  }
};

const getStatusTagType = (status) => {
  const typeMap = {
    '待审核': 'warning',
    '已拒绝': 'danger',
    '待出库': 'info',
    '已出库-维修借用': 'info',  // 新增状态类型
    '已出库-维修申领': 'success' // 新增状态类型
  };
  return typeMap[status] || '';
};

const openApplyDialog = () => {
  applyForm.value = { sparepartSn: '', type: '', description: '' };
  applyDialogVisible.value = true;
};

const clearApplyForm = () => {
  applyDialogVisible.value = false;
};
// 获取备件库存数据
const fetchSpareParts = async () => {
  try {
    const res = await axios.get('/spare_part/x');
    allSpareParts.value = res.data.filter(item =>
        item.status !== '已出库' &&
        (item.sparePartStatus === '新好件' || item.sparePartStatus === '修好件')

    );
  } catch (error) {
    ElMessage.error('获取备件库存失败');
  }
};
// 显示出库对话框
const showOutDialog = async (row) => {
  selectedSpares.value = [];
  currentApply.value = row;
  await fetchSpareParts();

  // 匹配相同名称和型号的备件（包含新好件和修好件）
  matchedSpareParts.value = allSpareParts.value.filter(item =>
      item.partName === row.partName &&
      item.partModel === row.partModel &&
      (item.sparePartStatus === '新好件' || item.sparePartStatus === '修好件') // 新增修好件
  );

  outDialogVisible.value = true;
};
// 修改确认出库方法
const confirmOut = async () => {
  try {
    const requiredNumber = currentApply.value.number;

    // 验证数量匹配
    if (selectedSpares.value.length !== requiredNumber) {
      ElMessage.warning(`需要选择${requiredNumber}个备件，当前已选${selectedSpares.value.length}个`);
      return;
    }

    // 构造完整出库数据
    const stockouts = selectedSpares.value.map(sp => ({
      requestId: currentApply.value.id,
      partName: sp.partName,
      partModel: sp.partModel,
      sn: sp.sn,
      operatorId: currentApply.value.applicantId, // 当前操作员ID
      locationId: sp.locationId,
      locationName: getLocationName(sp.locationId), // 获取仓库名称
      outTime: new Date().toISOString(), // 当前时间
      status: '已出库'
    }));

    // 调试输出
    console.log('提交的出库数据:', stockouts);

    // 提交批量出库
    await axios.post('/api/stockouts/batch', stockouts, {
      withCredentials: true
    });
    await Promise.all(selectedSpares.value.map(async (sp) => {
      try {
        // 构造更新数据，保留原有字段并设置status
        const updateData = {
          ...sp,
          status: '已出库' // 设置status字段
        };
        await axios.put(`/spare_part/${sp.partId}`, updateData, { withCredentials: true });
      } catch (error) {
        console.error('更新备件状态失败:', error);
      }
    }));
    // 更新关联领用单状态
    await axios.put(`/api/usage-requests/${currentApply.value.id}/complete`, {}, {
      withCredentials: true
    });

    ElMessage.success(`成功出库 ${selectedSpares.value.length} 个备件`);
    outDialogVisible.value = false;

    // 刷新数据
    await Promise.all([
      fetchUsageRequests(),
      fetchStockouts()
    ]);

  } catch (error) {
    ElMessage.error(`出库失败: ${error.response?.data?.message || error.message}`);
    console.error('出库错误详情:', error);
  }
};
// 枚举值格式化方法
const formatCategory = (val) => {
  const map = {
    '机械类': '机械类',
    '电气类': '电气类',
    '液压类': '液压类',
    '电子类': '电子类',
    '其他': '其他'
  };
  return map[val] || val;
};
const formatStatus = (val) => {
  const map = {
    '新好件': '新好件',
    '修好件': '修好件',
    '坏件': '坏件',
    '二级修': '二级修',
    '返厂修': '返厂修',
    '待调拨': '待调拨',
    '已报废': '已报废'
  };
  return map[val] || val;
};

const formatType = (val) => {
  const map = {
    '正常件': '正常件',
    '在保件': '在保件',
    '遗留件': '遗留件'
  };
  return map[val] || val;
};
// 新增获取仓库列表方法
const fetchWarehouseList = async () => {
  try {
    const res = await axios.get('/warehouse/x');
    warehouseList.value = res.data;
  } catch (error) {
    ElMessage.error('获取仓库信息失败');
  }
};
// 新增库位转换方法
const getLocationName = (locationId) => {
  // 将字符串类型的locationId转换为数字
  const targetId = Number(locationId);

  // 调试日志（生产环境可移除）
  console.log('转换后的locationId:', targetId, '原始值:', locationId);

  // 查找匹配的仓库
  const warehouse = warehouseList.value.find(w =>
      w.location_id === targetId // 数字类型严格匹配
  );

  // 返回匹配结果
  return warehouse ? warehouse.locationName : locationId;
};

const isSelectable = (row) => {
  // 已选数量未满时所有都可选
  if (selectedSpares.value.length < currentApply.value.number) {
    return true;
  }
  // 已选数量满时，仅已选中的可取消
  return selectedSpares.value.some(sp => sp.partId === row.partId);
};

// 修改后的handleSelectionChange方法
const handleSelectionChange = (selection) => {
  selectedSpares.value = selection;
};
// 添加获取出库记录方法
const fetchStockouts = async () => {
  try {
    await axios.get('/api/stockouts'); // 根据实际接口调整
  } catch (error) {
    console.error('刷新出库记录失败:', error);
  }
};
// 获取所有出库记录
const fetchStockoutsList = async () => {
  try {
    const res = await axios.get('/api/stockouts?size=1000'); // 获取足够大的分页数据
    stockoutsList.value = res.data.content;
  } catch (error) {
    console.error('获取出库记录失败:', error);
  }
};
// 获取最新出库时间
const getStockoutTime = (requestId) => {
  const matches = stockoutsList.value
      .filter(s => s.requestId === requestId)
      .sort((a, b) => new Date(b.outTime) - new Date(a.outTime));

  return matches[0]?.outTime;
};
</script>

<style scoped>
/* 添加禁用样式 */
:deep(.el-table__row) .is-disabled .el-checkbox__inner {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  cursor: not-allowed;
}
:deep(.el-table__row) .is-disabled .el-checkbox__input.is-disabled + span {
  color: #c0c4cc;
  cursor: not-allowed;
}
.selection-counter {
  flex: 1;
  color: #666;
  font-size: 14px;
  padding: 0 12px;
}

/* 高亮数量不匹配提示 */
:deep(.el-table__row) .el-checkbox.is-disabled .el-checkbox__label {
  color: #f56c6c;
}
.mt-4 {
  margin-top: 1rem;
}.out-info-container {
   max-height: 70vh;
   overflow-y: auto;
 }

.apply-info {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 4px;
}

.mt-4 {
  margin-top: 1rem;
}

h4 {
  margin: 0 0 15px 0;
  color: #606266;
}
/* 添加复选框间距 */
.el-table__selection-column {
  padding: 0 12px;
}

/* 调整操作按钮布局 */
.out-info-container {
  max-height: 60vh; /* 缩短高度适应批量操作 */
}
</style>