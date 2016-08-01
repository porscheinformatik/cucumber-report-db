namespace ccrdb.core {

    'use strict';

    export class SearchBarService {
        public searchText = '';
    }

    angular.module('ccrdb.core')
        .service('searchBarService', SearchBarService);
}