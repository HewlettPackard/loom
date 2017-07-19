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

  var BaseView = require('weaver/views/BaseView');

  describe('weaver/views/BaseView.js', function () {

    describe('template', function () {

      beforeEach(function () {
        var template = '<div class="my-view">' +
                       '<div class="my-view--content"></div>' +
                       '<div class="my-view--otherContent"></div>' +
                       '</div>';

        this.V = BaseView.extend({
          className: 'my-view has-defaultState',
          template: template
        });
      });

      it('Should create DOM contents based on provided template', function () {
        var view = new this.V({});
        expect(view.$el).to.have.class('has-defaultState');
        expect(view.$('.my-view--content')).to.exist;
        expect(view.$('.my-view--otherContent')).to.exist;
      });

      it('Should append child nodes when constructed with an element', function () {

        var existingNode = document.createElement('div');
        existingNode.classList.add('my-existingClass');

        var view = new this.V({
          el: existingNode
        });
        expect(view.$el).to.have.class('my-existingClass');
        expect(view.$el).to.have.class('my-view');
        expect(view.$el).to.have.class('has-defaultState');
        expect(view.$('.my-view--content')).to.exist;
        expect(view.$('.my-view--otherContent')).to.exist;
      });
    });

    it('Should set given classnames to its root element', function () {

      var view = new BaseView({
        className: 'my-class my-otherClass'
      });

      expect(view.$el).to.have.class('my-class');
      expect(view.$el).to.have.class('my-otherClass');

      var V = BaseView.extend({
        className: 'my-class my-otherClass'
      });
      view = new V();

      expect(view.$el).to.have.class('my-class');
      expect(view.$el).to.have.class('my-otherClass');
    });

    it('Should reference the view as $.data() `view` attribute', function () {

      var view = new BaseView();
      expect(view.$el).to.have.data('view', view);
    });
  });
});
