package com.youlexuan.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.youlexuan.CONSTANT;
import com.youlexuan.mapper.TbOrderMapper;
import com.youlexuan.mapper.TbPayLogMapper;
import com.youlexuan.pay.service.PayLogService;
import com.youlexuan.pojo.TbOrder;
import com.youlexuan.pojo.TbPayLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
@Service
public class PayLogServiceImpl  implements PayLogService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbPayLogMapper payLogMapper;
    @Autowired
    private TbOrderMapper orderMapper;


    @Override
    public TbPayLog searchPayLogFromRedis(String userId) {
        return (TbPayLog)redisTemplate.boundHashOps(CONSTANT.PAYLOG_LIST_KEY).get(userId);
    }

    @Override
    public void updateOrderStatus(String out_trade_no, String trade_no) {
        //修改支付日志状态
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        payLog.setPayTime(new Date());
        payLog.setTradeState("1");//已支付
        payLog.setTransactionId(trade_no);//交易号
        payLogMapper.updateByPrimaryKey(payLog);
        //修改订单状态
        String orderList = payLog.getOrderList();//获取订单号列表
        String[] orderIds = orderList.split(",");//获取订单号数组
        for (String orderId:orderIds){
            TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
            if (order != null){
                order.setStatus("2");//已付款
                orderMapper.updateByPrimaryKey(order);
            }
        }
        //清除redis缓存数据
        redisTemplate.boundHashOps(CONSTANT.PAYLOG_LIST_KEY).delete(payLog.getUserId());

    }
}
