var ValidateToken = require('../authentication_scripts/ValidateToken');
var queries = require('azure-mobile-apps/src/query');

module.exports = {
    //"get": function (req, res, next) {
    //}
    
    "post": [ValidateToken, function (req, res, next) {
        var userID = req.azureMobile.user.id;
		var item = req.body;
		var azureMobile = req.azureMobile;
        
        if (userID !== item.UserID) {
            return res.status(400).send('You do not have permission to leave League with such UserID');
        }
		
		return doesLeagueExist(azureMobile, item).then(function (league) {
            
            var leagueUsers = azureMobile.tables('LeagueUser');
	        return leagueUsers.delete({UserID: item.UserID, LeagueID: league.id }).then(function(leagueUser) {
                
                return res.status(200).send();
		    });
            
        }).catch(function(error) {
            return res.status(400).send(error.message);
		});
    }]
}

function doesLeagueExist(azureMobile, item) {
    
    var query = 'SELECT * FROM League l'
                + ' WHERE l.id = \'' + item.id + '\''
                + ' AND l.deleted = \'false\'';
    
    return azureMobile.data.execute({sql: query}).then(function (leagues) { 
        if (leagues.length !== 0) {
            var league = leagues[0];
            
            if (league.AdminID === item.UserID) {
                throw new Error("Cannot leave this League: you are admin.");
            } 
            else {
                return league;
            }
        }
        else {
            throw new Error("League does not exist.");
        }
    });
};
