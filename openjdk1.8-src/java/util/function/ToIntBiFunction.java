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
 * Represents a function that accepts two arguments and produces an int-valued
 * result.  This is the {@code int}-producing primitive specialization for
 * {@link BiFunction}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #applyAsInt(Object, Object)}.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 *
 * @see BiFunction
 * @since 1.8
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/4/25 20:28
@FunctionalInterface
public interface ToIntBiFunction<T, U> { /** 函数式接口, 接收一个T类型一个U类型参数, 返回一个int结果, 是特殊的{@link BiFunction} */

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     */ // 执行具体实现类逻辑, 接收一个T类型参数和一个U类型参数, 返回int结果
    int applyAsInt(T t, U u);
}
