package org.hugoandrade.euro2016.predictor.admin.view.main;

import android.content.Context;
import android.support.v4.app.Fragment;

import org.hugoandrade.euro2016.predictor.admin.common.ContextView;

public abstract class FragmentBase<ParentActivityOps extends MainFragComm.ProvidedMainActivityBaseOps>
        extends Fragment
        implements ContextView {

    protected final String TAG = getClass().getSimpleName();

    protected ParentActivityOps mParentInstance;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            //noinspection unchecked
            mParentInstance = (ParentActivityOps) context;
        }
        catch (ClassCastException e) {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentCommunication.ProvidedParentBaseActivityOps");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mParentInstance = null;
    }

    @Override
    public Context getActivityContext() {
        if (mParentInstance != null)
            return mParentInstance.getActivityContext();
        return null;
    }

    @Override
    public Context getApplicationContext() {
        if (mParentInstance != null)
            return mParentInstance.getApplicationContext();
        return null;
    }

    public void showMessage(String message) {
        if (mParentInstance != null)
            mParentInstance.reportMessage(message);
    }

    protected ParentActivityOps getParentActivity() {
        return mParentInstance;
    }
}
