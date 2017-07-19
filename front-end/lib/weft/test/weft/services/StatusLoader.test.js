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

  'use strict';

  var AggregatorClient = require('weft/services/AggregatorClient');
  var StatusLoader = require('weft/services/StatusLoader');

  describe('weft/services/StatusLoader.js', function () {

    before(function () {

      this.loader = new StatusLoader({
        aggregator: new AggregatorClient()
      });

    });

    describe('poll()', function () {

      it('Should set the status to not connected if call fails', sinon.test(function () {

        this.loader.poll();
        this.requests[0].respond(500);

        expect(this.loader.get('status')).to.be.falsy;

      }));

      it('Should set the status to connected if call succeed', sinon.test(function () {

        this.loader.poll();

        this.requests[0].respond(200, {'Content-Type': 'application/json'}, JSON.stringify({
          "build":"loom-server",
          "version":"1.0-SNAPSHOT (6597e957)",
          "adapters":[
          {"providerId": "private", "providerType": "os", "build":"loom-adapter-os","version":"1.0-SNAPSHOT (bfbe35aa)","name":"Private","className":"class com.hp.hpl.loom.adapter.os.fake.FakeAdapter","loadedTime":1412070991021},
          {"providerId": "private", "providerType": "os","build":"loom-adapter-os","version":"1.0-SNAPSHOT (bfbe35aa)","name":"Public","className":"class com.hp.hpl.loom.adapter.os.fake.FakeAdapter","loadedTime":1412070991029}
          ]}));

        expect(this.loader.get('status')).to.be.not.falsy;
        expect(this.loader.get('adapters')).to.be.not.falsy;

        expect(this.loader.get('adapters').length).to.eql(2);
      }));

    });

  });
});
