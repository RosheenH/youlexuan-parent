app.controller("baseController", function($scope) {

    //分页显示数据
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            $scope.reloadList();//重新加载
        }
    };
    $scope.reloadList = function () {
        // $scope.findPage($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }



    //设置被选中的id
    $scope.selectIds=[];
    $scope.updateSelection = function ($event, id) {
        //如果复选框被选中，将他添加到数组中
        if ($event.target.checked) {
            $scope.selectIds.push(id);
        }else{
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx,1);
        }
    }

    //[{"id":5,"text":"OPPO"},{"id":7,"text":"中兴"}]
    $scope.jsonToString = function (jsonString, key) {
        var json = JSON.parse(jsonString);//将json字符串转换为json对象
        var value = "";
        for (var i=0;i<json.length;i++){
            var obj = json[i];
            var val = obj[key];

            if (i>0){
                value = value + ",";
            }
            value = value + val;
        }
        return value;
    }



});