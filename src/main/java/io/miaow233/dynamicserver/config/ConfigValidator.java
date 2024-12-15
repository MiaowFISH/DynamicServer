package io.miaow233.dynamicserver.config;

import io.miaow233.dynamicserver.DynamicServer;
import org.jetbrains.annotations.NotNull;

public interface ConfigValidator {

    /**
     * Validates the configuration settings.
     * @throws IllegalStateException if the configuration is invalid
     */
    void validateConfig(@NotNull DynamicServer plugin) throws IllegalStateException;

}