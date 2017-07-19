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
define(['weaver/utils/EventBus'], function(EventBus) {
  "use strict";

  describe('weaver/utils/EventBus.js', function() {
    describe('Sanitizing event names', function() {
      before(function() {
        this.events = [
          {source: 'One', expected: 'one'},
          {source: 'TWO', expected: 'two'},
          {source: 'threE', expected: 'three'},
          {source: 'four', expected: 'four'},
          {source: ' five', expected: 'five'},
          {source: 'six ', expected: 'six'},
          {source: ' seven ', expected: 'seven'},
          {source: 'one-time', expected: 'one-time'}
        ];
      });
      it('Should create a consistent event name when enabling a feature', function() {
        this.events.map(function(event) {
          expect(EventBus.sanitizeString(event.source)).to.equal(event.expected);
        });
      });
    });
    describe('Building an event from parts', function() {
      it('Should just sanitize a string', function() {
        var sanitizeString = sinon.spy(EventBus, 'sanitizeString');
        var part = 'this-part';
        EventBus.createEventName(part);
        expect(sanitizeString).to.have.been.calledWith(part);
        sanitizeString.restore();
      });
      it('Should sanitize each part of an array', function() {
        var sanitizeString = sinon.stub(EventBus, 'sanitizeString');
        var parts = ['one', 'two', 'three'];
        EventBus.createEventName(parts);
        expect(sanitizeString.firstCall).to.have.been.calledWith(parts[0]);
        expect(sanitizeString.secondCall).to.have.been.calledWith(parts[1]);
        expect(sanitizeString.thirdCall).to.have.been.calledWith(parts[2]);
        sanitizeString.restore();
      });
    });
  });
});
