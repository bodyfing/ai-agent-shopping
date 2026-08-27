<template>
  <div class="page-wrapper">
    <TopNavBar />

    <div class="main-content">
      <div class="item-card">
        <div class="image-section">
          <div class="image-wrapper">
            <img :src="item.imageURL || require('../assets/logo.png')" alt="商品图片" />
          </div>
        </div>

        <div class="info-section">
          <div class="info-header">
            <h1 class="title">
              {{ item.title || '正在加载标题...' }}
              <span class="like-wrapper">
                <img :src="require('../assets/浏览.png')" class="browser-icon" alt="浏览" />
                <span class="browser-count">{{ item.browserCount || 0 }}</span>
                <img
                  :src="item.liked ? require('../assets/已赞.png') : require('../assets/点赞.png')"
                  class="like-icon"
                  alt="喜欢"
                  @click="handleLike(item.id)"
                />
                <span class="like-count">{{ item.likeCount || 0 }}</span>
                <img
                  :src="item.collected ? require('../assets/已收藏.png') : require('../assets/收藏.png')"
                  class="collect-icon"
                  alt="收藏"
                  @click="handleCollect(item.id)"
                />
                <span class="collect-count">{{ item.collectCount || 0 }}</span>
              </span>
            </h1>

            <div class="price-box">
              <span class="currency">¥</span>
              <span class="price-num">{{ item.price || '0.00' }}</span>
              <span class="stock">库存：{{ item.stock || 0 }}</span>
            </div>
          </div>

          <div class="divider"></div>

          <div class="detail-grid">
            <div class="detail-row">
              <span class="label">卖家</span>
              <div class="value-wrapper">
                <div class="mini-avatar">
                  <img :src="item.avatar || require('../assets/default-avatar.png')" alt="卖家头像" />
                </div>
                <span class="value">{{ item.seller || '校友匿名' }}</span>
                <button
                  class="follow-btn"
                  :class="{'active': item.isFollowed}"
                  @click="handleFollow(item.sellerId,item.isFollowed,1)"
                ></button>
                <span
                  v-if="followers && followers.length > 0"
                  style="font-size: 12px; color: #86868b; font-weight: 400;"
                >
                  你的关注
                  <span v-for="(name, index) in followers.slice(0, 3)" :key="name.id">
                    <span style="color: #0071e3; font-weight: 500;">{{ name }}</span>
                    <span v-if="index < Math.min(followers.length, 3) - 1">、</span>
                  </span>
                  <span v-if="followers.length > 3">等 {{ followers.length }} 人</span>
                  也关注了该卖家
                </span>
              </div>
            </div>

            <div class="detail-row">
              <span class="label">地点</span>
              <span class="value">{{ item.location || '校本部' }}</span>
            </div>

            <div class="detail-row">
              <span class="label">发布时间</span>
              <span class="value">{{ item.createTime || '刚刚' }}</span>
            </div>
          </div>

          <div class="description-box">
            <h3 class="section-title">商品描述</h3>
            <p class="description-text">{{ item.description || '暂无详细描述。' }}</p>
          </div>

          <div class="coupon-grid">
            <div v-for="coupon in couponList" :key="coupon.id" class="seckill-coupon">
              <div class="coupon-main">
                <div class="coupon-price">
                  <span class="currency">¥</span>
                  <span class="num">{{ coupon.value }}</span>
                  <span
                    :class="{'seckill-tag': coupon.couponType == 2, 'full-tag': coupon.couponType == 1}"
                  >{{ coupon.couponType == 2 ? '秒杀专享' : '全场通用满减券' }}</span>
                  <!-- <span id="countdown" class="coupon-time">{{ time }}</span> -->
                </div>
                <div class="coupon-details">
                  <h3 class="coupon-title">{{coupon.title}}</h3>
                  <p class="coupon-time">有效期：{{coupon.startTime}} - {{coupon.endTime}}</p>
                </div>
                <div class="circle-top"></div>
                <div class="circle-bottom"></div>
              </div>
              <button
                v-if="coupon.couponType == 2"
                class="seckill-btn"
                :disabled="getCouponStatus(coupon) !== '秒杀'"
                :class="{'disabled-btn': getCouponStatus(coupon) !== '秒杀'}"
                @click="addSeckillOrder(coupon.id)"
              >{{ getCouponStatus(coupon) }}</button>
              <button
                class="full-btn"
                id="claimBtn"
                v-if="coupon.couponType == 1"
                @click="addCouponOrder(coupon.id)"
              >领取</button>
            </div>
          </div>

          <div class="count-section">
            <div class="cart-count">
              <button class="btn-count" :disabled="count <= 1" @click="count > 1 && count--">-</button>
              <input
                type="number"
                class="count-display"
                v-model.number="count"
                min="1"
                max="99"
                @change="normalizeCartCount"
              />
              <button
                class="btn-count"
                :disabled="count >= 99"
                :class="{'gray-text': count >= 99}"
                @click="count < 99 && count++"
              >+</button>
            </div>
          </div>

          <div class="action-footer">
            <button
              class="btn chat-btn"
              :disabled="count <= 0"
              @click="addCartItem(item.id,count)"
            >
              <i class="icon">🛒</i>
              加入购物车
            </button>
            <button class="btn buy-btn" @click="buyItem(this.item)">
              <i class="icon">🔔</i>
              立即下单
            </button>
          </div>
        </div>
      </div>

      <div class="comments-section">
        <div class="comments-header">
          <h3 class="section-title">
            留言互动
            <span class="comment-count">{{ commentList.length || 0 }}条</span>
          </h3>
        </div>

        <div class="comment-input-wrapper">
          <textarea placeholder="输入你的留言，向卖家咨询吧..." rows="3" v-model="commentContent"></textarea>
          <div class="input-actions">
            <button class="submit-btn" @click="submitComment">发布留言</button>
          </div>
        </div>

        <div class="comments-list">
          <div v-for="comment in commentList" :key="comment.id" class="comment-item">
            <div class="user-avatar-small"></div>
            <div class="comment-content">
              <div class="comment-user">
                {{ comment.userName || '校友匿名' }}
                <span
                  class="time"
                >{{ comment.createTime || '刚刚' }}&nbsp;&nbsp;&nbsp;</span>
                <button
                  class="follow-btn"
                  @click="handleFollow(comment.userId,comment.isFollowed,0)"
                  :class="{'active': comment.isFollowed}"
                ></button>
              </div>
              <p class="text">{{ comment.content }}</p>
              <div class="comment-actions">
                <span class="reply-btn" @click="handleReply(comment)">回复</span>
                <span
                  v-if="comment.userId == currentUserId"
                  class="reply-btn"
                  @click="handleDeleteComment(comment.id)"
                >删除</span>
                <input
                  v-if="replyingId == comment.id"
                  type="text"
                  class="reply-input"
                  placeholder="输入你的回复..."
                  v-model="replyContent"
                />
                <span
                  class="reply-btn"
                  style="border-color: #0071e3;"
                  @click="submitReply(comment.id)"
                  v-if="replyingId == comment.id"
                >提交</span>
              </div>
              <div v-if="comment.children && comment.children.length > 0" class="sub-comments">
                <div v-for="child in comment.children" :key="child.id" class="sub-comment-item">
                  <div class="comment-user">
                    <span class="sub-user-name">{{ child.userName }}</span>
                    <button
                      v-if="child.userName!=comment.userName"
                      class="follow-btn"
                      @click="handleFollow(child.userId,child.isFollowed,0)"
                      :class="{'active': child.isFollowed}"
                    ></button>
                    <span class="reply-text">回复</span>
                    <span
                      v-if="comment.userId == currentUserId"
                      class="reply-btn"
                      @click="handleDeleteComment(child.id)"
                    >删除</span>
                    <span class="sub-user-name">@{{ child.replyUserName || comment.userName }}</span>
                    <span class="time">{{ child.createTime }}</span>
                  </div>
                  <p class="sub-text">{{ child.content }}</p>
                  <div class="comment-actions">
                    <span class="reply-btn" @click="handleReply(child)">回复</span>
                    <span
                      v-if="comment.userId == currentUserId"
                      class="reply-btn"
                      @click="handleDeleteComment(child.id)"
                    >删除</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// import { f } from "vue-router/dist/router-CWoNjPRp.mjs";
