package com.github.cadecode.java.practice.java8.stream;

import com.github.cadecode.java.practice.java8.stream.bean.Customer;
import com.github.cadecode.java.practice.java8.stream.bean.Order;
import com.github.cadecode.java.practice.java8.stream.bean.OrderItem;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Java 8 Stream 操作 demo
 *
 * @author Cade Li
 * @since 2023/10/17
 */
public class StreamDemo {

    public static List<Order> orders = Order.getData();

    /**
     * 通过stream方法把List或数组转换为流
     */
    public static void stream() {
        Arrays.asList("a1", "a2", "a3").stream().forEach(System.out::println);
        Arrays.stream(new int[]{1, 2, 3}).forEach(System.out::println);
    }

    /**
     * 通过Stream.of方法直接传入多个元素构成一个流
     */
    public static void of() {
        String[] arr = {"a", "b", "c"};
        Stream.of(arr).forEach(System.out::println);
        Stream.of("a", "b", "c").forEach(System.out::println);
        Stream.of(1, 2, "a").map(item -> item.getClass().getName()).forEach(System.out::println);
    }

    /**
     * 通过Stream.iterate方法使用迭代的方式构造一个无限流，然后使用limit限制流元素个数
     */
    public static void iterate() {
        Stream.iterate(2, item -> item * 2).limit(10).forEach(System.out::println);
        Stream.iterate(BigInteger.ZERO, n -> n.add(BigInteger.TEN)).limit(10).forEach(System.out::println);
    }

    /**
     * 通过Stream.generate方法从外部传入一个提供元素的Supplier来构造无限流，然后使用limit限制流元素个数
     */
    public static void generate() {
        Stream.generate(() -> "test").limit(3).forEach(System.out::println);
        Stream.generate(Math::random).limit(10).forEach(System.out::println);
    }

    /**
     * 随机生成的流
     */
    public static void random() {
        Stream<Integer> stream = new Random()
                .ints(1, 100)
                .boxed()
                .limit(10);
        stream.forEach(System.out::println);
    }

    /**
     * 通过IntStream或DoubleStream构造基本类型的流
     */
    public static void primitive() {
        // 演示IntStream和DoubleStream
        IntStream.range(1, 3).forEach(System.out::println);
        IntStream.range(0, 3).mapToObj(i -> "x").forEach(System.out::println);

        IntStream.rangeClosed(1, 3).forEach(System.out::println);
        DoubleStream.of(1.1, 2.2, 3.3).forEach(System.out::println);

        // 各种转换，后面注释代表了输出结果
        System.out.println(IntStream.of(1, 2).toArray().getClass()); // class [I
        System.out.println(Stream.of(1, 2).mapToInt(Integer::intValue).toArray().getClass()); // class [I
        System.out.println(IntStream.of(1, 2).boxed().toArray().getClass()); // class [Ljava.lang.Object;
        System.out.println(IntStream.of(1, 2).asDoubleStream().toArray().getClass()); // class [D
        System.out.println(IntStream.of(1, 2).asLongStream().toArray().getClass()); // class [J

        // 注意基本类型流和装箱后的流的区别
        Arrays.asList("a", "b", "c").stream()   // Stream<String>
                .mapToInt(String::length)       // IntStream
                .asLongStream()                 // LongStream
                .mapToDouble(x -> x / 10.0)     // DoubleStream
                .boxed()                        // Stream<Double>
                .mapToLong(x -> 1L)             // LongStream
                .mapToObj(x -> "")              // Stream<String>
                .collect(Collectors.toList());
    }

    public static void filter() {
        // 最近半年的金额大于40的订单
        orders.stream()
                .filter(Objects::nonNull) // 过滤null值
                .filter(order -> order.getPlacedAt().isAfter(LocalDateTime.now().minusMonths(6))) // 最近半年的订单
                .filter(order -> order.getTotalPrice() > 40) // 金额大于40的订单
                .forEach(System.out::println);
    }

