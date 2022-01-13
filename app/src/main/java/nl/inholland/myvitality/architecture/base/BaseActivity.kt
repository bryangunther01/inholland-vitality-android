package nl.inholland.myvitality.architecture.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife


abstract class BaseActivity : AppCompatActivity() {

    @LayoutRes
    protected abstract fun layoutResourceId(): Int
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResourceId())
        ButterKnife.bind(this)
    }
}