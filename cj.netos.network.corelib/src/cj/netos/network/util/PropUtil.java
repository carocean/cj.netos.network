package cj.netos.network.util;

public class PropUtil {
    public static String getValue(Object v) {
        if(v==null){
            return "";
        }
        return String.format("%s",v);
    }
}
