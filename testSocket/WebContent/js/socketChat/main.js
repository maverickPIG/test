require.config({
	baseUrl:"../../js",
	paths:{
		jquery:"lib/jquery",
		chat:"socketChat/chat"
	}
});
require(["chat"],function(chat){})
