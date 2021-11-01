<template>
  <div class="list-item">
    <table>
      <tr>
        <td class="list-item-left">
          <slot name="logo-img"></slot>
        </td>
        <td class="list-item-right" style="width: auto">
          <slot name="title-cont"></slot>
        </td>
      </tr>
    </table>
  </div>
</template>

<script>
export default {
  name: "ListItem",
  props:{
    rssSource:{
      type: Object,
      default(){
        return {}
      }
    }
  }
}
</script>

<style scoped>
  table,table tr th { border:1px solid #f3e0ec;}
  table {
    font-size: 12px;
    text-align: center;
    word-wrap:break-word;
    word-break:break-all;
    table-layout: fixed;
    width: 100%;
  }
  td{
    height: 70px;
  }
  .list-item-left{
    width: 20%;
  }
  .list-item-right{
    text-align: center;
  }
  .list-item-left img{
    width: 100%;
    height: 100%;
    object-fit:fill;
    border-radius: 50%;
  }
</style>
