var queries = require('azure-mobile-apps/src/query');

module.exports = function (azureMobile, accountID, callback) {
	
	var adminDatas = azureMobile.tables('Admin');
	var queryAdmin = queries.create('Admin').where({ AccountID : accountID });
	
	adminDatas.read(queryAdmin).then(function(results) {
		return callback(results.length === 1);
	});
}