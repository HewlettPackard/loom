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

  var Backbone = require('backbone');
  var _ = require('lodash');
  var Pattern = require('weft/models/Pattern');
  var PatternList = require('weaver/views/SideMenu/Tapestry/Patterns/PatternList');

  describe('weeaver/views/patterns/PatternList.js', function () {

    beforeEach(function () {

      this.patterns = _.times(3, function (index) {
        return new Pattern({
          id: index,
          name: 'Pattern #' + index,
          isInTapestry: (index % 2)
        });
      });

      this.menu = new PatternList({
        model: new Backbone.Collection(this.patterns)
      });
    });

    it('Should display the list of Patterns and highlight those in the Tapestry', function () {

      _.forEach(this.patterns, function (pattern) {

        var $li = this.menu.$('.mas-pattern-' + pattern.get('id'));
        expect($li).to.contain(pattern.get('name'));

        if (pattern.get('isInTapestry')) {
          expect($li).to.have.class('is-inTapestry');
        } else {
          expect($li).not.to.have.class('is-inTapestry');
        }
      }, this);
    });

    it('Should update which pattern is in the Tapestry', function () {

      this.patterns[0].set('isInTapestry', true);
      this.patterns[1].set('isInTapestry', false);

      expect(this.menu.$('.mas-pattern-0')).to.have.class('is-inTapestry');
      expect(this.menu.$('.mas-pattern-1')).not.to.have.class('is-inTapestry');
    });

    it('Should get updated when the list of pattern is updated', function () {

      this.patterns[3] = new Pattern({
        id: 3,
        name: 'Pattern #3',
        isInTapestry: true
      });

      this.menu.model.set([this.patterns[0], this.patterns[3]]);

      var $patterns = this.menu.$('.mas-pattern');
      expect($patterns.length).to.equal(2);
      [0, 3].forEach(function (id, index) {

        var pattern = this.patterns[id];
        expect($patterns.eq(index)).to.contain(pattern.get('name'));
      }, this);
    });
  });
});
