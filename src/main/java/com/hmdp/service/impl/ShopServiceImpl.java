package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ssh.JschUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisData;
import javafx.beans.binding.LongExpression;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CacheClient cacheClient;

    public Result queryById(long id){
        //缓存穿透
//        Shop shop = queryWithPassThrough(id);

        Shop shop = cacheClient.queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //用互斥锁解决缓存击穿
//        Shop shop =queryWithMutex(id);

//        逻辑过期解决缓存击穿
//        Shop shop = queryWithLogicalExpire(id);
        if (shop==null){
            return Result.fail("店铺不存在");
        }

        return Result.ok(shop);
    }
    public Shop queryWithMutex(long id){
        String key = CACHE_SHOP_KEY + id;

        String shopJson = stringRedisTemplate.opsForValue().get(key);

//        命中
        if (StrUtil.isNotBlank(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
//        缓存穿透结果
        if (shopJson != null) {
            Result fail = Result.fail("店铺信息不存在！");
            return null;
        }

        //实现缓存重建
        //获取互斥锁，判断是否获取成功，若果失败就休眠，成功就查询数据库
        //获取互斥锁
        String lockKey="lock:shop:"+id;
        Shop shop = null;
        try {
            boolean isLock = tryLock(lockKey);
            //获取锁失眠，休眠然后重新查询
            if (!isLock) {
                Thread.sleep(50);
                return queryWithPassThrough(id);
            }

            //获取锁成功
            shop = getById(id);

            //数据库中没有查到
            if (shop == null) {
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            //存在
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            //释放互斥锁
            unLock(lockKey);
        }


        return shop;
    }
    public Shop queryWithPassThrough(long id){
        String key = CACHE_SHOP_KEY + id;

        String shopJson = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isNotBlank(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        if (shopJson != null) {
            Result fail = Result.fail("店铺信息不存在！");
            return null;
        }

        Shop shop = getById(id);

        //数据库中没有查到
        if (shop == null) {
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        return shop;
    }
    public Shop queryWithLogicalExpire(long id){
        String key = CACHE_SHOP_KEY + id;
        //1.从redis查询商铺信息
        String shopJson = stringRedisTemplate.opsForValue().get(key);

        //2.判断是否存在
        if (StrUtil.isBlank(shopJson)) {
//            3.未命中，返回null
            return null;
        }

        //4.命中，把json反序列化为对象

        //5.判断是否过期

        //5.1未过期，返回商铺信息

        //5.2已过期，需要缓存重建

        //6缓存重建

        //6.1获取互斥锁
        //6.2判断是否获取锁成功
        //6.3成功，开启独立线程实现缓存重建
        //6.4 返回过期的商铺信息



        if (shopJson != null) {
            Result fail = Result.fail("店铺信息不存在！");
            return null;
        }

        Shop shop = getById(id);

        //数据库中没有查到
        if (shop == null) {
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        return shop;
    }

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.MINUTES);
        return BooleanUtil.isTrue(flag);
    }

    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }
    public void saveShop2Redis(long id,Long expireSeconds) {
        //查询店铺信息
        Shop shop = getById(id);
        //封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        //写入rdeis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY+id,JSONUtil.toJsonStr(redisData));


    }
}
