var ValidateToken = require('../authentication_scripts/ValidateToken');
var AuthorizationAdmin = require('../authentication_scripts/AuthorizationAdmin');
var GetSystemData = require('../utils_scripts/GetSystemData');

module.exports = {
    "get": function (req, res, next) {
		var azureMobile = req.azureMobile;
        
		GetSystemData(azureMobile).then(function(systemData) {
			return res.status(200).send(systemData);
		}).catch(function(error) {
			return res.status(400).send(error);
		});
    },
	
    "post": [ValidateToken, AuthorizationAdmin, function (req, res, next) {
		var item = req.body;
		var azureMobile = req.azureMobile;
		
	    var systemData = azureMobile.tables('SystemData');
		
		systemData.read().then(function(results) {
	        if (results.length === 0) {
				systemData.insert(item).then(function(result) {
					
					var diff = Math.abs(new Date() - result.DateOfChange);
					var systemDate = new Date(new Date().setTime(result.SystemDate.getTime() + diff));
					result.SystemDate = systemDate;
					delete result.DateOfChange;
					
					return res.status(200).send(result);
			    }).catch(function(error) {
	                return res.status(400).send(error);
				});
			}
			else {
				var i = results[0];
				i.Rules = item.Rules;
				i.AppState = item.AppState;
				i.SystemDate = item.SystemDate;
				i.DateOfChange = new Date();
				systemData.update(i).then(function(result) {
					
					var diff = Math.abs(new Date() - result.DateOfChange);
					var systemDate = new Date(new Date().setTime(result.SystemDate.getTime() + diff));
					result.SystemDate = systemDate;
					delete result.DateOfChange;
					
					return res.status(200).send(result);
			    }).catch(function(error) {
	                return res.status(400).send(error);
				});
			}
	    });
    }]
}
