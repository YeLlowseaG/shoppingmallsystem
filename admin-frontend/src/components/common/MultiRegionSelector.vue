<template>
  <div class="multi-region-selector">
    <el-button type="primary" size="small" @click="showDialog = true">选择地区</el-button>
    
    <el-dialog
      v-model="showDialog"
      title="选择地区"
      width="900px"
      @close="handleDialogClose"
    >
      <div class="region-selector-content">
        <!-- 地区选择器 -->
        <div class="region-columns">
          <!-- 省份列 -->
          <div class="region-column">
            <div class="column-header">
              <span>省份</span>
              <el-button type="primary" link size="small" @click="selectAllProvinces">全选</el-button>
            </div>
            <div class="region-list">
              <el-checkbox
                v-for="province in provinces"
                :key="province.id"
                :label="province.id"
                v-model="selectedProvinceIds"
                @change="handleProvinceChange"
              >
                {{ province.name }}
              </el-checkbox>
            </div>
          </div>

          <!-- 城市列 -->
          <div class="region-column">
            <div class="column-header">
              <span>城市</span>
              <el-button type="primary" link size="small" @click="selectAllCities">全选</el-button>
            </div>
            <div class="region-list">
              <el-checkbox
                v-for="city in cities"
                :key="city.id"
                :label="city.id"
                v-model="selectedCityIds"
                @change="handleCityChange"
              >
                {{ city.name }}
              </el-checkbox>
            </div>
          </div>

          <!-- 区县列 -->
          <div class="region-column">
            <div class="column-header">
              <span>区县</span>
              <el-button type="primary" link size="small" @click="selectAllDistricts">全选</el-button>
            </div>
            <div class="region-list">
              <el-checkbox
                v-for="district in districts"
                :key="district.id"
                :label="district.id"
                v-model="selectedDistrictIds"
                @change="handleDistrictChange"
              >
                {{ district.name }}
              </el-checkbox>
            </div>
          </div>
        </div>

        <!-- 已选地区显示 -->
        <div class="selected-regions" v-if="selectedRegions.length > 0">
          <div class="selected-header">已选地区：</div>
          <el-tag
            v-for="(region, index) in selectedRegions"
            :key="index"
            closable
            @close="removeRegion(index)"
            style="margin-right: 8px; margin-bottom: 8px"
          >
            {{ region.provinceName }}{{ region.cityName ? '-' + region.cityName : '' }}{{ region.districtName ? '-' + region.districtName : '' }}
          </el-tag>
        </div>
      </div>

      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleConfirm">确定选择</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProvinces, getChildrenByParentId, type RegionVO } from '@/api/common/region'

interface Props {
  modelValue?: Array<{
    provinceCode?: string
    provinceName?: string
    cityCode?: string
    cityName?: string
    districtCode?: string
    districtName?: string
  }>
}

interface Emits {
  (e: 'update:modelValue', value: Array<{
    provinceCode?: string
    provinceName?: string
    cityCode?: string
    cityName?: string
    districtCode?: string
    districtName?: string
  }>): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const showDialog = ref(false)
const loading = ref(false)
const provinces = ref<RegionVO[]>([])
const cities = ref<RegionVO[]>([])
const districts = ref<RegionVO[]>([])

const selectedProvinceIds = ref<number[]>([])
const selectedCityIds = ref<number[]>([])
const selectedDistrictIds = ref<number[]>([])

const selectedRegions = ref<Array<{
  provinceCode: string
  provinceName: string
  cityCode?: string
  cityName?: string
  districtCode?: string
  districtName?: string
}>>([])

// 初始化数据
onMounted(async () => {
  await loadProvinces()
  if (props.modelValue && props.modelValue.length > 0) {
    selectedRegions.value = props.modelValue.map(r => ({
      provinceCode: r.provinceCode || '',
      provinceName: r.provinceName || '',
      cityCode: r.cityCode,
      cityName: r.cityName,
      districtCode: r.districtCode,
      districtName: r.districtName
    }))
  }
})

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  if (newVal && newVal.length > 0) {
    selectedRegions.value = newVal.map(r => ({
      provinceCode: r.provinceCode || '',
      provinceName: r.provinceName || '',
      cityCode: r.cityCode,
      cityName: r.cityName,
      districtCode: r.districtCode,
      districtName: r.districtName
    }))
  } else {
    selectedRegions.value = []
  }
}, { deep: true })

// 加载省份
async function loadProvinces() {
  try {
    loading.value = true
    provinces.value = await getProvinces()
  } catch (error) {
    console.error('加载省份失败:', error)
    ElMessage.error('加载省份失败')
    provinces.value = []
  } finally {
    loading.value = false
  }
}

// 加载城市
async function loadCities(provinceIds: number[]) {
  try {
    loading.value = true
    cities.value = []
    districts.value = []
    selectedCityIds.value = []
    selectedDistrictIds.value = []

    if (provinceIds.length === 0) {
      return
    }

    // 加载所有选中省份的城市
    const allCities: RegionVO[] = []
    for (const provinceId of provinceIds) {
      const provinceCities = await getChildrenByParentId(provinceId)
      allCities.push(...provinceCities)
    }
    cities.value = allCities
  } catch (error) {
    console.error('加载城市失败:', error)
    ElMessage.error('加载城市失败')
    cities.value = []
  } finally {
    loading.value = false
  }
}

