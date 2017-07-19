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
  var FilterDepthSettingsView = require('weaver/views/FilterDepthSettingsView');

  describe('weaver/views/FilterDepthSettingsView.js', function () {

    it('Renders the title, default value and updates to whichever value gets selected', function () {

      var view = new FilterDepthSettingsView({
        model: new Backbone.Model(),
        title: 'Selected'
      });

      expect(view.$('.mas-filterDepthSettings--title')).to.have.text('Selected');
      expect(view.$('[value=all]')).to.be.checked;

      view.$('[value=2]').prop('checked',true).change();
      expect(view.model.get('depth')).to.equal(2);
    });

    it('Pre selects the appropriate value', function () {

      var view = new FilterDepthSettingsView({
        model: new Backbone.Model({
          depth: 1
        })
      });

      expect(view.$('[value=1]')).to.be.checked;

      view.$('[value=all]').prop('checked',true).change();
      expect(view.model.get('depth')).not.to.be.ok;
    });
  });
});
