<template>
  <div class="card">
    <div class="img-wrapper">
      <img :src="product.imageURL"  class="product-img" />
    </div>

    <div class="card-body">
      <h3 class="title">{{ product.title }}</h3>
      <div class="bottom-row">
        <span class="price">¥ {{ product.price }}</span>
        <span class="seller">
          <i class="user-icon">👤</i> {{ product.seller }}
        </span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "ProductCard",
  props: {
    product: {
      type: Object,
      required: true
    }
  }
};
</script>

<style scoped>
.card {
  background: white;
  border-radius: 12px;
  overflow: hidden; /* 防止图片溢出圆角 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); /* 轻微阴影 */
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
  border: 1px solid #f0f0f0;
}

/* 悬停浮起效果 */
.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.img-wrapper {
  width: 100%;
  height: 200px; /* 固定图片高度 */
  background-color: #f5f5f5;
}

.product-img {
  /* 确保图片元素撑满父容器的大小 */
  width: 100%;
  height: 100%;
  
  /* 关键修改：改为 contain */
  /* cover: 充满盒子，可能会裁切掉边缘 (之前的做法) */
  /* contain: 完整显示图片，保持比例，绝不裁切，但可能会留白 (现在的做法) */
  object-fit: contain; 
  display: block;
  background-color: #fff; 
}

.card-body {
  padding: 12px 16px;
}

.title {
  font-size: 16px;
  margin: 0 0 10px 0;
  color: #333;
  /* 限制标题最多显示一行，超出省略 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bottom-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  color: #ff5000; /* 淘宝红 */
  font-size: 18px;
  font-weight: bold;
}

.seller {
  font-size: 12px;
  color: #999;
}
</style>