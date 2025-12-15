import axios from 'axios'
import { Message } from 'element-ui'
import Cookies from 'js-cookie'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = Cookies.get('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 如果设置了 responseType 为 'text' 或 'blob'，直接返回原始响应
    const config = response.config || {}
    if (config.responseType === 'text' || config.responseType === 'blob') {
      return response
    }
    
    const res = response.data
    // 如果响应不是对象（可能是字符串），直接返回
    if (typeof res !== 'object' || res === null) {
      return res
    }
    
    if (res.code !== 200) {
      // 创建错误对象，包含完整的响应信息，方便前端处理
      const error = new Error(res.message || '请求失败')
      error.response = response
      error.code = res.code
      
      // 获取请求配置，检查是否需要自动显示错误消息
      const config = response.config || {}
      const shouldShowError = config.showError !== false // 默认显示错误，除非明确设置为false
      
      // 对于401，清除token但不跳转（让调用方处理）
      if (res.code === 401) {
        Cookies.remove('token')
        localStorage.removeItem('userInfo')
        // 登录接口的错误不在这里显示，由登录页面处理
        if (!config.url || !config.url.includes('/auth/login')) {
          if (shouldShowError) {
            Message.error(res.message || '未授权，请重新登录')
          }
        }
      } else {
        // 其他错误自动显示消息（除非明确设置不显示）
        if (shouldShowError) {
          Message.error(res.message || '操作失败')
        }
      }
      
      return Promise.reject(error)
    }
    return res
  },
  error => {
    // 处理HTTP错误响应（如400、404、500等）
    // 全局异常处理器返回的响应体仍然是Result格式
    const config = error.config || {}
    const shouldShowError = config.showError !== false // 默认显示错误，除非明确设置为false
    const isLoginRequest = config.url && config.url.includes('/auth/login')
    
    // 如果后端返回的是HTML（比如Spring Boot的默认错误页面），尝试从状态码推断错误
    if (error.response && error.response.headers && error.response.headers['content-type'] && 
        error.response.headers['content-type'].includes('text/html')) {
      let errorMessage = '请求失败'
      if (error.response.status === 500) {
        errorMessage = '系统内部错误，请稍后重试'
      } else if (error.response.status === 404) {
        errorMessage = '请求的资源不存在'
      } else if (error.response.status === 403) {
        errorMessage = '没有权限访问'
      } else if (error.response.status === 401) {
        errorMessage = '未授权，请重新登录'
        Cookies.remove('token')
        localStorage.removeItem('userInfo')
      }
      const customError = new Error(errorMessage)
      customError.response = error.response
      customError.code = error.response.status
      // 对于登录请求，不在这里显示错误（由Login.vue处理），但确保error.message包含正确的消息
      if (shouldShowError && !config.url?.includes('/auth/logout') && !isLoginRequest) {
        Message.error(errorMessage)
      }
      return Promise.reject(customError)
    }
    
    // 处理响应体数据
    if (error.response && error.response.data) {
      let res = error.response.data
      
      // 如果响应体是字符串，尝试解析为JSON
      if (typeof res === 'string') {
        try {
          res = JSON.parse(res)
        } catch (e) {
          // 如果解析失败，使用原始字符串作为错误消息
          const errorMessage = res || '请求失败'
          const customError = new Error(errorMessage)
          customError.response = error.response
          customError.code = error.response.status
          if (shouldShowError && !config.url?.includes('/auth/logout') && !isLoginRequest) {
            Message.error(errorMessage)
          }
          return Promise.reject(customError)
        }
      }
      
      // 如果响应体是Result格式（有code和message字段）
      if (res && typeof res === 'object') {
        // 检查是否是Spring Boot默认错误格式（有status和error字段）
        if (res.status !== undefined && res.error !== undefined) {
          // Spring Boot默认错误格式
          let errorMessage = ''
          if (res.message !== undefined && res.message !== null && res.message !== '' && res.message.trim() !== '') {
            errorMessage = String(res.message)
          } else {
            // 如果message为空，根据状态码推断错误消息
            if (res.status === 401) {
              errorMessage = '未授权，请重新登录'
            } else if (res.status === 403) {
              errorMessage = '该用户已被停用'
            } else if (res.status === 500) {
              // 500错误应该显示实际的错误信息，而不是"用户名或密码错误"
              errorMessage = res.error && res.error.trim() !== '' ? String(res.error) : '系统内部错误，请稍后重试'
            } else if (res.error && res.error.trim() !== '') {
              errorMessage = String(res.error)
            } else {
              errorMessage = '请求失败，请稍后重试'
            }
          }
          
          const customError = new Error(errorMessage)
          customError.response = error.response
          customError.code = res.status
          
          // 处理401未授权
          if (res.status === 401) {
            Cookies.remove('token')
            localStorage.removeItem('userInfo')
            // 登录接口的错误不在这里显示，由登录页面处理
            if (!isLoginRequest && shouldShowError) {
              Message.error(errorMessage)
            }
          } else {
            // 其他错误自动显示消息（除非明确设置不显示）
            if (shouldShowError && !isLoginRequest) {
              Message.error(errorMessage)
            }
          }
          
          return Promise.reject(customError)
        }
        
        // 检查是否是Result格式（有code和message字段）
        if (res.code !== undefined) {
          // 提取错误消息，优先使用message字段
          let errorMessage = ''
          if (res.message !== undefined && res.message !== null && res.message !== '') {
            errorMessage = String(res.message)
          } else if (res.error !== undefined && res.error !== null && res.error !== '') {
            errorMessage = String(res.error)
          } else if (res.msg !== undefined && res.msg !== null && res.msg !== '') {
            errorMessage = String(res.msg)
          }
          
          // 如果仍然没有错误消息，使用默认消息
          if (!errorMessage || errorMessage.trim() === '') {
            if (res.code === 401) {
              errorMessage = '未授权，请重新登录'
            } else if (res.code === 403) {
              errorMessage = '该用户已被停用'
            } else if (res.code === 500) {
              errorMessage = '系统内部错误，请稍后重试'
            } else {
              errorMessage = '请求失败'
            }
          }
          
          // 创建错误对象，包含完整的响应信息
          const customError = new Error(errorMessage)
          customError.response = error.response
          customError.code = res.code
          
          // 处理401未授权，清除token但不跳转（让调用方处理）
        if (res.code === 401) {
          Cookies.remove('token')
          localStorage.removeItem('userInfo')
            // 登录接口的错误不在这里显示，由登录页面处理
            if (!isLoginRequest && shouldShowError) {
              Message.error(errorMessage)
            }
          } else {
            // 其他错误自动显示消息（除非明确设置不显示）
            // 即使code是500，也显示后端返回的具体错误消息，而不是"500错误"
            if (shouldShowError && !isLoginRequest) {
              Message.error(errorMessage)
            }
          }
          
          return Promise.reject(customError)
        } else {
          // 不是Result格式，尝试从其他可能的字段提取错误消息
          let errorMessage = ''
          if (res.error !== undefined && res.error !== null && res.error !== '') {
            errorMessage = String(res.error)
          } else if (res.message !== undefined && res.message !== null && res.message !== '') {
            errorMessage = String(res.message)
          } else if (res.msg !== undefined && res.msg !== null && res.msg !== '') {
            errorMessage = String(res.msg)
          }
          
          // 如果仍然没有错误消息，使用默认消息
          if (!errorMessage || errorMessage.trim() === '') {
            errorMessage = '请求失败'
          }
          
          const customError = new Error(errorMessage)
          customError.response = error.response
          customError.code = res.code || error.response.status
          if (shouldShowError && !config.url?.includes('/auth/logout') && !isLoginRequest) {
            Message.error(errorMessage)
          }
          return Promise.reject(customError)
        }
      }
    }
    
    // 如果HTTP状态码是500，但响应体没有Result格式，可能是未处理的异常
    // 但也可能是后端返回了HTTP 500但响应体是Result格式（某些异常处理器可能这样）
    if (error.response && error.response.status === 500) {
      // 尝试解析响应体（可能是字符串格式的JSON）
      let res = error.response.data
      if (typeof res === 'string') {
        try {
          res = JSON.parse(res)
        } catch (e) {
          // 如果解析失败，使用原始字符串
          const errorMessage = res || '系统内部错误'
          const customError = new Error(errorMessage)
          customError.response = error.response
          customError.code = 500
          // 对于登录请求，不在这里显示错误（由Login.vue处理），但确保error.message包含正确的消息
          if (shouldShowError && !config.url?.includes('/auth/logout') && !isLoginRequest) {
            Message.error(errorMessage)
          }
          return Promise.reject(customError)
        }
      }
      
      // 检查是否是Result格式
      if (res && typeof res === 'object' && res.code !== undefined) {
        // 确实是Result格式，使用其中的错误消息
        let errorMessage = '系统内部错误'
        if (res.message) {
          errorMessage = String(res.message)
        } else if (res.error) {
          errorMessage = String(res.error)
        } else if (res.msg) {
          errorMessage = String(res.msg)
        }
        
        // 确保错误消息不是"用户名或密码错误"（这不应该出现在非登录接口中）
        if (errorMessage.includes('用户名或密码错误') || errorMessage.includes('登录')) {
          errorMessage = '系统内部错误，请检查后端日志'
        }
        
        const customError = new Error(errorMessage)
        customError.response = error.response
        customError.code = res.code
        // 对于登录请求，不在这里显示错误（由Login.vue处理），但确保error.message包含正确的消息
        if (shouldShowError && !config.url?.includes('/auth/logout') && !isLoginRequest) {
          Message.error(errorMessage)
        }
        return Promise.reject(customError)
      }
      
      // 尝试从响应体中提取错误信息
      let errorMessage = '系统内部错误'
      if (res) {
        if (typeof res === 'string') {
          errorMessage = res
        } else if (res.message) {
          errorMessage = String(res.message)
        } else if (res.error) {
          errorMessage = String(res.error)
        } else if (res.msg) {
          errorMessage = String(res.msg)
        }
      } else if (error.response.data) {
        if (typeof error.response.data === 'string') {
          errorMessage = error.response.data
        }
      }
      
      // 确保错误消息不是"用户名或密码错误"（这不应该出现在非登录接口中）
      if (errorMessage.includes('用户名或密码错误') || errorMessage.includes('登录')) {
        errorMessage = '系统内部错误，请检查后端日志'
      }
      
      // 对于登录请求，不在这里显示错误（由Login.vue处理），但确保error.message包含正确的消息
      if (shouldShowError && !config.url?.includes('/auth/logout') && !isLoginRequest) {
        Message.error(errorMessage)
      }
      const customError = new Error(errorMessage)
      customError.response = error.response
      customError.code = 500
      return Promise.reject(customError)
    }
    
    // 处理其他HTTP状态码（如400, 403, 404等）
    if (error.response && error.response.status !== 200 && error.response.status !== 500) {
      const res = error.response.data
      // 如果响应体是Result格式，使用其中的消息
      if (res && res.code && res.message) {
        const errorMessage = res.message || '请求失败'
        const customError = new Error(errorMessage)
        customError.response = error.response
        customError.code = res.code
        // 对于登录请求，不在这里显示错误（由Login.vue处理），但确保error.message包含正确的消息
        if (shouldShowError && !config.url?.includes('/auth/logout') && !isLoginRequest) {
          Message.error(errorMessage)
      }
        return Promise.reject(customError)
      }
    }
    
    // 处理网络错误（如连接被拒绝、超时等）
    // config 和 shouldShowError 已经在第63-64行声明，这里不需要重复声明
    
    if (error.code === 'ECONNREFUSED' || error.message.includes('ECONNREFUSED')) {
      const errorMessage = '无法连接到服务器，请检查后端服务是否已启动'
      // 对于退出登录等操作，不显示错误提示，直接返回
      if (error.config && error.config.url && error.config.url.includes('/auth/logout')) {
        return Promise.reject(new Error(errorMessage))
      }
      if (shouldShowError) {
        Message.error(errorMessage)
      }
      return Promise.reject(new Error(errorMessage))
    }
    
    // 处理请求超时
    if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      const errorMessage = '请求超时，请稍后重试'
      if (error.config && error.config.url && error.config.url.includes('/auth/logout')) {
        return Promise.reject(new Error(errorMessage))
      }
      if (shouldShowError) {
      Message.error(errorMessage)
      }
      return Promise.reject(new Error(errorMessage))
    }
    
    // 处理其他网络错误或未知错误
    const errorMessage = error.message || '网络错误，请检查网络连接'
    // 对于退出登录等操作，不显示错误提示
    if (error.config && error.config.url && error.config.url.includes('/auth/logout')) {
      return Promise.reject(new Error(errorMessage))
    }
    // 只有在真正是网络错误时才显示默认错误消息
    if (!error.response && shouldShowError) {
    Message.error(errorMessage)
    }
    return Promise.reject(error)
  }
)

export default service

