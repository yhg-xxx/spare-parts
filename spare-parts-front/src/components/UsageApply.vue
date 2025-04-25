<template>
  <div>
    <!-- 申请操作区和记录列表 -->
    <el-button @click="openApplyDialog" type="primary" :icon="Plus">新建领用</el-button>

    <!-- 领用记录表格 -->
    <el-table :data="usageRequests" stripe style="width: 100%" class="mt-4">
      <el-table-column prop="id" label="领用单号" width="120" />
      <el-table-column prop="partName" label="备件名称" />
      <el-table-column prop="partModel" label="备件型号" />
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
          {{ row.outTime ? formatDate(row.outTime) : '-' }}
        </template>
      </el-table-column>

    </el-table>

    <!-- 新增申请对话框 -->
    <el-dialog v-model="applyDialogVisible" title="备件领用申请" @close="clearApplyForm">
      <el-form :model="applyForm" label-width="100px" :rules="formRules">
        <!-- 新增备件名称 -->
        <el-form-item label="备件名称" prop="partName" required>
          <el-input
              v-model="applyForm.partName"
              placeholder="请输入备件标准名称（如：SSD-1TB）"
          />
        </el-form-item>

        <!-- 新增备件型号 -->
        <el-form-item label="备件型号" prop="partModel" required>
          <el-input
              v-model="applyForm.partModel"
              placeholder="请输入完整型号（如：SAMSUNG-860EVO）"
          />
        </el-form-item>

        <!-- 原类型选择 -->
        <el-form-item label="领用类型" prop="type" required>
          <el-select v-model="applyForm.type" placeholder="请选择">
            <el-option
                v-for="t in usageTypes"
                :key="t.value"
                :label="t.label"
                :value="t.value"
            />
          </el-select>
        </el-form-item>

        <!-- 说明字段 -->
        <el-form-item label="申请说明">
          <el-input
              v-model="applyForm.description"
              type="textarea"
              :rows="3"
              placeholder="请详细说明使用场景和用途"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交</el-button>
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
// 新增出库记录响应式变量
const stockouts = ref([]);
const applyDialogVisible = ref(false);
const usageRequests = ref([]);
const currentUser = ref(null);
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
  await fetchStockouts();  // 先获取出库记录
  await fetchUsageRequests();
});

// 修改后的领用记录获取方法
const fetchUsageRequests = async () => {
  try {
    const res = await axios.get('/api/usage-requests', {
      withCredentials: true
    });

    usageRequests.value = res.data
        .filter(item => item.applicantId === currentUser.value?.user_id)
        .map(item => {
          // 关联出库记录
          const stockout = stockouts.value.find(s => s.requestId === item.id);
          const outTime = stockout?.outTime || null;

          // 自动转换逻辑（使用来自Stockout的出库时间）
          if (item.type === '维修借用' && outTime) {
            const outTimeDate = new Date(outTime);
            const now = new Date();
            const diffHours = (now - outTimeDate) / (1000 * 60 * 60);

            if (diffHours > 48) {
              return {
                ...item,
                outTime, // 来自Stockout的出库时间
                type: '维修申领',
                status: '已出库-维修申领',
                autoConverted: true
              };
            }
          }

          return {
            ...item,
            outTime // 合并出库时间到领用记录
          };
        });

  } catch (error) {
    ElMessage.error('获取记录失败: ' + error.message);
  }
};

// 提交申请
const submitApply = async () => {
  try {
    await axios.post('/api/usage-requests', {
      ...applyForm.value,
      applicantId: currentUser.value.user_id
    }, {
      withCredentials: true
    });
    ElMessage.success('申请提交成功');
    applyDialogVisible.value = false;
    await fetchUsageRequests(); // 刷新列表
  } catch (error) {
    ElMessage.error(`提交失败: ${error.response?.data?.message || error.message}`);
  }
};


// 审批操作
const handleApprove = async (id) => {
  try {
    await ElMessageBox.confirm('确定批准该申请？', '确认操作');
    await axios.put(`/api/usage-requests/${id}/approve`, {}, {
      withCredentials: true
    });
    await fetchUsageRequests();
    ElMessage.success('操作成功');
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error.message);
  }
};

const handleReject = async (id) => {
  try {
    await ElMessageBox.confirm('确定驳回该申请？', '确认操作');
    await axios.put(`/api/usage-requests/${id}/reject`, {}, {
      withCredentials: true
    });
    await fetchUsageRequests();
    ElMessage.success('操作成功');
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error.message);
  }
};

// 辅助方法
const formatDate = (timeString) => {
  return new Date(timeString).toLocaleString('zh-CN');
};

const getStatusTagType = (status) => {
  const typeMap = {
    '待审核': 'warning',
    '已批准': 'success',
    '已拒绝': 'danger',
    '待出库': 'info',
    '已出库-维修借用': 'info',
    '已出库-维修申领': 'success'
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
// 新增：获取所有出库记录
const fetchStockouts = async () => {
  try {
    const res = await axios.get('/api/stockouts/all', {
      withCredentials: true
    });
    stockouts.value = res.data;
  } catch (error) {
    ElMessage.error('获取出库记录失败: ' + error.message);
  }
};
</script>

<style scoped>
.mt-4 {
  margin-top: 1rem;
}
</style>