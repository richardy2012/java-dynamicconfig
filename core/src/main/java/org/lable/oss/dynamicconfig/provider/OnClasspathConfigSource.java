/**
 * Copyright (C) 2015 Lable (info@lable.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lable.oss.dynamicconfig.provider;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.lable.oss.dynamicconfig.core.ConfigurationException;
import org.lable.oss.dynamicconfig.core.spi.HierarchicalConfigurationDeserializer;
import org.lable.oss.dynamicconfig.core.spi.ConfigurationSource;
import org.lable.oss.dynamicconfig.core.ConfigChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Load configuration from a file on the classpath.
 */
public class OnClasspathConfigSource implements ConfigurationSource {
    private final static Logger logger = LoggerFactory.getLogger(OnClasspathConfigSource.class);

    String configPath = null;

    /**
     * Construct a new OnClasspathConfigSource.
     */
    public OnClasspathConfigSource() {
        // Intentionally empty.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return "classpath";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> systemProperties() {
        return Collections.singletonList("path");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Expects a parameter "path" containing the path to the configuration file, relative to the classpath.
     */
    @Override
    public void configure(Configuration configuration) throws ConfigurationException {
        String configPath = configuration.getString("path");
        if (isBlank(configPath)) {
            throw new ConfigurationException("path", "Parameter not set or empty.");
        }

        this.configPath = configPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void listen(final HierarchicalConfigurationDeserializer deserializer, final ConfigChangeListener listener) {
        // No-op. This configuration source is presumed static.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean load(final HierarchicalConfigurationDeserializer deserializer, final ConfigChangeListener listener) {
        if (configPath == null) {
            logger.error("Path is empty. Was #configure called?");
            return false;
        }

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream is =  cl.getResourceAsStream(configPath);
        if (is == null) {
            logger.error("Could not find " + configPath + " in the classpath.");
            return false;
        }

        HierarchicalConfiguration hc;
        try {
            hc = deserializer.deserialize(is);
        } catch (org.apache.commons.configuration.ConfigurationException e) {
            logger.error("Failed to parse " + configPath + " found on classpath.", e);
            return false;
        }

        listener.changed(hc);
        return true;
    }
}