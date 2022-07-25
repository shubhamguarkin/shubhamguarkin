import com.vnera.analytics.engine.TaskManagerState;
import com.vnera.analytics.engine.configuration.StoreOptions;
import com.vnera.analytics.engine.datamodel.GenericMetric;
import com.vnera.analytics.engine.sink.PrimarySink;
import com.vnera.analytics.engine.sink.TSDBSink;
import com.vnera.common.Clock;
import com.vnera.common.TsdbSinkStats;
import com.vnera.grid.core.FlinkSinkName;
import com.vnera.grid.core.StreamTaskStatsReporter;
import com.vnera.grid.utils.KafkaTopic;
import com.vnera.model.core.system.ModelKey;
import com.vnera.model.protobufs.MetricName;
import com.vnera.model.protobufs.ObjectType;
import com.vnera.plan2.ArkinGroupCacheFactory;
import com.vnera.storage.config.ConfigStoreFactory;
import com.vnera.storage.metrics.MetricStore;
import com.vnera.storage.metrics.MetricStoreFactory;
import com.vnera.storage.utils.TestStorageUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.operators.testutils.MockEnvironment;
import org.apache.flink.runtime.operators.testutils.MockEnvironmentBuilder;
import org.apache.flink.runtime.operators.testutils.MockInputSplitProvider;
import org.apache.flink.streaming.api.operators.StreamSink;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static com.vnera.model.core.system.ModelConstants.TEST_CUSTOMER_ID;
import static org.mockito.ArgumentMatchers.any;

public class TsdbSinkLatenciesTest {
    private static final long ELEMENT_TS = 10;
    private static final int SUBTASK_INDEX = 0;
    private OneInputStreamOperatorTestHarness<GenericMetric, Object> harness;
    private StreamTaskStatsReporter reporter;

    @BeforeClass
    public static void beforeClass() throws Exception {
        TestStorageUtils.beforeClass(TsdbSinkLatenciesTest.class.getCanonicalName(), false, false, null);
        Clock.frozenClockNow.set(100_000L);
    }

    @Before
    public void setup() throws Exception {
        final StreamSink<GenericMetric> sinkOperator = new StreamSink<>(new PrimarySink(FlinkSinkName.RAW_METRIC_SINK));
        reporter = new StreamTaskStatsReporter(TestStorageUtils.getConfigStore(),
                                               "GenericStreamTask",
                                               KafkaTopic.TOPIC3.getName());
        harness = createHarness(sinkOperator, "MockTest");
    }

    @Test
    public void btrace() throws Exception {
        final StreamSink<GenericMetric> sink = new StreamSink<>(new TSDBSink(FlinkSinkName.DERIVED_METRIC_SINK));
        final OneInputStreamOperatorTestHarness<GenericMetric, Object> harness2 = createHarness(sink, "PrimarySinnk");
        long checkpointId = 1;
        while (true) {
            harnessProcessing(harness2, checkpointId);
            processGridStats(checkpointId);
//            getUsedTimeTest(checkpointId);
            System.out.printf("Checkpoint %d%n", checkpointId++);
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        }
    }

    @After
    public void after() throws Exception {
        harness.close();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        Clock.frozenClockNow.set(0L);
        ArkinGroupCacheFactory.cancel();
        TestStorageUtils.afterClass();
    }

    private static OneInputStreamOperatorTestHarness<GenericMetric, Object> createHarness(final StreamSink<GenericMetric> sinkOperator,
                                                                                          final String taskName) throws Exception {
        final MockEnvironment environment = (new MockEnvironmentBuilder()).setTaskName(taskName)
                                                                          .setManagedMemorySize(3145728L)
                                                                          .setInputSplitProvider(new MockInputSplitProvider())
                                                                          .setBufferSize(1024)
                                                                          .setMaxParallelism(1)
                                                                          .setParallelism(1)
                                                                          .setSubtaskIndex(SUBTASK_INDEX)
                                                                          .build();

        final OneInputStreamOperatorTestHarness<GenericMetric, Object> harness =
                new OneInputStreamOperatorTestHarness<>(
                sinkOperator,
                environment);
        harness.setup();
        harness.getExecutionConfig().setGlobalJobParameters(getConfig());
        try (MockedStatic<TaskManagerState> mock = Mockito.mockStatic(TaskManagerState.class,
                                                                      Mockito.CALLS_REAL_METHODS)) {
            mock.when(() -> TaskManagerState.initUploadHandler(any())).thenAnswer(inv -> null);
            harness.open();
        }
        return harness;
    }


    private void writeGenericMetric() throws Exception {
        final Iterable<GenericMetric> gms = getGenericMetrics();
        for (final GenericMetric gm : gms) {
            harness.processElement(gm, ELEMENT_TS);
        }
    }

    private Iterable<GenericMetric> getGenericMetrics() {
        return LongStream.range(1, 100001)
                         .mapToObj(oid -> ModelKey.create(TEST_CUSTOMER_ID, ObjectType.FLOW_INFO_VALUE, oid))
                         .map(this::createGenericMetric)
                         .collect(Collectors.toList());
    }

    private static Configuration getConfig() {
        final Configuration config = new Configuration();
        config.set(StoreOptions.CONFIG_STORE_TYPE, ConfigStoreFactory.StoreType.MEMORY.name());
        config.set(StoreOptions.METRIC_STORE_TYPE, MetricStoreFactory.StoreType.MEMORY.name());
        return config;
    }

    private GenericMetric createGenericMetric(final ModelKey flowMK) {
        return new GenericMetric(flowMK,
                                 MetricName.flow_srcBytes_delta_summation_bytes,
                                 new MetricStore.MetricPoint(30000000, 100, 300));
    }

    private void processGridStats(final long checkpointId) {
        final TsdbSinkStats stats = TsdbSinkStats.newBuilder()
                                                 .setMillisSpentInProcessing(checkpointId * 1_000)
                                                 .setMillisSpentInWindow(checkpointId * 3_000)
                                                 .build();
        reporter.processGridStats("someJob", 0, new AtomicLong(), new AtomicLong(), new AtomicLong(), stats);
    }

    private void getUsedTimeTest(final long checkpointId) {
        final TsdbSinkStats stats = TsdbSinkStats.newBuilder()
                                                 .setMillisSpentInProcessing(checkpointId * 2_000)
                                                 .setMillisSpentInWindow(checkpointId * 6_000)
                                                 .build();
        throw new RuntimeException(
                "StreamTaskStatsReporter.getUsedTime() is not accessible. PLease modify its access" + " specifier");
//        StreamTaskStatsReporter.getUsedTime(new AtomicLong(), new AtomicLong(), new AtomicLong(), stats);
    }

    private void harnessProcessing(final OneInputStreamOperatorTestHarness<GenericMetric, Object> harness2,
                                   final long checkpointId) throws Exception {
        writeGenericMetric();
        final Iterable<GenericMetric> gms = getGenericMetrics();
        for (final GenericMetric gm : gms) {
            harness2.processElement(gm, ELEMENT_TS);
        }
        harness.snapshot(checkpointId, ELEMENT_TS + 1);
        harness2.snapshot(checkpointId, ELEMENT_TS + 1);
        harness.notifyOfCompletedCheckpoint(checkpointId);
        harness2.notifyOfCompletedCheckpoint(checkpointId);
    }
}

