namespace ccrdb.core {

    'use strict';

    export class AuthenticationService {
        public isAdmin: boolean;

        static $inject = ['$http'];

        constructor(private $http: ng.IHttpService) {
            this.$http.get('rest/roles/current').success((roles: string[]) => {
                this.isAdmin = roles.indexOf('ROLE_ADMIN') >= 0;
            });
        }

        private success: (response: any) => {} = (response) => response.data;
    }

    angular.module('ccrdb.core')
        .service('authenticationService', ccrdb.core.AuthenticationService);
}