<template>
  <div class="sort-nav-container">
    <div 
      v-for="(group, index) in categoryList" 
      :key="index" 
      class="select-wrapper"
    >
      <select 
        class="sort-select" 
        @change.stop.prevent="handleSelect($event, group.name)"
      >
        <option value="" disabled selected hidden>{{ group.icon }} {{ group.name }}</option>
        <option 
          v-for="item in group.categoryItems" 
          :key="item.id" 
          :value="item.id"
        >
          {{ item.itemName }}
        </option>
      </select>
      
      <span class="arrow-icon">▼</span>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request';

export default {
  name: "SortNav",
  data() {
    return {
      // 绑定一个永远为空的值，用于每次选完后自动重置 select
      // resetValue: "",
      
      // 核心数据配置：以后改分类直接改这里，或者从后端 API 获取
      categoryList: []
    };
  },
  methods: {
    handleSelect(event, groupTitle) {
      const selectedItem = event.target.value;
      console.log("用户选择了:", selectedItem);
      if (!selectedItem) return;

      // 触发父组件事件，传递分类信息
      // 输出格式示例：{ group: "数码电子", item: "手机/平板" }
      this.$emit("filter-change", {
        groupTitle: groupTitle,
        selectedItem: selectedItem
      });
      event.target.selectedIndex = 0;
      console.log(`用户选择了 [${groupTitle}]: ${selectedItem}`);
    },
    getCategoryList() {
      request.get("/item/category").then(res => {
        this.categoryList = res || [];
        console.log("数据加载成功:", res);
      }).catch(err => {
        console.error("数据加载失败:", err);
      });

    },
    reset() {
    this.activeCategory = null; 
  }

  },
  mounted() {
    this.getCategoryList();
  }
};
</script>

<style scoped>
/* 容器布局 */
.sort-nav-container {
  width: 100%;
  height: 60px;
  display: flex;
  justify-content: space-around; /* 均匀分布 */
  align-items: center;
  background-color: #ffffff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05); /* 加一点阴影更有质感 */
  padding: 0 10px;
}

/* 每个 Select 的包装器 (为了放自定义箭头) */
.select-wrapper {
  position: relative;
  flex: 1; /* 等分宽度 */
  margin: 0 4px;
  height: 36px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

/* 鼠标悬停在容器上时的效果 */
.select-wrapper:hover {
  background-color: #ecf5ff; /* 浅蓝背景 */
  transform: translateY(-2px); /* 轻微上浮 */
}

.select-wrapper:hover .sort-select {
  color: #409eff; /* 文字变蓝 */
}
.select-wrapper:hover .arrow-icon {
  color: #409eff;
}

/* 原生 Select 样式重置 */
.sort-select {
  width: 100%;
  height: 100%;
  border: none;
  background: transparent;
  appearance: none; /* 隐藏原生箭头 */
  -webkit-appearance: none;
  -moz-appearance: none;
  
  padding: 0 20px 0 10px; /* 右边留出箭头的位置 */
  font-size: 14px;
  font-weight: 600;
  color: #333;
  cursor: pointer;
  outline: none;
  text-align-last: center; /* 让文字居中 */
}

/* 自定义小箭头 */
.arrow-icon {
  position: absolute;
  right: 4px;
  top: 50%;
  transform: translateY(-50%) scale(0.6);
  pointer-events: none; /* 让点击穿透箭头直接点到 select */
  color: #c0c4cc;
  transition: color 0.3s;
}

/* 选项样式 (部分浏览器有效) */
.sort-select option {
  color: #333;
  background: white;
  padding: 10px;
  font-weight: normal;
}
</style>