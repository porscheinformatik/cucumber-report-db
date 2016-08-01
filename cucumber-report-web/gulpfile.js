// - modules

var path = require('path'),
    gulp = require('gulp'),
    tsc = require('gulp-typescript'),
    tslint = require('gulp-tslint'),
    concat = require('gulp-concat'),
    uglify = require('gulp-uglify'),
    angularFilesort = require('gulp-angular-filesort'),
    sourcemaps = require('gulp-sourcemaps'),
    util = require('gulp-util');

// - config

var npmPath = 'node_modules/';
var srcDir = './src/main/web/';
var angularVersion = !!util.env.production ? 'node_modules/angular/angular.min.js' : 'node_modules/angular/angular.js';
var conf = {
    paths: {
        vendorjs: [
            angularVersion,
            'node_modules/angular-ui-router/release/angular-ui-router.min.js',
            'node_modules/angular-sanitize/angular-sanitize.min.js',
            'node_modules/angular-ui-bootstrap/dist/ui-bootstrap-tpls.js',
            'node_modules/angular-google-chart/ng-google-chart.min.js'],
        vendorstyles: [
            'node_modules/bootswatch/spacelab/bootstrap.min.css',
            'node_modules/angular-ui-bootstrap/dist/ui-bootstrap-csp.css'],
        dist: './target/classes/static/assets/'
    },
    production: !!util.env.production
};

var tsProject = tsc.createProject('tsconfig.json'),
    tsGlob = tsProject.config.compilerOptions.rootDir + '**/*.ts';

// - tasks
gulp.task('ts-lint', function() {
    return gulp.src(tsGlob)
        .pipe(tslint({
          formatter: "verbose"
        }))
        .pipe(tslint.report());
});

gulp.task('ts-compile', function() {
    return gulp.src(tsGlob)
        .pipe(tsc(tsProject))
        .pipe(angularFilesort())
        .pipe(concat('app.min.js'))
        .pipe(conf.production ? uglify() : util.noop())
        .pipe(gulp.dest(conf.paths.dist));
});

gulp.task('ts-testcompile', function() {
    gulp.src('src/test/typescript/**/*.ts')
        .pipe(tsc())
        .pipe(gulp.dest('target/typescript/'));
});


gulp.task('scripts-vendor', function () {
    return gulp.src(conf.paths.vendorjs)
        .pipe(sourcemaps.init({loadMaps: true}))
        .pipe(concat('vendor.min.js'))
        .pipe(conf.production ? util.noop() : sourcemaps.write())
        .pipe(gulp.dest(conf.paths.dist));
});

gulp.task('angular-i18n', function () {
    return gulp.src('node_modules/angular-i18n/angular-locale_*.js')
        .pipe(gulp.dest(conf.paths.dist + '/i18n'));
});

gulp.task('style-vendor', function () {
    return gulp.src(conf.paths.vendorstyles)
        .pipe(concat('vendor.css'))
        .pipe(gulp.dest(conf.paths.dist));
});

gulp.task('fonts', function () {
  return gulp.src('node_modules/bootswatch/fonts/glyphicons*')
      .pipe(gulp.dest(conf.paths.dist + '../fonts/'));
});


gulp.task('test', ['scripts-vendor', 'ts-testcompile', 'ts-compile'], function (done) {
    var Server = require('karma').Server;
    new Server({
        configFile: __dirname + '/src/test/karma.conf.js',
        browsers: ['Firefox'],
        singleRun: true
    }, done).start();
});

gulp.task('default', ['style-vendor', 'fonts', 'scripts-vendor', 'ts-lint', 'ts-compile']);

gulp.task('watch', ['default'], function () {
    gulp.watch(tsGlob, ['ts-compile', 'ts-lint']);
});
