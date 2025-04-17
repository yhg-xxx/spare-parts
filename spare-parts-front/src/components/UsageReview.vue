<template>
  <el-table :data="usageRequests" stripe style="width:100%">
    <el-table-column prop="id" label="单号" width="120" />

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
        <el-tag :type="statusTagType(row.status)">
          {{ row.status }}
        </el-tag>
      </template>
    </el-table-column>

    <el-table-column prop="createTime" label="申请时间" :formatter="formatTime" />

    <el-table-column label="操作" width="180">
      <template #default="{row}">
        <el-button-group v-if="row.status === '待审核'">
          <el-button
              type="success"
              size="small"
              @click="handleApprove(row.id)"
          >通过</el-button>

          <el-button
              type="danger"
              size="small"
              @click="handleReject(row.id)"
          >驳回</el-button>
        </el-button-group>

        <span v-else>-</span>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import axios from 'axios';

const usageRequests = ref([]);

onMounted(async () => {
  try {
    const { data } = await axios.get('/api/usage-requests');
    usageRequests.value = data;
  } catch (error) {
    ElMessage.error('加载失败: ' + error.message);
  }
});

const statusTagType = (status) => {
  const map = {
    '待审核': 'warning',
    '已批准': 'success',
    '已拒绝': 'danger',
    '待出库': 'info'
  };
  return map[status] || '';
};

const formatTime = (row) => {
  return new Date(row.createTime).toLocaleString();
};

const handleApprove = async (id) => {
  try {
    await ElMessageBox.confirm('确定批准该申请？', '确认操作');
    await axios.put(`/api/usage-requests/${id}/approve`);
    refreshData();
    ElMessage.success('操作成功');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message);
    }
  }
};

const handleReject = async (id) => {
  try {
    await ElMessageBox.confirm('确定驳回该申请？', '确认操作');
    await axios.put(`/api/usage-requests/${id}/reject`);
    refreshData();
    ElMessage.success('操作成功');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message);
    }
  }
};

const refreshData = async () => {
  const { data } = await axios.get('/api/usage-requests');
  usageRequests.value = data;
};
</script>