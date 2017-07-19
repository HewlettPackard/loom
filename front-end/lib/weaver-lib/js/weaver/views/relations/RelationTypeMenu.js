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

  /** @type BaseView */
  var BaseView = require('../BaseView');
  var RelationTypeList = require('./RelationTypeList');
  var RelationPathsExplorer = require('./RelationPathsExplorer');

  /**
   * @class  RelationTypeMenu
   * @module weaver
   * @submodule views.relations
   * @namespace  views.relations
   * @constructor
   * @extends BaseView
   */
  var RelationTypeMenu = BaseView.extend({

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.render();
    },

    /**
     * @method render
     */
    render: function () {
      this.relationTypeList = new RelationTypeList({
        model: this.options.relationTypeList,
        service: this.options.relationshipHighlightService,
        relationshipService: this.options.relationshipService
      });
      this.relationPathsExplorer = new RelationPathsExplorer({
        relationTypeList: this.options.relationTypeList,
        relationshipService: this.options.relationshipService,
        aggregatorClient: this.options.aggregatorClient
      });
      this.$el.append(this.relationTypeList.el, this.relationPathsExplorer.el);
    }
  });

  return RelationTypeMenu;
});
