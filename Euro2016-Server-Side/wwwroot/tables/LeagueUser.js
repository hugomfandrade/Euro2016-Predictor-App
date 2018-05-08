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
	console.log(context.req.query);
    //console.log("ReadLeagueUser::query::");
    
    var query = buildReadQuery(context.req.query);
	
    //console.log("ReadLeagueUser::query::" + query);
	//console.log("");
    
    return context.data.execute({sql: query});
}); 

function buildReadQuery(query) {
	
	var withQuery =
           'WITH '
		+ ' UltimateScore AS ('
		+ ' SELECT a.id, a.Email, ISNULL(SUM(ps.Score), 0) AS Score'
		+ ' FROM Account a'
		+ ' LEFT JOIN Prediction p ON a.id = p.UserID AND a.deleted = \'false\' AND p.deleted = \'false\''
		+ ' LEFT JOIN PredictionScore ps ON p.id = ps.PredictionID AND ps.deleted = \'false\''
		+ ' GROUP BY a.id, a.Email),'
		+ ' UltimateAmazingTable AS ('
		+ ' SELECT a.id, a.Email, a.Score'//, a.LeagueID'
		+ ' FROM (  SELECT a.id, a.Email, a.Score, lu.LeagueID'
		+ '         FROM UltimateScore a'
		+ '         LEFT JOIN LeagueUser lu ON a.id = lu.UserID AND lu.deleted = \'false\''
		+ '         LEFT JOIN League l ON lu.LeagueID = l.id AND lu.deleted = \'false\''
		+ '         ' + getWhereFilterLeagueID(query.LeagueID)
		+ ' ) a)';
	
	var select = '*';	
	if (query.$select !== undefined && query.$select )
	{
		select = query.$select;
	}
	var where = '';	
	if (query.$filter !== undefined && query.$filter )
	{
		where = BuildQuery.getWhere(query.$filter);
	}
	var mainQuery =
		  ' SELECT ' + select + ' FROM ('	
		+ ' 	SELECT s1.id, s1.Email, s1.Score, COUNT(DISTINCT s2.Score) AS Rank'
		+ ' 	FROM UltimateAmazingTable s1 '
		+ ' 	JOIN UltimateAmazingTable s2 ON s1.Score <= s2.Score'
		+ ' 	GROUP BY s1.id, s1.Email, s1.Score'
		+ ' ) a'
		+ ' ' + where
        + ' ORDER BY ' + BuildQuery.getProperty(query.$orderby, 'Rank')
		+ ' ' + BuildQuery.getOffset(query);

	
	return withQuery + ' ' + mainQuery;
}

function getWhereFilterLeagueID(whereProperty) {
	if (whereProperty !== undefined && whereProperty )
	{
		return ' WHERE LeagueID = \'' + whereProperty + '\'';
	}
	else {
		return '';
	}
}

function oldGetWhereFilterLeagueID(whereProperty, exclude) {
	var requestQuery = BuildQuery.getWhere(whereProperty);
	
	if (requestQuery === null) {
		return '';
	}
	else {
		if (exclude === false) {
			var query = includeLeagueIDOnly(requestQuery);
			if (query !== null) {
				return ' WHERE ' + query;
			}
			else {
				return '';
			}
		}
		else {
			var query =  excludeLeagueID(requestQuery);
			if (query !== null) {
				return ' WHERE ' + query;
			}
			else {
				return '';
			}
		}
	}
}

function includeLeagueIDOnly(wwhereClause) {
	console.log("includeLeagueID::" + wwhereClause);
	
	
	var filteredWhereClause = '';
	var currentWhereClause = wwhereClause;
	var i = currentWhereClause.indexOf("LeagueID = (\'");
	if (i === -1) {
		filteredWhereClause = filteredWhereClause + currentWhereClause;
	}
	while (i !== -1) {
		filteredWhereClause = filteredWhereClause + currentWhereClause.substring(0, i);
		//filteredWhereClause = filteredWhereClause + " = ";
		currentWhereClause = currentWhereClause.substring(i, currentWhereClause.length);
		//console.log("currentWhereClause::" + currentWhereClause);
		var itOpen = currentWhereClause.indexOf("(");
        var itClose = itOpen;
		var n = 0;
        var iDelta = 0;
        var size = 0;
		
		do {
			
			var nextItOpen = currentWhereClause.indexOf("(", iDelta + 1);
			itClose = currentWhereClause.indexOf("\')", iDelta + 1);
			
			if (nextItOpen === -1) {
				n = n - 1;
                iDelta = itClose;
			}
			else if (itClose < nextItOpen) {
				n = n - 1;
                iDelta = itClose;
			}
			else {
				n = n + 1;
                iDelta = nextItOpen;
			}
		}
        while (n !== 1);
        
        //filteredWhereClause = filteredWhereClause + currentWhereClause.substring(itOpen, itClose + 1);
		currentWhereClause = currentWhereClause.substring(itClose + 2, currentWhereClause.length);
        i = currentWhereClause.indexOf("LeagueID = (\'");
		
		if (i === -1) {
			filteredWhereClause = filteredWhereClause + currentWhereClause;
		}
	}
	if (filteredWhereClause === "()") {
		return null;
	}
	console.log("includeLeagueID::end::" + filteredWhereClause);
	return wwhereClause;
	//console.log("RemoveEQS");
	//console.log(filteredWhereClause);
	//return filteredWhereClause;
}

function excludeLeagueID(wwhereClause) {
	//console.log("excludeLeagueID::" + whereClause);
	//return whereClause;
	console.log("excludeLeagueID::" + wwhereClause);
	
	
	var filteredWhereClause = '(';
	var currentWhereClause = wwhereClause;
	var i = currentWhereClause.indexOf("LeagueID = (\'");
	if (i === -1) {
		filteredWhereClause = filteredWhereClause + currentWhereClause;
	}
	while (i !== -1) {
		filteredWhereClause = filteredWhereClause + currentWhereClause.substring(0, i);
		//filteredWhereClause = filteredWhereClause + " = ";
		currentWhereClause = currentWhereClause.substring(i, currentWhereClause.length);
		console.log("currentWhereClause::" + currentWhereClause);
		var itOpen = currentWhereClause.indexOf("(");
        var itClose = itOpen;
		var n = 0;
        var iDelta = 0;
        var size = 0;
		
		do {
			
			var nextItOpen = currentWhereClause.indexOf("(", iDelta + 1);
			itClose = currentWhereClause.indexOf("\')", iDelta + 1);
			
			if (nextItOpen === -1) {
				n = n - 1;
                iDelta = itClose;
			}
			else if (itClose < nextItOpen) {
				n = n - 1;
                iDelta = itClose;
			}
			else {
				n = n + 1;
                iDelta = nextItOpen;
			}
		}
        while (n !== 1);
        
        //filteredWhereClause = filteredWhereClause + currentWhereClause.substring(itOpen, itClose + 1);
		currentWhereClause = currentWhereClause.substring(itClose + 1, currentWhereClause.length);
        i = currentWhereClause.indexOf("LeagueID = (\'");
		
		if (i === -1) {
			filteredWhereClause = filteredWhereClause + currentWhereClause;
		}
	}
	
	filteredWhereClause = filteredWhereClause + ')';
	if (filteredWhereClause === "()") {
		return null;
	}
	console.log("excludeLeagueID::end::" + filteredWhereClause);
	return wwhereClause;
	//console.log("RemoveEQS");
	//console.log(filteredWhereClause);
	//return filteredWhereClause;
}
