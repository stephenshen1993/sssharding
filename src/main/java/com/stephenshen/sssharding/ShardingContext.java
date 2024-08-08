package com.stephenshen.sssharding;

/**
 * @author stephenshen
 * @date 2024/8/5 07:53:49
 */
public class ShardingContext {

    private static final ThreadLocal<ShardingResult> LOCAL = new ThreadLocal<>();

    public static ShardingResult get() {
        return LOCAL.get();
    }

    public static void set(ShardingResult shardingResult) {
        LOCAL.set(shardingResult);
    }

    public static void clear() {
        LOCAL.remove();
    }
}
