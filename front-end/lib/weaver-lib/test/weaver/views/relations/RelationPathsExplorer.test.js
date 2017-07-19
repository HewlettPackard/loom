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

  var Backbone = require('backbone');
  var RelationPathsExplorer = require('weaver/views/relations/RelationPathsExplorer');

  describe('weaver/views/relations/RelationPathsExplorer.js', function () {

    it('Should have its toggle disabled when no fiber is selected', function () {

      var view = new RelationPathsExplorer({

        // Mock relationshipService for now
        relationshipService: new Backbone.Model({
          filters: new Backbone.Collection()
        })
      });

      expect(view.$toggle).to.be.disabled;

      view.options.relationshipService.get('filters').set([new Backbone.Model()]);

      expect(view.$toggle).not.to.be.disabled;

      view.options.relationshipService.get('filters').set([]);

      expect(view.$toggle).to.be.disabled;
    });
  });

});
