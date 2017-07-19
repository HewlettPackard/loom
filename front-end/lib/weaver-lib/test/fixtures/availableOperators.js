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
define(function () {

  "use strict";

  return [{
    id: 'GROUP_BY',
    name: 'Group by',
    params: [{
      id: 'property',
      type: 'ATTRIBUTE_LIST',
      update: 'change'
    }],
    icon: 'fa-cubes',
    displayParameters: ['property']
  }, {
    id: 'SORT_BY',
    name: 'Sort by',
    params: [{
      id: 'property',
      type: 'ATTRIBUTE_LIST'
    }, {
      id: 'order',
      type: 'ENUM',
      range: ['ASC', 'DSC']
    }],
    icon: 'fa-exchange',
    displayParameters: ['property', 'order']
  }, {
    id: 'FILTER_STRING',
    name: 'Filter',
    params: [{
      id: 'pattern',
      type: 'STRING'
    }],
    icon: 'fa-filter',
    displayParameters: ['pattern'],
    canExcludeItems: true
  }, {
    id: 'GET_FIRST_N',
    name: 'First N',
    params: [{
      id: 'N',
      type: 'INT'
    }],
    icon: 'fa-star',
    displayParameters: ['N']
  }, {
    id: 'BRAID',
    name: 'Braid',
    params: [{
      id: 'maxFibres',
      type: 'INT'
    }],
    icon: 'fa-sitemap',
    displayParameters: ['maxFibres']
  }, {
    id: 'PERCENTILES',
    name: 'Percentiles',
    params: [{
      id: 'property',
      type: 'ATTRIBUTE_LIST'
    }, {
      id: 'numBuckets',
      type: 'INT',
      defaultValue: 10
    }],
    icon: 'fa-sitemap',
    displayParameters: ['property']
  }, {
    id: 'SUMMARY',
    name: 'Summary',
    icon: 'fa-sitemap'
  }, {
    id: 'FILTER_BY_REGION',
    name: 'Filter by region',
    icon: 'fa-filter'
  }];
});
