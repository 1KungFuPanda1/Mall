<template>
  <div class="category-page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" @click="showDialog()">新增分类</el-button>
      </div>
      <el-table :data="tableData" border stripe v-loading="loading" style="margin-top:16px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="200" />
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
            <el-popconfirm title="确定删除该分类？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="480px" destroy-on-close>
      <el-form :model="form" ref="formRef" label-width="80px" :rules="rules">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="20" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCategoryList, addCategory, updateCategory, deleteCategory } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

const form = reactive({ name: '', sortOrder: 0, status: 1 })
const rules = { name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] }

onMounted(() => loadData())

const loadData = async () => {
  loading.value = true
  try { tableData.value = await getCategoryList() } catch (e) { tableData.value = [] }
  finally { loading.value = false }
}

const showDialog = (row) => {
  if (row) {
    isEdit.value = true; editId.value = row.id
    Object.assign(form, { name: row.name, sortOrder: row.sortOrder || 0, status: row.status })
  } else {
    isEdit.value = false; editId.value = null
    Object.assign(form, { name: '', sortOrder: 0, status: 1 })
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (isEdit.value) { await updateCategory(editId.value, form); ElMessage.success('修改成功') }
      else { await addCategory(form); ElMessage.success('新增成功') }
      dialogVisible.value = false; loadData()
    } catch (e) {} finally { saving.value = false }
  })
}

const handleDelete = async (id) => {
  try { await deleteCategory(id); ElMessage.success('删除成功'); loadData() } catch (e) {}
}
</script>

<style scoped>
.category-page { padding: 0; }
.toolbar { display: flex; align-items: center; }
</style>
