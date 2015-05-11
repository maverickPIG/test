define(["jquery"],function(_$){
	var domin=window.location.host;
	console.log(domin);
	
	var user="";
	var tuser="";
	
	$(".userlist li").on("click",function(){
		alert("")
		$(this).css({"background-color":"gray"})
		tuser=$(this).text();
	})
	
	var w;
	if(window.WebSocket){
		w=new WebSocket(encodeURI('ws://'+domin+"/chatServlet"));
		w.onmessage=function(message){
			console.log(message);
			var data= eval("("+message.data+")");
			console.log(typeof data);
			if(data["cuser"]!=undefined){
				user=data["cuser"];
				console.log(data["cuser"]);
				return;
			}
			
			console.log(data['type'])
			if(data['type']=="user_list"){
				for(var i in data.msg){
					console.log(data.msg[i]);
					$(".userlist").append($("<li></li>").attr("user",data.msg[i]).text(data.msg[i]));
				}
			}
			if(data.type=="user_join"){
					console.log(data.user);
					$(".userlist").append($("<li></li>").attr("user",data.user).text(data.user));
			}
			if(data.type=="user_leave"){
					console.log(data.user);
					$(".userlist li[user='"+data.user+"']").remove();
			}
		}
	}
	
	
	$("#sendBtn").click(function(){
		var inputTxt=$("#input").val();
		if(inputTxt!=""){
			creatMsg("left","jeck",inputTxt);
		}
		$("#input").val("");
		w.send("type=single;tuser="+tuser+";msg="+inputTxt);
	});


	//创建信息框
	function creatMsg(type,user,msg){
		var d=new Date();
		var $msgDiv=$("<div></div>").addClass(type);
		var $timeSpan=$("<span></span>").addClass("time").text(d.getHours()+":"+d.getMinutes()+":"+d.getSeconds()).appendTo($msgDiv);
		var $div=$("<div></div>");
		var $user=$("<div></div>").addClass(type+"User").text(user).appendTo($div);
		var $msg=$("<div></div>").addClass(type+"Msg").text(msg).appendTo($div);
		$div.appendTo($msgDiv);
		$msgDiv.appendTo("#recive");
	}
})