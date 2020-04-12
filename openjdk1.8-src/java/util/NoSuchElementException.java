/*
 * Copyright (c) 1994, 2012, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

/**
 * Thrown by various accessor methods to indicate that the element being requested
 * does not exist.
 *
 * @author  unascribed
 * @see     java.util.Enumeration#nextElement()
 * @see     java.util.Iterator#next()
 * @since   JDK1.0
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/4/10 18:13
public
class NoSuchElementException extends RuntimeException { // 当方法中要访问的元素不存在时可以抛出此异常表示元素不存在
    private static final long serialVersionUID = 6769829250639411880L;

    /**
     * Constructs a <code>NoSuchElementException</code> with <tt>null</tt>
     * as its error message string.
     */ // 无错误信息的构造方法
    public NoSuchElementException() {
        super();
    }

    /**
     * Constructs a <code>NoSuchElementException</code>, saving a reference
     * to the error message string <tt>s</tt> for later retrieval by the
     * <tt>getMessage</tt> method.
     *
     * @param   s   the detail message.
     */ // 指定错误信息的构造方法
    public NoSuchElementException(String s) {
        super(s);
    }
}
