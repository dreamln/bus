/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.oauth.provider;

import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.cache.metric.ExtendCache;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.exception.AuthorizedException;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.http.Httpx;
import org.aoju.bus.oauth.Builder;
import org.aoju.bus.oauth.Context;
import org.aoju.bus.oauth.Registry;
import org.aoju.bus.oauth.magic.AccToken;
import org.aoju.bus.oauth.magic.Callback;
import org.aoju.bus.oauth.magic.Message;
import org.aoju.bus.oauth.magic.Property;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * 微博登录
 *
 * @author Kimi Liu
 * @version 5.9.8
 * @since JDK 1.8+
 */
public class WeiboProvider extends DefaultProvider {

    public WeiboProvider(Context context) {
        super(context, Registry.WEIBO);
    }

    public WeiboProvider(Context context, ExtendCache extendCache) {
        super(context, Registry.WEIBO, extendCache);
    }

    public static String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected AccToken getAccessToken(Callback Callback) {
        JSONObject object = JSONObject.parseObject(doPostAuthorizationCode(Callback.getCode()));
        if (object.containsKey("error")) {
            throw new AuthorizedException(object.getString("error_description"));
        }
        return AccToken.builder()
                .accessToken(object.getString("access_token"))
                .uid(object.getString("uid"))
                .openId(object.getString("uid"))
                .expireIn(object.getIntValue("expires_in"))
                .build();
    }

    @Override
    protected Property getUserInfo(AccToken token) {
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "OAuth2 " + String.format("uid=%s&access_token=%s",
                token.getUid(), token.getAccessToken()));
        header.put("API-RemoteIP", getLocalIp());

        String response = Httpx.get(userInfoUrl(token), null, header);
        JSONObject object = JSONObject.parseObject(response);

        if (object.containsKey("error")) {
            throw new AuthorizedException(object.getString("error"));
        }
        return Property.builder()
                .uuid(object.getString("id"))
                .username(object.getString("name"))
                .avatar(object.getString("profile_image_url"))
                .blog(StringKit.isEmpty(object.getString("url")) ? "https://weibo.com/" + object.getString("profile_url") : object
                        .getString("url"))
                .nickname(object.getString("screen_name"))
                .location(object.getString("location"))
                .remark(object.getString("description"))
                .gender(Normal.Gender.getGender(object.getString("gender")))
                .token(token)
                .source(source.toString())
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param token authToken
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken token) {
        return Builder.fromUrl(source.userInfo())
                .queryParam("access_token", token.getAccessToken())
                .queryParam("uid", token.getUid())
                .build();
    }

    @Override
    public Message revoke(AccToken token) {
        JSONObject object = JSONObject.parseObject(doGetRevoke(token));
        if (object.containsKey("error")) {
            return Message.builder().errcode(Builder.ErrorCode.FAILURE.getCode()).errmsg(object.getString("error")).build();
        }
        // 返回 result = true 表示取消授权成功，否则失败
        Builder.ErrorCode status = object.getBooleanValue("result") ? Builder.ErrorCode.SUCCESS : Builder.ErrorCode.FAILURE;
        return Message.builder().errcode(status.getCode()).errmsg(status.getMsg()).build();
    }

}
