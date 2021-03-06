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
package org.lable.oss.dynamicconfig.provider.zookeeper;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.lable.oss.dynamicconfig.core.ConfigurationException;
import org.lable.oss.dynamicconfig.core.ConfigurationInitializer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.lable.oss.dynamicconfig.provider.zookeeper.ZookeepersAsConfigSource.combinePath;

public class ZookeepersAsConfigSourceTest {
    @Test
    public void testConstructorSlashPath() throws ConfigurationException {
        ZookeepersAsConfigSource source = new ZookeepersAsConfigSource();
        Configuration config = new BaseConfiguration();
        config.setProperty("quorum", "QUORUM");
        config.setProperty("znode", "/path/node");
        config.setProperty(ConfigurationInitializer.APPNAME_PROPERTY, "my-app");
        source.configure(config);

        assertThat(source.znode, is("/path/node/my-app"));
        assertThat(source.quorum.length, is(1));
        assertThat(source.quorum[0], is("QUORUM"));
    }

    @Test
    public void testConstructorWithAppNameAndQuorumList() throws ConfigurationException {
        ZookeepersAsConfigSource source = new ZookeepersAsConfigSource();
        Configuration config = new BaseConfiguration();
        config.setProperty("quorum", "zk1,zk2");
        config.setProperty("znode", "/path");
        config.setProperty(ConfigurationInitializer.APPNAME_PROPERTY, "node");
        source.configure(config);

        assertThat(source.znode, is("/path/node"));
        assertThat(source.quorum.length, is(2));
        assertThat(source.quorum[0], is("zk1"));
        assertThat(source.quorum[1], is("zk2"));
    }

    @Test(expected = ConfigurationException.class)
    public void testConstructorNoAppName() throws ConfigurationException {
        ZookeepersAsConfigSource source = new ZookeepersAsConfigSource();
        Configuration config = new BaseConfiguration();
        config.setProperty("quorum", "QUORUM");
        config.setProperty("znode", "/path/node");
        source.configure(config);
    }

    @Test
    public void testCombinePath() {
        assertThat(combinePath("path", "node"), is("path/node"));
        assertThat(combinePath("path", "/node"), is("path/node"));
        assertThat(combinePath("path/", "node"), is("path/node"));
        assertThat(combinePath("path/", "/node"), is("path/node"));
        assertThat(combinePath("path", null), is("path"));
        assertThat(combinePath("path", ""), is("path"));
    }
}