    public static void map() {
        // 计算所有订单商品数量
        // 通过两次遍历实现
        LongAdder longAdder = new LongAdder();
        orders.stream().forEach(order ->
                order.getOrderItemList().forEach(orderItem -> longAdder.add(orderItem.getProductQuantity())));
        System.out.println("longAdder = " + longAdder);

        // 使用两次 mapToLong 和 sum 方法实现
        long sum = orders.stream().mapToLong(
                order -> order.getOrderItemList().stream()
                        .mapToLong(OrderItem::getProductQuantity)
                        .sum()
        ).sum();
        System.out.println("sum = " + sum);
    }

    public static void flat() {
        // 直接展开订单商品进行价格统计
        double sum = orders.stream()
                .flatMap(order -> order.getOrderItemList().stream())
                .mapToDouble(item -> item.getProductQuantity() * item.getProductPrice())
                .sum();
        System.out.println("sum = " + sum);

        // 另一种方式 flatMap + mapToDouble =flatMapToDouble
        double sum1 = orders.stream()
                .flatMapToDouble(order ->
                        order.getOrderItemList()
                                .stream().mapToDouble(item -> item.getProductQuantity() * item.getProductPrice()))
                .sum();
        System.out.println("sum1 = " + sum1);
    }

    public static void sorted() {
        // 大于50的订单,按照订单价格倒序前5
        orders.stream()
                .filter(order -> order.getTotalPrice() > 50)
                .sorted(Comparator.comparing(Order::getTotalPrice).reversed())
                .limit(5)
                .forEach(System.out::println);
    }

    public static void distinct() {
        // 去重的下单用户
        orders.stream()
                .map(Order::getCustomerName)
                .distinct()
                .forEach(System.out::println);


        // 所有购买过的商品
        orders.stream()
                .flatMap(order -> order.getOrderItemList().stream())
                .map(OrderItem::getProductName)
                .distinct()
                .forEach(System.out::println);
    }


    public static void skipAndLimit() {
        // 按照下单时间排序，查询前 2 个订单的顾客姓名
        orders.stream()
                .sorted(Comparator.comparing(Order::getPlacedAt))
                .map(order -> order.getCustomerName())
                .limit(2)
                .forEach(System.out::println);

        // 按照下单时间排序，查询第 3 和第 4 个订单的顾客姓名
        orders.stream()
                .sorted(Comparator.comparing(Order::getPlacedAt))
                .map(order -> order.getCustomerName())
                .skip(2).limit(2)
                .forEach(System.out::println);
    }

    public static void anyMatch() {
        boolean b = orders.stream()
                .filter(order -> order.getTotalPrice() > 50)
                .anyMatch(order -> order.getTotalPrice() < 100);
        System.out.println(b);
    }

    public static void reduce() {
        Optional<Double> reduce = orders.stream()
                .map(Order::getTotalPrice)
                .reduce((p, n) -> {
                    return p + n;
                });
        System.out.println("reduce = " + reduce);

        Double reduce2 = orders.stream()
                .map(Order::getTotalPrice)
                .reduce(0.0, (p, n) -> {
                    return p + n;
                });
        System.out.println("reduce2 = " + reduce2);

        StringBuilder reduce1 = orders.stream()
                .reduce(new StringBuilder(), (sb, next) -> {
                    return sb.append(next.getCustomerName() + ",");
                }, (sb1, sb2) -> {
                    return sb1.append(sb2);
                });
        System.out.println("reduce1 = " + reduce1);
    }

    public static void toMap() {
        System.out.println(
                new Random().ints(48, 122)
                        .filter(i -> (i < 57 || i > 65) && (i < 90 || i > 97))
                        .mapToObj(i -> (char) i).limit(20)
                        .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                        .toString()
        );

        // 使用 toMap 获取订单 ID + 下单用户名的 Map
        orders.stream()
                .collect(Collectors.toMap(Order::getId, Order::getCustomerName))
                .entrySet().forEach(System.out::println);

        // 使用 toMap 获取下单用户名 + 最近一次下单时间的 Map
        orders.stream()
                .collect(Collectors.toMap(Order::getCustomerName, Order::getPlacedAt,
                        (x, y) -> x.isAfter(y) ? x : y))
                .entrySet().forEach(System.out::println);
    }

