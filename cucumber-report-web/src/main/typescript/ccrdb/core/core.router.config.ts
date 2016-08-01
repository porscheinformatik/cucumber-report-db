namespace ccrdb.core {
    'use strict';
    angular
        .module('ccrdb.core')
        .config(routerConfig);

    routerConfig.$inject = ['$locationProvider', '$stateProvider', '$urlRouterProvider', '$httpProvider'];
    function routerConfig($locationProvider: ng.ILocationProvider, $stateProvider: ng.ui.IStateProvider,
        $urlRouterProvider: ng.ui.IUrlRouterProvider, $httpProvider: ng.IHttpProvider) {

        $locationProvider.html5Mode(true);
        $urlRouterProvider.otherwise('/');

        $stateProvider.state('base', {
            abstract: true,
            template: '<ui-view>',
            views: {
                'header': {
                    templateUrl: 'components/header.html',
                    controller: 'MenuController',
                    controllerAs: 'menuCtrl'
                }
            }
        });

        $stateProvider.state('products', {
            url: '/',
            parent: 'base',
            views: {
                '@': {
                    templateUrl: 'pages/products.html',
                    controller: 'ProductsController',
                    controllerAs: 'pCtrl'
                }
            }
        });

        $stateProvider.state('reports', {
            url: '/reports/{productId:string}/version/{versionId:string}/category/{categoryId:string}',
            params: {
                page: 0
            },
            parent: 'base',
            views: {
                '@': {
                    templateUrl: 'pages/reports.html',
                    controller: 'ReportsController',
                    controllerAs: 'rCtrl'
                }
            }
        });

        $stateProvider.state('features', {
            url: '/reports/{productId:string}/features/{reportId:string}',
            parent: 'base',
            views: {
                '@': {
                    templateUrl: 'pages/features.html',
                    controller: 'FeaturesController',
                    controllerAs: 'fCtrl'
                }
            }
        });

        $stateProvider.state('scenarios', {
            url: '/reports/{productId:string}/features/{reportId:string}/scenarios/{featureId:string}',
            parent: 'base',
            views: {
                '@': {
                    templateUrl: 'pages/scenarios.html',
                    controller: 'ScenariosController',
                    controllerAs: 'sCtrl'
                }
            }
        });

        $stateProvider.state('charts', {
            url: '/statistics/{productId: string}/type/{type: string}/limit/{limit: string}',
            parent: 'base',
            views: {
                '@': {
                    templateUrl: 'pages/statistics.html',
                    controller: 'StatisticsController',
                    controllerAs: 'statsCtrl'
                }
            }
        });

        $stateProvider.state('rankings', {
            url: '/statistics/rankings/{productId: string}',
            parent: 'base',
            views: {
                '@': {
                    templateUrl: 'pages/rankings.html',
                    controller: 'RankingsController',
                    controllerAs: 'ranksCtrl'
                }
            }
        });

        $stateProvider.state('assignments', {
            url: '/assignments',
            parent: 'base',
            views: {
                '@': {
                    templateUrl: 'pages/assignments.html',
                    controller: 'AssignmentsController',
                    controllerAs: 'assignCtrl'
                }
            }
        });

        $stateProvider.state('help', {
            url: '/help',
            parent: 'base',
            views: {
                '@': {
                    templateUrl: 'pages/help.html',
                    controller: 'HelpController',
                    controllerAs: 'helpCtrl'
                }
            }
        });
    }
}