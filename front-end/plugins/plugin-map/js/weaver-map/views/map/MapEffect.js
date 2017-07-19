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
define(function (require) {
  "use strict";

  var _ = require('lodash');
  var MapEvent = require('./MapEvent');

  var MapEffect = MapEvent.extend({

    initialize: _.noop,

    initializeWhenAttached: _.noop,

    _findIdGroup: function (d3selection) {
      // A proper version would be to look for common ancestor
      // of all nodes in the d3selection.
      var currentParent = d3selection[0][0].parentNode;
      while (!currentParent.getAttribute('idGroup') && currentParent.tagName !== 'svg') {
        currentParent = currentParent.parentNode;
      }
      return currentParent.getAttribute('idGroup') || '';
    },

    /**
     * Private method to return the id of the DOM element.
     * If it doesn't have one, one is added to it.
     * @param  {DOMElement} element is a dom element
     * @return {String}         the id of this element.
     */
    _getId: function (element) {
      var id;
      if (element.tagName === "use") {
        return element.href.baseVal.substr(1);
      }
      return element.id || (id = _.uniqueId(element.tagName), element.setAttribute("id", id), id);
    },

    /**
     * Protected method for sub effect classes.
     * @param  {Selections} d3selection Selection made with the d3 API
     * @return {Array[String]}             Array of ids from the selection.
     */
    __getIdsArray: function (d3selection) {
      var ids = [];
      var self = this;
      d3selection.each(function () {
        var id = self._getId(this);
        ids.push(id);
      });

      return ids;
    },

  });

  return MapEffect;
});