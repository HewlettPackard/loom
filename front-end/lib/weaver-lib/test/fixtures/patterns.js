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
define([
  'lodash',
  'exports'
], function (_, exports) {

  "use strict";

  function generateThreads() {
    var result = [];
    _.times(3, function (index) {

      result.push({
        id: 'thread-' + index,
        name: 'Thread #' + index,
        _meta: {
          href: 'http://some.provider/threads/' + index
        },
        itemProperties: generateProperties(),
        itemGroupBy: generateProperties(),
        itemSortBy: generateProperties(),
        itemMetrics: generateMetrics()
      });
    });
    return result;
  }

  function generateProperties() {

    var properties = {

      '@class': 'Properties'
    };

    _.times(4, function (index) {
      properties['prop-' + index] = 'Property #' + index;
    });

    return properties;
  }

  function generateMetrics() {

    var metrics = {
      '@class': 'Metrics'
    };

    _.times(4, function (index) {

      metrics['metric-' + index] = {
        'name': 'Metric #' + index,
        'unit': '%',
        'min': 0,
        'max': 20
      };
    });

    return metrics;
  }

  exports.samplePattern = {
    id: 'essentials',
    name: 'Essential Information',
    threads: generateThreads()
  };
});