package com.bit.bilikdigitalkarawang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.bit.bilikdigitalkarawang.core.Navigation
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import com.bit.bilikdigitalkarawang.ui.theme.BilikDigitalTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var dataStoreDiv: DataStoreDiv

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BilikDigitalTheme {
                val navController = rememberNavController()
                Navigation(navController, dataStoreDiv)
            }
        }
    }
}