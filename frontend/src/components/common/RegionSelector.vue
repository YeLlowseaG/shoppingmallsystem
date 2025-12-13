<template>
  <div class="region-selectors">
    <el-select
      v-model="selectedProvinceId"
      placeholder="请选择省份"
      class="region-select"
      @change="handleProvinceChange"
      :loading="loading"
      clearable
    >
      <el-option
        v-for="province in provinces"
        :key="province.id"
        :label="province.name"
        :value="province.id"
      />
    </el-select>
    
    <el-select
      v-model="selectedCityId"
      placeholder="请选择城市"
      class="region-select"
      :disabled="!selectedProvinceId"
      @change="handleCityChange"
      :loading="loading"
      clearable
    >
      <el-option
        v-for="city in cities"
        :key="city.id"
        :label="city.name"
        :value="city.id"
      />
    </el-select>
    
    <el-select
      v-model="selectedDistrictId"
      placeholder="请选择区县"
      class="region-select"
      :disabled="!selectedCityId"
      :loading="loading"
      clearable
    >
      <el-option
        v-for="district in districts"
        :key="district.id"
        :label="district.name"
        :value="district.id"
      />
    </el-select>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { getProvinces, getChildrenByParentId, type RegionVO } from '@/api/common/region';

interface Props {
  modelValue?: {
    provinceId?: number;
    cityId?: number;
    districtId?: number;
  };
}

interface Emits {
  (e: 'update:modelValue', value: {
    provinceId?: number;
    cityId?: number;
    districtId?: number;
  }): void;
  (e: 'change', value: {
    provinceId?: number;
    cityId?: number;
    districtId?: number;
    provinceCode?: string;
    cityCode?: string;
    districtCode?: string;
    provinceName?: string;
    cityName?: string;
    districtName?: string;
  }): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const loading = ref(false);
const provinces = ref<RegionVO[]>([]);
const cities = ref<RegionVO[]>([]);
const districts = ref<RegionVO[]>([]);

const selectedProvinceId = ref<number | undefined>();
const selectedCityId = ref<number | undefined>();
const selectedDistrictId = ref<number | undefined>();

// 初始化数据
onMounted(async () => {
  await loadProvinces();
  
  // 如果有初始值，加载对应的数据
  if (props.modelValue?.provinceId) {
    selectedProvinceId.value = props.modelValue.provinceId;
    await loadCities(props.modelValue.provinceId);
    
    if (props.modelValue.cityId) {
      selectedCityId.value = props.modelValue.cityId;
      await loadDistricts(props.modelValue.cityId);
      
      if (props.modelValue.districtId) {
        selectedDistrictId.value = props.modelValue.districtId;
      }
    }
  }
});

// 监听外部值变化
watch(() => props.modelValue, async (newVal) => {
  if (newVal) {
    // 如果省份ID变化，需要重新加载城市和区县
    if (newVal.provinceId && newVal.provinceId !== selectedProvinceId.value) {
      selectedProvinceId.value = newVal.provinceId;
      await loadCities(newVal.provinceId);
    } else {
      selectedProvinceId.value = newVal.provinceId;
    }
    
    // 如果城市ID变化，需要重新加载区县
    if (newVal.cityId && newVal.cityId !== selectedCityId.value) {
      selectedCityId.value = newVal.cityId;
      await loadDistricts(newVal.cityId);
    } else {
      selectedCityId.value = newVal.cityId;
    }
    
    selectedDistrictId.value = newVal.districtId;
  } else {
    selectedProvinceId.value = undefined;
    selectedCityId.value = undefined;
    selectedDistrictId.value = undefined;
    cities.value = [];
    districts.value = [];
  }
}, { deep: true, immediate: false });

// 加载省份
async function loadProvinces() {
  try {
    loading.value = true;
    provinces.value = await getProvinces();
  } catch (error) {
    console.error('加载省份失败:', error);
    provinces.value = [];
  } finally {
    loading.value = false;
  }
}

// 加载城市
async function loadCities(provinceId: number) {
  try {
    loading.value = true;
    cities.value = await getChildrenByParentId(provinceId);
  } catch (error) {
    console.error('加载城市失败:', error);
    cities.value = [];
  } finally {
    loading.value = false;
  }
}

// 加载区县
async function loadDistricts(cityId: number) {
  try {
    loading.value = true;
    districts.value = await getChildrenByParentId(cityId);
  } catch (error) {
    console.error('加载区县失败:', error);
    districts.value = [];
  } finally {
    loading.value = false;
  }
}

// 省份改变
async function handleProvinceChange(provinceId: number | undefined) {
  selectedCityId.value = undefined;
  selectedDistrictId.value = undefined;
  cities.value = [];
  districts.value = [];
  
  if (provinceId) {
    await loadCities(provinceId);
  }
  
  emitChange();
}

// 城市改变
async function handleCityChange(cityId: number | undefined) {
  selectedDistrictId.value = undefined;
  districts.value = [];
  
  if (cityId) {
    await loadDistricts(cityId);
  }
  
  emitChange();
}

// 监听区县改变
watch(selectedDistrictId, () => {
  emitChange();
});

// 发出变更事件
function emitChange() {
  const province = provinces.value.find(p => p.id === selectedProvinceId.value);
  const city = cities.value.find(c => c.id === selectedCityId.value);
  const district = districts.value.find(d => d.id === selectedDistrictId.value);
  
  const value = {
    provinceId: selectedProvinceId.value,
    cityId: selectedCityId.value,
    districtId: selectedDistrictId.value,
    provinceCode: province?.code,
    cityCode: city?.code,
    districtCode: district?.code,
    provinceName: province?.name,
    cityName: city?.name,
    districtName: district?.name,
  };
  
  emit('update:modelValue', {
    provinceId: value.provinceId,
    cityId: value.cityId,
    districtId: value.districtId,
  });
  
  emit('change', value);
}
</script>

<style scoped>
.region-selectors {
  display: flex;
  gap: 10px;
}

.region-select {
  flex: 1;
  min-width: 120px;
}
</style>

