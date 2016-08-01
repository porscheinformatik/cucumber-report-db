namespace ccrdb.pages {
    'use strict';

    export class HelpController {


        static $inject = ['backButtonService'];

        constructor(private backButtonService: ccrdb.core.BackButtonService,
            private loadingBarService: ccrdb.core.LoadingBarService) {
            backButtonService.enabled = true;
            backButtonService.prevState = 'products';
            backButtonService.prevParams = {};
        }
    }

    angular
        .module('ccrdb.pages')
        .controller('HelpController', HelpController);
}