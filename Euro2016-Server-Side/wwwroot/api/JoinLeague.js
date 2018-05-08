var ValidateToken = require('../authentication_scripts/ValidateToken');

var MAX_NUMBER_OF_LEAGUES = 10;

module.exports = {
    //"get": function (req, res, next) {
    //}
    
    "post": [ValidateToken, function (req, res, next) {
        var userID = req.azureMobile.user.id;
		var item = req.body;
		var azureMobile = req.azureMobile;
        
        if (userID !== item.UserID) {
            return res.status(400).send('You do not have permission to join League with such UserID');
        }
		
		return doesLeagueExist(azureMobile, item).then(function (league) {
            
            return isAlreadyMemberOfThisLeague(azureMobile, item, league).then(function() {
                
                return isInLessThanMaxNumberOfLeagues(azureMobile, item).then(function () {
                    
                    var leagueUsers = azureMobile.tables('LeagueUser');
    				return leagueUsers.insert({UserID: item.UserID, LeagueID: league.id }).then(function(leagueUserResult) {
                        
                        return readLeague(azureMobile, league.id).then(function(league) {
                            
                            return res.status(200).send(league);
                        });
                    });
                });
            });
        }).catch(function(error) {
            return res.status(400).send(error.message);
		});
    }]
}

function doesLeagueExist(azureMobile, item) {
    
    var query = 'SELECT * FROM League l'
                + ' WHERE l.Code = \'' + item.Code + '\''
                + ' AND l.deleted = \'false\'';
    
    return azureMobile.data.execute({sql: query}).then(function (leagues) { 
        if (leagues.length !== 0) {
            return leagues[0];
        }
        else {
            throw new Error("League with code \'" + item.Code + "\' does not exists.");
        }
    });
};

function isAlreadyMemberOfThisLeague(azureMobile, item, league) {
    
    var query = 'SELECT * FROM LeagueUser l'
                + ' WHERE l.LeagueID = \'' + league.id + '\''
                + ' AND l.UserID = \'' + item.UserID + '\''
                + ' AND deleted = \'false\'';
    
    return azureMobile.data.execute({sql: query}).then(function (leagueUsers) { 
        if (leagueUsers.length === 0) {
            return;
        }
        else {
            throw new Error("You are already member of this League");
        }
    });
};

function isInLessThanMaxNumberOfLeagues(azureMobile, item) {
    
    var query = 'SELECT * FROM League l'
                + ' INNER JOIN LeagueUser lu ON l.id = lu.LeagueID'
                + ' WHERE lu.UserID = \'' + item.UserID + '\''
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
