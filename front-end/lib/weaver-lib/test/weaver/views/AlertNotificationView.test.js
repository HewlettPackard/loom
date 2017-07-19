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
/* global describe, it, expect, beforeEach */
/* jshint expr: true */
define([
  'backbone',
  'weaver/views/Element/AlertNotificationView'
], function (Backbone, AlertNotificationView) {

  'use strict';

  describe('weaver/views/Element/AlertNotificationView.js', function () {

    beforeEach(function () {

      this.alert = new Backbone.Model({
        level: 3,
        description: 'A mild alert'
      });

      this.view = new AlertNotificationView({
        model: this.alert
      });
    });

    it('Should display the level of alert and have a class reflecting it', function () {

      expect(this.view.$el).to.have.class('mas-alertNotification-3');
    });

    it('Should update when the alert level changes', function () {

      this.alert.set('level', 19);
      // Alert levels higher than 10 will display '!'
      expect(this.view.$el).not.to.have.class('mas-alertNotification-3');
      expect(this.view.$el).to.have.class('mas-alertNotification-19');
    });
  });
});