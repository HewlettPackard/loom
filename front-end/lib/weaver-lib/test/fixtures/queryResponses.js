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

  var responseElements;

  var responseWithItems = {};
  responseElements = responseWithItems.elements = [];
  _.times(5, function (index) {
    responseElements.push({
      id: 'item-' + index,
      name: 'Item #' + index,
      someProperty: 'someValue' + index,
      '_meta': {
        type: 'item',
        href: 'http://some.rest.api/items' + index
      },
      relations: [
        'cached-' + index
      ]
    });
  });

  var responseWithClusters = {};
  responseElements = responseWithClusters.elements = [];
  _.times(3, function (index) {
    responseElements.push({
      id: 'cluster-' + index,
      name: 'Cluster #' + index,
      someProperty: 'someValue' + index,
      '_meta': {
        type: 'cluster',
        href: 'http://some.rest.api/clusters' + index
      }
    });
  });

  var responseWithGroups = {};
  responseElements = responseWithGroups.elements = [];
  _.times(4, function (index) {
    responseElements.push({
      id: 'group-' + index,
      name: 'Group #' + index,
      someProperty: 'someValue' + index,
      '_meta': {
        type: 'group',
        href: 'http://some.rest.api/groups' + index
      }
    });
  });

  var responseWithElementProperties = {
    elementProperties: {
      'aProperty': 'A property',
      'anotherProperty': 'Another property'
    },
    elements: []
  };

  var responseWithNewIdAndHref = {
    id: 'newId',
    elements: []
  };

  exports.responseWithItems = responseWithItems;
  exports.responseWithClusters = responseWithClusters;
  exports.responseWithGroups = responseWithGroups;
  exports.responseWithElementProperties = responseWithElementProperties;
  exports.responseWithNewIdAndHref = responseWithNewIdAndHref;
});