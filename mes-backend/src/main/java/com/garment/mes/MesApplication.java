package com.garment.mes;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 服装制造业进出口贸易 MES 系统 — 后端启动类
 */
@SpringBootApplication
@MapperScan("com.garment.mes.**.mapper")
public class MesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MesApplication.class, args);
    }
}
