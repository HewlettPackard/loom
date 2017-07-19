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

  var _ = require('lodash');
  var $ = require('jquery');
  var Backbone = require('backbone');
  var Thread = require('weft/models/Thread');
  var Pattern = require('weft/models/Pattern');
  var PatternController = require('weaver/views/SideMenu/Tapestry/Patterns/PatternController');
  var BraidingController = require('weft/models/Tapestry/BraidingController');

  describe('weaver/screens/TapestryScreen/PatternController.js', function () {

    beforeEach(function () {
      this.pattern = new Pattern({
        threads: _.times(2, function () {
          return new Thread();
        })
      });

      this.$patternToggle = $('<div class="mas-pattern">').data('pattern', this.pattern);

      this.patternController = new PatternController({
        model: new Backbone.Collection(),
        braidingController: new BraidingController()
      });
      this.patternController.$el.append(this.$patternToggle).appendTo(document.body);
    });

    afterEach(function () {
      this.patternController.remove();
    });

    it('Displays pattern when clicked', function () {

      this.$patternToggle.click();

      expect(this.patternController.model.size()).to.equal(2);

    });
  });

});
