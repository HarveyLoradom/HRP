# HRP医院资源规划系统

## 项目简介

HRP（Hospital Resource Planning）医院资源规划系统，基于Spring Cloud微服务架构和Vue前端框架开发。

## 技术栈

### 后端
- Spring Boot 2.3.12
- Spring Cloud Hoxton.SR12
- Spring Cloud Alibaba 2.2.7
- MyBatis 2.2.2
- MySQL 8.0
- Nacos（服务注册与发现）
- Spring Cloud Gateway（API网关）
- JWT（身份认证）
- BCrypt（密码加密）
- Druid（数据库连接池）

### 前端
- Vue 2.6.14
- Vue Router 3.5.4
- Vuex 3.6.2
- Element UI 2.15.13
- Axios 0.27.2

## 项目结构

```
HRP/
├── hrp-gateway/         # API网关服务
├── hrp-auth/            # 认证服务（登录）
├── hrp-user/            # 用户服务
├── hrp-menu/            # 菜单服务
├── hrp-common/          # 公共模块
├── hrp-frontend/        # Vue前端项目
└── database/            # 数据库脚本
```

## 功能模块

### 1. 系统平台
- 医院字典管理（部门、职工、科室等）
- 系统字典（币种、地区、计量单位等）
- 用户管理
- 用户权限

### 2. 智能报账
- 业务办理（我的申请、我的报账等）
- 业务审批（申请审批、报账审批等）
- 财务处理（报账查询、申请查询等）
- 信用管理（职工信用查询、信用指标、信用等级等）

### 3. 合同管理
- 工作台（全合同查询）
- 合同起草
- 合同审批
- 合同执行（合同变更、归档等）

### 4. 固定资产
- 业务审批
- 采购需求
- 综合查询

### 5. 全景人力
- 薪酬管理（人员薪酬、薪酬计算等）

### 6. DIP成本
- 成本报表
- 成本分析
- 成本核算

### 7. 单机效能
- 数据采集（科室、设备等）
- 收入数据（HIS收入、设备收入等）
- 成本数据
- 设备分析报告
- 投资收益分析

## 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.6+
- Node.js 14+
- MySQL 8.0+

### 数据库初始化

1. **创建数据库**：
```sql
CREATE DATABASE IF NOT EXISTS hrp_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **执行初始化脚本**：
```sql
USE hrp_db;
SOURCE database/init.sql;
```
或者直接在 MySQL 客户端中执行 `database/init.sql` 文件。

3. **生成并插入管理员用户**：
   - 运行密码生成工具：
     ```bash
     cd backward/hrp-auth
     mvn compile exec:java -Dexec.mainClass="com.hrp.auth.util.PasswordGenerator"
     ```
   - 工具会输出包含UUID和BCrypt密码哈希的SQL语句
   - 将生成的SQL语句执行到数据库中
   - 或者参考 `database/quick_setup.sql` 文件进行设置

3. **配置数据库连接**：
   
   修改各服务的 `application.yml` 文件中的数据库连接信息，根据你的 MySQL 实际配置修改：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/hrp_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
       username: root  # 修改为你的 MySQL 用户名
       password: root  # 修改为你的 MySQL 密码
   ```
   
   **如果遇到 "Access denied" 错误**，请：
   - 确认 MySQL 用户名和密码正确
   - 或者使用以下命令重置 root 密码：
     ```sql
     ALTER USER 'root'@'localhost' IDENTIFIED BY '你的新密码';
     FLUSH PRIVILEGES;
     ```
   - 或者创建新的数据库用户：
     ```sql
     CREATE USER 'hrp_user'@'localhost' IDENTIFIED BY 'hrp_password';
     GRANT ALL PRIVILEGES ON hrp_db.* TO 'hrp_user'@'localhost';
     FLUSH PRIVILEGES;
     ```
     然后在配置文件中使用 `hrp_user` 和 `hrp_password`

4. **生成管理员用户**：
   - 运行密码生成工具：
     ```bash
     cd backward/hrp-auth
     mvn compile exec:java -Dexec.mainClass="com.hrp.auth.util.PasswordGenerator"
     ```
   - 工具会生成UUID和BCrypt加密的密码，并输出SQL插入语句
   - 将生成的SQL语句执行到数据库中
   - 默认账号：ADMIN001
   - 默认密码：123456

### 后端启动

**前置条件：需要先启动Nacos服务注册中心**

1. 下载并启动Nacos：
   - 下载地址：https://github.com/alibaba/nacos/releases
   - 启动命令（Windows）：`startup.cmd -m standalone`
   - 启动命令（Linux/Mac）：`sh startup.sh -m standalone`
   - 访问地址：http://localhost:8848/nacos
   - 默认账号/密码：nacos/nacos

2. 启动API网关：
```bash
cd backward/hrp-gateway
mvn spring-boot:run
```

3. 启动认证服务：
```bash
cd backward/hrp-auth
mvn spring-boot:run
```

4. 启动用户服务：
```bash
cd backward/hrp-user
mvn spring-boot:run
```

5. 启动菜单服务：
```bash
cd backward/hrp-menu
mvn spring-boot:run
```

6. 启动智能报账服务：
```bash
cd hrp-reimb
mvn spring-boot:run
```

