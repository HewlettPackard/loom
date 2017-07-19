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
  var TemplateProcessor = require('./proc/TemplateProcessor');
  var DOMTraverseHelper = require('./helpers/DOMTraverseHelper');
  var UserEventBinder = require('./events/UserEventBinder');

  var domParser = new DOMParser();
  var domParserType = "image/svg+xml";
  
  var SVGNS = 'http://www.w3.org/2000/svg';
  var XLINKNS = 'http://www.w3.org/1999/xlink';

  
  var TemplateSVGRenderer = (function () {
    
    // Add traverseDOMHelper function.
    _.extend(TemplateSVGRenderer.prototype, DOMTraverseHelper);
    
    /**
     * Constructor.
     * @param {SVGElement} svg is the svg element that will be managed by this object.
     */
    function TemplateSVGRenderer(svg) {

      this.rootElement  = svg;

      this.defsElement = this._append(this.rootElement, 'defs');
      this.svgElement = this._append(this.rootElement, 'g');

      this.svgElement.setAttributeNS(null, 'id', _.uniqueId('content'));

      this.templatesToRefresh = [];

      this.defsrenderList = [];
      this.renderList = [];

      this.renderedElement = {};
      this.isModelDirty = false;
    };

    /**
     * Add a definition to the svg element. This definition can be re-rendered
     * if refresh is true.
     * @param {Object} args must contains:
     *                 template         {String}  used to create an svg to insert into the defs
     *                                            section.
     *                 processorContext {Object}  used to create the svg element.
     *                 modelContext     {Object}  used to create the svg element 
     *                                            @see TemplateProcessor for more details.
     *                 adapter          {Adapter} called after generating the dom. (only called with processorContext)
     *                 [refresh]        {Boolean} used to force refresh when refresh is called.
     *
     * @return {TemplateProcessor} a handle to the template model that can be passed
     *                         to refresh only this element.
     */
    TemplateSVGRenderer.prototype.addDefs = function (args) {

      return this._add(args, this.defsElement, this.defsrenderList);
    };

    /**
     * Add a blob to the svg. This blob can be re-rendered
     * if refresh is true.
     * @param {Object} args must contains:
     *                 template         {String}  used to create an svg to insert into the defs
     *                                            section.
     *                 processorContext {Object}  used to create the svg element.
     *                 modelContext     {Object}  used to create the svg element 
     *                                            @see TemplateProcessor for more details.
     *                 adapter          {Adapter} called after generating the dom. (only called with processorContext)
     *                 [refresh]        {Boolean} used to force refresh when refresh is called.
     *
     * @return {TemplateProcessor} a handle to the template model that can be passed
     *                         to refresh only this element.
     */
    TemplateSVGRenderer.prototype.add = function (args) {

      return this._add(args, this.svgElement, this.renderList);
    };

    // Helper function for two previous add.
    TemplateSVGRenderer.prototype._add = function (args, parentElement, list) {

      var template = args.template;
      var processorContext = args.processorContext;
      var modelContext = args.modelContext;
      var refresh = args.refresh || false;
      var adapter = args.adapter;

      var parsed = new TemplateProcessor({
        template: template,
        eventBinder: new UserEventBinder()
      });

      parsed.parentElement = parentElement;

      if (processorContext) {

        parsed.updateContext(processorContext);
        parsed.compile();
        this._renderOnlyOnceElement(parsed, parentElement);
        
        if (adapter) {
          
          adapter.adapt(this.getRendered(parsed));
        }

      } else if (modelContext) {

        parsed.updateModel(modelContext);
        if (refresh) {
          this.templatesToRefresh.push(parsed);
        }
        list.push(parsed);
      }

      return parsed;
    };

    /**
     * Returns the rendered element for the given template model.
     * You can only obtains element that can't be refreshed.
     * @param  {TemplateProcessor} templateProcessor is the template model
     * @return {SVGElement}                  Returns the svg element rendered.
     */
    TemplateSVGRenderer.prototype.getRendered = function (templateProcessor) {

      return this.renderedElement[templateProcessor.id];
    };

    /**
     * Remove the given template model, created previously with
     * on of the add/addDefs methods.
     * @param  {TemplateProcessor} templateModel is the element to remove.
     */
    TemplateSVGRenderer.prototype.remove = function (templateModel) {

      if (!templateModel instanceof TemplateProcessor) {
        return;
      }

      _.remove(this.renderList, function (tm) {
        return tm === templateModel;
      });
      _.remove(this.templatesToRefresh, function (tm) {
        return tm === templateModel;
      });
      _.remove(this.defsrenderList, function (tm) {
        return tm === templateModel;
      });

      var node = this.renderedElement[templateModel.id];

      if (node) {
        templateModel.parentElement.removeChild(node);
        delete this.renderedElement[templateModel.id];
      }
    };

    TemplateSVGRenderer.prototype.clear = function () {

      this.renderList = undefined;
      this.templatesToRefresh = undefined;
      this.defsrenderList = undefined;
      this.renderedElement = undefined;
      this.defsElement.remove();
      this.svgElement.remove();
    };

    /**
     * Refresh, re-render the svg part that have been marked as requesting
     * a re-rendering phase when the model could possibily have changed.
     * Optionally you can refresh only one element by passing it as an argument.
     * @param {TemplateProcessor} element [optional] is the element to refresh.
     */
    TemplateSVGRenderer.prototype.refresh = function (element) {

      if (element) {

        element.compile();
        this._updateOrRenderElement(element, element.parentElement);

      } else {

        // Re-render every template with the current model.
        _.forEach(this.templatesToRefresh, function (element) {
          // Force recompilation
          element.compile();
          // Update the element
          this._updateOrRenderElement(element, element.parentElement);
        }, this);
      }
    };
    
    /**
     * Sort the children based on the given comparator
     * @param {Function} comparator is a function taking a model in argument and return a value.
     * @param {Function} filter     is a function (optional) that take an element into argument and
     *                              return true if the elements can be used.
     */
    TemplateSVGRenderer.prototype.sortChildren = function (comparator, filter) {
      
      var list = this.renderList;
      
      if (_.isFunction(filter)) {
        list = _.filter(list, function (element) { return filter(element.model); });
      }
      
      list = _.sortBy(list, function (element) { return comparator(element.model); });
      
      for (var i = list.length - 1, next = this.getRendered(list[i]), node; --i >= 0;) {
        if (node = this.getRendered(list[i])) {
          if (next && next !== node.nextSibling) next.parentNode.insertBefore(node, next);
          next = node;
        }
      }
    };

    /**
     * Render all the templates registered within
     * the svg element gaven on construction of the TemplateSVGRenderer.
     * This method shouldn't be called several times, use refresh instead.
     */
    TemplateSVGRenderer.prototype.render = function () {

      // Render defs section
      _.forEach(this.defsrenderList, function (element) {

        if (!element.hasBeenCompiled() || this.isModelDirty) {
          element.compile();
        }

        this._updateOrRenderElement(element, this.defsElement);
      }, this);

      // Render others section
      _.forEach(this.renderList, function(element) {

        if (!element.hasBeenCompiled() || this.isModelDirty) {
          element.compile();
        }

        this._updateOrRenderElement(element, this.svgElement);
      }, this);

      this.isModelDirty = false;
    };

    /**
     * Convenience function to access the svg element.
     * @return {SVGElement} the inner modified svg element.
     */
    TemplateSVGRenderer.prototype.getSVGElement = function () {
      return this.svgElement;
    }

    /**
     * Update the processors on next render stage. (Re-compile them)
     * @see TemplateProcessor
     */
    TemplateSVGRenderer.prototype.update = function () {
      this.isModelDirty = true;
    };


    /////////////////////////////
    ///   PRIVATE INTERFACE
    /////////////////////////////

    TemplateSVGRenderer.prototype._updateOrRenderElement = function (element, parentElement) {

      var oldRendEl = this.renderedElement[element.id];

      // Update SVG
      this.renderedElement[element.id] = this._renderFromString(element.stringResult);

      if (oldRendEl) {
        parentElement.replaceChild(this.renderedElement[element.id], oldRendEl);
      } else {
        parentElement.appendChild(this.renderedElement[element.id]);
      }

      // Bind events
      element.bind(this.renderedElement[element.id]);
    };

    TemplateSVGRenderer.prototype._renderOnlyOnceElement = function (element, parentElement) {

      this.renderedElement[element.id] = this._renderFromString(element.stringResult);

      parentElement.appendChild(this.renderedElement[element.id]);      
    };

    TemplateSVGRenderer.prototype._renderFromString = function (str) {
      var tree = domParser.parseFromString(str, domParserType).firstChild;
      // Perform some clean up for 'href' (could be avoided if unecessary)
      // cf: parsing of the template
      this.traverseDOMTree(tree, _.bind(this._setAttr, this, 'href', XLINKNS));

      return tree;
    };

    TemplateSVGRenderer.prototype._setAttr = function (attributeName, namespace, svgElement) {
      var attr = svgElement.getAttributeNS(null, attributeName);
      if (attr) {
        svgElement.setAttributeNS(namespace, attributeName, attr)
      }
    };

    TemplateSVGRenderer.prototype._append = function (svgElement, tagName) {
      var res;

      if (_.isString(tagName)) {
        res = document.createElementNS(SVGNS, tagName);
        svgElement.appendChild(res);
      } else {
        res = tagName;
        svgElement.appendChild(tagName);
      }

      return res;
    };

    return TemplateSVGRenderer;
  })();
  
  return TemplateSVGRenderer;
});