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

  /**
   * This class isn't intended to be inherited from but,
   * assigned to other object. This way you can still have proper inheritance for your needs.
   * You'll just be adding functionalities to your class.
   */
  var CssClassHelper = {

    /**
     * This is the method to call to upgrade your class.
     * You just have to do the following call:
     *     CssClassHelper.assignTo(MyClass);
     * 
     * @param  {Object} Class is your class object.
     * @return {Class}        returns your class enhanced with Css functions helpers! :)
     */
    assignTo: function (Class) {
      _.assign(Class.prototype, CssClassHelper, function (a, b) {
        return _.isUndefined(a) ? b : a;
      });
    },

    /**
     * Set the current d3 selection that will received all the class elements.
     * @param {D3Selection} d3selection is a d3 object that support d3 selection methods.
     */
    _setSelection: function (d3selection) {
      // TODO: should we remove the list of class to the previous selection ?
      this.d3selection = d3selection;
      this._refreshClasses();
    },

    /**
     * Needs to be called only once by your class in the ctor or in any place that initialize
     * each instance of your class.
     * @param  {D3Selection} d3selection is the d3 selection. Can be omitted and set later
     *                                   with the _setSelection function.
     */
    _initListClass: function (d3selection) {
      this.listClass = [];
      this.d3selection = d3selection;
    },

    /**
     * Fill the List of class with the list of classes already applied to the selection.
     */
    _fillWithExistingClasses: function () {
      var listClass = this.d3selection.attr('class').split(/\s+/);
      for (var i = listClass.length - 1; i >= 0; i--) {
        if (!_.contains(this.listClass, listClass[i])) {
          this._addClass(listClass[i]);
        }
      }
    },
    /**
     * Add a css class to the selection.
     * @param {String} clazz name of the css class.
     */
    _addClass: function (clazz) {
      if (_.contains(this.listClass, clazz)) {
        return;
      }
      this.listClass.push(clazz);
      this._refreshClasses();
    },

    /**
     * Remove a css class from the selection.
     * @param  {String} clazz is the name of the css class to remove
     */
    _removeClass: function (clazz) {
      this.listClass = _.without(this.listClass, clazz);
      this._refreshClasses();
    },

    /**
     * Apply the given transformation to the list of classes.
     * @param  {Function} transform this function should accept a String and return a String
     */
    _eachClass: function (transform) {
      this.listClass = _.map(this.listClass, transform);
      this._refreshClasses();
    },

    /**
     * Apply the list of classes to the selection. Usefull if the selection change.
     * Note: this method is called by the _setSelection method.
     */
    _refreshClasses: function () {
      this.d3selection
        .attr('class', this.listClass.join(" "));
    },

    /**
     * This function is particular. It gives you the possibility to allow only one class at a time
     * based on a specific key.
     * @param {String} key        is the unique id of the set of class. Can be any value.
     * @param {String} clazzValue is the new value associated with the key. Any previous value
     *                            will be removed from the selection.
     */
    _setClass: function (key, clazzValue) {
      if (!this.___classes) {
        this.___classes = {};
      }

      var oldClass = this.___classes[key];

      if (oldClass === clazzValue) {
        return;
      }

      if (oldClass) {
        this._removeClass(oldClass);
      }

      this._addClass(clazzValue);
      this.___classes[key] = clazzValue;
    },
  };

  return CssClassHelper;

});