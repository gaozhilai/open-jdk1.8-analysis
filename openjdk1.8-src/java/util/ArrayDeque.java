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
 * Written by Josh Bloch of Google Inc. and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/.
 */

package java.util;

import java.io.Serializable;
import java.util.function.Consumer;
import sun.misc.SharedSecrets;

/**
 * Resizable-array implementation of the {@link Deque} interface.  Array
 * deques have no capacity restrictions; they grow as necessary to support
 * usage.  They are not thread-safe; in the absence of external
 * synchronization, they do not support concurrent access by multiple threads.
 * Null elements are prohibited.  This class is likely to be faster than
 * {@link Stack} when used as a stack, and faster than {@link LinkedList}
 * when used as a queue.
 *
 * <p>Most {@code ArrayDeque} operations run in amortized constant time.
 * Exceptions include {@link #remove(Object) remove}, {@link
 * #removeFirstOccurrence removeFirstOccurrence}, {@link #removeLastOccurrence
 * removeLastOccurrence}, {@link #contains contains}, {@link #iterator
 * iterator.remove()}, and the bulk operations, all of which run in linear
 * time.
 *
 * <p>The iterators returned by this class's {@code iterator} method are
 * <i>fail-fast</i>: If the deque is modified at any time after the iterator
 * is created, in any way except through the iterator's own {@code remove}
 * method, the iterator will generally throw a {@link
 * ConcurrentModificationException}.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behavior at an undetermined time in the
 * future.
 *
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw {@code ConcurrentModificationException} on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness: <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i>
 *
 * <p>This class and its iterator implement all of the
 * <em>optional</em> methods of the {@link Collection} and {@link
 * Iterator} interfaces.
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author  Josh Bloch and Doug Lea
 * @since   1.6
 * @param <E> the type of elements held in this collection
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/5/26 17:46
public class ArrayDeque<E> extends AbstractCollection<E>
                           implements Deque<E>, Cloneable, Serializable
{
    /**
     * The array in which the elements of the deque are stored.
     * The capacity of the deque is the length of this array, which is
     * always a power of two. The array is never allowed to become
     * full, except transiently within an addX method where it is
     * resized (see doubleCapacity) immediately upon becoming full,
     * thus avoiding head and tail wrapping around to equal each
     * other.  We also guarantee that all array cells not holding
     * deque elements are always null.
     */ // ArrayDeque底层用数组存储元素, 用两个下标值标记当前队尾和队首元素, 用数组实现了一个环形队列
    transient Object[] elements; // non-private to simplify nested class access

    /**
     * The index of the element at the head of the deque (which is the
     * element that would be removed by remove() or pop()); or an
     * arbitrary number equal to tail if the deque is empty.
     */ // 记录头结点在数组中的下标, 当队列为空时, head可能为任意下标, 但是head与tail相等
    transient int head;

    /**
     * The index at which the next element would be added to the tail
     * of the deque (via addLast(E), add(E), or push(E)).
     */ // 记录下一个在尾部添加元素的下标, 即当前尾结点下标加1
    transient int tail;

    /**
     * The minimum capacity that we'll use for a newly created deque.
     * Must be a power of 2.
     */ // 数组最小初始化容量为8
    private static final int MIN_INITIAL_CAPACITY = 8;

    // ******  Array allocation and resizing utilities ******
    // 工具方法, 根据指定的数量找出刚好比其大的2的次幂
    private static int calculateSize(int numElements) {
        int initialCapacity = MIN_INITIAL_CAPACITY;
        // Find the best power of two to hold elements.
        // Tests "<=" because arrays aren't kept full.
        if (numElements >= initialCapacity) { // 找到刚好比指定元素数量大的2的次幂, 下方以8作为示例
            initialCapacity = numElements; // 00000000 00000000 00000000 00001000
            initialCapacity |= (initialCapacity >>>  1); // 00000000 00000000 00000000 00001100
            initialCapacity |= (initialCapacity >>>  2); // 00000000 00000000 00000000 00001111
            initialCapacity |= (initialCapacity >>>  4); // 00000000 00000000 00000000 00001111
            initialCapacity |= (initialCapacity >>>  8); // 00000000 00000000 00000000 00001111
            initialCapacity |= (initialCapacity >>> 16); // 00000000 00000000 00000000 00001111
            initialCapacity++; // // 00000000 00000000 00000000 00010000

            if (initialCapacity < 0)   // Too many elements, must back off 元素太多, 原本第31位是1, 计算结果第32位符号位变成1
                initialCapacity >>>= 1;// Good luck allocating 2 ^ 30 elements // 将容量右移1位, 将符号位恢复成0
        }
        return initialCapacity; // 返回计算的容量
    }

    /**
     * Allocates empty array to hold the given number of elements.
     *
     * @param numElements  the number of elements to hold
     */ // 根据给定元素数量计算容量初始化数组
    private void allocateElements(int numElements) {
        elements = new Object[calculateSize(numElements)];
    }

    /**
     * Doubles the capacity of this deque.  Call only when full, i.e.,
     * when head and tail have wrapped around to become equal.
     */ // 将元素数组扩容到原始容量的二倍
    private void doubleCapacity() {
        assert head == tail; // 当head已经与tail相等时, 也就是数组已满, 才应该调用此方法
        int p = head; // 暂存当前头结点下标
        int n = elements.length; // 得到当前数组容量, 当前也就是元素总数
        int r = n - p; // number of elements to the right of p 计算头结点以及头结点右侧总共有几个元素
        int newCapacity = n << 1; // 计算新的容量, 即当前容量二倍
        if (newCapacity < 0) // 如果二倍后变成负数, 发生了溢出, 抛出异常
            throw new IllegalStateException("Sorry, deque too big");
        Object[] a = new Object[newCapacity]; // 初始化扩容后的数组
        System.arraycopy(elements, p, a, 0, r); // 将原始数组头结点以及头结点右侧元素复制到新数组
        System.arraycopy(elements, 0, a, r, p); // 将原始数组尾结点以及尾结点左侧元素复制到新数组, 即原始数组的环形已经展开, 并且从头到尾复制到新的数组头部
        elements = a; // 将元素数组设置为扩容后的新数组
        head = 0; // 头结点下标设置为0
        tail = n; // 尾结点的下一个下标设置为原始数组长度即n
    }

    /**
     * Copies the elements from our element array into the specified array,
     * in order (from first to last element in the deque).  It is assumed
     * that the array is large enough to hold all elements in the deque.
     *
     * @return its argument
     */ // 将当前队列包含的元素复制到给定数组并返回
    private <T> T[] copyElements(T[] a) { // 只考虑head>tail或者head<tail的情况, 因为head==tail时会触发扩容操作, 调用此方法时不会有head==tail的情况
        if (head < tail) { // 当head<tail时, 数组队列没有变成环形, 直接复制[head, tail)元素即可
            System.arraycopy(elements, head, a, 0, size()); // 将当前队列包含元素复制到给定数组
        } else if (head > tail) { // 如果head>tail证明队列在数组中已经是环形队列
            int headPortionLen = elements.length - head; // 计算头结点以及其右侧元素数量
            System.arraycopy(elements, head, a, 0, headPortionLen); // 复制头结点以及其右侧元素到给定数组
            System.arraycopy(elements, 0, a, headPortionLen, tail); // 复制尾结点以及其左侧元素到给定数组, 即环形队列已经展开并复制到了给定数组
        }
        return a; // 返回包含队列元素的给定数组
    }

    /**
     * Constructs an empty array deque with an initial capacity
     * sufficient to hold 16 elements.
     */ // 默认无参构造方法, 使用16作为初始容量
    public ArrayDeque() {
        elements = new Object[16];
    }

    /**
     * Constructs an empty array deque with an initial capacity
     * sufficient to hold the specified number of elements.
     *
     * @param numElements  lower bound on initial capacity of the deque
     */ // 根据给定预期元素数量计算出刚好比起大的2次幂值初始化实例
    public ArrayDeque(int numElements) {
        allocateElements(numElements);
    }

    /**
     * Constructs a deque containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.  (The first element returned by the collection's
     * iterator becomes the first element, or <i>front</i> of the
     * deque.)
     *
     * @param c the collection whose elements are to be placed into the deque
     * @throws NullPointerException if the specified collection is null
     */ // 用给定集合包含的元素初始化队列实例, 元素顺序取决于集合迭代器返回元素顺序
    public ArrayDeque(Collection<? extends E> c) {
        allocateElements(c.size()); // 初始化数组, 分配空间
        addAll(c); // 将集合元素添加到构造的队列实例
    }

    // The main insertion and extraction methods are addFirst,
    // addLast, pollFirst, pollLast. The other methods are defined in
    // terms of these.

    /**
     * Inserts the specified element at the front of this deque.
     *
     * @param e the element to add
     * @throws NullPointerException if the specified element is null
     */ // 在当前队列前端插入给定元素
    public void addFirst(E e) {
        if (e == null) // 检测给定元素不能为null
            throw new NullPointerException();
        elements[head = (head - 1) & (elements.length - 1)] = e; // 如果当前head已经是0, 那么-1的二进制(全1)与上length-1的二进制等于length-1即队列头变成了数组尾部, 环形队列关键在这
        if (head == tail) // 如果head==tail, 即数组已经装满了
            doubleCapacity(); // 扩容数组
    }

    /**
     * Inserts the specified element at the end of this deque.
     *
     * <p>This method is equivalent to {@link #add}.
     *
     * @param e the element to add
     * @throws NullPointerException if the specified element is null
     */ // 在当前队列尾部插入给定元素
    public void addLast(E e) {
        if (e == null) // 检测插入的元素不能为null
            throw new NullPointerException();
        elements[tail] = e; // 直接在下标tail处放入给定元素
        if ( (tail = (tail + 1) & (elements.length - 1)) == head) // 如果tail已经是最后一个元素下标, 那么tail+1就是length, length与上length-1等于0, 尾部从数组头部开始, 形成了环形队列
            doubleCapacity(); // 如果tail值与head相等, 那么执行扩容操作
    }

    /**
     * Inserts the specified element at the front of this deque.
     *
     * @param e the element to add
     * @return {@code true} (as specified by {@link Deque#offerFirst})
     * @throws NullPointerException if the specified element is null
     */ /** 见{@link Deque#offerFirst(Object)} */
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    /**
     * Inserts the specified element at the end of this deque.
     *
     * @param e the element to add
     * @return {@code true} (as specified by {@link Deque#offerLast})
     * @throws NullPointerException if the specified element is null
     */ /** 见{@link Deque#offerLast(Object)} */
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */ /** 见{@link Deque#removeFirst()} */
    public E removeFirst() {
        E x = pollFirst();
        if (x == null)
            throw new NoSuchElementException();
        return x;
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */ /** 见{@link Deque#removeLast()} */
    public E removeLast() {
        E x = pollLast();
        if (x == null)
            throw new NoSuchElementException();
        return x;
    }
    /** 见{@link Deque#pollFirst()} */
    public E pollFirst() {
        int h = head; // 暂存头结点下标
        @SuppressWarnings("unchecked")
        E result = (E) elements[h]; // 暂存下标对应的头元素
        // Element is null if deque empty
        if (result == null) // 如果元素是null, 代表队列为空
            return null; // 没有元素被移除, 返回null
        elements[h] = null;     // Must null out slot 清空对应下标元素
        head = (h + 1) & (elements.length - 1); // 更新head值为head+1, 与操作考虑到了环形队列的情况
        return result; // 返回移除的元素
    }
    /** 见{@link Deque#pollLast()} */
    public E pollLast() {
        int t = (tail - 1) & (elements.length - 1); // 获得尾结点下标, 与操作考虑到了环形队列的情况
        @SuppressWarnings("unchecked")
        E result = (E) elements[t]; // 暂存尾结点值
        if (result == null) // 如果为null证明队列是空队列
            return null; // 没有元素被移除返回null
        elements[t] = null; // 清空指定下标的元素
        tail = t; // 更新tail值
        return result; // 返回被移除的元素
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */ /** 见{@link Deque#getFirst()} */
    public E getFirst() {
        @SuppressWarnings("unchecked")
        E result = (E) elements[head];
        if (result == null)
            throw new NoSuchElementException();
        return result;
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */ /** 见{@link Deque#getLast()} */
    public E getLast() {
        @SuppressWarnings("unchecked")
        E result = (E) elements[(tail - 1) & (elements.length - 1)];
        if (result == null)
            throw new NoSuchElementException();
        return result;
    }
    /** 见{@link Deque#peekFirst()} */
    @SuppressWarnings("unchecked")
    public E peekFirst() {
        // elements[head] is null if deque empty
        return (E) elements[head];
    }
    /** 见{@link Deque#peekLast()} */
    @SuppressWarnings("unchecked")
    public E peekLast() {
        return (E) elements[(tail - 1) & (elements.length - 1)];
    }

    /**
     * Removes the first occurrence of the specified element in this
     * deque (when traversing the deque from head to tail).
     * If the deque does not contain the element, it is unchanged.
     * More formally, removes the first element {@code e} such that
     * {@code o.equals(e)} (if such an element exists).
     * Returns {@code true} if this deque contained the specified element
     * (or equivalently, if this deque changed as a result of the call).
     *
     * @param o element to be removed from this deque, if present
     * @return {@code true} if the deque contained the specified element
     */ /** 见{@link Deque#removeFirstOccurrence(Object)} */
    public boolean removeFirstOccurrence(Object o) {
        if (o == null) // 如果要移除的是null
            return false; // 直接返回false, 因为当前队列中不允许存在null元素
        int mask = elements.length - 1; // 获得掩码, 后续用于等价取余的操作
        int i = head; // 循环从头结点head开始
        Object x;
        while ( (x = elements[i]) != null) { // 如果当前遍历的元素不为null, 证明是存在的有效元素
            if (o.equals(x)) { // 如果等于给定参数
                delete(i); // 移除当前下标的元素
                return true; // 返回true
            }
            i = (i + 1) & mask; // 否则增加1继续遍历元素, 如果i大于数组最大下标, 那么与上方的掩码与操作相当于取余(环形队列的情况)
        }
        return false; // 没有元素被移除, 返回false
    }

    /**
     * Removes the last occurrence of the specified element in this
     * deque (when traversing the deque from head to tail).
     * If the deque does not contain the element, it is unchanged.
     * More formally, removes the last element {@code e} such that
     * {@code o.equals(e)} (if such an element exists).
     * Returns {@code true} if this deque contained the specified element
     * (or equivalently, if this deque changed as a result of the call).
     *
     * @param o element to be removed from this deque, if present
     * @return {@code true} if the deque contained the specified element
     */ /** 见{@link Deque#removeLastOccurrence(Object)} */
    public boolean removeLastOccurrence(Object o) {
        if (o == null) // 如果要移除的是null
            return false; // 直接返回false, 因为当前队列中不允许存在null元素
        int mask = elements.length - 1; // 获得掩码, 后续用于等价取余的操作
        int i = (tail - 1) & mask; // 从tail-1开始从后到前遍历元素, 与上掩码, 防止环形队列的情况
        Object x;
        while ( (x = elements[i]) != null) { // 如果当前遍历的元素不为null, 证明是存在的有效元素
            if (o.equals(x)) { // 如果等于给定参数
                delete(i); // 移除当前下标的元素
                return true; // 返回true
            }
            i = (i - 1) & mask; // 否则减少1继续遍历元素, 如果i小于0, (比如-1时)那么与上方的掩码与操作相当于从length-1开始继续遍历(环形队列的情况)
        }
        return false; // 没有元素被移除, 返回false
    }

    // *** Queue methods ***

    /**
     * Inserts the specified element at the end of this deque.
     *
     * <p>This method is equivalent to {@link #addLast}.
     *
     * @param e the element to add
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws NullPointerException if the specified element is null
     */ /** 队列尾部添加指定元素, 见{@link Deque#add(Object)} */
    public boolean add(E e) {
        addLast(e);
        return true;
    }

    /**
     * Inserts the specified element at the end of this deque.
     *
     * <p>This method is equivalent to {@link #offerLast}.
     *
     * @param e the element to add
     * @return {@code true} (as specified by {@link Queue#offer})
     * @throws NullPointerException if the specified element is null
     */ /** 队列尾部添加给定元素, 见{@link Deque#offer(Object)} */
    public boolean offer(E e) {
        return offerLast(e);
    }

    /**
     * Retrieves and removes the head of the queue represented by this deque.
     *
     * This method differs from {@link #poll poll} only in that it throws an
     * exception if this deque is empty.
     *
     * <p>This method is equivalent to {@link #removeFirst}.
     *
     * @return the head of the queue represented by this deque
     * @throws NoSuchElementException {@inheritDoc}
     */ /** 移除队列头部一个元素并返回, 见{@link Deque#remove()} */
    public E remove() {
        return removeFirst();
    }

    /**
     * Retrieves and removes the head of the queue represented by this deque
     * (in other words, the first element of this deque), or returns
     * {@code null} if this deque is empty.
     *
     * <p>This method is equivalent to {@link #pollFirst}.
     *
     * @return the head of the queue represented by this deque, or
     *         {@code null} if this deque is empty
     */ /** 见{@link Deque#poll()} */
    public E poll() {
        return pollFirst();
    }

    /**
     * Retrieves, but does not remove, the head of the queue represented by
     * this deque.  This method differs from {@link #peek peek} only in
     * that it throws an exception if this deque is empty.
     *
     * <p>This method is equivalent to {@link #getFirst}.
     *
     * @return the head of the queue represented by this deque
     * @throws NoSuchElementException {@inheritDoc}
     */ /** 见{@link Deque#element()} */
    public E element() {
        return getFirst();
    }

    /**
     * Retrieves, but does not remove, the head of the queue represented by
     * this deque, or returns {@code null} if this deque is empty.
     *
     * <p>This method is equivalent to {@link #peekFirst}.
     *
     * @return the head of the queue represented by this deque, or
     *         {@code null} if this deque is empty
     */ /** 见{@link Deque#peek()} */
    public E peek() {
        return peekFirst();
    }

    // *** Stack methods ***

    /**
     * Pushes an element onto the stack represented by this deque.  In other
     * words, inserts the element at the front of this deque.
     *
     * <p>This method is equivalent to {@link #addFirst}.
     *
     * @param e the element to push
     * @throws NullPointerException if the specified element is null
     */ /** 见{@link Deque#push(Object)} */
    public void push(E e) {
        addFirst(e);
    }

    /**
     * Pops an element from the stack represented by this deque.  In other
     * words, removes and returns the first element of this deque.
     *
     * <p>This method is equivalent to {@link #removeFirst()}.
     *
     * @return the element at the front of this deque (which is the top
     *         of the stack represented by this deque)
     * @throws NoSuchElementException {@inheritDoc}
     */ /** 见{@link Deque#pop()} */
    public E pop() {
        return removeFirst();
    }
    // 删除指定下标元素前进行实例内部数据检查
    private void checkInvariants() {
        assert elements[tail] == null; // tail对应的元素为null
        assert head == tail ? elements[head] == null : // head==tail时只允许是空队列(否则head==tail时会扩容)
            (elements[head] != null &&
             elements[(tail - 1) & (elements.length - 1)] != null); // head不等于tail时头结点与尾结点都不为null
        assert elements[(head - 1) & (elements.length - 1)] == null; // 头结点前一个元素为null
    }

    /**
     * Removes the element at the specified position in the elements array,
     * adjusting head and tail as necessary.  This can result in motion of
     * elements backwards or forwards in the array.
     *
     * <p>This method is called delete rather than remove to emphasize
     * that its semantics differ from those of {@link List#remove(int)}.
     *
     * @return true if elements moved backwards
     */ // 删除指定下标对应的元素
    private boolean delete(int i) {
        checkInvariants(); // 校验数据合法性
        final Object[] elements = this.elements; // 暂存元素数组
        final int mask = elements.length - 1; // 求出掩码, 因为数组长度都是2的次幂, -1后二进制有值的位都是1
        final int h = head; // 暂存头结点下标
        final int t = tail; // 暂存尾结点下一个下标
        final int front = (i - h) & mask; // 要删除元素前方元素数量
        final int back  = (t - i) & mask; // 要删除元素后方元素数量

        // Invariant: head <= i < tail mod circularity
        if (front >= ((t - h) & mask)) // 如果i前方元素个数大于总元素个数, 代表队列元素被并发移除了, 即当前i对应的元素已经不存在了, 不需要删除了
            throw new ConcurrentModificationException(); // 抛出异常

        // Optimize for least element motion
        if (front < back) { // 如果下标i前方的元素(距离h有几个元素, 不包含i), 少于i后方的元素(距离t有几个元素, 包含i)
            if (h <= i) {
                System.arraycopy(elements, h, elements, h + 1, front);
            } else { // Wrap around
                System.arraycopy(elements, 0, elements, 1, i); // 数组前面部分元素向后移动一位, 将i值覆盖
                elements[0] = elements[mask]; // 将最前方移动出来的1个空位用元素数组最后一个元素填充
                System.arraycopy(elements, h, elements, h + 1, mask - h); // 将元素数组尾部元素向后移动一位, 将数组最后一个元素的空位填满
            }
            elements[h] = null; // 将移动产生的头部空位置为null
            head = (h + 1) & mask; // head值加一
            return false; // 代表通过移动i前方的元素来删除i, 不影响迭代器remove方法后, cursor游标位置
        } else { // 下标i前方的元素(距离h有几个元素, 不包含i), 多于i后方的元素(距离t有几个元素, 包含i)
            if (i < t) { // Copy the null tail as well
                System.arraycopy(elements, i + 1, elements, i, back); // 将i后方元素向前移动一个位置, 将i覆盖
                tail = t - 1; // tail位置前移一位
            } else { // Wrap around
                System.arraycopy(elements, i + 1, elements, i, mask - i); // 将i后方元素前移1位, element[mask]为空位
                elements[mask] = elements[0]; // 用element[0]填补element[mask]
                System.arraycopy(elements, 1, elements, 0, t); // 用element[0]后方的元素前移1位, 将element[0]补全
                tail = (t - 1) & mask; // tail值减一
            }
            return true; // 代表通过移动i后方的元素来删除i, 影响迭代器remove方法后, cursor游标位置
        }
    }

    // *** Collection Methods ***

    /**
     * Returns the number of elements in this deque.
     *
     * @return the number of elements in this deque
     */ /** 见{@link Deque#size()} */
    public int size() {
        return (tail - head) & (elements.length - 1); // 通过与上掩码操作, 考虑环形队列情况, 求出队列包含元素数量
    }

    /**
     * Returns {@code true} if this deque contains no elements.
     *
     * @return {@code true} if this deque contains no elements
     */ /** 见{@link Deque#isEmpty()} */
    public boolean isEmpty() {
        return head == tail; // 如果head等于tail, 那么队列为空
    }

    /**
     * Returns an iterator over the elements in this deque.  The elements
     * will be ordered from first (head) to last (tail).  This is the same
     * order that elements would be dequeued (via successive calls to
     * {@link #remove} or popped (via successive calls to {@link #pop}).
     *
     * @return an iterator over the elements in this deque
     */ /** 见{@link Deque#iterator()} */
    public Iterator<E> iterator() {
        return new DeqIterator(); // 返回ArrayDeque版本的迭代器
    }
    /** 见{@link Deque#descendingIterator()} */
    public Iterator<E> descendingIterator() {
        return new DescendingIterator(); // 返回ArrayDeque版本的倒序迭代器
    }
    // ArrayDeque版本的迭代器定义
    private class DeqIterator implements Iterator<E> {
        /**
         * Index of element to be returned by subsequent call to next.
         */ // 定义cursor从第一个元素开始, 此字段记录了下一次调用next方法后返回的元素对应的下标
        private int cursor = head;

        /**
         * Tail recorded at construction (also in remove), to stop
         * iterator and also to check for comodification.
         */ // 定义fence字段, 记录tail值, 用于停止迭代器(环形队列, 迭代结束条件), 和检测并发结构性修改
        private int fence = tail;

        /**
         * Index of element returned by most recent call to next.
         * Reset to -1 if element is deleted by a call to remove.
         */ // 上一次调用next方法返回的下标值, 如果调用next后调用了remove方法, 那么此字段被赋值为-1
        private int lastRet = -1;
        /** 见{@link Iterator#hasNext()} */
        public boolean hasNext() {
            return cursor != fence; // cursor不等于fence时代表还存在需要迭代的元素
        }
        /** 见{@link Iterator#next()} */
        public E next() {
            if (cursor == fence) // 如果cursor等于fence了, 代表没有下一个元素
                throw new NoSuchElementException(); // 抛出异常
            @SuppressWarnings("unchecked")
            E result = (E) elements[cursor]; // 暂存要返回的元素
            // This check doesn't catch all possible comodifications,
            // but does catch the ones that corrupt traversal
            if (tail != fence || result == null) // 如果tail值与fence不等, 或者通过cursor找到的元素不存在[这样检测不能检测到所有并发修改, 但是能检测出影响遍历的并发修改]
                throw new ConcurrentModificationException(); // 抛出并发修改异常
            lastRet = cursor; // 记录本次调用next返回元素的下标
            cursor = (cursor + 1) & (elements.length - 1); // cursor后移一位, 为下次调用next做准备
            return result; // 返回本次方法调用对应的元素
        }
        /** 见{@link Iterator#remove()} */
        public void remove() {
            if (lastRet < 0) // 当字段lastRet小于0(-1)时, 代表没有调用过next方法或者调用一次next, 重复调用remove
                throw new IllegalStateException(); // 抛出异常
            if (delete(lastRet)) { // if left-shifted, undo increment in next() 如果是i后方元素左移完成删除操作, 那么cursor-1修正游标值
                cursor = (cursor - 1) & (elements.length - 1); // cursor减一, 与操作考虑环形队列
                fence = tail; // tail值有变动, 更新fence值
            }
            lastRet = -1; // 调用remove方法后将lastRet置为-1
        }
        /** 见{@link Iterator#forEachRemaining(Consumer)} */
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            Object[] a = elements; // 暂存元素数组
            int m = a.length - 1, f = fence, i = cursor;
            cursor = f; // 将游标值置为tail, 调用此方法后没有剩余元素需要遍历
            while (i != f) { // 只要当前下标没遍历到fence就继续遍历
                @SuppressWarnings("unchecked") E e = (E)a[i]; // 暂存本次遍历元素
                i = (i + 1) & m; // 下标自增
                if (e == null) // 如果元素为null, 那么就是被并发修改了
                    throw new ConcurrentModificationException(); // 抛出异常
                action.accept(e); // 对当前遍历元素执行给定动作action
            }
        }
    }
    /** ArrayDeque版本的倒序迭代器定义 */
    private class DescendingIterator implements Iterator<E> {
        /*
         * This class is nearly a mirror-image of DeqIterator, using
         * tail instead of head for initial cursor, and head instead of
         * tail for fence.
         */
        private int cursor = tail; // 定义cursor初始值从尾部tail下标开始
        private int fence = head; // 定义迭代终止条件为head
        private int lastRet = -1; // 用于保存上次next返回元素下标
        /** 见{@link Iterator#hasNext()} */
        public boolean hasNext() {
            return cursor != fence; // 当前游标不等于fence证明还有剩余元素需要遍历
        }
        /** 见{@link Iterator#next()} */
        public E next() {
            if (cursor == fence) // 如果cursor等于fence证明没有剩余元素需要遍历
                throw new NoSuchElementException(); // 抛出异常
            cursor = (cursor - 1) & (elements.length - 1); // cursor值更新, 减一
            @SuppressWarnings("unchecked")
            E result = (E) elements[cursor]; // 暂存本次要返回的元素
            if (head != fence || result == null) // 检测是否被并发修改
                throw new ConcurrentModificationException(); // 被并发修改抛出异常
            lastRet = cursor; // 否则记录本次返回元素下标值
            return result; // 将元素返回
        }
        /** 见{@link Iterator#remove()} */
        public void remove() {
            if (lastRet < 0) // 当字段lastRet小于0(-1)时, 代表没有调用过next方法或者调用一次next, 重复调用remove
                throw new IllegalStateException(); // 抛出异常
            if (!delete(lastRet)) { // 如果是移动前方元素完成删除, 当前倒序迭代器next是往前迭代, 所以要修正cursor
                cursor = (cursor + 1) & (elements.length - 1); // cursor加一
                fence = head; // 更新fence为新的head值
            }
            lastRet = -1; // 将lastRet置为-1
        }
    }

    /**
     * Returns {@code true} if this deque contains the specified element.
     * More formally, returns {@code true} if and only if this deque contains
     * at least one element {@code e} such that {@code o.equals(e)}.
     *
     * @param o object to be checked for containment in this deque
     * @return {@code true} if this deque contains the specified element
     */ /** 见{@link Deque#contains(Object)} */
    public boolean contains(Object o) {
        if (o == null) // ArrayDeque不允许放入null元素, 如果要寻找的元素是null
            return false; // 直接返回false
        int mask = elements.length - 1; // 求出当前元素数组长度对应的掩码
        int i = head; // 暂存头结点下标
        Object x;
        while ( (x = elements[i]) != null) { // 从头元素向后遍历
            if (o.equals(x)) // 如果遍历到的元素与给定元素相等
                return true; // 返回true
            i = (i + 1) & mask; // 否则下标增加继续遍历
        }
        return false; // 遍历完整个队列也没有找到相等的元素, 返回false
    }

    /**
     * Removes a single instance of the specified element from this deque.
     * If the deque does not contain the element, it is unchanged.
     * More formally, removes the first element {@code e} such that
     * {@code o.equals(e)} (if such an element exists).
     * Returns {@code true} if this deque contained the specified element
     * (or equivalently, if this deque changed as a result of the call).
     *
     * <p>This method is equivalent to {@link #removeFirstOccurrence(Object)}.
     *
     * @param o element to be removed from this deque, if present
     * @return {@code true} if this deque contained the specified element
     */ /** 见{@link Deque#remove(Object)} */
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    /**
     * Removes all of the elements from this deque.
     * The deque will be empty after this call returns.
     */ /** 见{@link Deque#clear()} */
    public void clear() {
        int h = head; // 暂存head
        int t = tail; // 暂存tail
        if (h != t) { // clear all cells head不等于tail证明队列有元素, 才需要清空
            head = tail = 0; // 将head与tail置为0, 从head与tail的角度来清空队列
            int i = h;
            int mask = elements.length - 1; // 求出当前元素数组长度对应的掩码
            do {
                elements[i] = null; // 将当前遍历的元素置空
                i = (i + 1) & mask; // 下标i增加, 准备下次遍历
            } while (i != t); // 从头到尾遍历每一个元素
        }
    }

    /**
     * Returns an array containing all of the elements in this deque
     * in proper sequence (from first to last element).
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this deque.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this deque
     */ /** 见{@link AbstractCollection#toArray()} */
    public Object[] toArray() {
        return copyElements(new Object[size()]); // 将队列包含的元素拷贝到Object类型的数组并返回
    }

    /**
     * Returns an array containing all of the elements in this deque in
     * proper sequence (from first to last element); the runtime type of the
     * returned array is that of the specified array.  If the deque fits in
     * the specified array, it is returned therein.  Otherwise, a new array
     * is allocated with the runtime type of the specified array and the
     * size of this deque.
     *
     * <p>If this deque fits in the specified array with room to spare
     * (i.e., the array has more elements than this deque), the element in
     * the array immediately following the end of the deque is set to
     * {@code null}.
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose {@code x} is a deque known to contain only strings.
     * The following code can be used to dump the deque into a newly
     * allocated array of {@code String}:
     *
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     *
     * @param a the array into which the elements of the deque are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose
     * @return an array containing all of the elements in this deque
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this deque
     * @throws NullPointerException if the specified array is null
     */ /** 见{@link AbstractCollection#toArray(Object[])} */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        int size = size(); // 暂存当前队列元素数量
        if (a.length < size) // 如果给定数组容量小于队列实际包含元素数量
            a = (T[])java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size); // 那新创建一个与给定数组同类型的数组
        copyElements(a); // 将队列元素复制到新数组中
        if (a.length > size) // 如果给定数组容量大于队列实际包含元素容量
            a[size] = null; // 将拷贝到给定数组的实际队列元素后面一个位置设置为null, 标记队列元素已经结束
        return a; // 返回包含队列元素的数组
    }

    // *** Object methods ***

    /**
     * Returns a copy of this deque.
     *
     * @return a copy of this deque
     */ /** 返回一个当前队列实例的浅拷贝实例 */
    public ArrayDeque<E> clone() {
        try {
            @SuppressWarnings("unchecked")
            ArrayDeque<E> result = (ArrayDeque<E>) super.clone();
            result.elements = Arrays.copyOf(elements, elements.length);
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    private static final long serialVersionUID = 2340985798034038923L;

    /**
     * Saves this deque to a stream (that is, serializes it).
     *
     * @serialData The current size ({@code int}) of the deque,
     * followed by all of its elements (each an object reference) in
     * first-to-last order.
     */ // 将当前队列实例序列化到输出流
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        s.defaultWriteObject();

        // Write out size
        s.writeInt(size());

        // Write out elements in order.
        int mask = elements.length - 1;
        for (int i = head; i != tail; i = (i + 1) & mask)
            s.writeObject(elements[i]);
    }

    /**
     * Reconstitutes this deque from a stream (that is, deserializes it).
     */ // 从输入流反序列化实例
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();

        // Read in size and allocate array
        int size = s.readInt();
        int capacity = calculateSize(size);
        SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, capacity);
        allocateElements(size);
        head = 0;
        tail = size;

        // Read in all elements in the proper order.
        for (int i = 0; i < size; i++)
            elements[i] = s.readObject();
    }

    /**
     * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
     * and <em>fail-fast</em> {@link Spliterator} over the elements in this
     * deque.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#SIZED},
     * {@link Spliterator#SUBSIZED}, {@link Spliterator#ORDERED}, and
     * {@link Spliterator#NONNULL}.  Overriding implementations should document
     * the reporting of additional characteristic values.
     *
     * @return a {@code Spliterator} over the elements in this deque
     * @since 1.8
     */ /** 见{@link Collection#spliterator()} */
    public Spliterator<E> spliterator() {
        return new DeqSpliterator<E>(this, -1, -1); // 返回一个ArrayDeque版本的可分割迭代器
    }
    // ArrayDeque版本的可分割迭代器
    static final class DeqSpliterator<E> implements Spliterator<E> {
        private final ArrayDeque<E> deq; // 迭代器包含的ArrayDeque实例
        private int fence;  // -1 until first use 初始值为-1
        private int index;  // current index, modified on traverse/split 当前迭代的元素下标

        /** Creates new spliterator covering the given array and range */
        DeqSpliterator(ArrayDeque<E> deq, int origin, int fence) { // 根据给定的ArrayDeque实例创建新的可分割迭代器, 并且覆盖给定范围的元素
            this.deq = deq;
            this.index = origin;
            this.fence = fence;
        }
        /** 获取fence值 */
        private int getFence() { // force initialization
            int t;
            if ((t = fence) < 0) { // 如果fence没有指定值
                t = fence = deq.tail; // 那么将其设置为当前包含的ArrayDeque实例的tail值
                index = deq.head; // index初始值设置为包含的ArrayDeque实例的head值
            }
            return t; // 返回fence的值
        }
        /** 见{@link Spliterator#trySplit()} */
        public DeqSpliterator<E> trySplit() {
            int t = getFence(), h = index, n = deq.elements.length; // 暂存fence值给t, 当前迭代坐标给h, 当前ArrayDeque实例元素数组长度给n
            if (h != t && ((h + 1) & (n - 1)) != t) { // ArrayDeque实例不为空, 并且剩余大于1个元素需要遍历
                if (h > t)
                    t += n; // @QUESTION 加n后的确能得到正确的一半的位置, 不过原理?
                int m = ((h + t) >>> 1) & (n - 1); // 将剩余元素, 中间位置作为新的可分割迭代器实例的终点fence
                return new DeqSpliterator<>(deq, h, index = m); // 返回新的可分割迭代器, 将当前可分割迭代器实例index更新为新实例的fence值
            }
            return null; // 剩余需要遍历的元素不大于1个, 直接返回null, 不再生成新的可分割迭代器
        }
        /** 见{@link Spliterator#forEachRemaining(Consumer)} */
        public void forEachRemaining(Consumer<? super E> consumer) {
            if (consumer == null)
                throw new NullPointerException();
            Object[] a = deq.elements; // 暂存当前ArrayDeque实例的元素数组
            int m = a.length - 1, f = getFence(), i = index; // 获得当前数组长度掩码, 获得当前可分割迭代器终止下标fence, 获得当前可分割迭代器迭代的元素下标
            index = f; // 设置下标等于fence, 即调用此方法后, 当前可分割迭代器实例没有剩余需要迭代的元素
            while (i != f) { // 只要迭代下标没到终止下标就继续遍历
                @SuppressWarnings("unchecked") E e = (E)a[i]; // 暂存当前遍历到的元素
                i = (i + 1) & m; // 遍历下标增加, 为下一轮遍历做准备
                if (e == null) // 如果当前遍历到的元素不存在, 证明队列被并发修改
                    throw new ConcurrentModificationException(); // 抛出异常
                consumer.accept(e); // 为当前遍历到的元素执行给定动作consumer
            }
        }
        /** 见{@link Spliterator#tryAdvance(Consumer)} */
        public boolean tryAdvance(Consumer<? super E> consumer) {
            if (consumer == null)
                throw new NullPointerException();
            Object[] a = deq.elements; // 暂存当前ArrayDeque的元素数组
            int m = a.length - 1, f = getFence(), i = index; // 获得当前数组长度掩码, 获得当前可分割迭代器终止下标fence, 获得当前可分割迭代器迭代的元素下标
            if (i != fence) { // 如果当前遍历的下标不等于终止下标, 证明有元素需要遍历
                @SuppressWarnings("unchecked") E e = (E)a[i]; // 暂存要遍历的元素
                index = (i + 1) & m; // 遍历下标增加, 为下一轮遍历做准备
                if (e == null) // 如果当前遍历到的元素不存在, 证明队列被并发修改
                    throw new ConcurrentModificationException(); // 抛出异常
                consumer.accept(e); // 为当前遍历到的元素执行给定动作consumer
                return true; // 返回true
            }
            return false; // 没有元素需要遍历, 返回false
        }
        /** 见{@link Spliterator#estimateSize()} */
        public long estimateSize() {
            int n = getFence() - index;
            if (n < 0)
                n += deq.elements.length;
            return (long) n;
        }
        /** 见{@link Spliterator#characteristics()} */
        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | // 特征为有序, 有限元素数量
                Spliterator.NONNULL | Spliterator.SUBSIZED; // 非空元素, 子迭代器也是有限元素数量
        }
    }

}
