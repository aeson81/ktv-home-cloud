<template>
  <div class="page">
    <header class="topbar">
      <button class="back" @click="$router.back()">‹</button>
      <b>排行榜</b>
      <span></span>
    </header>
    <div class="tabs">
      <button :class="{on: tab==='hot'}" @click="tab='hot';load()">🔥 热歌榜</button>
      <button :class="{on: tab==='new'}" @click="tab='new';load()">✨ 新歌榜</button>
    </div>
    <div class="list">
      <div v-for="(s,i) in songs" :key="s.id" class="row" @click="play(s)">
        <span class="rank" :class="{top: i<3}">{{ i+1 }}</span>
        <div class="info">
          <b>{{ s.title }}</b>
          <small>{{ s.artist }} · 播放 {{ s.playCount }} 次</small>
        </div>
        <button class="add" @click.stop="queue(s)">点歌</button>
      </div>
      <div v-if="!songs.length" class="empty">暂无数据</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api/client'

const tab = ref('hot')
const songs = ref([])

async function load() {
  const r = tab.value === 'hot' ? await api.rankingsHot().catch(() => ({ songs: [] })) : await api.rankingsNew().catch(() => ({ songs: [] }))
  songs.value = r.songs || []
}
async function play(s) { /* 点击播放 */ }
async function queue(s) {
  await api.queueAdd(s.id).catch(()=>{})
}

onMounted(load)
</script>

<style scoped>
.page { min-height: 100vh; background: var(--bg); color: var(--text); }
.topbar { height: 56px; display: flex; align-items: center; justify-content: space-between; padding: 0 16px; border-bottom: 1px solid var(--line); }
.topbar b { font-size: 17px; }
.back { font-size: 28px; color: var(--gold); }
.tabs { display: flex; gap: 12px; padding: 16px; }
.tabs button { flex: 1; height: 44px; border-radius: 10px; background: var(--panel); border: 1px solid var(--line); color: var(--dim); font-size: 15px; font-weight: 600; }
.tabs button.on { background: linear-gradient(135deg, var(--gold), #dba70e); color: #1a1400; border: none; }
.list { padding: 0 16px 24px; }
.row { display: flex; align-items: center; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--line); }
.rank { width: 28px; font-size: 18px; font-weight: 800; color: var(--dim2); text-align: center; font-family: ui-monospace, monospace; }
.rank.top { color: var(--gold); }
.info { flex: 1; min-width: 0; }
.info b { display: block; font-size: 15px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.info small { color: var(--dim2); font-size: 12px; }
.add { height: 32px; padding: 0 16px; border-radius: 16px; background: rgba(255,198,75,.12); border: 1px solid rgba(255,198,75,.3); color: var(--gold); font-size: 13px; font-weight: 600; }
.empty { text-align: center; padding: 48px; color: var(--dim2); }
</style>
