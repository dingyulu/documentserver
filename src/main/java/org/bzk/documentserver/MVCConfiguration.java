package org.bzk.documentserver;

import org.bzk.documentserver.propertie.DocumentServerProperties;
import org.bzk.documentserver.utils.CustomMap;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.annotation.Resource;

/**
 * @Author 2023/2/27 11:59 ly
 **/
@Configuration
public class MVCConfiguration implements WebMvcConfigurer {
    @Resource(name = "thymeleafViewResolver")
    private ThymeleafViewResolver thymeleafViewResolver;
    @Resource
    private DocumentServerProperties properties;

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        if (thymeleafViewResolver != null) {

            thymeleafViewResolver.setStaticVariables(CustomMap.build(1).pu1("documentServerApiJs", properties.getApi()));
        }
    }
}
