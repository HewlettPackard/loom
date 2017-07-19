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

  describe('weft/models/Element/UpdatedAttributesMonitor.js', function () {

    beforeEach(function () {

      this.element = new Item({}, {
        itemType: {
          attributes: {
            'a-property': {
              visible: true
            },
            'another-property': {
              visible: true
            },
            'yet-another-property': {
              visible: true
            },
            'a-fourth-property': {
              visible: true
            }
          }
        }
      });
    });

    it('Should store the list of properties that changed', function () {

      this.element.set({
        'a-property': 'some-value',
        'another-property': 'another-value'
      });

      var updatedAttributes = this.element.updatedAttributesMonitor.get('updatedAttributes');

      expect(updatedAttributes).to.deep.equal(['a-property', 'another-property']);
    });

    it('Should clear the list of properties when the object state goes back to normal', sinon.test(function () {

      this.element.set('a-property', 20);

      this.clock.tick(this.element.getStateChangeTimeout(Element.STATE_UPDATED));

      var updatedAttributes = this.element.updatedAttributesMonitor.get('updatedAttributes');
      expect(updatedAttributes).to.have.length(0);
    }));

    it('Should complete the list of updated properties when subsequent updates occur', function () {


      this.element.set('a-property', 10);

      var updatedAttributes = this.element.updatedAttributesMonitor.get('updatedAttributes');
      expect(updatedAttributes).to.deep.equal(['a-property']);

      this.element.set({
        'a-property': 'some-value',
        'another-property': 'another-value'
      });

      updatedAttributes = this.element.updatedAttributesMonitor.get('updatedAttributes');

      expect(updatedAttributes).to.deep.equal(['a-property', 'another-property']);
    });
  });
});