<template>
  <div>
    <!-- 申请操作区和记录列表 -->
    <el-button @click="openApplyDialog" type="primary" :icon="Plus">新建领用</el-button>

    <!-- 领用记录表格 -->
    <el-table :data="usageRequests" stripe style="width: 100%" class="mt-4">
      <el-table-column prop="id" label="领用单号" width="120" />
      <el-table-column prop="sparepartSn" label="备件SN" />
      <el-table-column prop="type" label="类型">
        <template #default="{row}">
          <el-tag :type="row.type === '维修借用' ? 'warning' : ''">
            {{ row.type }}
          </el-tag>
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

    </el-table>

    <!-- 新增申请对话框 -->
    <el-dialog v-model="applyDialogVisible" title="备件领用申请" @close="clearApplyForm">
      <el-form :model="applyForm" label-width="100px">
        <el-form-item label="备件SN" required>
          <el-input v-model="applyForm.sparepartSn" placeholder="输入备件序列号"/>
        </el-form-item>
        <el-form-item label="领用类型" required>
          <el-select v-model="applyForm.type" placeholder="请选择">
            <el-option
                v-for="t in usageTypes"
                :key="t.value"
                :label="t.label"
                :value="t.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="申请说明">
          <el-input
              v-model="applyForm.description"
              type="textarea"
              placeholder="填写申请理由（可选）"
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

const applyDialogVisible = ref(false);
const usageRequests = ref([]);
const currentUser = ref(null);
const usageTypes = [
  { value: '维修申领', label: '维修申领（永久领用）' },
  { value: '维修借用', label: '维修借用（48小时内归还）' }
];

const applyForm = ref({
  sparepartSn: '',
  type: '',
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
  await fetchUsageRequests();
});

// 获取领用记录（前端过滤）
const fetchUsageRequests = async () => {
  try {
    const res = await axios.get('/api/usage-requests', {
      withCredentials: true
    });

    // 前端过滤当前用户记录
    usageRequests.value = res.data.filter(item =>
        item.applicantId === currentUser.value?.user_id
    );

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
    '待出库': 'info'
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
</script>

<style scoped>
.mt-4 {
  margin-top: 1rem;
}
</style>
