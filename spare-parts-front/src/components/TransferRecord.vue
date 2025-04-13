<template>
  <div>
    <!-- 新增调拨记录按钮 -->
    <el-button @click="openAddTransferDialog" type="primary" :icon="Plus">新增调拨</el-button>
    <!-- 调拨记录列表 -->
    <el-table :data="transferList" stripe style="width: 100%">
      <el-table-column prop="transferId" label="调拨记录ID" />
      <el-table-column prop="fromLocationId" label="原库位ID" />
      <el-table-column prop="toLocationId" label="目标库位ID" />
      <el-table-column prop="partId" label="备件ID" />
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
        <el-form-item label="原库位ID" prop="fromLocationId">
          <el-input v-model.number="addTransferForm.fromLocationId" placeholder="请输入原库位ID" type="number"></el-input>
        </el-form-item>
        <el-form-item label="目标库位ID" prop="toLocationId">
          <el-input v-model.number="addTransferForm.toLocationId" placeholder="请输入目标库位ID" type="number"></el-input>
        </el-form-item>
        <el-form-item label="备件ID" prop="partId">
          <el-input v-model.number="addTransferForm.partId" placeholder="请输入备件ID" type="number"></el-input>
        </el-form-item>
        <el-form-item label="调拨事由" prop="transferReason">
          <el-input v-model="addTransferForm.transferReason" placeholder="请输入调拨事由"></el-input>
        </el-form-item>
      </el-form>
      <!--foot-->
      <div slot="footer" class="dialog-footer">
        <el-button @click="addTransferDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddTransfer">确定</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {ElMessage} from "element-plus";
import axios from "axios";
import router from "@/router.js";
import {Plus} from "@element-plus/icons-vue";

const currentUser = ref({})
const transferList = ref([]); // 调拨记录数据列表
const addTransferDialogVisible = ref(false); // 新增调拨记录对话框

onMounted(async () => {
  const user = JSON.parse(sessionStorage.getItem('user'))
  if (!user) {
    ElMessage.error('请先登录')
    await router.push('/')
    return
  }
  currentUser.value = user
  await getTransferList()
})

// 新增调拨记录表单
const addTransferForm = ref({
  fromLocationId: null,
  toLocationId: null,
  partId: null,
  transferReason: '',
  status: '待审核', // 默认状态
});

// 打开新增调拨记录对话框
const openAddTransferDialog = () => {
  addTransferDialogVisible.value = true;
};

// 清空新增调拨记录表单
const clearAddTransferForm = () => {
  addTransferForm.value = {
    fromLocationId: null,
    toLocationId: null,
    partId: null,
    transferReason: '',
    status: '待审核',
  };
};

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

// 提交新增调拨记录
const submitAddTransfer = async () => {
  try {
    const formData = {
      ...addTransferForm.value,
      applicantId: currentUser.value.user_id,
      createdAt: new Date().toISOString()
    }

    await axios.post('http://localhost:8080/api/transfer', formData, {
      withCredentials: true
    })
    await getTransferList()
    ElMessage.success('调拨记录新增成功！');
    addTransferDialogVisible.value = false;
  } catch (error) {
    ElMessage.error('提交失败: ' + error.message)
  }
}

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