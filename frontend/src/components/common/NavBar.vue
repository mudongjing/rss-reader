<template>
	<div class="nav-bar">
			<slot name="left"></slot>
			<slot name="middle"></slot>
			<slot name="right"></slot>
	</div>
</template>

<script>
	export default{
		name: "NavBar"
	}
</script>

<style scoped>

</style>
