package com.analysis.admin.Config;

import com.analysis.admin.Mapper.AnalysisSimpleReportMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.analysis.admin.Mapper", sqlSessionFactoryRef = "analysisSqlSessionFactory")
public class AnalysisSqlSessionConfiger {
    @Bean(name = "analysisSource")
    @ConfigurationProperties(prefix = "spring.datasource.analysis")
    public DataSource dataSource(){
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "analysisSqlSessionFactory")
    public SqlSessionFactory analysisSessionFactory(@Qualifier("analysisSource") DataSource dataSource)throws Exception {
        SqlSessionFactoryBean analysisSessionFactoryBean = new SqlSessionFactoryBean();
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setDefaultFetchSize(1000);
        configuration.setDefaultStatementTimeout(60);
        analysisSessionFactoryBean.setDataSource(dataSource);
        analysisSessionFactoryBean.setConfiguration(configuration);
        analysisSessionFactoryBean.setMapperLocations(
            new PathMatchingResourcePatternResolver().getResources("classpath:mybatis/mapper/*.xml")
        );
//        analysisSessionFactoryBean.setTypeAliasesPackage("com.hsf.admin.pojo.Entities");
        return analysisSessionFactoryBean.getObject();
    }

    @Bean(name = "analysisTransactionManager")
    public DataSourceTransactionManager transactionManager(@Qualifier("analysisSource") DataSource dataSource){
        // 事务
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "analysisSqlSessionTemple")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("analysisSqlSessionFactory") SqlSessionFactory sessionFactory){
        // template
        SqlSessionTemplate template = new SqlSessionTemplate(sessionFactory);
        template.getMapper(AnalysisSimpleReportMapper.class);
        return template;
    }
}
