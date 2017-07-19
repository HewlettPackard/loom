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
/* global describe, it, sinon, expect */
/* jshint expr: true */
define([
  'backbone',
  'weft/services/SelectionService'
], function (Backbone, SelectionService) {
  "use strict";

  describe('weft/services/SelectionService.js', function () {

    describe('select()', function () {

      it('Should add provided element to the selection', function () {

        var model = new Backbone.Model();
        var service = new SelectionService();

        service.select(model);

        expect(service.get('selection')).to.equal(model);
      });

      it('Should set appropriate property if the `flag` option is set', function () {
        var model = new Backbone.Model();
        var service = new SelectionService({
          flag: 'selected'
        });

        service.select(model);

        expect(model.get('selected')).to.be.true;
      });

      it('Should replace current selection by new element', function () {

        var currentSelection = new Backbone.Model({
          selected: true
        });
        var service = new SelectionService({
          flag: 'selected',
          selection: currentSelection
        });

        var newSelection = new Backbone.Model();

        service.select(newSelection);

        expect(service.get('selection')).to.equal(newSelection);
        expect(currentSelection.get('selected')).not.to.be.ok;
        expect(newSelection.get('selected')).to.be.true;
      });

      it('Should fire a `change:selection` event when a new item is selected', function () {

        var service = new SelectionService();

        var spy = sinon.spy();

        service.on('change:selection', spy);

        service.select('something');

        expect(spy).to.have.been.called;
        expect(spy.args[0][0]).to.equal(service);
        expect(spy.args[0][1]).to.equal('something');
      });

      it('Should not fire a `change:selection` event when the same item is selected', function () {

        var service = new SelectionService({
          selection: 'something'
        });

        var spy = sinon.spy();

        service.on('change:selection', spy);

        service.select('something');

        expect(spy).to.not.have.been.called;
      });

      it('Should fire only one event when the selection is updated', function () {

        var service = new SelectionService({
          selection: 'something'
        });

        var spy = sinon.spy();

        service.on('change:selection', spy);

        service.select('somethingElse');

        expect(spy).to.have.been.calledOnce;
      });
    });

    describe('unselect()', function () {

      it('Should remove provided element from the selection', function () {

        var model = new Backbone.Model();
        var service = new SelectionService({
          selection: model
        });

        service.unselect(model);

        expect(service.get('selection')).to.be.null;
      });

      it('Should unset appropriate property if the `flag` option is set', function () {

        var model = new Backbone.Model({
          selected: true
        });
        var service = new SelectionService({
          flag: 'selected',
          selection: model
        });

        service.unselect(model);

        expect(model.get('selected')).to.not.be.ok;
      });

      it('Should do nothing if the element is not currently in the selection', function () {

        var model = new Backbone.Model({
          selected: true
        });
        var service = new SelectionService({
          flag: 'selected',
          selection: model
        });

        var otherModel = new Backbone.Model({
          selected: 'Something'
        });

        service.unselect(otherModel);

        expect(service.get('selection')).to.equal(model);
        expect(model.get('selected')).to.be.true;
        expect(otherModel.get('selected')).to.equal('Something');
      });
    });
  });
});