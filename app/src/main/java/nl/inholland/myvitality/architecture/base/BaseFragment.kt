package nl.inholland.myvitality.architecture.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import butterknife.ButterKnife
import butterknife.Unbinder

/**
 * The base fragment for handling the base functionalities of a fragment
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: ViewBinding? = null
    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB

    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() = _binding as VB

    private var unbinder: Unbinder? = null
    var baseActivity: AppCompatActivity? = null
        private set

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = bindingInflater.invoke(inflater, container, false)
        unbinder = ButterKnife.bind(this, requireNotNull(_binding).root)

        return requireNotNull(_binding).root
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