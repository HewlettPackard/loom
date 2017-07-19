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
  
  var ParsingModelHelper = require('weaver-map/views/map/elements/helpers/ParsingModelHelper');

  var BaseModel = (function () {
    
    // Provides parseAndSet and replaceWithFunction methods
    _.extend(BaseModel.prototype, ParsingModelHelper);
    
    /**
     * BaseModel class should be use to manipulate
     * the model given to TemplateSVGRenderer.
     * @param {Object} options should contains:
     *                         mapData {MapData} object.
     */
    function BaseModel(options) {

      this.mapData = options.mapData;
      this.bindedModel = {};
      this.groupModels = {};

      // Only use of ParsingModelHelper
      this.parseAndSet(this.defaults(), this.bindedModel);
    };
   
    /**
     * Defaults for the context.
     * This function helps to understand what are
     * the different variables the templates can access
     * and also permit some bindings with variable accessed
     * by function.
     * {@link _replaceWithFunction has to be called on the result of defaults.}
     * @returns {Object} the default object for the context
     *                    the function are not bind after this call
     */
    BaseModel.prototype.defaults = function () {

      return {
        scene: {
          transform: 'getTransform',
          translate: 'getTranslate'
        },
        marker: {
          stroke: 'getStroke'
        },
        groups: 'getGroups',
        hatch: {
          id: _.uniqueId(),
          idMask: _.uniqueId()
        },
        inset: {
          stroke: 'getInsetStroke',
          strokeMask: 'getInsetStrokeMask'
        }
      }
    };

    /**
     * Add a group to this base model.
     * @param {GroupModel} group is the group to add.
     */
    BaseModel.prototype.addGroupModel = function (group) {
      group.moveToContainer(this);
      this.groupModels[group.id] = group;
    };

    /**
     * Remove the given group from this base model
     * @param  {GroupModel} group is the group to remove
     */
    BaseModel.prototype.removeGroupModel = function (group) {
      delete this.groupModels[group.id];
    };

    /**
     * Allow to access a group by id.
     * @param  {String} id is the id of the groupModel
     * @return {GroupModel}    Returns undefined if not found or the corresponding group.
     */
    BaseModel.prototype.getGroupModel = function (id) {
      return this.groupModels[id];
    };

    /**
     * Returns the list of groups sort based on focus.
     * Note: if the sort is changed here, to be consistent
     * we need to change it also within TemplateSVGRenderer.
     * @return {Array} Return the list of groups.
     */
    BaseModel.prototype.getGroups = function () {
      // Returns the list of groups sorted by focus
      return _.sortBy(_.map(this.groupModels, function (group) {
        return group.bindedModel;
      }), function (group) {
        return group.hasFocus();
      });
    };

    /**
     * Returns the context that will be used to 
     * generate the string from the template
     * {@link TemplateSVGRender}
     * @returns {Object} Returns the context.
     */
    BaseModel.prototype.getContext = function () {
      return this.bindedModel;
    };

    BaseModel.prototype.getInsetStrokeMask = function () {
      return 4 / this.getCurrentMapScale();
    };

    BaseModel.prototype.getInsetStroke = function () {
      return 12 / this.getCurrentMapScale();
    };

    BaseModel.prototype.getStroke = function() {
      return 1 / this.getCurrentMapScale();
    };

    BaseModel.prototype.getCurrentMapScale = function () {
      return this.mapData.getZoomCurrentScale();
    };

    BaseModel.prototype.getTransform = function () {
      return "translate(" + this.mapData.getZoomCurrentTranslate() + ") " +
             "scale(" + [this.getCurrentMapScale(), this.getCurrentMapScale()] + ")";
    };

    BaseModel.prototype.getTranslate = function () {
      return "translate(" + this.mapData.getZoomCurrentTranslate() + ")";
    };
        
    return BaseModel;

  })();


  return BaseModel;
});