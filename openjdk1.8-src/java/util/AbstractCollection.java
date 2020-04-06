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
 * This class provides a skeletal implementation of the <tt>Collection</tt>
 * interface, to minimize the effort required to implement this interface. <p>
 *
 * To implement an unmodifiable collection, the programmer needs only to
 * extend this class and provide implementations for the <tt>iterator</tt> and
 * <tt>size</tt> methods.  (The iterator returned by the <tt>iterator</tt>
 * method must implement <tt>hasNext</tt> and <tt>next</tt>.)<p>
 *
 * To implement a modifiable collection, the programmer must additionally
 * override this class's <tt>add</tt> method (which otherwise throws an
 * <tt>UnsupportedOperationException</tt>), and the iterator returned by the
 * <tt>iterator</tt> method must additionally implement its <tt>remove</tt>
 * method.<p>
 *
 * The programmer should generally provide a void (no argument) and
 * <tt>Collection</tt> constructor, as per the recommendation in the
 * <tt>Collection</tt> interface specification.<p>
 *
 * The documentation for each non-abstract method in this class describes its
 * implementation in detail.  Each of these methods may be overridden if
 * the collection being implemented admits a more efficient implementation.<p>
 *
 * This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see Collection
 * @since 1.2
 */
 // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/4/3 18:14
public abstract class AbstractCollection<E> implements Collection<E> { // AbstractCollection抽象类为具体的集合子类提供了骨架实现, 继承此类可以省去实现一些通用方法的付出, 不过也可以选择覆写骨架实现
    /**
     * Sole constructor.  (For invocation by subclass constructors, typically
     * implicit.)
     */ // 唯一的protected权限的构造器, 只能被子类调用
    protected AbstractCollection() {
    }

    // Query Operations 查询操作

    /**
     * Returns an iterator over the elements contained in this collection.
     *
     * @return an iterator over the elements contained in this collection
     */ // 返回一个当前集合的迭代器
    public abstract Iterator<E> iterator();
    // 返回当前集合包含的元素数量
    public abstract int size();

