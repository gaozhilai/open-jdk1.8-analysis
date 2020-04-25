/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * Represents a function that accepts a double-valued argument and produces an
 * int-valued result.  This is the {@code double}-to-{@code int} primitive
 * specialization for {@link Function}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #applyAsInt(double)}.
 *
 * @see Function
 * @since 1.8
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/4/25 18:43
@FunctionalInterface
public interface DoubleToIntFunction { /** 函数式接口, 代表了一个接收double类型参数返回int结果的函数, 是{@link Function}的特定版本 */

    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     * @return the function result
     */ // 执行实现类逻辑, 接收一个double类型参数, 返回int结果
    int applyAsInt(double value);
}
