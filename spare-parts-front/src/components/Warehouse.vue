<template>
  <div>
    <!-- 新增仓库按钮 -->
    <el-button @click="openAddDailyDialog" type="primary" :icon="Plus">新增仓库
</el-button>
    <!-- 仓库列表 -->
    <el-table :data="dailyList" stripe style="width: 100%">
      <el-table-column prop="location_id" label="编号" />
      <el-table-column prop="location_code" label="仓库编码" />
      <el-table-column prop="locationName" label="仓库名称" />
      <el-table-column prop="description" label="仓库描述" />
      <el-table-column prop="created_at" label="创建时间">
        <template #default="{ row }">
          {{ formatDate(row.created_at) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="仓库状态" />
      <el-table-column prop="name" label="库管人" />


      <el-table-column fixed="right" label="操作" min-width="150">

        <template #default="{ row, $index }">
          <el-button @click="openEditDailyDialog(row)" type="warning" size="small">修改仓库
</el-button>
          <el-button @click="deleteDaily(row.location_id)" type="danger" size="small">删除仓库
</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增仓库对话框 -->
    <el-dialog v-model="addDailyDialogVisible" title="新增仓库" @close="clearAddDailyForm">
      <el-form :model="addDailyForm" ref="addDailyFormRef" label-width="100px">

        <el-form-item label="仓库编码" prop="location_code">
          <el-input v-model="addDailyForm.location_code" placeholder="请输入仓库编码"></el-input>
        </el-form-item>
        <el-form-item label="仓库名称" prop="locationName">
          <el-input v-model="addDailyForm.locationName" placeholder="请输入仓库名称"></el-input>
        </el-form-item>
        <el-form-item label="仓库描述" prop="description">
          <el-input v-model="addDailyForm.description" placeholder="仓库描述"></el-input>
        </el-form-item>
        <el-form-item label="库管人" prop="name">
          <el-input v-model="addDailyForm.name" placeholder="库管人"></el-input>
        </el-form-item>
      </el-form>
      <!--foot-->
      <div slot="footer" class="dialog-footer">
        <el-button @click="addDailyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddDaily">确定</el-button>
      </div>
    </el-dialog>

    <!-- 修改仓库对话框 -->
    <el-dialog v-model="editDailyDialogVisible" title="修改仓库信息" @close="clearEditDailyForm">
      <el-form :model="editDailyForm" ref="editDailyFormRef" label-width="100px">
        <el-form-item label="仓库编码" prop="location_code">
          <el-input v-model="editDailyForm.location_code" placeholder="请输入仓库编码"></el-input>
        </el-form-item>
        <el-form-item label="仓库名称" prop="locationName">
          <el-input v-model="editDailyForm.locationName" placeholder="请输入仓库名称"></el-input>
        </el-form-item>
        <el-form-item label="仓库描述" prop="description">
          <el-input v-model="editDailyForm.description" placeholder="仓库描述"></el-input>
        </el-form-item>
        <el-form-item label="库管人" prop="name">
          <el-input v-model="editDailyForm.name" placeholder="库管人"></el-input>
        </el-form-item>
      </el-form>
        <!--foot-->
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDailyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEditDaily">确定</el-button>
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

const currentUser = ref({})
const dailyList = ref([]); // 仓库数据列表
const addDailyDialogVisible = ref(false); // 新增仓库对话框
const editDailyDialogVisible = ref(false); // 修改仓库对话框

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


// 新增仓库表单
const addDailyForm = ref({
  location_code: '',
  locationName: '',
  description: '',
  name:'',
  status: '可用',      // 状态（不需要用户输入，直接设为初始值）

});

// 修改仓库表单
const editDailyForm = ref({
  location_code: '',
  locationName: '',
  description: '',
  name:'',
  status: '可用',      // 状态（不需要用户输入，直接设为初始值）
});

// 打开新增仓库对话框
const openAddDailyDialog = () => {
  addDailyDialogVisible.value = true;
};
// 打开修改仓库对话框
const openEditDailyDialog = (row) => {
  editDailyForm.value = {...row};
  editDailyDialogVisible.value = true;
};

// 清空新增仓库表单
const clearAddDailyForm = () => {
  addDailyForm.value = {
    location_code: '',
    locationName: '',
    description: '',
    name:'',
    status: '可用',
  };
};

// 清空修改仓库表单
const clearEditDailyForm = () => {
  editDailyForm.value = {
    location_code: '',
    locationName: '',
    description: '',
    name:'',
    status: '可用',
  };
};
//显示数据
const getDailyList = async () => {
  try {
    const res = await axios.get("http://localhost:8080/warehouse/x", {
      withCredentials: true
    });
    dailyList.value = res.data.list || res.data;

  } catch (error) {
    ElMessage.error('获取仓库列表失败: ' + error.message)
  }


};


//  提交新增表单
const submitAddDaily = async () => {
  try {
    const formData = {
      ...addDailyForm.value,
      created_at: new Date().toISOString()
    }

    await axios.post('http://localhost:8080/warehouse',  formData, {
      withCredentials: true
    })
    console.log(addDailyForm.value)
    await getDailyList()
    ElMessage.success('仓库新增成功！');
    addDailyDialogVisible.value = false;
  } catch (error) {
    ElMessage.error('发送失败: ' + error.message)
  }
}

// 删除仓库

const deleteDaily = async (location_id) => {
  try {
    const response = await axios.delete(`http://localhost:8080/warehouse/${location_id}`);
    if (response.data === 1) {
      await getDailyList()
      ElMessage.success('删除成功！');
    }else {
      ElMessage.error("删除失败！");
    }
    // 这里可以处理删除后的逻辑，比如重新获取部门列表
  } catch (error) {
    console.error('删除失败:', error);
    // 这里可以处理错误，比如显示错误提示
  }
};

// 提交修改表单
const submitEditDaily = async () => {
  const response = await axios.put(`http://localhost:8080/warehouse/${editDailyForm.value.location_id}`, editDailyForm.value);
  if (response.data === 1)
    ElMessage.success('修改成功！');
  else {
    ElMessage.error("修改失败");
  }
  await getDailyList()
  editDailyDialogVisible.value = false;
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
