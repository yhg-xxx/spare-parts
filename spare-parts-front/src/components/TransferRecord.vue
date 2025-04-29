<template>
  <div>
    <el-dialog v-model="importDialogVisible" title="Excel导入" width="500px">
      <el-upload
          class="upload-dragger"
          drag
          :action="importUrl"
          :data="{ applicantId: currentUser.user_id }"
          :with-credentials="true"
          :before-upload="beforeUpload"
          :on-success="handleUploadSuccess"
          :show-file-list="false"
      >
        <div class="upload-area">
          <el-icon :size="40" color="#409eff"><Upload /></el-icon>
          <div class="upload-text">
            <p>点击或将文件拖拽到此区域</p>
            <p class="upload-tip">支持.xlsx格式文件</p>
          </div>
        </div>
      </el-upload>
    </el-dialog>
    <!-- 操作按钮和筛选容器 -->
    <!-- 操作栏容器 -->
    <div class="operation-container">
      <!-- 左侧操作组 -->
      <div class="left-actions">
        <el-button-group>
          <el-button type="primary" @click="openAddTransferDialog" :icon="Plus">新增调拨</el-button>
          <el-button type="primary" @click="openBatchTransferDialog" :icon="Plus">批量调拨</el-button>
          <el-button type="primary" @click="downloadTemplate" :icon="Download">下载模板</el-button>
        </el-button-group>
      </div>

      <!-- 修改右侧操作组部分 -->
      <div class="right-actions">
        <!-- 修改后正确代码 -->
        <div class="filter-group">
          <el-button
              type="success"
              plain
              :icon="Upload"
              @click="openImportDialog"
          >
            导入Excel
          </el-button>

          <!-- 状态筛选 -->
          <el-select
              v-model="selectedStatus"
              multiple
              collapse-tags
              placeholder="状态筛选"
              style="width: 160px;"
          >
            <el-option
                v-for="status in statusOptions"
                :key="status.value"
                :label="status.label"
                :value="status.value"
            />
          </el-select>
        </div>
      </div>
    </div>
    <!-- 调拨记录列表 -->
    <el-table :data="filteredTransferList" stripe style="width: 100%">
      <el-table-column prop="transferId" label="调拨记录ID" />
      <el-table-column prop="fromLocationName" label="原仓库" />
      <el-table-column prop="toLocationName" label="目标仓库" />
      <el-table-column prop="partName" label="备件名称" />
      <el-table-column prop="sn" label="SN号" />
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
          <el-select
              v-model="addTransferForm.fromLocationName"
              placeholder="请选择原仓库"
              @change="fetchSnListForAddForm">
            <el-option
                v-for="location in warehouseList"
                :key="location.location_id"
                :label="location.locationName"
                :value="location.locationName">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="目标仓库" prop="toLocationName" required>
          <el-select
              v-model="addTransferForm.toLocationName"
              placeholder="请选择目标仓库">
            <el-option
                v-for="location in warehouseList"
                :key="location.location_id"
                :label="location.locationName"
                :value="location.locationName">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备件名称" prop="partName" required>
          <el-select
              v-model="addTransferForm.partName"
              placeholder="请选择备件名称"
              @change="fetchSnListForAddForm">
            <el-option
                v-for="part in partList"
                :key="part.partId"
                :label="part.partName"
                :value="part.partName">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="SN号" prop="sn" required>
          <el-select
              v-model="addTransferForm.sn"
              placeholder="请选择SN号"
              :disabled="!addTransferForm.fromLocationName || !addTransferForm.partName">
            <el-option
                v-for="item in snList"
                :key="item"
                :label="item"
                :value="item">
            </el-option>
          </el-select>
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
    <!-- 批量调拨对话框 -->
    <el-dialog v-model="batchTransferDialogVisible" title="批量调拨" @close="clearBatchTransferForm">
      <el-form :model="batchTransferForm" ref="batchTransferFormRef" label-width="100px">
        <el-form-item label="原仓库" prop="fromLocationName" required>
          <el-select
              v-model="batchTransferForm.fromLocationName"
              placeholder="请选择原仓库"
              @change="fetchSnListForBatchForm">
            <el-option
                v-for="location in warehouseList"
                :key="location.location_id"
                :label="location.locationName"
                :value="location.locationName">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="目标仓库" prop="toLocationName" required>
          <el-select
              v-model="batchTransferForm.toLocationName"
              placeholder="请选择目标仓库">
            <el-option
                v-for="location in warehouseList"
                :key="location.location_id"
                :label="location.locationName"
                :value="location.locationName">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="调拨事由" prop="transferReason" required>
          <el-input v-model="batchTransferForm.transferReason" type="textarea" placeholder="请输入调拨事由"></el-input>
        </el-form-item>
        <el-form-item label="备件列表" required>
          <el-button type="primary" @click="addPartItem">添加备件</el-button>
          <el-table :data="batchTransferForm.parts" style="width: 100%; margin-top: 10px;">
            <el-table-column prop="partName" label="备件名称">
              <template #default="{ row, $index }">
                <!-- 修正：使用row.partName而不是addTransferForm.partName -->
                <el-select
                    v-model="row.partName"
                    placeholder="请选择备件名称"
                    @change="() => fetchSnListForBatchForm(row)">
                  <el-option
                      v-for="part in partList"
                      :key="part.partId"
                      :label="part.partName"
                      :value="part.partName">
                  </el-option>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="sn" label="SN号">
              <template #default="{ row, $index }">
                <el-select
                    v-model="row.sn"
                    placeholder="请选择SN号"
                    :disabled="!batchTransferForm.fromLocationName || !row.partName">
                  <el-option
                      v-for="item in row.snOptions || []"
                      :key="item"
                      :label="item"
                      :value="item">
                  </el-option>
                </el-select>
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
import {Check, Plus, Upload} from "@element-plus/icons-vue";
const importUrl = ref('http://localhost:8080/api/transfer/import') // 后端接口地址
const currentUser = ref({});
const transferList = ref([]);
const addTransferDialogVisible = ref(false);
const batchTransferDialogVisible = ref(false);
const snList = ref([]);
const warehouseList = ref([]);
const partList = ref([]);
// 在原有代码基础上增加以下内容
import { computed } from 'vue'

