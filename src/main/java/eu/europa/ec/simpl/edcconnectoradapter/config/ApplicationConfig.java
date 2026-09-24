package eu.europa.ec.simpl.edcconnectoradapter.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Profile("!test")
@Configuration
@ComponentScan(
        basePackages = {
            "eu.europa.ec.simpl.edcconnectoradapter",
            "eu.europa.ec.simpl.data1.common.config.feign",
            "eu.europa.ec.simpl.data1.common.config.openapi",
            "eu.europa.ec.simpl.data1.common.config.webmvc",
            "eu.europa.ec.simpl.data1.common.controller.advice.root",
            "eu.europa.ec.simpl.data1.common.controller.status"
        })
@EnableCaching
@EnableFeignClients(basePackages = "eu.europa.ec.simpl.edcconnectoradapter")
@EnableWebMvc
public class ApplicationConfig {}
