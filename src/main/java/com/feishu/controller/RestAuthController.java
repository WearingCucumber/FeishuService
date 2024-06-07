package com.feishu.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.feishu.container.TokenContainer;
import com.feishu.entity.APPKeyResDO;
import com.feishu.entity.Key;
import com.feishu.entity.UserAccessTokenResDO;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.request.AuthFeishuRequest;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@Slf4j
@RestController
@RequestMapping("/oauth")
public class RestAuthController {
    @Autowired
    private TokenContainer tokenContainer;
    @Value("${FeiShu.appId}")
    String appId;
    @Value("${FeiShu.appSecret}")
    String appSecret;
    @Value("${FeiShu.redirectUrl}")
    String redirectUri ;
    @RequestMapping("/render")
    public void renderAuth(HttpServletResponse response) throws IOException {
        AuthRequest authRequest = getAuthRequest();
        response.sendRedirect(authRequest.authorize(AuthStateUtils.createState()));
    }

    @RequestMapping("/callback/feishu")
    public Object login(AuthCallback callback) {
        tokenContainer.setCode(callback.getCode());
        String res =  HttpRequest.post("https://open.feishu.cn/open-apis/auth/v3/app_access_token/internal")
                .header("Content-Type", "application/json")
                .body("{\n" +
                        "    \"app_id\": \""+appId+"\",\n" +
                        "    \"app_secret\": \""+appSecret+"\"\n" +
                        "}")
                .execute().body();
        APPKeyResDO jsonRes = JSON.parseObject(res, APPKeyResDO.class);
        tokenContainer.setAppAccessToken(jsonRes.getApp_access_token());
        tokenContainer.setTenantAccessToken(jsonRes.getTenant_access_token());
        String access =  HttpRequest.post("https://open.feishu.cn/open-apis/authen/v1/oidc/access_token")
                .header("Content-Type", "application/json")
                .header("Authorization","Bearer "+tokenContainer.getAppAccessToken())
                .body("{\n" +
                        "    \"grant_type\": \"authorization_code\",\n" +
                        "    \"code\": \""+tokenContainer.getCode()+"\"\n" +
                        "}").execute().body();
        UserAccessTokenResDO UserAccessToken = JSON.parseObject(access, UserAccessTokenResDO.class);
        UserAccessTokenResDO.UserAccessTokenResDOData data = UserAccessToken.getData();
        if(data.getAccess_token() == null || data.getAccess_token().isEmpty()) return  "登录失败";
        if(data.getRefresh_token() == null || data.getRefresh_token().isEmpty()) return  "登录失败";
        tokenContainer.setUserAccessToken(data.getAccess_token());
        tokenContainer.setRefreshToken(data.getRefresh_token());
//        tokenContainer.setUserAccessToken(access.replaceAll(".*access_token\":\"(.*?)\".*", "$1"));
//        tokenContainer.setRefreshToken(access.replaceAll(".*refresh_token\":\"(.*?)\".*", "$1"));
        return "授权成功";
    }
    @RequestMapping("/getUserToken")
    public String getUserToken(){

        log.info(DateTime.now().toString("YYYY-MM-dd-hh-mm")+"获取了Token");
        return "Bearer "+tokenContainer.getUserAccessToken();
    }
    private AuthRequest getAuthRequest() {
        return new AuthFeishuRequest(AuthConfig.builder()
                .clientId(appId)
                .clientSecret(appSecret)
                .redirectUri(redirectUri)
                .build());
    }

}
