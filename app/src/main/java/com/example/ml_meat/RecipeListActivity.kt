package com.example.ml_meat

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MenuListActivity : ScopedAppActivity() {
    private lateinit var listviewMenu : ListView
    private val recipeList: MutableList<MutableMap<String, String>> = mutableListOf()
    private lateinit var adapter: RecipeListAdapter
    private val from = arrayOf("recipeTitle", "recipeDescription", "recipeImg")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_list)

        listviewMenu = findViewById(R.id.recipe_lv)
        adapter = RecipeListAdapter(applicationContext, recipeList, from)
        listviewMenu.onItemClickListener = ListItemClickListener()


        launch {
            val bgTask = async (Dispatchers.IO){
                val category = intent.getStringExtra("MEAT_CATEGORY")
                val urlStr = "https://app.rakuten.co.jp/services/api/Recipe/CategoryRanking/20170426?applicationId=1079183511722986325&categoryId=${category}"

                val url = URL(urlStr)
                val con = url.openConnection() as HttpsURLConnection
                con.requestMethod = "GET"
                con.connect()

                val stream = con.inputStream
                val result = is2String(stream)
                con.disconnect()
                stream.close()

                result
            }
            withContext(Dispatchers.Main){
                val rootJSON = JSONObject(bgTask.await())
                val resultJSON = rootJSON.getJSONArray("result")

                for(i in 0 until resultJSON.length()){
                    val oneRecipeInfo = resultJSON.getJSONObject(i)
                    val recipeTitle = oneRecipeInfo.getString("recipeTitle")
                    val recipeDescription = oneRecipeInfo.getString("recipeDescription")
                    val recipeImg = oneRecipeInfo.getString("smallImageUrl")
                    val recipeUrl = oneRecipeInfo.getString("recipeUrl")

                    val info = mutableMapOf("recipeTitle" to recipeTitle,
                        "recipeDescription" to recipeDescription,
                        "recipeImg" to recipeImg,
                        "recipeUrl" to recipeUrl)
                    recipeList.add(info)
                }


                adapter = RecipeListAdapter(applicationContext, recipeList, from)
                listviewMenu.adapter = adapter
            }
        }
    }

    //楽天のapi用
    //list
    //https://app.rakuten.co.jp/services/api/Recipe/CategoryList/20170426?
    //        applicationId=1079183511722986325&
    //        categoryType=large
    //ranking
    //https://app.rakuten.co.jp/services/api/Recipe/CategoryRanking/20170426?applicationId=1079183511722986325&categoryId="10-275-1483"

    //api詳細
    //https://webservice.rakuten.co.jp/api/recipecategoryranking/
    private inner class ListItemClickListener : AdapterView.OnItemClickListener{
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//            val item = parent?.getItemAtPosition(position) as String
//
//            val show = "this is $item"

            Toast.makeText(applicationContext, "$position", Toast.LENGTH_LONG).show()
        }

    }

    private fun is2String(stream: InputStream): String{
        val sb = StringBuilder()
        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        var line = reader.readLine()
        while(line != null){
            sb.append(line)
            line = reader.readLine()
        }
        reader.close()
        return sb.toString()
    }
}



