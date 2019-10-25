package com.youlexuan.pay.service;

import com.youlexuan.pojo.TbPayLog;

public interface PayLogService {
    //根据用户查询日志
    public TbPayLog searchPayLogFromRedis(String userId);

    //修改订单状态
    public void updateOrderStatus(String out_trade_no,String trade_no);
}
