<template>
  <div class="page-wrapper">
    <TopNavBar />

    <div class="main-content">
      <div class="cart-container">
        <div class="cart-title">
          购物车
          <span class="title-count">(2)</span>
        </div>

        <div class="cart-layout">
          <div class="cart-item-container" v-for="item in cartItems" :key="item.id">
            <div class="cart-item">
              <div class="cart-item-check">
                <div class="cart-item-check" @click="item.checked = !item.checked">
                  <input type="checkbox" v-model="item.checked" style="display: none;" />
                  <div class="custom-checkbox" :class="{ checked: item.checked }"></div>
                </div>
              </div>

              <div class="cart-item-info">
                <div class="cart-item-img">
                  <img :src="item.imageURL" alt="商品图" />
                </div>

                <div class="cart-item-details">
                  <div class="details-top">
                    <div class="cart-item-name">{{ item.title }}</div>
                    <div class="cart-item-remove" @click="removeCartItem(item.itemId)">✕</div>
                  </div>

                  <div class="details-bottom">
                    <div class="cart-item-price">¥ {{ item.price }}</div>
                    <div class="cart-item-count">
                      <button
                        class="btn-count"
                        @click="handleCountChange(item.itemId, -1)"
                        :disabled="item.count < 1"
                      >-</button>
                      <input
                        type="text"
                        class="count-display"
                        v-model="item.count"
                        @change="updateCartItemCount(item.itemId, item.count)"
                      />
                      <button
                        class="btn-count"
                        @click="handleCountChange(item.itemId, 1)"
                        :disabled="item.count >= 99"
                      >+</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="cart-summary-wrapper">
            <div class="cart-summary-card">
              <div class="summary-title">订单摘要</div>
              <div class="cart-total-container">
                <div class="summary-row">
                  <span>商品总额</span>
                  <span>{{ computeTotalPrice }}</span>
                </div>
                <!-- <div class="summary-row">
                  <span>运费</span>
                  <span class="free-text">免费</span>
                </div>-->
                <div class="divider"></div>
                <div class="summary-row total-row">
                  <span>总计</span>
                  <span>{{ computeTotalPrice }}元</span>
                </div>
              </div>
              <div class="cart-checkout-container">
                <button class="checkout-btn">立即结算</button>
              </div>
            </div>
            <div class="secure-info">🛡️ 交易受安全协议保护</div>
          </div>
        </div>

        <div class="cart-empty-container" style="display: none;">
          <div class="empty-icon">🛒</div>
          <p>购物车空空如也</p>
          <button class="go-shopping">去逛逛</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import TopNavBar from "../components/TopNavBar.vue";
import request from "../utils/request";

export default {
  name: "ItemCart",
  components: {
    TopNavBar
  },
  computed: {
    computeTotalPrice() {
      let total = 0;
      this.cartItems.forEach(item => {
        if (item.checked) {
          total += item.price * item.count;
        }
      });
      return total;
    }
  },
  data() {
    return {
      cartItems: [],
      totalPrice: 0
    };
  },
  methods: {
    getCartItems() {
      request
        .get("/cart")
        .then(res => {
          // res 是后端返回的新数据
          this.cartItems = res.map(newItem => {
            // 1. 在当前的 cartItems 数组中寻找对应的旧项
            const oldItem = this.cartItems.find(
              i => i.itemId === newItem.itemId
            );
            return {
              ...newItem,
              // 2. 如果找到了旧项，就用旧项的 checked 状态；找不到（说明是新加的）就用后端返回的
              checked: oldItem ? oldItem.checked : newItem.checked == 1
            };
          });
          console.log("数据加载成功，已保留当前勾选状态");
        })
        .catch(err => {
          console.error("获取购物车商品失败:", err);
        });
    },
    updateCartItemCount(itemId, count) {
      request
        .post("/cart/update", null, {
          params: {
            itemId: itemId,
            count: count
          }
        })
        .then(() => {
          this.getCartItems();
          console.log("更新购物车商品成功！");
        })
        .catch(err => {
          console.error("更新购物车商品失败:", err);
        });
    },
    removeCartItem(itemId) {
      request
        .post("/cart/delete", null, {
          params: {
            itemId: itemId
          }
        })
        .then(() => {
          this.getCartItems();
          console.log("删除购物车商品成功！");
        })
        .catch(err => {
          console.error("删除购物车商品失败:", err);
        });
    },
    handleCountChange(itemId, count) {
      const item = this.cartItems.find(i => i.itemId === itemId);
      if (item) {
        item.count += count;
        this.updateCartItemCount(itemId, item.count);
      }
    }
  },
  mounted() {
    this.getCartItems();
  }
};
</script>

