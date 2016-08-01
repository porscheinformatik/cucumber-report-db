namespace ccrdb.pages {

    'use strict';

    export interface IRankingsService {
        getRankings: (productId: string, type: string) => ng.IPromise<any>;
    }

    export class RankingsService implements IRankingsService {
        static $inject = ['$http'];

        constructor(private $http: ng.IHttpService) {
        }

        getRankings(productId: string, type: string): ng.IPromise<any> {
            let typePath: string;
            if (type === 'failed') {
                typePath = '/mostFailedStepsRanking';
            } else if (type === 'executed') {
                typePath = '/mostExecutedStepsRanking';
            } else if (type === 'single') {
                typePath = '/highestSingleStepDurationRanking';
            } else {
                typePath = '/CumulatedStepDurationRanking';
            }
            return this.$http.get('rest/statistics/rankings/' + productId + typePath).then((response: any) => {
                let result: any = {};
                result.steps = response.data;
                result.sum = 0;

                result.steps.forEach((step: any) => {
                    result.sum += step.value;
                });

                result.steps.sort(function (a: any, b: any) {
                    let x = a['value']; let y = b['value'];
                    return ((x > y) ? +1 : ((x < y) ? -1 : 0));
                });

                return result;
            });
        }

        private success: (response: any) => {} = (response) => response.data;
    }

    angular.module('ccrdb.pages')
        .service('rankingsService', ccrdb.pages.RankingsService);
}