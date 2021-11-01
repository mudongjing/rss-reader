<template>
  <div class="brief-rss">
    <table>
      <tr>
        <td>标题</td>
        <td class="brief-rss-title">
            <slot name="brief-title"></slot>
        </td>
      </tr>
      <tr>
        <td class="brief-rss-date" style="width: 15%">
            <slot name="brief-date"></slot>
        </td>
        <td class="brief-rss-content">
            <slot name="brief-content"></slot>
        </td>
      </tr>
    </table>
  </div>
</template>

<script>
export default {
  name: "BriefOfRss"
}
</script>

<style scoped>
  .brief-rss{
    display: flex;
    width: 99%;
  }
  table,table tr th, table tr td { border:1px solid #f3e0ec; }
  table {
    min-height: 25px;
    text-align: center;
    border-collapse: collapse;
    padding:2px;
    cellpadding:0px;
    width: 100%;
  }
  .brief-rss-title{
    font-size: 13px;
  }
  .brief-rss-content{
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 1;
    max-width: 95ch;
    max-height: 60px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 14px;
  }
  .brief-rss-date{
    font-size: 14px;
  }
</style>
