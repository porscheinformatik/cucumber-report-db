namespace ccrdb.pages {
    'use strict';

    export class FeaturesController {
        private productId: string;
        private reportId: string;
        private features: any[];
        private featureSums: any;


        static $inject = ['$stateParams', 'featuresService', 'backButtonService', 'searchBarService', 'loadingBarService'];

        constructor(private $stateParams: ng.ui.IStateParamsService,
            private featuresService: ccrdb.pages.IFeaturesService,
            private backButtonService: ccrdb.core.BackButtonService,
            private searchBar: ccrdb.core.SearchBarService,
            private loadingBarService: ccrdb.core.LoadingBarService) {
            this.getParams();
            this.loadFeatures();

            backButtonService.enabled = true;
            backButtonService.prevState = 'reports';
            backButtonService.prevParams = { productId: this.productId, versionId: 'ALL', categoryId: 'ALL' };
        }

        getParams() {
            this.productId = this.$stateParams['productId'];
            this.reportId = this.$stateParams['reportId'];
        }

        loadFeatures() {
            this.loadingBarService.reqCount++;
            this.featuresService.getFeatures(this.productId, this.reportId).then((features) => {
                this.loadingBarService.reqCount--;

                this.features = features;
                this.featureSums = this.featuresService.generateSums(this.features);
            });
        }

        formatTime(ns: number) {
            return Time.nsToString(ns);
        }
    }

    angular
        .module('ccrdb.pages')
        .controller('FeaturesController', FeaturesController);
}