    public static void groupingBy() {
        // 按照用户名分组，统计下单数量
        System.out.println(
                orders.stream()
                        .collect(Collectors.groupingBy(Order::getCustomerName, Collectors.counting()))
                        .entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .collect(Collectors.toList())
        );

        // 按照用户名分组，统计订单总金额
        System.out.println(
                orders.stream().collect(Collectors.groupingBy(Order::getCustomerName, Collectors.summingDouble(Order::getTotalPrice)))
                        .entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                        .collect(Collectors.toList())
        );

        // 按照用户名分组，统计商品采购数量
        System.out.println(
                orders.stream().collect(Collectors.groupingBy(Order::getCustomerName,
                                Collectors.summingInt(order -> order.getOrderItemList().stream()
                                        .collect(Collectors.summingInt(OrderItem::getProductQuantity)))))
                        .entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).collect(Collectors.toList())
        );

        // 统计最受欢迎的商品，倒序后取第一个
        orders.stream()
                .flatMap(order -> order.getOrderItemList().stream())
                .collect(Collectors.groupingBy(OrderItem::getProductName, Collectors.summingInt(OrderItem::getProductQuantity)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .findFirst()
                .ifPresent(System.out::println);

        // 统计最受欢迎的商品的另一种方式，直接利用maxBy
        orders.stream()
                .flatMap(order -> order.getOrderItemList().stream())
                .collect(Collectors.groupingBy(OrderItem::getProductName,
                        Collectors.summingInt(OrderItem::getProductQuantity)))
                .entrySet()
                .stream()
                .collect(Collectors.maxBy(Map.Entry.comparingByValue()))
                .map(Map.Entry::getKey)
                .ifPresent(System.out::println);

        // 按照用户名分组，选用户下的总金额最大的订单
        orders.stream().collect(Collectors.groupingBy(Order::getCustomerName, Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparingDouble(Order::getTotalPrice)), Optional::get)))
                .forEach((k, v) -> System.out.println(k + "#" + v.getTotalPrice() + "@" + v.getPlacedAt()));

        // 根据下单年月分组，统计订单ID列表
        System.out.println(
                orders.stream().collect(Collectors.groupingBy(order -> order.getPlacedAt().format(DateTimeFormatter.ofPattern("yyyyMM")),
                        Collectors.mapping(order -> order.getId(), Collectors.toList())))
        );

        // 根据下单年月+用户名两次分组，统计订单ID列表
        System.out.println(
                orders.stream().collect(Collectors.groupingBy(order -> order.getPlacedAt().format(DateTimeFormatter.ofPattern("yyyyMM")),
                        Collectors.groupingBy(order -> order.getCustomerName(),
                                Collectors.mapping(order -> order.getId(), Collectors.toList())))));
    }

    public static void partitioningBy() {
        // 根据是否有下单记录进行分区
        Map<Boolean, List<Customer>> collect = Customer.getData().stream()
                .collect(Collectors.partitioningBy(customer -> orders
                        .stream()
                        .filter(order -> order.getCustomerId() == customer.getId())
                        .findAny()
                        .isPresent()
                ));
        System.out.println(collect);

    }

    public static void max() {
        Map.Entry<String, Integer> e1 = orders.stream()
                .flatMap(order -> order.getOrderItemList().stream())
                .collect(Collectors.groupingBy(OrderItem::getProductName,
                        Collectors.summingInt(OrderItem::getProductQuantity)))
                .entrySet()
                .stream()
                .collect(Collectors.maxBy(Map.Entry.<String, Integer>comparingByValue()))
                .get();
        System.out.println("e1 = " + e1);

        Map.Entry<String, Integer> e2 = orders.stream()
                .flatMap(order -> order.getOrderItemList().stream())
                .collect(Collectors.groupingBy(OrderItem::getProductName,
                        Collectors.summingInt(OrderItem::getProductQuantity)))
                .entrySet()
                .stream()
                .max(Map.Entry.<String, Integer>comparingByValue())
                .get();
        System.out.println("e2 = " + e2);

        Map.Entry<String, Integer> e3 = orders.stream()
                .flatMap(order -> order.getOrderItemList().stream())
                .collect(Collectors.groupingBy(OrderItem::getProductName,
                        Collectors.summingInt(OrderItem::getProductQuantity)))
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry<String, Integer>::getValue).reversed())
                .findFirst()
                .get();
        System.out.println("e3 = " + e3);
    }
}
