package nl.inholland.myvitality.architecture.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import butterknife.ButterKnife

/**
 * The base fragment activity for handling the base functionalities of a fragment activity
 */
abstract class BaseFragmentActivity<VB : ViewBinding> : FragmentActivity() {

    private var _binding: ViewBinding? = null
    abstract val bindingInflater: (LayoutInflater) -> VB

    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() = _binding as VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingInflater.invoke(layoutInflater)

        setContentView(requireNotNull(_binding).root)
        ButterKnife.bind(this)
    }
}