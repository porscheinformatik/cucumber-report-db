namespace ccrdb.pages {
    'use strict';

    export class AssignmentsController {
        private products: String[];


        static $inject = ['$uibModal', 'productsService', 'backButtonService', 'searchBarService', 'loadingBarService'];

        constructor(private $uibModal: ng.ui.bootstrap.IModalService,
            private productsService: ccrdb.pages.IProductsService,
            private backButtonService: ccrdb.core.BackButtonService,
            private searchBar: ccrdb.core.SearchBarService,
            private loadingBarService: ccrdb.core.LoadingBarService) {
            this.loadProducts();

            backButtonService.enabled = true;
            backButtonService.prevState = 'products';
            backButtonService.prevParams = {};
        }

        loadProducts() {
            this.loadingBarService.reqCount++;
            this.productsService.getProducts().then((products) => {
                this.loadingBarService.reqCount--;
                this.products = products.slice().reverse();
            });
        }

        openModal(productId: string) {
            let modalInstance = this.$uibModal.open({
                templateUrl: 'GroupModalContent.html',
                controller: 'GroupModalInstanceController',
                controllerAs: 'gmiCtrl',
                resolve: {
                    productId: function () {
                        return productId;
                    }
                }
            });
        }
    }

    angular
        .module('ccrdb.pages')
        .controller('AssignmentsController', AssignmentsController);
}