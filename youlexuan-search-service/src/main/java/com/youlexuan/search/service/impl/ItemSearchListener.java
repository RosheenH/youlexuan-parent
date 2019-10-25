package com.youlexuan.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.youlexuan.pojo.TbItem;
import com.youlexuan.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

public class ItemSearchListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;


    @Override
    public void onMessage(Message message) {
        System.out.println("监听接收到消息");
        try{
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            List<TbItem> itemList = JSON.parseArray(text, TbItem.class);
            for (TbItem item:itemList){
                System.out.println(item.getId()+""+item.getTitle());
                Map specMap = JSON.parseObject(item.getSpec());//将spec字段中的json字符串转换为map
                item.setSpecMap(specMap);//给带注解字段赋值
            }
            itemSearchService.importList(itemList);//导入
            System.out.println("成功导入打索引库");

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
