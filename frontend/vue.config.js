module.exports = {
  devServer: {
    port: 3000,
    proxy: {
      // 静态资源路径：需要直接访问auth服务（8001），因为Gateway可能不转发静态资源
      // 或者也可以通过Gateway访问，但需要确认Gateway是否支持文件访问
      '/api/uploads': {
        target: 'http://localhost:8001',  // 静态资源直接访问auth服务
        changeOrigin: true,
        // 保持 /api 前缀，因为auth服务的WebMvcConfig映射了 /api/uploads/**
        logLevel: 'debug'
      },
      // 所有其他API请求：通过Gateway访问（8081）
      // Gateway会根据路径路由到对应的服务，并自动去掉 /api 前缀（StripPrefix=1）
      '/api': {
        target: 'http://localhost:8081',  // 通过Gateway访问
        changeOrigin: true,
        // 不需要pathRewrite，保持 /api 前缀，Gateway会处理路由和路径重写
        logLevel: 'debug'
      }
    }
  },
  lintOnSave: false
}
