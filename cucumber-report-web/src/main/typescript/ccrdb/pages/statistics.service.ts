namespace ccrdb.pages {

    'use strict';

    export interface IStatisticsService {
        getStatistics: (productId: string, limit: number) => ng.IPromise<any[]>;
    }

    export class StatisticsService implements IStatisticsService {
        static $inject = ['$http'];

        constructor(private $http: ng.IHttpService) {
        }

        getStatistics(productId: string, limit: any) {
            let query: string;
            if (limit !== null && !isNaN(limit) && limit !== '' && limit !== '0') {
                query = '?last=' + limit;
            } else {
                query = '';
            }
            return this.$http.get('rest/query/' + productId + '/' + query).then((response: any) => {
                return response.data;
            });
        }

        private success: (response: any) => {} = (response) => response.data;
    }

    angular.module('ccrdb.pages')
        .service('statisticsService', ccrdb.pages.StatisticsService);
}