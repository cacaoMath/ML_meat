package com.example.ml_meat

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MenuListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_list)

//        val listviewMenu = findViewById<ListView>(R.id.ListViewMenu)
//        var menuList = mutableListOf("abc","def","cnn")
//        listviewMenu.adapter = ArrayAdapter(applicationContext,android.R.layout.simple_list_item_1, menuList)
//        listviewMenu.onItemClickListener = ListItemClickListener()

        val receiver = RecipeReceiver()
        val meatCategory = intent.getStringExtra("MEAT_CATEGORY")
        receiver.execute(meatCategory)
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
            val item = parent?.getItemAtPosition(position) as String

            val show = "this is $item"

            Toast.makeText(applicationContext, show, Toast.LENGTH_LONG).show()
        }

    }
    private inner class RecipeReceiver(): AsyncTask<String, String, String>(){
        override fun doInBackground(vararg params: String?): String {
            val category = params[0]
            val urlStr = "https://app.rakuten.co.jp/services/api/Recipe/CategoryRanking/20170426?applicationId=1079183511722986325&categoryId=${category}"

            val url = URL(urlStr)
            val con = url.openConnection() as HttpsURLConnection
            con.requestMethod = "GET"
            con.connect()

            val stream = con.inputStream
            val result = is2String(stream)
            con.disconnect()
            stream.close()

            return result;
        }

        override fun onPostExecute(result: String?) {
            val rootJSON = JSONObject(result)
            val resultJSON = rootJSON.getJSONArray("result")

            val listviewMenu = findViewById<ListView>(R.id.recipe_lv)
            val recipeList: MutableList<MutableMap<String, String>> = mutableListOf()

            for(i in 0 until resultJSON.length()){
                val oneRecipeInfo = resultJSON.getJSONObject(i)
                val recipeTitle = oneRecipeInfo.getString("recipeTitle")
                val recipeDescription = oneRecipeInfo.getString("recipeDescription")

                val info = mutableMapOf("recipeTitle" to recipeTitle, "recipeDescription" to recipeDescription)
                recipeList.add(info)
            }

            val from = arrayOf("recipeTitle", "recipeDescription")
            val to = intArrayOf(android.R.id.text1, android.R.id.text2)

            val adapter = SimpleAdapter(applicationContext, recipeList,
                android.R.layout.simple_list_item_2, from, to)
            listviewMenu.adapter = adapter
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



