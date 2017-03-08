package nl.vpro.magnolia.jsr107;

/**
 * @author Michiel Meeuwissen
 * @since 1.11
 */
public enum EvictionPolicy {
    /**
     * Least Recently Used
     */
    LRU,
    /**
     * First In First Out
     */
    FIFO,
    /**
     * Less Frequently Used
     */
    LFU
}
