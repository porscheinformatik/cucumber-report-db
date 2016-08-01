namespace ccrdb.pages {

    'use strict';

    export interface IScenariosService {
        getFeature: (productId: string, reportId: string, featureId: string) => ng.IPromise<any[]>;
    }

    export class ScenariosService implements IScenariosService {
        static $inject = ['$http'];

        constructor(private $http: ng.IHttpService) {
        }

        getFeature(productId: string, reportId: string, featureId: string) {
            return this.$http.get('rest/query/' + productId + '/' + reportId).then((response: any) => {
                for (let i = 0; i < response.data[0].report.features.length; i++) {
                    if (response.data[0].report.features[i].id === featureId) {
                        let feature = response.data[0].report.features[i];

                        feature.scenarios.forEach((scenario: any) => {
                            if (scenario.result.failedStepCount) {
                                scenario.status = 'failed';
                            } else if (scenario.result.skippedStepCount || scenario.result.unknownStepCount) {
                                scenario.status = 'unknown';
                            } else {
                                scenario.status = 'passed';
                            }
                        });

                        return feature;
                    }
                }
            });
        }

        private success: (response: any) => {} = (response) => response.data;
    }

    angular.module('ccrdb.pages')
        .service('scenariosService', ccrdb.pages.ScenariosService);
}