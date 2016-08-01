namespace ccrdb.pages {

    'use strict';

    export interface IReportsService {
        getReportCount: (productId: string) => ng.IPromise<number>;
        getReports: (productId: string, limit: number, skip: number, versionId: string, categoryId: string) => ng.IPromise<any[]>;
        getVersions: (productId: string) => ng.IPromise<String[]>;
        getCategories: (productId: string) => ng.IPromise<String[]>;
        prepareReportData: (productId: string, limit: number, skip: number, versionId: string, categoryId: string) => ng.IPromise<any[]>;
        deleteDocument: (productId: string, id: string) => ng.IPromise<boolean>;
    }

    export class ReportsService implements IReportsService {
        static $inject = ['$http'];

        constructor(private $http: ng.IHttpService) {
        }

        getReportCount(productId: string) {
            return this.$http.get('rest/collection/' + productId + '/count')
                .then(this.success);
        }

        getReports(productId: string, limit: number, skip: number, versionId: string, categoryId: string) {
            let filters = '';
            if (versionId !== 'ALL') {
                filters += '&version=' + versionId;
            }
            if (categoryId !== 'ALL') {
                filters += '&category=' + categoryId;
            }
            return this.$http.get('rest/query/' + productId + '/?sort=true&limit=' + limit + '&skip=' + skip + filters)
                .then(this.success);
        }

        getVersions(productId: string) {
            return this.$http.get('rest/collection/' + productId + '/versions')
                .then(this.success);
        }

        getCategories(productId: string) {
            return this.$http.get('rest/collection/' + productId + '/categories')
                .then(this.success);
        }

        prepareReportData(productId: string, limit: number, skip: number, versionId: string, categoryId: string) {
            return this.getReports(productId, limit, skip, versionId, categoryId).then((reports: any[]) => {
                reports.forEach(report => {
                    report.statistics = {
                        passed: 0,
                        failed: 0,
                        unknown: 0,
                        passedPercent: 0,
                        failedPercent: 0,
                        unknownPercent: 0
                    };
                    report.featureNames = '';

                    report.report.features.forEach((feature: any) => {
                        feature.scenarios.forEach((scenario: any) => {
                            if (scenario.result.failedStepCount) {
                                report.statistics.failed++;
                            } else if (scenario.result.skippedStepCount) {
                                report.statistics.unknown++;
                            } else {
                                report.statistics.passed++;
                            }
                        });

                        report.featureNames += feature.name + ', ';
                    });
                    report.featureNames = report.featureNames.slice(0, report.featureNames.length - 2);

                    let sum = (report.statistics.passed + report.statistics.failed + report.statistics.unknown);

                    report.statistics.passedPercent = (report.statistics.passed / sum) * 100;
                    report.statistics.failedPercent = (report.statistics.failed / sum) * 100;
                    report.statistics.unknownPercent = (report.statistics.unknown / sum) * 100;
                });
                return reports;
            });
        }

        deleteDocument(productId: string, id: string) {
            return this.$http.delete('rest/query/' + productId + '/' + id).success(() => {
                return true;
            }).error(() => {
                return false;
            });
        }

        private success: (response: any) => {} = (response) => response.data;
    }

    angular.module('ccrdb.pages')
        .service('reportsService', ccrdb.pages.ReportsService);
}