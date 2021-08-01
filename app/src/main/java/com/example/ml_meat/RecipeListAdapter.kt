package com.example.ml_meat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide


class RecipeListAdapter(context: Context, private val recipeList: MutableList<MutableMap<String, String>>, private val from: Array<String>): BaseAdapter() {
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    override fun getCount(): Int {
        return  recipeList.size
    }

    override fun getItem(position: Int): Any {
        TODO("Not yet implemented")
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflater.inflate(R.layout.recipe_list_item, parent, false)

        val title : TextView = view.findViewById(R.id.text_title)
        val description : TextView = view.findViewById(R.id.text_description)
        val dishImg : ImageView = view.findViewById(R.id.dish_img)
        title.text = recipeList[position][from[0]]
        description.text = recipeList[position][from[1]]
        Glide.with(view.context).load(recipeList[position][from[2]]).into(dishImg)

        return view
    }
}