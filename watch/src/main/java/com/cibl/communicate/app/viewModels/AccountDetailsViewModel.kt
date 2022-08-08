package com.cibl.communicate.app.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cibl.communicate.app.models.AccountDetailsModel
import com.cibl.communicate.app.models.HomeListItem
import org.json.JSONArray
import org.json.JSONObject

class AccountDetailsViewModel: ViewModel() {
    var accountDetails: MutableLiveData<AccountDetailsModel> = MutableLiveData()
    fun getAccountDetails(): LiveData<AccountDetailsModel> {
        return accountDetails
    }

    fun parseAccountDetails(content: String) {
        val jsonObject = JSONObject(content)
        val acNumber = jsonObject.getInt("number")
        val acName = jsonObject.getString("name")
        val acBalance = jsonObject.getInt("balance")
        val acStatus = jsonObject.getString("status")
        val acId = jsonObject.getInt("id")
        val acDetails = AccountDetailsModel(id = acId, name =  acName, number = acNumber, status = acStatus, balance = acBalance)
        accountDetails.postValue(acDetails)
    }
}