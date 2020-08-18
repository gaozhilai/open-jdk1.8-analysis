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
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group.  Adapted and released, under explicit permission,
 * from JDK ArrayList.java which carries the following copyright:
 *
 * Copyright 1997 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */

package java.util.concurrent;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import sun.misc.SharedSecrets;

/**
 * A thread-safe variant of {@link java.util.ArrayList} in which all mutative
 * operations ({@code add}, {@code set}, and so on) are implemented by
 * making a fresh copy of the underlying array.
 *
 * <p>This is ordinarily too costly, but may be <em>more</em> efficient
 * than alternatives when traversal operations vastly outnumber
 * mutations, and is useful when you cannot or don't want to
 * synchronize traversals, yet need to preclude interference among
 * concurrent threads.  The "snapshot" style iterator method uses a
 * reference to the state of the array at the point that the iterator
 * was created. This array never changes during the lifetime of the
 * iterator, so interference is impossible and the iterator is
 * guaranteed not to throw {@code ConcurrentModificationException}.
 * The iterator will not reflect additions, removals, or changes to
 * the list since the iterator was created.  Element-changing
 * operations on iterators themselves ({@code remove}, {@code set}, and
 * {@code add}) are not supported. These methods throw
 * {@code UnsupportedOperationException}.
 *
 * <p>All elements are permitted, including {@code null}.
 *
 * <p>Memory consistency effects: As with other concurrent
 * collections, actions in a thread prior to placing an object into a
 * {@code CopyOnWriteArrayList}
 * <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 * actions subsequent to the access or removal of that element from
 * the {@code CopyOnWriteArrayList} in another thread.
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @since 1.5
 * @author Doug Lea
 * @param <E> the type of elements held in this collection
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/8/14 15:13
public class CopyOnWriteArrayList<E> // 在修改操作时先复制一份源数据的快照, 然后修改快照, 将快照设置为真实数据. (这就是在写操作前先复制的名字由来CopyOnWrite)
    implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
    private static final long serialVersionUID = 8673264195747942595L;

    /** The lock protecting all mutators 用于保证修改操作的可重入锁 */
    final transient ReentrantLock lock = new ReentrantLock();

    /** The array, accessed only via getArray/setArray. 当前实例包含的数据数组, 只能通过getArray和setArray方法访问 */
    private transient volatile Object[] array;

    /**
     * Gets the array.  Non-private so as to also be accessible
     * from CopyOnWriteArraySet class.
     */ /** 获得底层数据数组, 此方法非private是为了给{@link CopyOnWriteArraySet}复用 */
    final Object[] getArray() {
        return array;
    }

    /**
     * Sets the array.
     */ // 设置替换底层数据数组, 每个调用此方法的修改方法都由可重入锁保证线程安全, 所以并发修改时不会导致数据错误问题
    final void setArray(Object[] a) {
        array = a;
    }

    /**
     * Creates an empty list.
     */ // 默认创建一个容量为0的实例
    public CopyOnWriteArrayList() {
        setArray(new Object[0]);
    }

    /**
     * Creates a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection of initially held elements
     * @throws NullPointerException if the specified collection is null
     */ // 使用给定集合包含的元素初始化实例
    public CopyOnWriteArrayList(Collection<? extends E> c) {
        Object[] elements;
        if (c.getClass() == CopyOnWriteArrayList.class) // 如果给定集合本身就是CopyOnWriteArrayList实例
            elements = ((CopyOnWriteArrayList<?>)c).getArray(); // 直接通过getArray方法获得其包含的元素数组
        else {
            elements = c.toArray(); // 否则调用集合的toArray方法获得数组
            // toArray返回的数组可能是Object之外的具体类型, 比如Integer[], 其虽然能赋值给Object[], 但是数组再设置非Integer类型的数据就会报错
            if (elements.getClass() != Object[].class) // 判断如果返回的数组不是Object类型
                elements = Arrays.copyOf(elements, elements.length, Object[].class); // 那么直接将返回数组的元素复制到新的Object数组并赋值给elements
        }
        setArray(elements); // 将包含初始元素的数组设置到实例成员变量
    }

    /**
     * Creates a list holding a copy of the given array.
     *
     * @param toCopyIn the array (a copy of this array is used as the
     *        internal array)
     * @throws NullPointerException if the specified array is null
     */ // 使用给定的数组元素初始化实例
    public CopyOnWriteArrayList(E[] toCopyIn) {
        setArray(Arrays.copyOf(toCopyIn, toCopyIn.length, Object[].class));
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */ // 返回当前实例包含的元素个数
    public int size() {
        return getArray().length;
    }

    /**
     * Returns {@code true} if this list contains no elements.
     *
     * @return {@code true} if this list contains no elements
     */ // 判断当前实例是否为空
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Tests for equality, coping with nulls.
     */ // 判断给定的两个实例是否相等
    private static boolean eq(Object o1, Object o2) {
        return (o1 == null) ? o2 == null : o1.equals(o2);
    }

    /**
     * static version of indexOf, to allow repeated calls without
     * needing to re-acquire array each time.
     * @param o element to search for 要查询的对象
     * @param elements the array 被查询的数组
     * @param index first index to search 开始搜索的下标
     * @param fence one past last index to search 结束搜索的下标
     * @return index of element, or -1 if absent 要搜索对象在数组中的下标, 找不到返回-1
     */ // 检测指定数组中是否有给定的对象
    private static int indexOf(Object o, Object[] elements,
                               int index, int fence) {
        if (o == null) {
            for (int i = index; i < fence; i++)
                if (elements[i] == null)
                    return i;
        } else {
            for (int i = index; i < fence; i++)
                if (o.equals(elements[i]))
                    return i;
        }
        return -1;
    }

    /**
     * static version of lastIndexOf.
     * @param o element to search for
     * @param elements the array
     * @param index first index to search
     * @return index of element, or -1 if absent
     */ // 静态工具类, 从给定数组末尾开始搜索给定元素, 返回要搜索的元素下标, 搜索不到返回-1
    private static int lastIndexOf(Object o, Object[] elements, int index) {
        if (o == null) {
            for (int i = index; i >= 0; i--)
                if (elements[i] == null)
                    return i;
        } else {
            for (int i = index; i >= 0; i--)
                if (o.equals(elements[i]))
                    return i;
        }
        return -1;
    }

    /**
     * Returns {@code true} if this list contains the specified element.
     * More formally, returns {@code true} if and only if this list contains
     * at least one element {@code e} such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this list is to be tested
     * @return {@code true} if this list contains the specified element
     */ // 判断当前实例是否包含给定元素
    public boolean contains(Object o) {
        Object[] elements = getArray();
        return indexOf(o, elements, 0, elements.length) >= 0;
    }

    /**
     * {@inheritDoc}
     */ // 返回当前实例中给定元素的下标
    public int indexOf(Object o) {
        Object[] elements = getArray();
        return indexOf(o, elements, 0, elements.length);
    }

    /**
     * Returns the index of the first occurrence of the specified element in
     * this list, searching forwards from {@code index}, or returns -1 if
     * the element is not found.
     * More formally, returns the lowest index {@code i} such that
     * <tt>(i&nbsp;&gt;=&nbsp;index&nbsp;&amp;&amp;&nbsp;(e==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;e.equals(get(i))))</tt>,
     * or -1 if there is no such index.
     *
     * @param e element to search for
     * @param index index to start searching from
     * @return the index of the first occurrence of the element in
     *         this list at position {@code index} or later in the list;
     *         {@code -1} if the element is not found.
     * @throws IndexOutOfBoundsException if the specified index is negative
     */ // 返回当前实例中给定元素的下标, index是搜索起始下标
    public int indexOf(E e, int index) {
        Object[] elements = getArray();
        return indexOf(e, elements, index, elements.length);
    }

    /**
     * {@inheritDoc}
     */ // 返回当前实例中, 从后到前第一个遇到与给定元素相等的元素下标
    public int lastIndexOf(Object o) {
        Object[] elements = getArray();
        return lastIndexOf(o, elements, elements.length - 1);
    }

    /**
     * Returns the index of the last occurrence of the specified element in
     * this list, searching backwards from {@code index}, or returns -1 if
     * the element is not found.
     * More formally, returns the highest index {@code i} such that
     * <tt>(i&nbsp;&lt;=&nbsp;index&nbsp;&amp;&amp;&nbsp;(e==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;e.equals(get(i))))</tt>,
     * or -1 if there is no such index.
     *
     * @param e element to search for
     * @param index index to start searching backwards from
     * @return the index of the last occurrence of the element at position
     *         less than or equal to {@code index} in this list;
     *         -1 if the element is not found.
     * @throws IndexOutOfBoundsException if the specified index is greater
     *         than or equal to the current size of this list
     */ // 返回当前实例中, 从后到前第一个遇到与给定元素相等的元素下标, index是从指定值为末尾开始的下标
    public int lastIndexOf(E e, int index) {
        Object[] elements = getArray();
        return lastIndexOf(e, elements, index);
    }

    /**
     * Returns a shallow copy of this list.  (The elements themselves
     * are not copied.)
     *
     * @return a clone of this list
     */ // 浅克隆
    public Object clone() {
        try {
            @SuppressWarnings("unchecked")
            CopyOnWriteArrayList<E> clone =
                (CopyOnWriteArrayList<E>) super.clone();
            clone.resetLock();
            return clone;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
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
     * @return an array containing all the elements in this list
     */ // 返回当前实例包含的数组的一个副本
    public Object[] toArray() {
        Object[] elements = getArray(); // 获得原始数组
        return Arrays.copyOf(elements, elements.length); // 复制副本并且返回
    }

    /**
     * Returns an array containing all of the elements in this list in
     * proper sequence (from first to last element); the runtime type of
     * the returned array is that of the specified array.  If the list fits
     * in the specified array, it is returned therein.  Otherwise, a new
     * array is allocated with the runtime type of the specified array and
     * the size of this list.
     *
     * <p>If this list fits in the specified array with room to spare
     * (i.e., the array has more elements than this list), the element in
     * the array immediately following the end of the list is set to
     * {@code null}.  (This is useful in determining the length of this
     * list <i>only</i> if the caller knows that this list does not contain
     * any null elements.)
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
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     *
     * @param a the array into which the elements of the list are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return an array containing all the elements in this list
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this list
     * @throws NullPointerException if the specified array is null
     */ /** 见{@link List#toArray(Object[])} */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T a[]) {
        Object[] elements = getArray(); // 获得原始数据数组
        int len = elements.length;
        if (a.length < len) // 判断给定数组是否能容纳原始数组
            return (T[]) Arrays.copyOf(elements, len, a.getClass()); // 不能容纳直接将原始数组复制新副本返回
        else {
            System.arraycopy(elements, 0, a, 0, len); // 能容纳将原始数组数据复制到给定数组, 空余位置赋值为null
            if (a.length > len)
                a[len] = null;
            return a;
        }
    }

    // Positional Access Operations 随机位置操作

    @SuppressWarnings("unchecked")
    private E get(Object[] a, int index) { // 工具方法, 返回给定数组给定下标元素
        return (E) a[index];
    }

    /**
     * {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */ /** 见{@link List#get(int)} */
    public E get(int index) {
        return get(getArray(), index);
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element.
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */ /** 见{@link List#set(int, Object)} */
    public E set(int index, E element) {
        final ReentrantLock lock = this.lock;
        lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
        try {
            Object[] elements = getArray(); // 获得原始数据数组
            E oldValue = get(elements, index); // 获得要设置的下标对应的原始值

            if (oldValue != element) { // 旧值与要设置的值不是同一个内存对象
                int len = elements.length;
                Object[] newElements = Arrays.copyOf(elements, len); // 将原始数组赋值一个副本出来, 即CopyOnWrite
                newElements[index] = element; // 改变副本指定下标的元素
                setArray(newElements); // 将副本设置成实例的元素数组
            } else {
                // Not quite a no-op; ensures volatile write semantics
                /**
                 * 同时执行的线程, 共同操作一个volatile变量, 那么读操作一定能读到写操作之后最新值, 这就是happens-before
                 *
                 * 假设
                 * a初始值为0
                 * thread1
                 * ①int a = 1;
                 * ②CopyOnWriteArrayList.setArray();
                 * thread2
                 * ③CopyOnWriteArrayList.getArray();
                 * ④int b = a;
                 * 其中同一线程保证了①②, ③④的顺序, volatile保证了②③, 所以整体是①②③④
                 * 如果没有步骤②, 那么没有②③, 所以④有可能读到①之前的值
                 * */
                setArray(elements); // 不是简单的无操作, 即使元素没变化也要设置一下元素数组, 为了保证外部非volatile修饰的变量happens before
            }
            return oldValue; // 返回原始值
        } finally {
            lock.unlock(); // 释放可重入锁
        }
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param e element to be appended to this list
     * @return {@code true} (as specified by {@link Collection#add})
     */ /** 见{@link List#add(Object)} */
    public boolean add(E e) {
        final ReentrantLock lock = this.lock;
        lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
        try {
            Object[] elements = getArray(); // 获得原始数组
            int len = elements.length;
            Object[] newElements = Arrays.copyOf(elements, len + 1); // 将原始数组赋值一个副本出来, 即CopyOnWrite
            newElements[len] = e; // 改变副本数组
            setArray(newElements); // 将副本数组设置成实例的元素数组
            return true;
        } finally {
            lock.unlock(); // 释放可重入锁
        }
    }

    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */ /** 见{@link List#add(int, Object)} */
    public void add(int index, E element) {
        final ReentrantLock lock = this.lock;
        lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
        try {
            Object[] elements = getArray(); // 获取原始元素数组
            int len = elements.length;
            if (index > len || index < 0) // 检测要添加元素的下标是否越界
                throw new IndexOutOfBoundsException("Index: "+index+
                                                    ", Size: "+len);
            Object[] newElements;
            int numMoved = len - index; // 需要后移的元素数量
            if (numMoved == 0) // 如果没有需要移动的元素, 那么就是在末尾添加
                newElements = Arrays.copyOf(elements, len + 1); // 将原始元素复制副本, 副本末尾比原始数组长度多1
            else {
                newElements = new Object[len + 1]; // 创建空的新数组
                System.arraycopy(elements, 0, newElements, 0, index); // 将index前的元素复制到新数组头部
                System.arraycopy(elements, index, newElements, index + 1,
                                 numMoved); // 将要移动的元素复制到新数组index后部
            }
            newElements[index] = element; // 在新数组index处设置给定元素
            setArray(newElements); // 将新数组设置为实例元素数组, 完成新增元素
        } finally {
            lock.unlock(); // 释放可重入锁
        }
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).  Returns the element that was removed from the list.
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */ /** 见{@link List#remove(int)} */
    public E remove(int index) {
        final ReentrantLock lock = this.lock;
        lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
        try {
            Object[] elements = getArray(); // 获得原始元素数组
            int len = elements.length;
            E oldValue = get(elements, index); // 获取旧值
            int numMoved = len - index - 1;
            if (numMoved == 0) // 如果需要移动的元素为0个, 即要移除的元素在末尾
                setArray(Arrays.copyOf(elements, len - 1)); // 直接复制原始数组长度-1的快照数组, 并将快照数组设置为实例元素数组即可
            else {
                Object[] newElements = new Object[len - 1]; // 否则新建空白数组
                System.arraycopy(elements, 0, newElements, 0, index); // 将要移除下标前方元素复制到新数组前方
                System.arraycopy(elements, index + 1, newElements, index,
                                 numMoved); // 将要移除下标后方元素接着复制到新数组
                setArray(newElements); // 将新数组设置成实例元素数组
            }
            return oldValue; // 返回旧值
        } finally {
            lock.unlock(); // 释放可重入锁
        }
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
     */ /** 见{@link List#remove(Object)} */
    public boolean remove(Object o) {
        Object[] snapshot = getArray(); // 获得原始元素数组
        int index = indexOf(o, snapshot, 0, snapshot.length); // 定位要删除的元素在数组中的位置
        return (index < 0) ? false : remove(o, snapshot, index); // 删除指定元素并返回操作结果
    }

    /**
     * A version of remove(Object) using the strong hint that given
     * recent snapshot contains o at the given index.
     */ // 工具方法, 删除给定元素
    private boolean remove(Object o, Object[] snapshot, int index) {
        final ReentrantLock lock = this.lock;
        lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
        try {
            Object[] current = getArray(); // 获得当前的原始元素数组
            int len = current.length;
            if (snapshot != current) findIndex: { // 如果调用者传递的数组快照已经与当前原始数组不相等, 需要重新获得要删除元素下标
                int prefix = Math.min(index, len); // 如果当前元素数组长度比index小, 那么直接取当前长度全量遍历. 如果index比当前长度小, 那么先遍历当前数组index前面的部分
                for (int i = 0; i < prefix; i++) {
                    if (current[i] != snapshot[i] && eq(o, current[i])) { // 如果对应下标快照数组的元素与当前数组的元素不相等, 那么再接着判断是否相等
                        index = i; // 相等的话更新下标
                        break findIndex; // 并且跳出循环
                    }
                }
                if (index >= len) // 如果下标经过上面处理还是大于当前长度, 证明没有找到要删除的元素, 即元素已经被其他线程删除
                    return false; // 返回false
                if (current[index] == o) // 否则判断是否当前数组关于要删除的元素部分是否没有改变, 确定还是相等, 要找的下标没变化后
                    break findIndex; // 跳出循环
                index = indexOf(o, current, index, len); // 否则接着遍历当前数组在index之后包括index的元素, 寻找相同元素的下标
                if (index < 0) // 如果返回-1, 代表没找到
                    return false; // 返回false
            }
            Object[] newElements = new Object[len - 1]; // 否则新建当前元素数组长度-1的新数组
            System.arraycopy(current, 0, newElements, 0, index); // 将index前方元素复制到新数组
            System.arraycopy(current, index + 1,
                             newElements, index,
                             len - index - 1); // 将index以及后方元素复制到新数组
            setArray(newElements); // 将新数组设置成当前实例元素数组
            return true; // 返回删除成功
        } finally {
            lock.unlock(); // 释放可重入锁
        }
    }

    /**
     * Removes from this list all of the elements whose index is between
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     * Shifts any succeeding elements to the left (reduces their index).
     * This call shortens the list by {@code (toIndex - fromIndex)} elements.
     * (If {@code toIndex==fromIndex}, this operation has no effect.)
     *
     * @param fromIndex index of first element to be removed
     * @param toIndex index after last element to be removed
     * @throws IndexOutOfBoundsException if fromIndex or toIndex out of range
     *         ({@code fromIndex < 0 || toIndex > size() || toIndex < fromIndex})
     */ // 删除当前实例[fromIndex, toIndex)的元素
    void removeRange(int fromIndex, int toIndex) {
        final ReentrantLock lock = this.lock;
        lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
        try {
            Object[] elements = getArray(); // 获得当前元素数组
            int len = elements.length;

            if (fromIndex < 0 || toIndex > len || toIndex < fromIndex) // 检测参数下标是否越界
                throw new IndexOutOfBoundsException();
            int newlen = len - (toIndex - fromIndex); // 计算删除指定范围内元素后元素数量
            int numMoved = len - toIndex; // 计算要删除范围元素后方需要往前移动的元素数量
            if (numMoved == 0) // 如果后方没有需要移动的元素
                setArray(Arrays.copyOf(elements, newlen)); // 直接将保留的元素复制到新数组并设置成当前实例元素数组
            else {
                Object[] newElements = new Object[newlen]; // 否则用删除后元素数量新建数组
                System.arraycopy(elements, 0, newElements, 0, fromIndex); // 将fromIndex前方元素复制到新数组
                System.arraycopy(elements, toIndex, newElements,
                                 fromIndex, numMoved); // 将要往前移动的元素复制到新数组
                setArray(newElements); // 将新数组设置为当前实例的数组
            }
        } finally {
            lock.unlock(); // 释放可重入锁
        }
    }

    /**
     * Appends the element, if not present.
     *
     * @param e element to be added to this list, if absent
     * @return {@code true} if the element was added
     */ // 如果当前实例不包含给定元素则将给定元素添加到当前实例
    public boolean addIfAbsent(E e) {
        Object[] snapshot = getArray(); // 获得当前实例元素数组快照
        return indexOf(e, snapshot, 0, snapshot.length) >= 0 ? false :
            addIfAbsent(e, snapshot); // 如果元素数组快照中不存在当前元素, 将其放入当前实例
    }

    /**
     * A version of addIfAbsent using the strong hint that given
     * recent snapshot does not contain e.
     */ // 如果当前实例不包含给定元素则将给定元素添加到当前实例
    private boolean addIfAbsent(E e, Object[] snapshot) {
        final ReentrantLock lock = this.lock;
        lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
        try {
            Object[] current = getArray(); // 获得当前原始元素数组
            int len = current.length;
            if (snapshot != current) { // 如果当前原始元素数组与快照有差异
                // Optimize for lost race to another addXXX operation
                int common = Math.min(snapshot.length, len); // 先遍历快照长度与当前数组长度中比较小的部分元素
                for (int i = 0; i < common; i++)
                    if (current[i] != snapshot[i] && eq(e, current[i])) // 如果前面遍历部分有相同元素
                        return false; // 那么不进行添加, 返回false
                if (indexOf(e, current, common, len) >= 0) // 否则继续遍历后面部分元素, 如果找到相同的元素
                        return false; // 返回false不进行添加
            }
            Object[] newElements = Arrays.copyOf(current, len + 1); // 否则将原始元素复制到新的数组
            newElements[len] = e; // 在数组尾部添加给定元素
            setArray(newElements); // 将新数组设置成当前实例元素数组
            return true; // 返回true
        } finally {
            lock.unlock(); // 释放可重入锁
        }
    }

    /**
     * Returns {@code true} if this list contains all of the elements of the
     * specified collection.
     *
     * @param c collection to be checked for containment in this list
     * @return {@code true} if this list contains all of the elements of the
     *         specified collection
     * @throws NullPointerException if the specified collection is null
     * @see #contains(Object)
     */ /** 见{@link List#containsAll(Collection)} */
    public boolean containsAll(Collection<?> c) {
        Object[] elements = getArray(); // 获得当前实例包含的元素数组
        int len = elements.length;
        for (Object e : c) { // 依次判断给定集合包含的元素是否存在于元素数组中
            if (indexOf(e, elements, 0, len) < 0) // 一但有一个元素不存在, 就返回false
                return false;
        }
        return true; // 如果都存在就返回true
    }

    /**
     * Removes from this list all of its elements that are contained in
     * the specified collection. This is a particularly expensive operation
     * in this class because of the need for an internal temporary array.
     *
     * @param c collection containing elements to be removed from this list
     * @return {@code true} if this list changed as a result of the call
     * @throws ClassCastException if the class of an element of this list
     *         is incompatible with the specified collection
     *         (<a href="../Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this list contains a null element and the
     *         specified collection does not permit null elements
     *         (<a href="../Collection.html#optional-restrictions">optional</a>),
     *         or if the specified collection is null
     * @see #remove(Object)
     */ /** 见{@link List#removeAll(Collection)} */
    public boolean removeAll(Collection<?> c) {
        if (c == null) throw new NullPointerException();
        final ReentrantLock lock = this.lock;
        lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
        try {
            Object[] elements = getArray(); // 获得当前实例包含的元素数组
            int len = elements.length;
            if (len != 0) {
                // temp array holds those elements we know we want to keep
                int newlen = 0; // 记录移除给定集合元素后还剩下多少个元素
                Object[] temp = new Object[len];
                for (int i = 0; i < len; ++i) { // 遍历当前实例包含的元素
                    Object element = elements[i];
                    if (!c.contains(element)) // 如果元素不存在于要删除的集合中, 就是要保留的元素
                        temp[newlen++] = element; // 将要保留的元素放入临时数组中
                }
                if (newlen != len) {
                    setArray(Arrays.copyOf(temp, newlen)); // 将临时数组有效元素复制到新数组, 并设置成当前实例元素数组
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock(); // 释放可重入锁
        }
    }

    /**
     * Retains only the elements in this list that are contained in the
     * specified collection.  In other words, removes from this list all of
     * its elements that are not contained in the specified collection.
     *
     * @param c collection containing elements to be retained in this list
     * @return {@code true} if this list changed as a result of the call
     * @throws ClassCastException if the class of an element of this list
     *         is incompatible with the specified collection
     *         (<a href="../Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this list contains a null element and the
     *         specified collection does not permit null elements
     *         (<a href="../Collection.html#optional-restrictions">optional</a>),
     *         or if the specified collection is null
     * @see #remove(Object)
     */ /** 见{@link List#retainAll(Collection)} */
    public boolean retainAll(Collection<?> c) {
        if (c == null) throw new NullPointerException();
        final ReentrantLock lock = this.lock;
        lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
        try {
            Object[] elements = getArray(); // 获得当前实例元素数组
            int len = elements.length;
            if (len != 0) {
                // temp array holds those elements we know we want to keep
                int newlen = 0; // 记录要保留的元素个数
                Object[] temp = new Object[len]; // 临时数组
                for (int i = 0; i < len; ++i) { // 遍历元素数组元素
                    Object element = elements[i];
                    if (c.contains(element)) // 如果要保留的元素集合包含当前元素
                        temp[newlen++] = element; // 那么更新要保留元素计数, 将当前元素放入临时数组
                }
                if (newlen != len) {
                    setArray(Arrays.copyOf(temp, newlen)); // 复制临时数组有效元素到副本数组, 并将其设置成当前实例元素数组
                    return true; // 返回true
                }
            }
            return false; // 否则数据没有变化返回false
        } finally {
            lock.unlock(); // 释放可重入锁
        }
    }

    /**
     * Appends all of the elements in the specified collection that
     * are not already contained in this list, to the end of
     * this list, in the order that they are returned by the
     * specified collection's iterator.
     *
     * @param c collection containing elements to be added to this list
     * @return the number of elements added
     * @throws NullPointerException if the specified collection is null
     * @see #addIfAbsent(Object)
     */ // 将给定集合中尚未存在在此实例中的元素添加到当前元素
    public int addAllAbsent(Collection<? extends E> c) {
        Object[] cs = c.toArray(); // 获得给定集合包含元素的数组
        if (cs.length == 0)
            return 0;
        final ReentrantLock lock = this.lock;
        lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
        try {
            Object[] elements = getArray(); // 获得当前实例的元素数组
            int len = elements.length;
            int added = 0; // 新增元素计数, 要作为结果返回给调用方
            // uniquify and compact elements in cs
            for (int i = 0; i < cs.length; ++i) { // 遍历给定集合元素
                Object e = cs[i];
                if (indexOf(e, elements, 0, len) < 0 && // 如果给定集合当前元素在当前实例中不存在
                    indexOf(e, cs, 0, added) < 0) // 并且当前给定集合元素不存在于给定集合已处理过的元素中(就是没添加过嘛)
                    cs[added++] = e; // 将给定集合当前元素添加到给定集合元素数组前方(因为前方元素处理过了, 复用了集合元素数组暂存要新增的元素)
            }
            if (added > 0) { // 如果存在新增元素
                Object[] newElements = Arrays.copyOf(elements, len + added); // 将当前实例的元素复制到新数组前端
                System.arraycopy(cs, 0, newElements, len, added); // 将要新增的元素接着复制到新数组
                setArray(newElements); // 将新数组设置为当前实例的元素数组
            }
            return added; // 返回新增元素计数
        } finally {
            lock.unlock(); // 释放可重入锁
        }
    }

    /**
     * Removes all of the elements from this list.
     * The list will be empty after this call returns.
     */ /** 见{@link List#clear()} */
    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
        try {
            setArray(new Object[0]); // 将当前实例元素数组设置为空数组
        } finally {
            lock.unlock(); // 释放可重入锁
        }
    }

    /**
     * Appends all of the elements in the specified collection to the end
     * of this list, in the order that they are returned by the specified
     * collection's iterator.
     *
     * @param c collection containing elements to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     * @see #add(Object)
     */ /** 见{@link List#addAll(Collection)} */
    public boolean addAll(Collection<? extends E> c) {
        Object[] cs = (c.getClass() == CopyOnWriteArrayList.class) ?
            ((CopyOnWriteArrayList<?>)c).getArray() : c.toArray(); // 获得给定集合元素组成的数组
        if (cs.length == 0)
            return false;
        final ReentrantLock lock = this.lock;
        lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
        try {
            Object[] elements = getArray(); // 获得当前实例的元素数组
            int len = elements.length;
            if (len == 0 && cs.getClass() == Object[].class) // 如果当前实例不包含任何元素, 给定集合获得的数组又是Object[]
                setArray(cs); // 那么直接将给定集合元素的数组设置成当前实例的元素数组
            else {
                Object[] newElements = Arrays.copyOf(elements, len + cs.length); // 否则将当前实例已有元素复制到新数组
                System.arraycopy(cs, 0, newElements, len, cs.length); // 将给定集合元素数组复制到新数组
                setArray(newElements); // 将新数组设置为当前实例元素数组
            }
            return true; // 返回true
        } finally {
            lock.unlock(); // 释放可重入锁
        }
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list, starting at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements to
     * the right (increases their indices).  The new elements will appear
     * in this list in the order that they are returned by the
     * specified collection's iterator.
     *
     * @param index index at which to insert the first element
     *        from the specified collection
     * @param c collection containing elements to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException if the specified collection is null
     * @see #add(int,Object)
     */ /** 见{@link List#addAll(int, Collection)} */
    public boolean addAll(int index, Collection<? extends E> c) {
        Object[] cs = c.toArray(); // 获得给定集合的元素数组
        final ReentrantLock lock = this.lock;
        lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
        try {
            Object[] elements = getArray(); // 获得当前实例的元素数组
            int len = elements.length;
            if (index > len || index < 0) // 判断给定下标是否越界
                throw new IndexOutOfBoundsException("Index: "+index+
                                                    ", Size: "+len);
            if (cs.length == 0)
                return false;
            int numMoved = len - index; // 判断当前实例中需要往后移动的元素
            Object[] newElements;
            if (numMoved == 0) // 如果不需要移动元素, 证明是在当前实例尾部添加元素
                newElements = Arrays.copyOf(elements, len + cs.length); // 直接将当前实例元素复制到新数组备用
            else {
                newElements = new Object[len + cs.length]; // 否则新建空白数组
                System.arraycopy(elements, 0, newElements, 0, index); // 将给定index之前的元素复制到新数组
                System.arraycopy(elements, index,
                                 newElements, index + cs.length,
                                 numMoved); // 将index以及其后的元素复制到新数组尾部
            }
            System.arraycopy(cs, 0, newElements, index, cs.length); // 将给定集合的元素数组包含的元素复制到新数组中间
            setArray(newElements); // 将新数组设置成当前实例元素数组
            return true; // 返回true
        } finally {
            lock.unlock(); // 释放可重入锁
        }
    }
    /** 见{@link List#forEach(Consumer)} */
    public void forEach(Consumer<? super E> action) {
        if (action == null) throw new NullPointerException();
        Object[] elements = getArray(); // 获得当前实例元素数组
        int len = elements.length;
        for (int i = 0; i < len; ++i) { // 遍历每一个元素
            @SuppressWarnings("unchecked") E e = (E) elements[i];
            action.accept(e); // 对每一个元素执行给定动作
        }
    }
    /** 见{@link List#removeIf(Predicate)} */
    public boolean removeIf(Predicate<? super E> filter) {
        if (filter == null) throw new NullPointerException();
        final ReentrantLock lock = this.lock;
        lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
        try {
            Object[] elements = getArray(); // 获得当前实例元素数组
            int len = elements.length;
            if (len != 0) {
                int newlen = 0;
                Object[] temp = new Object[len]; // 新建临时数组
                for (int i = 0; i < len; ++i) { // 遍历每一个元素
                    @SuppressWarnings("unchecked") E e = (E) elements[i];
                    if (!filter.test(e)) // 测试当前元素是否符合给定断言
                        temp[newlen++] = e; // 不符合的元素保留到新数组
                }
                if (newlen != len) {
                    setArray(Arrays.copyOf(temp, newlen)); // 将新数组有效元素复制到另一个新数组, 并且将其设置为当前实例元素数组
                    return true; // 返回true
                }
            }
            return false;
        } finally {
            lock.unlock(); // 释放可重入锁
        }
    }
    /** 见{@link List#replaceAll(UnaryOperator)} */
    public void replaceAll(UnaryOperator<E> operator) {
        if (operator == null) throw new NullPointerException();
        final ReentrantLock lock = this.lock;
        lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
        try {
            Object[] elements = getArray(); // 获得当前实例元素数组
            int len = elements.length;
            Object[] newElements = Arrays.copyOf(elements, len); // 将元素复制到新数组, 防止操作时影响原始元素数组
            for (int i = 0; i < len; ++i) {
                @SuppressWarnings("unchecked") E e = (E) elements[i];
                newElements[i] = operator.apply(e); // 对每一个元素执行一元操作符, 并用产生的结果替换数组元素
            }
            setArray(newElements); // 将新数组设置为当前实例元素数组
        } finally {
            lock.unlock(); // 释放可重入锁
        }
    }
    /** 见{@link List#sort(Comparator)} */
    public void sort(Comparator<? super E> c) {
        final ReentrantLock lock = this.lock;
        lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
        try {
            Object[] elements = getArray(); // 获得当前实例元素数组
            Object[] newElements = Arrays.copyOf(elements, elements.length); // 将元素复制到新数组(CopyOnWrite)
            @SuppressWarnings("unchecked") E[] es = (E[])newElements;
            Arrays.sort(es, c); // 使用给定的比较器对新数组进行排序
            setArray(newElements); // 将排序后的新数组设置为当前实例元素数组
        } finally {
            lock.unlock(); // 释放可重入锁
        }
    }

    /**
     * Saves this list to a stream (that is, serializes it).
     *
     * @param s the stream
     * @throws java.io.IOException if an I/O error occurs
     * @serialData The length of the array backing the list is emitted
     *               (int), followed by all of its elements (each an Object)
     *               in the proper order.
     */ // 将当前实例序列化到对象输出流
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {

        s.defaultWriteObject();

        Object[] elements = getArray();
        // Write out array length
        s.writeInt(elements.length);

        // Write out all elements in the proper order.
        for (Object element : elements)
            s.writeObject(element);
    }

    /**
     * Reconstitutes this list from a stream (that is, deserializes it).
     * @param s the stream
     * @throws ClassNotFoundException if the class of a serialized object
     *         could not be found
     * @throws java.io.IOException if an I/O error occurs
     */ // 从对象输入流反序列化当前类的实例
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {

        s.defaultReadObject();

        // bind to new lock
        resetLock();

        // Read in array length and allocate array
        int len = s.readInt();
        SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, len);
        Object[] elements = new Object[len];

        // Read in all elements in the proper order.
        for (int i = 0; i < len; i++)
            elements[i] = s.readObject();
        setArray(elements);
    }

    /**
     * Returns a string representation of this list.  The string
     * representation consists of the string representations of the list's
     * elements in the order they are returned by its iterator, enclosed in
     * square brackets ({@code "[]"}).  Adjacent elements are separated by
     * the characters {@code ", "} (comma and space).  Elements are
     * converted to strings as by {@link String#valueOf(Object)}.
     *
     * @return a string representation of this list
     */ // 将当前实例包含的元素数组转换成字符串
    public String toString() {
        return Arrays.toString(getArray());
    }

    /**
     * Compares the specified object with this list for equality.
     * Returns {@code true} if the specified object is the same object
     * as this object, or if it is also a {@link List} and the sequence
     * of elements returned by an {@linkplain List#iterator() iterator}
     * over the specified list is the same as the sequence returned by
     * an iterator over this list.  The two sequences are considered to
     * be the same if they have the same length and corresponding
     * elements at the same position in the sequence are <em>equal</em>.
     * Two elements {@code e1} and {@code e2} are considered
     * <em>equal</em> if {@code (e1==null ? e2==null : e1.equals(e2))}.
     *
     * @param o the object to be compared for equality with this list
     * @return {@code true} if the specified object is equal to this list
     */ /** 见{@link Object#equals(Object)} */
    public boolean equals(Object o) {
        if (o == this) // 如果给定对象与当前实例内存地址相等
            return true; // 直接返回相等
        if (!(o instanceof List)) // 如果给定对象都不是个List类型
            return false; // 直接返回非false
        // 给定对象是List类型, 那么继续比较两者包含的元素在对应位置上是否相等
        List<?> list = (List<?>)(o);
        Iterator<?> it = list.iterator();
        Object[] elements = getArray(); // 获得当前实例的元素数组
        int len = elements.length;
        for (int i = 0; i < len; ++i) // 遍历当前实例包含的元素
            if (!it.hasNext() || !eq(elements[i], it.next())) // 如果相应位置有一个对象不存在元素, 或者存在的元素不相等
                return false; // 返回false
        if (it.hasNext()) // 如果比较完当前实例元素每个元素都相应的与给定List相等后, 给定的List还有元素
            return false; // 那么直接返回false
        return true; // 否则视为当前实例与给定List相等
    }

    /**
     * Returns the hash code value for this list.
     *
     * <p>This implementation uses the definition in {@link List#hashCode}.
     *
     * @return the hash code value for this list
     */ /** 见{@link Object#hashCode()} */
    public int hashCode() {
        int hashCode = 1;
        Object[] elements = getArray(); // 获得当前实例元素数组
        int len = elements.length;
        for (int i = 0; i < len; ++i) { // 遍历元素
            Object obj = elements[i];
            hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode()); // 计算当前实例的hashCode与每个包含元素相关
        }
        return hashCode; // 返回当前实例hashCode
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * <p>The returned iterator provides a snapshot of the state of the list
     * when the iterator was constructed. No synchronization is needed while
     * traversing the iterator. The iterator does <em>NOT</em> support the
     * {@code remove} method.
     *
     * @return an iterator over the elements in this list in proper sequence
     */ /** 见{@link Collection#iterator()} */
    public Iterator<E> iterator() {
        return new COWIterator<E>(getArray(), 0);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The returned iterator provides a snapshot of the state of the list
     * when the iterator was constructed. No synchronization is needed while
     * traversing the iterator. The iterator does <em>NOT</em> support the
     * {@code remove}, {@code set} or {@code add} methods.
     */ /** 见{@link List#listIterator()} */
    public ListIterator<E> listIterator() {
        return new COWIterator<E>(getArray(), 0);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The returned iterator provides a snapshot of the state of the list
     * when the iterator was constructed. No synchronization is needed while
     * traversing the iterator. The iterator does <em>NOT</em> support the
     * {@code remove}, {@code set} or {@code add} methods.
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */ /** 见{@link List#listIterator(int)} */
    public ListIterator<E> listIterator(int index) {
        Object[] elements = getArray(); // 获得当前实例元素数组
        int len = elements.length;
        if (index < 0 || index > len) // 判断给定迭代器初始下标是否越界
            throw new IndexOutOfBoundsException("Index: "+index);

        return new COWIterator<E>(elements, index);
    }

    /**
     * Returns a {@link Spliterator} over the elements in this list.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#IMMUTABLE},
     * {@link Spliterator#ORDERED}, {@link Spliterator#SIZED}, and
     * {@link Spliterator#SUBSIZED}.
     *
     * <p>The spliterator provides a snapshot of the state of the list
     * when the spliterator was constructed. No synchronization is needed while
     * operating on the spliterator.
     *
     * @return a {@code Spliterator} over the elements in this list
     * @since 1.8
     */ /** 见{@link Collection#spliterator()} */
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator // 使用工具类返回通用的基于数组的可分割迭代器, 当前类并没有自己的特殊实现版本
            (getArray(), Spliterator.IMMUTABLE | Spliterator.ORDERED); // 返回的可分割迭代器具有有序和不可变特征
    }
    /** ListIterator的CopyOnWriteArrayList版本 */
    static final class COWIterator<E> implements ListIterator<E> {
        /** Snapshot of the array */
        private final Object[] snapshot; // 迭代器创建时刻当前CopyOnWriteArrayList实例的元素数组快照
        /** Index of element to be returned by subsequent call to next.  */
        private int cursor; // 下次调用next方法要返回的元素对应的下标
        // 指定元素数组与初始迭代器下标的迭代器
        private COWIterator(Object[] elements, int initialCursor) {
            cursor = initialCursor;
            snapshot = elements;
        }
        /** 见{@link ListIterator#hasNext()} */
        public boolean hasNext() {
            return cursor < snapshot.length; // 判断游标后是否还有等待迭代的元素
        }
        /** 见{@link ListIterator#hasPrevious()} */
        public boolean hasPrevious() {
            return cursor > 0; // 判断游标前方是否有元素
        }
        /** 见{@link ListIterator#next()} */
        @SuppressWarnings("unchecked")
        public E next() {
            if (! hasNext()) // 如果没有下一个元素
                throw new NoSuchElementException(); // 抛出异常
            return (E) snapshot[cursor++]; // 否则返回当前游标下标元素, 并将游标值增加
        }
        /** 见{@link ListIterator#previous()} */
        @SuppressWarnings("unchecked")
        public E previous() {
            if (! hasPrevious()) // 判断如果没有前一个元素
                throw new NoSuchElementException(); // 抛出异常
            return (E) snapshot[--cursor]; // 否则将游标值减一, 然后返回新游标值对应元素
        }
        /** 见{@link ListIterator#nextIndex()} */
        public int nextIndex() {
            return cursor; // 返回游标值
        }
        /** 见{@link ListIterator#previousIndex()} */
        public int previousIndex() {
            return cursor-1; // 返回游标值减一
        }

        /**
         * Not supported. Always throws UnsupportedOperationException.
         * @throws UnsupportedOperationException always; {@code remove}
         *         is not supported by this iterator.
         */ /** 见{@link ListIterator#remove()} */
        public void remove() {
            throw new UnsupportedOperationException(); // COWIterator只支持读操作, 修改操作抛出异常
        }

        /**
         * Not supported. Always throws UnsupportedOperationException.
         * @throws UnsupportedOperationException always; {@code set}
         *         is not supported by this iterator.
         */ /** 见{@link ListIterator#set(Object)} */
        public void set(E e) {
            throw new UnsupportedOperationException(); // COWIterator只支持读操作, 修改操作抛出异常
        }

        /**
         * Not supported. Always throws UnsupportedOperationException.
         * @throws UnsupportedOperationException always; {@code add}
         *         is not supported by this iterator.
         */ /** 见{@link ListIterator#add(Object)} */
        public void add(E e) {
            throw new UnsupportedOperationException(); // COWIterator只支持读操作, 修改操作抛出异常
        }
        /** 见{@link ListIterator#forEachRemaining(Consumer)} */
        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            Object[] elements = snapshot; // 获得当前迭代器包含的快照元素数组
            final int size = elements.length;
            for (int i = cursor; i < size; i++) { // 遍历游标值以及其后的元素
                @SuppressWarnings("unchecked") E e = (E) elements[i];
                action.accept(e); // 对每个元素执行给定动作
            }
            cursor = size; // 将游标值设置为元素数组长度
        }
    }

    /**
     * Returns a view of the portion of this list between
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     * The returned list is backed by this list, so changes in the
     * returned list are reflected in this list.
     *
     * <p>The semantics of the list returned by this method become
     * undefined if the backing list (i.e., this list) is modified in
     * any way other than via the returned list.
     *
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex high endpoint (exclusive) of the subList
     * @return a view of the specified range within this list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */ /** 见{@link List#subList(int, int)} */
    public List<E> subList(int fromIndex, int toIndex) {
        final ReentrantLock lock = this.lock;
        lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
        try {
            Object[] elements = getArray(); // 获得当前CopyOnWriteArrayList实例的元素数组
            int len = elements.length;
            if (fromIndex < 0 || toIndex > len || fromIndex > toIndex) // 检测给定下标参数是否越界
                throw new IndexOutOfBoundsException();
            return new COWSubList<E>(this, fromIndex, toIndex); // 返回COWSublist实例
        } finally {
            lock.unlock(); // 释放可重入锁
        }
    }

    /**
     * Sublist for CopyOnWriteArrayList.
     * This class extends AbstractList merely for convenience, to
     * avoid having to define addAll, etc. This doesn't hurt, but
     * is wasteful.  This class does not need or use modCount
     * mechanics in AbstractList, but does need to check for
     * concurrent modification using similar mechanics.  On each
     * operation, the array that we expect the backing list to use
     * is checked and updated.  Since we do this for all of the
     * base operations invoked by those defined in AbstractList,
     * all is well.  While inefficient, this is not worth
     * improving.  The kinds of list operations inherited from
     * AbstractList are already so slow on COW sublists that
     * adding a bit more space/time doesn't seem even noticeable.
     */ /** 当前CopyOnWriteArrayList的子序列定义 */
    private static class COWSubList<E>
        extends AbstractList<E>
        implements RandomAccess
    {
        private final CopyOnWriteArrayList<E> l; // 子序列基于的CopyOnWriteArrayList实例
        private final int offset; // 偏移值
        private int size; // 尺寸
        private Object[] expectedArray; // 预期的数组

        // only call this holding l's lock 子序列构造方法, 只有持有CopyOnWriteArrayList实例的可重入锁时才可以调用此方法
        COWSubList(CopyOnWriteArrayList<E> list,
                   int fromIndex, int toIndex) {
            l = list; // 持有CopyOnWriteArrayList实例
            expectedArray = l.getArray(); // 实例包含的元素数组
            offset = fromIndex; // 偏移量等于fromIndex
            size = toIndex - fromIndex; // 子序列长度为toIndex - fromIndex
        }

        // only call this holding l's lock 检测持有的实例是否被修改, 只有持有CopyOnWriteArrayList实例的可重入锁时才可以调用此方法
        private void checkForComodification() {
            if (l.getArray() != expectedArray) // 如果持有实例的元素数组已经与构造子序列期间不一致
                throw new ConcurrentModificationException(); // 那么抛出并发修改异常
        }

        // only call this holding l's lock 检测给定下标是否越界, 只有持有CopyOnWriteArrayList实例的可重入锁时才可以调用此方法
        private void rangeCheck(int index) {
            if (index < 0 || index >= size)
                throw new IndexOutOfBoundsException("Index: "+index+
                                                    ",Size: "+size);
        }
        /** 见{@link List#set(int, Object)}, 此方法会影响其持有的CopyOnWriteArrayList实例数据 */
        public E set(int index, E element) {
            final ReentrantLock lock = l.lock;
            lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
            try {
                rangeCheck(index); // 检测给定下标是否合法
                checkForComodification(); // 检测子序列构造之后是否被其他线程修改持有实例数据
                E x = l.set(index+offset, element); // 调用持有实例的set方法, 改变给定index的元素值
                expectedArray = l.getArray(); // 更新子序列持有的元素数组
                return x; // 返回旧值
            } finally {
                lock.unlock(); // 释放可重入锁
            }
        }
        /** 见{@link List#get(int)} */
        public E get(int index) {
            final ReentrantLock lock = l.lock;
            lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
            try {
                rangeCheck(index); // 检测给定下标是否合法
                checkForComodification(); // 检测子序列构造之后是否被其他线程修改持有实例数据
                return l.get(index+offset); // 调用持有实例的get方法获得元素并返回
            } finally {
                lock.unlock(); // 释放可重入锁
            }
        }
        /** 见{@link List#size()} */
        public int size() {
            final ReentrantLock lock = l.lock;
            lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
            try {
                checkForComodification(); // 检测子序列构造之后是否被其他线程修改持有实例数据
                return size; // 返回维护的子序列长度
            } finally {
                lock.unlock(); // 释放可重入锁
            }
        }
        /** 见{@link List#add(int, Object)} */
        public void add(int index, E element) {
            final ReentrantLock lock = l.lock;
            lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
            try {
                checkForComodification(); // 检测子序列构造之后是否被其他线程修改持有实例数据
                if (index < 0 || index > size) // 检测下标是否越界
                    throw new IndexOutOfBoundsException();
                l.add(index+offset, element); // 调用持有实例的add方法
                expectedArray = l.getArray(); // 更新当前子序列持有的元素数组
                size++; // 更新维护的子序列长度
            } finally {
                lock.unlock(); // 释放可重入锁
            }
        }
        /** 见{@link List#clear()} */
        public void clear() {
            final ReentrantLock lock = l.lock;
            lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
            try {
                checkForComodification(); // 检测子序列构造之后是否被其他线程修改持有实例数据
                l.removeRange(offset, offset+size); // 调用持有实例removeRange方法, 移除子序列范围内的元素
                expectedArray = l.getArray(); // 更新子序列持有元素数组
                size = 0; // 更新维护的子序列长度
            } finally {
                lock.unlock(); // 释放可重入锁
            }
        }
        /** 见{@link List#remove(int)} */
        public E remove(int index) {
            final ReentrantLock lock = l.lock;
            lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
            try {
                rangeCheck(index); // 检测给定下标是否越界
                checkForComodification(); // 检测子序列构造之后是否被其他线程修改持有实例数据
                E result = l.remove(index+offset); // 调用持有实例的remove方法
                expectedArray = l.getArray(); // 更新子序列持有的元素数组
                size--; // 更新维护的子序列长度
                return result; // 返回移除的元素
            } finally {
                lock.unlock(); // 释放可重入锁
            }
        }
        /** 见{@link List#remove(Object)} */
        public boolean remove(Object o) {
            int index = indexOf(o); // 确定给定对象在当前子序列中的下标, 内部通过子序列的ListIterator实现
            if (index == -1) // 如果返回-1, 没有找到给定元素
                return false; // 返回false
            remove(index); // 找到了就调用当前子序列的remove方法
            return true; // 返回true
        }
        /** 见{@link List#iterator()} */
        public Iterator<E> iterator() {
            final ReentrantLock lock = l.lock;
            lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
            try {
                checkForComodification(); // 检测子序列构造之后是否被其他线程修改持有实例数据
                return new COWSubListIterator<E>(l, 0, offset, size); // 返回子序列迭代器
            } finally {
                lock.unlock(); // 释放可重入锁
            }
        }
        /** 见{@link List#listIterator(int)} */
        public ListIterator<E> listIterator(int index) {
            final ReentrantLock lock = l.lock;
            lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
            try {
                checkForComodification(); // 检测子序列构造之后是否被其他线程修改持有实例数据
                if (index < 0 || index > size) // 检测给定迭代器初始位置是否越界
                    throw new IndexOutOfBoundsException("Index: "+index+
                                                        ", Size: "+size);
                return new COWSubListIterator<E>(l, index, offset, size); // 返回子序列序列迭代器
            } finally {
                lock.unlock(); // 释放可重入锁
            }
        }
        /** 见{@link List#subList(int, int)} */
        public List<E> subList(int fromIndex, int toIndex) {
            final ReentrantLock lock = l.lock;
            lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
            try {
                checkForComodification(); // 检测子序列构造之后是否被其他线程修改持有实例数据
                if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) // 检测fromIndex和toIndex不能超过当前子序列限制
                    throw new IndexOutOfBoundsException();
                return new COWSubList<E>(l, fromIndex + offset,
                                         toIndex + offset); // 构造另一个子序列返回
            } finally {
                lock.unlock(); // 释放可重入锁
            }
        }
        /** 见{@link List#forEach(Consumer)} */
        public void forEach(Consumer<? super E> action) {
            if (action == null) throw new NullPointerException();
            int lo = offset; // 暂存子序列开始下标
            int hi = offset + size; // 暂存子序列结束下标
            Object[] a = expectedArray; // 获得预期元素数组
            if (l.getArray() != a) // 检测是否被并发修改
                throw new ConcurrentModificationException();
            if (lo < 0 || hi > a.length) // 检测开始下标与结束下标在合法范围内
                throw new IndexOutOfBoundsException();
            for (int i = lo; i < hi; ++i) { // 遍历子序列每个元素
                @SuppressWarnings("unchecked") E e = (E) a[i];
                action.accept(e); // 对每个元素执行给定动作
            }
        }
        /** 见{@link List#replaceAll(UnaryOperator)} */
        public void replaceAll(UnaryOperator<E> operator) {
            if (operator == null) throw new NullPointerException();
            final ReentrantLock lock = l.lock;
            lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
            try {
                int lo = offset; // 暂存子序列开始下标
                int hi = offset + size; // 暂存子序列结束下标
                Object[] elements = expectedArray; // 获得预期元素数组
                if (l.getArray() != elements) // 检测是否被并发修改
                    throw new ConcurrentModificationException();
                int len = elements.length;
                if (lo < 0 || hi > len) // 检测开始下标与结束下标在合法范围内
                    throw new IndexOutOfBoundsException();
                Object[] newElements = Arrays.copyOf(elements, len); // 将持有CopyOnWriteArrayList实例元素数组元素复制到新数组
                for (int i = lo; i < hi; ++i) { // 遍历在子序列范围内的元素
                    @SuppressWarnings("unchecked") E e = (E) elements[i];
                    newElements[i] = operator.apply(e); // 对范围内元素执行给定一元操作符, 并用一元操作符产生的结果替换此元素
                }
                l.setArray(expectedArray = newElements); // 将处理后的数组设置为CopyOnWriteArrayList实例的元素数组
            } finally {
                lock.unlock(); // 释放可重入锁
            }
        }
        /** 见{@link List#sort(Comparator)} */
        public void sort(Comparator<? super E> c) {
            final ReentrantLock lock = l.lock;
            lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
            try {
                int lo = offset; // 暂存子序列开始下标
                int hi = offset + size; // 暂存子序列结束下标
                Object[] elements = expectedArray; // 获得预期元素数组
                if (l.getArray() != elements) // 检测是否被并发修改
                    throw new ConcurrentModificationException();
                int len = elements.length;
                if (lo < 0 || hi > len) // 检测开始下标与结束下标在合法范围内
                    throw new IndexOutOfBoundsException();
                Object[] newElements = Arrays.copyOf(elements, len); // 将持有CopyOnWriteArrayList实例元素数组元素复制到新数组
                @SuppressWarnings("unchecked") E[] es = (E[])newElements;
                Arrays.sort(es, lo, hi, c); // 对数组的子序列范围内元素进行排序
                l.setArray(expectedArray = newElements); // 将处理后的数组设置为CopyOnWriteArrayList实例的元素数组
            } finally {
                lock.unlock(); // 释放可重入锁
            }
        }
        /** 见{@link List#removeAll(Collection)} */
        public boolean removeAll(Collection<?> c) {
            if (c == null) throw new NullPointerException();
            boolean removed = false;
            final ReentrantLock lock = l.lock;
            lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
            try {
                int n = size; // 暂存子序列长度
                if (n > 0) {
                    int lo = offset; // 暂存子序列开始下标
                    int hi = offset + n; // 暂存子序列结束下标
                    Object[] elements = expectedArray; // 获得预期元素数组
                    if (l.getArray() != elements) // 检测是否被并发修改
                        throw new ConcurrentModificationException();
                    int len = elements.length;
                    if (lo < 0 || hi > len) // 检测开始下标与结束下标在合法范围内
                        throw new IndexOutOfBoundsException();
                    int newSize = 0;
                    Object[] temp = new Object[n]; // 创建长度等于子序列长度的空白数组
                    for (int i = lo; i < hi; ++i) { // 遍历子序列元素
                        Object element = elements[i];
                        if (!c.contains(element)) // 如果当前元素不包含在要删除的集合中
                            temp[newSize++] = element; // newSize计数增加, 并且将其放在新数组中
                    }
                    if (newSize != n) {
                        Object[] newElements = new Object[len - n + newSize]; // 新建要保留子序列元素以及原始实例其他元素长度的空白数组
                        System.arraycopy(elements, 0, newElements, 0, lo); // 将原始元素数组, 子序列前面部分元素复制到新数组
                        System.arraycopy(temp, 0, newElements, lo, newSize); // 将子序列要保留的元素复制到新数组
                        System.arraycopy(elements, hi, newElements,
                                         lo + newSize, len - hi); // 将原始元素数组, 子序列后面的元素复制到新数组
                        size = newSize; // 更新子序列长度
                        removed = true; // 标记有数据改动
                        l.setArray(expectedArray = newElements); // 将处理后的数组设置为CopyOnWriteArrayList实例的元素数组
                    }
                }
            } finally {
                lock.unlock(); // 释放可重入锁
            }
            return removed; // 返回处理结果
        }
        /** 见{@link List#retainAll(Collection)} */
        public boolean retainAll(Collection<?> c) {
            if (c == null) throw new NullPointerException();
            boolean removed = false;
            final ReentrantLock lock = l.lock;
            lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
            try {
                int n = size; // 暂存子序列长度
                if (n > 0) {
                    int lo = offset; // 暂存子序列开始下标
                    int hi = offset + n; // 暂存子序列结束下标
                    Object[] elements = expectedArray; // 获得预期元素数组
                    if (l.getArray() != elements) // 检测是否被并发修改
                        throw new ConcurrentModificationException();
                    int len = elements.length;
                    if (lo < 0 || hi > len) // 检测开始下标与结束下标在合法范围内
                        throw new IndexOutOfBoundsException();
                    int newSize = 0;
                    Object[] temp = new Object[n]; // 创建长度等于子序列长度的空白数组
                    for (int i = lo; i < hi; ++i) { // 遍历子序列元素
                        Object element = elements[i];
                        if (c.contains(element)) // 判断当前元素是否在需要保留的集合中
                            temp[newSize++] = element; // 如果是, 将当前元素放在新数组中, 更新保留元素计数
                    }
                    if (newSize != n) {
                        Object[] newElements = new Object[len - n + newSize]; // 新建要保留子序列元素以及原始实例其他元素长度的空白数组
                        System.arraycopy(elements, 0, newElements, 0, lo); // 将原始元素数组, 子序列前面部分元素复制到新数组
                        System.arraycopy(temp, 0, newElements, lo, newSize); // 将子序列要保留的元素复制到新数组
                        System.arraycopy(elements, hi, newElements,
                                         lo + newSize, len - hi); // 将原始元素数组, 子序列后面的元素复制到新数组
                        size = newSize; // 更新子序列长度
                        removed = true; // 标记有数据改动
                        l.setArray(expectedArray = newElements); // 将处理后的数组设置为CopyOnWriteArrayList实例的元素数组
                    }
                }
            } finally {
                lock.unlock(); // 释放可重入锁
            }
            return removed; // 返回处理结果
        }
        /** 见{@link List#removeIf(Predicate)} */
        public boolean removeIf(Predicate<? super E> filter) {
            if (filter == null) throw new NullPointerException();
            boolean removed = false;
            final ReentrantLock lock = l.lock;
            lock.lock(); // 获得可重入锁, 保证当前实例同一时间只有一个修改数据操作
            try {
                int n = size; // 暂存子序列长度
                if (n > 0) {
                    int lo = offset; // 暂存子序列开始下标
                    int hi = offset + n; // 暂存子序列结束下标
                    Object[] elements = expectedArray; // 获得预期元素数组
                    if (l.getArray() != elements) // 检测是否被并发修改
                        throw new ConcurrentModificationException();
                    int len = elements.length;
                    if (lo < 0 || hi > len) // 检测开始下标与结束下标在合法范围内
                        throw new IndexOutOfBoundsException();
                    int newSize = 0;
                    Object[] temp = new Object[n]; // 创建长度等于子序列长度的空白数组
                    for (int i = lo; i < hi; ++i) { // 遍历子序列元素
                        @SuppressWarnings("unchecked") E e = (E) elements[i];
                        if (!filter.test(e)) // 判断当前元素是否符合给定断言
                            temp[newSize++] = e; // 如果不符合, 将当前元素保留到新数组
                    }
                    if (newSize != n) {
                        Object[] newElements = new Object[len - n + newSize]; // 新建要保留子序列元素以及原始实例其他元素长度的空白数组
                        System.arraycopy(elements, 0, newElements, 0, lo); // 将原始元素数组, 子序列前面部分元素复制到新数组
                        System.arraycopy(temp, 0, newElements, lo, newSize); // 将子序列要保留的元素复制到新数组
                        System.arraycopy(elements, hi, newElements,
                                         lo + newSize, len - hi); // 将原始元素数组, 子序列后面的元素复制到新数组
                        size = newSize; // 更新子序列长度
                        removed = true; // 标记有数据改动
                        l.setArray(expectedArray = newElements); // 将处理后的数组设置为CopyOnWriteArrayList实例的元素数组
                    }
                }
            } finally {
                lock.unlock(); // 释放可重入锁
            }
            return removed;
        }
        /** 见{@link List#spliterator()} */
        public Spliterator<E> spliterator() {
            int lo = offset; // 暂存子序列开始下标
            int hi = offset + size; // 暂存子序列结束下标
            Object[] a = expectedArray; // 获得预期元素数组
            if (l.getArray() != a) // 检测是否被并发修改
                throw new ConcurrentModificationException();
            if (lo < 0 || hi > a.length) // 检测开始下标与结束下标在合法范围内
                throw new IndexOutOfBoundsException();
            return Spliterators.spliterator
                (a, lo, hi, Spliterator.IMMUTABLE | Spliterator.ORDERED); // 通过工具类返回通用的基于数组的可分割迭代器
        }

    }
    // ListIterator的子序列版本
    private static class COWSubListIterator<E> implements ListIterator<E> {
        private final ListIterator<E> it; // 当前CopyOnWriteArrayList实例的ListIterator
        private final int offset; // 偏移值
        private final int size; // 子序列尺寸
        // 子序列序列迭代器构造方法
        COWSubListIterator(List<E> l, int index, int offset, int size) {
            this.offset = offset; // 初始化偏移值
            this.size = size; // 初始化尺寸
            it = l.listIterator(index+offset); // 获得CopyOnWriteArrayList的ListIterator
        }
        /** 见{@link ListIterator#hasNext()} */
        public boolean hasNext() {
            return nextIndex() < size;
        }
        /** 见{@link ListIterator#next()} */
        public E next() {
            if (hasNext())
                return it.next();
            else
                throw new NoSuchElementException();
        }
        /** 见{@link ListIterator#hasPrevious()} */
        public boolean hasPrevious() {
            return previousIndex() >= 0;
        }
        /** 见{@link ListIterator#previous()} */
        public E previous() {
            if (hasPrevious())
                return it.previous();
            else
                throw new NoSuchElementException();
        }
        /** 见{@link ListIterator#next()} */
        public int nextIndex() {
            return it.nextIndex() - offset;
        }
        /** 见{@link ListIterator#previousIndex()} */
        public int previousIndex() {
            return it.previousIndex() - offset;
        }
        /** 见{@link ListIterator#remove()} */
        public void remove() {
            throw new UnsupportedOperationException();
        }
        /** 见{@link ListIterator#set(Object)} */
        public void set(E e) {
            throw new UnsupportedOperationException();
        }
        /** 见{@link ListIterator#add(Object)} */
        public void add(E e) {
            throw new UnsupportedOperationException();
        }
        /** 见{@link ListIterator#forEachRemaining(Consumer)} */
        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            int s = size;
            ListIterator<E> i = it;
            while (nextIndex() < s) {
                action.accept(i.next());
            }
        }
    }

    // Support for resetting lock while deserializing 内部方法, 反序列化时重置可重入锁的状态
    private void resetLock() {
        UNSAFE.putObjectVolatile(this, lockOffset, new ReentrantLock());
    }
    private static final sun.misc.Unsafe UNSAFE;
    private static final long lockOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = CopyOnWriteArrayList.class;
            lockOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("lock"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
