package org.hugoandrade.euro2016.predictor.view.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        ((TextView) view.findViewById(R.id.tv_rule_correct_prediction)).setText(" - "
                + getString(R.string.correct_prediction) + ": "
                + rules.getRuleCorrectPrediction() + " "
                + getString(rules.getRuleCorrectPrediction() != 1 ? R.string.points : R.string.point));
        ((TextView) view.findViewById(R.id.tv_rule_correct_outcome)).setText(" - "
                + getString(R.string.correct_outcome) + ": " +
                rules.getRuleCorrectOutcome() + " "
                + getString(rules.getRuleCorrectPrediction() != 1 ? R.string.points : R.string.point));
        ((TextView) view.findViewById(R.id.tv_rule_correct_margin_of_victory)).setText(" - "
                + getString(R.string.correct_margin)+ ": " +
                + rules.getRuleCorrectMarginOfVictory()  + " "
                + getString(rules.getRuleCorrectPrediction() != 1 ? R.string.points : R.string.point));
        ((TextView) view.findViewById(R.id.tv_rule_incorrect_prediction)).setText(" - "
                + getString(R.string.incorrect_prediction) + ": " +
                rules.getRuleIncorrectPrediction()  + " "
                + getString(rules.getRuleCorrectPrediction() != 1 ? R.string.points : R.string.point));
        view.findViewById(R.id.tv_rule_incorrect_prediction).setVisibility(View.GONE);

    }
}
