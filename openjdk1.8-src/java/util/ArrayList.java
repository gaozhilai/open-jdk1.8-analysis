/*
 * Copyright (c) 1997, 2017, Oracle and/or its affiliates. All rights reserved.
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
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import sun.misc.SharedSecrets;

/**
 * Resizable-array implementation of the <tt>List</tt> interface.  Implements
 * all optional list operations, and permits all elements, including
 * <tt>null</tt>.  In addition to implementing the <tt>List</tt> interface,
 * this class provides methods to manipulate the size of the array that is
 * used internally to store the list.  (This class is roughly equivalent to
 * <tt>Vector</tt>, except that it is unsynchronized.)
 *
 * <p>The <tt>size</tt>, <tt>isEmpty</tt>, <tt>get</tt>, <tt>set</tt>,
 * <tt>iterator</tt>, and <tt>listIterator</tt> operations run in constant
 * time.  The <tt>add</tt> operation runs in <i>amortized constant time</i>,
 * that is, adding n elements requires O(n) time.  All of the other operations
 * run in linear time (roughly speaking).  The constant factor is low compared
 * to that for the <tt>LinkedList</tt> implementation.
 *
 * <p>Each <tt>ArrayList</tt> instance has a <i>capacity</i>.  The capacity is
 * the size of the array used to store the elements in the list.  It is always
 * at least as large as the list size.  As elements are added to an ArrayList,
 * its capacity grows automatically.  The details of the growth policy are not
 * specified beyond the fact that adding an element has constant amortized
 * time cost.
 *
 * <p>An application can increase the capacity of an <tt>ArrayList</tt> instance
 * before adding a large number of elements using the <tt>ensureCapacity</tt>
 * operation.  This may reduce the amount of incremental reallocation.
 *
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * If multiple threads access an <tt>ArrayList</tt> instance concurrently,
 * and at least one of the threads modifies the list structurally, it
 * <i>must</i> be synchronized externally.  (A structural modification is
 * any operation that adds or deletes one or more elements, or explicitly
 * resizes the backing array; merely setting the value of an element is not
 * a structural modification.)  This is typically accomplished by
 * synchronizing on some object that naturally encapsulates the list.
 *
 * If no such object exists, the list should be "wrapped" using the
 * {@link Collections#synchronizedList Collections.synchronizedList}
 * method.  This is best done at creation time, to prevent accidental
 * unsynchronized access to the list:<pre>
 *   List list = Collections.synchronizedList(new ArrayList(...));</pre>
 *
 * <p><a name="fail-fast">
 * The iterators returned by this class's {@link #iterator() iterator} and
 * {@link #listIterator(int) listIterator} methods are <em>fail-fast</em>:</a>
 * if the list is structurally modified at any time after the iterator is
 * created, in any way except through the iterator's own
 * {@link ListIterator#remove() remove} or
 * {@link ListIterator#add(Object) add} methods, the iterator will throw a
 * {@link ConcurrentModificationException}.  Thus, in the face of
 * concurrent modification, the iterator fails quickly and cleanly, rather
 * than risking arbitrary, non-deterministic behavior at an undetermined
 * time in the future.
 *
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw {@code ConcurrentModificationException} on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness:  <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i>
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see     Collection
 * @see     List
 * @see     LinkedList
 * @see     Vector
 * @since   1.2
 */
 // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/4/28 16:40