const statusOptions = [
  { value: '待审核', label: '待审核', color: '#e6a23c' },
  { value: '已通过', label: '已通过', color: '#67c23a' },
  { value: '已驳回', label: '已驳回', color: '#f56c6c' }
]

// 选中状态（数组实现多选）
const selectedStatus = ref([])

// 切换状态选择
const toggleStatus = (status) => {
  const index = selectedStatus.value.indexOf(status)
  if (index > -1) {
    selectedStatus.value.splice(index, 1)
  } else {
    selectedStatus.value.push(status)
  }
}

// 过滤后的数据
const filteredTransferList = computed(() => {
  if (selectedStatus.value.length === 0) return transferList.value
  return transferList.value.filter(item =>
      selectedStatus.value.includes(item.status)
  )
})
// 新增调拨记录表单
const addTransferForm = ref({
  fromLocationName: '',
  toLocationName: '',
  partName: '',
  sn: '',
  transferReason: '',
  status: '待审核',
});

// 批量调拨表单
const batchTransferForm = ref({
  fromLocationName: '',
  toLocationName: '',
  transferReason: '',
  parts: [
    { partName: '', sn: '', snOptions: [] }
  ]
});

onMounted(async () => {
  const user = JSON.parse(sessionStorage.getItem('user'));
  if (!user) {
    ElMessage.error('请先登录');
    await router.push('/');
    return;
  }
  currentUser.value = user;
  await getTransferList();
  await fetchWarehouseList();
  await fetchPartList();
});

// 获取仓库列表
const fetchWarehouseList = async () => {
  try {
    const res = await axios.get("http://localhost:8080/warehouse/x");
    warehouseList.value = res.data;
  } catch (error) {
    ElMessage.error('获取仓库列表失败: ' + error.message);
  }
};


// 修改后的获取备件列表方法
const fetchPartList = async () => {
  try {
    const res = await axios.get("http://localhost:8080/spare_part");
    const data = res.data.content || res.data;

    // 使用Map实现去重（保留第一个出现的记录）
    const uniqueParts = Array.from(new Map(
        data.map(part => [part.partName, part])
    ).values());

    partList.value = uniqueParts;
  } catch (error) {
    ElMessage.error('获取备件列表失败: ' + error.message);
  }
};
// 新增导入对话框显示状态
const importDialogVisible = ref(false)
// 下载模板
const downloadTemplate = () => {
  window.location.href = 'http://localhost:8080/files/download/1745809622545_%E5%A4%87%E4%BB%B6%E8%B0%83%E6%8B%A8%E6%A8%A1%E7%89%88.xlsx';
}
// 修改导入按钮点击事件（替换原有el-upload）
const openImportDialog = () => {
  importDialogVisible.value = true
}
// 修改beforeUpload方法
const beforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  if (!isExcel) {
    ElMessage.error('仅支持.xlsx格式文件')
  }
  return isExcel
}
// 修改上传成功处理
const handleUploadSuccess = (res) => {
  importDialogVisible.value = false
  handleImportSuccess(res) // 调用原有处理逻辑
}
// 处理导入结果
const handleImportSuccess = (res) => {
  if (res.errors && res.errors.length > 0) {
    ElMessage.warning(`导入完成，成功 ${res.successCount} 条，失败 ${res.errorCount} 条`);
    // 显示错误详情（示例）
    this.errorDetails = res.errors;
  } else {
    ElMessage.success(`成功导入 ${res.successCount} 条记录`);
  }
  this.getTransferList(); // 刷新列表
}
// 获取SN号列表（新增调拨表单）
const fetchSnListForAddForm = async () => {
  if (!addTransferForm.value.fromLocationName || !addTransferForm.value.partName) {
    return;
  }

  try {
    const res = await axios.get(
        `http://localhost:8080/spare_part/sn?locationName=${addTransferForm.value.fromLocationName}&partName=${addTransferForm.value.partName}`
    );
    snList.value = res.data;
    addTransferForm.value.sn = ''; // 重置SN号选择
  } catch (error) {
    ElMessage.error('获取SN号列表失败: ' + error.message);
    snList.value = [];
  }
};


