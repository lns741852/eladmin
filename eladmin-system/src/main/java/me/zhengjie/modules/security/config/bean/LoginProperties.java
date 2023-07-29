/*
 * Copyright 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version loginCode.length.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-loginCode.length.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.zhengjie.modules.security.config.bean;

import com.wf.captcha.*;
import com.wf.captcha.base.Captcha;
import lombok.Data;
//import me.zhengjie.exception.BadConfigurationException;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.utils.StringUtils;
import java.awt.*;
import java.util.Objects;

/**
 * 驗證碼參數配置
 *
 */
@Data
public class LoginProperties {

    /**
     *  帳號單用戶登入
     */
    private boolean singleLogin = false;

    private LoginCode loginCode;

    public static final String cacheKey = "user-login-cache:";

    public boolean isSingleLogin() {
        return singleLogin;
    }

    /**
     * 獲取驗證碼
     */
    public Captcha getCaptcha() {
        if (Objects.isNull(loginCode)) {
            loginCode = new LoginCode();
            if (Objects.isNull(loginCode.getCodeType())) {
                loginCode.setCodeType(LoginCodeEnum.ARITHMETIC);
            }
        }
        return switchCaptcha(loginCode);
    }

    /**
     * 驗證碼生成規則
     */
    private Captcha switchCaptcha(LoginCode loginCode) {
        Captcha captcha;    //null
        switch (loginCode.getCodeType()) {
            case ARITHMETIC:
                captcha = new FixedArithmeticCaptcha(loginCode.getWidth(), loginCode.getHeight());
                captcha.setLen(loginCode.getLength());
                break;
            case CHINESE:
                captcha = new ChineseCaptcha(loginCode.getWidth(), loginCode.getHeight());
                captcha.setLen(loginCode.getLength());
                break;
            case CHINESE_GIF:
                captcha = new ChineseGifCaptcha(loginCode.getWidth(), loginCode.getHeight());
                captcha.setLen(loginCode.getLength());
                break;
            case GIF:
                captcha = new GifCaptcha(loginCode.getWidth(), loginCode.getHeight());
                captcha.setLen(loginCode.getLength());
                break;
            case SPEC:
                captcha = new SpecCaptcha(loginCode.getWidth(), loginCode.getHeight());
                captcha.setLen(loginCode.getLength());
                break;
            default:
                //throw new BadConfigurationException("驗證碼配置錯誤！請查看 LoginCodeEnum ");
                throw new BadRequestException("驗證碼配置錯誤！請查看 LoginCodeEnum ");
        }
        if(StringUtils.isNotBlank(loginCode.getFontName())){
            captcha.setFont(new Font(loginCode.getFontName(), Font.PLAIN, loginCode.getFontSize()));
        }
        return captcha;
    }

    static class FixedArithmeticCaptcha extends ArithmeticCaptcha {
        public FixedArithmeticCaptcha(int width, int height) {
            super(width, height);
        }

        @Override
        protected char[] alphas() {
            // 隨機生成數字
            int n1 = num(1, 10), n2 = num(1, 10);
            int opt = num(3);

            // 隨機取運算結果
            int res = new int[]{n1 + n2, n1 - n2, n1 * n2}[opt];
            // 生成運算符
            char optChar = "+-x".charAt(opt);

            this.setArithmeticString(String.format("%s%c%s=?", n1, optChar, n2));
            this.chars = String.valueOf(res);

            return chars.toCharArray();
        }
    }
}
