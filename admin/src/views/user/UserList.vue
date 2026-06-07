<template>
  <div class="user-page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="searchKey" placeholder="搜索手机号/昵称" clearable style="width:260px" @keyup.enter="loadData" />
        <el-button type="primary" style="margin-left:12px" @click="loadData">搜索</el-button>
      </div>
      <el-table :data="tableData" border stripe v-loading="loading" style="margin-top:16px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="nickname" label="昵称" min-width="140" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : ''" size="small">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-popconfirm
              :title="row.status === 1 ? '确定禁用该用户？' : '确定启用该用户？'"
              @confirm="handleToggleStatus(row)">
              <template #reference>
                <el-button
                  :type="row.status === 1 ? 'danger' : 'success'"
                  link size="small">
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total"
                     layout="total, prev, pager, next" @current-change="loadData" @size-change="loadData"
                     style="margin-top:16px;justify-content:flex-end" background />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserList, toggleUserStatus } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const searchKey = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => loadData())

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (searchKey.value) params.keyword = searchKey.value
    const res = await getUserList(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) { tableData.value = []; total.value = 0 }
  finally { loading.value = false }
}

const handleToggleStatus = async (row) => {
  try {
    await toggleUserStatus(row.id)
    ElMessage.success(row.status === 1 ? '已禁用' : '已启用')
    loadData()
  } catch (e) {}
}
</script>

<style scoped>
.user-page { padding: 0; }
.toolbar { display: flex; align-items: center; }
</style>
