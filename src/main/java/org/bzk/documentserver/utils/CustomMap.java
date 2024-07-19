package org.bzk.documentserver.utils;

import java.util.HashMap;

/**
 * @Author 2023/3/1 17:46 ly
 **/
public class CustomMap<K, V> extends HashMap<K, V> {

    public CustomMap(int size) {
        super((int)(size / 0.75));

    }

    public static CustomMap build(int size) {
        return new CustomMap(size);
    }

    public CustomMap pu1(K k, V v) {
        this.put(k, v);
        return this;
    }
}
