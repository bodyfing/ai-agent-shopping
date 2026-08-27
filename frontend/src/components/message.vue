<template>
  <div id="common-modal" class="modal-overlay">
    <div class="modal-content">
      <h3 id="modal-title">提示</h3>
      <p id="modal-message">这是一条提示消息</p>
      <div class="modal-actions">
        <button id="modal-cancel-btn" class="btn-secondary">取消</button>
        <button id="modal-confirm-btn" class="btn-primary">确定</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "MessageModal"
};
const Modal = {
  // 初始化元素引用
  el: document.getElementById("common-modal"),
  title: document.getElementById("modal-title"),
  message: document.getElementById("modal-message"),
  confirmBtn: document.getElementById("modal-confirm-btn"),
  cancelBtn: document.getElementById("modal-cancel-btn"),

  /**
   * 显示弹窗
   * @param {Object} options { title, msg, showCancel }
   * @returns Promise
   */
  show(options = {}) {
    const { title = "提示", msg = "", showCancel = true } = options;

    this.title.innerText = title;
    this.message.innerText = msg;
    this.cancelBtn.style.display = showCancel ? "inline-block" : "none";
    this.el.style.display = "flex";

    return new Promise(resolve => {
      // 点击确定
      this.confirmBtn.onclick = () => {
        this.hide();
        resolve(true);
      };
      // 点击取消
      this.cancelBtn.onclick = () => {
        this.hide();
        resolve(false);
      };
    });
  },

  hide() {
    this.el.style.display = "none";
  }
};
</script>

<style>
.modal-overlay {
  display: none; /* 初始隐藏 */
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  z-index: 9999;
  justify-content: center;
  align-items: center;
}
.modal-content {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  width: 320px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  text-align: center;
}
/* 按钮样式省略，可根据你的 UI 风格调整 */
</style>