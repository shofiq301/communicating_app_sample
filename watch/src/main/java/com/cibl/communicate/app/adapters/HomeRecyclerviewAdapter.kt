package com.cibl.communicate.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cibl.communicate.app.R
import com.cibl.communicate.app.databinding.ListItemBinding
import com.cibl.communicate.app.models.HomeListItem
import com.cibl.communicate.app.util.MyDifUtil

class HomeRecyclerviewAdapter(private val clickListener: (HomeListItem) -> Unit): RecyclerView.Adapter<ViewHolder>() {

    private var homeItemList = emptyList<HomeListItem>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.list_item, parent,false)
        return ViewHolder(binding as ListItemBinding)
    }

    fun setData(newDataList: List<HomeListItem>){
        val diffUtil = MyDifUtil(homeItemList, newDataList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        homeItemList = newDataList
        diffResult.dispatchUpdatesTo(this)
//        homeItemList = newList as ArrayList<HomeListItem>
//        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(homeItemList[position], clickListener)
    }

    override fun getItemCount(): Int {
        return homeItemList.size
    }
}
class ViewHolder(private val item: ListItemBinding): RecyclerView.ViewHolder(item.root){
    fun bind(data: HomeListItem, clickListener: (HomeListItem) -> Unit){
        item.homeItem = data
        item.txtContent.setOnClickListener{
            clickListener(data)
        }
    }
}
