namespace ccrdb.pages {
    'use strict';

    export class ProductsController {
        private products: String[];


        static $inject = ['$rootScope', '$state', 'productsService', 'deletionModeService', 'backButtonService',
            'searchBarService', 'loadingBarService'];

        constructor(private $rootScope: ng.IRootScopeService,
            private $state: ng.ui.IStateService,
            private productsService: ccrdb.pages.IProductsService,
            private deletionModeService: ccrdb.core.DeletionModeService,
            private backButtonService: ccrdb.core.BackButtonService,
            private searchBar: ccrdb.core.SearchBarService,
            private loadingBarService: ccrdb.core.LoadingBarService) {
            this.loadProducts();

            backButtonService.enabled = false;
        }

        loadProducts() {
            this.loadingBarService.reqCount++;
            this.productsService.getProducts().then((products) => {
                this.loadingBarService.reqCount--;

                this.products = products.slice().reverse();
            });
        }

        bulkDelete(productId: string) {
            this.loadingBarService.reqCount++;
            this.productsService.deleteProduct(productId).then((success: boolean) => {
                this.loadingBarService.reqCount--;

                if (success) {
                    this.$state.reload();
                } else {
                    console.error('Failed to delete product.');
                }
            });
        }
    }

    angular
        .module('ccrdb.pages')
        .controller('ProductsController', ProductsController);
}