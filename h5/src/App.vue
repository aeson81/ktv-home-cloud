<template>
  <router-view v-slot="{ Component }">
    <component :is="Component" />
  </router-view>
  <ToastHost />
  <DialogHost />
</template>

<script setup>
/**
 * 应用根组件，负责挂载路由视图以及全局 Toast/对话框宿主。
 *
 * Root application component that mounts the router view and global toast/dialog hosts.
 */
import { onMounted, onUnmounted } from 'vue'
import { usePlayerStore } from './stores/player'
import { useUserStore } from './stores/user'
import { useFavoritesStore } from './stores/favorites'
import ToastHost from './components/ToastHost.vue'
import DialogHost from './components/DialogHost.vue'

// 已注册用户进入即连 WebSocket；未注册的在进入页提交后由路由跳转触发
// Registered users connect immediately; unregistered users connect after entry-page navigation.
const player = usePlayerStore()
const user = useUserStore()
const favorites = useFavoritesStore()

onMounted(() => {
  if (user.isRegistered) {
    player.connect()
    favorites.load(user.clientToken).catch(() => {})
  }
})
onUnmounted(() => player.disconnect())
</script>
