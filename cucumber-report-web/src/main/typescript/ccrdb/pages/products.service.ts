namespace ccrdb.pages {

    'use strict';

    export interface IProductsService {
        getProducts: () => ng.IPromise<String[]>;
        deleteProduct: (productId: string) => ng.IPromise<boolean>;
    }

    export class ProductsService implements IProductsService {
        static $inject = ['$http'];

        constructor(private $http: ng.IHttpService) {
        }

        getProducts() {
            return this.$http.get('rest/collection/products')
                .then(this.success);
        }

        deleteProduct(productId: string) {
            return this.$http.delete('rest/query/' + productId + '/_ALL').success(() => {
                return true;
            }).error(() => {
                return false;
            });
        }

        private success: (response: any) => {} = (response) => response.data;
    }

    angular.module('ccrdb.pages')
        .service('productsService', ccrdb.pages.ProductsService);
}