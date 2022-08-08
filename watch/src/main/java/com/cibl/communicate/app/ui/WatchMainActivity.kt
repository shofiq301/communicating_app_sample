package com.cibl.communicate.app.ui
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.wear.ambient.AmbientModeSupport
import androidx.wear.widget.WearableLinearLayoutManager
import com.cibl.communicate.app.R
import com.cibl.communicate.app.databinding.ActivityWatchMainBinding
import com.cibl.communicate.app.models.HomeListItem
import com.cibl.communicate.app.viewModels.HomeViewModel
import com.cibl.communicate.app.viewModels.HomeViewModelFactory
import com.google.android.gms.wearable.*
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.BuildConfig
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class WatchMainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private lateinit var binding: ActivityWatchMainBinding
    private lateinit var viewModel: HomeViewModel

    private var activityContext: Context? = null
    private var event = ""
    private var eventTitle = ""
    private var eventId = -1
    private lateinit var homeAdapter: HomeRecyclerviewAdapter

    companion object{
        const val ACCOUNT_LIST_REQUEST_CODE = 1001
    }
    private lateinit var ambientController: AmbientModeSupport.AmbientController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.addLogAdapter(object : AndroidLogAdapter() {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })
        binding = DataBindingUtil.setContentView(this, R.layout.activity_watch_main)
        val factory = HomeViewModelFactory()
        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        setContentView(binding.root)

        activityContext = this

        // Enables Always-on
        ambientController = AmbientModeSupport.attach(this)

        viewModel.homeListItems.observe(this) { homeList ->
            launch {
                homeAdapter.setData(homeList)
            }
        }

        //On click listener for sendmessage button
       event = intent.getStringExtra("event")!!
       eventTitle = intent.getStringExtra("eventTitle")!!
       eventId = intent.getIntExtra("eventId", -1)
        if (eventId == 1){
            viewModel.parseUserList(event)
        }
        initData()
    }
    private fun initData() {
        binding.toolbarContent.watchToolbar.title = eventTitle
        binding.toolbarContent.watchToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.TextInfoColor))
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);
        homeAdapter = HomeRecyclerviewAdapter { selectedItem: HomeListItem ->
            listItemCLicked(selectedItem)
        }
        binding.receivedList.apply {
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(this@WatchMainActivity)
            adapter = homeAdapter
        }

        binding.toolbarContent.watchToolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)
        binding.toolbarContent.watchToolbar.setNavigationOnClickListener(View.OnClickListener {
           finish()
        })
    }
    private fun listItemCLicked(item: HomeListItem){
        val intent = Intent()
        intent.putExtra("event3", item.id)
        setResult(ACCOUNT_LIST_REQUEST_CODE, intent)
        this.onBackPressed()
    }


}