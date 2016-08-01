/* global window, document */
/* global serverUrl, fileBaseUrl, queryBaseUrl, collectionBaseUrl, reportFileName */

(function (angular, google, $, undefined) {
	'use strict';

	var app = angular.module('cucumber', ['ngRoute', 'ui.bootstrap', 'LocalStorageModule'])
        .directive('ngReallyClick', [function() {
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {
                element.bind('click', function() {
                    var message = attrs.ngReallyMessage;
                    if (message && confirm(message)) {
                        scope.$apply(attrs.ngReallyClick);
                    }
                });
            }
        }
    }]);

	var prepareReportData = function(reports) {
		angular.forEach(reports, function(data){
			function getFailedScenarioCount(feature) {
				var failedScenarios = 0;
				feature.scenarios.forEach(function(scenario) {
					if(scenario.result.failedStepCount || scenario.result.stepCount === scenario.result.skippedStepCount){
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

			angular.forEach(data.report.features, function(feature, index){
        if(feature.scenarios === undefined || feature.scenarios.length === 0)
        {
            feature.result = {};
            feature.result.failedScenarioCount = 0;
            feature.result.unknownScenarioCount = 0;
            feature.result.passedScenarioCount = 0;
            feature.result.duration = 0;
        }
				else {
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

					if (scenario.result.failedStepCount || scenario.result.stepCount === scenario.result.skippedStepCount) {
						scenario.status = "failed";
					} else if (scenario.result.unknownStepCount) {
							scenario.status = 'unknown';
					} else {
						scenario.status = 'passed';
					}
					scenario.result.searchKeyword = ":" + scenario.status + "Scenario";
				});

				feature.result.passedStepCount = feature.result.passedStepCount || 0;
				feature.result.failedStepCount = feature.result.failedStepCount || 0;
				feature.result.unknownStepCount = feature.result.unknownStepCount || 0;
				feature.result.skippedStepCount = feature.result.skippedStepCount || 0;

				data.featureNames += feature.name + (index === data.report.features.length-1 ? '' : ', ');
			});


			data.report.duration = function(feature){
				var value=0;

				if (!isNaN(feature)) {
					value = feature;
				} else if(feature.result.duration) {
					value = feature.result.duration;
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
				return $http.get(url).success(function(data) {
				  prepareReportData(data);
				  return data;
				});
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
		.when('/reports/:colName/version/:version/category/:category', {
			templateUrl : 'pages/reports.html',
			controller : 'ReportListCtrl',
			resolve : loader
		})
		.when('/reports/:colName/features/:repId', {
			templateUrl : 'pages/features.html',
			controller : 'FeatureListCtrl',
			resolve : loader
		})
		.when('/reports/:colName/features/:repId/feature/:featureId', {
			templateUrl : 'pages/feature.html',
			controller : 'FeatureCtrl',
			resolve : loader
		})
    .when('/assignments/', {
      templateUrl : 'pages/assignments.html',
      controller : 'AssignmentsCtrl',
      resolve : loader
    })
		.otherwise({
			redirectTo : '/products/'
		});
	} ]);

	app.run(function ($rootScope, $location, $routeParams, $sce, $http, localStorageService) {

		$rootScope.deletionMode = false;

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

		$rootScope.toggleDeletionMode = function(){
			$rootScope.deletionMode = !$rootScope.deletionMode;
		};
		
    $rootScope.reportsOverview = function(product, version, category){
      if(typeof version === 'undefined'){
        version = "ALL";
      }
      if(typeof category === 'undefined'){
        category = "ALL";
      }
      var path = $location.path('/reports/' + product + '/version/' + version + '/category/' + category);
      $location.search('page', 0);
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

		$rootScope.bulkDelete = function(product){
			$http.delete(queryBaseUrl + product+'/_ALL');
            $location.path('/#');
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
		$rootScope.report = data.report;
		//TODO change next line to id?
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
			$location.path('/reports/' + $routeParams.colName + '/features/' + $routeParams.repId + '/feature/' + feature.id);
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

		//TODO change next line to id?
		$rootScope.reportDate = $scope.report.date.$date;
		$rootScope.loading = false;
	}

    app.controller('Menu', function($scope, $http) {
        $http.get(productsUrl).success(function (res) {
            $scope.products = res;
        });
    });

	/**
	 * ProductList Controller (see products.html)
	 */
	app.controller('ProductListCtrl', function($rootScope, $routeParams, $scope, $location, $filter, restApiQueryRequest, loadJsonFromFilesystem, restApiCollectionRequest) {
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
            $scope.productCondition = function(input)
            {
                if(input.indexOf($routeParams.product) !== -1 || $routeParams.product === undefined){
                    return input;
                }
                return false;
            };

			restApiCollectionRequest(collectionBaseUrl).success(function (data) {
				$rootScope.showDBError = false;
				$rootScope.showJSONFileError = false;
				$scope.products = data.slice().reverse();

				$scope[$scope.searchArrayName] = $scope.products;
				addSearchAndSortHandlers($scope, $filter, $scope.products);

                $scope.reportsForProduct = {};

				angular.forEach($scope.products, function (product)
                    {

                restApiQueryRequest(queryBaseUrl + product + '/?sort=true&limit=' + 1 + '&skip=' + 0)
                        .success(function (data)
                        {

                            $scope.reportsForProduct[product] = data;

                            var date = 0;
                            $scope.getLastTestDateForProduct = function(product){
                            var reportsForProduct = $scope.reportsForProduct[product];
                            angular.forEach(reportsForProduct, function (featureList)
                                {
                                    date = featureList.date.$date;
                                });
                                    return date;
                            };

                            $scope.getLastStatisticsForProduct = function (product)
                            {
                                var statistics = {
                                    passed: 0,
                                    failed: 0,
                                    unknown: 0
                                };

                                angular.forEach($scope.reportsForProduct[product], function (featureList)
                                    {
                                         angular.forEach(featureList.features, function (feature)
                                            {
                                                statistics.passed += feature.result.passedScenarioCount;
                                                statistics.failed += feature.result.failedScenarioCount;
                                                statistics.unknown += feature.result.unknownScenarioCount;
                                        });
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
                    });
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


		$rootScope.goBack = function() {
			$location.path('/products/');
		};
		$rootScope.backBtnEnabled = true;

    $scope.deleteDocument = function (id) {
        $http.delete(queryBaseUrl + $routeParams.colName + '/' + id);
        load();
    };

    $http.get(rolesBaseUrl+"current/").success(function (data) {
        $scope.isAdmin = $.inArray("ROLE_ADMIN", data) >= 0;
    });
    
    function load()
    {
        var filters = "";
        if($routeParams.version !== "ALL"){
            filters += '&version=' + $routeParams.version;
        }
        if($routeParams.category !== "ALL"){
            filters += '&category=' + $routeParams.category;
        }
        
        restApiCollectionRequest(queryBaseUrl + $routeParams.colName + "/?sort=false" + filters)
            .success(function (data)
            {
                $rootScope.databaseMode = true;

                $scope.pageLimit = 10;
                $scope.page = $routeParams.page === undefined ? 0 : $routeParams.page;
                $scope.pages = Math.ceil(data.length / $scope.pageLimit);

                restApiCollectionRequest(collectionBaseUrl + $routeParams.colName + '/versions')
                    .success(function(data)
                    {
                        $scope.versions = data;
                    })
                    .error(function ()
                    {
                        $rootScope.loading = false;
                        $location.path('/products/');
                    });
                
                restApiCollectionRequest(collectionBaseUrl + $routeParams.colName + '/categories')
                .success(function(data)
                {
                    $scope.categories = data;
                })
                .error(function ()
                {
                    $rootScope.loading = false;
                    $location.path('/products/');
                });
                
                restApiQueryRequest(queryBaseUrl + $routeParams.colName + '/?sort=true&limit=' + $scope.pageLimit + '&skip=' + ($scope.pageLimit * $scope.page) + filters)
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
		//TODO remove or update success
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
			restApiQueryRequest(queryBaseUrl + $routeParams.colName + '/' + $routeParams.repId)
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
		$scope.reportId = $routeParams.repId;

		$rootScope.goBack = function() {
			$location.path('/reports/' + $routeParams.colName + '/features/' + $routeParams.repId);
		};
		$rootScope.backBtnEnabled = true;

		$scope.$routeParams = $routeParams;

		// if a local report.json file was found: load the data from the filesystem
		// TODO remove or update success
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
			restApiQueryRequest(queryBaseUrl + $routeParams.colName + '/' + $routeParams.repId)
			.success(function (data) {
				if(!data.length){
					$rootScope.loading = false;
					$location.path('/products/');
					return;
				}
				prepareFeature(data[0].report, $rootScope, $scope, $routeParams, $filter, $location);

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
            if (step.comments)
            {
                $.each(step.comments, function (index, comment)
                {
                    comments += (comments === "" ? "" : "<br />") + '<dd>' +
                        comment.value + '</dd>';
                });
            }

            var template = '<div class="modal-header"><h3>Error Log</h3></div>' +
                '<div class="modal-body">' +
                '<button class="btn btn-default btn-xs" type="button" onclick="selectText(\'errorLogCode\')">Select all</button>' +
                '<pre id="errorLogCode" class="errorLogCode prettyprint lang-java">' +
                htmlspecialchars(step.result.error_message) +
                '</pre>' +
                '<dl><dt>Failed Step:</dt><dd>' +
                step.keyword +
                step.name +
                '</dd>' +
                (comments !== "" ? '<br /><dt>Comments:</dt>' + comments : '') +
                '<br /><dt>Feature File:</dt>' + '<dd>' + featureUri + ":" + step.line + '</dd>' + '</dl>' + '</div>' +
                '<div class="modal-footer"><button class="btn btn-primary" ng-click="$close()">Close</button></div>';

            $modal.open({
                template: template,
                size: 'lg'
            });
		};
        $scope.getHeaderRow = function(step) {
          return step.rows[0];
        };

        $scope.getDataRows = function(step) {
          return step.rows.slice().splice(1);
        };

        $scope.isEmbeddedImage = function (mimeType) {
            return mimeType === 'image/png' || mimeType === 'image/bmp';
        };
        $scope.isEmbeddedVideo = function (mimeType) {
            return mimeType === 'video/mp4';
        };
		$scope.isEmbeddedZIP = function (mimeType) {
			return mimeType === 'application/zip';
		};
        $scope.downloadUrl = function(embedded) {
            return fileBaseUrl + $routeParams.colName + '/' + embedded.url + '/';
        };
        $scope.getFileEnding = function(embedded) {
            return embedded.url.substr(embedded.url.lastIndexOf(".")+1);
        };
        $scope.getFileEndingWithCapitalFirstLetter = function(embedded) {
            var fileEnding = $scope.getFileEnding(embedded);
            return fileEnding.charAt(0).toUpperCase() + fileEnding.slice(1);
        };


        $scope.embeddingLightbox = function(embedded) {
          var scope = $rootScope.$new(true);
          scope.getEmbedding = $scope.getEmbedding;
          scope.embedded = embedded;
          scope.isImage = $scope.isEmbeddedImage(embedded.mime_type);
          scope.isVideo = $scope.isEmbeddedVideo(embedded.mime_type);
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

        var url = queryBaseUrl + $routeParams.product + '/';
        if($routeParams.limit !== '0') {
            url += '?last=' + $routeParams.limit;
        }
        $http.get(url).success(function(reportData) {
          reportData = reportData;
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
        });
    }
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
    }
    function sumOverAll(array,value){
			var sum=0;
			for (var i in array){
				   sum += array[i].value;
			}
			return sum;
    }
    /**
	 * Rankings Controller (see rankings.html)
	 */
    app.controller('RankingsCtrl', function ($rootScope, $scope, $http, $location, $routeParams)
    {
        $rootScope.loading = true;
		$scope.durationInMS=durationInMS;
		$scope.product=$routeParams.product;
		var rankingsRootPath='rest/statistics/rankings/';
		var failedPath='/mostFailedStepsRanking';
		var executedPath='/mostExecutedStepsRanking';
		var singleDurationPath='/highestSingleStepDurationRanking';
		var cumulatedDurationPath='/CumulatedStepDurationRanking';
		$scope.selectedStep=null;
		$scope.selectStep=function(step){
			if($scope.selectedStep===step){
				$scope.selectedStep=null;
			}
			else{
				$scope.selectedStep=step;
			}
		};

		$http.get(rankingsRootPath + $routeParams.product + failedPath)
		.success(function(steps){
			$scope.failedSteps=steps;

			var sum=sumOverAll($scope.failedSteps,"value");
			$scope.sumOverAllFailed=sum;

			sortByValue($scope.failedSteps, "value");
			$rootScope.loading = false;});

		$http.get(rankingsRootPath + $routeParams.product + executedPath)
		.success(function(steps){
			$scope.executedSteps=steps;

			var sum=sumOverAll($scope.executedSteps,"value");
			$scope.sumOverAllExecuted=sum;

			sortByValue($scope.executedSteps, "value");
			$rootScope.loading = false;});

		$http.get(rankingsRootPath + $routeParams.product + singleDurationPath)
		.success(function(steps){
			$scope.singleSteps=steps;

			var sum=sumOverAll($scope.singleSteps,"value");
			$scope.sumOverAllSingle=sum;

			sortByValue($scope.singleSteps, "value");
			$rootScope.loading = false;});

		$http.get(rankingsRootPath + $routeParams.product + cumulatedDurationPath)
		.success(function(steps){
			$scope.cumulatedSteps=steps;

			var sum=sumOverAll($scope.cumulatedSteps,"value");
			$scope.sumOverAllCumulated=sum;

			sortByValue($scope.cumulatedSteps, "value");
			$rootScope.loading = false;});

		$rootScope.goBack = function() {
			$location.path('/products/');
		};
		$rootScope.backBtnEnabled = true;
	});
    


  /**
   * Assignments Controller (see assignments.html)
   */
  app.controller('AssignmentsCtrl', function($rootScope, $routeParams, $scope, $location, $filter, $modal, restApiQueryRequest, restApiCollectionRequest, loadJsonFromFilesystem) {
    $scope.searchArrayName = 'products';
    $scope.orderPredicate = "";
    $scope.orderReverse = true;
    $rootScope.searchText = "";
    $rootScope.backBtnEnabled = false;

    $rootScope.databaseMode = true;
    
    $scope.showGroups = function(product) {
      
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
    
    $scope.hideModal = function(){
      $scope.showModal = false;
    }

    $scope.open = function (product) {

      var modalInstance = $modal.open({
        templateUrl: 'GroupModalContent.html',
        controller: 'GroupModalInstanceCtrl',
        resolve: {
          groups: function(){
            return restApiCollectionRequest(rightsBaseUrl+product).then(function(res) {
              return res.data;
            });
          },
          product: function(){
            return product;
          }
        }
      });

      modalInstance.result.then(function () {
      }, function () {
        $log.info('Modal dismissed at: ' + new Date());
      });
    };
  });

  app.controller('GroupModalInstanceCtrl', function ($scope, $modalInstance, $http, product, groups) {
    $scope.newGroup = {name : ""};

    $scope.groups = groups;
    $scope.product = product;
    
    $scope.addGroup = function(){
      if($scope.newGroup.name !== "" && groups.findIndex(function(element,index,array){return $scope.newGroup.name === element;}) <= -1){
        $http.post(rightsBaseUrl+$scope.product, $scope.newGroup.name).success(function(){
          groups.push($scope.newGroup.name);
        });
        
        //TODO error case
      }
    }
    
    $scope.removeGroup = function (index) {
      $http.delete(rightsBaseUrl+$scope.product+'/'+$scope.groups[index]).success(function(){
        groups.splice(index,1);
      });
      
      //TODO error case
    }

    $scope.ok = function () {
      $modalInstance.close();
    };
  });

	google.load('visualization', '1', {packages: ['corechart']});
	$('#ReportFileName').text(reportFileName);
	$('#ReportFileNameLink').attr('href',reportFileName);
	$('#ServerURL').text(serverUrl);
	$('#ServerURLLink').attr('href',collectionBaseUrl);

}(window.angular, window.google, window.jQuery));
