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
  var BaseView = require('weaver/views/BaseView');

  /**
   * @class  RelationPathsExplorer
   * @module weaver
   * @submodule views.relations
   * @namespace  views.relations
   * @constructor
   * @extends BaseView
   */
  var RelationPathsExplorer = BaseView.extend({

    /**
     * @property template
     * @type {String}
     */
    template: require('./RelationPathsExplorer.html'),

    /**
     * @property events
     * @type {Object}
     */
    events: {
      'click .mas-relationPathsExplorerToggle': function () {
        if (this.isSelecting) {
          this.endSelection();
        } else {
          this.startSelection();
        }
      }
    },

    /**
     * @method initialize
     */
    initialize: function () {
      BaseView.prototype.initialize.apply(this, arguments);
      this.$toggle = this.$('.mas-relationPathsExplorerToggle');
      this.$pathsList = this.$('.mas-relationPathsExplorerPaths');
      this.listenTo(this.options.relationshipService.get('filters'), 'add remove reset', this.updateToggleAvailability);
      this.listenTo(this.options.relationshipService.get('filters'), 'remove reset', this.maybeClearRelationPaths);
      this._boundSelectElement = _.bind(this.selectElement, this);
      this.render();
    },

    /**
     * @method render
     */
    render: function () {
      this.updateToggleAvailability(null, this.options.relationshipService.get('filters'));
    },

    /**
     * updateToggleAvailability
     * @param service
     * @param filters
     */
    updateToggleAvailability: function (service, filters) {
      if (filters.isEmpty()) {
        this.$toggle.prop('disabled', true);
      } else {
        this.$toggle.prop('disabled', false);
      }
    },

    /**
     * @method maybeClearRelationPaths
     * @param service
     * @param filters
     */
    maybeClearRelationPaths: function (service, filters) {
      if (filters.isEmpty()) {
        this.$pathsList.empty();
      }
    },

    /**
     * @method displayRelationPaths
     * @param relatedFiber
     */
    displayRelationPaths: function (relatedFiber) {
      this.$el.addClass('mas-relationPathsExplorer-loading');
      this.options.aggregatorClient.getSchema().then(_.bind(function (schema) {
        // TODO: Maybe make it its own component (RelationPathView). taking a `from` and `to` fiber?
        var selectedFiber = this.options.relationshipService.get('filters').at(0);
        var relationPath = selectedFiber.get('l.relationPaths')[relatedFiber.get('l.logicalId')];
        var currentItemType = selectedFiber.itemType.id;
        var steps = _.reduce(relationPath, function (result, relation) {
          result.push(this.createStep('relation',relation, schema.edges));
          var parts = relation.split(':');
          if (currentItemType.indexOf(parts[0]) !== -1) {
            currentItemType = parts[1];
          } else {
            currentItemType = parts[0];
          }
          result.push(this.createStep('itemType', currentItemType, schema.nodes));
          return result;
        }, [this.createStep('itemType', currentItemType, schema.nodes)], this);
        this.$el.removeClass('mas-relationPathsExplorer-loading');
        this.$pathsList.empty().append(steps);
      }, this));
    },

    /**
     * @method createStep
     * @param type
     * @param content
     * @param itemTypes
     * @returns {*|JQuery|jQuery}
     */
    createStep: function (type, content, itemTypes) {
      if (type === 'relation') {
        content = this.getRelationName(content, itemTypes);
      } else {
        content = this.getItemTypeName(content, itemTypes);
      }
      var $result = $('<li class="mas-relationPathStep mas-listItem">')
        .text(content)
        .addClass('mas-relationPathStep-' + type);
      if (type === 'relation' && content === 'Stitched') {
        $result.addClass('mas-relationPathStep-stichedRelation');
      }
      return $result;
    },

    /**
     * @method getRelationName
     * @param relationId
     * @param relationTypes
     * @returns {string}
     */
    getRelationName: function (relationId, relationTypes) {
      var relationName = 'Standard';
      var relationParts = relationId.split(':');
      var relation = _.find(relationTypes, function (relationType) {
        return (relationType.data.source === relationParts[0] && relationType.data.target === relationParts[1]) ||
          (relationType.data.source === relationParts[1] && relationType.data.target === relationParts[0]);
      });
      if (relation && relation.data.stitch) {
        relationName = 'Stitched';
      }
      if (relation && relation.data.name) {
        relationName = relation.data.name;
      }
      return relationName;
    },

    /**
     * @method getItemTypeName
     * @param itemTypeId
     * @param itemTypes
     * @returns {*}
     */
    getItemTypeName: function (itemTypeId, itemTypes) {
      var itemType = _.find(itemTypes, function (itemType) {
        return itemType.data.id === itemTypeId;
      });
      return itemType ? itemType.data.name : itemTypeId;
    },

    /**
     * @method selectElement
     * @param event
     */
    selectElement: function (event) {
      event.preventDefault();
      var $target = $(event.target).closest('.mas-element');
      if ($target.hasClass('is-related')) {
        this.displayRelationPaths($target.data('view').model);
      }
      this.endSelection();
    },

    /**
     * @method startSelection
     */
    startSelection: function () {
      this.isSelecting = true;
      document.body.addEventListener('click', this._boundSelectElement, true);
      this.$toggle.text('Cancel').addClass('mas-relationPathsExplorerToggle-active');
    },

    /**
     * @method endSelection
     */
    endSelection: function () {
      this.isSelecting = false;
      document.body.removeEventListener('click', this._boundSelectElement, true);
      this.$toggle.text('Pick fiber').removeClass('mas-relationPathsExplorerToggle-active');
    }
  });

  return RelationPathsExplorer;
});
