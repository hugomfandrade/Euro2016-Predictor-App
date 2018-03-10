var ValidateToken = require('../authentication_scripts/ValidateToken');
var AuthorizationAdmin = require('../authentication_scripts/AuthorizationAdmin');
var UpdateScoresOfPredictionsOfAllMatches = require('../processing_scripts/UpdateScoresOfPredictionsOfAllMatches');

module.exports = {
    "post": [ValidateToken, AuthorizationAdmin, function (req, res, next) {
        
        UpdateScoresOfPredictionsOfAllMatches(req.azureMobile).then(function(results) {
            console.log("--finish--");
            return res.status(200).send();
        }).catch(function(error) {
            console.log("--finish error --");
            return res.status(400).send(error);
    	});
    }]
}
