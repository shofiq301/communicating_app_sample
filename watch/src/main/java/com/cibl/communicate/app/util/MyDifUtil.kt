package com.cibl.communicate.app.util

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import com.cibl.communicate.app.models.HomeListItem

class MyDifUtil(private val oldList: List<HomeListItem>, private val newList: List<HomeListItem>): DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return  when {
            oldList[oldItemPosition].id != newList[newItemPosition].id -> {
                false
            }
            oldList[oldItemPosition].name != newList[newItemPosition].name -> {
                false
            }
            else -> {
                true
            }
        }
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val diffBundle = Bundle()
        diffBundle.putInt(oldList[oldItemPosition].id.toString(), newList[newItemPosition].id)
        return diffBundle
    }
}