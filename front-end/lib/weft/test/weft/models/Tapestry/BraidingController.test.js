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
define( function (require) {

  "use strict";

  var Backbone = require('backbone');
  var _ = require('lodash');
  var Thread = require('weft/models/Thread');
  var BraidingController = require('weft/models/Tapestry/BraidingController');

  describe('weft/models/Tapestry/BraidingController.js', function () {

    beforeEach(function () {

      this.threads = _.times(3, function (index) {
        return new Thread({
          id: index
        });
      });

      this.controller = new BraidingController({
        braiding: 200,
        threads: new Backbone.Collection(this.threads)
      });
    });

    it('Sets the braiding of the Threads to the configured braiding', function () {

      _.forEach(this.threads, function (thread) {

        expect(thread.get('query').get('limit').parameters.maxFibres).to.equal(200);
      }, this);
    });

    it('Updates Threads braiding when they get added to the list', function () {

      var newThread = new Thread();

      this.controller.get('threads').add(newThread);

      expect(newThread.get('query').get('limit').parameters.maxFibres).to.equal(200);
    });

    it('Updates Threads braiding when the braiding value changes', function () {

      this.controller.set('braiding', 400);

      _.forEach(this.threads, function (thread) {

        expect(thread.get('query').get('limit').parameters.maxFibres).to.equal(400);
      }, this);
    });
  });
});