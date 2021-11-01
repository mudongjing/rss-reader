<template>
  <div class="modal" v-if="is_open">
    <div class="content">
      <p class="close" @click="hide()">close</p>
      <p class="title">'rssId: '{{ rssId }}</p>
      <button @click="confirm">确认</button>
      <button @click="cancel">取消</button>
    </div>
  </div>
</template>

<script>
import {deleteRss} from "../../network/rssItem";

export default {
  name: "Modal",
  props: ["rssId"],
  data() {
    return {
      is_open: false,
    };
  },
  methods: {
    show() {
      this.is_open = true;
    },
    hide() {
      this.is_open = false;
    },
    confirm(){
      deleteRss(this.rssId);
      this.hide();
    },
    cancel(){
      this.hide()
    }
  }
}
</script>

<style  scoped>
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
}
.content {
  width: 200px;
  height: 200px;
  background-color: #fff;
  margin: 0px auto;
  margin-top: 200px;
  text-align: center;
  font-size: 14px;
  color: #333;
  padding: 5px;
}
.title {
  margin-bottom: 20px;
  font-size: 16px;
}
.close {
  text-align: right;
}
</style>
