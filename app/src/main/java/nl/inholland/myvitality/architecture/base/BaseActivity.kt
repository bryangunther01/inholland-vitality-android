package nl.inholland.myvitality.architecture.base

import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife

import android.os.Bundle
import androidx.annotation.LayoutRes
import dagger.android.support.DaggerAppCompatActivity

import androidx.lifecycle.ViewModel


abstract class BaseActivity : AppCompatActivity() {

    @LayoutRes
    protected abstract fun layoutResourceId(): Int
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResourceId())
        ButterKnife.bind(this)
    }
}