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
/* global describe, it, expect, before */
/* jshint expr: true */
define(function (require) {

  "use strict";

  var Aggregation = require('weft/models/Aggregation');
  var Operation = require('weft/models/Operation');
  var Thread = require('weft/models/Thread');
  var ItemType = require('weft/models/ItemType');
  var ThreadTitleView = require('weaver/views/ThreadViewHeader/ThreadTitleView');

  describe('weaver/views/ThreadTitleView.js', function () {

    before(function () {

      var itemType = new ItemType({
        attributes: {
          'a-property': {
            name: 'A property',
            unit: 'unit(s)',
            plottable: true
          }
        }
      });

      this.thread = new Thread({
        name: 'Some name',
        itemType: itemType
      });

      this.thread.pushOperation({
        operator: Operation.GROUP_BY_ID,
        parameters: {
          property:'a-property'
        }
      });

      this.group = new Thread({
        parent: this.thread,
        name: 'Value',
        aggregation: new Aggregation({
          'l.tags': Operation.GROUP_BY_ID
        }),
        itemType: itemType
      });
    });

    it('Should display the `name` property of its model', function () {

      var view = new ThreadTitleView({
        model: this.thread
      });

      expect(view.$el).to.have.text(this.thread.get('name'));
    });

    it('Should display the name of the property making the group if the Thread displays a group', function () {

      var view = new ThreadTitleView({
        model: this.group
      });

      expect(view.$el).to.contain('A property');
      expect(view.$el).to.contain('Value');
    });
  });
});
