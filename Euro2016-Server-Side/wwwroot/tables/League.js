var ValidateToken = require('../authentication_scripts/ValidateToken');
var BuildQuery = require("../utils_scripts/BuildQuery");
var queries = require('azure-mobile-apps/src/query');

var table = module.exports = require('azure-mobile-apps').table();

// table.read(function (context) {
//     return context.execute();
// });

// table.read.use(customMiddleware, table.operation);

table.read.use(ValidateToken, table.operation);
table.read(function (context) {
    //console.log("ReadLeague::query::");
    
    var query = buildReadQuery(context.req.query);
	
    //console.log("ReadLeague::query::" + query);
	//console.log("");
    
    return context.data.execute({sql: query});
}); 

function buildReadQuery(query) {
	
	var mainQuery =
          'SELECT DISTINCT l.id, l.Name, l.AdminID, l.Code, t.NumberOfMembers'
		+ ' FROM League l'
		+ ' INNER JOIN (SELECT LeagueID AS lLeagueID, COUNT(*) AS NumberOfMembers FROM LeagueUser WHERE LeagueUser.deleted = \'false\' GROUP BY LeagueID) AS t'
		+ ' ON l.id = t.lLeagueID AND l.deleted = \'false\''
		//+ ' INNER JOIN (SELECT LeagueID, COUNT(*) AS NumberOfMembers FROM LeagueUser WHERE LeagueUser.deleted = \'false\' GROUP BY LeagueID) AS t'
		//+ ' ON l.id = t.LeagueID AND l.deleted = \'false\''
		+ ' INNER JOIN LeagueUser lu ON l.id = lu.LeagueID AND lu.deleted = \'false\''
		+ ' ' + BuildQuery.getFullWhere(query.$filter, null)
        + ' GROUP BY l.id, l.Name, l.AdminID, l.Code, t.NumberOfMembers'
		+ ' UNION'
		+ ' SELECT \'Overall_ID\' AS id, NULL AS Name, NULL AS AdminID, NULL AS Code, COUNT(*) AS NumberOfMembers'
		+ ' FROM Account a' 
		+ ' WHERE a.deleted = \'false\''
		+ ' ORDER BY ' + BuildQuery.getProperty(query.$orderby, 'id')
		+ ' ' + BuildQuery.getOffset(query);
    
	if (query.$select !== undefined && query.$select )
	{
		mainQuery = 'SELECT ' + query.$select + ' FROM (' + mainQuery + ') AS t';
	}
	
	return mainQuery;
	
	
	/*
	var mainQuery =
          'SELECT DISTINCT l.id, l.Name, l.AdminID, l.Code, t.NumberOfMembers, COUNT(lu.UserID) AS LOL'
		+ ' FROM League l'
		+ ' INNER JOIN (SELECT LeagueID, COUNT(*) AS NumberOfMembers FROM LeagueUser WHERE LeagueUser.deleted = \'false\' GROUP BY LeagueID) AS t'
		+ ' ON l.id = t.LeagueID AND l.deleted = \'false\''
		+ ' INNER JOIN LeagueUser lu ON l.id = lu.LeagueID AND lu.deleted = \'false\''
		+ ' ' + BuildQuery.getFullWhere(query.$filter, null)
        + ' GROUP BY l.id, l.Name, l.AdminID, l.Code, t.NumberOfMembers'
		+ ' ORDER BY ' + BuildQuery.getProperty(query.$orderby, 'id')
		+ ' ' + BuildQuery.getOffset(query);
    
	var select = 't.id, t.Name, t.AdminID, t.Code, t.NumberOfMembers';
	if (query.$select !== undefined && query.$select )
	{
		select = query.$select;
	}
	mainQuery = 'SELECT ' + select + ' FROM (' + mainQuery + ') AS t';
	
	return mainQuery;/**/
}