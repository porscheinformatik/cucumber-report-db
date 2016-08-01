namespace ccrdb.core {

    'use strict';

    export class LoadingBarService {
        public reqCount = 0;
    }


    angular.module('ccrdb.core')
        .service('loadingBarService', LoadingBarService);
}