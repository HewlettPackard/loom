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
  var Item = require('weft/models/Item');
  var formattedAttributeValue = require('weaver/views/helpers/formattedAttributeValue');

  describe('weaver/views/helpers/formattedAttributeValue.js', function () {

    var item = new Item({
      'stringAttribute': 'String value',
      'numericAttribute': 123.456789,
      'numericAttributeWithUnit': 123.456789,
      'timeAttribute': '2015-10-21T05:25:09Z',
      'timeAttributeWithShortFormat': '2015-10-21T05:25:09Z',
      'undefinedAttribute': 'l.undefined',
      'undefinedString': 'undefined',
      'undefinedValue': undefined,
      'NaNAttribute': 'NaN'
    }, {
      itemType: {
        attributes: {
          'stringAttribute': {
            type: 'literal'
          },
          'undefinedAttribute': {
            type: 'literal'
          },
          'undefinedValue': {
            type: 'literal'
          },
          'undefinedString': {
            type: 'literal'
          },
          'numericAttribute': {
            type: 'numeric'
          },
          'NaNAttribute': {
            type: 'numeric'
          },
          'numericAttributeWithUnit': {
            type: 'numeric',
            unit: 'MB'
          },
          'timeAttribute': {
            type: 'time',
            format: 'MMM dd, YYYY'
          },
          'timeAttributeWithShortFormat': {
            type: 'time',
            format: 'MMM dd, YYYY',
            shortFormat: 'MMM dd'
          }
        }
      }
    });

    it('Formats numeric values to two decimals', function () {

      expect(formattedAttributeValue(item, 'numericAttribute')).to.equal('123.46');
    });

    it('Appends unit to numeric values if a unit is set in the attribute definition', function () {

      expect(formattedAttributeValue(item, 'numericAttributeWithUnit')).to.equal('123.46&hairsp;MB');
    });

    it('Displays the attribute value', function () {

      expect(formattedAttributeValue(item, 'stringAttribute')).to.equal('String value');
    });

    it('Formats time according to provided format', function () {
      expect(formattedAttributeValue(item, 'timeAttribute')).to.equal('Oct 21, 2015');
    });

    it('Optionally uses short format', function () {
      expect(formattedAttributeValue(item, 'timeAttributeWithShortFormat', {short: true})).to.equal('Oct 21', 'Should have used short format');
      expect(formattedAttributeValue(item, 'timeAttribute', {short: true})).to.equal('Oct 21, 2015', 'Should have used long format (no short format available)');
    });

    it('Formats l.undefined as an empty string', function () {
      expect(formattedAttributeValue(item, 'undefinedAttribute')).to.equal('');
    });

    it('Formats the value undefined as an empty string', function () {
      expect(formattedAttributeValue(item, 'undefinedValue')).to.equal('');
    });

    it('Formats the string "undefined" as the string "undefined"', function () {
      expect(formattedAttributeValue(item, 'undefinedString')).to.equal('undefined');
    });

    it('Handles NaN gracefully (silly numeral.js)', function () {
      expect(formattedAttributeValue(item, 'NaNAttribute')).to.equal('NaN');
    });
  });

});
