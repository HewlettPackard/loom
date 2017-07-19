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
define(['lodash', 'q'], function (_, Q) {
  "use strict";

  return {

    /**
     * Checks if we have a valid download file response action from loom
     * @method isDownloadFileAction
     * @param response
     */
    isDownloadFileAction: function (response) {
      return _.has(response, 'content') && _.has(response, 'filename') && _.has(response, 'type');
    },

    /**
     * Main entry point to force downloading file response content to the user in a browser.
     * Checks the response is compatible and processes it according to the correct mime type
     * @method forceFileDownload
     * @param response
     */
    forceFileDownload: function (response) {
      if (this.isDownloadFileAction(response)) {
        this._simulateDownloadFileClick(
          this._sanitizeFilename(response.filename),
          this._compileHrefDataString(response)
        );
      }
    },

    /**
     * A function for reading the contents of single file from disk. It only reads the first file from a FileList.
     * All others are deliberately ignored as we have no requirement for multi file handling
     * @method readSingleFileContents
     * @param inputs the object to load the content into
     * @param {FileList} files
     * @returns {jQuery.promise|*|promise.promise|promise|d.promise|r.promise}
     */
    readSingleFileContents: function(inputs, name, files) {
      var deferred = Q.defer();
      if (files.length > 0) {
        var f = files[0];
        var r = new FileReader();
        r.onload = function(e) {
          inputs[name] = JSON.stringify({
            filename: f.name,
            type: _.endsWith(f.name, '.json') ? 'application/json' : 'plain/text',
            content: e.target.result
          });
          deferred.resolve(inputs);
        };
        r.readAsText(f);
      } else {
        deferred.resolve(inputs);
      }
      return deferred.promise;
    },

    /**
     * Converts data and a filename into a downloadable clickable item that the browser can use to send data as a file
     * to the user
     * @method _simulateDownloadFileClick
     * @param filename
     * @param data
     * @private
     */
    _simulateDownloadFileClick: function(filename, data) {
      var a = this._createDownloadElement();
      a.href = data;
      a.download = filename;
      a.click();
      return a;
    },

    /**
     * helper function to create a testable DOM element
     * @returns {Element}
     * @private
     */
    _createDownloadElement: function () {
      return document.createElement('a');
    },

    /**
     * detect the mime type and process the file content into it.
     * @method _compileHrefDataString
     * @param response
     */
    _compileHrefDataString: function(response) {
      switch(response.type) {
        case 'application/json': return this._compileHrefDataStringJSON(response);
        case 'plain/text': return this._compileHrefDataStringPlainText(response);
      }
    },

    /**
     * Converts the file response into a browser compatible JSON data download string
     * @method _compileHrefDataStringJSON
     * @param response
     * @returns {string}
     * @private
     */
    _compileHrefDataStringJSON: function(response) {
      return 'data:text/json;charset=utf-8,' + encodeURIComponent(JSON.parse(response.content));
    },

    /**
     * Converts the file response into a browser compatible JSON data download string
     * @method _compileHrefDataStringPlainText
     * @param response
     * @returns {string}
     * @private
     */
    _compileHrefDataStringPlainText: function(response) {
      return 'data:text/plain;charset=utf-8,' +  encodeURIComponent(response.content);
    },

    /**
     * removes slashes and trims spaces from file names. just in case.
     * @method _sanitizeFilename
     * @param name
     * @returns {string}
     */
    _sanitizeFilename: function (name) {
      return name.trim().replace(/\\|\//g, '_');
    }
  };
});
