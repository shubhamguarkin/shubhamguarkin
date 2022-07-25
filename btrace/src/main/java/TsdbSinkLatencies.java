import org.openjdk.btrace.core.annotations.BTrace;
import org.openjdk.btrace.core.annotations.OnMethod;
import org.openjdk.btrace.core.annotations.ProbeClassName;
import org.openjdk.btrace.core.annotations.ProbeMethodName;
import org.openjdk.btrace.core.annotations.Self;
import org.openjdk.btrace.core.types.AnyType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import static org.openjdk.btrace.core.BTraceUtils.field;
import static org.openjdk.btrace.core.BTraceUtils.get;
import static org.openjdk.btrace.core.BTraceUtils.print;

@BTrace(trusted = true)
public class TsdbSinkLatencies {
    private static final String PRIMARY_SINK_CLASS_NAME = "PrimarySink";

    @OnMethod(clazz = "com.vnera.analytics.engine.sink.TSDBSink", method = "notifyCheckpointComplete")
    public static void tsdbSinkLatencies(@Self Object sink,
                                         @ProbeClassName String probeClass,
                                         @ProbeMethodName String probeMethod,
                                         AnyType[] args) throws Exception {
        final Class<?> sinkClass = sink.getClass();
        final AnyType checkPointId = args[0];
        if (sinkClass.getSimpleName().equals(PRIMARY_SINK_CLASS_NAME)) {
//            printSinkLatencies(sinkClass.getSuperclass(), sink, checkPointId);
        } else {
            printSinkLatencies(sinkClass, sink, checkPointId);
        }
    }

    @OnMethod(clazz = "com.vnera.grid.core.StreamTaskStatsReporter", method = "processGridStats")
    public static void processGridStats(@Self Object reporter,
                                        @ProbeClassName String probeClass,
                                        @ProbeMethodName String probeMethod,
                                        AnyType[] args) {
        final long epoch = System.currentTimeMillis();
        final String topic = (String) getValue(reporter, "topic");
        print(String.format("%s(%d):%s:%s:%s",
                            toHumanReadableDate(epoch),
                            epoch,
                            probeMethod,
                            topic,
                            Arrays.toString(args)));
    }

//    @OnMethod(clazz = "com.vnera.grid.core.StreamTaskStatsReporter", method = "getUsedTime")
    public static void getUsedTime(@Self Object reporter,
                                   @ProbeClassName String probeClass,
                                   @ProbeMethodName String probeMethod,
                                   AnyType[] args) {
        final long epoch = System.currentTimeMillis();
        String callerMethod = callerMethod(4);
        print(String.format("%s(%d):%s->%s:%s",
                            toHumanReadableDate(epoch),
                            epoch,
                            callerMethod,
                            probeMethod,
                            Arrays.toString(args)));
    }

    public static void printSinkLatencies(final Class _class,
                                          final Object sink,
                                          final Object checkPointId) throws Exception {
        final long epoch = System.currentTimeMillis();
        final int subtaskIdx = (Integer) getValue(_class, sink, "subtaskIdx");
        final long millisSpentInProcessing = ((AtomicLong) getValue(_class, sink, "millisSpentInProcessing")).get();
        final long millisSpentInWindow = ((AtomicLong) getValue(_class, sink, "millisSpentInWindow")).get();

        final String taskNme = (String) invokeMethod(sink, "getRuntimeContext.getTaskName");
        final double printed = (Double) invokeSingleMethod(sink, "print", new Class[]{String.class}, new Object[]{
                "yummy"});
//        print(printed);
        final Object tmpMap = getValue(_class, sink, "tmpMap");
        print(tmpMap.getClass().getSimpleName());
        final Object contaisKey = invokeSingleMethod(tmpMap, "containsKey", new Class[]{Object.class},
                                                     new Object[]{1});
        print(contaisKey);
        /*print(String.format("%s(%d): checkpoint=%s, Task %s subtaskId %d has millisSpentInProcessing=%d, "
                                    + "millisSpentInWindow=%d",
                            toHumanReadableDate(epoch),
                            epoch,
                            checkPointId,
                            taskNme,
                            subtaskIdx,
                            millisSpentInProcessing,
                            millisSpentInWindow));*/
    }

    //    @OnMethod(clazz = "com.vnera.analytics.engine.sink.TSDBSink", method = "notifyCheckpointComplete")
    /*public static void printLatencies(@Self Object tsdbSink,
                                      @ProbeClassName String probeClass,
                                      @ProbeMethodName String probeMethod,
                                      AnyType[] args) throws Exception {
        final int subtaskIdx = (int) getValue(tsdbSink, "subtaskIdx");
        final long millisSpentInProcessing = ((AtomicLong) getValue(tsdbSink, "millisSpentInProcessing")).get();
        final long millisSpentInWindow = ((AtomicLong) getValue(tsdbSink, "millisSpentInWindow")).get();
        final Object runtimeContext = getValue(tsdbSink.getClass().getSuperclass().getSuperclass().getName(),
                                               tsdbSink,
                                               "runtimeContext");
        final Object taskInfo = getValue(runtimeContext.getClass().getSuperclass().getName(),
                                         runtimeContext,
                                         "taskInfo");
        final String taskName = (String) getValue(taskInfo, "taskName");
        print(String.format("Task %s subtaskId %d has millisSpentInProcessing=%d, millisSpentInWindow=%d%n",
                            taskName,
                            subtaskIdx,
                            millisSpentInProcessing,
                            millisSpentInWindow));
    }*/

    // Utility Methods

    private static String callerMethod(final int depth) {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[depth];
        return e.getMethodName();
    }

    private static String toHumanReadableDate(final long epoch) {
        final String datePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
        return sdf.format(new Date(epoch));
    }

    private static Object getValue(final Class _class, final Object object, final String fieldName) {
        final Field field = field(_class.getName(), fieldName);
        return get(field, object);
    }

    private static Object invokeSingleMethod(final Object object,
                                             final String methodName,
                                             Class<?>[] parameterTypes,
                                             Object[] params) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        final Method method = object.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(object, params);
    }

    private static Object invokeMethod(final Object object,
                                       final String methodChain) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Object next = object;
        String[] methodNames = methodChain.split("\\.");
        for (final String methodName : methodNames) {
            final Method method = next.getClass().getMethod(methodName);
            next = method.invoke(next);
        }
        return next;
    }

    private static Object getValue(Object obj, String path) {
        Object next = obj;
        String[] fieldNames = path.split("\\.");
        for (final String fieldName : fieldNames) {
            Field f = field(next.getClass().getName(), fieldName);
            next = get(f, next);
        }
        return next;
    }
}
