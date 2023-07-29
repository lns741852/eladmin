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
package me.zhengjie.aspect;

import lombok.extern.slf4j.Slf4j;
import me.zhengjie.domain.SysLog;
import me.zhengjie.service.SysLogService;
import me.zhengjie.utils.RequestHolder;
import me.zhengjie.utils.SecurityUtils;
import me.zhengjie.utils.StringUtils;
import me.zhengjie.utils.ThrowableUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Zheng Jie
 * @date 2018-11-24
 */
@Component
@Aspect
@Slf4j
public class LogAspect {

    private final SysLogService sysLogService;

    ThreadLocal<Long> currentTime = new ThreadLocal<>();

    public LogAspect(SysLogService sysLogService) {
        this.sysLogService = sysLogService;
    }

    /**
     * 配置切入點 @Log註解
     */
    @Pointcut("@annotation(me.zhengjie.annotation.Log)")
    public void logPointcut() {
        // 該方法無方法體,主要為了讓同類中其他方法使用此切入點
    }

    /**
     * 配置環繞通知(切入點前後)
     *
     */
    @Around("logPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        currentTime.set(System.currentTimeMillis());
        result = joinPoint.proceed();
        //計時
        SysLog sysLog = new SysLog("INFO",System.currentTimeMillis() - currentTime.get());
        currentTime.remove();
        HttpServletRequest request = RequestHolder.getHttpServletRequest();
        sysLogService.save(getUsername(), StringUtils.getBrowser(request), StringUtils.getIp(request),joinPoint, sysLog);
        return result;
    }

    /**
     * 配置異常通知
     *
     * @param joinPoint join point for advice
     * @param e exception
     */
    @AfterThrowing(pointcut = "logPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        SysLog sysLog = new SysLog("ERROR",System.currentTimeMillis() - currentTime.get());
        currentTime.remove();
        sysLog.setExceptionDetail(ThrowableUtil.getStackTrace(e).getBytes());
        HttpServletRequest request = RequestHolder.getHttpServletRequest();
        sysLogService.save(getUsername(), StringUtils.getBrowser(request), StringUtils.getIp(request), (ProceedingJoinPoint)joinPoint, sysLog);
    }

    public String getUsername() {
        try {
            return SecurityUtils.getCurrentUsername();
        }catch (Exception e){
            return "";
        }
    }
}
