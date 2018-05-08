

function _BuildQuery(requestQuery, fromQuery, extraWhere) {
	//console.log("REQUESTQUERY");
	//console.log(requestQuery);
	
	var query = 'SELECT ' + _getProperty(requestQuery.$select, '*')
			+ ' FROM (' + fromQuery + ') m'
			+ ' ' + _getFullWhere(requestQuery.$filter, extraWhere)
			+ ' ORDER BY ' + _getProperty(requestQuery.$orderby, 'id')
			+ ' ' + _getOffset(requestQuery);
	
	return query;
}

function _getFullWhere(whereProperty, extraWhere) {
	var requestWhere  = _getWhere(whereProperty);
	var filteredWhere = _buildWhereClause(extraWhere);
	
	//console.log("FULLWHERE (0)");
	//console.log(whereProperty);
	//console.log("FULLWHERE (1)");
	//console.log(requestWhere);
	
	if (requestWhere === null && filteredWhere === null)
	{
	  return '';
	}
	
	var query = ' WHERE ';
	if (requestWhere !== null)
	{
		query = query + requestWhere;
	}
	if (filteredWhere !== null)
	{
		if (requestWhere !== null)
		{
		  query = query + ' AND ';
		}
		query = query + ' ' + filteredWhere;
	}
	return query;
}

function _buildWhereClause(extraWhere) {
	
	if (extraWhere === undefined || extraWhere === null || 
		extraWhere.column === undefined || extraWhere.column === null ||
		extraWhere.arr === undefined || extraWhere.arr === null  || 
		extraWhere.arr.length === 0) {
		return null;
	}
	
    var whereClause = '';
    extraWhere.arr.forEach(function(item) {
        if (whereClause !== '')
            whereClause = whereClause + ' OR ';
        whereClause = whereClause + extraWhere.column + ' = \'' + item + '\''
    });
    return whereClause;
}

function _getWhere(whereProperty) {
	if (typeof whereProperty !== undefined && whereProperty )
	{
		return '(' + _removeGes(_removeLes(_removeEqs(whereProperty))) + ')';
	}
	return null;
}

function _removeEqs(originalWhereClause) {
	//console.log("RemoveEQS init");
	//console.log(originalWhereClause);
	
	var filteredWhereClause = '';
	var whereClause = originalWhereClause;
	var i = whereClause.indexOf(" eq (");
	if (i === -1) {
		filteredWhereClause = filteredWhereClause + whereClause;
	}
	while (i !== -1) {
		filteredWhereClause = filteredWhereClause + whereClause.substring(0, i);
		filteredWhereClause = filteredWhereClause + " = ";
		whereClause = whereClause.substring(i, whereClause.length);
		
		var itOpen = whereClause.indexOf("(");
        var itClose = itOpen;
		var n = 0;
        var iDelta = 0;
        var size = 0;
		
		do {
			
			var nextItOpen = whereClause.indexOf("(", iDelta + 1);
			itClose = whereClause.indexOf(")", iDelta + 1);
			
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
        while (n !== 0);
        
        filteredWhereClause = filteredWhereClause + whereClause.substring(itOpen, itClose + 1);
		whereClause = whereClause.substring(itClose + 1, whereClause.length);
        i = whereClause.indexOf(" eq (");
		
		if (i === -1) {
			filteredWhereClause = filteredWhereClause + whereClause;
		}
	}
	//console.log("RemoveEQS");
	//console.log(filteredWhereClause);
	return filteredWhereClause;
}

function _removeGes(originalWhereClause) {
	//console.log("RemoveGES");
	//console.log(originalWhereClause);
	
	var filteredWhereClause = '';
	var whereClause = originalWhereClause;
	var i = whereClause.indexOf(" ge (");
	if (i === -1) {
		filteredWhereClause = filteredWhereClause + whereClause;
	}
	while (i !== -1) {
		filteredWhereClause = filteredWhereClause + whereClause.substring(0, i);
		filteredWhereClause = filteredWhereClause + " >= ";
		whereClause = whereClause.substring(i, whereClause.length);
		
		var itOpen = whereClause.indexOf("(");
        var itClose = itOpen;
		var n = 0;
        var iDelta = 0;
        var size = 0;
		
		do {
			
			var nextItOpen = whereClause.indexOf("(", iDelta + 1);
			itClose = whereClause.indexOf(")", iDelta + 1);
			
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
        while (n !== 0);
        
        filteredWhereClause = filteredWhereClause + whereClause.substring(itOpen, itClose + 1);
		whereClause = whereClause.substring(itClose + 1, whereClause.length);
        i = whereClause.indexOf(" ge (");
		
		if (i === -1) {
			filteredWhereClause = filteredWhereClause + whereClause;
		}
	}
	//console.log("RemoveGES");
	//console.log(filteredWhereClause);
	return filteredWhereClause;
}

function _removeLes(originalWhereClause) {
	//console.log("RemoveLES");
	//console.log(originalWhereClause);
	var filteredWhereClause = '';
	var whereClause = originalWhereClause;
	var i = whereClause.indexOf(" le (");
	if (i === -1) {
		filteredWhereClause = filteredWhereClause + whereClause;
	}
	while (i !== -1) {
		filteredWhereClause = filteredWhereClause + whereClause.substring(0, i);
		filteredWhereClause = filteredWhereClause + " <= ";
		whereClause = whereClause.substring(i, whereClause.length);
		
		var itOpen = whereClause.indexOf("(");
        var itClose = itOpen;
		var n = 0;
        var iDelta = 0;
        var size = 0;
		
		do {
			
			var nextItOpen = whereClause.indexOf("(", iDelta + 1);
			itClose = whereClause.indexOf(")", iDelta + 1);
			
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
        while (n !== 0);
        
        filteredWhereClause = filteredWhereClause + whereClause.substring(itOpen, itClose + 1);
		whereClause = whereClause.substring(itClose + 1, whereClause.length);
        i = whereClause.indexOf(" le (");
		
		if (i === -1) {
			filteredWhereClause = filteredWhereClause + whereClause;
		}
	}
	//console.log("RemoveLEs");
	//console.log(filteredWhereClause);
	return filteredWhereClause;
}

function _getOffset(requestQuery) {
	var offset = ' OFFSET ' + _getProperty(requestQuery.$skip, 0) + ' ROWS';
	
	if (typeof requestQuery.$top !== undefined && requestQuery.$top )
	{
	  offset = offset + ' FETCH NEXT ' + requestQuery.$top + ' ROWS ONLY' 
	}
	return offset;
}

function _getProperty(property, defaultValue) {
	if (typeof property !== undefined && property )
	{
		return property;
	}
	return defaultValue;
}

module.exports =  {
    getProperty : _getProperty,
    getOffset : _getOffset,
    removeLes : _removeLes,
    removeGes : _removeGes,
    removeEqs : _removeEqs,
    getWhere : _getWhere,
    buildWhereClause : _buildWhereClause,
    getFullWhere : _getFullWhere,
    BuildQuery : _BuildQuery
}
