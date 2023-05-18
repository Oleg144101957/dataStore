package com.adobe.datastore

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.adobe.datastore.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settingsDataStore")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.setOnClickListener {
            lifecycleScope.launch {
                save(
                    binding.keyET.text.toString(),
                    binding.valueET.text.toString()
                )
            }
        }

        binding.btnRead.setOnClickListener {
            lifecycleScope.launch {
                val value = read(binding.keyToGetET.text.toString())
                binding.tvField.text = value ?: "No value found"
            }
        }

        lifecycleScope.launch {
            myFlowPushingDataToUi().collect{value ->
                binding.tvField.text = value
            }
        }


    }

    private suspend fun save(key: String, value: String){
        val dataStoreKey = stringPreferencesKey(key)
        dataStore.edit {settings ->
            settings[dataStoreKey] = value
        }
    }

    private suspend fun read(key: String): String? {
        val dataStoreKey = stringPreferencesKey(key)
        val preferences = dataStore.data.first()
        return preferences[dataStoreKey]
    }

    private fun myFlowPushingDataToUi(): Flow<String>{
        val dataStoreKey = stringPreferencesKey("hello")
        return dataStore.data.catch {exception ->
            if (exception is IOException){
                emit(emptyPreferences())
            } else{
                throw exception
            }
        }.map {pref ->
            val name = pref[dataStoreKey] ?: "no data"
            name
        }
    }

}