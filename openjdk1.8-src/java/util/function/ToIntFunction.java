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
 * Represents a function that produces an int-valued result.  This is the
 * {@code int}-producing primitive specialization for {@link Function}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #applyAsInt(Object)}.
 *
 * @param <T> the type of the input to the function
 *
 * @see Function
 * @since 1.8
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/4/25 20:29
@FunctionalInterface
public interface ToIntFunction<T> { /** 函数式接口, 接收一个T类型参数, 返回一个int结果, 是特殊的{@link Function} */

    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     * @return the function result
     */ // 执行具体实现类逻辑, 接收一个T类型参数, 返回一个int结果
    int applyAsInt(T value);
}
