var queries = require('azure-mobile-apps/src/query');

module.exports = function (req, res, next) {
    var userID = req.azureMobile.user.id;
    
    var adminDatas = req.azureMobile.tables('Admin');
	var queryAdmin = queries.create('Admin')
					   .where({ UserID : userID });
	adminDatas.read(queryAdmin).then(function(results) {
		// If at least one, then is Admin
		if (results.length !== 1) {
            return res.status(400)
                      .send('Not authorized');
		}
        
        return next();
    });
};