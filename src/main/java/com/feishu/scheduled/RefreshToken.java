package com.feishu.scheduled;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.feishu.container.TokenContainer;
import com.feishu.entity.APPKeyResDO;
import com.feishu.entity.UserAccessTokenResDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RefreshToken {
    @Value("${FeiShu.appId}")
    String appId;
    @Value("${FeiShu.appSecret}")
    String appSecret;
    @Autowired
    private TokenContainer tokenContainer;

    /**
     * 用来定时刷新token
     */
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void refreshToken() {
        String res =  HttpRequest.post("https://open.feishu.cn/open-apis/auth/v3/app_access_token/internal")
                .header("Content-Type", "application/json")
                .body("{\n" +
                        "    \"app_id\": \""+appId+"\",\n" +
                        "    \"app_secret\": \""+appSecret+"\"\n" +
                        "}")
                .execute().body();
        APPKeyResDO jsonRes = JSON.parseObject(res, APPKeyResDO.class);
        tokenContainer.setAppAccessToken(jsonRes.getApp_access_token());
         res = HttpRequest.post("https://open.feishu.cn/open-apis/authen/v1/oidc/refresh_access_token")
                .header("Authorization", "Bearer " + tokenContainer.getAppAccessToken())
                .body("{\n" +
                        "    \"grant_type\": \"refresh_token\",\n" +
                        "    \"refresh_token\": \""+tokenContainer.getRefreshToken()+"\"\n" +
                        "}").execute().body();
        UserAccessTokenResDO UserAccessToken = JSON.parseObject(res, UserAccessTokenResDO.class);
        UserAccessTokenResDO.UserAccessTokenResDOData data = UserAccessToken.getData();
        if(data.getAccess_token() == null || data.getAccess_token().isEmpty()) System.out.println("刷新token失败"+res);
        if(data.getRefresh_token() == null || data.getRefresh_token().isEmpty()) System.out.println("刷新token失败"+res);
        tokenContainer.setUserAccessToken(data.getAccess_token());
        tokenContainer.setRefreshToken(data.getRefresh_token());
//        String access_token = res.replaceAll(".*\"access_token\":\"(.*?)\".*", "$1");
//        String refresh_token = res.replaceAll(".*\"refresh_token\":\"(.*?)\".*", "$1");
//        tokenContainer.setUserAccessToken(access_token);
//        tokenContainer.setRefreshToken(refresh_token);
        System.out.println("刷新token成功："+tokenContainer.getUserAccessToken());
    }
}