7. 启动合同管理服务：
```bash
cd hrp-contract
mvn spring-boot:run
```

8. 启动固定资产服务：
```bash
cd hrp-asset
mvn spring-boot:run
```

9. 启动全景人力服务：
```bash
cd hrp-hr
mvn spring-boot:run
```

10. 启动DIP成本服务：
```bash
cd hrp-cost
mvn spring-boot:run
```

11. 启动单机效能服务：
```bash
cd hrp-efficiency
mvn spring-boot:run
```

### 前端启动

```bash
cd frontend
npm install
npm run serve
```

访问地址：http://localhost:3000

## 登录功能说明

### 登录规则
1. 账号为工号
2. 初始密码：123456（已加密存储）
3. 登录失败3次后，需要输入验证码
4. 登录失败5次后，账户锁定10分钟

### 密码加密
使用BCrypt算法对密码进行加密存储，确保密码安全。

## 菜单管理

- 每个菜单都有唯一的ID和编码
- 菜单支持树形结构（父子关系）
- 菜单权限通过用户菜单关联表控制
- 只有拥有菜单权限的用户才能访问对应菜单

## 服务端口

- Nacos: 8848
- Gateway: 8081
- Auth: 8001
- User: 8002
- Menu: 8003
- Reimb: 8004 (智能报账)
- Contract: 8005 (合同管理)
- Asset: 8006 (固定资产)
- HR: 8007 (全景人力)
- Cost: 8008 (DIP成本)
- Efficiency: 8009 (单机效能)
- Frontend: 3000

## 开发说明

### Nacos配置

修改各服务的`application.yml`文件中的Nacos连接信息（如果需要）：
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: public
        group: DEFAULT_GROUP
```

### 数据库配置

**重要：** 请根据你的 MySQL 实际配置修改各服务的 `application.yml` 文件中的数据库连接信息。

需要修改的文件：
- `backward/hrp-auth/src/main/resources/application.yml`
- `backward/hrp-menu/src/main/resources/application.yml`
- `backward/hrp-user/src/main/resources/application.yml`
- `backward/hrp-reimb/src/main/resources/application.yml`
- `backward/hrp-contract/src/main/resources/application.yml`
- `backward/hrp-asset/src/main/resources/application.yml`
- `backward/hrp-hr/src/main/resources/application.yml`
- `backward/hrp-cost/src/main/resources/application.yml`
- `backward/hrp-efficiency/src/main/resources/application.yml`

配置示例：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hrp_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root  # 修改为你的 MySQL 用户名
    password: root  # 修改为你的 MySQL 密码
```

**常见问题解决：**

1. **Access denied for user 'root'@'localhost'**
   - 检查用户名和密码是否正确
   - 如果忘记 root 密码，可以重置：
     ```sql
     ALTER USER 'root'@'localhost' IDENTIFIED BY '新密码';
     FLUSH PRIVILEGES;
     ```

2. **Public Key Retrieval is not allowed**
   - 已在 URL 中添加 `allowPublicKeyRetrieval=true` 参数，如果仍有问题，请检查 MySQL 版本

3. **数据库不存在**
   - 请先创建数据库：`CREATE DATABASE hrp_db;`
   - 然后执行初始化脚本

## 功能模块说明

### 1. 系统平台
- **部门管理**：部门信息的增删改查
- **职工管理**：职工信息的增删改查
- **科室管理**：科室信息的增删改查
- **系统字典**：系统字典的维护
- **角色管理**：角色权限的配置
- **用户管理**：用户账号的管理

### 2. 智能报账
- **我的申请**：职工提交报账申请
- **申请审批**：审批人员审批报账申请
- **报账查询**：查询已审批的报账记录

### 3. 合同管理
- **工作台**：全合同查询
- **合同起草**：创建新合同
- **合同审批**：审批合同
- **合同执行**：合同变更和归档

### 4. 固定资产
- **业务审批**：固定资产相关业务审批
- **采购需求**：采购需求的申请和管理
- **综合查询**：固定资产综合查询

### 5. 全景人力
- **人员薪酬**：查看和管理人员薪酬信息
- **薪酬计算**：计算员工薪酬

### 6. DIP成本
- **成本报表**：生成和查看成本报表
- **成本分析**：成本数据分析
- **成本核算**：成本核算管理

### 7. 单机效能
- **数据采集**：科室和设备数据采集
- **收入数据**：HIS收入和设备收入数据
- **成本数据**：成本数据管理
- **设备分析报告**：设备分析报告生成
- **投资收益分析**：投资收益分析

## 后续开发

当前已完成：
- ✅ 微服务框架搭建（7个业务服务 + Gateway + Auth + Menu + User）
- ✅ 用户登录功能（密码加密、验证码、登录失败限制）
- ✅ 菜单管理功能（菜单CRUD、树形结构、权限控制）
- ✅ 前端登录页面和菜单展示
- ✅ 所有7个功能模块的数据库表结构
- ✅ 所有功能模块的实体类、Mapper、Service、Controller
- ✅ 所有功能模块的前端页面和路由配置
- ✅ 菜单数据初始化

**注意**：部分功能的前端页面为简化版本，可根据实际需求进一步完善。

