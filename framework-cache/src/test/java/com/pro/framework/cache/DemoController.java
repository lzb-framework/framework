//
//package com.pro.framework.cache;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("demo")
//public class DemoController {
//    @Autowired
//    private DemoService demoService;
//
//    @RequestMapping("clear")
//    public String clear() {
//        demoService.evictCache("key1");
//        return "clear ok";
//    }
//
//    @RequestMapping("getCachedData")
//    public String getCachedData() {
//        return JSONUtil.toJsonStr(demoService.getCachedData("key1"));
//    }
//
//}
