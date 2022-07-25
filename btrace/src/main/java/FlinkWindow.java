import org.openjdk.btrace.core.annotations.BTrace;
import org.openjdk.btrace.core.annotations.Kind;
import org.openjdk.btrace.core.annotations.Location;
import org.openjdk.btrace.core.annotations.OnMethod;
import org.openjdk.btrace.core.annotations.Return;
import org.openjdk.btrace.core.annotations.Where;
import org.openjdk.btrace.core.types.AnyType;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import static org.openjdk.btrace.core.BTraceUtils.field;
import static org.openjdk.btrace.core.BTraceUtils.get;
import static org.openjdk.btrace.core.BTraceUtils.jstack;
import static org.openjdk.btrace.core.BTraceUtils.println;

@BTrace(trusted = true)
public class FlinkWindow {
    private static final long startTime = System.currentTimeMillis();
    static {
        println("Current Time = " + startTime);
    }

    @OnMethod(
            clazz="com.vnera.analytics.engine.DerivedMetricCreator$MetricAggregateFunction",
            method="add"
    )
    public static void addDM(AnyType[] args) {
        Object task = args[0];
        String mk = getValue(task, "entityMetricDerivation.derivedEntity.key").toString();
        String eventTime = getValue(task, "metricPoint.timeInstant").toString();
        String value = getValue(task, "metricPoint.value").toString();
        String res = getValue(task, "metricPoint.resolutionSecs").toString();
        String metricName = getValue(task, "entityMetricDerivation.derivedMetric").toString();

        println("DMIN" + "," + System.currentTimeMillis() + "," + mk + "," + metricName + "," + eventTime + "," + res + "," + value);
    }

    @OnMethod(
            clazz="com.vnera.analytics.engine.DerivedMetricCreator$MetricProcessWindowFunction",
            method="process",
            location = @Location(Kind.RETURN)
    )
    public static void printDMLog(AnyType[] args) {
        Iterable it = (Iterable) args[2];
        if (it != null && it.iterator().hasNext()) {
            Object gm = it.iterator().next();
            String metricName = getValue(gm, "metricName.value").toString();
            String eventTime = getValue(gm, "metricPoint.timeInstant").toString();
            String value = getValue(gm, "metricPoint.value").toString();
            String mk = getValue(gm, "entityKey.key").toString();
            String res = getValue(gm, "metricPoint.resolutionSecs").toString();

            println("DMOUT" + "," + System.currentTimeMillis() + "," + mk + "," + metricName + "," + eventTime + "," + res + "," + value);
        }
    }

    @OnMethod(
            clazz="com.vnera.analytics.engine.MetricComputer$CollectAggregateFunction",
            method="add"
    )
    public static void printMCAdd(AnyType[] args) {
        Object gm = args[0];

        String metricName = getValue(gm, "metricName.value").toString();
        String eventTime = getValue(gm, "metricPoint.timeInstant").toString();
        String value = getValue(gm, "metricPoint.value").toString();
        String mk = getValue(gm, "entityKey.key").toString();
        String res = getValue(gm, "metricPoint.resolutionSecs").toString();

        println("MCADD" + "," + System.currentTimeMillis() + "," + mk + "," + metricName + "," + eventTime + "," + res + "," + value);
    }

    @OnMethod(
            clazz="com.vnera.analytics.engine.MetricComputer$ComputeMetricsFunction",
            method="process"
    )
    public static void printMCLog(AnyType[] args) {
        Iterable it = (Iterable) args[2];
        Iterator iter = it.iterator();

        while (iter.hasNext()) {
            List gms = (List) iter.next();
            for (Object gm : gms) {
                String metricName = getValue(gm, "metricName.value").toString();
                String eventTime = getValue(gm, "metricPoint.timeInstant").toString();
                String value = getValue(gm, "metricPoint.value").toString();
                String mk = getValue(gm, "entityKey.key").toString();
                String res = getValue(gm, "metricPoint.resolutionSecs").toString();

                println("MCIN" + "," + System.currentTimeMillis() + "," + mk + "," + metricName + "," + eventTime + "," + res + "," + value);
            }
        }
    }

    @OnMethod(
            clazz="com.vnera.analytics.engine.MetricComputer$ComputeMetricsFunction",
            method="process",
            location = @Location(
                    value = Kind.CALL,
                    clazz = "com.vnera.metricseval.SingleEntityMetricComputer",
                    method = "getAllMetricPoints",
                    where = Where.AFTER)
    )
    public static void log2(@Return List result) {
        for (Object triple : result) {
            String mk = getValue(triple, "first").toString();
            String metricName = getValue(triple, "second").toString();
            String eventTime = getValue(triple, "third.timeInstant").toString();
            String value = getValue(triple, "third.value").toString();
            String res = getValue(triple, "third.resolutionSecs").toString();

            println("MCOUT" + "," + System.currentTimeMillis() + "," + mk + "," + metricName + "," + eventTime + "," + res + "," + value);
        }
    }

    private static Object getValue(Object obj, String path) {
        Object next = obj;
        String[] fieldNames = path.split("\\.");
        for (String fieldName : fieldNames) {
            Field f = field(next.getClass().getName(), fieldName);
            next = get(f, next);
        }
        return next;
    }
}