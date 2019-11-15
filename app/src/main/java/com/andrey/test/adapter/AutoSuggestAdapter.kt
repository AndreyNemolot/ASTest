package com.andrey.test.adapter

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.annotation.NonNull
import androidx.annotation.Nullable


class AutoSuggestAdapter(@NonNull context: Context, resource: Int) : ArrayAdapter<Any>(context, resource),
    Filterable {
    val mlistData: MutableList<Any>

    init {
        mlistData = ArrayList()
    }

    fun setData(response: List<Any>) {
        mlistData.clear()
        mlistData.addAll(response)
    }

    override fun getCount(): Int {
        return mlistData.size
    }

    @Nullable
    override fun getItem(position: Int): Any? {
        return mlistData[position]
    }
    
    fun getObject(position: Int): Any {
        return mlistData[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null) {
                    filterResults.values = mlistData
                    filterResults.count = mlistData.size
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

}