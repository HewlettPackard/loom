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
/* global describe, it, sinon, expect */
/* jshint expr: true */
define(function (require) {

  "use strict";

  var Weaver = require('weaver/Weaver');
  describe.skip('weaver/Weaver.js', function () {
    describe('start()', function () {
      it('Should request the list of patterns available on the aggregator', sinon.test(function () {
        new Weaver().start();
        expect(this.requests[0].url).to.equal('/loom/providers');
      }));
    });
  });
});