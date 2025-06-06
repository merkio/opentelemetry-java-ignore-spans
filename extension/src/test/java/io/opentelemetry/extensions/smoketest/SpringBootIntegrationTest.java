package io.opentelemetry.extensions.smoketest;

import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import okhttp3.Request;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SpringBootIntegrationTest extends IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(SpringBootIntegrationTest.class);

    @Test
    public void extensionsAreLoadedFromJavaagent() throws IOException, InterruptedException {
        startTargetWithExtendedAgent();

        runGetRequest("/ping");
        runGetRequest("/actuator/health");
        runGetRequest("/actuator/metrics");
        runGetRequest("/health/readiness");
        runGetRequest("/client");
        runGetRequest("/actuator/info");

        Collection<ExportTraceServiceRequest> traces = waitForTraces();
        log.info("TRACES: {}", traces);

        Assertions.assertEquals(1, countSpansByName(traces, "GET /ping"));
        Assertions.assertEquals(0, countSpansByName(traces, "GET /actuator/health"));
        Assertions.assertEquals(0, countSpansByName(traces, "GET /actuator/metrics"));
        Assertions.assertEquals(0, countSpansByName(traces, "GET /health/readiness"));
        Assertions.assertEquals(2, countSpansByName(traces, "GET /actuator/info"));

        stopTarget();
    }

    private void runGetRequest(String route) throws IOException {
        String url = String.format("http://localhost:%d%s", target.getMappedPort(8080), route);
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).execute();
    }

    @Override
    protected Map<String, String> getExtraEnv() {
        return Map.of(
                "OTEL_DROP_SPANS_SERVER_HTTP.TARGET", ".*/health,.*/metrics,.*/prometheus",
                "OTEL_DROP_SPANS_CLIENT_HTTP.URL", ".*/info"
        );
    }
}
