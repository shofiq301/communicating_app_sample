package com.cibl.communicate.app.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cibl.communicate.app.models.HomeListItem
import org.json.JSONArray

class HomeViewModel: ViewModel(){
    lateinit var homeListItems: MutableLiveData<List<HomeListItem>>
    init {
        homeListItems = MutableLiveData()
    }

    fun getCurrentData(): LiveData<List<HomeListItem>>{
        return  homeListItems
    }

    fun parseUserList(content: String) {
        val jsonArray = JSONArray(content)
        val list: ArrayList<HomeListItem> = ArrayList()
        for (i in 0 until jsonArray.length()){
            val jsonObj = jsonArray.getJSONObject(i)
            val pName = jsonObj.getString("acNumber")
            val pId = jsonObj.getInt("acId")
            val item: HomeListItem = HomeListItem(id = pId, name = pName)
            list.add(item)
        }
        homeListItems.postValue(list)
    }
}