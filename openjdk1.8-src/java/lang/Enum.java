/*
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

import java.io.Serializable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;

/**
 * This is the common base class of all Java language enumeration types.
 *
 * More information about enums, including descriptions of the
 * implicitly declared methods synthesized by the compiler, can be
 * found in section 8.9 of
 * <cite>The Java&trade; Language Specification</cite>.
 *
 * <p> Note that when using an enumeration type as the type of a set
 * or as the type of the keys in a map, specialized and efficient
 * {@linkplain java.util.EnumSet set} and {@linkplain
 * java.util.EnumMap map} implementations are available.
 *
 * @param <E> The enum type subclass
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see     Class#getEnumConstants()
 * @see     java.util.EnumSet
 * @see     java.util.EnumMap
 * @since   1.5
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/7/7 16:22
public abstract class Enum<E extends Enum<E>> // 枚举类的父类型, enum类型将会被编译器转换成public final class Name extends Enum{}的形式
        implements Comparable<E>, Serializable { // 枚举中每一个元素都是当前枚举类型本身
    /**
     * The name of this enum constant, as declared in the enum declaration.
     * Most programmers should use the {@link #toString} method rather than
     * accessing this field.
     */ // 枚举元素的名字是枚举元素的字面定义名字, 由编译器实例化枚举元素时进行初始化
    private final String name;

    /**
     * Returns the name of this enum constant, exactly as declared in its
     * enum declaration.
     *
     * <b>Most programmers should use the {@link #toString} method in
     * preference to this one, as the toString method may return
     * a more user-friendly name.</b>  This method is designed primarily for
     * use in specialized situations where correctness depends on getting the
     * exact name, which will not vary from release to release.
     *
     * @return the name of this enum constant
     */ // 返回当前枚举元素的名字
    public final String name() {
        return name;
    }

    /**
     * The ordinal of this enumeration constant (its position
     * in the enum declaration, where the initial constant is assigned
     * an ordinal of zero).
     *
     * Most programmers will have no use for this field.  It is designed
     * for use by sophisticated enum-based data structures, such as
     * {@link java.util.EnumSet} and {@link java.util.EnumMap}.
     */ // 当前枚举元素的序号, 由枚举元素声明顺序决定, 从0开始, 也是由编译器实例化枚举元素时初始化
    private final int ordinal;

    /**
     * Returns the ordinal of this enumeration constant (its position
     * in its enum declaration, where the initial constant is assigned
     * an ordinal of zero).
     *
     * Most programmers will have no use for this method.  It is
     * designed for use by sophisticated enum-based data structures, such
     * as {@link java.util.EnumSet} and {@link java.util.EnumMap}.
     *
     * @return the ordinal of this enumeration constant
     */ // 返回当前枚举元素的序号
    public final int ordinal() {
        return ordinal;
    }

    /**
     * Sole constructor.  Programmers cannot invoke this constructor.
     * It is for use by code emitted by the compiler in response to
     * enum type declarations.
     *
     * @param name - The name of this enum constant, which is the identifier
     *               used to declare it.
     * @param ordinal - The ordinal of this enumeration constant (its position
     *         in the enum declaration, where the initial constant is assigned
     *         an ordinal of zero).
     */ // 指定枚举元素名字和序号的构造器, 由上面提到的编译器隐式生成代码进行调用
    protected Enum(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }

    /**
     * Returns the name of this enum constant, as contained in the
     * declaration.  This method may be overridden, though it typically
     * isn't necessary or desirable.  An enum type should override this
     * method when a more "programmer-friendly" string form exists.
     *
     * @return the name of this enum constant
     */ // 枚举元素的toString方法是打印当前枚举元素的名字
    public String toString() {
        return name;
    }

    /**
     * Returns true if the specified object is equal to this
     * enum constant.
     *
     * @param other the object to be compared for equality with this object.
     * @return  true if the specified object is equal to this
     *          enum constant.
     */ // 判断给定的对象是否与当前元素相等, 这里业务上的相等就是内存上的相等, 必须严格是一个同一个枚举元素实例才相等
    public final boolean equals(Object other) {
        return this==other;
    }

    /**
     * Returns a hash code for this enum constant.
     *
     * @return a hash code for this enum constant.
     */ // 返回当前枚举元素的hashCode, 使用Object中的hashCode方法实现
    public final int hashCode() {
        return super.hashCode();
    }

    /**
     * Throws CloneNotSupportedException.  This guarantees that enums
     * are never cloned, which is necessary to preserve their "singleton"
     * status.
     *
     * @return (never returns)
     */ // 枚举元素是独一无二的, 不支持克隆, 所以调用clone方法会抛出不支持异常
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Compares this enum with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * Enum constants are only comparable to other enum constants of the
     * same enum type.  The natural order implemented by this
     * method is the order in which the constants are declared.
     */ // 枚举元素默认的比较大小方法是以元素的序号作为依据
    public final int compareTo(E o) {
        Enum<?> other = (Enum<?>)o; //
        Enum<E> self = this;
        if (self.getClass() != other.getClass() && // optimization 优化判断效率, 如果getClass直接相等, 那么直接执行比较逻辑
            self.getDeclaringClass() != other.getDeclaringClass()) // 如果getClass不相等, 那还要判断其superClass是不是Enum.class, 不是的话是否相等(内部类的情况)
            throw new ClassCastException(); // 如果两个元素考虑了内部类的情况也的确不相等, 那么直接抛出类型转换异常
        return self.ordinal - other.ordinal; // 如果两个枚举元素类型相等, 那么根据枚举元素序号执行比较逻辑
    }

    /**
     * Returns the Class object corresponding to this enum constant's
     * enum type.  Two enum constants e1 and  e2 are of the
     * same enum type if and only if
     *   e1.getDeclaringClass() == e2.getDeclaringClass().
     * (The value returned by this method may differ from the one returned
     * by the {@link Object#getClass} method for enum constants with
     * constant-specific class bodies.)
     *
     * @return the Class object corresponding to this enum constant's
     *     enum type
     */ // 获得当前枚举实例的真实枚举类型, 考虑了枚举元素可能是当前枚举类型内部类的情况
    @SuppressWarnings("unchecked")
    public final Class<E> getDeclaringClass() {
        Class<?> clazz = getClass(); // 获得当前枚举类型
        Class<?> zuper = clazz.getSuperclass(); // 获得类型的父类型
        return (zuper == Enum.class) ? (Class<E>)clazz : (Class<E>)zuper; // 如果父类型是当前Enum.class, 那么不涉及内部类, 返回claszz即可. 否则内部类是Name$1, Name$2这种情况返回枚举元素真实类型zsuper
    }

    /**
     * Returns the enum constant of the specified enum type with the
     * specified name.  The name must match exactly an identifier used
     * to declare an enum constant in this type.  (Extraneous whitespace
     * characters are not permitted.)
     *
     * <p>Note that for a particular enum type {@code T}, the
     * implicitly declared {@code public static T valueOf(String)}
     * method on that enum may be used instead of this method to map
     * from a name to the corresponding enum constant.  All the
     * constants of an enum type can be obtained by calling the
     * implicit {@code public static T[] values()} method of that
     * type.
     *
     * @param <T> The enum type whose constant is to be returned
     * @param enumType the {@code Class} object of the enum type from which
     *      to return a constant
     * @param name the name of the constant to return
     * @return the enum constant of the specified enum type with the
     *      specified name
     * @throws IllegalArgumentException if the specified enum type has
     *         no constant with the specified name, or the specified
     *         class object does not represent an enum type
     * @throws NullPointerException if {@code enumType} or {@code name}
     *         is null
     * @since 1.5
     */ // 根据给定的枚举类型与标识符名称获得对应的实例
    public static <T extends Enum<T>> T valueOf(Class<T> enumType,
                                                String name) {
        T result = enumType.enumConstantDirectory().get(name); // 先是获得到了name-enum实例的Map, 然后通过Name获得到对应的枚举实例
        if (result != null) // 如果根据name获取到对应的枚举实例
            return result; // 返回此实例
        if (name == null) // 判断指定的标识符名字不能为空
            throw new NullPointerException("Name is null");
        throw new IllegalArgumentException(
            "No enum constant " + enumType.getCanonicalName() + "." + name); // 根据name获取不到对应的枚举实例抛出异常
    }

    /**
     * enum classes cannot have finalize methods.
     */
    protected final void finalize() { }

    /**
     * prevent default deserialization
     */ // 枚举类不能从输入流序列化
    private void readObject(ObjectInputStream in) throws IOException,
        ClassNotFoundException {
        throw new InvalidObjectException("can't deserialize enum");
    }
    // 枚举类不能从输入流序列化
    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("can't deserialize enum");
    }
}
