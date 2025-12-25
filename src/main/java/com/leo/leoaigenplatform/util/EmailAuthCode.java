package com.leo.leoaigenplatform.util;

import cn.hutool.extra.mail.MailAccount;
import org.springframework.beans.factory.annotation.Value;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2025-12-22 11:28
 **/
public class EmailAuthCode {

    @Value("${mail.host}")
    String host;
    @Value("${mail.port}")
    int port;
    @Value("${mail.from}")
    String from;
    @Value("${mail.pass}")
    String pass;

    private MailAccount account;

    public void sendEmailCode(String targetEmail, String authCode) {
        account = new MailAccount();
        account.setHost(host);
        account.setPort(port);
        account.setAuth(true);
        account.setFrom(from);
        account.setPass(pass);
        account.setSslEnable(true);


    }


}
