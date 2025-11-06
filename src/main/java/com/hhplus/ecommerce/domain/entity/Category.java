package com.hhplus.ecommerce.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    private Long id;
    private String name;
    private LocalDateTime createdAt;

    public Category(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }
}
