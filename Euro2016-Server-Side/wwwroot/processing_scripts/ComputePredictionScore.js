
module.exports = function (match, prediction, systemData) {
    
	var arrayOfRules = systemData.Rules.split(",");
	
	var incorrectPrediction = arrayOfRules[0];
	var correctOutcome = arrayOfRules[1];
	var correctMarginOfVictory = arrayOfRules[2];
	var correctPrediction = arrayOfRules[3];
    
    if (isMatchPlayed(match) === false) {
        return null;
    }
    if (isPredictionSet(prediction) === false) {
        return incorrectPrediction;
    }

    // Both (match and prediction) home teams win
    if ((didHomeTeamWin(match) && didPredictHomeTeamWin(prediction)) ||
        (didAwayTeamWin(match) && didPredictAwayTeamWin(prediction))) {
        if (isPredictionCorrect(match, prediction))
            return correctPrediction;
        else if (isMarginOfVictoryCorrect(match, prediction))
            return correctMarginOfVictory;
        else 
            return correctOutcome;
    }
    else if (didTeamsTied(match) && didPredictTie(prediction) && wasThereAPenaltyShootout(match) === false) {
        if (isPredictionCorrect(match, prediction))
            return correctPrediction;
        else
            return correctOutcome;
    }
    else if (didTeamsTied(match) && wasThereAPenaltyShootout(match)) {
        if (didHomeTeamWinByPenaltyShootout(match) && didPredictHomeTeamWin(prediction)) {
            return correctOutcome;
        }
        if (didAwayTeamWinByPenaltyShootout(match) && didPredictAwayTeamWin(prediction)) {
            return correctOutcome;
        }
    }
    return incorrectPrediction;
};


function isMatchPlayed(match) {
    return match.HomeTeamGoals !== null && match.AwayTeamGoals !== null
        && match.HomeTeamGoals !== -1 && match.AwayTeamGoals !== -1;
}

function didHomeTeamWin(match) {
    return match.HomeTeamGoals > match.AwayTeamGoals;
}

function didAwayTeamWin(match) {
    return match.AwayTeamGoals > match.HomeTeamGoals;
}

function didTeamsTied(match) {
    return match.HomeTeamGoals === match.AwayTeamGoals;
}

function didHomeTeamWinByPenaltyShootout(match) {
    return match.HomeTeamNotes !== null && match.HomeTeamNotes === "p";
}

function didAwayTeamWinByPenaltyShootout(match) {
    return match.AwayTeamNotes !== null && match.AwayTeamNotes === "p";
}

function wasThereAPenaltyShootout(match) {
    return (match.HomeTeamNotes !== null && match.HomeTeamNotes === "p" ||
            (match.AwayTeamNotes !== null && match.AwayTeamNotes === "p"));
}


function isPredictionSet(prediction) {
    return prediction.HomeTeamGoals !== null && prediction.AwayTeamGoals !== null &&
        prediction.HomeTeamGoals !== -1 && prediction.AwayTeamGoals !== -1;
}

function didPredictHomeTeamWin(prediction) {
    return prediction.HomeTeamGoals > prediction.AwayTeamGoals;
}

function didPredictAwayTeamWin(prediction) {
    return prediction.AwayTeamGoals > prediction.HomeTeamGoals;
}

function isPredictionCorrect(match, prediction) {
    return prediction.HomeTeamGoals == match.HomeTeamGoals
            && prediction.AwayTeamGoals == match.AwayTeamGoals;
}

function isMarginOfVictoryCorrect(match, prediction) {
    return prediction.HomeTeamGoals - prediction.AwayTeamGoals ==
            match.HomeTeamGoals - match.AwayTeamGoals;
}

function didPredictTie(prediction) {
    return prediction.HomeTeamGoals == prediction.AwayTeamGoals;
}