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

  'use strict';

  var Item = require('weft/models/Item');
  var Aggregation = require('weft/models/Aggregation');
  var AbstractElementView = require('weaver/views/Element/AbstractElementView');

  describe('weaver/views/AbstractElementView.js', function () {

    describe('Meaningful width', function () {

      it('Should set a default width when the view displays an item', function () {

        var model = new Item();
        var view = new AbstractElementView({
          model: model
        });

        // Cast to string due to varying browser representations as Number or String :(
        expect(parseInt(view.el.style.flexGrow || view.el.style.msFlexPositive)).to.equal(1);
      });

      it('Should adjust the width of the view to the Aggregation `numberOfItems`', function () {

        var model = new Aggregation({
          numberOfItems: 10
        });

        var view = new AbstractElementView({
          model: model
        });

        expect(parseInt(view.el.style.flexGrow || view.el.style.msFlexPositive)).to.equal(10);
      });

      it('Should get adjusted when the number of items change in an Aggregation', function () {

        var model = new Aggregation({
          numberOfItems: 10
        });

        var view = new AbstractElementView({
          model: model
        });

        model.set('numberOfItems', 25);

        expect(parseInt(view.el.style.flexGrow || view.el.style.msFlexPositive)).to.equal(25);
      });
    });

  });
});
