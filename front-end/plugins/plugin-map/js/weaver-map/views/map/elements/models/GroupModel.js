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

  var GroupModel = (function () {

    // Provides parseAndSet and replaceWithFunction methods
    _.extend(GroupModel.prototype, ParsingModelHelper);

    function GroupModel(thread) {

      this.thread = thread;
      this.id = _.uniqueId();
      this.bindedModel = {};
      this.elementModels = {};
      this.classList = [];

      // Only use of ParsingModelHelper
      this.parseAndSet(this.defaults(), this.bindedModel);
    }

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
    GroupModel.prototype.defaults = function () {

      return {
        idGroup: _.uniqueId(),
        models: 'getModels',
        hasFocus: 'hasFocus',
        classGroup: 'getClasses',
      }
    };

    /**
     * Returns the context for this group.
     * @return {Object} context ready to use with TemplateProcessor
     */
    GroupModel.prototype.getContext = function () {
      return this.bindedModel;
    };

    /**
     * Add an element to the underlying list of models.
     * @param {ElementModel} element is an element properly initialized.
     */
    GroupModel.prototype.addElementModel = function (element) {
      element.moveToContainer(this);
      this.elementModels[element.id] = element;
    };
    
    /**
     * Remove the given element from the list.
     * @param {ElementModel} element is the element to remove.
     */
    GroupModel.prototype.removeElementModel = function (element) {
      delete this.elementModels[element.id];
    };

    /**
     * Allow to access a model by id.
     * @param  {String} id is the id of the elementModel
     * @return {ElementModel}    Returns undefined or the found elementModel
     */
    GroupModel.prototype.getElementModel = function (id) {
      return this.elementModels[id];
    };

    /**
     * If you want do "delete" this model,
     * you have to call this function first.
     */
    GroupModel.prototype.remove = function () {
      
      // Remove the group from its base model:
      this.moveToContainer();
    };

    /**
     * This function allow you to switch a group
     * from a container of type BaseModel to another one.
     * It is equivalent to call BaseModel#addGroupModel
     * on the new container.
     * Calling this function without parameter is equivalent
     * to call GroupModel#remove.
     * @param  {BaseModel} c is the new container.
     */
    GroupModel.prototype.moveToContainer = function (c) {
      // Remove from old container and set to the new container:
      if (this.container && this.container !== c) {
        this.container.removeGroupModel(this);
      }
      this.container = c;

      if (this.container) {

        // Set the container as englobing context for bindedModel
        var bindedModelWithContainerContext = Object.create(this.container.getContext());
        _.assign(bindedModelWithContainerContext, this.bindedModel);
        this.bindedModel = bindedModelWithContainerContext;
      }
    };
    
    /**
     * Return the underlying list of models
     * @returns {Array} Returns the underlying list.
     */
    GroupModel.prototype.getModels = function () {
      // Returns the list of models sorted by radius
      return _.sortBy(_.map(this.elementModels, function (element) {
        return element.bindedModel;
      }), function (element) {
        return -element.nbFibres();
      });
    };
    
    /**
     * Returns true if the associated thread has the focus.
     * @returns {Boolean} true if this group has the focus.
     */
    GroupModel.prototype.hasFocus = function () {
      return this.thread.get('focus');
    };
    
    /**
     * Clear the underlying list of models.
     * Does nothing on the models themselves.
     */
    GroupModel.prototype.clearModels = function () {
      this.elementModels = {};
    };

    /**
     * Add a css class to the group.
     * @param {String} classStr name of the css class.
     */
    GroupModel.prototype.addClass = function (classStr) {
      
      if (_.contains(this.classList, classStr)) {
        return;
      }
      
      this.classList.push(classStr);
    };
    
    /**
     * Remove a css class of the group.
     * @param  {String} classStr is the name of the css class to remove
     */
    GroupModel.prototype.removeClass = function (classStr) {
      
      this.classList = _.without(this.classList, classStr);
    };

    /**
     * Returns the list of classes
     * @returns {String} list of classes
     */
    GroupModel.prototype.getClasses =  function () {
      
      return this.classList.join(' ');
    };


    return GroupModel;
  })();

  return GroupModel;
});