<h1 style="text-align: center">ELADMIN 后台管理系统</h1>
<div style="text-align: center">



#### 項目簡介
一個基於 Spring Boot 2.6.4 、 Spring Boot Jpa、 JWT、Spring Security、Redis、Vue的前後端分離的後台管理系統
</a>



#### 詳細結構

```
- eladmin-common 公共模块
    - annotation 为系统自定义注解
    - aspect 自定义注解的切面
    - base (提供了Entity、DTO基類和mapstruct的通用mapper)
		

    - config (自定義權限實現、redis配置、swagger配置、Rsa配置等)

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
