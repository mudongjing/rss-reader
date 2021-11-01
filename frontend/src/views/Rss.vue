<template>
	<div id="Rss">
    <RssNavBar/>
    <DoubleCol class="back-ground-rss">
      <div slot="left-col">
        <div v-for="item in logoTitle" @click="setIdAndAll(item.redisId)" :class="{'colordisplay':clicked==item.redisId}" @click.alt="removeThisRss(item.redisId)">
          <list-item>
            <img slot="logo-img" v-if="item.logo!=null" :src="item.logo">
            <img slot="logo-img" v-else src="../assets/img/failImg.svg">
            <p slot="title-cont">{{item.title}}</p>
          </list-item>
        </div>
      </div>

      <div slot="right-col">
        <div v-for="item in rssMessage" @click="jumpToRssDetail(item)" :class="{'colordisplay':item.isRead}">
          <brief-of-rss>
            <p slot="brief-title" v-html="item.title"></p>
            <p slot="brief-date" v-if="! item.hasOwnProperty('publishDate')"> </p>
            <p slot="brief-date" v-else>{{item.publishDate}}</p>
            <p slot="brief-content" v-if="item.articleContent.trim()!=='' && item.articleContent!==null" v-html="item.articleContent"></p>
            <p slot="brief-content" v-else v-html="item.articleDescription"></p>
          </brief-of-rss>
        </div>
      </div>
    </DoubleCol>
	</div>
</template>

<script>
  import NavBar from '../components/common/NavBar'
	import { getAllRss,getLogoAndTitle,getMessageFromId ,updateRead} from '../network/rssItem.js'
  import DoubleCol from "../components/common/DoubleCol";
  import ListItem from "../components/content/ListItem";
  import BriefOfRss from "../components/content/BriefOfRss";
  import RssNavBar from "../components/content/RssNavBar";
  import {createModal} from "../components/common/CreateModal";
  import Modal from "../components/content/Modal";
  export default {
		name:'Rss',
		props:{
			config: 'sd'
		},
		components: {
			NavBar,
      DoubleCol,
      ListItem,
      BriefOfRss,
      RssNavBar
		},
		data(){
			return {
        rssList: [],
				logoTitle: [],
        idAndAll: {id:3,isAll:false},
        rssMessage: [],
        clicked: null
			}
		},
    methods:{
      setIdAndAll(index){
        this.idAndAll.id=index;
        this.clicked=index
        getMessageFromId(this.idAndAll).then(res=>{this.rssMessage=res})
      },
      jumpToRssDetail(itemMessage){
        itemMessage.isRead=true
        let tmid={'tableId':this.idAndAll.id,'messageId':itemMessage.messageId}
        updateRead(tmid)
        this.$router.push({
          name: 'RssDetails',
          params:{
            item: itemMessage
          }
        })
      },
      removeThisRss(id){
        this.ele = createModal(Modal, {
          'rssId': id
        });
        this.ele.show();
      }
    },

		created() {
      getAllRss().then(res=>{
        this.rssList=res;
        if(res.length!==0 ){
          getLogoAndTitle(this.rssList).then(res=>{
            this.logoTitle=res;
          });
        }else{
          console.log('当前无rss数据')
        }

      });
		},
	}
</script>

<style scoped>
  .colordisplay{
    color: #c3c3ad;
  }
</style>
