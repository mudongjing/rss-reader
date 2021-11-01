import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import Rss from '@/views/Rss.vue'
Vue.use(VueRouter)

const Visit = ()=> import('@/views/Visit.vue')
const Search = ()=> import('@/views/Search.vue')
const Configure = ()=> import('@/views/Configure.vue')
const RssDetail= ()=>import('@/views/RssDetails.vue')

const routes = [
  {
    path: '/',
	  redirect: Rss,
	  // meta: {
		//   keepAlive: true, //此组件需要被缓存
		//   isBack:false, //用于判断上一个页面是哪个
	  // }
  },
	{
	  path: '/rss',
	  name: 'Rss',
	  component: Rss,
		meta: {
			keepAlive: true, //此组件需要被缓存
			isBack:false, //用于判断上一个页面是哪个
		}
	},
  {
    path: '/visit',
    name: 'Visit',
    component: Visit
  },
	{
		path: '/search',
		name: 'Search',
		component: Search
	},
	{
		path: '/configure',
		name: 'Configure',
		component: Configure
	},
	{
		path: '/rssDetail',
		name: 'RssDetails',
		component: RssDetail
	}
]

const router = new VueRouter({
  mode: 'hash',
  base: process.env.BASE_URL,
  routes
})

export default router
