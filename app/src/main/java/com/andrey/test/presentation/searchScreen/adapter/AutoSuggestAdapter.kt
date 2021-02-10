package com.andrey.test.presentation.searchScreen.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.annotation.Nullable
import com.andrey.test.domain.model.City
import com.andrey.test.presentation.textOrGone

class AutoSuggestAdapter(context: Context, private val resource: Int) :
    ArrayAdapter<City>(context, resource), Filterable {
    val dataList: MutableList<City>

    init {
        dataList = ArrayList()
    }

    fun setData(response: List<City>) {
        dataList.clear()
        dataList.addAll(response)
    }

    override fun getCount(): Int {
        return dataList.size
    }

    @Nullable
    override fun getItem(position: Int): City {
        return dataList[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val convertView = inflater.inflate(resource, parent, false) as TextView
        convertView.textOrGone = dataList[position].latinFullName
        return convertView
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as City).latinCityName ?: ""
            }

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null) {
                    filterResults.values = dataList
                    filterResults.count = dataList.size
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