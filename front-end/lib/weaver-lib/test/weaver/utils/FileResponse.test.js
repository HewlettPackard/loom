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
define(['weaver/utils/FileResponse'], function(FileResponse) {
  "use strict";

  describe('weaver/utils/FileResponse.js', function() {
    describe('Sanitizing file names', function() {
      it('Should sanitize file names', function() {
        [
          {source: 'this', expected: 'this'},
          {source: ' this', expected: 'this'},
          {source: 'this ', expected: 'this'},
          {source: ' this ', expected: 'this'},
          {source: 'this/is', expected: 'this_is'},
          {source: 'this\\is', expected: 'this_is'},
          {source: 'this/is/bad ', expected: 'this_is_bad'},
          {source: 'this\\is\\bad', expected: 'this_is_bad'},
          {source: 'this/is\\bad', expected: 'this_is_bad'},
          {source: ' this/is\\bad ', expected: 'this_is_bad'}
        ]
        .map(function(fixture) {
          expect(FileResponse._sanitizeFilename(fixture.source)).to.equal(fixture.expected);
        });
      });
    });
    describe('Detect a file action response', function() {
      it('Should correctly identify a file action response', function() {
        [
          {source: {}, expected: false},
          {source: {content:''}, expected: false},
          {source: {content:'', filename:''}, expected: false},
          {source: {content:'', filename:'', type: ''}, expected: true}
        ]
        .map(function(fixture) {
          expect(FileResponse.isDownloadFileAction(fixture.source)).to.equal(fixture.expected);
        });
      });
    });
    describe('Extracting content', function() {
      it('Should correctly identify JSON content', function() {
        var response = {
          content: JSON.stringify({hi: "there"}),
          filename: 'example.txt',
          type: 'application/json'
        };
        var spy = sinon.spy(FileResponse, '_compileHrefDataStringJSON');
        FileResponse._compileHrefDataString(response);
        expect(spy).to.be.calledWith(response);
        FileResponse._compileHrefDataStringJSON.restore();
      });
      it('Should correctly identify Plain text content', function() {
        var response = {
          content: 'this is plain text',
          filename: 'example.txt',
          type: 'plain/text'
        };
        var spy = sinon.spy(FileResponse, '_compileHrefDataStringPlainText');
        FileResponse._compileHrefDataString(response);
        expect(spy).to.be.calledWith(response);
        FileResponse._compileHrefDataStringPlainText.restore();
      });
    });
    describe('ForceFileDownload', function() {
      describe('With a valid response', function() {
        beforeEach(function() {
          this.response = {content:'hello', filename:'example.txt', type: 'plain/text'};
        });
        it('Should sanitize the filename', function() {
          var spy = sinon.spy(FileResponse, '_sanitizeFilename');
          sinon.stub(FileResponse, '_simulateDownloadFileClick');
          FileResponse.forceFileDownload(this.response);
          expect(spy).to.be.calledWith(this.response.filename);
          FileResponse._sanitizeFilename.restore();
          FileResponse._simulateDownloadFileClick.restore();
        });
        it('Should compile the href data string', function() {
          var spy = sinon.spy(FileResponse, '_compileHrefDataString');
          sinon.stub(FileResponse, '_simulateDownloadFileClick');
          FileResponse.forceFileDownload(this.response);
          expect(spy).to.be.calledWith(this.response);
          FileResponse._compileHrefDataString.restore();
          FileResponse._simulateDownloadFileClick.restore();
        });
        it('Should force a download in the users browser', function() {
          // No need to download the file in the test, just ensure that the function gets called with the correct values
          var compiled = 'data:compiled...',
            sanitize = sinon.stub(FileResponse, '_sanitizeFilename'),
            compile = sinon.stub(FileResponse, '_compileHrefDataString'),
            simulate = sinon.stub(FileResponse, '_simulateDownloadFileClick');
          sanitize.returns(this.response.filename);
          compile.returns(compiled);
          FileResponse.forceFileDownload(this.response);
          expect(simulate).to.be.calledWith(this.response.filename, compiled);
          FileResponse._sanitizeFilename.restore();
          FileResponse._compileHrefDataString.restore();
          FileResponse._simulateDownloadFileClick.restore();
        });
      });
    });
    describe('_simulateDownloadFileClick', function() {
      it('Should build a clickable item', function() {
        var
          filename = 'test.txt',
          data = '12345',
          a = {
            href: '',
            download: '',
            click: sinon.spy()
          };
        sinon.stub(FileResponse, '_createDownloadElement', function() {return a;});
        FileResponse._simulateDownloadFileClick(filename, data);
        expect(a.href).to.equal(data);
        expect(a.download).to.equal(filename);
        expect(a.click.called).to.be.true;
        FileResponse._createDownloadElement.restore();
      });
    });
  });
});
