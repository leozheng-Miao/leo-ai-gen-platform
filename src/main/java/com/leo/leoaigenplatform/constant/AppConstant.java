package com.leo.leoaigenplatform.constant;

/**
 * @program: leo-ai-gen-platform
 * @description: 应用常量
 * @author: Miao Zheng
 * @date: 2025-12-24 13:32
 **/
public interface AppConstant {
    Integer GOOD_APP_PRIORITY = 99;
    Integer DEFAULT_APP_PRIORITY = 0;

    String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";
    String FILE_DEPLOY_ROOT_DIR = System.getProperty("user.dir") + "/tmp/deploy_output";

}
