package com.youlexuan.content.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.youlexuan.content.service.ContentService;
import com.youlexuan.pojo.TbContent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/content")
public class ContentController {
    @Reference
    private ContentService contentService;
    /*
    * 根据广告分类Id查询广告列表
    * */
    @RequestMapping("/findByCategoryId")
    public List<TbContent> findByCategoryId(Long categoryId){
        return contentService.findByCategoryId(categoryId);
    }
}
