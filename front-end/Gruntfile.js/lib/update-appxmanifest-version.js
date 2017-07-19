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
module.exports = function updateVersion(path, version, done) {

  var xml2js = require('xml2js');
  var fs = require('fs');
  var xml = fs.readFileSync(path);

  xml2js.parseString(xml, function (err, object) {

    if (err) {
      done(err);
      return;
    }

    object['Package']['Identity'][0].$['Version'] = version;
    xml = new xml2js.Builder().buildObject(object);
    fs.writeFileSync(path, xml);
    done();
  });
};