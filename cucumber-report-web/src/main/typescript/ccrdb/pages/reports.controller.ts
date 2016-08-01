namespace ccrdb.pages {
    'use strict';

    export class ReportsController {
        private productId: string;
        private versionId: string;
        private categoryId: string;
        private versions: String[];
        private categories: String[];
        private pageLimit = 10;
        private page: number;
        private pages: number[];
        private reports: any[];
        private getStatistics: Function;

        static $inject = ['$state', '$stateParams', 'reportsService', 'authenticationService', 'backButtonService',
            'searchBarService', 'loadingBarService'];

        constructor(private $state: ng.ui.IStateService,
            private $stateParams: ng.ui.IStateParamsService,
            private reportsService: ccrdb.pages.IReportsService,
            private authenticationService: ccrdb.core.AuthenticationService,
            private backButtonService: ccrdb.core.BackButtonService,
            private searchBar: ccrdb.core.SearchBarService,
            private loadingBarService: ccrdb.core.LoadingBarService) {
            this.getParams();
            this.loadVersions();
            this.loadCategories();
            this.loadReports();

            backButtonService.enabled = true;
            backButtonService.prevState = 'products';
            backButtonService.prevParams = {};
        }

        getParams() {
            this.productId = this.$stateParams['productId'];
            this.versionId = this.$stateParams['versionId'];
            this.categoryId = this.$stateParams['categoryId'];
            this.page = this.$stateParams['page'] === undefined ? 0 : parseInt(this.$stateParams['page']);

            this.loadingBarService.reqCount++;
            this.reportsService.getReportCount(this.productId).then((count: number) => {
                this.loadingBarService.reqCount--;

                this.pages = [];
                const pageCount = count / this.pageLimit;
                for (let i = 0; i < pageCount; i++) {
                    this.pages.push(i);
                }
            });
        }

        loadVersions() {
            this.loadingBarService.reqCount++;
            this.reportsService.getVersions(this.productId).then((versions) => {
                this.loadingBarService.reqCount--;

                this.versions = versions;
            });
        }

        loadCategories() {
            this.loadingBarService.reqCount++;
            this.reportsService.getCategories(this.productId).then((categories) => {
                this.loadingBarService.reqCount--;

                this.categories = categories;
            });
        }

        loadReports() {
            this.loadingBarService.reqCount++;
            this.reportsService.prepareReportData(this.productId, this.pageLimit, (this.page * this.pageLimit), this.versionId, this.categoryId).then((reports) => {
                this.loadingBarService.reqCount--;

                this.reports = reports;
            });
        }

        deleteDocument(id: string) {
            this.loadingBarService.reqCount++;
            this.reportsService.deleteDocument(this.productId, id).then((success: boolean) => {
                this.loadingBarService.reqCount--;

                if (success) {
                    this.$state.reload();
                } else {
                    console.error('Failed to delete report.');
                }
            });
        }

        isAdmin() {
            return this.authenticationService.isAdmin;
        }
    }

    angular
        .module('ccrdb.pages')
        .controller('ReportsController', ReportsController);
}