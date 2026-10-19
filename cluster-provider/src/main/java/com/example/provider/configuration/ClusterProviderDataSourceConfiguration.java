package com.example.provider.configuration;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * @author xujiajun
 * @since 2026/10/16
 */
@Slf4j
@Configuration
@MapperScan(basePackages = "com.example.provider.dal.dao", sqlSessionTemplateRef = "sqlSessionTemplate")
public class ClusterProviderDataSourceConfiguration {

    private static final String MAPPER_LOCATION = "classpath*:com/example/provider/dal/mapper/*.xml";

    @ConfigurationProperties(prefix = "cluster-provider.datasource")
    @Bean("dataSource")
    public DataSource druidDataSource() {
        return new DruidDataSource();
    }

    @Bean("sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);

        PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = patternResolver.getResources(MAPPER_LOCATION);

        sqlSessionFactoryBean.setMapperLocations(resources);

        DruidDataSource druidDataSource = (DruidDataSource) dataSource;
        String driverClassName = druidDataSource.getDriverClassName();
        String url = druidDataSource.getUrl();
        String username = druidDataSource.getUsername();
        String password = druidDataSource.getPassword();
        log.warn("datasource info: driver-class-name: {} \n url: {} \n username: {} \n password: {} \n", driverClassName, url, username, password);
        return sqlSessionFactoryBean.getObject();
    }

    @Bean("sqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
