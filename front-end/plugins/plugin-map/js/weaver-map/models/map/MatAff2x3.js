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

  var MatAff2x3 = function MatAff2x3(a, b, c, d, e, f) {
    this.rawMat = [
      [a, c, e],
      [b, d, f],
      [0, 0, 1],
    ];
  };

  MatAff2x3.prototype._rawMult = function (vec) {
    var out = [0, 0, 1];
    for (var i = this.rawMat.length - 1; i >= 0; --i) {
      for (var j = this.rawMat[i].length - 1; j >= 0; --j) {
        out[i] += this.rawMat[i][j] * vec[j];
      }
    }
    return out;
  };

  MatAff2x3.prototype._rawScale = function (value) {
    for (var i = this.rawMat.length - 2; i >= 0; --i) {
      for (var j = this.rawMat[i].length - 2; j >= 0; --j) {
        this.rawMat[i][j] *= value;
      }
    }
  };

  MatAff2x3.prototype.invert = function () {
    var a = this.rawMat[0][0];
    var b = this.rawMat[1][0];
    var c = this.rawMat[0][1];
    var d = this.rawMat[1][1];
    var e = this.rawMat[0][2];
    var f = this.rawMat[1][2];
    var det = a * d - b * c;

    var out = new MatAff2x3(d, -b, -c, a, 0, 0);

    out._rawScale(1 / det);

    var translate = out._rawMult([-e, -f, 1]);

    out.rawMat[0][2] = translate[0];
    out.rawMat[1][2] = translate[1];

    return out;
  };

  MatAff2x3.prototype.toString = function () {
    var str = "";
    for (var j = 0; j < 3; ++j) {
      for (var i = 0; i < 2; ++i) {
        str += this.rawMat[i][j] + ", ";
      }
    }
    return str.substr(0, str.length - 2);
  };

  return MatAff2x3;
});