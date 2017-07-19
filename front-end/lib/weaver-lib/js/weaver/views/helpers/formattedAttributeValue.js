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
/**
 * @module weaver
 * @submodule views.helpers
 * @namespace  views.helpers
 */
define(function (require) {

  "use strict";

  var _ = require('lodash');
  var numeral = require('numeral');
  var moment = require('moment');
  require('moment-jdateformatparser');

  /**
   * @class formattedAttributeValue
   * @param fiber
   * @param attributeId
   * @param options
   * @returns {*}
   */
  function formattedAttributeValue(fiber, attributeId, options) {
    options = options || {};
    var result = fiber.get(attributeId);
    if (result === 'l.undefined' || result === undefined) {
      return '';
    }
    var attribute = fiber.itemType.getAttribute(attributeId);
    if (attribute) {
      var formater = formattedAttributeValue.formatters[attribute.type] || formattedAttributeValue.formatters['default'];
      return formater(result, attribute, options);
    }
    return result;
  }

  /**
   * @property formatters
   * @final
   * @type {{numeric: formattedAttributeValue.formatters.numeric, time: formattedAttributeValue.formatters.time, default: formattedAttributeValue.formatters.default}}
   */
  formattedAttributeValue.formatters = {
    /**
     * @method numeric
     * @param value
     * @param attribute
     * @returns {*}
     */
    numeric: function (value, attribute) {
      var floatValue = parseFloat(value);
      if (_.isNaN(floatValue)) {
        return 'NaN';
      }
      var result = numeral(floatValue).format('0.[00]');
      if (attribute.unit) {
        result += '&hairsp;' + attribute.unit;
      }
      return result;
    },

    /**
     * @method time
     * @param value
     * @param attribute
     * @param options
     * @returns {*}
     */
    time: function (value, attribute, options) {
      var format;
      if (options.short) {
        format = attribute.shortFormat;
      }
      format = format || attribute.format || 'YY/MM/DD HH:mm:ss';
      if (format === 'fromNow') {
        return moment(value).fromNow();
      } else {
        return moment(value).formatWithJDF(format);
      }
    },

    /**
     * @method default
     * @param value
     * @returns {*}
     */
    default: function (value) {
      return value;
    }
  };

  return formattedAttributeValue;
});
