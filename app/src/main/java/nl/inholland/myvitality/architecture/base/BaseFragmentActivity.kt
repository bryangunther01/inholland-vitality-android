package nl.inholland.myvitality.architecture.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import butterknife.ButterKnife


abstract class BaseFragmentActivity : FragmentActivity() {

    @LayoutRes
    protected abstract fun layoutResourceId(): Int
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResourceId())
        ButterKnife.bind(this)
    }
}