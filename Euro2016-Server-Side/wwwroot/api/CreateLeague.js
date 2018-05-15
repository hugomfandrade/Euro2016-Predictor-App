var Q = require("q");
var ValidateToken = require('../authentication_scripts/ValidateToken');

var MAX_NUMBER_OF_LEAGUES = 10;
var CODE_LENGTH = 8;

module.exports = {
    //"get": function (req, res, next) {
    //}
    
    "post": [ValidateToken, function (req, res, next) {
        var userID = req.azureMobile.user.id;
		var item = req.body;
		var azureMobile = req.azureMobile;
        
        
        if (userID !== item.AdminID) {
            return res.status(400).send('You do not have permission to create League with such AdminID');
        }
		
		//return doesLeagueExist(azureMobile, item).then(function () {
            
            return isInLessThanMaxNumberOfLeagues(azureMobile, item).then(function () {
            
                return generateUniqueCode(azureMobile).then(function(code) {
                    
                    var leagues = azureMobile.tables('League');
    				return leagues.insert({AdminID: item.AdminID, 
                                           Name: item.Name, 
                                           Code:code }).then(function(leagueResult) {
                        
                        var leagueUsers = azureMobile.tables('LeagueUser');
        				return leagueUsers.insert({UserID: leagueResult.AdminID, 
                                                   LeagueID: leagueResult.id }).then(function(leagueUserResult) {
                            
                            return readLeague(azureMobile, leagueResult.id).then(function(league) {
                                
                                return res.status(200).send(league);
                            });
        			    });
    			    });
                });
            //});
        }).catch(function(error) {
            return res.status(400).send(error.message);
		});
    }]
}

/*function doesLeagueExist(azureMobile, item) {
    
    var query = 'SELECT * FROM League l'
                + ' WHERE l.Name = \'' + item.Name + '\''
                + ' AND l.deleted = \'false\'';
    
    return azureMobile.data.execute({sql: query}).then(function (leagues) { 
        if (leagues.length === 0) {
            return;
        }
        else {
            throw new Error("League with name \'" + item.Name + "\' already exists.");
        }
    });
};/**/

function isInLessThanMaxNumberOfLeagues(azureMobile, item) {
    
    var query = 'SELECT * FROM League l'
                + ' INNER JOIN LeagueUser lu ON l.id = lu.LeagueID'
                + ' WHERE lu.UserID = \'' + item.AdminID + '\''
                + ' AND l.deleted = \'false\''
                + ' AND lu.deleted = \'false\'';
    
    return azureMobile.data.execute({sql: query}).then(function (leagues) { 
        if (leagues.length < 10) {
            return;
        }
        else {
            throw new Error("You are a member in 10 different leagues.");
        }
    });
};

function generateUniqueCode(azureMobile) {
    
	var defer = Q.defer();
    
    internalGenerateUniqueCode(azureMobile, defer);
    
	return defer.promise;
};

function internalGenerateUniqueCode(azureMobile, defer) {
    
    var code = generateCode(CODE_LENGTH);
    
    var query = 'SELECT * FROM League l'
                + ' WHERE l.Code = \'' + code + '\''
                + ' AND l.deleted = \'false\'';
    
    azureMobile.data.execute({sql: query}).then(function (leagues) { 
        if (leagues.length === 0) {
            defer.resolve(code);
        }
        else {
            internalGenerateUniqueCode(azureMobile, defer);
        }
	});
};

function generateCode(count) {
    
    var _sym = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890';
    var str = '';

    for(var i = 0; i < count; i++) {
        str += _sym[parseInt(Math.random() * (_sym.length))];
    }
    
    return str;
}

function readLeague(azureMobile, leagueID) {
    
	
	var query =
           'SELECT DISTINCT l.id, l.Name, l.AdminID, l.Code, t.NumberOfMembers'
		+ ' FROM League l'
		+ ' INNER JOIN (SELECT LeagueID, COUNT(*) AS NumberOfMembers FROM LeagueUser WHERE LeagueUser.deleted = \'false\' GROUP BY LeagueID) AS t'
		+ ' ON l.id = t.LeagueID AND l.deleted = \'false\''
		+ ' INNER JOIN LeagueUser lu ON l.id = lu.LeagueID AND lu.deleted = \'false\''
        + ' WHERE l.id = \'' + leagueID + '\''
        + ' GROUP BY l.id, l.Name, l.AdminID, l.Code, t.NumberOfMembers';
    
    return azureMobile.data.execute({sql: query}).then(function(leagues) {
        if (leagues.length === 1) {
            return leagues[0];
        }
        else {
            throw new Error("League not found.");
        }
    });
}; 