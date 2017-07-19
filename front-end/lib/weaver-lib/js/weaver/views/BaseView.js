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
  var $ = require('jquery');
  var Backbone = require('backbone');
  var Cocktail = require('backbone.cocktail');
  var EventBus = require('weaver/utils/EventBus');
  var EventBusMixin = require('weaver/mixins/EventBusMixin');
  var InjectServicesMixin = require('weaver/mixins/InjectServicesMixin');

  /**
   * BaseView encapsulate common utilities to be shared by all Weaver views.
   *
   * @backbone I am preparing the codebase for backbone 1.1 which has backwards breaking changes. this means a safe
   * initialize method that can be applied to all Views in the application. It also means EVERY view must be a BaseView.
   * This will ease the conversion to 1.1 by providing a single point of entry/change
   * @class BaseView
   * @namespace views
   * @module  weaver
   * @submodule views
   * @constructor
   * @extends Backbone.View
   */
  var BaseView = Backbone.View.extend({

    /**
     * @property className
     * @type {String}
     */
    className: '',

    /**
     * A string containing a HTML template to be used to scaffold the DOM structure when creating the view.
     * @property template
     * @type {undefined|String}
     */
    template: undefined,

    /**
     * Global event bus for inter module communication
     * @type Backbone.Events
     */
    EventBus: EventBus,

    /**
     * Services are declaratively Injected in..
     * @property {Object} services
     */
    services: {},

    /**
     * @type undefined|ServiceManager
     */
    serviceManager: undefined,

    /**
     * @backbone this.options is always available in 1.0.x
     * @backbone this.options is not available in 1.1.x
     * @method initialize
     * @param options
     */
    initialize: function (options) {
      Backbone.View.prototype.initialize.apply(this, arguments);
      Cocktail.mixin(this, EventBusMixin);
      this._injectServices(options);
      this._applyBackboneOptionsFix(options);
      this._initializeTemplate(); //@todo do ALL child classes need this?
      this._initializeEl();
    },

    /**
     *
     * @param options
     * @private
     */
    _injectServices: function (options) {
      Cocktail.mixin(this, InjectServicesMixin);
      if (_.has(options, 'ServiceManager')) {
        this.injectServices(options.ServiceManager);
      }
    },

    /**
     * Backbone 1.1 broke the View options handling in a non backwards compatible way. This method applies a fix and
     * makes the this.options.X values available again
     * @backbone Apply 1.1 fix
     * @method _applyBackboneOptionsFix
     * @param options
     * @returns {*}
     * @private
     */
    _applyBackboneOptionsFix: function (options) {
      options = options || {};
      if (this.options) {
        options = _.extend({}, _.result(this, 'options'), options);
      }
      this.options = options;
    },

    /**
     * @method _initializeTemplate
     * @private
     */
    _initializeTemplate: function () {
      if (this.template) {
        var $dom = this._getDOM(this.template).clone();
        if (this.options.el || this.options.$el) {
          $dom.children().appendTo(this.$el);
        } else {
          this.setElement($dom);
        }
      }
      // Windows 8 app security policies prevent the addition of elements
      // with a name attribute if they come from a template, so we need to go around that
      this.$('[data-name]').each(function () {
        this.setAttribute('name', this.getAttribute('data-name'));
      });
    },

    /**
     * Initializes the $el view element with classname and data if it exists
     * @method _initializeEl
     * @private
     */
    _initializeEl: function () {
      if (this.$el) {
        this.$el.addClass(this.className);
        this.$el.data('view', this);
      }
    },

    /**
     * @method _getDOM
     */
    _getDOM: _.memoize(function (template) {
      return $(template);
    }),

    /**
     * Dispatches a custom DOM event with given event name and data attached to it
     * @method  dispatchCustomEvent
     * @param  {String} eventName The name of the custom event
     * @param  {Object} data      Additional data to attach to the view
     */
    dispatchCustomEvent: function (eventName, data) {
      // todo: Why not jQuery's `trigger` function? or Backbone.Event, does this need to be sent on the DOM?
      var event = document.createEvent('Event');
      event.initEvent(eventName, true, true);
      _.extend(event, data, {
        view: this
      });
      if (false) {console.log('BaseView.dispatchCustomEvent', eventName, data);}
      this.el.dispatchEvent(event);
      EventBus.trigger(eventName, data);
    },

    /**
     * Chainable render function
     * @returns {BaseView}
     * @chainable
     */
    render: function() {
      Backbone.View.prototype.render.apply(this, arguments);
      return this;
    }

  });


  return BaseView;
});
