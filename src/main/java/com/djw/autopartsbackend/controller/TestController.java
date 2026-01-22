//package com.djw.autopartsbackend.controller;
//
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author dengjiawen
// * @since 2026/1/21
// */
//@RestController
//public class TestController {
//    private static List<byte[]> leak = new ArrayList<>();
//
//    @GetMapping("/leak")
//    public String leak() {
//        // 每次请求加 1MB
//        leak.add(new byte[1024 * 1024]);
//        return "OK";
//    }
//}
