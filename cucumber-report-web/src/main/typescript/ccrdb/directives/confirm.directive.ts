namespace ccrdb.directives {
    'use strict';


    export function confirmation(): ng.IDirective {
        return {
            restrict: 'A',
            link: (scope: ng.IScope, element: ng.IAugmentedJQuery, attrs: any) => {
                element.bind('click', function () {
                    let message = attrs.ngReallyMessage;
                    if (message && confirm(message)) {
                        scope.$apply(attrs.confirmation);
                    }
                });
            }
        };
    }

    angular
        .module('ccrdb.directives')
        .directive('confirmation', confirmation);
}