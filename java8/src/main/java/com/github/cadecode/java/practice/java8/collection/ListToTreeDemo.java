package com.github.cadecode.java.practice.java8.collection;

import cn.hutool.core.util.ObjUtil;
import com.github.cadecode.java.practice.java8.collection.bean.Menu;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 列表树形化 demo
 *
 * @author Cade Li
 * @since 2023/11/25
 */
public class ListToTreeDemo {

    public static List<Menu> menuListToTree0(List<Menu> menus, Long rootId) {
        List<Menu> resultList = new ArrayList<>();
        menus.forEach(menu -> {
            if (ObjUtil.notEqual(menu.getParentId(), rootId)) {
                return;
            }
            if (ObjUtil.isNull(menu.getChildren())) {
                menu.setChildren(new ArrayList<>());
            }
            menus.forEach(m -> {
                if (ObjUtil.notEqual(m.getParentId(), menu.getId())) {
                    return;
                }
                List<Menu> children = menu.getChildren();
                m.setChildren(menuListToTree0(menus, m.getId()));
                children.add(m);
            });
            resultList.add(menu);
        });
        return resultList;
    }

    public static List<Menu> menuListToTree1(List<Menu> menus, Long rootId) {
        List<Menu> resultList = new ArrayList<>();
        menus.forEach(menu -> {
            if (ObjUtil.notEqual(menu.getParentId(), rootId)) {
                return;
            }
            menu.setChildren(menuListToTree1(menus, menu.getId()));
            resultList.add(menu);
        });
        return resultList;
    }

    public static List<Menu> menuListToTree2(List<Menu> menus, Long rootId) {
        List<Menu> parentList = menus.stream().filter(o -> ObjUtil.equals(rootId, o.getParentId())).collect(Collectors.toList());
        parentList.forEach(p -> {
            List<Menu> children = menus.stream()
                    .filter(c -> ObjUtil.equals(c.getParentId(), p.getId()))
                    .peek(c -> c.setChildren(menuListToTree2(menus, c.getId())))
                    .collect(Collectors.toList());
            p.setChildren(children);
        });
        return parentList;
    }
}
