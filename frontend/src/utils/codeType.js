/**
 * 代码类型工具函数
 * 用于从后端获取代码类型数据并转换为前端可用的格式
 */
import { getCodeByType } from '@/api/user'

// 缓存代码类型数据
const codeTypeCache = new Map()

/**
 * 获取代码类型列表
 * @param {string} codeType 代码类型（如：PROCESS_TYPE, BUSINESS_TYPE等）
 * @returns {Promise<Array>} 返回格式化的选项数组 [{label: '显示名称', value: '代码值'}]
 */
export async function getCodeTypeOptions(codeType) {
  // 如果缓存中有数据，直接返回
  if (codeTypeCache.has(codeType)) {
    return codeTypeCache.get(codeType)
  }

  try {
    const response = await getCodeByType(codeType)
    if (response.code === 200 && response.data) {
      const options = response.data
        .filter(item => item.isStop === 0 || item.isStop === '0') // 只返回未停用的
        .map(item => ({
          label: item.codeName,
          value: item.codeValue
        }))
      // 缓存数据
      codeTypeCache.set(codeType, options)
      return options
    }
    return []
  } catch (error) {
    console.error(`获取代码类型 ${codeType} 失败:`, error)
    return []
  }
}

/**
 * 根据代码值获取代码名称
 * @param {string} codeType 代码类型
 * @param {string} codeValue 代码值
 * @returns {Promise<string>} 代码名称
 */
export async function getCodeName(codeType, codeValue) {
  const options = await getCodeTypeOptions(codeType)
  const option = options.find(item => item.value === codeValue)
  return option ? option.label : codeValue
}

/**
 * 清除代码类型缓存
 * @param {string} codeType 代码类型，如果不传则清除所有缓存
 */
export function clearCodeTypeCache(codeType) {
  if (codeType) {
    codeTypeCache.delete(codeType)
  } else {
    codeTypeCache.clear()
  }
}

/**
 * 预加载常用代码类型
 */
export async function preloadCommonCodeTypes() {
  const commonTypes = [
    'PROCESS_TYPE',
    'BUSINESS_TYPE',
    'PAYOUT_TYPE',
    'CONTRACT_TYPE',
    'TASK_STATUS',
    'PROCESS_STATUS',
    'APPLY_STATUS'
  ]
  
  await Promise.all(commonTypes.map(type => getCodeTypeOptions(type)))
}



