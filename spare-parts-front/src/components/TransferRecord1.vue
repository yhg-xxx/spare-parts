<template>
  <div>
    <!-- 批量操作按钮 -->
    <div style="margin-bottom: 20px;">
      <el-button
          type="success"
          :disabled="selectedTransfers.length === 0"
          @click="handleBatchApprove"
      >
        一键通过
      </el-button>
      <el-button
          type="danger"
          :disabled="selectedTransfers.length === 0"
          @click="handleBatchReject"
      >
        一键驳回
      </el-button>
    </div>

    <!-- 调拨记录列表 -->
    <el-table
        :data="transferList"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" :selectable="isSelectable" />
      <el-table-column prop="transferId" label="调拨记录ID" />
      <el-table-column prop="fromLocationName" label="原仓库" />
      <el-table-column prop="toLocationName" label="目标仓库" />
      <el-table-column prop="partName" label="备件名称" />
      <el-table-column prop="quantity" label="数量" />
      <el-table-column prop="transferReason" label="调拨事由" />
      <el-table-column prop="applicantId" label="申请人ID" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button
              v-if="row.status === '待审核'"
              size="small"
              type="success"
              @click="handleApprove(row.transferId)"
          >
            通过
          </el-button>
          <el-button
              v-if="row.status === '待审核'"
              size="small"
              type="danger"
              @click="handleReject(row.transferId)"
          >
            驳回
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {ElMessage, ElMessageBox} from "element-plus";
import axios from "axios";
import router from "@/router.js";

const transferList = ref([]); // 调拨记录数据列表
const selectedTransfers = ref([]); // 选中的调拨记录

onMounted(async () => {
  const user = JSON.parse(sessionStorage.getItem('user'))
  if (!user) {
    ElMessage.error('请先登录')
    await router.push('/')
    return
  }
  await getTransferList()
})

// 获取调拨记录列表
const getTransferList = async () => {
  try {
    const res = await axios.get("http://localhost:8080/api/transfer", {
      withCredentials: true
    });
    transferList.value = res.data.list || res.data;
  } catch (error) {
    ElMessage.error('获取调拨记录失败: ' + error.message)
  }
};

// 处理表格选择变化
const handleSelectionChange = (selection) => {
  selectedTransfers.value = selection;
};

// 判断记录是否可选（只有待审核的记录可选）
const isSelectable = (row) => {
  return row.status === '待审核';
};

// 处理批量批准操作
const handleBatchApprove = () => {
  ElMessageBox.confirm(
      `确定要通过选中的${selectedTransfers.value.length}条调拨申请吗？`,
      '批量操作确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    batchApproveTransfers();
  }).catch(() => {
    // 用户取消操作
  });
};

// 处理批量驳回操作
const handleBatchReject = () => {
  ElMessageBox.confirm(
      `确定要驳回选中的${selectedTransfers.value.length}条调拨申请吗？`,
      '批量操作确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    batchRejectTransfers();
  }).catch(() => {
    // 用户取消操作
  });
};

// 批量批准调拨申请
const batchApproveTransfers = async () => {
  try {
    const promises = selectedTransfers.value.map(transfer =>
        axios.put(`http://localhost:8080/api/transfer/${transfer.transferId}/approve`, {}, {
          withCredentials: true
        })
    );

    await Promise.all(promises);
    ElMessage.success(`成功批准${selectedTransfers.value.length}条调拨申请`);
    selectedTransfers.value = [];
    await getTransferList();
  } catch (error) {
    ElMessage.error('批量操作失败: ' + error.message);
  }
};

// 批量驳回调拨申请
const batchRejectTransfers = async () => {
  try {
    const promises = selectedTransfers.value.map(transfer =>
        axios.put(`http://localhost:8080/api/transfer/${transfer.transferId}/reject`, {}, {
          withCredentials: true
        })
    );

    await Promise.all(promises);
    ElMessage.success(`成功驳回${selectedTransfers.value.length}条调拨申请`);
    selectedTransfers.value = [];
    await getTransferList();
  } catch (error) {
    ElMessage.error('批量操作失败: ' + error.message);
  }
};

// 处理批准操作 - 先弹出确认对话框
const handleApprove = (transferId) => {
  ElMessageBox.confirm(
      '确定要通过这条调拨申请吗？',
      '操作确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    approveTransfer(transferId);
  }).catch(() => {
    // 用户取消操作
  });
};

// 处理驳回操作 - 先弹出确认对话框
const handleReject = (transferId) => {
  ElMessageBox.confirm(
      '确定要驳回这条调拨申请吗？',
      '操作确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    rejectTransfer(transferId);
  }).catch(() => {
    // 用户取消操作
  });
};

// 批准调拨申请
const approveTransfer = async (transferId) => {
  try {
    await axios.put(`http://localhost:8080/api/transfer/${transferId}/approve`, {}, {
      withCredentials: true
    });
    ElMessage.success('调拨申请已批准');
    await getTransferList();
  } catch (error) {
    ElMessage.error('操作失败: ' + error.message);
  }
};

// 驳回调拨申请
const rejectTransfer = async (transferId) => {
  try {
    await axios.put(`http://localhost:8080/api/transfer/${transferId}/reject`, {}, {
      withCredentials: true
    });
    ElMessage.success('调拨申请已驳回');
    await getTransferList();
  } catch (error) {
    ElMessage.error('操作失败: ' + error.message);
  }
};

// 显示数据时格式化时间
const formatDate = (timestamp) => {
  if (!timestamp) return '-';
  return new Date(timestamp).toLocaleString();
};

// 根据状态返回不同的tag类型
const getStatusTagType = (status) => {
  switch (status) {
    case '已通过':
      return 'success';
    case '已驳回':
      return 'danger';
    case '待审核':
      return 'warning';
    default:
      return 'info';
  }
};
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
</style>