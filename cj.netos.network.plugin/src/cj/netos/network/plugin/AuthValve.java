package cj.netos.network.plugin;

import cj.netos.network.*;
import cj.netos.network.util.Encript;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static cj.netos.network.plugin.AuthMode.accessToken;

public class AuthValve implements IValve {
    OkHttpClient client;
    PluginConfig config;

    public AuthValve(INetworkServiceProvider site) {
        this.client = (OkHttpClient) site.getService("$.http");
        this.config = (PluginConfig) site.getService("$.config");
    }

    @Override
    public void nextError(NetworkFrame frame, Throwable error, IPipeline pipeline) throws CircuitException {
        pipeline.nextError(frame, error, this);
    }

    @Override
    public void flow(NetworkFrame frame, IPipeline pipeline) throws CircuitException {
        if (pipeline.principal() != null) {
            pipeline.nextFlow(frame, this);
            return;
        }
        String authMode = "";
        if (frame.containsHead("auth-mode")) {
            authMode = frame.head("auth-mode");
        }
        if (frame.containsParameter("auth-mode")) {
            authMode = frame.parameter("auth-mode");
        }
        if (StringUtil.isEmpty(authMode)) {
            throw new CircuitException("404", "未指定认证模式");
        }
        AuthMode mode = AuthMode.valueOf(authMode);
        switch (mode) {
            case accessToken:
                String accessToken = frame.parameter("accessToken");
                if (StringUtil.isEmpty(accessToken)) {
                    throw new CircuitException("404", "accessToken为空");
                }
                try {
                    authByAccessToken(accessToken, pipeline);
                    NetworkFrame f = new NetworkFrame(String.format("auth /notify/?person=%s&peer=%s network/1.0", pipeline.principal().principal(), pipeline.principal().peer()));
                    pipeline.nextFlow(f, this);
                } catch (Exception e) {
                    Channel channel = (Channel) pipeline.attachment();
                    channel.close();
                    CircuitException ce = new CircuitException("500", e);
                    nextError(frame, ce, pipeline);
                    throw ce;
                }
                break;
            case password:
                String person = frame.parameter("person");
                String password = frame.parameter("password");
                String peer = frame.parameter("peer");
                if (StringUtil.isEmpty(person)) {
                    throw new CircuitException("404", "person为空");
                }
                if (StringUtil.isEmpty(password)) {
                    throw new CircuitException("404", "password为空");
                }
                if (StringUtil.isEmpty(peer)) {
                    throw new CircuitException("404", "peer为空");
                }
                try {
                    authByPassword(person, password, peer, pipeline);
                    NetworkFrame f = new NetworkFrame(String.format("auth /notify/?person=%s&peer=%s network/1.0", person, peer));
                    pipeline.nextFlow(f, this);
                } catch (Exception e) {
                    Channel channel = (Channel) pipeline.attachment();
                    channel.close();
                    CircuitException ce = new CircuitException("500", e);
                    nextError(frame, ce, pipeline);
                    throw ce;
                }
                break;
        }

    }

    private void authByPassword(String person, String password, String peer, IPipeline pipeline) throws CircuitException, IOException {
        int pos = person.indexOf("@");
        if (pos < 0) {
            throw new CircuitException("500", "person格式错误");
        }
        String accountCode = person.substring(0, pos);
        String appid = person.substring(pos + 1);
        Map<String, Object> appKeyPair = _getAppKeyPair(appid);
        _doAuthByPassword(appKeyPair, accountCode, password, peer, pipeline);
    }

    private void _doAuthByPassword(Map<String, Object> appKeyPair, String accountCode, String password, String peer, IPipeline pipeline) throws CircuitException, IOException {
        String url = String.format("%s?device=%s&accountCode=%s&password=%s", config.ports.get("uc.auth"), peer, accountCode, password);
        String nonce = Encript.md5(String.format("%s%s", UUID.randomUUID().toString(), System.currentTimeMillis()));
        String sign = Encript.md5(String.format("%s%s%s", appKeyPair.get("appKey"), nonce, appKeyPair.get("appSecret")));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Rest-Command", "auth")
                .addHeader("app-id", appKeyPair.get("appid") + "")
                .addHeader("app-key", appKeyPair.get("appKey") + "")
                .addHeader("app-nonce", nonce)
                .addHeader("app-sign", sign)
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        if (response.code() >= 400) {
            throw new CircuitException(response.code() + "", response.message());
        }
        String body = response.body().string();
        Map<String, Object> map = new Gson().fromJson(body, HashMap.class);
        if (Double.valueOf(map.get("status") + "") >= 400) {
            throw new CircuitException(map.get("status") + "", map.get("message") + "");
        }
        String json = map.get("dataText") + "";
        map = new Gson().fromJson(json, HashMap.class);
        Map<String, Object> subject = (Map<String, Object>) map.get("subject");
        String person = subject.get("person") + "";
        List<String> roles = (List<String>) subject.get("roles");
        Map<String, Object> token = (Map<String, Object>) map.get("token");
        String device = token.get("device") + "";
        IPrincipal principal = new DefaultPrincipal(person, device, roles);
        pipeline.principal(principal);
    }

    private Map<String, Object> _getAppKeyPair(String appid) throws CircuitException, IOException {
        String url = String.format("%s?appid=%s", config.ports.get("uc.platform"), appid);
        String nonce = Encript.md5(String.format("%s%s", UUID.randomUUID().toString(), System.currentTimeMillis()));
        String sign = Encript.md5(String.format("%s%s%s", config.appKey, nonce, config.appSecret));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Rest-Command", "getAppKeyStore")
                .addHeader("app-id", config.appid)
                .addHeader("app-key", config.appKey)
                .addHeader("app-nonce", nonce)
                .addHeader("app-sign", sign)
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        if (response.code() >= 400) {
            throw new CircuitException(response.code() + "", response.message());
        }
        String body = response.body().string();
        Map<String, Object> map = new Gson().fromJson(body, HashMap.class);
        if (Double.valueOf(map.get("status") + "") >= 400) {
            throw new CircuitException(map.get("status") + "", map.get("message") + "");
        }
        String json = map.get("dataText") + "";
        map = new Gson().fromJson(json, HashMap.class);
        return map;
    }

    private void authByAccessToken(String accessToken, IPipeline pipeline) throws IOException, CircuitException {
        String url = String.format("%s?token=%s", config.ports.get("uc.auth"), accessToken);
        String nonce = Encript.md5(String.format("%s%s", UUID.randomUUID().toString(), System.currentTimeMillis()));
        String sign = Encript.md5(String.format("%s%s%s", config.appKey, nonce, config.appSecret));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Rest-Command", "verification")
                .addHeader("app-id", config.appid)
                .addHeader("app-key", config.appKey)
                .addHeader("app-nonce", nonce)
                .addHeader("app-sign", sign)
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        if (response.code() >= 400) {
            throw new CircuitException(response.code() + "", response.message());
        }
        String body = response.body().string();
        Map<String, Object> map = new Gson().fromJson(body, HashMap.class);
        if (Double.valueOf(map.get("status") + "") >= 400) {
            throw new CircuitException(map.get("status") + "", map.get("message") + "");
        }
        String json = map.get("dataText") + "";
        map = new Gson().fromJson(json, HashMap.class);
        String person = map.get("person") + "";
        String peer = map.get("device") + "";
        List<String> roles = (List<String>) map.get("roles");
        IPrincipal principal = new DefaultPrincipal(person, peer, roles);
        pipeline.principal(principal);
    }

}
