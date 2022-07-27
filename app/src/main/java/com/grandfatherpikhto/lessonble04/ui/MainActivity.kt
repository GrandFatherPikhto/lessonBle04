package com.grandfatherpikhto.lessonble04.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.MenuProvider
import androidx.lifecycle.lifecycleScope
import com.grandfatherpikhto.blin.BleManager
import com.grandfatherpikhto.blin.BleManagerInterface
import com.grandfatherpikhto.blin.FakeBleManager
import com.grandfatherpikhto.blin.permissions.RequestPermissions
import com.grandfatherpikhto.lessonble04.LessonBle04App
import com.grandfatherpikhto.lessonble04.R
import com.grandfatherpikhto.lessonble04.databinding.ActivityMainBinding
import com.grandfatherpikhto.lessonble04.helper.linkMenu
import com.grandfatherpikhto.lessonble04.models.MainActivityViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val logTag = this.javaClass.simpleName

    private val _bleManager : BleManager? by lazy {
        BleManager(applicationContext).let {
            (application as LessonBle04App).bleManager = it
            it
        }
    }
//    private val _bleManager : BleManagerInterface? by lazy {
//        FakeBleManager().let {
//            (application as LessonBle04App).bleManager = it
//            it
//        }
//
//    }
    private val bleManager get() = _bleManager!!

    private val mainActivityViewModel by viewModels<MainActivityViewModel>()

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_main, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.action_settings -> true
                else -> { false }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(bleManager)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        requestPermissions()
        linkMenu(true, menuProvider)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        linkMenu(false, menuProvider)
        super.onDestroy()
    }

    private fun requestPermissions() {
        val requestPermissions = RequestPermissions(this)
        requestPermissions.requestPermissions(listOf(
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION",
        ))

        lifecycleScope.launch {
            requestPermissions.stateFlowRequestPermission.filterNotNull().collect { permission ->
                if (permission.granted) {
                    Toast.makeText(baseContext, getString(R.string.permission_granted, permission.permission), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(baseContext, getString(R.string.permission_not_granted, permission.permission), Toast.LENGTH_SHORT).show()
                    finishAndRemoveTask()
                    exitProcess(0)
                }
            }
        }
    }
}