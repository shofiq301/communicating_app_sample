package com.cibl.communicate.app.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.cibl.communicate.app.R
import com.cibl.communicate.app.databinding.ActivityAccountDetailsBinding
import com.cibl.communicate.app.databinding.ActivityWatchHomeBinding
import com.cibl.communicate.app.databinding.ActivityWatchMainBinding
import com.cibl.communicate.app.models.AccountDetailsModel
import com.cibl.communicate.app.models.HomeListItem
import com.cibl.communicate.app.viewModels.AccountDetailsViewModel
import com.cibl.communicate.app.viewModels.DetailsViewModelFactory
import com.cibl.communicate.app.viewModels.HomeViewModel
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.BuildConfig
import com.orhanobut.logger.Logger

class AccountDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountDetailsBinding
    private lateinit var viewModel: AccountDetailsViewModel
    private var details = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.addLogAdapter(object : AndroidLogAdapter() {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })
        binding =DataBindingUtil.setContentView(this, R.layout.activity_account_details)
        val factory = DetailsViewModelFactory()
        viewModel = ViewModelProvider(this, factory).get(AccountDetailsViewModel::class.java)
        details = intent.getStringExtra("details")!!
        Logger.d("RECEIVED_DATA",details)
        viewModel.parseAccountDetails(details)

        viewModel.getAccountDetails().observe(this, Observer {
          initData(accountDetails = it)
        })
    }
    private fun initData(accountDetails: AccountDetailsModel){
        binding.toolbarContent.watchToolbar.title = accountDetails.number.toString()
        binding.toolbarContent.watchToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.TextInfoColor))
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);

        binding.accountDetails = accountDetails

        binding.toolbarContent.watchToolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)
        binding.toolbarContent.watchToolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })

    }
}