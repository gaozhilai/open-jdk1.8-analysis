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
 * This class provides a skeletal implementation of the <tt>Set</tt>
 * interface to minimize the effort required to implement this
 * interface. <p>
 *
 * The process of implementing a set by extending this class is identical
 * to that of implementing a Collection by extending AbstractCollection,
 * except that all of the methods and constructors in subclasses of this
 * class must obey the additional constraints imposed by the <tt>Set</tt>
 * interface (for instance, the add method must not permit addition of
 * multiple instances of an object to a set).<p>
 *
 * Note that this class does not override any of the implementations from
 * the <tt>AbstractCollection</tt> class.  It merely adds implementations
 * for <tt>equals</tt> and <tt>hashCode</tt>.<p>
 *
 * This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @param <E> the type of elements maintained by this set
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see Collection
 * @see AbstractCollection
 * @see Set
 * @since 1.2
 */
 // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/7/3 18:21
public abstract class AbstractSet<E> extends AbstractCollection<E> implements Set<E> { // Set的骨架类, 提供了一些方法通用实现, 实现自己的Set可以继承此类
    /**
     * Sole constructor.  (For invocation by subclass constructors, typically
     * implicit.)
     */ // 保护权限的构造器, 只有子类可以调用
    protected AbstractSet() {
    }

    // Comparison and hashing 比较和哈希相关方法

    /**
     * Compares the specified object with this set for equality.  Returns
     * <tt>true</tt> if the given object is also a set, the two sets have
     * the same size, and every member of the given set is contained in
     * this set.  This ensures that the <tt>equals</tt> method works
     * properly across different implementations of the <tt>Set</tt>
     * interface.<p>
     *
     * This implementation first checks if the specified object is this
     * set; if so it returns <tt>true</tt>.  Then, it checks if the
     * specified object is a set whose size is identical to the size of
     * this set; if not, it returns false.  If so, it returns
     * <tt>containsAll((Collection) o)</tt>.
     *
     * @param o object to be compared for equality with this set
     * @return <tt>true</tt> if the specified object is equal to this set
     */ /** 见{@link Set#equals(Object)} 两个Set包含元素相同视为相等, 即使两个Set是不同子类的实例 */
    public boolean equals(Object o) {
        if (o == this) // 内存地址相等, 就是同一个实例
            return true; // 返回true

        if (!(o instanceof Set)) // 参数不是Set
            return false; // 返回false
        Collection<?> c = (Collection<?>) o; // 将参数上转型成集合
        if (c.size() != size()) // 如果两个实例包含元素个数不同, 那么一定不相等
            return false; // 返回false
        try {
            return containsAll(c); // 否则包含元素数量相等, 并且当前实例又包含参数实例所有元素, 那么两个实例包含的元素是相同的, 返回true
        } catch (ClassCastException unused)   {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
    }

    /**
     * Returns the hash code value for this set.  The hash code of a set is
     * defined to be the sum of the hash codes of the elements in the set,
     * where the hash code of a <tt>null</tt> element is defined to be zero.
     * This ensures that <tt>s1.equals(s2)</tt> implies that
     * <tt>s1.hashCode()==s2.hashCode()</tt> for any two sets <tt>s1</tt>
     * and <tt>s2</tt>, as required by the general contract of
     * {@link Object#hashCode}.
     *
     * <p>This implementation iterates over the set, calling the
     * <tt>hashCode</tt> method on each element in the set, and adding up
     * the results.
     *
     * @return the hash code value for this set
     * @see Object#equals(Object)
     * @see Set#equals(Object)
     */ /** 见{@link Set#hashCode()} hashCode与Set包含的每个元素相关 */
    public int hashCode() {
        int h = 0;
        Iterator<E> i = iterator(); // 获取当前Set迭代器
        while (i.hasNext()) { // 遍历每一个元素
            E obj = i.next();
            if (obj != null)
                h += obj.hashCode(); // 综合每一个元素的hashCode得到当前Set实例的hashCode
        }
        return h;
    }

    /**
     * Removes from this set all of its elements that are contained in the
     * specified collection (optional operation).  If the specified
     * collection is also a set, this operation effectively modifies this
     * set so that its value is the <i>asymmetric set difference</i> of
     * the two sets.
     *
     * <p>This implementation determines which is the smaller of this set
     * and the specified collection, by invoking the <tt>size</tt>
     * method on each.  If this set has fewer elements, then the
     * implementation iterates over this set, checking each element
     * returned by the iterator in turn to see if it is contained in
     * the specified collection.  If it is so contained, it is removed
     * from this set with the iterator's <tt>remove</tt> method.  If
     * the specified collection has fewer elements, then the
     * implementation iterates over the specified collection, removing
     * from this set each element returned by the iterator, using this
     * set's <tt>remove</tt> method.
     *
     * <p>Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by the
     * <tt>iterator</tt> method does not implement the <tt>remove</tt> method.
     *
     * @param  c collection containing elements to be removed from this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>removeAll</tt> operation
     *         is not supported by this set
     * @throws ClassCastException if the class of an element of this set
     *         is incompatible with the specified collection
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this set contains a null element and the
     *         specified collection does not permit null elements
     * (<a href="Collection.html#optional-restrictions">optional</a>),
     *         or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */ /** 见{@link Set#removeAll(Collection)} 骨架实现通过迭代器实现removeAll逻辑*/
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c); // 要移除的元素集合不能为空
        boolean modified = false; // 返回结果默认是没有修改过当前Set实例

        if (size() > c.size()) { // 如果当前实例包含元素大于给定的集合
            for (Iterator<?> i = c.iterator(); i.hasNext(); ) // 遍历给定集合每一个元素
                modified |= remove(i.next()); // 将其从当前Set实例移除, 并且记录是否有元素被移除
        } else { // 要移除的元素集合包含的元素比当前Set包含元素还多
            for (Iterator<?> i = iterator(); i.hasNext(); ) { // 遍历当前Set的每一个元素
                if (c.contains(i.next())) { // 判断当前遍历到的元素是否在要移除元素的集合中存在
                    i.remove(); // 要移除就进行移除操作
                    modified = true; // 并记录是否有元素被移除
                }
            }
        }
        return modified; // 返回当前Set实例是否有元素被移除
    }

}
