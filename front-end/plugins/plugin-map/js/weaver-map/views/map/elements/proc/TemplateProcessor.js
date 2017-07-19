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

  var Mustache = require('mustache');
  var _ = require('lodash');
  var SVGNS = 'http://www.w3.org/2000/svg';

  var TemplateProcessor = function TemplateProcessor(options) {
    
    this.template = options.template;
    this.eventBinder = options.eventBinder;
    this.id = _.uniqueId();
    this.model = undefined;
    this.rawContext = undefined;
    this.isDirty = false;
    
    // Add the svg namespace to the template first tag:
    this.template = this.template
      .replace(/(<\w+)/g, '$1 xmlns="' + SVGNS + '"')
      .replace(/xlink:/g, '')
      .replace(/(handler=\"[\w\.-]+\")/g, '$1 dataId="{{id}}"');
      // .replace(/handler=\"(.*)\"/g, function (_, group) {
      //   return 'handler="' + group.replace(/(\w+)/g, '$1{{id}}') + '"'; 
      // });
  };

  TemplateProcessor.prototype.updateModel = function (model) {
    this._setModel(model);
    this.rawContext = undefined;
    this.isDirty = true;
  };

  TemplateProcessor.prototype.updateContext = function (context) {
    this.rawContext = context;
    this._setModel(undefined);
    this.isDirty = true;
  };
  
  TemplateProcessor.prototype.compile = function () {
    // Compile with Mustache the template
    if (this.rawContext) {
      this.stringResult = Mustache.render(this.template, this.rawContext);
    } else {
      this.stringResult = Mustache.render(this.template, this.model.getContext());
    }
  };
  
  TemplateProcessor.prototype.bind = function (element) {
    this.eventBinder.bind(element);
  };
  
  TemplateProcessor.prototype.hasBeenCompiled = function () {
    return !this.isDirty;
  };
  
  TemplateProcessor.prototype._setModel = function (model) {
    this.model = model;
    this.eventBinder.updateModel(model);
  };

  return TemplateProcessor;
})