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

  var Backbone = require('backbone');

  /**
   * List of the relation types available on a given Tapestry
   * @class RelationTypeList
   * @namespace models.Tapestry
   * @module weft
   * @submodule models.Tapestry
   * @extends Backbone.Collection
   * @constructor
   */
  var RelationTypeList = Backbone.Collection.extend({
    /**
     * @attribute urlRoot
     */
    urlRoot: '/relation',

    /**
     * @method initialize
     * @param content
     * @param options
     */
    initialize: function (content, options) {
      this.tapestry = options.tapestry;
    },

    /**
     * @method url
     * @returns {string}
     */
    url: function () {
      return this.tapestry.aggregator.get('url') + this.tapestry.url() + this.urlRoot;
    },

    /**
     * @method parse
     * @param response
     * @returns {*}
     */
    parse: function (response) {
      return response.relationshipTypes;
    }
  });

  return RelationTypeList;
});