import org.openjdk.btrace.core.annotations.BTrace;
import org.openjdk.btrace.core.annotations.OnMethod;
import org.openjdk.btrace.core.annotations.ProbeClassName;
import org.openjdk.btrace.core.annotations.ProbeMethodName;
import org.openjdk.btrace.core.annotations.Self;
import org.openjdk.btrace.core.types.AnyType;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static org.openjdk.btrace.core.BTraceUtils.field;
import static org.openjdk.btrace.core.BTraceUtils.get;
import static org.openjdk.btrace.core.BTraceUtils.print;

@BTrace(trusted = true)
public class FlinkDebugger {
    static boolean hasBlockedYet = false;

    @OnMethod(clazz = "com.vnera.analytics.engine.sink.TSDBSink", method = "invoke")
    public static void blockSubTaskId(@Self Object tsdbSink,
                                      @ProbeClassName String probeClass,
                                      @ProbeMethodName String probeMethod,
                                      AnyType[] args) throws InterruptedException {
        if (hasBlockedYet) {
            return;
        }
        final String subtaskIdxFieldName = "subtaskIdx";
        final Field subtaskIdxField = field(probeClass, subtaskIdxFieldName);
        final int subtaskIdx = (Integer) get(subtaskIdxField, tsdbSink);
        final int subtaskIdToBlock = 20;
        if (subtaskIdx == subtaskIdToBlock) {
            final long sleepTimeoutMillis = TimeUnit.MINUTES.toMillis(10);
            print(String.format("Blocking subtask id %d for %d millis", subtaskIdx, sleepTimeoutMillis));
            Thread.sleep(sleepTimeoutMillis);
            print(String.format("Releasing subtask id %d after %d millis", subtaskIdx, sleepTimeoutMillis));
            hasBlockedYet = true;
        }
    }

    /*@OnMethod(clazz = "com.vnera.analytics.map.metrics.VrniFlinkDatadogReporter", method = "notifyOfAddedMetric")
    public static void addedFlinkMetrics(@Self Object reporter,
                                         @ProbeClassName String probeClass,
                                         @ProbeMethodName String probeMethod,
                                         AnyType[] args) throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        final String metricGrpClassName = "org.apache.flink.metrics.MetricGroup";
        final Class<?> metricGroupClass = BTraceUtils.Reflective.classForName(metricGrpClassName);
        final String getMetricIdentifierMethodName = "getMetricIdentifier";
        final Method getMetricIdentifierMethod = metricGroupClass.getMethod(getMetricIdentifierMethodName,
                                                                            String.class);

        final String metricNameArg = str(args[1]);
        final Object metricGroup = args[2];
        final String fullName = (String) getMetricIdentifierMethod.invoke(metricGroup, metricNameArg);
        print(String.format("Got metric name %s", fullName));
    }*/

/*    private static void printValue(Object obj, String path) {
         print(path);
        print(getValue(obj, path));
    }

        private static Object getValue(Object obj, String path) {
        Object next = obj;
        String[] fieldNames = path.split("\\.");
        for (String fieldName : fieldNames) {
            Field f = field(next.getClass().getName(), fieldName);
            next = get(f, next);
        }
        return next;
    }*/
}
