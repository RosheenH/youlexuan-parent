package com.youlexuan.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.youlexuan.mapper.TbGoodsDescMapper;
import com.youlexuan.mapper.TbGoodsMapper;
import com.youlexuan.mapper.TbItemCatMapper;
import com.youlexuan.mapper.TbItemMapper;
import com.youlexuan.page.service.ItemPageService;
import com.youlexuan.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
/*
* 用来生成详情页面
* 页面生成在page-web下的webapp下
*
*
*
* freeMarker使用步骤：
    第一步：创建一个 Configuration 对象，直接 new 一个对象。构造方法的参数就是 freemarker的版本号。
    第二步：设置模板文件所在的路径。
    第三步：设置模板文件使用的字符集。一般就是 utf-8.
    第四步：加载一个模板，创建一个模板对象。
    第五步：创建一个模板使用的数据集，可以是 pojo 也可以是 map。一般是 Map。
    第六步：创建一个 Writer 对象，一般创建一 FileWriter 对象，指定生成的文件名。
    第七步：调用模板对象的 process 方法输出文件。
    第八步：关闭流
*
*
* */
public class ItemPageSerivceImpl implements ItemPageService {

    @Value("${pagedir}")
    private String pagedir;

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItemMapper itemMapper;


    @Override
    public boolean genItemHtml(Long goodsId) {
        Writer fileWriter = null;
        try {
            //第一步
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            //第二步、第三步在配置文件中已配好
            //第四步
            Template template = configuration.getTemplate("item.ftl");
            //第五步
            Map dataModel = new HashMap();
            //加载商品表数据
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods",goods);
            //加载商品扩展表数据
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc",goodsDesc);
            //商品分类
            String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            dataModel.put("itemCat1",itemCat1);
            dataModel.put("itemCat2",itemCat2);
            dataModel.put("itemCat3",itemCat3);
            //sku列表
            TbItemExample exam = new TbItemExample();
            TbItemExample.Criteria criteria = exam.createCriteria();
            criteria.andStatusEqualTo("1");//状态为有效
            criteria.andGoodsIdEqualTo(goodsId);//指定spu id
            exam.setOrderByClause("is_default desc");//按照状态降序，保证第一个为默认
            List<TbItem> itemList = itemMapper.selectByExample(exam);
            dataModel.put("itemList",itemList);
            //第六步
            fileWriter = new FileWriter(pagedir + goodsId + ".html");
            //第七步
            template.process(dataModel,fileWriter);
            return  true;

        } catch (Exception e) {
            e.printStackTrace();
            return  false;
        }finally {
            try {
               if (fileWriter != null) fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
