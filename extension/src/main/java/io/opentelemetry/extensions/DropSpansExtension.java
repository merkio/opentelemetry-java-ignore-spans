package io.opentelemetry.extensions;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;

import static io.opentelemetry.api.common.AttributeKey.stringKey;


public class DropSpansExtension implements AutoConfigurationCustomizerProvider {

    private static final AttributeKey<String> URL_PATH = stringKey("url.path");
    private static final DropSpanSampler SAMPLER = new DropSpanSampler();

    @Override
    public void customize(AutoConfigurationCustomizer autoConfiguration) {
        autoConfiguration.addTracerProviderCustomizer((sdkTracerProviderBuilder, configProperties) ->
                sdkTracerProviderBuilder.setSampler(SAMPLER));
    }
}
