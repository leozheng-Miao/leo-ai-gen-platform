package com.leo.leoaigenplatform.langgraph4j.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ImageCollectionServiceTest {

    @Resource
    private ImageCollectionService imageCollectionService;

    @Test
    void testTechWebsiteImageCollection() {
        String result = imageCollectionService.collectImages("创建一个技术博客网站，需要展示编程教程和系统架构");
        Assertions.assertNotNull(result);
        System.out.println("技术网站收集到的图片: " + result);
    }

/**
 * 测试电商网站图片收集功能的方法
 * 该方法用于验证imageCollectionService的collectImages方法是否能正常收集电商网站相关的图片
 */
    @Test
    void testEcommerceWebsiteImageCollection() {
        // 调用imageCollectionService的collectImages方法，传入电商网站描述作为参数
        String result = imageCollectionService.collectImages("创建一个电商购物网站，需要展示商品和品牌形象");
        // 验证返回结果不为空，确保方法正常执行并返回有效数据
        Assertions.assertNotNull(result);
        // 打印收集到的图片结果，用于调试和查看
        System.out.println("电商网站收集到的图片: " + result);
    }
}