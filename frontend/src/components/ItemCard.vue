<template>
  <div>
    <div class="grid">
      <ProductCard
        v-for="(item, index) in paginatedProducts"
        :key="item.id || index"
        :product="item"
        @click="handleClickItem(item)"
      />

      <div v-if="paginatedProducts.length === 0" class="empty">暂无商品数据</div>
    </div>

    <div class="pagination-container">
      <div class="pagination">
        <button :disabled="page === 1" @click="page--" class="pager-btn">上一页</button>
        <div class="page-info">
          第
          <strong>{{ page }}</strong>
          / {{ totalPages || 1 }} 页
          <span class="total-count">(共 {{ products.length }} 条)</span>
        </div>

        <button :disabled="page >= totalPages" @click="page++" class="pager-btn">下一页</button>
      </div>
    </div>
  </div>
</template>

<script>
import ProductCard from "./ProductCard.vue";
import request from "@/utils/request";

export default {
  name: "ItemCard",
  components: { ProductCard },
  data() {
    return {
      products: [], // 原始全量数据
      page: 1, // 当前页码
      pageSize: 0 // 每页显示条数
    };
  },
  computed: {
    // 核心逻辑：计算当前页面应该显示的数据段
    paginatedProducts() {
      const start = (this.page - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.products.slice(start, end);
    },
    // 计算总页数
    totalPages() {
      return Math.ceil(this.products.length / this.pageSize);
    }
  },
  // ItemCard.vue 内部
  methods: {
    // 定义一个能接收参数的方法
    getItem(filterData = {}) {
      // 如果 filterData 为空，就是查全部；如果不为空，就是查分类
      const apiPath = filterData.groupTitle ? "/item/filter" : "/item/list";
      console.log(apiPath, filterData);
      request
        .post(apiPath, filterData)
        .then(res => {
          this.products = res || [];
        })
        .catch(err => {
          console.error("数据加载失败:", err);
        });
    },
    searchItem(keyword) {
      request
        .get("/item/search", {
          params: {
            keyword: keyword
          }
        })
        .then(res => {
          this.products = res.data || res || [];
          console.log("搜索成功，当前产品列表：", this.products);
        })
        .catch(err => {
          console.error("数据加载失败:", err);
          alert("无法获取商品列表，请检查后端服务是否已启动");
        });
    },
    updatePageSize() {
      const width = this.$el.querySelector(".grid").offsetWidth;
      const gap = 20; // 对应 CSS 中的 gap: 20px
      const minW = 350; // 对应 CSS 中的 minmax(350px, ...)

      // 2. 计算每行能放几个 (itemsPerRow)
      const itemsPerRow = Math.floor((width + gap) / (minW + gap));

      // 3. 计算 pageSize (假设每页显示 3 行)
      this.pageSize = itemsPerRow * 3;
    },
    // 点击商品跳转详情页
    handleClickItem(item) {
      this.$router.push({
        name: "ItemInfo",
        params: { id: item.id }
      });
    }
  },
  mounted() {
    this.getItem();
    this.updatePageSize(); // 初始化计算
    window.addEventListener("resize", this.updatePageSize);
  },
  beforeUnmount() {
    window.removeEventListener("resize", this.updatePageSize);
  }
};
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  padding: 12px;
  min-height: 400px; /* 防止翻页时容器高度塌陷 */
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  padding-bottom: 40px;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 15px;
  background: #f8f9fa;
  padding: 10px 20px;
  border-radius: 8px;
}

.pager-btn {
  padding: 6px 16px;
  cursor: pointer;
  background-color: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: all 0.2s;
}

.pager-btn:hover:not(:disabled) {
  color: #409eff;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.pager-btn:disabled {
  cursor: not-allowed;
  color: #c0c4cc;
  background-color: #f5f7fa;
}

.page-info {
  font-size: 14px;
  color: #606266;
}

.total-count {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}

.empty {
  grid-column: 1 / -1;
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>
