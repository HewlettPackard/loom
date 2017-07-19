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
/* global describe, it, expect */
/* jshint expr: true */
define(function (require) {

  "use strict";

  var Thread = require('weft/models/Thread');
  var ItemType = require('weft/models/ItemType');
  var MetricsMenu = require('weaver/views/ThreadViewHeader/MetricsMenu');

  describe('weaver/views/ThreadViewHeader/MetricsMenu.js', function () {

    var itemType = new ItemType ({
      attributes: {
        'cpuload': {
          name: 'CPU load',
          plottable: true
        },
        memory: {
          name: 'Memory use',
          plottable: true
        },
        'swap_avail': {
          name: 'Swap available',
          plottable: true
        }
      }
    });

    var thread = new Thread({
      itemType: itemType
    });

    var menu = new MetricsMenu({
      model: thread
    });


    it('Should allow the user to pick up a metric', function () {

      menu.propertySelector.$('li:contains(CPU load)').click();
      expect(thread.get('metrics').size()).to.equal(1);
      expect(thread.get('metrics').at(0).get('name')).to.equal('CPU load');
    });

    it('Should allow the user to pick up multiple metrics', function () {

      menu.propertySelector.$('li:contains(Swap available)').click();
      expect(thread.get('metrics').size()).to.equal(2);
      expect(thread.get('metrics').at(1).get('name')).to.equal('Swap available');
    });

    it('Should deselect metrics when metrics get removed from Thread', function () {

      menu.propertySelector.$('li:contains(Memory use)').click();

      expect(thread.get('metrics').last().id).to.equal('memory');

      thread.get('metrics').remove(thread.get('metrics').last());
      expect(thread.get('metrics').size()).to.equal(2);
      expect(menu.propertySelector.options.selection).to.deep.equal(['cpuload','swap_avail']);
      expect(menu.propertySelector.$('.mas-propertySelector--memory')).not.to.have.class('mas-property-selected');
    });

    it('Should allow the user to deselect a metric', function () {

      menu.propertySelector.$('li:contains(CPU load)').click();
      expect(thread.get('metrics').size()).to.equal(1);
      expect(thread.get('metrics').at(0).get('name')).to.equal('Swap available');
    });

    it.skip('Should allow the user to deselect all metrics at once', function () {
      menu.propertySelector.$('.mas-property-blank').click();
      expect(thread.get('metrics').size()).to.equal(0);
    });
  });
});