// 获取SN号列表（批量调拨表单）
const fetchSnListForBatchForm = async (row) => {
  if (!batchTransferForm.value.fromLocationName || !row.partName) {
    return;
  }

  try {
    const res = await axios.get(
        `http://localhost:8080/spare_part/sn?locationName=${batchTransferForm.value.fromLocationName}&partName=${row.partName}`
    );
    row.snOptions = res.data;
    row.sn = ''; // 重置SN号选择
  } catch (error) {
    ElMessage.error('获取SN号列表失败: ' + error.message);
    row.snOptions = [];
  }
};

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
    sn: '',
    transferReason: '',
    status: '待审核',
  };
  snList.value = [];
};

// 清空批量调拨表单
const clearBatchTransferForm = () => {
  batchTransferForm.value = {
    fromLocationName: '',
    toLocationName: '',
    transferReason: '',
    parts: [
      {partName: '', sn: '', snOptions: []}
    ]
  };
};

// 添加备件项
const addPartItem = () => {
  batchTransferForm.value.parts.push({partName: '', sn: '', snOptions: []});
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
    const res = await axios.get("http://localhost:8080/api/transfer");
    transferList.value = res.data;
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

    await axios.post('http://localhost:8080/api/transfer', formData);
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
      if (!part.partName || !part.sn) {
        ElMessage.error('请填写完整的备件信息');
        return;
      }
    }

    // 准备批量调拨数据
    const transfers = batchTransferForm.value.parts.map(part => ({
      fromLocationName: batchTransferForm.value.fromLocationName,
      toLocationName: batchTransferForm.value.toLocationName,
      partName: part.partName,
      sn: part.sn,
      transferReason: batchTransferForm.value.transferReason,
      applicantId: currentUser.value.user_id,
      status: '待审核'
    }));

    const res = await axios.post('http://localhost:8080/api/transfer/batch', transfers);
    await getTransferList();
    ElMessage.success(`成功提交${transfers.length}条调拨申请！`);
    batchTransferDialogVisible.value = false;
  } catch (error) {
    ElMessage.error('批量调拨提交失败: ' + error.message);
  }
};

// 批准调拨


// 格式化日期
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
/* 操作容器 */
.operation-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 4px;

  /* 移动端适配 */
  @media (max-width: 768px) {
    flex-direction: column;
    gap: 12px;

    .left-actions, .right-actions {
      width: 100%;
    }

    .el-button-group {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    .upload-wrapper {
      width: 100%;
      .el-button {
        width: 100%;
      }
    }
  }
}
/* 添加拖拽区域样式 */
.upload-dragger {
  :deep(.el-upload-dragger) {
    padding: 40px;
    border-radius: 8px;
    transition: border-color 0.3s;

    &:hover {
      border-color: #409eff !important;
    }
  }

  .upload-area {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;

    .upload-text {
      text-align: center;
      p {
        margin: 0;
        color: #606266;
      }

      .upload-tip {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}
/* 表格优化 */
.custom-table {
  .el-table__header th {
    background-color: #f5f7fa !important;
    font-weight: 600;
  }

  .el-table__fixed-right {
    box-shadow: -2px 0 8px rgba(0, 0, 0, 0.08);
  }
}

/* 分页容器 */
.pagination-container {
  margin-top: 16px;
  padding: 12px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 15px;
}

.status-filters {
  display: flex;
  gap: 8px;
  align-items: center;
}

.status-tag {
  display: flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 16px;
  border: 1px solid #dcdfe6;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 2px 6px rgba(32, 160, 255, 0.1);
  }

  &.active {
    border-color: currentColor;
    background-color: rgba(64, 158, 255, 0.1);

    .status-dot {
      background: currentColor;
      box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.2);
    }

    &.status-待审核 { color: #e6a23c; }
    &.status-已通过 { color: #67c23a; }
    &.status-已驳回 { color: #f56c6c; }
  }
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: transparent;
  margin-right: 6px;
  transition: all 0.2s ease;
}

.status-label {
  font-size: 13px;
  color: #606266;
}

.active .status-label {
  color: inherit;
  font-weight: 500;
}
</style>