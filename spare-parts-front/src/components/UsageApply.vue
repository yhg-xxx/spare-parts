<template>
  <div>
    <!-- 申请操作区和记录列表 -->
    <el-button id="usage-apply-add-btn" @click="openApplyDialog" type="primary" :icon="Plus">新建领用</el-button>

    <!-- 领用记录表格 -->
    <el-table :data="usageRequests" stripe style="width: 100%" class="mt-4">
      <el-table-column prop="id" label="领用单号" width="120" />
      <el-table-column prop="partName" label="备件名称" />
      <el-table-column prop="partModel" label="备件型号" />
      <el-table-column prop="number" label="数量" width="120">
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
          {{ row.outTime ? formatDate(row.outTime) : '-' }}
        </template>
      </el-table-column>

    </el-table>

    <!-- 新增申请对话框 -->
    <el-dialog v-model="applyDialogVisible" title="备件领用申请" @close="clearApplyForm">
      <el-form :model="applyForm" label-width="100px" :rules="formRules">
        <!-- 新增备件名称 -->
        <el-form-item label="备件名称" prop="partName" required>
          <el-autocomplete
              id="usage-apply-partname"
              v-model="applyForm.partName"
              :fetch-suggestions="nameQuerySearch"
              @input="handleNameInput"
              :loading="nameLoading"
              :debounce="300"
              placeholder="请输入备件标准名称"
              @select="handleNameSelect"
          />
        </el-form-item>

        <!-- 修改备件型号输入为自动补全 -->
        <el-form-item label="备件型号" prop="partModel" required>
          <el-autocomplete
              id="usage-apply-partmodel"
              v-model="applyForm.partModel"
              :fetch-suggestions="modelQuerySearch"
              :loading="nameLoading"
              :debounce="300"
              placeholder="请输入完整型号"
              :disabled="!applyForm.partName"
          />
        </el-form-item>

        <el-form-item label="数量" prop="number" required>
          <el-input-number
              id="usage-apply-number"
              v-model="applyForm.number"
              :min="1"
              :max="100"

          />
        </el-form-item>

        <!-- 原类型选择 -->
        <el-form-item label="领用类型" prop="type" required>
          <el-select id="usage-apply-type" v-model="applyForm.type" placeholder="请选择">
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
              id="usage-apply-desc"
              v-model="applyForm.description"
              type="textarea"
              :rows="3"
              placeholder="请详细说明使用场景和用途"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button id="usage-apply-cancel" @click="applyDialogVisible = false">取消</el-button>
        <el-button id="usage-apply-submit" type="primary" @click="submitApply">提交</el-button>
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
const nameLoading = ref(false);
const partNames = ref([]);
const partModels = ref([]);
const applyForm = ref({

  type: '',
  partName: '',
  partModel: '',
  description: '',
  number: null  // 新增数量字段
});
const formRules = {
  partName: [
    { required: true, message: '请输入备件名称', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value && !partNames.value.includes(value)) {
          callback(new Error('请输入有效的备件名称'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  partModel: [
    { required: true, message: '请输入备件型号', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value && applyForm.partName && !partModels.value.includes(value)) {
          callback(new Error('请输入有效的型号'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  number: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '数量至少为1', trigger: 'blur' }
  ]
};
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
  await loadPartNames();
});
const loadPartNames = async () => {
  try {
    nameLoading.value = true;
    const res = await axios.get('/api/spare-parts/names');
    partNames.value = res.data;
  } catch (error) {
    ElMessage.error('获取备件名称失败');
  }finally {
    nameLoading.value = false;
  }
};

// 名称输入自动补全
const nameQuerySearch = (queryString, cb) => {
  const results = queryString
      ? partNames.value.filter(name =>
          name.toLowerCase().includes(queryString.toLowerCase())
      ).map(name => ({ value: name }))  // 确保格式转换
      : partNames.value.map(name => ({ value: name }));  // 空查询时也转换格式

  // 添加调试日志
  console.log('Name suggestions:', results);

  cb(results.length > 0 ? results : [{ value: '无匹配结果', disabled: true }]);
};
const handleNameInput = async (value) => {
  try {
    if (value) {
      const res = await axios.get('/api/spare-parts/names');
      partNames.value = res.data;
      console.log('Updated names:', partNames.value);
    }
  } catch (error) {
    console.error('名称输入错误:', error);
  }
};
// 型号输入自动补全
const modelQuerySearch = (queryString, cb) => {
  const results = queryString
      ? partModels.value.filter(model =>
          model.toLowerCase().includes(queryString.toLowerCase())
      )
      : partModels.value;
  cb(results.map(model => ({ value: model })));
};

// 监听名称变化
const handleNameSelect = async (selected) => {
  try {
    const res = await axios.get('/api/spare-parts/models', {
      params: { partName: selected.value }
    });
    partModels.value = res.data;
  } catch (error) {
    ElMessage.error('获取型号失败');
  }
};
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