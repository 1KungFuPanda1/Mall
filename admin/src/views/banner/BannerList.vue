<template>
  <div class="banner-page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" @click="showDialog()">新增轮播图</el-button>
      </div>
      <el-table :data="tableData" border stripe v-loading="loading" style="margin-top:16px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="图片" width="200">
          <template #default="{ row }">
            <el-image :src="row.imageUrl" style="width:180px;height:80px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column label="排序" width="100" prop="sortOrder" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除该轮播图？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑轮播图' : '新增轮播图'" width="520px" destroy-on-close>
      <el-form :model="form" ref="formRef" label-width="80px" :rules="rules">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" maxlength="30" />
        </el-form-item>
        <el-form-item label="图片" prop="imageUrl">
          <el-upload :action="uploadUrl" :headers="uploadHeaders" list-type="picture-card"
                     :on-success="onUploadSuccess" :file-list="previewList" :limit="1">
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="链接地址">
          <el-input v-model="form.linkUrl" placeholder="跳转链接（选填）" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0"
                     active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getBannerList, addBanner, updateBanner, deleteBanner } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)
const uploadUrl = '/admin/upload'

const uploadHeaders = computed(() => ({ Authorization: 'Bearer ' + localStorage.getItem('admin_token') }))

const form = reactive({ title: '', imageUrl: '', linkUrl: '', sortOrder: 0, status: 1 })
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请上传图片', trigger: 'change' }]
}

const previewList = computed(() => {
  if (!form.imageUrl) return []
  return [{ url: form.imageUrl, name: form.imageUrl }]
})

onMounted(() => loadData())

const loadData = async () => {
  loading.value = true
  try { tableData.value = await getBannerList() } catch (e) { tableData.value = [] }
  finally { loading.value = false }
}

const showDialog = (row) => {
  if (row) {
    isEdit.value = true; editId.value = row.id
    Object.assign(form, {
      title: row.title, imageUrl: row.imageUrl, linkUrl: row.linkUrl || '',
      sortOrder: row.sortOrder || 0, status: row.status
    })
  } else {
    isEdit.value = false; editId.value = null
    Object.assign(form, { title: '', imageUrl: '', linkUrl: '', sortOrder: 0, status: 1 })
  }
  dialogVisible.value = true
}

const onUploadSuccess = (response) => {
  const url = typeof response === 'string' ? response : (response.data || response.url || '')
  if (url) form.imageUrl = url
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (isEdit.value) { await updateBanner(editId.value, form); ElMessage.success('修改成功') }
      else { await addBanner(form); ElMessage.success('新增成功') }
      dialogVisible.value = false; loadData()
    } catch (e) {} finally { saving.value = false }
  })
}

const handleDelete = async (id) => {
  try { await deleteBanner(id); ElMessage.success('删除成功'); loadData() } catch (e) {}
}
</script>

<style scoped>
.banner-page { padding: 0; }
.toolbar { display: flex; align-items: center; }
</style>
