namespace ccrdb.pages {
    'use strict';

    export class StatisticsController {
        private productId: string;
        private type: string;
        private limit: any;
        private reports: any[];

        private chartObject: any = {
            options: {
                title: this.productId,
                vAxis: { title: 'Scenarios', titleTextStyle: { color: 'black' } },
                hAxis: { title: 'Date', titleTextStyle: { color: 'black' } },
                isStacked: true,
                colors: ['#5cb85c', '#f0ad4e', '#d9534f']
            }
        };

        static $inject = ['$stateParams', '$window', 'statisticsService', 'backButtonService', 'loadingBarService'];

        constructor(private $stateParams: ng.ui.IStateParamsService,
            private $window: ng.IWindowService,
            private statisticsService: ccrdb.pages.IStatisticsService,
            private backButtonService: ccrdb.core.BackButtonService,
            private loadingBarService: ccrdb.core.LoadingBarService) {
            this.getParams();
            this.loadStatistics();

            backButtonService.enabled = true;
            backButtonService.prevState = 'products';
            backButtonService.prevParams = {};
        }

        getParams() {
            this.productId = this.$stateParams['productId'];
            this.type = this.$stateParams['type'];
            this.chartObject.type = this.type;
            this.limit = this.$stateParams['limit'];
            if (this.limit === null) {
                this.limit = 'ALL';
            }
        }

        loadStatistics() {
            this.loadingBarService.reqCount++;
            this.statisticsService.getStatistics(this.productId, this.limit).then((reports: any[]) => {
                this.loadingBarService.reqCount--;

                this.reports = reports;
                this.drawCharts();
            });
        }

        drawCharts() {
            this.chartObject.data = [];
            this.chartObject.data.push(['Date', 'Passed', 'Unknown', 'Failed']);
            this.reports.forEach((report: any) => {
                let row: any[] = [];
                let date = new Date(report.report.date.$date);

                let failedScenariosSum = 0;
                let unknownScenariosSum = 0;
                let passedScenariosSum = 0;

                report.report.features.forEach((feature: any) => {
                    failedScenariosSum += this.getFailedScenarioCount(feature);
                    unknownScenariosSum += this.getUnknownScenarioCount(feature);
                    passedScenariosSum += this.getPassedScenarioCount(feature);
                });

                row.push(date.toString());
                row.push();
                row.push(passedScenariosSum);
                row.push(unknownScenariosSum);
                row.push(failedScenariosSum);
                this.chartObject.data.push(row);
            });
        }

        getFailedScenarioCount(feature: any) {
            let failedScenarios = 0;
            if (feature.scenarios !== undefined) {
                feature.scenarios.forEach(function (scenario: any) {
                    if (scenario.result.failedStepCount) {
                        failedScenarios++;
                    }
                });
            }
            return failedScenarios;
        }


        getUnknownScenarioCount(feature: any) {
            let unknownScenarios = 0;
            if (feature.scenarios !== undefined) {
                feature.scenarios.forEach(function (scenario: any) {
                    if (scenario.result.unknownStepCount && !scenario.result.failedStepCount) {
                        unknownScenarios++;
                    }
                });
            }
            return unknownScenarios;
        }

        getPassedScenarioCount(feature: any) {
            let passedScenarios = 0;
            if (feature.scenarios !== undefined) {
                feature.scenarios.forEach(function (scenario: any) {
                    if (scenario.result.passedStepCount && !scenario.result.failedStepCount) {
                        passedScenarios++;
                    }
                });
            }
            return passedScenarios;
        }
    }

    angular
        .module('ccrdb.pages')
        .controller('StatisticsController', StatisticsController);
}