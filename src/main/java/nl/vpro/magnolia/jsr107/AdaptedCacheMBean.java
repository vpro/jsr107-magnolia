package nl.vpro.magnolia.jsr107;

public interface AdaptedCacheMBean {

    int getSize();

    int getBlockingTimeout();

    String getConfiguration();

    void clear();


}
