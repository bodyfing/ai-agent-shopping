<template>
  <nav class="nav-container">
    <div class="nav-content">
      <div class="logo" @click="handleSelectChange">
        <img src="../assets/GooShare.png" class="logo-icon" />
        <span class="logo-text">GooShare校园二手交易平台</span>
      </div>

      <div class="right-menu">
        <template v-if="token">
          <!-- <div class="menu-item">消息</div> -->

          <div class="user-pill">
            <span class="username">欢迎，{{ user ? user.username : '同学' }}</span>
          </div>

          <div class="menu-item" @click="$router.push('/itemCart')">购物车</div>
          <!-- <div class="menu-item" @click="$router.push('/collection')">我的收藏</div> -->
          <div class="menu-item" @click="$router.push('/inboxPrompt')">消息推送</div>
          <div class="menu-item" @click="$router.push('/publishItem')">我的发布</div>

          <div class="divider"></div>

          <div class="menu-item logout-link" @click="handleLogout">退出登录</div>
        </template>

        <template v-else>
          <div class="menu-item login-link" @click="$router.push('/login')">登录</div>
          <button class="register-btn" @click="$router.push('/register')">加入我们</button>
        </template>
      </div>
    </div>
  </nav>
</template>

<script>
export default {
  name: "TopNavBar",
  data() {
    return {
      // 响应式数据：直接从缓存读取
      user: JSON.parse(localStorage.getItem("userInfo")) || null,
      token: localStorage.getItem("token") || null
    };
  },
  methods: {
    // 返回首页并触发父组件刷新数据
    handleSelectChange() {
      this.$emit("return-change");
      if (this.$route.path !== "/") {
        this.$router.push("/");
      }
    },
    // 处理退出登录
    handleLogout() {
      if (!window.confirm("确定要退出登录吗？")) return;

      // 1. 清除本地缓存
      localStorage.removeItem("userInfo");
      localStorage.removeItem("token");

      // 2. 立即更新组件内部状态（实现无需刷新的 UI 更新）
      this.user = null;
      this.token = null;

      // 3. 跳转回首页
      if (this.$route.path !== "/") {
        this.$router.push("/");
      } else {
        // 如果已经在首页，手动触发一次父组件的数据刷新（清除点赞红心等状态）
        this.$emit("return-change");
      }
    }
  },
  // 监听路由变化，防止在其他页面登录后，导航栏没更新
  watch: {
    $route() {
      this.user = JSON.parse(localStorage.getItem("userInfo")) || null;
      this.token = localStorage.getItem("token") || null;
    }
  }
};
</script>

<style scoped>
/* 导航容器：毛玻璃效果 */
.nav-container {
  width: 100%;
  height: 52px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(15px); /* 现代毛玻璃质感 */
  -webkit-backdrop-filter: blur(15px);
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: center;
  transition: all 0.3s ease;
}

.nav-content {
  width: 100%;
  max-width: 1200px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
}

/* Logo 样式 */
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}
.logo-icon {
  width: 40px;
  height: 40px;
  background: white;
  overflow: hidden;
  object-fit: cover;
}
.logo-text {
  font-size: 17px;
  font-weight: 600;
  color: #1d1d1f; /* Apple 经典深灰 */
  letter-spacing: -0.2px;
}

/* 右侧菜单 */
.right-menu {
  display: flex;
  align-items: center;
  gap: 22px;
}

.menu-item {
  font-size: 13px;
  color: #515154;
  cursor: pointer;
  font-weight: 400;
  transition: all 0.2s ease;
}
.menu-item:hover {
  color: #0071e3; /* 经典蓝色交互 */
}

/* 用户欢迎语药丸设计 */
.user-pill {
  background: #f5f5f7;
  padding: 5px 12px;
  border-radius: 15px;
  display: flex;
  align-items: center;
}
.username {
  font-size: 12px;
  color: #1d1d1f;
  font-weight: 500;
}

/* 分割线 */
.divider {
  width: 1px;
  height: 14px;
  background: #d2d2d7;
}

/* 退出登录 */
.logout-link {
  color: #86868b;
}
.logout-link:hover {
  color: #ff3b30; /* 危险操作红色提示 */
}

/* 登录/注册按钮 */
.login-link {
  color: #0071e3;
  font-weight: 500;
}

.register-btn {
  background: #0071e3;
  color: #fff;
  border: none;
  padding: 6px 14px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.1s active;
}
.register-btn:hover {
  background: #0077ed;
}
.register-btn:active {
  transform: scale(0.96);
}
</style>