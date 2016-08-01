namespace ccrdb.pages {
    'use strict';

    export class GroupModalInstanceController {
        private groups: string[];
        private newGroup: any;


        static $inject = ['$uibModalInstance', 'groupModalInstanceService', 'productId', 'loadingBarService'];

        constructor(private $uibModalInstance: ng.ui.bootstrap.IModalServiceInstance,
            private groupModalInstanceService: ccrdb.pages.IGroupModalInstanceService,
            private productId: string,
            private loadingBarService: ccrdb.core.LoadingBarService) {
            this.loadGroups();
            this.newGroup = { name: '' };
        }

        loadGroups() {
            console.log(this);
            this.loadingBarService.reqCount++;
            this.groupModalInstanceService.getGroups(this.productId).then((groups: string[]) => {
                this.loadingBarService.reqCount--;

                this.groups = groups;
            });
        }

        addGroup() {
            this.loadingBarService.reqCount++;
            if (this.newGroup.name !== '' && this.groups.indexOf(this.newGroup.name) <= -1) {
                this.groupModalInstanceService.postGroup(this.productId, this.newGroup.name).then((success: boolean) => {
                    this.loadingBarService.reqCount--;

                    if (success) {
                        this.groups.push(this.newGroup.name);
                    } else {
                        console.error('Failed to add group.');
                    }
                });
            }
        }

        removeGroup(index: number) {
            this.loadingBarService.reqCount++;
            this.groupModalInstanceService.deleteGroup(this.productId, this.groups[index]).then((success: boolean) => {
                this.loadingBarService.reqCount--;

                if (success) {
                    this.groups.splice(index, 1);
                } else {
                    console.error('Failed to remove group.');
                }
            });
        }

        close() {
            this.$uibModalInstance.close();
        }
    }

    angular
        .module('ccrdb.pages')
        .controller('GroupModalInstanceController', GroupModalInstanceController);
}