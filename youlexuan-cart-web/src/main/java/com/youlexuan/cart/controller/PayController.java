package com.youlexuan.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.youlexuan.entity.Result;
import com.youlexuan.order.service.OrderService;
import com.youlexuan.pay.service.AliPayService;
import com.youlexuan.pay.service.PayLogService;
import com.youlexuan.pojo.TbPayLog;
import com.youlexuan.utils.IdWorker;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping("/pay")
public class PayController {
    @Reference(timeout = 60000)
    private AliPayService aliPayService;
    @Reference
    private PayLogService payLogService;

    //生成二维码
    @RequestMapping("/createNative")
    public Map createNative(){
        //获取当前用户
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //到redis查询支付日志
        TbPayLog payLog = payLogService.searchPayLogFromRedis(userId);
        //判断支付日志存在
        if (payLog!=null){
            return aliPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        }else {
            return new HashMap();
        }




        /*IdWorker idWorker = new IdWorker();
        String total_amount = "1";
        String out_trade_no = new IdWorker()+"";
        return aliPayService.createNative(out_trade_no,total_amount);*/
    }

    //查询支付状态
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result= null;
        int x = 0;
        while (true){
            //调用查询接口
            Map<String,String> map = null;
            try{

                map = aliPayService.queryPayStatus(out_trade_no);
            }catch (Exception e){
                System.out.println("调用查询服务出错");
            }
            if (map == null){
                 result = new Result(false, "支付出错");
                break;
            }
            if (map.get("tradestatus")!=null&&map.get("tradestatus").equals("TRADE_SUCCESS")){
                 result = new Result(true, "支付成功");
                 //修改订单状态
                payLogService.updateOrderStatus(out_trade_no,map.get("trade_no"));
                break;
            }
            if (map.get("tradestatus")!=null&&map.get("tradestatus").equals("TRADE_CLOSED")){
                 result = new Result(true, "未支付交易超时关闭，或支付完成后全额退款");
                break;
            }
            if(map.get("tradestatus")!=null&&map.get("tradestatus").equals("TRADE_FINISHED")){//如果成功
                 result=new  Result(true, "交易结束，不可退款");
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if (x>3){
                result = new Result(false,"二维码超时");
                break;
            }

        }
        return result;
    }

}
