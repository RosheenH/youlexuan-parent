 //控制层 
app.controller('seckillGoodsController' ,function($scope,$controller,$location,$interval  ,seckillGoodsService){
	

	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		seckillGoodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		seckillGoodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		seckillGoodsService.findOne($location.search()['id']).success(
			function(response){
				$scope.entity= response;
				//倒计时开始
				//获取从结束时间到当前日期的秒数
				allsecond = Math.floor((new Date($scope.entity.endTime).getTime()-new Date().getTime())/100);
				time = $interval(function () {
					allsecond=allsecond-1;
					$scope.timeString=convertTimeString(allsecond);
					if (allsecond<=0){
						$interval.cancel(time);
					}
					
                },1000);
			}
		);				
	}
	//转换秒为天小时分钟秒格式
    convertTimeString = function(allsecond){
		var days = Math.floor(allsecond/(60*60*24));//天数
		var hours = Math.floor((allsecond-days*60*60*24)/(60*60));//小时
		var minutes = Math.floor((allsecond-days*60*60*24-hours*60*60)/60);//分钟
		var seconds = allsecond-days*60*60*24-hours*60*60-minutes*60;//秒数
		var timeString = "";
		if (days>0){
			timeString = days+"天";
		}
		return timeString+hours+":"+minutes+":"+seconds;
	}

	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=seckillGoodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=seckillGoodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		seckillGoodsService.dele( $scope.selectIds ).success(
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
		seckillGoodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//秒杀商品
	$scope.findList = function () {
		seckillGoodsService.findList().success(
			function (response) {
				$scope.list = response;
            }
		)
		
    }

    //提交订单
	$scope.submitOrder = function () {

		seckillGoodsService.submitOrder($scope.entity.id).success(
			function (response) {
				if (response.success){
					alert("下单成功，请在五分钟内完成支付");
					location.href="pay.html";
				}else {
					alert(response.message);
				}
            }
		)
    }



    
});	