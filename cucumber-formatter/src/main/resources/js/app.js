(function () {
	"use strict";
	var app = window.angular.module('cucumber', ['ngRoute', 'ui.bootstrap', 'LocalStorageModule']);
	
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
			redirectTo : '/reports//features/'
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
	
	app.run(function ($rootScope, $templateCache, $location, $routeParams, $sce, localStorageService) {

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
				$scope.orderReverse = query !== "name";
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
	 * FeatureList Controller (see features.html)
	 */
	app.controller('FeatureListCtrl', function($rootScope, $routeParams, $scope, $location, $filter, $templateCache, restApiQueryRequest, loadJsonFromFilesystem) {
		
		$scope.colName = $routeParams.colName;
		// if a local report.json file was found: load the data from the filesystem
		loadJsonFromFilesystem().success(function(data) {
			$rootScope.databaseMode = false;
			$rootScope.backBtnEnabled = false;
			prepareReport(data, $rootScope, $scope, $routeParams, $filter, $location);
		});
	});

	/**
	 * Feature Controller (see feature.html)
	 */
	app.controller('FeatureCtrl', function($rootScope, $scope, $location, $filter, $routeParams, $templateCache, restApiQueryRequest, loadJsonFromFilesystem) {
		
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
		});
		
		$scope.errorLogLightbox = function(step) {
			//var stepScope = angular.element(document.getElementById(stepId)).scope();
			//var stepScope=$scope;
			//var step = stepScope.step;
			//var step = $scope.step;
			var featureUri = $scope.feature.uri;
			var comments = "";

			if (step.comments) {
				$.each(step.comments, function(index, comment) {
					comments += (comments === "" ? "" : "<br />") + '<dd>' +
							comment.value + '</dd>';
				});
			}
			$.colorbox({
				html : '<div class="errorLogContent">' +
						'<h4><strong>Error Log</strong></h4>' +
						'<button class="btn btn-default btn-xs" type="button" onclick="selectText(\'errorLogCode\')">Select all</button>' +
						'<pre id="errorLogCode" class="errorLogCode prettyprint lang-java">' +
						step.result.error_message +
						'</pre>' +
						'<dl>' +
						'<dt>Failed Step:</dt>' +
						'<dd>' +
						step.keyword +
						step.name +
						'</dd>' +
						(comments !== "" ? '<br /><dt>Comments:</dt>' +
								comments : '') +
						'<br /><dt>Feature File:</dt>' + '<dd>' + featureUri +
						":" + step.line + '</dd>' + '</dl>' + '</div>',
				width : "75%",
				trapFocus : false
			});
		};
		
		$scope.embeddingLightbox = function(scenarioIdx, stepIdx) {
			$.colorbox({
				inline : true,
				width : "75%",
				href : '#lightbox_'+scenarioIdx+'_'+stepIdx,
				closeButton : true,
				trapFocus : false
			});
		};
	});
	

	$('#ReportFileName').text(reportFileName);
	$('#ReportFileNameLink').attr('href',reportFileName);
	$('#ServerURL').text(serverUrl);
	$('#ServerURLLink').attr('href',collectionBaseUrl);
	
}());
