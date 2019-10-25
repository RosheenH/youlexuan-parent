package com.youlexuan.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.youlexuan.CONSTANT;
import com.youlexuan.cart.service.CartService;
import com.youlexuan.mapper.TbItemMapper;
import com.youlexuan.pojo.TbItem;
import com.youlexuan.pojo.TbOrderItem;
import com.youlexuan.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    /*购物车服务实现类
    *	//1.根据商品SKU ID查询SKU商品信息
		//2.获取商家ID
		//3.根据商家ID判断购物车列表中是否存在该商家的购物车
		//4.如果购物车列表中不存在该商家的购物车
		//4.1 新建购物车对象
		//4.2 将新建的购物车对象添加到购物车列表
		//5.如果购物车列表中存在该商家的购物车
		// 查询购物车明细列表中是否存在该商品
		//5.1. 如果没有，新增购物车明细
		//5.2. 如果有，在原购物车明细上添加数量，更改金额
    *
    * cartList是购物车中的历史商品
    * */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //根据商品sku id 查询sku商品信息；
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        //获取商家id
        String sellerId = item.getSellerId();
        //根据商家id判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList,sellerId);
        //如果购物车列表中不存在该商家的购物车
        if (cart == null){
            //新建购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());

            List<TbOrderItem> orderItemList = new ArrayList<>();
            TbOrderItem orderItem = createOrderItem(item,num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //将购物车对象添加到购物车列表
            cartList.add(cart);
            
        }else {
            //如果购物车列表中存在该商家的购物车
            //判断购物车明细列表中是否存在该商品
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId);
            if (orderItem == null){
                //如果没有，新增购物车明细
                orderItem = createOrderItem(item,num);
                cart.getOrderItemList().add(orderItem);
            }else {
                //如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()));
                //如果数量操作后小于等于0，则删除
                if (orderItem.getNum()<=0){
                    cart.getOrderItemList().remove(orderItem);//移除购物车明细
                }
                //如果移除后cart的明细数量为0，则将cart移除
                if (cart.getOrderItemList().size()==0){
                    cartList.remove(cart);
                }

            }
        }
        return cartList;


    }

    ////判断购物车明细列表中是否存在该商品
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem:orderItemList){
            if (itemId.longValue() == orderItem.getItemId().longValue()){
                return orderItem;
            }
        }
        return null;
    }

    //创建订单详情
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if (num<=0){
            throw new RuntimeException("非法数量");
        }
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;
    }

    //根据商家id判断购物车列表中是否存在该商家的购物车
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart:cartList){
            if (sellerId.equals(cart.getSellerId())){
                return cart;
            }
        }
        return null;
    }

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车数据....."+username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps(CONSTANT.CART_LIST_KEY).get(username);
        if (cartList==null){
            cartList=new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis存入购物车数据...."+username);
        redisTemplate.boundHashOps(CONSTANT.CART_LIST_KEY).put(username,cartList);

    }

    //合并购物车
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        System.out.println("合并购物车");
        for (Cart cart:cartList2){
            for (TbOrderItem orderItem:cart.getOrderItemList()){
                 cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }
}
