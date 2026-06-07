<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background:#409EFF"><el-icon size="28" color="#fff"><Goods /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.todayOrders ?? 0 }}</div>
              <div class="stat-label">今日订单数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background:#67C23A"><el-icon size="28" color="#fff"><Money /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">¥{{ stats.todayAmount ?? '0.00' }}</div>
              <div class="stat-label">今日营业额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background:#E6A23C"><el-icon size="28" color="#fff"><User /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalUsers ?? 0 }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" class="stat-cards" style="margin-top:20px">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background:#9B59B6"><el-icon size="28" color="#fff"><Truck /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingOrders ?? 0 }}</div>
              <div class="stat-label">待发货订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background:#00BCD4"><el-icon size="28" color="#fff"><TrendCharts /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">¥{{ stats.monthAmount ?? '0.00' }}</div>
              <div class="stat-label">本月营业额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background:#F06292"><el-icon size="28" color="#fff"><DocumentChecked /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.monthOrders ?? 0 }}</div>
              <div class="stat-label">本月订单数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="chart-card">
      <template #header><span class="card-title">快捷入口</span></template>
      <el-row :gutter="16">
        <el-col :span="6" v-for="entry in quickEntries" :key="entry.path">
          <div class="quick-entry" @click="$router.push(entry.path)">
            <el-icon :size="32" :color="entry.color"><component :is="entry.icon" /></el-icon>
            <span>{{ entry.label }}</span>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStatisticsSummary } from '@/api/admin'

const stats = ref({})

const quickEntries = [
  { path: '/products', label: '商品管理', icon: 'Goods', color: '#409EFF' },
  { path: '/categories', label: '分类管理', icon: 'Grid', color: '#67C23A' },
  { path: '/banners', label: '轮播图', icon: 'Picture', color: '#E6A23C' },
  { path: '/orders', label: '订单管理', icon: 'Document', color: '#F56C6C' },
  { path: '/users', label: '用户管理', icon: 'User', color: '#909399' }
]

onMounted(async () => {
  try {
    stats.value = await getStatisticsSummary()
  } catch (e) {
    stats.value = { todayOrders: 0, todayAmount: '0.00', totalUsers: 0, pendingOrders: 0, monthAmount: '0.00', monthOrders: 0 }
  }
})
</script>

<style scoped>
.dashboard { padding: 0; }
.stat-item { display: flex; align-items: center; gap: 16px; }
.stat-icon { width: 60px; height: 60px; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
.chart-card { margin-top: 20px; }
.card-title { font-size: 16px; font-weight: bold; }
.quick-entry { display: flex; flex-direction: column; align-items: center; padding: 20px; border-radius: 8px; cursor: pointer; transition: background 0.2s; border: 1px solid #ebeef5; }
.quick-entry:hover { background: #f5f7fa; }
.quick-entry span { margin-top: 10px; font-size: 14px; color: #333; }
</style>
