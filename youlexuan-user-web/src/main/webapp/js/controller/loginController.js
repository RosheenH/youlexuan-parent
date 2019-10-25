 //用户表控制层 
app.controller('loginController' ,function($scope,$controller   ,userService){
	
	// $controller('baseController',{$scope:$scope});//继承

	//获取登录名
	$scope.getName = function () {
		userService.getName().success(
			function (response) {

				$scope.name = response;
            }
		)
    }

});	