namespace ccrdb.pages {
    'use strict';

    export class RankingsController {
        private productId: string;

        private failedSteps: any[];
        private sumOverAllFailed: number;

        private executedSteps: any[];
        private sumOverAllExecuted: number;

        private singleSteps: any[];
        private sumOverAllSingle: number;

        private cumulatedSteps: any[];
        private sumOverAllCumulated: number;

        private selectedStep: string = '';
        private showAll_Failed: boolean = false;
        private showAll_MostExecuted: boolean = false;
        private showAll_SingleStepDurations: boolean = false;
        private showAll_CumulatedStepDuration: boolean = false;

        static $inject = ['$stateParams', 'rankingsService', 'backButtonService', 'loadingBarService'];

        constructor(private $stateParams: ng.ui.IStateParamsService,
            private rankingsService: ccrdb.pages.IRankingsService,
            private backButtonService: ccrdb.core.BackButtonService,
            private loadingBarService: ccrdb.core.LoadingBarService) {
            this.getParams();
            this.loadRankings();

            backButtonService.enabled = true;
            backButtonService.prevState = 'products';
            backButtonService.prevParams = {};
        }

        getParams() {
            this.productId = this.$stateParams['productId'];
        }

        loadRankings() {
            this.loadingBarService.reqCount += 4;
            this.rankingsService.getRankings(this.productId, 'failed').then((failed: any) => {
                this.loadingBarService.reqCount--;

                this.failedSteps = failed.steps;
                this.sumOverAllFailed = failed.sum;
            });

            this.rankingsService.getRankings(this.productId, 'executed').then((executed: any) => {
                this.loadingBarService.reqCount--;

                this.executedSteps = executed.steps;
                this.sumOverAllExecuted = executed.sum;
            });

            this.rankingsService.getRankings(this.productId, 'single').then((single: any) => {
                this.loadingBarService.reqCount--;

                this.singleSteps = single.steps;
                this.sumOverAllSingle = single.sum;
            });

            this.rankingsService.getRankings(this.productId, 'cumulated').then((cumulated: any) => {
                this.loadingBarService.reqCount--;

                this.cumulatedSteps = cumulated.steps;
                this.sumOverAllCumulated = cumulated.sum;
            });
        }

        selectStep(step: any) {
            if (this.selectedStep === step) {
                this.selectedStep = '';
            } else {
                this.selectedStep = step;
            }
        }

        formatTime(ns: number) {
            return Time.nsToString(ns);
        }
    }

    angular
        .module('ccrdb.pages')
        .controller('RankingsController', RankingsController);
}