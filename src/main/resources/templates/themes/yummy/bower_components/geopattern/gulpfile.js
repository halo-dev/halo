var gulp       = require('gulp');
var gutil      = require('gulp-util');
var browserify = require('gulp-browserify');
var concat     = require('gulp-concat');
var eslint     = require('gulp-eslint');
var plumber    = require('gulp-plumber');
var uglify     = require('gulp-uglify');

var scripts = ['geopattern.js', 'lib/*.js'];

gulp.task('lint', function () {
	gulp.src(scripts)
		.pipe(eslint())
		.pipe(eslint.format());
});

gulp.task('browserify', function () {
	gulp.src('geopattern.js')
		.pipe(plumber())
		.pipe(browserify({
			standalone: 'GeoPattern',
			ignore: 'buffer'
		}))
		.pipe(uglify())
		.pipe(concat('geopattern.min.js'))
		.pipe(gulp.dest('./js'));
});

gulp.task('watch', function () {
	gulp.watch(scripts, ['browserify']);
});

gulp.task('default', ['browserify', 'watch']);
