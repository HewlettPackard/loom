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
/* global describe, it, sinon, expect, beforeEach */
/* jshint expr: true */
define(function (require) {

  "use strict";

  var TapestryController = require('weaver/screens/TapestryScreen/ThreadMonitorController');
  var BaseView = require('weaver/views/BaseView');

  describe('weaver/screens/TapestryScreen/ThreadMonitorController.js', function () {

    beforeEach(function () {

      this.tapestryMock = {
        add: sinon.spy()
      };

      this.baseView = new BaseView();

      this.tapestryController = new TapestryController({
        el: this.baseView.el,
        model: this.tapestryMock
      });
    });


    it('Should add a Thread to the Tapesty when a Thread gets displayed', function () {

      var thread = {};

      this.baseView.dispatchCustomEvent('didDisplayThread', {thread: thread});

      expect(this.tapestryMock.add).to.have.been.calledWith(thread);
    });
  });
});