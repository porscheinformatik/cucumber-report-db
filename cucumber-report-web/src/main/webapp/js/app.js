(function() {
	
	var serverUrl = '/rest/';
	var queryBaseUrl = serverUrl + 'query/bddReports/';
	var collectionBaseUrl = serverUrl + 'collection/bddReports/';
	var fileBaseUrl = serverUrl + 'file/bddReports/';
	
	var app = window.angular.module('cucumber', ['ui.bootstrap']);
	
	var loader = {
		restApiCollectionRequest : ['$http', function($http) {
			return function(url) { return $http.get(url); }
		}], 
				
		restApiQueryRequest : ['$http', function($http) {
			return function(url) { 
				return $http.get(url).success(function(reports) {
					angular.forEach(reports, function(data){

						function getFailedScenarioCount(feature) {
							var failedScenarios = 0;
							feature.scenarios.forEach(function(scenario) {
								if(scenario.result.failedStepCount)
									failedScenarios++;
							});
							return failedScenarios;
						}
						
						
						function getUnknownScenarioCount(feature) {
							var unknownScenarios = 0;
							feature.scenarios.forEach(function(scenario) {
								if(scenario.result.unknownStepCount)
									unknownScenarios++;
							});
							return unknownScenarios;
						}
						
						angular.forEach(data.features, function(feature){
							if (feature.scenarios.length) {
								feature.result.failedScenarioCount = getFailedScenarioCount(feature);
								feature.result.unknownScenarioCount = getUnknownScenarioCount(feature);
								feature.result.passedScenarioCount = feature.result.scenarioCount
																	- feature.result.failedScenarioCount
																	- feature.result.unknownScenarioCount;
								
								if (feature.result.failedScenarioCount === 0)
									feature.result.failedScenarioCount = null;
								
								if (feature.result.unknownScenarioCount === 0)
									feature.result.unknownScenarioCount = null;
								
								if (feature.result.passedScenarioCount === 0)
									feature.result.passedScenarioCount = null;
							}
							
							feature.status = feature.result.failedScenarioCount ? "FAILED" : "OK";
							
							angular.forEach(feature.scenarios, function(scenario){
								
								angular.forEach(scenario.steps, function(step){
									if (step.result.status === "undefined")
										step.result.status = "unknown";
								});
								
								scenario.status = scenario.result.failedStepCount ? "failed" : (scenario.result.unknownStepCount ? 'unknown' : 'passed');
								
							});
						});
						
						data.duration = function(feature){
							var day=0; 
							var h=0; 
							var min=0; 
							var sec=0; 
							var value=0;
							
							var substract = function(v,b){
								var result=v-(b);
								return result;
							};
							
							if(isNaN(feature)) 
							{ 
									value = feature.result.duration; 
							} 
							else 
							{ 
									value = feature; 
							}
							if(value%(1000000000*60*60*24) >= 0)//day 
							{                         
									day=(value/(1000000000*60*60*24))|0;
									value=substract(value,(day*(1000000000*60*60*24)));
							} 
							if(value%(1000000000*60*60) >= 0)//hour 
							{                         
									
									h=(value/(1000000000*60*60))|0; 
									value = substract(value,(h*(1000000000*60*60)));
									if(h%24 === 0 && h !==0) {day++;h=0;}
							} 
							if(value%(1000000000*60) >= 0)//minute 
							{                                 
									min=(value/(1000000000*60))|0;
									value=substract(value,(min*(1000000000*60)));
									if(min%60 === 0 && min !==0){h++;min=0;}           
							} 
							if(value%(1000000000) >= 0)//second 
							{                         
									sec=(value/(1000000000))|0; 
									value=substract(value,(sec*(1000000000)));
									if(sec%60 === 0 && sec !==0){min++;sec=0;}
							} 
	
							if(day===0 && h===0 && min===0 && sec===0)
							{
								var msec=0; 
								if(value%1000000 >= 0)
								{                                                        
										msec=(value/1000000)|0; 
								}
								return msec > 0 ? msec+'ms' :'<1ms';
							}
							else if(day > 0) 
							{ 
									return(' '+ '('+ day + 'Day/s) '  + ' '  +' ' + h + ' ' +':' + ' ' + min +  ' ' + ':' + ' ' + sec); 
							} 
							else 
							{ 
									return(' ' + h + ' ' +':' + ' ' + min +  ' ' + ':' + ' ' + sec); 
							}
						};	
					});
				});
			}
		}]
	};

	app.config([ '$routeProvider', function($routeProvider) {
		$routeProvider
		.when('/help/', {
			templateUrl : 'help.html',
			controller : 'HelpCtrl',
			resolve : loader
		})
		.when('/products/', {
			templateUrl : 'products.html',
			controller : 'ProductListCtrl',
			resolve : loader
		})
		.when('/reports/:colName', {
			templateUrl : 'reports.html',
			controller : 'ReportListCtrl',
			resolve : loader
		})
		.when('/reports/:colName/features/:date', {
			templateUrl : 'features.html',
			controller : 'FeatureListCtrl',
			resolve : loader
		})
		.when('/reports/:colName/features/:date/feature/:featureId', {
			templateUrl : 'feature.html',
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
		replace:true,
		template: '<div ng-show=\"loading\" class=\"loading\"><div class=\"loadingBox\"><p id=\"loadingText\">Loading</p><p id=\"loadingSubText\">Please Wait ...</p><p><img src=\"img/load.gif\" /></p></div></div>',
		link: function (scope, element, attr) {
			  scope.$watch('loading', function (val) {
				  if (val)
					  $(element).show();
				  else
					  $(element).hide();
			  });
		}
	  }
	});
		
	app.run(function ($rootScope, $templateCache, $location, $routeParams) {
		$rootScope.clearCache = function() { 
			console.log("clear cache");
		    $templateCache.removeAll();
		}
		
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
	});
	
	app.controller('HelpCtrl', function($rootScope, $routeParams, $scope, $location, $filter, $templateCache, restApiQueryRequest) {
		$scope.scrollTo = function(id) {
			$location.hash(id);
			$anchorScroll();
		}
	});
	
	
	app.controller('ProductListCtrl', function($rootScope, $routeParams, $scope, $location, $filter, $templateCache, restApiQueryRequest) {
	
		$scope.regexCondition = function(input)
		{			
			var patt = new RegExp("[A-Z0-9]*\\_[A-Z0-9\\.\\-]*"); 
			if(patt.test(input) && input.indexOf(".chunks") === -1 && input.indexOf(".files") === -1 && (input.indexOf($routeParams.product) !== -1 || $routeParams.product === undefined))
				return input;
			
			return false;
		};
		
		$scope.reportsOverview = function(product) {
			$location.path('/reports/' + product);
		}
		
		$scope.$watch("searchText", function(query){
			$scope.filteredProducts = $filter("filter")($scope.products, query);
		});
	
		restApiQueryRequest(collectionBaseUrl).success(function (data) {
			$scope.products = data.slice().reverse();
		});
	});
	

	app.controller('ReportListCtrl', function($rootScope, $routeParams, $http, $scope, $location, $filter, $templateCache, restApiCollectionRequest, restApiQueryRequest) {

		$scope.$routeParams = $routeParams;
		
		$rootScope.goBack = function() {
			$location.path('/products/');
		}
		
		$scope.featuresOverview = function(date) {
			$location.path('/reports/' + $routeParams.colName + '/features/' + date);
		}
 
		$rootScope.loading = true;
		restApiCollectionRequest(collectionBaseUrl + $routeParams.colName + "/")
		.success(function (data) {

			$scope.pageLimit = 10;
			$scope.pages = Math.ceil(data.count / $scope.pageLimit);
			$scope.page = $routeParams.page === undefined ? 0 : $routeParams.page;
			
			restApiQueryRequest(queryBaseUrl + $routeParams.colName + '/?sort=true&limit=' + $scope.pageLimit + '&skip=' + ($scope.pageLimit * $scope.page))
			.success(function (data) {
				$scope.reports = data;
				
				$scope.$watch("searchText", function(query){
					$scope.filteredReports = $filter("filter")($scope.reports, query);
				});
				
				$scope.getStatistics = function(features) {
					var statistics = {
						passed : 0,
						failed : 0,
						unknown : 0
					}
					angular.forEach(features, function(feature){
						statistics.passed += feature.result.passedScenarioCount;
						statistics.failed += feature.result.failedScenarioCount;
						statistics.unknown += feature.result.unknownScenarioCount;

					});
					
					var sum = (statistics.passed + statistics.failed + statistics.unknown);
					
					console.log("Average: " + sum);
					console.log("Passed: " + statistics.passed);
					console.log("Failed: " + statistics.failed);
					console.log("Unknown: " + statistics.unknown);
					
					statistics.passedPercent = (statistics.passed / sum) * 100;
					statistics.failedPercent = (statistics.failed / sum) * 100;
					statistics.unknownPercent = (statistics.unknown / sum) * 100;
					
					return statistics;
				};
				
				$rootScope.loading = false;
			});
		});
	});
	
	app.controller('FeatureListCtrl', function($rootScope, $routeParams, $scope, $location, $filter, $templateCache, restApiQueryRequest) {
	
		$rootScope.goBack = function() {
			$location.path('/reports/' + $routeParams.colName);
		};
	
		$rootScope.loading = true;
		restApiQueryRequest(queryBaseUrl + $routeParams.colName + '/?field=date&value=' + $routeParams.date)
		.success(function (data) {
			$rootScope.report = data[0];
			$rootScope.reportDate = $scope.report.date.$date;
			
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
				if(!features) return null;
				var sum = features.reduce(function (sum, feature) {
					var value = parseInt(feature.result[field], 10);
					return isNaN(value) ? sum : sum + value;
				}, 0);
				return sum > 0 ? sum : null;
			};

			$scope.$watch("searchText", function(query){
				$scope.filteredFeatures = $filter("filter")($scope.report.features, query);
			});
			
			$scope.isCollapsed = true;
			$rootScope.loading = false;
		});
	});

	app.controller('FeatureCtrl', function($rootScope, $scope, $location, $routeParams, $templateCache, restApiQueryRequest) {

		$rootScope.goBack = function() {
			$location.path('/reports/' + $routeParams.colName + '/features/' + $routeParams.date);
		};
		
		$scope.$routeParams = $routeParams;
		$rootScope.loading = true;
		restApiQueryRequest(queryBaseUrl + $routeParams.colName + '/?field=date&value=' + $routeParams.date)
		.success(function (data) {
			
			$scope.report = data[0];
			$scope.duration = $scope.report.duration;
			$scope.getEmbeddingBaseUrl = function() {
				return fileBaseUrl;
			}

			function getFeature(featureId, features) {
				for (var i = 0; i < features.length; i++) {
					if (featureId === features[i].id) {
						return features[i];
					}
				}
			}
			
			$scope.searchText = $routeParams.searchText;

			$scope.feature = getFeature($routeParams.featureId, $scope.report.features);
			$rootScope.reportDate = $scope.report.date.$date;
			$rootScope.loading = false;
		});
	
	});
}());