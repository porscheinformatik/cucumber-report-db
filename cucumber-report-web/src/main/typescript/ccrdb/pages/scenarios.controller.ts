namespace ccrdb.pages {
    'use strict';

    export class ScenariosController {
        private productId: string;
        private reportId: string;
        private featureId: string;
        private feature: any;

        static $inject = ['$stateParams', '$uibModal', 'scenariosService', 'backButtonService', 'searchBarService', 'loadingBarService'];

        constructor(private $stateParams: ng.ui.IStateParamsService,
            private $uibModal: ng.ui.bootstrap.IModalService,
            private scenariosService: ccrdb.pages.IScenariosService,
            private backButtonService: ccrdb.core.BackButtonService,
            private searchBar: ccrdb.core.SearchBarService,
            private loadingBarService: ccrdb.core.LoadingBarService) {
            this.getParams();
            this.loadFeature();

            backButtonService.enabled = true;
            backButtonService.prevState = 'features';
            backButtonService.prevParams = { productId: this.productId, reportId: this.reportId };
        }

        getParams() {
            this.productId = this.$stateParams['productId'];
            this.reportId = this.$stateParams['reportId'];
            this.featureId = this.$stateParams['featureId'];
        }

        loadFeature() {
            this.loadingBarService.reqCount++;
            this.scenariosService.getFeature(this.productId, this.reportId, this.featureId).then((feature) => {
                this.loadingBarService.reqCount--;

                this.feature = feature;
            });
        }

        errorLogLightbox(step: any) {
            let featureUri = this.feature.uri;
            let comments = '';
            if (step.comments) {
                $.each(step.comments, function (index, comment) {
                    comments += (comments === '' ? '' : '<br />') + '<dd>' +
                        comment.value + '</dd>';
                });
            }

            let template = '<div class="modal-header"><h3>Error Log</h3></div>' +
                '<div class="modal-body">' +
                '<button class="btn btn-default btn-xs" type="button" onclick="selectText(\'errorLogCode\')">Select all</button>' +
                '<pre id="errorLogCode" class="errorLogCode prettyprint lang-java">' +
                this.htmlspecialchars(step.result.error_message) +
                '</pre>' +
                '<dl><dt>Failed Step:</dt><dd>' +
                step.keyword +
                step.name +
                '</dd>' +
                (comments !== '' ? '<br /><dt>Comments:</dt>' + comments : '') +
                '<br /><dt>Feature File:</dt>' + '<dd>' + featureUri + ':' + step.line + '</dd>' + '</dl>' + '</div>' +
                '<div class="modal-footer"><button class="btn btn-primary" ng-click="$close()">Close</button></div>';

            this.$uibModal.open({
                template: template,
                size: 'lg'
            });
        }

        htmlspecialchars(str: string) {
            return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&apos;');
        }

        formatTime(ns: number) {
            return Time.nsToString(ns);
        }
    }

    angular
        .module('ccrdb.pages')
        .controller('ScenariosController', ScenariosController);
}