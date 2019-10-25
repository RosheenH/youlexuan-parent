package com.youlexuan.search.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.youlexuan.CONSTANT;
import com.youlexuan.pojo.TbItem;
import com.youlexuan.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;




    @Override
    public Map<String, Object> search(Map searchMap) {
        //关键字空格处理
        String keywords = (String) searchMap.get("keywords");

        searchMap.put("keywords",keywords.replace(" ", ""));

        Map<String,Object> map = new HashMap<>();
        map.putAll(searchList(searchMap));

        //根据关键字查询商品分类
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);

        //查询品牌和规格表
        String category = (String) searchMap.get("category");
        category = StringUtils.isEmpty(category)?categoryList.get(0):category;
        Map brandAndSpecMap = searchBrandAndSpecList(category);
        map.putAll(brandAndSpecMap);

        return map;
    }

    private Map searchBrandAndSpecList(String category) {
        Map brandAndSpecMap = new HashMap(3);
        Long typeId = (Long) redisTemplate.boundHashOps(CONSTANT.ITEMCAT_LIST_KEY).get(category);
        if (typeId != null){
            List brandList = (List) redisTemplate.boundHashOps(CONSTANT.BRAND_LIST_KEY).get(typeId);
            brandAndSpecMap.put("brandList",brandList);

            List specList = (List) redisTemplate.boundHashOps(CONSTANT.SPEC_LIST_KEY).get(typeId);
            brandAndSpecMap.put("specList",specList);
        }
        return brandAndSpecMap;
    }

    private List searchCategoryList(Map searchMap) {
        List<String> list = new ArrayList<>();

        Query query = new SimpleQuery();
        //按照关键在查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分类项
        GroupOptions go = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(go);
        //按照分组查询
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);

        //获取需要的信息
        List<GroupEntry<TbItem>> content = page.getGroupResult("item_category").getGroupEntries().getContent();
        for (GroupEntry<TbItem> entry:content){
            list.add(entry.getGroupValue());
        }
        return list;
    }

    private Map searchList(Map searchMap) {
        Map map = new HashMap<>();
        HighlightQuery query = new SimpleHighlightQuery();
        //设置高亮项
        HighlightOptions ho = new HighlightOptions().addField("item_title");
        //定义高亮前缀
        ho.setSimplePrefix("<font style='color:red'>");
        //定义高亮后缀
        ho.setSimplePostfix("</font>");
        //设置高亮
        query.setHighlightOptions(ho);
        //添加查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);


        //按分类筛选
        if (!"".equals(searchMap.get("category"))){
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //按品牌筛选
        if (!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //按照规格筛选
        Map<String,String> specMap = (Map) searchMap.get("spec");
        for (String key:specMap.keySet()){
            Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //按照价格筛选

        if (!"".equals(searchMap.get("price"))){
            //将价格区间分割成数组
            String price = (String) searchMap.get("price");
            String[] prices = price.split("-");
            if (!prices[0].equals("0")){
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(prices[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!prices[1].equals("*")){
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(prices[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if (pageNo==null){
            pageNo=1;//默认第一页}
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageSize==null){
            pageSize=10;//默认每页数量
        }
        //从第几条数据开始查起
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);



        //排序
        String sortValue = (String) searchMap.get("sort");//ASC DESC
        String sortField = (String) searchMap.get("sortField");//排序字段
        if (sortValue != null && sortField != null){
            switch (sortValue){
                case "DESC":
                    Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                    query.addSort(sort);
                    break;
                case "ASC":
                    Sort sort1 = new Sort(Sort.Direction.ASC,"item_"+sortField);
                    query.addSort(sort1);
                    break;
                default:
                    break;
            }
        }


        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //获取高亮的数据

        for (HighlightEntry<TbItem> h:page.getHighlighted()){
            if (h.getHighlights().size()>0&&h.getHighlights().get(0).getSnipplets().size()>0){
                //给title复修改以后的值
                String title = h.getHighlights().get(0).getSnipplets().get(0);
                h.getEntity().setTitle(title);
            }
        }
        map.put("rows",page.getContent());
        map.put("totalPages",page.getTotalPages());//返回总页数
        map.put("total",page.getTotalElements());//返回总记录数
        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品ID"+goodsIdList);

        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
    }
}
