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
define(function () {

  "use strict";

  var $ = require('jquery');
  var Thread = require('weft/models/Thread');
  var ItemType = require('weft/models/ItemType');
  var ThreadActionsMenu = require('weaver/views/ThreadViewHeader/ThreadActionsMenu');

  describe('weaver/views/ThreadViewHeader/ThreadActionsMenu.js', function () {

    before(function () {
      this.itemTypeWithThreadActions = new ItemType({
        actions: {
          thread: {
            'action-1': {
              id: 'action-1',
              name: 'Action #1',
              params: [{
                id: "filename",
                name: "filename",
                range: {},
                type: "STRING"
              }]
            },
            'action-2': {
              id: 'action-2',
              name: 'Action #2',
              params: [{
                id: "filename",
                name: "filename",
                range: {},
                type: "STRING"
              }]
            }
          }
        }
      });

      this.itemTypeWithoutActions = new ItemType();

      this.threadWithActions = new Thread({
        itemType: this.itemTypeWithThreadActions
      });

      this.threadWithoutActions = new Thread({
        itemType: this.itemTypeWithoutActions
      });

      this.menuWithActions = new ThreadActionsMenu({
        model: this.threadWithActions,
        el: $('<div><button class="mas-menu--toggle"></button></div>')[0]
      });

      this.menuWithoutActions = new ThreadActionsMenu({
        model: this.threadWithoutActions,
        el: $('<div><button class="mas-menu--toggle"></button></div>')[0]
      });

      document.body.appendChild(this.menuWithActions.el);
      document.body.appendChild(this.menuWithoutActions.el);
    });

    after(function () {
      this.menuWithActions.remove();
      this.menuWithoutActions.remove();
    });

    it('Allows users to list actions available on the Thread', function () {

      this.menuWithActions.expand();
      expect(this.menuWithActions.$el).to.have.descendants(':contains(Action #1)');
      expect(this.menuWithActions.$el).to.have.descendants(':contains(Action #2)');
    });

    it('Dispatches a custom event when users have picked their action', function (done) {

      var expectedThread = this.threadWithActions;

      this.menuWithActions.$el.one('didSelectAction', function (event) {

        expect(event.originalEvent.action.id).to.equal('action-1');
        expect(event.originalEvent.thread).to.equal(expectedThread);
        done();
      });

      this.menuWithActions.propertySelector.trigger('change:selection', 'action-1');
    });

    it('Gets disabled when the Thread has no actions', function () {
      expect(this.menuWithoutActions.$el).to.have.class('is-disabled');

      this.menuWithoutActions.enable();

      expect(this.menuWithoutActions.$el).to.have.class('is-disabled');
    });

  });

});
