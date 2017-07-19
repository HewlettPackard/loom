/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.hp.hpl.loom;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;


@Configuration
@SuppressWarnings("checkstyle:hideutilityclassconstructor")
public class SpringConfiguration {
    private static final Log LOG = LogFactory.getLog(SpringConfiguration.class);


    public SpringConfiguration() {}

    @Bean
    public static PropertyPlaceholderConfigurer configurer() {
        PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        String overrideProperties = System.getProperty("deployment.properties");
        Resource resource = null;

        if (overrideProperties == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Using configuration from default.properties on the classpath");
            }

            resource = new ClassPathResource("deployment.properties");
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Using configuration from the file " + overrideProperties);
            }

            resource = new FileSystemResource(overrideProperties);
        }

        ppc.setLocation(resource);

        return ppc;
    }
}
