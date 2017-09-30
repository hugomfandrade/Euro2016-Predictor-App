package org.hugoandrade.euro2016.backend.presenter;

import android.util.Log;

import java.lang.ref.WeakReference;

import org.hugoandrade.euro2016.backend.common.ModelOps;

public class PresenterBase<ProvidedViewOps,
                              RequiredPresenterOps,
                              ProvidedModelOps,
                              ModelType extends ModelOps<RequiredPresenterOps>> {

    protected String TAG = getClass().getSimpleName();

    private ModelType mOpsInstance;

    private WeakReference<ProvidedViewOps> mView;
    /**
     * Lifecycle hook method that's called when the GenericPresenteris
     * created.
     *
     * @param opsType
     *            Class object that's used to create an model
     *            object.
     * @param presenter
     *            Reference to the RequiredPresenterOps in the Presenter layer.
     */
    public void onCreate(Class<ModelType> opsType,
                         ProvidedViewOps view,
                         RequiredPresenterOps presenter) {
        mView = new WeakReference<>(view);

        try {
            // Initialize the PresenterBase fields.
            initialize(opsType,
                    presenter);
        } catch (Exception e) {
            Log.d(TAG,
                    "handleConfiguration "
                            + e);
            // Propagate this as a runtime exception.
            throw new RuntimeException(e);
        }
    }

    /**
     * Initialize the PresenterBase fields.
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void initialize(Class<ModelType> opsType,
                            RequiredPresenterOps presenter)
            throws InstantiationException, IllegalAccessException {
        // Create the ModelType object.
        mOpsInstance = opsType.newInstance();

        // Perform the first initialization.
        mOpsInstance.onCreate(presenter);
    }

    /**
     * Return the initialized ProvidedModelOps instance for use by the
     * application.
     */
    @SuppressWarnings("unchecked")
    public ProvidedModelOps getModel() {
        return (ProvidedModelOps) mOpsInstance;
    }

    /**
     * Return the ProvidedViewOps instance for use by the
     * application.
     */
    public ProvidedViewOps getView() {
        return mView.get();
    }
}
