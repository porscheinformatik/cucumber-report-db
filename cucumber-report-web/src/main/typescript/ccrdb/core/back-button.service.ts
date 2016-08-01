namespace ccrdb.core {

    'use strict';

    export class BackButtonService {
        public enabled: boolean = false;
        public prevState: string = 'products';
        public prevParams: any = {};
    }

    angular.module('ccrdb.core')
        .service('backButtonService', BackButtonService);
}