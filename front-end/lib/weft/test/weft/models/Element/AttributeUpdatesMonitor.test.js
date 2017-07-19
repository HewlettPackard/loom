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
/* global describe, it, expect, beforeEach, sinon */
/* jshint expr: true */
define(function (require) {

  "use strict";

  var Item = require('weft/models/Item');

  // Issues with the timeout mocking with SinonJS
  describe.skip('weft/models/Element/AttributeUpdatesMonitor', function () {

    beforeEach(function () {

      this.element = new Item({}, {
        itemType: {
          attributes: {
            'a-property': {
              visible: true
            },
            'a-non-displayed-property': {
              visible: false
            }
          }
        }
      });
    });

    it('Should update the state of the element when its displayable properties change', sinon.test(function () {

      this.element.set('a-property', 'some-value');
      expect(this.element.state).to.equal(Element.STATE_UPDATED);
      this.clock.tick(this.element.getStateChangeTimeout("updated"));
      expect(this.element.state).to.be.undefined;
    }));

    it('Should not update the state of the element when it is not a displayable property that changed', sinon.test(function () {

      this.element.set('a-non-displayed-property', 'some-value');
      expect(this.element.state).to.be.undefined;
    }));
  });
});