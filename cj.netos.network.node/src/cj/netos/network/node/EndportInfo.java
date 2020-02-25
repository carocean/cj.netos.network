package cj.netos.network.node;

import cj.netos.network.ListenMode;
import cj.ultimate.gson2.com.google.gson.Gson;

import java.io.Serializable;

public class EndportInfo{
    String principalKey;
    ListenMode listenMode;

    public byte[] toBytes() {
        return new Gson().toJson(this).getBytes();
    }

    public static EndportInfo load(byte[] bytes) {
        return new Gson().fromJson(new String(bytes), EndportInfo.class);
    }

    public String getPrincipalKey() {
        return principalKey;
    }

    public ListenMode getListenMode() {
        return listenMode;
    }
}