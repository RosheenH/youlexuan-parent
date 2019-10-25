package com.youlexuan.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.youlexuan.CONSTANT;
import com.youlexuan.cart.service.CartService;
import com.youlexuan.entity.Result;
import com.youlexuan.pojogroup.Cart;
import com.youlexuan.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference(timeout = 60000)
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;


    //购物车列表
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

            String cartListString = CookieUtil.getCookieValue(request, CONSTANT.CART_LIST_KEY, "UTF-8");
            if (cartListString == null || cartListString.equals("")) {
                cartListString="[]";
            }
            List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
            if ("anonymousUser".equals(username)){//如果未登录
                return cartList_cookie;
            }else {//如果登录
                List<Cart> cartList_redis = cartService.findCartListFromRedis(username);//从reids中读取
                if (cartList_cookie.size()>0){//如果本地存在购物车
                    //合并购物车
                    cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);
                    //清除本地cookie数据
                    CookieUtil.deleteCookie(request,response,CONSTANT.CART_LIST_KEY);
                    //将合并后的数据存入redis
                    cartService.saveCartListToRedis(username,cartList_redis);

                }
                return cartList_redis;
        }

    }

    //添加商品到购物车
    @CrossOrigin(origins = "http://localhost:9105",allowCredentials = "true")
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId,Integer num){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录用户："+username);
        try{
            List<Cart> cartList_cookie = findCartList();//获取购物车列表
            List<Cart> cartList = cartService.addGoodsToCartList(cartList_cookie, itemId, num);
            if ("anonymousUser".equals(username)){//如果未登录,保存高cookie中

                CookieUtil.setCookie(request,response,CONSTANT.CART_LIST_KEY,JSON.toJSONString(cartList),3600*24,"UTF-8");
                System.out.println("向cookie存入数据");
            }else {//如果已登录，保存到redis中
                cartService.saveCartListToRedis(username,cartList);

            }
            return new Result(true,"添加成功");
        }catch (Exception e){
            e.printStackTrace();
            return  new Result(false,"添加失败");
        }
    }


}
