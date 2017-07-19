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
/* jshint expr: true, strict: false */
define([
  'backbone',
  'weaver/views/Element/AlertDetailsView'
], function (Backbone, AlertDetailsView) {

  describe('weaver/views/Element/AlertDetailsView.js', function () {

    beforeEach(function () {

      this.alert = new Backbone.Model({
        level: 10,
        description: 'Mamma mia, what a mess!',
        count: 3
      });

      this.view = new AlertDetailsView({
        model: this.alert
      });
    });

    it('Should display the values of the alert', function () {

      expect(this.view.$alertLevel).to.have.text('' + this.alert.get('level'));
      expect(this.view.$alertDescription).to.have.text('' + this.alert.get('description'));
      expect(this.view.$alertCount).to.have.text('' + this.alert.get('count'));

      this.alert.set({
        level: 2,
        description: 'Chill out, just a small glitch',
        count: 1
      });

      expect(this.view.$alertLevel).to.have.text('' + this.alert.get('level'));
      expect(this.view.$alertDescription).to.have.text('' + this.alert.get('description'));
      expect(this.view.$alertCount).to.have.text('' + this.alert.get('count'));
    });

    it('Should hide the counter if it empty', function () {

      this.alert.set({
        level: 1,
        description: 'Ooops, little hiccup :s',
        count: undefined
      });

      expect(this.view.$el).to.have.class('mas-alertDetails-noCount');
    });
  });
});