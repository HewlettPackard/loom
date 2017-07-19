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

  var Backbone = require('backbone');
  var CollectionNotificationMenu = require('weaver/views/CollectionNotificationMenu');

  describe('weaver/views/CollectionNotificationMenu.js', function() {

    before(function() {      
      this.collection = new Backbone.Collection();
      this.menu = new CollectionNotificationMenu({
        model: this.collection
      });
      this.menu.collapse();
    });

    it('Displays a notification when an item gets in the collection', function () {
      this.collection.add(new Backbone.Model());
      expect(this.menu.$el).to.have.class('has-notification');
    });

    it('Clears the notification when the menu gets expanded', function () {
      this.menu.expand();
      expect(this.menu.$el).not.to.have.class('has-notification');
    });

    it('Displays a notification when an item gets removed from the collection', function () {
      this.menu.collapse();
      this.collection.remove(this.collection.at(0));
      expect(this.menu.$el).to.have.class('has-notification');
    });
  });
});