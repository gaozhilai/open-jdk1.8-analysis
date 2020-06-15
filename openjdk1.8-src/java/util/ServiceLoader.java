/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.AccessController;
import java.security.AccessControlContext;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * A simple service-provider loading facility.
 *
 * <p> A <i>service</i> is a well-known set of interfaces and (usually
 * abstract) classes.  A <i>service provider</i> is a specific implementation
 * of a service.  The classes in a provider typically implement the interfaces
 * and subclass the classes defined in the service itself.  Service providers
 * can be installed in an implementation of the Java platform in the form of
 * extensions, that is, jar files placed into any of the usual extension
 * directories.  Providers can also be made available by adding them to the
 * application's class path or by some other platform-specific means.
 *
 * <p> For the purpose of loading, a service is represented by a single type,
 * that is, a single interface or abstract class.  (A concrete class can be
 * used, but this is not recommended.)  A provider of a given service contains
 * one or more concrete classes that extend this <i>service type</i> with data
 * and code specific to the provider.  The <i>provider class</i> is typically
 * not the entire provider itself but rather a proxy which contains enough
 * information to decide whether the provider is able to satisfy a particular
 * request together with code that can create the actual provider on demand.
 * The details of provider classes tend to be highly service-specific; no
 * single class or interface could possibly unify them, so no such type is
 * defined here.  The only requirement enforced by this facility is that
 * provider classes must have a zero-argument constructor so that they can be
 * instantiated during loading.
 *
 * <p><a name="format"> A service provider is identified by placing a
 * <i>provider-configuration file</i> in the resource directory
 * <tt>META-INF/services</tt>.</a>  The file's name is the fully-qualified <a
 * href="../lang/ClassLoader.html#name">binary name</a> of the service's type.
 * The file contains a list of fully-qualified binary names of concrete
 * provider classes, one per line.  Space and tab characters surrounding each
 * name, as well as blank lines, are ignored.  The comment character is
 * <tt>'#'</tt> (<tt>'&#92;u0023'</tt>,
 * <font style="font-size:smaller;">NUMBER SIGN</font>); on
 * each line all characters following the first comment character are ignored.
 * The file must be encoded in UTF-8.
 *
 * <p> If a particular concrete provider class is named in more than one
 * configuration file, or is named in the same configuration file more than
 * once, then the duplicates are ignored.  The configuration file naming a
 * particular provider need not be in the same jar file or other distribution
 * unit as the provider itself.  The provider must be accessible from the same
 * class loader that was initially queried to locate the configuration file;
 * note that this is not necessarily the class loader from which the file was
 * actually loaded.
 *
 * <p> Providers are located and instantiated lazily, that is, on demand.  A
 * service loader maintains a cache of the providers that have been loaded so
 * far.  Each invocation of the {@link #iterator iterator} method returns an
 * iterator that first yields all of the elements of the cache, in
 * instantiation order, and then lazily locates and instantiates any remaining
 * providers, adding each one to the cache in turn.  The cache can be cleared
 * via the {@link #reload reload} method.
 *
 * <p> Service loaders always execute in the security context of the caller.
 * Trusted system code should typically invoke the methods in this class, and
 * the methods of the iterators which they return, from within a privileged
 * security context.
 *
 * <p> Instances of this class are not safe for use by multiple concurrent
 * threads.
 *
 * <p> Unless otherwise specified, passing a <tt>null</tt> argument to any
 * method in this class will cause a {@link NullPointerException} to be thrown.
 *
 *
 * <p><span style="font-weight: bold; padding-right: 1em">Example</span>
 * Suppose we have a service type <tt>com.example.CodecSet</tt> which is
 * intended to represent sets of encoder/decoder pairs for some protocol.  In
 * this case it is an abstract class with two abstract methods:
 *
 * <blockquote><pre>
 * public abstract Encoder getEncoder(String encodingName);
 * public abstract Decoder getDecoder(String encodingName);</pre></blockquote>
 *
 * Each method returns an appropriate object or <tt>null</tt> if the provider
 * does not support the given encoding.  Typical providers support more than
 * one encoding.
 *
 * <p> If <tt>com.example.impl.StandardCodecs</tt> is an implementation of the
 * <tt>CodecSet</tt> service then its jar file also contains a file named
 *
 * <blockquote><pre>
 * META-INF/services/com.example.CodecSet</pre></blockquote>
 *
 * <p> This file contains the single line:
 *
 * <blockquote><pre>
 * com.example.impl.StandardCodecs    # Standard codecs</pre></blockquote>
 *
 * <p> The <tt>CodecSet</tt> class creates and saves a single service instance
 * at initialization:
 *
 * <blockquote><pre>
 * private static ServiceLoader&lt;CodecSet&gt; codecSetLoader
 *     = ServiceLoader.load(CodecSet.class);</pre></blockquote>
 *
 * <p> To locate an encoder for a given encoding name it defines a static
 * factory method which iterates through the known and available providers,
 * returning only when it has located a suitable encoder or has run out of
 * providers.
 *
 * <blockquote><pre>
 * public static Encoder getEncoder(String encodingName) {
 *     for (CodecSet cp : codecSetLoader) {
 *         Encoder enc = cp.getEncoder(encodingName);
 *         if (enc != null)
 *             return enc;
 *     }
 *     return null;
 * }</pre></blockquote>
 *
 * <p> A <tt>getDecoder</tt> method is defined similarly.
 *
 *
 * <p><span style="font-weight: bold; padding-right: 1em">Usage Note</span> If
 * the class path of a class loader that is used for provider loading includes
 * remote network URLs then those URLs will be dereferenced in the process of
 * searching for provider-configuration files.
 *
 * <p> This activity is normal, although it may cause puzzling entries to be
 * created in web-server logs.  If a web server is not configured correctly,
 * however, then this activity may cause the provider-loading algorithm to fail
 * spuriously.
 *
 * <p> A web server should return an HTTP 404 (Not Found) response when a
 * requested resource does not exist.  Sometimes, however, web servers are
 * erroneously configured to return an HTTP 200 (OK) response along with a
 * helpful HTML error page in such cases.  This will cause a {@link
 * ServiceConfigurationError} to be thrown when this class attempts to parse
 * the HTML page as a provider-configuration file.  The best solution to this
 * problem is to fix the misconfigured web server to return the correct
 * response code (HTTP 404) along with the HTML error page.
 *
 * @param  <S>
 *         The type of the service to be loaded by this loader
 *
 * @author Mark Reinhold
 * @since 1.6
 */
 // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/6/12 11:02
public final class ServiceLoader<S>
    implements Iterable<S> // ServiceLoader包含的元素是可迭代的
{
    // SPI实现类信息配置文件所在目录
    private static final String PREFIX = "META-INF/services/";

    // The class or interface representing the service being loaded 保存已经加载的服务定义接口/类具体实现类
    private final Class<S> service;

    // The class loader used to locate, load, and instantiate providers 如果没有指定类加载器默认是系统加载器AppClassLoader, 用于服务实现类的寻找和加载
    private final ClassLoader loader;

    // The access control context taken when the ServiceLoader is created 访问控制上下文
    private final AccessControlContext acc;

    // Cached providers, in instantiation order 当前ServiceLoader实例中已经加载的服务提供类(懒加载迭代器调用过nextService方法返回的类)
    private LinkedHashMap<String,S> providers = new LinkedHashMap<>();

    // The current lazy-lookup iterator 懒加载迭代器, 包含了所有的service的SPI机制的实现类, 只有调用相关方法, 此迭代器才会实际加载相应实现类
    private LazyIterator lookupIterator;

    /**
     * Clear this loader's provider cache so that all providers will be
     * reloaded.
     *
     * <p> After invoking this method, subsequent invocations of the {@link
     * #iterator() iterator} method will lazily look up and instantiate
     * providers from scratch, just as is done by a newly-created loader.
     *
     * <p> This method is intended for use in situations in which new providers
     * can be installed into a running Java virtual machine.
     */ // 清空并重新加载当前ServiceLoader实例指定的ServiceProviderInterface对应的实现类, 此方法存在是为了让运行中的虚拟机重新加载实现类
    public void reload() {
        providers.clear(); // 清空当前保存的加载过的服务实现类
        lookupIterator = new LazyIterator(service, loader); // 创建新的包含所有实现类的懒加载迭代器
    }
    // 私有构造器, 只有通过ServiceLoader提供的工具方法间接调用此方法才能构建ServiceLoader的实例
    private ServiceLoader(Class<S> svc, ClassLoader cl) {
        service = Objects.requireNonNull(svc, "Service interface cannot be null"); // 暂存校验指定的Service Interface不能为空
        loader = (cl == null) ? ClassLoader.getSystemClassLoader() : cl; // 暂存类加载器, 如果没有直接指定使用默认的AppClassLoader
        acc = (System.getSecurityManager() != null) ? AccessController.getContext() : null; // 暂存访问控制上下文
        reload(); // 加载服务实现类
    }
    // 根据service和消息以及具体异常构建异常并抛出
    private static void fail(Class<?> service, String msg, Throwable cause)
        throws ServiceConfigurationError
    {
        throw new ServiceConfigurationError(service.getName() + ": " + msg,
                                            cause);
    }
    // 根据service和异常信息构建异常并抛出
    private static void fail(Class<?> service, String msg)
        throws ServiceConfigurationError
    {
        throw new ServiceConfigurationError(service.getName() + ": " + msg);
    }
    // 根据异常信息以及url和行数异常信息构造异常并抛出
    private static void fail(Class<?> service, URL u, int line, String msg)
        throws ServiceConfigurationError
    {
        fail(service, u + ":" + line + ": " + msg);
    }

    // Parse a single line from the given configuration file, adding the name
    // on the line to the names list.
    // 解析给定的SPI配置文件一行内容, 将此行配置的name放到name列表中
    private int parseLine(Class<?> service, URL u, BufferedReader r, int lc,
                          List<String> names)
        throws IOException, ServiceConfigurationError
    {
        String ln = r.readLine(); // 从配置文件中读取一行内容
        if (ln == null) { // 如果内容为空
            return -1; // 返回-1
        }
        int ci = ln.indexOf('#'); // 如果配置里带着#号, 代表配置的行是具体实现类的方法
        if (ci >= 0) ln = ln.substring(0, ci); // 忽略方法名, 只保留到实现类全路径
        ln = ln.trim(); // 去除字符串两端空格
        int n = ln.length();
        if (n != 0) { // 如果配置内容长度不等于0, 即有效配置
            if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0)) // 配置内容里不能有空格或者制表符
                fail(service, u, lc, "Illegal configuration-file syntax");
            int cp = ln.codePointAt(0); // 获得当前行配置信息第0个字符, 在当前编码表中此字符对应的数字
            if (!Character.isJavaIdentifierStart(cp)) // 判断配置的实现类是否是java标识符开头
                fail(service, u, lc, "Illegal provider-class name: " + ln);
            for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) { // 依旧是检测配置的类名是否合法
                cp = ln.codePointAt(i);
                if (!Character.isJavaIdentifierPart(cp) && (cp != '.'))
                    fail(service, u, lc, "Illegal provider-class name: " + ln);
            }
            if (!providers.containsKey(ln) && !names.contains(ln)) // 当前providers字段中不存在此行配置的类名, 并且name列表中也不存在时
                names.add(ln); // 将名字加入name列表
        }
        return lc + 1; // 返回下一次解析的行号
    }

    // Parse the content of the given URL as a provider-configuration file.
    //
    // @param  service
    //         The service type for which providers are being sought;
    //         used to construct error detail strings
    //
    // @param  u
    //         The URL naming the configuration file to be parsed
    //
    // @return A (possibly empty) iterator that will yield the provider-class
    //         names in the given configuration file that are not yet members
    //         of the returned set
    //
    // @throws ServiceConfigurationError
    //         If an I/O error occurs while reading from the given URL, or
    //         if a configuration-file format error is detected
    // 解析配置文件, 将配置文件中配置的实现类全路径放入到name列表并返回其迭代器
    private Iterator<String> parse(Class<?> service, URL u)
        throws ServiceConfigurationError
    {
        InputStream in = null;
        BufferedReader r = null;
        ArrayList<String> names = new ArrayList<>();

        try {
            in = u.openStream(); // 从url指向的文件获取输入流
            r = new BufferedReader(new InputStreamReader(in, "utf-8")); // 装饰成带缓冲的输入流
            int lc = 1; // 从第一行开始解析
            while ((lc = parseLine(service, u, r, lc, names)) >= 0); // 直到解析到配置文件末尾
        } catch (IOException x) {
            fail(service, "Error reading configuration file", x);
        } finally {
            try {
                if (r != null) r.close();
                if (in != null) in.close();
            } catch (IOException y) {
                fail(service, "Error closing configuration file", y);
            }
        }
        return names.iterator(); // 返回包含实现类全路径作为元素的迭代器
    }

    // Private inner class implementing fully-lazy provider lookup
    // ServiceLoader版本的懒加载迭代器定义
    private class LazyIterator
        implements Iterator<S>
    {

        Class<S> service; // 服务定义类
        ClassLoader loader; // 要使用的类加载器
        Enumeration<URL> configs = null; // 配置文件url, 根据ClassLoader的不同, 给定配置文件名可能返回多个文件url
        Iterator<String> pending = null; // 实现类全路径列表
        String nextName = null; // 下一次调用next方法要返回的实现类全路径

        private LazyIterator(Class<S> service, ClassLoader loader) {
            this.service = service;
            this.loader = loader;
        }
        // 是否有下一个实现类
        private boolean hasNextService() {
            if (nextName != null) { // 如果有下次调用next要返回的值
                return true; // 返回true
            }
            if (configs == null) { // 如果configs为null, 那么接下来初始化配置文件
                try {
                    String fullName = PREFIX + service.getName(); // 先获取约定格式的文件路径
                    if (loader == null) // 如果没有指定类加载器
                        configs = ClassLoader.getSystemResources(fullName); // 使用系统类加载器AppCLassLoader获取资源
                    else
                        configs = loader.getResources(fullName); // 否则使用给定的类加载器加载配置文件url
                } catch (IOException x) {
                    fail(service, "Error locating configuration files", x);
                }
            }
            while ((pending == null) || !pending.hasNext()) { // 实现类列表为空, 或者实现类列表不存在下一个元素
                if (!configs.hasMoreElements()) { // 配置文件url也没有下一个元素
                    return false; // 返回false
                }
                pending = parse(service, configs.nextElement()); // 否则根据配置文件url解析实现类列表
            }
            nextName = pending.next(); // 设置下一次调用next要返回的实现类名称内容
            return true; // 返回true
        }
        // 返回下一个实现类的实例
        private S nextService() {
            if (!hasNextService()) // 如果不存在下一个实现类
                throw new NoSuchElementException(); // 抛出异常
            String cn = nextName; // 否则暂存本次要返回实例的全路径类名
            nextName = null; // 将nextName置空
            Class<?> c = null;
            try {
                c = Class.forName(cn, false, loader); // 通过类全路径和给定的loader获得Class对象
            } catch (ClassNotFoundException x) {
                fail(service,
                     "Provider " + cn + " not found");
            }
            if (!service.isAssignableFrom(c)) { // 判断获得的Class对象的实例是否可以赋值为service, 即Class是否是service的子类
                fail(service,
                     "Provider " + cn  + " not a subtype"); // 如果不是抛出异常
            }
            try {
                S p = service.cast(c.newInstance()); // 将获得的实现类Class对象构造的实例转换成service类型暂存
                providers.put(cn, p); // 将实例以对应的实现类全路径为key放入providers字段
                return p; // 返回实例
            } catch (Throwable x) {
                fail(service,
                     "Provider " + cn + " could not be instantiated",
                     x);
            }
            throw new Error();          // This cannot happen
        }
        /** 见{@link Iterator#hasNext()} */
        public boolean hasNext() {
            if (acc == null) { // 如果没有访问控制上下文
                return hasNextService(); // 直接返回是否有下一个实现类
            } else {
                PrivilegedAction<Boolean> action = new PrivilegedAction<Boolean>() {
                    public Boolean run() { return hasNextService(); }
                };
                return AccessController.doPrivileged(action, acc); // 以特权的方式判断是否有下一个实现类
            }
        }
        /** 见{@link Iterator#next()} */
        public S next() {
            if (acc == null) { // 如果没有访问控制上下文
                return nextService(); // 返回下一个实现类实例
            } else {
                PrivilegedAction<S> action = new PrivilegedAction<S>() {
                    public S run() { return nextService(); }
                };
                return AccessController.doPrivileged(action, acc); // 以特权的方式返回下一个实现类实例
            }
        }
        /** 见{@link Iterator#remove()} */
        public void remove() {
            throw new UnsupportedOperationException(); // 不支持移除操作
        }

    }

    /**
     * Lazily loads the available providers of this loader's service.
     *
     * <p> The iterator returned by this method first yields all of the
     * elements of the provider cache, in instantiation order.  It then lazily
     * loads and instantiates any remaining providers, adding each one to the
     * cache in turn.
     *
     * <p> To achieve laziness the actual work of parsing the available
     * provider-configuration files and instantiating providers must be done by
     * the iterator itself.  Its {@link java.util.Iterator#hasNext hasNext} and
     * {@link java.util.Iterator#next next} methods can therefore throw a
     * {@link ServiceConfigurationError} if a provider-configuration file
     * violates the specified format, or if it names a provider class that
     * cannot be found and instantiated, or if the result of instantiating the
     * class is not assignable to the service type, or if any other kind of
     * exception or error is thrown as the next provider is located and
     * instantiated.  To write robust code it is only necessary to catch {@link
     * ServiceConfigurationError} when using a service iterator.
     *
     * <p> If such an error is thrown then subsequent invocations of the
     * iterator will make a best effort to locate and instantiate the next
     * available provider, but in general such recovery cannot be guaranteed.
     *
     * <blockquote style="font-size: smaller; line-height: 1.2"><span
     * style="padding-right: 1em; font-weight: bold">Design Note</span>
     * Throwing an error in these cases may seem extreme.  The rationale for
     * this behavior is that a malformed provider-configuration file, like a
     * malformed class file, indicates a serious problem with the way the Java
     * virtual machine is configured or is being used.  As such it is
     * preferable to throw an error rather than try to recover or, even worse,
     * fail silently.</blockquote>
     *
     * <p> The iterator returned by this method does not support removal.
     * Invoking its {@link java.util.Iterator#remove() remove} method will
     * cause an {@link UnsupportedOperationException} to be thrown.
     *
     * @implNote When adding providers to the cache, the {@link #iterator
     * Iterator} processes resources in the order that the {@link
     * java.lang.ClassLoader#getResources(java.lang.String)
     * ClassLoader.getResources(String)} method finds the service configuration
     * files.
     *
     * @return  An iterator that lazily loads providers for this loader's
     *          service
     */ /** 见{@link Iterable#iterator()} */
    public Iterator<S> iterator() {
        return new Iterator<S>() { // 返回当前ServiceLoader包含的实现类实例的迭代器

            Iterator<Map.Entry<String,S>> knownProviders
                = providers.entrySet().iterator(); // 先基于已知的provider获取迭代器
            /** 见{@link Iterator#hasNext()} */
            public boolean hasNext() {
                if (knownProviders.hasNext()) // 如果已知的provider存在下一个元素
                    return true; // 直接反会true
                return lookupIterator.hasNext(); // 否则通过懒加载迭代器寻找是否存在下一个元素
            }
            /** 见{@link Iterator#next()} */
            public S next() {
                if (knownProviders.hasNext())
                    return knownProviders.next().getValue(); // 同样先尝试从已知的provider获得下一个实现类实例
                return lookupIterator.next(); // 不存在则尝试通过懒加载迭代器获得
            }
            /** 见{@link Iterator#remove()} */
            public void remove() {
                throw new UnsupportedOperationException(); // 同样不支持移除操作
            }

        };
    }

    /**
     * Creates a new service loader for the given service type and class
     * loader.
     *
     * @param  <S> the class of the service type
     *
     * @param  service
     *         The interface or abstract class representing the service
     *
     * @param  loader
     *         The class loader to be used to load provider-configuration files
     *         and provider classes, or <tt>null</tt> if the system class
     *         loader (or, failing that, the bootstrap class loader) is to be
     *         used
     *
     * @return A new service loader
     */ // 返回一个使用指定的ClassLoader和包含指定service实现类的ServiceLoader实例
    public static <S> ServiceLoader<S> load(Class<S> service,
                                            ClassLoader loader)
    {
        return new ServiceLoader<>(service, loader);
    }

    /**
     * Creates a new service loader for the given service type, using the
     * current thread's {@linkplain java.lang.Thread#getContextClassLoader
     * context class loader}.
     *
     * <p> An invocation of this convenience method of the form
     *
     * <blockquote><pre>
     * ServiceLoader.load(<i>service</i>)</pre></blockquote>
     *
     * is equivalent to
     *
     * <blockquote><pre>
     * ServiceLoader.load(<i>service</i>,
     *                    Thread.currentThread().getContextClassLoader())</pre></blockquote>
     *
     * @param  <S> the class of the service type
     *
     * @param  service
     *         The interface or abstract class representing the service
     *
     * @return A new service loader
     */ // 返回一个使用当前线程的ThreadContextClassLoader和包含指定service实现类的ServiceLoader实例
    public static <S> ServiceLoader<S> load(Class<S> service) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return ServiceLoader.load(service, cl);
    }

    /**
     * Creates a new service loader for the given service type, using the
     * extension class loader.
     *
     * <p> This convenience method simply locates the extension class loader,
     * call it <tt><i>extClassLoader</i></tt>, and then returns
     *
     * <blockquote><pre>
     * ServiceLoader.load(<i>service</i>, <i>extClassLoader</i>)</pre></blockquote>
     *
     * <p> If the extension class loader cannot be found then the system class
     * loader is used; if there is no system class loader then the bootstrap
     * class loader is used.
     *
     * <p> This method is intended for use when only installed providers are
     * desired.  The resulting service will only find and load providers that
     * have been installed into the current Java virtual machine; providers on
     * the application's class path will be ignored.
     *
     * @param  <S> the class of the service type
     *
     * @param  service
     *         The interface or abstract class representing the service
     *
     * @return A new service loader
     */ //  // 返回一个使用拓展类加载器ExtClassLoader和包含指定service实现类的ServiceLoader实例
    public static <S> ServiceLoader<S> loadInstalled(Class<S> service) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        ClassLoader prev = null;
        while (cl != null) {
            prev = cl;
            cl = cl.getParent();
        }
        return ServiceLoader.load(service, prev);
    }

    /**
     * Returns a string describing this service.
     *
     * @return  A descriptive string
     */ // toSring方法默认展示包含服务定义类的名字
    public String toString() {
        return "java.util.ServiceLoader[" + service.getName() + "]";
    }

}
