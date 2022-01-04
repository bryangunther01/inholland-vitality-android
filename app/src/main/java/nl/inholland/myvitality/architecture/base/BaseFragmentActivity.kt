package nl.inholland.myvitality.architecture.base

import butterknife.ButterKnife

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentActivity


abstract class BaseFragmentActivity : FragmentActivity() {

    @LayoutRes
    protected abstract fun layoutResourceId(): Int
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResourceId())
        ButterKnife.bind(this)
    }
}