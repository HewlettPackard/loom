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
define([

  'lodash',
  'weft/models/Sort',
  'weft/models/Metric',
  'weft/models/Thread',
  'weaver/views/ThreadViewHeader/SortingMenu'
], function (_, Sort, Metric, Thread, SortingMenu) {

  "use strict";

  function simulateClick(element) {

    var event = document.createEvent('MouseEvent');
    event.initEvent('click', true, true);
    element.dispatchEvent(event);
  }

  describe('weaver/views/ThreadViewHeader/SortingMenu.js', function () {

    beforeEach(function () {

      this.thread = new Thread();

      this.sorts = _.times(3, function (index) {

        return new Sort({
          id: index,
          property: new Metric({
            id: index,
            name: 'Metric #' + index
          }),
          order: Sort.ORDER_ASCENDING
        });
      });

      this.thread.availableSorts.add([
        this.sorts[0],
        this.sorts[1]
      ]);

      this.view = new SortingMenu({
        model: this.thread
      });

      document.body.appendChild(this.view.el);

      this.view.expand();
    });

    afterEach(function () {
      document.body.removeChild(this.view.el);
    });

    it.skip('Should highlight if the Thread is sorted', function () {

      this.thread.sortBy(new Sort({
        id: 'name'
      }));

      expect(this.view.$el).to.have.class('is-active');

      this.thread.sortBy();

      expect(this.view.$el).not.to.have.class('is-active');
    });

    it.skip('Should display the available sort options for the Thread', function () {

      var options = this.view.$('.mas-propertySelector--property');

      expect(options.length).to.equal(3);
      expect(options).to.contain('Default order');
      expect(options).to.contain('Metric #0');
      expect(options).to.contain('Metric #1');
    });


    it.skip('Should select sort when clicked', function () {

      var $selectedOption = this.view.$('.mas-propertySelector--property:contains(#1)');

      simulateClick($selectedOption[0]);

      var sort = this.thread.get('sort');
      expect(sort.id).to.equal(1);

      // Checks that clicking to select didn't reverse original order
      expect(sort.get('order')).to.equal(Sort.ORDER_ASCENDING);
    });

    it.skip('Should reverse sort order when clicking on selected element', function () {

      var $selectedOption = this.view.$('.mas-propertySelector--property:contains(#1)');

      // First click to select the option
      simulateClick($selectedOption[0]);
      // Second click to reverse the order
      simulateClick($selectedOption[0]);

      expect(this.thread.get('sort').get('order')).to.equal(Sort.ORDER_DESCENDING);
    });

    it.skip('Should be disabled if the Thread has no sort option', function () {

      var menu = new SortingMenu({
        model: new Thread()
      });
      menu.expand();

      expect(menu.$el).to.have.class('is-disabled');
      expect(menu.$('.mas-menu--toggle')).to.be.disabled;

      menu.model.availableSorts.set([this.sorts[1]]);

      expect(menu.$el).not.to.have.class('is-disabled');
      expect(menu.$('.mas-menu--toggle')).not.to.be.disabled;

      menu.model.availableSorts.set([]);

      expect(menu.$el).to.have.class('is-disabled');
      expect(menu.$('.mas-menu--toggle')).to.be.disabled;
    });

    it.skip('Should be disabled if the Thread gets `outdated`', function () {

      var menu = new SortingMenu({
        model: new Thread()
      });
      menu.model.availableSorts.set([this.sorts[1]]);

      menu.model.set('outdated', true);

      expect(menu.$el).to.have.class('is-disabled');

      menu.model.set('outdated', false);

      expect(menu.$el).not.to.have.class('is-disabled');
    });
  });
});
