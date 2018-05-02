package org.hugoandrade.euro2016.predictor.view.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.FragComm;
import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.raw.SystemData;

public class RulesFragment extends FragmentBase<FragComm.RequiredActivityBaseOps> {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_rules, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        SystemData.Rules rules = GlobalData.getInstance().systemData.getRules();
        ((TextView) view.findViewById(R.id.tv_rule_correct_prediction)).setText(
                " - Correct prediction: " + rules.getRuleCorrectPrediction() + " point" +
                        (rules.getRuleCorrectPrediction() != 1? "s" : ""));
        ((TextView) view.findViewById(R.id.tv_rule_correct_outcome)).setText(
                " - Correct outcome: " + rules.getRuleCorrectOutcome() + " point" +
                        (rules.getRuleCorrectOutcome() != 1? "s" : ""));
        ((TextView) view.findViewById(R.id.tv_rule_correct_margin_of_victory)).setText(
                " - Correct margin of victory: " + rules.getRuleCorrectMarginOfVictory() + " point" +
                        (rules.getRuleCorrectMarginOfVictory() != 1? "s" : ""));
        ((TextView) view.findViewById(R.id.tv_rule_incorrect_prediction)).setText(
                " - Incorrect prediction: " + rules.getRuleIncorrectPrediction() + " point" +
                        (rules.getRuleIncorrectPrediction() != 1? "s" : ""));
        view.findViewById(R.id.tv_rule_incorrect_prediction).setVisibility(View.GONE);

    }
}