    /**
     * {@inheritDoc}
     *
     * <p>This implementation returns <tt>size() == 0</tt>.
     */ // 判断当前集合是否为空, 即不包含任何元素返回true
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation iterates over the elements in the collection,
     * checking each element in turn for equality with the specified element.
     *
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */ // 判断当前集合是否包含指定元素, 此处逻辑通过迭代当前集合所有元素与给定元素比较实现
    public boolean contains(Object o) {
        Iterator<E> it = iterator(); // 获取当前集合迭代器
        if (o==null) { // 如果给定要判断的元素为null
            while (it.hasNext()) // 遍历所有元素
                if (it.next()==null) // 找到了包含相等的元素即null
                    return true;
        } else {
            while (it.hasNext()) // 遍历所有元素
                if (o.equals(it.next())) // 一次判断是否与给定元素相等
                    return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation returns an array containing all the elements
     * returned by this collection's iterator, in the same order, stored in
     * consecutive elements of the array, starting with index {@code 0}.
     * The length of the returned array is equal to the number of elements
     * returned by the iterator, even if the size of this collection changes
     * during iteration, as might happen if the collection permits
     * concurrent modification during iteration.  The {@code size} method is
     * called only as an optimization hint; the correct result is returned
     * even if the iterator returns a different number of elements.
     *
     * <p>This method is equivalent to:
     *
     *  <pre> {@code
     * List<E> list = new ArrayList<E>(size());
     * for (E e : this)
     *     list.add(e);
     * return list.toArray();
     * }</pre>
     */ // 返回一个包含当前集合所有元素的数组
    public Object[] toArray() {
        // Estimate size of array; be prepared to see more or fewer elements
        Object[] r = new Object[size()]; // 这里的size是为了提高性能, 如果集合允许并发操作, size可能不准确, 即使size不准确最后的finishToArray也会返回正确的数组
        Iterator<E> it = iterator(); // 获得集合迭代器
        for (int i = 0; i < r.length; i++) { // 遍历数组中每一个位置
            if (! it.hasNext()) // fewer elements than expected 如果集合时机元素数量比前面size估算的少
                return Arrays.copyOf(r, i); // 直接将当前包含所有元素的数组复制一份返回, 为了去除已有数组多余的长度
            r[i] = it.next(); // 将迭代器(集合)中元素放入数组
        }
        return it.hasNext() ? finishToArray(r, it) : r; // 迭代器没有下一个元素即估算的size与实际元素一致, 直接返回数组, 否则处理实际元素大于size的情况
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation returns an array containing all the elements
     * returned by this collection's iterator in the same order, stored in
     * consecutive elements of the array, starting with index {@code 0}.
     * If the number of elements returned by the iterator is too large to
     * fit into the specified array, then the elements are returned in a
     * newly allocated array with length equal to the number of elements
     * returned by the iterator, even if the size of this collection
     * changes during iteration, as might happen if the collection permits
     * concurrent modification during iteration.  The {@code size} method is
     * called only as an optimization hint; the correct result is returned
     * even if the iterator returns a different number of elements.
     *
     * <p>This method is equivalent to:
     *
     *  <pre> {@code
     * List<E> list = new ArrayList<E>(size());
     * for (E e : this)
     *     list.add(e);
     * return list.toArray(a);
     * }</pre>
     *
     * @throws ArrayStoreException  {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */ /** 将当前集合所有元素放入给定数组, 如果给定数组装不下所有元素, 那么返回一个新的长度等于元素数量的装有所有元素的数组 */
    @SuppressWarnings("unchecked") /** 方法逻辑与{@link #toArray()}一致, 与其不同的是可以指定数组元素类型 */
    public <T> T[] toArray(T[] a) {
        // Estimate size of array; be prepared to see more or fewer elements
        int size = size(); // 为了提高性能, 评估集合元素数量
        T[] r = a.length >= size ? a : // 如果给定数组长度大于评估的size, 那么给定数组能装下所有元素, 使用给定参数的数组
                  (T[])java.lang.reflect.Array
                  .newInstance(a.getClass().getComponentType(), size); // 给定数组容量不够, 用评估的size新创建指定类型的数组
        Iterator<E> it = iterator(); // 获取集合迭代器

        for (int i = 0; i < r.length; i++) { // 遍历数组每一个位置
            if (! it.hasNext()) { // fewer elements than expected 如果实际元素数量比数组预估长度少
                if (a == r) { // 如果当前数组就是给定数组, 实际元素比给定数组长度少
                    r[i] = null; // null-terminate 将数组多余部分全设置为null, 即空结束
                } else if (a.length < i) { // 给定数组a长度小于当前下标
                    return Arrays.copyOf(r, i); // 复制一个新的数组, 去除数组r多余位置
                } else { // 给定数组a长度大于当前元素下标, 将新数组r元素赋值给给定数组a. 因为177行时数组a长度的确小于元素size, 出现此处a.length>=i是可能因为并发操作减少了元素
                    System.arraycopy(r, 0, a, 0, i);
                    if (a.length > i) { // 将给定数组a剩余位置置为null
                        a[i] = null;
                    }
                }
                return a;
            }
            r[i] = (T)it.next(); // 将元素按顺序放入数组
        }
        // more elements than expected
        return it.hasNext() ? finishToArray(r, it) : r; // 实际元素数量比预期数量多, 调用finishToArray, 否则返回数组r
    }

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */ /** 数组最大长度限制为{@link Integer#MAX_VALUE}, 不过某些虚拟机实现数组保留了几个位置存储数组头信息, 所以此处设置集合中转换数组最大长度为{@link Integer#MAX_VALUE}-8 */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * Reallocates the array being used within toArray when the iterator
     * returned more elements than expected, and finishes filling it from
     * the iterator.
     *
     * @param r the array, replete with previously stored elements
     * @param it the in-progress iterator over this collection
     * @return array containing the elements in the given array, plus any
     *         further elements returned by the iterator, trimmed to size
     */ // 重新分配数组长度, 返回装有迭代器所有元素的数组. 此方法用于被toArray方法中预估的数组size比实际元素数量小的时候调用
    @SuppressWarnings("unchecked")
    private static <T> T[] finishToArray(T[] r, Iterator<?> it) {
        int i = r.length; // 得到旧的数组长度
        while (it.hasNext()) { // 遍历迭代器中剩余元素
            int cap = r.length; // cap为当前数组r的长度, 数组r会随着扩容改变
            if (i == cap) { // 遍历过程中当前元素下标已经等于当前数组长度, 需要扩容, 执行下方扩容逻辑
                int newCap = cap + (cap >> 1) + 1; // 扩容后的数组长度, 初步设置为newCap = oldCap + oldCap/2 + 1
                // overflow-conscious code 数组扩容后是否超出数组支持的最大长度MAX_ARRAY_SIZE
                if (newCap - MAX_ARRAY_SIZE > 0)
                    newCap = hugeCapacity(cap + 1); // 超出数组最大长度但是没超过Integer.Max_VALUE时返回Integer.MAX_VALUE, 否则抛出异常
                r = Arrays.copyOf(r, newCap); // 将扩容前数组复制到一个长度为扩容后的尺寸的数组, 扩容后数组代替扩容前数组, 继续遍历设置元素
            }
            r[i++] = (T)it.next(); // 将迭代器中元素依次放入数组, 然后将i自增, 得到下一个元素在数组中对应的下标
        }
        // trim if overallocated
        return (i == r.length) ? r : Arrays.copyOf(r, i); // 实际元素数量等于当前数组长度时直接返回数组, 否则实际元素数量小于数组长度, 将数组多余长度去除后返回
    }
    // 判断扩容后的数组容量是否溢出和合法
    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow 溢出抛出异常
            throw new OutOfMemoryError
                ("Required array size too large");
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE : // 超过了MAX_ARRAY_SIZE返回Integer.MAX_VALUE
            MAX_ARRAY_SIZE; // 否则返回MAX_ARRAY_SIZE
    }

    // Modification Operations 修改操作

    /**
     * {@inheritDoc}
     *
     * <p>This implementation always throws an
     * <tt>UnsupportedOperationException</tt>.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IllegalStateException         {@inheritDoc}
     */ // 添加元素到当前集合, 需要具体子类实现
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation iterates over the collection looking for the
     * specified element.  If it finds the element, it removes the element
     * from the collection using the iterator's remove method.
     *
     * <p>Note that this implementation throws an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by this
     * collection's iterator method does not implement the <tt>remove</tt>
     * method and this collection contains the specified object.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     */ // 从当前集合中移除指定元素, 此方法默认是通过迭代器来寻找和移除元素的, 具体子类可以有更高效的逻辑覆写
    public boolean remove(Object o) {
        Iterator<E> it = iterator(); // 获得集合迭代器
        if (o==null) { // 指定元素为null时, 迭代器遍历寻找第一个null元素并移除
            while (it.hasNext()) {
                if (it.next()==null) {
                    it.remove();
                    return true;
                }
            }
        } else { // 指定元素不为null时, 迭代器寻找第一个与指定元素相等的元素并移除
            while (it.hasNext()) {
                if (o.equals(it.next())) {
                    it.remove();
                    return true;
                }
            }
        }
        return false;
    }


    // Bulk Operations 批量操作

    /**
     * {@inheritDoc}
     *
     * <p>This implementation iterates over the specified collection,
     * checking each element returned by the iterator in turn to see
     * if it's contained in this collection.  If all elements are so
     * contained <tt>true</tt> is returned, otherwise <tt>false</tt>.
     *
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @see #contains(Object)
     */ /** 判断当前集合是否包含给定集合所有元素, 通过判断给定集合中的元素是否都被{@link #contains(Object)}返回true实现 */
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) // 遍历给定集合的每一个元素
            if (!contains(e)) // 如果有一个元素是不存在于当前集合的, 就返回false
                return false;
        return true; // 所有元素都存在于当前集合, 返回true
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation iterates over the specified collection, and adds
     * each object returned by the iterator to this collection, in turn.
     *
     * <p>Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> unless <tt>add</tt> is
     * overridden (assuming the specified collection is non-empty).
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IllegalStateException         {@inheritDoc}
     *
     * @see #add(Object)
     */ /** 将给定集合所有元素添加到当前集合. 通过循环调用{@link #add(Object)}实现 */
    public boolean addAll(Collection<? extends E> c) {
        boolean modified = false;
        for (E e : c) // 遍历给定集合每一个元素
            if (add(e)) // 将遍历的元素添加到当前集合
                modified = true; // 成功添加至少一次后改变修改标识
        return modified; // 返回修改标识
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation iterates over this collection, checking each
     * element returned by the iterator in turn to see if it's contained
     * in the specified collection.  If it's so contained, it's removed from
     * this collection with the iterator's <tt>remove</tt> method.
     *
     * <p>Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by the
     * <tt>iterator</tt> method does not implement the <tt>remove</tt> method
     * and this collection contains one or more elements in common with the
     * specified collection.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */ // 将当前集合中所有与给定集合相等的元素移除, 通过迭代器实现
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<?> it = iterator(); // 获取集合迭代器
        while (it.hasNext()) { // 遍历当前集合每一个元素
            if (c.contains(it.next())) { // 判断遍历到的元素是否存在于给定参数集合中
                it.remove(); // 如果存在就移除此元素
                modified = true; // 成功移除至少一次后改变修改标识
            }
        }
        return modified; // 返回修改标识
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation iterates over this collection, checking each
     * element returned by the iterator in turn to see if it's contained
     * in the specified collection.  If it's not so contained, it's removed
     * from this collection with the iterator's <tt>remove</tt> method.
     *
     * <p>Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by the
     * <tt>iterator</tt> method does not implement the <tt>remove</tt> method
     * and this collection contains one or more elements not present in the
     * specified collection.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */ // 将当前集合与给定集合不同的元素移除, 求当前集合与给定集合的交集
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<E> it = iterator(); // 获取集合迭代器
        while (it.hasNext()) { // 遍历当前集合每一个元素
            if (!c.contains(it.next())) { // 如果给定集合不包含当前元素
                it.remove(); // 将其移除
                modified = true; // 成功移除至少一次后改变修改标识
            }
        }
        return modified; // 返回修改标识
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation iterates over this collection, removing each
     * element using the <tt>Iterator.remove</tt> operation.  Most
     * implementations will probably choose to override this method for
     * efficiency.
     *
     * <p>Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by this
     * collection's <tt>iterator</tt> method does not implement the
     * <tt>remove</tt> method and this collection is non-empty.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     */ // 清空当前集合, 通过迭代器遍历移除实现
    public void clear() {
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
    }


    //  String conversion

    /**
     * Returns a string representation of this collection.  The string
     * representation consists of a list of the collection's elements in the
     * order they are returned by its iterator, enclosed in square brackets
     * (<tt>"[]"</tt>).  Adjacent elements are separated by the characters
     * <tt>", "</tt> (comma and space).  Elements are converted to strings as
     * by {@link String#valueOf(Object)}.
     *
     * @return a string representation of this collection
     */ // 默认的转换字符串实现
    public String toString() {
        Iterator<E> it = iterator();
        if (! it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            E e = it.next();
            sb.append(e == this ? "(this Collection)" : e);
            if (! it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }

}
