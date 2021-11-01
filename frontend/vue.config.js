module.exports = {
	configureWebpack:{
		resolve:{
			alias:{
				'assets':'@/assets',
				'common':'@/common',
				'components':'@/components',
				'network':'@/network',
				'view':'/src/views',
				'img':'@/assets/img'
			}
		}
	},
	publicPath: "./",
}
