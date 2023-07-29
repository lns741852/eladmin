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
package me.zhengjie.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.utils.enums.DataScopeEnum;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import java.util.List;

/**
 * 獲取當前登錄的用戶
 * @author Zheng Jie
 * @date 2019-01-17
 */
@Slf4j
public class SecurityUtils {

    /**
     * 獲取當前登錄的用戶
     * @return UserDetails
     */
    public static UserDetails getCurrentUser() {
        UserDetailsService userDetailsService = SpringContextHolder.getBean(UserDetailsService.class);
        return userDetailsService.loadUserByUsername(getCurrentUsername());
    }

    /**
     * 獲取系統用戶名稱
     *
     * @return 系統用戶名稱
     */
    public static String getCurrentUsername() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new BadRequestException(HttpStatus.UNAUTHORIZED, "當前登錄狀態過期");
        }
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        throw new BadRequestException(HttpStatus.UNAUTHORIZED, "找不到當前登錄的信息");
    }

    /**
     * 獲取系統用戶ID
     * @return 系統用戶ID
     */
    public static Long getCurrentUserId() {
        UserDetails userDetails = getCurrentUser();
        // 將 Java 對象轉換為 JSONObject 對象
        JSONObject jsonObject = (JSONObject) JSON.toJSON(userDetails);
        return jsonObject.getJSONObject("user").getLong("id");
    }

    /**
     * 獲取當前用戶的數據權限
     * @return /
     */
    public static List<Long> getCurrentUserDataScope(){
        UserDetails userDetails = getCurrentUser();
        // 將 Java 對象轉換為 JSONObject 對象
        JSONObject jsonObject = (JSONObject) JSON.toJSON(userDetails);
        JSONArray jsonArray = jsonObject.getJSONArray("dataScopes");
        return JSON.parseArray(jsonArray.toJSONString(), Long.class);
    }

    /**
     * 獲取數據權限級別
     * @return 級別
     */
    public static String getDataScopeType() {
        List<Long> dataScopes = getCurrentUserDataScope();
        if(dataScopes.size() != 0){
            return "";
        }
        return DataScopeEnum.ALL.getValue();
    }
}
