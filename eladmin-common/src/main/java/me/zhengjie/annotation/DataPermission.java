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
 * <p>
 *   用於判斷是否過濾數據權限
 *   1、如果沒有用到 @OneToOne 這種關聯關系，只需要填寫 fieldName [參考：DeptQueryCriteria.class]
 *   2、如果用到了 @OneToOne ，fieldName 和 joinName 都需要填寫，拿UserQueryCriteria.class舉例:
 *   應該是 @DataPermission(joinName = "dept", fieldName = "id")
 * </p>
 * @author Zheng Jie
 * @website https://eladmin.vip
 * @date 2020-05-07
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {

    /**
     * Entity 中的字段名稱
     */
    String fieldName() default "";

    /**
     * Entity 中與部門關聯的字段名稱
     */
    String joinName() default "";
}
