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
  var Item = require('weft/models/Item');
  var ElementView = require('weaver/views/Element/ElementView');
  var SortedLabellingStrategy = require('weaver/views/SortedLabellingStrategy');

  var UNIT = 'Kb';

  function createViews(numberOfViews) {

    var itemType = {
      attributes: {
        'name': {},
        'value': {
          type: 'literal'
        },
        'num': {
          type: 'numeric',
          unit: UNIT
        },
        'time': {
          type: 'time',
          shortFormat: "MMM dd"
        }
      }
    };

    return _.times(numberOfViews, function (index) {

      var item = new Item({
        name: 'Item #' + index,
        value: index + ' value',
        num: 10 + index, // Add 10 to make number different from the one in the name
        time: "2015-10-2" + index + "T05:25:09Z"
      },{
        itemType: itemType
      });

      return new ElementView({
        model: item
      });
    });
  }

  describe('weaver/views/SortedLabellingStrategy.js', function () {

    var labellingStrategy = new SortedLabellingStrategy();

    beforeEach(function () {
      this.views = createViews(5);
    });

    it('Renders the value used for sorting in the label', function () {

      labellingStrategy.attribute = 'value';
      labellingStrategy.updateLabels(this.views);

      this.views.forEach(function (view) {

        expect(view.$('.mas-fiberOverview--label')).to.contain(view.model.get('value'));
        expect(view.$('.mas-fiberOverview--label')).to.contain(view.model.get('name'));
      });
    });

    it('Displays units when sorting on a numeric value which has a unit', function () {

      labellingStrategy.attribute = 'num';
      labellingStrategy.updateLabels(this.views);

      this.views.forEach(function (view) {
        expect(view.$('.mas-fiberOverview--label')).to.contain(view.model.get('num'));
        expect(view.$('.mas-fiberOverview--label')).to.contain(UNIT);
        expect(view.$('.mas-fiberOverview--label')).to.contain(view.model.get('name'));
      });
    });

    it('Displays duration when sorting on a time value', function () {
      labellingStrategy.attribute = 'time';
      labellingStrategy.updateLabels(this.views);

      this.views.forEach(function (view, index) {
        expect(view.$('.mas-fiberOverview--label')).to.contain('Oct 2' + index);
      });
    });

    it('Does not render the name twice when sorting by name', function () {
      labellingStrategy.attribute = 'name';
      labellingStrategy.updateLabels(this.views);

      this.views.forEach(function (view) {
        expect(view.$('.mas-fiberOverview--label > *').length).to.equal(1);
      });
    });
  });
});
