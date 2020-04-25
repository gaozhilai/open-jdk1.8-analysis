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
 * Represents a function that accepts an int-valued argument and produces a
 * result.  This is the {@code int}-consuming primitive specialization for
 * {@link Function}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(int)}.
 *
 * @param <R> the type of the result of the function
 *
 * @see Function
 * @since 1.8
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/4/25 19:06
@FunctionalInterface
public interface IntFunction<R> { /** 函数式接口, 接收一个int参数, 返回一个R类型的结果. 是{@link Function}指定参数类型为int的特殊版本 */

    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     * @return the function result
     */ // 执行具体实现类逻辑, 接收int参数, 返回R类型结果
    R apply(int value);
}
