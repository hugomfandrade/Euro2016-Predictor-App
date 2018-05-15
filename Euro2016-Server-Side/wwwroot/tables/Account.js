
var ValidateToken = require('../authentication_scripts/ValidateToken');
var BuildQuery = require("../utils_scripts/BuildQuery");

var table = module.exports = require('azure-mobile-apps').table();

table.read.use(ValidateToken, table.operation);
table.read(function (context) {
    
    var query = buildReadQuery(context.req.query);
	console.log(query);
    
    return context.data.execute({sql: query});
});

/*function buildReadQuery(query) {
    
	var fromQuery =
           'SELECT a.id, a.Email, SUM(ps.Score) AS Score'
         + ' FROM Account a'
         + ' LEFT JOIN Prediction p ON a.id = p.UserID'
         + ' LEFT JOIN PredictionScore ps ON p.id = ps.PredictionID'
         + ' GROUP BY a.id, a.Email';
      
	return BuildQuery.BuildQuery(query, fromQuery, null);
}/**/

function buildReadQuery(query) {
	
	var minMatchNumber = '1';
	var maxMatchNumber = '52';
	if (query.MinMatchNumber !== undefined && query.MinMatchNumber )
	{
		minMatchNumber = query.MinMatchNumber;
	}
	if (query.MaxMatchNumber !== undefined && query.MaxMatchNumber )
	{
		maxMatchNumber = query.MaxMatchNumber;
	}
	
	
	var withQuery =
           'WITH '
		+ ' UltimateScore AS ('
		+ ' SELECT a.id, a.Email, ISNULL(SUM(CASE WHEN p.MatchNumber >= ' + minMatchNumber + ' AND p.MatchNumber <= ' + maxMatchNumber + ' THEN ps.Score END), 0) AS Score'
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
		where = ' WHERE ' + BuildQuery.getWhere(query.$filter);
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
