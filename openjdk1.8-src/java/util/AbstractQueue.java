/*
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

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util;

/**
 * This class provides skeletal implementations of some {@link Queue}
 * operations. The implementations in this class are appropriate when
 * the base implementation does <em>not</em> allow <tt>null</tt>
 * elements.  Methods {@link #add add}, {@link #remove remove}, and
 * {@link #element element} are based on {@link #offer offer}, {@link
 * #poll poll}, and {@link #peek peek}, respectively, but throw
 * exceptions instead of indicating failure via <tt>false</tt> or
 * <tt>null</tt> returns.
 *
 * <p>A <tt>Queue</tt> implementation that extends this class must
 * minimally define a method {@link Queue#offer} which does not permit
 * insertion of <tt>null</tt> elements, along with methods {@link
 * Queue#peek}, {@link Queue#poll}, {@link Collection#size}, and
 * {@link Collection#iterator}.  Typically, additional methods will be
 * overridden as well.  If these requirements cannot be met, consider
 * instead subclassing {@link AbstractCollection}.
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @since 1.5
 * @author Doug Lea
 * @param <E> the type of elements held in this collection
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/5/12 11:00
public abstract class AbstractQueue<E> /** Queue的骨架类, 实现自己的Queue继承此类, 最少只需要实现{@link #offer(Object)}{@link #poll()}{@link #peek()}即可 */
    extends AbstractCollection<E>
    implements Queue<E> {

    /**
     * Constructor for use by subclasses.
     */ // 只能由子类调用的构造方法
    protected AbstractQueue() {
    }

    /**
     * Inserts the specified element into this queue if it is possible to do so
     * immediately without violating capacity restrictions, returning
     * <tt>true</tt> upon success and throwing an <tt>IllegalStateException</tt>
     * if no space is currently available.
     *
     * <p>This implementation returns <tt>true</tt> if <tt>offer</tt> succeeds,
     * else throws an <tt>IllegalStateException</tt>.
     *
     * @param e the element to add
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     * @throws IllegalStateException if the element cannot be added at this
     *         time due to capacity restrictions
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this queue
     * @throws NullPointerException if the specified element is null and
     *         this queue does not permit null elements
     * @throws IllegalArgumentException if some property of this element
     *         prevents it from being added to this queue
     */ /** 见{@link Queue#add(Object)} */
    public boolean add(E e) {
        if (offer(e)) // 当前抽象类默认add方法实现依赖于offer方法, 这也是继承此抽象类后为什么至少要实现的方法中有offer
            return true;
        else
            throw new IllegalStateException("Queue full"); // add方法的特性是添加元素失败后抛出异常代表失败
    }

    /**
     * Retrieves and removes the head of this queue.  This method differs
     * from {@link #poll poll} only in that it throws an exception if this
     * queue is empty.
     *
     * <p>This implementation returns the result of <tt>poll</tt>
     * unless the queue is empty.
     *
     * @return the head of this queue
     * @throws NoSuchElementException if this queue is empty
     */ /** 见{@link Queue#remove()} */
    public E remove() {
        E x = poll(); // 当前抽象类默认remove方法实现依赖于poll方法, 这也是继承此抽象类后为什么至少要实现的方法中有poll
        if (x != null)
            return x;
        else
            throw new NoSuchElementException(); // remove方法的特性是移除元素失败后抛出异常代表失败
    }

    /**
     * Retrieves, but does not remove, the head of this queue.  This method
     * differs from {@link #peek peek} only in that it throws an exception if
     * this queue is empty.
     *
     * <p>This implementation returns the result of <tt>peek</tt>
     * unless the queue is empty.
     *
     * @return the head of this queue
     * @throws NoSuchElementException if this queue is empty
     */ /** 见{@link Queue#element()} */
    public E element() {
        E x = peek(); // 当前抽象类默认add方法实现依赖于element方法, 这也是继承此抽象类后为什么至少要实现的方法中有element
        if (x != null)
            return x;
        else
            throw new NoSuchElementException(); // element方法的特性是无元素可以查看后抛出异常代表失败
    }

    /**
     * Removes all of the elements from this queue.
     * The queue will be empty after this call returns.
     *
     * <p>This implementation repeatedly invokes {@link #poll poll} until it
     * returns <tt>null</tt>.
     */ /** {@link Queue#clear()} */
    public void clear() {
        while (poll() != null) // 当前抽象类的clear方法依赖于poll实现, 所以继承次抽象类要实现的方法中有poll
            ;
    }

    /**
     * Adds all of the elements in the specified collection to this
     * queue.  Attempts to addAll of a queue to itself result in
     * <tt>IllegalArgumentException</tt>. Further, the behavior of
     * this operation is undefined if the specified collection is
     * modified while the operation is in progress.
     *
     * <p>This implementation iterates over the specified collection,
     * and adds each element returned by the iterator to this
     * queue, in turn.  A runtime exception encountered while
     * trying to add an element (including, in particular, a
     * <tt>null</tt> element) may result in only some of the elements
     * having been successfully added when the associated exception is
     * thrown.
     *
     * @param c collection containing elements to be added to this queue
     * @return <tt>true</tt> if this queue changed as a result of the call
     * @throws ClassCastException if the class of an element of the specified
     *         collection prevents it from being added to this queue
     * @throws NullPointerException if the specified collection contains a
     *         null element and this queue does not permit null elements,
     *         or if the specified collection is null
     * @throws IllegalArgumentException if some property of an element of the
     *         specified collection prevents it from being added to this
     *         queue, or if the specified collection is this queue
     * @throws IllegalStateException if not all the elements can be added at
     *         this time due to insertion restrictions
     * @see #add(Object)
     */ /** 见{@link Queue#addAll(Collection)} */
    public boolean addAll(Collection<? extends E> c) {
        if (c == null) // 要添加的集合不能为null
            throw new NullPointerException();
        if (c == this) // 要添加的集合不能是自身
            throw new IllegalArgumentException();
        boolean modified = false;
        for (E e : c) // 增强for循环遍历给定集合每一个元素, 顺序取决于迭代器返回元素顺序
            if (add(e)) // 调用add方法依次添加元素到队列
                modified = true;
        return modified;
    }

}
