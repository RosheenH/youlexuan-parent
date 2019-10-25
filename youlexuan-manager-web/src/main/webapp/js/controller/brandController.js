app.controller("brandController", function($scope,$controller,brandService) {
    $controller('baseController',{$scope:$scope});//继承
    //普通显示
    $scope.findAll = function(){
       brandService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        )
    }

    $scope.findPage = function (page,rows) {
       brandService.findPage(page,rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;
            }
        )
    }
    //分页加模块查询显示数据
    $scope.searchEntity={};//定义搜索对象

    //搜索
    $scope.search=function(page,rows){
        brandService.search(page,rows,$scope.searchEntity).success(
            function(response){
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }

    $scope.save = function () {

        brandService.save($scope.entity).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();
                }else{
                    alert(response.message)
                }
            }
        )
    }

    $scope.findOne = function (id) {
       brandService.findOne(id).success(
            function (response) {
                $scope.entity = response;

            }
        )
    }


    $scope.del = function () {
       brandService.del($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();
                }else{
                    alert(response.message)
                }
            }
        )
    }

});