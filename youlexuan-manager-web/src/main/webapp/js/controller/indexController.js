app.controller("indexController",function ($scope,loginService) {


    //读取当前登录人名称
    $scope.loginName = function () {
        loginService.loginName().success(
            function (response) {
                $scope.loginName = response.loginName;
                $scope.lastTime = response.lastTime;
            }
        )
    }

})