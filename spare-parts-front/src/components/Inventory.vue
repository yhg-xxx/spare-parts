<template>
  <div class="container">
    <!-- 搜索区域 -->
    <div class="search-section">
      <el-card shadow="never" class="search-card">
        <div class="search-group">
          <el-input
              v-model="searchSn"
              placeholder="输入SN号查询全生命周期信息"
              class="sn-input"
              clearable
              @keyup.enter="handleSnSearch"
          >
            <template #append>
              <div class="button-group">
                <el-button
                    type="primary"
                    class="search-btn"
                    @click="handleSnSearch"
                >
                  查询
                </el-button>
                <el-button
                    class="clear-btn"
                    @click="handleClearSearch"
                >
                  清空
                </el-button>
              </div>
            </template>
          </el-input>
        </div>
      </el-card>
    </div>

    <!-- 结果展示区域 -->
    <div v-if="lifecycleData" class="result-section">
      <el-card shadow="never" class="result-card">
        <!-- 卡片头部样式优化 -->
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <span class="sn-title">SN: {{ lifecycleData.basicInfo.sn }}</span>
              <el-tag
                  :type="lifecycleData.basicInfo.status === '在库' ? 'success' : 'info'"
                  effect="light"
                  class="status-tag"
              >
                {{ lifecycleData.basicInfo.status }}
              </el-tag>
            </div>
            <el-text type="info" class="update-time">
              最后更新：{{ getLatestUpdateTime() || '无可用记录' }}
            </el-text>
          </div>
        </template>

        <el-collapse v-model="activeCollapse" class="smart-collapse">
          <!-- 基础信息 -->
          <el-collapse-item title="基础信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="备件名称">{{ lifecycleData.basicInfo.partName }}</el-descriptions-item>
              <el-descriptions-item label="型号">{{ lifecycleData.basicInfo.partModel }}</el-descriptions-item>
              <el-descriptions-item label="分类">{{ lifecycleData.basicInfo.category }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ lifecycleData.basicInfo.sparePartStatus }}</el-descriptions-item>
              <el-descriptions-item label="生产厂商">{{ lifecycleData.basicInfo.manufacturer }}</el-descriptions-item>
              <el-descriptions-item label="入库时间">{{ formatDateTime(lifecycleData.inboundInfo?.createdAt) }}</el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>

          <!-- 故障维修记录 -->
          <el-collapse-item title="故障维修记录" name="fault">
            <el-table :data="lifecycleData.faultRecords" empty-text="无故障记录">
              <el-table-column prop="faultTime" label="故障时间" width="180">
                <template #default="{row}">
                  {{ formatDateTime(row.faultTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="processedAt" label="处理时间" width="180">
                <template #default="{row}">
                  {{ formatDateTime(row.processedAt) }}
                </template>
              </el-table-column>
              <el-table-column prop="reviewAt" label="验收时间" width="180">
                <template #default="{row}">
                  {{ formatDateTime(row.reviewAt) }}
                </template>
              </el-table-column>
              <el-table-column prop="reviewResult" label="验收结果" width="120">
                <template #default="{row}">
                  {{ row.reviewResult || '未验收' }}
                </template>
              </el-table-column>
            </el-table>
          </el-collapse-item>

          <!-- 出入库记录 -->
          <el-collapse-item title="出入库记录" name="inout">
            <el-timeline>
              <el-timeline-item
                  v-for="(record, index) in lifecycleData.inoutRecords"
                  :key="index"
                  :timestamp="formatDateTime(record.time)"
                  placement="top"
              >
                <el-tag
                    :type="getInoutTagType(record.type)"
                    :class="['record-tag', record.type.toLowerCase()]"
                >
                  {{ record.type }}
                </el-tag>
                {{ record.detail }}
              </el-timeline-item>
            </el-timeline>
          </el-collapse-item>

          <!-- 调拨记录 -->
          <el-collapse-item title="调拨记录" name="transfer">
            <el-table :data="lifecycleData.transferRecords" empty-text="无调拨记录">
              <el-table-column prop="createdAt" label="调拨时间" width="180">
                <template #default="{row}">
                  {{ formatDateTime(row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="调拨路径" width="200">
                <template #default="{row}">
                  {{ row.fromLocationName }} → {{ row.toLocationName }}
                </template>
              </el-table-column>
              <el-table-column prop="transferReason" label="调拨事由"/>
            </el-table>
          </el-collapse-item>

          <el-collapse-item title="返厂记录" name="return">
            <el-table
                :data="lifecycleData.returnFactoryRecords"
                empty-text="无返厂记录"
                style="width: 100%"
            >
              <el-table-column prop="sentTime" label="发送时间" width="180">
                <template #default="{row}">
                  {{ convertStockoutTime(row.sentTime) }}
                </template>
              </el-table-column>

              <!-- 返厂时间 -->
              <el-table-column prop="actualReturnTime" label="返厂时间" width="180">
                <template #default="{row}">
                  {{ convertStockoutTime(row.actualReturnTime) }}
                </template>
              </el-table-column>

              <el-table-column prop="returnReason" label="返厂原因" />

              <el-table-column prop="repairResult" label="维修结果" width="120">
                <template #default="{row}">
                  <el-tag :type="row.repairResult === '修复成功' ? 'success' : 'warning'">
                    {{ row.repairResult || '处理中' }}
                  </el-tag>
                </template>
              </el-table-column>

              <el-table-column label="物流信息" width="200">
                <template #default="{row}">
                  <div class="logistics-info">
                    <div>{{ row.logisticsCompany }}</div>
                    <div class="logistics-number">{{ row.logisticsNumber }}</div>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </el-collapse-item>
          <!-- 报废记录 -->
          <el-collapse-item title="报废记录" name="scrap">
            <el-descriptions :column="2" border v-if="lifecycleData.scrapRecord">
              <el-descriptions-item label="报废单号">{{ lifecycleData.scrapRecord.orderId }}</el-descriptions-item>
              <el-descriptions-item label="申请时间">{{ formatDateTime(lifecycleData.scrapRecord.applyTime) }}</el-descriptions-item>
              <el-descriptions-item label="报废原因">{{ lifecycleData.scrapRecord.scrapReason }}</el-descriptions-item>
              <el-descriptions-item label="处置方式">{{ lifecycleData.scrapRecord.disposalMethod }}</el-descriptions-item>
              <el-descriptions-item label="执行人">{{ lifecycleData.scrapRecord.executor }}</el-descriptions-item>
              <el-descriptions-item label="执行时间">{{ formatDateTime(lifecycleData.scrapRecord.scrapTime) }}</el-descriptions-item>
              <el-descriptions-item label="当前状态">
                <el-tag :type="getScrapStatusTagType(lifecycleData.scrapRecord.partStatus)">
                  {{ lifecycleData.scrapRecord.partStatus }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="损坏照片" :span="2">
                <div class="damage-photos">
                  <el-image
                      :src="lifecycleData.scrapRecord.damagePhoto"
                      :preview-src-list="[lifecycleData.scrapRecord.damagePhoto]"
                      fit="cover"
                      class="photo-thumbnail"
                  >
                    <template #error>
                      <div class="image-error">图片加载失败</div>
                    </template>
                  </el-image>
                </div>
              </el-descriptions-item>
            </el-descriptions>
            <el-empty v-else description="无报废记录" />
          </el-collapse-item>
        </el-collapse>
      </el-card>
    </div>
    <div class="inventory-section">
    <!-- 新增库存按钮 -->
    <el-button @click="openAddDailyDialog" type="primary" :icon="Plus">新增库存</el-button>
    <!-- 库存列表 -->
    <el-table :data="dailyList" stripe style="width: 100%">
      <el-table-column prop="partName" label="备件名称" />
      <el-table-column prop="locationName" label="库存名称" />
      <el-table-column prop="number" label="数量" />
      <!-- 新增安全库存列 -->
      <el-table-column prop="safetyStock" label="安全库存" width="120">
        <template #default="{row}">
          <el-tag :type="row.number < row.safetyStock ? 'danger' : 'success'">
            {{ row.safetyStock }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="库存状态" />
      <el-table-column fixed="right" label="操作" min-width="150">

        <template #default="{ row, $index }">
          <el-button @click="openEditDailyDialog(row)" type="warning" size="small">修改库存</el-button>
          <el-button @click="deleteDaily(row.inventoryId)" type="danger" size="small">删除库存</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增库存对话框 -->
    <el-dialog v-model="addDailyDialogVisible" title="新增库存" @close="clearAddDailyForm">
      <el-form :model="addDailyForm" ref="addDailyFormRef" label-width="100px">

        <el-form-item label="备件名称" prop="partName">
          <el-input v-model="addDailyForm.partName" placeholder="请输入备件编号"></el-input>
        </el-form-item>
        <el-form-item label="库位名称" prop="locationName">
          <el-input v-model="addDailyForm.locationName" placeholder="请输入库位编号"></el-input>
        </el-form-item>
        <!-- 新增库存对话框 -->
        <el-form-item label="安全库存" prop="safetyStock">
          <el-input-number
              v-model="addDailyForm.safetyStock"
              :min="0"
              :max="1000"
              placeholder="安全库存量"
          />
        </el-form-item>
      </el-form>
      <!--foot-->
      <div slot="footer" class="dialog-footer">
        <el-button @click="addDailyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddDaily">确定</el-button>
      </div>
    </el-dialog>

    <!-- 修改库存对话框 -->
    <el-dialog v-model="editDailyDialogVisible" title="修改库存" @close="clearEditDailyForm">
      <el-form :model="editDailyForm" ref="editDailyFormRef" label-width="100px">
        <el-form-item label="备件名称" prop="partName">
          <el-input v-model="editDailyForm.partName" placeholder="请输入备件编号"></el-input>
        </el-form-item>
        <el-form-item label="库位名称" prop="locationName">
          <el-input v-model="editDailyForm.locationName" placeholder="请输入库位编号"></el-input>
        </el-form-item>
        <!-- 修改库存对话框 -->
        <el-form-item label="安全库存" prop="safetyStock">
          <el-input-number
              v-model="editDailyForm.safetyStock"
              :min="0"
              :max="1000"
              placeholder="安全库存量"
          />
        </el-form-item>
      </el-form>
      <!--foot-->
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDailyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEditDaily">确定</el-button>
      </div>
    </el-dialog>
    </div>
  </div>

</template>

<script setup>
import {onMounted, ref} from 'vue';
import {ElMessage, ElMessageBox} from "element-plus";
import axios from "axios";
import router from "@/router.js";
import {Close, Plus, Search} from "@element-plus/icons-vue";
// 在script setup部分添加
import dayjs from 'dayjs'

const convertStockoutTime = (timeString) => {
  if (!timeString) return '';
  // 移除Z字符并解析为UTC时间
  const utcTime = dayjs.utc(timeString.replace('Z', ''));
  // 转换为北京时间（UTC+8）
  return utcTime.isValid() ?
      utcTime.add(8, 'hour').format('YYYY-MM-DD HH:mm:ss') :
      timeString;
};
const getLatestUpdateTime = () => {
  if (!lifecycleData.value) return null;

  // 收集所有可能的时间字段（包含嵌套数据）
  const timeCollector = [];

  // 基础信息时间
  if (lifecycleData.value.basicInfo) {
    timeCollector.push(
        lifecycleData.value.basicInfo.productionDate,
        lifecycleData.value.basicInfo.warrantyEnd
    );
  }

  // 入库信息
  if (lifecycleData.value.inboundInfo) {
    timeCollector.push(
        lifecycleData.value.inboundInfo.createdAt,
        lifecycleData.value.inboundInfo.qualityCheckTime
    );
  }

  // 故障记录
  if (lifecycleData.value.faultRecords) {
    lifecycleData.value.faultRecords.forEach(record => {
      timeCollector.push(
          record.faultTime,
          record.processedAt,
          record.reviewAt,
      );
    });
  }

  // 调拨记录
  if (lifecycleData.value.transferRecords) {
    lifecycleData.value.transferRecords.forEach(record => {
      timeCollector.push(
          record.createdAt,
          record.actualTransferTime
      );
    });
  }

  // 返厂记录
  if (lifecycleData.value.returnFactoryRecords) {
    lifecycleData.value.returnFactoryRecords.forEach(record => {
      timeCollector.push(
          record.applyTime,
          record.sentTime,
          record.receivedTime,
          record.actualReturnTime
      );
    });
  }

  // 报废记录
  if (lifecycleData.value.scrapRecord) {
    timeCollector.push(
        lifecycleData.value.scrapRecord.applyTime,
        lifecycleData.value.scrapRecord.approvalTime,
        lifecycleData.value.scrapRecord.scrapTime
    );
  }

  // 转换有效时间戳（带错误处理）
  const validTimestamps = timeCollector
      .flat() // 展平嵌套数组
      .filter(t => t) // 过滤空值
      .map(t => {
        try {
          // 统一预处理时间格式
          const str = String(t)
              .replace(/[年月]/g, '-')
              .replace(/[日时分秒]/g, '')
              .replace('T', ' ')
              .replace(/Z$/, '')
              .replace(/\.\d{3}$/, '');

          const d = dayjs(str);
          return d.isValid() ? d.valueOf() : null;
        } catch {
          return null;
        }
      })
      .filter(t => t !== null);



  if (validTimestamps.length === 0) return null;

  // 获取最新时间并格式化
  const maxTimestamp = Math.max(...validTimestamps);
  return dayjs(maxTimestamp).format('YYYY-MM-DD HH:mm:ss');
};
const getScrapStatusTagType = (status) => {
  const statusMap = {
    '待审核': 'warning',
    '已报废': 'success',
    '驳回': 'danger'
  }
  return statusMap[status] || 'info'
}
// 新增出入库类型样式映射
const getInoutTagType = (type) => {
  const typeMap = {
    '入库': 'success',
    '领用出库': 'danger',
    '返厂出库': 'warning',
    '调拨出库': 'info'
  }
  return typeMap[type] || 'info'
}
// 在文件顶部添加dayjs时区插件

import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'

dayjs.extend(utc)
dayjs.extend(timezone)
dayjs.tz.setDefault('Asia/Shanghai') // 设置默认时区为北京时间

// 修改后的时间格式化方法
const formatDateTime = (value) => {
  if (!value) return '无记录';

  const preprocessed = String(value)
      .replace(/[年月]/g, '-')
      .replace(/[日时分秒]/g, '')
      .replace('T', ' ')
      .replace(/Z$/, '')
      .replace(/\.\d{3}$/, '');

  const date = dayjs(preprocessed);
  return date.isValid() ? date.format('YYYY-MM-DD HH:mm:ss') : '无效日期';
}
const currentUser = ref({})
const dailyList = ref([]); // 库存数据列表
const addDailyDialogVisible = ref(false); // 新增库存对话框
const editDailyDialogVisible = ref(false); // 修改库存对话框

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
// 清空搜索
const handleClearSearch = () => {
  searchSn.value = ''
  lifecycleData.value = null
  activeCollapse.value = ['basic'] // 重置折叠面板状态
}
// 新增状态
const searchSn = ref('')
const lifecycleData = ref(null)
const activeCollapse = ref(['basic'])

// 新增查询方法
const handleSnSearch = async () => {
  if (!searchSn.value) {
    ElMessage.warning('请输入SN号')
    return
  }

  try {
    const response = await axios.get(`/lifecycle/${searchSn.value}`)
    lifecycleData.value = response.data


    // 数据整合逻辑
    lifecycleData.value.inoutRecords = [
      ...(lifecycleData.value.inboundInfo ? [{
        time: lifecycleData.value.inboundInfo.createdAt,
        type: '入库',
        detail: `采购单号：${lifecycleData.value.inboundInfo.orderId}`
      }] : []),
      // 领用出库记录
      ...(lifecycleData.value.stockoutRecords?.map(s => ({
        time: convertStockoutTime(s.outTime),
        type: '领用出库',
        detail: `领用单号：SO-${s.id}（仓库：${s.locationName}）`
      })) ?? []),

      // 返厂出库记录
      ...lifecycleData.value.returnFactoryRecords?.map(r => ({
        time: convertStockoutTime(r.sentTime), // 已转换时间
        type: '返厂出库',
        detail: `返厂单号：RT-${r.returnId}`
      })) ?? [],

      // 返厂入库记录
      ...lifecycleData.value.returnFactoryRecords?.map(r => ({
        time: convertStockoutTime(r.actualReturnTime), // 已转换时间
        type: '返厂入库',
        detail: `返厂单号：RT-${r.returnId}`
      })) ?? [],

      // 调拨出库记录（使用安全访问）
      ...(lifecycleData.value.transferRecords?.map(t => ({
        time: t.createdAt,
        type: '调拨出库',
        detail: `调拨单号：T-${t.transferId}`
      })) ?? [])
    ].sort((a, b) => new Date(b.time) - new Date(a.time));

  } catch (error) {
    ElMessage.error('查询失败: ' + error.message)
    lifecycleData.value = null
  }
}
// 新增库存表单
const addDailyForm = ref({

  partName: '',
  locationName: '',
  number: '',
  status: '在库',      // 状态（不需要用户输入，直接设为初始值）
  safetyStock: 5
});
// 在现有formatDateTime方法后添加
const formatReturnType = (type) => {
  const typeMap = {
    'REPAIR': '维修返厂',
    'RECALL': '召回返厂',
    'QUALITY_CHECK': '质检返厂'
  }
  return typeMap[type] || type
}
// 修改库存表单
const editDailyForm = ref({
  partName: '',
  locationName: '',
  number: '',
  status: '在库',      // 状态（不需要用户输入，直接设为初始值）
  safetyStock: 5
});

// 打开新增库存对话框
const openAddDailyDialog = () => {
  addDailyDialogVisible.value = true;
};
// 打开修改库存对话框
const openEditDailyDialog = (row) => {
  editDailyForm.value = {...row};
  editDailyDialogVisible.value = true;
};

// 清空新增库存表单
const clearAddDailyForm = () => {
  addDailyForm.value = {
    ...addDailyForm.value, // 保留已有值
    partName: '',
    locationName: '',

  };
};

// 清空修改库存表单
const clearEditDailyForm = () => {
  editDailyForm.value = {
    partName: '',
    locationName: '',
    safetyStock:'',
  };
};
// 修改获取数据方法
const getDailyList = async () => {
  try {
    const res = await axios.get("/inventory/x", {
      withCredentials: true
    })
    dailyList.value = res.data.list || res.data
    await checkLowStock() // 新增检查
  } catch (error) {
    ElMessage.error('获取库存列表失败: ' + error.message)
  }
}

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
    ElMessage.success('库存新增成功！');
    addDailyDialogVisible.value = false;
  } catch (error) {
    ElMessage.error('发送失败: ' + error.message)
  }
}



// 修改后的删除方法
const deleteDaily = async (inventoryId) => {
  try {
    // 显示确认对话框
    await ElMessageBox.confirm(
        '确定要删除该库存记录吗？',
        '警告',
        {
          confirmButtonText: '确认删除',
          cancelButtonText: '取消',
          type: 'warning',
          // 添加自定义类名（可选）
          customClass: 'delete-confirm-box'
        }
    )

    // 用户确认后执行删除
    const response = await axios.delete(
        `http://localhost:8080/inventory/${inventoryId}`,
        { withCredentials: true }
    )

    await getDailyList()
    if (response.data === 1) {
      ElMessage.success('删除成功！')
    } else {
      ElMessage.error("删除失败！")
    }
  } catch (error) {
    // 捕获取消操作的情况
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(`操作失败: ${error.response?.data?.message || error.message}`)
    }
  }
}
// 提交修改表单
const submitEditDaily = async () => {
  const response = await axios.put(`http://localhost:8080/inventory/${editDailyForm.value.inventoryId}`, editDailyForm.value);
  if (response.data === 1)
    ElMessage.success('修改成功！');
  else {
    ElMessage.error("修改失败");
  }
  await getDailyList()
  editDailyDialogVisible.value = false;
};
// 检查低库存方法
const checkLowStock = async () => {
  try {
    const res = await axios.get('/inventory/low-stock', {
      withCredentials: true
    })

    if (res.data.length > 0 && currentUser.value.role === '库管员') {
      ElMessageBox.confirm(
          `发现${res.data.length}项库存低于安全阈值，请及时处理：\n${
              res.data.map(i => `${i.partName}（当前：${i.number}，安全：${i.safetyStock}）`).join('\n')
          }`,
          '库存预警',
          {
            confirmButtonText: '创建采购单',
            cancelButtonText: '发起调拨',
            distinguishCancelAndClose: true,
            type: 'warning',
            customClass: 'stock-alert-box',
            closeOnClickModal: false
          }
      ).then(() => {
        router.push('/view/purchase')
      }).catch(action => {
        if (action === 'cancel') {
          router.push('/view/transferManagement')
        }
      })
    }
  } catch (error) {
    console.error('库存检查失败:', error)
  }
}


</script>
<style scoped>
/* 容器布局 */
.container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 20px;
}

/* 搜索区域美化 */
.search-section {
  margin-bottom: 24px;
}

.search-card {
  border-radius: 12px;
  background: linear-gradient(145deg, #f8faff, #f0f4ff);
  border: 1px solid var(--el-border-color-light);
}

.search-group {
  display: flex;
  gap: 12px;
}

.sn-input {
  flex: 1;
}

.button-group {
  display: flex;
  gap: 8px;
}

.search-btn {
  background: var(--el-color-primary);
  color: white;
  padding: 0 24px;
  transition: all 0.3s;

  &:hover {
    opacity: 0.9;
    transform: translateY(-1px);
  }
}

.clear-btn {
  background: var(--el-color-info-light-9);
  color: var(--el-color-info);
  border-color: var(--el-color-info-light-8);

  &:hover {
    background: var(--el-color-info-light-8);
  }
}

/* 结果卡片美化 */
.result-card {
  border-radius: 12px;
  margin-top: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--el-color-info-light-9);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sn-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.status-tag {
  font-weight: 500;
  letter-spacing: 0.5px;
}

.update-time {
  font-size: 13px;
}

/* 折叠面板优化 */
.smart-collapse {
  --el-collapse-border-color: transparent;

  :deep(.el-collapse-item__header) {
    font-weight: 500;
    color: var(--el-text-color-primary);
    padding: 0 16px;
    background: var(--el-fill-color-light);
  }

  :deep(.el-collapse-item__content) {
    padding: 16px;
  }
}

/* 库存表格区域 */
.inventory-section {
  margin-top: 32px;
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

/* 响应式优化 */
@media (max-width: 768px) {
  .container {
    padding: 12px;
  }

  .search-group {
    flex-direction: column;
  }

  .button-group {
    width: 100%;

    .el-button {
      flex: 1;
    }
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>

<!-- 全局样式优化 -->
<style>
.el-timeline-item__node {
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.el-descriptions__body {
  background: var(--el-fill-color-light) !important;
}

.el-table {
  --el-table-border-color: var(--el-border-color-light);
  --el-table-header-bg-color: var(--el-fill-color-light);
}
/* 返厂记录状态标签样式 */
.el-table .return-status-cell {
  .el-tag {
    font-weight: 500;
    letter-spacing: 0.5px;

    &--success {
      background: var(--el-color-success-light-9);
      border-color: var(--el-color-success-light-8);
    }

    &--warning {
      background: var(--el-color-warning-light-9);
      border-color: var(--el-color-warning-light-8);
    }
  }
}
/* 损坏照片样式 */
.damage-photos {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.photo-thumbnail {
  width: 200px;
  height: 150px;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.3s;

  &:hover {
    transform: translateY(-3px);
  }
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
}
/* 响应式表格调整 */
@media (max-width: 768px) {
  .el-table.return-table {
    .hidden-columns {
      display: none;
    }

    .tracking-number {
      max-width: 120px;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}
</style>