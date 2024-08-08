package com.stephenshen.sssharding.engine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * sharding result.
 * @author stephenshen
 * @date 2024/8/5 07:55:22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShardingResult {

    private String targetDatasourceName;
}
