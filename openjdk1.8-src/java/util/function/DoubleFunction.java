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
 * Represents a function that accepts a double-valued argument and produces a
 * result.  This is the {@code double}-consuming primitive specialization for
 * {@link Function}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(double)}.
 *
 * @param <R> the type of the result of the function
 *
 * @see Function
 * @since 1.8
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/4/25 18:33
@FunctionalInterface
public interface DoubleFunction<R> { /** 函数式接口, 代表一个输入值为double返回结果为R的函数, 是{@link Function}的特定输入值类型版本 */

    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     * @return the function result
     */ // 应用具体的DoubleFunction实现类的逻辑, 返回结果R
    R apply(double value);
}
