<h1 style="text-align: center">ELADMIN 后台管理系统</h1>
<div style="text-align: center">



#### 項目簡介
一个基于 Spring Boot 2.6.4 、 Spring Boot Jpa、 JWT、Spring Security、Redis、Vue的前后端分离的后台管理系统
</a>


####  系統功能
- 用户管理：提供用户的相关配置，新增用户后，默认密码为123456
- 角色管理：对权限与菜单进行分配，可根据部门设置角色的数据权限
- 菜单管理：已实现菜单动态路由，后端可配置化，支持多级菜单
- 部门管理：可配置系统组织架构，树形表格展示
- 岗位管理：配置各个部门的职位
- 字典管理：可维护常用一些固定的数据，如：状态，性别等
- 系统日志：记录用户操作日志与异常日志，方便开发人员定位排错
- SQL监控：采用druid 监控数据库访问性能，默认用户名admin，密码123456
- 定时任务：整合Quartz做定时任务，加入任务日志，任务运行情况一目了然
- 代码生成：高灵活度生成前后端代码，减少大量重复的工作任务
- 邮件工具：配合富文本，发送html格式的邮件
- 七牛云存储：可同步七牛云存储的数据到系统，无需登录七牛云直接操作云数据
- 支付宝支付：整合了支付宝支付并且提供了测试账号，可自行测试
- 服务监控：监控服务器的负载情况
- 运维管理：一键部署你的应用

#### 項目結構
项目采用按功能分模块的开发方式，结构如下

- `eladmin-common` 为系统的公共模块，各种工具类，公共配置存在该模块

- `eladmin-system` 为系统核心模块也是项目入口模块，也是最终需要打包部署的模块

- `eladmin-logging` 为系统的日志模块，其他模块如果需要记录日志需要引入该模块

- `eladmin-tools` 为第三方工具模块，包含：邮件、七牛云存储、本地存储、支付宝

- `eladmin-generator` 为系统的代码生成模块，支持生成前后端CRUD代码

#### 詳細結構

```
- eladmin-common 公共模块
    - annotation 为系统自定义注解
    - aspect 自定义注解的切面
    - base 提供了Entity、DTO基类和mapstruct的通用mapper
    - config 自定义权限实现、redis配置、swagger配置、Rsa配置等
    - exception 项目统一异常的处理
    - utils 系统通用工具类
- eladmin-system 系统核心模块（系统启动入口）
	- config 配置跨域与静态资源，与数据权限
	    - thread 线程池相关
	- modules 
		-security (權限控制)
			-config
				-bean
					-loginCode(驗證碼bean)
					-loginCodeEnum(驗證碼enum)
					-LoginProperties(驗證碼配置管理)
					-SecurityProperties(Jwt配置管理)
				-ConfigBeanConfiguration(類別參數讀取application.yml)
				-SpringSecurityConfig(Spring Scurity配置)
			-rest
				-AuthorizationController(用戶API)
				-OnlineController(在線用戶API)
			-security
				-JwtAccessDeniedHandler(自訂返回403)
				-JwtAuthenticationEntryPoint(自訂返回401)
				-TokenConfigurer(繼承SecurityConfigurerAdapter)
				-TokenFilter(過濾器)
				-TokenProvider(Token配置管理)
			-service
				-dto
					-JwtUserDto(實現UserDetails)
					-AuthorityDto(避免Role序列化問題)
				-UserDetailsServiceImpl(實現UserDetailsService)
				
- eladmin-logging 系统日志模块
- eladmin-tools 系统第三方工具模块
- eladmin-generator 系统代码生成模块
```