public class ArrayList<E> extends AbstractList<E> // ArrayList继承了List的骨架类AbstractList
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable // ArrayList实现的接口证明了其是一个List, 支持高效的随机访问, 支持克隆(本类是浅客隆), 支持序列化
{
    private static final long serialVersionUID = 8683452581122892189L;

    /**
     * Default initial capacity.
     */ // 默认的初始容量
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * Shared empty array instance used for empty instances.
     */ // 所有构造时指定容量的ArrayList的空实例共享的空缓存数组
    private static final Object[] EMPTY_ELEMENTDATA = {};

    /**
     * Shared empty array instance used for default sized empty instances. We
     * distinguish this from EMPTY_ELEMENTDATA to know how much to inflate when
     * first element is added.
     */ /** 所有构造时没有指定容量的ArrayList空实例共享的空缓存数组. 与{@link #EMPTY_ELEMENTDATA}区分是为了优化第一次新增元素扩容逻辑, 见{@link #calculateCapacity(Object[], int)} */
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    /**
     * The array buffer into which the elements of the ArrayList are stored.
     * The capacity of the ArrayList is the length of this array buffer. Any
     * empty ArrayList with elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA
     * will be expanded to DEFAULT_CAPACITY when the first element is added.
     */ // 数组缓存, 当前实例元素实际存储在此缓存中. 如果此字段初始值为DEFAULTCAPACITY_EMPTY_ELEMENTDATA, 当第一个元素添加时此缓存容量会被扩充到DEFAULT_CAPACITY
    transient Object[] elementData; // non-private to simplify nested class access 容量指的就是此缓存数组的长度

    /**
     * The size of the ArrayList (the number of elements it contains).
     *
     * @serial
     */ // 当前实例包含的元素数量
    private int size;

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param  initialCapacity  the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity
     *         is negative
     */ // 用指定的容量值构造一个实例
    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) { // 指定的容量大于0时
            this.elementData = new Object[initialCapacity]; // 直接初始化数组缓存到指定容量
        } else if (initialCapacity == 0) { // 如果指定的容量是0, 那么初始化数组缓存为EMPTY_ELEMENTDATA, 此时无扩容优化逻辑
            this.elementData = EMPTY_ELEMENTDATA; // 前几个新增的元素会扩容几次, 而不是像DEFAULTCAPACITY_EMPTY_ELEMENTDATA默认扩容到默认容量
        } else {
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        }
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
     */ /** 无参构造方法, 用默认容量构造一个实例, 第一次添加元素时数组缓存会扩大到{@link #DEFAULT_CAPACITY}默认容量 */
    public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */ // 用给定集合的元素构造一个实例
    public ArrayList(Collection<? extends E> c) {
        elementData = c.toArray(); // 将给定集合的元素数组作为当前数组缓存
        if ((size = elementData.length) != 0) {
            // c.toArray可能返回的不是Object[] (see 6260652), Arrays.asList构造的List返回的是Arrays中的ArrayList, 其中数组缓存用的是E[], 所以返回的是具体类型数组
            if (elementData.getClass() != Object[].class) // 如果返回的数组不是Object[]
                elementData = Arrays.copyOf(elementData, size, Object[].class); // 将集合中元素复制到新的Object[]中, 并将其作为缓存, 否则具体类型数组作为缓存再存其他类型会报错
        } else {
            // replace with empty array.
            this.elementData = EMPTY_ELEMENTDATA; // 如果给定结合是空的, 将当前数组缓存设置为空缓存
        }
    }

    /**
     * Trims the capacity of this <tt>ArrayList</tt> instance to be the
     * list's current size.  An application can use this operation to minimize
     * the storage of an <tt>ArrayList</tt> instance.
     */ // 将当前实例数组缓存的容量调整成跟当前实例元素数量一致, 最小化当前实例的内存空间占用
    public void trimToSize() {
        modCount++;
        if (size < elementData.length) { // 当前元素数量小于数组缓存容量, 需要调整
            elementData = (size == 0)
              ? EMPTY_ELEMENTDATA // 元素数量为0时将数组缓存设置为空缓存
              : Arrays.copyOf(elementData, size); // 否则将当前实例元素复制到一个新的尺寸刚好等于元素个数的数组, 并设置成当前数组缓存
        }
    }

    /**
     * Increases the capacity of this <tt>ArrayList</tt> instance, if
     * necessary, to ensure that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param   minCapacity   the desired minimum capacity
     */ // 确保当前容量能装的下参数指定的容量个元素, 如果不够的话会将数组缓存扩容
    public void ensureCapacity(int minCapacity) {
        int minExpand = (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA) // 判断当前ArrayList实例最小扩容尺寸是0还是DEFAULT_CAPACITY
            // any size if not default element table
            ? 0
            // larger than default for default empty table. It's already
            // supposed to be at default size.
            : DEFAULT_CAPACITY;

        if (minCapacity > minExpand) { // 如果当前需要保证的最小容量比最小扩容尺寸小, 就不需要扩容, 第一次add方法会自动扩容, 否则执行下方方法
            ensureExplicitCapacity(minCapacity);
        }
    }
    /** 根据参数minCapacity计算当前需要的容量是多少 */
    private static int calculateCapacity(Object[] elementData, int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) { // 如果当前数组缓存对象就是DEFAULTCAPACITY_EMPTY_ELEMENTDATA, 那么minCapacity和默认容量中较大的一个
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        return minCapacity; // 如果当前缓存数组不是DEFAULTCAPACITY_EMPTY_ELEMENTDATA, 那么直接返回minCapacity
    }
    // ArrayList内部操作确保数组缓存容量能装的下要新增的元素
    private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }
    // 数组缓存实际的扩容方法
    private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // overflow-conscious code
        if (minCapacity - elementData.length > 0) // 当需要的最小容量已经大于当前缓存容量时
            grow(minCapacity); // 执行数组缓存扩容
    }

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */ /** 定义了数组最大长度, 其实数组最大长度是{@link Integer.MAX_VALUE}, 不过一些虚拟机实现可能需要保留几个位置写入数组对象头信息 */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8; // Hotspot虚拟机实现中, 32位机器header words是4byte, 64位机器是8byte, 考虑到最小类型的是byte[]数组, 所以要-8, 留8个数组位置给header words

    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */ // 扩容数组缓存方法, 确保数组缓存容量至少能提供参数指定的容量
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length; // 获得扩容前容量
        int newCapacity = oldCapacity + (oldCapacity >> 1); // 预计每次扩容扩容前容量的一半
        if (newCapacity - minCapacity < 0) // 如果预计容量还是不满足指定的最小容量
            newCapacity = minCapacity; // 本次扩容容量设置为参数值
        if (newCapacity - MAX_ARRAY_SIZE > 0) // 如果预期容量比当前最大数组容量还大
            newCapacity = hugeCapacity(minCapacity); // 使用指定参数容量为依据, 获得最终容量
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity); // 将旧数组元素复制到新数组, 新数组作为当前数组缓存, 扩容完成
    }
    /** 判断当前要求的最小容量是否大于{@link #MAX_ARRAY_SIZE} */
    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ? // 如果需求最小容量大于MAX_ARRAY_SIZE
            Integer.MAX_VALUE : // 将整形最大值作为容量, 再大就超过数组最大长度限制了
            MAX_ARRAY_SIZE; // 否则使用MAX_ARRAY_SIZE, 保证不同虚拟机实现的兼容性
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */ // 获得当前实例包含的元素数
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this list contains no elements.
     *
     * @return <tt>true</tt> if this list contains no elements
     */ // 返回当前实例是否不包含任何元素
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this list is to be tested
     * @return <tt>true</tt> if this list contains the specified element
     */ // 判断当前实例是否包含至少一个给定元素
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     */ // 通过遍历实际存储元素的数组缓存, 从前往后寻找, 返回第一次遇到的指定元素的下标
    public int indexOf(Object o) {
        if (o == null) { // 寻找null的下标
            for (int i = 0; i < size; i++)
                if (elementData[i]==null)
                    return i;
        } else { // 寻找正常对象元素的下标
            for (int i = 0; i < size; i++)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the highest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     */ // 通过遍历实际存储元素的数组缓存, 从后往前寻找, 返回第一次遇到的指定元素的下标
    public int lastIndexOf(Object o) {
        if (o == null) { // 寻找null的下标
            for (int i = size-1; i >= 0; i--)
                if (elementData[i]==null)
                    return i;
        } else { // 寻找正常对象元素的下标
            for (int i = size-1; i >= 0; i--)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    /**
     * Returns a shallow copy of this <tt>ArrayList</tt> instance.  (The
     * elements themselves are not copied.)
     *
     * @return a clone of this <tt>ArrayList</tt> instance
     */ // 克隆当前实例, 浅克隆, 只是复制了两份数组缓存出来, 具体数组缓存中的元素没有进行克隆
    public Object clone() {
        try {
            ArrayList<?> v = (ArrayList<?>) super.clone(); // 创建新的实例
            v.elementData = Arrays.copyOf(elementData, size); // 将当前实例的数组缓存复制到新的实例
            v.modCount = 0;
            return v; // 将产生的新实例返回
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
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
     * @return an array containing all of the elements in this list in
     *         proper sequence
     */ // 返回当前实例所有元素组成的数组. 即将当前实例数组缓存中元素复制到新的数组并返回
    public Object[] toArray() {
        return Arrays.copyOf(elementData, size);
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence (from first to last element); the runtime type of the returned
     * array is that of the specified array.  If the list fits in the
     * specified array, it is returned therein.  Otherwise, a new array is
     * allocated with the runtime type of the specified array and the size of
     * this list.
     *
     * <p>If the list fits in the specified array with room to spare
     * (i.e., the array has more elements than the list), the element in
     * the array immediately following the end of the collection is set to
     * <tt>null</tt>.  (This is useful in determining the length of the
     * list <i>only</i> if the caller knows that the list does not contain
     * any null elements.)
     *
     * @param a the array into which the elements of the list are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this list
     * @throws NullPointerException if the specified array is null
     */ // 将当前集合所有元素放入给定数组, 如果给定数组装不下所有元素, 那么返回一个新的长度等于元素数量的装有所有元素的数组
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size) // 如果给定参数数组长度不够装实例里的元素
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(elementData, size, a.getClass()); // 只取参数数组的类型信息, 返回一个新的数组
        System.arraycopy(elementData, 0, a, 0, size); // 给定的参数数组能装下实例包含的元素, 直接将元素复制到给定数组
        if (a.length > size) // 如果给定数组大于元素数量
            a[size] = null; // 在复制完实例元素后, 紧跟其后一个位置设置为null, 当知道实例中没有null元素时可以根据此判断实例元素数量
        return a;
    }

    // Positional Access Operations 根据下标随机访问元素
    // 返回数组缓存中给定下标的元素, 此方法不会校验元素的有效下标
    @SuppressWarnings("unchecked")
    E elementData(int index) {
        return (E) elementData[index];
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */ // 返回给定下标对应的元素, 此方法会校验下标是否在有效元素范围内, 下标超出了有效范围会抛出越界异常
    public E get(int index) {
        rangeCheck(index); // 检测下标是否在有效范围

        return elementData(index); // 返回数组缓存中给定下标对应的元素
    }

    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */ // 替换指定下标的元素为给定的新元素, 并返回旧元素
    public E set(int index, E element) {
        rangeCheck(index); // 检测给定下标是否有效

        E oldValue = elementData(index); // 暂存旧元素值
        elementData[index] = element; // 将给定位置设置为新元素
        return oldValue; // 返回旧元素
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param e element to be appended to this list
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */ // 在当前实例尾部添加一个指定的新元素, 并返回true
    public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // Increments modCount!! 增加操作计数, 并确保当前数组缓存容量能存储新增元素
        elementData[size++] = e; // 在数组缓存有效元素尾部新增指定元素
        return true; // 返回true
    }

    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */ // 在当前ArrayList实例给定下标插入给定元素, 将原来此位置元素及其后面的元素整体右移
    public void add(int index, E element) {
        rangeCheckForAdd(index); // 检测下标是否在有效范围

        ensureCapacityInternal(size + 1);  // Increments modCount!! 增加操作计数并保证数组缓存容量可以存储当前新增元素
        System.arraycopy(elementData, index, elementData, index + 1,
                         size - index); // 将给定下标原始元素以及其后元素整体右移
        elementData[index] = element; // 将给定下标设置为给定元素
        size++; // 当前ArrayList实例包含的元素计数自增一
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */ // 移除给定下标位置的元素, 并将后续元素整体前移一个位置
    public E remove(int index) {
        rangeCheck(index); // 检测下标是否在有效范围

        modCount++; // 增加操作计数
        E oldValue = elementData(index); // 暂存给定下标要删除的元素

        int numMoved = size - index - 1; // 移除指定下标元素后, 后方有几个元素要往前移
        if (numMoved > 0) // 的确有元素需要前移(移除的元素不在尾部)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved); // 通过复制实现后方元素整体前移一位
        elementData[--size] = null; // clear to let GC do its work 由于是复制实现后续元素前移, 所以原数组缓存最后一个元素还是有值的, 将其置为null, 让垃圾回收器进行回收

        return oldValue; // 返回被移除的元素
    }

    /**
     * Removes the first occurrence of the specified element from this list,
     * if it is present.  If the list does not contain the element, it is
     * unchanged.  More formally, removes the element with the lowest index
     * <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
     * (if such an element exists).  Returns <tt>true</tt> if this list
     * contained the specified element (or equivalently, if this list
     * changed as a result of the call).
     *
     * @param o element to be removed from this list, if present
     * @return <tt>true</tt> if this list contained the specified element
     */ // 移除下标较小的第一次遍历到的与给定参数相等的元素, 并将其后续元素整体前移一位
    public boolean remove(Object o) {
        if (o == null) { // 要移除的是null元素
            for (int index = 0; index < size; index++)
                if (elementData[index] == null) {
                    fastRemove(index); // 移除当前ArrayList实例中从前往后遍历第一个与给定元素相等的元素
                    return true;
                }
        } else { // 要移除的元素非null
            for (int index = 0; index < size; index++)
                if (o.equals(elementData[index])) {
                    fastRemove(index); // 移除当前ArrayList实例中从前往后遍历第一个与给定元素相等的元素
                    return true;
                }
        }
        return false; // 要移除的元素不存在返回false
    }

    /*
     * Private remove method that skips bounds checking and does not
     * return the value removed.
     */ /** 私有根据下标快速移除元素方法, 与{@link #remove(int)}相比, 不校验下标有效范围, 不返回要移除的元素 */
    private void fastRemove(int index) {
        modCount++; // 增加操作计数
        int numMoved = size - index - 1; // 计算要往前移动一个位置的元素数量
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved); // 将需要前移的元素前移
        elementData[--size] = null; // clear to let GC do its work 清空移除前数组缓存中最后一个元素, 触发垃圾回收
    }

    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns.
     */ // 清空当前实例包含的所有元素
    public void clear() {
        modCount++; // 增加操作计数

        // clear to let GC do its work
        for (int i = 0; i < size; i++) // 清空缓存数组中元素
            elementData[i] = null;

        size = 0; // 将包含元素个数设置为0
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the
     * specified collection's Iterator.  The behavior of this operation is
     * undefined if the specified collection is modified while the operation
     * is in progress.  (This implies that the behavior of this call is
     * undefined if the specified collection is this list, and this
     * list is nonempty.)
     *
     * @param c collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */ // 将给定集合包含的元素, 按照其迭代器返回元素顺序, 添加到当前ArrayList实例尾部
    public boolean addAll(Collection<? extends E> c) {
        Object[] a = c.toArray(); // 获得包含给定集合所有元素的数组
        int numNew = a.length;
        ensureCapacityInternal(size + numNew);  // Increments modCount 增加操作计数并保证数组缓存容量可以存储当前新增元素
        System.arraycopy(a, 0, elementData, size, numNew); // 将要添加的元素数组复制到数组缓存中
        size += numNew;
        return numNew != 0; // 返回本次是否有元素被添加
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list, starting at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements to
     * the right (increases their indices).  The new elements will appear
     * in the list in the order that they are returned by the
     * specified collection's iterator.
     *
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param c collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException if the specified collection is null
     */ // 向指定下标插入集合中包含的元素, 并将原来下标位置和其后的元素右移
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index); // 检测下标是否在有效范围

        Object[] a = c.toArray(); // 获取包含集合所有元素的数组
        int numNew = a.length;
        ensureCapacityInternal(size + numNew);  // Increments modCount 增加操作计数并保证数组缓存容量可以存储当前新增元素

        int numMoved = size - index; // 计算出要移动的元素个数
        if (numMoved > 0)
            System.arraycopy(elementData, index, elementData, index + numNew,
                             numMoved); // 将原来下标位置和其后的元素向右移numNew个位置

        System.arraycopy(a, 0, elementData, index, numNew); // 将要新增的元素放入数组缓存
        size += numNew; // 增加持有元素计数
        return numNew != 0; // 返回是否有元素新增
    }

    /**
     * Removes from this list all of the elements whose index is between
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     * Shifts any succeeding elements to the left (reduces their index).
     * This call shortens the list by {@code (toIndex - fromIndex)} elements.
     * (If {@code toIndex==fromIndex}, this operation has no effect.)
     *
     * @throws IndexOutOfBoundsException if {@code fromIndex} or
     *         {@code toIndex} is out of range
     *         ({@code fromIndex < 0 ||
     *          fromIndex >= size() ||
     *          toIndex > size() ||
     *          toIndex < fromIndex})
     */ // 移除当前ArrayList下标范围[fromIndex, toIndex)的元素
    protected void removeRange(int fromIndex, int toIndex) {
        modCount++; // 增加操作计数
        int numMoved = size - toIndex; // 计算出移除元素后尾部需要前移的元素数量
        System.arraycopy(elementData, toIndex, elementData, fromIndex,
                         numMoved); // 尾部元素前移

        // clear to let GC do its work
        int newSize = size - (toIndex-fromIndex); // 计算出新的持有元素数量
        for (int i = newSize; i < size; i++) {
            elementData[i] = null; // 将数组缓存中无效元素置为null, 触发垃圾回收
        }
        size = newSize; // 将持有元素数量设置为真实数量
    }

    /**
     * Checks if the given index is in range.  If not, throws an appropriate
     * runtime exception.  This method does *not* check if the index is
     * negative: It is always used immediately prior to an array access,
     * which throws an ArrayIndexOutOfBoundsException if index is negative.
     */ // 检测给定下标是否在有效元素下标范围内, 如果超出了范围抛出越界异常, 不校验负值, 因为此时index是立刻用于访问数组, 负值访问数组会报错
    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * A version of rangeCheck used by add and addAll.
     */ // 检测给定下标是否在有效元素下标范围内, 如果超出了范围抛出越界异常, 校验负值, 如果负值快速失败, 防止在扩容后, toArray后访问数组缓存付出代价后才失败
    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * Constructs an IndexOutOfBoundsException detail message.
     * Of the many possible refactorings of the error handling code,
     * this "outlining" performs best with both server and client VMs.
     */ // 构造越界异常信息
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    /**
     * Removes from this list all of its elements that are contained in the
     * specified collection.
     *
     * @param c collection containing elements to be removed from this list
     * @return {@code true} if this list changed as a result of the call
     * @throws ClassCastException if the class of an element of this list
     *         is incompatible with the specified collection
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this list contains a null element and the
     *         specified collection does not permit null elements
     * (<a href="Collection.html#optional-restrictions">optional</a>),
     *         or if the specified collection is null
     * @see Collection#contains(Object)
     */ // 移除当前实例包含的所有元素
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, false); // 移除当前实例中与给定集合包含的相同的元素
    }

    /**
     * Retains only the elements in this list that are contained in the
     * specified collection.  In other words, removes from this list all
     * of its elements that are not contained in the specified collection.
     *
     * @param c collection containing elements to be retained in this list
     * @return {@code true} if this list changed as a result of the call
     * @throws ClassCastException if the class of an element of this list
     *         is incompatible with the specified collection
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this list contains a null element and the
     *         specified collection does not permit null elements
     * (<a href="Collection.html#optional-restrictions">optional</a>),
     *         or if the specified collection is null
     * @see Collection#contains(Object)
     */ // 移除当前实例中所有给定集合不包含的元素
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, true); // 当前实例只保留给定集合包含的相同元素
    }
    // 移除元素工具方法, 当第二个参数为true时, 数组缓存保留给定集合包含的元素, 当第二个参数为false时, 数组缓存保留集合不包含的元素
    private boolean batchRemove(Collection<?> c, boolean complement) {
        final Object[] elementData = this.elementData;
        int r = 0, w = 0;
        boolean modified = false;
        try {
            for (; r < size; r++) // 遍历整个数组缓存有效元素部分
                if (c.contains(elementData[r]) == complement) // 根据第二个参数看是数组缓存保留的是集合包含的还是不包含的元素
                    elementData[w++] = elementData[r];  // 将符合条件的元素依次保留到数组缓存有效元素尾部
        } finally {
            // Preserve behavioral compatibility with AbstractCollection, even if c.contains() throws.
            // 下方这个if作用是当遍历筛选有效元素过程中抛出异常, 没有完成筛选, 那么有效元素设置为已经筛选有效的和未曾筛选的部分之和
            if (r != size) { // 如果r与原有效元素个数不等, 证明没有遍历完, 可能由于c.contains抛出异常导致
                System.arraycopy(elementData, r,
                                 elementData, w,
                                 size - r); // 将发生异常没有完成遍历的元素部分(当前r和之后的部分), 复制到当前有效元素w后面
                w += size - r; // 这时有效元素个数为已经筛选出来的有效元素w加上后方未遍历筛选的元素size-r, 得到当前有效元素数w = w + size -r
            }
            if (w != size) { // w为有效元素个数, 如果有效元素个数与之前不同, 证明有元素被移除
                // clear to let GC do its work
                for (int i = w; i < size; i++) // 将数组缓存w位置后面无效元素置为null, 触发垃圾回收
                    elementData[i] = null;
                modCount += size - w;
                size = w; // 更新有效元素计数
                modified = true; // 标记当前实例包含的元素已经被修改
            }
        }
        return modified;
    }

    /**
     * Save the state of the <tt>ArrayList</tt> instance to a stream (that
     * is, serialize it).
     *
     * @serialData The length of the array backing the <tt>ArrayList</tt>
     *             instance is emitted (int), followed by all of its elements
     *             (each an <tt>Object</tt>) in the proper order.
     */ // 序列化当前对象到输出流
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException{
        // Write out element count, and any hidden stuff
        int expectedModCount = modCount;
        s.defaultWriteObject();

        // Write out size as capacity for behavioural compatibility with clone()
        s.writeInt(size);

        // Write out all elements in the proper order.
        for (int i=0; i<size; i++) {
            s.writeObject(elementData[i]);
        }

        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * Reconstitute the <tt>ArrayList</tt> instance from a stream (that is,
     * deserialize it).
     */ // 反序列化输入流内容到当前对象
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        elementData = EMPTY_ELEMENTDATA;

        // Read in size, and any hidden stuff
        s.defaultReadObject();

        // Read in capacity
        s.readInt(); // ignored

        if (size > 0) {
            // be like clone(), allocate array based upon size not capacity
            int capacity = calculateCapacity(elementData, size);
            SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, capacity);
            ensureCapacityInternal(size);

            Object[] a = elementData;
            // Read in all elements in the proper order.
            for (int i=0; i<size; i++) {
                a[i] = s.readObject();
            }
        }
    }

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence), starting at the specified position in the list.
     * The specified index indicates the first element that would be
     * returned by an initial call to {@link ListIterator#next next}.
     * An initial call to {@link ListIterator#previous previous} would
     * return the element with the specified index minus one.
     *
     * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */ /** 返回初始位置在给定index处的迭代器, 第一次调用{@link ListIterator#next next}返回的是给定index处的元素, 第一次调用{@link ListIterator#previous previous}返回index-1处的元素 */
    public ListIterator<E> listIterator(int index) {
        if (index < 0 || index > size) // 检测初始下标位置是否越界
            throw new IndexOutOfBoundsException("Index: "+index);
        return new ListItr(index); // 返回序列迭代器
    }

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence).
     *
     * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @see #listIterator(int)
     */ // 返回一个默认位置在下标0的序列迭代器
    public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * <p>The returned iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @return an iterator over the elements in this list in proper sequence
     */ // 返回一个普通的集合迭代器
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * An optimized version of AbstractList.Itr
     */ // 基于数组优化版本的集合迭代器
    private class Itr implements Iterator<E> {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;

        Itr() {}
        // 判断当前集合是否还有更多元素需要遍历, 如果是返回true
        public boolean hasNext() {
            return cursor != size;
        }
        /** 返回下一个元素, 如果不存在更多元素抛出{@link NoSuchElementException}异常, 所以调用此方法前要通过{@link #hasNext()}方法判断是否有更多元素 */
        @SuppressWarnings("unchecked")
        public E next() {
            checkForComodification(); // 检测遍历过程中被并发修改就抛出异常
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i + 1; // 游标计数加一
            return (E) elementData[lastRet = i]; // 返回本次调用需要返回的元素
        }
        /** 将迭代器当前最后一个返回的元素从底层数组中移除, 此方法只能在每次{@link #next()}方法调用后调用一次 */
        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification(); // 检测遍历过程中被并发修改就抛出异常

            try {
                ArrayList.this.remove(lastRet); // 调用外部类ArrayList的remove方法, 移除上一次调用next方法返回的元素
                cursor = lastRet; // 移除上一次返回元素后, 原本后面的元素前移, 修正游标指向的下标
                lastRet = -1; // 将上一次返回元素下标设置为-1, 防止调用一次next方法后重复调用remove方法
                expectedModCount = modCount; // 修改操作计数
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
        // 对当前迭代器剩余需要遍历的每一个元素执行给定动作
        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);
            final int size = ArrayList.this.size; // 获得当前ArrayList包含有效元素数量
            int i = cursor; // 获得游标值, 也就是下一次next方法返回的元素的下标
            if (i >= size) { // 如果i已经大于等于有效元素值, 证明没有需要遍历的元素
                return;
            }
            final Object[] elementData = ArrayList.this.elementData; // 获得数组缓存
            if (i >= elementData.length) { // 检查在此期间ArrayList实例是否被并发修改
                throw new ConcurrentModificationException();
            }
            while (i != size && modCount == expectedModCount) { // 从游标值开始遍历所有剩余有效元素
                consumer.accept((E) elementData[i++]); // 对每个元素执行给定Consumer动作
            }
            // update once at end of iteration to reduce heap write traffic
            cursor = i; // 将游标值赋值为size
            lastRet = i - 1; // 将上一次返回元素下标值设置为size - 1
            checkForComodification(); // 检测是否被并发修改
        }
        // 检测当前实例遍历过程中是否被并发操作, 如果被并发操作抛出异常快速失败
        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    /**
     * An optimized version of AbstractList.ListItr
     */ // 基于数组优化的序列迭代器
    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {
            super();
            cursor = index;
        }

        ListItr() {
            super();
        }
        /** 见{@link ListIterator#hasPrevious()} */
        public boolean hasPrevious() {
            return cursor != 0;
        }
        /** 见{@link ListIterator#nextIndex()} */
        public int nextIndex() {
            return cursor;
        }
        /** 见{@link ListIterator#previousIndex()} */
        public int previousIndex() {
            return cursor - 1;
        }
        /** 见{@link ListIterator#previous()} */
        @SuppressWarnings("unchecked")
        public E previous() {
            checkForComodification();
            int i = cursor - 1;
            if (i < 0)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i;
            return (E) elementData[lastRet = i];
        }
        /** 见{@link ListIterator#set(Object)} */
        public void set(E e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ArrayList.this.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
        /** 见{@link ListIterator#add(Object)} */
        public void add(E e) {
            checkForComodification();

            try {
                int i = cursor;
                ArrayList.this.add(i, e);
                cursor = i + 1;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Returns a view of the portion of this list between the specified
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.  (If
     * {@code fromIndex} and {@code toIndex} are equal, the returned list is
     * empty.)  The returned list is backed by this list, so non-structural
     * changes in the returned list are reflected in this list, and vice-versa.
     * The returned list supports all of the optional list operations.
     *
     * <p>This method eliminates the need for explicit range operations (of
     * the sort that commonly exist for arrays).  Any operation that expects
     * a list can be used as a range operation by passing a subList view
     * instead of a whole list.  For example, the following idiom
     * removes a range of elements from a list:
     * <pre>
     *      list.subList(from, to).clear();
     * </pre>
     * Similar idioms may be constructed for {@link #indexOf(Object)} and
     * {@link #lastIndexOf(Object)}, and all of the algorithms in the
     * {@link Collections} class can be applied to a subList.
     *
     * <p>The semantics of the list returned by this method become undefined if
     * the backing list (i.e., this list) is <i>structurally modified</i> in
     * any way other than via the returned list.  (Structural modifications are
     * those that change the size of this list, or otherwise perturb it in such
     * a fashion that iterations in progress may yield incorrect results.)
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */ // 返回一个基于原始序列的范围为左闭右开[fromIndex, toIndex)(如果fromIndex==toIndex返回空序列)的子序列, 子序列与原始序列对元素的变动会互相影响
    public List<E> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size); // 检测下标范围是否合法
        return new SubList(this, 0, fromIndex, toIndex);
    }
    // 检测下标范围是否合法
    static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size)
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                               ") > toIndex(" + toIndex + ")");
    }
    // ArrayList的子序列, 子序列基于真正的ArrayList, 是其数据视图
    private class SubList extends AbstractList<E> implements RandomAccess {
        private final AbstractList<E> parent;
        private final int parentOffset;
        private final int offset;
        int size;

        SubList(AbstractList<E> parent,
                int offset, int fromIndex, int toIndex) {
            this.parent = parent;
            this.parentOffset = fromIndex;
            this.offset = offset + fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = ArrayList.this.modCount;
        }
        /** 见{@link AbstractList#set(int, Object)} */
        public E set(int index, E e) {
            rangeCheck(index);
            checkForComodification();
            E oldValue = ArrayList.this.elementData(offset + index);
            ArrayList.this.elementData[offset + index] = e;
            return oldValue;
        }
        /** 见{@link AbstractList#get(int)} */
        public E get(int index) {
            rangeCheck(index);
            checkForComodification();
            return ArrayList.this.elementData(offset + index);
        }
        /** 见{@link AbstractList#size()} */
        public int size() {
            checkForComodification();
            return this.size;
        }
        /** 见{@link AbstractList#add(Object)} */
        public void add(int index, E e) {
            rangeCheckForAdd(index);
            checkForComodification();
            parent.add(parentOffset + index, e);
            this.modCount = parent.modCount;
            this.size++;
        }
        /** 见{@link AbstractList#remove(int)} */
        public E remove(int index) {
            rangeCheck(index);
            checkForComodification();
            E result = parent.remove(parentOffset + index);
            this.modCount = parent.modCount;
            this.size--;
            return result;
        }
        /** 见{@link AbstractList#removeRange(int, int)} */
        protected void removeRange(int fromIndex, int toIndex) {
            checkForComodification();
            parent.removeRange(parentOffset + fromIndex,
                               parentOffset + toIndex);
            this.modCount = parent.modCount;
            this.size -= toIndex - fromIndex;
        }
        /** 见{@link AbstractList#addAll(Collection)} */
        public boolean addAll(Collection<? extends E> c) {
            return addAll(this.size, c);
        }
        /** 见{@link AbstractList#addAll(int, Collection)} */
        public boolean addAll(int index, Collection<? extends E> c) {
            rangeCheckForAdd(index);
            int cSize = c.size();
            if (cSize==0)
                return false;

            checkForComodification();
            parent.addAll(parentOffset + index, c);
            this.modCount = parent.modCount;
            this.size += cSize;
            return true;
        }
        /** 见{@link AbstractList#iterator()} */
        public Iterator<E> iterator() {
            return listIterator();
        }
        /** 见{@link AbstractList#listIterator()} */
        public ListIterator<E> listIterator(final int index) {
            checkForComodification();
            rangeCheckForAdd(index);
            final int offset = this.offset;

            return new ListIterator<E>() {
                int cursor = index;
                int lastRet = -1;
                int expectedModCount = ArrayList.this.modCount;
                /** 见{@link ListIterator#hasNext()} */
                public boolean hasNext() {
                    return cursor != SubList.this.size;
                }
                /** 见{@link ListIterator#next()} */
                @SuppressWarnings("unchecked")
                public E next() {
                    checkForComodification();
                    int i = cursor;
                    if (i >= SubList.this.size)
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i + 1;
                    return (E) elementData[offset + (lastRet = i)];
                }
                /** 见{@link ListIterator#hasPrevious()} */
                public boolean hasPrevious() {
                    return cursor != 0;
                }
                /** 见{@link ListIterator#previous()} */
                @SuppressWarnings("unchecked")
                public E previous() {
                    checkForComodification();
                    int i = cursor - 1;
                    if (i < 0)
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i;
                    return (E) elementData[offset + (lastRet = i)];
                }
                /** 见{@link ListIterator#forEachRemaining(Consumer)} */
                @SuppressWarnings("unchecked")
                public void forEachRemaining(Consumer<? super E> consumer) {
                    Objects.requireNonNull(consumer);
                    final int size = SubList.this.size;
                    int i = cursor;
                    if (i >= size) {
                        return;
                    }
                    final Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length) {
                        throw new ConcurrentModificationException();
                    }
                    while (i != size && modCount == expectedModCount) {
                        consumer.accept((E) elementData[offset + (i++)]);
                    }
                    // update once at end of iteration to reduce heap write traffic
                    lastRet = cursor = i;
                    checkForComodification();
                }
                /** 见{@link ListIterator#nextIndex()} */
                public int nextIndex() {
                    return cursor;
                }
                /** 见{@link ListIterator#previousIndex()} */
                public int previousIndex() {
                    return cursor - 1;
                }
                /** 见{@link ListIterator#remove()} */
                public void remove() {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        SubList.this.remove(lastRet);
                        cursor = lastRet;
                        lastRet = -1;
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }
                /** 见{@link ListIterator#set(Object)} */
                public void set(E e) {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        ArrayList.this.set(offset + lastRet, e);
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }
                /** 见{@link ListIterator#add(Object)} */
                public void add(E e) {
                    checkForComodification();

                    try {
                        int i = cursor;
                        SubList.this.add(i, e);
                        cursor = i + 1;
                        lastRet = -1;
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }
                // 工具方法, 检测当前ArrayList实例是否被并发修改
                final void checkForComodification() {
                    if (expectedModCount != ArrayList.this.modCount)
                        throw new ConcurrentModificationException();
                }
            };
        }
        /** 见{@link AbstractList#subList(int, int)} */
        public List<E> subList(int fromIndex, int toIndex) {
            subListRangeCheck(fromIndex, toIndex, size);
            return new SubList(this, offset, fromIndex, toIndex); // 在数据视图上创建另一个基于ArrayList的数据视图, 这里的下表范围要满足当前数据视图的限制
        }
        /** 见{@link ArrayList#rangeCheck(int)}  */
        private void rangeCheck(int index) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
        /** 见{@link ArrayList#rangeCheckForAdd(int)}  */
        private void rangeCheckForAdd(int index) {
            if (index < 0 || index > this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
        /** 见{@link ArrayList#outOfBoundsMsg(int)}  */
        private String outOfBoundsMsg(int index) {
            return "Index: "+index+", Size: "+this.size;
        }
        // 工具方法, 检测当前ArrayList实例是否被并发修改
        private void checkForComodification() {
            if (ArrayList.this.modCount != this.modCount)
                throw new ConcurrentModificationException();
        }
        /** 见{@link ArrayList#spliterator()}  */
        public Spliterator<E> spliterator() {
            checkForComodification();
            return new ArrayListSpliterator<E>(ArrayList.this, offset,
                                               offset + this.size, this.modCount);
        }
    }
    /** 见{@link Iterable#forEach(Consumer)} */
    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        final int expectedModCount = modCount;
        @SuppressWarnings("unchecked")
        final E[] elementData = (E[]) this.elementData;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            action.accept(elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
     * and <em>fail-fast</em> {@link Spliterator} over the elements in this
     * list.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#SIZED},
     * {@link Spliterator#SUBSIZED}, and {@link Spliterator#ORDERED}.
     * Overriding implementations should document the reporting of additional
     * characteristic values.
     *
     * @return a {@code Spliterator} over the elements in this list
     * @since 1.8
     */ /** 见{@link Collection#spliterator()} */
    @Override
    public Spliterator<E> spliterator() {
        return new ArrayListSpliterator<>(this, 0, -1, 0);
    }

    /** Index-based split-by-two, lazily initialized Spliterator */
    static final class ArrayListSpliterator<E> implements Spliterator<E> { // ArrayList的可分割迭代器, 基于数组

        /*
         * If ArrayLists were immutable, or structurally immutable (no
         * adds, removes, etc), we could implement their spliterators
         * with Arrays.spliterator. Instead we detect as much
         * interference during traversal as practical without
         * sacrificing much performance. We rely primarily on
         * modCounts. These are not guaranteed to detect concurrency
         * violations, and are sometimes overly conservative about
         * within-thread interference, but detect enough problems to
         * be worthwhile in practice. To carry this out, we (1) lazily
         * initialize fence and expectedModCount until the latest
         * point that we need to commit to the state we are checking
         * against; thus improving precision.  (This doesn't apply to
         * SubLists, that create spliterators with current non-lazy
         * values).  (2) We perform only a single
         * ConcurrentModificationException check at the end of forEach
         * (the most performance-sensitive method). When using forEach
         * (as opposed to iterators), we can normally only detect
         * interference after actions, not before. Further
         * CME-triggering checks apply to all other possible
         * violations of assumptions for example null or too-small
         * elementData array given its size(), that could only have
         * occurred due to interference.  This allows the inner loop
         * of forEach to run without any further checks, and
         * simplifies lambda-resolution. While this does entail a
         * number of checks, note that in the common case of
         * list.stream().forEach(a), no checks or other computation
         * occur anywhere other than inside forEach itself.  The other
         * less-often-used methods cannot take advantage of most of
         * these streamlinings.
         */

        private final ArrayList<E> list;
        private int index; // current index, modified on advance/split
        private int fence; // -1 until used; then one past last index
        private int expectedModCount; // initialized when fence set

        /** Create new spliterator covering the given  range */
        ArrayListSpliterator(ArrayList<E> list, int origin, int fence,
                             int expectedModCount) {
            this.list = list; // OK if null unless traversed
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence() { // initialize fence to size on first use
            int hi; // (a specialized variant appears in method forEach)
            ArrayList<E> lst;
            if ((hi = fence) < 0) {
                if ((lst = list) == null)
                    hi = fence = 0;
                else {
                    expectedModCount = lst.modCount;
                    hi = fence = lst.size;
                }
            }
            return hi;
        }
        /** 见{@link Spliterator#trySplit()} */
        public ArrayListSpliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null : // divide range in half unless too small
                new ArrayListSpliterator<E>(list, lo, index = mid,
                                            expectedModCount);
        }
        /** 见{@link Spliterator#trySplit()} */
        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            int hi = getFence(), i = index;
            if (i < hi) {
                index = i + 1;
                @SuppressWarnings("unchecked") E e = (E)list.elementData[i];
                action.accept(e);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }
        /** 见{@link Spliterator#forEachRemaining(Consumer)} */
        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi, mc; // hoist accesses and checks from loop
            ArrayList<E> lst; Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((lst = list) != null && (a = lst.elementData) != null) {
                if ((hi = fence) < 0) {
                    mc = lst.modCount;
                    hi = lst.size;
                }
                else
                    mc = expectedModCount;
                if ((i = index) >= 0 && (index = hi) <= a.length) {
                    for (; i < hi; ++i) {
                        @SuppressWarnings("unchecked") E e = (E) a[i];
                        action.accept(e);
                    }
                    if (lst.modCount == mc)
                        return;
                }
            }
            throw new ConcurrentModificationException();
        }
        /** 见{@link Spliterator#estimateSize()}  */
        public long estimateSize() {
            return (long) (getFence() - index);
        }
        /** 见{@link Spliterator#characteristics()} */
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }
    /** 见{@link Collection#removeIf(Predicate)} */
    @Override // 此处ArrayList的实现与Collection默认的基于迭代器的实现不同
    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        // figure out which elements are to be removed
        // any exception thrown from the filter predicate at this stage
        // will leave the collection unmodified
        int removeCount = 0;
        final BitSet removeSet = new BitSet(size); // 初始化一个容量为size的BitSet记录需要移除的元素的下标信息
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) { // 在当前ArrayList实例没被并发修改的前提下遍历数组缓存中的元素
            @SuppressWarnings("unchecked")
            final E element = (E) elementData[i];
            if (filter.test(element)) { // 如果此元素根据参数逻辑判断需要移除
                removeSet.set(i); // 那么在BitSet中记录此元素下标
                removeCount++; // 更新移除元素计数
            }
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }

        // shift surviving elements left over the spaces left by removed elements
        final boolean anyToRemove = removeCount > 0;
        if (anyToRemove) {
            final int newSize = size - removeCount; // 计算出移除后的实例持有元素数量
            for (int i=0, j=0; (i < size) && (j < newSize); i++, j++) {
                i = removeSet.nextClearBit(i); // 通过获取BitSet中没有被标记的位信息, 得到需要保留的元素下标
                elementData[j] = elementData[i]; // newSize即j的最终值, 是需要保留的元素的总数, 将要保留的元素依次保留在数组缓存头部
            }
            for (int k=newSize; k < size; k++) { // 将数组缓存有效元素之后的位置设置为null, 触发垃圾回收
                elementData[k] = null;  // Let gc do its work
            }
            this.size = newSize; // 将实例持有元素数量更新为移除需要移除元素后的有效元素数量
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            modCount++; // 更新操作计数
        }

        return anyToRemove; // 返回本次操作是否有元素改动
    }
    /** 见{@link List#replaceAll(UnaryOperator)} */
    @Override
    @SuppressWarnings("unchecked")
    public void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            elementData[i] = operator.apply((E) elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }
    /** 见{@link List#sort(Comparator)} */
    @Override
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super E> c) {
        final int expectedModCount = modCount;
        Arrays.sort((E[]) elementData, 0, size, c);
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }
}
