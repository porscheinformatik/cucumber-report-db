namespace ccrdb.pages {

    'use strict';

    export interface IGroupModalInstanceService {
        getGroups: (productId: string) => ng.IPromise<String[]>;
        postGroup: (productId: string, groupName: string) => ng.IPromise<boolean>;
        deleteGroup: (productId: string, groupName: string) => ng.IPromise<boolean>;
    }

    export class GroupModalInstanceService implements IGroupModalInstanceService {
        static $inject = ['$http'];

        constructor(private $http: ng.IHttpService) {
        }

        getGroups(productId: string) {
            return this.$http.get('rest/rights/' + productId).then(this.success);
        }

        postGroup(productId: string, groupName: string) {
            return this.$http.post('rest/rights/' + productId, groupName).success(() => {
                return true;
            }).error(() => {
                return false;
            });
        }

        deleteGroup(productId: string, groupName: string) {
            return this.$http.delete('rest/rights/' + productId + '/' + groupName).success(() => {
                return true;
            }).error(() => {
                return false;
            });
        }

        private success: (response: any) => {} = (response) => response.data;
    }

    angular.module('ccrdb.pages')
        .service('groupModalInstanceService', ccrdb.pages.GroupModalInstanceService);
}