<style scoped>
/* 全局页面样式 */
.page-wrapper {
  background-color: #f5f5f7; /* Apple 风格浅灰 */
  min-height: 100vh;
  color: #1d1d1f;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", sans-serif;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.cart-title {
  font-size: 25px;
  font-weight: 700;
  margin-bottom: 32px;
  text-align: left;
}
.title-count {
  color: #86868b;
  font-weight: 400;
  font-size: 24px;
}

/* 布局结构 */
.cart-layout {
  display: flex;
  gap: 12px;
  flex-direction: column;
  justify-content: flex-start;
}

/* 左侧列表 */
.cart-item-container {
  flex: 2;
}

.cart-item {
  background: #ffffff;
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  transition: transform 0.2s;
}

/* 自定义复选框 */
.custom-checkbox {
  width: 24px;
  height: 24px;
  border: 2px solid #d2d2d7;
  border-radius: 50%;
  margin-right: 20px;
  cursor: pointer;
}
.custom-checkbox.checked {
  background: #0071e3;
  border-color: #0071e3;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='white'%3E%3Cpath d='M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z'/%3E%3C/svg%3E");
  background-size: 14px;
  background-repeat: no-repeat;
  background-position: center;
}

/* 内容信息 */
.cart-item-info {
  flex: 1;
  display: flex;
  gap: 24px;
}

.cart-item-img {
  width: 120px;
  height: 120px;
  background: #fbfbfd;
  border-radius: 14px;
  overflow: hidden;
}
.cart-item-img img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.cart-item-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.details-top {
  display: flex;
  justify-content: space-between;
}
.cart-item-name {
  font-size: 18px;
  font-weight: 600;
  text-align: left;
}
.cart-item-remove {
  color: #86868b;
  cursor: pointer;
  font-size: 20px;
  padding: 0 5px;
}

.details-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.cart-item-price {
  font-size: 22px;
  font-weight: 700;
  color: #ff4d4f;
}

/* 数量加减组件 */
.cart-item-count {
  display: flex;
  align-items: center;
  background: #f5f5f7;
  border-radius: 12px;
  padding: 4px;
  border: 1px solid #e8e8ed;
}
.btn-count {
  width: 32px;
  height: 32px;
  border: none;
  background: white;
  border-radius: 8px;
  color: #0071e3;
  font-size: 20px;
  font-weight: 600;
  align-items: center;
  cursor: pointer;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}
.count-display {
  width: 45px;
  border: none;
  background: transparent;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  outline: none;
}

/* 右侧结算区域 */
.cart-summary-wrapper {
  flex: 1;
  position: sticky;
  top: 40px;
}
.cart-summary-card {
  background: #ffffff;
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  text-align: left;
}
.summary-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 24px;
}
.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  font-size: 16px;
  color: #424245;
}
.free-text {
  color: #34c759;
  font-weight: 600;
}
.divider {
  height: 1px;
  background: #d2d2d7;
  margin: 20px 0;
}
.total-row {
  font-size: 24px;
  font-weight: 700;
  color: #1d1d1f;
  margin-top: 10px;
}

.checkout-btn {
  width: 100%;
  background: #0071e3;
  color: #ffffff;
  border: none;
  padding: 18px;
  border-radius: 14px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 24px;
  transition: background 0.2s;
}
.checkout-btn:hover {
  background: #0077ed;
}

.secure-info {
  margin-top: 16px;
  font-size: 13px;
  color: #86868b;
  text-align: center;
}

/* 响应式：屏幕窄时改为纵向 */
@media (max-width: 900px) {
  .cart-layout {
    flex-direction: column;
  }
  .cart-summary-wrapper {
    width: 100%;
    position: static;
  }
  .cart-item-info {
    flex-direction: column;
    gap: 12px;
  }
  .cart-item-img {
    width: 100%;
    height: 200px;
  }
}
</style>