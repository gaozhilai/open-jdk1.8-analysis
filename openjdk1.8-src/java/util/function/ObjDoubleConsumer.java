/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package java.util.function;

/**
 * Represents an operation that accepts an object-valued and a
 * {@code double}-valued argument, and returns no result.  This is the
 * {@code (reference, double)} specialization of {@link BiConsumer}.
 * Unlike most other functional interfaces, {@code ObjDoubleConsumer} is
 * expected to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object, double)}.
 *
 * @param <T> the type of the object argument to the operation
 *
 * @see BiConsumer
 * @since 1.8
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/4/25 20:19
@FunctionalInterface
public interface ObjDoubleConsumer<T> { /** 函数式接口, 接收消耗一个T类型对象和一个double参数, 无返回结果. 是{@link BiConsumer}的特定版本 */

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param value the second input argument
     */ // 接收一个T类型对象和一个double参数, 执行具体实现类逻辑
    void accept(T t, double value);
}
