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

public final class Config {
    private final ConfigurationProvider configProvider;

    public Config(final String defaultConfigFilename, final String overrideConfigFilename) {
        final File defaultConfigFile = new File(defaultConfigFilename);
        final Environment defaultEnvironment = new ImmutableEnvironment(defaultConfigFile.getParent());
        final ConfigurationSource defaultConfigSource = new FilesConfigurationSource(() ->
            Paths.get(defaultConfigFile.getName())
        );
        final ConfigurationProvider defaultConfigProvider = new ConfigurationProviderBuilder()
            .withEnvironment(defaultEnvironment)
            .withConfigurationSource(defaultConfigSource)
            .build();

        final File overrideConfigFile = new File(overrideConfigFilename);
        final Environment overrideEnvironment = new ImmutableEnvironment(defaultConfigFile.getParent());
        if (new File(overrideConfigFilename).exists()) {
            final ConfigurationSource configSource = new MergeConfigurationSource(
                defaultConfigSource,
                new FilesConfigurationSource(() -> Paths.get(overrideConfigFile.getName())));
            configProvider = new ConfigurationProviderBuilder()
                .withEnvironment(overrideEnvironment)
                .withConfigurationSource(configSource)
                .build();
        } else {
            configProvider = defaultConfigProvider;
        }
    }

    public <T> T getConfig(final String name, final Class<T> bindingClass) {
        return configProvider.bind(name, bindingClass);
    }
}
