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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author /
 */
@Component
@SuppressWarnings({"unchecked", "all"})
public class RedisUtils {
    private static final Logger log = LoggerFactory.getLogger(RedisUtils.class);

    private RedisTemplate<Object, Object> redisTemplate;

    public RedisUtils(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        this.redisTemplate.setKeySerializer(new StringRedisSerializer());
        this.redisTemplate.setStringSerializer(new StringRedisSerializer());
    }

    /**
     * 指定緩存失效時間
     *
     * @param key  鍵
     * @param time 時間(秒) 注意:這里將會替換原有的時間
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * 指定緩存失效時間
     *
     * @param key      鍵
     * @param time     時間(秒) 注意:這里將會替換原有的時間
     * @param timeUnit 單位
     */
    public boolean expire(String key, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, timeUnit);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * 根據 key 獲取過期時間
     *
     * @param key 鍵 不能為null
     * @return 時間(秒) 返回0代表為永久有效
     */
    public long getExpire(Object key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 查找匹配key
     *
     * @param pattern key
     * @return /
     */
    public List<String> scan(String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).build();
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        RedisConnection rc = Objects.requireNonNull(factory).getConnection();
        Cursor<byte[]> cursor = rc.scan(options);
        List<String> result = new ArrayList<>();
        while (cursor.hasNext()) {
            result.add(new String(cursor.next()));
        }
        try {
            RedisConnectionUtils.releaseConnection(rc, factory);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 分頁查詢 key
     *
     * @param patternKey key
     * @param page       頁碼
     * @param size       每頁數目
     * @return /
     */
    public List<String> findKeysForPage(String patternKey, int page, int size) {
        ScanOptions options = ScanOptions.scanOptions().match(patternKey).build();
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        RedisConnection rc = Objects.requireNonNull(factory).getConnection();
        Cursor<byte[]> cursor = rc.scan(options);
        List<String> result = new ArrayList<>(size);
        int tmpIndex = 0;
        int fromIndex = page * size;
        int toIndex = page * size + size;
        while (cursor.hasNext()) {
            if (tmpIndex >= fromIndex && tmpIndex < toIndex) {
                result.add(new String(cursor.next()));
                tmpIndex++;
                continue;
            }
            // 獲取到滿足條件的數據後,就可以退出了
            if (tmpIndex >= toIndex) {
                break;
            }
            tmpIndex++;
            cursor.next();
        }
        try {
            RedisConnectionUtils.releaseConnection(rc, factory);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 判斷key是否存在
     *
     * @param key 鍵
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 刪除緩存
     *
     * @param key 可以傳一個值 或多個
     */
    public void del(String... keys) {
        if (keys != null && keys.length > 0) {
            if (keys.length == 1) {
                boolean result = redisTemplate.delete(keys[0]);
                log.debug("--------------------------------------------");
                log.debug(new StringBuilder("刪除緩存：").append(keys[0]).append("，結果：").append(result).toString());
                log.debug("--------------------------------------------");
            } else {
                Set<Object> keySet = new HashSet<>();
                for (String key : keys) {
                    if (redisTemplate.hasKey(key))
                        keySet.add(key);
                }
                long count = redisTemplate.delete(keySet);
                log.debug("--------------------------------------------");
                log.debug("成功刪除緩存：" + keySet.toString());
                log.debug("緩存刪除數量：" + count + "個");
                log.debug("--------------------------------------------");
            }
        }
    }

    /**
     * 批量模糊刪除key
     * @param pattern
     */
    public void scanDel(String pattern){
        ScanOptions options = ScanOptions.scanOptions().match(pattern).build();
        try (Cursor<byte[]> cursor = redisTemplate.executeWithStickyConnection(
                (RedisCallback<Cursor<byte[]>>) connection -> (Cursor<byte[]>) new ConvertingCursor<>(
                        connection.scan(options), redisTemplate.getKeySerializer()::deserialize))) {
            while (cursor.hasNext()) {
                redisTemplate.delete(cursor.next());
            }
        }
    }

    // ============================String=============================

    /**
     * 普通緩存獲取
     *
     * @param key 鍵
     * @return 值
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 批量獲取
     *
     * @param keys
     * @return
     */
    public List<Object> multiGet(List<String> keys) {
        List list = redisTemplate.opsForValue().multiGet(Sets.newHashSet(keys));
        List resultList = Lists.newArrayList();
        Optional.ofNullable(list).ifPresent(e-> list.forEach(ele-> Optional.ofNullable(ele).ifPresent(resultList::add)));
        return resultList;
    }

    /**
     * 普通緩存放入
     *
     * @param key   鍵
     * @param value 值
     * @return true成功 false失敗
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 普通緩存放入並設置時間
     *
     * @param key   鍵
     * @param value 值
     * @param time  時間(秒) time要大於0 如果time小於等於0 將設置無限期，注意:這里將會替換原有的時間
     * @return true成功 false 失敗
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 普通緩存放入並設置時間
     *
     * @param key      鍵
     * @param value    值
     * @param time     時間，注意:這里將會替換原有的時間
     * @param timeUnit 類型
     * @return true成功 false 失敗
     */
    public boolean set(String key, Object value, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, timeUnit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    // ================================Map=================================

    /**
     * HashGet
     *
     * @param key  鍵 不能為null
     * @param item 項 不能為null
     * @return 值
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 獲取hashKey對應的所有鍵值
     *
     * @param key 鍵
     * @return 對應的多個鍵值
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);

    }

    /**
     * HashSet
     *
     * @param key 鍵
     * @param map 對應多個鍵值
     * @return true 成功 false 失敗
     */
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * HashSet
     *
     * @param key  鍵
     * @param map  對應多個鍵值
     * @param time 時間(秒) 注意:如果已存在的hash表有時間,這里將會替換原有的時間
     * @return true成功 false失敗
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 向一張hash表中放入數據,如果不存在將創建
     *
     * @param key   鍵
     * @param item  項
     * @param value 值
     * @return true 成功 false失敗
     */
    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 向一張hash表中放入數據,如果不存在將創建
     *
     * @param key   鍵
     * @param item  項
     * @param value 值
     * @param time  時間(秒) 注意:如果已存在的hash表有時間,這里將會替換原有的時間
     * @return true 成功 false失敗
     */
    public boolean hset(String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 刪除hash表中的值
     *
     * @param key  鍵 不能為null
     * @param item 項 可以使多個 不能為null
     */
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判斷hash表中是否有該項的值
     *
     * @param key  鍵 不能為null
     * @param item 項 不能為null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * hash遞增 如果不存在,就會創建一個 並把新增後的值返回
     *
     * @param key  鍵
     * @param item 項
     * @param by   要增加幾(大於0)
     * @return
     */
    public double hincr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * hash遞減
     *
     * @param key  鍵
     * @param item 項
     * @param by   要減少記(小於0)
     * @return
     */
    public double hdecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    // ============================set=============================

    /**
     * 根據key獲取Set中的所有值
     *
     * @param key 鍵
     * @return
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根據value從一個set中查詢,是否存在
     *
     * @param key   鍵
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 將數據放入set緩存
     *
     * @param key    鍵
     * @param values 值 可以是多個
     * @return 成功個數
     */
    public long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 將set數據放入緩存
     *
     * @param key    鍵
     * @param time   時間(秒) 注意:這里將會替換原有的時間
     * @param values 值 可以是多個
     * @return 成功個數
     */
    public long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return count;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 獲取set緩存的長度
     *
     * @param key 鍵
     * @return
     */
    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 移除值為value的
     *
     * @param key    鍵
     * @param values 值 可以是多個
     * @return 移除的個數
     */
    public long setRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return 0;
        }
    }

    // ===============================list=================================

    /**
     * 獲取list緩存的內容
     *
     * @param key   鍵
     * @param start 開始
     * @param end   結束 0 到 -1代表所有值
     * @return
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 獲取list緩存的長度
     *
     * @param key 鍵
     * @return
     */
    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 通過索引 獲取list中的值
     *
     * @param key   鍵
     * @param index 索引 index>=0時， 0 表頭，1 第二個元素，依次類推；index<0時，-1，表尾，-2倒數第二個元素，依次類推
     * @return
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 將list放入緩存
     *
     * @param key   鍵
     * @param value 值
     * @return
     */
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 將list放入緩存
     *
     * @param key   鍵
     * @param value 值
     * @param time  時間(秒) 注意:這里將會替換原有的時間
     * @return
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 將list放入緩存
     *
     * @param key   鍵
     * @param value 值
     * @return
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 將list放入緩存
     *
     * @param key   鍵
     * @param value 值
     * @param time  時間(秒) 注意:這里將會替換原有的時間
     * @return
     */
    public boolean lSet(String key, List<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根據索引修改list中的某條數據
     *
     * @param key   鍵
     * @param index 索引
     * @param value 值
     * @return /
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 移除N個值為value
     *
     * @param key   鍵
     * @param count 移除多少個
     * @param value 值
     * @return 移除的個數
     */
    public long lRemove(String key, long count, Object value) {
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return 0;
        }
    }

    /**
     * @param prefix 前綴
     * @param ids    id
     */
    public void delByKeys(String prefix, Set<Long> ids) {
        Set<Object> keys = new HashSet<>();
        for (Long id : ids) {
            keys.addAll(redisTemplate.keys(new StringBuffer(prefix).append(id).toString()));
        }
        long count = redisTemplate.delete(keys);
        // 此處提示可自行刪除
        log.debug("--------------------------------------------");
        log.debug("成功刪除緩存：" + keys.toString());
        log.debug("緩存刪除數量：" + count + "個");
        log.debug("--------------------------------------------");
    }
}
