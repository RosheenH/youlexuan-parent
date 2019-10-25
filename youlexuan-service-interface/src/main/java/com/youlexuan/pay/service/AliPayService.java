package com.youlexuan.pay.service;

import java.util.Map;

public interface AliPayService {
    //生成支付宝支付二维码
    //out_trade_no:商户订单号，需要保证不重复。total_amount：订单金额
    public Map createNative(String out_trade_no,String total_amount);

    //查询支付状态
    public Map queryPayStatus(String out_trade_no);

    //关闭支付
    public Map closePay(String out_trade_no);
}
