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
"use strict";

require('weaver-lib/css/style.less');

var Backbone = require('backbone');
var $ = Backbone.$ = require('jquery');
var Cocktail = require('backbone.cocktail');
Cocktail.patch(Backbone);

var Weaver = require('weaver/Weaver');
var ConfigurationEditor = require('windows8/views/ConfigurationEditorView');
//require('plugins/main');

var LOCAL_STORAGE_KEY = 'configuration.url';


var configuration = {
  "loom-url": 'http://example.com'
};

function hideConfigurationEditor() {
  editor.remove();
  document.body.removeChild(title);
  document.body.classList.remove('mas-configurationScreen');
  document.body.classList.remove('mas-centeredFormLayout');
}

function displayError() {
  editor.showError('Loading requested pattern failed. Please try again!');
}

function storeUrl() {
  var currentUrl = window.weaver.aggregatorClient.get('url');
  console.log('storingURL', currentUrl);
  localStorage.setItem(LOCAL_STORAGE_KEY, currentUrl);
}

document.body.classList.add('mas-configurationScreen');
document.body.classList.add('mas-centeredFormLayout');

var title = document.createElement('h1');
title.classList.add('mas-centeredFormLayout--title');
title.classList.add('mas-configurationScreen--title');
title.textContent = 'Loom';

var previousUrl = localStorage.getItem('configuration.url');
console.log('previousUrl', previousUrl);
if (previousUrl) {
  configuration['loom-url'] = previousUrl;
}

var editor = new ConfigurationEditor({
  model: configuration
});
editor.el.classList.add('mas-centeredFormLayout--form');

editor.el.addEventListener('submit', function (event) {

  event.preventDefault();
  window.weaver = new Weaver(configuration);
  window.weaver.start(false)
               .then(hideConfigurationEditor, displayError)
               .then(storeUrl)
               .done();
});

document.body.appendChild(title);
document.body.appendChild(editor.el);
