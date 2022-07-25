import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.openjdk.btrace.core.BTraceUtils.field;
import static org.openjdk.btrace.core.BTraceUtils.get;

/**
 * Created by Shubham Gupta
 * on 24 Jul 2022.
 */
public class Utils {
    static String callerMethod(final int depth) {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[depth];
        return e.getMethodName();
    }

    static String toHumanReadableDate(final long epoch) {
        final String datePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        return sdf.format(new Date(epoch));
    }

    static Object getValue(final Class _class, final Object object, final String fieldName) {
        final Field field = field(_class.getName(), fieldName);
        return get(field, object);
    }

    static Object invokeWithParams(final Object object,
                                   final String methodName,
                                   Class<?>[] parameterTypes,
                                   Object[] params) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method method = object.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(object, params);
    }

    static Object invoke(final Object object,
                         final String methodChain) throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        Object next = object;
        String[] methodNames = methodChain.split("\\.");
        for (final String methodName : methodNames) {
            final Method method = next.getClass().getMethod(methodName);
            next = method.invoke(next);
        }
        return next;
    }

    static Object getValue(Object obj, String path) {
        Object next = obj;
        String[] fieldNames = path.split("\\.");
        for (final String fieldName : fieldNames) {
            Field f = field(next.getClass().getName(), fieldName);
            next = get(f, next);
        }
        return next;
    }
}
