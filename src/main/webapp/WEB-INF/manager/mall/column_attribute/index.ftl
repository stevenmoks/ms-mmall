<@ms.html5>
	<div id="toolbar">
		<@ms.panelNav>
			<@ms.buttonGroup>
				<@ms.addButton id="addColumnAttributeBtn"/>
				<@ms.delButton id="delColumnAttributeBtn"/>
			</@ms.buttonGroup>
		</@ms.panelNav>
	</div>
	<table id="columnAttributeList" 
		data-show-refresh="true"
		data-show-columns="true"
		data-show-export="true"
		data-method="post" 
		data-pagination="true"
		data-page-size="10"
		data-side-pagination="server">
	</table>
	<@ms.modal  modalName="delColumnAttribute" title="规格数据删除" >
		<@ms.modalBody>删除此规格
			<@ms.modalButton>
				<!--模态框按钮组-->
				<@ms.button  value="确认删除？"  id="deleteColumnAttributeBtn"  />
			</@ms.modalButton>
		</@ms.modalBody>
	</@ms.modal>
</@ms.html5>

<script>
	$(function(){
		var caCategoryId = {caCategoryId:${categoryId?default('0')}};
		$("#columnAttributeList").bootstrapTable({
			url:"${managerPath}/mall/columnAttribute/list.do",
			contentType : "application/x-www-form-urlencoded",
			queryParamsType : "undefined",
			queryParams:function(params) {
				return $.extend(params,caCategoryId);
			},
			toolbar: "#toolbar",
	    	columns: [{ checkbox: true},
				    	{
				        	field: 'caName',
				        	title: '规格名称',
				        	formatter:function(value,row,index) {
				        		var url = "${managerPath}/mall/columnAttribute/form.do?caId="+row.caId;
				        		return "<a href=" +url+ " target='_self'>" + value + "</a>";
				        	}
				    	},{
				        	field: 'caFields',
				        	title: '默认的字段',
				    	}]
	    })
	})
	//增加按钮
	$("#addColumnAttributeBtn").click(function(){
		location.href ="${managerPath}/mall/columnAttribute/form.do?caCategoryId="+${categoryId?default('')}; ; 
	})
	//删除按钮
	$("#delColumnAttributeBtn").click(function(){
		//获取checkbox选中的数据
		var rows = $("#columnAttributeList").bootstrapTable("getSelections");
		//没有选中checkbox
		if(rows.length <= 0){
			<@ms.notify msg="请选择需要删除的记录" type="warning"/>
		}else{
			$(".delColumnAttribute").modal();
		}
	})
	
	$("#deleteColumnAttributeBtn").click(function(){
		var rows = $("#columnAttributeList").bootstrapTable("getSelections");
		$(this).text("正在删除...");
		$(this).attr("disabled","true");
		$.ajax({
			type: "post",
			url: "${managerPath}/mall/columnAttribute/delete.do",
			data: JSON.stringify(rows),
			dataType: "json",
			contentType: "application/json",
			success:function(msg) {
				if(msg.result == true) {
					<@ms.notify msg= "删除成功" type= "success" />
				}else {
					<@ms.notify msg= "删除失败" type= "fail" />
				}
				location.reload();
			}
		})
	});
	//查询功能
	function search(){
		var search = $("form[name='searchForm']").serializeJSON();
        var params = $('#columnAttributeList').bootstrapTable('getOptions');
        params.queryParams = function(params) {  
        	$.extend(params,search);
	        return params;  
       	}  
   	 	$("#columnAttributeList").bootstrapTable('refresh', {query:$("form[name='searchForm']").serializeJSON()});
	}
</script>