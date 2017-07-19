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
  var Q = require('q');
  var fastdom = require('fastdom');

  /**
   * @class getClientRect
   * @param menu
   * @returns {(function(any=): JQueryPromise<T>)|*|promise.promise|jQuery.promise|(function(string=, Object=): JQueryPromise<any>)|promise}
   */
  function getClientRect(menu) {
    var deferred = Q.defer();
    fastdom.read(function () {
      var middleX = document.body.clientWidth / 2;
      var middleY = document.body.clientHeight / 2;
      var rect = menu.$('.mas-menu--toggle')[0].getBoundingClientRect();
      var elementX = rect.left + (rect.left - rect.right) / 2;
      var elementY = rect.top + (rect.top - rect.bottom) / 2;
      deferred.resolve({
        right: elementX > middleX,
        bottom: elementY > middleY
      });
    });
    return deferred.promise;
  }

  /**
   * @class updateContentPosition
   * @param menu
   * @param quadrant
   */
  function updateContentPosition(menu, quadrant) {
    if (quadrant.bottom) {
      menu.$el.addClass('mas-menu-expandsUp');
      menu.$el.removeClass('mas-menu-expandsDown');
    } else {
      menu.$el.addClass('mas-menu-expandsDown');
      menu.$el.removeClass('mas-menu-expandsUp');
    }
    if (quadrant.right) {
      menu.$el.addClass('mas-menu-rightAligned');
    } else {
      menu.$el.removeClass('mas-menu-rightAligned');
    }
  }

  /**
   * @class positionMenuContent
   * @param menu
   * @returns {DeserializeHelper|JQueryGenericPromise<U>|*|JQueryPromise<any>|JQueryPromise<U>}
   */
  function positionMenuContent(menu) {
    return getClientRect(menu).then(function (clientRect) {
      updateContentPosition(menu, clientRect);
    });
  }

  positionMenuContent.getClientRect = getClientRect;
  positionMenuContent.updateContentPosition = updateContentPosition;
  return positionMenuContent;

});