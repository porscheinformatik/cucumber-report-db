namespace ccrdb.pages {

    'use strict';

    export interface IFeaturesService {
        getFeatures: (productId: string, reportId: string) => ng.IPromise<any[]>;
        generateSums: (features: any[]) => any;
    }

    export class FeaturesService implements IFeaturesService {
        static $inject = ['$http'];

        constructor(private $http: ng.IHttpService) {
        }

        getFeatures(productId: string, reportId: string) {
            return this.$http.get('rest/query/' + productId + '/' + reportId).then((response: any) => {
                let features: any[] = response.data[0].report.features;
                features.forEach(feature => {
                    // need to be supplemented
                    feature.result.passedScenarioCount = 0;
                    feature.result.unknownScenarioCount = 0;
                    feature.result.failedScenarioCount = 0;

                    // feature.result.stepCount seems broken, has to be recalculated from scenarios
                    feature.result.stepCount = 0;

                    feature.scenarios.forEach((scenario: any) => {
                        if (scenario.result.failedStepCount) {
                            feature.result.failedScenarioCount++;
                        } else if (scenario.result.skippedStepCount) {
                            feature.result.unknownScenarioCount++;
                        } else {
                            feature.result.passedScenarioCount++;
                        }
                        feature.result.stepCount += scenario.result.stepCount;
                    });
                });

                return features;
            });
        }

        generateSums(features: any[]) {
            let featureSums = {
                duration: String,
                passedScenarioCount: Number,
                unknownScenarioCount: Number,
                failedScenarioCount: Number,
                scenarioCount: Number,
                passedStepCount: Number,
                unknownStepCount: Number,
                failedStepCount: Number,
                skippedStepCount: Number,
                stepCount: Number
            };

            featureSums.duration = this.sum(features, 'duration');
            featureSums.passedScenarioCount = this.sum(features, 'passedScenarioCount');
            featureSums.unknownScenarioCount = this.sum(features, 'unknownScenarioCount');
            featureSums.failedScenarioCount = this.sum(features, 'failedScenarioCount');
            featureSums.scenarioCount = this.sum(features, 'scenarioCount');
            featureSums.passedStepCount = this.sum(features, 'passedStepCount');
            featureSums.unknownStepCount = this.sum(features, 'unknownStepCount');
            featureSums.failedStepCount = this.sum(features, 'failedStepCount');
            featureSums.skippedStepCount = this.sum(features, 'skippedStepCount');
            featureSums.stepCount = this.sum(features, 'stepCount');

            return featureSums;
        }

        sum(features: any[], field: string) {
            if (!(features.length > 0)) { return null; }
            let sum = features.reduce(function (sum, feature) {
                let value = parseInt(feature.result[field], 10);
                return isNaN(value) ? sum : sum + value;
            }, 0);
            return sum;
        };

        private success: (response: any) => {} = (response) => response.data;
    }

    angular.module('ccrdb.pages')
        .service('featuresService', ccrdb.pages.FeaturesService);
}