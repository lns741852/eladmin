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
package me.zhengjie.service;

import me.zhengjie.domain.SysLog;
import me.zhengjie.service.dto.SysLogQueryCriteria;
import me.zhengjie.service.dto.SysLogSmallDto;
import me.zhengjie.utils.PageResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author Zheng Jie
 * @date 2018-11-24
 */
public interface SysLogService {

    /**
     * 分頁查詢
     * @param criteria 查詢條件
     * @param pageable 分頁參數
     * @return /
     */
    Object queryAll(SysLogQueryCriteria criteria, Pageable pageable);

    /**
     * 查詢全部數據
     * @param criteria 查詢條件
     * @return /
     */
    List<SysLog> queryAll(SysLogQueryCriteria criteria);

    /**
     * 查詢用戶日志
     * @param criteria 查詢條件
     * @param pageable 分頁參數
     * @return -
     */
    PageResult<SysLogSmallDto> queryAllByUser(SysLogQueryCriteria criteria, Pageable pageable);

    /**
     * 保存日志數據
     * @param username 用戶
     * @param browser 瀏覽器
     * @param ip 請求IP
     * @param joinPoint /
     * @param sysLog 日志實體
     */
    @Async
    void save(String username, String browser, String ip, ProceedingJoinPoint joinPoint, SysLog sysLog);

    /**
     * 查詢異常詳情
     * @param id 日志ID
     * @return Object
     */
    Object findByErrDetail(Long id);

    /**
     * 導出日志
     * @param sysLogs 待導出的數據
     * @param response /
     * @throws IOException /
     */
    void download(List<SysLog> sysLogs, HttpServletResponse response) throws IOException;

    /**
     * 刪除所有錯誤日志
     */
    void delAllByError();

    /**
     * 刪除所有INFO日志
     */
    void delAllByInfo();
}
