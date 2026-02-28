package com.github.cadecode.java.practice.java8.collection.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜单实体
 *
 * @author Cade Li
 * @since 2023/11/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Menu {

    private Long id;
    private Long parentId;
    private String name;
    private List<Menu> children;
}
