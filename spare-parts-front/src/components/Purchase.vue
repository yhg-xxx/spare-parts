<template>
  <div>
    <!-- 操作按钮行 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="12">
        <el-button @click="openAddDailyDialog" type="primary" :icon="Plus">新增订单</el-button>
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
    <!-- 订单列表 -->
    <el-table :data="dailyList" stripe style="width: 100%">
      <el-table-column prop="order_id" label="采购订单id" />
      <el-table-column prop="applicant_id" label="申请人id" />
      <el-table-column prop="station" label="需求车站" />
      <el-table-column prop="workshop" label="所属工区" />
      <el-table-column prop="spare_part_name" label="备件名称" />
      <el-table-column prop="spare_part_model" label="备件型号" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="created_at" label="创建时间">
        <template #default="{ row }">
          {{ formatDate(row.created_at) }}
        </template>
      </el-table-column>
      <el-table-column prop="completed_at" label="完成时间">
        <template #default="{ row }">
          {{ formatDate(row.completed_at) }}
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增订单对话框 -->
    <el-dialog v-model="addDailyDialogVisible" title="新增日报" @close="clearAddDailyForm">
      <!-- 修改对话框中的表单定义 -->
      <el-form
          :model="addDailyForm"
          :rules="addDailyFormRules"
      ref="addDailyFormRef"
      label-width="100px"
      >
      <el-form-item label="需求车站" prop="station">  <!-- 添加prop属性 -->
        <el-input
            v-model="addDailyForm.station"
            placeholder="请输入需求车站"
            clearable
        ></el-input>
      </el-form-item>

      <el-form-item label="所属工区" prop="workshop">
        <el-input
            v-model="addDailyForm.workshop"
            placeholder="请输入所属工区"
            clearable
        ></el-input>
      </el-form-item>

      <el-form-item label="备件名称" prop="spare_part_name">
        <el-input
            v-model="addDailyForm.spare_part_name"
            placeholder="请输入备件名称"
            clearable
        ></el-input>
      </el-form-item>

      <el-form-item label="备件型号" prop="spare_part_model">
        <el-input
            v-model="addDailyForm.spare_part_model"
            placeholder="请输入备件型号"
            clearable
        ></el-input>
      </el-form-item>
      </el-form>
      <!--foot-->
      <div slot="footer" class="dialog-footer">
        <el-button @click="addDailyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddDaily">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {ElMessage} from "element-plus";
import axios from "axios";
import router from "@/router.js";
import {Plus, Search} from "@element-plus/icons-vue";
// 新增查询条件变量
const querySparePartName = ref('');

const currentUser = ref({})
const dailyList = ref([]); // 日报数据列表
const addDailyDialogVisible = ref(false); // 新增日报对话框
// 在script setup顶部添加模板引用声明
const addDailyFormRef = ref(null);

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


// 新增订单表单
const addDailyForm = ref({

  station: '',
  workshop: '',
  spare_part_name: '',
  spare_part_model: '',
  status: '待审核',      // 状态（不需要用户输入，直接设为初始值）

});


// 打开新增订单对话框
const openAddDailyDialog = () => {
  addDailyDialogVisible.value = true;
};

// 清空新增订单表单
const clearAddDailyForm = () => {
  addDailyFormRef.value?.resetFields(); // 清除验证状态
  addDailyForm.value = {
    ...addDailyForm.value, // 保留已有值
    station: '',
    workshop: '',
    spare_part_name: '',
    spare_part_model: '',
  };
};


// 新增订单表单验证规则
const addDailyFormRules = ref({
  station: [
    { required: true, message: '请输入需求车站', trigger: 'blur' }
  ],
  workshop: [
    { required: true, message: '请输入所属工区', trigger: 'blur' }
  ],
  spare_part_name: [
    { required: true, message: '请输入备件名称', trigger: 'blur' }
  ],
  spare_part_model: [
    { required: true, message: '请输入备件型号', trigger: 'blur' }
  ]
});
// 修改获取订单列表方法，支持参数
const getDailyList = async () => {
  try {
    const params = {};
    if (querySparePartName.value) {
      params.spare_part_name = querySparePartName.value;
    }
    const res = await axios.get("http://localhost:8080/purchase_order/a", {
      params,
      withCredentials: true
    });
    dailyList.value = res.data;
  } catch (error) {
    ElMessage.error('获取订单列表失败: ' + error.message)
  }
};
// 修改后的提交方法
const submitAddDaily = async () => {
  try {
    // 先进行表单验证
    await addDailyFormRef.value.validate();

    const formData = {
      ...addDailyForm.value,
      applicant_id: currentUser.value.user_id,
      created_at: new Date().toISOString()
    }

    await axios.post('http://localhost:8080/purchase_order', formData, {
      withCredentials: true
    });

    await getDailyList();
    ElMessage.success('采购订单新增成功！');
    addDailyDialogVisible.value = false;
  } catch (error) {
    if (error.name === 'ElFormValidateError') { // 表单验证失败的特殊处理
      console.log('表单校验未通过');
    } else {
      ElMessage.error('提交失败: ' + (error.response?.data?.message || error.message));
    }
  }
};


// 查询按钮事件
const handleSearch = () => {
  getDailyList();
};

// 清空按钮事件
const clearSearch = () => {
  querySparePartName.value = '';
  getDailyList();
};


// 显示数据时格式化时间
const formatDate = (timestamp) => {
  if (!timestamp) return '-';
  return new Date(timestamp).toLocaleString();
};



</script>
<style scoped>
.dialog-footer {
  text-align: right;
}
</style>
