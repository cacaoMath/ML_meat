package com.example.ml_meat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class RecipeListAdapter(val context: Context, val recipeList: MutableList<MutableMap<String, String>>, val from: Array<String>): BaseAdapter() {
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    override fun getCount(): Int {
        return  recipeList.size
    }

    override fun getItem(position: Int): Any {
        TODO("Not yet implemented")
    }

    override fun getItemId(position: Int): Long {
        TODO("Not yet implemented")
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflater.inflate(R.layout.recipe_list_item, parent, false)

        val title : TextView = view.findViewById(R.id.text_title)
        val description : TextView = view.findViewById(R.id.text_description)
        val dishImg : ImageView
        title.text = recipeList[position][from[0]]
        description.text = recipeList[position][from[1]]

        return view
    }
}