import TopNavBar from "../components/TopNavBar.vue";
import request from "@/utils/request";
// script setup 部分

// 如果你的 userId 是数字类型，记得转换：
export default {
  components: {
    TopNavBar
  },
  data() {
    // 1. 获取原始字符串
    const userInfoRaw = localStorage.getItem("userInfo");

    // 2. 解析为对象（增加容错，防止没登录时报错）
    const userInfo = userInfoRaw ? JSON.parse(userInfoRaw) : {};
    return {
      id: this.$route.params.id,
      item: {},
      count: 0,
      couponList: [],
      commentList: [],
      followers: [],
      replyContent: "",
      replyingId: null, // 必须加上这个，否则点击回复没反应
      currentUserId: userInfo.userId
    };
  },
  methods: {
    getItemInfo() {
      request
        .get(`/item/${this.id}`)
        .then(res => {
          console.log(JSON.stringify(res));
          this.item = res;
          const stock = Number(this.item.stock) || 0;
          this.count = stock > 0 ? 1 : 0;
          console.log(JSON.stringify(this.item.sellerId));
          this.getCommonFollowers(this.item.sellerId);
        })
        .catch(err => {
          console.log(err);
        });
    },
    getCouponList() {
      request
        .get(`/seckill/coupon/${this.id}`)
        .then(res => {
          console.log(JSON.stringify(res));
          this.couponList = res;
        })
        .catch(err => {
          console.log(err);
        });
    },
    addBrowserCount() {
      request
        .post(`/item/browser/${this.id}`)
        .then(res => {
          console.log(JSON.stringify(res));
        })
        .catch(err => {
          console.log(err);
        })
        .finally(() => {
          this.getItemInfo();
        });
    },
    handleLike(itemId) {
      console.log(itemId);
      console.log(JSON.stringify(this.item));
      request
        .post(`/item/like/${itemId}`)
        .then(res => {
          console.log(JSON.stringify(res));
        })
        .catch(err => {
          console.log(err);
        })
        .finally(() => {
          this.getItemInfo();
        });
    },
    handleCollect(itemId) {
      console.log(itemId);
      console.log(JSON.stringify(this.item));
      request
        .post(`/item/collect/${itemId}`)
        .then(res => {
          console.log(JSON.stringify(res));
        })
        .catch(err => {
          console.log(err);
        })
        .finally(() => {
          this.getItemInfo();
        });
    },
    handleFollow(id, isFollowed, type) {
      // 防止重复点击导致数据错乱
      if (type == 1) {
        const originalStatus = this.item.isFollowed;
        request
          .post(`/follow/add`, null, {
            params: {
              followId: id,
              isFollowed: originalStatus, // 传给后端当前状态，由后端判断是新增还是删除
              type: type
            }
          })
          .then(() => {
            // 只有成功了才改变前端状态
            this.item.isFollowed = !originalStatus;
            alert(this.item.isFollowed ? "关注成功" : "已取消关注");
            this.getItemInfo();
            // 可选：使用轻提示代替 alert，更符合科研蓝/简约风格
            // this.$message.success(this.item.isFollowed ? "关注成功" : "已取消关注");
          })
          .catch(err => {
            alert(JSON.stringify(err).replace(/"/g, "")); //去掉双引号
          });
      } else {
        const originalStatus = isFollowed;
        request
          .post(`/follow/add`, null, {
            params: {
              followId: id,
              isFollowed: originalStatus, // 传给后端当前状态，由后端判断是新增还是删除
              type: type
            }
          })
          .then(() => {
            // 只有成功了才改变前端状态
            this.getCommentList(this.id);
            alert(!isFollowed ? "关注成功" : "已取消关注");
          })
          .catch(err => {
            alert(JSON.stringify(err).replace(/"/g, "")); //去掉双引号
          });
      }
    },
    addCartItem(itemId, count) {
      const selectedCount = Number(count);
      const stock = Number(this.item.stock) || 0;

      if (stock <= 0) {
        alert("商品库存不足，暂时无法加入购物车！");
        return;
      }

      if (!Number.isInteger(selectedCount) || selectedCount <= 0) {
        alert("请选择大于0的商品数量！");
        return;
      }

      if (selectedCount > stock) {
        alert(`库存不足，最多只能选择${stock}件！`);
        return;
      }

      request
        .post(`/cart`, null, {
          params: {
            itemId: itemId,
            count: selectedCount
          }
        })
        .then(() => {
          alert("加入购物车成功！");
        })
        .catch(err => {
          alert(JSON.stringify(err));
        });
    },
    normalizeCartCount() {
      const stock = Number(this.item.stock) || 0;
      const selectedCount = Number(this.count);

      if (stock <= 0) {
        this.count = 0;
        return;
      }

      if (!Number.isInteger(selectedCount) || selectedCount <= 0) {
        this.count = 1;
        return;
      }

      this.count = Math.min(selectedCount, 99);
    },
    buyItem(item) {
      if (this.count <= 0) {
        alert("请选择购买数量！");
        return;
      }
      request
        .post(`/order`, null, {
          params: {
            itemId: item.id,
            price: item.price,
            count: this.count,
            sellerId: item.sellerId
          }
        })
        .then(res => {
          alert("购买成功！订单号：" + JSON.stringify(res).replace(/"/g, "")); //去掉双引号
          this.getItemInfo();
        })
        .catch(err => {
          alert("购买失败！" + JSON.stringify(err).replace(/"/g, "")); //去掉双引号
          console.log("购买失败" + item.sellerId);
        });
    },
    addSeckillOrder(couponId) {
      request
        .post(`/seckill/order`, {
          value: this.item.price,
          id: couponId,
          couponType: 2
        })
        .then(res => {
          alert("购买成功！订单号：" + JSON.stringify(res).replace(/"/g, "")); //去掉双引号
        })
        .catch(err => {
          alert("购买失败！" + JSON.stringify(err).replace(/"/g, "")); //去掉双引号
        });
    },
    getCommentList(itemId) {
      request
        .get(`/comment/show/${itemId}`)
        .then(res => {
          console.log(JSON.stringify(res) + "?????????????");
          this.commentList = res;
        })
        .catch(err => {
          console.log(err);
        });
    },
    submitComment() {
      request
        .post(`/comment`, null, {
          params: {
            itemId: this.item.id,
            content: this.commentContent
          }
        })
        .then(() => {
          alert("留言成功！");
          this.getCommentList(this.item.id);
          this.commentContent = "";
        })
        .catch(err => {
          alert(JSON.stringify(err));
        });
    },
    getCommonFollowers(sellerId) {
      console.log(sellerId);
      request
        .post(`/follow/common`, null, {
          params: {
            sellerId: sellerId
          }
        })
        .then(res => {
          this.followers = res;
          console.log(this.followers);
        })
        .catch(err => {
          console.log(err);
        });
    },
    submitReply(commentId) {
      request
        .post(`/comment/reply`, null, {
          params: {
            itemId: this.item.id,
            commentId: commentId,
            content: this.replyContent
          }
        })
        .then(() => {
          alert("回复成功！");
          this.getCommentList(this.item.id);
          this.replyContent = "";
        })
        .catch(err => {
          alert(JSON.stringify(err));
        });
    },
    // addCouponOrder(couponId) {
    //   request
    //     .post(`/order/coupon`, null, {
    //       params: {
    //         itemId: this.item.id,
    //         price: this.item.price,
    //         count: this.count,
    //         couponId: couponId
    //       }
    //     })
    //     .then(res => {
    //       alert("购买成功！订单号：" + JSON.stringify(res).replace(/"/g, "")); //去掉双引号
    //     });
    // }
    handleReply(comment) {
      this.replyingId = comment.id;
      this.getItemInfo();
      console.log(this.replyingId);
    },
    handleDeleteComment(commentId) {
      request
        .post(`/comment/delete`, null, {
          params: {
            commentId: commentId
          }
        })
        .then(() => {
          alert("删除成功！");
          this.getCommentList(this.item.id);
        })
        .catch(err => {
          alert(JSON.stringify(err));
        });
    },
    getCouponStatus(coupon) {
      if (!coupon.startTime || !coupon.endTime) return "加载中";

      // 兼容性处理：将 2026-03-23 替换为 2026/03/23
      const start = new Date(coupon.startTime.replace(/-/g, "/")).getTime();
      const end = new Date(coupon.endTime.replace(/-/g, "/")).getTime();
      const now = Date.now();

      if (now < start) return "未开始";
      if (now > end) return "已结束";
      return "秒杀";
    }
  },

  // addCouponOrder(couponId){
  //   request
  //     .post(`/order/coupon`, null, {
  //       params: {
  //         itemId: this.item.id,
  //         price: this.item.price,
  //         count: this.count,
  //         couponId: couponId
  //       }
  //     })
  //     .then(res => {
  //       alert("购买成功！订单号：" + JSON.stringify(res).replace(/"/g, "")); //去掉双引号
  //     })
  // }
  // submitComment() {
  //   request
  //     .post(`/comment`, null, {
  //       params: {
  //         itemId: this.item.id,
  //         content: "这是留言内容"
  //       }
  //     })
  //     .then(() => {
  //       alert("留言成功！");
  //     })
  //     .catch(err => {
  //       alert(JSON.stringify(err));
  //     });
  // }

  mounted() {
    const itemId = this.$route.params.id;
    this.getCommentList(itemId);
    // 先自增浏览量，然后获取最新数据
    this.addBrowserCount(itemId);
    this.getCouponList(itemId);
    console.log(this.currentUserId);
  }
};
</script>

<style scoped>
.gray-text {
  color: #8e8e93;
}
.page-wrapper {
  background-color: #fcfcfd;
  min-height: 100vh;
  color: #1d1d1f;
}

.main-content {
  width: 70%; /* 稍微缩小点，更精致 */
  margin: 0 auto;
  padding: 40px 20px;
}

/* 商品卡片立体化 */
.item-card {
  display: flex;
  gap: 40px; /* 间距稍微缩小一点点，给内容留空间 */
  margin-bottom: 60px;
  background: #fff;
  padding: 30px;
  border-radius: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  align-items: stretch; /* 改为 stretch 让左右高度尽量对齐 */
}

/* 左侧图片区域 */
.image-section {
  flex: 1;
  width: 0; /* 关键：强制平分 */
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-wrapper {
  width: 100%;
  aspect-ratio: 1 / 1; /* 保持正方形 */
  background-color: #f5f5f7;
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-wrapper img {
  max-width: 90%;
  max-height: 90%;
  transition: transform 0.3s ease;
}

.image-wrapper:hover img {
  transform: scale(1.05); /* 悬浮微动效果 */
}

/* 1. 定义网格容器 */
.coupon-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px; /* 缩小间距 */
  margin: 15px 0;
  width: 100%;
}

/* 3. 针对小屏幕微调价格字体，防止挤压 */
@media (max-width: 600px) {
  .coupon-price .num {
    font-size: 24px;
  }
  .coupon-main {
    padding: 10px 15px;
  }
}
/* --- 优惠券网格系统修复 --- */
.coupon-grid {
  display: grid;
  /* 关键：使用 minmax 防止网格溢出父容器 */
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin: 20px 0;
  width: 100%;
  box-sizing: border-box;
}

.seckill-coupon {
  display: flex;
  height: 80px; /* 降低高度，压缩感更强 */
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ffe8e8;
  box-sizing: border-box;
}

/* 左侧主体：核心是让他具备收缩能力 */
.coupon-main {
  flex: 1;
  min-width: 0; /* 允许内部元素缩得比内容小 */
  padding: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  background: radial-gradient(circle at right, transparent 6px, #fff 0);
}

.coupon-price {
  color: #ff4d4f;
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.coupon-price .num {
  font-size: 18px; /* 调小金额 */
  font-weight: 800;
  line-height: 1;
}

.coupon-price .tag {
  font-size: 10px;
  background: #fff1f0;
  padding: 0 4px;
  border-radius: 3px;
  white-space: nowrap; /* 保证标签不换行 */
  transform: scale(0.85); /* 视觉缩小 */
}

.seckill-tag {
  background: #ff4d4f;
  color: #fff;
  font-size: 10px;
  padding: 0 4px;
  border-radius: 3px;
  white-space: nowrap; /* 保证标签不换行 */
  transform: scale(0.85); /* 视觉缩小 */
}
.full-tag {
  background: #fff1f0;
  color: #ff4d4f;
  font-size: 10px;
  padding: 0 4px;
  border-radius: 3px;
  white-space: nowrap; /* 保证标签不换行 */
  transform: scale(0.85); /* 视觉缩小 */
}

/* 标题和时间：最容易溢出的地方 */
.coupon-details .coupon-title {
  margin: 4px 0 2px;
  font-size: 12px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis; /* 溢出变点点点 */
}

.coupon-details .coupon-time {
  margin: 0;
  font-size: 10px;
  color: #999;
  white-space: wrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transform: scale(0.9);
  transform-origin: left;
}

/* 右侧按钮：固定宽度，不参与挤压 */
.coupon-action {
  flex-shrink: 0; /* 禁止被挤压 */
  width: 45px;
  background: linear-gradient(180deg, #ff7875 0%, #ff4d4f 100%);
  color: white;
  border: none;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  writing-mode: vertical-lr; /* 竖排文字，节省横向空间 */
  letter-spacing: 2px;
}

/* 装饰性半圆 */
.circle-top,
.circle-bottom {
  position: absolute;
  right: -8px;
  width: 16px;
  height: 16px;
  background-color: #f5f5f5; /* 外部背景色 */
  border-radius: 50%;
  z-index: 10;
}

.seckill-btn {
  flex-shrink: 0; /* 禁止被挤压 */
  width: 45px;
  background: linear-gradient(180deg, #ff7875 0%, #ffac4d 100%);
  color: white;
  border: none;
  font-size: 14px;
  font-weight: bold;
  line-height: 1.4;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  border-left: 1px dashed rgba(255, 255, 255, 0.4);
}

.full-btn {
  flex-shrink: 0; /* 禁止被挤压 */
  width: 45px;
  background: linear-gradient(180deg, #ff7875 0%, #ff4d4f 100%);
  color: white;
  border: none;
  font-size: 14px;
  font-weight: bold;
  line-height: 1.4;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  border-left: 1px dashed rgba(255, 255, 255, 0.4);
}

.seckill-btn:hover,
.full-btn:hover {
  filter: brightness(1.1);
  width: 70px; /* 悬停时稍微加宽，增加动感 */
}

.seckill-btn:active,
.full-btn:active {
  filter: brightness(0.9);
}

/* 已领取状态样式 */
.seckill-btn.disabled,
.full-btn.disabled {
  background: #d9d9d9;
  cursor: not-allowed;
  color: #fff;
}

.circle-top {
  top: -8px;
}

.circle-bottom {
  bottom: -8px;
}

/* 右侧大按钮 */
.coupon-action {
  width: 60px;
  background: linear-gradient(180deg, #ff7875 0%, #ff4d4f 100%);
  color: white;
  border: none;
  font-size: 14px;
  font-weight: bold;
  line-height: 1.4;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  border-left: 1px dashed rgba(255, 255, 255, 0.4);
}

.coupon-action:hover {
  filter: brightness(1.1);
  width: 70px; /* 悬停时稍微加宽，增加动感 */
}

.coupon-action:active {
  filter: brightness(0.9);
}

/* 已领取状态样式 */
.coupon-action.disabled {
  background: #d9d9d9;
  cursor: not-allowed;
  color: #fff;
}

/* 右侧详情区域 */
.info-section {
  flex: 1;
  width: 0; /* 关键：强制平分 */
  min-width: 0;
  display: flex;
  flex-direction: column;
  text-align: left;
}

.title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 15px;
  line-height: 1.3;
}

.like-wrapper {
  display: inline-flex; /* 开启行内弹性布局 */
  align-items: center; /* 垂直居中核心代码 */
  gap: 4px; /* 图片和数字之间的间距，不用再写 margin 了 */
  margin-left: 12px;
  cursor: pointer;
  vertical-align: middle; /* 解决整个组件在标题里的垂向偏移 */
}

.like-icon {
  margin-left: 8px;
  cursor: pointer;
  width: 20px;
  height: 20px;
}

.like-icon:hover {
  transform: scale(1.1);
}

.collect-icon {
  margin-left: 8px;
  cursor: pointer;
  width: 20px;
  height: 20px;
}

.collect-icon:hover {
  transform: scale(1.1);
}

.collect-icon {
  margin-left: 8px;
  width: 20px;
  height: 20px;
}

.browser-icon {
  margin-left: 8px;
  width: 20px;
  height: 20px;
}

.browser-count,
.like-count,
.collect-count {
  font-size: 16px;
}

.price-box {
  color: #ff4d4f;
  display: flex;
  align-items: center;
}

.currency {
  font-size: 20px;
  font-weight: 600;
  margin-right: 2px;
}

.price-num {
  font-size: 36px;
  font-weight: 700;
}

.stock{
  margin-left: 20px;
  color: #999;
}

.divider {
  height: 1px;
  background: #f0f0f2;
  margin: 24px 0;
}

/* 信息网格美化 */
.detail-grid {
  display: grid;
  gap: 16px;
  margin-bottom: 30px;
}

.detail-row {
  display: flex;
  align-items: center;
}

.label {
  width: 80px;
  color: #86868b;
  font-size: 14px;
}

.value-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mini-avatar {
  width: 24px;
  height: 24px;
  background: white;
  border-radius: 50%;
  overflow: hidden;
}

.mini-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* “关注”状态：蓝色渐变，吸引眼球 */
/* --- 关注按钮核心修复版 --- */

/* --- 1. 基础样式 (公用逻辑) --- */
.follow-btn {
  outline: none;
  border: none;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px; /* 统一圆角 */
  display: inline-flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

/* 默认状态 (未关注) */
.follow-btn:not(.active) {
  background: #ff4e4e;
  color: white;
  box-shadow: 0 4px 12px rgba(255, 78, 78, 0.2);
}

.follow-btn:not(.active)::before {
  content: "关注";
}

/* 已关注状态 (active) */
.follow-btn.active {
  background: #f0f0f0 !important;
  color: #666 !important;
  border: 1px solid #ddd !important;
  box-shadow: none !important;
}

.follow-btn.active::before {
  content: "已关注";
}

/* 已关注 + 悬停 (取消关注逻辑) */
.follow-btn.active:hover {
  background: #ffecec !important;
  color: #ff4d4f !important;
  border-color: #ffccc7 !important;
}

.follow-btn.active:hover::before {
  content: "取消关注";
}

/* --- 2. 尺寸变体 (区分主页和留言区) --- */

/* 详情页主按钮 (大) */
.follow-btn.main-btn {
  height: 32px;
  min-width: 88px;
  font-size: 13px;
}

/* 留言区/子回复按钮 (小) */
.follow-btn.mini-btn {
  height: 22px;
  min-width: 50px; /* 稍微给点宽度，防止“已关注”三个字挤在一起 */
  font-size: 11px;
  border-radius: 4px;
}

/* --- 3. 交互反馈 --- */
.follow-btn:hover {
  transform: translateY(-1px);
}

.follow-btn:active {
  transform: scale(0.95);
}

.description-box {
  background: #f9f9fb;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 30px;
}

.description-text {
  color: #424245;
  line-height: 1.7;
  font-size: 15px;
}

/* 按钮美化：Apple 风格 */
.action-footer {
  display: flex;
  gap: 16px;
  /* margin-top: auto;  <-- 删除这一行 */
  margin-top: 30px; /* 改为固定的间距，比如 20px */
}

.btn {
  flex: 1;
  height: 50px;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.disabled-btn {
  background: #f5f5f7;
  color: #999;
  cursor: not-allowed;
}

.chat-btn {
  background: #fff;
  border: 1.5px solid #0071e3;
  color: #0071e3;
}

.chat-btn:hover {
  background: #f5faff;
}

.buy-btn {
  background: #0071e3;
  color: #fff;
}

.buy-btn:hover {
  background: #0077ed;
  transform: translateY(-1px);
}

/* 1. 外层容器：负责在右侧区域水平居中 */
.count-section {
  display: flex;
  justify-content: center;
  margin: 10px 0; /* 缩小这个间距，从 20px 改为 10px */
  width: 100%;
}

/* 2. 计数器组件主体 */
.cart-count {
  display: flex;
  align-items: center; /* 垂直居中符号和数字 */
  background: #f5f5f7; /* 浅灰色背景 */
  border-radius: 12px;
  padding: 6px;
  gap: 16px; /* 给数字框留出稳定宽度 */
  border: 1px solid #e8e8ed;
}

/* 3. 按钮样式：确保 + 和 - 在圆圈内居中 */
.btn-count {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: none;
  background: #ffffff;
  color: #0071e3; /* Apple 蓝色 */
  font-size: 20px;
  font-weight: 500;
  display: flex;
  align-items: center; /* 符号水平居中 */
  justify-content: center; /* 符号垂直居中 */
  cursor: pointer;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  transition: all 0.2s;
}

.btn-count:hover {
  background: #0071e3;
  color: #ffffff;
}

.btn-count:active {
  transform: scale(0.9);
}

/* 4. 数字显示 */
.count-display {
  width: 64px;
  min-width: 64px;
  height: 36px;
  padding: 0 8px;
  box-sizing: border-box;
  font-size: 18px;
  font-weight: 600;
  text-align: center;
  background: #f5f5f7; /* 浅灰色背景 */
  border: none;
  outline: none;
  appearance: textfield;
  -moz-appearance: textfield;
}

.count-display::-webkit-outer-spin-button,
.count-display::-webkit-inner-spin-button {
  margin: 0;
  -webkit-appearance: none;
}

/* 留言区精修 */
/* --- 评论区整体布局增强 --- */
.comments-section {
  width: 100%; /* 让它跟上面的商品卡片宽度对齐 */
  margin-left: 0; /* 显式声明靠左 */
  margin-right: 0;
  text-align: left; /* 内部文字靠左 */
  margin-top: 40px;
  padding: 30px;
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.comment-item {
  display: flex;
  gap: 16px;
  padding: 24px 0;
  border-bottom: 1px solid #f2f2f7;
}

.comment-item:last-child {
  border-bottom: none;
}

/* --- 评论内容主体 --- */
.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-user {
  font-size: 15px;
  font-weight: 600;
  color: #0071e3;
  margin-bottom: 6px;
  display: flex;
  /* align-items: center; */
}

.comment-user .time {
  font-size: 12px;
  color: #86868b;
  font-weight: 400;
  margin-left: 10px;
}

.comment-content .text {
  font-size: 15px;
  line-height: 1.6;
  color: #424245;
  margin: 8px 0;
}

/* --- 回复操作按钮 --- */
.comment-actions {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}

.reply-btn {
  font-size: 13px;
  color: #0071e3; /* 科研蓝 */
  height: 20px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  background: #f5faff;
  transition: all 0.2s;
}

.reply-btn:hover {
  background: #0071e3;
  color: #fff;
}

.reply-input {
  width: 50%;
  height: 10px;
  border-radius: 16px;
  padding: 16px;
  border: 1px solid #d2d2d7;
  background: #f5f5f7;
  resize: none;
  font-family: inherit;
  transition: all 0.3s;
}

/* --- 子回复区域 (核心修改点) --- */
.sub-comments {
  margin-top: 16px;
  padding-left: 16px;
  border-left: 2px solid #f2f2f7; /* 左侧引导线 */
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sub-comment-item {
  background: #f9f9fb; /* 浅灰色背景块区分 */
  padding: 12px 16px;
  border-radius: 12px;
  transition: background 0.2s;
}

.sub-comment-item:hover {
  background: #f2f2f7;
}

.reply-text {
  margin: 0 6px;
  color: #86868b;
  font-size: 13px;
  font-weight: 400;
}

.sub-user-name {
  color: #0071e3;
  font-weight: 600;
}

.sub-text {
  font-size: 14px;
  color: #424245;
  line-height: 1.5;
  margin: 4px 0;
}

/* --- 输入框增强 --- */
.comment-input-wrapper textarea {
  width: 90%;
  border-radius: 16px;
  padding: 16px;
  border: 1px solid #d2d2d7;
  background: #f5f5f7;
  resize: none;
  font-family: inherit;
  transition: all 0.3s;
}

.comment-input-wrapper textarea:focus {
  outline: none;
  border-color: #0071e3;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.1);
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.submit-btn {
  background: #0071e3;
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 20px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s;
}

.submit-btn:active {
  transform: scale(0.96);
}

.user-avatar-small {
  width: 40px;
  height: 40px;
  background: #f2f2f7;
  border-radius: 50%;
}

.time {
  margin-left: 12px;
  color: #86868b;
  font-size: 12px;
}

.reply-btn {
  margin-top: 10px;
  font-weight: 500;
  transition: color 0.2s;
}

.reply-btn:hover {
  color: #ffffff;
}

@media (max-width: 900px) {
  .item-card {
    flex-direction: column;
    padding: 20px;
  }
  .image-section,
  .info-section {
    width: 100%;
  }
}
</style>