// 加载区县
async function loadDistricts(cityIds: number[]) {
  try {
    loading.value = true
    districts.value = []
    selectedDistrictIds.value = []

    if (cityIds.length === 0) {
      return
    }

    // 加载所有选中城市的区县
    const allDistricts: RegionVO[] = []
    for (const cityId of cityIds) {
      const cityDistricts = await getChildrenByParentId(cityId)
      allDistricts.push(...cityDistricts)
    }
    districts.value = allDistricts
  } catch (error) {
    console.error('加载区县失败:', error)
    ElMessage.error('加载区县失败')
    districts.value = []
  } finally {
    loading.value = false
  }
}

// 省份改变
async function handleProvinceChange() {
  await loadCities(selectedProvinceIds.value)
}

// 城市改变
async function handleCityChange() {
  await loadDistricts(selectedCityIds.value)
}

// 区县改变
function handleDistrictChange() {
  // 区县改变时不需要额外操作
}

// 全选省份
function selectAllProvinces() {
  if (selectedProvinceIds.value.length === provinces.value.length) {
    selectedProvinceIds.value = []
  } else {
    selectedProvinceIds.value = provinces.value.map(p => p.id)
  }
  handleProvinceChange()
}

// 全选城市
function selectAllCities() {
  if (selectedCityIds.value.length === cities.value.length) {
    selectedCityIds.value = []
  } else {
    selectedCityIds.value = cities.value.map(c => c.id)
  }
  handleCityChange()
}

// 全选区县
function selectAllDistricts() {
  if (selectedDistrictIds.value.length === districts.value.length) {
    selectedDistrictIds.value = []
  } else {
    selectedDistrictIds.value = districts.value.map(d => d.id)
  }
}

// 确定选择
function handleConfirm() {
  const regions: Array<{
    provinceCode: string
    provinceName: string
    cityCode?: string
    cityName?: string
    districtCode?: string
    districtName?: string
  }> = []

  // 处理选中的区县（最具体）
  for (const districtId of selectedDistrictIds.value) {
    const district = districts.value.find(d => d.id === districtId)
    if (district) {
      const city = cities.value.find(c => c.id === district.parentId)
      const province = provinces.value.find(p => p.id === city?.parentId)
      if (province) {
        regions.push({
          provinceCode: province.code,
          provinceName: province.name,
          cityCode: city?.code,
          cityName: city?.name,
          districtCode: district.code,
          districtName: district.name
        })
      }
    }
  }

  // 处理只选中城市的情况
  const selectedCityIdsWithoutDistrict = selectedCityIds.value.filter(
    cityId => !selectedDistrictIds.value.some(districtId => {
      const district = districts.value.find(d => d.id === districtId)
      return district?.parentId === cityId
    })
  )
  for (const cityId of selectedCityIdsWithoutDistrict) {
    const city = cities.value.find(c => c.id === cityId)
    const province = provinces.value.find(p => p.id === city?.parentId)
    if (province && city) {
      regions.push({
        provinceCode: province.code,
        provinceName: province.name,
        cityCode: city.code,
        cityName: city.name
      })
    }
  }

  // 处理只选中省份的情况
  const selectedProvinceIdsWithoutCity = selectedProvinceIds.value.filter(
    provinceId => !selectedCityIds.value.some(cityId => {
      const city = cities.value.find(c => c.id === cityId)
      return city?.parentId === provinceId
    })
  )
  for (const provinceId of selectedProvinceIdsWithoutCity) {
    const province = provinces.value.find(p => p.id === provinceId)
    if (province) {
      regions.push({
        provinceCode: province.code,
        provinceName: province.name
      })
    }
  }

  selectedRegions.value = regions
  emit('update:modelValue', regions)
  showDialog.value = false
  ElMessage.success(`已选择 ${regions.length} 个地区`)
}

// 移除地区
function removeRegion(index: number) {
  selectedRegions.value.splice(index, 1)
  emit('update:modelValue', selectedRegions.value)
}

// 对话框关闭
function handleDialogClose() {
  // 重置选择状态
  selectedProvinceIds.value = []
  selectedCityIds.value = []
  selectedDistrictIds.value = []
  cities.value = []
  districts.value = []
}
</script>

<style scoped lang="scss">
.multi-region-selector {
  .region-selector-content {
    .region-columns {
      display: flex;
      gap: 20px;
      margin-bottom: 20px;
      max-height: 400px;
      overflow: hidden;

      .region-column {
        flex: 1;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        overflow: hidden;

        .column-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 10px;
          background-color: #f5f7fa;
          border-bottom: 1px solid #dcdfe6;
          font-weight: 500;
        }

        .region-list {
          max-height: 350px;
          overflow-y: auto;
          padding: 10px;

          .el-checkbox {
            display: block;
            margin-bottom: 8px;
          }
        }
      }
    }

    .selected-regions {
      margin-top: 20px;
      padding: 15px;
      background-color: #f5f7fa;
      border-radius: 4px;

      .selected-header {
        margin-bottom: 10px;
        font-weight: 500;
      }
    }
  }
}
</style>

