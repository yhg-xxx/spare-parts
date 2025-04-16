<template>
  <div class="app-container">
    <!-- 第一行：操作按钮和搜索 -->
    <el-row :gutter="20" class="toolbar-container">
      <el-col :span="12">
        <el-button type="success" @click="handleCreate" :icon="Plus">添加备件</el-button>
      </el-col>
      <el-col :span="12" class="search-container">
        <el-input
            v-model="listQuery.partName
"
            placeholder="请输入备件名称"
            clearable
            class="search-input"
        />
        <el-button type="primary" @click="handleFilter" :icon="Search" class="search-btn">查询</el-button>
        <el-button @click="clearSearch" class="reset-btn">清空</el-button>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table
        :data="tableData"
        stripe
        style="width: 100%"
        class="custom-table"
    >
      <el-table-column prop="part_id" label="序号" width="80" align="center" />
      <el-table-column prop="partName" label="备件名称" min-width="120" />
      <el-table-column prop="part_model" label="型号规格" min-width="150" />
      <el-table-column prop="category" label="分类" width="120">
        <template #default="{row}">
          <el-tag type="info">{{ row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="manufacturer" label="生产厂家" min-width="150" />
      <el-table-column prop="unit" label="单位" min-width="150" />
      <el-table-column prop="number" label="数量" width="160" />
      <el-table-column prop="anquan" label="安全库存" width="160" />
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
              @click="handleUpdate(row)"
              type="warning"
              size="small"
          >编辑</el-button>
          <el-button
              @click="handleDelete(row.part_id)"
              type="danger"
              size="small"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 第三行：分页 -->
    <el-pagination
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        v-model:current-page="listQuery.page"
        v-model:page-size="listQuery.limit"
        @current-change="handleCurrentChange"
        class="pagination-container"
    />

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTypeMap[dialogType]" width="600px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="备件名称" prop="partName">
          <el-input v-model="formData.partName" placeholder="请输入备件名称" />
        </el-form-item>

        <el-form-item label="型号规格" prop="part_model">
          <el-input v-model="formData.part_model" placeholder="请输入型号规格" />
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select
              v-model="formData.category"
              placeholder="请选择分类"
              style="width: 100%"
              clearable
          >
            <el-option
                v-for="item in categoryOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="生产厂家" prop="manufacturer">
          <el-input v-model="formData.manufacturer" placeholder="请输入生产厂家" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="formData.unit" placeholder="请输入单位" />
        </el-form-item>
        <el-form-item label="备件数量" prop="number">
          <el-input v-model="formData.number" placeholder="请输入备件数量" />
        </el-form-item>






      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import axios from 'axios'
// 原有脚本逻辑保持不变，增加清空搜索方法
const clearSearch = () => {
  listQuery.partName
 = ''
  handleFilter()
}
const categoryOptions = [
  { value: '机械', label: '机械' },
  { value: '电子', label: '电子' },
  { value: '液压', label: '液压' },
  { value: '电气', label: '电气' }
]


// 数据管理
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('create')
const dialogTypeMap = { create: '新增备件', update: '编辑备件' }

// 查询参数
const listQuery = reactive({
  page: 1,
  limit: 10,
  partName: ''
})

// 表单数据
const formRef = ref(null)
const formData = reactive({
  part_id: null,
  partName: '',
  part_model: '',
  category: '',
  manufactorer:'',
  unit:'',
  number:'',
  anquan:'20',
})

// 验证规则
// ✅ 正确写法（使用大括号包裹键值对）
const rules = {
  partName: [{ required: true, message: '必填项', trigger: 'blur' }],
  part_model: [{ required: true, message: '必填项', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  unit: [{ required: true, message: '单位', trigger: 'change' }],
  manufactorer: [{ required: true, message: '生产厂家', trigger: 'change' }],
  number: [{ required: true, message: '数量', trigger: 'change' }],

};

// 初始化加载数据
const fetchData = async () => {
  try {
    loading.value = true
    const params = {
      page: listQuery.page,
      size: listQuery.limit,
      partName: listQuery.partName
    }
    const res = await axios.get('/spare_part', { params })
    tableData.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    ElMessage.error('数据加载失败: ' + error.message)
  } finally {
    loading.value = false
  }
}
const handleCurrentChange = (val) => {
  listQuery.page = val
  fetchData()
}

const handleFilter = () => {
  listQuery.page = 1
  fetchData()
}


// 打开新增对话框
const handleCreate = () => {
  dialogType.value = 'create'
  resetForm()
  dialogVisible.value = true
}

// 打开编辑对话框
const handleUpdate = (row) => {
  dialogType.value = 'update'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  try {
    await formRef.value.validate()

    const isCreate = dialogType.value === 'create'
    const method = isCreate ? 'post' : 'put'
    const url = isCreate ? '/spare_part' : `/spare_part/${formData.part_id}`

    await axios[method](url, formData)
    ElMessage.success(`${dialogTypeMap[dialogType.value]}成功`)
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('表单提交失败:', error)
  }
}

// 删除操作
const handleDelete = async (partId) => {
  try {
    await ElMessageBox.confirm('确认删除该备件？', '警告', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await axios.delete(`/spare_part/${partId}`, {
      withCredentials: true,
      validateStatus: (status) => status === 200
    })

    if (res.data === 1) {
      ElMessage.success('删除成功')
      await fetchData()
    } else {
      ElMessage.warning('删除失败：备件不存在')
    }
  } catch (error) {
    if (error.response) {
      ElMessage.error(`操作失败: ${error.response.data?.message || '服务器错误'}`)
    } else if (!error.response) {
      console.log('操作已取消')
    }
  }
}


// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  formData.part_id = null
}

// 日期格式化
const formatDate = (dateStr) => {
  return dateStr || '--'
}

// 初始化加载
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar-container {
  margin-bottom: 16px;

  .el-col {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

.search-container {
  justify-content: flex-end;

  .search-input {
    width: 240px;
  }

  .search-btn {
    margin-left: 8px;
  }
}

.data-table {
  flex: 1;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);

  :deep(.el-table__header) th {
    background-color: #f8fafc;
    font-weight: 600;
  }
}

.pagination-container {
  background: #fff;
  border-radius: 8px;
  padding: 12px 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .toolbar-container {
    flex-direction: column;

    .el-col {
      width: 100%;
      justify-content: flex-start;

      &:last-child {
        flex-wrap: wrap;
        gap: 8px;
      }
    }

    .search-input {
      width: 100%;
    }

    .el-button {
      flex: 1;
    }
  }

  .data-table {
    :deep(.el-table__cell) {
      padding: 8px 0;
    }
  }
}
</style>
