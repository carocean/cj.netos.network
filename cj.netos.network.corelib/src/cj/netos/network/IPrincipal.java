package cj.netos.network;

public interface IPrincipal {
    String principal();
    String peer();
    String key();
    boolean roleIn(String role);

    Object property(String key);

    void property(String key, Object value);

    boolean roleStart(String s);

}
