package at.porscheinformatik.cucumber.web.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
@EnableConfigurationProperties({ResourceProperties.class})
public class WebMvcConfig extends WebMvcConfigurerAdapter
{
    @Autowired
    private ResourceProperties resourceProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        Integer cachePeriod = resourceProperties.getCachePeriod();

        registry.addResourceHandler("/**/*.*")
            .addResourceLocations(resourceProperties.getStaticLocations())
            .setCachePeriod(cachePeriod);

        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/index.html")
            .setCachePeriod(cachePeriod)
            .resourceChain(true)
            .addResolver(new PathResourceResolver()
            {
                @Override
                protected Resource getResource(String resourcePath, Resource location) throws IOException
                {
                    return location.exists() && location.isReadable() ? location : null;
                }
            });
    }
}