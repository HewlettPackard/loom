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

  var $ = require('jquery');
  var _ = require('lodash');
  var BaseView = require('weaver/views/BaseView');
  var FilterDepthSettingsView = require('weaver/views/FilterDepthSettingsView');
  var template = require('./RelationTypeList.html');

  /**
   * @class  RelationTypeList
   * @module weaver
   * @submodule views.relations
   * @namespace  views.relations
   * @constructor
   * @extends BaseView
   */
  var RelationTypeList = BaseView.extend({

    /**
     * @property template
     * @type {String}
     */
    template: template,

    /**
     * An index of which relationTypes are highlighted
     * @property {Object} highlightedRelationTypes
     */

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'click .mas-relationType': function (event) {
        var relationType = $(event.target).data('relationType');
        this.toggleHighlight(relationType);
      }
    },

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.highlightedRelationTypes = _.reduce(
        this.options.service.get('relationType'),
        function (result, relationType) {
          result[relationType] = true;
          return result;
        }, {}
      );
      this.listenTo(this.model, "sync", this.render);
      // this.relationshipDepthSettings = new FilterDepthSettingsView({
      //   model: this.options.relationshipService,
      //   title: 'All'
      // });
      this.relationshipHighlightDepthSettings = new FilterDepthSettingsView({
        model: this.options.service,
        title: 'Value'
      });
      this.$el.append(this.relationshipHighlightDepthSettings.el);
      // todo: does this work? There is a typo in the class name!
      this.relationshipHighlightDepthSettings.$el.addClass('mas-filterDepthSettings-highglight');
      this.render();
    },

    /**
     * @method render
     */
    render: function () {
      //this.relationshipHighlightDepthSettings.remove();
      this.$('.mas-relationTypeList--list')
        .empty()
        .append(this.model.map(this.renderRelationType, this));
      // if (this.model.length) {
      //
      //   this.$el.append(this.relationshipHighlightDepthSettings.el);
      // }
    },

    /**
     * @method renderRelationType
     * @param relationType
     * @returns {JQuery|*|string|jQuery}
     */
    renderRelationType: function (relationType) {
      return $('<li class="mas-relationType mas-listItem">')
        .text(relationType.get('name'))
        .toggleClass('is-highlighted', this.isHighlighted(relationType.get('id')))
        .attr('data-relation-type', relationType.get('id'));
    },

    /**
     * @method toggleHighlight
     * @param relationType
     */
    toggleHighlight: function (relationType) {
      if (this.isHighlighted(relationType)) {
        this.clearRelationTypeHighlight(relationType, true);
      } else {
        this.highlightRelationType(relationType);
      }
    },

    /**
     * @method isHighlighted
     * @param relationType
     * @returns {boolean}
     */
    isHighlighted: function (relationType) {
      return !!this.highlightedRelationTypes[relationType];
    },

    /**
     * @method highlightRelationType
     * @param relationType
     */
    highlightRelationType: function (relationType) {
      this.highlightedRelationTypes[relationType] = true;
      this.$('[data-relation-type="' + relationType + '"]').addClass('is-highlighted');
      this.options.service.set('relationType', this.getListOfHighlightedRelationTypes());
      this.options.service.activate();
    },

    /**
     * @method clearRelationTypeHighlight
     * @param relationType
     * @param deactivateIfLast
     */
    clearRelationTypeHighlight: function (relationType, deactivateIfLast) {
      this.highlightedRelationTypes[relationType] = false;
      this.$('[data-relation-type="' + relationType + '"]').removeClass('is-highlighted');
      this.options.service.set('relationType', this.getListOfHighlightedRelationTypes());
      if (deactivateIfLast && !this.hasHighlightedRelationType()) {
        this.options.service.deactivate();
      }
    },

    /**
     * @method getListOfHighlightedRelationTypes
     * @returns {Array|TResult}
     */
    getListOfHighlightedRelationTypes: function () {
      return _.reduce(this.highlightedRelationTypes, function (result, isHighlighted, relationType) {
        if (isHighlighted) {
          result.push(relationType);
        }
        return result;
      }, []);
    },

    /**
     * @method hasHighlightedRelationType
     * @returns {T}
     */
    hasHighlightedRelationType: function () {
      return _.find(this.highlightedRelationTypes, _.identity);
    }
  });

  return RelationTypeList;
});
