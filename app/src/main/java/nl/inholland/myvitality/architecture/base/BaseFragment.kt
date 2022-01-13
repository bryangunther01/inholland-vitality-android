package nl.inholland.myvitality.architecture.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.Unbinder

abstract class BaseFragment : Fragment() {

    private var unbinder: Unbinder? = null
    var baseActivity: AppCompatActivity? = null
        private set

    @LayoutRes
    protected abstract fun layoutResourceId(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(layoutResourceId(), container, false)
        unbinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseActivity = context as AppCompatActivity
    }

    override fun onDetach() {
        super.onDetach()
        baseActivity = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (unbinder != null) {
            unbinder!!.unbind()
            unbinder = null
        }
    }
}