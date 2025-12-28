package com.leo.leoaigenplatform.generate;

import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.ColumnConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;

public class Codegen {

    private static final String BASE_PACKAGE = "com.leo.leoaigenplatform.generate";
    private static final String[] TABLE_NAMES = {"chat_history"};

    public static void main(String[] args) {

        Dict dict = YamlUtil.loadByPath("application.yml");
        Map<String, Object> sources = dict.getByPath("spring.datasource");
        String url = (String) sources.get("url");
        String username = (String) sources.get("username");
        String password = (String) sources.get("password");
        //配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        //创建配置内容，两种风格都可以。
//        GlobalConfig globalConfig = createGlobalConfigUseStyle1();
        GlobalConfig globalConfig = createGlobalConfigUseStyle2();

        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        //生成代码
        generator.generate();
    }

    public static GlobalConfig createGlobalConfigUseStyle1() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        //设置根包
        globalConfig.setBasePackage("com.test");

        //设置表前缀和只生成哪些表
        globalConfig.setTablePrefix("tb_");
        globalConfig.setGenerateTable("tb_account", "tb_account_session");

        //设置生成 entity 并启用 Lombok
        globalConfig.setEntityGenerateEnable(true);
        globalConfig.setEntityWithLombok(true);
        //设置项目的JDK版本，项目的JDK为14及以上时建议设置该项，小于14则可以不设置
        globalConfig.setEntityJdkVersion(17);

        //设置生成 mapper
        globalConfig.setMapperGenerateEnable(true);

        //可以单独配置某个列
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setColumnName("tenant_id");
        columnConfig.setLarge(true);
        columnConfig.setVersion(true);
        globalConfig.setColumnConfig("tb_account", columnConfig);

        return globalConfig;
    }

    public static GlobalConfig createGlobalConfigUseStyle2() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        //设置根包
        globalConfig.getPackageConfig()
                .setBasePackage(BASE_PACKAGE);

        //设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                .setGenerateTable(TABLE_NAMES);


        globalConfig.enableTableDef();
        //设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setJdkVersion(21);

        //设置生成 mapper
        globalConfig.enableMapper();
        globalConfig.enableMapperXml();

        //设置生成 service 和 serviceImpl
        globalConfig.enableService();
        globalConfig.enableServiceImpl();
        globalConfig.enableController();

        //设置生成 controller 时，是否启用 Restful 风格
        globalConfig.getJavadocConfig()
                .setAuthor("Leo")
                .setSince("1.0.1");

        return globalConfig;
    }
}