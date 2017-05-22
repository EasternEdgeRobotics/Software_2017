package com.easternedgerobotics.rov.config;

import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.compose.MergeConfigurationSource;
import org.cfg4j.source.context.environment.Environment;
import org.cfg4j.source.context.environment.ImmutableEnvironment;
import org.cfg4j.source.files.FilesConfigurationSource;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;

public final class Config {
    private final ConfigurationProvider configProvider;

    public Config(final String defaultConfigFilename, final String overrideConfigFilename) {
        final File defaultConfigFile = new File(defaultConfigFilename);
        final Environment rootEnvironment = new ImmutableEnvironment("/");
        final ConfigurationSource defaultConfigSource = new FilesConfigurationSource(() ->
            Collections.singletonList(Paths.get(defaultConfigFile.getAbsolutePath()))
        );
        final ConfigurationProvider defaultConfigProvider = new ConfigurationProviderBuilder()
            .withEnvironment(rootEnvironment)
            .withConfigurationSource(defaultConfigSource)
            .build();

        final File overrideConfigFile = new File(overrideConfigFilename);
        if (overrideConfigFile.exists()) {
            final ConfigurationSource configSource = new MergeConfigurationSource(
                defaultConfigSource,
                new FilesConfigurationSource(() ->
                    Collections.singletonList(Paths.get(overrideConfigFile.getAbsolutePath()))
                )
            );
            configProvider = new ConfigurationProviderBuilder()
                .withEnvironment(rootEnvironment)
                .withConfigurationSource(configSource)
                .build();
        } else {
            configProvider = defaultConfigProvider;
        }
    }

    public <T> T getConfig(final String name, final Class<T> bindingClass) {
        if (!bindingClass.isInterface()) {
            return configProvider.getProperty(name, bindingClass);
        }
        return configProvider.bind(name, bindingClass);
    }
}
