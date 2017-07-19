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

  var BarGraphView = require('weaver/views/Element/BarGraphView');

  if (!window.PHANTOMJS) {
    describe('weaver/views/BarGraphView', function () {

      // Create a space in the DOM to put the views in
      before(function () {

        this.barGraphViews = document.createElement('div');
        document.body.appendChild(this.barGraphViews);
      });

      beforeEach(function () {

        this.barGraphView = new BarGraphView({
          maximumNumberOfValues: 5
        });
        this.barGraphViews.appendChild(this.barGraphView.el);

        this.barGraphView.$el.css({
          height: '20px',
          width: '50px',
          float: 'left',
          background: 'hsl(350, 50%, 50%)',
          'margin-right': '10px'
        });
      });

      describe('render()', function () {

        it('Renders the value', function () {

          this.barGraphView.render(0.5);
        });

        it('Renders a list of value', function () {
          this.barGraphView.render([0.5, 0.8, 0.4]);
        });

        it('Renders according to top origin', function () {
          this.barGraphView.render(0.5, 1);
        });

        it('Renders according to random origin', function () {
          this.barGraphView.render(0.3, 0.75);
        });
      });

      describe('renderOriginAxis()', function () {

        afterEach(function () {
          this.barGraphView.$axis.css({
            stroke: 'hsl(35, 50%, 70%)',
            strokeSize: '1px'
          });
        });

        it('Renders an origin axis at bottom', function () {

          this.barGraphView.render(0.5);
          this.barGraphView.renderOriginAxis(0);

        });

        it('Renders an origin axis at the top', function () {
          this.barGraphView.render(0.5, 1);
          this.barGraphView.renderOriginAxis(1);
        });

        it('Renders an origin axis at provided value', function () {

          this.barGraphView.render(0.80, 0.33);
          this.barGraphView.renderOriginAxis(0.33);
        });

        it('Renders negative values with an origin axis', function () {
          this.barGraphView.render(0.12, 0.63);
          this.barGraphView.renderOriginAxis(0.63);
        });
      });
    });
  }
});
