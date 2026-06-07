<template>
  <div class="order-page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="searchStatus" placeholder="全部状态" clearable style="width:150px" @change="loadData">
          <el-option label="全部" value="" />
          <el-option label="待付款" value="WAIT_PAY" />
          <el-option label="已付款" value="PAID" />
          <el-option label="待收货" value="RECEIVING" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-input v-model="searchKey" placeholder="搜索订单号" clearable style="width:220px;margin-left:12px" @keyup.enter="loadData" />
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="margin-top:16px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.statusDesc }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="170" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'WAIT_PAY'" type="success" link size="small" @click="handlePay(row)">
              模拟支付
            </el-button>
            <el-button v-if="row.status === 'WAIT_PAY'" type="danger" link size="small" @click="handleCancel(row)">
              取消订单
            </el-button>
            <el-button v-if="row.status === 'PAID'" type="success" link size="small" @click="showDeliverDialog(row)">
              发货
            </el-button>
            <el-button v-if="row.status === 'RECEIVING'" type="warning" link size="small" @click="handleConfirm(row)">
              确认收货
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total"
                     layout="total, prev, pager, next" @current-change="loadData" @size-change="loadData"
                     style="margin-top:16px;justify-content:flex-end" background />
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="680px" destroy-on-close>
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detail.status)" size="small">{{ detail.statusDesc }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ detail.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="支付时间" v-if="detail.payTime">{{ detail.payTime }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin-top:20px;margin-bottom:10px">收货地址</h4>
        <el-descriptions :column="2" border v-if="detail.addressJson">
          <el-descriptions-item label="收货人">{{ tryParseAddress()?.name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ tryParseAddress()?.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ tryParseAddress()?.fullAddress || tryParseAddress()?.detail || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-descriptions :column="1" border v-else>
          <el-descriptions-item label="收货地址">暂无地址信息</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin-top:20px;margin-bottom:10px">商品清单</h4>
        <el-table :data="detail.items" border size="small">
          <el-table-column label="商品" min-width="200">
            <template #default="{ row }">
              <div style="display:flex;align-items:center;gap:10px">
                <el-image :src="row.productImage" style="width:50px;height:50px" fit="cover" />
                <span>{{ row.productName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="单价" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="100">
            <template #default="{ row }">¥{{ row.totalAmount }}</template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>

    <!-- 发货对话框 -->
    <el-dialog v-model="deliverVisible" title="发货" width="480px" destroy-on-close>
      <el-form :model="deliverForm" ref="deliverFormRef" label-width="100px" :rules="deliverRules">
        <el-form-item label="物流公司" prop="expressCompany">
          <el-input v-model="deliverForm.expressCompany" placeholder="如：顺丰速运" maxlength="30" />
        </el-form-item>
        <el-form-item label="物流单号" prop="expressNo">
          <el-input v-model="deliverForm.expressNo" placeholder="请输入物流单号" maxlength="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deliverVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDeliver" :loading="delivering">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, getOrderDetail, deliverOrder, cancelOrder, payOrder, confirmOrderReceipt } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const searchStatus = ref('')
const searchKey = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const detailVisible = ref(false)
const detail = ref(null)

const deliverVisible = ref(false)
const delivering = ref(false)
const deliverFormRef = ref(null)
const deliverForm = reactive({ expressCompany: '', expressNo: '' })
const deliverRules = {
  expressCompany: [{ required: true, message: '请输入物流公司', trigger: 'blur' }],
  expressNo: [{ required: true, message: '请输入物流单号', trigger: 'blur' }]
}
let currentDeliverOrderId = null

onMounted(() => loadData())

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (searchStatus.value) params.status = searchStatus.value
    if (searchKey.value) params.keyword = searchKey.value
    const res = await getOrderList(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) { tableData.value = []; total.value = 0 }
  finally { loading.value = false }
}

const showDetail = async (row) => {
  try {
    detail.value = await getOrderDetail(row.id)
    detailVisible.value = true
  } catch (e) {}
}

const showDeliverDialog = (row) => {
  currentDeliverOrderId = row.id
  deliverForm.expressCompany = ''
  deliverForm.expressNo = ''
  deliverVisible.value = true
}

const handleDeliver = async () => {
  if (!deliverFormRef.value) return
  await deliverFormRef.value.validate(async (valid) => {
    if (!valid) return
    delivering.value = true
    try {
      await deliverOrder(currentDeliverOrderId, { ...deliverForm })
      ElMessage.success('发货成功')
      deliverVisible.value = false
      loadData()
    } catch (e) {} finally { delivering.value = false }
  })
}

// 模拟支付
const handlePay = async (row) => {
  try {
    await ElMessageBox.confirm(`确认支付订单 ${row.orderNo} ？`, '模拟支付', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'info'
    })
    await payOrder(row.id)
    ElMessage.success('支付成功')
    loadData()
  } catch (e) {} // 用户取消或支付失败
}

// 取消订单
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(`确认取消订单 ${row.orderNo} ？取消后将恢复商品库存。`, '取消订单', {
      confirmButtonText: '确认取消',
      cancelButtonText: '返回',
      type: 'warning'
    })
    await cancelOrder(row.id)
    ElMessage.success('订单已取消，库存已恢复')
    loadData()
  } catch (e) {} // 用户取消或操作失败
}

// 确认收货
const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm(`确认订单 ${row.orderNo} 已收货？`, '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'success'
    })
    await confirmOrderReceipt(row.id)
    ElMessage.success('已确认收货')
    loadData()
  } catch (e) {} // 用户取消或操作失败
}

// 解析收货地址 JSON
const tryParseAddress = () => {
  if (!detail.value?.addressJson) return null
  try {
    return JSON.parse(detail.value.addressJson)
  } catch (e) {
    return null
  }
}

const statusType = (s) => {
  const map = { WAIT_PAY: 'warning', PAID: '', RECEIVING: 'primary', COMPLETED: 'success', CANCELLED: 'info' }
  return map[s] || ''
}
</script>

<style scoped>
.order-page { padding: 0; }
.toolbar { display: flex; align-items: center; }
</style>
