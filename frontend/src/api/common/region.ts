import axios from 'axios';

export interface RegionVO {
  id: number;
  code: string;
  name: string;
  parentId?: number;
  level: number;
  sortOrder: number;
  children?: RegionVO[];
}

const API_BASE = '/api/regions';

// localStorage 缓存键
const CACHE_KEY_PROVINCES = 'region_provinces';
const CACHE_KEY_CHILDREN = 'region_children_';
const CACHE_EXPIRE_TIME = 24 * 60 * 60 * 1000; // 24小时

interface CacheData<T> {
  data: T;
  timestamp: number;
}

/**
 * 从 localStorage 获取缓存
 */
function getCache<T>(key: string): T | null {
  try {
    const cached = localStorage.getItem(key);
    if (!cached) return null;
    
    const cacheData: CacheData<T> = JSON.parse(cached);
    const now = Date.now();
    
    // 检查是否过期
    if (now - cacheData.timestamp > CACHE_EXPIRE_TIME) {
      localStorage.removeItem(key);
      return null;
    }
    
    return cacheData.data;
  } catch (e) {
    console.error('读取缓存失败:', e);
    return null;
  }
}

/**
 * 保存到 localStorage
 */
function setCache<T>(key: string, data: T): void {
  try {
    const cacheData: CacheData<T> = {
      data,
      timestamp: Date.now()
    };
    localStorage.setItem(key, JSON.stringify(cacheData));
  } catch (e) {
    console.error('保存缓存失败:', e);
  }
}

/**
 * 获取所有省份
 */
export async function getProvinces(): Promise<RegionVO[]> {
  // 先尝试从缓存获取
  const cached = getCache<RegionVO[]>(CACHE_KEY_PROVINCES);
  if (cached) {
    return cached;
  }
  
  // 缓存未命中，请求接口
  const response = await axios.get<{ code: number; data: RegionVO[]; message: string }>(`${API_BASE}/provinces`);
  const provinces = response.data.data;
  
  // 存入缓存
  setCache(CACHE_KEY_PROVINCES, provinces);
  
  return provinces;
}

/**
 * 根据父级ID获取子级地区
 */
export async function getChildrenByParentId(parentId: number): Promise<RegionVO[]> {
  if (!parentId) return [];
  
  const cacheKey = CACHE_KEY_CHILDREN + parentId;
  
  // 先尝试从缓存获取
  const cached = getCache<RegionVO[]>(cacheKey);
  if (cached) {
    return cached;
  }
  
  // 缓存未命中，请求接口
  const response = await axios.get<{ code: number; data: RegionVO[]; message: string }>(`${API_BASE}/children/${parentId}`);
  const children = response.data.data;
  
  // 存入缓存
  setCache(cacheKey, children);
  
  return children;
}

/**
 * 根据编码获取地区信息
 */
export async function getRegionByCode(code: string): Promise<RegionVO | null> {
  if (!code) return null;
  
  const response = await axios.get<{ code: number; data: RegionVO; message: string }>(`${API_BASE}/code/${code}`);
  return response.data.data;
}

/**
 * 根据编码获取完整路径
 */
export async function getFullPathByCode(code: string): Promise<string> {
  if (!code) return '';
  
  const response = await axios.get<{ code: number; data: string; message: string }>(`${API_BASE}/path/${code}`);
  return response.data.data || '';
}

/**
 * 清除所有地区缓存
 */
export function clearRegionCache(): void {
  // 清除所有以 region_ 开头的缓存
  Object.keys(localStorage).forEach(key => {
    if (key.startsWith('region_')) {
      localStorage.removeItem(key);
    }
  });
}











































