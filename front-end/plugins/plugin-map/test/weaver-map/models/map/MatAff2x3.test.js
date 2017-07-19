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
/* global describe, it, sinon, expect, beforeEach, after, afterEach */
/* jshint expr: true */
define(function (require) {
  "use strict";

  var _ = require('lodash');
  var MatAff2x3 = require('weaver-map/models/map/MatAff2x3');

  describe('MatAff2x3', function () {


    describe('invert', function () {

      it('Should not modify the current matrix', function () {

        var m = new MatAff2x3(2, 3, -3, 2, -4, 0.0003);
        var i = m.invert();

        expect(m.rawMat).to.eql([
          [2, -3, -4],
          [3, 2, 0.0003],
          [0, 0, 1],
        ]);
      });

      it('Should compute the invert of an affine transform (scalar)', function () {

        var m = new MatAff2x3(2, 0, 0, 2, 0, 0);
        var i = m.invert();

        var a = 1/2;
        expect(i.rawMat).to.eql([
          [a, -0, 0],
          [-0, a, 0],
          [0, 0, 1],
        ]);
      });

      it('Should compute the invert of an affine transform (translation)', function () {

        var m = new MatAff2x3(1, 0, 0, 1, 3, -234.34);
        var i = m.invert();

        expect(i.rawMat).to.eql([
          [1, 0, -3],
          [0, 1, 234.34],
          [0, 0, 1],
        ]);
      });
    });
  });

});