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
  var Backbone = require('backbone');
  var Pattern = require('weft/models/Pattern');
  var Tapestry = require('weft/models/Tapestry');
  var Thread = require('weft/models/Thread');
  var PatternInTapestryMarker = require('weft/services/PatternInTapestryMarker');

  describe('weft/services/PatternInTapestryMarker.js', function () {

    beforeEach(function () {

      this.threadDefinitions = _.times(2, function (index) {
        return new Thread({
          itemType: {
            id: '/itemType/' + index
          }
        }, {parse: true});
      });

      this.pattern = new Pattern({
        threads: this.threadDefinitions
      });

      this.otherPattern = new Pattern({
        threads: [this.threadDefinitions[0]]
      });

      this.tapestry = new Tapestry();

      this.marker = new PatternInTapestryMarker({
        patterns: new Backbone.Collection([this.pattern, this.otherPattern]),
        tapestry: this.tapestry
      });
    });

    it('Should mark patterns when Threads get added or removed to the Tapestry', sinon.test(function () {

      var thread = this.threadDefinitions[0].clone();
      this.tapestry.get('threads').add(thread);

      expect(this.otherPattern.get('isInTapestry')).to.be.true;

      var secondThread = this.threadDefinitions[1].clone();
      this.tapestry.get('threads').add(secondThread);

      expect(this.pattern.get('isInTapestry')).to.be.true;
      expect(this.otherPattern.get('isInTapestry')).to.be.true;


      this.tapestry.get('threads').remove(thread);

      expect(this.pattern.get('isInTapestry')).to.be.false;
      expect(this.otherPattern.get('isInTapestry')).to.be.false;
    }));
  });
});
