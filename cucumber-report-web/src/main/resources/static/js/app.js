/* global window, document */
/* global serverUrl, fileBaseUrl, queryBaseUrl, collectionBaseUrl, reportFileName */

(function (angular, google, $, undefined) {
	'use strict';

	var app = angular.module('cucumber', ['ngRoute', 'ui.bootstrap', 'LocalStorageModule']);
	
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
			
			data.featureNames = '';
			
			angular.forEach(data.features, function(feature, index){
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
				
				data.featureNames += feature.name + (index === data.features.length-1 ? '' : ', ');
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
		.when('/statistics/:product/type/:type/limit/:limit', {
			templateUrl : 'pages/statistics.html',
			controller : 'StatisticsCtrl',
			resolve : loader
		})
		.when('/statistics/rankings/:product', {
			templateUrl : 'pages/rankings.html',
			controller : 'RankingsCtrl',
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

	app.run(function ($rootScope, $location, $routeParams, $sce, localStorageService) {

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
		
		$rootScope.openChart = function(product, type, limit) {
			if(typeof type === 'undefined'){
				type = localStorageService.get("chartsType");
				if(type === null){
					type = "ColumnChart";
				}
			}
			if(typeof limit === 'undefined'){
				limit = localStorageService.get("chartsLimit");
				if(limit === null){
					limit = 10;
				}
			}
			localStorageService.add("chartsType", type);
			localStorageService.add("chartsLimit", limit);
			$location.path('/statistics/' + product + '/type/' + type + '/limit/' + limit);
		};
		
		$rootScope.openRanking = function(product){
			$location.path('/statistics/rankings/' + product);
		};
		
		$rootScope.storageType = 'Local storage';
		if (!localStorageService.isSupported()) {
			$rootScope.storageType = 'Cookie';
		}
		
		$rootScope.$watch('databaseMode', function(value){
			localStorageService.add('databaseMode',value);
		});
		
		$rootScope.showJSONFileError = false;
		$rootScope.showDBError = false;
		$rootScope.databaseMode = localStorageService.get("databaseMode") || false;
		
		$rootScope.trustSrc = function(src) {
			return $sce.trustAsResourceUrl(src);
		};
	});
	
	
	
	function addSearchAndSortHandlers($scope, $filter, dataArray){
		$scope.$watch("searchText", function(query){
			$scope[$scope.searchArrayName] = $filter("filter")(dataArray, query);
			$scope[$scope.searchArrayName] = $filter('orderBy')($scope[$scope.searchArrayName], $scope.orderPredicate, $scope.orderReverse);
		});
		$scope.$watch("orderPredicate", function(query){
			if($scope.lastOrderPredicate !== query){
				$scope.orderReverse = query === "name" ? false : true;
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
		$rootScope.reportDate = $rootScope.report.date.$date;
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
	
	function prepareFeature(data, $rootScope, $scope, $routeParams, $filter)
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
		
		if($routeParams.searchText !== undefined)
		{
			$scope.searchText = $routeParams.searchText;
		}

		$scope.feature = getFeature($routeParams.featureId, $scope.report.features);
		$scope[$scope.searchArrayName] = $scope.feature.scenarios;
		
		addSearchAndSortHandlers($scope, $filter, $scope.feature.scenarios);
		
		$rootScope.reportDate = $scope.report.date.$date;
		$rootScope.loading = false;
	}
	
	/**
	 * ProductList Controller (see products.html)
	 */
	app.controller('ProductListCtrl', function($rootScope, $routeParams, $scope, $location, $filter, restApiQueryRequest, loadJsonFromFilesystem) {
		$scope.searchArrayName = 'filteredProducts';
		$scope.orderPredicate = "";
		$scope.orderReverse = true;
		$rootScope.searchText = "";
		$rootScope.backBtnEnabled = false;
		
		// if a local report.json file was found: load the data from the filesystem
		loadJsonFromFilesystem().success(function() {
			$rootScope.databaseMode = false;
			$rootScope.showDBError = false;
			$rootScope.showJSONFileError = false;
			$location.path('/reports/features/');
		})
		// else: load the data from the mongo database 
		.error(function() {
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
			.error(function() {
				$rootScope.showJSONFileError = true;
				$rootScope.showDBError = true;
			});
		});
		
	});
	
	/**
	 * ReportList Controller (see reports.html)
	 */
	app.controller('ReportListCtrl', function($rootScope, $routeParams, $http, $scope, $location, $filter, restApiCollectionRequest, restApiQueryRequest) {
		$scope.searchArrayName = 'filteredReports';
		$scope.orderPredicate = "";
		$scope.orderReverse = true;

		$scope.$routeParams = $routeParams;
		
		$rootScope.goBack = function() {
			$location.path('/products/');
		};
		$rootScope.backBtnEnabled = true;
		
		$scope.featuresOverview = function(date) {
			$location.path('/reports/' + $routeParams.colName + '/features/' + date);
		};
        $scope.deleteDocument = function (id) {
            $http.delete(queryBaseUrl + $routeParams.colName + '/' + id);
            load();
        };
 
		$rootScope.loading = true;
        function load()
        {
            restApiCollectionRequest(collectionBaseUrl + $routeParams.colName + "/")
                .success(function (data)
                {
                    $rootScope.databaseMode = true;

                    $scope.pageLimit = 10;
                    $scope.pages = Math.ceil(data.count / $scope.pageLimit);
                    $scope.page = $routeParams.page === undefined ? 0 : $routeParams.page;

                    restApiQueryRequest(queryBaseUrl + $routeParams.colName + '/?sort=true&limit=' + $scope.pageLimit + '&skip=' + ($scope.pageLimit * $scope.page))
                        .success(function (data)
                        {
                            $scope.reports = data;

                            $scope[$scope.searchArrayName] = $scope.reports;
                            addSearchAndSortHandlers($scope, $filter, $scope.reports);

                            $scope.getStatistics = function (features)
                            {
                                var statistics = {
                                    passed: 0,
                                    failed: 0,
                                    unknown: 0
                                };
                                angular.forEach(features, function (feature)
                                {
                                    statistics.passed += feature.result.passedScenarioCount;
                                    statistics.failed += feature.result.failedScenarioCount;
                                    statistics.unknown += feature.result.unknownScenarioCount;
                                });

                                var sum = (statistics.passed + statistics.failed + statistics.unknown);

                                statistics.passedPercent = (statistics.passed / sum) * 100;
                                statistics.failedPercent = (statistics.failed / sum) * 100;
                                statistics.unknownPercent = (statistics.unknown / sum) * 100;

                                return statistics;
                            };


                            $rootScope.loading = false;


                        })
                        .error(function ()
                        {
                            $rootScope.loading = false;
                            $location.path('/products/');
                        });

                })
                .error(function ()
                {
                    $rootScope.loading = false;
                    $location.path('/products/');
                });
        }
        load();
	});
	
	/**
	 * FeatureList Controller (see features.html)
	 */
	app.controller('FeatureListCtrl', function($rootScope, $routeParams, $scope, $location, $filter, restApiQueryRequest, loadJsonFromFilesystem) {
		
		$scope.colName = $routeParams.colName;
		// if a local report.json file was found: load the data from the filesystem
		loadJsonFromFilesystem().success(function(data) {
			$rootScope.databaseMode = false;
			$rootScope.backBtnEnabled = false;
			prepareReport(data, $rootScope, $scope, $routeParams, $filter, $location);
		})
		// else: load the data from the mongo database 
		.error(function() {
			$rootScope.databaseMode = true;
			$rootScope.backBtnEnabled = true;
			$rootScope.goBack = function() {
				$location.path('/reports/' + $routeParams.colName);
			};
			
			
			$rootScope.loading = true;
			restApiQueryRequest(queryBaseUrl + $routeParams.colName + '/?field=date&value=' + $routeParams.date)
			.success(function (data) {
				if(!data.length){
					$rootScope.loading = false;
					$location.path('/products/');
					return;
				}
				prepareReport(data[0], $rootScope, $scope, $routeParams, $filter, $location);
			})
			.error(function() {
				$rootScope.loading = false;
				$location.path('/products/');
			});
		});
	});

	/**
	 * Feature Controller (see feature.html)
	 */
	app.controller('FeatureCtrl', function($rootScope, $scope, $location, $filter, $routeParams, $modal, restApiQueryRequest, loadJsonFromFilesystem) {
		
		$scope.colName = $routeParams.colName;
		$scope.reportDate = $routeParams.date;
		
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
		.error(function() {
			$rootScope.databaseMode=true;
			$rootScope.loading = true;
			restApiQueryRequest(queryBaseUrl + $routeParams.colName + '/?field=date&value=' + $routeParams.date)
			.success(function (data) {
				if(!data.length){
					$rootScope.loading = false;
					$location.path('/products/');
					return;
				}
				prepareFeature(data[0], $rootScope, $scope, $routeParams, $filter, $location);
				
				// return rest api screenshot/video path
				$scope.getEmbedding = function(embedding) {
					return fileBaseUrl + $routeParams.colName + '/' + embedding.url + '/';
				};
			})
			.error(function() {
				$rootScope.loading = false;
				$location.path('/products/');
			});

		});

		$scope.errorLogLightbox = function(step) {
      var featureUri = $scope.feature.uri;
      var comments = "";

      if (step.comments) {
        $.each(step.comments, function(index, comment) {
          comments += (comments === "" ? "" : "<br />") + '<dd>' +
              comment.value + '</dd>';
        });
      }

      var template = '<div class="modal-header"><h3>Error Log</h3></div>' +
        '<div class="modal-body">' +
        '<button class="btn btn-default btn-xs" type="button" onclick="selectText(\'errorLogCode\')">Select all</button>' +
        '<pre id="errorLogCode" class="errorLogCode prettyprint lang-java">' +
        step.result.error_message +
        '</pre>' +
        '<dl><dt>Failed Step:</dt><dd>' +
        step.keyword +
        step.name +
        '</dd>' +
        (comments !== "" ? '<br /><dt>Comments:</dt>' + comments : '') +
        '<br /><dt>Feature File:</dt>' + '<dd>' + featureUri + ":" + step.line + '</dd>' + '</dl>' + '</div>' +
        '<div class="modal-footer"><button class="btn btn-primary" ng-click="$close()">Close</button></div>';

      $modal.open({
        template : template,
        size: 'lg'
      });

		};

    $scope.embeddingLightbox = function(embedded) {
      var scope = $rootScope.$new(true);
      scope.getEmbedding = $scope.getEmbedding;
      scope.embedded = embedded;
      $modal.open({
        templateUrl : 'pages/embedded_lightbox.html',
        size : 'lg',
        scope : scope
      });
    };

	});

	/**
	 * Help Controller (see help.html)
	 */
	app.controller('HelpCtrl', function($rootScope, $routeParams, $scope, $location) {
		
		$rootScope.goBack = function() {
			$location.path('/products/');
		};
		$rootScope.backBtnEnabled = true;
		
	});
	
	/**
	 * Statistics Controller (see statistics.html)
	 */
	app.controller('StatisticsCtrl', function($rootScope, $scope, $http, $location, $routeParams){    
		$rootScope.loading = true;

		$http.get(queryBaseUrl + $routeParams.product + '/?limit=' + $routeParams.limit).success(function(reportData) {
			var options = {
				title: $routeParams.product,
				vAxis: {title: 'Scenarios',  titleTextStyle: {color: 'black'}}, 
				hAxis: {title: 'Date',  titleTextStyle: {color: 'black'}}, 
				isStacked:true,
				colors:['#5cb85c','#f0ad4e','#d9534f']
			};

			var googleChart = new google.visualization[$routeParams.type](document.getElementById('chart'));
			googleChart.draw(google.visualization.arrayToDataTable(getResults(reportData)), options);
			
			$rootScope.loading = false;
		});

		$rootScope.goBack = function() {
			$location.path('/products/');
		};
		$rootScope.backBtnEnabled = true;
	});

	
	function sortByValue(array, value) {
	    return array.sort(function(a, b) {
	        var x = a[value]; var y = b[value];
	        return ((x < y) ? +1 : ((x > y) ? -1 : 0));
	    });};
	
  
	function durationInMS(durationInNS){
			var value=0;
			
				value = durationInNS; 	
				
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
	
		function sumOverAll(array,value){
			var sum=0;
			for (var i in array){
				   sum += array[i].value;
			}
			return sum;
		};
		
	/**
	 * Rankings Controller (see rankings.html)
	 */
	app.controller('RankingsCtrl',function($rootScope, $scope, $http, $location, $routeParams, $filter){
		$rootScope.loading = true;
		$scope.durationInMS=durationInMS;
		$scope.product=$routeParams.product;
		var rankingsRootPath='rest/statistics/rankings/';
		var failedPath='/mostFailedStepsRanking';
		var executedPath='/mostExecutedStepsRanking';
		var singleDurationPath='/highestSingleStepDurationRanking';
		var cumulatedDurationPath='/CumulatedStepDurationRanking';
		
		$http.get(rankingsRootPath + $routeParams.product + failedPath)
		.success(function(steps){
			$scope.failedSteps=steps;
			
			var sum=sumOverAll($scope.failedSteps.results,"value");
			$scope.sumOverAllFailed=sum;
			
			sortByValue($scope.failedSteps.results, "value");
			$rootScope.loading = false;});
		
		$http.get(rankingsRootPath + $routeParams.product + executedPath)
		.success(function(steps){
			$scope.executedSteps=steps;
			
			var sum=sumOverAll($scope.executedSteps.results,"value");
			$scope.sumOverAllExecuted=sum;
			
			sortByValue($scope.executedSteps.results, "value");
			$rootScope.loading = false;});
		
		$http.get(rankingsRootPath + $routeParams.product + singleDurationPath)
		.success(function(steps){
			$scope.singleSteps=steps;
			
			var sum=sumOverAll($scope.singleSteps.results,"value");
			$scope.sumOverAllSingle=sum;
			
			sortByValue($scope.singleSteps.results, "value");
			$rootScope.loading = false;});
		
		$http.get(rankingsRootPath + $routeParams.product + cumulatedDurationPath)
		.success(function(steps){
			$scope.cumulatedSteps=steps;
			
			var sum=sumOverAll($scope.cumulatedSteps.results,"value");
			$scope.sumOverAllCumulated=sum;
			
			sortByValue($scope.cumulatedSteps.results, "value");
			$rootScope.loading = false;});
		
		$rootScope.goBack = function() {
			$location.path('/products/');
		};
		$rootScope.backBtnEnabled = true;
	});

	google.load('visualization', '1', {packages: ['corechart']});
	$('#ReportFileName').text(reportFileName);
	$('#ReportFileNameLink').attr('href',reportFileName);
	$('#ServerURL').text(serverUrl);
	$('#ServerURLLink').attr('href',collectionBaseUrl);

}(window.angular, window.google, window.jQuery));
