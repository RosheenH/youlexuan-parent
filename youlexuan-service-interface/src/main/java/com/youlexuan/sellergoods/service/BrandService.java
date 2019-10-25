package com.youlexuan.sellergoods.service;

import com.youlexuan.entity.PageResult;
import com.youlexuan.pojo.TbBrand;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface BrandService {

    List<TbBrand> findAll();

    PageResult findPage(TbBrand tbBrand,int pageNum,int pageSize);

    void add(TbBrand tbBrand);

    void update(TbBrand tbBrand);

    TbBrand findOne(Long id);

    void del(Long[] ids);

    List<Map> findBrand();

}
