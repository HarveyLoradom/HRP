import request from './request'

/**
 * 通用Excel导出
 * @param {Object} exportData - 导出数据对象
 * @param {string} exportData.fileName - 文件名（不含扩展名）
 * @param {Array<string>} exportData.headers - 表头列表
 * @param {Array<Array<string>>} exportData.dataList - 数据列表（二维数组）
 * @returns {Promise} 返回blob响应
 */
export const exportExcel = (exportData) => {
  return request({
    url: '/auth/common/excel/export',
    method: 'post',
    data: exportData,
    responseType: 'blob' // 重要：设置响应类型为blob
  })
}

