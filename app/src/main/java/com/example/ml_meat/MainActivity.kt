package com.example.ml_meat

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.ml_meat.ml.ModelUnquant
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    private val TAG = this::class.java.simpleName
    private lateinit var predictResultTv : TextView
    private lateinit var targetImgView : ImageView
    private lateinit var targetImage : Bitmap
    private lateinit var meatLabelList : List<String>
    private lateinit var pieChart : PieChart

    private var resultMeatCategory = ""//推定結果の肉カテゴリを入れる

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //ボタンなどの初期化
        val selectBtn = findViewById<ImageButton>(R.id.selectImg_btn)
        val cameraBtn = findViewById<ImageButton>(R.id.camera_btn)
        val recipeSearchBtn = findViewById<ImageButton>(R.id.recipeSearch_btn)
        predictResultTv = findViewById(R.id.prediction_tv)
        targetImgView = findViewById(R.id.imageView)
        pieChart = findViewById(R.id.pieChart)

        //初期画面用
        val bmp = BitmapFactory.decodeResource(resources, R.drawable.meat)
        targetImage = Bitmap.createScaledBitmap(bmp, 224, 224,true)
        targetImgView.setImageBitmap(targetImage)
        targetImgView.setImageResource(R.drawable.meat)

        //推論後の正解ラベルの読みこみ
        try {
            meatLabelList = FileUtil.loadLabels(this, "labels.txt")
        }catch (e:IOException){
            Log.d(TAG,"Reading label data is error")
        }

        //ギャラリーから画像をとってくる
        selectBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, READ_REQUEST_CODE)
        }

        //その場で写真をとる
        cameraBtn.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, RESULT_CAMERA)
        }

        //apiから肉にあったメニューをとってくる
        recipeSearchBtn.setOnClickListener{
            if (resultMeatCategory.isBlank()){
                Toast.makeText(applicationContext, "写真をとるか選んでください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val menuListIntent = Intent(this, MenuListActivity::class.java)

            menuListIntent.putExtra("MEAT_CATEGORY",returnCategoryStr(resultMeatCategory))
            //画面遷移を開始
            startActivity(menuListIntent)
        }
    }

    private fun meatPrediction(image : Bitmap){
        //以下tensorflowLiteによる推論
        val model = ModelUnquant.newInstance(this)

        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)

        val tensorImg = TensorImage(DataType.FLOAT32)
        tensorImg.load(image)
        val byteBuffer : ByteBuffer = tensorImg.buffer
        Log.d(TAG,"ByteBuffer : $byteBuffer")
        inputFeature0.loadBuffer(byteBuffer)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        val resultLabel = TensorLabel(meatLabelList, outputFeature0)

        //結果を推定のリスト(確率が高いものがはじめに来るようにソート)
        val predictElmList = resultLabel.mapWithFloatValue.toList().sortedBy { it.second }.reversed()
        if (predictElmList != null) {
            resultMeatCategory = predictElmList[0].first

            if(predictElmList[0].second > 0.6f)
                predictResultTv.text = "${predictElmList[0].first}です"
            else
                predictResultTv.text = "${predictElmList[0].first}かも"

            val entries : ArrayList<PieEntry> = arrayListOf()

            for (elm in predictElmList){
                val elmValue = (elm.second * 1000).roundToInt() /10f
                if(elmValue > 1f && entries.size < 5)
                    entries.add(PieEntry(elmValue, elm.first))
            }
            val set = PieDataSet(entries, "meat")
            set.colors = listOf(Color.rgb(102,139,138), Color.rgb(159,176,131),
                Color.rgb(249, 238,211), Color.rgb(164, 124,100) )
            val data = PieData(set)
            pieChart.data = data
            pieChart.description.text = "画像がどの肉に近いかの確率"
            pieChart.invalidate()
        }
        Log.d(TAG,resultLabel.mapWithFloatValue.toList().toString())
        // Releases model resources if no longer used.
        model.close()
    }

    private fun returnCategoryStr(category: String): String {

        return when (category) {
            "牛ハラミ" -> {
                MeatCategory.GyuHarami.categoryId
            }
            "牛ロース" -> {
                MeatCategory.GyuRose.categoryId
            }
            "牛タン" -> {
                MeatCategory.GyuTongue.categoryId
            }
            "三角バラ" -> {
                MeatCategory.GyuBara.categoryId
            }
            "ササミ" -> {
                MeatCategory.Sasami.categoryId
            }
            "セセリ" -> {
                MeatCategory.Seseri.categoryId
            }
            "砂ぎも" -> {
                MeatCategory.SunaGimo.categoryId
            }
            "鳥レバー" -> {
                MeatCategory.ToriLever.categoryId
            }
            "鳥モモ" -> {
                MeatCategory.ToriMomo.categoryId
            }
            else -> "10-275-1483"
        }
    }

    enum class MeatCategory(val categoryId : String){
        GyuTongue("10-275-1483"),
        GyuHarami("10-275-822"),
        GyuRose("10-275-822"),
        GyuBara("10-275-2134"),
        Sasami("10-277-519"),
        Seseri("10-277-834"),
        SunaGimo("10-277-1489"),
        ToriLever("10-277-1490"),
        ToriMomo("10-277-518")
    }
    companion object {
        private const val READ_REQUEST_CODE: Int = 42
        private const val RESULT_CAMERA: Int = 55
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        when(requestCode){
            // カメラから
            RESULT_CAMERA ->{
                if(resultData?.extras == null){
                    Log.d(TAG,"Camera app was Error or Cancel")
                    return
                }else{
                    val bitmap = resultData.extras?.get("data")
                    val image = Bitmap.createScaledBitmap(bitmap as Bitmap, 224, 224,true)
                    targetImgView.setImageBitmap(image)
                    meatPrediction(image)//画像推定
                    Toast.makeText(this, "とれたよ！！", Toast.LENGTH_LONG).show()

                }
            }

            // ギャラリーから
            READ_REQUEST_CODE ->{
                try {
                    resultData?.data?.also { uri ->
                        val inputStream = contentResolver?.openInputStream(uri)
                        val imageTmp = BitmapFactory.decodeStream(inputStream)
                        val image = Bitmap.createScaledBitmap(imageTmp, 224, 224,true)
                        targetImgView.setImageBitmap(image)
                        meatPrediction(image)//画像推定
                        Toast.makeText(this, "とれたよ！！", Toast.LENGTH_LONG).show()

                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "エラーが発生しました", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

}