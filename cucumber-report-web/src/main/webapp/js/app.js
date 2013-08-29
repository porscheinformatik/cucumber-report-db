(function () {
	"use strict";
	var app = window.angular.module('cucumber', ['ui.bootstrap']);
	
	var prepareReportData = function(reports) {
		
		angular.forEach(reports, function(data){

			function getFailedScenarioCount(feature) {
				var failedScenarios = 0;
				feature.scenarios.forEach(function(scenario) {
					if(scenario.result.failedStepCount){
						failedScenarios++;
					}
				});
				return failedScenarios;
			}
			
			
			function getUnknownScenarioCount(feature) {
				var unknownScenarios = 0;
				feature.scenarios.forEach(function(scenario) {
					if(scenario.result.unknownStepCount&&!scenario.result.failedStepCount){
						unknownScenarios++;
					}
				});
				return unknownScenarios;
			}
			
			angular.forEach(data.features, function(feature){
				if (feature.scenarios.length) {
					var res = feature.result;
					res.failedScenarioCount = getFailedScenarioCount(feature);
					res.unknownScenarioCount = getUnknownScenarioCount(feature);
					res.passedScenarioCount = res.scenarioCount - res.failedScenarioCount - res.unknownScenarioCount;
				}
				
				feature.status = feature.result.failedScenarioCount ? "failed" : "ok";
				feature.result.searchKeyword = feature.status === "failed" ? ":failedFeature" : ":okFeature";
				
				angular.forEach(feature.scenarios, function(scenario){
					
					angular.forEach(scenario.steps, function(step){
						if(!step.result){
							step.result={status:"skipped"};
							feature.result.skippedStepCount=(feature.result.skippedStepCount||0)+1;
						}
						if (step.result.status === "undefined"){
							step.result.status = "unknown";
						}
						step.result.searchKeyword = ":" + step.result.status + "Step";
					});
					
					scenario.status = scenario.result.failedStepCount ? "failed" : (scenario.result.unknownStepCount ? 'unknown' : 'passed');
					scenario.result.searchKeyword = ":" + scenario.status + "Scenario";
				});
				
				feature.result.passedStepCount = feature.result.passedStepCount || 0;
				feature.result.failedStepCount = feature.result.failedStepCount || 0;
				feature.result.unknownStepCount = feature.result.unknownStepCount || 0;
				feature.result.skippedStepCount = feature.result.skippedStepCount || 0;
			});
			
			data.duration = function(feature){
				var value=0;
				
				if(isNaN(feature)) 
				{ 
					value = feature.result.duration; 
				} 
				else 
				{ 
					value = feature; 
				}
				
				if(value<1000000000)
				{
					var msec=0; 
					if(value%1000000 >= 0)
					{
						msec=Math.round(value/1000000); 
					}
					return msec > 0 ? msec+'ms' : '<1ms';
				}
				else
				{
					value = (value / 1000000).toFixed()*1;
					var timeSpan = new TimeSpan(new Date(value) - new Date(0));
					return (timeSpan.days>0 ?
								' (' + timeSpan.toString('d') + ' Day' + 
									(timeSpan.days>1 ? 's' : '') + 
								')'  + ':' : ''
							) + 
							timeSpan.toString('HH:mm:ss');
				}
			};
		});
	};
	var loader = {
		loadJsonFromFilesystem : ['$http', function($http) {
			return function() { return $http.get(reportFileName).success(function(data){data[0] = data; prepareReportData(data);}); };
		}],
		
		restApiCollectionRequest : ['$http', function($http) {
			return function(url) { return $http.get(url); };
		}], 
				
		restApiQueryRequest : ['$http', function($http) {
			return function(url) {
				return $http.get(url).success(function(data) {prepareReportData(data);});
			};
		}]
	};
		app.config([ '$routeProvider', function($routeProvider) {
		$routeProvider
		.when('/help/', {
			templateUrl : 'pages/help.html',
			controller : 'HelpCtrl',
			resolve : loader
		})
		.when('/statistics/', {
			templateUrl : 'pages/statistics.html',
			controller : 'StatisticsCtrl',
			resolve : loader
		})
		.when('/products/', {
			templateUrl : 'pages/products.html',
			controller : 'ProductListCtrl',
			resolve : loader
		})
		.when('/reports/:colName', {
			templateUrl : 'pages/reports.html',
			controller : 'ReportListCtrl',
			resolve : loader
		})
		.when('/reports/:colName/features/:date', {
			templateUrl : 'pages/features.html',
			controller : 'FeatureListCtrl',
			resolve : loader
		})
		.when('/reports/:colName/features/:date/feature/:featureId', {
			templateUrl : 'pages/feature.html',
			controller : 'FeatureCtrl',
			resolve : loader
		})
		.otherwise({
			redirectTo : '/products/'
		});
	} ]);
	
	app.directive('loading', function () {
		return {
			restrict: 'E',
			replace: true,
			template: '<div ng-show=\"loading\" class=\"loading\"><div class=\"loadingBox\"><p id=\"loadingText\">Loading</p><p id=\"loadingSubText\">Please Wait ...</p><p><img src=\"img/load.gif\" /></p></div></div>',
			link: function (scope, element, attr) {
				scope.$watch('loading', function (val) {
					if (val){
						$(element).show();
					}else{
						$(element).hide();
					}
				});
			}
		};
	});
	
	app.run(function ($rootScope, $templateCache, $location, $routeParams) {
		$rootScope.clearCache = function() { 
			console.log("clear cache");
			$templateCache.removeAll();
		};
		
		$rootScope.range = function (start, end) {
			var ret = [];
			if (!end) {
				end = start;
				start = 0;
			}
			for (var i = start; i < end; i++) {
				ret.push(i);
			}
			return ret;
		};
		$rootScope.$routeParams = $routeParams;
		
		$rootScope.uriWrapRegEx = new RegExp("[\\/\\\\\\._A-Z]","g");
		$rootScope.uriWrapReplaceFunc = function (m) {
			if(/[A-Z]/g.test(m)){
				return '\u200b'+m;
			}else{
				return {'\\': "\\\u200b",'.': '.\u200b','_': '_\u200b', '/':'\/\u200b'}[m];
			}
		};
		
	});
	
	function addSearchAndSortHandlers($scope, $filter, dataArray){
		$scope.$watch("searchText", function(query){
			$scope[$scope.searchArrayName] = $filter("filter")(dataArray, query);
			$scope[$scope.searchArrayName] = $filter('orderBy')($scope[$scope.searchArrayName], $scope.orderPredicate, $scope.orderReverse);
		});
		$scope.$watch("orderPredicate", function(query){
			if($scope.lastOrderPredicate !== query){
				$scope.orderReverse = true;
			}
			$scope[$scope.searchArrayName] = $filter('orderBy')($scope[$scope.searchArrayName], query, $scope.orderReverse);
			$scope.lastOrderPredicate = query;
		});
		$scope.$watch("orderReverse", function(query){
			$scope[$scope.searchArrayName] = $filter('orderBy')($scope[$scope.searchArrayName], $scope.orderPredicate, query);
		});
	}
	
	function prepareReport(data, $rootScope, $scope, $routeParams, $filter, $location)
	{
		$rootScope.report = data;
		$rootScope.reportDate = $scope.report.date.$date;
		$scope.searchArrayName = 'filteredFeatures';
		$scope[$scope.searchArrayName] = $scope.report.features;
		$scope.orderPredicate = '';
		$scope.orderReverse = true;
		
		$rootScope.convertToUTC = function(dt) {
			var localDate = new Date(dt);
			var localTime = localDate.getTime();
			var localOffset = localDate.getTimezoneOffset() * 60000;
			return new Date(localTime + localOffset);
		};
		
		$scope.duration = $scope.report.duration;

		$scope.featureDetails = function(feature) {
			$location.path('/reports/' + $routeParams.colName + '/features/' + $routeParams.date + '/feature/' + feature.id);
		};

		$scope.sum = function(features, field) {
			if(!features){return null;}
			var sum = features.reduce(function (sum, feature) {
				var value = parseInt(feature.result[field], 10);
				return isNaN(value) ? sum : sum + value;
			}, 0);
			return sum;
		};
		
		addSearchAndSortHandlers($scope, $filter, $scope.report.features);
		
		$scope.isCollapsed = true;
		$rootScope.loading = false;
	}
	
	function prepareFeature(data, $rootScope, $scope, $routeParams, $filter, $location)
	{
		$scope.report = data;
		$scope.duration = $scope.report.duration;
		$scope.searchArrayName = 'filteredScenarios';
		$scope.orderPredicate = "";
		$scope.orderReverse = true;

		function getFeature(featureId, features) {
			for (var i = 0; i < features.length; i++) {
				if (featureId === features[i].id) {
					return features[i];
				}
			}
		}
		
		//$scope.searchText = $routeParams.searchText;

		$scope.feature = getFeature($routeParams.featureId, $scope.report.features);
		$scope[$scope.searchArrayName] = $scope.feature.scenarios;
		
		addSearchAndSortHandlers($scope, $filter, $scope.feature.scenarios);
		
		$rootScope.reportDate = $scope.report.date.$date;
		$rootScope.loading = false;
	}
	
	/**
	 * ProductList Controller (see products.html)
	 */
	app.controller('ProductListCtrl', function($rootScope, $routeParams, $scope, $location, $filter, $templateCache, restApiQueryRequest, loadJsonFromFilesystem) {
		$scope.searchArrayName = 'filteredProducts';
		$scope.orderPredicate = "";
		$scope.orderReverse = true;
		$rootScope.searchText = "";
		$rootScope.backBtnEnabled = false;
		
		// if a local report.json file was found: load the data from the filesystem
		loadJsonFromFilesystem().success(function(data) {
			$rootScope.databaseMode = false;
			$rootScope.showJSONFileError = false;
			$location.path('/reports/VDV3_13.08-SNAPSHOT/features/');
		})
		// else: load the data from the mongo database 
		.error(function(data, status, headers, config) {
			$rootScope.showJSONFileError = true;
			$rootScope.databaseMode = true;
			$scope.regexCondition = function(input)
			{
				var patt = new RegExp("[A-Z0-9]*\\_[A-Z0-9\\.\\-]*"); 
				if(patt.test(input) && input.indexOf(".chunks") === -1 && input.indexOf(".files") === -1 && (input.indexOf($routeParams.product) !== -1 || $routeParams.product === undefined)){
					return input;
				}
				return false;
			};
			
			$scope.reportsOverview = function(product) {
				$location.path('/reports/' + product);
			};
	
			restApiQueryRequest(collectionBaseUrl).success(function (data) {
				$rootScope.showDBError = false;
				$rootScope.showJSONFileError = false;
				$scope.products = data.slice().reverse();
				
				$scope[$scope.searchArrayName] = $scope.products;
				addSearchAndSortHandlers($scope, $filter, $scope.products);
			})
			.error(function(data, status, headers, config) {
				$rootScope.showDBError = true;
			});
		});
		
	});
	
	/**
	 * ReportList Controller (see reports.html)
	 */
	app.controller('ReportListCtrl', function($rootScope, $routeParams, $http, $scope, $location, $filter, $templateCache, restApiCollectionRequest, restApiQueryRequest) {
		$scope.searchArrayName = 'filteredReports';
		$scope.orderPredicate = "";
		$scope.orderReverse = true;
		
		$scope.$routeParams = $routeParams;
		
		$rootScope.goBack = function() {
			$location.path('/products/');
		};
		$rootScope.backBtnEnabled = $rootScope.databaseMode;
		
		$scope.featuresOverview = function(date) {
			$location.path('/reports/' + $routeParams.colName + '/features/' + date);
		};
 
		$rootScope.loading = true;
		restApiCollectionRequest(collectionBaseUrl + $routeParams.colName + "/")
		.success(function (data) {

			$scope.pageLimit = 10;
			$scope.pages = Math.ceil(data.count / $scope.pageLimit);
			$scope.page = $routeParams.page === undefined ? 0 : $routeParams.page;
			
			restApiQueryRequest(queryBaseUrl + $routeParams.colName + '/?sort=true&limit=' + $scope.pageLimit + '&skip=' + ($scope.pageLimit * $scope.page))
			.success(function (data) {
				$scope.reports = data;
				
				$scope[$scope.searchArrayName] = $scope.reports;
				addSearchAndSortHandlers($scope, $filter, $scope.reports);
				
				$scope.getStatistics = function(features) {
					var statistics = {
						passed : 0,
						failed : 0,
						unknown : 0
					};
					angular.forEach(features, function(feature){
						statistics.passed += feature.result.passedScenarioCount;
						statistics.failed += feature.result.failedScenarioCount;
						statistics.unknown += feature.result.unknownScenarioCount;
					});
					
					var sum = (statistics.passed + statistics.failed + statistics.unknown);
					
					// console.log("Average: " + sum);
					// console.log("Passed: " + statistics.passed);
					// console.log("Failed: " + statistics.failed);
					// console.log("Unknown: " + statistics.unknown);
					
					statistics.passedPercent = (statistics.passed / sum) * 100;
					statistics.failedPercent = (statistics.failed / sum) * 100;
					statistics.unknownPercent = (statistics.unknown / sum) * 100;
					
					return statistics;
				};
				
				$rootScope.loading = false;
			})
			.error(function(data, status, headers, config) {
				$rootScope.loading = false;
				$location.path('/products/');
			});

		});
	});
	
	/**
	 * FeatureList Controller (see features.html)
	 */
	app.controller('FeatureListCtrl', function($rootScope, $routeParams, $scope, $location, $filter, $templateCache, restApiQueryRequest, loadJsonFromFilesystem) {
		
		// if a local report.json file was found: load the data from the filesystem
		loadJsonFromFilesystem().success(function(data) {
			$rootScope.databaseMode = false;
			$rootScope.backBtnEnabled = false;
			prepareReport(data, $rootScope, $scope, $routeParams, $filter, $location);
		})
		// else: load the data from the mongo database 
		.error(function(data, status, headers, config) {
			$rootScope.databaseMode = true;
			$rootScope.backBtnEnabled = true;
			$rootScope.goBack = function() {
				$location.path('/reports/' + $routeParams.colName);
			};
			
			
			$rootScope.loading = true;
			restApiQueryRequest(queryBaseUrl + $routeParams.colName + '/?field=date&value=' + $routeParams.date)
			.success(function (data) {
				prepareReport(data[0], $rootScope, $scope, $routeParams, $filter, $location);
			})
			.error(function(data, status, headers, config) {
				$rootScope.loading = false;
				$location.path('/products/');
			});
		});
	});

	/**
	 * Feature Controller (see feature.html)
	 */
	app.controller('FeatureCtrl', function($rootScope, $scope, $location, $filter, $routeParams, $templateCache, restApiQueryRequest, loadJsonFromFilesystem) {
		
		$rootScope.goBack = function() {
			$location.path('/reports/' + $routeParams.colName + '/features/' + $routeParams.date);
		};
		$rootScope.backBtnEnabled = true;
		
		$scope.$routeParams = $routeParams;
		
		// if a local report.json file was found: load the data from the filesystem
		loadJsonFromFilesystem().success(function(data) {
			$rootScope.databaseMode=false;
			prepareFeature(data, $rootScope, $scope, $routeParams, $filter, $location);

			// return filesystem screenshot/video path 
			$scope.getEmbedding = function(embedding) {
				return embedding.url;
			};
		})
		// else: load the data from the mongo database 
		.error(function(data, status, headers, config) {
			$rootScope.databaseMode=true;
			$rootScope.loading = true;
			restApiQueryRequest(queryBaseUrl + $routeParams.colName + '/?field=date&value=' + $routeParams.date)
			.success(function (data) {
				prepareFeature(data[0], $rootScope, $scope, $routeParams, $filter, $location);
				
				// return rest api screenshot/video path
				$scope.getEmbedding = function(embedding) {
					return fileBaseUrl + $routeParams.colName + '/' + embedding.url + '/';
				};
			})
			.error(function(data, status, headers, config) {
				$rootScope.loading = false;
				$location.path('/products/');
			});

		});
	});
	
	/**
	 * Statistics Controller (see statistics.html)
	 */
	app.controller('StatisticsCtrl', function($rootScope, $routeParams, $scope, $location, $filter, $templateCache, restApiQueryRequest, loadJsonFromFilesystem) {
		
		$rootScope.goBack = function() {
			$location.path('/products/');
		};
		$rootScope.backBtnEnabled = true;
		
	});
	
	/**
	 * Help Controller (see help.html)
	 */
	app.controller('HelpCtrl', function($rootScope, $routeParams, $scope, $location, $filter, $templateCache, restApiQueryRequest, loadJsonFromFilesystem) {
		
		$rootScope.goBack = function() {
			$location.path('/products/');
		};
		$rootScope.backBtnEnabled = true;
		
	});
	
	$('#ReportFileName').text(reportFileName);
	$('#ReportFileNameLink').attr('href',reportFileName);
	$('#ServerURL').text(serverUrl);
	$('#ServerURLLink').attr('href',collectionBaseUrl);
}());
