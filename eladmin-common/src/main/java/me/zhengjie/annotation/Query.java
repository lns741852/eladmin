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
package me.zhengjie.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Zheng Jie
 * @date 2019-6-4 13:52:30
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    // Dong ZhaoYang 2017/8/7 基本對象的屬性名
    String propName() default "";
    // Dong ZhaoYang 2017/8/7 查詢方式
    Type type() default Type.EQUAL;

    /**
     * 連接查詢的屬性名，如User類中的dept
     */
    String joinName() default "";

    /**
     * 默認左連接
     */
    Join join() default Join.LEFT;

    /**
     * 多字段模糊搜索，僅支持String類型字段，多個用逗號隔開, 如@Query(blurry = "email,username")
     */
    String blurry() default "";

    enum Type {
        // 相等
        EQUAL
        //  大於等於
        , GREATER_THAN
        //  小於等於
        , LESS_THAN
        //  中模糊查詢
        , INNER_LIKE
        //  左模糊查詢
        , LEFT_LIKE
        //  右模糊查詢
        , RIGHT_LIKE
        // 大於
        ,GREATER_THAN_NQ
        //  小於
        , LESS_THAN_NQ
        //  包含
        , IN
        // 不包含
        , NOT_IN
        // 不等於
        ,NOT_EQUAL
        // between
        ,BETWEEN
        // 不為空
        ,NOT_NULL
        // 為空
        ,IS_NULL,
        //  對應SQL: SELECT * FROM table WHERE FIND_IN_SET('querytag', table.tags);
        FIND_IN_SET
    }

    /**
     * @author Zheng Jie
     * 適用於簡單連接查詢，覆雜的請自定義該注解，或者使用sql查詢
     */
    enum Join {
        /** jie 2019-6-4 13:18:30 */
        LEFT, RIGHT, INNER
    }

}

