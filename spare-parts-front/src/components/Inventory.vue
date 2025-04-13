<template>
  <div>
    <!-- 新增日报按钮 -->
    <el-button @click="openAddDailyDialog" type="primary" :icon="Plus">新增库位</el-button>
    <!-- 查询日报按钮 -->
    <!--    <el-button @click="router.push('/chaxun')" type="primary" :icon="Search" >查询</el-button>-->
    <!-- 日报列表 -->
    <el-table :data="dailyList" stripe style="width: 100%">
      <el-table-column prop="inventory_id" label="库存记录ID" />
      <el-table-column prop="part_id" label="备件编号" />
      <el-table-column prop="location_id" label="库位编号" />
      <el-table-column prop="sn" label="SN号" />
      <el-table-column prop="status" label="库存状态" />
      <el-table-column prop="inbound_time" label="入库时间">
      </el-table-column>
      <el-table-column prop="outbound_time" label="出库时间" />

      <el-table-column fixed="right" label="操作" min-width="150">

        <template #default="{ row, $index }">
          <el-button @click="openEditDailyDialog(row)" type="warning" size="small">修改库存</el-button>
          <el-button @click="deleteDaily(row.inventory_id)" type="danger" size="small">删除库存</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增日报对话框 -->
    <el-dialog v-model="addDailyDialogVisible" title="新增库位" @close="clearAddDailyForm">
      <el-form :model="addDailyForm" ref="addDailyFormRef" label-width="100px">

        <el-form-item label="备件编号" prop="part_id">
          <el-input v-model="addDailyForm.part_id" placeholder="请输入备件编号"></el-input>
        </el-form-item>
        <el-form-item label="库位编号" prop="location_id">
          <el-input v-model="addDailyForm.location_id" placeholder="库位编号"></el-input>
        </el-form-item>
        <el-form-item label="SN号" prop="sn">
          <el-input v-model="addDailyForm.sn" placeholder="SN号"></el-input>
        </el-form-item>
      </el-form>
      <!--foot-->
      <div slot="footer" class="dialog-footer">
        <el-button @click="addDailyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddDaily">确定</el-button>
      </div>
    </el-dialog>

    <!-- 修改日报对话框 -->
    <el-dialog v-model="editDailyDialogVisible" title="修改日报" @close="clearEditDailyForm">
      <el-form :model="editDailyForm" ref="editDailyFormRef" label-width="100px">
        <el-form-item label="备件编号" prop="part_id">
          <el-input v-model="editDailyForm.part_id" placeholder="请输入备件编号"></el-input>
        </el-form-item>
        <el-form-item label="库位编号" prop="location_id">
          <el-input v-model="editDailyForm.location_id" placeholder="库位编号"></el-input>
        </el-form-item>
        <el-form-item label="SN号" prop="sn">
          <el-input v-model="editDailyForm.sn" placeholder="SN号"></el-input>
        </el-form-item>
      <!--foot-->
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDailyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEditDaily">确定</el-button>
      </div>
      </el-form>
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
const dailyList = ref([]); // 日报数据列表
const addDailyDialogVisible = ref(false); // 新增日报对话框
const editDailyDialogVisible = ref(false); // 修改日报对话框

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


// 新增日报表单
const addDailyForm = ref({
  part_id: '',
  location_id: '',
  sn: '',
  status: '在库',      // 状态（不需要用户输入，直接设为初始值）

});

// 修改日报表单
const editDailyForm = ref({
  part_id: '',
  location_id: '',
  sn: '',
  status: '在库',      // 状态（不需要用户输入，直接设为初始值）

});

// 打开新增日报对话框
const openAddDailyDialog = () => {
  addDailyDialogVisible.value = true;
};
// 打开修改日报对话框
const openEditDailyDialog = (row) => {
  editDailyForm.value = {...row};
  editDailyDialogVisible.value = true;
};

// 清空新增日报表单
const clearAddDailyForm = () => {
  addDailyForm.value = {
    part_id: '',
    location_id: '',
    sn: '',
  };
};

// 清空修改日报表单
const clearEditDailyForm = () => {
  editDailyForm.value = {
    part_id: '',
    location_id: '',
    sn: '',
  };
};
//显示数据
const getDailyList = async () => {
  try {
    const res = await axios.get("http://localhost:8080/inventory/x", {
      withCredentials: true
    });
    dailyList.value = res.data.list || res.data;

  } catch (error) {
    ElMessage.error('获取学生列表失败: ' + error.message)
  }


};

//  提交新增表单
const submitAddDaily = async () => {
  try {
    const formData = {
      ...addDailyForm.value,
    }

    await axios.post('http://localhost:8080/inventory',  formData, {
      withCredentials: true
    })
    console.log(addDailyForm.value)
    await getDailyList()
    ElMessage.success('库位新增成功！');
    addDailyDialogVisible.value = false;
  } catch (error) {
    ElMessage.error('发送失败: ' + error.message)
  }
}

// 删除日报
const deleteDaily = async (inventory_id) => {
  try {
    const response = await axios.delete(`http://localhost:8080/inventory/${inventory_id}`);
    await getDailyList()
    if (response.data === 1)
      ElMessage.success('删除成功！');
    else {
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
  const response = await axios.put(`http://localhost:8080/inventory/${editDailyForm.value.inventory_id}`, editDailyForm.value);
  if (response.data === 1)
    ElMessage.success('修改成功！');
  else {
    ElMessage.error("修改失败");
  }
  await getDailyList()
  editDailyDialogVisible.value = false;
};

</script>
<style scoped>
.dialog-footer {
  text-align: right;
}
</style>
