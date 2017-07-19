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
package com.hp.hpl.loom.api;

/**
 * This class contains public static fields for controllers' path.
 */
public class ApiConfig {
    /**
     * The api base path.
     */
    public static final String API_BASE = "/";
    /**
     * Path for Pattern controller.
     */
    public static final String PATTERNS_BASE = "/patterns";

    /**
     * Path for Tapestry controller.
     */
    public static final String TAPESTRY_BASE = "/tapestries";

    /**
     * Path for Provider controller.
     */
    public static final String PROVIDERS_BASE = "/providers";

    /**
     * Path for Actions controller.
     */
    public static final String ACTIONS_BASE = "/actions";

    /**
     * Path for Action Results controller.
     */
    public static final String ACTION_RESULTS_BASE = "/actionResults";

    /**
     * Path for Items controller.
     */
    public static final String ITEM = "/items";

    /**
     * Path for ItemTypes controller.
     */
    public static final String ITEM_TYPE = "/itemTypes";

    /**
     * Path for Operations controller.
     */
    public static final String OPERATION = "/operations";

    /**
     * The deployment properties property.
     */
    public static final String DEPLOYMENT_PROPERTIES_PROPERTY = "deployment.properties";

    /**
     * The api headers accept header.
     */
    public static final String API_HEADERS = "Accept=application/json";

    /**
     * The api headers accept text header.
     */
    public static final String API_HEADERS_TEXT = "Accept=application/text";

    /**
     * The api headers produces header.
     */
    public static final String API_PRODUCES = "application/json";

    /**
     * The api headers text producing header.
     */
    public static final String API_PRODUCES_TEXT = "application/text";

    /**
     * Path for Status controller.
     */
    public static final String API_STATUS = "/status";

    /**
     * Protected constructor as this is a utility class.
     */
    protected ApiConfig() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }
}
