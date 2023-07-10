/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.zhengjie.modules.security.security;

import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.ExpiredJwtException;
import me.zhengjie.modules.security.config.bean.SecurityProperties;
import me.zhengjie.modules.security.service.UserCacheManager;
import me.zhengjie.modules.security.service.dto.OnlineUserDto;
import me.zhengjie.modules.security.service.OnlineUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Objects;

/**
 * @author /
 */
public class TokenFilter extends GenericFilterBean {
    private static final Logger log = LoggerFactory.getLogger(TokenFilter.class);

    private final TokenProvider tokenProvider;
    private final SecurityProperties properties;
    private final OnlineUserService onlineUserService;
    private final UserCacheManager userCacheManager;

    /**
     * @param tokenProvider     Token
     * @param properties        JWT
     * @param onlineUserService 用戶載在線
     * @param userCacheManager    用戶緩存
     */
    public TokenFilter(TokenProvider tokenProvider, SecurityProperties properties, OnlineUserService onlineUserService, UserCacheManager userCacheManager) {
        this.properties = properties;
        this.onlineUserService = onlineUserService;
        this.tokenProvider = tokenProvider;
        this.userCacheManager = userCacheManager;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String token = resolveToken(httpServletRequest);
        // Token 為空的不需要去查 Redis
        if (StrUtil.isNotBlank(token)) {
            OnlineUserDto onlineUserDto = null;
            boolean cleanUserCache = false;
            try {
                String loginKey = tokenProvider.loginKey(token);
                onlineUserDto = onlineUserService.getOne(loginKey);
            } catch (ExpiredJwtException e) {
                log.error(e.getMessage());
                cleanUserCache = true;
            } finally {
                if (cleanUserCache || Objects.isNull(onlineUserDto)) {
                    userCacheManager.cleanUserCache(String.valueOf(tokenProvider.getClaims(token).get(TokenProvider.AUTHORITIES_KEY)));
                }
            }
            // StringUtils.hasText(token) 判斷是否不含非法字符
            if (onlineUserDto != null && StringUtils.hasText(token)) {
                Authentication authentication = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // Token 續期
                tokenProvider.checkRenewal(token);
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * 初步檢測Token
     *
     * @param request /
     * @return /
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(properties.getHeader());
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(properties.getTokenStartWith())) {
            // 去掉前墜
            return bearerToken.replace(properties.getTokenStartWith(), "");
        } else {
            log.debug("非法Token：{}", bearerToken);
        }
        return null;
    }
}
