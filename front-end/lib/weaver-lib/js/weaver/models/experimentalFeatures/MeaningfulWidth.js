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
define([
  'lodash',
  'weaver/models/ExperimentalFeature',
  'weaver/views/AbstractElementView'
], function (_, ExperimentalFeature, AbstractElementView) {

  "use strict";

  /**
   * @class  MeaningfulWidth
   * @module  weaver
   * @submodule models.experimentalFeatures
   * @namespace  models.experimentalFeatures
   */
  var MeaningfulWidth = new ExperimentalFeature({
    id: 'meaningful-width',
    name: 'Meaningful width'
  });

  /**
   * @method updateElementsWidth
   */
  MeaningfulWidth.updateElementsWidth = function () {
    AbstractElementView.prototype.meaningfulWidthEnabled = true;
    //todo: this.weaver.. Where does this come from? is it passed in?
    var views = this.weaver.getElementViews();
    _.forEach(views, function (view) {
      view._updateWidth();
    });
  };

  /**
   * @method updateLabelling
   */
  MeaningfulWidth.updateLabelling = function () {
    //todo: this.weaver.. Where does this come from? is it passed in?
    var views = this.weaver.getThreadElementsViews();
    _.forEach(views, function (view) {
      view.refreshElementsLabels();
    });
  };

  /**
   * @method restoreElementsWidth
   */
  MeaningfulWidth.restoreElementsWidth = function () {
    //todo: this.weaver.. Where does this come from? is it passed in?
    var views = this.weaver.getElementViews();
    _.forEach(views, function (view) {
      view.el.style.msFlexPositive = "";
      view.el.style.flexGrow = "";
    });

    AbstractElementView.prototype.meaningfulWidthEnabled = false;
  };

  /**
   * Listens to the change events on the {ExperimentalFeature.enabled} property and updates the labels accordingly
   */
  MeaningfulWidth.on('change:enabled', function (feature, enabled) {
    if (enabled) {
      feature.updateElementsWidth();
    } else {
      feature.restoreElementsWidth();
    }
    feature.updateLabelling();
  });

  return MeaningfulWidth;
});
