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

  //todo: Warning! Is this still true? Investigate.. Lodash now at 3.10.1
  // escapeRegExp function pulled from Lodash 3.10.1
  // as Weaver uses 2.4.1 for now
  // TODO: Test if updating to 3.10.1 breaks anything
  // and if not, delete this

  var reRegExpChars = /[\\^$.*+?()[\]{}|]/g,
    reHasRegExpChars = new RegExp(reRegExpChars.source);

  function baseToString(value) {
    return value === null ? '' : (value + '');
  }

  /**
   * Escapes the `RegExp` special characters "^", "$", "\", ".", "*", "+",
   * "?", "(", ")", "[", "]", "{", "}", and "|" in `string`.
   *
   * @static
   * @memberOf _
   * @category String
   * @param {string} [string=''] The string to escape.
   * @returns {string} Returns the escaped string.
   * @example
   *
   * _.escapeRegExp('[lodash](https://lodash.com/)');
   * // => '\[lodash\]\(https://lodash\.com/\)'
   */
  function escapeRegExp(string) {
    string = baseToString(string);
    if (string && reHasRegExpChars.test(string)) {
      return string.replace(reRegExpChars, '\\$&');
    } else {
      return string;
    }
  }

  return escapeRegExp;
});