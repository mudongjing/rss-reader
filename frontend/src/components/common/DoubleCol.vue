<template>
  <div class="double-col">
    <div class="left">
      <slot name="left-col"></slot>
    </div>
    <div class="right">
        <slot name="right-col"></slot>
    </div>
  </div>
</template>

<script>

export default {
  name: "DoubleCol"
}
</script>

<style scoped>
  .left{
    display: flex;
    left: 0px;
    position:absolute;
    height: 90%;
    width: 32%;
    overflow: auto;
  }
  .right{
    display: flex;
    position:absolute;
    right:0.5%;
    width: 67%;
    height: 89%;
    overflow: scroll;
  }
</style>
