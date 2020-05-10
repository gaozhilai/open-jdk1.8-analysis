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

import java.util.function.Consumer;

/**
 * Doubly-linked list implementation of the {@code List} and {@code Deque}
 * interfaces.  Implements all optional list operations, and permits all
 * elements (including {@code null}).
 *
 * <p>All of the operations perform as could be expected for a doubly-linked
 * list.  Operations that index into the list will traverse the list from
 * the beginning or the end, whichever is closer to the specified index.
 *
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * If multiple threads access a linked list concurrently, and at least
 * one of the threads modifies the list structurally, it <i>must</i> be
 * synchronized externally.  (A structural modification is any operation
 * that adds or deletes one or more elements; merely setting the value of
 * an element is not a structural modification.)  This is typically
 * accomplished by synchronizing on some object that naturally
 * encapsulates the list.
 *
 * If no such object exists, the list should be "wrapped" using the
 * {@link Collections#synchronizedList Collections.synchronizedList}
 * method.  This is best done at creation time, to prevent accidental
 * unsynchronized access to the list:<pre>
 *   List list = Collections.synchronizedList(new LinkedList(...));</pre>
 *
 * <p>The iterators returned by this class's {@code iterator} and
 * {@code listIterator} methods are <i>fail-fast</i>: if the list is
 * structurally modified at any time after the iterator is created, in
 * any way except through the Iterator's own {@code remove} or
 * {@code add} methods, the iterator will throw a {@link
 * ConcurrentModificationException}.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than
 * risking arbitrary, non-deterministic behavior at an undetermined
 * time in the future.
 *
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw {@code ConcurrentModificationException} on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness:   <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i>
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author  Josh Bloch
 * @see     List
 * @see     ArrayList
 * @since 1.2
 * @param <E> the type of elements held in this collection
 */
 // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/5/8 19:40
