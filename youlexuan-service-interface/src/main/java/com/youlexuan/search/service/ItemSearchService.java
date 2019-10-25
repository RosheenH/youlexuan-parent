package com.youlexuan.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
    //搜索
    public Map<String,Object> search(Map searchMap);

    /*
     * 更新到索引库
     * 导入数据
     */

    public void importList(List list);

    //索引库中删除数据
    public void deleteByGoodsIds(List goodsIdList);
}
