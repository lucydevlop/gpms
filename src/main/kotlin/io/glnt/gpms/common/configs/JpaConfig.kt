package io.glnt.gpms.common.configs

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager",
    basePackages = ["io.glnt.gpms.model.repository"]
)
class JpaConfig {

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource")
    fun dataSource(): DataSource {

        val dataSource = DataSourceBuilder.create().type(HikariDataSource::class.java).build()

        // UTF-8이 아닌 레거시 데이터베이스에 연결시 한글 문자열을 온전히 처리하기 위해 사용
        dataSource.connectionInitSql = "SET NAMES utf8mb4"

        return dataSource
    }

    @Primary
    @Bean
    fun entityManagerFactory(
        builder: EntityManagerFactoryBuilder,
        @Qualifier("dataSource") dataSource: DataSource): LocalContainerEntityManagerFactoryBean {

        return builder
            .dataSource(dataSource)
            .packages("io.glnt.gpms.model.entity")
//            .persistenceUnit("somedb")
            .build()
    }

    @Primary
    @Bean
    fun transactionManager(

        @Qualifier("entityManagerFactory") entityManagerFactory: EntityManagerFactory
    ): PlatformTransactionManager {

        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = entityManagerFactory

        return transactionManager
    }
}