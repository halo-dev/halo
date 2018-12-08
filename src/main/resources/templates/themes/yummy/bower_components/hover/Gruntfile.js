module.exports = function(grunt) {

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),

        version: {
            json: {
              src: ['package.json']
            },

            css: {
              options: {
                prefix: 'Version[:=]\\s*'
              },
              src: ['css/hover.css', 'scss/hover.scss', 'less/hover.less']
            }

        },

        sass: {
          dist: {
            options: {
              style: 'expanded'
            },
            files: {
              'css/hover.css': 'scss/hover.scss'
            }
          }
        },

        less: {
            dist: {
                options: {
                    style: 'expanded'
                },
                files: {
                    'css/hover.css': 'less/hover.less'
                }
            }
        },

        cssmin: {
          combine: {
            files: {
              'css/hover-min.css': ['css/hover.css']
            }
          }
        },


        watch: {
          options: {
            livereload: true,
          },

          reload: {
            files: ['*.html', 'css/*.css'],
            options: {
                spawn: false
            }
          },

          scss: {
            files: ['scss/**/*.scss'],
            tasks: ['sass', 'cssmin'],
            options: {
              spawn: false
            }
          },

          less: {
            files: ['less/**/*.less'],
            tasks: ['less', 'cssmin'],
            options: {
              spawn: false
            }
          },

          version: {
            files: ['package.json'],
            tasks: ['version'],
            options: {
                spawn: false
            }
          }
        },

        connect: {
          server: {
            options: {
              livereload: true,
              hostname: '0.0.0.0',
              port: 8000,
              base: './'
            }
          }
        },

    });

    require('load-grunt-tasks')(grunt);

    grunt.registerTask('default', ['connect', 'watch']);
};
