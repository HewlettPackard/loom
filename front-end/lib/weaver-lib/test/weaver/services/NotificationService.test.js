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
    'weaver/services/NotificationService',
    'weaver/utils/EventBus',
    'backbone'
  ],
  function (NotificationService, EventBus, Backbone) {
    "use strict";

    describe('weaver/services/NotificationService.js', function () {
      beforeEach(function() {
        this.tapestry = new Backbone.View();
        this.service = new NotificationService(this.tapestry);
      });
      describe('EventBus monitoring', function() {
        it('Should react to screen:notify:braiding:narrower events', function () {
          sinon.stub(NotificationService.prototype, 'showNotification');
          EventBus.trigger('screen:notify:braiding:narrower', {});
          expect(this.service.showNotification.called).to.be.true;
          NotificationService.prototype.showNotification.restore();
        });
        it('Should react to screen:notify:braiding:wider events', function () {
          sinon.stub(NotificationService.prototype, 'showNotification');
          EventBus.trigger('screen:notify:braiding:wider', {});
          expect(this.service.showNotification.called).to.be.true;
          NotificationService.prototype.showNotification.restore();
        });
        it('Should react to notification:cancel events', function () {
          sinon.stub(NotificationService.prototype, 'clearNotification');
          EventBus.trigger('notification:cancel', {});
          expect(this.service.clearNotification.called).to.be.true;
          NotificationService.prototype.clearNotification.restore();
        });
      });
      describe('showNotification', function() {
        it('Should clear the notification when set', function() {
          var notification = new Backbone.View();
          this.service.showNotification(notification);
          expect(this.service.notification).to.eql(notification);
        });
      });
      describe('clearNotification', function() {
        it('Should clear the notification when set', function() {
          this.service.notification = new Backbone.View();
          this.service.clearNotification();
          expect(this.service.notification).to.be.null;
        });
      });
    });
  });
