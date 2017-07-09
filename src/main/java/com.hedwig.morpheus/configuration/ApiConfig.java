package com.hedwig.morpheus.configuration;

import com.hedwig.morpheus.converter.MessageDtoToMessageConverter;
import com.hedwig.morpheus.converter.MqttMessageToMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by hugo. All rights reserved.
 */

@Configuration
public class ApiConfig {

    public Set<Converter> getConverters() {
        Set<Converter> converters = new HashSet<>();

        converters.add(new MessageDtoToMessageConverter());
        converters.add(new MqttMessageToMessageConverter());

        return converters;
    }

    @Bean
    public ConversionService conversionService() {
        ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
        bean.setConverters(this.getConverters());
        bean.afterPropertiesSet();

        return bean.getObject();
    }

}
