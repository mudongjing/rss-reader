<template>
  <div id="confirmDel">
    <p>{{rssId}}</p>
    <button @click="confirmDel">确认</button>
    <button @click="cancelDel">取消</button>
  </div>
</template>

<script>
export default {
  name: "ConfirmDelte",
  props:{
    rssId: null
  },
  methods:{
    confirmDel(){
      console.log('确认')
    },
    cancelDel(){
      console.log('取消')
    }
  }
}
</script>

<style scoped>
  #confirmDel{
    position: fixed;
    width: 500px;
    height: 300px;
  }
</style>
