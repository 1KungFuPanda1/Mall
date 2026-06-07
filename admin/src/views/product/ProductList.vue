<template>
  <div class="product-page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="searchKey" placeholder="搜索商品名称" clearable style="width:260px" @clear="loadData" @keyup.enter="loadData" />
        <el-select v-model="searchCategory" placeholder="按分类筛选" clearable style="width:180px;margin-left:12px" @change="loadData">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-button type="primary" @click="showDialog()" style="margin-left:12px">新增商品</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="margin-top:16px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <el-image :src="getFirstImage(row.image)" style="width:60px;height:60px" fit="cover"
                      :preview-src-list="getAllImages(row.image)" :hide-on-click-modal="true" lazy>
              <template #error>
                <div class="image-error">无图</div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showDialog(row)">编辑</el-button>
            <el-button :type="row.status===1 ? 'warning' : 'success'" link size="small" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-popconfirm title="确定删除该商品？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page" v-model:page-size="pageSize"
        :total="total" layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10,20,50]" @current-change="loadData" @size-change="loadData"
        style="margin-top:16px;justify-content:flex-end" background />
    </el-card>

    <!-- 商品编辑/新增对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="640px" destroy-on-close>
      <el-form :model="form" ref="formRef" label-width="80px" :rules="formRules">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :step="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload :action="uploadUrl" :headers="uploadHeaders" list-type="picture-card"
                     :on-success="onUploadSuccess" :on-remove="onRemoveImage"
                     :file-list="imageList" :limit="5">
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入商品描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0"
                     active-text="上架" inactive-text="下架" />
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
import { getProductList, addProduct, updateProduct, deleteProduct, toggleProductStatus, getCategoryList } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const categories = ref([])
const searchKey = ref('')
const searchCategory = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)
const imageUrls = ref([])
const uploadUrl = '/admin/upload'

const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + localStorage.getItem('admin_token')
}))

const imageList = computed(() => imageUrls.value.map(url => ({ url, name: url })))

const form = reactive({ name: '', categoryId: null, price: 0.01, stock: 0, description: '', status: 1 })
const formRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

onMounted(() => { loadData(); loadCategories() })

const loadCategories = async () => {
  try { categories.value = await getCategoryList() } catch (e) {}
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (searchKey.value) params.keyword = searchKey.value
    if (searchCategory.value) params.categoryId = searchCategory.value
    const res = await getProductList(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) { tableData.value = []; total.value = 0 }
  finally { loading.value = false }
}

const showDialog = (row) => {
  if (row) {
    isEdit.value = true
    editId.value = String(row.id)
    Object.assign(form, {
      name: row.name, categoryId: row.categoryId, price: row.price,
      stock: row.stock, description: row.description || '', status: row.status
    })
    imageUrls.value = row.image ? row.image.split(',') : []
  } else {
    isEdit.value = false
    editId.value = null
    Object.assign(form, { name: '', categoryId: null, price: 0.01, stock: 0, description: '', status: 1 })
    imageUrls.value = []
  }
  dialogVisible.value = true
}

const onUploadSuccess = (response) => {
  if (response) {
    const url = typeof response === 'string' ? response : (response.data || response.url || '')
    if (url) imageUrls.value.push(url)
  }
}

const onRemoveImage = (file) => {
  const url = file.url || file.response?.url
  if (url) {
    imageUrls.value = imageUrls.value.filter(u => u !== url)
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const data = { ...form, image: imageUrls.value.join(',') }
      if (isEdit.value) {
        await updateProduct(editId.value, data)
        ElMessage.success('修改成功')
      } else {
        await addProduct(data)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (e) {} finally { saving.value = false }
  })
}

const handleToggleStatus = async (row) => {
  try {
    await toggleProductStatus(String(row.id))
    ElMessage.success(row.status === 1 ? '已下架' : '已上架')
    loadData()
  } catch (e) {}
}

const handleDelete = async (id) => {
  try {
    await deleteProduct(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {}
}

/** 获取首张图片 URL（兼容各种存储格式） */
const getFirstImage = (img) => {
  if (!img) return ''
  const url = img.includes(',') ? img.split(',')[0] : img
  // 确保相对路径以 /uploads/ 开头
  if (url && !url.startsWith('http') && !url.startsWith('/')) {
    return '/uploads/' + url
  }
  return url
}

/** 获取所有图片 URL 数组（用于 el-image 大图预览） */
const getAllImages = (img) => {
  if (!img) return []
  return img.split(',').filter(u => u.trim())
}
</script>

<style scoped>
.product-page { padding: 0; }
.toolbar { display: flex; align-items: center; }
.image-error {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  background: #f5f7fa; color: #c0c4cc; font-size: 12px;
}
</style>
