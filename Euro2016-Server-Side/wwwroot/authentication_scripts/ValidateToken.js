var queries = require('azure-mobile-apps/src/query');

module.exports = function (req, res, next) {
    if (req.azureMobile.user == null) {
        return res.status(400)
                  .send('You must be logged in to use this application');
    }
    // console.log("auth data validate");
    var userID = req.azureMobile.user.id;
    var token = req.azureMobile.user.token;
    
    var authDatas = req.azureMobile.tables('AuthData');
	var query = queries.create('AuthData')
					   .where({ UserID : userID,
                                Token : token});
    authDatas.read(query).then(function(results) {
		if (results.length == 0) {
            // console.log("auth data not found");
            return res.status(400)
                      .send('Error validating access token');
		} 
        if (results[0].ExpiryDate.getTime() < new Date().getTime()) {
            // console.log("auth data past expiration");
            return res.status(400)
                      .send('Error validating access token');
		} 
        /*console.log("success: "
             + results[0].ExpiryDate.getTime()
             + " , "
             +  new Date().getTime()
             ); /**/
        return next();
    });
};