public class LinkedList<E> // LinkedList元素存储基于链表, 适合增删多和顺序访问
    extends AbstractSequentialList<E> // 继承了顺序序列, 虽然也支持下标随机访问, 但是随机访问效率不如ArrayList高
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable // LinkedList本身也是双端队列, 支持克隆和序列化
{ // 读懂此类首先要理解两个名词前驱结点(predecessor代码里简写为pred)和后继结点(successor代码里简写为succ)
    transient int size = 0; // 持有元素计数, 当前实例包含元素数量

    /**
     * Pointer to first node.
     * Invariant: (first == null && last == null) ||
     *            (first.prev == null && first.item != null)
     */ // 链表第一个结点
    transient Node<E> first;

    /**
     * Pointer to last node.
     * Invariant: (first == null && last == null) ||
     *            (last.next == null && last.item != null)
     */ // 链表最后一个结点
    transient Node<E> last;

    /**
     * Constructs an empty list.
     */ // 空构造器
    public LinkedList() {
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param  c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */ // 用指定集合的元素构造实例, 顺序取决于指定集合迭代器返回的元素顺序
    public LinkedList(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    /**
     * Links e as first element.
     */ // 将指定元素包装成结点并放置在链表头部
    private void linkFirst(E e) {
        final Node<E> f = first; // 暂存原先的头结点
        final Node<E> newNode = new Node<>(null, e, f); // 用给定元素构建结点, 新结点后继结点是原先的头结点
        first = newNode; // 将头结点赋值为新构造的结点(将新构造结点放到了链表头部)
        if (f == null) // 如果原先头结点是null, 证明原始实例不包含任何结点. 新增结点既是头结点又是尾结点
            last = newNode; // 将尾结点也赋值为新结点
        else // 否则原始头结点不为null有值
            f.prev = newNode; // 那么将原始结点的前驱结点设置为新增结点
        size++; // 持有元素计数增加
        modCount++; // 操作计数增加
    }

    /**
     * Links e as last element.
     */ // 将指定元素包装成结点并放置在链表尾部
    void linkLast(E e) {
        final Node<E> l = last; // 暂存原先的尾结点
        final Node<E> newNode = new Node<>(l, e, null); // 用给定元素构建新结点, 其前驱结点是原先的尾结点(将新结点放在链表尾部)
        last = newNode; // 将尾结点赋值为新结点
        if (l == null) // 如果原尾结点是null, 那么原链表不包含任何元素, 新结点既是尾结点也是头结点
            first = newNode; // 将头结点赋值为新结点
        else // 否则原尾结点有值
            l.next = newNode; // 将原尾结点的后继结点设置为新结点
        size++; // 持有元素计数增加
        modCount++; // 操作计数增加
    }

    /**
     * Inserts element e before non-null Node succ.
     */ // 将给定元素插入到给定的后继结点前
    void linkBefore(E e, Node<E> succ) {
        // assert succ != null;
        final Node<E> pred = succ.prev; // 暂存后继结点原前驱结点
        final Node<E> newNode = new Node<>(pred, e, succ); // 用给定元素构造新结点, 新结点前驱是pred, 后继是succ, (相当于将结点放在succ和原前驱之间)
        succ.prev = newNode; // 给定的结点前驱结点更新成新结点
        if (pred == null) // 如果给定结点succ的原前驱是null, 那么新的前驱也就是新结点就是头结点
            first = newNode; // 将头结点赋值为新结点
        else // 否则succ原前驱结点有值
            pred.next = newNode; // 那么将succ原前驱结点的后继结点赋值为新结点(相当于将结点放在succ和原前驱之间)
        size++; // 持有元素计数增加
        modCount++; // 操作计数增加
    }

    /**
     * Unlinks non-null first node f.
     */ // 移除链表第一个结点f
    private E unlinkFirst(Node<E> f) {
        // assert f == first && f != null;
        final E element = f.item; // 暂存f结点包含的元素值, 用于方法结束时返回
        final Node<E> next = f.next; // 得到结点f的后继结点
        f.item = null; // 将f结点包含元素值为null
        f.next = null; // help GC 将f结点的后继结点引用值为null, 用于帮助垃圾回收
        first = next; // 将f结点的后继结点设置为头结点
        if (next == null) // 如果f的后继结点为null, 链表是空的, 头结点尾结点都是null
            last = null; // 将尾结点赋值为null
        else // 否则f的后继结点有值
            next.prev = null; // 将f后继结点的前驱结点设置为null, 因为f的后继结点是新的头结点, 原头结点f已经被移除
        size--; // 持有元素计数减一
        modCount++; // 操作计数加一
        return element; // 返回被移除的结点包含的元素
    }

    /**
     * Unlinks non-null last node l.
     */ // 移除链表最后一个结点l
    private E unlinkLast(Node<E> l) {
        // assert l == last && l != null;
        final E element = l.item; // 暂存结点l包含的元素
        final Node<E> prev = l.prev; // 找到最后一个结点l的前驱结点
        l.item = null; // 将要移除的l结点包含的元素置为null
        l.prev = null; // help GC 将要移除的l结点持有的前驱结点引用值为null帮助垃圾回收
        last = prev; // 将最后一个移除的结点的前驱结点设置为最后一个结点
        if (prev == null) // 如果最后一个要移除的结点前驱结点是null, 那么移除后链表为空, 最后一个结点和第一个结点都是null
            first = null; // 将第一个结点设置为null
        else // 否则最后一个移除的结点前驱结点有值
            prev.next = null; // 将其前驱结点持有的下一个结点置为null
        size--; // 持有元素计数减一
        modCount++; // 操作计数加一
        return element; // 返回被移除的结点包含的元素
    }

    /**
     * Unlinks non-null node x.
     */ // 移除指定的结点, 结点可以在链表中任意位置
    E unlink(Node<E> x) {
        // assert x != null;
        final E element = x.item; // 暂存要移除结点包含的元素
        final Node<E> next = x.next; // 得到要移除结点后继节点
        final Node<E> prev = x.prev; // 得到要移除结点前驱结点

        if (prev == null) { // 如果前驱结点为null, 那么移除当前节点后, 其后继节点就是第一个结点
            first = next; // 将后继结点设置为第一个结点
        } else { // 如果前驱结点不为空
            prev.next = next; // 那么要移除结点的前驱结点持有的的下一个结点引用设置为要移除结点的后继结点
            x.prev = null; // 要移除结点持有的前驱结点引用设置为null, 便于移除的结点被垃圾回收
        }

        if (next == null) { // 要移除结点的后继结点为null, 那么移除当前结点后, 其前驱结点就是最后一个结点
            last = prev; // 将要移除结点的前驱结点设置为最后一个节点
        } else { // 否则要移除结点的后继结点有值
            next.prev = prev; // 那么后继结点持有的前一个结点设置为要移除结点的前驱结点
            x.next = null; // 将要移除结点持有的下一个节点引用设置为null
        }
        // 此时要移除结点持有的前驱结点和后继结点引用都已经设置为null
        x.item = null; // 将要移除结点持有元素设置为null, 设置为null操作都是为了方便垃圾回收
        size--; // 持有元素计数减一
        modCount++; // 操作计数加一
        return element; // 返回被移除的结点包含的元素
    }

    /**
     * Returns the first element in this list.
     *
     * @return the first element in this list
     * @throws NoSuchElementException if this list is empty
     */ /** 见{@link Deque#getFirst()} */
    public E getFirst() {
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return f.item;
    }

    /**
     * Returns the last element in this list.
     *
     * @return the last element in this list
     * @throws NoSuchElementException if this list is empty
     */ /** 见{@link Deque#getLast()} */
    public E getLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return l.item;
    }

    /**
     * Removes and returns the first element from this list.
     *
     * @return the first element from this list
     * @throws NoSuchElementException if this list is empty
     */ /** 见{@link Deque#removeFirst()} */
    public E removeFirst() {
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return unlinkFirst(f);
    }

    /**
     * Removes and returns the last element from this list.
     *
     * @return the last element from this list
     * @throws NoSuchElementException if this list is empty
     */ /** 见{@link Deque#removeLast()} */
    public E removeLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return unlinkLast(l);
    }

    /**
     * Inserts the specified element at the beginning of this list.
     *
     * @param e the element to add
     */ /** 见{@link Deque#addFirst(Object)}, 与Deque中不同的是, LinkedList不是固定容量队列, 所以添加元素不会因为容量不足抛出异常 */
    public void addFirst(E e) {
        linkFirst(e);
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * <p>This method is equivalent to {@link #add}.
     *
     * @param e the element to add
     */ /** 见{@link Deque#addLast(Object)}, 与Deque中不同的是, LinkedList不是固定容量队列, 所以添加元素不会因为容量不足抛出异常 */
    public void addLast(E e) {
        linkLast(e);
    }

    /**
     * Returns {@code true} if this list contains the specified element.
     * More formally, returns {@code true} if and only if this list contains
     * at least one element {@code e} such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this list is to be tested
     * @return {@code true} if this list contains the specified element
     */ /** 见{@link Deque#contains(Object)} */
    public boolean contains(Object o) {
        return indexOf(o) != -1; // 此处通过indexOf寻找指定元素得到下标, 来判断指定元素是否存在
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */ /** 见{@link Deque#size()} */
    public int size() {
        return size;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * <p>This method is equivalent to {@link #addLast}.
     *
     * @param e element to be appended to this list
     * @return {@code true} (as specified by {@link Collection#add})
     */ /** 见{@link Deque#add(Object)}在当前类中, 此方法只会成功返回true, 不会失败 */
    public boolean add(E e) {
        linkLast(e);
        return true;
    }

    /**
     * Removes the first occurrence of the specified element from this list,
     * if it is present.  If this list does not contain the element, it is
     * unchanged.  More formally, removes the element with the lowest index
     * {@code i} such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
     * (if such an element exists).  Returns {@code true} if this list
     * contained the specified element (or equivalently, if this list
     * changed as a result of the call).
     *
     * @param o element to be removed from this list, if present
     * @return {@code true} if this list contained the specified element
     */ /** 见{@link Deque#remove(Object)}, 在此类中通过遍历链表找到包含指定元素的结点并移除, 最差的情况要遍历整个链表(所以随机访问慢, 但是移除新增结点快) */
    public boolean remove(Object o) {
        if (o == null) { // 要移除的元素是null
            for (Node<E> x = first; x != null; x = x.next) { // 遍历链表
                if (x.item == null) { // 找到包含元素为null的节点
                    unlink(x); // 移除找到的节点
                    return true; // 返回true
                }
            }
        } else { // 要移除的元素不是null
            for (Node<E> x = first; x != null; x = x.next) { // 遍历链表
                if (o.equals(x.item)) { // 找到包含元素为参数值的节点
                    unlink(x); // 移除找到的节点
                    return true; // 返回true
                }
            }
        }
        return false; // 没有结点被移除(没有找到对应结点), 返回false
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the specified
     * collection's iterator.  The behavior of this operation is undefined if
     * the specified collection is modified while the operation is in
     * progress.  (Note that this will occur if the specified collection is
     * this list, and it's nonempty.)
     *
     * @param c collection containing elements to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */ /** 见{@link List#addAll(Collection)} */
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list, starting at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements to
     * the right (increases their indices).  The new elements will appear
     * in the list in the order that they are returned by the
     * specified collection's iterator.
     *
     * @param index index at which to insert the first element
     *              from the specified collection
     * @param c collection containing elements to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException if the specified collection is null
     */ /** 见{@link List#addAll(int, Collection)} */
    public boolean addAll(int index, Collection<? extends E> c) {
        checkPositionIndex(index); // 校验index合法性, index必须大于等于0, 小于等于持有元素数量size

        Object[] a = c.toArray(); // 获得包含给定集合所有元素的数组
        int numNew = a.length; // 判断有多少个新元素需要插入
        if (numNew == 0) // 需要新增的元素为0个
            return false; // 直接返回false, 当前LinkedList没有被改变

        Node<E> pred, succ; // 定义前驱结点和后继结点变量
        if (index == size) { // 如果要添加元素的位置下标等于size, 那么就是在链表尾部添加新元素
            succ = null; // 尾部后面没有后继结点
            pred = last; // 尾部的前驱结点就是新增元素前链表最后一个结点
        } else { // 当新增元素不是在链表尾部时
            succ = node(index); // 那么新增元素肯定要把原有元素及原有元素后方元素整体向后移动, 也就是指定下标原始结点就是新增结点的后继结点
            pred = succ.prev; // 指定下标原始结点的前驱结点就是新增结点的前驱结点
        }

        for (Object o : a) { // 遍历每一个要新增的元素
            @SuppressWarnings("unchecked") E e = (E) o;
            Node<E> newNode = new Node<>(pred, e, null); // 将要新增的元素包装成新结点, 新结点的后继先设置为null, 前驱就是当前的pred值
            if (pred == null) // 如果当前pred值为null, 证明新增结点在链表头部
                first = newNode; // 那么第一个新增结点设置为第一个结点
            else // 否则pred前驱结点有值
                pred.next = newNode; // 将前驱结点持有的后继结点引用设置为新结点
            pred = newNode; // 更新pred值, 为添加下一个元素做准备
        }

        if (succ == null) { // 如果后继结点是null
            last = pred; // 那么将最后一个新增的元素对应的节点设置为最后一个结点
        } else { // 否则后继结点succ有值
            pred.next = succ; // 将最后一个新增结点持有的下一个结点设置为原succ后继结点
            succ.prev = pred; // 原后继结点succ持有的前驱结点设置为最后一个新结点, 到此新增元素已经添加到原链表中
        }

        size += numNew; // 持有元素计数增加新添加元素数
        modCount++; // 操作计数加一
        return true; // 返回true代表当前实例元素有改动
    }

    /**
     * Removes all of the elements from this list.
     * The list will be empty after this call returns.
     */ /** 见{@link List#clear()} */
    public void clear() { // 清空每个结点是为了方便垃圾回收, 即使存在可达的迭代器也不会影响链表结点的回收
        // Clearing all of the links between nodes is "unnecessary", but:
        // - helps a generational GC if the discarded nodes inhabit
        //   more than one generation
        // - is sure to free memory even if there is a reachable Iterator
        for (Node<E> x = first; x != null; ) { // 遍历链表所有结点
            Node<E> next = x.next;
            x.item = null; // 将结点持有元素引用设置为null
            x.next = null; // 将结点持有的后继结点设置为null
            x.prev = null; // 将结点持有的前驱结点设置为null
            x = next; // 遍历下一个节点
        }
        first = last = null; // 第一个结点和最后一个结点设置为null
        size = 0; // 更新持有元素计数
        modCount++; // 更新操作计数
    }


    // Positional Access Operations 通过下标随机访问操作

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */ /**见{@link List#get(int)}*/
    public E get(int index) {
        checkElementIndex(index); // 检测下标合法性
        return node(index).item; // 通过下标找到对应位置的结点, 返回其包含的元素
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element.
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */ /** 见{@link List#set(int, Object)} */
    public E set(int index, E element) {
        checkElementIndex(index); // 检测下标合法性
        Node<E> x = node(index); // 通过下标找到对应的结点
        E oldVal = x.item; // 暂存结点包含的旧元素
        x.item = element; // 将新元素负值给结点
        return oldVal; // 返回旧值
    }

    /**
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position (if any) and any
     * subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */ /** 见{@link List#add(int, Object)} */
    public void add(int index, E element) {
        checkPositionIndex(index); // 检测下标合法性

        if (index == size) // 如果要添加元素的位置在最后
            linkLast(element); // 那么在链表尾部添加元素
        else
            linkBefore(element, node(index)); // 否则先找到指定下标位置原始节点, 然后在其前方新增指定元素结点
    }

    /**
     * Removes the element at the specified position in this list.  Shifts any
     * subsequent elements to the left (subtracts one from their indices).
     * Returns the element that was removed from the list.
     *
     * @param index the index of the element to be removed
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */ /** 见{@link List#remove(int)} */
    public E remove(int index) {
        checkElementIndex(index); // 检测下标合法性
        return unlink(node(index)); // 根据下标找到要移除的结点, 然后将其从链表中移除
    }

    /**
     * Tells if the argument is the index of an existing element.
     */ // 判断给定下标是否对应一个存在的结点元素
    private boolean isElementIndex(int index) {
        return index >= 0 && index < size;
    }

    /**
     * Tells if the argument is the index of a valid position for an
     * iterator or an add operation.
     */ // 判断给定的下标对迭代器或者新增操作是否在合法范围内
    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }

    /**
     * Constructs an IndexOutOfBoundsException detail message.
     * Of the many possible refactorings of the error handling code,
     * this "outlining" performs best with both server and client VMs.
     */ // 根据下标构建越界异常信息
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }
        // 检测下标是否是对应和合法的元素, 如果不合法抛出越界异常
    private void checkElementIndex(int index) {
        if (!isElementIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
        // 判断给定下标对于迭代器或者是新增操作是否合法, 不合法抛出越界异常
    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * Returns the (non-null) Node at the specified element index.
     */ // 返回给定下标对应的结点
    Node<E> node(int index) {
        // assert isElementIndex(index);

        if (index < (size >> 1)) { // 如果给定下标小于持有元素数量的一
            Node<E> x = first;
            for (int i = 0; i < index; i++) // 从前往后寻找链表中对应下标位置的结点
                x = x.next;
            return x;
        } else { // 如果给定下标大于持有元素数量的一半
            Node<E> x = last;
            for (int i = size - 1; i > index; i--) // 从后往前寻找链表对应下标位置的节点
                x = x.prev;
            return x;
        }
    }

    // Search Operations 搜索操作

    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the lowest index {@code i} such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param o element to search for
     * @return the index of the first occurrence of the specified element in
     *         this list, or -1 if this list does not contain the element
     */ /** 见{@link List#indexOf(Object)} */
    public int indexOf(Object o) {
        int index = 0;
        if (o == null) { // 区分要寻找的元素是null还是对象
            for (Node<E> x = first; x != null; x = x.next) { // 遍历链中每一个结点
                if (x.item == null) // 用==判断null的情况是否与节点值相等
                    return index; // 如果相等直接返回第一次遇到相等结点的下标
                index++; // 否则继续寻找
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item)) // 如果寻找的是对象, 用equals方法判断相等
                    return index;
                index++;
            }
        }
        return -1; // 如果链表中没有找到指定元素, 返回-1
    }

    /**
     * Returns the index of the last occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the highest index {@code i} such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param o element to search for
     * @return the index of the last occurrence of the specified element in
     *         this list, or -1 if this list does not contain the element
     */ /** 见{@link List#lastIndexOf(Object)}  */
    public int lastIndexOf(Object o) {
        int index = size;
        if (o == null) { // 区分要寻找的元素是null还是对象
            for (Node<E> x = last; x != null; x = x.prev) { // 从后往前寻找链表
                index--;
                if (x.item == null) // 如果节中包含元素与给定元素相等
                    return index; // 返回节点的下标
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (o.equals(x.item)) // 如果寻找的是对象, 用equals方法判断相等
                    return index;
            }
        }
        return -1; // 如果没有找到给定元素, 返回-1
    }

    // Queue operations. 队列操作

    /**
     * Retrieves, but does not remove, the head (first element) of this list.
     *
     * @return the head of this list, or {@code null} if this list is empty
     * @since 1.5
     */ /** 见{@link Queue#peek()} */
    public E peek() {
        final Node<E> f = first; // 暂存链表第一个结点
        return (f == null) ? null : f.item; // 如果第一个结点为null, 返回null, 否则返回节点包含的元素
    }

    /**
     * Retrieves, but does not remove, the head (first element) of this list.
     *
     * @return the head of this list
     * @throws NoSuchElementException if this list is empty
     * @since 1.5
     */ /** 见{@link Queue#element()} */
    public E element() {
        return getFirst(); // 返回链表第一个结点包含的元素, 如果第一个节点为null会抛出异常
    }

    /**
     * Retrieves and removes the head (first element) of this list.
     *
     * @return the head of this list, or {@code null} if this list is empty
     * @since 1.5
     */ /** 见{@link Queue#poll()} */
    public E poll() {
        final Node<E> f = first; // 暂存第一个结点
        return (f == null) ? null : unlinkFirst(f); // 如果第一个结点为null, 返回null. 否则移除第一个结点并返回其包含的元素
    }

    /**
     * Retrieves and removes the head (first element) of this list.
     *
     * @return the head of this list
     * @throws NoSuchElementException if this list is empty
     * @since 1.5
     */ /** 见{@link Queue#remove()} */
    public E remove() {
        return removeFirst(); // 移除链表第一个结点并返回其包含的元素, 如果第一个结点为null, 抛出异常
    }

    /**
     * Adds the specified element as the tail (last element) of this list.
     *
     * @param e the element to add
     * @return {@code true} (as specified by {@link Queue#offer})
     * @since 1.5
     */ /** 见{@link Queue#add(Object)} */
    public boolean offer(E e) {
        return add(e); // 向链表尾部添加元素并返回true
    }

    // Deque operations 双向队列操作
    /**
     * Inserts the specified element at the front of this list.
     *
     * @param e the element to insert
     * @return {@code true} (as specified by {@link Deque#offerFirst})
     * @since 1.6
     */ /** 见{@link Deque#offerFirst(Object)} */
    public boolean offerFirst(E e) {
        addFirst(e); // 将指定的元素包装成结点放到链表头部
        return true;
    }

    /**
     * Inserts the specified element at the end of this list.
     *
     * @param e the element to insert
     * @return {@code true} (as specified by {@link Deque#offerLast})
     * @since 1.6
     */ /** 见{@link Deque#offerLast(Object)} */
    public boolean offerLast(E e) {
        addLast(e); // 将指定的元素包装成结点放到链表尾部
        return true;
    }

    /**
     * Retrieves, but does not remove, the first element of this list,
     * or returns {@code null} if this list is empty.
     *
     * @return the first element of this list, or {@code null}
     *         if this list is empty
     * @since 1.6
     */ /** 见{@link Deque#peekFirst()} */
    public E peekFirst() {
        final Node<E> f = first; // 暂存第一个结点
        return (f == null) ? null : f.item; // 第一个结点为null时返回null, 否则返回结点包含的元素
     }

    /**
     * Retrieves, but does not remove, the last element of this list,
     * or returns {@code null} if this list is empty.
     *
     * @return the last element of this list, or {@code null}
     *         if this list is empty
     * @since 1.6
     */ /** 见{@link Deque#peekLast()} */
    public E peekLast() {
        final Node<E> l = last; // 暂存最后一个结点
        return (l == null) ? null : l.item; // 最后一个结点为null时返回null, 否则返回结点包含的元素
    }

    /**
     * Retrieves and removes the first element of this list,
     * or returns {@code null} if this list is empty.
     *
     * @return the first element of this list, or {@code null} if
     *     this list is empty
     * @since 1.6
     */ /** 见{@link Deque#pollFirst()} */
    public E pollFirst() {
        final Node<E> f = first; // 暂存第一个结点
        return (f == null) ? null : unlinkFirst(f); // 如果第一个结点为null返回null, 否则将结点从链表中移除并返回其中的元素值
    }

    /**
     * Retrieves and removes the last element of this list,
     * or returns {@code null} if this list is empty.
     *
     * @return the last element of this list, or {@code null} if
     *     this list is empty
     * @since 1.6
     */ /** 见{@link Deque#pollLast()} */
    public E pollLast() {
        final Node<E> l = last; // 暂存最后一个结点
        return (l == null) ? null : unlinkLast(l); // 如果最后一个结点为null返回null, 否则将结点从链表中移除并返回其中的元素值
    }

    /**
     * Pushes an element onto the stack represented by this list.  In other
     * words, inserts the element at the front of this list.
     *
     * <p>This method is equivalent to {@link #addFirst}.
     *
     * @param e the element to push
     * @since 1.6
     */ /** 见{@link Deque#push(Object)} */
    public void push(E e) {
        addFirst(e); // 将给定元素包装成结点添加到链表头部
    }

    /**
     * Pops an element from the stack represented by this list.  In other
     * words, removes and returns the first element of this list.
     *
     * <p>This method is equivalent to {@link #removeFirst()}.
     *
     * @return the element at the front of this list (which is the top
     *         of the stack represented by this list)
     * @throws NoSuchElementException if this list is empty
     * @since 1.6
     */ /** 见{@link Deque#pop()} */
    public E pop() {
        return removeFirst(); // 移除第一个元素并返回, 注意没有结点可以移除时会抛出异常
    }

    /**
     * Removes the first occurrence of the specified element in this
     * list (when traversing the list from head to tail).  If the list
     * does not contain the element, it is unchanged.
     *
     * @param o element to be removed from this list, if present
     * @return {@code true} if the list contained the specified element
     * @since 1.6
     */ /** 见{@link Deque#removeFirstOccurrence(Object)} */
    public boolean removeFirstOccurrence(Object o) {
        return remove(o); // 移除链表中从前往后第一个包含元素与给定元素相等的结点
    }

    /**
     * Removes the last occurrence of the specified element in this
     * list (when traversing the list from head to tail).  If the list
     * does not contain the element, it is unchanged.
     *
     * @param o element to be removed from this list, if present
     * @return {@code true} if the list contained the specified element
     * @since 1.6
     */ /** 见{@link Deque#removeLastOccurrence(Object)} */
    public boolean removeLastOccurrence(Object o) {
        if (o == null) { // 如果要移除的元素是null
            for (Node<E> x = last; x != null; x = x.prev) { // 从后往前遍历链表结点
                if (x.item == null) { // 第一次遇到的包含值与给定值相等的结点
                    unlink(x); // 将其移除
                    return true; // 返回true
                }
            }
        } else { // 如果要移除的元素不是null
            for (Node<E> x = last; x != null; x = x.prev) {
                if (o.equals(x.item)) { // 那么用equals方法作为比较相等的依据
                    unlink(x);
                    return true;
                }
            }
        }
        return false; // 如果没有找到包含给定的元素的结点, 返回false
    }

    /**
     * Returns a list-iterator of the elements in this list (in proper
     * sequence), starting at the specified position in the list.
     * Obeys the general contract of {@code List.listIterator(int)}.<p>
     *
     * The list-iterator is <i>fail-fast</i>: if the list is structurally
     * modified at any time after the Iterator is created, in any way except
     * through the list-iterator's own {@code remove} or {@code add}
     * methods, the list-iterator will throw a
     * {@code ConcurrentModificationException}.  Thus, in the face of
     * concurrent modification, the iterator fails quickly and cleanly, rather
     * than risking arbitrary, non-deterministic behavior at an undetermined
     * time in the future.
     *
     * @param index index of the first element to be returned from the
     *              list-iterator (by a call to {@code next})
     * @return a ListIterator of the elements in this list (in proper
     *         sequence), starting at the specified position in the list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @see List#listIterator(int)
     */ /** 见{@link List#listIterator(int)}  */
    public ListIterator<E> listIterator(int index) {
        checkPositionIndex(index); // 检测指定的下标位置对于迭代器来说是否合法
        return new ListItr(index); // 返回当前LinkedList中的序列迭代器实现
    }
    // LinkedList中序列迭代器实现
    private class ListItr implements ListIterator<E> {
        private Node<E> lastReturned; // 上一次返回的结点
        private Node<E> next; // 下一次要返回的结点
        private int nextIndex; // 下一个要返回结点的下标
        private int expectedModCount = modCount; // 初始化预期操作数与当前LinedList实例操作数相等
        // 构造方法, 用指定下标位置构造序列迭代器实例
        ListItr(int index) {
            // assert isPositionIndex(index);
            next = (index == size) ? null : node(index); // 当指定初始位置为链表尾部后面一个位置时, 设置下一个返回结点next为null, 否则设置为根据index找到的结点
            nextIndex = index; // 下一个要返回结点的下标设置为指定下标
        }
        /** 见{@link ListIterator#hasNext()} */
        public boolean hasNext() {
            return nextIndex < size; // 根据下一个要返回结点的下标与持有元素数量size相比较, 判断是否存在下一个要迭代的元素
        }
        /** 见{@link ListIterator#next()} */
        public E next() {
            checkForComodification(); // 检测迭代过程中当前LinkedList实例是否被并发修改, 如果是则抛出异常
            if (!hasNext()) // 判断如果没有要返回的元素则抛出异常, 所以在调用此方法前要通过hasNext方法检测是否还存在要迭代的元素
                throw new NoSuchElementException();

            lastReturned = next; // 将上一个返回结点变量lastReturned值更新成原本下一个返回结点next值
            next = next.next; // 将下一个返回结点next值, 更新成原本next值的后继结点
            nextIndex++; // 更新下一个要返回结点的下标值
            return lastReturned.item; // 返回本次调用此方法返回的结点包含的值, 即调用方法前next字段的值
        }
        /** 见{@link ListIterator#hasPrevious()} */
        public boolean hasPrevious() {
            return nextIndex > 0;
        }
        /** 见{@link ListIterator#previous()} */
        public E previous() {
            checkForComodification(); // 检测迭代过程中当前LinkedList实例是否被并发修改, 如果是则抛出异常
            if (!hasPrevious()) // 判断如果没有要返回的元素则抛出异常, 所以在调用此方法前要通过hasPrevious方法检测是否还存在要迭代的元素
                throw new NoSuchElementException();

            lastReturned = next = (next == null) ? last : next.prev; // 更新上一次返回结点字段lastReturned值为下一个要返回的结点字段next值, 如果next为空, 赋值为链表最后一个结点
            nextIndex--; // 将下一个要返回的结点下标字段值减一
            return lastReturned.item; // 返回本次方法调用找到的结点包含的值
        }
        /** 见{@link ListIterator#nextIndex()} */
        public int nextIndex() {
            return nextIndex; // 返回下一次返回结点的下标值
        }
        /** 见{@link ListIterator#previousIndex()} */
        public int previousIndex() {
            return nextIndex - 1; // 返回下一次返回结点下标的前一个下标值
        }
        /** 见{@link ListIterator#remove()} */
        public void remove() {
            checkForComodification(); // 检测迭代过程中当前LinkedList实例是否被并发修改, 如果是则抛出异常
            if (lastReturned == null) // 如果lastReturned为null, 证明上次调用迭代方法后已经有remove或者add操作, 无法重复操作
                throw new IllegalStateException(); // 抛出异常

            Node<E> lastNext = lastReturned.next; // 暂存上一次返回结点的后继结点
            unlink(lastReturned); // 从链表中移除上一次返回结点
            if (next == lastReturned) // 如果下一个返回结点等于上一次返回结点(即调用previous后调用的remove)
                next = lastNext; // 更新下一次返回结点为其后继节点, 即next字段值往后移动一个节点, 为了不会影响下一次previous调用
            else // 否则是调用next方法后调用的remove方法
                nextIndex--; // 下一个返回结点下标减一, 为了不会影响下一次next调用
            lastReturned = null; // 将上一次返回结点字段值置为null, 连续重复调用remove会抛异常
            expectedModCount++; // 更新操作计数
        }
        /** 见{@link ListIterator#set(Object)} */
        public void set(E e) {
            if (lastReturned == null) // 如果上一次返回值为null, 证明上一次调用next方法后返回结点已经被移除, 或者有add操作, 不能替换元素值
                throw new IllegalStateException(); // 抛出异常
            checkForComodification(); // 检测迭代过程中当前LinkedList实例是否被并发修改, 如果是则抛出异常
            lastReturned.item = e; // 将上一次返回结点包含的元素替换成本方法的参数
        }
        /** 见{@link ListIterator#add(Object)} */
        public void add(E e) {
            checkForComodification(); // 检测迭代过程中当前LinkedList实例是否被并发修改, 如果是则抛出异常
            lastReturned = null; // 将上一次返回值设置为null
            if (next == null) // 如果下一次返回结点值为null
                linkLast(e); //将新增元素添加到链表尾部
            else
                linkBefore(e, next); // 否则将新增元素添加到下一次返回元素前方
            nextIndex++;
            expectedModCount++;
        }
        // 遍历当前序列迭代器剩余元素, 并对每个元素执行给定的action动作
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            while (modCount == expectedModCount && nextIndex < size) { // 遍历剩余元素
                action.accept(next.item); // 对每个元素执行给定动作
                lastReturned = next; // 更新上一次返回结点变量值
                next = next.next; // 更新下一次返回结点变量值
                nextIndex++; // 更新下一个返回结点下标值
            }
            checkForComodification(); // 检测迭代过程中当前LinkedList实例是否被并发修改, 如果是则抛出异常
        }
        // 检测迭代过程中当前LinkedList实例是否被并发修改, 如果是则抛出异常
        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }
    // 结点结构定义, 组成链表的最小单位, LinkedList的灵魂
    private static class Node<E> {
        E item; // 结点包含的元素
        Node<E> next; // 当前结点的后继结点
        Node<E> prev; // 当前结点的前驱结点
        // 构造器
        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }

    /**
     * @since 1.6
     */ /** 见{@link Deque#descendingIterator()} */
    public Iterator<E> descendingIterator() {
        return new DescendingIterator(); // 返回一个倒序的迭代器
    }

    /**
     * Adapter to provide descending iterators via ListItr.previous
     */ // 倒序迭代器, 通过代理ListIterator实现
    private class DescendingIterator implements Iterator<E> {
        private final ListItr itr = new ListItr(size()); // 默认持有初始位置为size的序列迭代器
        public boolean hasNext() {
            return itr.hasPrevious(); // hasNext代理到方法hasPrevious
        }
        public E next() {
            return itr.previous(); // next代理到previous方法
        }
        public void remove() {
            itr.remove(); // remove方法就是代理到remove方法
        }
    }
    /** 调用默认的{@link Object#clone()}方法, 得到当前实例的浅拷贝 */
    @SuppressWarnings("unchecked")
    private LinkedList<E> superClone() {
        try {
            return (LinkedList<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * Returns a shallow copy of this {@code LinkedList}. (The elements
     * themselves are not cloned.)
     *
     * @return a shallow copy of this {@code LinkedList} instance
     */ // LinkedList实现的克隆方法, 光有Object的浅拷贝还不够, 也要将实例中链表每一个结点都复制一份, 多个LinkedList实例中的结点状态不能共享
    public Object clone() {
        LinkedList<E> clone = superClone(); // 通过Object获得实例的浅拷贝

        // Put clone into "virgin" state 处理一些字段初始状态
        clone.first = clone.last = null;
        clone.size = 0;
        clone.modCount = 0;

        // Initialize clone with our elements
        for (Node<E> x = first; x != null; x = x.next) // 遍历原型实例每一个结点
            clone.add(x.item); // 将结点中包含的元素值取出用于构建新的结点对象, 并添加到克隆对象中, 这时原型与克隆出的LinkedList实例中的结点对象状态就不会共享了

        return clone; // 返回浅克隆实例, 内部的结点对象也一次被克隆, 但是结点包含的元素没有进一步深克隆, 所以返回结果对象本质还是浅克隆
    }

    /**
     * Returns an array containing all of the elements in this list
     * in proper sequence (from first to last element).
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this list.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this list
     *         in proper sequence
     */ /** 见{@link List#toArray()} */
    public Object[] toArray() {
        Object[] result = new Object[size]; // 创建一个长度为当前实例size个数长度的数组
        int i = 0;
        for (Node<E> x = first; x != null; x = x.next) // 遍历链表中每一个结点
            result[i++] = x.item; // 将结点中包含的元素放入数组
        return result; // 返回包含当前实例所有元素的数组, 元素类型丢失, 返回的是Object数组
    }

    /**
     * Returns an array containing all of the elements in this list in
     * proper sequence (from first to last element); the runtime type of
     * the returned array is that of the specified array.  If the list fits
     * in the specified array, it is returned therein.  Otherwise, a new
     * array is allocated with the runtime type of the specified array and
     * the size of this list.
     *
     * <p>If the list fits in the specified array with room to spare (i.e.,
     * the array has more elements than the list), the element in the array
     * immediately following the end of the list is set to {@code null}.
     * (This is useful in determining the length of the list <i>only</i> if
     * the caller knows that the list does not contain any null elements.)
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose {@code x} is a list known to contain only strings.
     * The following code can be used to dump the list into a newly
     * allocated array of {@code String}:
     *
     * <pre>
     *     String[] y = x.toArray(new String[0]);</pre>
     *
     * Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     *
     * @param a the array into which the elements of the list are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this list
     * @throws NullPointerException if the specified array is null
     */ /** 见{@link List#toArray(Object[])} */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size) // 如果给定数组容量不够装入当前实例元素
            a = (T[])java.lang.reflect.Array.newInstance(
                                a.getClass().getComponentType(), size); // 利用反射创建一个给定数组元素类型的数组, 长度为当前实例元素个数size
        int i = 0;
        Object[] result = a;
        for (Node<E> x = first; x != null; x = x.next) // 遍历链表中每一个结点
            result[i++] = x.item; // 将结点中包含的元素放入数组

        if (a.length > size) // 如果给定数组长度大于元素个数
            a[size] = null; // 将数组尾部多余位置设置为null

        return a; // 返回包含当前实例所有元素的数组, 此数组保留了元素类型
    }

    private static final long serialVersionUID = 876323262645176354L;

    /**
     * Saves the state of this {@code LinkedList} instance to a stream
     * (that is, serializes it).
     *
     * @serialData The size of the list (the number of elements it
     *             contains) is emitted (int), followed by all of its
     *             elements (each an Object) in the proper order.
     */ // 将当前实例序列化到输出流
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // Write out any hidden serialization magic
        s.defaultWriteObject();

        // Write out size
        s.writeInt(size);

        // Write out all elements in the proper order.
        for (Node<E> x = first; x != null; x = x.next)
            s.writeObject(x.item);
    }

    /**
     * Reconstitutes this {@code LinkedList} instance from a stream
     * (that is, deserializes it).
     */ // 从输入流中反序列化成LinkedList对象实例
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();

        // Read in size
        int size = s.readInt();

        // Read in all elements in the proper order.
        for (int i = 0; i < size; i++)
            linkLast((E)s.readObject());
    }

    /**
     * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
     * and <em>fail-fast</em> {@link Spliterator} over the elements in this
     * list.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#SIZED} and
     * {@link Spliterator#ORDERED}.  Overriding implementations should document
     * the reporting of additional characteristic values.
     *
     * @implNote
     * The {@code Spliterator} additionally reports {@link Spliterator#SUBSIZED}
     * and implements {@code trySplit} to permit limited parallelism..
     *
     * @return a {@code Spliterator} over the elements in this list
     * @since 1.8
     */ /** 见{@link List#spliterator()} */
    @Override
    public Spliterator<E> spliterator() {
        return new LLSpliterator<E>(this, -1, 0); // 返回一个LinkedList可分割迭代器
    }

    /** A customized variant of Spliterators.IteratorSpliterator */
    static final class LLSpliterator<E> implements Spliterator<E> {
        static final int BATCH_UNIT = 1 << 10;  // batch array size increment
        static final int MAX_BATCH = 1 << 25;  // max batch array size;
        final LinkedList<E> list; // null OK unless traversed
        Node<E> current;      // current node; null until initialized
        int est;              // size estimate; -1 until first needed
        int expectedModCount; // initialized when est set
        int batch;            // batch size for splits

        LLSpliterator(LinkedList<E> list, int est, int expectedModCount) {
            this.list = list;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }
        // 获得当前迭代器包含的元素个数(评估值)
        final int getEst() {
            int s; // force initialization
            final LinkedList<E> lst;
            if ((s = est) < 0) {
                if ((lst = list) == null)
                    s = est = 0;
                else {
                    expectedModCount = lst.modCount;
                    current = lst.first;
                    s = est = lst.size;
                }
            }
            return s;
        }
        /** 见{@link Spliterator#estimateSize()} */
        public long estimateSize() { return (long) getEst(); }
        /** 见{@link Spliterator#trySplit()} */
        public Spliterator<E> trySplit() {
            Node<E> p;
            int s = getEst();
            if (s > 1 && (p = current) != null) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                Object[] a = new Object[n];
                int j = 0;
                do { a[j++] = p.item; } while ((p = p.next) != null && j < n);
                current = p;
                batch = j;
                est = s - j;
                return Spliterators.spliterator(a, 0, j, Spliterator.ORDERED);
            }
            return null;
        }
        /** 见{@link Spliterator#forEachRemaining(Consumer)} */
        public void forEachRemaining(Consumer<? super E> action) {
            Node<E> p; int n;
            if (action == null) throw new NullPointerException();
            if ((n = getEst()) > 0 && (p = current) != null) {
                current = null;
                est = 0;
                do {
                    E e = p.item;
                    p = p.next;
                    action.accept(e);
                } while (p != null && --n > 0);
            }
            if (list.modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
        /** 见{@link Spliterator#tryAdvance(Consumer)} */
        public boolean tryAdvance(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) throw new NullPointerException();
            if (getEst() > 0 && (p = current) != null) {
                --est;
                E e = p.item;
                current = p.next;
                action.accept(e);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }
        /** 见{@link Spliterator#characteristics()} */
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

}
