<template>
  <div class="ranking-card">
    <div class="ranking-header">
      <span class="fire-icon">🔥</span>
      <span class="ranking-title">热门搜索榜</span>
    </div>

    <div class="ranking-list">
      <div
        class="ranking-item"
        v-for="(item, index) in hotItems"
        :key="index"
      >
        <span :class="['rank-num', item.ranking <= 3 ? 'top-three' : '']">{{ item.ranking}}</span>
        <span class="item-name" @click="handleClickItem(item)">{{ item.title }}</span>
        <span class="hot-tag" v-if="index < 3">HOT</span>
      </div>
    </div>
  </div>
</template>

<script>
import request from "@/utils/request";

export default {
  name: "ItemRanking",
  data() {
    return {
      // 模拟数据，后期你可以从后端接口获取
      hotItems: []
    };
  },
  methods: {
    getHotItems() {
      request
        .get("/item/ranking")
        .then(res => {
          this.hotItems = res || [];
          console.log("获取热门搜索榜成功:", res);
        })
        .catch(err => {
          console.error("获取热门搜索榜失败:", err);
        });
    },
    handleClickItem(item) {
      this.$router.push({
      name: 'ItemInfo',
      params: { id: item.id }
    });
  }
  },
  mounted() {
    this.getHotItems();
  }
};
</script>


<style scoped>
.ranking-card {
  margin-left: 80px; /* 增加左边距，让它别死贴左边 */
  width: 280px;
  background: rgba(255, 255, 255, 0.9); /* 半透明白色 */
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08); /* 柔和阴影 */
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: transform 0.3s ease;
}

.ranking-card:hover {
  transform: translateY(-5px); /* 悬浮稍微往上飘 */
}

.ranking-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.ranking-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  letter-spacing: 1px;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 4px 0;
  transition: all 0.2s;
}

.ranking-item:hover {
  padding-left: 5px; /* 悬浮时向右微动 */
  color: #3a8eff;
}

/* 排名数字样式 */
.rank-num {
  font-size: 16px;
  font-weight: 800;
  color: #999;
  font-style: italic;

  /* --- 关键修改 --- */
  width: 28px; /* 宽度稍微给大一点，确保 1 和 10 占地一样大 */
  display: inline-block; /* 确保宽度生效 */
  text-align: center; /* 让数字居中，或者 left 靠左 */
  flex-shrink: 0; /* 防止数字在空间不足时被挤压 */
  /* ---------------- */
}

/* 前三名的数字颜色高亮 */
.top-three {
  color: #ff4d4f; /* 红色 */
  font-size: 18px;
}

.item-name {
  font-size: 14px;
  color: #444;
  flex: 1;

  /* --- 关键修改 --- */
  white-space: nowrap; /* 强制不换行 */
  overflow: hidden; /* 隐藏溢出内容 */
  text-overflow: ellipsis; /* 显示省略号 ... */
  text-align: left; /* 确保左对齐 */
}

.hot-tag {
  font-size: 10px;
  background: #ff4d4f;
  color: white;
  padding: 1px 4px;
  border-radius: 4px;
  font-weight: bold;
}
</style>