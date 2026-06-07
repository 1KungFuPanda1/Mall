<template>
  <!-- ================================================
  编辑收货地址页 — 新增/修改
  ================================================ -->
  <view class="page-edit">
    <view class="form-card">
      <view class="input-row">
        <text class="label">收货人</text>
        <input class="input" v-model="form.receiverName" placeholder="请输入收货人姓名" />
      </view>
      <view class="input-row">
        <text class="label">手机号</text>
        <input class="input" v-model="form.receiverPhone" type="number" maxlength="11" placeholder="请输入手机号" />
      </view>
      <view class="input-row" @click="openRegionPicker">
        <text class="label">省市区</text>
        <input class="input" :value="regionStr" placeholder="请选择省市区" disabled />
        <u-icon name="arrow-right" size="14" color="#999" />
      </view>
      <view class="input-row">
        <text class="label">详细地址</text>
        <input class="input" v-model="form.detail" placeholder="街道、门牌号等" />
      </view>
      <view class="switch-row">
        <text class="label">设为默认</text>
        <switch :checked="form.isDefault === 1" color="#FF6034"
                @change="form.isDefault = $event.detail.value ? 1 : 0" />
      </view>
    </view>

    <!-- 省市区三级联动选择器 -->
    <u-picker ref="regionPickerRef" :show="showRegion" :columns="regionColumns"
              keyName="text" @change="onRegionChange"
              @confirm="onRegionConfirm" @cancel="showRegion = false" />

    <view class="btn-wrap">
      <button class="btn-save" @click="handleSave">{{ isEdit ? '保存' : '添加' }}</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getAddressList, addAddress, updateAddress } from '@/services/index.js'
import { provinces, cities, districts } from '@/utils/region-data.js'

const isEdit = ref(false)
const editId = ref(null)
const showRegion = ref(false)
const regionStr = ref('')
const regionPickerRef = ref(null)

// 当前选中的索引（用于编辑回显时定位）
let currentProvinceIdx = 0
let currentCityIdx = 0

// 初始化三列数据（二维数组格式，每项带 text 和 id 防止崩溃）
function buildColumns(pIdx, cIdx) {
  const pIdxVal = pIdx || 0
  const pName = provinces[pIdxVal] || provinces[0]
  const cityList = cities[pName] || []
  const cIdxVal = Math.min(cIdx || 0, cityList.length - 1)
  const cName = cityList[cIdxVal] || ''
  const distList = districts[cName] || []

  return [
    provinces.map(p => ({ text: p, id: p })),
    cityList.map(c => ({ text: c, id: c })),
    distList.map(d => ({ text: d, id: d }))
  ]
}

// 初始列数据
const regionColumns = ref(buildColumns(0, 0))

/** 打开选择器前初始化列数据 */
const openRegionPicker = () => {
  showRegion.value = true
}

/** 列变化 — 级联联动核心逻辑 */
const onRegionChange = (e) => {
  const { columnIndex, indexs } = e
  const picker = regionPickerRef.value
  if (!picker) return

  if (columnIndex === 0) {
    // 省份变化 → 更新城市和区县
    currentProvinceIdx = indexs[0]
    currentCityIdx = 0
    const pName = provinces[indexs[0]]
    if (pName && cities[pName]) {
      const cityList = cities[pName]
      const firstCity = cityList[0]
      const distList = districts[firstCity] || []
      picker.setColumnValues(1, cityList.map(c => ({ text: c, id: c })))
      picker.setColumnValues(2, distList.map(d => ({ text: d, id: d })))
    }
  } else if (columnIndex === 1) {
    // 城市变化 → 更新区县
    currentCityIdx = indexs[1]
    const pName = provinces[currentProvinceIdx]
    if (pName && cities[pName]) {
      const cityList = cities[pName]
      const cityName = cityList[indexs[1]]
      if (cityName) {
        const distList = districts[cityName] || []
        picker.setColumnValues(2, distList.map(d => ({ text: d, id: d })))
      }
    }
  }
}

/** 确认选择 */
const onRegionConfirm = (e) => {
  const values = e.value
  if (values && values.length >= 3) {
    form.province = values[0].text
    form.city = values[1].text
    form.district = values[2].text
    regionStr.value = `${values[0].text}-${values[1].text}-${values[2].text}`
  }
  showRegion.value = false
}

const form = reactive({
  receiverName: '', receiverPhone: '',
  province: '', city: '', district: '', detail: '',
  isDefault: 0
})

onLoad(async (options) => {
  if (options.id) {
    isEdit.value = true
    editId.value = options.id
    uni.setNavigationBarTitle({ title: '修改地址' })
    try {
      const list = await getAddressList()
      const addr = list.find(a => a.id == options.id)
      if (addr) {
        form.receiverName = addr.receiverName || ''
        form.receiverPhone = addr.receiverPhone || ''
        form.province = addr.province || ''
        form.city = addr.city || ''
        form.district = addr.district || ''
        form.detail = addr.detail || ''
        form.isDefault = addr.isDefault || 0
        regionStr.value = `${addr.province}-${addr.city}-${addr.district}`

        // 定位到已有地址的省市区索引
        const pIdx = provinces.indexOf(addr.province)
        if (pIdx >= 0) {
          currentProvinceIdx = pIdx
          const cityList = cities[addr.province] || []
          const cIdx = cityList.indexOf(addr.city)
          if (cIdx >= 0) currentCityIdx = cIdx
          regionColumns.value = buildColumns(pIdx, cIdx)
        }
      }
    } catch (e) {}
  }
})

const handleSave = async () => {
  if (!form.receiverName) return uni.showToast({ title: '请输入收货人', icon: 'none' })
  if (!form.receiverPhone || form.receiverPhone.length !== 11) return uni.showToast({ title: '请输入正确手机号', icon: 'none' })
  if (!form.province) return uni.showToast({ title: '请选择省市区', icon: 'none' })
  if (!form.detail) return uni.showToast({ title: '请输入详细地址', icon: 'none' })

  const submitData = {
    receiverName: form.receiverName,
    receiverPhone: form.receiverPhone,
    province: form.province,
    city: form.city,
    district: form.district,
    detail: form.detail,
    isDefault: form.isDefault
  }

  try {
    if (isEdit.value) {
      await updateAddress(editId.value, submitData)
    } else {
      await addAddress(submitData)
    }
    uni.showToast({ title: isEdit.value ? '修改成功' : '添加成功', icon: 'success' })
    setTimeout(() => { uni.navigateBack() }, 500)
  } catch (e) {}
}
</script>

<style lang="scss" scoped>
.page-edit { min-height: 100vh; background: #f5f5f5; }
.form-card { background: #fff; padding: 0 24rpx; margin-bottom: 16rpx; }
.input-row { display: flex; align-items: center; border-bottom: 1rpx solid #f5f5f5; padding: 24rpx 0; }
.label { width: 160rpx; font-size: 28rpx; color: #333; flex-shrink: 0; }
.input { flex: 1; font-size: 28rpx; color: #333; }
.switch-row { display: flex; align-items: center; justify-content: space-between; padding: 24rpx 0; }
.btn-wrap { padding: 40rpx 24rpx; }
.btn-save {
  width: 100%; height: 88rpx; line-height: 88rpx;
  background: linear-gradient(135deg, #FF6034, #FF8A6A);
  color: #fff; font-size: 32rpx; border-radius: 44rpx; text-align: center;
}
</style>
