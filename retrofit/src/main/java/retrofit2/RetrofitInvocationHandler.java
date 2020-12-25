package retrofit2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

/**
 * @author zhangpan
 * @date 2020/12/24
 */
public class RetrofitInvocationHandler<T> implements InvocationHandler {
    private final Platform platform = Platform.get();
    private final Object[] emptyArgs = new Object[0];
    private final Map<Method, ServiceMethod<?>> serviceMethodCache = new ConcurrentHashMap<>();
    private final Class<T> service;
    private final Retrofit retrofit;

    public RetrofitInvocationHandler(Retrofit retrofit, Class<T> service) {
        this.service = service;
        this.retrofit = retrofit;
    }

    @Override
    public @Nullable
    Object invoke(Object proxy, Method method, @Nullable Object[] args)
            throws Throwable {
        // If the method is a method from Object then defer to normal invocation.
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        args = args != null ? args : emptyArgs;
        return platform.isDefaultMethod(method)
                ? platform.invokeDefaultMethod(method, service, proxy, args)
                : loadServiceMethod(retrofit, method).invoke(args);
    }

    ServiceMethod<?> loadServiceMethod(Retrofit retrofit, Method method) {
        ServiceMethod<?> result = serviceMethodCache.get(method);
        if (result != null) return result;

        synchronized (serviceMethodCache) {
            result = serviceMethodCache.get(method);
            if (result == null) {
                result = ServiceMethod.parseAnnotations(retrofit, method);// 解析方法以及注解信息，并将这些信息封装成ServiceMethod
                serviceMethodCache.put(method, result);// 缓存ServiceMethod
            }
        }
        return result;
    }
}
