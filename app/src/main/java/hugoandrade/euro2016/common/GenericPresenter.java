package hugoandrade.euro2016.common;

import android.util.Log;
import java.lang.ref.WeakReference;

public class GenericPresenter<ProvidedViewOps,
                              RequiredPresenterOps,
                              ProvidedModelOps,
                              ModelType extends ModelOps<RequiredPresenterOps>> {
    /**
     * Debugging tag used by the Android logger.
     */
    protected String TAG = getClass().getSimpleName();

    /**
     * Instance of the operations ("Ops") type.
     */
    private ModelType mOpsInstance;

    /**
     * Used to enable garbage collection.
     */
    private WeakReference<ProvidedViewOps> mView;

    /**
     * Lifecycle hook method that's called when the GenericPresenter is
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
            // Initialize the GenericPresenter fields.
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
     * Initialize the GenericPresenter fields.
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
     * Hook method that's called by the GenericPresenter.View after
     * a runtime configuration change to reset the mView instance.
     *
     * @param view         The currently active GenericPresenter.View.
     */
    public void onConfigurationChange(ProvidedViewOps view) {
        // Reset the mView WeakReference.
        mView = new WeakReference<>(view);
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
