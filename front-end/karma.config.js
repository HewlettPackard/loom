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
// Karma configuration
// Generated on Wed Jul 01 2015 14:40:40 GMT+0100 (BST)
"use strict";

module.exports = function (config) {
  config.set({

    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath: '',


    // frameworks to use
    // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
    frameworks: ['mocha'],


    // list of files / patterns to load in the browser
    files: [
      // PhantomJS doesn't implement some browser features, so we need polyfills for them
      './node_modules/polyfill-service/polyfills/performance.now/polyfill.js',
      './node_modules/polyfill-service/polyfills/requestAnimationFrame/polyfill.js',
      './node_modules/polyfill-service/polyfills/Function.prototype.bind/polyfill.js',

      // Actual tests for Weft and Weaver
      './lib/weft/test/karma.test.js',
      './lib/weaver-lib/test/karma.test.js',

      // If plugins have tests, add them on hereafter.
      // eg.: './plugins/plugin-map/test/karma.test.js'
      './plugins/plugin-dma/test/karma.test.js'
    ],


    // list of files to exclude
    exclude: [
    ],


    // preprocess matching files before serving them to the browser
    // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
    preprocessors: {
      '**/*.test.js': ['webpack'],
    },

    client: {
      mocha: {
        reporter: 'html', // change Karma's debug.html to the mocha web reporter
      }
    },


    // test results reporter to use
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
    reporters: [
      // Mocha reporter, for printing results of Mocha tests in the console
      'mocha',
      // JUnit reporter, for exporting the results in JUnit's XML format
      'junit',
      // Coverage reporter, for generating coverage reports
      'coverage',
      // Threshold reporter, for failing test runs if they don't match minimum coverage
      'threshold'
    ],


    // web server port
    port: 9876,


    // enable / disable colors in the output (reporters and logs)
    colors: true,


    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,


    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,


    // start these browsers
    // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
    browsers: ['PhantomJS'],


    // Continuous Integration mode
    // if true, Karma captures browsers, runs the tests and exits
    singleRun: true,

    // Webpack configuration
    webpack: require('./webpack.config.js/karma')(__dirname),
    webpackMiddleware: {

      // Avoids spamming the console with Webpack info
      noInfo: true
    },

    junitReporter: {
      outputFile: 'reports/test-results.xml'
    },

    coverageReporter: {
      dir: 'reports/coverage',
      reporters: [
        {
          type: 'html',
          subdir: 'coverage-html'
        },
        {
          type: 'cobertura',
          subdir: '.',
          file: 'cobertura.txt'
        }
      ]
    },

    thresholdReporter: {
      statements: 80,
      branches: 70,
      functions: 80,
      lines: 80
    },

    plugins: [
      'karma-mocha',
      'karma-webpack',
      'karma-coverage',
      'karma-firefox-launcher',
      'karma-phantomjs-launcher',
      'karma-mocha-reporter',
      'karma-junit-reporter',
      'karma-threshold-reporter'
    ]
  });
};
