package com.youlexuan.task;

import com.youlexuan.CONSTANT;
import com.youlexuan.mapper.TbSeckillGoodsMapper;
import com.youlexuan.pojo.TbSeckillGoods;
import com.youlexuan.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class SeckillTask {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    //刷新秒杀商品
    @Scheduled(cron="0 * * * * ?")
    public void refreshSeckillGoods(){
        System.out.println("执行了任务调度"+new Date());
        //查询所有的秒杀商品建的集合
        List ids = new ArrayList(redisTemplate.boundHashOps(CONSTANT.SECKILLGOODS_LIST_KEY).keys());
        //查询需要秒杀的商品列表
        TbSeckillGoodsExample exam = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = exam.createCriteria();
        criteria.andStatusEqualTo("1");//审核通过
        criteria.andStockCountGreaterThan(0);//剩余库存大于0
        criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于等于当前时间
        criteria.andEndTimeGreaterThan(new Date());//结束时间大于当前时间
        if (ids.size()>0){
            criteria.andIdNotIn(ids);
        }
        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(exam);
        //装入缓存
        for (TbSeckillGoods seckillGoods:seckillGoodsList){
            redisTemplate.boundHashOps(CONSTANT.SECKILLGOODS_LIST_KEY).put(seckillGoods.getId(),seckillGoods);
        }
        System.out.println("将"+seckillGoodsList.size()+"条商品装入缓存");

    }

    //移除过期的秒杀商品
    @Scheduled(cron = "0 * * * * ?")
    public void removeSeckillGoods(){
        System.out.println("移除秒杀商品任务正在执行");
        //扫描缓存中秒杀商品列表，发现过期的移除
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps(CONSTANT.SECKILLGOODS_LIST_KEY).values();
        for (TbSeckillGoods seckillGoods:seckillGoodsList){
            if (seckillGoods.getEndTime().getTime()<new Date().getTime()){
                //结束日期小于当前日期
                seckillGoodsMapper.updateByPrimaryKey(seckillGoods);//向数据库保存记录
                redisTemplate.boundHashOps(CONSTANT.SECKILLGOODS_LIST_KEY).delete(seckillGoods.getId());//删除缓存
                System.out.println("移除秒杀商品"+seckillGoods.getId());
            }
        }

    }
}
