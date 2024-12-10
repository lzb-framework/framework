package com.pro.framework.cache;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CacheTests {

    @Autowired
    private TestRestTemplate restTemplate;

//    @LocalServerPort
    private int port;

//    @Test
    @SneakyThrows
    public void testWeb1() {
        System.out.println("启动该端口 " + port);
        Thread.sleep(1000000);
    }
//    @Test
    @SneakyThrows
    public void testWeb2() {
        System.out.println("启动该端口 " + port);
        Thread.sleep(1000000);
    }
}
