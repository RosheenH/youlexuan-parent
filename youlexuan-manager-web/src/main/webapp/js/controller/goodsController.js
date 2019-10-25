 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location   ,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
        var id = $location.search()['id'];
        if(id==null){
            return;
        }
        goodsService.findOne(id).success(
            function(response){
                $scope.entity= response;
                //回显商品描述
                editor.html($scope.entity.goodsDesc.introduction);
                //回显图片
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                //回显扩展属性
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //回显规格参数
                $scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
                //回显sku中的spce
                for(var i=0;i<$scope.entity.itemList.length;i++){
                    $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec)
                }
            }
        );
    }
	
	//保存 
	$scope.save=function(){
        //提取文本编辑器的值
        $scope.entity.goodsDesc.introduction=editor.html();
        var serviceObject;//服务层对象
        if($scope.entity.goods.id!=null){//如果有ID
            serviceObject=goodsService.update( $scope.entity ); //修改
        }else{
            serviceObject=goodsService.add( $scope.entity  );//增加
        }
        serviceObject.success(
            function(response){
                if(response.success){
                    alert('保存成功');
                    $scope.entity={};
                    editor.html("");
                }else{
                    alert(response.message);
                }
            }
        );
    }

    $scope.entity = {'goodsDesc':{'itemImages':[],'specificationItems':[]}};
    $scope.add = function(){
        $scope.entity.goodsDesc.introduction = editor.html();
        goodsService.add($scope.entity).success(
            function (response) {
                if(response.success){
                    alert("添加成功");
                    $scope.entity = {};
                    editor.html('');
                }else{
                    alert(response.messages);
                }
            }
        )
    }



    //批量删除
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
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
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	$scope.entity = {};
	$scope.add = function () {
		$scope.entity.goodsDesc.introduction = editor.html();
		goodsService.add($scope.entity).success(
			function (response) {
				if (response.success) {
					alert(response.message);
                    $scope.entity = {};
					editor.html('');//清空富文本编辑器
				}else{
					alert(response.message);
				}
            }
		)
    }

    $scope.uploadFile = function () {
		uploadService.uploadFile().success(
			function (response) {
				if (response.success) {
					$scope.image_entity.url = response.message;
				}else {
					alert(response.message);
				}
            }
		)
    }

    $scope.entity = {'goods':{},'goodsDesc':{'itemImages':[]}};
	//添加图片列表
	$scope.add_image_entity = function(){
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}

    $scope.remove_image_entity = function(index){
        $scope.entity.goodsDesc.itemImages.splice(index,1);
    }

    $scope.selectItemCat1List = function () {
		itemCatService.findParent(0).success(
			function (response) {
				$scope.itemCat1List = response;
            }
		)
    }

    $scope.$watch('entity.goods.category1Id',function (newValue, oldValue) {
		//根据一级选择的值，查询二级分类
		itemCatService.findParent(newValue).success(
			function (response) {
				$scope.itemCat2List = response;
            }
		)
    })

	$scope.$watch('entity.goods.category2Id',function (newValue, oldValue) {
		//根据二级选择的值，查询三级分类
		itemCatService.findParent(newValue).success(
			function (response) {
				$scope.itemCat3List = response;
            }
		)
    })


	$scope.$watch('entity.goods.category3Id',function (newValue, oldValue) {
		//根据三级选择的值，查询模板ID
		itemCatService.findOne(newValue).success(
			function (response) {
				$scope.entity.goods.typeTemplateId = response.typeId
;            }
		)
    })

	$scope.$watch('entity.goods.typeTemplateId',function (newValue, oldValue) {
		//根据模板选择的值，查询品牌
            typeTemplateService.findOne(newValue).success(
                function (response) {
                    $scope.typeTemplate = response;
                    //格式化json为对象
                    $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
                    //[{"text":"内存大小","value":"10M"},{"text":"颜色","value":"红色"}]-->[{"text":"内存大小",value=111},{"text":"颜色"}]
                    var id = $location.search()['id'];
                    if(id==null){
                        $scope.entity.goodsDesc.customAttributeItems =  JSON.parse($scope.typeTemplate.customAttributeItems);
                    }
                    //模板ID得到对应的规格和规格项
                    typeTemplateService.findSpecList(newValue).success(
                        function (response) {
                            $scope.specList = response;
                        }
                    )
                }
            )
        }
    )


	$scope.entity = {goodsDesc:{itemImages:[],specificationItems:[]}};
	$scope.updateSpecAttribute = function ($event, name, value) {
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);
		if (object != null){
			if ($event.target.checked){
				object.attributeValue.push(value);
			}else {
				//取消勾选
				object.attributeValue.splice(object.attributeValue.indexOf(value),1);//移除选项
				//如果选项都取消了，将此条记录移除
				if (object.attributeValue.length == 0){
					$scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				}
			}
		}else {
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}

    }

    //创建SKU列表
    $scope.createItemList=function(){
        $scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0' } ];//初始
        var items=  $scope.entity.goodsDesc.specificationItems;
        for(var i=0;i< items.length;i++){
            $scope.entity.itemList = addColumn( $scope.entity.itemList,items[i].attributeName,items[i].attributeValue );
        }
    }
	//添加列值
    addColumn=function(list,columnName,conlumnValues){
        var newList=[];//新的集合
        for(var i=0;i<list.length;i++){
            var oldRow= list[i];
            for(var j=0;j<conlumnValues.length;j++){
                var newRow= JSON.parse( JSON.stringify( oldRow )  );//深克隆
                newRow.spec[columnName]=conlumnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    }

    $scope.status=['待审核','审核通过','审核驳回','删除'];//商品状态

	$scope.itemCatList = [];//商品分类列表
	//加载商品分类列表
	$scope.findItemCatList = function () {
		itemCatService.findAll().success(
			function (response) {
				for (var i = 0; i < response.length; i++) {
					$scope.itemCatList[response[i].id] = response[i].name;
				}
            }
		)
    }

    $scope.checkAttributeValue=function(specName,optionName){
        var items = $scope.entity.goodsDesc.specificationItems
        var obj = $scope.searchObjectByKey(items,'attributeName',specName);
        if(obj==null){
            return false;
        }else {
            if(obj.attributeValue.indexOf(optionName)>=0) {
                return true;
            }else {
                return false;
            }
        }
    }

    $scope.updateStatus = function (status) {
        goodsService.updateStatus($scope.selectIds,status).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();
                }else {
                    alert(response.message);
                }
            }
        )
    }


    
});	