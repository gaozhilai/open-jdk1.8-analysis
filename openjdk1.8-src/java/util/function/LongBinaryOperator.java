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
 * Represents an operation upon two {@code long}-valued operands and producing a
 * {@code long}-valued result.   This is the primitive type specialization of
 * {@link BinaryOperator} for {@code long}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #applyAsLong(long, long)}.
 *
 * @see BinaryOperator
 * @see LongUnaryOperator
 * @since 1.8
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/4/25 20:11
@FunctionalInterface
public interface LongBinaryOperator { /** 函数式接口, LongBinaryOperator接收两个long类型参数, 经过运算后产生一个long类型结果. 是{@link BinaryOperator}的特殊版本 */

    /**
     * Applies this operator to the given operands.
     *
     * @param left the first operand
     * @param right the second operand
     * @return the operator result
     */ // 对两个long参数进行运算, 返回一个long结果
    long applyAsLong(long left, long right);
}
