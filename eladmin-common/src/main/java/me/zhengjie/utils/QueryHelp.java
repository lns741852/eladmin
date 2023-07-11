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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
//import me.zhengjie.annotation.DataPermission;
import me.zhengjie.annotation.Query;
import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 通用模糊查詢，傳入(類別，自訂查詢類，固定類別CriteriaBuilder)
 *
 */
@Slf4j
@SuppressWarnings({"unchecked","all"})
public class QueryHelp {

    public static <R, Q> Predicate getPredicate(Root<R> root, Q query, CriteriaBuilder cb) {
        List<Predicate> list = new ArrayList<>();
        if(query == null){
            return cb.and(list.toArray(new Predicate[0]));
        }
        // 數據權限驗證
//        DataPermission permission = query.getClass().getAnnotation(DataPermission.class);
//        if(permission != null){
//            // 獲取數據權限
//            List<Long> dataScopes = SecurityUtils.getCurrentUserDataScope();
//            if(CollectionUtil.isNotEmpty(dataScopes)){
//                if(StringUtils.isNotBlank(permission.joinName()) && StringUtils.isNotBlank(permission.fieldName())) {
//                    Join join = root.join(permission.joinName(), JoinType.LEFT);
//                    list.add(getExpression(permission.fieldName(),join, root).in(dataScopes));
//                } else if (StringUtils.isBlank(permission.joinName()) && StringUtils.isNotBlank(permission.fieldName())) {
//                    list.add(getExpression(permission.fieldName(),null, root).in(dataScopes));
//                }
//            }
//        }
        try {
            Map<String, Join> joinKey = new HashMap<>();
            //反射獲取query對象
            List<Field> fields = getAllFields(query.getClass(), new ArrayList<>());
            for (Field field : fields) {
                boolean accessible = field.isAccessible();
                // 設置對象的訪問權限，保證對private的屬性的訪
                field.setAccessible(true);
                //獲取@Query註解類，註解的值會帶入
                Query q = field.getAnnotation(Query.class);
                if (q != null) {
                    String propName = q.propName();
                    String joinName = q.joinName();
                    String blurry = q.blurry();
                    String attributeName = isBlank(propName) ? field.getName() : propName;
                    // 獲取參數類型
                    Class<?> fieldType = field.getType();
                    Object val = field.get(query);
                    if (ObjectUtil.isNull(val) || "".equals(val)) {
                        continue;
                    }
                    Join join = null;
                    // 模糊多參數
                    if (ObjectUtil.isNotEmpty(blurry)) {
                        String[] blurrys = blurry.split(",");
                        List<Predicate> orPredicate = new ArrayList<>();
                        for (String s : blurrys) {
                            orPredicate.add(cb.like(root.get(s).as(String.class), "%" + val.toString() + "%"));
                        }
                        //存入list
                        Predicate[] p = new Predicate[orPredicate.size()];
                        list.add(cb.or(orPredicate.toArray(p)));
                        continue;
                    }
                    if (ObjectUtil.isNotEmpty(joinName)) {
                        join = joinKey.get(joinName);
                        if(join == null){
                            String[] joinNames = joinName.split(">");
                            for (String name : joinNames) {
                                switch (q.join()) {
                                    case LEFT:
                                        if(ObjectUtil.isNotNull(join) && ObjectUtil.isNotNull(val)){
                                            join = join.join(name, JoinType.LEFT);
                                        } else {
                                            join = root.join(name, JoinType.LEFT);
                                        }
                                        break;
                                    case RIGHT:
                                        if(ObjectUtil.isNotNull(join) && ObjectUtil.isNotNull(val)){
                                            join = join.join(name, JoinType.RIGHT);
                                        } else {
                                            join = root.join(name, JoinType.RIGHT);
                                        }
                                        break;
                                    case INNER:
                                        if(ObjectUtil.isNotNull(join) && ObjectUtil.isNotNull(val)){
                                            join = join.join(name, JoinType.INNER);
                                        } else {
                                            join = root.join(name, JoinType.INNER);
                                        }
                                        break;
                                    default: break;
                                }
                            }
                            joinKey.put(joinName, join);
                        }
                    }

                   //暫時沒用到
                    switch (q.type()) {
                        case EQUAL:
                            list.add(cb.equal(getExpression(attributeName,join,root)
                                    .as((Class<? extends Comparable>) fieldType),val));
                            break;
                        case GREATER_THAN:
                            list.add(cb.greaterThanOrEqualTo(getExpression(attributeName,join,root)
                                    .as((Class<? extends Comparable>) fieldType), (Comparable) val));
                            break;
                        case LESS_THAN:
                            list.add(cb.lessThanOrEqualTo(getExpression(attributeName,join,root)
                                    .as((Class<? extends Comparable>) fieldType), (Comparable) val));
                            break;
                        case LESS_THAN_NQ:
                            list.add(cb.lessThan(getExpression(attributeName,join,root)
                                    .as((Class<? extends Comparable>) fieldType), (Comparable) val));
                            break;
                        case INNER_LIKE:
                            list.add(cb.like(getExpression(attributeName,join,root)
                                    .as(String.class), "%" + val.toString() + "%"));
                            break;
                        case LEFT_LIKE:
                            list.add(cb.like(getExpression(attributeName,join,root)
                                    .as(String.class), "%" + val.toString()));
                            break;
                        case RIGHT_LIKE:
                            list.add(cb.like(getExpression(attributeName,join,root)
                                    .as(String.class), val.toString() + "%"));
                            break;
                        case IN:
                            if (CollUtil.isNotEmpty((Collection<Object>)val)) {
                                list.add(getExpression(attributeName,join,root).in((Collection<Object>) val));
                            }
                            break;
                        case NOT_IN:
                            if (CollUtil.isNotEmpty((Collection<Object>)val)) {
                                list.add(getExpression(attributeName,join,root).in((Collection<Object>) val).not());
                            }
                            break;
                        case NOT_EQUAL:
                            list.add(cb.notEqual(getExpression(attributeName,join,root), val));
                            break;
                        case NOT_NULL:
                            list.add(cb.isNotNull(getExpression(attributeName,join,root)));
                            break;
                        case IS_NULL:
                            list.add(cb.isNull(getExpression(attributeName,join,root)));
                            break;
                        case BETWEEN:
                            List<Object> between = new ArrayList<>((List<Object>)val);
                            if(between.size() == 2){
                                list.add(cb.between(getExpression(attributeName, join, root).as((Class<? extends Comparable>) between.get(0).getClass()),
                                        (Comparable) between.get(0), (Comparable) between.get(1)));
                            }
                            break;
                        case FIND_IN_SET:
                            list.add(cb.greaterThan(cb.function("FIND_IN_SET", Integer.class,
                                    cb.literal(val.toString()), root.get(attributeName)), 0));
                            break;
                        default: break;
                    }
                }
                field.setAccessible(accessible);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        int size = list.size();
        return cb.and(list.toArray(new Predicate[size]));
    }

    @SuppressWarnings("unchecked")
    private static <T, R> Expression<T> getExpression(String attributeName, Join join, Root<R> root) {
        if (ObjectUtil.isNotEmpty(join)) {
            return join.get(attributeName);
        } else {
            return root.get(attributeName);
        }
    }

    private static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }


    /**
     *  反射
     * @param clazz
     * @param fields
     * @return
     */
    public static List<Field> getAllFields(Class clazz, List<Field> fields) {
        if (clazz != null) {
           // 透過反射獲取參數  Field[] declaredFields = clazz.getDeclaredFields();
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            getAllFields(clazz.getSuperclass(), fields);
        }
        return fields;
    }
}
