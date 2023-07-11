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
package me.zhengjie.modules.security.config.bean;

import lombok.Data;

/**
 * Jwt參數配置
 *
 */
@Data
public class SecurityProperties {

    /**
     * Request Headers ： Authorization
     */
    private String header;

    /**
     * 令牌前綴，最後留個空格 Bearer
     */
    private String tokenStartWith;

    /**
     * 必須使用最少88位的Base64對該令牌進行編碼
     */
    private String base64Secret;

    /**
     * 令牌過期時間 此處單位/毫秒
     */
    private Long tokenValidityInSeconds;

    /**
     * 在線用戶 key，根據 key 查詢 redis 中在線用戶數據
     */
    private String onlineKey;

    /**
     * 驗證碼 key
     */
    private String codeKey;

    /**
     * token 續期檢查
     */
    private Long detect;

    /**
     * 續期時間
     */
    private Long renew;

    public String getTokenStartWith() {
        return tokenStartWith + " ";
    }
}
