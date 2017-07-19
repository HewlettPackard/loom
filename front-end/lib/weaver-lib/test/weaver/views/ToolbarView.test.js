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
  var ToolbarView = require('weaver/views/Toolbar/ToolbarView');
  var ServiceManager = require('weaver/utils/ServiceManager');

  describe('weaver/views/ToolbarView.js', function () {

    describe('showNotification()', function () {
      beforeEach(function () {
        this.view = new ToolbarView({serviceManager: ServiceManager});
        document.body.appendChild(this.view.el);
        this.$notification = $('<p class="my-notification">Notification</p>');
      });

      afterEach(function () {
        this.view.remove();
      });

      it('Should display provided notification', function () {
        this.view.showNotification(this.$notification);
        expect(this.view.$el).to.have.descendants('.my-notification');
        expect(this.$notification).to.have.class('mas-toolbar--notification');
      });

      it('Should replace previous notification', function () {
        this.view.showNotification(this.$notification);
        var $newNotification = $('<p class="my-newNotification">New notification</p>');
        this.view.showNotification($newNotification);
        expect(this.view.$el).to.have.descendants('.my-newNotification');
        expect($newNotification).to.have.class('mas-toolbar--notification');
        expect(this.$notification).not.to.have.class('mas-toolbar--notification');
      });

      it('Should dispatch an event notifying the new height of the scrollbar', function (done) {
        $(document.body).on('change:height', function checkHeightChange () {
          $(document.body).off('change:height', checkHeightChange);
          done();
        });
        this.view.showNotification(this.$notification);
      });
    });
  });
});
