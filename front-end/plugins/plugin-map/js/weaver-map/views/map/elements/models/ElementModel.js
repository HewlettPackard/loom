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
  
  var Element = require('weft/models/Element');
  var ParsingModelHelper = require('weaver-map/views/map/elements/helpers/ParsingModelHelper');
  
  var ElementModel = (function () {
    
    // Provides parseAndSet and replaceWithFunction methods
    _.extend(ElementModel.prototype, ParsingModelHelper);
    
    function ElementModel(options) {
      
      // This property is optional and
      // is just used for the UserEventBinder
      this.eventHandler = options.eventHandler;

      // Used properties
      this.model = options.model;
      this.idRef = options.idRef;

      this.id = _.uniqueId();
      this.bindedModel = {};
      this.classList = [];
      
      // Only use of ParsingModelHelper
      this.parseAndSet(this.defaults(), this.bindedModel);
    };

    /**
     * If you want do "delete" this model,
     * you have to call this function first.
     */
    ElementModel.prototype.remove = function () {
      
      // Remove the element from its base model:
      this.moveToContainer();
    };

    /**
     * This function allow you to switch an element
     * from a container of type GroupModel to another one.
     * It is equivalent to call GroupModel#addElementModel
     * on the new container.
     * Calling this function without parameter is equivalent
     * to call ElementModel#remove.
     * @param  {GroupModel} c is the new container.
     */
    ElementModel.prototype.moveToContainer = function (c) {
      // Remove from old container and set to the new container:
      if (this.container && this.container !== c) {
        this.container.removeElementModel(this);
      }
      this.container = c;
    };
    
    ElementModel.prototype.defaults = function () {
      return {
        id: this.id,
        classes: 'getClasses',
        idRef: 'getIdRef',
        inset: {
          idMask: _.uniqueId(),
        },
        nbFibres: 'getNbFibres',
        hasBeenUpdated: 'hasBeenUpdated',
        hasBeenAdded: 'hasBeenAdded',
        hasAlert: 'hasAlert',
        hasBeenUpdatedOrHasBeenAdded: 'hasBeenUpdatedOrHasBeenAdded',
        hasAlertOrHasBeenAdded: 'hasAlertOrHasBeenAdded',
        hasAlertOrHasBeenUpdated: 'hasAlertOrHasBeenUpdated',
        title: 'getTitle'
      };
    };
    
    ElementModel.prototype.getTitle = function () {
      return this.model.getTranslated('name');
    };
    
    ElementModel.prototype.getNbFibres = function () {
      var nb = this.model.get('numberOfItems');
      return (nb === undefined && 1) || nb;
    };
      
    ElementModel.prototype.hasBeenAdded = function () {
      return this.model.state === Element.STATE_ADDED;
    };

    ElementModel.prototype.hasBeenUpdated = function () {
      return this.model.state === Element.STATE_UPDATED || this.model.state === 'nestedStateChanges';
    };
    
    ElementModel.prototype.hasAlert = function () {
      return this.model.alert.get('level') > 0;
    };

    ElementModel.prototype.hasBeenUpdatedOrHasBeenAdded = function () {
      return this.hasBeenUpdated() || this.hasBeenAdded();
    };

    ElementModel.prototype.hasAlertOrHasBeenAdded = function () {
      return this.hasAlert() || this.hasBeenAdded();
    };

    ElementModel.prototype.hasAlertOrHasBeenUpdated = function () {
      return this.hasAlert() || this.hasBeenUpdated();
    };


    ElementModel.prototype.getIdRef = function () {
      return this.idRef;
    };
    
    /**
     * Add a css class to the selection.
     * @param {String} clazz name of the css class.
     */
    ElementModel.prototype.addClass = function (classStr) {
      
      if (_.contains(this.classList, classStr)) {
        return;
      }
      
      this.classList.push(classStr);
    };
    
    /**
     * Remove a css class from the selection.
     * @param  {String} clazz is the name of the css class to remove
     */
    ElementModel.prototype.removeClass = function (classStr) {
      
      this.classList = _.without(this.classList, classStr);
    };
    
    /**
     * This function gives you the possibility to allow only one class at a time
     * based on a specific key.
     * @param {String} key        is the unique id of the set of class. Can be any value.
     * @param {String} clazzValue is the new value associated with the key. Any previous value
     *                            will be removed from the selection.
     */
    ElementModel.prototype.setClass = function (key, clazzValue) {
      if (!this.___classes) {
        this.___classes = {};
      }

      var oldClass = this.___classes[key];

      if (oldClass === clazzValue) {
        return;
      }

      if (oldClass) {
        this.removeClass(oldClass);
      }

      this.addClass(clazzValue);
      this.___classes[key] = clazzValue;
    };
    
    /**
     * Returns the list of classes
     * @returns {String} list of classes
     */
    ElementModel.prototype.getClasses =  function () {
      
      return this.classList.join(' ');
    };
    
    return ElementModel;
  })();
  
  return ElementModel;
});