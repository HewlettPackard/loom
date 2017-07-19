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
  var FilterService = require('weft/services/FilterService');
  var RelationTypeList = require('weaver/views/relations/RelationTypeList');

  describe('weaver/views/relations/RelationTypeList.js', function () {

    before(function () {
      this.service = new FilterService({
        active: false
      });
      this.relationTypeList = new RelationTypeList({
        model: new Backbone.Collection ([{
          id: 'equivalence',
          name: 'Equivalence'
        }, {
          id: 'default',
          name: 'Default'
        }]),
        service: this.service
      });
      this.$equivalenceRelationType = this.relationTypeList.$('.mas-relationType:contains(Equivalence)');
      this.$defaultRelationType = this.relationTypeList.$('.mas-relationType:contains(Default)');

      document.body.appendChild(this.relationTypeList.el);
    });

    after(function () {
      this.relationTypeList.remove();
    });

    it('Should list the available relation types', function () {

      expect(this.$equivalenceRelationType).to.exist;
      expect(this.$equivalenceRelationType).to.exist;
    });

    it('Should configure its service with selected relation type', function () {

      this.$equivalenceRelationType.click();
      expect(this.service.get('active')).to.be.true;
      expect(this.service.get('relationType')).to.contain('equivalence');
      expect(this.$equivalenceRelationType).to.have.class('is-highlighted');
    });

    it('Should update which relation the service uses', function () {

      this.$defaultRelationType.click();

      expect(this.service.get('relationType')).to.contain('equivalence');
      expect(this.service.get('relationType')).to.contain('default');

      expect(this.$equivalenceRelationType).to.have.class('is-highlighted');
      expect(this.$defaultRelationType).to.have.class('is-highlighted');
    });

    it('Should remove relation type when clicking an highlighted relation type', function () {
      this.$equivalenceRelationType.click();
      expect(this.service.get('relationType')).not.to.contain('equivalence');
      expect(this.service.get('relationType')).to.contain('default');
      expect(this.$equivalenceRelationType).not.to.have.class('is-highlighted');
      expect(this.$defaultRelationType).to.have.class('is-highlighted');

    });

    it('Should deactivate the service and clear relation type', function () {

      this.$defaultRelationType.click();
      expect(this.service.get('active')).to.be.false;
      expect(this.service.get('relationType')).to.deep.equal([]);
      expect(this.$equivalenceRelationType).not.to.have.class('is-highlighted');
      expect(this.$defaultRelationType).not.to.have.class('is-highlighted');
    });
  });
});
