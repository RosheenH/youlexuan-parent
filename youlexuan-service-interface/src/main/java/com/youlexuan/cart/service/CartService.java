package com.youlexuan.cart.service;

import com.youlexuan.pojogroup.Cart;

import java.util.List;

public interface CartService {
    //添加商品到购物车
    public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);

    //从redis中查找数据
    public List<Cart> findCartListFromRedis(String username);
    //将数据添加到redis中
    public void saveCartListToRedis(String username,List<Cart> cartList);
    //合并本地和redis中的购物车
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);


}
