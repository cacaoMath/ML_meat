package com.example.ml_meat

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ml_meat.ml.ModelUnquant
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer


class MainActivity : AppCompatActivity() {
    private val TAG = this::class.java.simpleName
    private lateinit var selectBtn :Button
    private lateinit var predictBtn : Button
    private lateinit var cameraBtn : Button
    private lateinit var predictTv : TextView
    private lateinit var imgView : ImageView
    private lateinit var image : Bitmap
    private lateinit var results : List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //ボタンなどの初期化
        selectBtn = findViewById(R.id.selectImg_btn)
        predictBtn = findViewById(R.id.predict_btn)
        cameraBtn = findViewById(R.id.camera_btn)
        predictTv = findViewById(R.id.predection_tv)
        imgView = findViewById(R.id.imageView)

        //初期画面用
        val bmp = BitmapFactory.decodeResource(resources, R.drawable.meat)
        image = Bitmap.createScaledBitmap(bmp, 224, 224,true)
        imgView.setImageBitmap(image)
        imgView.setImageResource(R.drawable.meat)

        //推論後の正解ラベルの読みこみ
        try {
            results = FileUtil.loadLabels(this, "labels.txt")
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

        //表示された画像から推定をする
        predictBtn.setOnClickListener {
            //以下tensorflowLiteによる推論
            val model = ModelUnquant.newInstance(this)

            // Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)

            val tensorImg = TensorImage(DataType.FLOAT32)
            tensorImg.load(image)
            val byteBuffer : ByteBuffer = tensorImg.buffer
            inputFeature0.loadBuffer(byteBuffer)

            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

            val resultLabel = TensorLabel(results, outputFeature0)

            //結果の表示
            predictTv.text = resultLabel.mapWithFloatValue.toList().maxByOrNull { it.second }.toString()
            Log.d(TAG,resultLabel.mapWithFloatValue.toList().toString())
            // Releases model resources if no longer used.
            model.close()
        }

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
                    val bitmap = resultData?.extras?.get("data")
                    image = Bitmap.createScaledBitmap(bitmap as Bitmap, 224, 224,true)
                    imgView.setImageBitmap(image)
                }
                Toast.makeText(this, "とれたよ！！", Toast.LENGTH_LONG).show()
            }

            // ギャラリーから
            READ_REQUEST_CODE ->{
                try {
                    resultData?.data?.also { uri ->
                        val inputStream = contentResolver?.openInputStream(uri)
                        val image2 = BitmapFactory.decodeStream(inputStream)
                        image = Bitmap.createScaledBitmap(image2, 224, 224,true)
                        imgView.setImageBitmap(image)
                    }
                    Toast.makeText(this, "とれたよ！！", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "エラーが発生しました", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

}