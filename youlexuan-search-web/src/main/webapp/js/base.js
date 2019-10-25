var app = angular.module("youlexuan",[]);
// 定义模块:
var app = angular.module("youlexuan",[]);
/*$sce服务写成过滤器*/
app.filter('trustHtml',['$sce',function($sce){
    return function(data){
        return $sce.trustAsHtml(data);
    }
}]);