package com.github.cadecode.learn.java8.collection;

import cn.hutool.json.JSONUtil;
import com.github.cadecode.learn.java8.collection.bean.Menu;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * 列表树形化 demo test
 *
 * @author Cade Li
 * @since 2023/11/25
 */
public class ListToTreeDemoTest {

    @Test
    public void test() {
        List<Menu> menus = Arrays.asList(
                new Menu(1L, null, "菜单1", null),
                new Menu(2L, null, "菜单2", null),
                new Menu(3L, null, "菜单3", null),
                new Menu(4L, null, "菜单4", null),
                new Menu(5L, null, "菜单5", null),
                new Menu(11L, 1L, "菜单1-1", null),
                new Menu(21L, 2L, "菜单2-1", null),
                new Menu(22L, 2L, "菜单2-2", null),
                new Menu(111L, 11L, "菜单1-1-1", null),
                new Menu(112L, 11L, "菜单1-1-2", null),
                new Menu(1121L, 112L, "菜单1-1-2-1", null),
                new Menu(1122L, 112L, "菜单1-1-2-2", null),
                new Menu(31L, 3L, "菜单3-1", null),
                new Menu(32L, 3L, "菜单3-2", null),
                new Menu(311L, 31L, "菜单3-1-1", null),
                new Menu(41L, 4L, "菜单4-1", null),
                new Menu(42L, 4L, "菜单4-2", null),
                new Menu(421L, 42L, "菜单4-2-1", null),
                new Menu(422L, 42L, "菜单4-2-2", null),
                new Menu(423L, 42L, "菜单4-2-3", null),
                new Menu(424L, 42L, "菜单4-2-4", null),
                new Menu(425L, 42L, "菜单4-2-5", null),
                new Menu(4251L, 425L, "菜单4-2-5-1", null),
                new Menu(4252L, 452L, "菜单4-2-5-2", null),
                new Menu(4253L, 425L, "菜单4-2-5-3", null),
                new Menu(42531L, 4253L, "菜单4-2-5-3-1", null),
                new Menu(42532L, 4253L, "菜单4-2-5-3-2", null),
                new Menu(42533L, 4253L, "菜单4-2-5-3-3", null),
                new Menu(51L, 5L, "菜单5-1", null),
                new Menu(52L, 5L, "菜单5-2", null),
                new Menu(53L, 5L, "菜单5-3", null),
                new Menu(521L, 52L, "菜单5-2-1", null),
                new Menu(5211L, 521L, "菜单5-2-1-1", null),
                new Menu(531L, 53L, "菜单5-3-1", null),
                new Menu(532L, 53L, "菜单5-3-2", null)
        );

        long currTime0 = System.currentTimeMillis();
        System.out.println(JSONUtil.toJsonPrettyStr(ListToTreeDemo.menuListToTree0(menus, null)));
        long currTime1 = System.currentTimeMillis();
        System.out.println(JSONUtil.toJsonPrettyStr(ListToTreeDemo.menuListToTree1(menus, null)));
        long currTime2 = System.currentTimeMillis();
        System.out.println(JSONUtil.toJsonPrettyStr(ListToTreeDemo.menuListToTree2(menus, null)));
        long currTime3 = System.currentTimeMillis();
        System.out.println(currTime1 - currTime0);
        System.out.println(currTime2 - currTime1);
        System.out.println(currTime3 - currTime2);
    }
}
