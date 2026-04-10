package com.ticketscale.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve assets estáticos de /admin/assets/**
        registry.addResourceHandler("/admin/assets/**")
                .addResourceLocations("classpath:/static/admin/assets/");

        // Para qualquer outra rota sob /admin/** que não seja um arquivo, serve o index.html
        registry.addResourceHandler("/admin/**")
                .addResourceLocations("classpath:/static/admin/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        
                        // Se o recurso existe e é legível, serve ele (ex: favicon.svg, index.html)
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        
                        // Caso contrário (é uma rota da SPA), serve o index.html
                        return new ClassPathResource("/static/admin/index.html");
                    }
                });
    }
}
