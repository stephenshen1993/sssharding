package com.stephenshen.sssharding.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Order entity.
 *
 * @author stephenshen
 * @date 2024/8/11 12:54:49
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private int id;
    private int uid;
    private double price;
}
