 //商品类目控制层 
app.controller('itemCatController' ,function($scope,$controller   ,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){
		$scope.entity.parentId = $scope.parentId;
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{
			serviceObject=itemCatService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	//$scope.reloadList();//重新加载
					$scope.findParent($scope.parentId);
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		itemCatService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	$scope.findParent = function (parentId) {
		$scope.parentId = parentId;
		itemCatService.findParent(parentId).success(
			function (response) {
				$scope.list = response;
            }
		)
    }

    $scope.grade = 1;
    $scope.addGrade = function (grade) {
        $scope.grade = grade;
    }

    $scope.selectChildren = function (entity) {
		console.info($scope.grade);
        if ($scope.grade == 1) {
            $scope.item_grade2 = null;
            $scope.item_grade3 = null;
        }
        else if($scope.grade == 2){
			console.info(entity);
            $scope.item_grade2 = entity;
            $scope.item_grade3 = null;
        }
        else if($scope.grade == 3){

            $scope.item_grade3 = entity;
        }

        $scope.findParent(entity.id);


    }

    $scope.findTempList = function () {
		typeTemplateService.findAll().success(
			function (response) {
				console.info(response);
				$scope.tempList = response;
            }
		)
    }



    
});	