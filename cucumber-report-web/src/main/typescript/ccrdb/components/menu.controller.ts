namespace ccrdb.components {
    'use strict';

    export class MenuController {
        private products: String[];

        static $inject = ['$state', 'productsService', 'authenticationService', 'deletionModeService', 'backButtonService',
            'searchBarService'];

        constructor(
            private $state: ng.ui.IStateService,
            private productsService: ccrdb.pages.IProductsService,
            private authenticationService: ccrdb.core.AuthenticationService,
            private deletionModeService: ccrdb.core.DeletionModeService,
            private backButtonService: ccrdb.core.BackButtonService,
            private searchBar: ccrdb.core.SearchBarService) {
        }

        toggleDeletionMode() {
            this.deletionModeService.enabled = !this.deletionModeService.enabled;
        }

        isAdmin() {
            return this.authenticationService.isAdmin;
        }

        clearCache() {
            // TODO make it do whatever it's purpose is
        }

        goBack() {
            this.$state.go(this.backButtonService.prevState, this.backButtonService.prevParams);
        }
    }

    angular
        .module('ccrdb.components')
        .controller('MenuController', MenuController);
}