namespace ccrdb.core {
    'use strict';

    export class IndexController {
        static $inject = ['loadingBarService'];

        constructor(private loadingBarService: ccrdb.core.LoadingBarService) {
        }
    }

    angular
        .module('ccrdb.core')
        .controller('IndexController', IndexController);
}