/*
 * Copyright 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.zhengjie.modules.security.config.bean;

import lombok.Data;

/**
 * 驗證碼Bean
 *
 */
@Data
public class LoginCode {

    /**
     * 驗證碼類型
     */
    private LoginCodeEnum codeType;
    /**
     * 驗證碼效期 分鐘
     */
    private Long expiration = 2L;
    /**
     * 驗證碼內容長度
     */
    private int length = 2;
    /**
     * 驗證碼寬度
     */
    private int width = 111;
    /**
     * 驗證碼高度
     */
    private int height = 36;
    /**
     * 字體
     */
    private String fontName;
    /**
     * 字體大小
     */
    private int fontSize = 25;

    public LoginCodeEnum getCodeType() {
        return codeType;
    }
}
