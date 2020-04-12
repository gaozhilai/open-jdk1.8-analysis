/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.annotation;

/**
 * The constants of this enumerated type provide a simple classification of the
 * syntactic locations where annotations may appear in a Java program. These
 * constants are used in {@link Target java.lang.annotation.Target}
 * meta-annotations to specify where it is legal to write annotations of a
 * given type.
 *
 * <p>The syntactic locations where annotations may appear are split into
 * <em>declaration contexts</em> , where annotations apply to declarations, and
 * <em>type contexts</em> , where annotations apply to types used in
 * declarations and expressions.
 *
 * <p>The constants {@link #ANNOTATION_TYPE} , {@link #CONSTRUCTOR} , {@link
 * #FIELD} , {@link #LOCAL_VARIABLE} , {@link #METHOD} , {@link #PACKAGE} ,
 * {@link #PARAMETER} , {@link #TYPE} , and {@link #TYPE_PARAMETER} correspond
 * to the declaration contexts in JLS 9.6.4.1.
 *
 * <p>For example, an annotation whose type is meta-annotated with
 * {@code @Target(ElementType.FIELD)} may only be written as a modifier for a
 * field declaration.
 *
 * <p>The constant {@link #TYPE_USE} corresponds to the 15 type contexts in JLS
 * 4.11, as well as to two declaration contexts: type declarations (including
 * annotation type declarations) and type parameter declarations.
 *
 * <p>For example, an annotation whose type is meta-annotated with
 * {@code @Target(ElementType.TYPE_USE)} may be written on the type of a field
 * (or within the type of the field, if it is a nested, parameterized, or array
 * type), and may also appear as a modifier for, say, a class declaration.
 *
 * <p>The {@code TYPE_USE} constant includes type declarations and type
 * parameter declarations as a convenience for designers of type checkers which
 * give semantics to annotation types. For example, if the annotation type
 * {@code NonNull} is meta-annotated with
 * {@code @Target(ElementType.TYPE_USE)}, then {@code @NonNull}
 * {@code class C {...}} could be treated by a type checker as indicating that
 * all variables of class {@code C} are non-null, while still allowing
 * variables of other classes to be non-null or not non-null based on whether
 * {@code @NonNull} appears at the variable's declaration.
 *
 * @author  Joshua Bloch
 * @since 1.5
 * @jls 9.6.4.1 @Target
 * @jls 4.1 The Kinds of Types and Values
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/4/11 10:22
public enum ElementType { /** 定义注解时, 将此枚举值作为{@link Target}元注解参数, 标记被标记的注解可以注解的位置 */
    /** Class, interface (including annotation type), or enum declaration */
    TYPE, // 标记注解可以用到类, 接口(包括注解), 或者枚举类声明上

    /** Field declaration (includes enum constants) */
    FIELD, // 标记注解可以用在变量声明上, 包含枚举实例变量

    /** Method declaration */
    METHOD, // 标记注解可以用在方法上

    /** Formal parameter declaration */
    PARAMETER, // 标记注解可以用在形参上

    /** Constructor declaration */
    CONSTRUCTOR, // 标记注解可以用在构造方法上

    /** Local variable declaration */
    LOCAL_VARIABLE, // 标记注解可以用在局部变量上

    /** Annotation type declaration */
    ANNOTATION_TYPE, // 标记注解可以用在注解上

    /** Package declaration */
    PACKAGE, // 标记注解可以用在包声明上, 此注解用的比较少, 主流IDE不支持可能会报错, 注解信息还要包含在package-info.java中, 不推荐使用.

    /**
     * Type parameter declaration
     *
     * @since 1.8
     */
    TYPE_PARAMETER, // 标记注解可以用在泛型参数上

    /**
     * Use of a type
     *
     * @since 1.8
     */
    TYPE_USE // 标记注解可以用在任意类型上, 包含接口, 类, 泛型参数, 局部变量, 形参, 成员变量, 返回类型上等
}
