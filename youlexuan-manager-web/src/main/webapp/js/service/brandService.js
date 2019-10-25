app.service("brandService", function($http) {
    //普通显示
    this.findAll = function(){
        return $http.get("../brand/findAll.do")
    }

    this.findPage = function (page,rows) {
        return $http.get("../brand/findPage.do?page="+page+"&rows="+rows)
    }
    //分页加模块查询显示数据
    this.search = function (page,rows,searchEntity) {
        return $http.post("../brand/search.do?page="+page+"&rows="+rows,searchEntity)
    }
    this.save = function (entity) {
        var m = "save";
        if ($scope.entity.id != null){
            m = "update";
        }
        return $http.post("../brand/"+m+".do",entity)
    }

    this.findOne = function (id) {
        return $http.get("../brand/findOne.do?id="+id)
    }


    this.del = function (selectIds) {
        return $http.get("../brand/del.do?ids="+selectIds)
    }

    this.findBrand = function () {
        return $http.get("../brand/findBrand.do");
    }

});