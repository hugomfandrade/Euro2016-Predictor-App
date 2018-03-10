var Q = require("q");

module.exports = function (azureMobile) {
	
	var defer = Q.defer();
    
    var systemData = azureMobile.tables('SystemData');
	
	systemData.read().then(function(results) {
        if (results.length === 0) {
			var d = new Date();
			systemData.insert({
				Rules: "0,1,2,4", 
				AppState: true,
				SystemDate: d,
				DateOfChange: d
			}).then(function(result) {
				
				var diff = Math.abs(new Date() - result.DateOfChange);
				var systemDate = new Date(new Date().setTime(result.SystemDate.getTime() + diff));
                result.SystemDate = systemDate;
				delete result.DateOfChange;
					
                return defer.resolve(result);
		    }).catch(function(error) {
        		return defer.reject(error);
			});
			return;
		}
		var result = results[0];
		
		// This will give you the difference between two dates, in milliseconds
		var diff = Math.abs(new Date() - result.DateOfChange);
		var systemDate = new Date(new Date().setTime(result.SystemDate.getTime() + diff));
		result.SystemDate = systemDate;
		delete result.DateOfChange;
			
        return defer.resolve(result);
    }).catch(function(error) {
        return defer.reject(error);
	});
    
	return defer.promise;
};