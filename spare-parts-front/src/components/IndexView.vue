<template>
  <!-- 在侧边栏模板顶部添加加载状态 -->
  <template v-if="loading" class="loading-mask">
    <el-icon class="is-loading"><Loading /></el-icon>
  </template>
  <template v-else>

      <!-- 侧边栏 -->
      <div class="sidebar">
        <div class="sidebar-header">
          <h2 class="welcome-message">欢迎 {{ user.name }}!</h2>
        </div>

        <!-- 库管员菜单 -->
        <div v-if="user.role === '库管员'" class="menu-container">
          <router-link to="/view/purchase" class="menu-item">
            <el-icon><ChromeFilled /></el-icon>
            <span class="menu-text">采购管理</span>
          </router-link>
          <router-link to="/view/inventory" class="menu-item">
            <el-icon><ChromeFilled /></el-icon>
            <span class="menu-text">库存管理</span>
          </router-link>
          <router-link to="/view/transferManagement" class="menu-item">
            <el-icon><ChromeFilled /></el-icon>
            <span class="menu-text">调拨申请</span>
          </router-link>

          <div class="menu-item logout" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span class="menu-text">退出登录</span>
          </div>
        </div>

        <!-- 管理员菜单 -->
        <div v-else-if="user.role === '管理员'" class="menu-container">
          <router-link to="/view/warehouse" class="menu-item">
            <el-icon><SuitcaseLine /></el-icon>
            <span class="menu-text">库位信息</span>
          </router-link>
          <router-link to="/view/transferManagement1" class="menu-item">
            <el-icon><ChromeFilled /></el-icon>
            <span class="menu-text">调拨审核</span>
          </router-link>


          <!-- 修改退出登录按钮（所有菜单中） -->
          <div class="menu-item logout" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span class="menu-text">退出登录</span>
          </div>
        </div>

<!--        &lt;!&ndash; 评分员菜单 &ndash;&gt;-->
<!--        <div v-else-if="user.usercategory === '2'" class="menu-container">-->
<!--          <router-link to="/view/ping" class="menu-item">-->
<!--            <el-icon><SuitcaseLine /></el-icon>-->
<!--            <span class="menu-text">日报评分</span>-->
<!--          </router-link>-->

<!--          <router-link to="/view/tong" class="menu-item">-->
<!--            <el-icon><DocumentChecked /></el-icon>-->
<!--            <span class="menu-text">发表情况统计</span>-->
<!--          </router-link>-->

<!--          <router-link to="/view/xiao" class="menu-item">-->
<!--            <el-icon><Edit /></el-icon>-->
<!--            <span class="menu-text">通知学生</span>-->
<!--          </router-link>-->

<!--          &lt;!&ndash; 修改退出登录按钮（所有菜单中） &ndash;&gt;-->
<!--          <div class="menu-item logout" @click="handleLogout">-->
<!--            <el-icon><SwitchButton /></el-icon>-->
<!--            <span class="menu-text">退出登录</span>-->
<!--          </div>-->
        </div>


      <div class="main-content">
        <router-view></router-view>
      </div>
  </template>
</template>

<script setup>
import {
  Bell, ChatLineRound, Checked,
  ChromeFilled, DataAnalysis, DataLine, Document,
  DocumentChecked,
  Edit, EditPen,
  Loading, Lock,
  Message, Notification, Promotion, Refresh,
  SuitcaseLine,
  SwitchButton
} from "@element-plus/icons-vue";
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 用户信息响应式处理
const loading = ref(true);
const userData = ref(null); // 避免命名冲突

// 退出登录处理
const handleLogout = () => {
  sessionStorage.removeItem('user');
  // 强制清除内存数据并刷新
  router.push('/').then(() => {
    window.location.reload(true); // 添加强制刷新参数
  });
};

// 用户信息初始化
onMounted(async () => {
  try {
    // 添加延迟确保sessionStorage写入
    await new Promise(resolve => setTimeout(resolve, 50));
    userData.value = JSON.parse(sessionStorage.getItem('user')) || {};
  } catch (e) {
    userData.value = {};
  } finally {
    loading.value = false;
  }
});

// 计算属性包装
const user = computed(() => userData.value);
</script>

<style scoped>
.app-container {
  display: flex;
  min-height: 100vh;
}
.welcome-panel {
  max-width: 1200px;
  margin: 20px auto;
}

.panel-header {
  display: flex;
  align-items: center;
  padding: 12px 0;
}

.role-tag {
  text-align: center;
  margin: 20px 0;
}

.function-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
  margin: 20px 0;
}

.function-card {
  text-align: center;
  transition: transform 0.3s;
}

.function-card:hover {
  transform: translateY(-5px);
}

.feature-row {
  margin-top: 20px;
}

.feature-card {
  height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.feature-card h4 {
  margin: 10px 0;
}

.feature-card p {
  color: #909399;
  font-size: 0.9em;
}

/* 侧边栏样式 */
.sidebar {
  width: 240px;
  background: #ffffff;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  padding: 20px 0;
  position: fixed;
  height: 100%;
}

.sidebar-header {
  padding: 0 20px 20px;
  border-bottom: 1px solid #eee;
}

.welcome-message {
  font-size: 18px;
  color: #333;
  margin: 0;
}

.menu-container {
  padding: 15px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 25px;
  color: #5a5e66;
  text-decoration: none;
  transition: all 0.3s;
  margin: 4px 0;
}

.menu-item:hover {
  background: #f5f7fa;
  color: #409eff;
  transform: translateX(5px);
}

.menu-item.router-link-exact-active {
  background: #ecf5ff;
  color: #409eff;
  border-right: 3px solid #409eff;
}

.menu-item i {
  font-size: 18px;
  width: 24px;
}

.menu-text {
  margin-left: 12px;
  font-size: 14px;
}

.logout {

  bottom: 20px;
  width: calc(100% - 40px);
}

.main-content {
  flex: 1;
  margin-left: 240px;
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

@media (max-width: 768px) {
  .sidebar {
    width: 180px;
  }

  .main-content {
    margin-left: 180px;
    padding: 15px;
  }

  .menu-text {
    font-size: 13px;
  }
}
</style>