namespace ccrdb.core {

    'use strict';

    export class DeletionModeService {
        public enabled = false;
    }

    angular.module('ccrdb.core')
        .service('deletionModeService', DeletionModeService);
}