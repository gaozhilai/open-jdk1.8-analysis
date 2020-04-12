/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * This exception may be thrown by methods that have detected concurrent
 * modification of an object when such modification is not permissible.
 * <p>
 * For example, it is not generally permissible for one thread to modify a Collection
 * while another thread is iterating over it.  In general, the results of the
 * iteration are undefined under these circumstances.  Some Iterator
 * implementations (including those of all the general purpose collection implementations
 * provided by the JRE) may choose to throw this exception if this behavior is
 * detected.  Iterators that do this are known as <i>fail-fast</i> iterators,
 * as they fail quickly and cleanly, rather that risking arbitrary,
 * non-deterministic behavior at an undetermined time in the future.
 * <p>
 * Note that this exception does not always indicate that an object has
 * been concurrently modified by a <i>different</i> thread.  If a single
 * thread issues a sequence of method invocations that violates the
 * contract of an object, the object may throw this exception.  For
 * example, if a thread modifies a collection directly while it is
 * iterating over the collection with a fail-fast iterator, the iterator
 * will throw this exception.
 *
 * <p>Note that fail-fast behavior cannot be guaranteed as it is, generally
 * speaking, impossible to make any hard guarantees in the presence of
 * unsynchronized concurrent modification.  Fail-fast operations
 * throw {@code ConcurrentModificationException} on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness: <i>{@code ConcurrentModificationException}
 * should be used only to detect bugs.</i>
 *
 * @author  Josh Bloch
 * @see     Collection
 * @see     Iterator
 * @see     Spliterator
 * @see     ListIterator
 * @see     Vector
 * @see     LinkedList
 * @see     HashSet
 * @see     Hashtable
 * @see     TreeMap
 * @see     AbstractList
 * @since   1.2
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/4/10 17:46
public class ConcurrentModificationException extends RuntimeException { // 并发修改异常, 由不支持并发操作的方法在检测到并发修改后抛出
    private static final long serialVersionUID = -3666751008965953603L;

    /**
     * Constructs a ConcurrentModificationException with no
     * detail message.
     */ // 无详细信息的构造器
    public ConcurrentModificationException() {
    }

    /**
     * Constructs a {@code ConcurrentModificationException} with the
     * specified detail message.
     *
     * @param message the detail message pertaining to this exception.
     */ // 有详细信息的构造器
    public ConcurrentModificationException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of {@code (cause==null ? null : cause.toString())} (which
     * typically contains the class and detail message of {@code cause}.
     *
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link Throwable#getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     * @since  1.7
     */ // 指定Throwable参数的构造器, 参数中包含了异常栈, 可能包含了详细说明信息
    public ConcurrentModificationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     *
     * <p>Note that the detail message associated with <code>cause</code> is
     * <i>not</i> automatically incorporated in this exception's detail
     * message.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link Throwable#getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link Throwable#getCause()} method).  (A {@code null} value
     *         is permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     * @since 1.7
     */ // 指定Throwable参数和详细说明信息的构造器
    public ConcurrentModificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
