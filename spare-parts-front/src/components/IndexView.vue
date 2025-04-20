<template>
  <!-- 在侧边栏模板顶部添加加载状态 -->
  <template v-if="loading" class="loading-mask">
    <el-icon class="is-loading"><Loading /></el-icon>
  </template>
  <template v-else>
    <div class="app-container">
      <!-- 侧边栏 -->
      <div class="sidebar">
        <div class="sidebar-header">
          <h2 class="welcome-message">欢迎 {{ user.name }}!</h2>
        </div>

        <!-- 库管员菜单 -->
        <div v-if="user.role === '库管员'" class="menu-container">
          <router-link to="/view/purchase" class="menu-item">
            <el-icon><Goods /></el-icon>
            <span class="menu-text">采购管理</span>
          </router-link>

          <router-link to="/view/inventory" class="menu-item">
            <el-icon><Box /></el-icon>
            <span class="menu-text">库存管理</span>
          </router-link>

          <router-link to="/view/transferManagement" class="menu-item">
            <el-icon><Connection  /></el-icon>
            <span class="menu-text">调拨申请</span>
          </router-link>

          <router-link to="/view/UsageRequestReview" class="menu-item">
            <el-icon><DocumentChecked /></el-icon>
            <span class="menu-text">领用审核</span>
          </router-link>

          <router-link to="/view/inbound" class="menu-item">
            <el-icon><Download /></el-icon>
            <span class="menu-text">备件入库</span>
          </router-link>
          <!-- 新增退出登录 -->
          <div class="logout-item" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span class="menu-text">退出登录</span>
          </div>
        </div>

        <!-- 管理员菜单 -->
        <div v-else-if="user.role === '管理员'" class="menu-container">
          <div class="menu-list">
          <el-menu
              router
              :default-active="$route.path"
              class="side-menu"
              background-color="#fff"
              text-color="#606266"
              active-text-color="#409eff"
          >
            <!-- 基础信息子菜单 -->
            <el-sub-menu index="basic-info">
              <template #title>
                <el-icon><FolderOpened /></el-icon>
                <span>基础信息</span>
              </template>

              <el-menu-item index="/view/warehouse">
                <el-icon><MapLocation /></el-icon>
                <span>仓库信息</span>
              </el-menu-item>

              <el-menu-item index="/view/sparepart">
                <el-icon><Collection /></el-icon>
                <span>备件信息</span>
              </el-menu-item>

              <el-menu-item index="/view/inventory">
                <el-icon><Box /></el-icon>
                <span>库存管理</span>
              </el-menu-item>
            </el-sub-menu>
          </el-menu>
            <!-- 调拨审核 -->
            <router-link to="/view/transferManagement1" class="menu-item">
              <el-icon><Connection /></el-icon>
              <span class="menu-text">调拨审核</span>
            </router-link>


          <!-- 退出登录 -->
              <div class="logout-item" @click="handleLogout">
                   <el-icon><SwitchButton /></el-icon>
                   <span class="menu-text">退出登录</span>
               </div>
          </div>
        </div>

        <!-- 现场工程师菜单 -->
        <div v-else-if="user.role === '现场工程师'" class="menu-container">
          <router-link to="/view/UsageRequestApply" class="menu-item">
            <el-icon><Tickets /></el-icon>
            <span class="menu-text">领用申请</span>
          </router-link>

          <router-link to="/view/" class="menu-item">
            <el-icon><RefreshLeft /></el-icon>
            <span class="menu-text">备件返还</span>
          </router-link>
          <!-- 新增退出登录 -->
          <div class="logout-item" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span class="menu-text">退出登录</span>
          </div>
        </div>
      </div>

          <div class="main-content">
        <router-view></router-view>
      </div>
    </div>
  </template>
</template>

<script setup>
import {
  Loading,
  Goods,
  Box,
  Connection,
  DocumentChecked,
  Download,
  MapLocation,
  Collection,
  Tickets,
  RefreshLeft, SwitchButton, FolderOpened
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
  position: relative; /* 添加定位上下文 */
}

/* 侧边栏固定定位 */
.sidebar {
  width: 240px;
  background: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 1000;
  overflow-y: auto;
}
.menu-item,
:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  display: flex;
  align-items: center;
  padding: 12px 24px;
  margin: 4px 12px;
  border-radius: 6px;
  color: #606266;
  transition: all 0.2s ease;
  text-decoration: none !important;
}

/* 统一悬停效果 - 修改颜色部分 */
.menu-item:hover,
:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: #f5f7fa !important;
  color: #409eff !important;  /* 修改为蓝色 */
  transform: translateX(4px);
}


/* 统一激活状态 */
.menu-item.router-link-exact-active,
:deep(.el-menu-item.is-active) {
  background: #ecf5ff !important;
  color: #409eff !important;
}

/* 新增.logout-item到共享样式 */
.menu-item,
:deep(.el-menu-item),
:deep(.el-sub-menu__title),
.logout-item {
  display
  : flex;
  align-items
  : center;
  padding: 12px 24px
;
  margin: 4px 12px
;
  border-radius: 6px
;
  color: #606266
;
  transition: all 0.2s
  ease;
  text-decoration: none !important
;
  cursor
  : pointer;
}
/* 退出登录单独样式 */
.logout-item:hover {
  background: #fef0f0 !important;
  color: #f56c6c !important;  /* 保持红色警示色 */
  transform: translateX(4px);
}

/* 子菜单项缩进 */
:deep(.el-menu--inline .el-menu-item) {
  padding-left: 48px !important;
}

/* 统一退出登录样式 */
.logout-item {
  position: sticky;
  bottom: 20px;
  margin: 16px 12px;
  background: #fef0f0;
  color: #f56c6c;
  z-index: 1;
}
/* 调整退出按钮定位 */
.menu-container {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 60px; /* 为退出按钮留出空间 */
}
/* 移动端适配 */
@media (max-width: 768px) {
  .menu-item,
  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    padding: 10px 16px;
    margin: 4px 8px;
  }

  :deep(.el-menu--inline .el-menu-item) {
    padding-left: 40px !important;
  }
}
/* 主内容区适配 */
.main-content {
  flex: 1;
  margin-left: 240px; /* 与侧边栏宽度一致 */
  min-height: 100vh;
  background: #f8f9fc;
  padding: 24px;
  transition: margin 0.3s;
}
</style>