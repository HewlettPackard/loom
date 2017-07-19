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
module.exports = function (grunt) {

  "use strict";

  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-autoprefixer');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadTasks(__dirname + '/grunt');

  var path = require('path');

  grunt.initConfig({

    'dye-server': {
      all: {}
    },

    less: {
      theme: {
        files: {
          'css/theme.no-prefix.css': "less/theme.less"
        },
        options: {
          modifyVars: {
            'DYE_PATH': "'" + __dirname + "'"
          }
        }
      }
    },

    autoprefixer: {
      options: {
        browsers: ['last 2 version', 'ie 10'],
        cascade: true
      },
      theme: {
        src: 'css/theme.no-prefix.css',
        dest: 'css/theme.css'
      }
    },

    watch: {
      theme: {
        files: ['less/*.less', 'less/**/*.less'],
        tasks: ['css']
      }
    },

    copy: {
      'font-awesome': {
        files: [
          {expand: true, cwd: 'node_modules/font-awesome/fonts', src: ['*'], dest: 'fonts/'}
        ]
      }
    }
  });

  grunt.registerTask('css', 'Process files creating the CSS', [
    'less',
    'autoprefixer'
  ]);
};