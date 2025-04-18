<template>
  <div>
    <!-- 新增调拨记录按钮 -->
    <el-button @click="openAddTransferDialog" type="primary" :icon="Plus">新增调拨</el-button>
    <el-button @click="openBatchTransferDialog" type="primary" :icon="Plus">批量调拨</el-button>

    <!-- 调拨记录列表 -->
    <el-table :data="transferList" stripe style="width: 100%">
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
    </el-table>

    <!-- 新增调拨记录对话框 -->
    <el-dialog v-model="addTransferDialogVisible" title="新增调拨记录" @close="clearAddTransferForm">
      <el-form :model="addTransferForm" ref="addTransferFormRef" label-width="100px">
        <el-form-item label="原仓库" prop="fromLocationName" required>
          <el-input v-model="addTransferForm.fromLocationName" placeholder="请输入原仓库"></el-input>
        </el-form-item>
        <el-form-item label="目标仓库" prop="toLocationName" required>
          <el-input v-model="addTransferForm.toLocationName" placeholder="请输入目标仓库"></el-input>
        </el-form-item>
        <el-form-item label="备件名称" prop="partName" required>
          <el-input v-model="addTransferForm.partName" placeholder="请输入备件名称"></el-input>
        </el-form-item>
        <el-form-item label="数量" prop="quantity" required>
          <el-input-number v-model="addTransferForm.quantity" :min="1" placeholder="请输入数量"></el-input-number>
        </el-form-item>
        <el-form-item label="调拨事由" prop="transferReason" required>
          <el-input v-model="addTransferForm.transferReason" type="textarea" placeholder="请输入调拨事由"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addTransferDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddTransfer">确定</el-button>
      </div>
    </el-dialog>

    <!-- 批量调拨对话框 -->
    <el-dialog v-model="batchTransferDialogVisible" title="批量调拨" @close="clearBatchTransferForm">
      <el-form :model="batchTransferForm" ref="batchTransferFormRef" label-width="100px">
        <el-form-item label="原仓库" prop="fromLocationName" required>
          <el-input v-model="batchTransferForm.fromLocationName" placeholder="请输入原仓库"></el-input>
        </el-form-item>
        <el-form-item label="目标仓库" prop="toLocationName" required>
          <el-input v-model="batchTransferForm.toLocationName" placeholder="请输入目标仓库"></el-input>
        </el-form-item>
        <el-form-item label="调拨事由" prop="transferReason" required>
          <el-input v-model="batchTransferForm.transferReason" type="textarea" placeholder="请输入调拨事由"></el-input>
        </el-form-item>
        <el-form-item label="备件列表" required>
          <el-button type="primary" @click="addPartItem">添加备件</el-button>
          <el-table :data="batchTransferForm.parts" style="width: 100%; margin-top: 10px;">
            <el-table-column prop="partName" label="备件名称">
              <template #default="{ row, $index }">
                <el-input v-model="row.partName" placeholder="备件名称"></el-input>
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量">
              <template #default="{ row, $index }">
                <el-input-number v-model="row.quantity" :min="1"></el-input-number>
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default="{ row, $index }">
                <el-button type="danger" size="small" @click="removePartItem($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="batchTransferDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBatchTransfer">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from "element-plus";
import axios from "axios";
import router from "@/router.js";
import { Plus } from "@element-plus/icons-vue";

const currentUser = ref({});
const transferList = ref([]); // 调拨记录数据列表
const addTransferDialogVisible = ref(false); // 新增调拨记录对话框
const batchTransferDialogVisible = ref(false); // 批量调拨对话框

onMounted(async () => {
  const user = JSON.parse(sessionStorage.getItem('user'));
  if (!user) {
    ElMessage.error('请先登录');
    await router.push('/');
    return;
  }
  currentUser.value = user;
  await getTransferList();
});

// 新增调拨记录表单
const addTransferForm = ref({
  fromLocationName: '',
  toLocationName: '',
  partName: '',
  quantity: 1,
  transferReason: '',
  status: '待审核', // 默认状态
});

// 批量调拨表单
const batchTransferForm = ref({
  fromLocationName: '',
  toLocationName: '',
  transferReason: '',
  parts: [
    { partName: '', quantity: 1 }
  ]
});

// 打开新增调拨记录对话框
const openAddTransferDialog = () => {
  addTransferDialogVisible.value = true;
};

// 打开批量调拨对话框
const openBatchTransferDialog = () => {
  batchTransferDialogVisible.value = true;
};

// 清空新增调拨记录表单
const clearAddTransferForm = () => {
  addTransferForm.value = {
    fromLocationName: '',
    toLocationName: '',
    partName: '',
    quantity: 1,
    transferReason: '',
    status: '待审核',
  };
};

// 清空批量调拨表单
const clearBatchTransferForm = () => {
  batchTransferForm.value = {
    fromLocationName: '',
    toLocationName: '',
    transferReason: '',
    parts: [
      { partName: '', quantity: 1 }
    ]
  };
};

// 添加备件项
const addPartItem = () => {
  batchTransferForm.value.parts.push({ partName: '', quantity: 1 });
};

// 移除备件项
const removePartItem = (index) => {
  if (batchTransferForm.value.parts.length > 1) {
    batchTransferForm.value.parts.splice(index, 1);
  } else {
    ElMessage.warning('至少需要一项备件');
  }
};

// 获取调拨记录列表
const getTransferList = async () => {
  try {
    const res = await axios.get("http://localhost:8080/api/transfer", {
      withCredentials: true
    });
    transferList.value = res.data.list || res.data;
  } catch (error) {
    ElMessage.error('获取调拨记录失败: ' + error.message);
  }
};

// 提交新增调拨记录
const submitAddTransfer = async () => {
  try {
    const formData = {
      ...addTransferForm.value,
      applicantId: currentUser.value.user_id,
    };

    await axios.post('http://localhost:8080/api/transfer', formData, {
      withCredentials: true
    });
    await getTransferList();
    ElMessage.success('调拨记录新增成功！');
    addTransferDialogVisible.value = false;
  } catch (error) {
    ElMessage.error('提交失败: ' + error.message);
  }
};

// 提交批量调拨
const submitBatchTransfer = async () => {
  try {
    // 验证表单
    if (!batchTransferForm.value.fromLocationName ||
        !batchTransferForm.value.toLocationName ||
        !batchTransferForm.value.transferReason) {
      ElMessage.error('请填写完整的调拨信息');
      return;
    }

    // 验证备件列表
    for (const part of batchTransferForm.value.parts) {
      if (!part.partName || part.quantity <= 0) {
        ElMessage.error('请填写完整的备件信息');
        return;
      }
    }

    // 准备批量调拨数据
    const transfers = batchTransferForm.value.parts.map(part => ({
      fromLocationName: batchTransferForm.value.fromLocationName,
      toLocationName: batchTransferForm.value.toLocationName,
      partName: part.partName,
      quantity: part.quantity,
      transferReason: batchTransferForm.value.transferReason,
      applicantId: currentUser.value.user_id,
      status: '待审核'
    }));

    const res = await axios.post('http://localhost:8080/api/transfer/batch', transfers, {
      withCredentials: true
    });

    await getTransferList();
    ElMessage.success(`成功提交${transfers.length}条调拨申请！`);
    batchTransferDialogVisible.value = false;
  } catch (error) {
    ElMessage.error('批量调拨提交失败: ' + error.message);
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